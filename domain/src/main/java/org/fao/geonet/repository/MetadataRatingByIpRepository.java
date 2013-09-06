package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataRatingByIp;
import org.fao.geonet.domain.MetadataRatingByIpId;

import java.util.List;

/**
 * Data Access object for accessing {@link MetadataRatingByIp} entities.
 *
 * @author Jesse
 */
public interface MetadataRatingByIpRepository extends GeonetRepository<MetadataRatingByIp, MetadataRatingByIpId>,
        MetadataRatingByIpRepositoryCustom {
    /**
     * Find all the ratings for the given Metadata.
     *
     * @param metadataId id of metadata.
     */
    List<MetadataRatingByIp> findAllByIdMetadataId(int metadataId);

}
