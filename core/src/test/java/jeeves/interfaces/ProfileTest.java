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
    public void testGetChildren() {
        assertContainsAllExactly(Administrator.getChildren(), UserAdmin, Monitor);
        assertContainsOnly(Reviewer, UserAdmin.getChildren());
        assertContainsOnly(Editor, Reviewer.getChildren());
        assertContainsOnly(RegisteredUser, Editor.getChildren());
        assertContainsOnly(Guest, RegisteredUser.getChildren());
        assertEquals(0, Monitor.getChildren().size());
        assertEquals(0, Guest.getChildren().size());
    }

    @Test
    public void testGetParents() {
        assertEquals(0, Administrator.getParents().size());
        assertContainsOnly(Administrator, UserAdmin.getParents());
        assertContainsOnly(UserAdmin, Reviewer.getParents());
        assertContainsOnly(Reviewer, Editor.getParents());
        assertContainsOnly(Editor, RegisteredUser.getParents());
        assertContainsOnly(RegisteredUser, Guest.getParents());
        assertContainsOnly(Administrator, Monitor.getParents());

    }

    private void assertContainsOnly(Profile profile, Set<Profile> parents) {
        assertEquals(1, parents.size());
        assertEquals(profile, parents.iterator().next());
    }

    @Test
    public void testGetProfileAndAllChildren() {
        assertContainsAllExactly(Administrator.getProfileAndAllChildren(), Administrator, UserAdmin, Reviewer, Editor, RegisteredUser, Guest, Monitor);
        assertContainsAllExactly(UserAdmin.getProfileAndAllChildren(), UserAdmin, Reviewer, Editor, RegisteredUser, Guest);
        assertContainsAllExactly(Reviewer.getProfileAndAllChildren(), Reviewer, Editor, RegisteredUser, Guest);
        assertContainsAllExactly(Editor.getProfileAndAllChildren(), Editor, RegisteredUser, Guest);
        assertContainsAllExactly(RegisteredUser.getProfileAndAllChildren(), RegisteredUser, Guest);
        assertContainsAllExactly(Guest.getProfileAndAllChildren(), Guest);
        assertContainsAllExactly(Monitor.getProfileAndAllChildren(), Monitor);
    }

    @Test
    public void testGetProfileAndAllParents() {
        assertContainsAllExactly(Administrator.getProfileAndAllParents(), Administrator);
        assertContainsAllExactly(UserAdmin.getProfileAndAllParents(), UserAdmin, Administrator);
        assertContainsAllExactly(Reviewer.getProfileAndAllParents(), Reviewer, UserAdmin, Administrator);
        assertContainsAllExactly(Editor.getProfileAndAllParents(), Editor, Reviewer, UserAdmin, Administrator);
        assertContainsAllExactly(RegisteredUser.getProfileAndAllParents(), RegisteredUser, Editor, Reviewer, UserAdmin, Administrator);
        assertContainsAllExactly(Guest.getProfileAndAllParents(), Guest, RegisteredUser, Editor, Reviewer, UserAdmin, Administrator);
        assertContainsAllExactly(Monitor.getProfileAndAllParents(), Monitor, Administrator);
    }

    private void assertContainsAllExactly(Set<Profile> all, Profile... profiles) {
        assertEquals(profiles.length, all.size());

        for (Profile profile : profiles) {
            assertTrue(profile + " is not one of " + all, all.contains(profile));
        }
    }
}
