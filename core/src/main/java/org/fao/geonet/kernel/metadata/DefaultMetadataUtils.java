/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import static org.fao.geonet.repository.specification.MetadataSpecs.hasMetadataUuid;
import static org.springframework.data.jpa.domain.Specifications.where;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.domain.MetadataHarvestInfo;
import org.fao.geonet.domain.MetadataRatingByIp;
import org.fao.geonet.domain.MetadataRatingByIpId;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.SvnManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.index.IndexingList;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.notifier.MetadataNotifierManager;
import org.fao.geonet.repository.MetadataRatingByIpRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.MetadataStatusSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.repository.specification.UserSpecs;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.TransactionStatus;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import jeeves.server.context.ServiceContext;
import jeeves.transaction.TransactionManager;
import jeeves.transaction.TransactionTask;

/**
 * trunk-core
 * 
 * @author delawen
 * 
 * 
 */
public class DefaultMetadataUtils implements IMetadataUtils {
    @PersistenceContext
    private EntityManager _entityManager;

    @Autowired
    private IMetadataIndexer metadataIndexer;

    @Autowired
    private IMetadataSchemaUtils metadataSchemaUtils;

    @Autowired
    private IMetadataManager metadataManager;

    @Autowired
    private MetadataRepository mdRepository;

    @Autowired
    private MetadataStatusRepository mdStatusRepository;

    @Autowired
    private MetadataRatingByIpRepository ratingByIpRepository;

    @Autowired
    private SearchManager searchManager;

    @Autowired(required = false)
    private SvnManager svnManager;

    @Autowired
    private GeonetworkDataDirectory dataDirectory;

    private SchemaManager schemaManager;

    private EditLib editLib;

    /**
     * @param context
     */
    @Override
    public void init(ServiceContext context) {
        this.metadataIndexer = context.getBean(IMetadataIndexer.class);
        this.metadataSchemaUtils = context.getBean(IMetadataSchemaUtils.class);
        this.metadataManager = context.getBean(IMetadataManager.class);
        this.mdRepository = context.getBean(MetadataRepository.class);
        this.mdStatusRepository = context.getBean(MetadataStatusRepository.class);
        this.ratingByIpRepository = context.getBean(MetadataRatingByIpRepository.class);
        this.searchManager = context.getBean(SearchManager.class);
        this.svnManager = context.getBean(SvnManager.class);
        this.dataDirectory = context.getBean(GeonetworkDataDirectory.class);
        this.setSchemaManager(context.getBean(SchemaManager.class));
    }

    /**
     * @param schemaManager
     *            the schemaManager to set
     */
    @Autowired
    public void setSchemaManager(SchemaManager schemaManager) {
        this.schemaManager = schemaManager;
        this.editLib = new EditLib(this.schemaManager);
    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#extractUUID(java.lang.String,
     *      org.jdom.Element)
     * @param schema
     * @param md
     * @return
     * @throws Exception
     * @deprecated
     */
    @Override
    public String extractUUID(String schema, Element md) throws Exception {
        Path styleSheet = metadataSchemaUtils.getSchemaDir(schema)
                .resolve(Geonet.File.EXTRACT_UUID);
        String uuid = Xml.transform(md, styleSheet).getText().trim();

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Extracted UUID '" + uuid
                    + "' for schema '" + schema + "'");

        // --- needed to detach md from the document
        md.detach();

