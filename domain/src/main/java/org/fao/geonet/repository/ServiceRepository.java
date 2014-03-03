package org.fao.geonet.repository;

import org.fao.geonet.domain.Service;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link Service} entities.
 *
 * @author Jesse
 */
public interface ServiceRepository extends GeonetRepository<Service, Integer>, JpaSpecificationExecutor<Service> {
    /**
     * Look up a service by name
     *
     * @param name the name of the service
     */
    public Service findOneByName(String name);
}
