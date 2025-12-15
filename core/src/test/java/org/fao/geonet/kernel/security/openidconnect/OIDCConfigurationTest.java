/*
 * Copyright (C) 2025 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.security.openidconnect;

import org.apache.commons.jcs3.access.exception.InvalidArgumentException;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.security.SecurityProviderConfiguration;
import org.junit.Test;

import static org.junit.Assert.*;

public class OIDCConfigurationTest {

    @Test
    public void testDefaultValues() {
        OIDCConfiguration config = new OIDCConfiguration();
        assertEquals("email", config.getUserNameAttribute());
        assertEquals("organization", config.getOrganizationProperty());
        assertEquals(":", config.getGroupPermissionSeparator());
        assertEquals("groups", config.getIdTokenRoleLocation());
        assertEquals("Guest", config.getMinimumProfile().name());
        assertTrue(config.isUpdateProfile());
        assertTrue(config.isUpdateGroup());
        assertFalse(config.isLogSensitiveInformation());
    }

    @Test
    public void testSetters() {
        OIDCConfiguration config = new OIDCConfiguration();
        config.setUserNameAttribute("username");
        assertEquals("username", config.getUserNameAttribute());

        config.setOrganizationProperty("org");
        assertEquals("org", config.getOrganizationProperty());

        config.setGroupPermissionSeparator("-");
        assertEquals("-", config.getGroupPermissionSeparator());

        config.setIdTokenRoleLocation("roles");
        assertEquals("roles", config.getIdTokenRoleLocation());

        config.setUpdateProfile(false);
        assertFalse(config.isUpdateProfile());

        config.setUpdateGroup(false);
        assertFalse(config.isUpdateGroup());

        config.setLogSensitiveInformation(true);
        assertTrue(config.isLogSensitiveInformation());
    }

    @Test
    public void testRoleConverterString() {
        OIDCConfiguration config = new OIDCConfiguration();
        config.setRoleConverterString("admin=Editor,user=Guest");
        assertEquals("Editor", config.getRoleConverter().get("admin"));
        assertEquals("Guest", config.getRoleConverter().get("user"));
    }

    @Test
    public void testLoginType() throws Exception {
        OIDCConfiguration config = new OIDCConfiguration();
        config.setLoginType(SecurityProviderConfiguration.LoginType.LINK.name());
        assertEquals(SecurityProviderConfiguration.LoginType.LINK.toString(), config.getLoginType());

        config.setLoginType(SecurityProviderConfiguration.LoginType.AUTOLOGIN.name());
        assertEquals(SecurityProviderConfiguration.LoginType.AUTOLOGIN.toString(), config.getLoginType());

        assertThrows(InvalidArgumentException.class, () -> {
            config.setLoginType(SecurityProviderConfiguration.LoginType.FORM.name());
        });

        assertThrows(BadParameterEx.class, () -> {
            config.setLoginType("INVALID");
        });
    }
}
