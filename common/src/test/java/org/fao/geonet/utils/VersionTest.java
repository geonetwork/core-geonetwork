/*
 * Copyright (C) 2001-2021 Food and Agriculture Organization of the
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
package org.fao.geonet.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Double check the functionality of Version utility class
 */
public class VersionTest {

    final Version V3_10_SNAPSHOT = new Version("3", "10", null, null, "SNAPSHOT");
    final Version V3_10_5 = new Version("3", "10", "5");
    final Version V3_10_0 = new Version("3", "10", null);

    final Version V1_SNAPSHOT = new Version("1", null, null, null, "SNAPSHOT");
    final Version V1_1 = new Version("1", "1", null);
    final Version V1_1_0 = new Version("1", "1", "0");
    final Version V1 = new Version("1", null, null);
    final Version V1_0_SNAPSHOT = new Version("1", "0", null, null, "SNAPSHOT");
    final Version V1_0 = new Version("1", "0", null);
    final Version V1_0_0 = new Version("1", "0", "0");

    @Test
    public void eq() throws Exception {
        assertEquals(V1, V1_0);
        assertEquals(V1.hashCode(), V1_0.hashCode());

        assertEquals(V1, V1_0_0);
        assertEquals(V1.hashCode(), V1_0_0.hashCode());

        assertEquals(V1_0, V1_0_0);
        assertEquals(V1_0.hashCode(), V1_0_0.hashCode());

        assertNotEquals(V1_0, V1_1);
        assertNotEquals(V1_0.hashCode(), V1_1.hashCode());

        assertNotEquals(V1, V1_SNAPSHOT);
        assertNotEquals(V1.hashCode(), V1_SNAPSHOT.hashCode());
    }

    public void string() {
        assertEquals("1", V1.toString());
        assertEquals("1.0", V1_0.toString());
        assertEquals("1.0.0", V1_0_0.toString());
        assertEquals("1.1.0", V1_1_0.toString());

        assertEquals("1-SNAPSHOT", V1_SNAPSHOT.toString());
        assertEquals("1.0-SNAPSHOT", V1_0_SNAPSHOT.toString());

    }

    @Test
    public void compare() throws Exception {
        assertEquals("1 = 1", 0, V1.compareTo(V1));
        assertEquals("1 = 1.0", 0, V1.compareTo(V1_0));
        assertEquals("1 = 1.0.0", 0, V1.compareTo(V1_0_0));
        assertEquals("1.1 > 1.0", 1, V1_1.compareTo(V1_0));

        assertEquals("1-SNAPSHOT > 1", 1, V1_SNAPSHOT.compareTo(V1));
        assertEquals("1-SNAPSHOT > 1.0", 1, V1_SNAPSHOT.compareTo(V1_0));
        assertEquals("1-SNAPSHOT > 1.0.0", 1, V1_SNAPSHOT.compareTo(V1_0_0));
    }

    @Test
    public void compareMore() throws Exception {
        assertCompareVersions("3.10.0 < 3.10.5");
        assertCompareVersions("3.10.6 > 3.10.5");
        assertCompareVersions("3.10.6 = 3.10.6-0");
        assertCompareVersions("3.10.2 < 3.10-SNAPSHOT");
        assertCompareVersions("3.10-RC < 3.10");
        assertCompareVersions("3.10-RC < 3.10.0");
        assertCompareVersions("3.10 = 3.10.0-0");
    }

    private void assertCompareVersions(String comparison) {
        String[] split = comparison.split(" ");
        Version leftSideVersion = Version.parseVersionNumber(split[0]);
        Version rightSideVersion = Version.parseVersionNumber(split[2]);
        char operator = split[1].charAt(0);
        switch (leftSideVersion.compareTo(rightSideVersion)) {
        case -1:
            assertEquals(comparison + " was <", '<', operator);
            break;

        case 0:
            assertEquals(comparison + " was =", '=', operator);
            break;

        case 1:
            assertEquals(comparison + " was >", '>', operator);
            break;
        }
    }

    @Test
    public void parse() {
        assertEquals(V1, Version.parseVersionNumber("1"));
        assertEquals(V1_0, Version.parseVersionNumber("1.0"));
        assertEquals(V1_0_SNAPSHOT, Version.parseVersionNumber("1.0-SNAPSHOT"));

        assertEquals(V3_10_0, Version.parseVersionNumber("3.10.0"));
        assertEquals(V3_10_0, Version.parseVersionNumber("3.10"));
        assertEquals(V3_10_5, Version.parseVersionNumber("3.10.5"));
        assertEquals(V3_10_SNAPSHOT, Version.parseVersionNumber("3.10-SNAPSHOT"));
    }

    @Test
    public void equivalence() {
        Version v1 = Version.parseVersionNumber("3.10.6");
        Version v2 = Version.parseVersionNumber("3.10.6.0");
        Version v3 = Version.parseVersionNumber("3.10.6-0");
        Version v4 = Version.parseVersionNumber("3.10.6.0.0");
        Version v5 = Version.parseVersionNumber("3.10.6.0-0");
        Version v6 = Version.parseVersionNumber("3.10.6-FINAL");
        List<Version> equivaList = Arrays.asList(v1, v2, v3, v4, v5, v6);

        for (Version v : equivaList) {
            assertEquals("equals " + v1, v1, v);
            assertEquals("equals " + v2, v2, v);
            assertEquals("equals " + v3, v3, v);
            assertEquals("equals " + v4, v4, v);
            assertEquals("equals " + v5, v5, v);
            assertEquals("equals " + v6, v6, v);

            assertEquals("hashcode " + v.hashCode(), v1.hashCode(), v.hashCode());
            assertEquals("= " + v1, 0, v1.compareTo(v));
        }
    }
}
