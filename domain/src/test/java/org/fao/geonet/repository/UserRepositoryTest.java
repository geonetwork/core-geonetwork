package org.fao.geonet.repository;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Optional;
import org.fao.geonet.domain.*;
import org.hibernate.ejb.criteria.OrderImpl;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Order;

@Transactional
public class UserRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    UserGroupRepository _userGroupRepository;
    @Autowired
    MetadataRepository _metadataRepo;
    @Autowired
    GroupRepository _groupRepo;
    @Autowired
    UserRepository _userRepo;

    AtomicInteger inc = new AtomicInteger();

    @Test
    public void testFindByEmailAddress() {
        User user1 = newUser();
        String add1 = "add1";
        String add1b = "add1b";
        user1.getEmailAddresses().add(add1);
        user1.getEmailAddresses().add(add1b);
        user1 = _userRepo.save(user1);

        User user2 = newUser();
        String add2 = "add2";
        String add2b = "add2b";
        user2.getEmailAddresses().add(add2);
        user2.getEmailAddresses().add(add2b);
        user2 = _userRepo.save(user2);

        List<User> users = _userRepo.findAllByEmail(add1);

        assertEquals(1, users.size());
        assertEquals(user1, users.get(0));
    }

    @Test
    public void testFindAllByGroupOwnerNameAndProfile() {
        Group group1 = _groupRepo.save(GroupRepositoryTest.newGroup(inc));
        Group group2 = _groupRepo.save(GroupRepositoryTest.newGroup(inc));

        User editUser = _userRepo.save(newUser().setProfile(Profile.Editor));
        User reviewerUser = _userRepo.save(newUser().setProfile(Profile.Reviewer));
        User registeredUser = _userRepo.save(newUser().setProfile(Profile.RegisteredUser));
        User adminUser = _userRepo.save(newUser().setProfile(Profile.Administrator));

        Metadata md1 = MetadataRepositoryTest.newMetadata(inc);
        md1.getSourceInfo().setGroupOwner(group1.getId());
        md1 = _metadataRepo.save(md1);

        Metadata md2 = MetadataRepositoryTest.newMetadata(inc);
        md2.getSourceInfo().setGroupOwner(group1.getId());
        md2 = _metadataRepo.save(md2);

        Metadata md3 = MetadataRepositoryTest.newMetadata(inc);
        md3.getSourceInfo().setGroupOwner(group2.getId());
        md3 = _metadataRepo.save(md3);

        UserGroup ug1 = _userGroupRepository.save(new UserGroup().setGroup(group1).setUser(editUser).setProfile(Profile.Editor));
        UserGroup ug2 = _userGroupRepository.save(new UserGroup().setGroup(group2).setUser(registeredUser).setProfile(Profile.RegisteredUser));
        UserGroup ug3 = _userGroupRepository.save(new UserGroup().setGroup(group2).setUser(reviewerUser).setProfile(Profile.Editor));
        UserGroup ug4 = _userGroupRepository.save(new UserGroup().setGroup(group1).setUser(reviewerUser).setProfile(Profile.Reviewer));

        List<Pair<Integer, User>> found = _userRepo.findAllByGroupOwnerNameAndProfile(Arrays.asList(md1.getId()),
                Optional.<Profile>absent(),
                Optional.of(new Sort(User_.name.getName())));

        assertEquals(2, found.size());
        assertEquals(md1.getId(), found.get(0).one().intValue());
        assertEquals(md1.getId(), found.get(1).one().intValue());
        assertEquals(editUser, found.get(0).two());
        assertEquals(reviewerUser, found.get(1).two());

        found = _userRepo.findAllByGroupOwnerNameAndProfile(Arrays.asList(md1.getId()),
                Optional.<Profile>absent(),
                Optional.of(new Sort(new Sort.Order(Sort.Direction.DESC, User_.name.getName()))));

        assertEquals(2, found.size());
        assertEquals(md1.getId(), found.get(0).one().intValue());
        assertEquals(md1.getId(), found.get(1).one().intValue());
        assertEquals(editUser, found.get(1).two());
        assertEquals(reviewerUser, found.get(0).two());


        found = _userRepo.findAllByGroupOwnerNameAndProfile(Arrays.asList(md1.getId(), md2.getId()),
                Optional.<Profile>absent(),
                Optional.<Sort>absent());

        assertEquals(4, found.size());
        int md1Found = 0;
        int md2Found = 0;
        for (Pair<Integer, User> record : found) {
            if (record.one() == md1.getId()) {
                md1Found++;
            } else {
                md2Found++;
            }
        }
        assertEquals(2, md1Found);
        assertEquals(2, md2Found);
    }

    private User newUser() {
        int val = inc.incrementAndGet();
        User user = new User().setName("name" + val).setUsername("username" + val);
        user.getSecurity().setPassword("1234567");
        return user;
    }

}
