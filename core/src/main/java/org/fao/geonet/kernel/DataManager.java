//==============================================================================
//===
//=== DataManager
//===
//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.transaction.TransactionManager;
import jeeves.transaction.TransactionTask;
import jeeves.xlink.Processor;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Geonet.Namespaces;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.domain.MetadataDataInfo_;
import org.fao.geonet.domain.MetadataFileUpload;
import org.fao.geonet.domain.MetadataFileUpload_;
import org.fao.geonet.domain.MetadataHarvestInfo;
import org.fao.geonet.domain.MetadataRatingByIp;
import org.fao.geonet.domain.MetadataRatingByIpId;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatusId;
import org.fao.geonet.domain.MetadataStatusId_;
import org.fao.geonet.domain.MetadataStatus_;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.MetadataValidationId;
import org.fao.geonet.domain.MetadataValidationStatus;
import org.fao.geonet.domain.Metadata_;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.Schematron;
import org.fao.geonet.domain.SchematronCriteria;
import org.fao.geonet.domain.SchematronCriteriaGroup;
import org.fao.geonet.domain.SchematronRequirement;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.UserGroupId;
import org.fao.geonet.exceptions.JeevesException;
import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.exceptions.SchemaMatchConflictException;
import org.fao.geonet.exceptions.SchematronValidationErrorEx;
import org.fao.geonet.exceptions.ServiceNotAllowedEx;
import org.fao.geonet.exceptions.XSDValidationErrorEx;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.index.IndexingList;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.notifier.MetadataNotifierManager;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataFileUploadRepository;
import org.fao.geonet.repository.MetadataRatingByIpRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.SchematronCriteriaGroupRepository;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.StatusValueRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.MetadataFileUploadSpecs;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.MetadataStatusSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.repository.specification.UserSpecs;
import org.fao.geonet.repository.statistic.PathSpec;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.util.ThreadUtils;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.Xml.ErrorHandler;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.Root;

import static org.fao.geonet.kernel.schema.MetadataSchema.SCHEMATRON_DIR;
import static org.fao.geonet.repository.specification.MetadataSpecs.hasMetadataUuid;
import static org.springframework.data.jpa.domain.Specifications.where;

/**
 * Handles all operations on metadata (select,insert,update,delete etc...).
 *
 */
//@Transactional(propagation = Propagation.REQUIRED, noRollbackFor = {XSDValidationErrorEx.class, NoSchemaMatchesException.class})
public class DataManager {

    private static final int METADATA_BATCH_PAGE_SIZE = 100000;

    @PersistenceContext
    private EntityManager _entityManager;
    @Autowired
    private ApplicationContext _applicationContext;
    @Autowired
    private MetadataRepository _metadataRepository;
    @Autowired
    private MetadataValidationRepository _metadataValidationRepository;
    @Autowired
    private AccessManager  accessMan;
    @Autowired
    private SearchManager  searchMan;
    @Autowired
    private SettingManager settingMan;
    @Autowired
    private SchemaManager  schemaMan;
    @Autowired
    private XmlSerializer xmlSerializer;
    @Autowired
    private SvnManager svnManager;

    // initialize in init method
    private ServiceContext servContext;
    private EditLib editLib;

    private java.nio.file.Path dataDir;
    private java.nio.file.Path thesaurusDir;
    private java.nio.file.Path stylePath;


    private String baseURL;

    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------

    /**
     *
     * @return
     */
    public EditLib getEditLib() {
        return editLib;
    }

    /**
     * Init Data manager and refresh index if needed.
     * Can also be called after GeoNetwork startup in order to rebuild the lucene
     * index
     *
     * @param context
     * @param force         Force reindexing all from scratch
     *
     **/
    public synchronized void init(ServiceContext context, Boolean force) throws Exception {
        this.servContext = context;
        final GeonetworkDataDirectory dataDirectory = context.getBean(GeonetworkDataDirectory.class);
        stylePath = dataDirectory.resolveWebResource(Geonet.Path.STYLESHEETS);
        editLib = new EditLib(schemaMan);
        dataDir = _applicationContext.getBean(GeonetworkDataDirectory.class).getSystemDataDir();
        thesaurusDir = _applicationContext.getBean(ThesaurusManager.class).getThesauriDirectory();

        if (context.getUserSession() == null) {
            UserSession session = new UserSession();
            context.setUserSession(session);
            session.loginAs(new User().setUsername("admin").setId(-1).setProfile(Profile.Administrator));
        }
        // get lastchangedate of all metadata in index
        Map<String,String> docs = searchMan.getDocsChangeDate();

        // set up results HashMap for post processing of records to be indexed
        ArrayList<String> toIndex = new ArrayList<String>();

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "INDEX CONTENT:");


        Sort sortByMetadataChangeDate = SortUtils.createSort(Metadata_.dataInfo, MetadataDataInfo_.changeDate);
        int currentPage=0;
        Page<Pair<Integer, ISODate>> results = _metadataRepository.findAllIdsAndChangeDates(new PageRequest(currentPage,
                METADATA_BATCH_PAGE_SIZE, sortByMetadataChangeDate));


        // index all metadata in DBMS if needed
        while (results.getNumberOfElements() > 0) {
            for (Pair<Integer, ISODate> result : results) {

                // get metadata
                String id = String.valueOf(result.one());

                if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                    Log.debug(Geonet.DATA_MANAGER, "- record (" + id + ")");
                }

                String idxLastChange = docs.get(id);

                // if metadata is not indexed index it
                if (idxLastChange == null) {
                    Log.debug(Geonet.DATA_MANAGER, "-  will be indexed");
                    toIndex.add(id);

                    // else, if indexed version is not the latest index it
                } else {
                    docs.remove(id);

                    String lastChange = result.two().toString();

                    if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                        Log.debug(Geonet.DATA_MANAGER, "- lastChange: " + lastChange);
                    if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                        Log.debug(Geonet.DATA_MANAGER, "- idxLastChange: " + idxLastChange);

                    // date in index contains 't', date in DBMS contains 'T'
                    if (force || !idxLastChange.equalsIgnoreCase(lastChange)) {
                        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                            Log.debug(Geonet.DATA_MANAGER, "-  will be indexed");
                        toIndex.add(id);
                    }
                }
            }

