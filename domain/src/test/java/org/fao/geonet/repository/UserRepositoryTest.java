package org.fao.geonet.repository;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.fao.geonet.domain.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class UserRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    UserRepository repo;

    AtomicInteger inc = new AtomicInteger();

    @Test
    public void testFindByEmailAddress() {
        User user1 = newUser();
        String add1 = "add1";
        String add1b = "add1b";
        user1.getEmailAddresses().add(add1);
        user1.getEmailAddresses().add(add1b);
        user1 = repo.save(user1);

        User user2 = newUser();
        String add2 = "add2";
        String add2b = "add2b";
        user2.getEmailAddresses().add(add2);
        user2.getEmailAddresses().add(add2b);
        user2 = repo.save(user2);

        List<User> users = repo.findAllByEmail(add1);

        assertEquals(1, users.size());
        assertEquals(user1, users.get(0));
    }

    private User newUser() {
        int val = inc.incrementAndGet();
        User user = new User().setName("name" + val).setUsername("username" + val);
        user.getSecurity().setPassword("1234567");
        return user;
    }

}
