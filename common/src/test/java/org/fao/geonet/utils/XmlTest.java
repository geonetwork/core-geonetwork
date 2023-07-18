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

package org.fao.geonet.utils;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.utils.debug.OpenResourceTracker;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;


/**
 * Test methods in the {@link org.fao.geonet.utils.Xml} utility class Created by Jesse on 2/6/14.
 */
public class XmlTest {
    public static final Namespace GMD = Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
    public static final Namespace GCO = Namespace.getNamespace("gco", "http://www.isotc211.org/2005/gco");
    private static final List<Namespace> NAMESPACES = Arrays.asList(GMD, GCO);
    public static Element TEST_METADATA;

    @BeforeClass
    public static void setUp() throws Exception {
        TEST_METADATA = Xml.loadFile(XmlTest.class.getResource("xmltest/sampleXml.xml"));
    }

    @Test
    public void testLoadXml() throws Exception {
        Path path = Paths.get(XmlTest.class.getResource("xmltest/xml.xsd").toURI());
        Element element = Xml.loadFile(path);
        assertXsdFile(element);

        final Path test = setupMemoryFs(path);

        element = Xml.loadFile(test.resolve(path.getFileName().toString()));
        assertXsdFile(element);
    }

    @Test
    public void testLoadString() throws Exception {
        Path path = Paths.get(XmlTest.class.getResource("xmltest/sampleXml.xml").toURI());
        String data = new String(Files.readAllBytes(path), Constants.CHARSET);
        Element element = Xml.loadString(data, false);
        assertSampleXml(element);
    }

    private void assertSampleXml(Element element) {
        assertNotNull(element.getChild("fileIdentifier", GMD));
        assertNotNull(element.getChild("language", GMD));
        assertNotNull(element.getChild("characterSet", GMD));
        assertNotNull(element.getChild("contact", GMD));
        assertNotNull(element.getChild("dateStamp", GMD));
    }

    @Test
    public void testLoadStream() throws Exception {
        final InputStream resourceAsStream = XmlTest.class.getResourceAsStream("xmltest/sampleXml.xml");
        Element element = Xml.loadStream(resourceAsStream);
        assertSampleXml(element);
    }

    protected void assertXsdFile(Element element) {
        assertNotNull(element);
        assertNotNull(element.getChild("annotation", Namespace.getNamespace("xs", "http://www.w3.org/2001/XMLSchema")));
    }

    protected Path setupMemoryFs(Path path) throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        final Path test = fs.getPath("test");
        IO.copyDirectoryOrFile(path.getParent(), test, false);
        return test;
    }

    @Test
    public void testTransformDefaultTransformer() throws Exception {
        TransformerFactoryFactory.init(null);
        doTestTransform();
    }

    protected void doTestTransform() throws Exception {
        final GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.refresh();
        applicationContext.getBeanFactory().registerSingleton("systemInfo", SystemInfo.createForTesting(SystemInfo.STAGE_DEVELOPMENT));
        ApplicationContextHolder.set(applicationContext);

        Path path = Paths.get(XmlTest.class.getResource("xmltest/xsl/test.xsl").toURI());
        Element result = Xml.transform(new Element("el"), path);
        assertTransformedXml(result);

        final Path test = setupMemoryFs(path.getParent());
        result = Xml.transform(new Element("el"), test.resolve("xsl/test.xsl"));
        assertTransformedXml(result);
        final StringWriter openResources = new StringWriter();
        PrintWriter writer = new PrintWriter(openResources);
        OpenResourceTracker.printExceptions(writer, 100);
        assertEquals(openResources.toString(), 0, OpenResourceTracker.numberOfOpenResources());
    }

    @Test
    public void testTransformSaxonTransformer() throws Exception {
        TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");
        try {
            doTestTransform();
        } finally {
            TransformerFactoryFactory.init(null);
        }
    }

    protected void assertTransformedXml(Element result) {
        assertEquals("root", result.getName());
        assertEquals(3, result.getChildren().size());
        assertEquals("dep1", result.getChildText("dep1"));
        assertEquals("dep2", result.getChildText("dep2"));
        assertEquals("dep3", result.getChildText("dep3"));
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

    @Test
    public void testIsXmlLike() {
        assertEquals(true,
            Xml.isXMLLike("<selfclosingtag attribute=\"\"/>"));
        assertEquals(true,
            Xml.isXMLLike("<tag attribute=\"\"></tag>"));
        assertEquals(true,
            Xml.isXMLLike("<?xml version='1.0' encoding='utf-8'?>\n<tag attribute=\"\"></tag>"));
        assertEquals(true,
            Xml.isRDFLike("<?xml version='1.0' encoding='utf-8'?>\n<rdf:RDF \n" +
                "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"/>"));
        assertEquals(true,
            Xml.isRDFLike("<?xml version='1.0' encoding='utf-8'?>\n<rdf:RDF\n" +
                "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"/>"));
    }
}
