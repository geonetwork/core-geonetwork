/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import static org.fao.geonet.repository.specification.MetadataSpecs.hasMetadataUuid;
import static org.springframework.data.jpa.domain.Specifications.where;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Geonet.Namespaces;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.IMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.domain.MetadataDataInfo_;
import org.fao.geonet.domain.MetadataFileUpload;
import org.fao.geonet.domain.MetadataFileUpload_;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.Metadata_;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.User;
import org.fao.geonet.exceptions.MetadataLockedException;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.HarvestInfoProvider;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.notifier.MetadataNotifierManager;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataFileUploadRepository;
import org.fao.geonet.repository.MetadataLockRepository;
import org.fao.geonet.repository.MetadataRatingByIpRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.UserSavedSelectionRepository;
import org.fao.geonet.repository.specification.MetadataFileUploadSpecs;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.repository.statistic.PathSpec;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

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
public class DefaultMetadataManager implements IMetadataManager {
    protected static final int METADATA_BATCH_PAGE_SIZE = 100000;

    @Autowired
    protected IMetadataSchemaUtils metadataSchemaUtils;

    @Autowired
    private IMetadataUtils metadataUtils;

    @Autowired
    private IMetadataValidator metadataValidator;

    @Autowired
    private IMetadataIndexer metadataIndexer;

    @Autowired
    protected IMetadataOperations metadataOperations;

    @Autowired
    protected MetadataRepository mdRepository;

    @Autowired
    protected MetadataLockRepository mdLockRepository;

    protected SchemaManager schemaManager;

    @Autowired
    protected GroupRepository groupRepository;

    @Autowired
    private MetadataRatingByIpRepository mdRatingByIpRepository;

    @Autowired
    private MetadataStatusRepository mdStatusRepository;

    @Autowired
    private MetadataFileUploadRepository mdFileUploadRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected UserSavedSelectionRepository userSavedSelectionRepository;
    
    @Autowired
    private MetadataCategoryRepository mdCatRepository;

    @Autowired
    protected MetadataValidationRepository mdValidationRepository;

    @Autowired
    private OperationAllowedRepository operationAllowedRepository;

    private EditLib editLib;
    
    @Autowired
    private SearchManager searchManager;

    @Autowired
    private MetadataNotifierManager metadataNotifierManager;
    
