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

package org.fao.geonet.domain;

import org.jdom.Element;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Test usergroup test.
 *
 * User: Jesse Date: 10/15/13 Time: 4:19 PM
 */
public class UserGroupTest {
    @Test
    public void testAsXml() throws Exception {
        final Group group = new Group().setId(1);
        final User user = new User().setId(2);
        final UserGroup userGroup = new UserGroup().setGroup(group).setUser(user).setProfile(Profile.Editor);

        final Element element = userGroup.asXml();

        assertEquals("" + group.getId(), element.getChildText("group"));
        assertEquals("" + user.getId(), element.getChildText("user"));
        assertEquals(Profile.Editor.name(), element.getChildText("profile"));
    }
}
