package org.fao.geonet.repository;

import java.util.concurrent.atomic.AtomicInteger;

import org.fao.geonet.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class UserRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    UserRepository repo;

    AtomicInteger inc = new AtomicInteger();

    private User newUser() {
        int val = inc.incrementAndGet();
        return new User().setName("name" + val);
    }

}
