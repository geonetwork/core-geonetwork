package org.fao.geonet.kernel.datamanager.draft;

import javax.annotation.Nonnull;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.kernel.datamanager.IMetadataCategory;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataCategory;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.Updater;
import org.springframework.beans.factory.annotation.Autowired;

import jeeves.server.context.ServiceContext;

public class DraftMetadataCategory extends BaseMetadataCategory implements IMetadataCategory {

    @Autowired
    private MetadataDraftRepository metadataDraftRepository;

    public void init(ServiceContext context, Boolean force) throws Exception {
        super.init(context, force);
        this.metadataDraftRepository = context.getBean(MetadataDraftRepository.class);
    }

    /**
     * Adds a category to a metadata. Metadata is not reindexed.
     */
    @Override
    public void setCategory(ServiceContext context, String mdId, String categId) throws Exception {

        final MetadataCategory newCategory = getMetadataCategoryRepository().findOne(Integer.valueOf(categId));
        final boolean[] changed = new boolean[1];
        getMetadataRepository().update(Integer.valueOf(mdId), new Updater<Metadata>() {
            @Override
            public void apply(@Nonnull Metadata entity) {
                changed[0] = !entity.getMetadataCategories().contains(newCategory);
                entity.getMetadataCategories().add(newCategory);
            }
        });
        metadataDraftRepository.update(Integer.valueOf(mdId), new Updater<MetadataDraft>() {
            @Override
            public void apply(@Nonnull MetadataDraft entity) {
                changed[0] = !entity.getMetadataCategories().contains(newCategory);
                entity.getCategories().add(newCategory);
            }
        });

        if (changed[0]) {
            if (getSvnManager() != null) {
                getSvnManager().setHistory(mdId, context);
            }
        }
    }
}
