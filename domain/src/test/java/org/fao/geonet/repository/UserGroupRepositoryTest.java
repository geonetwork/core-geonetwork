package org.fao.geonet.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.data.jpa.domain.Specifications.*;

import org.fao.geonet.repository.specification.UserGroupSpecs;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.UserGroupId;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class UserGroupRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    UserGroupRepository repo;
    @Autowired
    UserRepository _userRepo;
    @Autowired
    GroupRepository _groupRepo;

    AtomicInteger inc = new AtomicInteger();
    
    @Test
    public void testFindUserIds () {
        UserGroup ug1 = repo.save(newUserGroup());
        UserGroup ug2 = repo.save(newUserGroup());
        repo.save(newUserGroup());
        repo.save(newUserGroup());
        
        assertEquals(4, repo.count());
        
        int ug1Id = ug1.getId().getGroupId();
        int ug2Id = ug2.getId().getGroupId();
        Specifications<UserGroup> ug1Or2Spec = where(UserGroupSpecs.hasUserId(ug1Id)).or(UserGroupSpecs.hasUserId(ug2Id));
        List<Integer> ug1Or2Ids = repo.findUserIds(ug1Or2Spec);
        
        assertTrue (ug1Or2Ids.contains(ug1Id));
        assertTrue (ug1Or2Ids.contains(ug2Id));
        assertEquals(2, ug1Or2Ids.size());
    }

    @Test
    public void testFindGroupIds () {
        UserGroup ug1 = repo.save(newUserGroup());
        UserGroup ug2 = repo.save(newUserGroup());
        repo.save(newUserGroup());
        repo.save(newUserGroup());
        
        assertEquals(4, repo.count());

        int ug1Id = ug1.getId().getUserId();
        int ug2Id = ug2.getId().getUserId();
        Specifications<UserGroup> ug1Or2Spec = where(UserGroupSpecs.hasUserId(ug1Id)).or(UserGroupSpecs.hasUserId(ug2Id));
        List<Integer> ug1Or2Ids = repo.findGroupIds(ug1Or2Spec);
        
        assertTrue (ug1Or2Ids.contains(ug1Id));
        assertTrue (ug1Or2Ids.contains(ug2Id));
        assertEquals(2, ug1Or2Ids.size());
    }
    private UserGroup newUserGroup() {
        int groupName = inc.incrementAndGet();
        int userName = inc.incrementAndGet();
        User user = _userRepo.save(new User().setUsername(userName+""));
        Group group = _groupRepo.save(new Group().setName(groupName+""));
        return new UserGroup().setGroup(group).setUser(user);
    }

}
