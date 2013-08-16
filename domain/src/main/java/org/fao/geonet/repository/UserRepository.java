package org.fao.geonet.repository;

import java.util.List;

import jeeves.interfaces.Profile;

import org.fao.geonet.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link User} entities.
 * 
 * @author Jesse
 */
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User>, UserRepositoryCustom {
    /**
     * Find the user identified by  the username.
     * 
     * @param username the username to use in the query.
     * @return the user identified by  the username.
     */
    public User findByUsername(String username);
    /**
     * Find the user identified by the username but also has a null authtype (if user exists but has a nonnull authtype null is returned)
     * @param username the username to use in the query.
     * @return the user identified by the username but also has a null authtype (if user exists but has a nonnull authtype null is returned)
     */
    public User findByUsernameAndSecurityAuthTypeIsNull(String username);
    /**
     * Find all users with the given email address.
     *
     * @param email the email address to use in search query.
     * @return
     */
    public List<User> findAllByEmail(String email);
    /**
     * find all users with the given profile.
     * @param profile the profile to use in search query.

     * @return all users with the given profile.
     */
    public List<User> findAllByProfile(Profile profile);
    
}
