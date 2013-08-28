package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataRatingByIp;
import org.fao.geonet.domain.MetadataRatingByIpId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Data Access object for accessing {@link MetadataRatingByIp} entities.
 * 
 * @author Jesse
 */
public interface MetadataRatingByIpRepository extends JpaRepository<MetadataRatingByIp, MetadataRatingByIpId>, JpaSpecificationExecutor<MetadataRatingByIp> {
    /**
     * Find all the ratings for the given Metadata.
     *
     * @param metadataId id of metadata.
     */
    List<MetadataRatingByIp> findAllByIdMetadataId(int metadataId);
}
