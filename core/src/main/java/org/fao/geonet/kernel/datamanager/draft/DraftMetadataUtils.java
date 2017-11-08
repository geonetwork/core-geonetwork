package org.fao.geonet.kernel.datamanager.draft;

import static org.fao.geonet.repository.specification.MetadataSpecs.hasMetadataUuid;
import static org.springframework.data.jpa.domain.Specifications.where;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.IMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.MetadataHarvestInfo;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.datamanager.IMetadataOperations;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataUtils;
import org.fao.geonet.kernel.schema.AssociatedResourcesSchemaPlugin;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.SimpleMetadata;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.reports.MetadataReportsQueries;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import jeeves.server.context.ServiceContext;

public class DraftMetadataUtils extends BaseMetadataUtils implements IMetadataUtils {

    @Autowired
    private MetadataDraftRepository metadataDraftRepository;
    @Autowired
    private IMetadataOperations metadataOperations;
    @Autowired
    private OperationAllowedRepository operationAllowedRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private AccessManager am;

    private ServiceContext context;

    public void init(ServiceContext context, Boolean force) throws Exception {
        this.metadataDraftRepository = context.getBean(MetadataDraftRepository.class);
        this.metadataOperations = context.getBean(IMetadataOperations.class);
        this.operationAllowedRepository = context.getBean(OperationAllowedRepository.class);
        this.groupRepository = context.getBean(GroupRepository.class);
        this.am = context.getBean(AccessManager.class);
        this.context = context;
        super.init(context, force);
    }

    @Override
    public void setTemplateExt(final int id, final MetadataType metadataType) throws Exception {
        try {
            super.setTemplateExt(id, metadataType);
        } catch (RuntimeException e) {
            metadataDraftRepository.update(id, new Updater<MetadataDraft>() {
                @Override
                public void apply(@Nonnull MetadataDraft metadata) {
                    final MetadataDataInfo dataInfo = metadata.getDataInfo();
                    dataInfo.setType(metadataType);
                }
            });
        }
    }

    /**
     * Start an editing session. This will record the original metadata record in the session under the
     * {@link org.fao.geonet.constants.Geonet.Session#METADATA_BEFORE_ANY_CHANGES} + id session property.
     *
     * The record contains geonet:info element.
     *
     * Note: Only the metadata record is stored in session. If the editing session upload new documents or thumbnails, those documents will
     * not be cancelled. This needs improvements.
     */
    @Override
    public void startEditingSession(ServiceContext context, String id) throws Exception {
        // Check id
        IMetadata md = super.getMetadataRepository().findOne(Integer.valueOf(id));

        if (md != null) {
            boolean isPublished = loadOperationsAllowed(context,
                    where(OperationAllowedSpecs.hasMetadataId(id)).and(OperationAllowedSpecs.isPublic(ReservedOperation.view))).keySet()
                            .contains(Integer.valueOf(id));

            // We need to create a draft to avoid modifying the published
            // metadata
            if (isPublished && metadataDraftRepository.findOneByUuid(md.getUuid()) == null) {

                // Get parent record from this record
                String parentUuid = "";
                String schemaIdentifier = super.getMetadataSchemaUtils().getMetadataSchema(id);
                SchemaPlugin instance = SchemaManager.getSchemaPlugin(schemaIdentifier);
                AssociatedResourcesSchemaPlugin schemaPlugin = null;
                if (instance instanceof AssociatedResourcesSchemaPlugin) {
                    schemaPlugin = (AssociatedResourcesSchemaPlugin) instance;
                }
                if (schemaPlugin != null) {
                    Set<String> listOfUUIDs = schemaPlugin.getAssociatedParentUUIDs(md.getXmlData(false));
                    if (listOfUUIDs.size() > 0) {
                        // FIXME more than one parent? Is it even possible?
                        parentUuid = listOfUUIDs.iterator().next();
                    }
                }

                String groupOwner = null;
                String source = null;
                Integer owner = 1;

                if (md.getSourceInfo() != null) {
                    if (md.getSourceInfo().getSourceId() != null) {
                        source = md.getSourceInfo().getSourceId().toString();
                    }
                    if (md.getSourceInfo().getGroupOwner() != null) {
                        groupOwner = md.getSourceInfo().getGroupOwner().toString();
                    }
                    owner = md.getSourceInfo().getOwner();
                }

                id = createDraft(context, id, groupOwner, source, owner, parentUuid, md.getDataInfo().getType().codeString, false,
                        md.getUuid());
            } else if (isPublished && metadataDraftRepository.findOneByUuid(md.getUuid()) != null) {
                // We already have a draft created
                id = Integer.toString(metadataDraftRepository.findOneByUuid(md.getUuid()).getId());
            }
        }

        super.startEditingSession(context, id);
    }

