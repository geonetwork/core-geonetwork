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
