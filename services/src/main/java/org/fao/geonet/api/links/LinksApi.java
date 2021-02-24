//=============================================================================
//===   Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.api.links;

import com.google.common.collect.Sets;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Link;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.exceptions.OperationNotAllowedEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.url.UrlAnalyzer;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.specification.LinkSpecs;
import org.jdom.JDOMException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.naming.SelfNaming;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.PostConstruct;
import javax.management.MalformedObjectNameException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Set;

import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION;

@EnableWebMvc
@Service
@RestController
@RequestMapping(value = {
    "/{portal}/api/records/links"
})
@Tag(name = "links",
    description = "Record link operations")
public class LinksApi {
    private static final int NUMBER_OF_SUBSEQUENT_PROCESS_MBEAN_TO_KEEP = 5;
    @Autowired
    protected ApplicationContext appContext;
    @Autowired
    LinkRepository linkRepository;
    @Autowired
    IMetadataUtils metadataUtils;
    @Autowired
    MetadataRepository metadataRepository;
    @Autowired
    DataManager dataManager;
    @Autowired
    UrlAnalyzer urlAnalyser;
    @Autowired
    MBeanExporter mBeanExporter;
    @Autowired
    AccessManager accessManager;

    private ArrayDeque<SelfNaming> mAnalyseProcesses = new ArrayDeque<>(NUMBER_OF_SUBSEQUENT_PROCESS_MBEAN_TO_KEEP);

