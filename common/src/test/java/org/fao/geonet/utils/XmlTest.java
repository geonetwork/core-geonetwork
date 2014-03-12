package org.fao.geonet.utils;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Test methods in the {@link org.fao.geonet.utils.Xml} utility class
 * Created by Jesse on 2/6/14.
 */
public class XmlTest {
    public static final Namespace GMD = Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
    public static final Namespace GCO = Namespace.getNamespace("gco", "http://www.isotc211.org/2005/gco");
    private static final List<Namespace> NAMESPACES = Arrays.asList(GMD, GCO);
    public static Element TEST_METADATA;

    @BeforeClass
    public static void setUp() throws Exception {
        TEST_METADATA = Xml.loadFile(XmlTest.class.getClassLoader().getResource("sampleXml.xml"));
    }

    @Test
    public void testGetXPathExpr() throws Exception {
        final Element charString = TEST_METADATA.getChild("fileIdentifier", GMD).getChild("CharacterString", GCO);
        String xpath = Xml.getXPathExpr(charString);

        assertEquals("gmd:fileIdentifier/gco:CharacterString[normalize-space(text())='fileIdentifier']", xpath.replaceAll("\\s+", ""));
        assertSame(charString, Xml.selectElement(TEST_METADATA, xpath, NAMESPACES));
    }

    @Test
    public void testGetXPathExprString() throws Exception {
        final Text charString = (Text) TEST_METADATA.getChild("fileIdentifier", GMD).getChild("CharacterString", GCO).getContent().get(0);
        String xpath = Xml.getXPathExpr(charString);

        assertEquals("gmd:fileIdentifier/gco:CharacterString[normalize-space(text())='fileIdentifier']/text()", xpath.replaceAll("\\s+", ""));

        final List<?> actual = Xml.selectNodes(TEST_METADATA, xpath, NAMESPACES);
        assertSame(1, actual.size());
        assertSame(charString, actual.get(0));
    }
    @Test
    public void testGetXPathExprAttribute() throws Exception {
        final Attribute attribute = TEST_METADATA.getChild("characterSet", GMD)
                .getChild("MD_CharacterSetCode", GMD)
                .getAttribute("codeListValue");
        String xpath = Xml.getXPathExpr(attribute);

        final List<?> actual = Xml.selectNodes(TEST_METADATA, xpath, NAMESPACES);
        assertSame(1, actual.size());
        assertSame(attribute, actual.get(0));
    }


}
