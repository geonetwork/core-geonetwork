package org.fao.geonet.repository;

import org.fao.geonet.domain.StatusValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link StatusValue} entities.
 * 
 * @author Jesse
 */
public interface StatusValueRepository extends JpaRepository<StatusValue, Integer>, JpaSpecificationExecutor<StatusValue> {
}
