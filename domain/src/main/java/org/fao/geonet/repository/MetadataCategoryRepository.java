package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataCategory;

/**
 * Data Access object for accessing {@link MetadataCategory} entities.
 *
 * @author Jesse
 */
public interface MetadataCategoryRepository extends GeonetRepository<MetadataCategory, Integer>, MetadataCategoryRepositoryCustom {
    /**
     * Find the category with the given name.
     *
     * @param name the name
     * @return category
     */
    MetadataCategory findOneByName(String name);
}
