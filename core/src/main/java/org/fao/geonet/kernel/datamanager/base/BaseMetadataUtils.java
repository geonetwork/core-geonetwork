package org.fao.geonet.kernel.datamanager.base;

import static org.fao.geonet.repository.specification.MetadataSpecs.hasMetadataUuid;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;

import org.apache.commons.lang.NotImplementedException;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.domain.MetadataHarvestInfo;
import org.fao.geonet.domain.MetadataRatingByIp;
import org.fao.geonet.domain.MetadataRatingByIpId;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.index.IndexingList;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.notifier.MetadataNotifierManager;
import org.fao.geonet.repository.MetadataRatingByIpRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SimpleMetadata;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.reports.MetadataReportsQueries;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

public class BaseMetadataUtils implements IMetadataUtils {
	@Autowired
	private MetadataRepository metadataRepository;

	@Autowired
	private MetadataNotifierManager metadataNotifierManager;

	// FIXME Remove when get rid of Jeeves
	private ServiceContext servContext;
	@Autowired
	protected IMetadataSchemaUtils metadataSchemaUtils;
	@Autowired
	protected IMetadataIndexer metadataIndexer;
	@Autowired
	protected SchemaManager schemaManager;
	@Autowired
	protected MetadataRatingByIpRepository ratingByIpRepository;
	@Autowired
	@Lazy
	protected SettingManager settingManager;

	@Autowired
	private IndexingList indexingList;

	@Autowired(required = false)
	protected XmlSerializer xmlSerializer;

	private Path stylePath;

	protected IMetadataManager metadataManager;

	@Override
	public void setMetadataManager(IMetadataManager metadataManager) {
		this.metadataManager = metadataManager;
	}

	@SuppressWarnings("unchecked")
	public void init(ServiceContext context, Boolean force) throws Exception {
		metadataRepository = context.getBean(MetadataRepository.class);
		metadataNotifierManager = context.getBean(MetadataNotifierManager.class);
		servContext = context;
		schemaManager = context.getBean(SchemaManager.class);
		metadataSchemaUtils = context.getBean(IMetadataSchemaUtils.class);
		metadataIndexer = context.getBean(IMetadataIndexer.class);
		ratingByIpRepository = context.getBean(MetadataRatingByIpRepository.class);
		settingManager = context.getBean(SettingManager.class);
		xmlSerializer = context.getBean(XmlSerializer.class);
		indexingList = context.getBean(IndexingList.class);

		final GeonetworkDataDirectory dataDirectory = context.getBean(GeonetworkDataDirectory.class);
		stylePath = dataDirectory.resolveWebResource(Geonet.Path.STYLESHEETS);
	}

	/**
	 * Needed to avoid circular dependency injection
	 */
	@PostConstruct
	public void init() {
		this.metadataIndexer.setMetadataUtils(this);
	}

	/**
	 *
	 * @param md
	 * @param metadataId
	 * @throws Exception
	 */
	@Override
	public void notifyMetadataChange(Element md, String metadataId) throws Exception {
		final AbstractMetadata metadata = findOne(metadataId);
		if (metadata != null && metadata.getDataInfo().getType() == MetadataType.METADATA) {
			MetadataSchema mds = schemaManager.getSchema(metadata.getDataInfo().getSchemaId());
			Pair<String, Element> editXpathFilter = mds.getOperationFilter(ReservedOperation.editing);
			XmlSerializer.removeFilteredElement(md, editXpathFilter, mds.getNamespaces());

			String uuid = getMetadataUuid(metadataId);
			metadataNotifierManager.updateMetadata(md, metadataId, uuid, getServiceContext());
		}
	}

	/**
	 *
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public @Nullable @Override String getMetadataUuid(@Nonnull String id) throws Exception {
		AbstractMetadata metadata = findOne(id);

		if (metadata == null)
			return null;

		return metadata.getUuid();
	}

	protected ServiceContext getServiceContext() {
		ServiceContext context = ServiceContext.get();
		return context == null ? servContext : context;
	}

	/**
	 * Start an editing session. This will record the original metadata record in
	 * the session under the
	 * {@link org.fao.geonet.constants.Geonet.Session#METADATA_BEFORE_ANY_CHANGES} +
	 * id session property.
	 *
	 * The record contains geonet:info element.
	 *
	 * Note: Only the metadata record is stored in session. If the editing session
	 * upload new documents or thumbnails, those documents will not be cancelled.
	 * This needs improvements.
	 */
	@Override
	public Integer startEditingSession(ServiceContext context, String id) throws Exception {
		if (Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
			Log.debug(Geonet.EDITOR_SESSION, "Editing session starts for record " + id);
		}

		boolean keepXlinkAttributes = true;
		boolean forEditing = false;
		boolean withValidationErrors = false;
		Element metadataBeforeAnyChanges = context.getBean(IMetadataManager.class).getMetadata(context, id, forEditing,
				withValidationErrors, keepXlinkAttributes);
		context.getUserSession().setProperty(Geonet.Session.METADATA_BEFORE_ANY_CHANGES + id, metadataBeforeAnyChanges);
		return Integer.valueOf(id);
	}

