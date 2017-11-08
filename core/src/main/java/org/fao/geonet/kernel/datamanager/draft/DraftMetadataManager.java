package org.fao.geonet.kernel.datamanager.draft;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang.NotImplementedException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.IMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataManager;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.PathSpec;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    public synchronized void updateMetadataOwner(final int id, final String owner, final String groupOwner) throws Exception {
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
    @Transactional(readOnly=false, isolation=Isolation.READ_COMMITTED, propagation=Propagation.REQUIRES_NEW)
    public IMetadata save(IMetadata info) {
        if (info instanceof Metadata) {
            return super.save((Metadata) info);
        } else if (info instanceof MetadataDraft) {
            try {
                return metadataDraftRepository.save((MetadataDraft) info);
            } catch (Throwable t) {
                Log.error(Geonet.DATA_MANAGER, t.getMessage(), t);
            }
        } else {
            throw new NotImplementedException("Unknown IMetadata subtype: " + info.getClass().getName());
        }
        return info;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IMetadata update(int id, @Nonnull Updater<? extends IMetadata> updater) {
        super.update(id, updater);
        return metadataDraftRepository.update(id, (Updater<MetadataDraft>) updater);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deleteAll(Specification<? extends IMetadata> specs) {
        super.deleteAll(specs);
        try {
            metadataDraftRepository.deleteAll((Specification<MetadataDraft>) specs);
        } catch (Throwable t) {
            // Maybe it is not a Specification<MetadataDraft>
        }
    }

    @Override
    @Transactional(readOnly = false, isolation = Isolation.READ_COMMITTED, noRollbackFor = { EmptyResultDataAccessException.class })
    public void delete(Integer id) {
        super.delete(id);
        if (metadataDraftRepository.exists(id)) {
            metadataDraftRepository.delete(id);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void createBatchUpdateQuery(PathSpec<? extends IMetadata, String> servicesPath, String newUuid,
            Specification<? extends IMetadata> harvested) {
        metadataDraftRepository.createBatchUpdateQuery((PathSpec<MetadataDraft, String>) servicesPath, newUuid,
                (Specification<MetadataDraft>) harvested);
        super.createBatchUpdateQuery(servicesPath, newUuid, harvested);
    }
}