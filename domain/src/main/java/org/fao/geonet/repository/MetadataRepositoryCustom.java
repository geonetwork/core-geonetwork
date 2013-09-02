package org.fao.geonet.repository;

import org.fao.geonet.domain.Metadata;

/**
 * Custom (Non spring-data) Query methods for {@link Metadata} entities.
 *
 * @author Jesse
 */
public interface MetadataRepositoryCustom {
    /**
     * Permit finding a metadata by its ids as a string.
     *
     * The id needs to be convertable to an integer
     *
     * This is just short for repository.findOne(Integer.parseInt(id))
     *
     * @param id the id in string form instead of integer.
     * @return
     */
    Metadata findOne(String id);
}
