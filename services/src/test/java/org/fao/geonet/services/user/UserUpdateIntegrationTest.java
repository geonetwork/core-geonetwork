package org.fao.geonet.services.user;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Address;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.UserGroupId;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.GroupRepositoryTest;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.geonetwork.http.proxy.util.ServletConfigUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Test User update service.
 * <p/>
 * User: Jesse Date: 10/16/13 Time: 3:20 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(inheritLocations = true, locations = {"classpath:encoder-bean.xml", "classpath*:config-node/srv.xml"})
public class UserUpdateIntegrationTest extends AbstractServiceIntegrationTest {
    private static final String COMPAT_UPDATE_PARAMS = "<request>\n"
            + "  <zip>zip1</zip>\n"
            + "  <groups_RegisteredUser>2</groups_RegisteredUser>\n"
            + "  <state>state1</state>\n" + "  <surname>lastname</surname>\n"
            + "  <org>c2c</org>\n" + "  <password>password</password>\n"
            + "  <kind>consultant</kind>\n" + "  <city>city1</city>\n"
            + "  <country>ca</country>\n" + "  <id>%s</id>\n"
            + "  <operation>%s</operation>\n"
            + "  <username>newuser</username>\n"
            + "  <password2>newuser</password2>\n"
            + "  <groups_Reviewer>2</groups_Reviewer>\n"
            + "  <email>newuser@email.com</email>\n"
            + "  <address>address1</address>\n"
            + "  <groups_UserAdmin>2</groups_UserAdmin>\n"
            + "  <name>firstname</name>\n"
            + "  <groups_Editor>2</groups_Editor>\n"
            + "  <profile>UserAdmin</profile>\n" + "</request>";

    @Autowired
    UserGroupRepository _userGroupRepository;
    @Autowired
    GroupRepository _groupRepository;
    @Autowired
    PasswordEncoder _encoder;
    private AtomicInteger _inc = new AtomicInteger();

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        MockServletContext servletContext = new MockServletContext(
                "../web/src/main/webapp");

        XmlWebApplicationContext springContext = new XmlWebApplicationContext();
        springContext.setConfigLocation("classpath:node-test.xml");
        springContext.setServletContext(servletContext);
        springContext.refresh();

