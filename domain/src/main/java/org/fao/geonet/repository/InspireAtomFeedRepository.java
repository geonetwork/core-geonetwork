package org.fao.geonet.repository;

import org.fao.geonet.domain.InspireAtomFeed;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


/**
 * Repository class for InspireAtomFeed.
 * Repository class for InspireAtomFeed.
 *
 * @author Jose García
 */
public interface InspireAtomFeedRepository extends GeonetRepository<InspireAtomFeed, Integer>,
        InspireAtomFeedRepositoryCustom, JpaSpecificationExecutor<InspireAtomFeed> {
    /**
     * Find an inspire atom feed related to a metadata.
     *
     * @param metadataId metadata identifier
     * @return the metadata related to the inspire atom feed
     */
    InspireAtomFeed findByMetadataId(final int metadataId);
}
