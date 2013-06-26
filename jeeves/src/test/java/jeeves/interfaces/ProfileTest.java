package jeeves.interfaces;

import static jeeves.interfaces.Profile.*;
import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

public class ProfileTest {

    @Test
    public void testGetParents() {
        assertContainsOnly(UserAdmin, Administrator.getParents());
        assertContainsOnly(Reviewer, UserAdmin.getParents());
        assertContainsOnly(Editor, Reviewer.getParents());
        assertContainsOnly(RegisteredUser, Editor.getParents());
        assertContainsOnly(Guest, RegisteredUser.getParents());
        assertEquals(0, Monitor.getParents().size());
    }

    private void assertContainsOnly(Profile profile, Set<Profile> parents) {
        assertEquals(1, parents.size());
        assertEquals(profile, parents.iterator().next());
    }

    @Test
    public void testGetAll() {
        assertContainsAllExactly(Administrator.getAll(), Administrator, UserAdmin, Reviewer, Editor, RegisteredUser, Guest);
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