        this.mockMvc = MockMvcBuilders.webAppContextSetup(springContext)
                .build();

    }

    @Test
    public void testExecAddNewUserCompatibilityModeAsAdmin() throws Exception {
        String xml = String.format(COMPAT_UPDATE_PARAMS, "",
                Params.Operation.NEWUSER);

        long numUsers = _userRepo.count() + 1;

        MockHttpSession session = loginAsAdmin();
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders
                .get("/srv/eng/admin.user.update");
        get.session(session);
        get.content(xml);
        get.accept(org.springframework.http.MediaType.APPLICATION_JSON);
        mockMvc.perform(get);

        assertEquals(numUsers, _userRepo.count());
        List<User> users = _userRepo.findAllByProfile(Profile.UserAdmin);
        assertEquals(1, users.size());

        User user = users.get(0);

        assertExpectedUser(user);

        assertNotNull(_userGroupRepository.findOne(new UserGroupId()
                .setGroupId(2).setUserId(user.getId())
                .setProfile(Profile.Editor)));
        assertNotNull(_userGroupRepository.findOne(new UserGroupId()
                .setGroupId(2).setUserId(user.getId())
                .setProfile(Profile.RegisteredUser)));
        assertNotNull(_userGroupRepository.findOne(new UserGroupId()
                .setGroupId(2).setUserId(user.getId())
                .setProfile(Profile.UserAdmin)));
        assertNotNull(_userGroupRepository.findOne(new UserGroupId()
                .setGroupId(2).setUserId(user.getId())
                .setProfile(Profile.Reviewer)));
        assertNull(_userGroupRepository.findOne(new UserGroupId().setGroupId(2)
                .setUserId(user.getId()).setProfile(Profile.Administrator)));

    }

    @Test
    public void testExecFullUpdateUserCompatibilityModeAsAdmin()
            throws Exception {

        User startUser = new User().setName("abc").setKind("abc")
                .setOrganisation("abc").setProfile(Profile.Guest)
                .setSurname("abc").setUsername("abc");
        startUser.getSecurity().setPassword("abc");

        startUser = _userRepo.save(startUser);

        assertEquals(0, _userGroupRepository.count());
        assertEquals(2, _userRepo.count());

        String xml = String.format(COMPAT_UPDATE_PARAMS, startUser.getId(),
                Params.Operation.FULLUPDATE);

        MockHttpSession session = loginAsAdmin();
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders
                .get("/eng/admin.user.update");
        get.session(session);
        get.content(xml);
        get.accept(org.springframework.http.MediaType.APPLICATION_JSON);
        mockMvc.perform(get);

        assertEquals(2, _userRepo.count());
        User user = _userRepo.findOne(startUser.getId());

        assertNotNull(user);

        assertExpectedUser(user);

        assertNotNull(_userGroupRepository.findOne(new UserGroupId()
                .setGroupId(2).setUserId(user.getId())
                .setProfile(Profile.Editor)));
        assertNotNull(_userGroupRepository.findOne(new UserGroupId()
                .setGroupId(2).setUserId(user.getId())
                .setProfile(Profile.RegisteredUser)));
        assertNotNull(_userGroupRepository.findOne(new UserGroupId()
                .setGroupId(2).setUserId(user.getId())
                .setProfile(Profile.UserAdmin)));
        assertNotNull(_userGroupRepository.findOne(new UserGroupId()
                .setGroupId(2).setUserId(user.getId())
                .setProfile(Profile.Reviewer)));
        assertNull(_userGroupRepository.findOne(new UserGroupId().setGroupId(2)
                .setUserId(user.getId()).setProfile(Profile.Administrator)));

    }

    @Test
    public void testExecPasswordUpdateAsAdmin() throws Exception {

        User startUser = getUser();

        startUser = _userRepo.save(startUser);

        assertEquals(2, _userRepo.count());

        String xml = "<request><" + Params.ID + ">" + startUser.getId() + "</"
                + Params.ID + ">" + "<" + Params.PASSWORD + ">password</"
                + Params.PASSWORD + ">" + "<" + Params.OPERATION + ">"
                + Params.Operation.RESETPW + "</" + Params.OPERATION + ">"
                + "</request>";

        MockHttpSession session = loginAsAdmin();
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders
                .get("/eng/admin.user.update");
        get.session(session);
        get.content(xml);
        get.accept(org.springframework.http.MediaType.APPLICATION_JSON);
        mockMvc.perform(get);

        User user = _userRepo.findOne(startUser.getId());
        assertExpectedUser(user);
    }

    @Test
    public void testExecPasswordUpdate() throws Exception {

        User startUser = getUser();

        startUser = _userRepo.save(startUser);

        assertEquals(2, _userRepo.count());

        String xml = "<request><" + Params.ID + ">" + startUser.getId() + "</"
                + Params.ID + ">" + "<" + Params.PASSWORD + ">password</"
                + Params.PASSWORD + ">" + "<" + Params.OPERATION + ">"
                + Params.Operation.RESETPW + "</" + Params.OPERATION + ">"
                + "</request>";

        MockHttpSession session = loginAs(startUser);
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders
                .get("/eng/admin.user.update");
        get.session(session);
        get.content(xml);
        get.accept(org.springframework.http.MediaType.APPLICATION_JSON);
        mockMvc.perform(get);

        User user = _userRepo.findOne(startUser.getId());
        assertExpectedUser(user);
    }

    @Test
    public void testPartialUpdate() throws Exception {

        User startUser = getUser();
        startUser.setName("abc");
        startUser = _userRepo.save(startUser);

        assertEquals(2, _userRepo.count());

        String xml = "<request><" + Params.ID + ">" + startUser.getId() + "</"
                + Params.ID + ">" + "<" + Params.NAME + ">firstname</"
                + Params.NAME + ">" + "<" + Params.OPERATION + ">"
                + Params.Operation.EDITINFO + "</" + Params.OPERATION + ">"
                + "</request>";

        MockHttpSession session = loginAs(startUser);
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders
                .get("/eng/admin.user.update");
        get.session(session);
        get.content(xml);
        get.accept(org.springframework.http.MediaType.APPLICATION_JSON);
        mockMvc.perform(get);

        User user = _userRepo.findOne(startUser.getId());
        assertExpectedUser(user);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCannotOwnEscalatePrivilegesToEditor() throws Exception {
        final Profile profile = Profile.Editor;

        assertCannotEscalateOwnPrivileges(profile);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCannotOwnEscalatePrivilegesToUserAdmin() throws Exception {
        final Profile profile = Profile.UserAdmin;

        assertCannotEscalateOwnPrivileges(profile);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCannotOwnEscalatePrivilegesToAdmin() throws Exception {
        final Profile profile = Profile.Administrator;

        assertCannotEscalateOwnPrivileges(profile);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCannotOwnEscalatePrivilegesToReviewer() throws Exception {
        final Profile profile = Profile.Reviewer;

        assertCannotEscalateOwnPrivileges(profile);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUseradminCannotEscalateOthersPrivilegesToAdministrator()
            throws Exception {

        final Group one = _groupRepository.findOne(2);
        User toUpdateUser = getUser();
        toUpdateUser = _userRepo.save(toUpdateUser);

        User updatingUser = getUser();
        updatingUser.setProfile(Profile.UserAdmin);
        updatingUser.setUsername("updater");
        updatingUser = _userRepo.save(updatingUser);

        _userGroupRepository.save(Arrays.asList(
                new UserGroup().setProfile(Profile.Editor)
                        .setUser(toUpdateUser).setGroup(one), new UserGroup()
                        .setProfile(Profile.UserAdmin).setUser(updatingUser)
                        .setGroup(one)));

        String xml = "<request><" + Params.ID + ">" + toUpdateUser.getId()
                + "</" + Params.ID + ">" + "<" + Params.PROFILE + ">"
                + Profile.Administrator.name() + "</" + Params.PROFILE + ">"
                + "<" + Params.OPERATION + ">" + Params.Operation.EDITINFO
                + "</" + Params.OPERATION + ">" + "</request>";

        MockHttpSession session = loginAs(updatingUser);
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders
                .get("/eng/admin.user.update");
        get.session(session);
        get.content(xml);
        get.accept(org.springframework.http.MediaType.APPLICATION_JSON);
        mockMvc.perform(get);

    }

    public void testCanReducePrivileges() throws Exception {
        final Profile profile = Profile.Guest;

        assertCannotEscalateOwnPrivileges(profile);
    }

    public void testCanKeepSamePrivileges() throws Exception {
        final Profile profile = Profile.RegisteredUser;

        assertCannotEscalateOwnPrivileges(profile);
    }

    private void assertCannotEscalateOwnPrivileges(Profile profile)
            throws Exception {
        User startUser = getUser();
        startUser.setProfile(Profile.RegisteredUser);
        startUser = _userRepo.save(startUser);

        assertEquals(2, _userRepo.count());

        String xml = "<request><" + Params.ID + ">" + startUser.getId() + "</"
                + Params.ID + ">" + "<" + Params.NAME + ">newname</"
                + Params.NAME + ">" + "<" + Params.PROFILE + ">"
                + profile.name() + "</" + Params.PROFILE + ">" + "<"
                + Params.OPERATION + ">" + Params.Operation.EDITINFO + "</"
                + Params.OPERATION + ">" + "</request>";

        MockHttpSession session = loginAs(startUser);
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders
                .get("/eng/admin.user.update");
        get.session(session);
        get.content(xml);
        get.accept(org.springframework.http.MediaType.APPLICATION_JSON);
        mockMvc.perform(get);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecPasswordUpdateIllegalAccess() throws Exception {

        User toUpdateUser = getUser();
        toUpdateUser = _userRepo.save(toUpdateUser);

        User updatingUser = getUser();
        updatingUser.setProfile(Profile.Editor);
        updatingUser.setUsername("updater");
        updatingUser = _userRepo.save(updatingUser);

        String xml = "<request><" + Params.ID + ">" + toUpdateUser.getId()
                + "</" + Params.ID + ">" + "<" + Params.PASSWORD
                + ">password</" + Params.PASSWORD + ">" + "<"
                + Params.OPERATION + ">" + Params.Operation.RESETPW + "</"
                + Params.OPERATION + ">" + "</request>";

        MockHttpSession session = loginAs(updatingUser);
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders
                .get("/eng/admin.user.update");
        get.session(session);
        get.content(xml);
        get.accept(org.springframework.http.MediaType.APPLICATION_JSON);
        mockMvc.perform(get);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateUserInOtherGroup() throws Exception {
        final Group one = _groupRepository.findOne(2);
        final Group two = _groupRepository.save(GroupRepositoryTest
                .newGroup(_inc));
        User toUpdateUser = getUser();
        toUpdateUser = _userRepo.save(toUpdateUser);

        User updatingUser = getUser();
        updatingUser.setProfile(Profile.Editor);
        updatingUser.setUsername("updater");
        updatingUser = _userRepo.save(updatingUser);

        _userGroupRepository.save(Arrays.asList(
                new UserGroup().setProfile(Profile.Editor)
                        .setUser(toUpdateUser).setGroup(one), new UserGroup()
                        .setProfile(Profile.UserAdmin).setUser(updatingUser)
                        .setGroup(two)));

        String xml = "<request><" + Params.ID + ">" + toUpdateUser.getId()
                + "</" + Params.ID + ">" + "<" + Params.PASSWORD
                + ">password</" + Params.PASSWORD + ">" + "<"
                + Params.OPERATION + ">" + Params.Operation.RESETPW + "</"
                + Params.OPERATION + ">" + "</request>";

        MockHttpSession session = loginAs(updatingUser);
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders
                .get("/eng/admin.user.update");
        get.session(session);
        get.content(xml);
        get.accept(org.springframework.http.MediaType.APPLICATION_JSON);
        mockMvc.perform(get);

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
                new UserGroup().setProfile(Profile.Editor)
                        .setUser(toUpdateUser).setGroup(one), new UserGroup()
                        .setProfile(Profile.UserAdmin).setUser(updatingUser)
                        .setGroup(one)));

        String xml = "<request><" + Params.ID + ">" + toUpdateUser.getId()
                + "</" + Params.ID + ">" + "<" + Params.PASSWORD
                + ">password</" + Params.PASSWORD + ">" + "<"
                + Params.OPERATION + ">" + Params.Operation.RESETPW + "</"
                + Params.OPERATION + ">" + "</request>";

        MockHttpSession session = loginAs(updatingUser);
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders
                .get("/eng/admin.user.update");
        get.session(session);
        get.content(xml);
        get.accept(org.springframework.http.MediaType.APPLICATION_JSON);
        mockMvc.perform(get);

        User user = _userRepo.findOne(toUpdateUser.getId());
        assertExpectedUser(user);
    }

    private User getUser() {
        User startUser = new User().setName("firstname").setKind("consultant")
                .setOrganisation("c2c").setProfile(Profile.UserAdmin)
                .setSurname("lastname").setUsername("newuser");
        startUser.getSecurity().setPassword(_encoder.encode("password"));
        startUser.getAddresses().add(
                new Address().setAddress("address1").setCity("city1")
                        .setCountry("ca").setState("state1").setZip("zip1"));
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