    @PostConstruct
    public void iniMBeansSlidingWindowWithEmptySlot() {
        for (int i = 0; i < NUMBER_OF_SUBSEQUENT_PROCESS_MBEAN_TO_KEEP; i++) {
            EmptySlot emptySlot = new EmptySlot(i);
            mAnalyseProcesses.addFirst(emptySlot);
            try {
                mBeanExporter.registerManagedResource(emptySlot, emptySlot.getObjectName());
            } catch (MalformedObjectNameException e) {
                e.printStackTrace();
            }
        }
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get record links",
        description = "")
    @Parameters({
        @Parameter(name = "page",
            in = ParameterIn.QUERY, schema = @Schema(type = "integer", format = "int32"),
            description = "Results page you want to retrieve (0..N)"),
        @Parameter(name = "size",
            in = ParameterIn.QUERY, schema = @Schema(type = "integer", format = "int32"),
            description = "Number of records per page."),
        @Parameter(name = "sort",
            //allowMultiple = false
            in = ParameterIn.QUERY, schema = @Schema(type = "string"),
            description = "Sorting criteria in the format: property(,asc|desc). " +
                "Default sort order is ascending. ")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public Page<Link> getRecordLinks(
        @Parameter(description = "Filter, e.g. \"{url: 'png', lastState: 'ko', records: 'e421', groupId: 12}\", lastState being 'ok'/'ko'/'unknown'", required = false)
        @RequestParam(required = false)
            JSONObject filter,
        @Parameter(description = "Optional, filter links to records published in that group.", required = false)
        @RequestParam(required = false)
            Integer[] groupIdFilter,
        @Parameter(description = "Optional, filter links to records created in that group.", required = false)
        @RequestParam(required = false)
            Integer[] groupOwnerIdFilter,
        @Parameter(hidden = true)
            Pageable pageRequest,
        @Parameter(hidden = true)
            HttpSession session,
        @Parameter(hidden = true)
            HttpServletRequest request) throws Exception {

        final UserSession userSession = ApiUtils.getUserSession(session);
        return getLinks(filter, groupIdFilter, groupOwnerIdFilter, pageRequest, userSession);
    }

    private Page<Link> getLinks(
        JSONObject filter,
        Integer[] groupIdFilter,
        Integer[] groupOwnerIdFilter,
        Pageable pageRequest,
        UserSession userSession) throws SQLException, JSONException {
        Integer[] editingGroups = null;
        if (userSession.getProfile() != Profile.Administrator) {
            final List<Integer> editingGroupList = AccessManager.getGroups(userSession, Profile.Editor);
            if (editingGroupList.size() > 0) {
                editingGroups = editingGroupList.toArray(new Integer[editingGroupList.size()]);
            }
        }

        if (filter == null && (groupIdFilter != null || groupOwnerIdFilter != null || editingGroups != null)) {
            return linkRepository.findAll(LinkSpecs.filter(null, null, null, groupIdFilter, groupOwnerIdFilter, editingGroups), pageRequest);
        }

        if (filter != null) {
            Integer stateToMatch = null;
            String url = null;
            String associatedRecord = null;
            if (filter.has("lastState")) {
                stateToMatch = 0;
                if (filter.getString("lastState").equalsIgnoreCase("ok")) {
                    stateToMatch = 1;
                } else if (filter.getString("lastState").equalsIgnoreCase("ko")) {
                    stateToMatch = -1;
                }
            }

            if (filter.has("url")) {
                url = filter.getString("url");
            }

            if (filter.has("records")) {
                associatedRecord = filter.getString("records");
            }

            return linkRepository.findAll(LinkSpecs.filter(url, stateToMatch, associatedRecord, groupIdFilter, groupOwnerIdFilter, editingGroups), pageRequest);
        } else {
            return linkRepository.findAll(pageRequest);
        }
    }


    @Operation(
        description = "Get record links as CSV")
    @Parameters({
        @Parameter(name = "page",
            in = ParameterIn.QUERY, schema = @Schema(type = "integer", format = "int32"),
            description = "Results page you want to retrieve (0..N)"),
        @Parameter(name = "size",
            in = ParameterIn.QUERY, schema = @Schema(type = "integer", format = "int32"),
            description = "Number of records per page."),
        @Parameter(name = "sort",
            //allowMultiple = false
            in = ParameterIn.QUERY, schema = @Schema(type = "string"),
            description = "Sorting criteria in the format: property(,asc|desc). " +
                "Default sort order is ascending. ")
    })
    @RequestMapping(
        path = "/csv",
        method = RequestMethod.GET,
        produces = MediaType.TEXT_PLAIN_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public void getRecordLinksAsCsv(
        @Parameter(description = "Filter, e.g. \"{url: 'png', lastState: 'ko', records: 'e421', groupId: 12}\", lastState being 'ok'/'ko'/'unknown'", required = false)
        @RequestParam(required = false)
            JSONObject filter,
        @Parameter(description = "Optional, filter links to records published in that group.", required = false)
        @RequestParam(required = false)
            Integer[] groupIdFilter,
        @Parameter(description = "Optional, filter links to records created in that group.", required = false)
        @RequestParam(required = false)
            Integer[] groupOwnerIdFilter,
        @Parameter(hidden = true)
            Pageable pageRequest,
        @Parameter(hidden = true)
            HttpSession session,
        @Parameter(hidden = true)
            HttpServletResponse response) throws Exception {
        final UserSession userSession = ApiUtils.getUserSession(session);

        final Page<Link> links = getLinks(filter, groupIdFilter, groupOwnerIdFilter, pageRequest, userSession);
        response.setHeader("Content-disposition", "attachment; filename=links.csv");
        LinkAnalysisReport.create(links, response.getWriter());
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Analyze records links",
        description = "One of uuids or bucket parameter is required if not an Administrator. Only records that you can edit will be validated.")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public SimpleMetadataProcessingReport analyzeRecordLinks(
        @Parameter(description = API_PARAM_RECORD_UUIDS_OR_SELECTION)
        @RequestParam(required = false)
            String[] uuids,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @Parameter(
            description = "Only allowed if Administrator."
        )
        @RequestParam(
            required = false,
            defaultValue = "true")
            boolean removeFirst,
        @RequestParam(
            required = false,
            defaultValue = "false")
            boolean analyze,
        @Parameter(hidden = true)
            HttpSession httpSession,
        @Parameter(hidden = true)
            HttpServletRequest request
    ) throws IOException, JDOMException {
        MAnalyseProcess registredMAnalyseProcess = getRegistredMAnalyseProcess();

        ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        UserSession session = ApiUtils.getUserSession(httpSession);

        boolean isAdministrator = session.getProfile() == Profile.Administrator;
        if (isAdministrator && removeFirst) {
            registredMAnalyseProcess.deleteAll();
        }

        SimpleMetadataProcessingReport report =
            new SimpleMetadataProcessingReport();

        Set<Integer> ids = Sets.newHashSet();

        if (uuids != null || StringUtils.isNotEmpty(bucket)) {
            try {
                Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, session);
                for (String uuid : records) {
                    if (!metadataUtils.existsMetadataUuid(uuid)) {
                        report.incrementNullRecords();
                    }
                    for (AbstractMetadata record : metadataRepository.findAllByUuid(uuid)) {
                        if (!accessManager.canEdit(serviceContext, String.valueOf(record.getId()))) {
                            report.addNotEditableMetadataId(record.getId());
                        } else {
                            ids.add(record.getId());
                            report.addMetadataId(record.getId());
                            report.incrementProcessedRecords();
                        }
                    }
                }
            } catch (Exception e) {
                report.addError(e);
            } finally {
                report.close();
            }
        } else {
            if (isAdministrator) {
                // Process all
                final List<Metadata> metadataList = metadataRepository.findAll();
                for (Metadata m : metadataList) {
                    ids.add(m.getId());
                    report.addMetadataId(m.getId());
                    report.incrementProcessedRecords();
                }
            } else {
                throw new OperationNotAllowedEx(String.format(
                    "Only administrator can trigger link analysis on the entire catalogue. This is not allowed for %s.",
                    session.getProfile()
                ));
            }
            report.close();
        }

        registredMAnalyseProcess.processMetadataAndTestLink(analyze, ids);
        return report;
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Remove all links and status history",
        description = "")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseBody
    public ResponseEntity purgeAll() throws IOException, JDOMException {
        urlAnalyser.deleteAll();
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    private MAnalyseProcess getRegistredMAnalyseProcess() {
        MAnalyseProcess mAnalyseProcess = new MAnalyseProcess(linkRepository, metadataRepository, urlAnalyser, appContext);
        mBeanExporter.registerManagedResource(mAnalyseProcess, mAnalyseProcess.getObjectName());
        try {
            mBeanExporter.unregisterManagedResource(mAnalyseProcesses.removeLast().getObjectName());
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
        mAnalyseProcesses.addFirst(mAnalyseProcess);
        return mAnalyseProcess;
    }
}
