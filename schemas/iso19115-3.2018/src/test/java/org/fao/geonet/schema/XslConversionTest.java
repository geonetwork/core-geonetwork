/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
package org.fao.geonet.schema;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.fao.geonet.schema.iso19115_3_2018.ISO19115_3_2018SchemaPlugin;
import org.fao.geonet.schemas.XslProcessTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

public class XslConversionTest extends XslProcessTest {

    public XslConversionTest() {
        super();
        this.setNs(ISO19115_3_2018SchemaPlugin.allNamespaces);
    }

    @Test
    public void testOdsConversion() throws Exception {
        xslFile = Paths.get(testClass.getClassLoader().getResource("convert/fromJsonOpenDataSoft.xsl").toURI());
        xmlFile = Paths.get(testClass.getClassLoader().getResource("ods.xml").toURI());
        Path jsonFile = Paths.get(testClass.getClassLoader().getResource("ods.json").toURI());
        String jsonString = Files.readString(jsonFile);
        Element xmlFromJSON = Xml.getXmlFromJSON(jsonString);
        xmlFromJSON.setName("record");
        xmlFromJSON.addContent(new Element("nodeUrl").setText("https://www.odwb.be"));

        Element inputElement = Xml.loadFile(xmlFile);
        String expectedXml = Xml.getString(inputElement);

        Element resultElement = Xml.transform(xmlFromJSON, xslFile);
        String resultOfConversion = Xml.getString(resultElement);

        Diff diff = DiffBuilder
            .compare(Input.fromString(resultOfConversion))
            .withTest(Input.fromString(expectedXml))
            .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
            .normalizeWhitespace()
            .ignoreComments()
            .checkForSimilar()
            .build();
        assertFalse(
            String.format("Differences: %s", diff.toString()),
            diff.hasDifferences());
    }


    @Test
    public void testZenodoConversion() throws Exception {
        xslFile = Paths.get(testClass.getClassLoader().getResource("convert/fromZenodo.xsl").toURI());
        xmlFile = Paths.get(testClass.getClassLoader().getResource("zenodo.xml").toURI());
        Path jsonFile = Paths.get(testClass.getClassLoader().getResource("zenodo.json").toURI());
        String jsonString = Files.readString(jsonFile);
        Element xmlFromJSON = Xml.getXmlFromJSON(jsonString);
        xmlFromJSON.setName("record");

        Element inputElement = Xml.loadFile(xmlFile);
        String expectedXml = Xml.getString(inputElement);

        Element resultElement = Xml.transform(xmlFromJSON, xslFile);
        String resultOfConversion = Xml.getString(resultElement);

        Diff diff = DiffBuilder
            .compare(Input.fromString(resultOfConversion))
            .withTest(Input.fromString(expectedXml))
            .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
            .normalizeWhitespace()
            .ignoreComments()
            .checkForSimilar()
            .build();
        assertFalse(
            String.format("Differences: %s", diff.toString()),
            diff.hasDifferences());
    }


    @Test
    public void testPangaeaConversion() throws Exception {
        xslFile = Paths.get(testClass.getClassLoader().getResource("convert/fromPangaea.xsl").toURI());
        xmlFile = Paths.get(testClass.getClassLoader().getResource("pangaea_output.xml").toURI());
        Path pangaeaInputFile = Paths.get(testClass.getClassLoader().getResource("pangaea_input.xml").toURI());
        String pangaeaInputDocument = Files.readString(pangaeaInputFile);
        Element pangaeaInput = Xml.loadString(pangaeaInputDocument, false);

        Element inputElement = Xml.loadFile(xmlFile);
        String expectedXml = Xml.getString(inputElement);

        Element resultElement = Xml.transform(pangaeaInput, xslFile);
        String resultOfConversion = Xml.getString(resultElement);

        Diff diff = DiffBuilder
            .compare(Input.fromString(resultOfConversion))
            .withTest(Input.fromString(expectedXml))
            .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
            .normalizeWhitespace()
            .ignoreComments()
            .checkForSimilar()
            .build();
        assertFalse(
            String.format("Differences: %s", diff.toString()),
            diff.hasDifferences());
    }

}
