package org.fao.geonet.repository;

import java.util.List;
import java.util.Set;

import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.UserGroup;
import org.springframework.data.jpa.domain.Specification;

/**
 * Custom methods for loading {@link UserGroup} entities.
 * @author Jesse
 *
 */
public interface UserGroupRepositoryCustom {
    /**
     * Find all the groupIds that match the specification provided.
     *
     * @param spec a UserGroup selector specification
     */
    List<Integer> findGroupIds(Specification<UserGroup> spec);
    /**
     * Find all the userIds that match the specification provided.
     *
     * @param spec a UserGroup selector specification
     */
    List<Integer> findUserIds(Specification<UserGroup> spec);
}
