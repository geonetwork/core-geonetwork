package org.fao.geonet.repository;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.fao.geonet.domain.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UserRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    UserRepository repo;

    AtomicInteger inc = new AtomicInteger();

    @Test
    public void testSaveSubclass() {
        User user = new TestSubclass().setName("TestSubclass");
        
        User savedUser = repo.save(user);
        
        repo.flush();
        
        // no error? good
        User loadedUser = repo.findOne(savedUser.getId());
                
        assertEquals(user.getName(), loadedUser.getName());
    }
    private User newUser() {
        int val = inc.incrementAndGet();
        return new User().setName("name" + val);
    }

    private class TestSubclass extends User {
        private Object extraData;
        
        public Object getExtraData() {
            return extraData;
        }
    }
}
