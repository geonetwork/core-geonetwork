/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.services.user;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.sources.http.JeevesServlet;

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
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpSession;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Test User update service.
 * <p/>
 * User: Jesse Date: 10/16/13 Time: 3:20 PM
 */
@ContextConfiguration(inheritLocations = true, locations = "classpath:encoder-bean.xml")
public class UserUpdateIntegrationTest extends AbstractServiceIntegrationTest {

    private static String zip = "zip1";
    private static String state = "state1";
    private static String kind = "consultant";
    private static String surname = "lastname";
    private static String password = "password";
    private static String country = "ca";
    private static String city = "city1";
    private static String username = "newuser";
    private static String email = "newuser@email.com";
    private static String address = "address1";
    private static String name = "firstname";
    private static String profile = Profile.UserAdmin.name();
    private static String organization = "c2c";
    private static Boolean enabled = Boolean.TRUE;

    @Autowired
    UserGroupRepository _userGroupRepository;
    @Autowired
    GroupRepository _groupRepository;
    @Autowired
    PasswordEncoder _encoder;
    @Autowired
    Update update;
    private AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testExecResetPassword() throws Exception {

        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession();
        HttpSession session = new MockHttpSession();

        session.setAttribute(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY,
            serviceContext.getUserSession());
        User admin = _userRepo.findOneByUsername("admin");
        assertFalse(_encoder.matches(password, admin.getPassword()));

        update.resetPassword(session, serviceContext.getUserSession().getUserId(), password, password);

        admin = _userRepo.findOneByUsername("admin");
        assertTrue(_encoder.matches(password, admin.getPassword()));
    }

    @Test
    public void testExecAddNewUserCompatibilityModeAsAdmin() throws Exception {

        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession();
        HttpSession session = new MockHttpSession();

        session.setAttribute(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY,
            serviceContext.getUserSession());

        request.addParameter("groups_RegisteredUser", "2");
        request.addParameter("groups_Reviewer", "2");

        update.run(session, request, Params.Operation.NEWUSER, null, username,
            password, profile, surname, name, address, city, state, zip,
            country, email, organization, kind, enabled);

        assertEquals(2, _userRepo.count());
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
        assertNull(_userGroupRepository.findOne(new UserGroupId()
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

        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession();
        HttpSession session = new MockHttpSession();

        session.setAttribute(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY,
            serviceContext.getUserSession());

        request.addParameter("groups_RegisteredUser", "2");
        request.addParameter("groups_Reviewer", "2");

        update.run(session, request, Params.Operation.FULLUPDATE,
            Integer.toString(startUser.getId()), username, password,
            profile, surname, name, address, city, state, zip, country,
            email, organization, kind, enabled);

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
        assertNull(_userGroupRepository.findOne(new UserGroupId()
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

        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession();
        HttpSession session = new MockHttpSession();

        session.setAttribute(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY,
            serviceContext.getUserSession());

        update.run(session, request, Params.Operation.RESETPW,
            Integer.toString(startUser.getId()), username, password, null,
            null, null, null, null, null, null, null, null, null, null, null);

        User user = _userRepo.findOne(startUser.getId());
        assertExpectedUser(user);
    }

    @Test
    public void testExecPasswordUpdate() throws Exception {

        User startUser = getUser();

        startUser = _userRepo.save(startUser);

        assertEquals(2, _userRepo.count());

        final UserSession userSession = new UserSession();
        final ServiceContext serviceContext = createServiceContext();
        userSession.loginAs(startUser);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession();
        HttpSession session = new MockHttpSession();

        session.setAttribute(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY,
            serviceContext.getUserSession());

        update.run(session, request, Params.Operation.RESETPW,
            Integer.toString(startUser.getId()), username, password, null,
            null, null, null, null, null, null, null, null, null, null, null);

        User user = _userRepo.findOne(startUser.getId());
        assertExpectedUser(user);
    }

    @Test
    public void testPartialUpdate() throws Exception {

        User startUser = getUser();
        startUser.setName("abc");
        startUser = _userRepo.save(startUser);

        assertEquals(2, _userRepo.count());

        final UserSession userSession = new UserSession();
        final ServiceContext serviceContext = createServiceContext();
        userSession.loginAs(startUser);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession();
        HttpSession session = new MockHttpSession();

        session.setAttribute(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY,
            serviceContext.getUserSession());

        update.run(session, request, Params.Operation.EDITINFO,
            Integer.toString(startUser.getId()), username, null, null,
            null, "firstname", null, null, null, null, null, null, null,
            null, null);

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

        final UserSession userSession = new UserSession();
        final ServiceContext serviceContext = createServiceContext();
        userSession.loginAs(updatingUser);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession();
        HttpSession session = new MockHttpSession();

        session.setAttribute(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY,
            serviceContext.getUserSession());

        update.run(session, request, Params.Operation.EDITINFO,
            Integer.toString(updatingUser.getId()), null, null,
            Profile.Administrator.name(), null, null, null, null, null,
            null, null, null, null, null, null);

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

        final UserSession userSession = new UserSession();
        final ServiceContext serviceContext = createServiceContext();
        userSession.loginAs(startUser);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession();
        HttpSession session = new MockHttpSession();

        session.setAttribute(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY,
            serviceContext.getUserSession());

        update.run(session, request, Params.Operation.EDITINFO,
            Integer.toString(startUser.getId()), null, null,
            profile.name(), null, "newname", null, null, null, null, null,
            null, null, null, null);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecPasswordUpdateIllegalAccess() throws Exception {

        User toUpdateUser = getUser();
        toUpdateUser = _userRepo.save(toUpdateUser);

        User updatingUser = getUser();
        updatingUser.setProfile(Profile.Editor);
        updatingUser.setUsername("updater");
        updatingUser = _userRepo.save(updatingUser);

        final UserSession userSession = new UserSession();
        final ServiceContext serviceContext = createServiceContext();
        userSession.loginAs(updatingUser);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession();
        HttpSession session = new MockHttpSession();

        session.setAttribute(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY,
            serviceContext.getUserSession());

        update.run(session, request, Params.Operation.RESETPW,
            Integer.toString(toUpdateUser.getId()), null, password, null,
            null, null, null, null, null, null, null, null, null, null, null);
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

        final UserSession userSession = new UserSession();
        final ServiceContext serviceContext = createServiceContext();
        userSession.loginAs(updatingUser);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession();
        HttpSession session = new MockHttpSession();

        session.setAttribute(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY,
            serviceContext.getUserSession());

        update.run(session, request, Params.Operation.RESETPW,
            Integer.toString(toUpdateUser.getId()), null, password, null,
            null, null, null, null, null, null, null, null, null, null, null);

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


        final UserSession userSession = new UserSession();
        final ServiceContext serviceContext = createServiceContext();
        userSession.loginAs(updatingUser);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession();
        HttpSession session = new MockHttpSession();

        session.setAttribute(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY,
            serviceContext.getUserSession());

        update.run(session, request, Params.Operation.RESETPW,
            Integer.toString(toUpdateUser.getId()), null, password, null,
            null, null, null, null, null, null, null, null, null, null, null);

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
