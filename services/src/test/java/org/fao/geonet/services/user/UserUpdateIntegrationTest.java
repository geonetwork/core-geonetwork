package org.fao.geonet.services.user;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.GroupRepositoryTest;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.*;

/**
 * Test User update service.
 * <p/>
 * User: Jesse
 * Date: 10/16/13
 * Time: 3:20 PM
 */
@ContextConfiguration(inheritLocations = true, locations = "classpath:encoder-bean.xml")
public class UserUpdateIntegrationTest extends AbstractServiceIntegrationTest {
    private static final String COMPAT_UPDATE_PARAMS = "<request>\n" +
                                                       "  <zip>zip1</zip>\n" +
                                                       "  <groups_RegisteredUser>2</groups_RegisteredUser>\n" +
                                                       "  <state>state1</state>\n" +
                                                       "  <surname>lastname</surname>\n" +
                                                       "  <org>c2c</org>\n" +
                                                       "  <password>password</password>\n" +
                                                       "  <kind>consultant</kind>\n" +
                                                       "  <city>city1</city>\n" +
                                                       "  <country>ca</country>\n" +
                                                       "  <id />\n" +
                                                       "  <operation>%s</operation>\n" +
                                                       "  <username>newuser</username>\n" +
                                                       "  <password2>newuser</password2>\n" +
                                                       "  <groups_Reviewer>2</groups_Reviewer>\n" +
                                                       "  <email>newuser@email.com</email>\n" +
                                                       "  <address>address1</address>\n" +
                                                       "  <groups_UserAdmin>2</groups_UserAdmin>\n" +
                                                       "  <name>firstname</name>\n" +
                                                       "  <groups_Editor>2</groups_Editor>\n" +
                                                       "  <profile>UserAdmin</profile>\n" +
                                                       "</request>";

    @Autowired
    UserGroupRepository _userGroupRepository;
    @Autowired
    GroupRepository _groupRepository;
    @Autowired
    PasswordEncoder _encoder;
    private AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testExecAddNewUserCompatibilityModeAsAdmin() throws Exception {
        Element request = Xml.loadString(String.format(COMPAT_UPDATE_PARAMS, Params.Operation.NEWUSER), false);

        final Update update = new Update();

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        update.serviceSpecificExec(request, context);

        assertEquals(2, _userRepo.count());
        List<User> users = _userRepo.findAllByProfile(Profile.UserAdmin);
        assertEquals(1, users.size());

        User user = users.get(0);

        assertExpectedUser(user);

        assertNotNull(_userGroupRepository.findOne(new UserGroupId().setGroupId(2).setUserId(user.getId()).setProfile(Profile.Editor)));
        assertNotNull(_userGroupRepository.findOne(new UserGroupId().setGroupId(2).setUserId(user.getId()).setProfile(Profile
                .RegisteredUser)));
        assertNotNull(_userGroupRepository.findOne(new UserGroupId().setGroupId(2).setUserId(user.getId()).setProfile(Profile
                .UserAdmin)));
        assertNotNull(_userGroupRepository.findOne(new UserGroupId().setGroupId(2).setUserId(user.getId()).setProfile(Profile.Reviewer)));
        assertNull(_userGroupRepository.findOne(new UserGroupId().setGroupId(2).setUserId(user.getId()).setProfile(Profile
                .Administrator)));

    }

