package org.fao.geonet.kernel.repository;

import org.fao.geonet.kernel.domain.Metadata;

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
    Metadata findByIdString(String id);
}
