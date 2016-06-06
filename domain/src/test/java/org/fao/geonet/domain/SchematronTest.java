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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test some of the logic methods in {@link org.fao.geonet.domain.Schematron} Created by Jesse on
 * 1/22/14.
 */
public class SchematronTest {
    @Test
    public void testGetRuleName() throws Exception {
        final Schematron schematron = new Schematron();
        schematron.setFile("xyz/bcd/abc.xsl");

        assertEquals("abc", schematron.getRuleName());

        schematron.setFile(schematron.getFile().replace("/", "\\"));
        assertEquals("abc", schematron.getRuleName());

        schematron.setFile("abc.xsl");
        assertEquals("abc", schematron.getRuleName());
    }

    @Test
    public void testGetRuleName_Parse_Disabled() throws Exception {
        final Schematron schematron = new Schematron();
        schematron.setFile("xyz/bcd/abc.disabled.xsl");

        assertEquals("abc", schematron.getRuleName());

        schematron.setFile(schematron.getFile().replace("/", "\\"));
        assertEquals("abc", schematron.getRuleName());

        schematron.setFile("abc.DISABLED.xsl");
        assertEquals("abc", schematron.getRuleName());
    }

    @Test
    public void testGetRuleName_Parse_Report() throws Exception {
        final Schematron schematron = new Schematron();
        schematron.setFile("xyz/bcd/abc.report_only.xsl");

        assertEquals("abc", schematron.getRuleName());

        schematron.setFile(schematron.getFile().replace("/", "\\"));
        assertEquals("abc", schematron.getRuleName());

        schematron.setFile("abc.REPORT_ONLY.xsl");
        assertEquals("abc", schematron.getRuleName());
    }

    @Test
    public void testGetRuleName_Parse_Required() throws Exception {
        final Schematron schematron = new Schematron();
        schematron.setFile("xyz/bcd/abc.required.xsl");

        assertEquals("abc", schematron.getRuleName());

        schematron.setFile(schematron.getFile().replace("/", "\\"));
        assertEquals("abc", schematron.getRuleName());

        schematron.setFile("abc.REQUIRED.xsl");
        assertEquals("abc", schematron.getRuleName());
    }

    @Test
    public void testGetDefaultRequirement() throws Exception {
        final Schematron schematron = new Schematron();

        schematron.setFile("xyz/bcd/abc.report_only.xsl");
        assertEquals(SchematronRequirement.REPORT_ONLY, schematron.getDefaultRequirement());
        schematron.setFile("xyz/bcd/abc.REPORT_ONLY.xsl");
        assertEquals(SchematronRequirement.REPORT_ONLY, schematron.getDefaultRequirement());

        schematron.setFile("xyz/bcd/abc.xsl");
        assertEquals(SchematronRequirement.REQUIRED, schematron.getDefaultRequirement());
        schematron.setFile("xyz/bcd/abc.required.xsl");
        assertEquals(SchematronRequirement.REQUIRED, schematron.getDefaultRequirement());
        schematron.setFile("xyz/bcd/abc.REQUIRED.xsl");
        assertEquals(SchematronRequirement.REQUIRED, schematron.getDefaultRequirement());

        schematron.setFile("xyz/bcd/abc.disabled.xsl");
        assertEquals(SchematronRequirement.DISABLED, schematron.getDefaultRequirement());
        schematron.setFile("xyz/bcd/abc.DISABLED.xsl");
        assertEquals(SchematronRequirement.DISABLED, schematron.getDefaultRequirement());
    }
}
