/**
 * 
 */
package org.fao.geonet.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.UserGroupId;
import org.fao.geonet.domain.UserGroupId_;
import org.fao.geonet.domain.UserGroup_;
import org.springframework.data.jpa.domain.Specification;

/**
 * Implementation object for methods in {@link UserGroupRepositoryCustom}.
 * 
 * @author Jesse
 */
public class UserGroupRepositoryImpl implements UserGroupRepositoryCustom {

    @PersistenceContext
    private EntityManager _entityManager;
    @Override
    public List<Integer> findGroupIds(Specification<UserGroup> spec) {
        return findIdsBy(spec, UserGroupId_.groupId);

    }
    @Override
    public List<Integer> findUserIds(Specification<UserGroup> spec) {
        return findIdsBy(spec, UserGroupId_.userId);
        
    }
    private List<Integer> findIdsBy(Specification<UserGroup> spec, SingularAttribute<UserGroupId, Integer> groupId) {
        CriteriaBuilder builder = _entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> query = builder.createQuery(Integer.class);
        Root<UserGroup> from = query.from(UserGroup.class);
        query.select(from.get(UserGroup_.id).get(groupId));
        spec.toPredicate(from, query, builder);

        return _entityManager.createQuery(query).getResultList();
    }

}
