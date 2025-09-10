//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.datamanager.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.yammer.metrics.core.TimerContext;
import jeeves.monitor.MonitorManager;
import jeeves.monitor.timer.IndexingRecordMeter;
import jeeves.monitor.timer.IndexingRecordTimer;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.xlink.Processor;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.annotations.IndexIgnore;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.domain.userfeedback.RatingsSetting;
import org.fao.geonet.events.history.RecordDeletedEvent;
import org.fao.geonet.events.md.MetadataIndexCompleted;
import org.fao.geonet.events.md.MetadataIndexStarted;
import org.fao.geonet.kernel.*;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.datamanager.draft.DraftMetadataIndexer;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.IndexFields;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.userfeedback.UserFeedbackRepository;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.util.ThreadUtils;
import org.fao.geonet.utils.Log;
import org.jdom.Attribute;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fao.geonet.resources.Resources.DEFAULT_LOGO_EXTENSION;


public class BaseMetadataIndexer implements IMetadataIndexer, ApplicationEventPublisherAware {
    @Autowired
    private EsSearchManager searchManager;
    @Autowired
    private SourceRepository sourceRepository;
    @Autowired
    protected MetadataStatusRepository statusRepository;
    @Autowired
    MonitorManager monitorManager;
    private IMetadataUtils metadataUtils;
    private IMetadataManager metadataManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OperationAllowedRepository operationAllowedRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private MetadataValidationRepository metadataValidationRepository;
    @Autowired
    private SchemaManager schemaManager;
    @Autowired(required = false)
    private SvnManager svnManager;
    @Autowired
    private InspireAtomFeedRepository inspireAtomFeedRepository;
    @Autowired(required = false)
    private XmlSerializer xmlSerializer;
    @Autowired
    @Lazy
    private SettingManager settingManager;
    @Autowired
    private UserFeedbackRepository userFeedbackRepository;
    @Autowired
    @Qualifier("resourceStore")
    private Store store;
    @Autowired
    private Resources resources;

    // FIXME remove when get rid of Jeeves
    private ServiceContext servContext;

    private ApplicationEventPublisher publisher;

    @Autowired
    private UserSavedSelectionRepository userSavedSelectionRepository;

    public BaseMetadataIndexer() {
    }

    public void init(ServiceContext context, Boolean force) throws Exception {
        servContext = context;
    }

    @Override
    public void setMetadataUtils(IMetadataUtils metadataUtils) {
        this.metadataUtils = metadataUtils;
    }

    @Override
    public void setMetadataManager(IMetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }

    Set<String> waitForIndexing = new HashSet<String>();
    Set<String> indexing = new HashSet<String>();
    Set<IndexMetadataTask> batchIndex = ConcurrentHashMap.newKeySet();

    @Override
    public void forceIndexChanges() throws IOException {
        searchManager.forceIndexChanges();
    }

    @Override
    public int batchDeleteMetadataAndUpdateIndex(Specification<? extends AbstractMetadata> specification)
        throws Exception {
        final List<? extends AbstractMetadata> metadataToDelete = metadataUtils.findAll(specification);

        // Remove records from the database
        // Delete all works on a database created by hibernate
        // (because some foreign constraints are missing.
        // See https://github.com/geonetwork/core-geonetwork/issues/1863). FIXME
        // Delete all does not work on older database
        // where operationAllowed contains references to the metadata table.
        //
//        for (AbstractMetadata md : metadataToDelete) {
//            // --- remove metadata directory for each record
//            store.delResources(ServiceContext.get(), md.getUuid(), true);
//        }
//
//        // Remove records from the index
//        searchManager.delete(metadataToDelete.stream().map(input -> Integer.toString(input.getId())).collect(Collectors.toList()));
//        metadataManager.deleteAll(specification);
        // So delete one by one even if slower
        metadataToDelete.forEach(md -> {
            try {
                // Extract information for RecordDeletedEvent
                LinkedHashMap<String, String> titles = metadataUtils.extractTitles(Integer.toString(md.getId()));
                UserSession userSession = ServiceContext.get().getUserSession();
                String xmlBefore = md.getData();

                store.delResources(ServiceContext.get(), md.getUuid());
                metadataManager.deleteMetadata(ServiceContext.get(), String.valueOf(md.getId()));

                // Trigger RecordDeletedEvent
                new RecordDeletedEvent(md.getId(), md.getUuid(), titles, userSession.getUserIdAsInt(), xmlBefore).publish(ApplicationContextHolder.get());
            } catch (Exception e) {
                Log.warning(Geonet.DATA_MANAGER, String.format(

                    "Error during removal of metadata %s part of batch delete operation. " +
                        "This error may create a ghost record (ie. not in the index " +
                        "but still present in the database). " +
                        "You can reindex the catalogue to see it again. " +
                        "Error was: %s.", md.getUuid(), e.getMessage()));
                e.printStackTrace();
            }
        });

        return metadataToDelete.size();
    }

