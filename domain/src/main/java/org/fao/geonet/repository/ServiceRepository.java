package org.fao.geonet.repository;

import org.fao.geonet.domain.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link Service} entities.
 * 
 * @author Jesse
 */
public interface ServiceRepository extends JpaRepository<Service, Integer>, JpaSpecificationExecutor<Service> {
}
