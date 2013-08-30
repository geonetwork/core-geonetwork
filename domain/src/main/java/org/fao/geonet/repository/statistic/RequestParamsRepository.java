package org.fao.geonet.repository.statistic;

import org.fao.geonet.domain.MetadataRelation;
import org.fao.geonet.domain.MetadataRelationId;
import org.fao.geonet.domain.statistic.RequestParams;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.MetadataCategory} entities.
 *
 * @author Jesse
 */
public interface RequestParamsRepository extends JpaRepository<RequestParams, Integer>,
        JpaSpecificationExecutor<RequestParams> {
}
