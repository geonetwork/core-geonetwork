/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.IMetadata;
import org.fao.geonet.domain.InspireAtomFeed;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatusId_;
import org.fao.geonet.domain.MetadataStatus_;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.MetadataValidationStatus;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.User;
import org.fao.geonet.events.md.MetadataIndexCompleted;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.IndexMetadataTask;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.InspireAtomFeedRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.util.ThreadUtils;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.jdom.Attribute;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.xlink.Processor;

/**
 * trunk-core
 * 
 * @author delawen
 * 
 * 
 */
public class DefaultMetadataIndexer
        implements IMetadataIndexer, ApplicationEventPublisherAware {

    protected Set<String> waitForIndexing = Collections.synchronizedSet(new HashSet<String>());
    protected Set<String> indexing = Collections.synchronizedSet(new HashSet<String>());
    protected Set<IndexMetadataTask> batchIndex = Collections.synchronizedSet(new HashSet<IndexMetadataTask>());

    protected ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private IMetadataUtils metadataUtils;

    @PersistenceContext
    private EntityManager _entityManager;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected MetadataValidationRepository mdValidationRepository;

    @Autowired
    protected MetadataRepository mdRepository;

    @Autowired
    protected MetadataStatusRepository mdStatusRepository;

    protected SchemaManager schemaManager;

    @Autowired
    protected GroupRepository groupRepository;

    @Autowired
    protected OperationAllowedRepository operationAllowedRepository;

    @Autowired
    protected InspireAtomFeedRepository inspireAtomFeedRepository;

    @Autowired
    protected SearchManager searchManager;

    /**
     * @param context
     */
    @Override
    public void init(ServiceContext context) {
        this.metadataUtils = context.getBean(IMetadataUtils.class);
        this.userRepository = context.getBean(UserRepository.class);
        this.mdValidationRepository = context
                .getBean(MetadataValidationRepository.class);
        this.mdRepository = context.getBean(MetadataRepository.class);
        this.mdStatusRepository = context
                .getBean(MetadataStatusRepository.class);
        this.groupRepository = context.getBean(GroupRepository.class);
        this.searchManager = context.getBean(SearchManager.class);
        this.operationAllowedRepository = context
                .getBean(OperationAllowedRepository.class);
        this.inspireAtomFeedRepository = context
                .getBean(InspireAtomFeedRepository.class);
        this.setSchemaManager(context.getBean(SchemaManager.class));
    }

    /**
     * @param schemaManager
     *            the schemaManager to set
     */
    @Autowired
    public void setSchemaManager(SchemaManager schemaManager) {
        this.schemaManager = schemaManager;
    }

    @Override
    public void setApplicationEventPublisher(
            ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataIndexer#rebuildIndexXLinkedMetadata(jeeves.server.context.ServiceContext)
     * @param context
     * @throws Exception
     */
    @Override
    public synchronized void rebuildIndexXLinkedMetadata(
            final ServiceContext context) throws Exception {

        // get all metadata with XLinks
        Set<Integer> toIndex = getSearchManager().getDocsWithXLinks();

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Will index " + toIndex.size() + " records with XLinks");
        if (toIndex.size() > 0) {
            // clean XLink Cache so that cache and index remain in sync
            Processor.clearCache();

            ArrayList<String> stringIds = new ArrayList<String>();
            for (Integer id : toIndex) {
                stringIds.add(id.toString());
            }
            // execute indexing operation
            batchIndexInThreadPool(context, stringIds);
        }
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataIndexer#rebuildIndexForSelection(jeeves.server.context.ServiceContext,
     *      boolean)
     * @param context
     * @param clearXlink
     * @throws Exception
     */
    @Override
    public synchronized void rebuildIndexForSelection(
            final ServiceContext context, boolean clearXlink) throws Exception {

        // get all metadata ids from selection
        ArrayList<String> listOfIdsToIndex = new ArrayList<String>();
        UserSession session = context.getUserSession();
        SelectionManager sm = SelectionManager.getManager(session);

        synchronized (sm.getSelection("metadata")) {
            for (Iterator<String> iter = sm.getSelection("metadata").iterator();
                 iter.hasNext(); ) {
                String uuid = (String) iter.next();
                String id = metadataUtils.getMetadataId(uuid);
                if (id != null) {
                    listOfIdsToIndex.add(id);
                }
            }
        }

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
            Log.debug(Geonet.DATA_MANAGER, "Will index " +
                listOfIdsToIndex.size() + " records from selection.");
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
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataIndexer#batchIndexInThreadPool(jeeves.server.context.ServiceContext,
     *      java.util.List)
     * @param context
     * @param metadataIds
     */
    @Override
    public void batchIndexInThreadPool(ServiceContext context,
            List<?> metadataIds) {

        TransactionStatus transactionStatus = null;
        try {
            transactionStatus = TransactionAspectSupport.currentTransactionStatus();
        } catch (NoTransactionException e) {
            // not in a transaction so we can go ahead.
        }
        // split reindexing task according to number of processors we can assign
        int threadCount = ThreadUtils.getNumberOfThreads();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        int perThread;
        if (metadataIds.size() < threadCount) perThread = metadataIds.size();
        else perThread = metadataIds.size() / threadCount;
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
            Runnable worker = new IndexMetadataTask(context, subList,
                batchIndex, transactionStatus, numIndexedTracker);
            executor.execute(worker);
            index += count;
        }

        executor.shutdown();
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataIndexer#isIndexing()
     * @return
     */
    @Override
    public boolean isIndexing() {
        try {
            return !indexing.isEmpty() || !batchIndex.isEmpty();
        } finally {
        }
    }

    /**
     * 
     * @param metadataIds
     * @throws Exception
     */
    @Override
    public void indexMetadata(final List<String> metadataIds) throws Exception {
        for (String metadataId : metadataIds) {
            indexMetadata(metadataId, false);
        }

        searchManager.forceIndexChanges();
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataIndexer#indexMetadata(java.lang.String,
     *      boolean)
     * @param metadataId
     * @param forceRefreshReaders
     * @throws Exception
     */
    @Override
    public void indexMetadata(final String metadataId,
            boolean forceRefreshReaders) throws Exception {
        try {
            if (waitForIndexing.contains(metadataId)) {
                return;
            }
            while (indexing.contains(metadataId)) {
                try {
                    waitForIndexing.add(metadataId);
                    // don't index the same metadata 2x
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    return;
                } finally {
                    waitForIndexing.remove(metadataId);
                }
            }
            indexing.add(metadataId);
        } finally {
        }
        Metadata fullMd;

        try {
            Vector<Element> moreFields = new Vector<Element>();
            int id$ = Integer.parseInt(metadataId);

            // get metadata, extracting and indexing any xlinks
            Element md = getXmlSerializer().selectNoXLinkResolver(metadataId, true, false);
            if (getXmlSerializer().resolveXLinks()) {
                List<Attribute> xlinks = Processor.getXLinks(md);
                if (xlinks.size() > 0) {
                    moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.HASXLINKS, "1", true, true));
                    StringBuilder sb = new StringBuilder();
                    for (Attribute xlink : xlinks) {
                        sb.append(xlink.getValue());
                        sb.append(" ");
                    }
                    moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.XLINK, sb.toString(), true, true));
                    Processor.detachXLink(md, getServiceContext());
                } else {
                    moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.HASXLINKS, "0", true, true));
                }
            } else {
                moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.HASXLINKS, "0", true, true));
            }

            fullMd = getMetadataRepository().findOne(id$);

            final String schema = fullMd.getDataInfo().getSchemaId();
            final String createDate = fullMd.getDataInfo().getCreateDate().getDateAndTime();
            final String changeDate = fullMd.getDataInfo().getChangeDate().getDateAndTime();
            final String source = fullMd.getSourceInfo().getSourceId();
            final MetadataType metadataType = fullMd.getDataInfo().getType();
            final String root = fullMd.getDataInfo().getRoot();
            final String uuid = fullMd.getUuid();
            final String extra = fullMd.getDataInfo().getExtra();
            final String isHarvested = String.valueOf(Constants.toYN_EnabledChar(fullMd.getHarvestInfo().isHarvested()));
            final String owner = String.valueOf(fullMd.getSourceInfo().getOwner());
            final Integer groupOwner = fullMd.getSourceInfo().getGroupOwner();
            final String popularity = String.valueOf(fullMd.getDataInfo().getPopularity());
            final String rating = String.valueOf(fullMd.getDataInfo().getRating());
            final String displayOrder = fullMd.getDataInfo().getDisplayOrder() == null ? null : String.valueOf(fullMd.getDataInfo().getDisplayOrder());
            final String draft = "N";

            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "record schema (" + schema + ")"); //DEBUG
                Log.debug(Geonet.DATA_MANAGER, "record createDate (" + createDate + ")"); //DEBUG
            }

            addMoreFields(fullMd, moreFields, id$, schema, createDate,
                    changeDate, source, metadataType, root, uuid, extra,
                    isHarvested, owner, groupOwner, popularity, rating,
                    displayOrder, draft);
            
            getSearchManager().index(getSchemaManager().getSchemaDir(schema), md, metadataId, moreFields, metadataType, root, forceRefreshReaders);
        } catch (Exception x) {
            Log.error(Geonet.DATA_MANAGER, "The metadata document index with id=" + metadataId + " is corrupt/invalid - ignoring it. Error: " + x.getMessage(), x);
            fullMd = null;
        } finally {
            try {
                indexing.remove(metadataId);
            } finally {
            }
        }
        if (fullMd != null) {
            applicationEventPublisher.publishEvent(new MetadataIndexCompleted(fullMd));
        }
    }

    protected void addMoreFields(IMetadata fullMd, Vector<Element> moreFields,
            int id$, final String schema, final String createDate,
            final String changeDate, final String source,
            final MetadataType metadataType, final String root,
            final String uuid, final String extra, final String isHarvested,
            final String owner, final Integer groupOwner,
            final String popularity, final String rating,
            final String displayOrder, final String draft) {
        addBasicMoreFields(moreFields, schema, createDate, changeDate,
                source, metadataType, root, uuid, extra, isHarvested, owner,
                popularity, rating, displayOrder);
        addAtom(moreFields, id$);
        addOwner(fullMd, moreFields, owner);
        OperationAllowedRepository operationAllowedRepository = getBean(OperationAllowedRepository.class);
        GroupRepository groupRepository = getBean(GroupRepository.class);

        addLogo(moreFields, source, groupOwner, groupRepository);
        addPrivileges(moreFields, id$, operationAllowedRepository,
                groupRepository);
        addCategories(fullMd, moreFields);
        addStatus(moreFields, id$);
        addValid(moreFields, id$);
        
        moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.DRAFT, draft, true, true));
    }

    protected void addOwner(IMetadata fullMd, Vector<Element> moreFields,
            final String owner) {
        if (owner != null) {
            User user = getBean(UserRepository.class).findOne(fullMd.getSourceInfo().getOwner());
            if (user != null) {
                moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.USERINFO, user.getUsername() + "|" + user.getSurname() + "|" + user
                    .getName() + "|" + user.getProfile(), true, false));
            }
        }
    }

    protected void addCategories(IMetadata fullMd, Vector<Element> moreFields) {
        //FIXME not very elegant...
        if(fullMd instanceof Metadata) {
            for (MetadataCategory category : ((Metadata)fullMd).getMetadataCategories()) {
                moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.CAT, category.getName(), true, true));
            }
        } else if(fullMd instanceof MetadataDraft) {
            for (MetadataCategory category :((MetadataDraft)fullMd).getMetadataCategories()) {
                moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.CAT, category.getName(), true, true));
            }
        }
    }

    protected void addLogo(Vector<Element> moreFields, final String source,
            final Integer groupOwner, GroupRepository groupRepository) {
        
        final ServiceContext serviceContext = getServiceContext();
        
        String logoUUID = null;
        if (groupOwner != null) {
            final Group group = groupRepository.findOne(groupOwner);
            if (group != null) {
                moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.GROUP_OWNER, String.valueOf(groupOwner), true, true));
                final boolean preferGroup = getSettingManager().getValueAsBool(Settings.SYSTEM_PREFER_GROUP_LOGO, true);
                if (group.getWebsite() != null && !group.getWebsite().isEmpty() && preferGroup) {
                    moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.GROUP_WEBSITE, group.getWebsite(), true, false));
                }
                if (group.getLogo() != null && preferGroup) {
                    logoUUID = group.getLogo();
                }
            }
        }

        // Group logo are in the harvester folder and contains extension in file name
        final Path harvesterLogosDir = Resources.locateHarvesterLogosDir(serviceContext);
        boolean added = false;
        if (StringUtils.isNotEmpty(logoUUID)) {
            final Path logoPath = harvesterLogosDir.resolve(logoUUID);
            if (Files.exists(logoPath)) {
                added = true;
                moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.LOGO, "/images/harvesting/" + logoPath.getFileName(), true, false));
            }
        }

        // If not available, use the local catalog logo
        if (!added) {
            logoUUID = source + ".png";
            final Path logosDir = Resources.locateLogosDir(serviceContext);
            final Path logoPath = logosDir.resolve(logoUUID);
            if (Files.exists(logoPath)) {
                moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.LOGO, "/images/logos/" + logoUUID, true, false));
            }
        }
    }

    protected void addAtom(Vector<Element> moreFields, int id$) {
        // If the metadata has an atom document, index related information
        InspireAtomFeedRepository inspireAtomFeedRepository = getApplicationContext().getBean(InspireAtomFeedRepository.class);
        InspireAtomFeed feed = inspireAtomFeedRepository.findByMetadataId(id$);

        if ((feed != null) && StringUtils.isNotEmpty(feed.getAtom())) {
            moreFields.add(SearchManager.makeField("has_atom", "y", true, true));
            moreFields.add(SearchManager.makeField("any", feed.getAtom(), false, true));
        }
    }

    protected void addPrivileges(Vector<Element> moreFields, int id$,
            OperationAllowedRepository operationAllowedRepository,
            GroupRepository groupRepository) {
        // get privileges
        List<OperationAllowed> operationsAllowed = operationAllowedRepository.findAllById_MetadataId(id$);

        for (OperationAllowed operationAllowed : operationsAllowed) {
            OperationAllowedId operationAllowedId = operationAllowed.getId();
            int groupId = operationAllowedId.getGroupId();
            int operationId = operationAllowedId.getOperationId();

            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.OP_PREFIX + operationId, String.valueOf(groupId), true, true));
            if (operationId == ReservedOperation.view.getId()) {
                Group g = groupRepository.findOne(groupId);
                if (g != null) {
                    moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.GROUP_PUBLISHED, g.getName(), true, true));
                }
            }
        }
    }

    protected void addStatus(Vector<Element> moreFields, int id$) {
        final MetadataStatusRepository statusRepository = getApplicationContext().getBean(MetadataStatusRepository.class);

        // get status
        Sort statusSort = new Sort(Sort.Direction.DESC, MetadataStatus_.id.getName() + "." + MetadataStatusId_.changeDate.getName());
        List<MetadataStatus> statuses = statusRepository.findAllById_MetadataId(id$, statusSort);
        if (!statuses.isEmpty()) {
            MetadataStatus stat = statuses.get(0);
            String status = String.valueOf(stat.getId().getStatusId());
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.STATUS, status, true, true));
            String statusChangeDate = stat.getId().getChangeDate().getDateAndTime();
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.STATUS_CHANGE_DATE, statusChangeDate, true, true));
        }
    }

    protected void addValid(Vector<Element> moreFields, int id$) {
        // getValidationInfo
        // -1 : not evaluated
        // 0 : invalid
        // 1 : valid
        MetadataValidationRepository metadataValidationRepository = getBean(MetadataValidationRepository
            .class);
        List<MetadataValidation> validationInfo = metadataValidationRepository.findAllById_MetadataId(id$);
        if (validationInfo.isEmpty()) {
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.VALID, "-1", true, true));
        } else {
            String isValid = "1";
            for (MetadataValidation vi : validationInfo) {
                String type = vi.getId().getValidationType();
                MetadataValidationStatus status = vi.getStatus();
                if (status == MetadataValidationStatus.INVALID && vi.isRequired()) {
                    isValid = "0";
                }
                moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.VALID + "_" + type, status.getCode(), true, true));
            }
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.VALID, isValid, true, true));
        }
    }

    protected Element processXLinks(final String metadataId,
            Vector<Element> moreFields) throws Exception {
        Element md = getXmlSerializer().selectNoXLinkResolver(metadataId, true, false);
        if (getXmlSerializer().resolveXLinks()) {
            List<Attribute> xlinks = Processor.getXLinks(md);
            if (xlinks.size() > 0) {
                moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.HASXLINKS, "1", true, true));
                StringBuilder sb = new StringBuilder();
                for (Attribute xlink : xlinks) {
                    sb.append(xlink.getValue());
                    sb.append(" ");
                }
                moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.XLINK, sb.toString(), true, true));
                Processor.detachXLink(md, getServiceContext());
            } else {
                moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.HASXLINKS, "0", true, true));
            }
        } else {
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.HASXLINKS, "0", true, true));
        }
        return md;
    }

    protected void addBasicMoreFields(Vector<Element> moreFields,
            final String schema, final String createDate,
            final String changeDate, final String source,
            final MetadataType metadataType, final String root,
            final String uuid, final String extra, final String isHarvested,
            final String owner, final String popularity, final String rating,
            final String displayOrder) {
        moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.ROOT, root, true, true));
        moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.SCHEMA, schema, true, true));
        moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.DATABASE_CREATE_DATE, createDate, true, true));
        moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.DATABASE_CHANGE_DATE, changeDate, true, true));
        moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.SOURCE, source, true, true));
        moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.IS_TEMPLATE, metadataType.codeString, true, true));
        moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.UUID, uuid, true, true));
        moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.IS_HARVESTED, isHarvested, true, true));
        moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.OWNER, owner, true, true));
        moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.DUMMY, "0", false, true));
        moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.POPULARITY, popularity, true, true));
        moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.RATING, rating, true, true));
        moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.DISPLAY_ORDER, displayOrder, true, false));
        moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.EXTRA, extra, false, true));
    }

    protected ServiceContext getServiceContext() {
        // TODO
        ServiceContext context = ServiceContext.get();
        return context;
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataIndexer#rescheduleOptimizer(java.util.Calendar,
     *      int)
     * @param beginAt
     * @param interval
     * @throws Exception
     */
    @Override
    public void rescheduleOptimizer(Calendar beginAt, int interval)
            throws Exception {
        searchManager.rescheduleOptimizer(beginAt, interval);
    }

    /**
     * 
     * @throws Exception
     */
    @Override
    public void disableOptimizer() throws Exception {
        searchManager.disableOptimizer();
    }
    


    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataIndexer#forceIndexChanges()
     * @throws IOException
     */
    @Override
    public void forceIndexChanges() throws IOException {
        searchManager.forceIndexChanges();
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataIndexer#batchDeleteMetadataAndUpdateIndex(org.springframework.data.jpa.domain.Specification)
     * @param specification
     * @return
     * @throws Exception
     */
    @Override
    public int batchDeleteMetadataAndUpdateIndex(
            Specification<Metadata> specification) throws Exception {
        final List<Integer> idsOfMetadataToDelete = mdRepository
                .findAllIdsBy(specification);

        for (Integer id : idsOfMetadataToDelete) {
            //--- remove metadata directory for each record
            final Path metadataDataDir = getApplicationContext().getBean(GeonetworkDataDirectory.class).getMetadataDataDir();
            Path pb = Lib.resource.getMetadataDir(metadataDataDir, id + "");
            IO.deleteFileOrDirectory(pb);
        }

        // Remove records from the index
        getSearchManager().delete("_id", Lists.transform(idsOfMetadataToDelete, new Function<Integer, String>() {
            @Nullable
            @Override
            public String apply(@Nonnull Integer input) {
                return input.toString();
            }
        }));

        // Remove records from the database
        getMetadataRepository().deleteAll(specification);

        return idsOfMetadataToDelete.size();
    }


    private MetadataRepository getMetadataRepository() {
        return mdRepository;
    }

    protected SearchManager getSearchManager() {
        return searchManager;
    }
    
    protected XmlSerializer getXmlSerializer() {
        return ApplicationContextHolder.get()
                .getBean(XmlSerializer.class);
    }
    
    protected SchemaManager getSchemaManager() {
        return schemaManager;
    }

    protected SettingManager getSettingManager() {
        return getBean(SettingManager.class);
    }
    
    private ApplicationContext getApplicationContext() {
        return ApplicationContextHolder.get();
    }

    protected <T> T getBean(Class<T> requiredType) {
        return getApplicationContext().getBean(requiredType);
    }
}
