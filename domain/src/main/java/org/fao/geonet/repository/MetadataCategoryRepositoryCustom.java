package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataCategory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Custom methods for finding and saving MetadataCategory entities.
 * <p/>
 * User: Jesse
 * Date: 9/10/13
 * Time: 7:23 AM
 */
public interface MetadataCategoryRepositoryCustom {
    /**
     * Find the metadata category with the given name ignoring the case of the name.
     *
     * @param name the name to use as the key.
     * @return a metadata category or null
     */
    @Nullable
    MetadataCategory findOneByNameIgnoreCase(@Nonnull String name);

    /**
     * Remove category from all metadata that references it and delete the category from the table.
     *
     * @param id id of category.
     */
    void deleteCategoryAndMetadataReferences(int id);
}
