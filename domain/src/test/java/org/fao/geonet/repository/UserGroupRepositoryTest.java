package org.fao.geonet.repository;

import org.fao.geonet.domain.*;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.springframework.data.jpa.domain.Specifications.where;

public class UserGroupRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    UserGroupRepository _repo;
    @Autowired
    UserRepository _userRepo;
    @Autowired
    GroupRepository _groupRepo;

    @Test
    public void testFindUserIds() {
        UserGroup ug1 = _repo.save(newUserGroup());
        UserGroup ug2 = _repo.save(newUserGroup());
        _repo.save(newUserGroup());
        _repo.save(newUserGroup());

        assertEquals(4, _repo.count());

        int ug1Id = ug1.getId().getUserId();
        int ug2Id = ug2.getId().getUserId();

        Specifications<UserGroup> ug1Or2Spec = where(UserGroupSpecs.hasUserId(ug1Id)).or(UserGroupSpecs.hasUserId(ug2Id));
        List<Integer> ug1Or2Ids = _repo.findUserIds(ug1Or2Spec);

        assertTrue(ug1Or2Ids.contains(ug1Id));
        assertTrue(ug1Or2Ids.contains(ug2Id));
        assertEquals(2, ug1Or2Ids.size());
    }

    @Test
    public void testPrimaryKey() throws Exception {
        Group group = _groupRepo.save(GroupRepositoryTest.newGroup(_inc));
        User user =_userRepo.save(UserRepositoryTest.newUser(_inc));

        UserGroup userGroup = new UserGroup().setGroup(group).setUser(user).setProfile(Profile.Editor);
        userGroup = _repo.save(userGroup);

        UserGroup userGroup2 = new UserGroup().setGroup(group).setUser(user).setProfile(Profile.RegisteredUser);
        userGroup2 = _repo.save(userGroup2);

        UserGroup userGroup3 = new UserGroup().setGroup(group).setUser(user).setProfile(Profile.RegisteredUser);
        _repo.save(userGroup3);

        assertEquals(2, _repo.count());

        assertNotNull(_repo.findOne(userGroup.getId()));
        assertNotNull(_repo.findOne(userGroup2.getId()));

    }

    @Test
    public void testFindGroupIds() {
        UserGroup ug1 = _repo.save(newUserGroup());
        UserGroup ug2 = _repo.save(newUserGroup());
        final UserGroup entity = newUserGroup();
        entity.setGroup(ug2.getGroup());
        _repo.save(entity);
        _repo.save(newUserGroup());

        assertEquals(4, _repo.count());

        int ug1Id = ug1.getId().getGroupId();
        int ug2Id = ug2.getId().getGroupId();
        Specifications<UserGroup> ug1Or2Spec = where(UserGroupSpecs.hasGroupId(ug1Id)).or(UserGroupSpecs.hasGroupId(ug2Id));

        List<Integer> ug1Or2Ids = _repo.findGroupIds(ug1Or2Spec);

        assertTrue(ug1Or2Ids.contains(ug1Id));
        assertTrue(ug1Or2Ids.contains(ug2Id));
        assertEquals(2, ug1Or2Ids.size());
    }

    @Test
    public void testFindAllByProfileSpec() {
        UserGroup ug1 = _repo.save(newUserGroup());
        UserGroup ug2 = _repo.save(newUserGroup());
        _repo.save(newUserGroup());
        _repo.save(newUserGroup());

        assertEquals(4, _repo.count());

        Profile p1 = ug1.getProfile();
        Profile p2 = ug2.getProfile();
        Specifications<UserGroup> specifications = where(UserGroupSpecs.hasProfile(p1)).or(UserGroupSpecs.hasProfile(p2));

        List<UserGroup> found = _repo.findAll(specifications);

        assertTrue(found.contains(ug1));
        assertTrue(found.contains(ug2));
        assertEquals(2, found.size());
    }

    @Test
    public void testDeleteAllWithUserIdsIn() {
        UserGroup ug1 = _repo.save(newUserGroup());
        UserGroup ug2 = _repo.save(newUserGroup());
        final UserGroup ug3 = _repo.save(newUserGroup());
        final UserGroup ug4 = _repo.save(newUserGroup());

        assertEquals(4, _repo.count());

        int deleted = _repo.deleteAllByIdAttribute(UserGroupId_.userId, Arrays.asList(ug1.getId().getUserId(), ug2.getId().getUserId()));
        assertEquals(2, deleted);
        assertEquals(2, _repo.count());

        assertFalse(_repo.exists(ug1.getId()));
        assertFalse(_repo.exists(ug2.getId()));
        assertTrue(_repo.exists(ug3.getId()));
        assertTrue(_repo.exists(ug4.getId()));

        assertNull(_repo.findOne(ug1.getId()));
    }


    private UserGroup newUserGroup() {
        return getUserGroup(_inc, _userRepo, _groupRepo);
    }

    public static UserGroup getUserGroup(AtomicInteger atomicInteger, UserRepository _userRepo, GroupRepository _groupRepo) {
        int groupName = atomicInteger.incrementAndGet();
        int userName = atomicInteger.incrementAndGet();

        User user = new User().setUsername(userName + "");
        user.getSecurity().setPassword("password" + userName);
        user = _userRepo.save(user);

        Group group = _groupRepo.save(new Group().setName(groupName + ""));
        UserGroup userGroup = new UserGroup().setGroup(group).setUser(user).setId(new UserGroupId(user, group));
        userGroup.setProfile(Profile.values()[userName % Profile.values().length]);

        return userGroup;
    }

}
