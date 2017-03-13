//=============================================================================
//===	Copyright (C) 2001-2016 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.api.registries;

import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.metadata.BatchOpsMetadataReindexer;
import org.jdom.Element;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import javax.servlet.http.HttpServletRequest;

@EnableWebMvc
@Service
@RequestMapping(value = {
    "/api/registries/actions/entries",
    "/api/" + API.VERSION_0_1 +
        "/registries/actions/entries"
})
@Api(value = "registries",
    tags = "registries",
    description = "Registries related operations")
public class DirectoryApi {
    public static final String LOGGER = Geonet.GEONETWORK + ".registries.directory";
    public static final String API_SYNCHRONIZE_ENTRIES_NOTE =
        "Scan one or more records for element matching the XPath provided " +
            "and then check if this element is available in the directory. " +
            "If Found, the element from the directory update the element " +
            "in the record and optionally properties are preserved.<br/><br/>" +
            "The identifier XPath is used to find a match. An optional filter" +
            "can be added to restrict search to a subset of the directory. " +
            "If no identifier XPaths is provided, the UUID " +
            "is based on the content of the snippet (hash). It is recommended to use " +
            "an identifier for better matching (eg. ISO19139 contact with different " +
            "roles will not match on the automatic UUID mode).";
    public static final String APIURL_ACTIONS_ENTRIES_COLLECT =
        "/collect";
    public static final String APIURL_ACTIONS_ENTRIES_SYNCHRONIZE =
        "/synchronize";
    public static final String APIPARAM_XPATH =
        "XPath of the elements to extract as entry.";
    public static final String APIPARAM_IDENTIFIER_XPATH =
        "XPath of the element identifier. If not defined " +
            "a random UUID is generated and analysis will not check " +
            "for duplicates.";
    public static final String APIPARAM_PROPERTIESTOCOPY =
        "List of XPath of properties to copy from record to matching entry.";
    public static final String APIPARAM_REPLACEWITHXLINK =
        "Replace entry by XLink.";
    public static final String APIPARAM_DIRECTORYFILTERQUERY =
        "Filter query for directory search.";
    private static final String API_COLLECT_ENTRIES_NOTE =
        "Scan one or more records for element matching the XPath provided " +
            "and save them as directory entries (ie. subtemplate).<br/><br/>" +
            "Only records that the current user can edit are analyzed.";