    @Test
    public void testExecFullUpdateUserCompatibilityModeAsAdmin() throws Exception {

        User startUser = new User()
                .setName("abc")
                .setKind("abc")
                .setOrganisation("abc")
                .setProfile(Profile.Guest)
                .setSurname("abc")
                .setUsername("abc");
        startUser.getSecurity().setPassword("abc");

        startUser = _userRepo.save(startUser);

        assertEquals(0, _userGroupRepository.count());
        assertEquals(2, _userRepo.count());

        Element request = Xml.loadString(String.format(COMPAT_UPDATE_PARAMS, Params.Operation.FULLUPDATE), false);
        request.getChild("id").setText(""+startUser.getId());

        final Update update = new Update();

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        update.serviceSpecificExec(request, context);

        assertEquals(2, _userRepo.count());
        User user = _userRepo.findOne(startUser.getId());

        assertNotNull(user);

        assertExpectedUser(user);

        assertNotNull(_userGroupRepository.findOne(new UserGroupId().setGroupId(2).setUserId(user.getId()).setProfile(Profile.Editor)));
        assertNotNull(_userGroupRepository.findOne(new UserGroupId().setGroupId(2).setUserId(user.getId()).setProfile(Profile
                .RegisteredUser)));
        assertNotNull(_userGroupRepository.findOne(new UserGroupId().setGroupId(2).setUserId(user.getId()).setProfile(Profile
                .UserAdmin)));
        assertNotNull(_userGroupRepository.findOne(new UserGroupId().setGroupId(2).setUserId(user.getId()).setProfile(Profile.Reviewer)));
        assertNull(_userGroupRepository.findOne(new UserGroupId().setGroupId(2).setUserId(user.getId()).setProfile(Profile
                .Administrator)));

    }

    @Test
    public void testExecPasswordUpdateAsAdmin() throws Exception {

        User startUser = getUser();

        startUser = _userRepo.save(startUser);

        assertEquals(2, _userRepo.count());

        final Update update = new Update();

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element request = new Element("request")
                .addContent(new Element(Params.ID).setText("" + startUser.getId()))
                .addContent(new Element(Params.PASSWORD).setText("password"))
                .addContent(new Element(Params.OPERATION).setText(Params.Operation.RESETPW));
        update.serviceSpecificExec(request, context);

        User user = _userRepo.findOne(startUser.getId());
        assertExpectedUser(user);
    }
    @Test
    public void testExecPasswordUpdate() throws Exception {

        User startUser = getUser();

        startUser = _userRepo.save(startUser);

        assertEquals(2, _userRepo.count());

        final Update update = new Update();

        ServiceContext context = createServiceContext();
        final UserSession userSession = new UserSession();
        userSession.loginAs(startUser);
        context.setUserSession(userSession);

        Element request = new Element("request")
                .addContent(new Element(Params.ID).setText(""+startUser.getId()))
                .addContent(new Element(Params.PASSWORD).setText("password"))
                .addContent(new Element(Params.OPERATION).setText(Params.Operation.RESETPW));
        update.serviceSpecificExec(request, context);

        User user = _userRepo.findOne(startUser.getId());
        assertExpectedUser(user);
    }

