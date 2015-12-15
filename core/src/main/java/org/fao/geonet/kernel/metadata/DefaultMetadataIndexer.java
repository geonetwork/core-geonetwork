/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
import org.fao.geonet.domain.InspireAtomFeed;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
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
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.IndexMetadataTask;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
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

    Set<String> waitForIndexing = new HashSet<String>();
    Set<String> indexing = new HashSet<String>();
    Set<IndexMetadataTask> batchIndex = new ConcurrentHashSet<IndexMetadataTask>();
    Lock indexLock = new ReentrantLock();

    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private DataManager dm;

    @PersistenceContext
    private EntityManager _entityManager;

    @Autowired
    private ApplicationContext _applicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MetadataValidationRepository mdValidationRepository;

    @Autowired
    private MetadataRepository mdRepository;

    @Autowired
    private MetadataStatusRepository mdStatusRepository;

    private SchemaManager schemaManager;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private OperationAllowedRepository operationAllowedRepository;

    @Autowired
    private InspireAtomFeedRepository inspireAtomFeedRepository;

    @Autowired
    private SearchManager searchManager;

    private EditLib editLib;

    /**
     * @param schemaManager
     *            the schemaManager to set
     */
    @Autowired
    public void setSchemaManager(SchemaManager schemaManager) {
        this.schemaManager = schemaManager;
        this.editLib = new EditLib(this.schemaManager);
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
        Set<Integer> toIndex = context.getBean(SearchManager.class)
                .getDocsWithXLinks();

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER,
                    "Will index " + toIndex.size() + " records with XLinks");
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
            for (Iterator<String> iter = sm.getSelection("metadata")
                    .iterator(); iter.hasNext();) {
                String uuid = (String) iter.next();
                String id = dm.getMetadataId(uuid);
                if (id != null) {
                    listOfIdsToIndex.add(id);
                }
            }
        }

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
            Log.debug(Geonet.DATA_MANAGER, "Will index "
                    + listOfIdsToIndex.size() + " records from selection.");
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
            transactionStatus = TransactionAspectSupport
                    .currentTransactionStatus();
        } catch (NoTransactionException e) {
            // not in a transaction so we can go ahead.
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
            Log.debug(Geonet.INDEX_ENGINE,
                    "Indexing " + metadataIds.size() + " records.");
            Log.debug(Geonet.INDEX_ENGINE, metadataIds.toString());
        }
        AtomicInteger numIndexedTracker = new AtomicInteger();
        while (index < metadataIds.size()) {
            int start = index;
            int count = Math.min(perThread, metadataIds.size() - start);
            int nbRecords = start + count;

            if (Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
                Log.debug(Geonet.INDEX_ENGINE,
                        "Indexing records from " + start + " to " + nbRecords);
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
        indexLock.lock();
        try {
            return !indexing.isEmpty() || !batchIndex.isEmpty();
        } finally {
            indexLock.unlock();
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
        indexLock.lock();
        try {
            if (waitForIndexing.contains(metadataId)) {
                return;
            }
            while (indexing.contains(metadataId)) {
                try {
                    waitForIndexing.add(metadataId);
                    // don't index the same metadata 2x
                    wait(200);
                } catch (InterruptedException e) {
                    return;
                } finally {
                    waitForIndexing.remove(metadataId);
                }
            }
            indexing.add(metadataId);
        } finally {
            indexLock.unlock();
        }
        Metadata fullMd;

        try {
            Vector<Element> moreFields = new Vector<Element>();
            int id$ = Integer.parseInt(metadataId);

            // get metadata, extracting and indexing any xlinks
            Element md = _applicationContext.getBean(XmlSerializer.class)
                    .selectNoXLinkResolver(metadataId, true);
            if (_applicationContext.getBean(XmlSerializer.class)
                    .resolveXLinks()) {
                List<Attribute> xlinks = Processor.getXLinks(md);
                if (xlinks.size() > 0) {
                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.HASXLINKS, "1", true, true));
                    StringBuilder sb = new StringBuilder();
                    for (Attribute xlink : xlinks) {
                        sb.append(xlink.getValue());
                        sb.append(" ");
                    }
                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.XLINK, sb.toString(), true,
                            true));
                    Processor.detachXLink(md, getServiceContext());
                } else {
                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.HASXLINKS, "0", true, true));
                }
            } else {
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.HASXLINKS, "0", true, true));
            }

            fullMd = mdRepository.findOne(id$);

            final String schema = fullMd.getDataInfo().getSchemaId();
            final String createDate = fullMd.getDataInfo().getCreateDate()
                    .getDateAndTime();
            final String changeDate = fullMd.getDataInfo().getChangeDate()
                    .getDateAndTime();
            final String source = fullMd.getSourceInfo().getSourceId();
            final MetadataType metadataType = fullMd.getDataInfo().getType();
            final String root = fullMd.getDataInfo().getRoot();
            final String uuid = fullMd.getUuid();
            final String extra = fullMd.getDataInfo().getExtra();
            final String isHarvested = String.valueOf(Constants
                    .toYN_EnabledChar(fullMd.getHarvestInfo().isHarvested()));
            final String owner = String
                    .valueOf(fullMd.getSourceInfo().getOwner());
            final Integer groupOwner = fullMd.getSourceInfo().getGroupOwner();
            final String popularity = String
                    .valueOf(fullMd.getDataInfo().getPopularity());
            final String rating = String
                    .valueOf(fullMd.getDataInfo().getRating());
            final String displayOrder = fullMd.getDataInfo()
                    .getDisplayOrder() == null ? null
                            : String.valueOf(
                                    fullMd.getDataInfo().getDisplayOrder());

            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER,
                        "record schema (" + schema + ")"); // DEBUG
                Log.debug(Geonet.DATA_MANAGER,
                        "record createDate (" + createDate + ")"); // DEBUG
            }

            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.ROOT,
                    root, true, true));
            moreFields.add(SearchManager.makeField(
                    Geonet.IndexFieldNames.SCHEMA, schema, true, true));
            moreFields.add(SearchManager.makeField(
                    Geonet.IndexFieldNames.DATABASE_CREATE_DATE, createDate,
                    true, true));
            moreFields.add(SearchManager.makeField(
                    Geonet.IndexFieldNames.DATABASE_CHANGE_DATE, changeDate,
                    true, true));
            moreFields.add(SearchManager.makeField(
                    Geonet.IndexFieldNames.SOURCE, source, true, true));
            moreFields.add(
                    SearchManager.makeField(Geonet.IndexFieldNames.IS_TEMPLATE,
                            metadataType.codeString, true, true));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.UUID,
                    uuid, true, true));
            moreFields.add(
                    SearchManager.makeField(Geonet.IndexFieldNames.IS_HARVESTED,
                            isHarvested, true, true));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.OWNER,
                    owner, true, true));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.DUMMY,
                    "0", false, true));
            moreFields.add(SearchManager.makeField(
                    Geonet.IndexFieldNames.POPULARITY, popularity, true, true));
            moreFields.add(SearchManager.makeField(
                    Geonet.IndexFieldNames.RATING, rating, true, true));
            moreFields.add(SearchManager.makeField(
                    Geonet.IndexFieldNames.DISPLAY_ORDER, displayOrder, true,
                    false));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.EXTRA,
                    extra, false, true));

            // If the metadata has an atom document, index related information
            InspireAtomFeed feed = inspireAtomFeedRepository
                    .findByMetadataId(id$);

            if ((feed != null) && StringUtils.isNotEmpty(feed.getAtom())) {
                moreFields.add(
                        SearchManager.makeField("has_atom", "y", true, true));
                moreFields.add(SearchManager.makeField("any", feed.getAtom(),
                        false, true));
            }

            if (owner != null) {
                User user = userRepository
                        .findOne(fullMd.getSourceInfo().getOwner());
                if (user != null) {
                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.USERINFO,
                            user.getUsername() + "|" + user.getSurname() + "|"
                                    + user.getName() + "|" + user.getProfile(),
                            true, false));
                }
            }

            String logoUUID = null;
            if (groupOwner != null) {
                final Group group = groupRepository.findOne(groupOwner);
                if (group != null) {
                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.GROUP_OWNER,
                            String.valueOf(groupOwner), true, true));
                    final boolean preferGroup = _applicationContext
                            .getBean(SettingManager.class).getValueAsBool(
                                    SettingManager.SYSTEM_PREFER_GROUP_LOGO,
                                    true);
                    if (group.getWebsite() != null
                            && !group.getWebsite().isEmpty() && preferGroup) {
                        moreFields.add(SearchManager.makeField(
                                Geonet.IndexFieldNames.GROUP_WEBSITE,
                                group.getWebsite(), true, false));
                    }
                    if (group.getLogo() != null && preferGroup) {
                        logoUUID = group.getLogo();
                    }
                }
            }
            if (logoUUID == null) {
                logoUUID = source;
            }

            if (logoUUID != null) {
                final Path logosDir = Resources
                        .locateLogosDir(getServiceContext());
                final String[] logosExt = { "png", "PNG", "gif", "GIF", "jpg",
                        "JPG", "jpeg", "JPEG", "bmp", "BMP", "tif", "TIF",
                        "tiff", "TIFF" };
                boolean added = false;
                for (String ext : logosExt) {
                    final Path logoPath = logosDir
                            .resolve(logoUUID + "." + ext);
                    if (Files.exists(logoPath)) {
                        added = true;
                        moreFields.add(SearchManager
                                .makeField(Geonet.IndexFieldNames.LOGO,
                                        "/images/logos/"
                                                + logoPath.getFileName(),
                                        true, false));
                        break;
                    }
                }

                if (!added) {
                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.LOGO,
                            "/images/logos/" + logoUUID + ".png", true, false));
                }
            }

            // get privileges
            List<OperationAllowed> operationsAllowed = operationAllowedRepository
                    .findAllById_MetadataId(id$);

            for (OperationAllowed operationAllowed : operationsAllowed) {
                OperationAllowedId operationAllowedId = operationAllowed
                        .getId();
                int groupId = operationAllowedId.getGroupId();
                int operationId = operationAllowedId.getOperationId();

                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.OP_PREFIX + operationId,
                        String.valueOf(groupId), true, true));
                if (operationId == ReservedOperation.view.getId()) {
                    Group g = groupRepository.findOne(groupId);
                    if (g != null) {
                        moreFields.add(SearchManager.makeField(
                                Geonet.IndexFieldNames.GROUP_PUBLISHED,
                                g.getName(), true, true));
                    }
                }
            }

            for (MetadataCategory category : fullMd.getCategories()) {
                moreFields
                        .add(SearchManager.makeField(Geonet.IndexFieldNames.CAT,
                                category.getName(), true, true));
            }

            // get status
            Sort statusSort = new Sort(Sort.Direction.DESC,
                    MetadataStatus_.id.getName() + "."
                            + MetadataStatusId_.changeDate.getName());
            List<MetadataStatus> statuses = mdStatusRepository
                    .findAllById_MetadataId(id$, statusSort);
            if (!statuses.isEmpty()) {
                MetadataStatus stat = statuses.get(0);
                String status = String.valueOf(stat.getId().getStatusId());
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.STATUS, status, true, true));
                String statusChangeDate = stat.getId().getChangeDate()
                        .getDateAndTime();
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.STATUS_CHANGE_DATE,
                        statusChangeDate, true, true));
            }

            // getValidationInfo
            // -1 : not evaluated
            // 0 : invalid
            // 1 : valid
            List<MetadataValidation> validationInfo = mdValidationRepository
                    .findAllById_MetadataId(id$);
            if (validationInfo.isEmpty()) {
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.VALID, "-1", true, true));
            } else {
                String isValid = "1";
                for (MetadataValidation vi : validationInfo) {
                    String type = vi.getId().getValidationType();
                    MetadataValidationStatus status = vi.getStatus();
                    if (status == MetadataValidationStatus.INVALID
                            && vi.isRequired()) {
                        isValid = "0";
                    }
                    moreFields.add(SearchManager.makeField(
                            Geonet.IndexFieldNames.VALID + "_" + type,
                            status.getCode(), true, true));
                }
                moreFields.add(SearchManager.makeField(
                        Geonet.IndexFieldNames.VALID, isValid, true, true));
            }
            searchManager.index(schemaManager.getSchemaDir(schema), md,
                    metadataId, moreFields, metadataType, root,
                    forceRefreshReaders);
        } catch (Exception x) {
            Log.error(Geonet.DATA_MANAGER,
                    "The metadata document index with id=" + metadataId
                            + " is corrupt/invalid - ignoring it. Error: "
                            + x.getMessage(),
                    x);
            fullMd = null;
        } finally {
            indexLock.lock();
            try {
                indexing.remove(metadataId);
            } finally {
                indexLock.unlock();
            }
        }
        if (fullMd != null) {
            applicationEventPublisher
                    .publishEvent(new MetadataIndexCompleted(fullMd));
        }
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
            // --- remove metadata directory for each record
            final Path metadataDataDir = ApplicationContextHolder.get()
                    .getBean(GeonetworkDataDirectory.class)
                    .getMetadataDataDir();
            Path pb = Lib.resource.getMetadataDir(metadataDataDir, id + "");
            IO.deleteFileOrDirectory(pb);
        }

        // Remove records from the index
        searchManager.delete("_id", Lists.transform(idsOfMetadataToDelete,
                new Function<Integer, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nonnull Integer input) {
                        return input.toString();
                    }
                }));

        // Remove records from the database
        mdRepository.deleteAll(specification);

        return idsOfMetadataToDelete.size();
    }

}
