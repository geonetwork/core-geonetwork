package org.fao.geonet.repository;

import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Data Access object for accessing {@link User} entities.
 *
 * @author Jesse
 */
public interface UserRepository extends GeonetRepository<User, Integer>, JpaSpecificationExecutor<User>, UserRepositoryCustom {
    /**
     * Find the user identified by  the username.
     *
     * @param username the username to use in the query.
     * @return the user identified by  the username.
     */
    public User findOneByUsername(String username);

    /**
     * find all users with the given profile.
     *
     * @param profile the profile to use in search query.
     * @return all users with the given profile.
     */
    public List<User> findAllByProfile(Profile profile);
}
