package org.fao.geonet.kernel.datamanager.draft;

import static org.fao.geonet.repository.specification.MetadataSpecs.hasMetadataUuid;
import static org.springframework.data.jpa.domain.Specifications.where;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Group;
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
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.geotools.coverage.processing.Operations;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
			metadataDraftRepository.update(id, new Updater<MetadataDraft>() {
				@Override
				public void apply(@Nonnull MetadataDraft metadata) {
					final MetadataDataInfo dataInfo = metadata.getDataInfo();
					dataInfo.setType(metadataType);
				}
			});
		} catch (EntityNotFoundException e) {
			super.setTemplateExt(id, metadataType);
		}
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
	public void setHarvestedExt(final int id, final String harvestUuid, final Optional<String> harvestUri)
			throws Exception {
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
	 * @param ipAddress
	 *            ipAddress IP address of the submitting client
	 * @param rating
	 *            range should be 1..5
	 * @throws Exception
	 *             hmm
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
	public long count(Specification<? extends AbstractMetadata> specs) {
		long tmp = 0;
		try {
			tmp += super.count((Specification<Metadata>) specs);
		} catch (ClassCastException t) {
			// Maybe it is not a Specification<Metadata>
		}
		try {
			tmp += metadataDraftRepository.count((Specification<MetadataDraft>) specs);
		} catch (ClassCastException t) {
			// Maybe it is not a Specification<MetadataDraft>
		}
		return tmp;
	}

	@Override
	public long count() {
		return super.count() + metadataDraftRepository.count();
	}

	@Override
	public AbstractMetadata findOne(int id) {
		if (super.exists(id)) {
			return super.findOne(id);
		}
		return metadataDraftRepository.findOne(id);
	}

	@Override
	public boolean existsMetadataUuid(String uuid) throws Exception {
		return super.existsMetadataUuid(uuid) || !findAllIdsBy(hasMetadataUuid(uuid)).isEmpty();
	}
	
	/**
	 * If the user has permission to see the draft, draft goes first
	 */
	@Override
	public AbstractMetadata findOneByUuid(String uuid) {
		AbstractMetadata md = super.findOneByUuid(uuid);
		try {
			if (md != null && am.canEdit(context, Integer.toString(md.getId()))) {
				AbstractMetadata tmp = metadataDraftRepository.findOneByUuid(uuid);
				if (tmp != null) {
					md = tmp;
				}
			} else if (md == null) {
				// A draft without an approved md
				md = metadataDraftRepository.findOneByUuid(uuid);
			}
		} catch (Exception e) {
			Log.error(Geonet.DATA_MANAGER, e);
		}
		return md;
	}

	@Override
	public AbstractMetadata findOne(Specification<? extends AbstractMetadata> spec) {
		AbstractMetadata md = null;

		try {
			md = super.findOne(spec);
		} catch (ClassCastException t) {
			// That's fine, it can be a draft specification
		}

		if (md == null) {
			try {
				md = metadataDraftRepository.findOne((Specification<MetadataDraft>) spec);
			} catch (ClassCastException t) {
				throw new ClassCastException("Unknown AbstractMetadata subtype: " + spec.getClass().getName());
			}
		}

		return md;
	}

	@Override
	public AbstractMetadata findOne(String id) {
		AbstractMetadata md = super.findOne(id);
		if (md == null) {
			md = metadataDraftRepository.findOne(id);
		}
		return md;
	}

	@Override
	public List<? extends AbstractMetadata> findAllByHarvestInfo_Uuid(String uuid) {
		List<AbstractMetadata> list = new LinkedList<AbstractMetadata>();
		list.addAll(metadataDraftRepository.findAllByHarvestInfo_Uuid(uuid));
		list.addAll(super.findAllByHarvestInfo_Uuid(uuid));
		return list;
	}

	@Override
	public Iterable<? extends AbstractMetadata> findAll(Set<Integer> keySet) {
		List<AbstractMetadata> list = new LinkedList<AbstractMetadata>();
		for (AbstractMetadata md : super.findAll(keySet)) {
			list.add(md);
		}
		list.addAll(metadataDraftRepository.findAll(keySet));
		return list;
	}

	@Override
	public List<? extends AbstractMetadata> findAll(Specification<? extends AbstractMetadata> specs) {
		List<AbstractMetadata> list = new LinkedList<AbstractMetadata>();
		try {
			list.addAll(super.findAll(specs));
		} catch (ClassCastException t) {
			// That's fine, maybe it is a draft specification
		}
		try {
			list.addAll(metadataDraftRepository.findAll((Specification<MetadataDraft>) specs));
		} catch (ClassCastException t) {
			// That's fine, maybe it is a metadata specification
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
	public Page<Pair<Integer, ISODate>> findAllIdsAndChangeDates(Pageable pageable) {
		List<Pair<Integer, ISODate>> list = new LinkedList<Pair<Integer, ISODate>>();

		list.addAll(super.findAllIdsAndChangeDates(pageable).getContent());
		list.addAll(metadataDraftRepository.findAllIdsAndChangeDates(pageable).getContent());

		Page<Pair<Integer, ISODate>> res = new PageImpl<Pair<Integer, ISODate>>(list, pageable, list.size());
		return res;
	}

	@Override
	public Map<Integer, MetadataSourceInfo> findAllSourceInfo(Specification<? extends AbstractMetadata> spec) {
		Map<Integer, MetadataSourceInfo> map = new LinkedHashMap<Integer, MetadataSourceInfo>();
		try {
			map.putAll(super.findAllSourceInfo(spec));
		} catch (ClassCastException t) {
			// Maybe it is not a Specification<Metadata>
		}

		try {
			map.putAll(metadataDraftRepository.findAllSourceInfo((Specification<MetadataDraft>) spec));
		} catch (ClassCastException t) {
			// Maybe it is not a Specification<MetadataDraft>
		}
		return map;
	}
	
	@Override
	public List<Integer> findAllIdsBy(Specification<? extends AbstractMetadata> specs) {
		List<Integer> res = new LinkedList<Integer>();

		try {
			res.addAll(super.findAllIdsBy(specs));
		} catch (ClassCastException t) {
			// Maybe it is not a Specification<Metadata>
		}
		
		try {
			res.addAll(metadataDraftRepository.findAllIdsBy((Specification<MetadataDraft>) specs));
		} catch (ClassCastException t) {
			// Maybe it is not a Specification<MetadataDraft>
		}
		
		return res;
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
		// Check id
		AbstractMetadata md = findOne(Integer.valueOf(id));
		
		if(md == null) {
			throw new EntityNotFoundException("We couldn't find the metadata to edit");
		}

		// Do we have a metadata draft already?
		 if (metadataDraftRepository.findOneByUuid(md.getUuid()) != null) {
			id = Integer.toString(metadataDraftRepository.findOneByUuid(md.getUuid()).getId());
		} else {
			// We have to create the draft using the metadata information
			String parentUuid = "";
			String schemaIdentifier = metadataSchemaUtils.getMetadataSchema(id);
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

			id = createDraft(context, id, groupOwner, source, owner, parentUuid,
					md.getDataInfo().getType().codeString, md.getUuid());		
		}

		return super.startEditingSession(context, id);
	}

	private String createDraft(ServiceContext context, String templateId, String groupOwner, String source, int owner,
			String parentUuid, String isTemplate, String uuid) throws Exception {
		Metadata templateMetadata = getMetadataRepository().findOne(templateId);
		if (templateMetadata == null) {
			throw new IllegalArgumentException("Template id not found : " + templateId);
		}

		String schema = templateMetadata.getDataInfo().getSchemaId();
		String data = templateMetadata.getData();
		Element xml = Xml.loadString(data, false);
		if (templateMetadata.getDataInfo().getType() == MetadataType.METADATA) {
			xml = metadataManager.updateFixedInfo(schema, Optional.<Integer>absent(), uuid, xml, parentUuid,
					UpdateDatestamp.NO, context);
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

		for (MetadataCategory mc : templateMetadata.getCategories()) {
			newMetadata.getCategories().add(mc);
		}

		try {
			Integer finalId = metadataManager.insertMetadata(context, newMetadata, xml, false, true, true,
					UpdateDatestamp.YES, false, true).getId();
			
			//Remove all default privileges:
			metadataOperations.deleteMetadataOper(context, String.valueOf(finalId), false);

			// Copy privileges from original metadata
			for (OperationAllowed op : metadataOperations.getAllOperations(templateMetadata.getId())) { 
				
				//Only interested in editing and reviewing privileges
				//No one else should be able to see it
				if(op.getId().getOperationId() == ReservedOperation.editing.getId()) {
					//except for reserved groups
					Group g = groupRepository.findOne(op.getId().getGroupId());
					if (!g.isReserved()) {
						metadataOperations.forceSetOperation(context, finalId, op.getId().getGroupId(),
								op.getId().getOperationId());
					}
				}
			}

			//We have to index both draft and approved metadata to get relation on the index
			metadataIndexer.indexMetadata(String.valueOf(finalId), true, null);

			metadataIndexer.indexMetadata(String.valueOf(templateId), true, null);

			return String.valueOf(finalId);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return templateId;
	}

}
