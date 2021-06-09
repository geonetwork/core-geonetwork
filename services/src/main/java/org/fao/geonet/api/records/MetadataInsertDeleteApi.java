/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.api.records.attachments.StoreUtils;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.utils.ObjectJSONUtils;
import org.fao.geonet.events.history.RecordCreateEvent;
import org.fao.geonet.events.history.RecordDeletedEvent;
import org.fao.geonet.events.history.RecordImportedEvent;
import org.fao.geonet.events.md.MetadataPreRemove;
import org.fao.geonet.exceptions.BadFormatEx;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.XSDValidationErrorEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataOperations;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.datamanager.IMetadataValidator;
import org.fao.geonet.kernel.mef.Importer;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.util.UserUtil;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.input.JDOMParseException;
import org.jdom.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUID;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION;
import static org.springframework.data.jpa.domain.Specification.where;


@RequestMapping(value = {"/{portal}/api/records"})
@Tag(name = API_CLASS_RECORD_TAG, description = API_CLASS_RECORD_OPS)
@Controller("recordInsertOrDelete")
@PreAuthorize("hasAuthority('Editor')")
@ReadWriteController
public class MetadataInsertDeleteApi {
    /**
     * Logger name.
     */
    public static final String LOGGER = Geonet.GEONETWORK + ".api.metadatainsertdelete";
    public static final String API_PARAM_REPORT_ABOUT_IMPORTED_RECORDS = "Report about imported records.";
    public static final String API_PARAM_RECORD_GROUP = "The group the record is attached to.";
    public static final String API_PARAM_RECORD_UUID_PROCESSING = "Record identifier processing.";
    private static final String API_PARAM_RECORD_TAGS = "Tags to assign to the record.";
    private static final String API_PARAM_RECORD_VALIDATE = "Validate the record first and reject it if not valid.";
    private static final String API_PARAM_RECORD_XSL = "XSL transformation to apply to the record.";
    private static final String API_PARAM_FORCE_SCHEMA = "Force the schema of the record. If not set, schema autodetection "
        + "is used (and is the preferred method).";
    private static final String API_PARAM_BACKUP_FIRST = "Backup first the record as MEF in the metadata removed folder.";
    private static final String API_PARAM_RECORD_TYPE = "The type of record.";
    @Autowired
    MetadataDraftRepository metadataDraftRepository;
    @Autowired
    EsSearchManager searchManager;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private AccessManager accessMan;
    @Autowired
    private MetadataRepository metadataRepository;
    @Autowired
    private SettingManager settingManager;

    @Autowired
    private SchemaManager schemaManager;

    @Autowired
    private GeonetworkDataDirectory dataDirectory;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private IMetadataManager metadataManager;

    @Autowired
    private AccessManager accessManager;

    @Autowired
    IMetadataUtils metadataUtils;

    @Autowired
    IMetadataOperations metadataOperations;

    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    RoleHierarchy roleHierarchy;

    @Autowired
    IMetadataValidator metadataValidator;

