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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.auditable.UserAuditable;
import org.junit.Test;
import org.springframework.util.StringUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserAuditableTest {

    @Test
    public void testBuildUserAuditable() {
        Group group = new Group().setId(1).setName("sample");
        Group group2 = new Group().setId(2).setName("sampleGroup2");


        User user = new User()
            .setId(1)
            .setName("name")
            .setSurname("surname")
            .setUsername("username")
            .setEnabled(true)
            .setEmailAddresses(new HashSet<>(Collections.singleton("test@mail.com")))
            .setProfile(Profile.Reviewer);


        List<UserGroup> userGroupList = new ArrayList<>();
        UserGroup userGroup1 = new UserGroup()
            .setGroup(group)
            .setUser(user)
            .setProfile(Profile.Editor);

        UserGroup userGroup2 = new UserGroup()
            .setGroup(group2)
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
        assertEquals(user.getEmailAddresses().toArray()[0], userAuditable.getEmailAddress());
        assertEquals(user.getProfile().toString(), userAuditable.getProfile());
        assertFalse(StringUtils.hasLength(userAuditable.getGroupsRegisteredUser()));
        assertTrue(userAuditable.getGroupsEditor().contains(group.getName()));
        assertTrue(userAuditable.getGroupsReviewer().contains(group2.getName()));
        assertFalse(StringUtils.hasLength(userAuditable.getGroupsUserAdmin()));

        assertEquals(group.getName(), userAuditable.getGroupsEditor());
        assertEquals(group2.getName(), userAuditable.getGroupsReviewer());
    }
}
