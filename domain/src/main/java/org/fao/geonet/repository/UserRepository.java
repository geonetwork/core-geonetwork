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
    public User findByUsername(String username);
    public User findByUsernameAndSecurityAuthTypeIsNull(String username);
    public List<User> findAllByEmail(String email);
    public List<User> findAllByProfile(Profile profile);
    
}