    @Test
    public void testPartialUpdate() throws Exception {

        User startUser = getUser();
        startUser.setName("abc");
        startUser = _userRepo.save(startUser);

        assertEquals(2, _userRepo.count());

        final Update update = new Update();

        ServiceContext context = createServiceContext();
        final UserSession userSession = new UserSession();
        userSession.loginAs(startUser);
        context.setUserSession(userSession);

        Element request = new Element("request")
                .addContent(new Element(Params.ID).setText(""+startUser.getId()))
                .addContent(new Element(Params.NAME).setText("firstname"))
                .addContent(new Element(Params.OPERATION).setText(Params.Operation.EDITINFO));
        update.serviceSpecificExec(request, context);

        User user = _userRepo.findOne(startUser.getId());
        assertExpectedUser(user);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCannotOwnEscalatePrivilegesToEditor() throws Exception {
        final Profile profile = Profile.Editor;

        assertCannotEscalateOwnPrivileges(profile);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCannotOwnEscalatePrivilegesToUserAdmin() throws Exception {
        final Profile profile = Profile.UserAdmin;

        assertCannotEscalateOwnPrivileges(profile);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCannotOwnEscalatePrivilegesToAdmin() throws Exception {
        final Profile profile = Profile.Administrator;

        assertCannotEscalateOwnPrivileges(profile);
    }
    @Test(expected=IllegalArgumentException.class)
    public void testCannotOwnEscalatePrivilegesToReviewer() throws Exception {
        final Profile profile = Profile.Reviewer;

        assertCannotEscalateOwnPrivileges(profile);
    }
    @Test(expected=IllegalArgumentException.class)
    public void testUseradminCannotEscalateOthersPrivilegesToAdministrator() throws Exception {

        final Group one = _groupRepository.findOne(2);
        User toUpdateUser = getUser();
        toUpdateUser = _userRepo.save(toUpdateUser);

        User updatingUser = getUser();
        updatingUser.setProfile(Profile.UserAdmin);
        updatingUser.setUsername("updater");
        updatingUser = _userRepo.save(updatingUser);

        _userGroupRepository.save(Arrays.asList(
                new UserGroup().setProfile(Profile.Editor).setUser(toUpdateUser).setGroup(one),
                new UserGroup().setProfile(Profile.UserAdmin).setUser(updatingUser).setGroup(one)
        ));


        final Update update = new Update();

        ServiceContext context = createServiceContext();
        final UserSession userSession = new UserSession();
        userSession.loginAs(updatingUser);
        context.setUserSession(userSession);

        Element request = new Element("request")
                .addContent(new Element(Params.ID).setText(""+toUpdateUser.getId()))
                .addContent(new Element(Params.PROFILE).setText(Profile.Administrator.name()))
                .addContent(new Element(Params.OPERATION).setText(Params.Operation.EDITINFO));
        update.serviceSpecificExec(request, context);
    }
    public void testCanReducePrivileges() throws Exception {
        final Profile profile = Profile.Guest;

        assertCannotEscalateOwnPrivileges(profile);
    }
   public void testCanKeepSamePrivileges() throws Exception {
        final Profile profile = Profile.RegisteredUser;

        assertCannotEscalateOwnPrivileges(profile);
    }

    private void assertCannotEscalateOwnPrivileges(Profile profile) throws Exception {
        User startUser = getUser();
        startUser.setProfile(Profile.RegisteredUser);
        startUser = _userRepo.save(startUser);

        assertEquals(2, _userRepo.count());

        final Update update = new Update();

        ServiceContext context = createServiceContext();
        final UserSession userSession = new UserSession();
        userSession.loginAs(startUser);
        context.setUserSession(userSession);

        Element request = new Element("request")
                .addContent(new Element(Params.NAME).setText("newname"))
                .addContent(new Element(Params.ID).setText(""+startUser.getId()))
                .addContent(new Element(Params.PROFILE).setText(profile.name()))
                .addContent(new Element(Params.OPERATION).setText(Params.Operation.EDITINFO));
        update.serviceSpecificExec(request, context);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testExecPasswordUpdateIllegalAccess() throws Exception {

        User toUpdateUser = getUser();
        toUpdateUser = _userRepo.save(toUpdateUser);

        User updatingUser = getUser();
        updatingUser.setProfile(Profile.Editor);
        updatingUser.setUsername("updater");
        updatingUser = _userRepo.save(updatingUser);

        final Update update = new Update();

        ServiceContext context = createServiceContext();
        final UserSession userSession = new UserSession();
        userSession.loginAs(updatingUser);
        context.setUserSession(userSession);

        Element request = new Element("request")
                .addContent(new Element(Params.PASSWORD).setText("password"))
                .addContent(new Element(Params.ID).setText("" + toUpdateUser.getId()))
                .addContent(new Element(Params.OPERATION).setText(Params.Operation.RESETPW));
        update.serviceSpecificExec(request, context);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testUpdateUserInOtherGroup() throws Exception {
        final Group one = _groupRepository.findOne(2);
        final Group two = _groupRepository.save(GroupRepositoryTest.newGroup(_inc));
        User toUpdateUser = getUser();
        toUpdateUser = _userRepo.save(toUpdateUser);

        User updatingUser = getUser();
        updatingUser.setProfile(Profile.Editor);
        updatingUser.setUsername("updater");
        updatingUser = _userRepo.save(updatingUser);

        _userGroupRepository.save(Arrays.asList(
                new UserGroup().setProfile(Profile.Editor).setUser(toUpdateUser).setGroup(one),
                new UserGroup().setProfile(Profile.UserAdmin).setUser(updatingUser).setGroup(two)
            ));

        final Update update = new Update();

        ServiceContext context = createServiceContext();
        final UserSession userSession = new UserSession();
        userSession.loginAs(updatingUser);
        context.setUserSession(userSession);

        Element request = new Element("request")
                .addContent(new Element(Params.PASSWORD).setText("password"))
                .addContent(new Element(Params.ID).setText("" + toUpdateUser.getId()))
                .addContent(new Element(Params.OPERATION).setText(Params.Operation.RESETPW));
        update.serviceSpecificExec(request, context);

    }

    public void testUpdateUserByUserAdmin() throws Exception {
        final Group one = _groupRepository.findOne(2);
        User toUpdateUser = getUser();
        toUpdateUser.getSecurity().setPassword("asd");
        toUpdateUser = _userRepo.save(toUpdateUser);

        User updatingUser = getUser();
        updatingUser.setProfile(Profile.UserAdmin);
        updatingUser.setUsername("updater");
        updatingUser = _userRepo.save(updatingUser);

        _userGroupRepository.save(Arrays.asList(
                new UserGroup().setProfile(Profile.Editor).setUser(toUpdateUser).setGroup(one),
                new UserGroup().setProfile(Profile.UserAdmin).setUser(updatingUser).setGroup(one)
            ));

        final Update update = new Update();

        ServiceContext context = createServiceContext();
        final UserSession userSession = new UserSession();
        userSession.loginAs(updatingUser);
        context.setUserSession(userSession);

        Element request = new Element("request")
                .addContent(new Element(Params.PASSWORD).setText("password"))
                .addContent(new Element(Params.ID).setText("" + toUpdateUser.getId()))
                .addContent(new Element(Params.OPERATION).setText(Params.Operation.RESETPW));
        update.serviceSpecificExec(request, context);

        User user = _userRepo.findOne(toUpdateUser.getId());
        assertExpectedUser(user);
    }

    private User getUser() {
        User startUser = new User()
                .setName("firstname")
                .setKind("consultant")
                .setOrganisation("c2c")
                .setProfile(Profile.UserAdmin)
                .setSurname("lastname")
                .setUsername("newuser");
        startUser.getSecurity().setPassword(_encoder.encode("password"));
        startUser.getAddresses().add(new Address()
                .setAddress("address1")
                .setCity("city1")
                .setCountry("ca")
                .setState("state1")
                .setZip("zip1"));
        startUser.getEmailAddresses().add("newuser@email.com");

        return startUser;
    }

    private void assertExpectedUser(User user) {
        assertEquals(1, user.getAddresses().size());
        assertEquals("zip1", user.getPrimaryAddress().getZip());
        assertEquals("address1", user.getPrimaryAddress().getAddress());
        assertEquals("city1", user.getPrimaryAddress().getCity());
        assertEquals("ca", user.getPrimaryAddress().getCountry());
        assertEquals("state1", user.getPrimaryAddress().getState());

        assertEquals(1, user.getEmailAddresses().size());
        assertEquals("newuser@email.com", user.getEmail());

        assertEquals("consultant", user.getKind());
        assertEquals("firstname", user.getName());
        assertEquals("lastname", user.getSurname());
        assertEquals("c2c", user.getOrganisation());
        assertTrue(_encoder.matches("password", user.getPassword()));
        assertEquals("newuser", user.getUsername());
        assertEquals(Profile.UserAdmin, user.getProfile());
    }
}
