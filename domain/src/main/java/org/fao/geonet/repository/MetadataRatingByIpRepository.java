package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataRatingByIp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link MetadataRatingByIp} entities.
 * 
 * @author Jesse
 */
public interface MetadataRatingByIpRepository extends JpaRepository<MetadataRatingByIp, Integer>, JpaSpecificationExecutor<MetadataRatingByIp> {
}
