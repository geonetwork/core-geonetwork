package org.fao.geonet.kernel.datamanager.base;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.kernel.SvnManager;
import org.fao.geonet.kernel.datamanager.IMetadataCategory;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.Updater;
import org.springframework.beans.factory.annotation.Autowired;

import jeeves.server.context.ServiceContext;

public class BaseMetadataCategory implements IMetadataCategory {

    @Autowired
    private IMetadataUtils metadataUtils;
    @Autowired
    private MetadataRepository metadataRepository;
    @Autowired(required = false)
    private SvnManager svnManager;
    @Autowired
    private MetadataCategoryRepository metadataCategoryRepository;

    public void init(ServiceContext context, Boolean force) throws Exception {
        this.metadataUtils = context.getBean(IMetadataUtils.class);
        this.svnManager = context.getBean(SvnManager.class);
        this.metadataCategoryRepository = context.getBean(MetadataCategoryRepository.class);
    }

    /**
     * Adds a category to a metadata. Metadata is not reindexed.
     */
    @Override
    public void setCategory(ServiceContext context, String mdId, String categId) throws Exception {

        final MetadataCategory newCategory = metadataCategoryRepository.findOne(Integer.valueOf(categId));
        final boolean[] changed = new boolean[1];
        getMetadataRepository().update(Integer.valueOf(mdId), new Updater<Metadata>() {
            @Override
            public void apply(@Nonnull Metadata entity) {
                changed[0] = !entity.getMetadataCategories().contains(newCategory);
                entity.getMetadataCategories().add(newCategory);
            }
        });

        if (changed[0]) {
            if (getSvnManager() != null) {
                getSvnManager().setHistory(mdId, context);
            }
        }
    }

    /**
     *
     * @param mdId
     * @param categId
     * @return
     * @throws Exception
     */
    @Override
    public boolean isCategorySet(final String mdId, final int categId) throws Exception {
        Set<MetadataCategory> categories = getMetadataUtils().findOne(mdId).getCategories();
        for (MetadataCategory category : categories) {
            if (category.getId() == categId) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param mdId
     * @param categId
     * @throws Exception
     */
    @Override
    public void unsetCategory(final ServiceContext context, final String mdId, final int categId) throws Exception {
        AbstractMetadata metadata = getMetadataUtils().findOne(mdId);

        if (metadata == null) {
            return;
        }
        boolean changed = false;
        for (MetadataCategory category : metadata.getCategories()) {
            if (category.getId() == categId) {
                changed = true;
                metadata.getCategories().remove(category);
                break;
            }
        }

        if (changed) {
            context.getBean(IMetadataManager.class).save(metadata);
            if (getSvnManager() != null) {
                getSvnManager().setHistory(mdId + "", context);
            }
        }
    }

    /**
     *
     * @param mdId
     * @return
     * @throws Exception
     */
    @Override
    public Collection<MetadataCategory> getCategories(final String mdId) throws Exception {
        AbstractMetadata metadata = getMetadataUtils().findOne(mdId);
        if (metadata == null) {
            throw new IllegalArgumentException("No metadata found with id: " + mdId);
        }

        return metadata.getCategories();
    }

    protected SvnManager getSvnManager() {
        return svnManager;

    }

    protected IMetadataUtils getMetadataUtils() {
        return metadataUtils;

    }
    
    protected MetadataCategoryRepository getMetadataCategoryRepository() {
        return metadataCategoryRepository;
    }
    
    protected MetadataRepository getMetadataRepository() {
        return metadataRepository;
    }
}
