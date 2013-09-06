package org.fao.geonet.repository;

import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.ReservedGroup;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Custom (non-spring-data) query methods for {@link Group} entities
 *
 * @author Jesse
 */
public interface GroupRepositoryCustom {
    /**
     * Find a group given one of the Reserved groups enumeration values.
     *
     * @param groupEnum one of the Reserved groups enumeration values.
     * @return the actual group.
     */
    @Nonnull
    public Group findReservedGroup(@Nonnull ReservedGroup groupEnum);

    @Nonnull
    public List<Integer> findIds();
}
