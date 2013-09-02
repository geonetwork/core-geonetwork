package org.fao.geonet.repository.statistic;

import org.fao.geonet.domain.statistic.SearchRequestParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.MetadataCategory} entities.
 *
 * @author Jesse
 */
public interface SearchRequestParamRepository extends JpaRepository<SearchRequestParam, Integer>,
        JpaSpecificationExecutor<SearchRequestParam> {
}
