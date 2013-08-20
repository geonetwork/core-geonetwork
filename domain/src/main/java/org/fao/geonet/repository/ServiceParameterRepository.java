package org.fao.geonet.repository;

import java.util.List;

import org.fao.geonet.domain.ServiceParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link ServiceParameter} entities.
 * 
 * @author Jesse
 */
public interface ServiceParameterRepository extends JpaRepository<ServiceParameter, Integer>, JpaSpecificationExecutor<ServiceParameter> {
}
