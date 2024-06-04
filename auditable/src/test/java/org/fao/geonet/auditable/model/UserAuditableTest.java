/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.auditable.model;

import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class UserAuditableTest {

    @Test
    public void testBuildUserAuditable() {
        Group group = new Group()
            .setId(1)
            .setName("sample");


        User user = new User()
            .setId(1)
            .setName("name")
            .setSurname("surname")
            .setUsername("username")
            .setEnabled(true)
            .setEmailAddresses(new HashSet<>(Arrays.asList("test@mail.com")))
            .setProfile(Profile.Reviewer);


        List<UserGroup> userGroupList = new ArrayList<>();
        UserGroup userGroup1 = new UserGroup()
            .setGroup(group)
            .setUser(user)
            .setProfile(Profile.Editor);

        UserGroup userGroup2 = new UserGroup()
            .setGroup(group)
            .setUser(user)
            .setProfile(Profile.Reviewer);

        userGroupList.add(userGroup1);
        userGroupList.add(userGroup2);

        UserAuditable userAuditable = UserAuditable.build(user, userGroupList);

        assertEquals(user.getId(), userAuditable.getId());
        assertEquals(user.isEnabled(), userAuditable.isEnabled());
        assertEquals(user.getName(), userAuditable.getName());
        assertEquals(user.getSurname(), userAuditable.getSurname());
        assertEquals(user.getUsername(), userAuditable.getUsername());
        assertEquals(user.getEmailAddresses(), userAuditable.getEmailAddresses());
        assertEquals(user.getProfile().toString(), userAuditable.getProfile());
        assertEquals(0, userAuditable.getGroupsRegisteredUser().size());
        assertEquals(1, userAuditable.getGroupsEditor().size());
        assertEquals(1, userAuditable.getGroupsReviewer().size());
        assertEquals(0, userAuditable.getGroupsUserAdmin().size());

        assertEquals(group.getName(), userAuditable.getGroupsEditor().get(0));
        assertEquals(group.getName(), userAuditable.getGroupsReviewer().get(0));
    }
}
