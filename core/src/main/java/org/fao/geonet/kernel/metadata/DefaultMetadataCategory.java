/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.kernel.SvnManager;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.Updater;
import org.springframework.beans.factory.annotation.Autowired;

import jeeves.server.context.ServiceContext;

/**
 * trunk-core
 * 
 * @author delawen
 * 
 * 
 */
public class DefaultMetadataCategory implements IMetadataCategory {

    @Autowired
    protected MetadataRepository mdRepository;

    @Autowired
    protected MetadataCategoryRepository categoryRepository;

    /**
     * @param context
     */
    @Override
    public void init(ServiceContext context) {
        this.categoryRepository = context
                .getBean(MetadataCategoryRepository.class);
        this.mdRepository = context.getBean(MetadataRepository.class);
    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataCategory#setCategory(jeeves.server.context.ServiceContext,
     *      java.lang.String, java.lang.String)
     * @param context
     * @param mdId
     * @param categId
     * @throws Exception
     */
    @Override
    public void setCategory(ServiceContext context, String mdId, String categId)
            throws Exception {

        final MetadataCategory newCategory = categoryRepository
                .findOne(Integer.valueOf(categId));
        final boolean[] changed = new boolean[1];
        mdRepository.update(Integer.valueOf(mdId), new Updater<Metadata>() {
            @Override
            public void apply(@Nonnull Metadata entity) {
                changed[0] = !entity.getMetadataCategories().contains(newCategory);
                entity.getMetadataCategories().add(newCategory);
            }
        });

        if (changed[0]) {
            SvnManager svnManager = context.getBean(SvnManager.class);
            if (svnManager != null) {
                svnManager.setHistory(mdId, context);
            }
        }
    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataCategory#isCategorySet(java.lang.String,
     *      int)
     * @param mdId
     * @param categId
     * @return
     * @throws Exception
     */
    @Override
    public boolean isCategorySet(final String mdId, final int categId)
            throws Exception {
        Set<MetadataCategory> categories = mdRepository.findOne(mdId)
                .getMetadataCategories();
        for (MetadataCategory category : categories) {
            if (category.getId() == categId) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataCategory#unsetCategory(jeeves.server.context.ServiceContext,
     *      java.lang.String, int)
     * @param context
     * @param mdId
     * @param categId
     * @throws Exception
     */
    @Override
    public void unsetCategory(final ServiceContext context, final String mdId,
            final int categId) throws Exception {
        Metadata metadata = mdRepository.findOne(mdId);

        if (metadata == null) {
            return;
        }
        boolean changed = false;
        for (MetadataCategory category : metadata.getMetadataCategories()) {
            if (category.getId() == categId) {
                changed = true;
                metadata.getMetadataCategories().remove(category);
                break;
            }
        }

        if (changed) {
            mdRepository.save(metadata);

            SvnManager svnManager = context.getBean(SvnManager.class);
            if (svnManager != null) {
                svnManager.setHistory(mdId, context);
            }
        }
    }

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataCategory#getCategories(java.lang.String)
     * @param mdId
     * @return
     * @throws Exception
     */
    @Override
    public Collection<MetadataCategory> getCategories(final String mdId)
            throws Exception {
        Metadata metadata = mdRepository.findOne(mdId);
        if (metadata == null) {
            throw new IllegalArgumentException(
                    "No metadata found with id: " + mdId);
        }

        return metadata.getMetadataCategories();
    }

}
