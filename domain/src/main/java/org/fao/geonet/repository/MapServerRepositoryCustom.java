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
     * @param userId the userid.
     * @return the use with the given userid
     */
    @Nullable
    MapServer findOne(@Nonnull String id);
}