	/**
	 * Rollback to the record in the state it was when the editing session started
	 * (See {@link #startEditingSession(ServiceContext, String)}).
	 */
	@Override
	public void cancelEditingSession(ServiceContext context, String id) throws Exception {
		UserSession session = context.getUserSession();
		Element metadataBeforeAnyChanges = (Element) session
				.getProperty(Geonet.Session.METADATA_BEFORE_ANY_CHANGES + id);

		if (Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
			Log.debug(Geonet.EDITOR_SESSION, "Editing session end. Cancel changes. Restore record " + id
					+ ". Replace by original record which was: ");
		}

		if (metadataBeforeAnyChanges != null) {
			if (Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
				Log.debug(Geonet.EDITOR_SESSION, " > restoring record: ");
				Log.debug(Geonet.EDITOR_SESSION, Xml.getString(metadataBeforeAnyChanges));
			}
			Element info = metadataBeforeAnyChanges.getChild(Edit.RootChild.INFO, Edit.NAMESPACE);
			boolean validate = false;
			boolean ufo = false;
			boolean index = true;
			metadataBeforeAnyChanges.removeChild(Edit.RootChild.INFO, Edit.NAMESPACE);
			context.getBean(IMetadataManager.class).updateMetadata(context, id, metadataBeforeAnyChanges, validate, ufo,
					index, context.getLanguage(), info.getChildText(Edit.Info.Elem.CHANGE_DATE), false);
			endEditingSession(id, session);
		} else {
			if (Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
				Log.debug(Geonet.EDITOR_SESSION, " > nothing to cancel for record " + id
						+ ". Original record was null. Use starteditingsession to.");
			}
		}
	}

