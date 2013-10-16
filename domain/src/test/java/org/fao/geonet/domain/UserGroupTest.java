package org.fao.geonet.domain;

import org.jdom.Element;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Test usergroup test.
 *
 * User: Jesse
 * Date: 10/15/13
 * Time: 4:19 PM
 */
public class UserGroupTest {
    @Test
    public void testAsXml() throws Exception {
        final Group group = new Group().setId(1);
        final User user = new User().setId(2);
        final UserGroup userGroup = new UserGroup().setGroup(group).setUser(user).setProfile(Profile.Editor);

        final Element element = userGroup.asXml();

        assertEquals(""+group.getId(), element.getChildText("group"));
        assertEquals("" + user.getId(), element.getChildText("user"));
        assertEquals(Profile.Editor.name(), element.getChildText("profile"));
    }
}
