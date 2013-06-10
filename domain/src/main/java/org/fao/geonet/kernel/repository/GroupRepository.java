package org.fao.geonet.kernel.repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.fao.geonet.kernel.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Access object for the Group objects.
 *
 * @author Jesse
 */
public interface GroupRepository extends JpaRepository<Group, Integer>, GroupRepositoryCustom {
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