    /**
     * @param context
     */
    @Override
    public void init(ServiceContext context) {
        this.metadataSchemaUtils = context.getBean(IMetadataSchemaUtils.class);
        this.metadataUtils = context.getBean(IMetadataUtils.class);
        this.metadataIndexer = context.getBean(IMetadataIndexer.class);
        this.metadataOperations = context.getBean(IMetadataOperations.class);
        this.mdRepository = context.getBean(MetadataRepository.class);
        this.groupRepository = context.getBean(GroupRepository.class);
        this.mdRatingByIpRepository = context
                .getBean(MetadataRatingByIpRepository.class);
        this.mdStatusRepository = context
                .getBean(MetadataStatusRepository.class);
        this.mdFileUploadRepository = context
                .getBean(MetadataFileUploadRepository.class);
        this.userRepository = context.getBean(UserRepository.class);
        this.userSavedSelectionRepository = context.getBean(UserSavedSelectionRepository.class);
        this.mdCatRepository = context
                .getBean(MetadataCategoryRepository.class);
        this.mdValidationRepository = context
                .getBean(MetadataValidationRepository.class);
        this.operationAllowedRepository = context
                .getBean(OperationAllowedRepository.class);
        this.mdLockRepository = context.getBean(MetadataLockRepository.class);
        this.setSchemaManager(context.getBean(SchemaManager.class));
        this.searchManager = context.getBean(SearchManager.class);
        this.metadataNotifierManager = context.getBean(MetadataNotifierManager.class);
    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#getEditLib()
     * @return
     */
    @Override
    public EditLib getEditLib() {
        return editLib;
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
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#startEditingSession(jeeves.server.context.ServiceContext,
     *      java.lang.String)
     * @param context
     * @param id
     * @throws Exception
     */
    @Override
    public String startEditingSession(ServiceContext context, String id, Boolean lock)
            throws Exception {
        if (Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
            Log.debug(Geonet.EDITOR_SESSION,
                    "Editing session starts for record " + id);
        }
        
        UserSession userSession = context.getUserSession();
        
        if(lock) {
            synchronized (this) {
                if(mdLockRepository.isLocked(id, userSession.getPrincipal())) {
                    throw new MetadataLockedException(id);            
                }
                mdLockRepository.lock(id, userSession.getPrincipal());            
            }
        }

        boolean keepXlinkAttributes = true;
        boolean forEditing = false;
        boolean withValidationErrors = false;
        Element metadataBeforeAnyChanges = getMetadata(context, id, forEditing,
                withValidationErrors, keepXlinkAttributes);
        
        //Check
        if(metadataBeforeAnyChanges == null) {
          throw new RuntimeException("We are trying to edit a metadata that doesn't exist! id:" + id);
        }
        
        userSession.setProperty(
                Geonet.Session.METADATA_BEFORE_ANY_CHANGES + id,
                metadataBeforeAnyChanges);

        return id;
    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#cancelEditingSession(jeeves.server.context.ServiceContext,
     *      java.lang.String)
     * @param context
     * @param id
     * @throws Exception
     */
    @Override
    public void cancelEditingSession(ServiceContext context, String id)
            throws Exception {
        UserSession session = context.getUserSession();
        Element metadataBeforeAnyChanges = (Element) session
                .getProperty(Geonet.Session.METADATA_BEFORE_ANY_CHANGES + id);

        if (Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
            Log.debug(Geonet.EDITOR_SESSION,
                    "Editing session end. Cancel changes. Restore record " + id
                            + ". Replace by original record which was: ");
        }

        if (metadataBeforeAnyChanges != null) {
            if (Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
                Log.debug(Geonet.EDITOR_SESSION, " > restoring record: ");
                Log.debug(Geonet.EDITOR_SESSION,
                        Xml.getString(metadataBeforeAnyChanges));
            }
            Element info = metadataBeforeAnyChanges
                    .getChild(Edit.RootChild.INFO, Edit.NAMESPACE);
            boolean validate = false;
            boolean ufo = false;
            boolean index = true;
            metadataBeforeAnyChanges.removeChild(Edit.RootChild.INFO,
                    Edit.NAMESPACE);
            updateMetadata(context, id, metadataBeforeAnyChanges, validate, ufo,
                    index, context.getLanguage(),
                    info.getChildText(Edit.Info.Elem.CHANGE_DATE), false);
            endEditingSession(id, session);
        } else {
            if (Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
                Log.debug(Geonet.EDITOR_SESSION,
                        " > nothing to cancel for record " + id
                                + ". Original record was null. Use starteditingsession to.");
            }
        }
        
        mdLockRepository.unlock(id, session.getPrincipal());

    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#endEditingSession(java.lang.String,
     *      jeeves.server.UserSession)
     * @param id
     * @param session
     */
    @Override
    public void endEditingSession(String id, UserSession session) {
        if (Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
            Log.debug(Geonet.EDITOR_SESSION, "Editing session end.");
        }
        session.removeProperty(Geonet.Session.METADATA_BEFORE_ANY_CHANGES + id);
        
        mdLockRepository.unlock(id, session.getPrincipal());
    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#createMetadata(jeeves.server.context.ServiceContext,
     *      java.lang.String, java.lang.String, java.lang.String, int,
     *      java.lang.String, java.lang.String, boolean, java.lang.String)
     * @param context
     * @param templateId
     * @param groupOwner
     * @param source
     * @param owner
     * @param parentUuid
     * @param isTemplate
     * @param fullRightsForGroup
     * @param uuid
     * @return
     * @throws Exception
     */
    @Override
    public String createMetadata(ServiceContext context, String templateId,
            String groupOwner, String source, int owner, String parentUuid,
            String isTemplate, boolean fullRightsForGroup, String uuid)
                    throws Exception {
        Metadata templateMetadata = mdRepository.findOne(templateId);
        if (templateMetadata == null) {
            throw new IllegalArgumentException(
                    "Template id not found : " + templateId);
        }

        String schema = templateMetadata.getDataInfo().getSchemaId();
        String data = templateMetadata.getData();
        Element xml = Xml.loadString(data, false);
        if (templateMetadata.getDataInfo().getType() == MetadataType.METADATA) {
            xml = updateFixedInfo(schema, Optional.<Integer> absent(), uuid,
                    xml, parentUuid, UpdateDatestamp.NO, context);
        }
        final Metadata newMetadata = new Metadata();
        newMetadata.setUuid(uuid);
        newMetadata.getDataInfo().setChangeDate(new ISODate())
                .setCreateDate(new ISODate()).setSchemaId(schema)
                .setType(MetadataType.lookup(isTemplate));
        newMetadata.getSourceInfo().setGroupOwner(Integer.valueOf(groupOwner))
                .setOwner(owner).setSourceId(source);

        // If there is a default category for the group, use it:
        Group group = groupRepository.findOne(Integer.valueOf(groupOwner));
        if (group.getDefaultCategory() != null) {
            newMetadata.getMetadataCategories().add(group.getDefaultCategory());
        }
        Collection<MetadataCategory> filteredCategories = Collections2.filter(
                templateMetadata.getMetadataCategories(),
                new Predicate<MetadataCategory>() {
                    @Override
                    public boolean apply(@Nullable MetadataCategory input) {
                        return input != null;
                    }
                });

        newMetadata.getMetadataCategories().addAll(filteredCategories);

        int finalId = insertMetadata(context, newMetadata, xml, false, true,
                true, UpdateDatestamp.YES, fullRightsForGroup, true).getId();

        return String.valueOf(finalId);
    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#createMetadata(jeeves.server.context.ServiceContext,
     *      java.lang.String, java.lang.String, java.lang.String, int,
     *      java.lang.String, java.lang.String, boolean)
     * @param context
     * @param templateId
     * @param groupOwner
     * @param source
     * @param owner
     * @param parentUuid
     * @param isTemplate
     * @param fullRightsForGroup
     * @return
     * @throws Exception
     */
    @Override
    public String createMetadata(ServiceContext context, String templateId,
            String groupOwner, String source, int owner, String parentUuid,
            String isTemplate, boolean fullRightsForGroup) throws Exception {

        return createMetadata(context, templateId, groupOwner, source, owner,
                parentUuid, isTemplate, fullRightsForGroup,
                UUID.randomUUID().toString());
    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#insertMetadata(jeeves.server.context.ServiceContext,
     *      java.lang.String, org.jdom.Element, java.lang.String, int,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, boolean, boolean)
     * @param context
     * @param schema
     * @param metadataXml
     * @param uuid
     * @param owner
     * @param groupOwner
     * @param source
     * @param metadataType
     * @param docType
     * @param category
     * @param createDate
     * @param changeDate
     * @param ufo
     * @param index
     * @return
     * @throws Exception
     */
    @Override
    public String insertMetadata(ServiceContext context, String schema,
            Element metadataXml, String uuid, int owner, String groupOwner,
            String source, String metadataType, String docType, String category,
            String createDate, String changeDate, boolean ufo, boolean index)
                    throws Exception {
        boolean notifyChange = true;

        if (source == null) {
            source = context.getBean(SettingManager.class).getSiteId();
        }

        if (StringUtils.isBlank(metadataType)) {
            metadataType = MetadataType.METADATA.codeString;
        }
        final Metadata newMetadata = new Metadata();
        newMetadata.setUuid(uuid);
        final ISODate isoChangeDate = changeDate != null
                ? new ISODate(changeDate) : new ISODate();
        final ISODate isoCreateDate = createDate != null
                ? new ISODate(createDate) : new ISODate();
        newMetadata.getDataInfo().setChangeDate(isoChangeDate)
                .setCreateDate(isoCreateDate).setSchemaId(schema)
                .setDoctype(docType).setRoot(metadataXml.getQualifiedName())
                .setType(MetadataType.lookup(metadataType));
        newMetadata.getSourceInfo().setOwner(owner).setSourceId(source);
        if (StringUtils.isNotBlank(groupOwner)) {
            newMetadata.getSourceInfo()
                    .setGroupOwner(Integer.valueOf(groupOwner));
        }
        if (StringUtils.isNotBlank(category)) {
            MetadataCategory metadataCategory = mdCatRepository
                    .findOneByName(category);
            if (metadataCategory == null) {
                throw new IllegalArgumentException(
                        "No category found with name: " + category);
            }
            newMetadata.getMetadataCategories().add(metadataCategory);
        } else if (StringUtils.isNotBlank(groupOwner)) {
            // If the group has a default category, use it
            Group group = groupRepository.findOne(Integer.valueOf(groupOwner));
            if (group.getDefaultCategory() != null) {
                newMetadata.getMetadataCategories().add(group.getDefaultCategory());
            }
        }

        boolean fullRightsForGroup = false;

        int finalId = insertMetadata(context, newMetadata, metadataXml,
                notifyChange, index, ufo, UpdateDatestamp.NO,
                fullRightsForGroup, false).getId();

        return String.valueOf(finalId);
    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#insertMetadata(jeeves.server.context.ServiceContext,
     *      org.fao.geonet.domain.Metadata, org.jdom.Element, boolean, boolean,
     *      boolean, org.fao.geonet.kernel.UpdateDatestamp, boolean, boolean)
     * @param context
     * @param newMetadata
     * @param metadataXml
     * @param notifyChange
     * @param index
     * @param updateFixedInfo
     * @param updateDatestamp
     * @param fullRightsForGroup
     * @param forceRefreshReaders
     * @return
     * @throws Exception
     */
    @Override
    public IMetadata insertMetadata(ServiceContext context,
            IMetadata newMetadata, Element metadataXml, boolean notifyChange,
            boolean index, boolean updateFixedInfo,
            UpdateDatestamp updateDatestamp, boolean fullRightsForGroup,
            boolean forceRefreshReaders) throws Exception {

        final String schema = newMetadata.getDataInfo().getSchemaId();

        // --- force namespace prefix for iso19139 metadata
        setNamespacePrefixUsingSchemas(schema, metadataXml);

        if (updateFixedInfo && newMetadata.getDataInfo()
                .getType() == MetadataType.METADATA) {
            String parentUuid = null;
            metadataXml = updateFixedInfo(schema, Optional.<Integer> absent(),
                    newMetadata.getUuid(), metadataXml, parentUuid,
                    updateDatestamp, context);
        }

        // --- store metadata
        final IMetadata savedMetadata = context.getBean(XmlSerializer.class)
                .insert(newMetadata, metadataXml, context);

        final String stringId = String.valueOf(savedMetadata.getId());
        String groupId = null;
        final Integer groupIdI = newMetadata.getSourceInfo().getGroupOwner();
        if (groupIdI != null) {
            groupId = String.valueOf(groupIdI);
        }
        metadataOperations.copyDefaultPrivForGroup(context, stringId, groupId,
                fullRightsForGroup);

        if (index) {
            metadataIndexer.indexMetadata(stringId, forceRefreshReaders);
        }

        if (notifyChange) {
            // Notifies the metadata change to metatada notifier service
            metadataUtils.notifyMetadataChange(metadataXml, stringId);
        }
        return savedMetadata;
    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#updateMetadata(jeeves.server.context.ServiceContext,
     *      java.lang.String, org.jdom.Element, boolean, boolean, boolean,
     *      java.lang.String, java.lang.String, boolean)
     * @param context
     * @param metadataId
     * @param md
     * @param validate
     * @param ufo
     * @param index
     * @param lang
     * @param changeDate
     * @param updateDateStamp
     * @return
     * @throws Exception
     */
    @Override
    public IMetadata updateMetadata(ServiceContext context, String metadataId,
            Element md, boolean validate, boolean ufo, boolean index,
            String lang, String changeDate, boolean updateDateStamp)
                    throws Exception {
        Element metadataXml = md;

        // when invoked from harvesters, session is null?
        UserSession session = context.getUserSession();
        if (session != null) {
            session.removeProperty(
                    Geonet.Session.VALIDATION_REPORT + metadataId);
        }
        String schema = metadataSchemaUtils.getMetadataSchema(metadataId);
        if (ufo) {
            String parentUuid = null;
            Integer intId = Integer.valueOf(metadataId);
            // Notifies the metadata change to metatada notifier service
            final Metadata metadata = getMetadataRepository().findOne(metadataId);
            
            String uuid = null;

            if (getSchemaManager().getSchema(schema).isReadwriteUUID()
                && metadata.getDataInfo().getType() != MetadataType.SUB_TEMPLATE
                && metadata.getDataInfo().getType() != MetadataType.TEMPLATE_OF_SUB_TEMPLATE) {
                uuid = extractUUID(schema, metadataXml);
            }
            
            metadataXml = updateFixedInfo(schema, Optional.of(intId), uuid, metadataXml, parentUuid, (updateDateStamp ? UpdateDatestamp.YES : UpdateDatestamp.NO), context);
        }

        // --- force namespace prefix for iso19139 metadata
        setNamespacePrefixUsingSchemas(schema, metadataXml);

        // Notifies the metadata change to metatada notifier service
        final IMetadata metadata = getMetadataObject(
                Integer.valueOf(metadataId));

        String uuid = null;
        if (schemaManager.getSchema(schema).isReadwriteUUID() && metadata
                .getDataInfo().getType() != MetadataType.SUB_TEMPLATE
                && metadata.getDataInfo().getType() != MetadataType.TEMPLATE_OF_SUB_TEMPLATE) {
            uuid = extractUUID(schema, metadataXml);
        }

        // --- write metadata to dbms
        context.getBean(XmlSerializer.class).update(metadataId, metadataXml,
                changeDate, updateDateStamp, uuid, context);
        if (metadata.getDataInfo().getType() == MetadataType.METADATA) {
            // Notifies the metadata change to metatada notifier service
            metadataUtils.notifyMetadataChange(metadataXml, metadataId);
        }

        try {
            // --- do the validation last - it throws exceptions
            if (session != null && validate) {
                metadataValidator.doValidate(session, schema, metadataId,
                        metadataXml, lang, false);
            }
        } finally {
            if (index) {
                // --- update search criteria
                metadataIndexer.indexMetadata(metadataId, true);
            }
        }
        return mdRepository.findOne(metadataId);
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#getMetadata(jeeves.server.context.ServiceContext,
     *      java.lang.String, boolean, boolean, boolean)
     * @param srvContext
     * @param id
     * @param forEditing
     * @param withEditorValidationErrors
     * @param keepXlinkAttributes
     * @return
     * @throws Exception
     */
    @Override
    public Element getMetadata(ServiceContext srvContext, String id,
            boolean forEditing, boolean withEditorValidationErrors,
            boolean keepXlinkAttributes) throws Exception {
        boolean doXLinks = srvContext.getBean(XmlSerializer.class)
                .resolveXLinks();
        Element metadataXml = srvContext.getBean(XmlSerializer.class)
                .selectNoXLinkResolver(id, false, forEditing);
        if (metadataXml == null)
            return null;

        String version = null;

        if (forEditing) { // copy in xlink'd fragments but leave xlink atts to
                          // editor
            if (doXLinks)
                Processor.processXLink(metadataXml, srvContext);
            String schema = metadataSchemaUtils.getMetadataSchema(id);

            if (withEditorValidationErrors) {
                version = metadataValidator.doValidate(
                        srvContext.getUserSession(), schema, id, metadataXml,
                        srvContext.getLanguage(), forEditing).two();
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
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#updateFixedInfo(java.lang.String,
     *      com.google.common.base.Optional, java.lang.String, org.jdom.Element,
     *      java.lang.String, org.fao.geonet.kernel.UpdateDatestamp,
     *      jeeves.server.context.ServiceContext)
     * @param schema
     * @param metadataId
     * @param uuid
     * @param md
     * @param parentUuid
     * @param updateDatestamp
     * @param context
     * @return
     * @throws Exception
     */
    @Override
    public Element updateFixedInfo(String schema, Optional<Integer> metadataId,
            String uuid, Element md, String parentUuid,
            UpdateDatestamp updateDatestamp, ServiceContext context)
                    throws Exception {
        boolean autoFixing = getSettingManager().getValueAsBool(Settings.SYSTEM_AUTOFIXING_ENABLE, true);
        if (autoFixing) {
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "Autofixing is enabled, trying update-fixed-info (updateDatestamp: " + updateDatestamp.name() + ")");
            }

            Metadata metadata = null;
            if (metadataId.isPresent()) {
                metadata = getMetadataRepository().findOne(metadataId.get());
                boolean isTemplate = metadata != null && metadata.getDataInfo().getType() != MetadataType.METADATA;

                // don't process templates
                if (isTemplate) {
                    if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
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

            final ThesaurusManager thesaurusManager = ApplicationContextHolder.get().getBean(ThesaurusManager.class);
            env.addContent(thesaurusManager.buildResultfromThTable(context));

            Element schemaLoc = new Element("schemaLocation");
            schemaLoc.setAttribute(getSchemaManager().getSchemaLocation(schema, context));
            env.addContent(schemaLoc);

            if (updateDatestamp == UpdateDatestamp.YES) {
                env.addContent(new Element("changeDate").setText(new ISODate().toString()));
            }
            if (parentUuid != null) {
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
            env.addContent(new Element("siteURL").setText(getSettingManager().getSiteURL(context)));

            // Settings were defined as an XML starting with root named config
            // Only second level elements are defined (under system).
            List<?> config = getSettingManager().getAllAsXML(true).cloneContent();
            for (Object c : config) {
                Element settings = (Element) c;
                env.addContent(settings);
            }

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
     * TODO : buildInfoElem contains similar portion of code with indexMetadata
     * 
     * @param context
     * @param id
     * @param version
     * @return
     * @throws Exception
     */
    protected Element buildInfoElem(ServiceContext context, String id,
            String version) throws Exception {
        Metadata metadata = mdRepository.findOne(id);
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
            HarvestInfoProvider infoProvider = context
                    .getBean(HarvestInfoProvider.class);
            if (infoProvider != null) {
                info.addContent(
                        infoProvider.getHarvestInfo(harvestUuid, id, uuid));
            }
        }
        if (version != null) {
            addElement(info, Edit.Info.Elem.VERSION, version);
        }

        Map<String, Element> map = Maps.newHashMap();
        map.put(id, info);
        buildPrivilegesMetadataInfo(context, map);

        // add owner name
        User user = userRepository.findOne(owner);
        if (user != null) {
            String ownerName = user.getName();
            addElement(info, Edit.Info.Elem.OWNERNAME, ownerName);
        }

        for (MetadataCategory category : metadata.getMetadataCategories()) {
            addElement(info, Edit.Info.Elem.CATEGORY, category.getName());
        }

        // add subtemplates
        /*
         * -- don't add as we need to investigate indexing for the fields -- in
         * the metadata table used here List subList = getSubtemplates(dbms,
         * schema); if (subList != null) { Element subs = new
         * Element(Edit.Info.Elem.SUBTEMPLATES); subs.addContent(subList);
         * info.addContent(subs); }
         */

        // Add validity information
        List<MetadataValidation> validationInfo = mdValidationRepository
                .findAllById_MetadataId(Integer.parseInt(id));
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

                String ratio = "xsd".equals(type) ? ""
                        : vi.getNumFailures() + "/" + vi.getNumTests();

                info.addContent(new Element(Edit.Info.Elem.VALID + "_details")
                        .addContent(new Element("type").setText(type))
                        .addContent(new Element("status")
                                .setText(vi.isValid() ? "1" : "0").addContent(
                                        new Element("ratio").setText(ratio))));
            }
            addElement(info, Edit.Info.Elem.VALID, isValid);
        }

        // add baseUrl of this site (from settings)
        String protocol = context.getBean(SettingManager.class)
                .getValue(Settings.SYSTEM_SERVER_PROTOCOL);
        String host = context.getBean(SettingManager.class)
                .getValue(Settings.SYSTEM_SERVER_HOST);
        String port = context.getBean(SettingManager.class)
                .getValue(Settings.SYSTEM_SERVER_PORT);
        if (port.equals("80")) {
            port = "";
        } else {
            port = ":" + port;
        }
        addElement(info, Edit.Info.Elem.BASEURL,
                protocol + "://" + host + port + context.getBaseUrl());
        addElement(info, Edit.Info.Elem.LOCSERV, "/srv/en");
        return info;
    }

    /**
     *
     * @param root
     * @param name
     * @param value
     */
    protected static void addElement(Element root, String name, Object value) {
        root.addContent(new Element(name)
                .setText(value == null ? "" : value.toString()));
    }

    /**
     * Add privileges information about metadata record which depends on context
     * and usually could not be stored in db or Lucene index because depending
     * on the current user or current client IP address.
     *
     * @param context
     * @param mdIdToInfoMap
     *            a map from the metadata Id -> the info element to which the
     *            privilege information should be added.
     * @throws Exception
     */
    @VisibleForTesting
    protected void buildPrivilegesMetadataInfo(ServiceContext context,
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
        final Map<Integer, MetadataSourceInfo> allSourceInfo = getSourceInfos(metadataIds);

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

    protected Map<Integer, MetadataSourceInfo> getSourceInfos(
            Collection<Integer> metadataIds) {
        return mdRepository
                .findAllSourceInfo(MetadataSpecs.hasMetadataIdIn(metadataIds));
    }

    protected SetMultimap<Integer, ReservedOperation> loadOperationsAllowed(
            ServiceContext context,
            Specification<OperationAllowed> operationAllowedSpec) {
        List<OperationAllowed> operationsAllowed = operationAllowedRepository
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
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#extractUUID(java.lang.String,
     *      org.jdom.Element)
     * @param schema
     * @param md
     * @return
     * @throws Exception
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
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#getMetadataNoInfo(jeeves.server.context.ServiceContext,
     *      java.lang.String)
     * @param srvContext
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public Element getMetadataNoInfo(ServiceContext srvContext, String id)
            throws Exception {
        Element md = getMetadata(srvContext, id, false, false, false);
        md.removeChild(Edit.RootChild.INFO, Edit.NAMESPACE);
        return md;
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#getMetadata(java.lang.String)
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public Element getMetadata(String id) throws Exception {
        Element md = ApplicationContextHolder.get().getBean(XmlSerializer.class)
                .selectNoXLinkResolver(id, false, false);
        if (md == null)
            return null;
        md.detach();
        return md;
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#getElementByRef(org.jdom.Element,
     *      java.lang.String)
     * @param md
     * @param ref
     * @return
     */
    @Override
    public Element getElementByRef(Element md, String ref) {
        return editLib.findElement(md, ref);
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#existsMetadata(int)
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public boolean existsMetadata(int id) throws Exception {
        return mdRepository.exists(id);
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#existsMetadataUuid(java.lang.String)
     * @param uuid
     * @return
     * @throws Exception
     */
    @Override
    public boolean existsMetadataUuid(String uuid) throws Exception {
        return !mdRepository.findAllIdsBy(hasMetadataUuid(uuid)).isEmpty();
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#updateMetadataOwner(int,
     *      java.lang.String, java.lang.String)
     * @param id
     * @param owner
     * @param groupOwner
     * @throws Exception
     */
    @Override
    public synchronized void updateMetadataOwner(final int id,
            final String owner, final String groupOwner) throws Exception {
        mdRepository.update(id, new Updater<Metadata>() {
            @Override
            public void apply(@Nonnull Metadata entity) {
                entity.getSourceInfo()
                        .setGroupOwner(Integer.valueOf(groupOwner));
                entity.getSourceInfo().setOwner(Integer.valueOf(owner));
            }
        });
    }

    protected void deleteMetadataFromDB(ServiceContext context, String id)
            throws Exception {
        // --- remove operations
        deleteMetadataOper(context, id, false);

        int intId = Integer.parseInt(id);
        mdRatingByIpRepository.deleteAllById_MetadataId(intId);
        mdValidationRepository.deleteAllById_MetadataId(intId);
        mdStatusRepository.deleteAllById_MetadataId(intId);
        userSavedSelectionRepository.deleteAllByUuid(metadataUtils.getMetadataUuid(id));

        // Logical delete for metadata file uploads
        PathSpec<MetadataFileUpload, String> deletedDatePathSpec = new PathSpec<MetadataFileUpload, String>() {
            @Override
            public javax.persistence.criteria.Path<String> getPath(
                    Root<MetadataFileUpload> root) {
                return root.get(MetadataFileUpload_.deletedDate);
            }
        };
        mdFileUploadRepository.createBatchUpdateQuery(deletedDatePathSpec,
                new ISODate().toString(),
                MetadataFileUploadSpecs.isNotDeletedForMetadata(intId));

        // --- remove metadata
        context.getBean(XmlSerializer.class).delete(id, context);
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#deleteMetadata(jeeves.server.context.ServiceContext,
     *      java.lang.String)
     * @param context
     * @param metadataId
     * @throws Exception
     */
    @Override
    public synchronized void deleteMetadata(ServiceContext context,
            String metadataId) throws Exception {
        String uuid = metadataUtils.getMetadataUuid(metadataId);
        Metadata findOne = mdRepository.findOne(metadataId);
        if (findOne != null) {
            boolean isMetadata = findOne.getDataInfo()
                    .getType() == MetadataType.METADATA;

            deleteMetadataFromDB(context, metadataId);

            // Notifies the metadata change to metatada notifier service
            if (isMetadata) {
                metadataNotifierManager
                        .deleteMetadata(metadataId, uuid, context);
            }
        }

        // --- update search criteria

        searchManager.delete("_id", metadataId + "");
        // _entityManager.flush();
        // _entityManager.clear();
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#deleteMetadataGroup(jeeves.server.context.ServiceContext,
     *      java.lang.String)
     * @param context
     * @param metadataId
     * @throws Exception
     */
    @Override
    public synchronized void deleteMetadataGroup(ServiceContext context,
            String metadataId) throws Exception {
        deleteMetadataFromDB(context, metadataId);
        // --- update search criteria

        context.getBean(SearchManager.class).deleteGroup("_id",
                metadataId + "");
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#deleteMetadataOper(jeeves.server.context.ServiceContext,
     *      java.lang.String, boolean)
     * @param context
     * @param metadataId
     * @param skipAllIntranet
     * @throws Exception
     */
    @Override
    @Transactional
    public void deleteMetadataOper(ServiceContext context, String metadataId, boolean skipAllReservedGroup) throws Exception {
        OperationAllowedRepository operationAllowedRepository = context.getBean(OperationAllowedRepository.class);

        if (skipAllReservedGroup) {
            Integer[] exclude = new Integer[] {
                ReservedGroup.all.getId(),
                    ReservedGroup.intranet.getId(),
                    ReservedGroup.guest.getId()
            };
            operationAllowedRepository.deleteAllByMetadataIdExceptGroupId(
                Integer.parseInt(metadataId), exclude
            );
        } else {
            operationAllowedRepository.deleteAllByIdAttribute(OperationAllowedId_.metadataId, Integer.parseInt(metadataId));
        }
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#init(jeeves.server.context.ServiceContext,
     *      java.lang.Boolean)
     * @param context
     * @param force
     * @throws Exception
     */
    @Override
    public synchronized void init(ServiceContext context, Boolean force)
            throws Exception {

        // TODO check that all "autowired" fields are filled

        editLib = new EditLib(schemaManager);

        if (context.getUserSession() == null) {
            UserSession session = new UserSession();
            context.setUserSession(session);
            session.loginAs(new User().setUsername("admin").setId(-1)
                    .setProfile(Profile.Administrator));
        }
        // get lastchangedate of all metadata in index
        Map<String, String> docs = context.getBean(SearchManager.class)
                .getDocsChangeDate();

        // set up results HashMap for post processing of records to be indexed
        ArrayList<String> toIndex = new ArrayList<String>();

        index(force, docs, toIndex);

        // if anything to index then schedule it to be done after servlet is
        // up so that any links to local fragments are resolvable
        if (toIndex.size() > 0) {
            metadataIndexer.batchIndexInThreadPool(context, toIndex);
        }

        if (docs.size() > 0) { // anything left?
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER,
                        "INDEX HAS RECORDS THAT ARE NOT IN DB:");
            }
        }

        // remove from index metadata not in DBMS
        for (String id : docs.keySet()) {
            context.getBean(SearchManager.class).delete("_id", id);

            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER,
                        "- removed record (" + id + ") from index");
            }
        }
    }

    protected void index(Boolean force, Map<String, String> docs,
            ArrayList<String> toIndex) {
        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "INDEX CONTENT:");

        Sort sortByMetadataChangeDate = SortUtils.createSort(Metadata_.dataInfo,
                MetadataDataInfo_.changeDate);
        int currentPage = 0;
        Page<Pair<Integer, ISODate>> results = mdRepository
                .findAllIdsAndChangeDates(new PageRequest(currentPage,
                        METADATA_BATCH_PAGE_SIZE, sortByMetadataChangeDate));

        // index all metadata in DBMS if needed
        while (results.getNumberOfElements() > 0) {
            currentPage = index(force, docs, toIndex, currentPage, results);
            results = mdRepository.findAllIdsAndChangeDates(
                    new PageRequest(currentPage, METADATA_BATCH_PAGE_SIZE,
                            sortByMetadataChangeDate));
        }
    }

    protected int index(Boolean force, Map<String, String> docs,
            ArrayList<String> toIndex, int currentPage,
            Page<Pair<Integer, ISODate>> results) {
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
                    Log.debug(Geonet.DATA_MANAGER,
                            "- lastChange: " + lastChange);
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER,
                            "- idxLastChange: " + idxLastChange);

                // date in index contains 't', date in DBMS contains 'T'
                if (force || !idxLastChange.equalsIgnoreCase(lastChange)) {
                    if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                        Log.debug(Geonet.DATA_MANAGER,
                                "-  will be indexed");
                    toIndex.add(id);
                }
            }
        }

        currentPage++;
        return currentPage;
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#setNamespacePrefixUsingSchemas(java.lang.String,
     *      org.jdom.Element)
     * @param schema
     * @param md
     * @throws Exception
     */
    @Override
    public void setNamespacePrefixUsingSchemas(String schema, Element md)
            throws Exception {
        //--- if the metadata has no namespace or already has a namespace prefix
        //--- then we must skip this phase
        Namespace ns = md.getNamespace();
        if (ns == Namespace.NO_NAMESPACE)
            return;

        MetadataSchema mds = getSchemaManager().getSchema(schema);

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

    private void setNamespacePrefix(final Element md, final Namespace ns) {
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
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#getMetadataObject(java.lang.String)
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public IMetadata getMetadataObject(Integer id) throws Exception {
        if (existsMetadata(id)) {
            return mdRepository.findOne(id);
        } else {
            return null;
        }
    }
    
    @Override
    public IMetadata getMetadataObjectNoPriv(Integer id) throws Exception {
        return getMetadataObject(id); 
    }
    
    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#save(org.fao.geonet.domain.IMetadata)
     * @param md
     */
    @Override
    public IMetadata save(IMetadata md) {
        return mdRepository.save((Metadata)md);
    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#getMetadataObject(java.lang.String)
     * @param uuid
     * @return
     * @throws Exception
     */
    @Override
    public IMetadata getMetadataObject(String uuid) throws Exception {
        return mdRepository.findOneByUuid(uuid);
    }

    private SettingManager getSettingManager() {
        return ApplicationContextHolder.get().getBean(SettingManager.class);
    }
    
    private MetadataRepository getMetadataRepository() {
        return mdRepository;
    }
    
    private SchemaManager getSchemaManager() {
        return schemaManager;
    }

    public Path getSchemaDir(String name) {
        return getSchemaManager().getSchemaDir(name);
    }
}
