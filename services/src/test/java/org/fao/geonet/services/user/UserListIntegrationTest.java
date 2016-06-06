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

import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBElement;

import static org.junit.Assert.assertEquals;

/**
 * Test listing a user service.
 *
 * User: Jesse Date: 10/12/13 Time: 8:30 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class UserListIntegrationTest extends AbstractServiceIntegrationTest {
    @Autowired
    UserRepository _userRepo;
    @Autowired
    UserGroupRepository _userGroupRepo;
    @Autowired
    GroupRepository _groupRepo;
    @Autowired
    List listService;
    private AtomicInteger inc = new AtomicInteger();

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

        assertEquals("record/username", editor.getUsername(), user.getUsername());
        assertEquals("record/primaryaddress/address", editor.getPrimaryAddress().getAddress(), user.getPrimaryAddress().getAddress());
        assertEquals("record/profile", editor.getProfile().name(), user.getProfile().name());
        assertEquals("record/emailaddresses/emailaddress", editor.getEmailAddresses().iterator().next(), user.getEmailAddresses().iterator().next());
    }


    @Test
    public void testExecAsAdmin() throws Exception {
        final User entity = UserRepositoryTest.newUser(inc);
        _userRepo.save(entity);

        _userRepo.save(UserRepositoryTest.newUser(inc));

        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

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
