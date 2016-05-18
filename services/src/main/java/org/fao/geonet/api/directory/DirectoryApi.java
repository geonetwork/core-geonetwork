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

package org.fao.geonet.api.directory;

import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.api.API;
import org.fao.geonet.services.metadata.BatchOpsMetadataReindexer;
import org.jdom.Element;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

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

    private static final String API_COLLECT_ENTRIES_NOTE =
            "Scan one or more records for element matching the XPath provided " +
            "and save them as directory entries (ie. subtemplate).<br/><br/>" +
            "Only records that the current user can edit are analyzed.";
    public static final String API_SYNCHRONIZE_ENTRIES_NOTE =
            "Scan one or more records for element matching the XPath provided " +
            "and then check if this element is available in the directory. " +
            "If Found, the element from the directory update the element " +
            "in the record and optionally properties are preserved.<br/><br/>" +
            "The identifier XPath is used to find a match. If not, the UUID " +
            "is based on the content of the snippet. It is recommended to use " +
            "an identifier for better matching (eg. ISO19139 contact with different " +
            "roles will not match on the automatic UUID mode).";

    public static final String APIURL_ACTIONS_ENTRIES_COLLECT =
            "/collect";
    public static final String APIURL_ACTIONS_ENTRIES_SYNCHRONIZE =
            "/synchronize";

    public static final String APIPARAM_RECORD_UUIDS_OR_SELECTION =
            "Record UUIDs. If null current selection is used.";
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

    @ApiOperation(value = "Preview directory entries extracted from records",
            nickname = "previewExtractedEntries",
            notes = API_COLLECT_ENTRIES_NOTE)
    @RequestMapping(
            value = APIURL_ACTIONS_ENTRIES_COLLECT,
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<Element> previewExtractedEntries(
            @ApiParam(value = APIPARAM_RECORD_UUIDS_OR_SELECTION,
                    required = false,
                    example = "")
            @RequestParam(required = false)
            String[] uuids,
            @ApiParam(value = APIPARAM_XPATH,
                    required = false,
                    example = ".//gmd:CI_ResponsibleParty")
            @RequestParam(required = true)
            String xpath,
            @ApiParam(value = APIPARAM_IDENTIFIER_XPATH,
                    required = false,
                    example = "@uuid")
            @RequestParam(required = false)
            String identifierXpath
    ) throws Exception {
        return collectEntries(uuids, xpath, identifierXpath, false);
    }


    @ApiOperation(value = "Extracts directory entries from records",
            nickname = "extractEntries",
            notes = API_COLLECT_ENTRIES_NOTE)
    @RequestMapping(
            value = APIURL_ACTIONS_ENTRIES_COLLECT,
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<Element> extractEntries(
            @ApiParam(value = APIPARAM_RECORD_UUIDS_OR_SELECTION,
                    required = false,
                    example = "")
            @RequestParam(required = false)
            String[] uuids,
            @ApiParam(value = APIPARAM_XPATH,
                    required = false,
                    example = ".//gmd:CI_ResponsibleParty")
            @RequestParam(required = true)
            String xpath,
            @ApiParam(value = APIPARAM_IDENTIFIER_XPATH,
                    required = false,
                    example = "@uuid")
            @RequestParam(required = false)
            String identifierXpath
            // TODO: Add an option to set categories ?
            // TODO: Add an option to set groupOwner ?
            // TODO: Add an option to set privileges ?
    ) throws Exception {
        return collectEntries(uuids, xpath, identifierXpath, true);
    }


    private ResponseEntity<Element> collectEntries(
            String[] uuids,
            String xpath,
            String identifierXpath,
            boolean save) throws Exception {
        ServiceContext context = ServiceContext.get();
        UserSession session = context.getUserSession();
        Profile profile = session.getProfile();
        if (profile != Profile.Administrator && profile != Profile.Reviewer) {
            // TODO: i18n
            throw new SecurityException(
                    "Only administrator and reviewer can extract directory entries.");
        }


        // Check which records to analyse
        final Set<String> setOfUuidsToEdit;
        if (uuids == null) {
            SelectionManager selectionManager =
                    SelectionManager.getManager(session);
            synchronized (
                    selectionManager.getSelection(
                            SelectionManager.SELECTION_METADATA)) {
                final Set<String> selection = selectionManager.getSelection(SelectionManager.SELECTION_METADATA);
                setOfUuidsToEdit = Sets.newHashSet(selection);
            }
        } else {
            setOfUuidsToEdit = Sets.newHashSet(Arrays.asList(uuids));
        }
        if (setOfUuidsToEdit.size() == 0) {
            // TODO: i18n
            throw new IllegalArgumentException(
                    "At least one record should be defined or selected for analysis.");
        }

        DataManager dataMan = context.getBean(DataManager.class);
        AccessManager accessMan = context.getBean(AccessManager.class);

        // List of identifier to check for duplicates
        Set<Element> listOfEntries = new HashSet<>();
        Set<Integer> listOfEntriesInternalId = new HashSet<>();
        final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
        final int user = context.getUserSession().getUserIdAsInt();
        final String siteId = context.getBean(SettingManager.class).getSiteId();

        for (String recordUuid : setOfUuidsToEdit) {
            Metadata record = metadataRepository.findOneByUuid(recordUuid);
            if (record == null) {
                // Skip
            } else if (!accessMan.canEdit(context, String.valueOf(record.getId()))) {
                // Skip
            } else {
                // Processing
                try {
                    CollectResults collectResults =
                            DirectoryUtils.collectEntries(
                                    record, xpath, identifierXpath);
                    if (save) {
                        DirectoryUtils.saveEntries(
                                collectResults,
                                siteId, user,
                                1, // TODO: Define group or take a default one
                                false);
                        listOfEntriesInternalId.addAll(
                                collectResults.getEntryIdentifiers().values()
                        );
                    } else {
                        listOfEntries.addAll(collectResults.getEntries().values());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (save) {
            dataMan.flush();
            BatchOpsMetadataReindexer r = new BatchOpsMetadataReindexer(dataMan, listOfEntriesInternalId);
            r.process();
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            Element response = new Element("list");
            for (Element e : listOfEntries) {
                response.addContent(e);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
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
    public ResponseEntity<Element> previewUpdatedRecordEntries(
            @ApiParam(value = APIPARAM_RECORD_UUIDS_OR_SELECTION,
                    required = false,
                    example = "")
            @RequestParam(required = false)
            String[] uuids,
            @ApiParam(value = APIPARAM_XPATH,
                    required = false,
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
            @ApiIgnore
            HttpServletRequest httpRequest
    ) throws Exception {
        return updateRecordEntries(uuids, xpath, identifierXpath, propertiesToCopy, substituteAsXLink, false);
    }


    @ApiOperation(value = "Update matching entries in records",
            nickname = "updateRecordEntries",
            notes = API_SYNCHRONIZE_ENTRIES_NOTE)
    @RequestMapping(
            value = APIURL_ACTIONS_ENTRIES_SYNCHRONIZE,
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<Element> updateRecordEntries(
            @ApiParam(value = APIPARAM_RECORD_UUIDS_OR_SELECTION,
                    required = false,
                    example = "")
            @RequestParam(required = false)
            String[] uuids,
            @ApiParam(value = APIPARAM_XPATH,
                    required = false,
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
            boolean substituteAsXLink
    ) throws Exception {
        return updateRecordEntries(uuids, xpath, identifierXpath, propertiesToCopy, substituteAsXLink, true);
    }


    private ResponseEntity<Element> updateRecordEntries(
            String[] uuids,
            String xpath,
            String identifierXpath,
            List<String> propertiesToCopy,
            boolean substituteAsXLink,
            boolean save) throws Exception {
        ServiceContext context = ServiceContext.get();
        UserSession session = context.getUserSession();
        Profile profile = session.getProfile();
        if (profile != Profile.Administrator && profile != Profile.Reviewer) {
            // TODO: i18n
            throw new SecurityException(
                    "Only administrator and reviewer can extract directory entries.");
        }


        // Check which records to analyse
        final Set<String> setOfUuidsToEdit;
        if (uuids == null) {
            SelectionManager selectionManager =
                    SelectionManager.getManager(session);
            synchronized (
                    selectionManager.getSelection(
                            SelectionManager.SELECTION_METADATA)) {
                final Set<String> selection = selectionManager.getSelection(SelectionManager.SELECTION_METADATA);
                setOfUuidsToEdit = Sets.newHashSet(selection);
            }
        } else {
            setOfUuidsToEdit = Sets.newHashSet(Arrays.asList(uuids));
        }
        if (setOfUuidsToEdit.size() == 0) {
            // TODO: i18n
            throw new IllegalArgumentException(
                    "At least one record should be defined or selected for analysis.");
        }

        DataManager dataMan = context.getBean(DataManager.class);
        AccessManager accessMan = context.getBean(AccessManager.class);

        // List of identifier to check for duplicates
        Set<Element> listOfUpdatedRecord = new HashSet<>();
        Set<Integer> listOfRecordInternalId = new HashSet<>();
        final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);

        boolean validate = false, ufo = false, index = false;
        for (String recordUuid : setOfUuidsToEdit) {
            Metadata record = metadataRepository.findOneByUuid(recordUuid);
            if (record == null) {
                // Skip
            } else if (!accessMan.canEdit(context, String.valueOf(record.getId()))) {
                // Skip
            } else {
                // Processing
                try {
                    CollectResults collectResults =
                            DirectoryUtils.synchronizeEntries(
                                    record, xpath, identifierXpath,
                                    propertiesToCopy, substituteAsXLink);
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
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (collectResults.isRecordUpdated()) {
                            listOfUpdatedRecord.add(collectResults.getUpdatedRecord());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (save) {
            dataMan.flush();
            BatchOpsMetadataReindexer r =
                    new BatchOpsMetadataReindexer(dataMan, listOfRecordInternalId);
            r.process();
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            // TODO: Limite size of large response ?
            Element response = new Element("list");
            for (Element e : listOfUpdatedRecord) {
                response.addContent(e);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }
}