package org.fao.geonet.repository;

import org.fao.geonet.domain.HarvestHistory;
import org.fao.geonet.domain.MetadataCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Data Access object for accessing {@link MetadataCategory} entities.
 * 
 * @author Jesse
 */
public interface HarvestHistoryRepository extends GeonetRepository<HarvestHistory, Integer>, JpaSpecificationExecutor<HarvestHistory> {
    /**
     * Find all the HarvestHistory objects of the given type.
     *
     * @param harvesterType the harvester type
     */
    List<HarvestHistory> findAllByHarvesterType(String harvesterType);
}
