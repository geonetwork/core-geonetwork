package org.fao.geonet.repository;

import org.fao.geonet.domain.User;

/**
 * Custom (Non spring-data) Query methods for {@link User} entities.
 * 
 * @author Jesse
 */
public interface UserRepositoryCustom {
    User findOne(String userId);
}
