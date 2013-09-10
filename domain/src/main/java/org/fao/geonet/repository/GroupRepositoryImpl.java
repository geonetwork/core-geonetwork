package org.fao.geonet.repository;

import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Group_;
import org.fao.geonet.domain.ReservedGroup;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Implementation for {@link GroupRepositoryCustom} queries.
 *
 * @author Jesse
 */
public class GroupRepositoryImpl implements GroupRepositoryCustom {

    @PersistenceContext
    private EntityManager _entityManager;

    @Override
    @Nonnull
    public Group findReservedGroup(@Nonnull ReservedGroup group) {
        return _entityManager.find(Group.class, group.getId());
    }

    @Override
    public List<Integer> findIds() {
        CriteriaBuilder builder = _entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> query = builder.createQuery(Integer.class);
        Root<Group> from = query.from(Group.class);
        query.select(from.get(Group_.id));

        return _entityManager.createQuery(query).getResultList();
    }

}