    /**
     * Reindex all records in current selection.
     */
    @Override
    public synchronized void rebuildIndexForSelection(final ServiceContext context, String bucket, boolean clearXlink)
        throws Exception {

        // get all metadata ids from selection
        ArrayList<String> listOfIdsToIndex = new ArrayList<String>();
        UserSession session = context.getUserSession();
        SelectionManager sm = SelectionManager.getManager(session);

        synchronized (sm.getSelection(bucket)) {
            for (Iterator<String> iter = sm.getSelection(bucket).iterator(); iter.hasNext(); ) {
                String uuid = (String) iter.next();
                String id = metadataUtils.getMetadataId(uuid);
                if (id != null) {
                    listOfIdsToIndex.add(id);
                }
            }
        }

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
            Log.debug(Geonet.DATA_MANAGER, "Will index " + listOfIdsToIndex.size() + " records from selection.");
        }

        if (listOfIdsToIndex.size() > 0) {
            // clean XLink Cache so that cache and index remain in sync
            if (clearXlink) {
                Processor.clearCache();
            }

            // execute indexing operation
            batchIndexInThreadPool(context, listOfIdsToIndex);
        }
    }

    /**
     * Index multiple metadata in a separate thread. Wait until the current
     * transaction commits before starting threads (to make sure that all metadata
     * are committed).
     *
     * @param context     context object
     * @param metadataIds the metadata ids to index
     */
    @Override
    public void batchIndexInThreadPool(ServiceContext context, List<?> metadataIds) {
        batchIndexInThreadPool(context, metadataIds, null);
    }

    @Override
    public void batchIndexInThreadPool(ServiceContext context, List<?> metadataIds, TransactionStatus pTransactionStatus) {
        TransactionStatus transactionStatus = pTransactionStatus;
        if (transactionStatus == null) {
            try {
                transactionStatus = TransactionAspectSupport.currentTransactionStatus();
            } catch (NoTransactionException e) {
                // not in a transaction so we can go ahead.
            }
        }
        // split reindexing task according to number of processors we can assign
        int threadCount = ThreadUtils.getNumberOfThreads();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        int perThread;
        if (metadataIds.size() < threadCount)
            perThread = metadataIds.size();
        else
            perThread = metadataIds.size() / threadCount;
        int index = 0;
        if (Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
            Log.debug(Geonet.INDEX_ENGINE, "Indexing " + metadataIds.size() + " records.");
            Log.debug(Geonet.INDEX_ENGINE, metadataIds.toString());
        }
        AtomicInteger numIndexedTracker = new AtomicInteger();
        while (index < metadataIds.size()) {
            int start = index;
            int count = Math.min(perThread, metadataIds.size() - start);
            int nbRecords = start + count;

            if (Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
                Log.debug(Geonet.INDEX_ENGINE, "Indexing records from " + start + " to " + nbRecords);
            }

            List<?> subList = metadataIds.subList(start, nbRecords);

            if (Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
                Log.debug(Geonet.INDEX_ENGINE, subList.toString());
            }

            // create threads to process this chunk of ids
            Runnable worker = new IndexMetadataTask(context, subList, batchIndex, transactionStatus, numIndexedTracker);
            executor.execute(worker);
            index += count;
        }

        executor.shutdown();
    }

    @Override
    public boolean isIndexing() {
        return searchManager.isIndexing();
    }

    @Override
    public void indexMetadata(final List<String> metadataIds) throws Exception {
        for (String metadataId : metadataIds) {
            indexMetadata(metadataId, true, IndexingMode.full);
        }
    }

    @Override
    public void indexMetadata(final String metadataId,
                              final boolean forceRefreshReaders,
                              final IndexingMode indexingMode)
        throws Exception {
        AbstractMetadata fullMd;
        monitorManager.getMeter(IndexingRecordMeter.class).mark();
        TimerContext timerContext = monitorManager.getTimer(IndexingRecordTimer.class).time();
        long start = System.currentTimeMillis();
        try {
            Multimap<String, Object> fields = ArrayListMultimap.create();
            int id$ = Integer.parseInt(metadataId);

            // get metadata, extracting and indexing any xlinks
            Element md = getXmlSerializer().selectNoXLinkResolver(metadataId, true, false);
            final ServiceContext serviceContext = getServiceContext();
            if (getXmlSerializer().resolveXLinks()) {
                List<Attribute> xlinks = Processor.getXLinks(md);
                if (xlinks.size() > 0) {
                    fields.put(Geonet.IndexFieldNames.HASXLINKS, true);
                    for (Attribute xlink : xlinks) {
                        fields.put(Geonet.IndexFieldNames.XLINK, xlink.getValue());
                        fields.put(Geonet.IndexFieldNames.XLINK, xlink.getValue().replaceAll("local://srv/api/registries/entries/(.*)\\?.*", "$1"));
                    }
                    Processor.detachXLink(md, getServiceContext());
                } else {
                    fields.put(Geonet.IndexFieldNames.HASXLINKS, false);
                }
            } else {
                fields.put(Geonet.IndexFieldNames.HASXLINKS, false);
            }

            fullMd = metadataUtils.findOne(id$);

            final String schema = fullMd.getDataInfo().getSchemaId();
            final String createDate = fullMd.getDataInfo().getCreateDate().getDateAndTime();
            final String changeDate = fullMd.getDataInfo().getChangeDate().getDateAndTime();
            final String source = fullMd.getSourceInfo().getSourceId();
            final MetadataType metadataType = fullMd.getDataInfo().getType();
            final String uuid = fullMd.getUuid();
            String indexKey = uuid;
            if (fullMd instanceof MetadataDraft) {
                indexKey += "-draft";
            }

            final String extra = fullMd.getDataInfo().getExtra();
            final boolean isHarvested = fullMd.getHarvestInfo().isHarvested();
            final String owner = String.valueOf(fullMd.getSourceInfo().getOwner());
            final Integer groupOwner = fullMd.getSourceInfo().getGroupOwner();
            final String popularity = String.valueOf(fullMd.getDataInfo().getPopularity());
            final String rating = String.valueOf(fullMd.getDataInfo().getRating());
            final String displayOrder = fullMd.getDataInfo().getDisplayOrder() == null ? null
                : String.valueOf(fullMd.getDataInfo().getDisplayOrder());

            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "record schema (" + schema + ")"); // DEBUG
                Log.debug(Geonet.DATA_MANAGER, "record createDate (" + createDate + ")"); // DEBUG
            }

            fields.put(Geonet.IndexFieldNames.SCHEMA, schema);
            fields.put(Geonet.IndexFieldNames.RECORDLINKFLAG, "record");
            fields.put(Geonet.IndexFieldNames.DATABASE_CREATE_DATE, createDate);
            fields.put(Geonet.IndexFieldNames.DATABASE_CHANGE_DATE, changeDate);
            fields.put(Geonet.IndexFieldNames.SOURCE, source);
            fields.put(Geonet.IndexFieldNames.IS_TEMPLATE, metadataType.codeString);
            fields.put(Geonet.IndexFieldNames.UUID, uuid);
            fields.put(Geonet.IndexFieldNames.ID, metadataId);
            fields.put(Geonet.IndexFieldNames.FEATUREOFRECORD, "record");
            fields.put(Geonet.IndexFieldNames.IS_HARVESTED, isHarvested);
            if (isHarvested) {
                fields.put(Geonet.IndexFieldNames.HARVESTUUID, fullMd.getHarvestInfo().getUuid());
            }
            fields.put(Geonet.IndexFieldNames.OWNER, owner);


            if (!schemaManager.existsSchema(schema)) {
                fields.put(IndexFields.DRAFT, "n");
                fields.put(IndexFields.INDEXING_ERROR_FIELD, true);
                fields.put(IndexFields.INDEXING_ERROR_MSG,
                    searchManager.createIndexingErrorMsgObject("indexingErrorMsg-schemaNotRegistered",
                        "error",
                        Map.of("record", metadataId, "schema", schema)));
                searchManager.index(null, md, indexKey, fields, metadataType,
                    forceRefreshReaders, indexingMode);
                Log.error(Geonet.DATA_MANAGER, String.format(
                    "Record %s / Schema '%s' is not registered in this catalog. Install it or remove those records. Record is indexed indexing error flag.",
                    metadataId, schema));
            } else {

                fields.put(Geonet.IndexFieldNames.POPULARITY, popularity);
                fields.put(Geonet.IndexFieldNames.RATING, rating);

                if (RatingsSetting.ADVANCED.equals(settingManager.getValue(Settings.SYSTEM_LOCALRATING_ENABLE))) {
                    int nbOfFeedback = userFeedbackRepository.findByMetadata_Uuid(uuid).size();
                    fields.put(Geonet.IndexFieldNames.FEEDBACKCOUNT, nbOfFeedback);
                }

                fields.put(Geonet.IndexFieldNames.DISPLAY_ORDER, displayOrder);
                fields.put(Geonet.IndexFieldNames.EXTRA, extra);

                // If the metadata has an atom document, index related information
                InspireAtomFeed feed = inspireAtomFeedRepository.findByMetadataId(id$);

                if ((feed != null) && StringUtils.isNotEmpty(feed.getAtom())) {
                    fields.put("atomfeed", feed.getAtom());
                }

                if (owner != null) {
                    Optional<User> userOpt = userRepository.findById(fullMd.getSourceInfo().getOwner());
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        fields.put(Geonet.IndexFieldNames.USERINFO, user.getUsername() + "|" + user.getSurname() + "|" + user
                            .getName() + "|" + user.getProfile());
                        fields.put(Geonet.IndexFieldNames.OWNERNAME, user.getName() + " " + user.getSurname());
                    }
                }

                String logoUUID = null;
                if (groupOwner != null) {
                    final Optional<Group> groupOpt = groupRepository.findById(groupOwner);
                    if (groupOpt.isPresent()) {
                        Group group = groupOpt.get();
                        fields.put(Geonet.IndexFieldNames.GROUP_OWNER, String.valueOf(groupOwner));
                        final boolean preferGroup = settingManager.getValueAsBool(Settings.SYSTEM_PREFER_GROUP_LOGO, true);
                        if (group.getWebsite() != null && !group.getWebsite().isEmpty() && preferGroup) {
                            fields.put(Geonet.IndexFieldNames.GROUP_WEBSITE, group.getWebsite());
                        }
                        if (group.getLogo() != null && preferGroup) {
                            logoUUID = group.getLogo();
                        }
                    }
                }

                // Group logo are in the harvester folder and contains extension in file name
                boolean added = false;
                if (StringUtils.isNotEmpty(logoUUID)) {
                    final Path harvesterLogosDir = resources.locateHarvesterLogosDir(getServiceContext());
                    try (Resources.ResourceHolder logo = resources.getImage(getServiceContext(), logoUUID, harvesterLogosDir)) {
                        if (logo != null) {
                            added = true;
                            fields.put(Geonet.IndexFieldNames.LOGO,
                                "/images/harvesting/" + logo.getPath().getFileName());
                        }
                    }
                }

                // If not available, use the local catalog logo
                if (!added) {
                    Source sourceCatalogue = sourceRepository.findOneByUuid(source);
                    logoUUID =
                        sourceCatalogue != null
                            && StringUtils.isNotEmpty(sourceCatalogue.getLogo())
                        ? sourceCatalogue.getLogo() : source + DEFAULT_LOGO_EXTENSION;
                    final Path logosDir = resources.locateLogosDir(getServiceContext());
                    try (Resources.ResourceHolder image = resources.getImage(getServiceContext(), logoUUID, logosDir)) {
                        if (image != null) {
                            fields.put(Geonet.IndexFieldNames.LOGO,
                                "/images/logos/" + logoUUID);
                        }
                    }
                }

                fields.putAll(buildFieldsForPrivileges(id$));

                for (MetadataCategory category : fullMd.getCategories()) {
                    fields.put(Geonet.IndexFieldNames.CAT, category.getName());
                }

                // get status
                Sort statusSort = Sort.by(Sort.Direction.DESC,
                    MetadataStatus_.changeDate.getName());
                List<MetadataStatus> statuses = statusRepository.findAllByMetadataIdAndByType(id$, StatusValueType.workflow, statusSort);
                if (!statuses.isEmpty()) {
                    MetadataStatus stat = statuses.get(0);
                    String status = String.valueOf(stat.getStatusValue().getId());
                    fields.put(Geonet.IndexFieldNames.STATUS, status);
                    String statusChangeDate = stat.getChangeDate().getDateAndTime();
                    fields.put(Geonet.IndexFieldNames.STATUS_CHANGE_DATE, statusChangeDate);
                }

                // getValidationInfo
                // -1 : not evaluated
                // 0 : invalid
                // 1 : valid
                List<MetadataValidation> validationInfo = metadataValidationRepository.findAllById_MetadataId(id$);
                if (validationInfo.isEmpty()) {
                    fields.put(Geonet.IndexFieldNames.VALID, "-1");
                } else {
                    String isValid = "1";
                    boolean hasInspireValidation = false;
                    for (MetadataValidation vi : validationInfo) {
                        String type = vi.getId().getValidationType();
                        MetadataValidationStatus status = vi.getStatus();

                        // TODO: Check if ignore INSPIRE validation?
                        if (!type.equalsIgnoreCase("inspire")) {
                            // If never validated and required then set status to never validated.
                            if (status == MetadataValidationStatus.NEVER_CALCULATED && vi.isRequired()) {
                                isValid = "-1";
                            }
                            if (status == MetadataValidationStatus.INVALID && vi.isRequired() && isValid != "-1") {
                                isValid = "0";
                            }
                        } else {
                            hasInspireValidation = true;
                            fields.put(Geonet.IndexFieldNames.INSPIRE_REPORT_URL, vi.getReportUrl());
                            fields.put(Geonet.IndexFieldNames.INSPIRE_VALIDATION_DATE, vi.getValidationDate().getDateAndTime());
                        }
                        fields.put(Geonet.IndexFieldNames.VALID + "_" + type, status.getCode());
                    }
                    fields.put(Geonet.IndexFieldNames.VALID, isValid);

                    if (!hasInspireValidation) {
                        fields.put(Geonet.IndexFieldNames.VALID_INSPIRE, "-1");
                    }
                }

                // index the amount of users that have saved this record in the "Preferred Records" list (id=0)
                int savedCount = userSavedSelectionRepository.countTimesUserSavedMetadata(uuid, 0);
                fields.put(Geonet.IndexFieldNames.USER_SAVED_COUNT, savedCount);

                // Add metadata file store information
                fields.putAll(indexMetadataFileStore(fullMd));

                fields.putAll(addExtraFields(fullMd));

                if (fullMd != null) {
                    this.publisher.publishEvent(new MetadataIndexStarted(fullMd, fields));
                }

                searchManager.index(schemaManager.getSchemaDir(schema), md, indexKey, fields, metadataType,
                    forceRefreshReaders, indexingMode);
            }
        } catch (Exception x) {
            Log.error(Geonet.DATA_MANAGER, "The metadata document index with id=" + metadataId
                + " is corrupt/invalid - ignoring it. Error: " + x.getMessage(), x);
            fullMd = null;
        } finally {
            timerContext.stop();
        }
        if (fullMd != null) {
            this.publisher.publishEvent(new MetadataIndexCompleted(fullMd));
        }
        Log.warning(Geonet.INDEX_ENGINE, String.format("Record #%s (mode: %s) indexed in %dms",
            metadataId, indexingMode, System.currentTimeMillis() - start));
    }


    @Override
    public void indexMetadataPrivileges(String uuid, int id) throws Exception {
        Set<String> operationFields = new HashSet<>();
        Arrays.asList(ReservedOperation.values()).forEach(o ->
            operationFields.add("op" + o.getId())
        );

        searchManager.updateFields(uuid, buildFieldsForPrivileges(id), operationFields);
    }

    private Multimap<String, Object> buildFieldsForPrivileges(int recordId) {
        List<OperationAllowed> operationsAllowed = operationAllowedRepository.findAllById_MetadataId(recordId);
        Multimap<String, Object> privilegesFields = ArrayListMultimap.create();
        boolean isPublishedToAll = false;
        boolean isPublishedToIntranet = false;
        boolean isPublishedToGuest = false;

        for (OperationAllowed operationAllowed : operationsAllowed) {
            OperationAllowedId operationAllowedId = operationAllowed.getId();
            int groupId = operationAllowedId.getGroupId();
            int operationId = operationAllowedId.getOperationId();

            privilegesFields.put(Geonet.IndexFieldNames.OP_PREFIX + operationId, String.valueOf(groupId));
            if (operationId == ReservedOperation.view.getId()) {
                Optional<Group> g = groupRepository.findById(groupId);
                if (g.isPresent()) {
                    privilegesFields.put(Geonet.IndexFieldNames.GROUP_PUBLISHED, g.get().getName());
                    privilegesFields.put(Geonet.IndexFieldNames.GROUP_PUBLISHED + "Id", g.get().getId());


                    if (g.get().getId() == ReservedGroup.all.getId()) {
                        isPublishedToAll = true;
                    } else if (g.get().getId() == ReservedGroup.intranet.getId()) {
                        isPublishedToIntranet = true;
                    } else if (g.get().getId() == ReservedGroup.guest.getId()) {
                        isPublishedToGuest = true;
                    }
                }
            }
        }

        if (isPublishedToAll) {
            privilegesFields.put(Geonet.IndexFieldNames.IS_PUBLISHED_TO_ALL, true);
        } else {
            privilegesFields.put(Geonet.IndexFieldNames.IS_PUBLISHED_TO_ALL, false);
        }

        if (isPublishedToIntranet) {
            privilegesFields.put(Geonet.IndexFieldNames.IS_PUBLISHED_TO_INTRANET, true);
        } else {
            privilegesFields.put(Geonet.IndexFieldNames.IS_PUBLISHED_TO_INTRANET, false);
        }

        if (isPublishedToGuest) {
            privilegesFields.put(Geonet.IndexFieldNames.IS_PUBLISHED_TO_GUEST, true);
        } else {
            privilegesFields.put(Geonet.IndexFieldNames.IS_PUBLISHED_TO_GUEST, false);
        }

        return privilegesFields;
    }


    /**
     * Function to be overrided by children to add extra fields cleanly.
     * Don't forget to call always super.addExtraFields, just in case
     *
     * @param fullMd
     */
    protected Multimap<String, Object> addExtraFields(AbstractMetadata fullMd) {
        // If we are not using draft utils, mark all as "no draft"
        // needed to be compatible with UI searches that check draft existence
        Multimap<String, Object> extraFields = ArrayListMultimap.create();
        if (!DraftMetadataIndexer.class.isInstance(this)) {
            extraFields.put(Geonet.IndexFieldNames.DRAFT, "n");
        }
        return extraFields;
    }

    private XmlSerializer getXmlSerializer() {
        return xmlSerializer;
    }

    /**
     * @param context
     * @param id
     * @param md
     * @throws Exception
     */
    @Override
    public void versionMetadata(ServiceContext context, String id, Element md) throws Exception {
        if (svnManager != null) {
            svnManager.createMetadataDir(id, context, md);
        }
    }

    private ServiceContext getServiceContext() {
        ServiceContext context = ServiceContext.get();
        return context == null ? servContext : context;
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Get file store properties to be used in the index.
     *
     * @return multimap object representing the file store resources to be added to the index.
     */
    public Multimap<String, Object> indexMetadataFileStore(AbstractMetadata fullMd) {
        Multimap<String, Object> indexMetadataFileStoreFields = ArrayListMultimap.create();
        try {
            List<MetadataResource> metadataResources = store.getResources(
                ServiceContext.get(),
                fullMd.getUuid(),
                (org.fao.geonet.api.records.attachments.Sort) null,
                null,
                !(fullMd instanceof MetadataDraft));

            if (metadataResources != null && !metadataResources.isEmpty()) {

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                // Exclude fields with the index ignore annotation
                objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
                    @Override
                    public boolean hasIgnoreMarker(AnnotatedMember member) {
                        return member.hasAnnotation(IndexIgnore.class) || super.hasIgnoreMarker(member);
                    }

                    @Override
                    public PropertyName findNameForSerialization(Annotated annotated) {
                        if (annotated.hasAnnotation(IndexIgnore.class)) return null;
                        return super.findNameForSerialization(annotated);
                    }
                });

                JsonNode jsonNode = objectMapper.valueToTree(metadataResources);
                indexMetadataFileStoreFields.put("fileStore", jsonNode);
            }
        } catch (Exception e) {
            Log.warning(Geonet.INDEX_ENGINE, String.format(
                "Resource for metadata '%s'(%d) is not accessible or cannot be found. Skipping index. Error is: %s",
                fullMd.getUuid(), fullMd.getId(), e.getMessage()));
        }
        return indexMetadataFileStoreFields;
    }
}
