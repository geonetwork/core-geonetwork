package org.fao.geonet.repository;

import org.fao.geonet.domain.User;

/**
 * Custom (Non spring-data) Query methods for {@link User} entities.
 * 
 * @author Jesse
 */
public interface UserRepositoryCustom {
    /**
     * Find the use with the given userid (where userid is a string).  The string will be converted to an integer for making the query.
     * 
     * @param userId the userid.
     * 
     * @return the use with the given userid 
     */
    User findOne(String userId);
}
