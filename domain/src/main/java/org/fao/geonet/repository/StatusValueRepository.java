package org.fao.geonet.repository;

import org.fao.geonet.domain.StatusValue;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link StatusValue} entities.
 *
 * @author Jesse
 */
public interface StatusValueRepository extends GeonetRepository<StatusValue, Integer>, JpaSpecificationExecutor<StatusValue> {
}
