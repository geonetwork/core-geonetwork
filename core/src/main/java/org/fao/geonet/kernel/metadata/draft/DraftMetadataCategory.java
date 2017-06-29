/**
 * 
 */
package org.fao.geonet.kernel.metadata.draft;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.kernel.SvnManager;
import org.fao.geonet.kernel.metadata.DefaultMetadataCategory;
import org.fao.geonet.repository.MetadataDraftRepository;
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
public class DraftMetadataCategory extends DefaultMetadataCategory {

    @Autowired
    private MetadataDraftRepository mdDraftRepository;

    /**
     * @see org.fao.geonet.kernel.metadata.DefaultMetadataCategory#getCategories(java.lang.String)
     * @param mdId
     * @return
     * @throws Exception
     */
    @Override
    public Collection<MetadataCategory> getCategories(String mdId)
            throws Exception {
        Metadata md = mdRepository.findOne(mdId);
        if (md != null) {
            return super.getCategories(mdId);
        }

        MetadataDraft mdD = mdDraftRepository.findOne(mdId);

        if (mdD == null) {
            throw new IllegalArgumentException(
                    "No metadata found with id: " + mdId);
        }

        return mdD.getMetadataCategories();
    }

    /**
     * @see org.fao.geonet.kernel.metadata.DefaultMetadataCategory#isCategorySet(java.lang.String,
     *      int)
     * @param mdId
     * @param categId
     * @return
     * @throws Exception
     */
    @Override
    public boolean isCategorySet(String mdId, int categId) throws Exception {

        Metadata md = mdRepository.findOne(mdId);
        if (md != null) {
            return super.isCategorySet(mdId, categId);
        }

        Set<MetadataCategory> categories = mdDraftRepository.findOne(mdId)
                .getMetadataCategories();
        for (MetadataCategory category : categories) {
            if (category.getId() == categId) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see org.fao.geonet.kernel.metadata.DefaultMetadataCategory#setCategory(jeeves.server.context.ServiceContext,
     *      java.lang.String, java.lang.String)
     * @param context
     * @param mdId
     * @param categId
     * @throws Exception
     */
    @Override
    public void setCategory(ServiceContext context, String mdId, String categId)
            throws Exception {
        Metadata md = mdRepository.findOne(mdId);
        if (md != null) {
            super.setCategory(context, mdId, categId);
        } else {
            final MetadataCategory newCategory = categoryRepository
                    .findOne(Integer.valueOf(categId));
            final boolean[] changed = new boolean[1];
            mdDraftRepository.update(Integer.valueOf(mdId),
                    new Updater<MetadataDraft>() {
                        @Override
                        public void apply(@Nonnull MetadataDraft entity) {
                            changed[0] = !entity.getMetadataCategories()
                                    .contains(newCategory);
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
    }

    /**
     * @see org.fao.geonet.kernel.metadata.DefaultMetadataCategory#unsetCategory(jeeves.server.context.ServiceContext,
     *      java.lang.String, int)
     * @param context
     * @param mdId
     * @param categId
     * @throws Exception
     */
    @Override
    public void unsetCategory(ServiceContext context, String mdId, int categId)
            throws Exception {
        Metadata md = mdRepository.findOne(mdId);
        if (md != null) {
            super.unsetCategory(context, mdId, categId);
        } else {
            MetadataDraft metadata = mdDraftRepository.findOne(mdId);

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
                mdDraftRepository.save(metadata);

                SvnManager svnManager = context.getBean(SvnManager.class);
                if (svnManager != null) {
                    svnManager.setHistory(mdId, context);
                }
            }
        }
    }

    /**
     * @see org.fao.geonet.kernel.metadata.DefaultMetadataCategory#init(jeeves.server.context.ServiceContext)
     * @param context
     */
    @Override
    public void init(ServiceContext context) {
        super.init(context);
        this.mdDraftRepository = context.getBean(MetadataDraftRepository.class);
    }
}
