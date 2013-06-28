package org.fao.geonet.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.fao.geonet.domain.User;

public class UserRepositoryImpl implements UserRepositoryCustom {

    @PersistenceContext
    EntityManager _entityManager;
    @Override
    public User findOne(String userId) {
        return _entityManager.find(User.class, Integer.valueOf(userId));
    }

}
