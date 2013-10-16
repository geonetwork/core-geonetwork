package org.fao.geonet.repository;

import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.UserGroupId;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.metamodel.SingularAttribute;
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
     * Delete all the UserGroups with an id in the collection of ids.  The component of the id that is used in the 'in' clause
     * is determined by the idAttribute.
     *
     * @param idAttribute the part of the id object that is compared to the collection of ids
     * @param ids         the ids for finding the {@link UserGroup} to delete.
     * @return the number of entities deleted
     */
    int deleteAllByIdAttribute(SingularAttribute<UserGroupId, Integer> idAttribute, Collection<Integer> ids);
}
