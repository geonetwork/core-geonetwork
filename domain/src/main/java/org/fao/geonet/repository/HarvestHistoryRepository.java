package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link MetadataCategory} entities.
 * 
 * @author Jesse
 */
public interface HarvestHistoryRepository extends JpaRepository<MetadataCategory, Integer>, JpaSpecificationExecutor<MetadataCategory> {
}
