//=============================================================================
//===    Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
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
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.userfeedback.RatingsSetting;
import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataOperations;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.HarvesterUtil;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.kernel.mef.IMEFVisitor;
import org.fao.geonet.kernel.mef.IVisitor;
import org.fao.geonet.kernel.mef.Importer;
import org.fao.geonet.kernel.mef.MEF2Visitor;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.mef.MEFVisitor;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * The BaseGeoNetworkAligner class is an abstract base class that provides functionality and
 * utility methods for aligning remote metadata with the local GeoNetwork metadata catalog
 * during harvesting processes.
 * <p>
 * This class supports various metadata alignment tasks, including handling metadata updates,
 * managing privileges, synchronizing resources, and ensuring metadata integrity based
 * on various configurations and conditions.
 * <p>
 * It leverages several GeoNetwork services and utilities for metadata handling, such as
 * metadata managers, schema utilities, access managers, and resource management.
 *
 */
public abstract class BaseGeoNetworkAligner<P extends BaseGeonetParams> extends BaseAligner<P> {
    public static final String GENERAL = "general";
    protected final Logger log;
    protected final ServiceContext context;
    protected final DataManager dataMan;
    protected final IMetadataManager metadataManager;
    protected final IMetadataIndexer metadataIndexer;
    protected final IMetadataOperations metadataOperations;
    protected final IMetadataUtils metadataUtils;
    protected final IMetadataSchemaUtils metadataSchemaUtils;
    protected final MetadataRepository metadataRepository;
    protected final SettingManager settingManager;
    protected final AccessManager accessManager;
    protected CategoryMapper localCategory;
    protected GroupMapper localGroups;
    protected UUIDMapper localUuids;
    protected String processName;
    protected String preferredSchema;
    protected Map<String, Object> processParams = new HashMap<>();
    protected HarvestResult result;
    protected Map<String, Map<String, String>> hmRemoteGroups = new HashMap<>();

    /**
     * Constructs an instance of BaseGeoNetworkAligner, initializing required dependencies and settings
     * for metadata alignment operations within the geonetwork harvesting process.
     *
     * @param cancelMonitor an AtomicBoolean used to monitor cancellation requests during alignment
     * @param log           the Logger instance used to log debug, info, warning, and error messages
     * @param context       the ServiceContext providing access to GeoNetwork application context and resources
     * @param params        parameters of type P that represent the configuration used for the alignment process
     */
    public BaseGeoNetworkAligner(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, P params) {
        super(cancelMonitor);
        this.log = log;
        this.context = context;
        this.params = params;

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        metadataIndexer = gc.getBean(IMetadataIndexer.class);
        metadataManager = gc.getBean(IMetadataManager.class);
        metadataOperations = gc.getBean(IMetadataOperations.class);
        metadataUtils = gc.getBean(IMetadataUtils.class);
        metadataSchemaUtils = gc.getBean(IMetadataSchemaUtils.class);
        metadataRepository = gc.getBean(MetadataRepository.class);
        settingManager = gc.getBean(SettingManager.class);
        accessManager = gc.getBean(AccessManager.class);
        dataMan = gc.getBean(DataManager.class);

        result = new HarvestResult();
    }

    /**
     * Aligns the provided records from a remote source with the local metadata catalog, performing
     * necessary add, update, or removal operations based on the current state of the catalog.
     *
     * @param records a sorted set of {@link RecordInfo} objects representing metadata records from a remote source
     * @param errors  a list of {@link HarvestError} to be populated with errors encountered during the alignment process
     * @return a {@link HarvestResult} object capturing the result of the alignment, including counts of added, updated,
     * removed, and unchanged records, among other metrics
     * @throws Exception if an error occurs during the alignment process
     */
    public HarvestResult align(SortedSet<RecordInfo> records, List<HarvestError> errors) throws Exception {
        log.info("Start of alignment for : " + params.getName());

        //-----------------------------------------------------------------------
        //--- retrieve all local categories and groups
        //--- retrieve harvested uuids for given harvesting node

        localCategory = new CategoryMapper(context);
        localGroups = new GroupMapper(context);
        localUuids = new UUIDMapper(context.getBean(IMetadataUtils.class), params.getUuid());

        Pair<String, Map<String, Object>> filter = HarvesterUtil.parseXSLFilter(params.xslfilter);
        processName = filter.one();
        processParams = filter.two();

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
            } catch (Throwable t) {
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

                // MEF full format provides ISO19139 records in both the profile
                // and ISO19139, so we could be able to import them as far as
                // ISO19139 schema is installed by default.
                if (!metadataSchemaUtils.existsSchema(ri.schema) && !ri.schema.startsWith("iso19139.")) {
                    log.info("  - Metadata skipped due to unknown schema. uuid:" + ri.uuid
                        + ", schema:" + ri.schema);
                    result.unknownSchema++;
                } else {
                    String id = metadataUtils.getMetadataId(ri.uuid);

                    // look up the value of localrating/enable
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
                                result.updatedMetadata++;

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
            } catch (Throwable t) {
                log.error("Couldn't insert or update metadata with uuid " + ri.uuid);
                log.error(t);
                result.unchangedMetadata++;
            }
        }

