package org.fao.geonet.repository.statistic;

import org.fao.geonet.domain.MetadataRelation;
import org.fao.geonet.domain.MetadataRelationId;
import org.fao.geonet.domain.statistic.SearchRequest;
import org.fao.geonet.repository.GeonetRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.MetadataCategory} entities.
 *
 * @author Jesse
 */
public interface SearchRequestRepository extends GeonetRepository<SearchRequest, Integer>,
        JpaSpecificationExecutor<SearchRequest> {
}
