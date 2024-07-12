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

package org.fao.geonet.api.records;

import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.utils.ObjectJSONUtils;
import org.fao.geonet.events.history.RecordImportedEvent;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.fao.geonet.api.ApiParams.*;

@RequestMapping(value = {
    "/{portal}/api/records"
})
@Tag(name = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("samplesAndTemplates")
@ReadWriteController
public class MetadataSampleApi {

    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    DataManager dataManager;

    @Autowired
    SettingManager settingManager;

    @Autowired
    SchemaManager schemaManager;


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Add samples",
        description = "Add sample records for one or more schemas. " +
            "Samples are defined for each standard in the samples folder " +
            "as MEF files.")
    @RequestMapping(value = "/samples",
        method = RequestMethod.PUT
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Return a report of what has been done."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseStatus(HttpStatus.CREATED)
    public
    @ResponseBody
    SimpleMetadataProcessingReport addSamples(
        @Parameter(description = API_PARAM_SCHEMA_IDENTIFIERS,
            required = true,
            example = "iso19139")
        @RequestParam(required = false)
            String[] schema,
        HttpServletRequest request
    )
        throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);
        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();
        UserSession userSession = ApiUtils.getUserSession(request.getSession());

        Element params = new Element("params");
        params.addContent(new Element(Params.FILE_TYPE).setText("mef"));
        params.addContent(new Element(Params.UUID_ACTION).setText(MEFLib.UuidAction.NOTHING.toString()));
        for (String schemaName : schema) {
            Log.info(Geonet.DATA_MANAGER, "Loading sample data for schema "
                + schemaName);
            Path schemaDir = schemaManager.getSchemaSampleDataDir(schemaName);
            if (schemaDir == null) {
                report.addInfos(String.format(
                    "No samples available for schema '%s'.", schemaName
                ));
                continue;
            }

            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "Searching for mefs in: " + schemaDir);
            }

            List<Path> sampleDataFilesList;
            try (DirectoryStream<Path> newDirectoryStream =
                     Files.newDirectoryStream(schemaDir, "*.mef")) {
                sampleDataFilesList = Lists.newArrayList(newDirectoryStream);
            }

            int schemaCount = 0;
            for (final Path file : sampleDataFilesList) {
                try {
                    if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                        Log.debug(Geonet.DATA_MANAGER,
                            String.format("Loading %s sample file %s ...", schemaName, file));
                    }
                    List<String> importedMdIds = MEFLib.doImport(params, context, file, null);

                    if (importedMdIds != null && importedMdIds.size() > 0) {
                        schemaCount += importedMdIds.size();
                        for (String mdId : importedMdIds) {
                            AbstractMetadata metadata = ApiUtils.getRecord(mdId);
                            new RecordImportedEvent(Integer.parseInt(mdId), userSession.getUserIdAsInt(),
                                ObjectJSONUtils.convertObjectInJsonObject(userSession.getPrincipal(), RecordImportedEvent.FIELD),
                                metadata.getData()).publish(applicationContext);
                        }
                    }
                } catch (Exception e) {
                    Log.error(Geonet.DATA_MANAGER,
                        String.format("Error loading %s sample file %s. Error is %s.",
                            schemaName, file, e.getMessage()),
                        e);
                    report.addError(new Exception(String.format(
                        "Error loading '%s' sample file '%s'. Error is %s.",
                        schemaName, file, e.getMessage())));
                }
                dataManager.flush();
            }
            report.addInfos(String.format(
                "%d record(s) added for schema '%s'.",
                schemaCount, schemaName));
        }
        report.close();
        return report;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Add templates",
        description = "Add template records for one or more schemas. " +
            "Templates are defined for each standard in the template folder " +
            "as XML files. Template may also contains subtemplates.")
    @RequestMapping(value = "/templates",
        method = RequestMethod.PUT
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Return a report of what has been done."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseStatus(HttpStatus.CREATED)
    public
    @ResponseBody
    SimpleMetadataProcessingReport addTemplates(
        @Parameter(description = API_PARAM_SCHEMA_IDENTIFIERS,
            required = true,
            example = "iso19139")
        @RequestParam(required = false)
            String[] schema,
        @Parameter(hidden = true)
            HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();

        String siteId = settingManager.getSiteId();
        int owner = ApiUtils.getUserSession(session).getUserIdAsInt();

        Log.info(Geonet.DATA_MANAGER, String.format(
            "Loading templates for schemas '%s'.", schema));

        for (String schemaName : schema) {
            Path templatesDir = schemaManager.getSchemaTemplatesDir(schemaName);
            if (templatesDir == null) {
                report.addInfos(String.format(
                    "No templates available for schema '%s'.", schemaName
                ));
                continue;
            }
            final String subTemplatePrefix = "sub-";
            final String templateOfSubTemplatePrefix = "sub-tpl-";
            final int prefixLength = subTemplatePrefix.length();
            int schemaCount = 0;
            try (DirectoryStream<Path> newDirectoryStream =
                     Files.newDirectoryStream(templatesDir, "*.xml")) {
                for (Path temp : newDirectoryStream) {
                    String status = "failed";
                    String templateName = temp.getFileName().toString();

                    Element template = new Element("template");
                    template.setAttribute("name", templateName);

                    if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                        Log.debug(Geonet.DATA_MANAGER,
                            String.format(" - Adding %s template file %s ...",
                                schemaName, templateName));
                    }

                    try {
                        Element xml = Xml.loadFile(temp);
                        String uuid = UUID.randomUUID().toString();
                        String isTemplate = "y";
                        String title = null;

                        if (templateName.startsWith(subTemplatePrefix)) {
                            isTemplate = templateName.startsWith(templateOfSubTemplatePrefix) ?
                                "t" : "s";
                        }

                        if (isTemplate.equals("s")) {
                            // subtemplates loaded here can have a specific uuid
                            // attribute
                            String tryUuid = xml.getAttributeValue("uuid");
                            if (!StringUtils.isEmpty(tryUuid)) uuid = tryUuid;
                        }
                        if (dataManager.existsMetadataUuid(uuid)) {
                            String upid = dataManager.getMetadataId(uuid);
                            AbstractMetadata metadata = dataManager.updateMetadata(context, upid, xml, false, true, context.getLanguage(), null, true, IndexingMode.none);
                            report.addMetadataInfos(metadata,
                            String.format(
                            "Template for schema '%s' with UUID '%s' updated.",
                            schemaName, uuid));
                        } else {
                            //
                            // insert metadata
                            //
                            Metadata metadata = new Metadata();
                            metadata.setUuid(uuid);
                            metadata.getDataInfo().
                                setSchemaId(schemaName).
                                setRoot(xml.getQualifiedName()).
                                setType(MetadataType.lookup(isTemplate));
                            metadata.getSourceInfo().
                                setSourceId(siteId).
                                setOwner(owner).
                                setGroupOwner(1);
                            // Set the UUID explicitly, insertMetadata doesn't update the xml with the generated UUID for templates
                            if (MetadataType.lookup(isTemplate) == MetadataType.TEMPLATE) {
                                xml = dataManager.setUUID(schemaName, uuid, xml);
                            }
                            dataManager.insertMetadata(context, metadata, xml, IndexingMode.full, true, UpdateDatestamp.NO, false, true);
                            report.addMetadataInfos(metadata,
                            String.format(
                            "Template for schema '%s' with UUID '%s' added.",
                            schemaName, metadata.getUuid()));
                        }

                        schemaCount++;
                    } catch (Exception e) {
                        Log.error(Geonet.DATA_MANAGER,
                            String.format("Error loading %s template file %s. Error is %s.",
                                schemaName, temp, e.getMessage()),
                            e);
                        report.addError(new Exception(String.format(
                            "Error loading '%s' template file '%s'. Error is %s.",
                            schemaName, temp, e.getMessage())));
                    }
                }
            }

            report.addInfos(String.format(
                "%d record(s) added for schema '%s'.",
                schemaCount, schemaName));
        }
        report.close();
        return report;
    }
}
