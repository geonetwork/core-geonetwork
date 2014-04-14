package org.fao.geonet.repository;

import org.fao.geonet.domain.MapServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.MapServer} entities.
 *
 * @author Francois
 */
public interface MapServerRepositoryCustom {
    /**
     * Find the mapserver with the given id
     * (where id is a string). The string will be
     * converted to an integer for making the query.
     *
     * @param id the mapserver id.
     * @return the mapserver with the given id
     */
    @Nullable
    MapServer findOneById(@Nonnull String id);
}