package org.fao.geonet.repository;

import org.fao.geonet.domain.Validation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link Validation} entities.
 * 
 * @author Jesse
 */
public interface ValidationRepository extends JpaRepository<Validation, Integer>, JpaSpecificationExecutor<Validation> {
}
