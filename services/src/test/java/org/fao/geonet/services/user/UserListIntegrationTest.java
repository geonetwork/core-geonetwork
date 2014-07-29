package org.fao.geonet.services.user;

import jeeves.constants.Jeeves;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.domain.*;
import org.fao.geonet.repository.*;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Test listing a user service.
 *
 * User: Jesse
 * Date: 10/12/13
 * Time: 8:30 PM
 */
public class UserListIntegrationTest extends AbstractServiceIntegrationTest {
    private AtomicInteger inc = new AtomicInteger();
    @Autowired
    UserRepository _userRepo;
    @Autowired
    UserGroupRepository _userGroupRepo;
    @Autowired
    GroupRepository _groupRepo;

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

        final List listService = new List();

        final ServiceContext serviceContext = createServiceContext();
        UserSession userSession = new UserSession();
        userSession.loginAs(editor);
        serviceContext.setUserSession(userSession);
        Element params = createParams();
        final Element response = listService.exec(params, serviceContext);

        java.util.List<?> records = Xml.selectNodes(response, "record");
        assertEquals("Expected to find a record in: "+Xml.getString(response), 1, records.size());
        assertEquals(Jeeves.Elem.RESPONSE, response.getName());

        assertEqualsText(editor.getUsername(), response, "record/username");
        assertEqualsText(editor.getPrimaryAddress().getAddress(), response, "record/addresses/address/address");
        assertEqualsText(editor.getPrimaryAddress().getAddress(), response, "record/primaryaddress/address");
        assertEqualsText(editor.getProfile().name(), response, "record/profile");
        assertEqualsText(editor.getEmailAddresses().iterator().next(), response, "record/emailaddresses/emailaddress");
    }


    @Test
    public void testExecAsAdmin() throws Exception {
        final User entity = UserRepositoryTest.newUser(inc);
        _userRepo.save(entity);

        _userRepo.save(UserRepositoryTest.newUser(inc));

        final List listService = new List();

        final ServiceContext serviceContext = createServiceContext();
        UserSession userSession = new UserSession();
        User administrator = new User().setProfile(Profile.Administrator).setName("admin").setOrganisation("org").setSurname("admin");

        userSession.loginAs(administrator);
        serviceContext.setUserSession(userSession);
        Element params = createParams();
        final Element response = listService.exec(params, serviceContext);
        assertEquals(Jeeves.Elem.RESPONSE, response.getName());

        java.util.List<?> records = Xml.selectNodes(response, "record");
        assertEquals("Expected to find a record in: "+Xml.getString(response), 3, records.size());
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

        final List listService = new List();

        final ServiceContext serviceContext = createServiceContext();
        UserSession userSession = new UserSession();
        userSession.loginAs(user1);

        serviceContext.setUserSession(userSession);
        Element params = createParams();
        final Element response = listService.exec(params, serviceContext);
        assertEquals(Jeeves.Elem.RESPONSE, response.getName());

        java.util.List<?> records = Xml.selectNodes(response, "record");
        assertEquals("Expected to find a record in: "+Xml.getString(response), 2, records.size());
    }

}
