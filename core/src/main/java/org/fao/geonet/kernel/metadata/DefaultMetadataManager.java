/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import static org.springframework.data.jpa.domain.Specifications.where;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.HarvestInfoProvider;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

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

    @Autowired
    private DataManager dm;

    @Autowired
    private MetadataRepository mdRepository;

    private SchemaManager schemaManager;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MetadataCategoryRepository mdCatRepository;

    @Autowired
    private MetadataValidationRepository mdValidationRepository;

    @Autowired
    private OperationAllowedRepository operationAllowedRepository;

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

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#startEditingSession(jeeves.server.context.ServiceContext,
     *      java.lang.String)
     * @param context
     * @param id
     * @throws Exception
     */
    public void startEditingSession(ServiceContext context, String id)
            throws Exception {
        if (Log.isDebugEnabled(Geonet.EDITOR_SESSION)) {
            Log.debug(Geonet.EDITOR_SESSION,
                    "Editing session starts for record " + id);
        }

        boolean keepXlinkAttributes = true;
        boolean forEditing = false;
        boolean withValidationErrors = false;
        Element metadataBeforeAnyChanges = getMetadata(context, id, forEditing,
                withValidationErrors, keepXlinkAttributes);
        context.getUserSession().setProperty(
                Geonet.Session.METADATA_BEFORE_ANY_CHANGES + id,
                metadataBeforeAnyChanges);
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
            newMetadata.getCategories().add(group.getDefaultCategory());
        }
        Collection<MetadataCategory> filteredCategories = Collections2.filter(
                templateMetadata.getCategories(),
                new Predicate<MetadataCategory>() {
                    @Override
                    public boolean apply(@Nullable MetadataCategory input) {
                        return input != null;
                    }
                });

        newMetadata.getCategories().addAll(filteredCategories);

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
        if (groupOwner != null) {
            newMetadata.getSourceInfo()
                    .setGroupOwner(Integer.valueOf(groupOwner));
        }
        if (category != null) {
            MetadataCategory metadataCategory = mdCatRepository
                    .findOneByName(category);
            if (metadataCategory == null) {
                throw new IllegalArgumentException(
                        "No category found with name: " + category);
            }
            newMetadata.getCategories().add(metadataCategory);
        } else if (groupOwner != null) {
            // If the group has a default category, use it
            Group group = groupRepository.findOne(Integer.valueOf(groupOwner));
            if (group.getDefaultCategory() != null) {
                newMetadata.getCategories().add(group.getDefaultCategory());
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
    public Metadata insertMetadata(ServiceContext context, Metadata newMetadata,
            Element metadataXml, boolean notifyChange, boolean index,
            boolean updateFixedInfo, UpdateDatestamp updateDatestamp,
            boolean fullRightsForGroup, boolean forceRefreshReaders)
                    throws Exception {

        final String schema = newMetadata.getDataInfo().getSchemaId();

        // --- force namespace prefix for iso19139 metadata
        dm.setNamespacePrefixUsingSchemas(schema, metadataXml);

        if (updateFixedInfo && newMetadata.getDataInfo()
                .getType() == MetadataType.METADATA) {
            String parentUuid = null;
            metadataXml = updateFixedInfo(schema,
                    Optional.<Integer> absent(), newMetadata.getUuid(),
                    metadataXml, parentUuid, updateDatestamp, context);
        }

        // --- store metadata
        final Metadata savedMetadata = context.getBean(XmlSerializer.class)
                .insert(newMetadata, metadataXml, context);

        final String stringId = String.valueOf(savedMetadata.getId());
        String groupId = null;
        final Integer groupIdI = newMetadata.getSourceInfo().getGroupOwner();
        if (groupIdI != null) {
            groupId = String.valueOf(groupIdI);
        }
        dm.copyDefaultPrivForGroup(context, stringId, groupId,
                fullRightsForGroup);

        if (index) {
            dm.indexMetadata(stringId, forceRefreshReaders);
        }

        if (notifyChange) {
            // Notifies the metadata change to metatada notifier service
            dm.notifyMetadataChange(metadataXml, stringId);
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
    public Metadata updateMetadata(ServiceContext context, String metadataId,
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
        String schema = getMetadataSchema(metadataId);
        if (ufo) {
            String parentUuid = null;
            Integer intId = Integer.valueOf(metadataId);
            metadataXml = updateFixedInfo(schema, Optional.of(intId), null,
                    metadataXml, parentUuid, (updateDateStamp
                            ? UpdateDatestamp.YES : UpdateDatestamp.NO),
                    context);
        }

        // --- force namespace prefix for iso19139 metadata
        dm.setNamespacePrefixUsingSchemas(schema, metadataXml);

        // Notifies the metadata change to metatada notifier service
        final Metadata metadata = mdRepository.findOne(metadataId);

        String uuid = null;
        if (schemaManager.getSchema(schema).isReadwriteUUID() && metadata
                .getDataInfo().getType() != MetadataType.SUB_TEMPLATE) {
            uuid = extractUUID(schema, metadataXml);
        }

        // --- write metadata to dbms
        context.getBean(XmlSerializer.class).update(metadataId, metadataXml,
                changeDate, updateDateStamp, uuid, context);
        if (metadata.getDataInfo().getType() == MetadataType.METADATA) {
            // Notifies the metadata change to metatada notifier service
            dm.notifyMetadataChange(metadataXml, metadataId);
        }

        try {
            // --- do the validation last - it throws exceptions
            if (session != null && validate) {
                dm.doValidate(session, schema, metadataId, metadataXml, lang,
                        false);
            }
        } finally {
            if (index) {
                // --- update search criteria
                dm.indexMetadata(metadataId, true);
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
                .selectNoXLinkResolver(id, false);
        if (metadataXml == null)
            return null;

        String version = null;

        if (forEditing) { // copy in xlink'd fragments but leave xlink atts to
                          // editor
            if (doXLinks)
                Processor.processXLink(metadataXml, srvContext);
            String schema = getMetadataSchema(id);

            if (withEditorValidationErrors) {
                version = dm.doValidate(srvContext.getUserSession(), schema, id,
                        metadataXml, srvContext.getLanguage(), forEditing)
                        .two();
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
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#getMetadataSchema(java.lang.String)
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public String getMetadataSchema(String id) throws Exception {
        Metadata md = mdRepository.findOne(id);

        if (md == null) {
            throw new IllegalArgumentException(
                    "Metadata not found for id : " + id);
        } else {
            // get metadata
            return md.getDataInfo().getSchemaId();
        }
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
        boolean autoFixing = context.getBean(SettingManager.class)
                .getValueAsBool("system/autofixing/enable", true);
        if (autoFixing) {
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER,
                        "Autofixing is enabled, trying update-fixed-info (updateDatestamp: "
                                + updateDatestamp.name() + ")");

            Metadata metadata = null;
            if (metadataId.isPresent()) {
                metadata = mdRepository.findOne(metadataId.get());
                boolean isTemplate = metadata != null && metadata.getDataInfo()
                        .getType() != MetadataType.METADATA;

                // don't process templates
                if (isTemplate) {
                    if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                        Log.debug(Geonet.DATA_MANAGER,
                                "Not applying update-fixed-info for a template");
                    }
                    return md;
                }
            }

            String currentUuid = metadata != null ? metadata.getUuid() : null;
            String id = metadata != null ? metadata.getId() + "" : null;
            uuid = uuid == null ? currentUuid : uuid;

            // --- setup environment
            Element env = new Element("env");
            env.addContent(new Element("id").setText(id));
            env.addContent(new Element("uuid").setText(uuid));

            final ThesaurusManager thesaurusManager = context
                    .getBean(ThesaurusManager.class);
            env.addContent(thesaurusManager.buildResultfromThTable(context));

            Element schemaLoc = new Element("schemaLocation");
            schemaLoc.setAttribute(
                    schemaManager.getSchemaLocation(schema, context));
            env.addContent(schemaLoc);

            if (updateDatestamp == UpdateDatestamp.YES) {
                env.addContent(new Element("changeDate")
                        .setText(new ISODate().toString()));
            }
            if (parentUuid != null) {
                env.addContent(new Element("parentUuid").setText(parentUuid));
            }
            if (metadataId.isPresent()) {
                String metadataIdString = String.valueOf(metadataId.get());
                final Path resourceDir = Lib.resource.getDir(context,
                        Params.Access.PRIVATE, metadataIdString);
                env.addContent(
                        new Element("datadir").setText(resourceDir.toString()));
            }

            // add original metadata to result
            Element result = new Element("root");
            result.addContent(md);
            // add 'environment' to result
            env.addContent(new Element("siteURL").setText(
                    context.getBean(SettingManager.class).getSiteURL(context)));

            // Settings were defined as an XML starting with root named config
            // Only second level elements are defined (under system).
            List<?> config = context.getBean(SettingManager.class)
                    .getAllAsXML(true).cloneContent();
            for (Object c : config) {
                Element settings = (Element) c;
                env.addContent(settings);
            }

            result.addContent(env);
            // apply update-fixed-info.xsl
            Path styleSheet = dm.getSchemaDir(schema)
                    .resolve(Geonet.File.UPDATE_FIXED_INFO);
            result = Xml.transform(result, styleSheet);
            return result;
        } else {
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER,
                        "Autofixing is disabled, not applying update-fixed-info");
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
    private Element buildInfoElem(ServiceContext context, String id,
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
        String isHarvested = "" + Constants
                .toYN_EnabledChar(metadata.getHarvestInfo().isHarvested());
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

        for (MetadataCategory category : metadata.getCategories()) {
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
                .getValue(Geonet.Settings.SERVER_PROTOCOL);
        String host = context.getBean(SettingManager.class)
                .getValue(Geonet.Settings.SERVER_HOST);
        String port = context.getBean(SettingManager.class)
                .getValue(Geonet.Settings.SERVER_PORT);
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
    private static void addElement(Element root, String name, Object value) {
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
    void buildPrivilegesMetadataInfo(ServiceContext context,
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

    private SetMultimap<Integer, ReservedOperation> loadOperationsAllowed(
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
     * @see org.fao.geonet.kernel.metadata.IMetadataManager#extractUUID(java.lang.String, org.jdom.Element)
     * @param schema
     * @param md
     * @return
     * @throws Exception
     */
    @Override
    public String extractUUID(String schema, Element md) throws Exception {
        Path styleSheet = dm.getSchemaDir(schema).resolve(Geonet.File.EXTRACT_UUID);
        String uuid       = Xml.transform(md, styleSheet).getText().trim();

        if(Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Extracted UUID '"+ uuid +"' for schema '"+ schema +"'");

        //--- needed to detach md from the document
        md.detach();

        return uuid;
    }

}
