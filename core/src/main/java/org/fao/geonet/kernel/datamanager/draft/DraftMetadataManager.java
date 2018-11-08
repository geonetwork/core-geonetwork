package org.fao.geonet.kernel.datamanager.draft;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;

import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataManager;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.PathSpec;
import org.fao.geonet.repository.Updater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import jeeves.server.context.ServiceContext;

public class DraftMetadataManager extends BaseMetadataManager implements IMetadataManager {

	@Autowired
	private MetadataDraftRepository metadataDraftRepository;

	/**
	 * To avoid cyclic references on autowired
	 */
	@PostConstruct
	@Override
	public void init() {
		super.init();
	}

	public void init(ServiceContext context, Boolean force) throws Exception {
		metadataDraftRepository = context.getBean(MetadataDraftRepository.class);
		super.init(context, force);
	}

	/**
	 * For update of owner info.
	 */
	@Override
	public synchronized void updateMetadataOwner(final int id, final String owner, final String groupOwner)
			throws Exception {
		try {
			metadataDraftRepository.update(id, new Updater<MetadataDraft>() {
				@Override
				public void apply(@Nonnull MetadataDraft entity) {
					entity.getSourceInfo().setGroupOwner(Integer.valueOf(groupOwner));
					entity.getSourceInfo().setOwner(Integer.valueOf(owner));
				}
			});
		} catch (EntityNotFoundException e) {
			super.updateMetadataOwner(id, owner, groupOwner);
		}
	}

	@Override
	public AbstractMetadata save(AbstractMetadata info) {
		if (info instanceof Metadata) {
			return super.save((Metadata) info);
		} else if (info instanceof MetadataDraft) {
			return metadataDraftRepository.save((MetadataDraft) info);
		} else {
			throw new ClassCastException("Unknown AbstractMetadata subtype: " + info.getClass().getName());
		}
	}

	@Override
	public AbstractMetadata update(int id, @Nonnull Updater<? extends AbstractMetadata> updater) {
		AbstractMetadata md = null;
		try {
			md = super.update(id, updater);
		} catch (ClassCastException t) {
			// That's fine, maybe we are on the draft side
		}
		if (md == null) {
			try {
				md = metadataDraftRepository.update(id, (Updater<MetadataDraft>) updater);
			} catch (ClassCastException t) {
				throw new ClassCastException("Unknown AbstractMetadata subtype: " + updater.getClass().getName());
			}
		}
		return md;
	}

	@Override
	public void deleteAll(Specification<? extends AbstractMetadata> specs) {
		try {
			super.deleteAll(specs);
		} catch (ClassCastException t) {
			// That's fine, maybe we are on the draft side
		}
		try {
			metadataDraftRepository.deleteAll((Specification<MetadataDraft>) specs);
		} catch (ClassCastException t) {
			throw new ClassCastException("Unknown AbstractMetadata subtype: " + specs.getClass().getName());
		}
	}

	@Override
	public void delete(Integer id) {
		super.delete(id);
		if (metadataDraftRepository.exists(id)) {
			metadataDraftRepository.delete(id);
		}
	}

	@Override
	public void createBatchUpdateQuery(PathSpec<? extends AbstractMetadata, String> servicesPath, String newUuid,
			Specification<? extends AbstractMetadata> harvested) {
		try {
			super.createBatchUpdateQuery(servicesPath, newUuid, harvested);
		} catch (ClassCastException t) {
			// That's fine, maybe we are on the draft side
		}
		try {
			metadataDraftRepository.createBatchUpdateQuery((PathSpec<MetadataDraft, String>) servicesPath, newUuid,
					(Specification<MetadataDraft>) harvested);
		} catch (ClassCastException t) {
			throw new ClassCastException("Unknown AbstractMetadata subtype: " + servicesPath.getClass().getName());
		}

	}
}