package org.fao.geonet.kernel.datamanager.base;

import java.io.IOException;
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.InspireAtomFeed;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatusId_;
import org.fao.geonet.domain.MetadataStatus_;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.MetadataValidationStatus;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.userfeedback.RatingsSetting;
import org.fao.geonet.events.md.MetadataIndexCompleted;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.IndexMetadataTask;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.SvnManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.ISearchManager;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.InspireAtomFeedRepository;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.userfeedback.UserFeedbackRepository;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.util.ThreadUtils;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.jdom.Attribute;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Lazy;
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

public class BaseMetadataIndexer implements IMetadataIndexer, ApplicationEventPublisherAware {

	Lock waitLoopLock = new ReentrantLock();
	Lock indexingLock = new ReentrantLock();

	@Autowired
	private SearchManager searchManager;
	@Autowired
	private GeonetworkDataDirectory geonetworkDataDirectory;
	@Autowired
	private MetadataStatusRepository statusRepository;

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

	// FIXME remove when get rid of Jeeves
	private ServiceContext servContext;

	private ApplicationEventPublisher publisher;

	public BaseMetadataIndexer() {
	}

	public void init(ServiceContext context, Boolean force) throws Exception {
		searchManager = context.getBean(SearchManager.class);
		geonetworkDataDirectory = context.getBean(GeonetworkDataDirectory.class);
		statusRepository = context.getBean(MetadataStatusRepository.class);
		metadataUtils = context.getBean(IMetadataUtils.class);
		metadataManager = context.getBean(IMetadataManager.class);
		userRepository = context.getBean(UserRepository.class);
		operationAllowedRepository = context.getBean(OperationAllowedRepository.class);
		groupRepository = context.getBean(GroupRepository.class);
		metadataValidationRepository = context.getBean(MetadataValidationRepository.class);
		schemaManager = context.getBean(SchemaManager.class);
		svnManager = context.getBean(SvnManager.class);
		inspireAtomFeedRepository = context.getBean(InspireAtomFeedRepository.class);
		xmlSerializer = context.getBean(XmlSerializer.class);
		settingManager = context.getBean(SettingManager.class);
		userFeedbackRepository = context.getBean(UserFeedbackRepository.class);

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
	Set<IndexMetadataTask> batchIndex = new ConcurrentHashSet<IndexMetadataTask>();

	@Override
	public void forceIndexChanges() throws IOException {
		searchManager.forceIndexChanges();
	}

	@Override
	public int batchDeleteMetadataAndUpdateIndex(Specification<? extends AbstractMetadata> specification)
			throws Exception {
		final List<Integer> idsOfMetadataToDelete = metadataUtils.findAllIdsBy(specification);

		for (Integer id : idsOfMetadataToDelete) {
			// --- remove metadata directory for each record
			final Path metadataDataDir = geonetworkDataDirectory.getMetadataDataDir();
			Path pb = Lib.resource.getMetadataDir(metadataDataDir, id + "");
			IO.deleteFileOrDirectory(pb);
		}

		// Remove records from the index
		searchManager.delete(Lists.transform(idsOfMetadataToDelete, new Function<Integer, String>() {
			@Nullable
			@Override
			public String apply(@Nonnull Integer input) {
				return input.toString();
			}
		}));

		// Remove records from the database
		metadataManager.deleteAll(specification);

		return idsOfMetadataToDelete.size();
	}

	@Override
	/**
	 * Search for all records having XLinks (ie. indexed with _hasxlinks flag),
	 * clear the cache and reindex all records found.
	 */
	public synchronized void rebuildIndexXLinkedMetadata(final ServiceContext context) throws Exception {

		// get all metadata with XLinks
		Set<Integer> toIndex = searchManager.getDocsWithXLinks();

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
			for (Iterator<String> iter = sm.getSelection(bucket).iterator(); iter.hasNext();) {
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
	 * @param context
	 *            context object
	 * @param metadataIds
	 *            the metadata ids to index
	 */
	@Override
	public void batchIndexInThreadPool(ServiceContext context, List<?> metadataIds) {

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
		indexingLock.lock();
		try {
			return !indexing.isEmpty() || !batchIndex.isEmpty();
		} finally {
			indexingLock.unlock();
		}
	}

	@Override
	public void indexMetadata(final List<String> metadataIds) throws Exception {
		for (String metadataId : metadataIds) {
			indexMetadata(metadataId, false, null);
		}

		searchManager.forceIndexChanges();
	}

	@Override
	public void indexMetadata(final String metadataId, boolean forceRefreshReaders, ISearchManager searchManager)
			throws Exception {
		waitLoopLock.lock();
		try {
			if (waitForIndexing.contains(metadataId)) {
				return;
			}
			while (indexing.contains(metadataId)) {
				try {
					waitForIndexing.add(metadataId);
					// don't index the same metadata 2x
					synchronized (this) {
						wait(200);
					}
				} catch (InterruptedException e) {
					return;
				} finally {
					waitForIndexing.remove(metadataId);
				}
			}
			indexingLock.lock();
			try {
				indexing.add(metadataId);
			} finally {
				indexingLock.unlock();
			}
		} finally {
			waitLoopLock.unlock();
		}
		AbstractMetadata fullMd;

		try {
			Vector<Element> moreFields = new Vector<Element>();
			int id$ = Integer.parseInt(metadataId);

			// get metadata, extracting and indexing any xlinks
			Element md = getXmlSerializer().selectNoXLinkResolver(metadataId, true, false);
			if (getXmlSerializer().resolveXLinks()) {
				List<Attribute> xlinks = Processor.getXLinks(md);
				if (xlinks.size() > 0) {
					moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.HASXLINKS, "1", true, true));
					for (Attribute xlink : xlinks) {
						moreFields.add(
								SearchManager.makeField(Geonet.IndexFieldNames.XLINK, xlink.getValue(), true, true));
					}
					Processor.detachXLink(md, getServiceContext());
				} else {
					moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.HASXLINKS, "0", true, true));
				}
			} else {
				moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.HASXLINKS, "0", true, true));
			}

			fullMd = metadataUtils.findOne(id$);

			final String schema = fullMd.getDataInfo().getSchemaId();
			final String createDate = fullMd.getDataInfo().getCreateDate().getDateAndTime();
			final String changeDate = fullMd.getDataInfo().getChangeDate().getDateAndTime();
			final String source = fullMd.getSourceInfo().getSourceId();
			final MetadataType metadataType = fullMd.getDataInfo().getType();
			final String root = fullMd.getDataInfo().getRoot();
			final String uuid = fullMd.getUuid();
			final String extra = fullMd.getDataInfo().getExtra();
			final String isHarvested = String
					.valueOf(Constants.toYN_EnabledChar(fullMd.getHarvestInfo().isHarvested()));
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

			moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.ROOT, root, true, true));
			moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.SCHEMA, schema, true, true));
			moreFields
					.add(SearchManager.makeField(Geonet.IndexFieldNames.DATABASE_CREATE_DATE, createDate, true, true));
			moreFields
					.add(SearchManager.makeField(Geonet.IndexFieldNames.DATABASE_CHANGE_DATE, changeDate, true, true));
			moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.SOURCE, source, true, true));
			moreFields.add(
					SearchManager.makeField(Geonet.IndexFieldNames.IS_TEMPLATE, metadataType.codeString, true, true));
			moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.UUID, uuid, true, true));
			moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.IS_HARVESTED, isHarvested, true, true));
			moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.OWNER, owner, true, true));
			moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.DUMMY, "0", false, true));
			moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.POPULARITY, popularity, true, true));
			moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.RATING, rating, true, true));
			if (RatingsSetting.ADVANCED.equals(settingManager.getValue(Settings.SYSTEM_LOCALRATING_ENABLE))) {
				int nbOfFeedback = userFeedbackRepository.findByMetadata_Uuid(uuid).size();
				moreFields.add(
						SearchManager.makeField(Geonet.IndexFieldNames.FEEDBACKCOUNT, nbOfFeedback + "", true, true));
			}
			moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.DISPLAY_ORDER, displayOrder, true, false));
			moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.EXTRA, extra, false, true));

			// If the metadata has an atom document, index related information
			InspireAtomFeed feed = inspireAtomFeedRepository.findByMetadataId(id$);

			if ((feed != null) && StringUtils.isNotEmpty(feed.getAtom())) {
				moreFields.add(SearchManager.makeField("has_atom", "y", true, true));
				moreFields.add(SearchManager.makeField("any", feed.getAtom(), false, true));
			}

			if (owner != null) {
				User user = userRepository.findOne(fullMd.getSourceInfo().getOwner());
				if (user != null) {
					moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.USERINFO, user.getUsername() + "|"
							+ user.getSurname() + "|" + user.getName() + "|" + user.getProfile(), true, false));
				}
			}

			String logoUUID = null;
			if (groupOwner != null) {
				final Group group = groupRepository.findOne(groupOwner);
				if (group != null) {
					moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.GROUP_OWNER,
							String.valueOf(groupOwner), true, true));
					final boolean preferGroup = settingManager.getValueAsBool(Settings.SYSTEM_PREFER_GROUP_LOGO, true);
					if (group.getWebsite() != null && !group.getWebsite().isEmpty() && preferGroup) {
						moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.GROUP_WEBSITE, group.getWebsite(),
								true, false));
					}
					if (group.getLogo() != null && preferGroup) {
						logoUUID = group.getLogo();
					}
				}
			}

			// Group logo are in the harvester folder and contains extension in file name
			final Path harvesterLogosDir = Resources.locateHarvesterLogosDir(getServiceContext());
			boolean added = false;
			if (StringUtils.isNotEmpty(logoUUID)) {
				final Path logoPath = harvesterLogosDir.resolve(logoUUID);
				if (Files.exists(logoPath)) {
					added = true;
					moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.LOGO,
							"/images/harvesting/" + logoPath.getFileName(), true, false));
				}
			}

			// If not available, use the local catalog logo
			if (!added) {
				logoUUID = source + ".png";
				final Path logosDir = Resources.locateLogosDir(getServiceContext());
				final Path logoPath = logosDir.resolve(logoUUID);
				if (Files.exists(logoPath)) {
					moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.LOGO, "/images/logos/" + logoUUID,
							true, false));
				}
			}

			// get privileges
			List<OperationAllowed> operationsAllowed = operationAllowedRepository.findAllById_MetadataId(id$);

			boolean isPublishedToAll = false;

			for (OperationAllowed operationAllowed : operationsAllowed) {
				OperationAllowedId operationAllowedId = operationAllowed.getId();
				int groupId = operationAllowedId.getGroupId();
				int operationId = operationAllowedId.getOperationId();

				moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.OP_PREFIX + operationId,
						String.valueOf(groupId), true, true));
				if (operationId == ReservedOperation.view.getId()) {
					Group g = groupRepository.findOne(groupId);
					if (g != null) {
						moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.GROUP_PUBLISHED, g.getName(),
								true, true));
						if (g.getId() == ReservedGroup.all.getId()) {
							isPublishedToAll = true;
						}
					}
				}
			}

			if (isPublishedToAll) {
				moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.IS_PUBLISHED_TO_ALL, "y", true, true));
			} else {
				moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.IS_PUBLISHED_TO_ALL, "n", true, true));
			}

			for (MetadataCategory category : fullMd.getCategories()) {
				moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.CAT, category.getName(), true, true));
			}

			// get status
			Sort statusSort = new Sort(Sort.Direction.DESC,
					MetadataStatus_.id.getName() + "." + MetadataStatusId_.changeDate.getName());
			List<MetadataStatus> statuses = statusRepository.findAllById_MetadataId(id$, statusSort);
			if (!statuses.isEmpty()) {
				MetadataStatus stat = statuses.get(0);
				String status = String.valueOf(stat.getId().getStatusId());
				moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.STATUS, status, true, true));
				String statusChangeDate = stat.getId().getChangeDate().getDateAndTime();
				moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.STATUS_CHANGE_DATE, statusChangeDate,
						true, true));
			}

			// getValidationInfo
			// -1 : not evaluated
			// 0 : invalid
			// 1 : valid
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
					moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.VALID + "_" + type, status.getCode(),
							true, true));
				}
				moreFields.add(SearchManager.makeField(Geonet.IndexFieldNames.VALID, isValid, true, true));
			}
			
			//To inject extra fields from BaseMetadataIndexer inherited beans
			addExtraFields(fullMd, moreFields);

			if (searchManager == null) {
				searchManager = servContext.getBean(SearchManager.class);
			}

			searchManager.index(schemaManager.getSchemaDir(schema), md, metadataId, moreFields, metadataType, root,
					forceRefreshReaders);

		} catch (Exception x) {
			Log.error(Geonet.DATA_MANAGER, "The metadata document index with id=" + metadataId
					+ " is corrupt/invalid - ignoring it. Error: " + x.getMessage(), x);
			fullMd = null;
		} finally {
			indexingLock.lock();
			try {
				indexing.remove(metadataId);
			} finally {
				indexingLock.unlock();
			}
		}
		if (fullMd != null) {
			this.publisher.publishEvent(new MetadataIndexCompleted(fullMd));
		}
	}
	

	/**
	 * Function to be overrided by children to add extra fields cleanly. 
	 * Don't forget to call always super.addExtraFields, just in case
	 * @param fullMd
	 * @param moreFields
	 */
    protected void addExtraFields(AbstractMetadata fullMd, Vector<Element> moreFields) {
    	
    }

	private XmlSerializer getXmlSerializer() {
		return xmlSerializer;
	}

	/**
	 *
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

	/**
	 *
	 * @param beginAt
	 * @param interval
	 * @throws Exception
	 */
	@Override
	public void rescheduleOptimizer(Calendar beginAt, int interval) throws Exception {
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

	private ServiceContext getServiceContext() {
		ServiceContext context = ServiceContext.get();
		return context == null ? servContext : context;
	}

	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}
}
