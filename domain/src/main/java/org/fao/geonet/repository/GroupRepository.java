package org.fao.geonet.repository;

import org.fao.geonet.domain.Group;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Data Access object for the {@link Group} entities.
 *
 * @author Jesse
 */
public interface GroupRepository extends GeonetRepository<Group, Integer>, GroupRepositoryCustom, JpaSpecificationExecutor<Group> {
    /**
     * Look up a group by its name
     *
     * @param name the name of the group
     */
    @Nullable
    Group findByName(@Nonnull String name);

    /**
     * Look up a group by its email address
     *
     * @param email the email address of the group
     */
    @Nullable
    Group findByEmail(@Nonnull String email);


}
