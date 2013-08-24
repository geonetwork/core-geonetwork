package org.fao.geonet.repository;

import java.util.List;

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
    
    /**
     * Find all users with the given email address.
     *
     * @param email the email address to use in search query.
     * @return
     */
    public List<User> findAllByEmail(String email);
}
