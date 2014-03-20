package org.fao.geonet.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test some of the logic methods in {@link org.fao.geonet.domain.Schematron}
 * Created by Jesse on 1/22/14.
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
