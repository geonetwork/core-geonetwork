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
package org.fao.geonet.kernel;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link AccessManager}.
 */
public class AccessManagerTest extends AbstractCoreIntegrationTest {
    @Autowired
    private SettingManager settingManager;
    @Autowired
    private AccessManager accessManager;

    /**
     * In previous versions network needed to end in a series of zeroes to be recognized as part
     * of the intranet. This is a test for checking if this kind of network definition still
     * works.
     */
    @Test
    public void testIsIntranetBackwardCompatibility() {
        settingManager.setValue(Settings.SYSTEM_INTRANET_NETMASK, "255.255.255.0");
        settingManager.setValue(Settings.SYSTEM_INTRANET_NETWORK, "192.168.1.0");
        String ipPrefix = "192.168.1.";

        for (int i = 1; i < 255; i++) {
            String ipToTest = ipPrefix + i;
            assertTrue("IP " + ipToTest +" must be in the intranet", accessManager.isIntranet(ipToTest));
        }
        assertFalse("IP 192.168.2.1 is not in the intranet", accessManager.isIntranet("192.168.2.1"));
    }

    @Test
    public void testIsIntranet() {
        String ipPrefix = "192.168.1.";
        settingManager.setValue(Settings.SYSTEM_INTRANET_NETMASK, "255.255.255.255");
        settingManager.setValue(Settings.SYSTEM_INTRANET_NETWORK, "192.168.1.1");
        assertTrue(accessManager.isIntranet("192.168.1.1"));
        assertFalse(accessManager.isIntranet("192.168.1.2"));
        assertFalse(accessManager.isIntranet("192.168.1.0"));

        settingManager.setValue(Settings.SYSTEM_INTRANET_NETMASK, "255.255.255.254");
        assertTrue(accessManager.isIntranet("192.168.1.0"));
        assertTrue(accessManager.isIntranet("192.168.1.1"));
        assertFalse(accessManager.isIntranet("192.168.1.2"));

        settingManager.setValue(Settings.SYSTEM_INTRANET_NETMASK, "255.255.255.252");
        assertTrue(accessManager.isIntranet("192.168.1.0"));
        assertTrue(accessManager.isIntranet("192.168.1.1"));
        assertTrue(accessManager.isIntranet("192.168.1.2"));
        assertTrue(accessManager.isIntranet("192.168.1.3"));
        assertFalse(accessManager.isIntranet("192.168.1.4"));

        settingManager.setValue(Settings.SYSTEM_INTRANET_NETMASK, "255.255.255.248");
        for (int i = 1; i < 8; i++) {
            String ipToTest = ipPrefix + i;
            assertTrue("IP " + ipToTest +" is in the intranet", accessManager.isIntranet(ipToTest));
        }
        assertFalse("IP " + "192.168.1.8" +" is not in the intranet", accessManager.isIntranet("192.168.1.8"));

        settingManager.setValue(Settings.SYSTEM_INTRANET_NETMASK, "255.255.255.0");
        for (int i = 1; i < 256; i++) {
            String ipToTest = ipPrefix + i;
            assertTrue("IP " + ipToTest +" is in the intranet", accessManager.isIntranet(ipToTest));
        }
        assertFalse("IP " + "192.168.2.1" +" is not in the intranet", accessManager.isIntranet("192.168.2.1"));
    }
}
