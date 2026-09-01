/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Verifies that {@link User#asXml()} never leaks the password hash or other
 * {@link org.springframework.security.core.userdetails.UserDetails} security fields, regardless
 * of which caller invokes it (e.g. {@link org.fao.geonet.util.XslUtil#getUserDetails}, admin
 * user listings, feedback/download services). Unlike {@code UserTest}, this does not extend
 * {@link org.fao.geonet.repository.AbstractSpringDataTest}, so it runs with the default,
 * DB-less {@code mvn test} build rather than only under the {@code it} Maven profile.
 */
public class UserXmlSanitizationTest {

    private User newUser() {
        User user = new User().setName("name1").setUsername("username1").setId(1);
        user.getSecurity().setPassword("secret-hash");
        user.getSecurity().setAuthType("authtype1");
        user.getSecurity().getSecurityNotifications().add(UserSecurityNotification.UPDATE_HASH_REQUIRED);
        user.setProfile(Profile.Administrator);
        return user;
    }

    @Test
    public void asXmlDoesNotExposePasswordHash() {
        Element xml = newUser().asXml();

        assertNoElementNamed(xml, "password");
    }

    @Test
    public void asXmlDoesNotExposeAuthoritiesOrAccountStatusFlags() {
        Element xml = newUser().asXml();

        assertNull("authorities must not be serialized to XML", xml.getChild("authorities"));
        assertNull("accountnonexpired must not be serialized to XML", xml.getChild("accountnonexpired"));
        assertNull("accountnonlocked must not be serialized to XML", xml.getChild("accountnonlocked"));
        assertNull("credentialsnonexpired must not be serialized to XML", xml.getChild("credentialsnonexpired"));
        // getRandomPassword() is a static utility returning a fresh random value, not a user
        // property, so it must not be serialized either.
        assertNull("randompassword must not be serialized to XML", xml.getChild("randompassword"));
    }

    @Test
    public void asXmlStillExposesNonSensitiveSecurityFields() {
        Element xml = newUser().asXml();

        Element security = xml.getChild("security");
        assertNotNull("the security element itself should still be present", security);
        assertNull("password must not appear inside the security element either", security.getChild("password"));
        assertNotNull("authtype is not sensitive and should still be available",
            security.getChild("authtype"));
        assertNotNull("securitynotifications is not sensitive and should still be available",
            security.getChild("securitynotifications"));
    }

    /**
     * Recursively asserts that no element anywhere in the tree has the given name, since a
     * regression here (e.g. excluding the wrong key) could otherwise leave the field reachable
     * through a different nesting level than the one a narrower assertion happens to check.
     */
    private void assertNoElementNamed(Element root, String name) {
        assertFalse("found unexpected <" + name + "> in " + root.getName(), name.equals(root.getName()));

        @SuppressWarnings("unchecked")
        List<Element> children = root.getChildren();
        for (Element child : children) {
            assertNoElementNamed(child, name);
        }
    }
}
