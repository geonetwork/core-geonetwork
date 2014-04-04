/**
 *
 */
package org.fao.geonet.repository;

import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.UserGroupId;
import org.fao.geonet.domain.UserGroupId_;
import org.fao.geonet.domain.UserGroup_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Collection;
import java.util.List;

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

    @Override
    @Transactional
    public int deleteAllByIdAttribute(SingularAttribute<UserGroupId, Integer> idAttribute, Collection<Integer> ids) {
        String userIdPath = SortUtils.createPath(UserGroup_.id, idAttribute);

        StringBuilder idString = new StringBuilder();

        for (Integer id : ids) {
            if (idString.length() > 0) {
                idString.append(",");
            }
            idString.append(id);
        }
        final String qlString = "DELETE FROM " + UserGroup.class.getSimpleName() + " WHERE " + userIdPath + " IN (" + idString + ")";
        final int deleted = _entityManager.createQuery(qlString).executeUpdate();

        _entityManager.flush();
        _entityManager.clear();

        return deleted;
    }

    private List<Integer> findIdsBy(Specification<UserGroup> spec, SingularAttribute<UserGroupId, Integer> groupId) {
        CriteriaBuilder builder = _entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> query = builder.createQuery(Integer.class);
        Root<UserGroup> from = query.from(UserGroup.class);
        query.select(from.get(UserGroup_.id).get(groupId));
        Predicate predicate = spec.toPredicate(from, query, builder);
        query.where(predicate);
        query.distinct(true);
        return _entityManager.createQuery(query).getResultList();
    }

}
