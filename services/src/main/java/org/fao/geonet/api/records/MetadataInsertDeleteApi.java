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

import com.google.common.collect.Maps;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Util;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.mef.Importer;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;

@RequestMapping(value = {
    "/api/records",
    "/api/" + API.VERSION_0_1 +
        "/records"
})
@Api(value = "records",
    tags = "records",
    description = "Metadata record operations")
@Controller("recordInsertOrDelete")
@PreAuthorize("hasRole('Editor')")
@ReadWriteController
public class MetadataInsertDeleteApi {

    @ApiOperation(
        value = "Delete a metadata record",
        notes = "",
        nickname = "delete")
    @RequestMapping(value = "/{metadataUuid}",
        method = RequestMethod.DELETE
    )
    public
    @ResponseBody
    ResponseEntity deleteRecord(
        @ApiParam(
            value = "Record UUID.",
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(
            value = "Backup first the record as MEF in the metadata removed folder.",
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "true")
            boolean withBackup,
        HttpServletRequest request
    )
        throws Exception {
        Metadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);
        DataManager dataManager = appContext.getBean(DataManager.class);

        if (metadata.getDataInfo().getType() != MetadataType.SUB_TEMPLATE && withBackup) {
            MetadataUtils.backupRecord(metadata, context);
        }

        IO.deleteFileOrDirectory(
            Lib.resource.getMetadataDir(context.getBean(GeonetworkDataDirectory.class),
                String.valueOf(metadata.getId())));

        dataManager.deleteMetadata(context, metadataUuid);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @ApiOperation(
        value = "Add a record",
        notes = "An XML fragment or a URL MUST be provided.",
        nickname = "insert")
    @RequestMapping(
        method = {
            RequestMethod.PUT,
            RequestMethod.POST,
        },
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        },
        consumes = {
            MediaType.APPLICATION_XML_VALUE
        }
    )
    public
    @ResponseBody
    ResponseEntity<Object> insert(
        @ApiParam(
            value = "The type of the record.",
            required = false,
            defaultValue = "METADATA"
        )
        @RequestParam(
            required = false,
            defaultValue = "METADATA"
        )
        final MetadataType metadataType,
        @ApiParam(
            value = "XML fragment.",
            required = false
        )
        @RequestBody(
            required = false
        )
            String xml,
        @ApiParam(
            value = "URL of a file to download and insert.",
            required = false)
        @RequestParam(
            required = false
        )
            String[] url,
        @ApiParam(
            value = "Record identifier processing.",
            required = false,
            defaultValue = "NOTHING"
        )
        @RequestParam(
            required = false,
            defaultValue = "NOTHING"
        )
        final MEFLib.UuidAction uuidProcessing,
        @ApiParam(
            value = "The group the record is attached to.",
            required = false
        )
        @RequestParam(
            required = false
        )
        final String group,
        @ApiParam(
            value = "Tags to assign to the record.",
            required = false)
        @RequestParam(
            required = false
        )
        final String[] category,
        @ApiParam(
            value = "Validate the record first and reject it if not valid.",
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
        final boolean rejectIfInvalid,
        @ApiParam(
            value = "XSL transformation to apply to the record.",
            required = false,
            defaultValue = "_none_"
        )
        @RequestParam(
            required = false,
            defaultValue = "_none_"
        )
        final String transformWith,
        @ApiParam(
            value = "Force the schema of the record. If not set, schema autodetection " +
                "is used (and is the preferred method).",
            required = false)
        @RequestParam(
            required = false
        )
        String schema,
        @ApiParam(
            value = "(experimental) Add extra information to the record.",
            required = false)
        @RequestParam(
            required = false
        )
        final String extra,
        HttpServletRequest request
    )
        throws Exception {
        if (url == null && xml == null) {
            throw new IllegalArgumentException(String.format(
                "XML fragment or a URL MUST be provided."));
        }

        if (xml != null) {
            loadRecord(
                metadataType, Xml.loadString(xml, false),
                uuidProcessing, group, category, rejectIfInvalid, transformWith, schema, extra, request);
        }
        if (url != null) {
            for (String u : url) {
                loadRecord(
                    metadataType, Xml.loadFile(ApiUtils.downloadUrlInTemp(u)),
                    uuidProcessing, group, category, rejectIfInvalid, transformWith, schema, extra, request);
            }
        }
        // TODO: Add a report
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(
        value = "Add a record from XML or MEF file",
        notes = "",
        nickname = "insertFile")
    @RequestMapping(
        method = {
            RequestMethod.POST,
        },
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        },
        consumes = {
            MediaType.APPLICATION_XML_VALUE
        }
    )
    public
    @ResponseBody
    ResponseEntity<Object> insertFile(
        @ApiParam(
            value = "The type of the record.",
            required = false,
            defaultValue = "METADATA"
        )
        @RequestParam(
            required = false,
            defaultValue = "METADATA"
        )
        final MetadataType metadataType,
        @ApiParam(
            value = "XML or MEF file to upload",
            required = false
        )
        @RequestParam(
            value = "file",
            required = false
        )
            MultipartFile[] file,
        @ApiParam(
            value = "Record identifier processing.",
            required = false,
            defaultValue = "NOTHING"
        )
        @RequestParam(
            required = false,
            defaultValue = "NOTHING"
        )
        final MEFLib.UuidAction uuidProcessing,
        @ApiParam(
            value = "The group the record is attached to.",
            required = false
        )
        @RequestParam(
            required = false
        )
        final String group,
        @ApiParam(
            value = "Tags to assign to the record.",
            required = false)
        @RequestParam(
            required = false
        )
        final String[] category,
        @ApiParam(
            value = "Validate the record first and reject it if not valid.",
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
        final boolean rejectIfInvalid,
        @ApiParam(
            value = "XSL transformation to apply to the record.",
            required = false,
            defaultValue = "_none_"
        )
        @RequestParam(
            required = false,
            defaultValue = "_none_"
        )
        final String transformWith,
        @ApiParam(
            value = "Force the schema of the record. If not set, schema autodetection " +
                "is used (and is the preferred method).",
            required = false)
        @RequestParam(
            required = false
        )
        String schema,
        @ApiParam(
            value = "(experimental) Add extra information to the record.",
            required = false)
        @RequestParam(
            required = false
        )
        final String extra,
        HttpServletRequest request
    )
        throws Exception {
        if (file == null) {
            throw new IllegalArgumentException(String.format(
                "A file MUST be provided."));
        }
        if (file != null) {
            for (MultipartFile f : file) {
                loadRecord(
                    metadataType, Xml.loadStream(f.getInputStream()),
                    uuidProcessing, group, category, rejectIfInvalid, transformWith, schema, extra, request);
            }
        }
        // TODO: Add a report
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    private void loadRecord(
        MetadataType metadataType,
        Element xmlElement,
        final MEFLib.UuidAction uuidProcessing,
        final String group,
        final String[] category,
        boolean rejectIfInvalid,
        final String transformWith,
        String schema,
        final String extra,
        HttpServletRequest request) throws Exception {

        ApplicationContext appContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);

        if (!transformWith.equals("_none_")) {
            GeonetworkDataDirectory dataDirectory = appContext.getBean(GeonetworkDataDirectory.class);
            Path folder = dataDirectory.getWebappDir().resolve(Geonet.Path.IMPORT_STYLESHEETS);
            Path xslFile = folder.resolve(transformWith);
            if (Files.exists(xslFile)) {
                xmlElement = Xml.transform(xmlElement, xslFile);
            } else {
                throw new ResourceNotFoundException(String.format(
                    "XSL transformation '%s' not found.",
                    transformWith
                ));
            }
        }

        DataManager dataMan = appContext.getBean(DataManager.class);
        if (schema == null) {
            schema = dataMan.autodetectSchema(xmlElement);
            if (schema == null) {
                throw new BadParameterEx(
                    "Can't detect schema for metadata automatically.",
                    "schema is unknown"
                );
                // TODO: Report what are the supported schema
            }
        } else {
            // TODO: Check that the schema is supported
        }

        if (rejectIfInvalid) {
            DataManager.validateMetadata(schema, xmlElement, context);
        }

        //--- if the uuid does not exist we generate it for metadata and templates
        String uuid;
        if (metadataType == MetadataType.SUB_TEMPLATE) {
            uuid = UUID.randomUUID().toString();
        } else {
            uuid = dataMan.extractUUID(schema, xmlElement);
            if (uuid.length() == 0) {
                uuid = UUID.randomUUID().toString();
                xmlElement = dataMan.setUUID(schema, uuid, xmlElement);
            }
        }
        String date = new ISODate().toString();

        final List<String> id = new ArrayList<String>();
        final List<Element> md = new ArrayList<Element>();
        md.add(xmlElement);


        // Import record
        SettingManager settingManager = appContext.getBean(SettingManager.class);
        Map<String, String> sourceTranslations = Maps.newHashMap();
        try {
            Importer.importRecord(uuid, uuidProcessing, md, schema, 0,
                settingManager.getSiteId(),
                settingManager.getSiteName(),
                sourceTranslations, context, id, date, date, group, metadataType);

        } catch (DataIntegrityViolationException ex) {
            throw new DataIntegrityViolationException(
                "Record can't be imported due to database constraint error.", ex);
        }
        int iId = Integer.parseInt(id.get(0));


        // Set template
        dataMan.setTemplate(iId, metadataType, null);

        dataMan.activateWorkflowIfConfigured(context, id.get(0), group);

        if (category != null) {
            for (String c : category) {
                dataMan.setCategory(context, id.get(0), c);
            }
        }

        if (extra != null) {
            context.getBean(MetadataRepository.class).update(iId, new Updater<Metadata>() {
                @Override
                public void apply(@Nonnull Metadata metadata) {
                    if (extra != null) {
                        metadata.getDataInfo().setExtra(extra);
                    }
                }
            });
        }

        dataMan.indexMetadata(id.get(0), true);
    }
}
