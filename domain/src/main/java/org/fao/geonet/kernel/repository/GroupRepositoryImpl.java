package org.fao.geonet.kernel.repository;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.fao.geonet.kernel.domain.Group;
import org.fao.geonet.kernel.domain.ReservedGroup;

public class GroupRepositoryImpl implements GroupRepositoryCustom {

    @PersistenceContext
    private EntityManager _entityManager;
    @Override
    @Nonnull
    public Group findReservedGroup(@Nonnull ReservedGroup group) {
        return _entityManager.find(Group.class, group.getId());
    }

}
