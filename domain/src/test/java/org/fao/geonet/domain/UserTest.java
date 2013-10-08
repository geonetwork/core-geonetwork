package org.fao.geonet.domain;

import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.UserRepositoryTest;
import org.jdom.Element;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

/**
 * Test user methods.
 * User: Jesse
 * Date: 10/6/13
 * Time: 9:30 PM
 */
public class UserTest extends AbstractSpringDataTest {
    private AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testAsXml() throws Exception {
        final User user = UserRepositoryTest.newUser(_inc);
        user.getSecurity().setAuthType("authtype");
        user.getSecurity().getSecurityNotifications().add(UserSecurityNotification.HASH_UPDATE_REQUIRED);

        Element xml = user.asXml();

        final Element security = xml.getChild("security");
        assertNull(security.getChild("password"));
        assertEquals(user.getSecurity().getSecurityNotifications().toString(), xml.getChild("security").getChild("securitynotifications").getText());


    }
}
