package org.fao.geonet.repository;

import org.fao.geonet.domain.StatusValue;

/**
 * Data Access object for accessing {@link StatusValue} entities.
 *
 * @author Jesse
 */
public interface StatusValueRepository extends GeonetRepository<StatusValue, Integer> {
    StatusValue findOneByName(String statusValueName);
    StatusValue findOneById(int statusValueId);
}
