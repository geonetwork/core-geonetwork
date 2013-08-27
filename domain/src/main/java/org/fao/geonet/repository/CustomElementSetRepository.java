package org.fao.geonet.repository;

import org.fao.geonet.domain.CustomElementSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link CustomElementSet} entities.
 * 
 * @author Jesse
 */
public interface CustomElementSetRepository extends JpaRepository<CustomElementSet, String> {
}
