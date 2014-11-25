package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataCategory;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link MetadataCategory} entities.
 *
 * @author Jesse
 */
public interface MetadataCategoryRepository extends GeonetRepository<MetadataCategory, Integer>, MetadataCategoryRepositoryCustom,
        JpaSpecificationExecutor<MetadataCategory> {
    /**
     * Find the category with the given name.
     *
     * @param name the name
     * @return category
     */
    MetadataCategory findOneByName(String name);
}
