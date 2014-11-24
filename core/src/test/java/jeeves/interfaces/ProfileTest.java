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
            assertTrue(profile+" is not one of "+all, all.contains(profile));
        }
    }
}
