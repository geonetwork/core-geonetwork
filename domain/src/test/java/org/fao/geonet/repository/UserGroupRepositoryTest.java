package org.fao.geonet.repository;

import org.fao.geonet.domain.*;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.data.jpa.domain.Specifications.where;

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
    public void testFindUserIds() {
        UserGroup ug1 = repo.save(newUserGroup());
        UserGroup ug2 = repo.save(newUserGroup());
        repo.save(newUserGroup());
        repo.save(newUserGroup());

        assertEquals(4, repo.count());

        int ug1Id = ug1.getId().getUserId();
        int ug2Id = ug2.getId().getUserId();

        Specifications<UserGroup> ug1Or2Spec = where(UserGroupSpecs.hasUserId(ug1Id)).or(UserGroupSpecs.hasUserId(ug2Id));
        List<Integer> ug1Or2Ids = repo.findUserIds(ug1Or2Spec);

        assertTrue(ug1Or2Ids.contains(ug1Id));
        assertTrue(ug1Or2Ids.contains(ug2Id));
        assertEquals(2, ug1Or2Ids.size());
    }

    @Test
    public void testFindGroupIds() {
        UserGroup ug1 = repo.save(newUserGroup());
        UserGroup ug2 = repo.save(newUserGroup());
        repo.save(newUserGroup());
        repo.save(newUserGroup());

        assertEquals(4, repo.count());

        int ug1Id = ug1.getId().getGroupId();
        int ug2Id = ug2.getId().getGroupId();
        Specifications<UserGroup> ug1Or2Spec = where(UserGroupSpecs.hasGroupId(ug1Id)).or(UserGroupSpecs.hasGroupId(ug2Id));

        List<Integer> ug1Or2Ids = repo.findGroupIds(ug1Or2Spec);

        assertTrue(ug1Or2Ids.contains(ug1Id));
        assertTrue(ug1Or2Ids.contains(ug2Id));
        assertEquals(2, ug1Or2Ids.size());
    }

    @Test
    public void testFindAllByProfileSpec() {
        UserGroup ug1 = repo.save(newUserGroup());
        UserGroup ug2 = repo.save(newUserGroup());
        repo.save(newUserGroup());
        repo.save(newUserGroup());

        assertEquals(4, repo.count());

        Profile p1 = ug1.getProfile();
        Profile p2 = ug2.getProfile();
        Specifications<UserGroup> specifications = where(UserGroupSpecs.hasProfile(p1)).or(UserGroupSpecs.hasProfile(p2));

        List<UserGroup> found = repo.findAll(specifications);

        assertTrue(found.contains(ug1));
        assertTrue(found.contains(ug2));
        assertEquals(2, found.size());
    }

    private UserGroup newUserGroup() {
        int groupName = inc.incrementAndGet();
        int userName = inc.incrementAndGet();

        User user = new User().setUsername(userName + "");
        user.getSecurity().setPassword("password" + userName);
        user = _userRepo.save(user);

        Group group = _groupRepo.save(new Group().setName(groupName + ""));
        UserGroup userGroup = new UserGroup().setGroup(group).setUser(user).setId(new UserGroupId(user, group));
        userGroup.setProfile(Profile.values()[userName % Profile.values().length]);

        return userGroup;
    }

}
