package org.fao.geonet.repository;

import javax.annotation.Nonnull;

import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.ReservedGroup;

/**
 * Custom (non-spring-data) query methods for {@link Group} entities
 *
 * @author Jesse
 */
public interface GroupRepositoryCustom {
    @Nonnull
    public Group findReservedGroup(@Nonnull ReservedGroup groupEnum);
}
