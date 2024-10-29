//=============================================================================
//===    Copyright (C) 2001-2024 Food and Agriculture Organization of the
//===    United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===    and United Nations Environment Programme (UNEP)
//===
//===    This program is free software; you can redistribute it and/or modify
//===    it under the terms of the GNU General Public License as published by
//===    the Free Software Foundation; either version 2 of the License, or (at
//===    your option) any later version.
//===
//===    This program is distributed in the hope that it will be useful, but
//===    WITHOUT ANY WARRANTY; without even the implied warranty of
//===    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===    General Public License for more details.
//===
//===    You should have received a copy of the GNU General Public License
//===    along with this program; if not, write to the Free Software
//===    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===    Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===    Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.harvest.harvester.geonet;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.MetadataResourceDatabaseMigration;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.domain.userfeedback.RatingsSetting;
import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.*;
import org.fao.geonet.kernel.mef.*;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class Aligner extends BaseAligner<GeonetParams> {

    private Logger log;

    private ServiceContext context;
    private XmlRequest request;
    private DataManager dataMan;
    private IMetadataManager metadataManager;
    private HarvestResult result;
    private CategoryMapper localCateg;
    private GroupMapper localGroups;
    private UUIDMapper localUuids;
    private String processName;
    private String preferredSchema;
    private Map<String, Object> processParams = new HashMap<>();
    private MetadataRepository metadataRepository;
    private Map<String, Map<String, String>> hmRemoteGroups = new HashMap<>();
    private SettingManager settingManager;

    public Aligner(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, XmlRequest req,
                   GeonetParams params, Element remoteInfo) {
        super(cancelMonitor);
        this.log = log;
        this.context = context;
        this.request = req;
        this.params = params;

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        dataMan = gc.getBean(DataManager.class);
        metadataManager = gc.getBean(IMetadataManager.class);
        metadataRepository = gc.getBean(MetadataRepository.class);
        settingManager = gc.getBean(SettingManager.class);

        result = new HarvestResult();

        //--- save remote categories and groups into hashmaps for a fast access

        // Before 2.11 response contains groups. Now group is used.
        Element groups = remoteInfo.getChild("groups");
        if (groups == null) {
            groups = remoteInfo.getChild("group");
        }
        if (groups != null) {
            @SuppressWarnings("unchecked")
            List<Element> list = groups.getChildren("group");
            setupLocEntity(list, hmRemoteGroups);
        }
    }

    //--------------------------------------------------------------------------

    private void setupLocEntity(List<Element> list, Map<String, Map<String, String>> hmEntity) {

        for (Element entity : list) {
            String name = entity.getChildText("name");

            Map<String, String> hm = new HashMap<>();
            hmEntity.put(name, hm);

            @SuppressWarnings("unchecked")
            List<Element> labels = entity.getChild("label").getChildren();

            for (Element el : labels) {
                hm.put(el.getName(), el.getText());
            }
        }
    }

    public HarvestResult align(SortedSet<RecordInfo> records, List<HarvestError> errors) throws Exception {
        log.info("Start of alignment for : " + params.getName());

        //-----------------------------------------------------------------------
        //--- retrieve all local categories and groups
        //--- retrieve harvested uuids for given harvesting node

        localCateg = new CategoryMapper(context);
        localGroups = new GroupMapper(context);
        localUuids = new UUIDMapper(context.getBean(IMetadataUtils.class), params.getUuid());

        Pair<String, Map<String, Object>> filter = HarvesterUtil.parseXSLFilter(params.xslfilter);
        processName = filter.one();
        processParams = filter.two();

        //-----------------------------------------------------------------------
        //--- remove old metadata

        for (String uuid : localUuids.getUUIDs()) {
            if (cancelMonitor.get()) {
                return this.result;
            }

            try {
                if (!exists(records, uuid)) {
                    String id = localUuids.getID(uuid);

                    if (log.isDebugEnabled()) log.debug("  - Removing old metadata with id:" + id);
                    metadataManager.deleteMetadata(context, id);

                    result.locallyRemoved++;
                }
            } catch (Exception t) {
                log.error("Couldn't remove metadata with uuid " + uuid);
                log.error(t);
                result.unchangedMetadata++;
            }
        }
        //--- insert/update new metadata
        // Load preferred schema and set to iso19139 by default
        preferredSchema = context.getBean(ServiceConfig.class).getMandatoryValue("preferredSchema");
        if (preferredSchema == null) {
            preferredSchema = "iso19139";
        }

        for (RecordInfo ri : records) {
            if (cancelMonitor.get()) {
                return this.result;
            }

            try {

                result.totalMetadata++;

                // Mef full format provides ISO19139 records in both the profile
                // and ISO19139 so we could be able to import them as far as
                // ISO19139 schema is installed by default.
                if (!dataMan.existsSchema(ri.schema) && !ri.schema.startsWith("iso19139.")) {
                    if (log.isDebugEnabled())
                        log.debug("  - Metadata skipped due to unknown schema. uuid:" + ri.uuid
                            + ", schema:" + ri.schema);
                    result.unknownSchema++;
                } else {
                    String id = dataMan.getMetadataId(ri.uuid);

                    // look up value of localrating/enable
                    String localRating = settingManager.getValue(Settings.SYSTEM_LOCALRATING_ENABLE);

                    if (id == null) {
                        //record doesn't exist (so it doesn't belong to this harvester)
                        log.debug("Adding record with uuid " + ri.uuid);
                        addMetadata(ri, localRating.equals(RatingsSetting.BASIC), ri.uuid);
                    } else if (localUuids.getID(ri.uuid) == null) {
                        //record doesn't belong to this harvester but exists
                        result.datasetUuidExist++;

                        switch (params.getOverrideUuid()) {
                            case OVERRIDE:
                                updateMetadata(ri,
                                    id,
                                    localRating.equals(RatingsSetting.BASIC),
                                    params.useChangeDateForUpdate(),
                                    localUuids.getChangeDate(ri.uuid), true);
                                log.info("Overriding record with uuid " + ri.uuid);

                                if (params.isIfRecordExistAppendPrivileges()) {
                                    addPrivileges(id, params.getPrivileges(), localGroups, context);
                                    result.privilegesAppendedOnExistingRecord++;
                                }
                                break;
                            case RANDOM:
                                log.info("Generating random uuid for remote record with uuid " + ri.uuid);
                                addMetadata(ri, localRating.equals(RatingsSetting.BASIC), UUID.randomUUID().toString());
                                break;
                            case SKIP:
                                log.debug("Skipping record with uuid " + ri.uuid);
                                result.uuidSkipped++;
                                break;
                            default:
                                break;
                        }
                    } else {
                        //record exists and belongs to this harvester
                        log.debug("Updating record with uuid " + ri.uuid);
                        updateMetadata(ri, id,
                            localRating.equals(RatingsSetting.BASIC),
                            params.useChangeDateForUpdate(),
                            localUuids.getChangeDate(ri.uuid), false);

                        if (params.isIfRecordExistAppendPrivileges()) {
                            addPrivileges(id, params.getPrivileges(), localGroups, context);
                            result.privilegesAppendedOnExistingRecord++;
                        }
                    }

                }
            } catch (Exception t) {
                log.error("Couldn't insert or update metadata with uuid " + ri.uuid);
                log.error(t);
                result.unchangedMetadata++;
            }
        }

        log.info("End of alignment for : " + params.getName());

        return result;
    }

    private Element extractValidMetadataForImport(DirectoryStream<Path> files, Element info) throws IOException, JDOMException {
        Element metadataValidForImport;
        final String finalPreferredSchema = preferredSchema;

        String infoSchema = "_none_";
        if (info != null && info.getContentSize() != 0) {
            Element general = info.getChild("general");
            if (general != null && general.getContentSize() != 0) {
                if (general.getChildText("schema") != null) {
                    infoSchema = general.getChildText("schema");
                }
            }
        }

        Path lastUnknownMetadataFolderName = null;

        if (Log.isDebugEnabled(Geonet.MEF))
            Log.debug(Geonet.MEF, "Multiple metadata files");

        Map<String, Pair<String, Element>> mdFiles =
            new HashMap<>();
        for (Path file : files) {
            if (Files.isRegularFile(file)) {
                Element metadata = Xml.loadFile(file);
                try {
                    Path parent = file.getParent();
                    Path parent2 = parent.getParent();
                    String metadataSchema = dataMan.autodetectSchema(metadata, null);
                    // If local node doesn't know metadata
                    // schema try to load next xml file.
                    if (metadataSchema == null) {
                        continue;
                    }

                    String currFile = "Found metadata file " + parent2.relativize(file);
                    mdFiles.put(metadataSchema, Pair.read(currFile, metadata));

                } catch (NoSchemaMatchesException e) {
                    // Important folder name to identify metadata should be ../../
                    Path parent = file.getParent();
                    if (parent != null) {
                        Path parent2 = parent.getParent();
                        if (parent2 != null) {
                            lastUnknownMetadataFolderName = parent2.relativize(parent);
                        }
                    }
                    log.debug("No schema match for " + lastUnknownMetadataFolderName + file.getFileName() + ".");
                } catch (NullPointerException e) {
                    log.error("Check the schema directory");
                    log.error(e);
                }
            }
        }

        if (mdFiles.size() == 0) {
            log.debug("No valid metadata file found" +
                ((lastUnknownMetadataFolderName == null) ?
                    "" :
                    (" in " + lastUnknownMetadataFolderName)
                ) + ".");
            return null;
        }

        // 1st: Select metadata with schema in info file
        Pair<String, Element> mdInform = mdFiles.get(infoSchema);
        if (mdInform != null) {
            log.debug(mdInform.one()
                + " with info.xml schema (" + infoSchema + ").");
            metadataValidForImport = mdInform.two();
            return metadataValidForImport;
        }
        // 2nd: Select metadata with preferredSchema
        mdInform = mdFiles.get(finalPreferredSchema);
        if (mdInform != null) {
            log.debug(mdInform.one()
                + " with preferred schema (" + finalPreferredSchema + ").");
            metadataValidForImport = mdInform.two();
            return metadataValidForImport;
        }

        // Lastly: Select the first metadata in the map
        String metadataSchema = (String) mdFiles.keySet().toArray()[0];
        mdInform = mdFiles.get(metadataSchema);
        log.debug(mdInform.one()
            + " with known schema (" + metadataSchema + ").");
        metadataValidForImport = mdInform.two();

        return metadataValidForImport;
    }

    private void addMetadata(final RecordInfo ri, final boolean localRating, String uuid) throws Exception {
        final String[] id = {null};
        final Element[] md = {null};

        //--- import metadata from MEF file

        Path mefFile = null;

        try {
            mefFile = retrieveMEF(ri.uuid);
            String fileType = "mef";
            MEFLib.Version version = MEFLib.getMEFVersion(mefFile);
            if (version != null && version.equals(MEFLib.Version.V2)) {
                fileType = "mef2";
            }

            IVisitor visitor = fileType.equals("mef2") ? new MEF2Visitor() : new MEFVisitor();

            MEFLib.visit(mefFile, visitor, new IMEFVisitor() {
                public void handleMetadata(Element mdata, int index) throws Exception {
                    md[index] = mdata;
                }

                //--------------------------------------------------------------------

                public void handleMetadataFiles(DirectoryStream<Path> files, Element info, int index) throws Exception {
                    // Import valid metadata
                    Element metadataValidForImport = extractValidMetadataForImport(files, info);

                    if (metadataValidForImport != null) {
                        handleMetadata(metadataValidForImport, index);
                    }
                }

                //--------------------------------------------------------------------

                public void handleInfo(Element info, int index) throws Exception {

                    final Element metadata = md[index];
                    String schema = dataMan.autodetectSchema(metadata, null);
                    if (info != null && info.getContentSize() != 0) {
                        Element general = info.getChild("general");
                        if (general != null && general.getContentSize() != 0) {
                            Element schemaInfo = general.getChild("schema");
                            if (schemaInfo != null) {
                                schemaInfo.setText(schema);
                            }
                        }
                    }
                    if (info != null) {
                        id[index] = addMetadata(ri, md[index], info, localRating, uuid);
                    }
                }

                //--------------------------------------------------------------------

                public void handlePublicFile(String file, String changeDate, InputStream is, int index) throws Exception {
                    handleFile(file, changeDate, is, index, MetadataResourceVisibility.PUBLIC);
                }

                private void handleFile(String file, String changeDate, InputStream is, int index, MetadataResourceVisibility visibility) throws Exception {
                    if (id[index] == null) return;
                    if (log.isDebugEnabled())
                        log.debug("    - Adding remote " + visibility + " file with name: " + file);
                    final Store store = context.getBean("resourceStore", Store.class);
                    final IMetadataUtils metadataUtils = context.getBean(IMetadataUtils.class);
                    final String metadataUuid = metadataUtils.getMetadataUuid(id[index]);
                    store.putResource(context, metadataUuid, file, is, new ISODate(changeDate).toDate(), visibility, true);
                }

                public void handleFeatureCat(Element md, int index)
                    throws Exception {
                    // Feature Catalog not managed for harvesting
                }

                public void handlePrivateFile(String file, String changeDate,
                                              InputStream is, int index) throws Exception {
                    if (params.mefFormatFull) {
                        handleFile(file, changeDate, is, index, MetadataResourceVisibility.PRIVATE);
                    }
                }
            });
        } catch (Exception e) {
            //--- we ignore the exception here. Maybe the metadata has been removed just now
            if (log.isDebugEnabled())
                log.debug("  - Skipped unretrievable metadata (maybe has been removed) with uuid:" + ri.uuid);
            result.unretrievable++;
            log.error(e);
        } finally {
            if (mefFile != null) {
                FileUtils.deleteQuietly(mefFile.toFile());
            }
        }
    }

    private String addMetadata(RecordInfo ri, Element md, Element info, boolean localRating, String uuid) throws Exception {
        Element general = info.getChild("general");

        String createDate = general.getChildText("createDate");
        String changeDate = general.getChildText("changeDate");
        String isTemplate = general.getChildText("isTemplate");
        String siteId = general.getChildText("siteId");
        String popularity = general.getChildText("popularity");
        String schema = general.getChildText("schema");

        if ("true".equals(isTemplate)) isTemplate = "y";
        else isTemplate = "n";

        if (log.isDebugEnabled()) log.debug("  - Adding metadata with remote uuid:" + ri.uuid);

        // Translate metadata
        if (params.isTranslateContent()) {
            md = translateMetadataContent(context, md, schema);
        }

        try {
            Integer groupIdVal = null;
            if (StringUtils.isNotEmpty(params.getOwnerIdGroup())) {
                groupIdVal = getGroupOwner();
            }

            params.getValidate().validate(dataMan, context, md, groupIdVal);
        } catch (Exception e) {
            log.info("Ignoring invalid metadata uuid: " + uuid);
            result.doesNotValidate++;
            return null;
        }

        if (params.mefFormatFull && ri.schema.startsWith(ISO19139SchemaPlugin.IDENTIFIER)) {
            // In GeoNetwork 3.x, links to resources changed:
            // * thumbnails contains full URL instead of file name only
            // * API mode change old URL structure.
            MetadataResourceDatabaseMigration.updateMetadataResourcesLink(md, null, settingManager);
        }

        if (!params.xslfilter.equals("")) {
            md = HarvesterUtil.processMetadata(dataMan.getSchema(schema),
                md, processName, processParams);
        }

        // insert metadata
        // If MEF format is full, private file links needs to be updated
        boolean ufo = params.mefFormatFull;
        AbstractMetadata metadata = new Metadata();
        metadata.setUuid(uuid);
        metadata.getDataInfo().
            setSchemaId(schema).
            setRoot(md.getQualifiedName()).
            setType(MetadataType.lookup(isTemplate)).
            setCreateDate(new ISODate(createDate)).
            setChangeDate(new ISODate(changeDate));
        metadata.getSourceInfo().
            setSourceId(siteId).
            setOwner(getOwner());
        metadata.getHarvestInfo().
            setHarvested(true).
            setUuid(params.getUuid());

        try {
            metadata.getSourceInfo().setGroupOwner(Integer.valueOf(params.getOwnerIdGroup()));
        } catch (NumberFormatException e) {
        }

        addCategories(metadata, params.getCategories(), localCateg, context, null, false);

        metadata = metadataManager.insertMetadata(context, metadata, md, IndexingMode.none, ufo, UpdateDatestamp.NO, false, this.batchingIndexSubmittor);

        String id = String.valueOf(metadata.getId());

        if (!localRating) {
            String rating = general.getChildText("rating");
            if (rating != null) {
                metadata.getDataInfo().setRating(Integer.valueOf(rating));
            }
        }

        if (popularity != null) {
            metadata.getDataInfo().setPopularity(Integer.valueOf(popularity));
        }


        if (params.createRemoteCategory) {
            Element categs = info.getChild("categories");
            if (categs != null) {
                Importer.addCategoriesToMetadata(metadata, categs, context);
            }
        }
        if (((ArrayList<Group>) params.getGroupCopyPolicy()).size() == 0) {
            addPrivileges(id, params.getPrivileges(), localGroups, context);
        } else {
            addPrivilegesFromGroupPolicy(id, info.getChild("privileges"));
        }
        context.getBean(IMetadataManager.class).save(metadata);

        dataMan.indexMetadata(id, this.batchingIndexSubmittor);
        result.addedMetadata++;

        return id;
    }

    private void addPrivilegesFromGroupPolicy(String id, Element privil) throws Exception {
        Map<String, Set<String>> groupOper = buildPrivileges(privil);

        for (Group remoteGroup : params.getGroupCopyPolicy()) {
            //--- get operations allowed to remote group
            Set<String> oper = groupOper.get(remoteGroup.name);

            //--- if we don't find any match, maybe the remote group has been removed

            if (oper == null)
                log.info("    - Remote group has been removed or no privileges exist : " + remoteGroup.name);
            else {
                String localGrpId = localGroups.getID(remoteGroup.name);

                if (localGrpId == null) {
                    //--- group does not exist locally

                    if (remoteGroup.policy == Group.CopyPolicy.CREATE_AND_COPY) {
                        if (log.isDebugEnabled())
                            log.debug("    - Creating local group : " + remoteGroup.name);
                        localGrpId = createGroup(remoteGroup.name);

                        if (localGrpId == null)
                            log.info("    - Specified group was not found remotely : " + remoteGroup.name);
                        else {
                            if (log.isDebugEnabled())
                                log.debug("    - Setting privileges for group : " + remoteGroup.name);
                            addOperations(id, localGrpId, oper);
                        }
                    }
                } else {
                    //--- group exists locally

                    if (remoteGroup.policy == Group.CopyPolicy.COPY_TO_INTRANET) {
                        if (log.isDebugEnabled())
                            log.debug("    - Setting privileges for 'intranet' group");
                        addOperations(id, "0", oper);
                    } else {
                        if (log.isDebugEnabled())
                            log.debug("    - Setting privileges for group : " + remoteGroup.name);
                        addOperations(id, localGrpId, oper);
                    }
                }
            }
        }
    }

    private Map<String, Set<String>> buildPrivileges(Element privil) {
        Map<String, Set<String>> map = new HashMap<>();

        for (Object o : privil.getChildren("group")) {
            Element group = (Element) o;
            String name = group.getAttributeValue("name");

            Set<String> set = new HashSet<>();
            map.put(name, set);

            for (Object op : group.getChildren("operation")) {
                Element oper = (Element) op;
                name = oper.getAttributeValue("name");
                set.add(name);
            }
        }

        return map;
    }

    private void addOperations(String id, String groupId, Set<String> oper) throws Exception {
        for (String opName : oper) {
            int opId = dataMan.getAccessManager().getPrivilegeId(opName);

            //--- allow only: view, download, dynamic, featured
            if (opId == 0 || opId == 1 || opId == 5 || opId == 6) {
                if (log.isDebugEnabled()) log.debug("       --> " + opName);
                dataMan.setOperation(context, id, groupId, opId + "");
            } else {
                if (log.isDebugEnabled()) log.debug("       --> " + opName + " (skipped)");
            }
        }
    }

    private String createGroup(String name) throws Exception {
        Map<String, String> hm = hmRemoteGroups.get(name);

        if (hm == null)
            return null;

        org.fao.geonet.domain.Group group = new org.fao.geonet.domain.Group()
            .setName(name);
        group.getLabelTranslations().putAll(hm);

        group = context.getBean(GroupRepository.class).save(group);

        int id = group.getId();
        localGroups.add(name, id + "");

        return id + "";
    }

    /**
     * Updates the record on the database. The force parameter allows you to force an update even
     * if the date is not more updated, to make sure transformation and attributes assigned by the
     * harvester are applied. Also, it changes the ownership of the record so it is assigned to the
     * new harvester that last updated it.
     *
     * @param ri
     * @param id
     * @param localRating
     * @param useChangeDate
     * @param localChangeDate
     * @param force
     * @throws Exception
     */
    private void updateMetadata(final RecordInfo ri, final String id, final boolean localRating,
                                final boolean useChangeDate, String localChangeDate, Boolean force) throws Exception {
        final Element[] md = {null};
        final Element[] publicFiles = {null};
        final Element[] privateFiles = {null};

        if (localUuids.getID(ri.uuid) == null && !force) {
            if (log.isDebugEnabled())
                log.debug("  - Skipped metadata managed by another harvesting node. uuid:" + ri.uuid + ", name:" + params.getName());
        } else {
            if (force || !useChangeDate || ri.isMoreRecentThan(localChangeDate)) {
                Path mefFile = null;

                try {
                    mefFile = retrieveMEF(ri.uuid);

                    String fileType = "mef";
                    MEFLib.Version version = MEFLib.getMEFVersion(mefFile);
                    if (version != null && version.equals(MEFLib.Version.V2)) {
                        fileType = "mef2";
                    }

                    IVisitor visitor = fileType.equals("mef2") ? new MEF2Visitor() : new MEFVisitor();

                    MEFLib.visit(mefFile, visitor, new IMEFVisitor() {
                        public void handleMetadata(Element mdata, int index) throws Exception {
                            md[index] = mdata;
                        }

                        //-----------------------------------------------------------------

                        public void handleMetadataFiles(DirectoryStream<Path> files, Element info, int index) throws Exception {
                            // Import valid metadata
                            Element metadataValidForImport = extractValidMetadataForImport(files, info);

                            if (metadataValidForImport != null) {
                                handleMetadata(metadataValidForImport, index);
                            }
                        }

                        public void handleInfo(Element info, int index) throws Exception {
                            updateMetadata(ri, id, md[index], info, localRating, force);
                            publicFiles[index] = info.getChild("public");
                            privateFiles[index] = info.getChild("private");
                        }

                        //-----------------------------------------------------------------

                        public void handlePublicFile(String file, String changeDate, InputStream is, int index) throws Exception {
                            handleFile(id, file, MetadataResourceVisibility.PUBLIC, changeDate, is, publicFiles[index]);
                        }

                        public void handleFeatureCat(Element md, int index)
                            throws Exception {
                            // Feature Catalog not managed for harvesting
                        }

                        public void handlePrivateFile(String file,
                                                      String changeDate, InputStream is, int index)
                            throws Exception {
                            handleFile(id, file, MetadataResourceVisibility.PRIVATE, changeDate, is, privateFiles[index]);
                        }

                    });
                } catch (Exception e) {
                    //--- we ignore the exception here. Maybe the metadata has been removed just now
                    result.unretrievable++;
                } finally {
                    if (mefFile != null) {
                        FileUtils.deleteQuietly(mefFile.toFile());
                    }
                }
            } else {
                result.unchangedMetadata++;
            }
        }
    }

    private void updateMetadata(RecordInfo ri, String id, Element md,
                                Element info, boolean localRating, boolean force) throws Exception {
        String date = localUuids.getChangeDate(ri.uuid);


        // Translate metadata
        if (params.isTranslateContent()) {
            md = translateMetadataContent(context, md, ri.schema);
        }

        try {
            Integer groupIdVal = null;
            if (StringUtils.isNotEmpty(params.getOwnerIdGroup())) {
                groupIdVal = getGroupOwner();
            }

            params.getValidate().validate(dataMan, context, md, groupIdVal);
        } catch (Exception e) {
            log.info("Ignoring invalid metadata uuid: " + ri.uuid);
            result.doesNotValidate++;
            return;
        }

        Metadata metadata;
        if (!force && !ri.isMoreRecentThan(date)) {
            if (log.isDebugEnabled())
                log.debug("  - XML not changed for local metadata with uuid:" + ri.uuid);
            result.unchangedMetadata++;
            metadata = metadataRepository.findOneById(Integer.valueOf(id));
            if (metadata == null) {
                throw new NoSuchElementException("Unable to find a metadata with ID: " + id);
            }
        } else {
            if (params.mefFormatFull && ri.schema.startsWith(ISO19139SchemaPlugin.IDENTIFIER)) {
                // In GeoNetwork 3.x, links to resources changed:
                // * thumbnails contains full URL instead of file name only
                // * API mode change old URL structure.
                MetadataResourceDatabaseMigration.updateMetadataResourcesLink(md, null, settingManager);
            }

            if (!params.xslfilter.equals("")) {
                md = HarvesterUtil.processMetadata(dataMan.getSchema(ri.schema),
                    md, processName, processParams);
            }
            // update metadata
            if (log.isDebugEnabled())
                log.debug("  - Updating local metadata with id=" + id);

            boolean validate = false;
            boolean ufo = params.mefFormatFull;
            boolean updateDateStamp = true;
            String language = context.getLanguage();
            metadataManager.updateMetadata(context, id, md, validate, ufo, language, ri.changeDate,
                updateDateStamp, IndexingMode.none);
            metadata = metadataRepository.findOneById(Integer.valueOf(id));
            result.updatedMetadata++;
            if (force) {
                //change ownership of metadata to new harvester
                metadata.getHarvestInfo().setUuid(params.getUuid());
                metadata.getSourceInfo().setSourceId(params.getUuid());

                metadataManager.save(metadata);
            }
        }

        metadata.getCategories().clear();
        addCategories(metadata, params.getCategories(), localCateg, context, null, true);
        metadata = metadataRepository.findOneById(Integer.valueOf(id));

        Element general = info.getChild("general");

        String popularity = general.getChildText("popularity");

        if (!localRating) {
            String rating = general.getChildText("rating");
            if (rating != null) {
                metadata.getDataInfo().setRating(Integer.valueOf(rating));
            }
        }

        if (popularity != null) {
            metadata.getDataInfo().setPopularity(Integer.valueOf(popularity));
        }

        if (params.createRemoteCategory) {
            Element categs = info.getChild("categories");
            if (categs != null) {
                Importer.addCategoriesToMetadata(metadata, categs, context);
            }
        }

        if (((ArrayList<Group>) params.getGroupCopyPolicy()).size() == 0) {
            addPrivileges(id, params.getPrivileges(), localGroups, context);
        } else if (info != null){
            addPrivilegesFromGroupPolicy(id, info.getChild("privileges"));
        }

        metadataManager.save(metadata);
//        dataMan.flush();

        dataMan.indexMetadata(id, this.batchingIndexSubmittor);
    }

    private void handleFile(String id, String file, MetadataResourceVisibility visibility, String changeDate,
                            InputStream is, Element files) throws Exception {
        if (files == null) {
            if (log.isDebugEnabled())
                log.debug("  - No file found in info.xml. Cannot update file:" + file);
        } else {
            final Store store = context.getBean("resourceStore", Store.class);
            final IMetadataUtils metadataUtils = context.getBean(IMetadataUtils.class);
            final String metadataUuid = metadataUtils.getMetadataUuid(id);
            removeOldFile(store, metadataUuid, files, visibility);
            saveFile(store, metadataUuid, file, visibility, changeDate, is);
        }
    }

    private void removeOldFile(Store store, String metadataUuid, Element infoFiles, MetadataResourceVisibility visibility) throws Exception {
        final List<MetadataResource> resources = store.getResources(context, metadataUuid, visibility, null, true);
        for (MetadataResource resource: resources) {
            if (infoFiles != null && !existsFile(resource.getId(), infoFiles)) {
                if (log.isDebugEnabled()) {
                    log.debug("  - Removing old " + metadataUuid + " file with name=" + resource.getFilename());
                }
                store.delResource(context, metadataUuid, visibility, resource.getFilename(), true);
            }
        }
    }

    private boolean existsFile(String fileName, Element files) {
        @SuppressWarnings("unchecked")
        List<Element> list = files.getChildren("file");

        for (Element elem : list) {
            String name = elem.getAttributeValue("name");

            if (fileName.equals(name)) {
                return true;
            }
        }

        return false;
    }

    private void saveFile(final Store store, String metadataUuid, String file,
                          MetadataResourceVisibility visibility, String changeDate, InputStream is) throws Exception {
        ISODate remIsoDate = new ISODate(changeDate);
        boolean saveFile;

        Store.ResourceHolder resourceHolder;
        try {
            resourceHolder = store.getResource(context, metadataUuid, visibility, file, true);
        } catch (ResourceNotFoundException ex) {
            resourceHolder = null;
        }

        if ((resourceHolder != null) && (resourceHolder.getMetadata() != null)) {
            ISODate locIsoDate = new ISODate(resourceHolder.getMetadata().getLastModification().getTime(), false);
            saveFile = (remIsoDate.timeDifferenceInSeconds(locIsoDate) > 0);
        } else {
            saveFile = true;
        }

        if (saveFile) {
            if (log.isDebugEnabled()) {
                log.debug("  - Adding remote " + metadataUuid + "  file with name:" + file);
            }

            store.putResource(context, metadataUuid, file, is, remIsoDate.toDate(), visibility, true);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("  - Nothing to do in dir " + metadataUuid + " for file with name:" + file);
            }
        }
    }

    /**
     * Return true if the uuid is present in the remote node
     */

    private boolean exists(SortedSet<RecordInfo> records, String uuid) {
        // Records is a TreeSet sorted by uuid attribute.
        // Method equals of RecordInfo only checks equality using `uuid` attribute.
        // TreeSet.contains can be used more efficiently instead of doing a loop over all the recordInfo elements.
        RecordInfo recordToTest = new RecordInfo(uuid, null);
        return records.contains(recordToTest);

    }

    private Path retrieveMEF(String uuid) throws IOException {
        request.clearParams();
        request.addParam("uuid", uuid);
        request.addParam("format", (params.mefFormatFull ? "full" : "partial"));

        // Request MEF2 format - if remote node is old
        // it will ignore this parameter and return a MEF1 format
        // which will be handle in addMetadata/updateMetadata.
        request.addParam("version", "2");
        request.addParam("relation", "false");
        request.setAddress(params.getServletPath() + "/" + params.getNode()
            + "/eng/" + Geonet.Service.MEF_EXPORT);

        Path tempFile = Files.createTempFile("temp-", ".dat");
        request.executeLarge(tempFile);

        return tempFile;
    }
}
