package org.fao.geonet.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/** Double check the functionality of Version utility class */
public class VersionTest {

    final Version V1_SNAPSHOT = new Version("1", null, null, null, "SNAPSHOT");
    final Version V1_1_1_SNAPSHOT = new Version("1","1","1",null,"SNAPSHOT");
    final Version V1_1_1 = new Version("1","1","1");
    final Version V1_1 = new Version("1","1",null);
    final Version V1_1_0 = new Version("1","1","0");
    final Version V1 = new Version("1",null,null);
    final Version V1_0_SNAPSHOT = new Version("1", "0", null, null, "SNAPSHOT");
    final Version V1_0 = new Version("1","0",null);
    final Version V1_0_0 = new Version("1","0","0");


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

    public void string(){
        assertEquals("1", V1.toString());
        assertEquals("1.0", V1_0.toString());
        assertEquals("1.0.0", V1_0_0.toString());
        assertEquals("1.1.0", V1_1_0.toString());

        assertEquals("1-SNAPSHOT", V1_SNAPSHOT.toString());
        assertEquals("1.0-SNAPSHOT", V1_0_SNAPSHOT.toString());

    }

    @Test
    public void compare() throws Exception {
        assertEquals( "1 = 1", 0, V1.compareTo(V1));
        assertEquals( "1 = 1.0", 0, V1.compareTo(V1_0));
        assertEquals( "1 = 1.0.0", 0, V1.compareTo(V1_0_0));
        assertEquals( "1.1 > 1.0", 1, V1_1.compareTo(V1_0));

        assertEquals( "1-SNAPSHOT > 1", 1, V1_SNAPSHOT.compareTo(V1));
        assertEquals( "1-SNAPSHOT > 1.0", 1, V1_SNAPSHOT.compareTo(V1_0));
        assertEquals( "1-SNAPSHOT > 1.0.0", 1, V1_SNAPSHOT.compareTo(V1_0_0));
    }

    @Test
    public void parse(){
        assertEquals(V1,Version.parseVersionNumber("1"));
        assertEquals(V1_0,Version.parseVersionNumber("1.0"));
        assertEquals(V1_0_SNAPSHOT,Version.parseVersionNumber("1.0-SNAPSHOT"));
    }
}
