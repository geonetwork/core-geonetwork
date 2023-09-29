/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.processing.report.MetadataReplacementProcessingReport;
import org.fao.geonet.api.processing.report.XsltMetadataProcessingReport;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.events.history.RecordProcessingChangeEvent;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MetadataIndexerProcessor;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.util.UserUtil;
import org.fao.geonet.utils.Diff;
import org.fao.geonet.utils.DiffType;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Set;

import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

/**
 * Process a metadata with an SQL search and replace.
 * It is not as fast as a direct SQL UPDATE but will check
 * that the document after the replace is still well-formed XML.
 */
@RequestMapping(value = {
    "/{portal}/api/processes/db"
})
@Tag(name = "processes",
    description = "Processing operations")
@Controller("dbprocess")
@ReadWriteController
public class DatabaseProcessApi {

    @Autowired
    DataManager dataMan;

    @Autowired
    SchemaManager schemaMan;

    @Autowired
    SettingManager settingManager;

    @Autowired
    RoleHierarchy roleHierarchy;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Preview of search and replace text.",
        description =" When errors occur during processing, the processing report is returned in JSON format.")
    @RequestMapping(
        value = "/search-and-replace",
        method = RequestMethod.GET,
        consumes = {
            MediaType.TEXT_HTML_VALUE,
            MediaType.ALL_VALUE
        },
        produces = {
            MediaType.ALL_VALUE
        })
    @ResponseBody
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Processed records."),
        @ApiResponse(responseCode = "500", description = "If one record processed is invalid."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    public ResponseEntity<Object> previewProcessSearchAndReplace(
        @Parameter(
            description = "Use regular expression (may not be supported by all databases - tested with H2 and PostgreSQL)",
            required = false
        )
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
            boolean useRegexp,
        @Parameter(
            description = "Value to search for"
        )
        @RequestParam
            String search,
        @Parameter(
            description = "Replacement"
        )
        @RequestParam(
            defaultValue = ""
        )
            String replace,
        @Parameter(
            description = "regexpFlags"
        )
        @RequestParam(
            required = false,
            defaultValue = ""
        )
            String regexpFlags,
        @Parameter(
            description = "Return differences with diff, diffhtml or patch",
            required = false
        )
        @RequestParam(
            required = false
        )
            DiffType diffType,
        @Parameter(description = API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @Parameter(hidden = true)
            HttpSession httpSession,
        @Parameter(hidden = true)
            HttpServletRequest request,
        @Parameter(hidden = true)
            HttpServletResponse response) throws IllegalArgumentException {
        UserSession session = ApiUtils.getUserSession(httpSession);

        MetadataReplacementProcessingReport processingReport =
            new MetadataReplacementProcessingReport(search + "-" + replace);

        Element preview = new Element("preview");

        try {
            ServiceContext serviceContext = ApiUtils.createServiceContext(request);
            checkUserProfileToBatchEditMetadata(serviceContext.getUserSession());

            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, session);

            final String siteURL = request.getRequestURL().toString() + "?" + request.getQueryString();
            for (String uuid : records) {
                String id = dataMan.getMetadataId(uuid);
                Log.info("org.fao.geonet.services.metadata",
                    "Processing metadata for preview with id:" + id);

                Element record = DatabaseProcessUtils.process(
                    serviceContext,
                    id, useRegexp, search, replace, regexpFlags,
                    false, false,
                    false, processingReport);
                if (record != null) {
                    if (diffType != null) {
                        IMetadataUtils metadataUtils = serviceContext.getBean(IMetadataUtils.class);
                        AbstractMetadata metadata = metadataUtils.findOne(id);
                        preview.addContent(
                            Diff.diff(metadata.getData(), Xml.getString(record), diffType));
                    } else {
                        preview.addContent(record.detach());
                    }
                }
            }
        } catch (Exception exception) {
            processingReport.addError(exception);
        } finally {
            processingReport.close();
        }

        // In case of errors during processing return report.
        if (processingReport.getMetadataErrors().size() > 0) {
            response.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ObjectMapper mapper = new ObjectMapper();
            try {
                return new ResponseEntity(
                    mapper.writeValueAsString(processingReport),
                    HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (JsonProcessingException errorReportException) {
                return new ResponseEntity(
                    String.format("Failed to generate error report due to '%s'.",
                    errorReportException.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<>(preview, HttpStatus.OK);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Apply a database search and replace to one or more records",
        description = ApiParams.API_OP_NOTE_PROCESS)
    @RequestMapping(
        value = "/search-and-replace",
        method = RequestMethod.POST,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Report about processed records."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    public XsltMetadataProcessingReport processSearchAndReplace(
        @Parameter(
            description = "Use regular expression (may not be supported by all databases - tested with H2 and PostgreSQL)",
            required = false
        )
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
            boolean useRegexp,
        @Parameter(
            description = "Value to search for"
        )
        @RequestParam
            String search,
        @Parameter(
            description = "Replacement"
        )
        @RequestParam(
            defaultValue = ""
        )
            String replace,
        @Parameter(
            description = "regexpFlags"
        )
        @RequestParam(
            required = false,
            defaultValue = ""
        )
            String regexpFlags,
        @Parameter(description = API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
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
            description = ApiParams.API_PARAM_UPDATE_DATESTAMP,
            required = false
        )
        @RequestParam(
            required = false,
            defaultValue = "true"
        )
            boolean updateDateStamp,
        @Parameter(description = "Index after processing",
            required = false,
            example = "false")
        @RequestParam(required = false, defaultValue = "true")
            boolean index,
        @Parameter(hidden = true)
            HttpSession httpSession,
        @Parameter(hidden = true)
            HttpServletRequest request) throws Exception {
        UserSession session = ApiUtils.getUserSession(httpSession);

        MetadataReplacementProcessingReport processingReport =
            new MetadataReplacementProcessingReport(search + "-" + replace);

        try {
            ServiceContext serviceContext = ApiUtils.createServiceContext(request);
            checkUserProfileToBatchEditMetadata(serviceContext.getUserSession());
            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, session);
            UserSession userSession = ApiUtils.getUserSession(httpSession);

            final String siteURL = request.getRequestURL().toString() + "?" + request.getQueryString();

            processingReport.setTotalRecords(records.size());

            BatchDatabaseUpdateMetadataReindexer m = new BatchDatabaseUpdateMetadataReindexer(
                serviceContext,
                dataMan, records, useRegexp, search, replace, regexpFlags, httpSession, siteURL,
                processingReport, request, index, updateDateStamp, userSession.getUserIdAsInt());
            m.process(settingManager.getSiteId());

        } catch (Exception exception) {
            processingReport.addError(exception);
        } finally {
            processingReport.close();
        }

        return processingReport;
    }

    static final class BatchDatabaseUpdateMetadataReindexer extends
        MetadataIndexerProcessor {
        private final boolean index;
        private final boolean updateDateStamp;
        Set<String> records;
        boolean useRegexp;
        String search;
        String replace;
        String regexpFlags;
        String siteURL;
        HttpSession session;
        MetadataReplacementProcessingReport processingReport;
        HttpServletRequest request;
        ServiceContext context;
        int userId;

        public BatchDatabaseUpdateMetadataReindexer(ServiceContext context,
                                                    DataManager dm,
                                                    Set<String> records,
                                                    boolean useRegexp,
                                                    String search,
                                                    String replace,
                                                    String regexpFlags,
                                                    HttpSession session,
                                                    String siteURL,
                                                    MetadataReplacementProcessingReport processingReport,
                                                    HttpServletRequest request, boolean index,
                                                    boolean updateDateStamp, int userId) {
            super(dm);
            this.records = records;
            this.useRegexp = useRegexp;
            this.search = search;
            this.replace = replace;
            this.regexpFlags = regexpFlags;
            this.session = session;
            this.index = index;
            this.updateDateStamp = updateDateStamp;
            this.siteURL = siteURL;
            this.request = request;
            this.processingReport = processingReport;
            this.context = context;
            this.userId = userId;
        }

        @Override
        public void process(String catalogueId) throws Exception {
            DataManager dataMan = context.getBean(DataManager.class);
            ApplicationContext appContext = ApplicationContextHolder.get();
            for (String uuid : this.records) {
                String id = getDataManager().getMetadataId(uuid);
                Log.info("org.fao.geonet.services.metadata",
                    "Processing metadata with id:" + id);

                Element beforeMetadata = dataMan.getMetadata(context, id, false, false, false);

                Element record = DatabaseProcessUtils.process(
                    ApiUtils.createServiceContext(request),
                    id, useRegexp, search, replace, regexpFlags,
                    true, index,
                    updateDateStamp, processingReport);

                Element afterMetadata = dataMan.getMetadata(context, id, false, false, false);

                XMLOutputter outp = new XMLOutputter();
                String xmlAfter = outp.outputString(afterMetadata);
                String xmlBefore = outp.outputString(beforeMetadata);
                new RecordProcessingChangeEvent(
                    Long.parseLong(id), this.userId,
                    xmlBefore, xmlAfter,
                    processingReport.getProcessId()).publish(appContext);
            }
        }
    }

    /**
     * Checks if the user profile is allowed to batch edit metadata.
     *
     * @param userSession
     */
    private void checkUserProfileToBatchEditMetadata(UserSession userSession) {
        if (userSession.getProfile() != Profile.Administrator) {
            String allowedUserProfileToImportMetadata =
                StringUtils.defaultIfBlank(settingManager.getValue(Settings.METADATA_BATCH_EDITING_ACCESS_LEVEL), Profile.Editor.toString());

            // Is the user profile is higher than the profile allowed to import metadata?
            if (!UserUtil.hasHierarchyRole(allowedUserProfileToImportMetadata, this.roleHierarchy)) {
                throw new NotAllowedException("The user has no permissions to batch edit metadata.");
            }
        }

    }
}
