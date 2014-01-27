package org.fao.geonet.domain;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Test harvesterData class.
 *
 * Created by Jesse on 1/23/14.
 */
public class HarvesterDataTest {

    @Test
    public void testGetValue() throws Exception {
        final HarvesterData data = new HarvesterData();
        data.setValue(true);
        assertTrue(data.getValueAsBoolean());
        data.setValue(false);
        assertFalse(data.getValueAsBoolean());
    }

    @Test
    public void testGetValueAsInt() throws Exception {
        final HarvesterData data = new HarvesterData();
        data.setValue(1);
        assertEquals(1, data.getValueAsInt());
        assertEquals(1, data.getValueAsLong());
        data.setValue(100);
        assertEquals(100, data.getValueAsInt());
        assertEquals(100, data.getValueAsLong());
    }


    @Test
    public void testGetValueAsBoolean() throws Exception {
        final HarvesterData data = new HarvesterData();

        final ISODate value = new ISODate();
        data.setValue(value);
        assertEquals(value, data.getValueAsDate());
    }

    @Test
    public void testGetValueAsDate() throws Exception {

    }
}