            currentPage++;
            results = _metadataRepository.findAllIdsAndChangeDates(new PageRequest(currentPage, METADATA_BATCH_PAGE_SIZE,
                    sortByMetadataChangeDate));
        }

        // if anything to index then schedule it to be done after servlet is
        // up so that any links to local fragments are resolvable
        if (toIndex.size() > 0) {
            batchIndexInThreadPool(context, toIndex);
        }

        if (docs.size() > 0) { // anything left?
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "INDEX HAS RECORDS THAT ARE NOT IN DB:");
            }
        }

        // remove from index metadata not in DBMS
        for (String id : docs.keySet()) {
            searchMan.delete("_id", id);

            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "- removed record (" + id + ") from index");
            }
        }
    }

    /**
     * Search for all records having XLinks (ie. indexed with
     * _hasxlinks flag), clear the cache and reindex all
     * records found.
     *
     * @param context
     * @throws Exception
     */
    public synchronized void rebuildIndexXLinkedMetadata(final ServiceContext context) throws Exception {

        // get all metadata with XLinks
        Set<Integer> toIndex = searchMan.getDocsWithXLinks();

        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Will index "+toIndex.size()+" records with XLinks");
        if ( toIndex.size() > 0 ) {
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
     * Reindex all records in current selection.
     *
     * @param context
     * @param clearXlink
     * @throws Exception
     */
    public synchronized void rebuildIndexForSelection(final ServiceContext context,
                                                      boolean clearXlink)
            throws Exception {

        // get all metadata ids from selection
        ArrayList<String> listOfIdsToIndex = new ArrayList<String>();
        UserSession session = context.getUserSession();
        SelectionManager sm = SelectionManager.getManager(session);

        synchronized (sm.getSelection("metadata")) {
            for (Iterator<String> iter = sm.getSelection("metadata").iterator();
                 iter.hasNext(); ) {
                String uuid = (String) iter.next();
                String id = getMetadataId(uuid);
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
     * Index multiple metadata in a separate thread.  Wait until the current transaction commits before
     * starting threads (to make sure that all metadata are committed).
     *
     * @param context context object
     * @param metadataIds the metadata ids to index
     */
    public void batchIndexInThreadPool(ServiceContext context, List<String> metadataIds) {

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
        while(index < metadataIds.size()) {
            int start = index;
            int count = Math.min(perThread, metadataIds.size() - start);
            int nbRecords = start + count;

            if (Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
                Log.debug(Geonet.INDEX_ENGINE, "Indexing records from " + start + " to " + nbRecords);
            }

            List<String> subList = metadataIds.subList(start, nbRecords);

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

    Lock indexLock = new ReentrantLock();
    Set<String> waitForIndexing = new HashSet<String>();
    Set<String> indexing = new HashSet<String>();
    Set<IndexMetadataTask> batchIndex = new ConcurrentHashSet<IndexMetadataTask>();

    public boolean isIndexing() {
        indexLock.lock();
        try {
            return !indexing.isEmpty() || !batchIndex.isEmpty();
        } finally {
            indexLock.unlock();
        }
    }

    public void indexMetadata(final List<String> metadataIds) throws Exception {
        for (String metadataId : metadataIds) {
            indexMetadata(metadataId, false);
        }
    }
    /**
     * TODO javadoc.
     *
     * @param metadataId
     * @throws Exception
     */
    public void indexMetadata(final String metadataId, boolean forceRefreshReaders) throws Exception {
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
        try {
            Vector<Element> moreFields = new Vector<Element>();
            int id$ = Integer.valueOf(metadataId);

            // get metadata, extracting and indexing any xlinks
            Element md   = xmlSerializer.selectNoXLinkResolver(metadataId, true);
            if (xmlSerializer.resolveXLinks()) {
                List<Attribute> xlinks = Processor.getXLinks(md);
                if (xlinks.size() > 0) {
                    moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.HASXLINKS, "1", true, true));
                    StringBuilder sb = new StringBuilder();
                    for (Attribute xlink : xlinks) {
                        sb.append(xlink.getValue()); sb.append(" ");
                    }
                    moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.XLINK, sb.toString(), true, true));
                    Processor.detachXLink(md, servContext);
                } else {
                    moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.HASXLINKS, "0", true, true));
                }
            } else {
                moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.HASXLINKS, "0", true, true));
            }

            final Metadata fullMd = _metadataRepository.findOne(id$);

            final String  schema     = fullMd.getDataInfo().getSchemaId();
            final String  createDate = fullMd.getDataInfo().getCreateDate().getDateAndTime();
            final String  changeDate = fullMd.getDataInfo().getChangeDate().getDateAndTime();
            final String  source     = fullMd.getSourceInfo().getSourceId();
            final MetadataType metadataType = fullMd.getDataInfo().getType();
            final String  root       = fullMd.getDataInfo().getRoot();
            final String  uuid       = fullMd.getUuid();
            final String  extra       = fullMd.getDataInfo().getExtra();
            final String  isHarvested = String.valueOf(Constants.toYN_EnabledChar(fullMd.getHarvestInfo().isHarvested()));
            final String  owner      = String.valueOf(fullMd.getSourceInfo().getOwner());
            final Integer groupOwner = fullMd.getSourceInfo().getGroupOwner();
            final String  popularity = String.valueOf(fullMd.getDataInfo().getPopularity());
            final String  rating     = String.valueOf(fullMd.getDataInfo().getRating());
            final String  displayOrder = fullMd.getDataInfo().getDisplayOrder() == null ? null : String.valueOf(fullMd.getDataInfo().getDisplayOrder());

            if(Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "record schema (" + schema + ")"); //DEBUG
                Log.debug(Geonet.DATA_MANAGER, "record createDate (" + createDate + ")"); //DEBUG
            }

            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.ROOT,        root,        true, true));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.SCHEMA,      schema,      true, true));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.CREATE_DATE,  createDate,  true, true));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.CHANGE_DATE,  changeDate,  true, true));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.SOURCE,      source,      true, true));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.IS_TEMPLATE,  metadataType.codeString,  true, true));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.UUID,        uuid,        true, true));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.IS_HARVESTED, isHarvested, true, true));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.OWNER,       owner,       true, true));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.DUMMY,       "0",        false, true));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.POPULARITY,  popularity,  true, true));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.RATING,      rating,      true, true));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.DISPLAY_ORDER,displayOrder, true, false));
            moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.EXTRA,       extra,       true, false));

            if (owner != null) {
                User user = _applicationContext.getBean(UserRepository.class).findOne(fullMd.getSourceInfo().getOwner());
                if (user != null) {
                    moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.USERINFO, user.getUsername() + "|" + user.getSurname() + "|" + user
                            .getName() + "|" + user.getProfile(), true, false));
                }
            }

            OperationAllowedRepository operationAllowedRepository = _applicationContext.getBean(OperationAllowedRepository.class);
            GroupRepository groupRepository = _applicationContext.getBean(GroupRepository.class);

            String logoUUID = null;
            if (groupOwner != null) {
                final Group group = groupRepository.findOne(groupOwner);
                if (group != null) {
                    moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.GROUP_OWNER, String.valueOf(groupOwner), true, true));
                    moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.GROUP_WEBSITE, group.getWebsite(), true, false));
                    if (group.getLogo() != null) {
                        logoUUID = group.getLogo();
                    }
                }
            }
            if (logoUUID == null) {
                logoUUID = source;
            }

            if (logoUUID != null) {
                final Path logosDir = Resources.locateLogosDir(servContext);
                final String[] logosExt = {"png", "PNG", "gif", "GIF", "jpg", "JPG", "jpeg", "JPEG", "bmp", "BMP",
                        "tif", "TIF", "tiff", "TIFF"};
                boolean added = false;
                for (String ext : logosExt) {
                    final Path logoPath = logosDir.resolve(logoUUID + "." + ext);
                    if (Files.exists(logoPath)) {
                        added = true;
                        moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.LOGO, "/images/logos/" + logoPath.getFileName(), true, false));
                        break;
                    }
                }

                if (!added) {
                    moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.LOGO, "/images/logos/" + logoUUID + ".gif", true, false));
                }
            }

            // get privileges
            List<OperationAllowed> operationsAllowed = operationAllowedRepository.findAllById_MetadataId(id$);

            for (OperationAllowed operationAllowed : operationsAllowed) {
                OperationAllowedId operationAllowedId = operationAllowed.getId();
                int groupId = operationAllowedId.getGroupId();
                int operationId = operationAllowedId.getOperationId();

                moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.OP_PREFIX + operationId, String.valueOf(groupId), true, true));
                if(operationId == ReservedOperation.view.getId()) {
                    Group g = groupRepository.findOne(groupId);
                    if (g != null) {
                        moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.GROUP_PUBLISHED, g.getName(), true, true));
                    }
                }
            }

            for (MetadataCategory category : fullMd.getCategories()) {
                moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.CAT, category.getName(), true, true));
            }

            final MetadataStatusRepository statusRepository = _applicationContext.getBean(MetadataStatusRepository.class);

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

            // getValidationInfo
            // -1 : not evaluated
            // 0 : invalid
            // 1 : valid
            MetadataValidationRepository metadataValidationRepository = _applicationContext.getBean(MetadataValidationRepository.class);
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
            searchMan.index(schemaMan.getSchemaDir(schema), md, metadataId, moreFields, metadataType, root, forceRefreshReaders);
        } catch (Exception x) {
            Log.error(Geonet.DATA_MANAGER, "The metadata document index with id=" + metadataId + " is corrupt/invalid - ignoring it. Error: " + x.getMessage(), x);
        } finally {
            indexLock.lock();
            try {
                indexing.remove(metadataId);
            } finally {
                indexLock.unlock();
            }
        }
    }

    /**
     *
     * @param beginAt
     * @param interval
     * @throws Exception
     */
    public void rescheduleOptimizer(Calendar beginAt, int interval) throws Exception {
        searchMan.rescheduleOptimizer(beginAt, interval);
    }

    /**
     *
     * @throws Exception
     */
    public void disableOptimizer() throws Exception {
        searchMan.disableOptimizer();
    }



    //--------------------------------------------------------------------------
    //---
    //--- Schema management API
    //---
    //--------------------------------------------------------------------------

    /**
     *
     * @param name
     * @return
     */
    public MetadataSchema getSchema(String name) {
        return schemaMan.getSchema(name);
    }

    /**
     *
     * @return
     */
    public Set<String> getSchemas() {
        return schemaMan.getSchemas();
    }

    /**
     *
     * @param name
     * @return
     */
    public boolean existsSchema(String name) {
        return schemaMan.existsSchema(name);
    }

    /**
     *
     * @param name
     * @return
     */
    public Path getSchemaDir(String name) {
        return schemaMan.getSchemaDir(name);
    }

    /**
     * Use this validate method for XML documents with dtd.
     *
     * @param schema
     * @param doc
     * @throws Exception
     */
    public void validate(String schema, Document doc) throws Exception {
        Xml.validate(doc);
    }

    /**
     * Use this validate method for XML documents with xsd validation.
     *
     * @param schema
     * @param md
     * @throws Exception
     */
    public void validate(String schema, Element md) throws Exception {
        String schemaLoc = md.getAttributeValue("schemaLocation", Namespaces.XSI);
        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Extracted schemaLocation of "+schemaLoc);
        if (schemaLoc == null) schemaLoc = "";

        if (schema == null) {
            // must use schemaLocation
            Xml.validate(md);
        } else {
            // if schemaLocation use that
            if (!schemaLoc.equals("")) {
                Xml.validate(md);
                // otherwise use supplied schema name
            } else {
                Xml.validate(getSchemaDir(schema).resolve(Geonet.File.SCHEMA), md);
            }
        }
    }

    /**
     * TODO javadoc.
     *
     * @param schema
     * @param md
     * @param eh
     * @return
     * @throws Exception
     */
    public Element validateInfo(String schema, Element md, ErrorHandler eh) throws Exception {
        String schemaLoc = md.getAttributeValue("schemaLocation", Namespaces.XSI);
        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Extracted schemaLocation of "+schemaLoc);
        if (schemaLoc == null) schemaLoc = "";

        if (schema == null) {
            // must use schemaLocation
            return Xml.validateInfo(md, eh);
        } else {
            // if schemaLocation use that
            if (!schemaLoc.equals("")) {
                return Xml.validateInfo(md, eh);
                // otherwise use supplied schema name
            } else {
                return Xml.validateInfo(getSchemaDir(schema).resolve(Geonet.File.SCHEMA), md, eh);
            }
        }
    }

    /**
     * Creates XML schematron report.
     * @param schema
     * @param md
     * @param lang
     * @return
     * @throws Exception
     */
    public Element doSchemaTronForEditor(String schema,Element md,String lang) throws Exception {
        // enumerate the metadata xml so that we can report any problems found
        // by the schematron_xml script to the geonetwork editor
        editLib.enumerateTree(md);

        // get an xml version of the schematron errors and return for error display
        Element schemaTronXmlReport = getSchemaTronXmlReport(schema, md, lang, null);

        // remove editing info added by enumerateTree
        editLib.removeEditingInfo(md);

        return schemaTronXmlReport;
    }

    /**
     * TODO javadoc.
     *
     * @param id
     * @return
     * @throws Exception
     */
    public String getMetadataSchema(String id) throws Exception {
        Metadata md = _metadataRepository.findOne(id);

        if (md == null) {
            throw new IllegalArgumentException("Metadata not found for id : " +id);
        } else {
            // get metadata
            return md.getDataInfo().getSchemaId();
        }
    }

    /**
     *
     * @param context
     * @param id
     * @param md
     * @throws Exception
     */
    public void versionMetadata(ServiceContext context, String id, Element md) throws Exception {
        if (svnManager != null) {
            svnManager.createMetadataDir(id, context, md);
        }
    }

    /**
     * Start an editing session. This will record the original metadata record
     * in the session under the {@link org.fao.geonet.constants.Geonet.Session#METADATA_BEFORE_ANY_CHANGES} + id
     * session property.
     * 
     * The record contains geonet:info element.
     * 
     * Note: Only the metadata record is stored in session. If the editing
     * session upload new documents or thumbnails, those documents will not
     * be cancelled. This needs improvements.
     * 
     * @param context
     * @param id
     * @throws Exception
     */
    public void startEditingSession(ServiceContext context, String id)
        throws Exception {
      if(Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
        Log.debug(Geonet.EDITOR_SESSION, "Editing session starts for record " + id);
      }
      
      boolean keepXlinkAttributes = false;
      boolean forEditing = false;
      boolean withValidationErrors = false;
      Element metadataBeforeAnyChanges = getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);
      context.getUserSession().setProperty(Geonet.Session.METADATA_BEFORE_ANY_CHANGES + id, metadataBeforeAnyChanges);
    }

    /**
     * Rollback to the record in the state it was when the editing session started
     * (See {@link #startEditingSession(ServiceContext, String)}).
     * 
     * @param context
     * @param id
     * @throws Exception
     */
    public void cancelEditingSession(ServiceContext context,
        String id) throws Exception {
        UserSession session = context.getUserSession();
        Element metadataBeforeAnyChanges = (Element) session.getProperty(Geonet.Session.METADATA_BEFORE_ANY_CHANGES + id);
        
        if(Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
              Log.debug(Geonet.EDITOR_SESSION, 
                  "Editing session end. Cancel changes. Restore record " + id + 
                  ". Replace by original record which was: ");
        }
        
        if (metadataBeforeAnyChanges != null) {
            if(Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
              Log.debug(Geonet.EDITOR_SESSION, " > restoring record: ");
              Log.debug(Geonet.EDITOR_SESSION, Xml.getString(metadataBeforeAnyChanges));
            }
            Element info = metadataBeforeAnyChanges.getChild(Edit.RootChild.INFO, Edit.NAMESPACE);
            boolean validate = false;
            boolean ufo = false;
                boolean index = true;
                metadataBeforeAnyChanges.removeChild(Edit.RootChild.INFO, Edit.NAMESPACE);
                updateMetadata(context, id, metadataBeforeAnyChanges, 
                    validate, ufo, index, 
                    context.getLanguage(), info.getChildText(Edit.Info.Elem.CHANGE_DATE), false);
                endEditingSession(id, session);
        } else {
            if(Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
              Log.debug(Geonet.EDITOR_SESSION, 
                  " > nothing to cancel for record " + id + 
                  ". Original record was null. Use starteditingsession to.");
            }
        }
    }

    /**
     * Remove the original record stored in session.
     * 
     * @param id
     * @param session
     */
    public void endEditingSession(String id, UserSession session) {
        if(Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
          Log.debug(Geonet.EDITOR_SESSION, "Editing session end.");
        }
        session.removeProperty(Geonet.Session.METADATA_BEFORE_ANY_CHANGES + id);
    }

    /**
     *
     * @param md
     * @return
     * @throws Exception
     */
    public Element enumerateTree(Element md) throws Exception {
        editLib.enumerateTree(md);
        return md;
    }

    /**
     * Validates metadata against XSD and schematron files related to metadata schema throwing XSDValidationErrorEx
     * if xsd errors or SchematronValidationErrorEx if schematron rules fails.
     *
     * @param schema
     * @param xml
     * @param context
     * @throws Exception
     */
    public static void validateMetadata(String schema, Element xml, ServiceContext context) throws Exception
    {
        validateMetadata(schema, xml, context, " ");
    }

    /**
     * Validates metadata against XSD and schematron files related to metadata schema throwing XSDValidationErrorEx
     * if xsd errors or SchematronValidationErrorEx if schematron rules fails.
     *
     * @param schema
     * @param xml
     * @param context
     * @param fileName
     * @throws Exception
     */
    public static void validateMetadata(String schema, Element xml, ServiceContext context, String fileName) throws Exception
    {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        DataManager dataMan = gc.getBean(DataManager.class);

        DataManager.setNamespacePrefix(xml);
        try {
            dataMan.validate(schema, xml);
        } catch (XSDValidationErrorEx e) {
            if (!fileName.equals(" ")) {
                throw new XSDValidationErrorEx(e.getMessage()+ "(in "+fileName+"): ",e.getObject());
            } else {
                throw new XSDValidationErrorEx(e.getMessage(),e.getObject());
            }
        }

        //--- Now do the schematron validation on this file - if there are errors
        //--- then we say what they are!
        //--- Note we have to use uuid here instead of id because we don't have
        //--- an id...

        Element schemaTronXml = dataMan.doSchemaTronForEditor(schema,xml,context.getLanguage());
        xml.detach();
        if (schemaTronXml != null && schemaTronXml.getContent().size() > 0) {
            Element schemaTronReport = dataMan.doSchemaTronForEditor(schema,xml,context.getLanguage());

            List<Namespace> theNSs = new ArrayList<Namespace>();
            theNSs.add(Namespace.getNamespace("geonet", "http://www.fao.org/geonetwork"));
            theNSs.add(Namespace.getNamespace("svrl", "http://purl.oclc.org/dsdl/svrl"));

            Element failedAssert = Xml.selectElement(schemaTronReport, "geonet:report/svrl:schematron-output/svrl:failed-assert", theNSs);

            Element failedSchematronVerification = Xml.selectElement(schemaTronReport, "geonet:report/geonet:schematronVerificationError", theNSs);

            if ((failedAssert != null) || (failedSchematronVerification != null)) {
                throw new SchematronValidationErrorEx("Schematron errors detected for file "+fileName+" - "
                        + Xml.getString(schemaTronReport) + " for more details",schemaTronReport);
            }
        }

    }

    /**
     * Creates XML schematron report for each set of rules defined in schema directory.
     * @param schema
     * @param md
     * @param lang
     * @param valTypeAndStatus
     * @return
     * @throws Exception
     */
    private Element getSchemaTronXmlReport(String schema, Element md, String lang, Map<String, Integer[]> valTypeAndStatus) throws Exception {
        // NOTE: this method assumes that you've run enumerateTree on the
        // metadata

        MetadataSchema metadataSchema = getSchema(schema);
        String[] rules = metadataSchema.getSchematronRules();

        // Schematron report is composed of one or more report(s)
        // for each set of rules.
        Element schemaTronXmlOut = new Element("schematronerrors",
                Edit.NAMESPACE);
        if (rules != null) {
            for (String rule : rules) {
                // -- create a report for current rules.
                // Identified by a rule attribute set to shematron file name
                if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, " - rule:" + rule);
                String ruleId = rule.substring(0, rule.indexOf(".xsl"));
                Element report = new Element("report", Edit.NAMESPACE);
                report.setAttribute("rule", ruleId,
                        Edit.NAMESPACE);

                java.nio.file.Path schemaTronXmlXslt = metadataSchema.getSchemaDir().resolve("schematron").resolve(rule);
                try {
                    Map<String,Object> params = new HashMap<String,Object>();
                    params.put("lang", lang);
                    params.put("rule", rule);
                    params.put("thesaurusDir", this.thesaurusDir);
                    Element xmlReport = Xml.transform(md, schemaTronXmlXslt, params);
                    if (xmlReport != null) {
                        report.addContent(xmlReport);
                        // add results to persitent validation information
                        int firedRules = 0;
                        Iterator<?> firedRulesElems = xmlReport.getDescendants(new ElementFilter ("fired-rule", Namespaces.SVRL));
                        while (firedRulesElems.hasNext()) {
                            firedRulesElems.next();
                            firedRules ++;
                        }
                        int invalidRules = 0;
                        Iterator<?> faileAssertElements = xmlReport.getDescendants(new ElementFilter ("failed-assert",
                                Namespaces.SVRL));
                        while (faileAssertElements.hasNext()) {
                            faileAssertElements.next();
                            invalidRules ++;
                        }
                        Integer[] results = {invalidRules!=0?0:1, firedRules, invalidRules};
                        if (valTypeAndStatus != null) {
                            valTypeAndStatus.put(ruleId, results);
                        }
                    }
                } catch (Exception e) {
                    Log.error(Geonet.DATA_MANAGER,"WARNING: schematron xslt "+schemaTronXmlXslt+" failed");

                    // If an error occurs that prevents to verify schematron rules, add to show in report
                    Element errorReport = new Element("schematronVerificationError", Edit.NAMESPACE);
                    errorReport.addContent("Schematron error ocurred, rules could not be verified: " + e.getMessage());
                    report.addContent(errorReport);

                    e.printStackTrace();
                }

                // -- append report to main XML report.
                schemaTronXmlOut.addContent(report);
            }
        }
        return schemaTronXmlOut;
    }

    /**
     * Valid the metadata record against its schema. For each error found, an xsderror attribute is added to
     * the corresponding element trying to find the element based on the xpath return by the ErrorHandler.
     *
     * @param schema
     * @param md
     * @return
     * @throws Exception
     */
    private synchronized Element getXSDXmlReport(String schema, Element md) {
        // NOTE: this method assumes that enumerateTree has NOT been run on the metadata
        ErrorHandler errorHandler = new ErrorHandler();
        errorHandler.setNs(Edit.NAMESPACE);
        Element xsdErrors;

        try {
            xsdErrors = validateInfo(schema,
                    md, errorHandler);
        }catch (Exception e) {
            xsdErrors = JeevesException.toElement(e);
            return xsdErrors;
        }

        if (xsdErrors != null) {
            MetadataSchema mds = getSchema(schema);
            List<Namespace> schemaNamespaces = mds.getSchemaNS();

            //-- now get each xpath and evaluate it
            //-- xsderrors/xsderror/{message,xpath}
            @SuppressWarnings("unchecked")
            List<Element> list = xsdErrors.getChildren();
            for (Element elError : list) {
                String xpath = elError.getChildText("xpath", Edit.NAMESPACE);
                String message = elError.getChildText("message", Edit.NAMESPACE);
                message = "\\n" + message;

                //-- get the element from the xpath and add the error message to it
                Element elem = null;
                try {
                    elem = Xml.selectElement(md, xpath, schemaNamespaces);
                } catch (JDOMException je) {
                    je.printStackTrace();
                    Log.error(Geonet.DATA_MANAGER,"Attach xsderror message to xpath "+xpath+" failed: "+je.getMessage());
                }
                if (elem != null) {
                    String existing = elem.getAttributeValue("xsderror",Edit.NAMESPACE);
                    if (existing != null) message = existing + message;
                    elem.setAttribute("xsderror",message,Edit.NAMESPACE);
                } else {
                    Log.warning(Geonet.DATA_MANAGER,"WARNING: evaluating XPath "+xpath+" against metadata failed - XSD validation message: "+message+" will NOT be shown by the editor");
                }
            }
        }
        return xsdErrors;
    }

    /**
     *
     * @return
     */
    public AccessManager getAccessManager() {
        return accessMan;
    }

    //--------------------------------------------------------------------------
    //---
    //--- General purpose API
    //---
    //--------------------------------------------------------------------------

    /**
     * Extract UUID from the metadata record using the schema
     * XSL for UUID extraction)
     *
     * @param schema
     * @param md
     * @return
     * @throws Exception
     */
    public String extractUUID(String schema, Element md) throws Exception {
        Path styleSheet = getSchemaDir(schema).resolve(Geonet.File.EXTRACT_UUID);
        String uuid       = Xml.transform(md, styleSheet).getText().trim();

        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Extracted UUID '"+ uuid +"' for schema '"+ schema +"'");

        //--- needed to detach md from the document
        md.detach();

        return uuid;
    }


    /**
     *
     * @param schema
     * @param md
     * @return
     * @throws Exception
     */
    public String extractDateModified(String schema, Element md) throws Exception {
        Path styleSheet = getSchemaDir(schema).resolve(Geonet.File.EXTRACT_DATE_MODIFIED);
        String dateMod    = Xml.transform(md, styleSheet).getText().trim();

        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Extracted Date Modified '"+ dateMod +"' for schema '"+ schema +"'");

        //--- needed to detach md from the document
        md.detach();

        return dateMod;
    }

    /**
     *
     * @param schema
     * @param uuid
     * @param md
     * @return
     * @throws Exception
     */
    public Element setUUID(String schema, String uuid, Element md) throws Exception {
        //--- setup environment

        Element env = new Element("env");
        env.addContent(new Element("uuid").setText(uuid));

        //--- setup root element

        Element root = new Element("root");
        root.addContent(md.detach());
        root.addContent(env.detach());

        //--- do an XSL  transformation

        Path styleSheet = getSchemaDir(schema).resolve(Geonet.File.SET_UUID);

        return Xml.transform(root, styleSheet);
    }

    /**
     *
     * @param md
     * @return
     * @throws Exception
     */
    public Element extractSummary(Element md) throws Exception {
        Path styleSheet = stylePath.resolve(Geonet.File.METADATA_BRIEF);
        Element summary       = Xml.transform(md, styleSheet);
        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Extracted summary '\n"+Xml.getString(summary));

        //--- needed to detach md from the document
        md.detach();

        return summary;
    }

    /**
     *
     * @param uuid
     * @return
     * @throws Exception
     */
    public @Nullable String getMetadataId(@Nonnull String uuid) throws Exception {
        final List<Integer> idList = _metadataRepository.findAllIdsBy(hasMetadataUuid(uuid));
        if (idList.isEmpty()) {
            return null;
        }
        return String.valueOf(idList.get(0));
    }

    /**
     *
     * @param id
     * @return
     * @throws Exception
     */
    public @Nullable String getMetadataUuid(@Nonnull String id) throws Exception {
        Metadata metadata = _metadataRepository.findOne(id);

        if (metadata == null)
            return null;

        return metadata.getUuid();
    }

    /**
     *
     * @param id
     * @return
     */
    public String getVersion(String id) {
        return editLib.getVersion(id);
    }

    /**
     *
     * @param id
     * @return
     */
    public String getNewVersion(String id){
        return editLib.getNewVersion(id);
    }

    /**
     * TODO javadoc.
     *
     * @param id
     * @param type
     * @param title
     * @throws Exception
     */
    public void setTemplate(final int id, final MetadataType type, final String title) throws Exception {
        setTemplateExt(id, type);
        indexMetadata(Integer.toString(id), true);
    }

    /**
     * TODO javadoc.
     *
     * @param id
     * @throws Exception
     */
    public void setTemplateExt(final int id, final MetadataType metadataType) throws Exception {
        _metadataRepository.update(id, new Updater<Metadata>() {
            @Override
            public void apply(@Nonnull Metadata metadata) {
                final MetadataDataInfo dataInfo = metadata.getDataInfo();
                dataInfo.setType(metadataType);
            }
        });
    }

    /**
     * TODO javadoc.
     *
     * @param id
     * @param harvestUuid
     * @throws Exception
     */
    public void setHarvested(int id, String harvestUuid) throws Exception {
        setHarvestedExt(id, harvestUuid);
        indexMetadata(Integer.toString(id), false);
    }

    /**
     * TODO javadoc.
     *
     * @param id
     * @param harvestUuid
     * @throws Exception
     */
    public void setHarvestedExt(int id, String harvestUuid) throws Exception {
        setHarvestedExt(id, harvestUuid, Optional.<String>absent());
    }

    /**
     * TODO javadoc.
     *
     * @param id
     * @param harvestUuid
     * @param harvestUri
     * @throws Exception
     */
    public void setHarvestedExt(final int id, final String harvestUuid, final Optional<String> harvestUri) throws Exception {
        _metadataRepository.update(id, new Updater<Metadata>() {
            @Override
            public void apply(Metadata metadata) {
                MetadataHarvestInfo harvestInfo = metadata.getHarvestInfo();
                harvestInfo.setUuid(harvestUuid);
                harvestInfo.setHarvested(harvestUuid != null);
                harvestInfo.setUri(harvestUri.orNull());
            }
        });
    }

    /**
     * Checks autodetect elements in installed schemas to determine whether the metadata record belongs to that schema.
     * Use this method when you want the default schema from the geonetwork config to be returned when no other match
     * can be found.
     *
     * @param md Record to checked against schemas
     * @throws SchemaMatchConflictException
     * @throws NoSchemaMatchesException
     * @return
     */
    public @CheckForNull String autodetectSchema(Element md) throws SchemaMatchConflictException, NoSchemaMatchesException {
        return autodetectSchema(md, schemaMan.getDefaultSchema());
    }

    /**
     * Checks autodetect elements in installed schemas to determine whether the metadata record belongs to that schema.
     * Use this method when you want to set the default schema to be returned when no other match can be found.
     *
     * @param md Record to checked against schemas
     * @param defaultSchema Schema to be assigned when no other schema matches
     * @throws SchemaMatchConflictException
     * @throws NoSchemaMatchesException
     * @return
     */
    public @CheckForNull String autodetectSchema(Element md, String defaultSchema) throws SchemaMatchConflictException, NoSchemaMatchesException {

        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Autodetect schema for metadata with :\n * root element:'" + md.getQualifiedName()
                    + "'\n * with namespace:'" + md.getNamespace()
                    + "\n * with additional namespaces:" + md.getAdditionalNamespaces().toString());
        String schema =  schemaMan.autodetectSchema(md, defaultSchema);
        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Schema detected was "+schema);
        return schema;
    }

    /**
     *
     * @param id
     * @param displayOrder
     * @throws Exception
     */
    public void updateDisplayOrder(final String id, final String displayOrder) throws Exception {
        _metadataRepository.update(Integer.valueOf(id), new Updater<Metadata>() {
            @Override
            public void apply(Metadata entity) {
                entity.getDataInfo().setDisplayOrder(Integer.parseInt(displayOrder));
            }
        });
    }

    /**
     *
     * @param srvContext
     * @param id
     * @throws Exception hmm
     */
    public void increasePopularity(ServiceContext srvContext, String id) throws Exception {
        // READONLYMODE
        if (!srvContext.getBean(NodeInfo.class).isReadOnly()) {
            // Update the popularity in database
            Integer iId = Integer.valueOf(id);
            _metadataRepository.update(iId, new Updater<Metadata>() {
                @Override
                public void apply(@Nonnull Metadata entity) {
                    final MetadataDataInfo dataInfo = entity.getDataInfo();
                    int popularity = dataInfo.getPopularity();
                    dataInfo.setPopularity(popularity + 1);
                }
            });

            // And register the metadata to be indexed in the near future
            final IndexingList list = srvContext.getBean(IndexingList.class);
            list.add(iId);
        } else {
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "GeoNetwork is operating in read-only mode. IncreasePopularity is skipped.");
            }
        }
    }

    /**
     * Rates a metadata.
     *
     * @param metadataId
     * @param ipAddress ipAddress IP address of the submitting client
     * @param rating range should be 1..5
     * @return
     * @throws Exception hmm
     */
    public int rateMetadata(final int metadataId, final String ipAddress, final int rating) throws Exception {
        MetadataRatingByIp ratingEntity = new MetadataRatingByIp();
        ratingEntity.setRating(rating);
        ratingEntity.setId(new MetadataRatingByIpId(metadataId, ipAddress));

        final MetadataRatingByIpRepository ratingByIpRepository = _applicationContext.getBean(MetadataRatingByIpRepository.class);
        ratingByIpRepository.save(ratingEntity);

        //
        // calculate new rating
        //
        final int newRating = ratingByIpRepository.averageRating(metadataId);

        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Setting rating for id:"+ metadataId +" --> rating is:"+newRating);


        _metadataRepository.update(metadataId, new Updater<Metadata>() {
            @Override
            public void apply(Metadata entity) {
                entity.getDataInfo().setRating(newRating);
            }
        });

        indexMetadata(Integer.toString(metadataId), false);

        return rating;
    }

    //--------------------------------------------------------------------------
    //---
    //--- Metadata Insert API
    //---
    //--------------------------------------------------------------------------

    /**
     * Creates a new metadata duplicating an existing template.
     *
     * @param context
     * @param templateId
     * @param groupOwner
     * @param source
     * @param owner
     * @param parentUuid
     * @param isTemplate TODO
     * @param fullRightsForGroup TODO
     * @return
     * @throws Exception
     */
    public String createMetadata(ServiceContext context, String templateId, String groupOwner,
                                 String source, int owner,
                                 String parentUuid, String isTemplate, boolean fullRightsForGroup) throws Exception {
        Metadata templateMetadata = _metadataRepository.findOne(templateId);
        if (templateMetadata == null) {
            throw new IllegalArgumentException("Template id not found : " + templateId);
        }

        String schema = templateMetadata.getDataInfo().getSchemaId();
        String data   = templateMetadata.getData();
        String uuid   = UUID.randomUUID().toString();
        Element xml = Xml.loadString(data, false);
        if (templateMetadata.getDataInfo().getType() == MetadataType.METADATA) {
            xml = updateFixedInfo(schema, Optional.<Integer>absent(), uuid, xml, parentUuid, UpdateDatestamp.NO, context);
        }
        final Metadata newMetadata = new Metadata().setUuid(uuid);
        newMetadata.getDataInfo()
                .setChangeDate(new ISODate())
                .setCreateDate(new ISODate())
                .setSchemaId(schema)
                .setType(MetadataType.lookup(isTemplate));
        newMetadata.getSourceInfo()
                .setGroupOwner(Integer.valueOf(groupOwner))
                .setOwner(owner)
                .setSourceId(source);

        Collection<MetadataCategory> filteredCategories = Collections2.filter(templateMetadata.getCategories(),
                new Predicate<MetadataCategory>() {
                    @Override
                    public boolean apply(@Nullable MetadataCategory input) {
                        return input != null;
                    }
                });

        newMetadata.getCategories().addAll(filteredCategories);

        int finalId = insertMetadata(context, newMetadata, xml, false, true, true, UpdateDatestamp.YES,
                fullRightsForGroup, true).getId();

        return String.valueOf(finalId);
    }

    /**
     * Inserts a metadata into the database, optionally indexing it, and optionally applying automatic changes to it (update-fixed-info).
     *
     *
     *
     * @param context the context describing the user and service
     * @param schema XSD this metadata conforms to
     * @param metadataXml the metadata to store
     * @param uuid unique id for this metadata
     * @param owner user who owns this metadata
     * @param groupOwner group this metadata belongs to
     * @param source id of the origin of this metadata (harvesting source, etc.)
     * @param metadataType whether this metadata is a template
     * @param docType ?!
     * @param category category of this metadata
     * @param createDate date of creation
     * @param changeDate date of modification
     * @param ufo whether to apply automatic changes
     * @param index whether to index this metadata
     * @return id, as a string
     * @throws Exception hmm
     */
    public String insertMetadata(ServiceContext context, String schema, Element metadataXml, String uuid, int owner, String groupOwner, String source,
                                 String metadataType, String docType, String category, String createDate, String changeDate, boolean ufo, boolean index) throws Exception {

        boolean notifyChange = true;

        if (source == null) {
            source = settingMan.getSiteId();
        }

        if(StringUtils.isBlank(metadataType)) {
            metadataType = MetadataType.METADATA.codeString;
        }
        final Metadata newMetadata = new Metadata().setUuid(uuid);
        final ISODate isoChangeDate = changeDate != null ? new ISODate(changeDate) : new ISODate();
        final ISODate isoCreateDate = createDate != null ? new ISODate(createDate) : new ISODate();
        newMetadata.getDataInfo()
                .setChangeDate(isoChangeDate)
                .setCreateDate(isoCreateDate)
                .setSchemaId(schema)
                .setDoctype(docType)
                .setRoot(metadataXml.getQualifiedName())
                .setType(MetadataType.lookup(metadataType));
        newMetadata.getSourceInfo()
                .setOwner(owner)
                .setSourceId(source);
        if (groupOwner != null) {
            newMetadata.getSourceInfo().setGroupOwner(Integer.valueOf(groupOwner));
        }
        if (category != null) {
            MetadataCategory metadataCategory = _applicationContext.getBean(MetadataCategoryRepository.class).findOneByName(category);
            if (metadataCategory == null) {
                throw new IllegalArgumentException("No category found with name: "+category);
            }
            newMetadata.getCategories().add(metadataCategory);
        }

        boolean fullRightsForGroup = false;

        int finalId = insertMetadata(context, newMetadata, metadataXml, notifyChange, index, ufo, UpdateDatestamp.NO,
                fullRightsForGroup, false).getId();

        return String.valueOf(finalId);
    }

    public Metadata insertMetadata(ServiceContext context, Metadata newMetadata, Element metadataXml, boolean notifyChange,
                                    boolean index, boolean updateFixedInfo, UpdateDatestamp updateDatestamp,
                                    boolean fullRightsForGroup, boolean forceRefreshReaders) throws Exception {
        final String schema = newMetadata.getDataInfo().getSchemaId();

        //--- force namespace prefix for iso19139 metadata
        setNamespacePrefixUsingSchemas(schema, metadataXml);


        if (updateFixedInfo && newMetadata.getDataInfo().getType() == MetadataType.METADATA) {
            String parentUuid = null;
            metadataXml = updateFixedInfo(schema, Optional.<Integer>absent(), newMetadata.getUuid(), metadataXml, parentUuid, updateDatestamp, context);
        }

        //--- store metadata
        final Metadata savedMetadata = xmlSerializer.insert(newMetadata, metadataXml, context);

        final String stringId = String.valueOf(savedMetadata.getId());
        String groupId = null;
        final Integer groupIdI = newMetadata.getSourceInfo().getGroupOwner();
        if (groupIdI != null) {
            groupId = String.valueOf(groupIdI);
        }
        copyDefaultPrivForGroup(context, stringId, groupId, fullRightsForGroup);

        if (index) {
            indexMetadata(stringId, forceRefreshReaders);
        }

        if (notifyChange) {
            // Notifies the metadata change to metatada notifier service
            notifyMetadataChange(metadataXml, stringId);
        }
        return savedMetadata;
    }

    //--------------------------------------------------------------------------
    //---
    //--- Metadata Get API
    //---
    //--------------------------------------------------------------------------

    /**
     * Retrieves a metadata (in xml) given its id with no geonet:info.
     * @param srvContext
     * @param id
     * @return
     * @throws Exception
     */
    public Element getMetadataNoInfo(ServiceContext srvContext, String id) throws Exception {
        Element md = getMetadata(srvContext, id, false, false, false);
        md.removeChild(Edit.RootChild.INFO, Edit.NAMESPACE);
        return md;
    }

    /**
     * Retrieves a metadata (in xml) given its id. Use this method when you must retrieve a metadata in the same
     * transaction.
     * @param id
     * @return
     * @throws Exception
     */
    public Element getMetadata(String id) throws Exception {
        Element md = xmlSerializer.selectNoXLinkResolver(id, false);
        if (md == null) return null;
        md.detach();
        return md;
    }

    /**
     * Retrieves a metadata (in xml) given its id; adds editing information if requested and validation errors if
     * requested.
     *
     * @param srvContext
     * @param id
     * @param forEditing        Add extra element to build metadocument {@link EditLib#expandElements(String, Element)}
     * @param withEditorValidationErrors
     * @param keepXlinkAttributes When XLinks are resolved in non edit mode, do not remove XLink attributes.
     * @return
     * @throws Exception
     */
    public Element getMetadata(ServiceContext srvContext, String id, boolean forEditing, boolean withEditorValidationErrors, boolean keepXlinkAttributes) throws Exception {
        boolean doXLinks = xmlSerializer.resolveXLinks();
        Element metadataXml = xmlSerializer.selectNoXLinkResolver(id, false);
        if (metadataXml == null) return null;

        String version = null;

        if (forEditing) { // copy in xlink'd fragments but leave xlink atts to editor
            if (doXLinks) Processor.processXLink(metadataXml, srvContext);
            String schema = getMetadataSchema(id);

            if (withEditorValidationErrors) {
                version = doValidate(srvContext.getUserSession(), schema, id, metadataXml, srvContext.getLanguage(), forEditing).two();
            } else {
                editLib.expandElements(schema, metadataXml);
                version = editLib.getVersionForEditing(schema, id, metadataXml);
            }
        } else {
            if (doXLinks) {
                if (keepXlinkAttributes) {
                    Processor.processXLink(metadataXml, srvContext);
                } else {
                    Processor.detachXLink(metadataXml, srvContext);
                }
            }
        }

        metadataXml.addNamespaceDeclaration(Edit.NAMESPACE);
        Element info = buildInfoElem(srvContext, id, version);
        metadataXml.addContent(info);

        metadataXml.detach();
        return metadataXml;
    }

    /**
     * Retrieves a metadata element given it's ref.
     *
     * @param md
     * @param ref
     * @return
     */
    public Element getElementByRef(Element md, String ref) {
        return editLib.findElement(md, ref);
    }

    /**
     * Returns true if the metadata exists in the database.
     * @param id
     * @return
     * @throws Exception
     */
    public boolean existsMetadata(int id) throws Exception {
        return _metadataRepository.exists(id);
    }

    /**
     * Returns true if the metadata uuid exists in the database.
     * @param uuid
     * @return
     * @throws Exception
     */
    public boolean existsMetadataUuid(String uuid) throws Exception {
        return !_metadataRepository.findAllIdsBy(hasMetadataUuid(uuid)).isEmpty();
    }

    /**
     * Returns all the keywords in the system.
     *
     * @return
     * @throws Exception
     */
    public Element getKeywords() throws Exception {
        Collection<String> keywords = searchMan.getTerms("keyword");
        Element el = new Element("keywords");

        for (Object keyword : keywords) {
            el.addContent(new Element("keyword").setText((String) keyword));
        }
        return el;
    }

    //--------------------------------------------------------------------------
    //---
    //--- Metadata Update API
    //---
    //--------------------------------------------------------------------------

    /**
     *  For update of owner info.
     *
     * @param id
     * @param owner
     * @param groupOwner
     * @throws Exception
     */
    public synchronized void updateMetadataOwner(final int id, final String owner, final String groupOwner) throws Exception {
        _metadataRepository.update(id, new Updater<Metadata>() {
            @Override
            public void apply(@Nonnull Metadata entity) {
                entity.getSourceInfo().setGroupOwner(Integer.valueOf(groupOwner));
                entity.getSourceInfo().setOwner(Integer.valueOf(owner));
            }
        });
    }

    /**
     * Updates a metadata record. Deletes validation report currently in session (if any). If user asks for validation
     * the validation report will be (re-)created then.
     *
     * @param context
     * @param metadataId
     * @param validate
     * @param lang
     * @param changeDate
     * @param updateDateStamp
     *
     * @return metadata if the that was updated
     * @throws Exception
     */
    public synchronized Metadata updateMetadata(final ServiceContext context, final String metadataId, final Element md,
                                               final boolean validate, final boolean ufo, final boolean index, final String lang,
                                               final String changeDate, final boolean updateDateStamp) throws Exception {
        Element metadataXml = md;

        // when invoked from harvesters, session is null?
        UserSession session = context.getUserSession();
        if(session != null) {
            session.removeProperty(Geonet.Session.VALIDATION_REPORT + metadataId);
        }
        String schema = getMetadataSchema(metadataId);
        if(ufo) {
            String parentUuid = null;
            Integer intId = Integer.valueOf(metadataId);
            metadataXml = updateFixedInfo(schema, Optional.of(intId), null, metadataXml, parentUuid, (updateDateStamp ? UpdateDatestamp.YES : UpdateDatestamp.NO), context);
        }

        //--- force namespace prefix for iso19139 metadata
        setNamespacePrefixUsingSchemas(schema, metadataXml);

        // Notifies the metadata change to metatada notifier service
        final Metadata metadata = _metadataRepository.findOne(metadataId);

        String uuid = null;
        if (schemaMan.getSchema(schema).isReadwriteUUID()
                && metadata.getDataInfo().getType() != MetadataType.SUB_TEMPLATE) {
            uuid = extractUUID(schema, metadataXml);
        }

        //--- write metadata to dbms
        xmlSerializer.update(metadataId, metadataXml, changeDate, updateDateStamp, uuid, context);
        if (metadata.getDataInfo().getType() == MetadataType.METADATA) {
            // Notifies the metadata change to metatada notifier service
            notifyMetadataChange(metadataXml, metadataId);
        }

        try {
            //--- do the validation last - it throws exceptions
            if (session != null && validate) {
                doValidate(session, schema,metadataId,metadataXml,lang, false);
            }
        } finally {
            if(index) {
                //--- update search criteria
                indexMetadata(metadataId, false);
            }
        }
        // Return an up to date metadata record
        return _metadataRepository.findOne(metadataId);
    }

    /**
     * Validates an xml document, using autodetectschema to determine how.
     *
     * @param xml
     * @return true if metadata is valid
     */
    public boolean validate(Element xml) {
        try {
            String schema = autodetectSchema(xml);
            validate(schema, xml);
            return true;
        }
        // XSD validation error(s)
        catch (Exception x) {
            // do not print stacktrace as this is 'normal' program flow
            if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "invalid metadata: " + x.getMessage(), x);
            return false;
        }
    }

    /**
     * Used by harvesters that need to validate metadata.
     *
     * @param schema name of the schema to validate against
     * @param metadataId metadata id - used to record validation status
     * @param doc metadata document as JDOM Document not JDOM Element
     * @param lang Language from context
     * @return
     */
    public boolean doValidate(String schema, String metadataId, Document doc, String lang) {
        Integer intMetadataId = Integer.valueOf(metadataId);
        List<MetadataValidation> validations = new ArrayList<>();
        boolean valid = true;

        if (doc.getDocType() != null) {
            if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "Validating against dtd " + doc.getDocType());

            // if document has a doctype then validate using that (assuming that the
            // dtd is either mapped locally or will be cached after first validate)
            try {
                Xml.validate(doc);
                validations.add(new MetadataValidation().
                        setId(new MetadataValidationId(intMetadataId, "dtd")).
                        setStatus(MetadataValidationStatus.VALID).
                        setRequired(true).
                        setNumTests(1).
                        setNumFailures(0));
                if(Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                    Log.debug(Geonet.DATA_MANAGER, "Valid.");
                }
            } catch (Exception e) {
                validations.add(new MetadataValidation().
                        setId(new MetadataValidationId(intMetadataId, "dtd")).
                        setStatus(MetadataValidationStatus.INVALID).
                        setRequired(true).
                        setNumTests(1).
                        setNumFailures(1));

                if(Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                    Log.debug(Geonet.DATA_MANAGER, "Invalid.", e);
                }
                valid = false;
            }
        } else {
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "Validating against XSD " + schema);
            }
            // do XSD validation
            Element md = doc.getRootElement();
            Element xsdErrors = getXSDXmlReport(schema, md);

            int xsdErrorCount = 0;
            if (xsdErrors != null && xsdErrors.getContent().size() > 0) {
                xsdErrorCount = xsdErrors.getContent().size();
            }
            if (xsdErrorCount > 0) {
                validations.add(new MetadataValidation().
                        setId(new MetadataValidationId(intMetadataId, "xsd")).
                        setStatus(MetadataValidationStatus.INVALID).
                        setRequired(true).
                        setNumTests(xsdErrorCount).
                        setNumFailures(xsdErrorCount));
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, "Invalid.");
                valid = false;
            } else {
                validations.add(new MetadataValidation().
                        setId(new MetadataValidationId(intMetadataId, "xsd")).
                        setStatus(MetadataValidationStatus.VALID).
                        setRequired(true).
                        setNumTests(1).
                        setNumFailures(0));
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, "Valid.");
            }
            try {
                editLib.enumerateTree(md);
                //Apply custom schematron rules
                Element errors = applyCustomSchematronRules(schema, Integer.parseInt(metadataId), doc.getRootElement(), lang, validations);
                valid = valid && errors == null;
                editLib.removeEditingInfo(md);
            } catch (Exception e) {
                e.printStackTrace();
                Log.error(Geonet.DATA_MANAGER, "Could not run schematron validation on metadata " + metadataId + ": " + e.getMessage());
                valid = false;
            }
        }

        // now save the validation status
        try {
            saveValidationStatus(intMetadataId, validations);
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(Geonet.DATA_MANAGER, "Could not save validation status on metadata "+metadataId+": "+e.getMessage());
        }

        return valid;
    }

    /**
     * Used by the validate embedded service. The validation report is stored in the session.
     *
     * @param session
     * @param schema
     * @param metadataId
     * @param md
     * @param lang
     * @param forEditing TODO
     * @return
     * @throws Exception
     */
    public Pair <Element, String> doValidate(UserSession session, String schema, String metadataId, Element md, String lang, boolean forEditing) throws Exception {
        int intMetadataId = Integer.parseInt(metadataId);
        String version = null;
        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Creating validation report for record #" + metadataId + " [schema: " + schema + "].");

        Element sessionReport = (Element)session.getProperty(Geonet.Session.VALIDATION_REPORT + metadataId);
        if (sessionReport != null && !forEditing) {
            if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "  Validation report available in session.");
            sessionReport.detach();
            return Pair.read(sessionReport, version);
        }

        List<MetadataValidation> validations = new ArrayList<>();
        Element errorReport = new Element("report", Edit.NAMESPACE);
        errorReport.setAttribute("id", metadataId, Edit.NAMESPACE);

        //-- get an XSD validation report and add results to the metadata
        //-- as geonet:xsderror attributes on the affected elements
        Element xsdErrors = getXSDXmlReport(schema, md);
        int xsdErrorCount = 0;
        if (xsdErrors != null) {
            xsdErrorCount = xsdErrors.getContent().size();
        }
        if (xsdErrorCount > 0) {
            errorReport.addContent(xsdErrors);
            validations.add(new MetadataValidation().
                    setId(new MetadataValidationId(intMetadataId, "xsd")).
                    setStatus(MetadataValidationStatus.INVALID).
                    setRequired(true).
                    setNumTests(xsdErrorCount).
                    setNumFailures(xsdErrorCount));

            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "  - XSD error: " + Xml.getString(xsdErrors));
            }
        } else {
            validations.add(new MetadataValidation().
                    setId(new MetadataValidationId(intMetadataId, "xsd")).
                    setStatus(MetadataValidationStatus.VALID).
                    setRequired(true).
                    setNumTests(1).
                    setNumFailures(0));

            if (Log.isTraceEnabled(Geonet.DATA_MANAGER)) {
                Log.trace(Geonet.DATA_MANAGER, "Valid.");
            }
        }

        // ...then schematrons
        // edit mode
        Element error = null;
        if (forEditing) {
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "  - Schematron in editing mode.");
            //-- now expand the elements and add the geonet: elements
            editLib.expandElements(schema, md);
            version = editLib.getVersionForEditing(schema, metadataId, md);

            //Apply custom schematron rules
            error = applyCustomSchematronRules(schema, Integer.parseInt(metadataId), md, lang, validations);
        } else {
            try {
                // enumerate the metadata xml so that we can report any problems found
                // by the schematron_xml script to the geonetwork editor
                editLib.enumerateTree(md);

                //Apply custom schematron rules
                error = applyCustomSchematronRules(schema, Integer.parseInt(metadataId), md, lang, validations);

                // remove editing info added by enumerateTree
                editLib.removeEditingInfo(md);

            } catch (Exception e) {
                e.printStackTrace();
                Log.error(Geonet.DATA_MANAGER, "Could not run schematron validation on metadata " + metadataId + ": " + e.getMessage());
            }
        }

        if (error != null) {
            errorReport.addContent(error);
        }

        // Save report in session (invalidate by next update) and db
        try {
            saveValidationStatus(intMetadataId, validations);
        } catch (Exception e) {
            Log.error(Geonet.DATA_MANAGER, "Could not save validation status on metadata " + metadataId + ": " + e.getMessage(), e);
        }

        return Pair.read(errorReport, version);
    }


    /**
     *
     * Creates XML schematron report for each set of rules defined in schema
     * directory. This method assumes that you've run enumerateTree on the
     * metadata
     *
     * Returns null if no error on validation.
     *
     *
     * @param schema
     * @param metadataId
     * @param md
     * @param lang
     * @param validations
     *
     * @return errors
     */
    public Element applyCustomSchematronRules(String schema, int metadataId, Element md,
                                              String lang, List<MetadataValidation> validations) {
        MetadataSchema metadataSchema = getSchema(schema);
        final Path schemaDir = this.schemaMan.getSchemaDir(schema);

        Element schemaTronXmlOut = new Element("schematronerrors", Edit.NAMESPACE);
        try {
            final SchematronRepository schematronRepository = _applicationContext.getBean(SchematronRepository.class);
            final SchematronCriteriaGroupRepository criteriaGroupRepository = _applicationContext.getBean(SchematronCriteriaGroupRepository.class);

            final List<Schematron> schematronList = schematronRepository.findAllBySchemaName(metadataSchema.getName());

            //Loop through all xsl files
            for (Schematron schematron : schematronList) {

                int id = schematron.getId();
                //it contains absolute path to the xsl file
                String rule = schematron.getRuleName();
                String dbident = ""+id;

                List<SchematronCriteriaGroup> criteriaGroups = criteriaGroupRepository.findAllById_SchematronId(schematron.getId());

                //Loop through all criteria to see if apply schematron
                //if any criteria does not apply, do not apply at all (AND)
                SchematronRequirement requirement = SchematronRequirement.DISABLED;
                for (SchematronCriteriaGroup criteriaGroup : criteriaGroups) {
                    List<SchematronCriteria> criterias = criteriaGroup.getCriteria();
                    boolean apply = false;
                    for(SchematronCriteria criteria : criterias) {
                        boolean tmpApply = criteria.accepts(_applicationContext, metadataId, md, metadataSchema.getSchemaNS());

                        if(!tmpApply) {
                            apply = false;
                            break;
                        } else {
                            apply = true;
                        }

                    }

                    if (apply) {
                        if(Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                            Log.debug(Geonet.DATA_MANAGER, " - Schematron group is accepted:" + criteriaGroup.getId().getName() + " for schematron: "+schematron.getRuleName());
                        }
                        requirement = requirement.highestRequirement(criteriaGroup.getRequirement());
                    } else {
                        requirement = SchematronRequirement.DISABLED;
                    }
                }

                if(requirement != SchematronRequirement.DISABLED) {
                    if(Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                        Log.debug(Geonet.DATA_MANAGER, " - rule:" + rule);
                    }

                    String ruleId = schematron.getRuleName();

                    Element report = new Element("report", Edit.NAMESPACE);
                    report.setAttribute("rule", ruleId,
                            Edit.NAMESPACE);
                    report.setAttribute("displayPriority", ""+schematron.getDisplayPriority(),
                            Edit.NAMESPACE);
                    report.setAttribute("dbident", dbident,
                            Edit.NAMESPACE);
                    report.setAttribute("required", requirement.toString(), Edit.NAMESPACE);

                    try {
                        Map<String,Object> params = new HashMap<String,Object>();
                        params.put("lang", lang);
                        params.put("rule", ruleId);
                        params.put("thesaurusDir", this.thesaurusDir);

                        Path file = schemaDir.resolve(SCHEMATRON_DIR).resolve(schematron.getFile());
                        Element xmlReport = Xml.transform(md, file, params);
                        if (xmlReport != null) {
                            report.addContent(xmlReport);
                            // add results to persistent validation information
                            int firedRules = 0;
                            @SuppressWarnings("unchecked")
                            Iterator<Element> i = xmlReport.getDescendants(new ElementFilter ("fired-rule", Namespaces.SVRL));
                            while (i.hasNext()) {
                                i.next();
                                firedRules ++;
                            }
                            int invalidRules = 0;
                            i = xmlReport.getDescendants(new ElementFilter ("failed-assert", Namespaces.SVRL));
                            while (i.hasNext()) {
                                i.next();
                                invalidRules ++;
                            }

                            if (validations != null) {
                                validations.add(new MetadataValidation().
                                        setId(new MetadataValidationId(metadataId, ruleId)).
                                        setStatus(invalidRules!=0 ? MetadataValidationStatus.INVALID : MetadataValidationStatus.VALID).
                                        setRequired(requirement == SchematronRequirement.REQUIRED).
                                        setNumTests(firedRules).
                                        setNumFailures(invalidRules));

                            }
                        }
                    } catch (Exception e) {
                        Log.error(Geonet.DATA_MANAGER,"WARNING: schematron xslt "+rule+" failed");

                        // If an error occurs that prevents to verify schematron rules, add to show in report
                        Element errorReport = new Element("schematronVerificationError", Edit.NAMESPACE);
                        errorReport.addContent("Schematron error ocurred, rules could not be verified: " + e.getMessage());
                        report.addContent(errorReport);

                        e.printStackTrace();
                    }

                    // -- append report to main XML report.
                    schemaTronXmlOut.addContent(report);


                }

            }
        } catch (Throwable e) {
            Element errorReport = new Element("schematronVerificationError", Edit.NAMESPACE);
            errorReport.addContent("Schematron error ocurred, rules could not be verified: " + e.getMessage());
            schemaTronXmlOut.addContent(errorReport);
        }

        if(schemaTronXmlOut.getChildren().isEmpty()) {
            schemaTronXmlOut = null;
        }

        return schemaTronXmlOut;
    }

    /**
     * Saves validation status information into the database for the current record.
     *
     * @param id   the metadata record internal identifier
     * @param validations  the validation reports for each type of validation and schematron validation
     */
    private void saveValidationStatus(int id, List<MetadataValidation> validations) throws Exception {
        final MetadataValidationRepository validationRepository = _applicationContext.getBean(MetadataValidationRepository.class);
        validationRepository.deleteAllById_MetadataId(id);
        validationRepository.save(validations);
    }


    //--------------------------------------------------------------------------
    //---
    //--- Metadata Delete API
    //---
    //--------------------------------------------------------------------------

    /**
     * TODO Javadoc.
     *
     * @param context
     * @param id
     * @throws Exception
     */
    private void deleteMetadataFromDB(ServiceContext context, String id) throws Exception {
        //--- remove operations
        deleteMetadataOper(context, id, false);

        int intId = Integer.valueOf(id);
        _applicationContext.getBean(MetadataRatingByIpRepository.class).deleteAllById_MetadataId(intId);
        _applicationContext.getBean(MetadataValidationRepository.class).deleteAllById_MetadataId(intId);
        _applicationContext.getBean(MetadataStatusRepository.class).deleteAllById_MetadataId(intId);

        // Logical delete for metadata file uploads
        PathSpec<MetadataFileUpload, String> deletedDatePathSpec = new PathSpec<MetadataFileUpload, String>() {
            @Override
            public javax.persistence.criteria.Path<String> getPath(Root<MetadataFileUpload> root) {
                return root.get(MetadataFileUpload_.deletedDate);
            }
        };

        _applicationContext.getBean(MetadataFileUploadRepository.class).createBatchUpdateQuery(deletedDatePathSpec,
                new ISODate().toString(),
                MetadataFileUploadSpecs.isNotDeletedForMetadata(intId));

        //--- remove metadata
        xmlSerializer.delete(id, context);
    }

    /**
     * Removes a metadata.
     *
     * @param context
     * @param metadataId
     * @throws Exception
     */
    public synchronized void deleteMetadata(ServiceContext context, String metadataId) throws Exception {
        String uuid = getMetadataUuid(metadataId);
        boolean isMetadata = _metadataRepository.findOne(metadataId).getDataInfo().getType() == MetadataType.METADATA;

        deleteMetadataFromDB(context, metadataId);

        // Notifies the metadata change to metatada notifier service
        if (isMetadata) {
            context.getBean(MetadataNotifierManager.class).deleteMetadata(metadataId, uuid, context);
        }

        //--- update search criteria
        searchMan.delete("_id", metadataId + "");
//        _entityManager.flush();
//        _entityManager.clear();
    }

    /**
     *
     * @param context
     * @param metadataId
     * @throws Exception
     */
    public synchronized void deleteMetadataGroup(ServiceContext context, String metadataId) throws Exception {
        deleteMetadataFromDB(context, metadataId);
        //--- update search criteria
        searchMan.deleteGroup("_id", metadataId + "");
    }

    /**
     * Removes all operations stored for a metadata.
     * @param metadataId
     * @param skipAllIntranet
     * @throws Exception
     */
    public void deleteMetadataOper(ServiceContext context, String metadataId, boolean skipAllIntranet) throws Exception {
        OperationAllowedRepository operationAllowedRepository = context.getBean(OperationAllowedRepository.class);
        
        if (skipAllIntranet) {
            operationAllowedRepository.deleteAllByMetadataIdExceptGroupId(Integer.valueOf(metadataId), ReservedGroup.intranet.getId());
        } else {
            operationAllowedRepository.deleteAllByIdAttribute(OperationAllowedId_.metadataId, Integer.valueOf(metadataId));
        }
    }

    //--------------------------------------------------------------------------
    //---
    //--- Metadata thumbnail API
    //---
    //--------------------------------------------------------------------------

    /**
     *
     * @param metadataId
     * @return
     * @throws Exception
     */
    public Element getThumbnails(ServiceContext context, String metadataId) throws Exception {
        Element md = xmlSerializer.select(context, metadataId);

        if (md == null)
            return null;

        md.detach();

        String schema = getMetadataSchema(metadataId);

        //--- do an XSL  transformation
        Path styleSheet = getSchemaDir(schema).resolve(Geonet.File.EXTRACT_THUMBNAILS);

        Element result = Xml.transform(md, styleSheet);
        result.addContent(new Element("id").setText(metadataId));

        return result;
    }

    /**
     *
     * @param context
     * @param id
     * @param small
     * @param file
     * @throws Exception
     */
    public void setThumbnail(ServiceContext context, String id, boolean small, String file, boolean indexAfterChange) throws Exception {
        int    pos = file.lastIndexOf('.');
        String ext = (pos == -1) ? "???" : file.substring(pos +1);

        Element env = new Element("env");
        env.addContent(new Element("file").setText(file));
        env.addContent(new Element("ext").setText(ext));

        String host    = settingMan.getValue(Geonet.Settings.SERVER_HOST);
        String port    = settingMan.getValue(Geonet.Settings.SERVER_PORT);
        String baseUrl = context.getBaseUrl();

        env.addContent(new Element("host").setText(host));
        env.addContent(new Element("port").setText(port));
        env.addContent(new Element("baseUrl").setText(baseUrl));
        // TODO: Remove host, port, baseUrl and simplify the
        // URL created in the XSLT. Keeping it for the time
        // as many profiles depend on it.
        env.addContent(new Element("url").setText(settingMan.getSiteURL(context)));

        manageThumbnail(context, id, small, env, Geonet.File.SET_THUMBNAIL, indexAfterChange);
    }

    /**
     *
     * @param context
     * @param id
     * @param small
     * @throws Exception
     */
    public void unsetThumbnail(ServiceContext context, String id, boolean small, boolean indexAfterChange) throws Exception {
        Element env = new Element("env");

        manageThumbnail(context, id, small, env, Geonet.File.UNSET_THUMBNAIL, indexAfterChange);
    }

    /**
     *
     * @param context
     * @param id
     * @param small
     * @param env
     * @param styleSheet
     * @param indexAfterChange
     * @throws Exception
     */
    private void manageThumbnail(ServiceContext context, String id, boolean small, Element env,
                                 String styleSheet, boolean indexAfterChange) throws Exception {
        boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = true;
        Element md = getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);

        if (md == null)
            return;

        md.detach();

        String schema = getMetadataSchema(id);

        //--- setup environment
        String type = small ? "thumbnail" : "large_thumbnail";
        env.addContent(new Element("type").setText(type));
        transformMd(context, id, md, env, schema, styleSheet, indexAfterChange);
    }

    /**
     *
     * @param context
     * @param metadataId
     * @param md
     * @param env
     * @param schema
     * @param styleSheet
     * @param indexAfterChange
     * @throws Exception
     */
    private void transformMd(ServiceContext context, String metadataId, Element md, Element env, String schema, String styleSheet, boolean indexAfterChange) throws Exception {

        if(env.getChild("host")==null){
            String host    = settingMan.getValue(Geonet.Settings.SERVER_HOST);
            String port    = settingMan.getValue(Geonet.Settings.SERVER_PORT);

            env.addContent(new Element("host").setText(host));
            env.addContent(new Element("port").setText(port));
        }

        //--- setup root element
        Element root = new Element("root");
        root.addContent(md);
        root.addContent(env);

        //--- do an XSL  transformation
        Path styleSheetPath = getSchemaDir(schema).resolve(styleSheet);

        md = Xml.transform(root, styleSheetPath);
        String changeDate = null;
        String uuid = null;
        if (schemaMan.getSchema(schema).isReadwriteUUID()) {
            uuid = extractUUID(schema, md);
        }

        xmlSerializer.update(metadataId, md, changeDate, true, uuid, context);

        if (indexAfterChange) {
            // Notifies the metadata change to metatada notifier service
            notifyMetadataChange(md, metadataId);

            //--- update search criteria
            indexMetadata(metadataId, true);
        }
    }

    /**
     *
     * @param context
     * @param id
     * @param licenseurl
     * @param imageurl
     * @param jurisdiction
     * @param licensename
     * @param type
     * @throws Exception
     */
    public void setDataCommons(ServiceContext context, String id, String licenseurl, String imageurl, String jurisdiction, String licensename, String type) throws Exception {
        Element env = prepareCommonsEnv(licenseurl, imageurl, jurisdiction, licensename, type);
        manageCommons(context,id,env,Geonet.File.SET_DATACOMMONS);
    }

    private Element prepareCommonsEnv(String licenseurl, String imageurl, String jurisdiction, String licensename, String type) {
        Element env = new Element("env");
        env.addContent(new Element("imageurl").setText(imageurl));
        env.addContent(new Element("licenseurl").setText(licenseurl));
        env.addContent(new Element("jurisdiction").setText(jurisdiction));
        env.addContent(new Element("licensename").setText(licensename));
        env.addContent(new Element("type").setText(type));
        return env;
    }

    /**
     *
     * @param context
     * @param id
     * @param licenseurl
     * @param imageurl
     * @param jurisdiction
     * @param licensename
     * @param type
     * @throws Exception
     */
    public void setCreativeCommons(ServiceContext context, String id, String licenseurl, String imageurl, String jurisdiction, String licensename, String type) throws Exception {
        Element env = prepareCommonsEnv(licenseurl, imageurl, jurisdiction, licensename, type);
        manageCommons(context, id, env, Geonet.File.SET_CREATIVECOMMONS);
    }

    /**
     *
     * @param context
     * @param id
     * @param env
     * @param styleSheet
     * @throws Exception
     */
    private void manageCommons(ServiceContext context, String id, Element env, String styleSheet) throws Exception {
        Lib.resource.checkEditPrivilege(context, id);
        Element md = xmlSerializer.select(context, id);

        if (md == null) return;

        md.detach();

        String schema = getMetadataSchema(id);
        transformMd(context, id, md, env, schema, styleSheet, true);
    }

    //--------------------------------------------------------------------------
    //---
    //--- Privileges API
    //---
    //--------------------------------------------------------------------------
    
    /**
     *  Adds a permission to a group. Metadata is not reindexed.
     *
     * @param context
     * @param mdId
     * @param grpId
     * @throws Exception
     */
    public void setOperation(ServiceContext context, String mdId, String grpId, ReservedOperation op) throws Exception {
        setOperation(context,Integer.valueOf(mdId),Integer.valueOf(grpId), op.getId());
    }

    /**
     *  Adds a permission to a group. Metadata is not reindexed.
     *
     * @param context
     * @param mdId
     * @param grpId
     * @param opId
     * @throws Exception
     */
    public void setOperation(ServiceContext context, String mdId, String grpId, String opId) throws Exception {
        setOperation(context, Integer.valueOf(mdId), Integer.valueOf(grpId), Integer.valueOf(opId));
    }

    /**
     * Set metadata privileges.
     *
     * Administrator can set operation for any groups.
     *
     * For reserved group (ie. Internet, Intranet & Guest), user MUST be reviewer of one group.
     * For other group, if "Only set privileges to user's groups" is set in catalog configuration
     * user MUST be a member of the group.
     *
     * @param context
     * @param mdId The metadata identifier
     * @param grpId The group identifier
     * @param opId The operation identifier
     *
     * @return true if the operation was set.
     * @throws Exception
     */
    public boolean setOperation(ServiceContext context, int mdId, int grpId, int opId) throws Exception {
        OperationAllowedRepository opAllowedRepo = _applicationContext.getBean(OperationAllowedRepository.class);
        Optional<OperationAllowed> opAllowed = getOperationAllowedToAdd(context, mdId, grpId, opId);

        // Set operation
        if (opAllowed.isPresent()) {
            opAllowedRepo.save(opAllowed.get());
            svnManager.setHistory(mdId + "", context);
            return true;
        }

        return false;
    }

    /**
     * Check that the operation has not been added and if not that it can be added.
     * <ul>
     *     <li>
     *         If the operation can be added then an non-empty optional is return.
 *         </li>
     *     <li>
     *         If it has already been added the return empty optional
     *     </li>
     *     <li>
     *         If it is not permitted to be added throw exception.
     *     </li>
     * </ul>
     *
     * @param context
     * @param mdId
     * @param grpId
     * @param opId
     * @return
     */
    public Optional<OperationAllowed> getOperationAllowedToAdd(final ServiceContext context, final int mdId, final int grpId, final int opId) {
        OperationAllowedRepository opAllowedRepo = _applicationContext.getBean(OperationAllowedRepository.class);
        UserGroupRepository userGroupRepo = _applicationContext.getBean(UserGroupRepository.class);
        final OperationAllowed operationAllowed = opAllowedRepo
                .findOneById_GroupIdAndId_MetadataIdAndId_OperationId(grpId, mdId, opId);

        if (operationAllowed == null) {
            checkOperationPermission(context, grpId, userGroupRepo);
        }

        if (operationAllowed == null) {
            return Optional.of(new OperationAllowed(new OperationAllowedId().setGroupId(grpId).setMetadataId(mdId).setOperationId(opId)));
        } else {
            return Optional.absent();
        }
    }

    public void checkOperationPermission(ServiceContext context, int grpId, UserGroupRepository userGroupRepo) {
        // Check user privileges
        // Session may not be defined when a harvester is running
        if (context.getUserSession() != null) {
            Profile userProfile = context.getUserSession().getProfile();
            if (!(userProfile == Profile.Administrator || userProfile == Profile.UserAdmin)) {
                int userId = context.getUserSession().getUserIdAsInt();
                // Reserved groups
                if (ReservedGroup.isReserved(grpId)) {

                    Specification<UserGroup> hasUserIdAndProfile = where(UserGroupSpecs.hasProfile(Profile.Reviewer))
                            .and(UserGroupSpecs.hasUserId(userId));
                    List<Integer> groupIds = userGroupRepo.findGroupIds(hasUserIdAndProfile);

                    if (groupIds.isEmpty()) {
                        throw new ServiceNotAllowedEx("User can't set operation for group " + grpId + " because the user in not a "
                                                      + "Reviewer of any group.");
                    }
                } else {
                    String userGroupsOnly = settingMan.getValue("system/metadataprivs/usergrouponly");
                    if (userGroupsOnly.equals("true")) {
                        // If user is member of the group, user can set operation

                        if (userGroupRepo.exists(new UserGroupId().setGroupId(grpId).setUserId(userId))) {
                            throw new ServiceNotAllowedEx("User can't set operation for group " + grpId + " because the user in not"
                                                          + " member of this group.");
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @param context
     * @param mdId
     * @param grpId
     * @param opId
     * @throws Exception
     */
    public void unsetOperation(ServiceContext context, String mdId, String grpId, ReservedOperation opId) throws Exception {
        unsetOperation(context,Integer.valueOf(mdId),Integer.valueOf(grpId),opId.getId());
    }

    /**
     *
     * @param context
     * @param mdId
     * @param grpId
     * @param opId
     * @throws Exception
     */
    public void unsetOperation(ServiceContext context, String mdId, String grpId, String opId) throws Exception {
        unsetOperation(context,Integer.valueOf(mdId),Integer.valueOf(grpId),Integer.valueOf(opId));
    }

    /**
     *
     * @param context
     * @param mdId metadata id
     * @param groupId group id
     * @param operId operation id
     */
    public void unsetOperation(ServiceContext context, int mdId, int groupId, int operId) throws Exception {
        checkOperationPermission(context, groupId, context.getBean(UserGroupRepository.class));

        OperationAllowedId id = new OperationAllowedId().setGroupId(groupId).setMetadataId(mdId).setOperationId(operId);
        final OperationAllowedRepository repository = context.getBean(OperationAllowedRepository.class);
        if (repository.exists(id)) {
            repository.delete(id);
            if (svnManager != null) {
                svnManager.setHistory(mdId+"", context);
            }
        }
    }

    /**
     * Sets VIEW and NOTIFY privileges for a metadata to a group.
     *
     * @param context service context
     * @param id metadata id
     * @param groupId group id
     * @param fullRightsForGroup TODO
     * @throws Exception hmmm
     */
    public void copyDefaultPrivForGroup(ServiceContext context, String id, String groupId, boolean fullRightsForGroup) throws Exception {
        if(StringUtils.isBlank(groupId)) {
            Log.info(Geonet.DATA_MANAGER, "Attempt to set default privileges for metadata " + id + " to an empty groupid");
            return;
        }
        //--- store access operations for group

        setOperation(context, id, groupId, ReservedOperation.view);
        setOperation(context, id, groupId, ReservedOperation.notify);
        //
        // Restrictive: new and inserted records should not be editable,
        // their resources can't be downloaded and any interactive maps can't be
        // displayed by users in the same group
        if(fullRightsForGroup) {
            setOperation(context, id, groupId, ReservedOperation.editing);
            setOperation(context, id, groupId, ReservedOperation.download);
            setOperation(context, id, groupId, ReservedOperation.dynamic);
        }
        // Ultimately this should be configurable elsewhere
    }

    //--------------------------------------------------------------------------
    //---
    //--- Check User Id to avoid foreign key problems
    //---
    //--------------------------------------------------------------------------

    public boolean isUserMetadataOwner(int userId) throws Exception {
        return _metadataRepository.count(MetadataSpecs.isOwnedByUser(userId)) > 0;
    }

    public boolean isUserMetadataStatus(int userId) throws Exception {
        MetadataStatusRepository statusRepository = _applicationContext.getBean(MetadataStatusRepository.class);

        return statusRepository.count(MetadataStatusSpecs.hasUserId(userId)) > 0;
    }

    public boolean existsUser(ServiceContext context, int id) throws Exception {
        return context.getBean(UserRepository.class).count(where(UserSpecs.hasUserId(id))) > 0;
    }

    //--------------------------------------------------------------------------
    //---
    //--- Status API
    //---
    //--------------------------------------------------------------------------


    /**
     * Return all status records for the metadata id - current status is the
     * first child due to sort by DESC on changeDate
     *
     * @param metadataId
     * @return
     * @throws Exception
     *
     */
    public MetadataStatus getStatus(int metadataId) throws Exception {
        String sortField = SortUtils.createPath(MetadataStatus_.id,MetadataStatusId_.changeDate);
        final MetadataStatusRepository statusRepository = _applicationContext.getBean(MetadataStatusRepository.class);
        List<MetadataStatus> status = statusRepository.findAllById_MetadataId(metadataId, new Sort(Sort.Direction.DESC, sortField));
        if (status.isEmpty()) {
            return null;
        } else {
            return status.get(0);
        }
    }

    /**
     * Return status of metadata id.
     *
     * @param metadataId
     * @return
     * @throws Exception
     *
     */
    public String getCurrentStatus(int metadataId) throws Exception {
        MetadataStatus status = getStatus(metadataId);
        if (status == null) {
            return Params.Status.UNKNOWN;
        }

        return String.valueOf(status.getId().getStatusId());
    }

    /**
     * Set status of metadata id and reindex metadata id afterwards.
     *
     *
     * @param context
     * @param id
     * @param status
     * @param changeDate
     * @param changeMessage
     * @throws Exception
     *
     * @return the saved status entity object
     */
    public MetadataStatus setStatus(ServiceContext context, int id, int status, ISODate changeDate, String changeMessage) throws Exception {
        MetadataStatus statusObject = setStatusExt(context, id, status, changeDate, changeMessage);
        indexMetadata(Integer.toString(id), true);
        return statusObject;
    }

    /**
     * Set status of metadata id and do not reindex metadata id afterwards.
     *
     *
     * @param context
     * @param id
     * @param status
     * @param changeDate
     * @param changeMessage
     * @throws Exception
     *
     * @return the saved status entity object
     */
    public MetadataStatus setStatusExt(ServiceContext context, int id, int status, ISODate changeDate, String changeMessage) throws Exception {
        final StatusValueRepository statusValueRepository = _applicationContext.getBean(StatusValueRepository.class);

        MetadataStatus metatatStatus = new MetadataStatus();
        metatatStatus.setChangeMessage(changeMessage);
        metatatStatus.setStatusValue(statusValueRepository.findOne(status));
        int userId = context.getUserSession().getUserIdAsInt();
        MetadataStatusId mdStatusId = new MetadataStatusId()
                .setStatusId(status)
                .setMetadataId(id)
                .setChangeDate(changeDate)
                .setUserId(userId);
        mdStatusId.setChangeDate(changeDate);

        metatatStatus.setId(mdStatusId);

        return _applicationContext.getBean(MetadataStatusRepository.class).save(metatatStatus);
    }

    //--------------------------------------------------------------------------
    //---
    //--- Categories API
    //---
    //--------------------------------------------------------------------------

    /**
     * Adds a category to a metadata. Metadata is not reindexed.
     *
     * @param mdId
     * @param categId
     * @throws Exception
     */
    public void setCategory(ServiceContext context, String mdId, String categId) throws Exception {
        final MetadataCategoryRepository categoryRepository = _applicationContext.getBean(MetadataCategoryRepository.class);

        final MetadataCategory newCategory = categoryRepository.findOne(Integer.valueOf(categId));
        final boolean[] changed = new boolean[1];
        _metadataRepository.update(Integer.valueOf(mdId), new Updater<Metadata>() {
            @Override
            public void apply(@Nonnull Metadata entity) {
                changed[0] = !entity.getCategories().contains(newCategory);
                entity.getCategories().add(newCategory);
            }
        });

        if (changed[0]) {
            if (svnManager != null) {
                svnManager.setHistory(mdId, context);
            }
        }
    }

    /**
     *
     * @param mdId
     * @param categId
     * @return
     * @throws Exception
     */
    public boolean isCategorySet(final String mdId, final int categId) throws Exception {
        Set<MetadataCategory> categories = _metadataRepository.findOne(mdId).getCategories();
        for (MetadataCategory category : categories) {
            if (category.getId() == categId) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param mdId
     * @param categId
     * @throws Exception
     */
    public void unsetCategory(final ServiceContext context, final String mdId, final int categId) throws Exception {
        Metadata metadata = _metadataRepository.findOne(mdId);

        if (metadata == null) {
            return;
        }
        boolean changed = false;
        for (MetadataCategory category : metadata.getCategories()) {
            if (category.getId() == categId) {
                changed = true;
                metadata.getCategories().remove(category);
                break;
            }
        }

        if (changed) {
            _metadataRepository.save(metadata);
            if (svnManager != null) {
                svnManager.setHistory(mdId+"", context);
            }
        }
    }

    /**
     *
     * @param mdId
     * @return
     * @throws Exception
     */
    public Collection<MetadataCategory> getCategories(final String mdId) throws Exception {
        Metadata metadata = _metadataRepository.findOne(mdId);
        if (metadata == null) {
            throw new IllegalArgumentException("No metadata found with id: "+mdId);
        }

        return metadata.getCategories();
    }

    /**
     * Update metadata record (not template) using update-fixed-info.xsl
     *
     *
     * @param schema
     * @param metadataId
     * @param uuid If the metadata is a new record (not yet saved), provide the uuid for that record
     * @param md
     * @param parentUuid
     * @param updateDatestamp   FIXME ? updateDatestamp is not used when running XSL transformation
     * @return
     * @throws Exception
     */
    public Element updateFixedInfo(String schema, Optional<Integer> metadataId, String uuid, Element md, String parentUuid, UpdateDatestamp updateDatestamp, ServiceContext context) throws Exception {
        boolean autoFixing = settingMan.getValueAsBool("system/autofixing/enable", true);
        if(autoFixing) {
            if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "Autofixing is enabled, trying update-fixed-info (updateDatestamp: " + updateDatestamp.name() + ")");

            Metadata metadata = null;
            if (metadataId.isPresent()) {
                metadata = _metadataRepository.findOne(metadataId.get());
                boolean isTemplate = metadata != null && metadata.getDataInfo().getType() != MetadataType.METADATA;

                // don't process templates
                if(isTemplate) {
                    if(Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                        Log.debug(Geonet.DATA_MANAGER, "Not applying update-fixed-info for a template");
                    }
                    return md;
                }
            }

            String currentUuid = metadata != null ? metadata.getUuid() : null;
            String id = metadata != null ? metadata.getId() + "" : null;
            uuid = uuid == null ? currentUuid : uuid;

            //--- setup environment
            Element env = new Element("env");
            env.addContent(new Element("id").setText(id));
            env.addContent(new Element("uuid").setText(uuid));
            Element schemaLoc = new Element("schemaLocation");
            schemaLoc.setAttribute(schemaMan.getSchemaLocation(schema,context));
            env.addContent(schemaLoc);

            if (updateDatestamp == UpdateDatestamp.YES) {
                env.addContent(new Element("changeDate").setText(new ISODate().toString()));
            }
            if(parentUuid != null) {
                env.addContent(new Element("parentUuid").setText(parentUuid));
            }
            if (metadataId.isPresent()) {
                String metadataIdString = String.valueOf(metadataId.get());
                final Path resourceDir = Lib.resource.getDir(context, Params.Access.PRIVATE, metadataIdString);
                env.addContent(new Element("datadir").setText(resourceDir.toString()));
            }

            // add original metadata to result
            Element result = new Element("root");
            result.addContent(md);
            // add 'environment' to result
            env.addContent(new Element("siteURL")   .setText(settingMan.getSiteURL(context)));

            // Settings were defined as an XML starting with root named config
            // Only second level elements are defined (under system).
            List config = settingMan.getAllAsXML(true).cloneContent();
            Element settings = (Element) config.get(0);
            settings.setName("config");
            env.addContent(settings);

            result.addContent(env);
            // apply update-fixed-info.xsl
            Path styleSheet = getSchemaDir(schema).resolve(Geonet.File.UPDATE_FIXED_INFO);
            result = Xml.transform(result, styleSheet);
            return result;
        } else {
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "Autofixing is disabled, not applying update-fixed-info");
            }
            return md;
        }
    }

    /**
     * Updates all children of the selected parent. Some elements are protected
     * in the children according to the stylesheet used in
     * xml/schemas/[SCHEMA]/update-child-from-parent-info.xsl.
     *
     * Children MUST be editable and also in the same schema of the parent.
     * If not, child is not updated.
     *
     *
     * @param srvContext
     *            service context
     * @param parentUuid
     *            parent uuid
     * @param children
     *            children
     * @param params
     *            parameters
     * @return
     * @throws Exception
     */
    public Set<String> updateChildren(ServiceContext srvContext, String parentUuid, String[] children, Map<String, Object> params) throws Exception {
        String parentId = (String)params.get(Params.ID);
        String parentSchema = (String)params.get(Params.SCHEMA);

        // --- get parent metadata in read/only mode
        boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
        Element parent = getMetadata(srvContext, parentId, forEditing, withValidationErrors, keepXlinkAttributes);

        Element env = new Element("update");
        env.addContent(new Element("parentUuid").setText(parentUuid));
        env.addContent(new Element("siteURL").setText(settingMan.getSiteURL(srvContext)));
        env.addContent(new Element("parent").addContent(parent));

        // Set of untreated children (out of privileges, different schemas)
        Set<String> untreatedChildSet = new HashSet<String>();

        // only get iso19139 records
        for (String childId : children) {

            // Check privileges
            if (!accessMan.canEdit(srvContext, childId)) {
                untreatedChildSet.add(childId);
                if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, "Could not update child ("
                            + childId + ") because of privileges.");
                continue;
            }

            Element child = getMetadata(srvContext, childId, forEditing, withValidationErrors, keepXlinkAttributes);

            String childSchema = child.getChild(Edit.RootChild.INFO,
                    Edit.NAMESPACE).getChildText(Edit.Info.Elem.SCHEMA);

            // Check schema matching. CHECKME : this suppose that parent and
            // child are in the same schema (even not profil different)
            if (!childSchema.equals(parentSchema)) {
                untreatedChildSet.add(childId);
                if(Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                    Log.debug(Geonet.DATA_MANAGER, "Could not update child ("
                            + childId + ") because schema (" + childSchema
                            + ") is different from the parent one (" + parentSchema
                            + ").");
                }
                continue;
            }

            if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "Updating child (" + childId +") ...");

            // --- setup xml element to be processed by XSLT

            Element rootEl = new Element("root");
            Element childEl = new Element("child").addContent(child.detach());
            rootEl.addContent(childEl);
            rootEl.addContent(env.detach());

            // --- do an XSL transformation

            Path styleSheet = getSchemaDir(parentSchema).resolve(Geonet.File.UPDATE_CHILD_FROM_PARENT_INFO);
            Element childForUpdate = Xml.transform(rootEl, styleSheet, params);

            xmlSerializer.update(childId, childForUpdate, new ISODate().toString(), true, null, srvContext);


            // Notifies the metadata change to metatada notifier service
            notifyMetadataChange(childForUpdate, childId);

            rootEl = null;
        }

        return untreatedChildSet;
    }

    /**
     * TODO : buildInfoElem contains similar portion of code with indexMetadata
     * @param context
     * @param id
     * @param version
     * @return
     * @throws Exception
     */
    private Element buildInfoElem(ServiceContext context, String id, String version) throws Exception {
        Metadata metadata = _metadataRepository.findOne(id);
        final MetadataDataInfo dataInfo = metadata.getDataInfo();
        String schema = dataInfo.getSchemaId();
        String createDate = dataInfo.getCreateDate().getDateAndTime();
        String changeDate = dataInfo.getChangeDate().getDateAndTime();
        String source = metadata.getSourceInfo().getSourceId();
        String isTemplate = dataInfo.getType().codeString;
        String title = dataInfo.getTitle();
        String uuid = metadata.getUuid();
        String isHarvested = "" + Constants.toYN_EnabledChar(metadata.getHarvestInfo().isHarvested());
        String harvestUuid = metadata.getHarvestInfo().getUuid();
        String popularity = "" + dataInfo.getPopularity();
        String rating = "" + dataInfo.getRating();
        String owner = "" + metadata.getSourceInfo().getOwner();
        String displayOrder = "" + dataInfo.getDisplayOrder();

        Element info = new Element(Edit.RootChild.INFO, Edit.NAMESPACE);

        addElement(info, Edit.Info.Elem.ID, id);
        addElement(info, Edit.Info.Elem.SCHEMA, schema);
        addElement(info, Edit.Info.Elem.CREATE_DATE, createDate);
        addElement(info, Edit.Info.Elem.CHANGE_DATE, changeDate);
        addElement(info, Edit.Info.Elem.IS_TEMPLATE, isTemplate);
        addElement(info, Edit.Info.Elem.TITLE, title);
        addElement(info, Edit.Info.Elem.SOURCE, source);
        addElement(info, Edit.Info.Elem.UUID, uuid);
        addElement(info, Edit.Info.Elem.IS_HARVESTED, isHarvested);
        addElement(info, Edit.Info.Elem.POPULARITY, popularity);
        addElement(info, Edit.Info.Elem.RATING, rating);
        addElement(info, Edit.Info.Elem.DISPLAY_ORDER, displayOrder);

        if (metadata.getHarvestInfo().isHarvested()) {
            HarvestInfoProvider infoProvider = _applicationContext.getBean(HarvestInfoProvider.class);
            if (infoProvider != null) {
                info.addContent(infoProvider.getHarvestInfo(harvestUuid, id, uuid));
            }
        }
        if (version != null) {
            addElement(info, Edit.Info.Elem.VERSION, version);
        }

        Map<String, Element> map = Maps.newHashMap();
        map.put(id, info);
        buildPrivilegesMetadataInfo(context, map);

        // add owner name
        User user = _applicationContext.getBean(UserRepository.class).findOne(owner);
        if (user != null) {
            String ownerName = user.getName();
            addElement(info, Edit.Info.Elem.OWNERNAME, ownerName);
        }


        for (MetadataCategory category : metadata.getCategories()) {
            addElement(info, Edit.Info.Elem.CATEGORY, category.getName());
        }

        // add subtemplates
        /* -- don't add as we need to investigate indexing for the fields
		   -- in the metadata table used here
		List subList = getSubtemplates(dbms, schema);
		if (subList != null) {
			Element subs = new Element(Edit.Info.Elem.SUBTEMPLATES);
			subs.addContent(subList);
			info.addContent(subs);
		}
		*/


        // Add validity information
        List<MetadataValidation> validationInfo = _metadataValidationRepository.findAllById_MetadataId(Integer.valueOf(id));
        if (validationInfo == null || validationInfo.size() == 0) {
            addElement(info, Edit.Info.Elem.VALID, "-1");
        } else {
            String isValid = "1";
            for (Object elem : validationInfo) {
                MetadataValidation vi = (MetadataValidation) elem;
                String type = vi.getId().getValidationType();
                if (!vi.isValid()) {
                    isValid = "0";
                }

                String ratio = "xsd".equals(type) ? "" : vi.getNumFailures() + "/" + vi.getNumTests();

                info.addContent(new Element(Edit.Info.Elem.VALID + "_details").
                        addContent(new Element("type").setText(type)).
                        addContent(new Element("status").setText(vi.isValid() ? "1" : "0").
                        addContent(new Element("ratio").setText(ratio)))
                );
            }
            addElement(info, Edit.Info.Elem.VALID, isValid);
        }

        // add baseUrl of this site (from settings)
        String protocol = settingMan.getValue(Geonet.Settings.SERVER_PROTOCOL);
        String host = settingMan.getValue(Geonet.Settings.SERVER_HOST);
        String port = settingMan.getValue(Geonet.Settings.SERVER_PORT);
        if (port.equals("80")) {
            port = "";
        } else {
            port = ":"+port;
        }
        addElement(info, Edit.Info.Elem.BASEURL, protocol + "://" + host + port + baseURL);
        addElement(info, Edit.Info.Elem.LOCSERV, "/srv/en");
        return info;
    }

    /**
     * Add privileges information about metadata record
     * which depends on context and usually could not be stored in db 
     * or Lucene index because depending on the current user
     * or current client IP address.
     *
     * @param context
     * @param mdIdToInfoMap a map from the metadata Id -> the info element to which the privilege information should be added.
     * @throws Exception
     */
    @VisibleForTesting
    void buildPrivilegesMetadataInfo(ServiceContext context, Map<String,Element> mdIdToInfoMap) throws Exception {
        Collection<Integer> metadataIds = Collections2.transform(mdIdToInfoMap.keySet(), new Function<String, Integer>() {
            @Nullable
            @Override
            public Integer apply(String input) {
                return Integer.valueOf(input);
            }
        });
        Specification<OperationAllowed> operationAllowedSpec = OperationAllowedSpecs.hasMetadataIdIn(metadataIds);

        final Collection<Integer> allUserGroups = accessMan.getUserGroups(context.getUserSession(), context.getIpAddress(), false);
        final SetMultimap<Integer, ReservedOperation> operationsPerMetadata = loadOperationsAllowed(context, where(operationAllowedSpec).and(OperationAllowedSpecs.hasGroupIdIn(allUserGroups)));
        final Set<Integer> visibleToAll = loadOperationsAllowed(context, where(operationAllowedSpec).and(OperationAllowedSpecs.isPublic(ReservedOperation.view))).keySet();
        final Set<Integer> downloadableByGuest = loadOperationsAllowed(context, where(operationAllowedSpec).and(OperationAllowedSpecs.hasGroupId(ReservedGroup.guest.getId())).and(OperationAllowedSpecs.hasOperation(ReservedOperation.download))).keySet();
        final Map<Integer, MetadataSourceInfo> allSourceInfo = _metadataRepository.findAllSourceInfo(MetadataSpecs.hasMetadataIdIn(metadataIds));

        for (Map.Entry<String, Element> entry : mdIdToInfoMap.entrySet()) {
            Element infoEl = entry.getValue();
            final Integer mdId = Integer.valueOf(entry.getKey());
            MetadataSourceInfo sourceInfo = allSourceInfo.get(mdId);
            Set<ReservedOperation> operations = operationsPerMetadata.get(mdId);
            if (operations == null) {
                operations = Collections.emptySet();
            }

            boolean isOwner = accessMan.isOwner(context, sourceInfo);

            if (isOwner) {
                operations = Sets.newHashSet(Arrays.asList(ReservedOperation.values()));
            }

            if (isOwner || operations.contains(ReservedOperation.editing)) {
                addElement(infoEl, Edit.Info.Elem.EDIT, "true");
            }

            if (isOwner) {
                addElement(infoEl, Edit.Info.Elem.OWNER, "true");
            }

            addElement(infoEl, Edit.Info.Elem.IS_PUBLISHED_TO_ALL, visibleToAll.contains(mdId));
            addElement(infoEl, ReservedOperation.view.name(), operations.contains(ReservedOperation.view));
            addElement(infoEl, ReservedOperation.notify.name(), operations.contains(ReservedOperation.notify));
            addElement(infoEl, ReservedOperation.download.name(), operations.contains(ReservedOperation.download));
            addElement(infoEl, ReservedOperation.dynamic.name(), operations.contains(ReservedOperation.dynamic));
            addElement(infoEl, ReservedOperation.featured.name(), operations.contains(ReservedOperation.featured));

            if (!operations.contains(ReservedOperation.download)) {
                addElement(infoEl, Edit.Info.Elem.GUEST_DOWNLOAD, downloadableByGuest.contains(mdId));
            }
        }
    }

    private SetMultimap<Integer, ReservedOperation> loadOperationsAllowed(ServiceContext context, Specification<OperationAllowed>
            operationAllowedSpec) {
        final OperationAllowedRepository operationAllowedRepo= context.getBean(OperationAllowedRepository.class);
        List<OperationAllowed> operationsAllowed = operationAllowedRepo.findAll(operationAllowedSpec);
        SetMultimap<Integer, ReservedOperation> operationsPerMetadata = HashMultimap.create();
        for (OperationAllowed allowed : operationsAllowed) {
            final OperationAllowedId id = allowed.getId();
            operationsPerMetadata.put(id.getMetadataId(), ReservedOperation.lookup(id.getOperationId()));
        }
        return operationsPerMetadata;
    }

    /**
     *
     * @param root
     * @param name
     * @param value
     */
    private static void addElement(Element root, String name, Object value) {
        root.addContent(new Element(name).setText(value == null ? "" : value.toString()));
    }


    //---------------------------------------------------------------------------
    //---
    //--- Static methods are for external modules like GAST to be able to use
    //--- them.
    //---
    //---------------------------------------------------------------------------

    /**
     *
     * @param md
     */
    public static void setNamespacePrefix(final Element md) {
        //--- if the metadata has no namespace or already has a namespace then
        //--- we must skip this phase

        Namespace ns = md.getNamespace();
        if (ns != Namespace.NO_NAMESPACE && (md.getNamespacePrefix().equals(""))) {
            //--- set prefix for iso19139 metadata

            ns = Namespace.getNamespace("gmd", md.getNamespace().getURI());
            setNamespacePrefix(md, ns);
        }
    }

    /**
     *
     * @param md
     * @param ns
     */
    private static void setNamespacePrefix(final Element md, final Namespace ns) {
        if (md.getNamespaceURI().equals(ns.getURI())) {
            md.setNamespace(ns);
        }

        Attribute xsiType = md.getAttribute("type", Namespaces.XSI);
        if (xsiType != null) {
            String xsiTypeValue = xsiType.getValue();

            if (StringUtils.isNotEmpty(xsiTypeValue) && !xsiTypeValue.contains(":")) {
                xsiType.setValue(ns.getPrefix() + ":" + xsiType.getValue());
            }
        }


        for (Object o : md.getChildren()) {
            setNamespacePrefix((Element) o, ns);
        }
    }

    /**
     *
     * @param md
     * @throws Exception
     */
    private void setNamespacePrefixUsingSchemas(String schema, Element md) throws Exception {
        //--- if the metadata has no namespace or already has a namespace prefix
        //--- then we must skip this phase
        Namespace ns = md.getNamespace();
        if (ns == Namespace.NO_NAMESPACE)
            return;

        MetadataSchema mds = schemaMan.getSchema(schema);

        //--- get the namespaces and add prefixes to any that are
        //--- default (ie. prefix is '') if namespace match one of the schema
        ArrayList<Namespace> nsList = new ArrayList<Namespace>();
        nsList.add(ns);
        @SuppressWarnings("unchecked")
        List<Namespace> additionalNamespaces = md.getAdditionalNamespaces();
        nsList.addAll(additionalNamespaces);
        for (Object aNsList : nsList) {
            Namespace aNs = (Namespace) aNsList;
            if (aNs.getPrefix().equals("")) { // found default namespace
                String prefix = mds.getPrefix(aNs.getURI());
                if (prefix == null) {
                    Log.warning(Geonet.DATA_MANAGER, "Metadata record contains a default namespace " + aNs.getURI() + " (with no prefix) which does not match any " + schema + " schema's namespaces.");
                }
                ns = Namespace.getNamespace(prefix, aNs.getURI());
                setNamespacePrefix(md, ns);
                if (!md.getNamespace().equals(ns)) {
                    md.removeNamespaceDeclaration(aNs);
                    md.addNamespaceDeclaration(ns);
                }
            }
        }
    }

    /**
     *
     * @param md
     * @param metadataId
     * @throws Exception
     */

    public void notifyMetadataChange (Element md, String metadataId) throws Exception {

        final Metadata metadata = _metadataRepository.findOne(metadataId);
        if (metadata != null && metadata.getDataInfo().getType() == MetadataType.METADATA) {
            MetadataSchema mds = servContext.getBean(DataManager.class).getSchema(metadata.getDataInfo().getSchemaId());
            Pair<String, Element> editXpathFilter = mds.getOperationFilter(ReservedOperation.editing);
            XmlSerializer.removeFilteredElement(md, editXpathFilter, mds.getNamespaces());

            String uuid = getMetadataUuid( metadataId);
            servContext.getBean(MetadataNotifierManager.class).updateMetadata(md, metadataId, uuid, servContext);
        }
    }

    public void flush() {
        TransactionManager.runInTransaction("DataManager flush()", _applicationContext,
                TransactionManager.TransactionRequirement.CREATE_ONLY_WHEN_NEEDED,
                TransactionManager.CommitBehavior.ALWAYS_COMMIT, false, new TransactionTask<Object>() {
                    @Override
                    public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                        _entityManager.flush();
                        return null;
                    }
                });

    }

    public int batchDeleteMetadataAndUpdateIndex(Specification<Metadata> specification) throws Exception {
        final List<Integer> idsOfMetadataToDelete = _metadataRepository.findAllIdsBy(specification);

        for (Integer id: idsOfMetadataToDelete) {
            //--- remove metadata directory for each record
            final Path metadataDataDir = _applicationContext.getBean(GeonetworkDataDirectory.class).getMetadataDataDir();
            Path pb = Lib.resource.getMetadataDir(metadataDataDir, id + "");
            IO.deleteFileOrDirectory(pb);
        }

        // Remove records from the index
        searchMan.delete("_id", Lists.transform(idsOfMetadataToDelete, new Function<Integer, String>() {
            @Nullable
            @Override
            public String apply(@Nonnull Integer input) {
                return input.toString();
            }
        }));

        // Remove records from the database
        _metadataRepository.deleteAll(specification);

        return idsOfMetadataToDelete.size();
    }
}
