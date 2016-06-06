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

package jeeves.interfaces;

import org.fao.geonet.domain.Profile;
import org.junit.Test;

import java.util.Set;

import static org.fao.geonet.domain.Profile.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProfileTest {

    @Test
    public void testGetParents() {
        assertEquals(2, Administrator.getParents().size());
        assertTrue(Administrator.getParents().contains(UserAdmin));
        assertTrue(Administrator.getParents().contains(Monitor));
        assertContainsOnly(Reviewer, UserAdmin.getParents());
        assertContainsOnly(Editor, Reviewer.getParents());
        assertContainsOnly(RegisteredUser, Editor.getParents());
        assertContainsOnly(Guest, RegisteredUser.getParents());
        assertEquals(0, Monitor.getParents().size());
        assertEquals(0, Guest.getParents().size());
    }

    private void assertContainsOnly(Profile profile, Set<Profile> parents) {
        assertEquals(1, parents.size());
        assertEquals(profile, parents.iterator().next());
    }

    @Test
    public void testGetAll() {
        assertContainsAllExactly(Administrator.getAll(), Administrator, UserAdmin, Reviewer, Editor, RegisteredUser, Guest, Monitor);
        assertContainsAllExactly(UserAdmin.getAll(), UserAdmin, Reviewer, Editor, RegisteredUser, Guest);
        assertContainsAllExactly(Reviewer.getAll(), Reviewer, Editor, RegisteredUser, Guest);
        assertContainsAllExactly(Editor.getAll(), Editor, RegisteredUser, Guest);
        assertContainsAllExactly(Editor.getAll(), Editor, RegisteredUser, Guest);
    }

    private void assertContainsAllExactly(Set<Profile> all, Profile... profiles) {
        assertEquals(profiles.length, all.size());

        for (Profile profile : profiles) {
            assertTrue(profile + " is not one of " + all, all.contains(profile));
        }
    }
}
