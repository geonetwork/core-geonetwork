package org.fao.geonet.repository;

import org.fao.geonet.domain.Address;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.MetadataCategory} entities.
 *
 * @author Jesse
 */
public interface AddressRepository extends GeonetRepository<Address, Integer>, JpaSpecificationExecutor<Address> {
    /**
     * Find all the addresses in the given zip code.
     *
     * @param zip the zip code
     * @return all the addresses in the given zip code
     */
    List<Address> findAllByZip(String zip);
}
