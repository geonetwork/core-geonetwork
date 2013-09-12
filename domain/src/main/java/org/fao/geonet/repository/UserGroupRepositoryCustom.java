package org.fao.geonet.repository;

import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;

/**
 * Custom methods for loading {@link UserGroup} entities.
 *
 * @author Jesse
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

    /**
     * Delete all entities for the users in the collection.
     *
     * @param userIds the collection of userIds that specifies which user groups to delete
     */
    int deleteAllWithUserIdsIn(Collection<Integer> userIds);
}