        return uuid;
    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#extractDateModified(java.lang.String,
     *      org.jdom.Element)
     * @param schema
     * @param md
     * @return
     * @throws Exception
     */
    @Override
    public String extractDateModified(String schema, Element md)
            throws Exception {
        Path styleSheet = metadataSchemaUtils.getSchemaDir(schema)
                .resolve(Geonet.File.EXTRACT_DATE_MODIFIED);
        String dateMod = Xml.transform(md, styleSheet).getText().trim();

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Extracted Date Modified '" + dateMod
                    + "' for schema '" + schema + "'");

        // --- needed to detach md from the document
        md.detach();

        return dateMod;
    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#setUUID(java.lang.String,
     *      java.lang.String, org.jdom.Element)
     * @param schema
     * @param uuid
     * @param md
     * @return
     * @throws Exception
     */
    @Override
    public Element setUUID(String schema, String uuid, Element md)
            throws Exception {
        // --- setup environment

        Element env = new Element("env");
        env.addContent(new Element("uuid").setText(uuid));

        // --- setup root element

        Element root = new Element("root");
        root.addContent(md.detach());
        root.addContent(env.detach());

        // --- do an XSL transformation

        Path styleSheet = metadataSchemaUtils.getSchemaDir(schema)
                .resolve(Geonet.File.SET_UUID);

        return Xml.transform(root, styleSheet);
    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#extractSummary(org.jdom.Element)
     * @param md
     * @return
     * @throws Exception
     */
    @Override
    public Element extractSummary(Element md) throws Exception {

        Path stylePath = dataDirectory
                .resolveWebResource(Geonet.Path.STYLESHEETS);
        Path styleSheet = stylePath.resolve(Geonet.File.METADATA_BRIEF);
        Element summary = Xml.transform(md, styleSheet);
        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER,
                    "Extracted summary '\n" + Xml.getString(summary));

        // --- needed to detach md from the document
        md.detach();

        return summary;
    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#getMetadataId(java.lang.String)
     * @param uuid
     * @return
     * @throws Exception
     */
    @Override
    public @Nullable String getMetadataId(@Nonnull String uuid)
            throws Exception {
        final List<Integer> idList = mdRepository
                .findAllIdsBy(hasMetadataUuid(uuid));
        if (idList.isEmpty()) {
            return null;
        }
        return String.valueOf(idList.get(0));
    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#getMetadataUuid(java.lang.String)
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public @Nullable String getMetadataUuid(@Nonnull String id)
            throws Exception {
        Metadata metadata = mdRepository.findOne(id);

        if (metadata == null)
            return null;

        return metadata.getUuid();
    }

    /**
     *
     * @param id
     * @return
     */
    @Override
    public String getVersion(String id) {
        return editLib.getVersion(id);
    }

    /**
     *
     * @param id
     * @return
     */
    @Override
    public String getNewVersion(String id) {
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
    @Override
    public void setTemplate(final int id, final MetadataType type,
            final String title) throws Exception {
        setTemplateExt(id, type);
        metadataIndexer.indexMetadata(Integer.toString(id), true);
    }

    /**
     * TODO javadoc.
     *
     * @param id
     * @throws Exception
     */
    @Override
    public void setTemplateExt(final int id, final MetadataType metadataType)
            throws Exception {
        mdRepository.update(id, new Updater<Metadata>() {
            @Override
            public void apply(@Nonnull Metadata metadata) {
                final MetadataDataInfo dataInfo = metadata.getDataInfo();
                dataInfo.setType(metadataType);
            }
        });
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#setHarvested(int,
     *      java.lang.String)
     * @param id
     * @param harvestUuid
     * @throws Exception
     */
    @Override
    public void setHarvested(int id, String harvestUuid) throws Exception {
        setHarvestedExt(id, harvestUuid);
        metadataIndexer.indexMetadata(Integer.toString(id), true);
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#setHarvestedExt(int,
     *      java.lang.String)
     * @param id
     * @param harvestUuid
     * @throws Exception
     */
    @Override
    public void setHarvestedExt(int id, String harvestUuid) throws Exception {
        setHarvestedExt(id, harvestUuid, Optional.<String> absent());
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#setHarvestedExt(int,
     *      java.lang.String, com.google.common.base.Optional)
     * @param id
     * @param harvestUuid
     * @param harvestUri
     * @throws Exception
     */
    @Override
    public void setHarvestedExt(final int id, final String harvestUuid,
            final Optional<String> harvestUri) throws Exception {
        mdRepository.update(id, new Updater<Metadata>() {
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
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#updateDisplayOrder(java.lang.String,
     *      java.lang.String)
     * @param id
     * @param displayOrder
     * @throws Exception
     */
    @Override
    public void updateDisplayOrder(final String id, final String displayOrder)
            throws Exception {
        mdRepository.update(Integer.valueOf(id), new Updater<Metadata>() {
            @Override
            public void apply(Metadata entity) {
                entity.getDataInfo()
                        .setDisplayOrder(Integer.parseInt(displayOrder));
            }
        });
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#increasePopularity(jeeves.server.context.ServiceContext,
     *      java.lang.String)
     * @param srvContext
     * @param id
     * @throws Exception
     */
    @Override
    public void increasePopularity(ServiceContext srvContext, String id)
            throws Exception {
        // READONLYMODE
        if (!srvContext.getBean(NodeInfo.class).isReadOnly()) {
            // Update the popularity in database
            int iId = Integer.parseInt(id);
            mdRepository.incrementPopularity(iId);
            _entityManager.flush();
            _entityManager.clear();

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
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#rateMetadata(int,
     *      java.lang.String, int)
     * @param metadataId
     * @param ipAddress
     * @param rating
     * @return
     * @throws Exception
     */
    @Override
    public int rateMetadata(final int metadataId, final String ipAddress,
            final int rating) throws Exception {
        MetadataRatingByIp ratingEntity = new MetadataRatingByIp();
        ratingEntity.setRating(rating);
        ratingEntity.setId(new MetadataRatingByIpId(metadataId, ipAddress));

        ratingByIpRepository.save(ratingEntity);

        //
        // calculate new rating
        //
        final int newRating = ratingByIpRepository.averageRating(metadataId);

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Setting rating for id:" + metadataId
                    + " --> rating is:" + newRating);

        mdRepository.update(metadataId, new Updater<Metadata>() {
            @Override
            public void apply(Metadata entity) {
                entity.getDataInfo().setRating(newRating);
            }
        });

        metadataIndexer.indexMetadata(Integer.toString(metadataId), true);

        return rating;
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#versionMetadata(jeeves.server.context.ServiceContext,
     *      java.lang.String, org.jdom.Element)
     * @param context
     * @param id
     * @param md
     * @throws Exception
     */
    @Override
    public void versionMetadata(ServiceContext context, String id, Element md)
            throws Exception {
        if (svnManager != null) {
            svnManager.createMetadataDir(id, context, md);
        }
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#enumerateTree(org.jdom.Element)
     * @param md
     * @return
     * @throws Exception
     */
    @Override
    public Element enumerateTree(Element md) throws Exception {
        editLib.enumerateTree(md);
        return md;
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#getKeywords()
     * @return
     * @throws Exception
     */
    @Override
    public Element getKeywords() throws Exception {
        Collection<String> keywords = searchManager.getTerms("keyword");
        Element el = new Element("keywords");

        for (Object keyword : keywords) {
            el.addContent(new Element("keyword").setText((String) keyword));
        }
        return el;
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#getThumbnails(jeeves.server.context.ServiceContext,
     *      java.lang.String)
     * @param context
     * @param metadataId
     * @return
     * @throws Exception
     */
    @Override
    public Element getThumbnails(ServiceContext context, String metadataId)
            throws Exception {
        Element md = context.getBean(XmlSerializer.class).select(context,
                metadataId);

        if (md == null)
            return null;

        md.detach();

        String schema = metadataSchemaUtils.getMetadataSchema(metadataId);

        // --- do an XSL transformation
        Path styleSheet = metadataSchemaUtils.getSchemaDir(schema)
                .resolve(Geonet.File.EXTRACT_THUMBNAILS);

        Element result = Xml.transform(md, styleSheet);
        result.addContent(new Element("id").setText(metadataId));

        return result;
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#setThumbnail(jeeves.server.context.ServiceContext,
     *      java.lang.String, boolean, java.lang.String, boolean)
     * @param context
     * @param id
     * @param small
     * @param file
     * @param indexAfterChange
     * @throws Exception
     */
    @Override
    public void setThumbnail(ServiceContext context, String id, boolean small,
            String file, boolean indexAfterChange) throws Exception {
        int pos = file.lastIndexOf('.');
        String ext = (pos == -1) ? "???" : file.substring(pos + 1);

        Element env = new Element("env");
        env.addContent(new Element("file").setText(file));
        env.addContent(new Element("ext").setText(ext));

        SettingManager sm = context.getBean(SettingManager.class);

        String host = sm.getValue(Geonet.Settings.SERVER_HOST);
        String port = sm.getValue(Geonet.Settings.SERVER_PORT);
        String baseUrl = context.getBaseUrl();

        env.addContent(new Element("host").setText(host));
        env.addContent(new Element("port").setText(port));
        env.addContent(new Element("baseUrl").setText(baseUrl));
        // TODO: Remove host, port, baseUrl and simplify the
        // URL created in the XSLT. Keeping it for the time
        // as many profiles depend on it.
        env.addContent(new Element("url").setText(sm.getSiteURL(context)));

        manageThumbnail(context, id, small, env, Geonet.File.SET_THUMBNAIL,
                indexAfterChange);
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#unsetThumbnail(jeeves.server.context.ServiceContext,
     *      java.lang.String, boolean, boolean)
     * @param context
     * @param id
     * @param small
     * @param indexAfterChange
     * @throws Exception
     */
    @Override
    public void unsetThumbnail(ServiceContext context, String id, boolean small,
            boolean indexAfterChange) throws Exception {
        Element env = new Element("env");

        manageThumbnail(context, id, small, env, Geonet.File.UNSET_THUMBNAIL,
                indexAfterChange);
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
    private void manageThumbnail(ServiceContext context, String id,
            boolean small, Element env, String styleSheet,
            boolean indexAfterChange) throws Exception {
        boolean forEditing = false, withValidationErrors = false,
                keepXlinkAttributes = true;
        Element md = metadataManager.getMetadata(context, id, forEditing,
                withValidationErrors, keepXlinkAttributes);

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
    private void transformMd(ServiceContext context, String metadataId,
            Element md, Element env, String schema, String styleSheet,
            boolean indexAfterChange) throws Exception {
        SettingManager sm = context.getBean(SettingManager.class);

        if (env.getChild("host") == null) {
            String host = sm.getValue(Geonet.Settings.SERVER_HOST);
            String port = sm.getValue(Geonet.Settings.SERVER_PORT);

            env.addContent(new Element("host").setText(host));
            env.addContent(new Element("port").setText(port));
        }

        // --- setup root element
        Element root = new Element("root");
        root.addContent(md);
        root.addContent(env);

        // --- do an XSL transformation
        Path styleSheetPath = metadataSchemaUtils.getSchemaDir(schema)
                .resolve(styleSheet);

        md = Xml.transform(root, styleSheetPath);
        String changeDate = null;
        String uuid = null;
        if (metadataSchemaUtils.getSchema(schema).isReadwriteUUID()) {
            uuid = extractUUID(schema, md);
        }

        context.getBean(XmlSerializer.class).update(metadataId, md, changeDate,
                true, uuid, context);

        if (indexAfterChange) {
            // Notifies the metadata change to metatada notifier service
            notifyMetadataChange(md, metadataId);

            // --- update search criteria
            metadataIndexer.indexMetadata(metadataId, true);
        }
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#setDataCommons(jeeves.server.context.ServiceContext,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
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
    public void setDataCommons(ServiceContext context, String id,
            String licenseurl, String imageurl, String jurisdiction,
            String licensename, String type) throws Exception {
        Element env = prepareCommonsEnv(licenseurl, imageurl, jurisdiction,
                licensename, type);
        manageCommons(context, id, env, Geonet.File.SET_DATACOMMONS);
    }

    private Element prepareCommonsEnv(String licenseurl, String imageurl,
            String jurisdiction, String licensename, String type) {
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
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#setCreativeCommons(jeeves.server.context.ServiceContext,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
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
    public void setCreativeCommons(ServiceContext context, String id,
            String licenseurl, String imageurl, String jurisdiction,
            String licensename, String type) throws Exception {
        Element env = prepareCommonsEnv(licenseurl, imageurl, jurisdiction,
                licensename, type);
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
    private void manageCommons(ServiceContext context, String id, Element env,
            String styleSheet) throws Exception {
        Lib.resource.checkEditPrivilege(context, id);
        Element md = context.getBean(XmlSerializer.class).select(context, id);

        if (md == null)
            return;

        md.detach();

        String schema = metadataSchemaUtils.getMetadataSchema(id);
        transformMd(context, id, md, env, schema, styleSheet, true);
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#isUserMetadataOwner(int)
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public boolean isUserMetadataOwner(int userId) throws Exception {
        return mdRepository.count(MetadataSpecs.isOwnedByUser(userId)) > 0;
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#isUserMetadataStatus(int)
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public boolean isUserMetadataStatus(int userId) throws Exception {

        return mdStatusRepository
                .count(MetadataStatusSpecs.hasUserId(userId)) > 0;
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#existsUser(jeeves.server.context.ServiceContext,
     *      int)
     * @param context
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public boolean existsUser(ServiceContext context, int id) throws Exception {
        return context.getBean(UserRepository.class)
                .count(where(UserSpecs.hasUserId(id))) > 0;
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#updateChildren(jeeves.server.context.ServiceContext,
     *      java.lang.String, java.lang.String[], java.util.Map)
     * @param srvContext
     * @param parentUuid
     * @param children
     * @param params
     * @return
     * @throws Exception
     */
    @Override
    public Set<String> updateChildren(ServiceContext srvContext,
            String parentUuid, String[] children, Map<String, Object> params)
                    throws Exception {
        String parentId = (String) params.get(Params.ID);
        String parentSchema = (String) params.get(Params.SCHEMA);

        // --- get parent metadata in read/only mode
        boolean forEditing = false, withValidationErrors = false,
                keepXlinkAttributes = false;
        Element parent = metadataManager.getMetadata(srvContext, parentId,
                forEditing, withValidationErrors, keepXlinkAttributes);

        Element env = new Element("update");
        env.addContent(new Element("parentUuid").setText(parentUuid));
        env.addContent(new Element("siteURL").setText(srvContext
                .getBean(SettingManager.class).getSiteURL(srvContext)));
        env.addContent(new Element("parent").addContent(parent));

        // Set of untreated children (out of privileges, different schemas)
        Set<String> untreatedChildSet = new HashSet<String>();

        // only get iso19139 records
        for (String childId : children) {

            // Check privileges
            if (!srvContext.getBean(AccessManager.class).canEdit(srvContext,
                    childId)) {
                untreatedChildSet.add(childId);
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, "Could not update child ("
                            + childId + ") because of privileges.");
                continue;
            }

            Element child = metadataManager.getMetadata(srvContext, childId,
                    forEditing, withValidationErrors, keepXlinkAttributes);

            String childSchema = child
                    .getChild(Edit.RootChild.INFO, Edit.NAMESPACE)
                    .getChildText(Edit.Info.Elem.SCHEMA);

            // Check schema matching. CHECKME : this suppose that parent and
            // child are in the same schema (even not profil different)
            if (!childSchema.equals(parentSchema)) {
                untreatedChildSet.add(childId);
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                    Log.debug(Geonet.DATA_MANAGER,
                            "Could not update child (" + childId
                                    + ") because schema (" + childSchema
                                    + ") is different from the parent one ("
                                    + parentSchema + ").");
                }
                continue;
            }

            if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER,
                        "Updating child (" + childId + ") ...");

            // --- setup xml element to be processed by XSLT

            Element rootEl = new Element("root");
            Element childEl = new Element("child").addContent(child.detach());
            rootEl.addContent(childEl);
            rootEl.addContent(env.detach());

            // --- do an XSL transformation

            Path styleSheet = metadataSchemaUtils.getSchemaDir(parentSchema)
                    .resolve(Geonet.File.UPDATE_CHILD_FROM_PARENT_INFO);
            Element childForUpdate = Xml.transform(rootEl, styleSheet, params);

            srvContext.getBean(XmlSerializer.class).update(childId,
                    childForUpdate, new ISODate().toString(), true, null,
                    srvContext);

            // Notifies the metadata change to metatada notifier service
            notifyMetadataChange(childForUpdate, childId);

            rootEl = null;
        }

        return untreatedChildSet;
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#buildPrivilegesMetadataInfo(jeeves.server.context.ServiceContext,
     *      java.util.Map)
     * @param context
     * @param mdIdToInfoMap
     * @throws Exception
     */
    @Override
    public void buildPrivilegesMetadataInfo(ServiceContext context,
            Map<String, Element> mdIdToInfoMap) throws Exception {
        Collection<Integer> metadataIds = Collections2.transform(
                mdIdToInfoMap.keySet(), new Function<String, Integer>() {
                    @Nullable
                    @Override
                    public Integer apply(String input) {
                        return Integer.valueOf(input);
                    }
                });
        Specification<OperationAllowed> operationAllowedSpec = OperationAllowedSpecs
                .hasMetadataIdIn(metadataIds);

        final Collection<Integer> allUserGroups = context
                .getBean(AccessManager.class)
                .getUserGroups(context.getUserSession(), context.getIpAddress(),
                        false);
        final SetMultimap<Integer, ReservedOperation> operationsPerMetadata = loadOperationsAllowed(
                context, where(operationAllowedSpec).and(
                        OperationAllowedSpecs.hasGroupIdIn(allUserGroups)));
        final Set<Integer> visibleToAll = loadOperationsAllowed(context,
                where(operationAllowedSpec).and(
                        OperationAllowedSpecs.isPublic(ReservedOperation.view)))
                                .keySet();
        final Set<Integer> downloadableByGuest = loadOperationsAllowed(context,
                where(operationAllowedSpec)
                        .and(OperationAllowedSpecs
                                .hasGroupId(ReservedGroup.guest.getId()))
                        .and(OperationAllowedSpecs
                                .hasOperation(ReservedOperation.download)))
                                        .keySet();
        final Map<Integer, MetadataSourceInfo> allSourceInfo = mdRepository
                .findAllSourceInfo(MetadataSpecs.hasMetadataIdIn(metadataIds));

        for (Map.Entry<String, Element> entry : mdIdToInfoMap.entrySet()) {
            Element infoEl = entry.getValue();
            final Integer mdId = Integer.valueOf(entry.getKey());
            MetadataSourceInfo sourceInfo = allSourceInfo.get(mdId);
            Set<ReservedOperation> operations = operationsPerMetadata.get(mdId);
            if (operations == null) {
                operations = Collections.emptySet();
            }

            boolean isOwner = context.getBean(AccessManager.class)
                    .isOwner(context, sourceInfo);

            if (isOwner) {
                operations = Sets
                        .newHashSet(Arrays.asList(ReservedOperation.values()));
            }

            if (isOwner || operations.contains(ReservedOperation.editing)) {
                addElement(infoEl, Edit.Info.Elem.EDIT, "true");
            }

            if (isOwner) {
                addElement(infoEl, Edit.Info.Elem.OWNER, "true");
            }

            addElement(infoEl, Edit.Info.Elem.IS_PUBLISHED_TO_ALL,
                    visibleToAll.contains(mdId));
            addElement(infoEl, ReservedOperation.view.name(),
                    operations.contains(ReservedOperation.view));
            addElement(infoEl, ReservedOperation.notify.name(),
                    operations.contains(ReservedOperation.notify));
            addElement(infoEl, ReservedOperation.download.name(),
                    operations.contains(ReservedOperation.download));
            addElement(infoEl, ReservedOperation.dynamic.name(),
                    operations.contains(ReservedOperation.dynamic));
            addElement(infoEl, ReservedOperation.featured.name(),
                    operations.contains(ReservedOperation.featured));

            if (!operations.contains(ReservedOperation.download)) {
                addElement(infoEl, Edit.Info.Elem.GUEST_DOWNLOAD,
                        downloadableByGuest.contains(mdId));
            }
        }
    }

    private static void addElement(Element root, String name, Object value) {
        root.addContent(new Element(name)
                .setText(value == null ? "" : value.toString()));
    }

    private SetMultimap<Integer, ReservedOperation> loadOperationsAllowed(
            ServiceContext context,
            Specification<OperationAllowed> operationAllowedSpec) {
        final OperationAllowedRepository operationAllowedRepo = context
                .getBean(OperationAllowedRepository.class);
        List<OperationAllowed> operationsAllowed = operationAllowedRepo
                .findAll(operationAllowedSpec);
        SetMultimap<Integer, ReservedOperation> operationsPerMetadata = HashMultimap
                .create();
        for (OperationAllowed allowed : operationsAllowed) {
            final OperationAllowedId id = allowed.getId();
            operationsPerMetadata.put(id.getMetadataId(),
                    ReservedOperation.lookup(id.getOperationId()));
        }
        return operationsPerMetadata;
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#notifyMetadataChange(org.jdom.Element,
     *      java.lang.String)
     * @param md
     * @param metadataId
     * @throws Exception
     */
    @Override
    public void notifyMetadataChange(Element md, String metadataId)
            throws Exception {

        final Metadata metadata = mdRepository.findOne(metadataId);
        if (metadata != null
                && metadata.getDataInfo().getType() == MetadataType.METADATA) {
            MetadataSchema mds = metadataSchemaUtils
                    .getSchema(metadata.getDataInfo().getSchemaId());
            Pair<String, Element> editXpathFilter = mds
                    .getOperationFilter(ReservedOperation.editing);
            XmlSerializer.removeFilteredElement(md, editXpathFilter,
                    mds.getNamespaces());

            String uuid = getMetadataUuid(metadataId);
            ServiceContext.get().getBean(MetadataNotifierManager.class)
                    .updateMetadata(md, metadataId, uuid, ServiceContext.get());
        }
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataUtils#flush()
     */
    @Override
    public void flush() {
        TransactionManager.runInTransaction("DataManager flush()",
                ApplicationContextHolder.get(),
                TransactionManager.TransactionRequirement.CREATE_ONLY_WHEN_NEEDED,
                TransactionManager.CommitBehavior.ALWAYS_COMMIT, false,
                new TransactionTask<Object>() {
                    @Override
                    public Object doInTransaction(TransactionStatus transaction)
                            throws Throwable {
                        _entityManager.flush();
                        return null;
                    }
                });

    }
}