    @io.swagger.v3.oas.annotations.Operation(summary = "Delete a record", description = "User MUST be able to edit the record to delete it. "
        + "By default, a backup is made in ZIP format. After that, "
        + "the record attachments are removed, the document removed "
        + "from the index and then from the database.")
    @RequestMapping(value = "/{metadataUuid:.+}", method = RequestMethod.DELETE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Record deleted.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "401", description = "This template is referenced"),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecord(
        @Parameter(description = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
        @Parameter(description = API_PARAM_BACKUP_FIRST, required = false) @RequestParam(required = false, defaultValue = "true") boolean withBackup,
        HttpServletRequest request) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext context = ApiUtils.createServiceContext(request);
        Store store = context.getBean("resourceStore", Store.class);

        MetadataPreRemove preRemoveEvent = new MetadataPreRemove(metadata);
        ApplicationContextHolder.get().publishEvent(preRemoveEvent);

        if (metadata.getDataInfo().getType() != MetadataType.SUB_TEMPLATE
            && metadata.getDataInfo().getType() != MetadataType.TEMPLATE_OF_SUB_TEMPLATE && withBackup) {
            MEFLib.backupRecord(metadata, context);
        }

        boolean approved=true;
        if (metadata instanceof MetadataDraft) {
            approved=false;
        }

        UserSession userSession = ApiUtils.getUserSession(request.getSession());
        if (accessMan.isVisibleToAll(String.valueOf(metadata.getId())) ) {
            UserUtil.checkUserProfileLevel(userSession, settingManager, roleHierarchy, Settings.METADATA_PUBLISHED_DELETE_USERPROFILE, Profile.Editor, "delete published metadata");
        }

        store.delResources(context, metadata.getUuid(), approved);
        RecordDeletedEvent recordDeletedEvent = triggerDeletionEvent(request, metadata.getId() + "");
        metadataManager.deleteMetadata(context, metadata.getId() + "");
        recordDeletedEvent.publish(ApplicationContextHolder.get());

        dataManager.forceIndexChanges();
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Delete one or more records", description ="User MUST be able to edit the record to delete it. "
        + "")
    @RequestMapping(
        method = RequestMethod.DELETE,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Report about deleted records."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)})
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SimpleMetadataProcessingReport deleteRecords(
        @Parameter(description = API_PARAM_RECORD_UUIDS_OR_SELECTION, required = false, example = "") @RequestParam(required = false) String[] uuids,
        @Parameter(description = ApiParams.API_PARAM_BUCKET_NAME, required = false) @RequestParam(required = false) String bucket,
        @Parameter(description = API_PARAM_BACKUP_FIRST, required = false) @RequestParam(required = false, defaultValue = "true") boolean withBackup,
        @Parameter(hidden = true) HttpSession session, HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        Store store = context.getBean("resourceStore", Store.class);

        Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, ApiUtils.getUserSession(session));
        UserSession userSession = ApiUtils.getUserSession(request.getSession());

        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();
        for (String uuid : records) {
            AbstractMetadata metadata = metadataRepository.findOneByUuid(uuid);
            if (metadata == null) {
                report.incrementNullRecords();
            } else if (!accessManager.canEdit(context, String.valueOf(metadata.getId()))
                || metadataDraftRepository.findOneByUuid(uuid) != null) {
                report.addNotEditableMetadataId(metadata.getId());
            } else {

                if (accessMan.isVisibleToAll(String.valueOf(metadata.getId())) ) {
                    try {
                        UserUtil.checkUserProfileLevel(userSession, settingManager, roleHierarchy, Settings.METADATA_PUBLISHED_DELETE_USERPROFILE, Profile.Editor, "delete published metadata");
                    } catch (NotAllowedException ex) {
                        report.addMetadataInfos(metadata, ex.getMessage());
                        continue;
                    }
                }


                MetadataPreRemove preRemoveEvent = new MetadataPreRemove(metadata);
                ApplicationContextHolder.get().publishEvent(preRemoveEvent);

                if (metadata.getDataInfo().getType() != MetadataType.SUB_TEMPLATE
                    && metadata.getDataInfo().getType() != MetadataType.TEMPLATE_OF_SUB_TEMPLATE && withBackup) {
                    MEFLib.backupRecord(metadata, context);
                }

                store.delResources(context, metadata.getUuid());

                RecordDeletedEvent recordDeletedEvent = triggerDeletionEvent(request, String.valueOf(metadata.getId()));
                metadataManager.deleteMetadata(context, String.valueOf(metadata.getId()));
                recordDeletedEvent.publish(ApplicationContextHolder.get());

                report.incrementProcessedRecords();
                report.addMetadataId(metadata.getId());
            }
        }
        report.close();
        return report;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Add a record", description = "Add one or more record from an XML fragment, "
        + "URL or file in a folder on the catalog server. When loading"
        + "from the catalog server folder, it might be faster to use a "
        + "local filesystem harvester.")
    @RequestMapping(method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_XML_VALUE)
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = API_PARAM_REPORT_ABOUT_IMPORTED_RECORDS),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)})
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    SimpleMetadataProcessingReport insert(
        @Parameter(description = API_PARAM_RECORD_TYPE, required = false) @RequestParam(required = false, defaultValue = "METADATA") final MetadataType metadataType,
        @Parameter(description = "XML fragment.", required = false) @RequestBody(required = false) String xml,
        @Parameter(description = "URL of a file to download and insert.", required = false) @RequestParam(required = false) String[] url,
        @Parameter(description = "Server folder where to look for files.", required = false) @RequestParam(required = false) String serverFolder,
        @Parameter(description = "(Server folder import only) Recursive search in folder.", required = false) @RequestParam(required = false, defaultValue = "false") final boolean recursiveSearch,
        @Parameter(description = "(XML file only and if workflow is not enabled) Publish record.", required = false) @RequestParam(required = false, defaultValue = "false") boolean publishToAll,
        @Parameter(description = "(MEF file only) Assign to current catalog.", required = false) @RequestParam(required = false, defaultValue = "false") final boolean assignToCatalog,
        @Parameter(description = API_PARAM_RECORD_UUID_PROCESSING, required = false) @RequestParam(required = false, defaultValue = "NOTHING") final MEFLib.UuidAction uuidProcessing,
        @Parameter(description = API_PARAM_RECORD_GROUP, required = false) @RequestParam(required = false) final String group,
        @Parameter(description = API_PARAM_RECORD_TAGS, required = false) @RequestParam(required = false) final String[] category,
        @Parameter(description = API_PARAM_RECORD_VALIDATE, required = false) @RequestParam(required = false, defaultValue = "false") final boolean rejectIfInvalid,
        @Parameter(description = API_PARAM_RECORD_XSL, required = false) @RequestParam(required = false, defaultValue = "_none_") final String transformWith,
        @Parameter(description = API_PARAM_FORCE_SCHEMA, required = false) @RequestParam(required = false) String schema,
        @Parameter(description = "Is editable by group members with editor profile? "
            + "If not, only the author and administrator can edit the record.", required = false) @RequestParam(required = false, defaultValue = "false") final boolean allowEditGroupMembers,
        @Parameter(description = "(experimental) Add extra information to the record.", required = false) @RequestParam(required = false) final String extra,
         HttpServletRequest request) throws Exception {

        ResourceBundle messages = ApiUtils.getMessagesResourceBundle(request.getLocales());

        if (url == null && xml == null && serverFolder == null) {
            throw new IllegalArgumentException(
                messages.getString("api.metadata.import.errorMissingXMLFragmentOrUrl"));
        }
        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();

        UserSession userSession = ApiUtils.getUserSession(request.getSession());
        UserUtil.checkUserProfileLevel(userSession, settingManager, roleHierarchy, Settings.METADATA_IMPORT_USERPROFILE, Profile.Editor, "import metadata");


        boolean isMdWorkflowEnable = settingManager.getValueAsBool(Settings.METADATA_WORKFLOW_ENABLE);
        if (isMdWorkflowEnable) {
            publishToAll = false;
        }

        if (xml != null) {
            Element element = null;
            try {
                element = Xml.loadString(xml, false);
            } catch (JDOMParseException ex) {
                throw new IllegalArgumentException(
                    String.format(messages.getString("api.metadata.import.errorInvalidXMLFragment"), ex.getMessage()));
            }

            Pair<Integer, String> pair = loadRecord(metadataType, element, uuidProcessing, group, category,
                    rejectIfInvalid, publishToAll, allowEditGroupMembers, transformWith, schema, extra, request);
            report.addMetadataInfos(pair.one(), pair.two(), !publishToAll, false, String.format(messages.getString("api.metadata.import.importedFromXMLWithUuid"), pair.two()));

            triggerImportEvent(request, pair.two());

            report.incrementProcessedRecords();
        }
        if (url != null) {
            for (String u : url) {
                Element xmlContent = null;
                Path tempFile = null;
                try {
                    tempFile = ApiUtils.downloadUrlInTemp(u);
                    xmlContent = Xml.loadFile(tempFile);
                } catch (Exception e) {
                    Log.error(LOGGER, String.format("Error importing metadata from '%s'.", url), e);
                    report.addError(new Exception(
                        String.format(messages.getString("api.metadata.import.errorFromUrl"), url)));
                } finally {
                    if (tempFile != null) {
                        FileUtils.deleteQuietly(tempFile.toFile());
                    }
                }
                if (xmlContent != null) {
                    Pair<Integer, String> pair = loadRecord(metadataType, xmlContent, uuidProcessing, group, category,
                            rejectIfInvalid, publishToAll, allowEditGroupMembers, transformWith, schema, extra, request);
                    report.addMetadataInfos(pair.one(), pair.two(), !publishToAll, false,
                            String.format(messages.getString("api.metadata.import.importedFromUrl"), pair.two()));
                    triggerImportEvent(request, pair.two());

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
                throw new Exception(
                    String.format(messages.getString("api.metadata.import.errorMissingMEF"), serverFolder));
            }
            ServiceContext context = ApiUtils.createServiceContext(request);
            for (Path f : files) {
                if (MEFLib.isValidArchiveExtensionForMEF(f.getFileName().toString())) {
                    try {
                        MEFLib.Version version = MEFLib.getMEFVersion(f);
                        List<String> ids = MEFLib.doImport(version == MEFLib.Version.V1 ? "mef" : "mef2",
                            uuidProcessing, transformWith, settingManager.getSiteId(), metadataType, category,
                            group, rejectIfInvalid, assignToCatalog, context, f);
                        for (String id : ids) {
                            report.addMetadataInfos(Integer.parseInt(id), id, !publishToAll, false,
                                    String.format(messages.getString("api.metadata.import.importedFromMEF"), id));
                            triggerCreationEvent(request, id);

                            report.incrementProcessedRecords();
                        }
                    } catch (Exception e) {
                        report.addError(e);
                        report.addInfos(String.format(messages.getString("api.metadata.import.errorImportMEF"),
                            f.getFileName().toString()));
                    }
                } else {
                    try {
                        Pair<Integer, String> pair = loadRecord(metadataType, Xml.loadFile(f), uuidProcessing, group,
                                category, rejectIfInvalid, publishToAll, allowEditGroupMembers, transformWith, schema, extra, request);
                        report.addMetadataInfos(pair.one(), pair.two(), !publishToAll, false,
                                String.format(messages.getString("api.metadata.import.importedFromServerFolder"), pair.two()));

                        triggerCreationEvent(request, pair.two());

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

    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new record", description ="Create a record from a template or by copying an existing record."
        + "Return the UUID of the newly created record. Existing links in the "
        + "source record are preserved, this means that the new record may "
        + "contains link to the source attachments. They need to be manually "
        + "updated after creation.")
    @RequestMapping(value = "/duplicate", method = {RequestMethod.PUT}, produces = {
        MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Return the internal id of the newly created record."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)})
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    String create(
        @Parameter(description = API_PARAM_RECORD_TYPE, required = false) @RequestParam(required = false, defaultValue = "METADATA") final MetadataType metadataType,
        @Parameter(description = "UUID of the source record to copy.", required = true) @RequestParam(required = true) String sourceUuid,
        @Parameter(description = "Assign a custom UUID. If this UUID already exist an error is returned. "
            + "This is enabled only if metadata create / generate UUID settings is activated.", required = false) @RequestParam(required = false) String targetUuid,
        @Parameter(description = API_PARAM_RECORD_GROUP, required = true) @RequestParam(required = true) final String group,
        @Parameter(description = "Is editable by group members with editor profile? "
            + "If not, only the author and administrator can edit the record.", required = false) @RequestParam(required = false, defaultValue = "false") final boolean allowEditGroupMembers,
        @Parameter(description = API_PARAM_RECORD_TAGS, required = false) @RequestParam(required = false) final String[] category,
        @Parameter(description = "Copy categories from source?", required = false) @RequestParam(required = false, defaultValue = "false") final boolean hasCategoryOfSource,
        @Parameter(description = "Is child of the record to copy?", required = false) @RequestParam(required = false, defaultValue = "false") final boolean isChildOfSource,
        @Parameter(description = "Copy attachments from source?", required = false) @RequestParam(required = false, defaultValue = "true") final boolean hasAttachmentsOfSource,
        @Parameter(hidden = true) HttpSession httpSession, HttpServletRequest request) throws Exception {

        ResourceBundle messages = ApiUtils.getMessagesResourceBundle(request.getLocales());

        AbstractMetadata sourceMetadata = ApiUtils.getRecord(sourceUuid);

        boolean generateUuid = settingManager.getValueAsBool(Settings.SYSTEM_METADATACREATE_GENERATE_UUID);

        // User assigned uuid: check if already exists
        String metadataUuid = null;


        if (!generateUuid && !StringUtils.isEmpty(targetUuid)) {
            // Check if the UUID exists
            try {
                ApiUtils.getRecord(targetUuid);
                throw new BadParameterEx(String.format(
                    messages.getString("api.metadata.import.errorDuplicatedUUID"),
                    targetUuid), targetUuid);
            } catch (ResourceNotFoundException e) {
                metadataUuid = targetUuid;
            }
        } else {
            metadataUuid = UUID.randomUUID().toString();
        }

        // TODO : Check user can create a metadata in that group
        UserSession user = ApiUtils.getUserSession(httpSession);
        if (user.getProfile() != Profile.Administrator) {
            final Specification<UserGroup> spec = where(UserGroupSpecs.hasProfile(Profile.Editor))
                .and(UserGroupSpecs.hasUserId(user.getUserIdAsInt()))
                .and(UserGroupSpecs.hasGroupId(Integer.valueOf(group)));

            final List<UserGroup> userGroups = userGroupRepository.findAll(spec);

            if (userGroups.isEmpty()) {
                throw new SecurityException(
                    messages.getString("api.metadata.import.errorNotEditorInGroup"));
            }
        }

        ServiceContext context = ApiUtils.createServiceContext(request);
        String newId = dataManager.createMetadata(context, String.valueOf(sourceMetadata.getId()), group,
            settingManager.getSiteId(), context.getUserSession().getUserIdAsInt(),
            isChildOfSource ? sourceMetadata.getUuid() : null, metadataType.toString(), allowEditGroupMembers,
            metadataUuid);

        triggerCreationEvent(request, newId);

        dataManager.activateWorkflowIfConfigured(context, newId, group);

        if (hasAttachmentsOfSource) {
            try {
                StoreUtils.copyDataDir(context, sourceMetadata.getId(), Integer.parseInt(newId), true);
            } catch (Exception e) {
                Log.warning(Geonet.DATA_MANAGER,
                    String.format(
                        "Error while copying metadata resources. Error is %s. "
                            + "Metadata is created but without resources from the source record with id '%s':",
                        e.getMessage(), newId));
            }
        }

        if (hasCategoryOfSource) {
            final Collection<MetadataCategory> categories = dataManager.getCategories(sourceMetadata.getId() + "");
            try {
                for (MetadataCategory c : categories) {
                    dataManager.setCategory(context, newId, c.getId() + "");
                }
            } catch (Exception e) {
                Log.warning(Geonet.DATA_MANAGER,
                    String.format("Error while copying source record category to new record. Error is %s. "
                            + "Metadata is created but without the categories from the source record with id '%d':",
                        e.getMessage(), newId));
            }
        }

        if (category != null && category.length > 0) {
            try {
                for (String c : category) {
                    dataManager.setCategory(context, newId, c);
                }
            } catch (Exception e) {
                Log.warning(Geonet.DATA_MANAGER,
                    String.format(
                        "Error while setting record category to new record. Error is %s. "
                            + "Metadata is created but without the requested categories.",
                        e.getMessage(), newId));
            }
        }

        return newId;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Add a record from XML or MEF/ZIP file", description ="Add record in the catalog by uploading files.")
    @RequestMapping(method = {RequestMethod.POST,}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = API_PARAM_REPORT_ABOUT_IMPORTED_RECORDS),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)})
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public SimpleMetadataProcessingReport insertFile(
        @Parameter(description = API_PARAM_RECORD_TYPE, required = false) @RequestParam(required = false, defaultValue = "METADATA") final MetadataType metadataType,
        @Parameter(description = "XML or MEF file to upload", required = false) @RequestParam(value = "file", required = false) MultipartFile[] file,
        @Parameter(description = API_PARAM_RECORD_UUID_PROCESSING, required = false) @RequestParam(required = false, defaultValue = "NOTHING") final MEFLib.UuidAction uuidProcessing,
        @Parameter(description = API_PARAM_RECORD_GROUP, required = false) @RequestParam(required = false) final String group,
        @Parameter(description = API_PARAM_RECORD_TAGS, required = false) @RequestParam(required = false) final String[] category,
        @Parameter(description = API_PARAM_RECORD_VALIDATE, required = false) @RequestParam(required = false, defaultValue = "false") final boolean rejectIfInvalid,
        @Parameter(description = "(XML file only) Publish record.", required = false) @RequestParam(required = false, defaultValue = "false") boolean publishToAll,
        @Parameter(description = "(MEF file only) Assign to current catalog.", required = false) @RequestParam(required = false, defaultValue = "false") final boolean assignToCatalog,
        @Parameter(description = API_PARAM_RECORD_XSL, required = false) @RequestParam(required = false, defaultValue = "_none_") final String transformWith,
        @Parameter(description = API_PARAM_FORCE_SCHEMA, required = false) @RequestParam(required = false) String schema,
        @Parameter(description = "(experimental) Add extra information to the record.", required = false) @RequestParam(required = false) final String extra,
        @Parameter(description = "Is editable by group members with editor profile? "
            + "If not, only the author and administrator can edit the record.", required = false) @RequestParam(required = false, defaultValue = "false") final boolean allowEditGroupMembers,
        HttpServletRequest request) throws Exception {

        ResourceBundle messages = ApiUtils.getMessagesResourceBundle(request.getLocales());

        if (file == null) {
            throw new IllegalArgumentException(messages.getString("api.metadata.import.errorFileRequired"));
        }

        boolean isMdWorkflowEnable = settingManager.getValueAsBool(Settings.METADATA_WORKFLOW_ENABLE);
        if (isMdWorkflowEnable) {
            publishToAll = false;
        }

        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();
        if (file != null) {
            ServiceContext context = ApiUtils.createServiceContext(request);

            UserUtil.checkUserProfileLevel(context.getUserSession(), settingManager, roleHierarchy, Settings.METADATA_IMPORT_USERPROFILE, Profile.Editor, "import metadata");

            for (MultipartFile f : file) {
                if (MEFLib.isValidArchiveExtensionForMEF(f.getOriginalFilename())) {
                    Path tempFile = Files.createTempFile("mef-import", ".zip");
                    try {
                        FileUtils.copyInputStreamToFile(f.getInputStream(), tempFile.toFile());

                        MEFLib.Version version = MEFLib.getMEFVersion(tempFile);

                        List<String> ids = MEFLib.doImport(version == MEFLib.Version.V1 ? "mef" : "mef2",
                                uuidProcessing, transformWith, settingManager.getSiteId(), metadataType, category,
                                group, rejectIfInvalid, assignToCatalog, context, tempFile);
                        if (ids.isEmpty()) {
                            //we could have used a finer-grained error handling inside the MEFLib import call (MEF MD file processing)
                            //This is a catch-for-call for the case when there is no record is imported, to notify the user the import is not successful.
                            throw new BadFormatEx(messages.getString("api.metadata.import.errorInvalidMEF"));
                        }
                        boolean finalPublishToAll = publishToAll;
                        ids.forEach(e -> {
                            report.addMetadataInfos(Integer.parseInt(e), e, !finalPublishToAll, false,
                                    String.format(messages.getString("api.metadata.import.importedWithId"), e));

                            try {
                                triggerCreationEvent(request, e);
                            } catch (Exception e1) {
                                report.addError(e1);
                                report.addInfos(
                                    String.format(messages.getString("api.metadata.import.errorEventStore"),
                                        f.getOriginalFilename()));
                            }

                            report.incrementProcessedRecords();
                        });
                    } catch (Exception e) {
                        report.addError(e);
                        report.addInfos(String.format(messages.getString("api.metadata.import.errorImportMEF"),
                            f.getOriginalFilename()));
                    } finally {
                        IO.deleteFile(tempFile, false, Geonet.MEF);
                    }
                } else {
                    Pair<Integer, String> pair = loadRecord(metadataType, Xml.loadStream(f.getInputStream()),
                            uuidProcessing, group, category, rejectIfInvalid, publishToAll, allowEditGroupMembers, transformWith, schema,
                            extra, request);
                    report.addMetadataInfos(pair.one(), pair.two(), !publishToAll, false, String.format(messages.getString("api.metadata.import.importedWithUuid"), pair.two()));

                    triggerImportEvent(request, pair.two());

                    report.incrementProcessedRecords();
                }
            }
        }
        report.close();
        return report;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Add a map metadata record from OGC OWS context", description ="Add record in the catalog by uploading a map context.")
    @RequestMapping(value = "/importfrommap", method = {RequestMethod.POST,}, produces = {
        MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = API_PARAM_REPORT_ABOUT_IMPORTED_RECORDS),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)})
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public SimpleMetadataProcessingReport insertOgcMapContextFile(
        @Parameter(description = "A map title", required = true) @RequestParam(value = "title", required = true) final String title,
        @Parameter(description = "A map abstract", required = false) @RequestParam(value = "recordAbstract", required = false) final String recordAbstract,
        @Parameter(description = "OGC OWS context as string", required = false) @RequestParam(value = "xml", required = false) final String xml,
        @Parameter(description = "OGC OWS context file name", required = false) @RequestParam(value = "filename", required = false) final String filename,
        @Parameter(description = "OGC OWS context URL", required = false) @RequestParam(value = "url", required = false) final String url,
        @Parameter(description = "A map viewer URL to visualize the map", required = false) @RequestParam(value = "viewerUrl", required = false) final String viewerUrl,
        @Parameter(description = "Map overview as PNG (base64 encoded)", required = false) @RequestParam(value = "overview", required = false) final String overview,
        @Parameter(description = "Map overview filename", required = false) @RequestParam(value = "overviewFilename", required = false) final String overviewFilename,
        @Parameter(description = "Topic category", required = false) @RequestParam(value = "topic", required = false) final String topic,
        @Parameter(description = "Publish record.", required = false) @RequestParam(required = false, defaultValue = "false") final boolean publishToAll,
        @Parameter(description = API_PARAM_RECORD_UUID_PROCESSING, required = false) @RequestParam(required = false, defaultValue = "NOTHING") final MEFLib.UuidAction uuidProcessing,
        @Parameter(description = API_PARAM_RECORD_GROUP, required = false) @RequestParam(required = false) final String group,
        @Parameter(description = "Schema", required = false)
        @RequestParam(required = false, defaultValue = "iso19139")
        final String schema,
        HttpServletRequest request) throws Exception {
        if (StringUtils.isEmpty(xml) && StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException("A context as XML or a remote URL MUST be provided.");
        }
        if (StringUtils.isEmpty(xml) && StringUtils.isEmpty(filename)) {
            throw new IllegalArgumentException(String.format("A context as XML will be saved as a record attachment. "
                + "You MUST provide a filename in this case."));
        }

        ServiceContext context = ApiUtils.createServiceContext(request);
        Path styleSheetWmc = dataDirectory.getXsltConversion(
                String.format("schema:%s:convert/fromOGCWMC-OR-OWSC",
                schema));

        FilePathChecker.verify(filename);

        String uuid = UUID.randomUUID().toString();

        // Convert the context in an ISO19139 records
        Map<String, Object> xslParams = new HashMap<>();
        xslParams.put("uuid", uuid);
        xslParams.put("viewer_url", viewerUrl);
        xslParams.put("map_url", url);
        xslParams.put("topic", topic);
        xslParams.put("title", title);
        xslParams.put("abstract", recordAbstract);
        xslParams.put("lang", context.getLanguage());

        // Assign current user to the record
        UserSession us = context.getUserSession();

        if (us != null) {
            xslParams.put("currentuser_name", us.getName() + " " + us.getSurname());
            // phone number is georchestra-specific
            // xslParams.put("currentuser_phone", us.getPrincipal().getPhone());
            xslParams.put("currentuser_mail", us.getEmailAddr());
            xslParams.put("currentuser_org", us.getOrganisation());
        }

        // 1. JDOMize the string
        Element wmcDoc = Xml.loadString(xml, false);
        // 2. Apply XSL (styleSheetWmc)
        Element transformedMd = Xml.transform(wmcDoc, styleSheetWmc, xslParams);

        // 4. Inserts the metadata (does basically the same as the metadata.insert.paste
        // service (see Insert.java)

        String date = new ISODate().toString();
        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();

        final List<String> id = new ArrayList<>();
        final List<Element> md = new ArrayList<>();

        md.add(transformedMd);

        // Import record
        Importer.importRecord(uuid, uuidProcessing, md, schema, 0, settingManager.getSiteId(),
            settingManager.getSiteName(), null, context, id, date, date, group, MetadataType.METADATA);

        final Store store = context.getBean("resourceStore", Store.class);
        final String metadataUuid = metadataUtils.getMetadataUuid(id.get(0));

        // Save the context if no context-url provided
        if (StringUtils.isEmpty(url)) {
            store.putResource(context, metadataUuid, filename, IOUtils.toInputStream(Xml.getString(wmcDoc)), null,
                MetadataResourceVisibility.PUBLIC, true);

            // Update the MD
            Map<String, Object> onlineSrcParams = new HashMap<>();
            onlineSrcParams.put("protocol", "OGC:OWS-C");
            onlineSrcParams.put("url",
                settingManager.getNodeURL() + String.format("api/records/%s/attachments/%s", uuid, filename));
            onlineSrcParams.put("name", filename);
            onlineSrcParams.put("desc", title);
            transformedMd = Xml.transform(transformedMd,
                schemaManager.getSchemaDir(schema).resolve("process").resolve("onlinesrc-add.xsl"),
                onlineSrcParams);
            dataManager.updateMetadata(context, id.get(0), transformedMd, false, true, context.getLanguage(),
                null, true, IndexingMode.none);
        }

        if (StringUtils.isNotEmpty(overview) && StringUtils.isNotEmpty(overviewFilename)) {
            store.putResource(context, metadataUuid, overviewFilename, new ByteArrayInputStream(Base64.decodeBase64(overview)), null,
                MetadataResourceVisibility.PUBLIC, true);

            // Update the MD
            Map<String, Object> onlineSrcParams = new HashMap<>();
            onlineSrcParams.put("thumbnail_url", settingManager.getNodeURL()
                + String.format("api/records/%s/attachments/%s", uuid, overviewFilename));
            transformedMd = Xml.transform(transformedMd,
                schemaManager.getSchemaDir(schema).resolve("process").resolve("thumbnail-add.xsl"),
                onlineSrcParams);
            dataManager.updateMetadata(context, id.get(0), transformedMd, false, true, context.getLanguage(),
                null, true, IndexingMode.none);
        }

        int iId = Integer.parseInt(id.get(0));
        if (publishToAll) {
            metadataOperations.setOperation(context, iId, ReservedGroup.all.getId(), ReservedOperation.view.getId());
            metadataOperations.setOperation(context, iId, ReservedGroup.all.getId(), ReservedOperation.download.getId());
            metadataOperations.setOperation(context, iId, ReservedGroup.all.getId(), ReservedOperation.dynamic.getId());
        }
        if (StringUtils.isNotEmpty(group)) {
            int gId = Integer.parseInt(group);
            metadataOperations.setOperation(context, iId, gId, ReservedOperation.view.getId());
            metadataOperations.setOperation(context, iId, gId, ReservedOperation.download.getId());
            metadataOperations.setOperation(context, iId, gId, ReservedOperation.dynamic.getId());
        }

        dataManager.indexMetadata(id);
        report.addMetadataInfos(Integer.parseInt(id.get(0)), uuid, !publishToAll, false, uuid);

        triggerCreationEvent(request, uuid);

        report.incrementProcessedRecords();
        report.close();
        return report;
    }

    /**
     * This triggers a metadata created event (after save)
     *
     * @param request
     * @param uuid    or id of metadata
     * @throws Exception
     * @throws JsonProcessingException
     */
    private void triggerCreationEvent(HttpServletRequest request, String uuid)
        throws Exception, JsonProcessingException {
        AbstractMetadata metadata = ApiUtils.getRecord(uuid);
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        UserSession userSession = ApiUtils.getUserSession(request.getSession());
        new RecordCreateEvent(metadata.getId(), userSession.getUserIdAsInt(),
            ObjectJSONUtils.convertObjectInJsonObject(userSession.getPrincipal(), RecordCreateEvent.FIELD),
            metadata.getData()).publish(applicationContext);
    }

    /**
     * This triggers a metadata created event (after save)
     *
     * @param request
     * @param uuid    or id of metadata
     * @throws Exception
     */
    private RecordDeletedEvent triggerDeletionEvent(HttpServletRequest request, String uuid)
            throws Exception {
        AbstractMetadata metadata = ApiUtils.getRecord(uuid);
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        UserSession userSession = ApiUtils.getUserSession(request.getSession());

        ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        DataManager dataMan = applicationContext.getBean(DataManager.class);
        Element beforeMetadata = dataMan.getMetadata(serviceContext, String.valueOf(metadata.getId()), false, false, false);
        XMLOutputter outp = new XMLOutputter();
        String xmlBefore = outp.outputString(beforeMetadata);
        LinkedHashMap<String, String> titles = new LinkedHashMap<>();
        try {
           titles = metadataUtils.extractTitles(Integer.toString(metadata.getId()));
        } catch (Exception e) {
            Log.warning(Geonet.DATA_MANAGER,
                String.format(
                    "Error while extracting title for the metadata %d " +
                        "while creating delete event. Error is %s. " +
                        "It may happen on subtemplates.",
                    metadata.getId(), e.getMessage()));
        }
        return new RecordDeletedEvent(metadata.getId(), metadata.getUuid(), titles, userSession.getUserIdAsInt(), xmlBefore);
    }


    /**
     * This triggers a metadata created event (after save)
     *
     * @param request
     * @param uuid    or id of metadata
     * @throws Exception
     * @throws JsonProcessingException
     */
    private void triggerImportEvent(HttpServletRequest request, String uuid) throws Exception, JsonProcessingException {
        AbstractMetadata metadata = ApiUtils.getRecord(uuid);
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        UserSession userSession = ApiUtils.getUserSession(request.getSession());
        new RecordImportedEvent(metadata.getId(), userSession.getUserIdAsInt(),
            ObjectJSONUtils.convertObjectInJsonObject(userSession.getPrincipal(), RecordImportedEvent.FIELD),
            metadata.getData()).publish(applicationContext);
    }

    private Pair<Integer, String> loadRecord(MetadataType metadataType, Element xmlElement,
                                             final MEFLib.UuidAction uuidProcessing, final String group, final String[] category,
                                             final boolean rejectIfInvalid, final boolean publishToAll,
                                             final boolean allowEditGroupMembers, final String transformWith, String schema,
                                             final String extra, HttpServletRequest request) throws Exception {

        ServiceContext context = ApiUtils.createServiceContext(request);

        ResourceBundle messages = ApiUtils.getMessagesResourceBundle(request.getLocales());

        if (!transformWith.equals("_none_")) {
            FilePathChecker.verify(transformWith);
            Path xslFile = dataDirectory.getXsltConversion(transformWith);
            if (Files.exists(xslFile)) {
                xmlElement = Xml.transform(xmlElement, xslFile);
            } else {
                throw new ResourceNotFoundException(String.format(messages.getString("api.metadata.import.errorMissingXsl"), transformWith));
            }
        }

        if (schema == null) {
            schema = dataManager.autodetectSchema(xmlElement);
            if (schema == null) {
                throw new IllegalArgumentException(messages.getString("api.metadata.import.errorDetectMetadataSchema"));
                // TODO: Report what are the supported schema
            }
        } else {
            // TODO: Check that the schema is supported
        }

        if (rejectIfInvalid) {
            try {
                Integer groupId = null;
                if (StringUtils.isNotEmpty(group)) {
                    groupId = Integer.parseInt(group);
                }
                DataManager.validateExternalMetadata(schema, xmlElement, context, groupId);
            } catch (XSDValidationErrorEx e) {
                throw new IllegalArgumentException(e);
            }
        }

        // --- if the uuid does not exist we generate it for metadata and templates
        String uuid;
        if (metadataType == MetadataType.SUB_TEMPLATE || metadataType == MetadataType.TEMPLATE_OF_SUB_TEMPLATE) {
            // subtemplates may need to be loaded with a specific uuid
            // that will be attached to the root element so check for that
            // and if not found, generate a new uuid
            uuid = xmlElement.getAttributeValue("uuid");
            if (StringUtils.isEmpty(uuid)) {
                uuid = UUID.randomUUID().toString();
            }
        } else {
            uuid = dataManager.extractUUID(schema, xmlElement);
            if (uuid.length() == 0) {
                uuid = UUID.randomUUID().toString();
                xmlElement = dataManager.setUUID(schema, uuid, xmlElement);
            }
        }

        if (uuidProcessing == MEFLib.UuidAction.NOTHING) {
            AbstractMetadata md = metadataRepository.findOneByUuid(uuid);
            if (md != null) {
                throw new IllegalArgumentException(
                    String.format(messages.getString("api.metadata.import.errorDuplicatedUUIDDetailed"), uuid));
            }
        }

        String date = new ISODate().toString();

        final List<String> id = new ArrayList<>();
        final List<Element> md = new ArrayList<>();
        md.add(xmlElement);

        // Import record
        Map<String, String> sourceTranslations = Maps.newHashMap();
        try {
            Importer.importRecord(uuid, uuidProcessing, md, schema, 0, settingManager.getSiteId(),
                settingManager.getSiteName(), sourceTranslations, context, id, date, date, group, metadataType);

        } catch (DataIntegrityViolationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ex;
        }
        int iId = Integer.parseInt(id.get(0));
        uuid = dataManager.getMetadataUuid(iId + "");

        // Set template
        dataManager.setTemplate(iId, metadataType, null);

        if (allowEditGroupMembers) {
            metadataOperations.copyDefaultPrivForGroup(context, String.valueOf(iId), group, allowEditGroupMembers);
        }

        if (publishToAll) {
            metadataOperations.setOperation(context, iId, ReservedGroup.all.getId(), ReservedOperation.view.getId());
            metadataOperations.setOperation(context, iId, ReservedGroup.all.getId(), ReservedOperation.download.getId());
            metadataOperations.setOperation(context, iId, ReservedGroup.all.getId(), ReservedOperation.dynamic.getId());
        }

        dataManager.activateWorkflowIfConfigured(context, id.get(0), group);

        if (category != null) {
            for (String c : category) {
                dataManager.setCategory(context, id.get(0), c);
            }
        }

        if (extra != null) {
            metadataRepository.update(iId, metadata -> {
                if (extra != null) {
                    metadata.getDataInfo().setExtra(extra);
                }
            });
        }

        if (rejectIfInvalid) {
            // Persist the validation status
            AbstractMetadata metadata = metadataRepository.findOneById(iId);

            metadataValidator.doValidate(metadata, context.getLanguage());
        }

        dataManager.indexMetadata(id.get(0), true);
        return Pair.read(Integer.valueOf(id.get(0)), uuid);
    }

}
