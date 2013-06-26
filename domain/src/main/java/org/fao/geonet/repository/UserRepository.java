package org.fao.geonet.repository;

import java.util.List;

import org.fao.geonet.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    public User findByUsername(String username);
    public User findByUsernameAndAuthTypeIsNull(String username);
    public List<User> findAllByEmail(String email);
    
}
