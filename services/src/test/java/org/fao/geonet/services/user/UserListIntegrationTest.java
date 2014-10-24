package org.fao.geonet.services.user;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBElement;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.domain.Address;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.responses.UserList;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.GroupRepositoryTest;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.UserRepositoryTest;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test listing a user service.
 *
 * User: Jesse
 * Date: 10/12/13
 * Time: 8:30 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class UserListIntegrationTest extends AbstractServiceIntegrationTest {
    private AtomicInteger inc = new AtomicInteger();
    @Autowired
    UserRepository _userRepo;
    @Autowired
    UserGroupRepository _userGroupRepo;
    @Autowired
    GroupRepository _groupRepo;

    @Autowired
    List listService;


    @Test
    public void testExecAsUserNoGroups() throws Exception {
        final User entity = UserRepositoryTest.newUser(inc);
        entity.setProfile(Profile.Editor);

        entity.getAddresses().add(
                new Address()
                        .setAddress("add1")
                        .setCity("city1")
                        .setCountry("country1")
                        .setState("state1")
                        .setZip("zip1"));
        entity.getEmailAddresses().add("email2");
        final User editor = _userRepo.save(entity);

        _userRepo.save(UserRepositoryTest.newUser(inc));

        final ServiceContext serviceContext = createServiceContext();
        UserSession userSession = new UserSession();
        userSession.loginAs(editor);
        serviceContext.setUserSession(userSession);
        final UserList response = listService.exec();

        java.util.List<JAXBElement<? extends User>> records = response.getUsers();
        assertEquals("Expected to find a record.", 1, records.size());
        
        User user = records.get(0).getValue();

        assertEquals(editor.getUsername(), user.getUsername(), "record/username");
        assertEquals(editor.getPrimaryAddress().getAddress(), user.getPrimaryAddress().getAddress(), "record/primaryaddress/address");
        assertEquals(editor.getProfile().name(), user.getProfile().name(), "record/profile");
        assertEquals(editor.getEmailAddresses().iterator().next(), user.getEmailAddresses().iterator().next(), "record/emailaddresses/emailaddress");
    }


    @Test
    public void testExecAsAdmin() throws Exception {
        final User entity = UserRepositoryTest.newUser(inc);
        _userRepo.save(entity);

        _userRepo.save(UserRepositoryTest.newUser(inc));

        final ServiceContext serviceContext = createServiceContext();
        UserSession userSession = new UserSession();
        User administrator = new User().setProfile(Profile.Administrator).setName("admin").setOrganisation("org").setSurname("admin");

        userSession.loginAs(administrator);
        serviceContext.setUserSession(userSession);
        final UserList response = listService.exec();

        java.util.List<?> records = response.getUsers();
        assertEquals("Expected to find 3 records", 3, records.size());
    }

    @Test
    public void testExecAsWithGroups() throws Exception {
        final Group group = _groupRepo.save(GroupRepositoryTest.newGroup(inc));
        User user1 = UserRepositoryTest.newUser(inc);
        user1.setProfile(Profile.Editor);
        user1 = _userRepo.save(user1);

        User user2 = UserRepositoryTest.newUser(inc);
        user2.setProfile(Profile.RegisteredUser);
        user2 = _userRepo.save(user2);

        _userGroupRepo.save(new UserGroup().setGroup(group).setUser(user1).setProfile(Profile.Editor));
        _userGroupRepo.save(new UserGroup().setGroup(group).setUser(user2).setProfile(Profile.RegisteredUser));

        final ServiceContext serviceContext = createServiceContext();
        UserSession userSession = new UserSession();
        userSession.loginAs(user1);

        serviceContext.setUserSession(userSession);
        final UserList response = listService.exec();

        java.util.List<?> records = response.getUsers();
        assertEquals("Expected to find 2 records. ", 2, records.size());
    }

}
