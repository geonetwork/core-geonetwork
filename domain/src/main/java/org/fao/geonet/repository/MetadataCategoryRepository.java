package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataCategory;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Data Access object for accessing {@link MetadataCategory} entities.
 *
 * @author Jesse
 */
public interface MetadataCategoryRepository extends GeonetRepository<MetadataCategory, Integer>, MetadataCategoryRepositoryCustom,
        LocalizedEntityRepository {
    /**
     * Find the category with the given name.
     *
     * @param name the name
     * @return category
     */
    MetadataCategory findOneByName(String name);
}
