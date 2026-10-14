/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

import org.fao.geonet.schema.iso19115_3_2018.ISO19115_3_2018SchemaPlugin;
import org.fao.geonet.schemas.XslProcessTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertFalse;

@RunWith(Parameterized.class)
public class OdsConversionTest extends XslProcessTest {

    private String jsonFilename;

    public OdsConversionTest(String xmlFilename, String jsonFilename) {
        super();
        this.setNs(ISO19115_3_2018SchemaPlugin.allNamespaces);
        this.xmlFilename = xmlFilename;
        this.jsonFilename = jsonFilename;
    }

    @Parameterized.Parameters(name = "{index}: xml={0}, json={1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "ods.xml", "ods.json" },
            { "ods_v1.xml", "ods_v1.json" },
            { "ods_v2.0.xml", "ods_v2.0.json" },
            { "ods_v2.0.xml", "ods_v2.1.json" },
            { "ods_asset.xml", "ods_asset.json" }
        });
    }

    @Test
    public void testOdsConversion() throws Exception {
        xslFile = Paths.get(testClass.getClassLoader().getResource("convert/fromJsonOpenDataSoft.xsl").toURI());
        // xmlFile is initialized by parent setup() method because we set xmlFilename in constructor

        Path jsonFile = Paths.get(testClass.getClassLoader().getResource(jsonFilename).toURI());
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
}
