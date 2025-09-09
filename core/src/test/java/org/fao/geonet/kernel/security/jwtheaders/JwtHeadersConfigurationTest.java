/*
 * Copyright (C) 2024 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.security.jwtheaders;

import org.junit.Assert;
import org.junit.Test;

/**
 * very simple tests for JwtHeadersConfiguration for the GN-only portions.
 */
public class JwtHeadersConfigurationTest {

    //Very very simple test to ensure that setters/getters are working correctly
    @Test
    public void testGetSet() {
        var config = JwtHeadersIntegrationTest.getBasicConfig();

        //CONST
        Assert.assertEquals("autologin", config.getLoginType());
        Assert.assertEquals("JWT-HEADERS", config.getSecurityProvider());

        config.setUpdateGroup(false);
        Assert.assertEquals(false, config.isUpdateGroup());
        Assert.assertEquals(false, !config.isUserGroupUpdateEnabled());
        config.setUpdateGroup(true);
        Assert.assertEquals(true, config.isUpdateGroup());
        Assert.assertEquals(true, !config.isUserGroupUpdateEnabled());


        config.setUpdateProfile(false);
        Assert.assertEquals(false, config.isUpdateProfile());
        Assert.assertEquals(false, !config.isUserProfileUpdateEnabled());
        config.setUpdateProfile(true);
        Assert.assertEquals(true, config.isUpdateProfile());
        Assert.assertEquals(true, !config.isUserProfileUpdateEnabled());


        Assert.assertEquals(config.jwtConfiguration, config.getJwtConfiguration());
        config.setJwtConfiguration(null);
        Assert.assertNull(config.getJwtConfiguration());
    }
}