	/**
	 * Remove the original record stored in session.
	 */
	@Override
	public void endEditingSession(String id, UserSession session) {
		if (Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
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
	@Override
	public Element enumerateTree(Element md) throws Exception {
		metadataManager.getEditLib().enumerateTree(md);
		return md;
	}

	/**
	 * Extract UUID from the metadata record using the schema XSL for UUID
	 * extraction)
	 */
	@Override
	public String extractUUID(String schema, Element md) throws Exception {
		Path styleSheet = metadataSchemaUtils.getSchemaDir(schema).resolve(Geonet.File.EXTRACT_UUID);
		String uuid = Xml.transform(md, styleSheet).getText().trim();

		if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
			Log.debug(Geonet.DATA_MANAGER, "Extracted UUID '" + uuid + "' for schema '" + schema + "'");

		// --- needed to detach md from the document
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
	@Override
	public String extractDateModified(String schema, Element md) throws Exception {
		Path styleSheet = metadataSchemaUtils.getSchemaDir(schema).resolve(Geonet.File.EXTRACT_DATE_MODIFIED);
		String dateMod = Xml.transform(md, styleSheet).getText().trim();

		if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
			Log.debug(Geonet.DATA_MANAGER, "Extracted Date Modified '" + dateMod + "' for schema '" + schema + "'");

		// --- needed to detach md from the document
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
	@Override
	public Element setUUID(String schema, String uuid, Element md) throws Exception {
		// --- setup environment

		Element env = new Element("env");
		env.addContent(new Element("uuid").setText(uuid));

		// --- setup root element

		Element root = new Element("root");
		root.addContent(md.detach());
		root.addContent(env.detach());

		// --- do an XSL transformation

		Path styleSheet = metadataSchemaUtils.getSchemaDir(schema).resolve(Geonet.File.SET_UUID);

		return Xml.transform(root, styleSheet);
	}

	/**
	 *
	 * @param md
	 * @return
	 * @throws Exception
	 */
	@Override
	public Element extractSummary(Element md) throws Exception {
		Path styleSheet = stylePath.resolve(Geonet.File.METADATA_BRIEF);
		Element summary = Xml.transform(md, styleSheet);
		if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
			Log.debug(Geonet.DATA_MANAGER, "Extracted summary '\n" + Xml.getString(summary));

		// --- needed to detach md from the document
		md.detach();

		return summary;
	}

	/**
	 *
	 * @param uuid
	 * @return
	 * @throws Exception
	 */
	@Override
	public @Nullable String getMetadataId(@Nonnull String uuid) throws Exception {
		final List<Integer> idList = findAllIdsBy(hasMetadataUuid(uuid));
		if (idList.isEmpty()) {
			return null;
		}
		return String.valueOf(idList.get(0));
	}

	@Override
	public List<Integer> findAllIdsBy(Specification<? extends AbstractMetadata> specs) {
		try {
			return metadataRepository.findAllIdsBy((Specification<Metadata>) specs);
		} catch (ClassCastException t) {
			// Maybe it is not a Specification<Metadata>
		}
		
		return Collections.emptyList();
	}

	/**
	 *
	 * @param id
	 * @return
	 */
	@Override
	public String getVersion(String id) {
		return metadataManager.getEditLib().getVersion(id);
	}

	/**
	 *
	 * @param id
	 * @return
	 */
	@Override
	public String getNewVersion(String id) {
		return metadataManager.getEditLib().getNewVersion(id);
	}

	@Override
	public void setTemplate(final int id, final MetadataType type, final String title) throws Exception {
		setTemplateExt(id, type);
		metadataIndexer.indexMetadata(Integer.toString(id), true, null);
	}

	@Override
	public void setTemplateExt(final int id, final MetadataType metadataType) throws Exception {
		metadataRepository.update(id, new Updater<Metadata>() {
			@Override
			public void apply(@Nonnull Metadata metadata) {
				final MetadataDataInfo dataInfo = metadata.getDataInfo();
				dataInfo.setType(metadataType);
			}
		});
	}

	@Override
	public void setHarvested(int id, String harvestUuid) throws Exception {
		setHarvestedExt(id, harvestUuid);
		metadataIndexer.indexMetadata(Integer.toString(id), true, null);
	}

	/**
	 * Set metadata type to subtemplate and set the title. Only subtemplates need to
	 * persist the title as it is used to give a meaningful title for use when
	 * offering the subtemplate to users in the editor.
	 *
	 * @param id
	 *            Metadata id to set to type subtemplate
	 * @param title
	 *            Title of metadata of subtemplate/fragment
	 */
	@Override
	public void setSubtemplateTypeAndTitleExt(final int id, String title) throws Exception {
		metadataRepository.update(id, new Updater<Metadata>() {
			@Override
			public void apply(@Nonnull Metadata metadata) {
				final MetadataDataInfo dataInfo = metadata.getDataInfo();
				dataInfo.setType(MetadataType.SUB_TEMPLATE);
				if (title != null) {
					dataInfo.setTitle(title);
				}
			}
		});
	}

	@Override
	public void setHarvestedExt(int id, String harvestUuid) throws Exception {
		setHarvestedExt(id, harvestUuid, Optional.<String>absent());
	}

	@Override
	public void setHarvestedExt(final int id, final String harvestUuid, final Optional<String> harvestUri)
			throws Exception {
		metadataRepository.update(id, new Updater<Metadata>() {
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
	 *
	 * @param id
	 * @param displayOrder
	 * @throws Exception
	 */
	@Override
	public void updateDisplayOrder(final String id, final String displayOrder) throws Exception {
		metadataRepository.update(Integer.valueOf(id), new Updater<Metadata>() {
			@Override
			public void apply(Metadata entity) {
				entity.getDataInfo().setDisplayOrder(Integer.parseInt(displayOrder));
			}
		});
	}

	/**
	 * @throws Exception
	 *             hmm
	 */
	@Override
	public void increasePopularity(ServiceContext srvContext, String id) throws Exception {
		// READONLYMODE
		if (!srvContext.getBean(NodeInfo.class).isReadOnly()) {
			// Update the popularity in database
			int iId = Integer.parseInt(id);
			metadataRepository.incrementPopularity(iId);

			// And register the metadata to be indexed in the near future
			final IndexingList list = srvContext.getBean(IndexingList.class);
			list.add(iId);
		} else {
			if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
				Log.debug(Geonet.DATA_MANAGER,
						"GeoNetwork is operating in read-only mode. IncreasePopularity is skipped.");
			}
		}
	}

	/**
	 * Rates a metadata.
	 *
	 * @param ipAddress
	 *            ipAddress IP address of the submitting client
	 * @param rating
	 *            range should be 1..5
	 * @throws Exception
	 *             hmm
	 */
	@Override
	public int rateMetadata(final int metadataId, final String ipAddress, final int rating) throws Exception {
		// Save rating for this IP
		MetadataRatingByIp ratingEntity = new MetadataRatingByIp();
		ratingEntity.setRating(rating);
		ratingEntity.setId(new MetadataRatingByIpId(metadataId, ipAddress));

		ratingByIpRepository.save(ratingEntity);

		// calculate new rating
		final int newRating = ratingByIpRepository.averageRating(metadataId);

		if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
			Log.debug(Geonet.DATA_MANAGER, "Setting rating for id:" + metadataId + " --> rating is:" + newRating);

		metadataRepository.update(metadataId, new Updater<Metadata>() {
			@Override
			public void apply(Metadata entity) {
				entity.getDataInfo().setRating(newRating);
			}
		});
		// And register the metadata to be indexed in the near future
		indexingList.add(metadataId);

		return rating;
	}

	/**
	 * Retrieves a metadata (in xml) given its id with no geonet:info.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Element getMetadataNoInfo(ServiceContext srvContext, String id) throws Exception {
		Element md = srvContext.getBean(IMetadataManager.class).getMetadata(srvContext, id, false, false, false);
		md.removeChild(Edit.RootChild.INFO, Edit.NAMESPACE);

		// Drop Geonet namespace declaration. It may be contained
		// multiple times, so loop on all.
		final List<Namespace> additionalNamespaces = new ArrayList<Namespace>(md.getAdditionalNamespaces());
		for (Namespace n : additionalNamespaces) {
			if (Edit.NAMESPACE.getURI().equals(n.getURI())) {
				md.removeNamespaceDeclaration(Edit.NAMESPACE);
			}
		}
		return md;
	}

	/**
	 * Retrieves a metadata element given it's ref.
	 */
	@Override
	public Element getElementByRef(Element md, String ref) {
		return metadataManager.getEditLib().findElement(md, ref);
	}

	/**
	 * Returns true if the metadata exists in the database.
	 */
	@Override
	public boolean existsMetadata(int id) throws Exception {
		return exists(id);
	}

	/**
	 * Returns true if the metadata uuid exists in the database.
	 */
	@Override
	public boolean existsMetadataUuid(String uuid) throws Exception {
		return !findAllIdsBy(hasMetadataUuid(uuid)).isEmpty();
	}

	/**
	 * Returns all the keywords in the system.
	 */
	@Override
	public Element getKeywords() throws Exception {
		// TODO ES
		// Collection<String> keywords = getSearchManager().getTerms("keyword");
		Element el = new Element("keywords");

		// for (Object keyword : keywords) {
		// el.addContent(new Element("keyword").setText((String) keyword));
		// }
		return el;
	}

	/**
	 *
	 * @param metadataId
	 * @return
	 * @throws Exception
	 */
	@Override
	public Element getThumbnails(ServiceContext context, String metadataId) throws Exception {
		Element md = getXmlSerializer().select(context, metadataId);

		if (md == null)
			return null;

		md.detach();

		String schema = metadataSchemaUtils.getMetadataSchema(metadataId);

		// --- do an XSL transformation
		Path styleSheet = metadataSchemaUtils.getSchemaDir(schema).resolve(Geonet.File.EXTRACT_THUMBNAILS);

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
	@Override
	public void setThumbnail(ServiceContext context, String id, boolean small, String file, boolean indexAfterChange)
			throws Exception {
		int pos = file.lastIndexOf('.');
		String ext = (pos == -1) ? "???" : file.substring(pos + 1);

		Element env = new Element("env");
		env.addContent(new Element("file").setText(file));
		env.addContent(new Element("ext").setText(ext));

		String host = getSettingManager().getValue(Settings.SYSTEM_SERVER_HOST);
		String port = getSettingManager().getValue(Settings.SYSTEM_SERVER_PORT);
		String baseUrl = context.getBaseUrl();

		env.addContent(new Element("host").setText(host));
		env.addContent(new Element("port").setText(port));
		env.addContent(new Element("baseUrl").setText(baseUrl));
		// TODO: Remove host, port, baseUrl and simplify the
		// URL created in the XSLT. Keeping it for the time
		// as many profiles depend on it.
		env.addContent(new Element("url").setText(getSettingManager().getSiteURL(context)));

		manageThumbnail(context, id, small, env, Geonet.File.SET_THUMBNAIL, indexAfterChange);
	}

	/**
	 *
	 * @param context
	 * @param id
	 * @param small
	 * @throws Exception
	 */
	@Override
	public void unsetThumbnail(ServiceContext context, String id, boolean small, boolean indexAfterChange)
			throws Exception {
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
	private void manageThumbnail(ServiceContext context, String id, boolean small, Element env, String styleSheet,
			boolean indexAfterChange) throws Exception {
		boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = true;
		Element md = context.getBean(IMetadataManager.class).getMetadata(context, id, forEditing, withValidationErrors,
				keepXlinkAttributes);

		if (md == null)
			return;

		md.detach();

		String schema = metadataSchemaUtils.getMetadataSchema(id);

		// --- setup environment
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
	private void transformMd(ServiceContext context, String metadataId, Element md, Element env, String schema,
			String styleSheet, boolean indexAfterChange) throws Exception {

		if (env.getChild("host") == null) {
			String host = getSettingManager().getValue(Settings.SYSTEM_SERVER_HOST);
			String port = getSettingManager().getValue(Settings.SYSTEM_SERVER_PORT);
			env.addContent(new Element("host").setText(host));
			env.addContent(new Element("port").setText(port));
		}

		// --- setup root element
		Element root = new Element("root");
		root.addContent(md);
		root.addContent(env);

		// --- do an XSL transformation
		Path styleSheetPath = metadataSchemaUtils.getSchemaDir(schema).resolve(styleSheet);

		md = Xml.transform(root, styleSheetPath);
		String changeDate = null;
		String uuid = null;
		if (schemaManager.getSchema(schema).isReadwriteUUID()) {
			uuid = extractUUID(schema, md);
		}

		getXmlSerializer().update(metadataId, md, changeDate, true, uuid, context);

		if (indexAfterChange) {
			// Notifies the metadata change to metatada notifier service
			notifyMetadataChange(md, metadataId);

			// --- update search criteria
			metadataIndexer.indexMetadata(metadataId, true, null);
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
	@Override
	public void setDataCommons(ServiceContext context, String id, String licenseurl, String imageurl,
			String jurisdiction, String licensename, String type) throws Exception {
		Element env = prepareCommonsEnv(licenseurl, imageurl, jurisdiction, licensename, type);
		manageCommons(context, id, env, Geonet.File.SET_DATACOMMONS);
	}

	private Element prepareCommonsEnv(String licenseurl, String imageurl, String jurisdiction, String licensename,
			String type) {
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
	@Override
	public void setCreativeCommons(ServiceContext context, String id, String licenseurl, String imageurl,
			String jurisdiction, String licensename, String type) throws Exception {
		Element env = prepareCommonsEnv(licenseurl, imageurl, jurisdiction, licensename, type);
		manageCommons(context, id, env, Geonet.File.SET_CREATIVECOMMONS);
	}

	/**
	 * Extract the title field from the Metadata Repository. This is only valid for
	 * subtemplates as the title can be stored with the subtemplate (since
	 * subtemplates don't have a title) - metadata records don't store the title
	 * here as this is part of the metadata.
	 *
	 * @param id
	 *            metadata id to retrieve
	 */
	@Override
	public String getMetadataTitle(String id) throws Exception {
		AbstractMetadata md = findOne(id);

		if (md == null) {
			throw new IllegalArgumentException("Metadata not found for id : " + id);
		} else {
			// get metadata title
			return md.getDataInfo().getTitle();
		}
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
		Element md = getXmlSerializer().select(context, id);

		if (md == null)
			return;

		md.detach();

		String schema = metadataSchemaUtils.getMetadataSchema(id);
		transformMd(context, id, md, env, schema, styleSheet, true);
	}

	private XmlSerializer getXmlSerializer() {
		return xmlSerializer;
	}

	private SettingManager getSettingManager() {
		return this.settingManager;
	}

	@Override
	public long count(Specification<? extends AbstractMetadata> specs) {
		try {
			return metadataRepository.count((Specification<Metadata>) specs);
		} catch (Throwable t) {
			// Maybe it is not a Specification<Metadata>
		}
		throw new NotImplementedException("Unknown IMetadata subtype: " + specs.getClass().getName());
	}

	@Override
	public long count() {
		return metadataRepository.count();
	}

	@Override
	public AbstractMetadata findOne(int id) {
		return metadataRepository.findOne(id);
	}

	@Override
	public AbstractMetadata findOneByUuid(String uuid) {
		return metadataRepository.findOneByUuid(uuid);
	}

	@Override
	public AbstractMetadata findOne(Specification<? extends AbstractMetadata> spec) {
		try {
			return metadataRepository.findOne((Specification<Metadata>) spec);
		} catch (ClassCastException t) {
			throw new ClassCastException("Unknown AbstractMetadata subtype: " + spec.getClass().getName());
		}
	}

	@Override
	public AbstractMetadata findOne(String id) {
		return metadataRepository.findOne(id);
	}

	@Override
	public List<? extends AbstractMetadata> findAllByHarvestInfo_Uuid(String uuid) {
		return metadataRepository.findAllByHarvestInfo_Uuid(uuid);
	}

	@Override
	public Iterable<? extends AbstractMetadata> findAll(Set<Integer> keySet) {
		return metadataRepository.findAll(keySet);
	}

	@Override
	public List<? extends AbstractMetadata> findAll(Specification<? extends AbstractMetadata> specs) {
		try {
			return metadataRepository.findAll((Specification<Metadata>) specs);
		} catch (Throwable t) {
			// Maybe it is not a Specification<Metadata>
		}
		throw new NotImplementedException("Unknown AbstractMetadata subtype: " + specs.getClass().getName());
	}

	@Override
	public List<SimpleMetadata> findAllSimple(String harvestUuid) {
		return metadataRepository.findAllSimple(harvestUuid);
	}

	@Override
	public boolean exists(Integer iId) {
		return metadataRepository.exists(iId);
	}
	
	@Override
	public Element findAllAsXml(Specification<? extends AbstractMetadata> specs, Sort sortByChangeDateDesc) {
		try {
			return metadataRepository.findAllAsXml((Specification<Metadata>) specs, sortByChangeDateDesc);
			
		} catch (Throwable t) {
			// Maybe it is not a Specification<Metadata>
		}
		throw new NotImplementedException("Unknown AbstractMetadata subtype: " + specs.getClass().getName());
	}

	@Override
	public MetadataReportsQueries getMetadataReports() {
		return metadataRepository.getMetadataReports();
	}

	@Override
	public Element findAllAsXml(@Nullable Specification<? extends AbstractMetadata> specs,
			@Nullable Pageable pageable) {
		try {
			return metadataRepository.findAllAsXml((Specification<Metadata>) specs, pageable);
		} catch (Throwable t) {
			// Maybe it is not a Specification<Metadata>
		}
		throw new NotImplementedException("Unknown AbstractMetadata subtype: " + specs.getClass().getName());
	}

	protected MetadataRepository getMetadataRepository() {
		return metadataRepository;
	}

	protected SchemaManager getSchemaManager() {
		return schemaManager;
	}

	@Override
	public boolean checkMetadataWithSameUuidExist(String uuid, int id) {
		// Check if another record exist with that UUID
		Metadata recordWithThatUuid = getMetadataRepository().findOneByUuid(uuid);
		if (recordWithThatUuid != null && recordWithThatUuid.getId() != id) {
			// If yes, this would have triggered a DataIntegrityViolationException
			throw new IllegalArgumentException(String.format(
					"Another record exist with UUID '%s'. This record as internal id '%d'. The record you're trying to update with id '%d' can not be saved.",
					uuid, recordWithThatUuid.getId(), id));
		}
		return false;
	}

	@Override
	public Page<Pair<Integer, ISODate>> findAllIdsAndChangeDates(Pageable pageable) {
		return metadataRepository.findAllIdsAndChangeDates(pageable);
	}

	@Override
	public Map<Integer, MetadataSourceInfo> findAllSourceInfo(Specification<? extends AbstractMetadata> spec) {
		try {
			return metadataRepository.findAllSourceInfo((Specification<Metadata>) spec);
		} catch (Throwable t) {
			// Maybe it is not a Specification<Metadata>
		}
		throw new NotImplementedException("Unknown AbstractMetadata subtype: " + spec.getClass().getName());
	}
}
