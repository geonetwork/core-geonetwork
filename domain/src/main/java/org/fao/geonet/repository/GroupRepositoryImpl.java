package org.fao.geonet.repository;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.ReservedGroup;

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

}