    protected SetMultimap<Integer, ReservedOperation> loadOperationsAllowed(ServiceContext context,
            Specification<OperationAllowed> operationAllowedSpec) {
        List<OperationAllowed> operationsAllowed = operationAllowedRepository.findAll(operationAllowedSpec);
        SetMultimap<Integer, ReservedOperation> operationsPerMetadata = HashMultimap.create();
        for (OperationAllowed allowed : operationsAllowed) {
            final OperationAllowedId id = allowed.getId();
            operationsPerMetadata.put(id.getMetadataId(), ReservedOperation.lookup(id.getOperationId()));
        }
        return operationsPerMetadata;
    }

    private String createDraft(ServiceContext context, String templateId, String groupOwner, String source, int owner, String parentUuid,
            String isTemplate, boolean fullRightsForGroup, String uuid) throws Exception {
        Metadata templateMetadata = super.getMetadataRepository().findOne(templateId);
        if (templateMetadata == null) {
            throw new IllegalArgumentException("Template id not found : " + templateId);
        }

        String schema = templateMetadata.getDataInfo().getSchemaId();
        String data = templateMetadata.getData();
        Element xml = Xml.loadString(data, false);
        if (templateMetadata.getDataInfo().getType() == MetadataType.METADATA) {
            xml = super.getMetadataManager().updateFixedInfo(schema, Optional.<Integer> absent(), uuid, xml, parentUuid, UpdateDatestamp.NO,
                    context);
        }
        final MetadataDraft newMetadata = new MetadataDraft();
        newMetadata.setUuid(uuid);
        newMetadata.getDataInfo().setChangeDate(new ISODate()).setCreateDate(new ISODate()).setSchemaId(schema)
                .setType(MetadataType.lookup(isTemplate));
        if (groupOwner != null) {
            newMetadata.getSourceInfo().setGroupOwner(Integer.valueOf(groupOwner));
        }
        newMetadata.getSourceInfo().setOwner(owner);

        if (source != null) {
            newMetadata.getSourceInfo().setSourceId(source);
        }
        // If there is a default category for the group, use it:
        if (groupOwner != null) {
            Group group = groupRepository.findOne(Integer.valueOf(groupOwner));
            if (group.getDefaultCategory() != null) {
                newMetadata.getCategories().add(group.getDefaultCategory());
            }
        }

        for (MetadataCategory mc : templateMetadata.getMetadataCategories()) {
            newMetadata.getCategories().add(mc);
        }

        try {
            Integer finalId = super.getMetadataManager()
                    .insertMetadata(context, newMetadata, xml, false, true, true, UpdateDatestamp.YES, fullRightsForGroup, true).getId();

            // Copy privileges from original metadata
            for (OperationAllowed op : metadataOperations.getAllOperations(templateMetadata.getId())) {
                if (ReservedGroup.all.getId() != op.getId().getGroupId()) { // except for group All
                    try {
                        metadataOperations.setOperation(context, finalId, op.getId().getGroupId(), op.getId().getOperationId());
                    } catch (Throwable t) {
                        // On this particular case, we want to set up the operations
                        // even if the person creating the draft does not own the groups

                        metadataOperations.forceSetOperation(context, finalId, op.getId().getGroupId(), op.getId().getOperationId());
                    }
                }
            }

            super.getMetadataIndexer().indexMetadata(String.valueOf(finalId), true, null);

            super.getMetadataIndexer().indexMetadata(String.valueOf(templateId), true, null);

            return String.valueOf(finalId);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return templateId;
    }

    /**
     * Set metadata type to subtemplate and set the title. Only subtemplates need to persist the title as it is used to give a meaningful
     * title for use when offering the subtemplate to users in the editor.
     *
     * @param id Metadata id to set to type subtemplate
     * @param title Title of metadata of subtemplate/fragment
     */
    @Override
    public void setSubtemplateTypeAndTitleExt(final int id, String title) throws Exception {
        try {
            super.setSubtemplateTypeAndTitleExt(id, title);
        } catch (EntityNotFoundException e) {
            metadataDraftRepository.update(id, new Updater<MetadataDraft>() {
                @Override
                public void apply(@Nonnull MetadataDraft metadata) {
                    final MetadataDataInfo dataInfo = metadata.getDataInfo();
                    dataInfo.setType(MetadataType.SUB_TEMPLATE);
                    if (title != null) {
                        dataInfo.setTitle(title);
                    }
                }
            });
        }
    }

    @Override
    public void setHarvestedExt(final int id, final String harvestUuid, final Optional<String> harvestUri) throws Exception {
        try {
            super.setHarvestedExt(id, harvestUuid, harvestUri);
        } catch (EntityNotFoundException e) {
            metadataDraftRepository.update(id, new Updater<MetadataDraft>() {
                @Override
                public void apply(MetadataDraft metadata) {
                    MetadataHarvestInfo harvestInfo = metadata.getHarvestInfo();
                    harvestInfo.setUuid(harvestUuid);
                    harvestInfo.setHarvested(harvestUuid != null);
                    harvestInfo.setUri(harvestUri.orNull());
                }
            });
        }
    }

    /**
     *
     * @param id
     * @param displayOrder
     * @throws Exception
     */
    @Override
    public void updateDisplayOrder(final String id, final String displayOrder) throws Exception {
        try {
            super.updateDisplayOrder(id, displayOrder);
        } catch (EntityNotFoundException e) {
            metadataDraftRepository.update(Integer.valueOf(id), new Updater<MetadataDraft>() {
                @Override
                public void apply(MetadataDraft entity) {
                    entity.getDataInfo().setDisplayOrder(Integer.parseInt(displayOrder));
                }
            });
        }
    }

    /**
     * Rates a metadata.
     *
     * @param ipAddress ipAddress IP address of the submitting client
     * @param rating range should be 1..5
     * @throws Exception hmm
     */
    @Override
    public int rateMetadata(final int metadataId, final String ipAddress, final int rating) throws Exception {
        final int newRating = ratingByIpRepository.averageRating(metadataId);
        try {
            return super.rateMetadata(metadataId, ipAddress, rating);
        } catch (EntityNotFoundException e) {
            metadataDraftRepository.update(metadataId, new Updater<MetadataDraft>() {
                @Override
                public void apply(MetadataDraft entity) {
                    entity.getDataInfo().setRating(newRating);
                }
            });
        }
        return rating;
    }

    @SuppressWarnings("unchecked")
    @Override
    public long count(Specification<? extends IMetadata> specs) {
        long tmp = 0;
        try {
            tmp += super.count((Specification<Metadata>) specs);
        } catch (Throwable t) {
            // Maybe it is not a Specification<Metadata>
        }
        try {
            tmp += metadataDraftRepository.count((Specification<MetadataDraft>) specs);
        } catch (Throwable t) {
            // Maybe it is not a Specification<MetadataDraft>
        }
        return tmp;
    }

    @Override
    public long count() {
        return super.count() + metadataDraftRepository.count();
    }

    @Override
    public IMetadata findOne(int id) {
        if (super.exists(id)) {
            return super.findOne(id);
        }
        return metadataDraftRepository.findOne(id);
    }

    /**
     * If the user has permission to see the draft, draft goes first
     */
    @Override
    public IMetadata findOneByUuid(String uuid) {
        IMetadata md = super.findOneByUuid(uuid);
        if (md != null && am.canEdit(context, Integer.toString(md.getId()))) {
            IMetadata tmp = metadataDraftRepository.findOneByUuid(uuid);
            if (tmp != null) {
                md = tmp;
            }
        } else if (md == null) {
            //This is a failback code which shouldn't be used. A draft without an md?
            //But in case of bug, at least don't leave orphans unreachable
            md = metadataDraftRepository.findOneByUuid(uuid);
            if(md != null) {
                Log.error(Geonet.DATA_MANAGER, "Found a drafted record without metadata: " + uuid);
            }
        }
        return md;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IMetadata findOne(Specification<? extends IMetadata> spec) {
        IMetadata md = super.findOne(spec);

        if (md == null) {
            try {
                md = metadataDraftRepository.findOne((Specification<MetadataDraft>) spec);
            } catch (Throwable t) {
                // Maybe it is not a Specification<MetadataDraft>
            }
        }

        return md;
    }

    @Override
    public IMetadata findOne(String id) {
        IMetadata md = super.findOne(id);
        if (md == null) {
            md = metadataDraftRepository.findOne(id);
        }
        return md;
    }

    @Override
    public List<? extends IMetadata> findAllByHarvestInfo_Uuid(String uuid) {
        List<IMetadata> list = new LinkedList<IMetadata>();
        list.addAll(metadataDraftRepository.findAllByHarvestInfo_Uuid(uuid));
        list.addAll(super.findAllByHarvestInfo_Uuid(uuid));
        return list;
    }

    @Override
    public Iterable<? extends IMetadata> findAll(Set<Integer> keySet) {
        List<IMetadata> list = new LinkedList<IMetadata>();
        for (IMetadata md : super.findAll(keySet)) {
            list.add(md);
        }
        list.addAll(metadataDraftRepository.findAll(keySet));
        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends IMetadata> findAll(Specification<? extends IMetadata> specs) {
        List<IMetadata> list = new LinkedList<IMetadata>();
        list.addAll(super.findAll(specs));
        try {
            list.addAll(metadataDraftRepository.findAll((Specification<MetadataDraft>) specs));
        } catch (Throwable t) {
            // Maybe it is not a Specification<MetadataDraft>
        }
        return list;
    }

    @Override
    public List<SimpleMetadata> findAllSimple(String harvestUuid) {
        List<SimpleMetadata> list = super.findAllSimple(harvestUuid);
        list.addAll(metadataDraftRepository.findAllSimple(harvestUuid));
        return list;
    }

    @Override
    public boolean exists(Integer iId) {
        return super.exists(iId) || metadataDraftRepository.exists(iId);
    }

    /**
     * TODO
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
    // TODO maybe we should add drafts here too?
    public MetadataReportsQueries getMetadataReports() {
        return super.getMetadataReports();
    }

    @Override
    // TODO maybe we should add drafts here too?
    public Element findAllAsXml(Specification<? extends IMetadata> specs, Sort sortByChangeDateDesc) {
        return super.findAllAsXml(specs, sortByChangeDateDesc);
    }

    @Override
    // TODO maybe we should add drafts here too?
    public Element findAllAsXml(@Nullable Specification<? extends IMetadata> specs, @Nullable Pageable pageable) {
        return super.findAllAsXml(specs, pageable);
    }

    @Override
    public Page<Pair<Integer, ISODate>> findAllIdsAndChangeDates(Pageable pageable) {
        List<Pair<Integer, ISODate>> list = new LinkedList<Pair<Integer, ISODate>>();

        list.addAll(super.findAllIdsAndChangeDates(pageable).getContent());
        list.addAll(metadataDraftRepository.findAllIdsAndChangeDates(pageable).getContent());

        Page<Pair<Integer, ISODate>> res = new PageImpl<Pair<Integer, ISODate>>(list, pageable, list.size());
        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Integer, MetadataSourceInfo> findAllSourceInfo(Specification<? extends IMetadata> spec) {
        Map<Integer, MetadataSourceInfo> map = super.findAllSourceInfo(spec);
        try {
            map.putAll(metadataDraftRepository.findAllSourceInfo((Specification<MetadataDraft>) spec));
        } catch (Throwable t) {
            // Maybe it is not a Specification<MetadataDraft>
        }
        return map;
    }
}
