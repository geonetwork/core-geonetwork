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
import com.google.common.collect.Maps;

import io.swagger.annotations.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.XSDValidationErrorEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.mef.Importer;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.input.JDOMParseException;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import springfox.documentation.annotations.ApiIgnore;

import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUID;
import static org.springframework.data.jpa.domain.Specifications.where;

@RequestMapping(value = {
    "/api/records",
    "/api/" + API.VERSION_0_1 +
        "/records"
})
@Api(value = API_CLASS_RECORD_TAG,
    tags = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("recordInsertOrDelete")
@PreAuthorize("hasRole('Editor')")
@ReadWriteController
public class MetadataInsertDeleteApi {

    public static final String API_PARAM_REPORT_ABOUT_IMPORTED_RECORDS = "Report about imported records.";
    private final String API_PARAP_RECORD_GROUP = "The group the record is attached to.";
    private final String API_PARAM_RECORD_UUID_PROCESSING = "Record identifier processing.";
    private final String API_PARAM_RECORD_TAGS = "Tags to assign to the record.";
    private final String API_PARAM_RECORD_VALIDATE = "Validate the record first and reject it if not valid.";
    private final String API_PARAM_RECORD_XSL = "XSL transformation to apply to the record.";
    private final String API_PARAM_FORCE_SCHEMA = "Force the schema of the record. If not set, schema autodetection " +
        "is used (and is the preferred method).";
    private final String API_PARAM_BACKUP_FIRST = "Backup first the record as MEF in the metadata removed folder.";
    private final String API_PARAM_RECORD_TYPE = "The type of record.";

    @ApiOperation(
        value = "Delete a record",
        notes = "User MUST be able to edit the record to delete it. " +
            "By default, a backup is made in ZIP format. After that, " +
            "the record attachments are removed, the document removed " +
            "from the index and then from the database.",
        nickname = "deleteRecord")
    @RequestMapping(value = "/{metadataUuid}",
        method = RequestMethod.DELETE
    )
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Record deleted."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecord(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(
            value = API_PARAM_BACKUP_FIRST,
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
        SearchManager searchManager = appContext.getBean(SearchManager.class);

        if (metadata.getDataInfo().getType() != MetadataType.SUB_TEMPLATE && withBackup) {
            MetadataUtils.backupRecord(metadata, context);
        }

        IO.deleteFileOrDirectory(
            Lib.resource.getMetadataDir(context.getBean(GeonetworkDataDirectory.class),
                String.valueOf(metadata.getId())));

        dataManager.deleteMetadata(context, metadataUuid);

        searchManager.forceIndexChanges();
    }

    @ApiOperation(
        value = "Delete one or more records",
        notes = "User MUST be able to edit the record to delete it. " +
            "",
        nickname = "deleteRecords")
    @RequestMapping(
        method = RequestMethod.DELETE
    )
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Report about deleted records."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SimpleMetadataProcessingReport deleteRecords(
        @ApiParam(value = API_PARAM_RECORD_UUIDS_OR_SELECTION,
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
        @ApiParam(
            value = API_PARAM_BACKUP_FIRST,
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "true")
            boolean withBackup,
        @ApiIgnore
            HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);
        DataManager dataManager = appContext.getBean(DataManager.class);
        AccessManager accessMan = appContext.getBean(AccessManager.class);
        SearchManager searchManager = appContext.getBean(SearchManager.class);

        Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, ApiUtils.getUserSession(session));

        final MetadataRepository metadataRepository = appContext.getBean(MetadataRepository.class);
        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();
        for (String uuid : records) {
            Metadata metadata = metadataRepository.findOneByUuid(uuid);
            if (metadata == null) {
                report.incrementNullRecords();
            } else if (!accessMan.canEdit(context, String.valueOf(metadata.getId()))) {
                report.addNotEditableMetadataId(metadata.getId());
            } else {
                if (metadata.getDataInfo().getType() != MetadataType.SUB_TEMPLATE && withBackup) {
                    MetadataUtils.backupRecord(metadata, context);
                }

                IO.deleteFileOrDirectory(
                    Lib.resource.getMetadataDir(context.getBean(GeonetworkDataDirectory.class),
                        String.valueOf(metadata.getId())));

                dataManager.deleteMetadata(context, String.valueOf(metadata.getId()));

                report.incrementProcessedRecords();
                report.addMetadataId(metadata.getId());
            }
        }

        searchManager.forceIndexChanges();

        return report;
    }


    @ApiOperation(
        value = "Add a record",
        notes =
            "Add one or more record from an XML fragment, " +
            "URL or file in a folder on the catalog server. When loading" +
            "from the catalog server folder, it might be faster to use a " +
            "local filesystem harvester.",
        nickname = "insert")
    @RequestMapping(
        method = {
            RequestMethod.PUT
        },
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        },
        consumes = {
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_FORM_URLENCODED_VALUE
        }
    )
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = API_PARAM_REPORT_ABOUT_IMPORTED_RECORDS),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.CREATED)
    public
    @ResponseBody SimpleMetadataProcessingReport insert(
        @ApiParam(
            value = API_PARAM_RECORD_TYPE,
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
            value = "Server folder where to look for files.",
            required = false)
        @RequestParam(
            required = false
        )
            String serverFolder,
        @ApiParam(
            value = "(Server folder import only) Recursive search in folder.",
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
        final boolean recursiveSearch,
        @ApiParam(
            value = "(MEF file only) Assign to current catalog.",
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
        final boolean assignToCatalog,
        @ApiParam(
            value = API_PARAM_RECORD_UUID_PROCESSING,
            required = false,
            defaultValue = "NOTHING"
        )
        @RequestParam(
            required = false,
            defaultValue = "NOTHING"
        )
        final MEFLib.UuidAction uuidProcessing,
        @ApiParam(
            value = API_PARAP_RECORD_GROUP,
            required = false
        )
        @RequestParam(
            required = false
        )
        final String group,
        @ApiParam(
            value = API_PARAM_RECORD_TAGS,
            required = false)
        @RequestParam(
            required = false
        )
        final String[] category,
        @ApiParam(
            value = API_PARAM_RECORD_VALIDATE,
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
        final boolean rejectIfInvalid,
        @ApiParam(
            value = API_PARAM_RECORD_XSL,
            required = false,
            defaultValue = "_none_"
        )
        @RequestParam(
            required = false,
            defaultValue = "_none_"
        )
        final String transformWith,
        @ApiParam(
            value = API_PARAM_FORCE_SCHEMA,
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
        if (url == null && xml == null && serverFolder == null) {
            throw new IllegalArgumentException(String.format(
                "XML fragment or a URL or a server folder MUST be provided."));
        }
        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();

        if (xml != null) {
            Element element = null;
            try {
                element = Xml.loadString(xml, false);
            } catch (JDOMParseException ex) {
                throw new IllegalArgumentException(String.format(
                    "XML fragment is invalid. Error is %s",
                    ex.getMessage()
                ));
            }
            Pair<Integer, String> pair = loadRecord(
                metadataType, Xml.loadString(xml, false),
                uuidProcessing, group, category, rejectIfInvalid, transformWith, schema, extra, request);
            report.addMetadataInfos(pair.one(), String.format(
                "Metadata imported from XML with UUID '%s'", pair.two())
            );
            report.incrementProcessedRecords();
        }
        if (url != null) {
            for (String u : url) {
                Element xmlContent = null;
                try {
                    xmlContent = Xml.loadFile(ApiUtils.downloadUrlInTemp(u));
                } catch (Exception e) {
                    report.addError(e);
                }
                if (xmlContent != null) {
                    Pair<Integer, String> pair = loadRecord(
                        metadataType, xmlContent,
                        uuidProcessing, group, category, rejectIfInvalid, transformWith, schema, extra, request);
                    report.addMetadataInfos(pair.one(), String.format(
                        "Metadata imported from URL with UUID '%s'", pair.two())
                    );
                }
                report.incrementProcessedRecords();
            }
        }
        if (serverFolder != null) {
            Path serverFolderPath = IO.toPath(serverFolder);

            final List<Path> files = Lists.newArrayList();
            final MEFLib.MefOrXmlFileFilter predicate = new MEFLib.MefOrXmlFileFilter();
            if (recursiveSearch) {
                Files.walkFileTree(serverFolderPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (predicate.accept(file)) {
                            files.add(file);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                try (DirectoryStream<Path> paths = Files.newDirectoryStream(serverFolderPath, predicate)) {
                    for (Path file : paths) {
                        files.add(file);
                    }
                }
            }

            if (files.size() == 0) {
                throw new Exception(String.format(
                    "No XML or MEF or ZIP file found in server folder '%s'.",
                    serverFolder
                ));
            }
            SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
            ServiceContext context = ApiUtils.createServiceContext(request);
            for (Path f : files) {
                if (MEFLib.isValidArchiveExtensionForMEF(f.getFileName().toString())) {
                    try {
                        MEFLib.Version version = MEFLib.getMEFVersion(f);
                        List<String> ids = MEFLib.doImport(
                            version == MEFLib.Version.V1 ? "mef" : "mef2",
                            uuidProcessing, transformWith,
                            settingManager.getSiteId(),
                            metadataType, category, group, rejectIfInvalid,
                            assignToCatalog, context, f);
                        for (String id : ids) {
                            report.addMetadataInfos(Integer.parseInt(id), String.format(
                                "Metadata imported from MEF with id '%s'", id)
                            );
                            report.incrementProcessedRecords();
                        }
                    } catch (Exception e) {
                        report.addError(e);
                        report.addInfos(String.format(
                            "Failed to import MEF file '%s'. Check error for details.",
                            f.getFileName().toString()));
                    }
                } else {
                    try {
                        Pair<Integer, String> pair = loadRecord(
                            metadataType, Xml.loadFile(f),
                            uuidProcessing, group, category, rejectIfInvalid, transformWith, schema, extra, request);
                        report.addMetadataInfos(pair.one(), String.format(
                            "Metadata imported from server folder with UUID '%s'", pair.two())
                        );
                    } catch (Exception e) {
                        report.addError(e);
                    }
                    report.incrementProcessedRecords();
                }

            }
        }
        report.close();
        return report;
    }


    @ApiOperation(
        value = "Create a new record",
        notes =
            "Create a record from a template or by copying an existing record." +
            "Return the UUID of the newly created record. Existing links in the " +
            "source record are preserved, this means that the new record may " +
            "contains link to the source attachements. They need to be manually " +
            "updated after creation.",
        nickname = "create")
    @RequestMapping(
        value = "/duplicate",
        method = {
            RequestMethod.PUT
        },
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        },
        consumes = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Return the internal id of the newly created record."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.CREATED)
    public
    @ResponseBody
    String create(
        @ApiParam(
            value = API_PARAM_RECORD_TYPE,
            required = false,
            defaultValue = "METADATA"
        )
        @RequestParam(
            required = false,
            defaultValue = "METADATA"
        )
        final MetadataType metadataType,
        @ApiParam(
            value = "UUID of the source record to copy.",
            required = true
        )
        @RequestParam(
            required = true
        )
            String sourceUuid,
        @ApiParam(
            value = "Assign a custom UUID. If this UUID already exist an error is returned. " +
                "This is enabled only if metadata create / generate UUID settings is activated.",
            required = false
        )
        @RequestParam(
            required = false
        )
            String targetUuid,
        @ApiParam(
            value = API_PARAP_RECORD_GROUP,
            required = true
        )
        @RequestParam(
            required = true
        )
        final String group,
        @ApiParam(
            value = "Is published to all user group members? " +
                "If not, only the author and administrator can edit the record.",
            required = false,
            defaultValue = "false"
        )
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
        // TODO: Would be more flexible to add a privilege object ?
        final boolean isVisibleByAllGroupMembers,
        @ApiParam(
            value = API_PARAM_RECORD_TAGS,
            required = false)
        @RequestParam(
            required = false
        )
        final String[] category,
        @ApiParam(
        value = "Is child of the record to copy?",
        required = false,
        defaultValue = "false"
    )
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
        final boolean isChildOfSource,
        @ApiIgnore
        @ApiParam(hidden = true)
        HttpSession httpSession,
        HttpServletRequest request
    )
        throws Exception {

        Metadata sourceMetadata = ApiUtils.getRecord(sourceUuid);
        ApplicationContext applicationContext = ApplicationContextHolder.get();

        SettingManager sm = applicationContext.getBean(SettingManager.class);
        boolean generateUuid = sm.getValueAsBool(Settings.SYSTEM_METADATACREATE_GENERATE_UUID);


        // User assigned uuid: check if already exists
        String metadataUuid = null;
        if (generateUuid) {
            if (StringUtils.isEmpty(targetUuid)) {
                // Create a random UUID
                metadataUuid = UUID.randomUUID().toString();
            } else {
                // Check if the UUID exists
                try {
                    Metadata checkRecord = ApiUtils.getRecord(targetUuid);
                    if (checkRecord != null) {
                        throw new BadParameterEx(String.format(
                            "You can't create a new record with the UUID '%s' because a record already exist with this UUID.",
                            targetUuid), targetUuid);
                    }
                } catch (ResourceNotFoundException e) {
                    // Ignore. Ok to create a new record with the requested UUID.
                }
            }
        } else {
            metadataUuid = UUID.randomUUID().toString();
        }


        // TODO : Check user can create a metadata in that group
        UserSession user = ApiUtils.getUserSession(httpSession);
        if (user.getProfile() != Profile.Administrator) {
            final Specifications<UserGroup> spec = where(UserGroupSpecs.hasProfile(Profile.Editor))
                .and(UserGroupSpecs.hasUserId(user.getUserIdAsInt()))
                .and(UserGroupSpecs.hasGroupId(Integer.valueOf(group)));

            final List<UserGroup> userGroups = applicationContext.getBean(UserGroupRepository.class).findAll(spec);

            if (userGroups.size() == 0) {
                throw new SecurityException(String.format(
                    "You can't create a record in this group. User MUST be an Editor in that group"
                ));
            }
        }

        DataManager dataManager = applicationContext.getBean(DataManager.class);
        ServiceContext context = ApiUtils.createServiceContext(request);
        String newId = dataManager.createMetadata(context,
            String.valueOf(sourceMetadata.getId()),
            group,
            sm.getSiteId(),
            context.getUserSession().getUserIdAsInt(),
            isChildOfSource ? sourceMetadata.getUuid() : null,
            metadataType.toString(),
            isVisibleByAllGroupMembers,
            metadataUuid);

        dataManager.activateWorkflowIfConfigured(context, newId, group);

        try {
            copyDataDir(context, sourceMetadata.getId(), newId, Params.Access.PUBLIC);
            copyDataDir(context, sourceMetadata.getId(), newId, Params.Access.PRIVATE);
        } catch (IOException e) {
            Log.warning(Geonet.DATA_MANAGER, String.format(
                "Error while copying metadata resources. Error is %s. " +
                    "Metadata is created but without resources from the source record with id '%d':",
                    e.getMessage(), newId));
        }

        return newId;
    }


    private void copyDataDir(ServiceContext context, int oldId, String newId, String access) throws IOException {
        final Path sourceDir = Lib.resource.getDir(context, access, oldId);
        final Path destDir = Lib.resource.getDir(context, access, newId);

        if (Files.exists(sourceDir)) {
            IO.copyDirectoryOrFile(sourceDir, destDir, false);
        }
    }

    @ApiOperation(
        value = "Add a record from XML or MEF/ZIP file",
        notes = "Add record in the catalog by uploading files.",
        nickname = "insertFile")
    @RequestMapping(
        method = {
            RequestMethod.POST,
        },
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = API_PARAM_REPORT_ABOUT_IMPORTED_RECORDS),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public SimpleMetadataProcessingReport insertFile(
        @ApiParam(
            value = API_PARAM_RECORD_TYPE,
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
            value = API_PARAM_RECORD_UUID_PROCESSING,
            required = false,
            defaultValue = "NOTHING"
        )
        @RequestParam(
            required = false,
            defaultValue = "NOTHING"
        )
        final MEFLib.UuidAction uuidProcessing,
        @ApiParam(
            value = API_PARAP_RECORD_GROUP,
            required = false
        )
        @RequestParam(
            required = false
        )
        final String group,
        @ApiParam(
            value = API_PARAM_RECORD_TAGS,
            required = false)
        @RequestParam(
            required = false
        )
        final String[] category,
        @ApiParam(
            value = API_PARAM_RECORD_VALIDATE,
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
        final boolean rejectIfInvalid,
        @ApiParam(
            value = "(MEF file only) Assign to current catalog.",
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
        final boolean assignToCatalog,
        @ApiParam(
            value = API_PARAM_RECORD_XSL,
            required = false,
            defaultValue = "_none_"
        )
        @RequestParam(
            required = false,
            defaultValue = "_none_"
        )
        final String transformWith,
        @ApiParam(
            value = API_PARAM_FORCE_SCHEMA,
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
        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();
        if (file != null) {
            ServiceContext context = ApiUtils.createServiceContext(request);
            ApplicationContext applicationContext = ApplicationContextHolder.get();
            SettingManager settingManager = applicationContext.getBean(SettingManager.class);
            for (MultipartFile f : file) {
                if (MEFLib.isValidArchiveExtensionForMEF(f.getOriginalFilename())) {
                    Path tempFile = Files.createTempFile("mef-import", ".zip");
                    try {
                        FileUtils.copyInputStreamToFile(f.getInputStream(), tempFile.toFile());

                        MEFLib.Version version = MEFLib.getMEFVersion(tempFile);

                        List<String> ids = MEFLib.doImport(
                            version == MEFLib.Version.V1 ? "mef" : "mef2",
                            uuidProcessing, transformWith,
                            settingManager.getSiteId(),
                            metadataType, category, group, rejectIfInvalid,
                            assignToCatalog, context, tempFile);
                        ids.forEach(e -> {
                            report.addMetadataInfos(Integer.parseInt(e), String.format(
                                "Metadata imported with ID '%s'", e)
                            );
                            report.incrementProcessedRecords();
                        });
                    } catch (Exception e) {
                        report.addError(e);
                        report.addInfos(String.format(
                            "Failed to import MEF file '%s'. Check error for details.",
                            f.getOriginalFilename()));
                    } finally {
                        IO.deleteFile(tempFile, false, Geonet.MEF);
                    }
                } else {
                    Pair<Integer, String> pair = loadRecord(
                        metadataType, Xml.loadStream(f.getInputStream()),
                        uuidProcessing, group, category, rejectIfInvalid, transformWith, schema, extra, request);
                    report.addMetadataInfos(pair.one(), String.format(
                        "Metadata imported with UUID '%s'", pair.two())
                    );
                    report.incrementProcessedRecords();
                }
            }
        }
        report.close();
        return report;
    }


    private Pair<Integer, String> loadRecord(
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
            FilePathChecker.verify(transformWith);
            Path xslFile = folder.resolve(transformWith + ".xsl");
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
                throw new IllegalArgumentException(
                    "Can't detect schema for metadata automatically. " +
                        "You could try to force the schema with the schema parameter."
                );
                // TODO: Report what are the supported schema
            }
        } else {
            // TODO: Check that the schema is supported
        }

        if (rejectIfInvalid) {
            try {
                DataManager.validateMetadata(schema, xmlElement, context);
            } catch (XSDValidationErrorEx e) {
                throw new IllegalArgumentException(e);
            }
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


        if (uuidProcessing == MEFLib.UuidAction.NOTHING) {
            MetadataRepository metadataRepository = appContext.getBean(MetadataRepository.class);
            Metadata md = metadataRepository.findOneByUuid(uuid);
            if (md != null) {
                throw new IllegalArgumentException(String.format(
                    "A record with UUID '%s' already exist and you choose no " +
                        "action on UUID processing. Choose to overwrite existing record " +
                        "or to generate a new UUID.",
                    uuid
                ));
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
        }catch (Exception ex) {
            throw new Exception(
                "Record can't be imported due to the following error.", ex);
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
        return Pair.read(Integer.valueOf(id.get(0)), uuid);
    }
}