        metadataIndexer.forceIndexChanges();

        log.info("End of alignment for : " + params.getName());

        return result;
    }

    /**
     * Updates the record in the database. The `force` parameter allows you to force an update even if the date
     * is not more recent, ensuring that transformations and attributes assigned by the harvester are applied.
     * Additionally, it changes the ownership of the record so it is assigned to the new harvester that last
     * updated it.
     * <br>
     * If certain conditions such as change date comparison and forced update flags are met, the method
     * retrieves, processes, and aligns the metadata from a remote source.
     *
     * @param ri              the {@link RecordInfo} object containing metadata record details such as UUID and change date
     * @param id              the identifier of the metadata record being updated
     * @param localRating     a boolean indicating whether local ratings are enabled for the metadata record
     * @param useChangeDate   a boolean specifying whether to compare change dates for synchronized updates
     * @param localChangeDate the change date of the existing local metadata for comparison
     * @param force           a Boolean flag to enforce the metadata update regardless of conditions (e.g., ownership check)
     * @throws Exception if an error occurs while retrieving or processing the metadata
     */
    private void updateMetadata(final RecordInfo ri, final String id, final boolean localRating,
                                final boolean useChangeDate, String localChangeDate, Boolean force) throws Exception {
        final Element[] md = {null};
        final Element[] publicFiles = {null};
        final Element[] privateFiles = {null};

        if (localUuids.getID(ri.uuid) == null && !force) {
            log.info("  - Skipped metadata managed by another harvesting node. uuid:" + ri.uuid + ", name:" + params.getName());
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

                    //
                    MEFLib.visit(mefFile, visitor, new IMEFVisitor() {
                        public void handleMetadata(Element mdata, int index) throws Exception {
                            md[index] = mdata;
                        }


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

                        public void indexMetadata(int index) throws Exception {
                            metadataIndexer.indexMetadata(id, true, IndexingMode.full);
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

    /**
     * Updates the metadata record in the local catalog with the provided XML document and related information.
     * This method performs validation, processing, category assignment, privilege updates, and indexing for
     * the metadata record. It handles both forced updates and regular synchronization based on change dates.
     *
     * @param ri          the {@link RecordInfo} object containing metadata record details, such as UUID and schema
     * @param id          the unique identifier of the metadata record being updated in the local database
     * @param md          the {@link Element} representing the metadata XML document to be used for updating
     * @param info        the {@link Element} containing additional metadata information, such as rating and popularity
     * @param localRating a boolean indicating whether local rating values should be preserved or replaced
     * @param force       a boolean flag indicating whether the update should occur regardless of change date or other conditions
     * @throws Exception if there are issues with metadata validation, processing, database operations, or indexing
     */
    private void updateMetadata(RecordInfo ri, String id, Element md,
                                Element info, boolean localRating, boolean force) throws Exception {
        String date = localUuids.getChangeDate(ri.uuid);


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
            log.info("  - XML not changed for local metadata with uuid:" + ri.uuid);
            result.unchangedMetadata++;
            metadata = metadataRepository.findOneById(Integer.parseInt(id));
            if (metadata == null) {
                throw new NoSuchElementException("Unable to find a metadata with ID: " + id);
            }
        } else {
            if (params.mefFormatFull && ri.schema.startsWith(ISO19139SchemaPlugin.IDENTIFIER)) {
                // In GeoNetwork 3.x, links to resources changed:
                // * thumbnails contain full URL instead of file name only
                // * API mode change old URL structure.
                MetadataResourceDatabaseMigration.updateMetadataResourcesLink(md, null, settingManager);
            }

            if (!params.xslfilter.isEmpty()) {
                md = HarvesterUtil.processMetadata(metadataSchemaUtils.getSchema(ri.schema),
                    md, processName, processParams);
            }
            // update metadata
            if (log.isDebugEnabled()) {
                log.debug("  - Updating local metadata with id=" + id);
            }

            boolean validate = false;
            boolean ufo = params.mefFormatFull;
            boolean updateDateStamp = true;
            String language = context.getLanguage();
            metadataManager.updateMetadata(context, id, md, validate, ufo, language, ri.changeDate,
                updateDateStamp, IndexingMode.none);
            metadata = metadataRepository.findOneById(Integer.parseInt(id));
            result.updatedMetadata++;
            if (force) {
                //change ownership of metadata to new harvester
                metadata.getHarvestInfo().setUuid(params.getUuid());
                metadata.getSourceInfo().setSourceId(params.getUuid());

                metadataManager.save(metadata);
            }
        }

        metadata.getCategories().clear();
        addCategories(metadata, params.getCategories(), localCategory, context, null, true);
        metadata = metadataRepository.findOneById(Integer.parseInt(id));

        Element general = info.getChild(GENERAL);

        String popularity = general.getChildText("popularity");

        if (!localRating) {
            String rating = general.getChildText("rating");
            if (rating != null) {
                metadata.getDataInfo().setRating(Integer.parseInt(rating));
            }
        }

        if (popularity != null) {
            metadata.getDataInfo().setPopularity(Integer.parseInt(popularity));
        }

        if (params.createRemoteCategory) {
            Element categs = info.getChild("categories");
            if (categs != null) {
                Importer.addCategoriesToMetadata(metadata, categs, context);
            }
        }

        if (((ArrayList<Group>) params.getGroupCopyPolicy()).isEmpty()) {
            addPrivileges(id, params.getPrivileges(), localGroups, context);
        } else {
            addPrivilegesFromGroupPolicy(id, info.getChild("privileges"));
        }

        metadataManager.save(metadata);

        metadataIndexer.indexMetadata(id, true, IndexingMode.full);
    }

    /**
     * Removes old files associated with the specified metadata UUID from the store if they are not
     * present in the provided list of information files. This operation ensures that redundant or
     * outdated resources are cleaned up, maintaining consistency with the metadata state.
     *
     * @param context      the service context providing access to application-level resources and configurations
     * @param log          the logger used to track and debug the removal process details
     * @param store        the storage mechanism handling metadata resource operations (e.g., retrieval, deletion)
     * @param metadataUuid the unique identifier of the metadata record whose files are being processed
     * @param infoFiles    an XML element containing a list of currently valid file references for comparison
     * @param visibility   the visibility level of the resources to manage (e.g., public, private)
     * @throws Exception if an error occurs during the process of retrieving or deleting metadata resources
     */
    protected void removeOldFile(ServiceContext context, Logger log, Store store, String metadataUuid, Element infoFiles,
                                 MetadataResourceVisibility visibility) throws Exception {
        final List<MetadataResource> resources = store.getResources(context, metadataUuid, visibility, null, true);
        for (MetadataResource resource : resources) {
            if (infoFiles != null && !existsFile(resource.getId(), infoFiles)) {
                if (log.isDebugEnabled()) {
                    log.debug("  - Removing old " + metadataUuid + " file with name=" + resource.getFilename());
                }
                store.delResource(context, metadataUuid, visibility, resource.getFilename(), true);
            }
        }
    }

    /**
     * Return true if the uuid is present in the remote node.
     *
     * @param records a sorted set of {@link RecordInfo} objects, which are ordered by their UUID attribute
     * @param uuid    the unique identifier of the record to be checked for existence
     * @return true if a record with the specified UUID exists in the provided set, false otherwise
     */
    protected boolean exists(SortedSet<RecordInfo> records, String uuid) {
        // Records is a TreeSet sorted by uuid attribute.
        // Method equals of RecordInfo only checks equality using the ` uuid ` attribute.
        // TreeSet.contains can be used more efficiently instead of doing a loop over all the recordInfo elements.
        RecordInfo recordToTest = new RecordInfo(uuid, null);
        return records.contains(recordToTest);

    }


    /**
     * Adds privileges to a metadata record based on the group policies defined in the provided element.
     * This method processes group policies, creates local groups if necessary, and assigns privileges accordingly,
     * ensuring alignment between remote and local groups.
     *
     * @param id     the unique identifier of the metadata record to which privileges are to be assigned
     * @param privil the {@link Element} containing the group policies and associated privileges
     * @throws Exception if an error occurs while processing group policies, creating groups, or assigning privileges
     */
    protected void addPrivilegesFromGroupPolicy(String id, Element privil) throws Exception {
        Map<String, Set<String>> groupOper = buildPrivileges(privil);

        Iterable<Group> iterable = params.getGroupCopyPolicy();
        for (Group remoteGroup : iterable) {
            //--- get operations allowed to the remote group
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

    /**
     * Adds specific operations or privileges to metadata records for a specific group.
     * Only certain operations (view, download, dynamic, featured) are allowed, and others are skipped.
     * This method interacts with the {@link AccessManager} to retrieve operation IDs
     * and uses {@link IMetadataOperations} to assign them to the metadata.
     *
     * @param id      the unique identifier of the metadata record to which the operations are being added
     * @param groupId the identifier of the group to which the operations are assigned
     * @param oper    a set of operation names (as strings) to assign to the metadata for the specified group
     * @throws Exception if an error occurs during operation ID retrieval or assignment
     */
    protected void addOperations(String id, String groupId, Set<String> oper) throws Exception {
        for (String opName : oper) {
            int opId = accessManager.getPrivilegeId(opName);

            //--- allow only: view, download, dynamic, featured
            if (opId == 0 || opId == 1 || opId == 5 || opId == 6) {
                if (log.isDebugEnabled()) {
                    log.debug("       --> " + opName);
                }
                metadataOperations.setOperation(context, id, groupId, opId + "");
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("       --> " + opName + " (skipped)");
                }
            }
        }
    }

    /**
     * Creates a new group with the specified name, assigns translations to it,
     * saves it to the repository, and updates the localGroups mapping with the generated group ID.
     *
     * @param name the name of the group to be created
     * @return the unique identifier (as a String) of the newly created group, or null if the group name
     * does not exist in the remote groups mapping
     */
    protected String createGroup(String name) {
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
     * Builds a map of privileges by processing the given XML element that contains group and operation definitions.
     * Each group is mapped to a set of operations as specified in the provided element.
     *
     * @param privil the {@link Element} representing the root XML element containing group and operation details.
     *               Each group is expected to have a "name" attribute, and operations within the group are expected
     *               to have a "name" attribute.
     * @return a {@link Map} where each key is a group name (as a {@link String}), and the value is a {@link Set}
     * of operation names (as {@link String}) associated with that group.
     */
    protected Map<String, Set<String>> buildPrivileges(Element privil) {
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

    /**
     * Checks if a file with the specified name exists within the provided XML element.
     *
     * @param fileName the name of the file being searched for
     * @param files    the XML element containing a list of file elements
     * @return true if a file with the given name exists, otherwise false
     */
    protected boolean existsFile(String fileName, Element files) {
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

    /**
     * Saves a file to the specified storage location if the change date indicates
     * that the file requires updating or does not already exist. The method checks
     * the file's metadata for its last modification date and compares it to the
     * provided date to determine if the file needs to be updated.
     *
     * @param store        the storage system where the file will be saved
     * @param metadataUuid the unique identifier for the metadata associated with the file
     * @param file         the name of the file to be saved
     * @param visibility   the visibility scope for the file being saved
     * @param changeDate   the change date as a string, used to compare with the
     *                     file's last modification date
     * @param is           an InputStream containing the data of the file
     * @throws Exception if an error occurs while saving the file or interacting
     *                   with the storage system
     */
    protected void saveFile(final Store store, String metadataUuid, String file,
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
     * Handles the processing of a file, including updating or saving the file in the resource store
     * and removing any old file references.
     *
     * @param id         the identifier of the metadata to which the file belongs
     * @param file       the name of the file to be processed
     * @param visibility the visibility level of the resource, determining its accessibility
     * @param changeDate the date of the modification or update to the file
     * @param is         the input stream of the file content to be processed
     * @param files      the XML element containing file information to assist with the file update
     * @throws Exception if any error occurs during the processing of the file
     */
    protected void handleFile(String id, String file, MetadataResourceVisibility visibility, String changeDate,
                              InputStream is, Element files) throws Exception {
        if (files == null) {
            if (log.isDebugEnabled())
                log.debug("  - No file found in info.xml. Cannot update file:" + file);
        } else {
            final Store store = context.getBean("resourceStore", Store.class);
            final IMetadataUtils metadataUtils = context.getBean(IMetadataUtils.class);
            final String metadataUuid = metadataUtils.getMetadataUuid(id);
            removeOldFile(context, log, store, metadataUuid, files, visibility);
            saveFile(store, metadataUuid, file, visibility, changeDate, is);
        }
    }


    /**
     * Retrieves a metadata exchange format (MEF) file path for the given unique identifier (UUID).
     * This method is abstract and must be implemented by subclasses to define the specific retrieval logic.
     *
     * @param uuid the unique identifier of the metadata resource to retrieve
     * @return the file path of the retrieved MEF file
     * @throws URISyntaxException if the URI syntax of the resource is invalid
     * @throws IOException        if an I/O error occurs during retrieval
     */
    protected abstract Path retrieveMEF(String uuid) throws URISyntaxException, IOException;


    /**
     * Adds metadata to the system by processing a metadata exchange format (MEF) file.
     * The method imports metadata, processes associated files, and handles schema information
     * for the provided UUID. It uses visitor patterns to traverse and extract metadata, public and private
     * files, and related information from the MEF structure.
     *
     * @param ri          The record information object containing details about the metadata record to be added.
     * @param localRating Indicates whether the metadata should be locally rated or not.
     * @param uuid        The unique identifier associated with the metadata being imported.
     * @throws Exception If an error occurs while processing the MEF file, extracting metadata,
     *                   or performing operations on the metadata resources.
     */
    protected void addMetadata(final RecordInfo ri, final boolean localRating, String uuid) throws Exception {
        final String[] id = {null};
        final Element[] md = {null};

        //--- import metadata from MEF file
        Path mefFile = null;

        try {
            mefFile = retrieveMEF(uuid);
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

                public void handleMetadataFiles(DirectoryStream<Path> files, Element info, int index) throws Exception {
                    // Import valid metadata
                    Element metadataValidForImport = extractValidMetadataForImport(files, info);

                    if (metadataValidForImport != null) {
                        handleMetadata(metadataValidForImport, index);
                    }
                }

                public void handleInfo(Element info, int index) throws Exception {

                    final Element metadata = md[index];
                    String schema = metadataSchemaUtils.autodetectSchema(metadata, null);
                    if (info != null && info.getContentSize() != 0) {
                        Element general = info.getChild(GENERAL);
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

                public void handlePublicFile(String file, String changeDate, InputStream is, int index) throws Exception {
                    handleFile(file, changeDate, is, index, MetadataResourceVisibility.PUBLIC);
                }

                private void handleFile(String file, String changeDate, InputStream is, int index, MetadataResourceVisibility visibility) throws Exception {
                    if (id[index] == null) return;
                    if (log.isDebugEnabled()) {
                        log.debug("    - Adding remote " + visibility + " file with name: " + file);
                    }
                    final Store store = context.getBean("resourceStore", Store.class);
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

                public void indexMetadata(int index) throws Exception {
                    metadataIndexer.indexMetadata(id[index], true, IndexingMode.full);
                }
            });
        } catch (Exception e) {
            //--- we ignore the exception here. Maybe the metadata has been removed just now
            log.info("  - Skipped unretrievable metadata (maybe has been removed) with uuid:" + ri.uuid);

            result.unretrievable++;
            log.error(e);
        } finally {
            if (mefFile != null) {
                FileUtils.deleteQuietly(mefFile.toFile());
            }
        }
    }

    /**
     * Adds metadata to the system, handling various preprocessing steps
     * such as validation, schema processing, and privilege application.
     * It saves the metadata into the database and indexes it.
     *
     * @param ri          Information about the record to be added, including the remote UUID and schema details.
     * @param md          The metadata element to be added, typically in XML format.
     * @param info        Additional information related to the metadata, including general metadata details.
     * @param localRating Indicates whether the rating is to be resolved locally or taken from the remote information.
     * @param uuid        The unique universal identifier for the metadata to be added.
     * @return The ID of the added metadata as a String, or null if the metadata validation fails.
     * @throws Exception If an error occurs during the metadata insertion or processing.
     */
    private String addMetadata(RecordInfo ri, Element md, Element info, boolean localRating, String uuid) throws Exception {
        Element general = info.getChild(GENERAL);

        String createDate = general.getChildText("createDate");
        String changeDate = general.getChildText("changeDate");
        String isTemplate = general.getChildText("isTemplate");
        String siteId = general.getChildText("siteId");
        String popularity = general.getChildText("popularity");
        String schema = general.getChildText("schema");

        if ("true".equals(isTemplate)) {
            isTemplate = "y";
        } else {
            isTemplate = "n";
        }

        if (log.isDebugEnabled()) {
            log.debug("  - Adding metadata with remote uuid:" + ri.uuid);
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
            // * thumbnails contain full URL instead of file name only
            // * API mode changes the old URL structure.
            MetadataResourceDatabaseMigration.updateMetadataResourcesLink(md, null, settingManager);
        }

        if (!params.xslfilter.isEmpty()) {
            md = HarvesterUtil.processMetadata(metadataSchemaUtils.getSchema(schema),
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

        addCategories(metadata, params.getCategories(), localCategory, context, null, false);

        metadata = metadataManager.insertMetadata(context, metadata, md, IndexingMode.none, ufo, UpdateDatestamp.NO, false, false);

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
        if (((ArrayList<Group>) params.getGroupCopyPolicy()).isEmpty()) {
            addPrivileges(id, params.getPrivileges(), localGroups, context);
        } else {
            addPrivilegesFromGroupPolicy(id, info.getChild("privileges"));
        }
        context.getBean(IMetadataManager.class).save(metadata);

        metadataIndexer.indexMetadata(id, true, IndexingMode.full);
        result.addedMetadata++;

        return id;
    }

    /**
     * Extracts a valid metadata element suitable for import based on the provided schema preferences
     * and metadata attributes from a collection of files.
     * <br>
     * This method iterates through a directory stream of metadata files, attempts to autodetect
     * their schemas, and prioritizes them based on the schema defined in the provided info element,
     * followed by a preferred schema, and finally defaults to the first valid metadata file detected.
     * If no suitable metadata is found, the method will return null.
     *
     * @param files a directory stream containing metadata files to be processed
     * @param info  an element containing metadata information, including the schema used for prioritization
     * @return an Element corresponding to the valid metadata extracted for import, or null if no valid metadata is found
     * @throws IOException   if an I/O error occurs while processing the files
     * @throws JDOMException if an error occurs while handling XML content
     */
    private Element extractValidMetadataForImport(DirectoryStream<Path> files, Element info) throws IOException, JDOMException {
        Element metadataValidForImport;
        final String finalPreferredSchema = preferredSchema;

        String infoSchema = "_none_";
        if (info != null && info.getContentSize() != 0) {
            Element general = info.getChild(GENERAL);
            if (general != null && general.getContentSize() != 0 && general.getChildText("schema") != null) {
                infoSchema = general.getChildText("schema");
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
                    String metadataSchema = metadataSchemaUtils.autodetectSchema(metadata, null);
                    // If the local node doesn't know metadata
                    // schema try to load the next XML file.
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

        if (mdFiles.isEmpty()) {
            log.debug("No valid metadata file found" +
                ((lastUnknownMetadataFolderName == null) ?
                    "" :
                    (" in " + lastUnknownMetadataFolderName)
                ) + ".");
            return null;
        }

        // 1st: Select metadata with schema in an info file
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
}
