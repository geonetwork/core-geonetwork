/**
 * 
 */
package org.fao.geonet.kernel.metadata.draft;

import static org.springframework.data.jpa.domain.Specifications.where;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.IMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.HarvestInfoProvider;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.metadata.DefaultMetadataManager;
import org.fao.geonet.kernel.schema.AssociatedResourcesSchemaPlugin;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.specification.MetadataDraftSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;

import jeeves.server.context.ServiceContext;

/**
 * trunk-core
 * 
 * @author delawen
 * 
 * 
 */
public class DraftMetadataManager extends DefaultMetadataManager {

    @Autowired
    private MetadataDraftRepository mdDraftRepository;

    /**
     * @param context
     */
    @Override
    public void init(ServiceContext context) {
        super.init(context);
        this.mdDraftRepository = context.getBean(MetadataDraftRepository.class);
    }

    /**
     * @see org.fao.geonet.kernel.metadata.DefaultMetadataManager#deleteMetadata(jeeves.server.context.ServiceContext,
     *      java.lang.String)
     * @param context
     * @param metadataId
     * @throws Exception
     */
    @Override
    public synchronized void deleteMetadata(ServiceContext context,
            String metadataId) throws Exception {
        Metadata md = mdRepository.findOne(metadataId);
        MetadataDraft mdD = mdDraftRepository.findOneByUuid(md.getUuid());
        if (mdD != null) {
            super.deleteMetadata(context, Integer.toString(mdD.getId()));
        }
        super.deleteMetadata(context, metadataId);
    }

    /**
     * @see org.fao.geonet.kernel.metadata.DefaultMetadataManager#existsMetadata(int)
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public boolean existsMetadata(int id) throws Exception {
        return super.existsMetadata(id) || mdDraftRepository.exists(id);
    }

    /**
     * @see org.fao.geonet.kernel.metadata.DefaultMetadataManager#existsMetadataUuid(java.lang.String)
     * @param uuid
     * @return
     * @throws Exception
     */
    @Override
    public boolean existsMetadataUuid(String uuid) throws Exception {
        return super.existsMetadataUuid(uuid) || !mdDraftRepository
                .findAllIdsBy(MetadataDraftSpecs.hasMetadataUuid(uuid))
                .isEmpty();
    }

    /**
     * @see org.fao.geonet.kernel.metadata.DefaultMetadataManager#startEditingSession(jeeves.server.context.ServiceContext,
     *      java.lang.String)
     * @param context
     * @param id
     * @throws Exception
     */
    @Override
    public String startEditingSession(ServiceContext context, String id)
            throws Exception {
        Metadata md = mdRepository.findOne(Integer.valueOf(id));

        if (md != null) {
            boolean isPublished = loadOperationsAllowed(context,
                    where(OperationAllowedSpecs.hasMetadataId(id))
                            .and(OperationAllowedSpecs
                                    .isPublic(ReservedOperation.view))).keySet()
                                            .contains(Integer.valueOf(id));

            // We need to create a draft to avoid modifying the published
            // metadata
            if (isPublished
                    && mdDraftRepository.findOneByUuid(md.getUuid()) == null) {

                boolean fullRightsForGroup = true;
                // Get parent record from this record
                String parentUuid = "";
                String schemaIdentifier = metadataSchemaUtils
                        .getMetadataSchema(id);
                SchemaPlugin instance = SchemaManager
                        .getSchemaPlugin(schemaIdentifier);
                AssociatedResourcesSchemaPlugin schemaPlugin = null;
                if (instance instanceof AssociatedResourcesSchemaPlugin) {
                    schemaPlugin = (AssociatedResourcesSchemaPlugin) instance;
                }
                if (schemaPlugin != null) {
                    Set<String> listOfUUIDs = schemaPlugin
                            .getAssociatedParentUUIDs(md.getXmlData(false));
                    if (listOfUUIDs.size() > 0) {
                        // FIXME more than one parent? Is it even possible?
                        parentUuid = listOfUUIDs.iterator().next();
                    }
                }

                String groupOwner = md.getSourceInfo().getGroupOwner()
                        .toString();
                String source = md.getSourceInfo().getSourceId().toString();
                Integer owner = md.getSourceInfo().getOwner();

                id = createDraft(context, id, groupOwner, source, owner,
                        parentUuid, md.getDataInfo().getType().codeString,
                        fullRightsForGroup, md.getUuid());
            } else if (isPublished
                    && mdDraftRepository.findOneByUuid(md.getUuid()) != null) {
                // We already have a draft created
                id = Integer.toString(
                        mdDraftRepository.findOneByUuid(md.getUuid()).getId());
            }
        }

        return super.startEditingSession(context, id);
    }

    private String createDraft(ServiceContext context, String templateId,
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
        final MetadataDraft newMetadata = new MetadataDraft();
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
     * @see org.fao.geonet.kernel.metadata.DefaultMetadataManager#updateFixedInfo(java.lang.String,
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

            IMetadata metadata = null;
            if (metadataId.isPresent()) {
                metadata = mdRepository.findOne(metadataId.get());
                if (metadata == null) {
                    metadata = mdDraftRepository.findOne(metadataId.get());
                }
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
            Path styleSheet = metadataSchemaUtils.getSchemaDir(schema)
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
     * @see org.fao.geonet.kernel.metadata.DefaultMetadataManager#updateMetadata(jeeves.server.context.ServiceContext,
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
        IMetadata metaData = super.updateMetadata(context, metadataId, md,
                validate, ufo, index, lang, changeDate, updateDateStamp);

        if (metaData != null) {
            return metaData;
        } else {
            return mdDraftRepository.findOne(metadataId);
        }
    }

    /**
     * @see org.fao.geonet.kernel.metadata.DefaultMetadataManager#updateMetadataOwner(int,
     *      java.lang.String, java.lang.String)
     * @param id
     * @param owner
     * @param groupOwner
     * @throws Exception
     */
    @Override
    public synchronized void updateMetadataOwner(int id, final String owner,
            final String groupOwner) throws Exception {

        if (mdRepository.exists(id)) {
            super.updateMetadataOwner(id, owner, groupOwner);
        } else {
            mdDraftRepository.update(id, new Updater<MetadataDraft>() {
                @Override
                public void apply(@Nonnull MetadataDraft entity) {
                    entity.getSourceInfo()
                            .setGroupOwner(Integer.valueOf(groupOwner));
                    entity.getSourceInfo().setOwner(Integer.valueOf(owner));
                }
            });
        }
    }

    @Override
    protected Element buildInfoElem(ServiceContext context, String id,
            String version) throws Exception {
        IMetadata metadata = mdRepository.findOne(id);
        if (metadata == null) {
            metadata = mdDraftRepository.findOne(id);
        }
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

        if (metadata instanceof Metadata) {
            for (MetadataCategory category : ((Metadata) metadata)
                    .getCategories()) {
                addElement(info, Edit.Info.Elem.CATEGORY, category.getName());
            }
        } else {
            for (MetadataCategory category : ((MetadataDraft) metadata)
                    .getCategories()) {
                addElement(info, Edit.Info.Elem.CATEGORY, category.getName());
            }
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
     * @see org.fao.geonet.kernel.metadata.DefaultMetadataManager#getMetadataObject(java.lang.Integer)
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public IMetadata getMetadataObject(Integer id) throws Exception {
        IMetadata md = super.getMetadataObject(id);
        if (md == null && existsMetadata(id)) {
            md = mdDraftRepository.findOne(id);
        }
        return md;
    }
}
