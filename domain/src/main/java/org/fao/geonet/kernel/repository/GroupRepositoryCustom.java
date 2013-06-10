package org.fao.geonet.kernel.repository;

import javax.annotation.Nonnull;

import org.fao.geonet.kernel.domain.Group;
import org.fao.geonet.kernel.domain.ReservedGroup;

/**
 * Custom (non-spring-data) query methods
 *
 * @author Jesse
 */
public interface GroupRepositoryCustom {
    @Nonnull
    public Group findReservedGroup(@Nonnull ReservedGroup groupEnum);
}
