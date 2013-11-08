package org.fao.geonet.repository.statistic;

import org.fao.geonet.domain.statistic.SearchRequestParam;
import org.fao.geonet.repository.GeonetRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.MetadataCategory} entities.
 *
 * @author Jesse
 */
public interface SearchRequestParamRepository extends GeonetRepository<SearchRequestParam, Integer>,
        SearchRequestParamRepositoryCustom, JpaSpecificationExecutor<SearchRequestParam> {
}