    @ApiOperation(value = "Preview directory entries extracted from records",
        nickname = "previewExtractedEntries",
        notes = API_COLLECT_ENTRIES_NOTE)
    @RequestMapping(
        value = APIURL_ACTIONS_ENTRIES_COLLECT,
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('Reviewer')")
    public ResponseEntity<Object> previewExtractedEntries(
        @ApiParam(value = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @ApiParam(
            value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @ApiParam(value = APIPARAM_XPATH,
            required = true,
            example = ".//gmd:CI_ResponsibleParty")
        @RequestParam(required = true)
            String xpath,
        @ApiParam(value = APIPARAM_IDENTIFIER_XPATH,
            required = false,
            example = "@uuid")
        @RequestParam(required = false)
            String identifierXpath,
        HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        return collectEntries(context, uuids, bucket, xpath, identifierXpath, false, null);
    }


    @ApiOperation(value = "Extracts directory entries from records",
        nickname = "extractEntries",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        notes = API_COLLECT_ENTRIES_NOTE)
    @RequestMapping(
        value = APIURL_ACTIONS_ENTRIES_COLLECT,
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('Reviewer')")
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<Object> extractEntries(
        @ApiParam(value = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @ApiParam(
            value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @ApiParam(value = APIPARAM_XPATH,
            required = true,
            example = ".//gmd:CI_ResponsibleParty")
        @RequestParam(required = true)
            String xpath,
        @ApiParam(value = APIPARAM_IDENTIFIER_XPATH,
            required = false,
            example = "@uuid")
        @RequestParam(required = false)
            String identifierXpath,
        HttpServletRequest request
        // TODO: Add an option to set categories ?
        // TODO: Add an option to set groupOwner ?
        // TODO: Add an option to set privileges ?
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        return collectEntries(context, uuids, bucket, xpath, identifierXpath, true, null);
    }


    private ResponseEntity<Object> collectEntries(
        ServiceContext context,
        String[] uuids,
        String bucket,
        String xpath,
        String identifierXpath,
        boolean save, String directoryFilterQuery) throws Exception {

        UserSession session = context.getUserSession();

        // Check which records to analyse
        final Set<String> setOfUuidsToEdit = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, session);

        DataManager dataMan = context.getBean(DataManager.class);
        AccessManager accessMan = context.getBean(AccessManager.class);
        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();

        // List of identifier to check for duplicates
        Set<Element> listOfEntries = new HashSet<>();
        Set<Integer> listOfEntriesInternalId = new HashSet<>();
        final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
        final int user = context.getUserSession().getUserIdAsInt();
        final String siteId = context.getBean(SettingManager.class).getSiteId();

        for (String recordUuid : setOfUuidsToEdit) {
            Metadata record = metadataRepository.findOneByUuid(recordUuid);
            if (record == null) {
                report.incrementNullRecords();
            } else if (!accessMan.canEdit(context, String.valueOf(record.getId()))) {
                report.addNotEditableMetadataId(record.getId());
            } else {
                // Processing
                try {
                    CollectResults collectResults =
                        DirectoryUtils.collectEntries(context,
                            record, xpath, identifierXpath);
                    if (save) {
                        DirectoryUtils.saveEntries(
                            context,
                            collectResults,
                            siteId, user,
                            1, // TODO: Define group or take a default one
                            false);
                        listOfEntriesInternalId.addAll(
                            collectResults.getEntryIdentifiers().values()
                        );
                        report.incrementProcessedRecords();
                        report.addMetadataInfos(record.getId(), String.format(
                            "%d entry(ies) extracted from record '%s'. UUID(s): %s",
                            collectResults.getEntryIdentifiers().size(),
                            record.getUuid(),
                            collectResults.getEntryIdentifiers().toString()
                        ));
                    } else {
                        listOfEntries.addAll(collectResults.getEntries().values());
                    }
                } catch (Exception ex) {
                    report.addMetadataError(record.getId(), ex);
                }
            }
        }

        if (save) {
            dataMan.flush();
            BatchOpsMetadataReindexer r = new BatchOpsMetadataReindexer(dataMan, listOfEntriesInternalId);
            r.process();
            report.close();
            return new ResponseEntity<>((Object) report, HttpStatus.CREATED);
        } else {
            Element response = new Element("entries");
            for (Element e : listOfEntries) {
                response.addContent(e);
            }
            return new ResponseEntity<>((Object) response, HttpStatus.OK);
        }
    }


    @ApiOperation(value = "Preview updated matching entries in records",
        nickname = "previewUpdatedRecordEntries",
        notes = API_SYNCHRONIZE_ENTRIES_NOTE)
    @RequestMapping(
        value = APIURL_ACTIONS_ENTRIES_SYNCHRONIZE,
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<Object> previewUpdatedRecordEntries(
        @ApiParam(value = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @ApiParam(
            value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @ApiParam(value = APIPARAM_XPATH,
            required = true,
            example = ".//gmd:CI_ResponsibleParty")
        @RequestParam(required = true)
            String xpath,
        @ApiParam(value = APIPARAM_IDENTIFIER_XPATH,
            required = false,
            example = "@uuid or .//gmd:electronicMailAddress/gco:CharacterString/text()")
        @RequestParam(required = false)
            String identifierXpath,
        @ApiParam(value = APIPARAM_PROPERTIESTOCOPY,
            required = false,
            example = "./gmd:role/*/@codeListValue")
        @RequestParam(required = false)
            List<String> propertiesToCopy,
        @ApiParam(value = APIPARAM_REPLACEWITHXLINK,
            required = false,
            example = "@uuid")
        @RequestParam(required = false, defaultValue = "false")
            boolean substituteAsXLink,
        @ApiParam(value = APIPARAM_DIRECTORYFILTERQUERY,
            required = false,
            example = "groupPublished:IFREMER")
        @RequestParam(required = false)
            String fq,
        HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        return updateRecordEntries(context, uuids, bucket, xpath, identifierXpath, propertiesToCopy, substituteAsXLink, false, fq);
    }


    @ApiOperation(value = "Update matching entries in records",
        nickname = "updateRecordEntries",
        notes = API_SYNCHRONIZE_ENTRIES_NOTE)
    @RequestMapping(
        value = APIURL_ACTIONS_ENTRIES_SYNCHRONIZE,
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("hasRole('Reviewer')")
    @ResponseBody
    public ResponseEntity<Object> updateRecordEntries(
        @ApiParam(value = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @ApiParam(
            value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @ApiParam(value = APIPARAM_XPATH,
            required = true,
            example = ".//gmd:CI_ResponsibleParty")
        @RequestParam(required = true)
            String xpath,
        @ApiParam(value = APIPARAM_IDENTIFIER_XPATH,
            required = false,
            example = "@uuid")
        @RequestParam(required = false)
            String identifierXpath,
        @ApiParam(value = APIPARAM_PROPERTIESTOCOPY,
            required = false,
            example = "./gmd:role/*/@codeListValue")
        @RequestParam(required = false)
            List<String> propertiesToCopy,
        @ApiParam(value = APIPARAM_REPLACEWITHXLINK,
            required = false)
        @RequestParam(required = false, defaultValue = "false")
            boolean substituteAsXLink,
        @ApiParam(value = APIPARAM_DIRECTORYFILTERQUERY,
            required = false,
            example = "groupPublished:IFREMER")
        @RequestParam(required = false)
            String fq,
        HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        return updateRecordEntries(context, uuids, bucket, xpath, identifierXpath, propertiesToCopy, substituteAsXLink, true, fq);
    }


    private ResponseEntity<Object> updateRecordEntries(
        ServiceContext context,
        String[] uuids,
        String bucket,
        String xpath,
        String identifierXpath,
        List<String> propertiesToCopy,
        boolean substituteAsXLink,
        boolean save, String directoryFilterQuery) throws Exception {

        UserSession session = context.getUserSession();
        Profile profile = session.getProfile();

        // Check which records to analyse
        final Set<String> setOfUuidsToEdit = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, session);

        DataManager dataMan = context.getBean(DataManager.class);
        AccessManager accessMan = context.getBean(AccessManager.class);

        // List of identifier to check for duplicates
        Set<Element> listOfUpdatedRecord = new HashSet<>();
        Set<Integer> listOfRecordInternalId = new HashSet<>();
        final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();

        boolean validate = false, ufo = false, index = false;
        report.setTotalRecords(setOfUuidsToEdit.size());
        for (String recordUuid : setOfUuidsToEdit) {
            Metadata record = metadataRepository.findOneByUuid(recordUuid);
            if (record == null) {
                report.incrementNullRecords();
            } else if (!accessMan.canEdit(context, String.valueOf(record.getId()))) {
                report.addNotEditableMetadataId(record.getId());
            } else {
                // Processing
                try {
                    CollectResults collectResults =
                        DirectoryUtils.synchronizeEntries(
                            context,
                            record, xpath, identifierXpath,
                            propertiesToCopy, substituteAsXLink, directoryFilterQuery);
                    listOfRecordInternalId.add(record.getId());
                    if (save && collectResults.isRecordUpdated()) {
                        // TODO: Only if there was a change
                        try {
                            // TODO: Should we update date stamp ?
                            dataMan.updateMetadata(
                                context, "" + record.getId(),
                                collectResults.getUpdatedRecord(),
                                validate, ufo, index, context.getLanguage(),
                                new ISODate().toString(), true);
                            listOfRecordInternalId.add(record.getId());
                            report.addMetadataInfos(record.getId(), "Metadata updated.");
                        } catch (Exception e) {
                            report.addMetadataError(record.getId(), e);
                        }
                    } else {
                        if (collectResults.isRecordUpdated()) {
                            listOfUpdatedRecord.add(collectResults.getUpdatedRecord());
                        }
                    }
                    report.incrementProcessedRecords();
                } catch (Exception e) {
                    report.addMetadataError(record.getId(), e);
                }
            }
        }

        if (save) {
            dataMan.flush();
            BatchOpsMetadataReindexer r =
                new BatchOpsMetadataReindexer(dataMan, listOfRecordInternalId);
            r.process();
            report.close();
            return new ResponseEntity<>((Object) report, HttpStatus.CREATED);
        } else {
            // TODO: Limite size of large response ?
            Element response = new Element("records");
            for (Element e : listOfUpdatedRecord) {
                response.addContent(e);
            }
            report.close();
            return new ResponseEntity<>((Object) response, HttpStatus.OK);
        }
    }
}
