//=============================================================================
//===	Copyright (C) 2001-2011 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.datamanager.draft;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataManager;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.PathSpec;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

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
     * Removes a metadata.
     */
    @Override
    public void deleteMetadata(ServiceContext context, String metadataId) throws Exception {
        AbstractMetadata findOne = metadataUtils.findOne(metadataId);

        if (findOne != null) {
            boolean isMetadata = findOne.getDataInfo().getType() == MetadataType.METADATA;


            if (findOne instanceof Metadata) {
                // Check if exists draft version and don't allow to remove until draft is removed
                long countDraft =  metadataDraftRepository.count((Specification<MetadataDraft>) MetadataSpecs.hasMetadataUuid(findOne.getUuid()));

                if (countDraft > 0) {
                    throw new Exception("The metadata " + findOne.getUuid() + " has a draft version. Cancel the modification to be able to remove the approved version.");
                }
            }

            deleteMetadataFromDB(context, metadataId);
        }

        // --- update search criteria
        getSearchManager().delete(String.format("+id:%s", metadataId));
        // _entityManager.flush();
        // _entityManager.clear();
    }

    /**
     * For update of owner info.
     */
    @Override
    public synchronized void updateMetadataOwner(final int id, final String owner, final String groupOwner)
        throws Exception {

        if (metadataDraftRepository.existsById(id)) {
            metadataDraftRepository.update(id, new Updater<MetadataDraft>() {
                @Override
                public void apply(@Nonnull MetadataDraft entity) {
                    entity.getSourceInfo().setGroupOwner(Integer.valueOf(groupOwner));
                    entity.getSourceInfo().setOwner(Integer.valueOf(owner));
                }
            });
        } else {
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
        Log.trace(Geonet.DATA_MANAGER, "AbstractMetadata.update(" + id + ")");
        try {
            Log.trace(Geonet.DATA_MANAGER, "Updating metadata table.");
            md = super.update(id, updater);
        } catch (ClassCastException t) {
            // That's fine, maybe we are on the draft side
        } catch (Throwable e) {
            Log.error(Geonet.DATA_MANAGER, e.getMessage(), e);
        }
        if (md == null) {
            try {
                Log.trace(Geonet.DATA_MANAGER, "Updating draft table.");
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
        if (metadataDraftRepository.existsById(id)) {
            metadataDraftRepository.deleteById(id);
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

    @Override
    public Map<Integer, MetadataSourceInfo> findAllSourceInfo(Specification<? extends AbstractMetadata> specs) {
        Map<Integer, MetadataSourceInfo> res = new HashMap<Integer, MetadataSourceInfo>();
        try {
            res.putAll(super.findAllSourceInfo(specs));
        } catch (ClassCastException t) {
            // That's fine, maybe we are on the draft side
        }
        try {
            res.putAll(metadataDraftRepository.findSourceInfo((Specification<MetadataDraft>) specs));
        } catch (ClassCastException t) {
            // That's fine, maybe we are on the metadata side
        }

        return res;
    }

}
