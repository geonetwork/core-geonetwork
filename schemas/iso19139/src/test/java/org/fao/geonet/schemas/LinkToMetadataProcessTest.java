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

package org.fao.geonet.schemas;

import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath;

/**
 * Check that source-add and fcats-add link one record using the single record
 * parameters and several records using the relatedRecords parameter.
 */
public class LinkToMetadataProcessTest extends XslProcessTest {

    public LinkToMetadataProcessTest() {
        super();
        this.setXslFilename("process/source-add.xsl");
        this.setXmlFilename("schemas/xsl/process/input.xml");
        this.setNs(ISO19139SchemaPlugin.allNamespaces);
        this.getNs().put("xlink", "http://www.w3.org/1999/xlink");
    }

    private Path xsl(String filename) throws Exception {
        return Paths.get(testClass.getClassLoader().getResource(filename).toURI());
    }

    @Test
    public void testAddOneSource() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("sourceUuid", "uuid-a");
        params.put("sourceTitle", "Record A");

        String result = Xml.getString(Xml.transform(Xml.loadFile(xmlFile), xslFile, params));

        assertThat(result, hasXPath("count(//gmd:source)", equalTo("1")).withNamespaceContext(ns));
        assertThat(result, hasXPath("count(//gmd:source[@uuidref = 'uuid-a'][@xlink:title = 'Record A'])",
            equalTo("1")).withNamespaceContext(ns));
    }

    @Test
    public void testAddSeveralSources() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("relatedRecords", "uuid-a#Record A#,uuid-b#Record B#http://remote/b");

        String result = Xml.getString(Xml.transform(Xml.loadFile(xmlFile), xslFile, params));

        assertThat(result, hasXPath("count(//gmd:source)", equalTo("2")).withNamespaceContext(ns));
        assertThat(result, hasXPath("count(//gmd:source[@uuidref = 'uuid-a'][@xlink:title = 'Record A'])",
            equalTo("1")).withNamespaceContext(ns));
        assertThat(result, hasXPath("count(//gmd:source[@uuidref = 'uuid-b'][@xlink:href = 'http://remote/b'])",
            equalTo("1")).withNamespaceContext(ns));
    }

    @Test
    public void testAddOneFeatureCatalogue() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("uuidref", "uuid-a");
        params.put("fcatsTitle", "Catalogue A");

        String result = Xml.getString(
            Xml.transform(Xml.loadFile(xmlFile), xsl("process/fcats-add.xsl"), params));

        assertThat(result, hasXPath("count(//gmd:featureCatalogueCitation)",
            equalTo("1")).withNamespaceContext(ns));
        assertThat(result, hasXPath(
            "count(//gmd:featureCatalogueCitation[@uuidref = 'uuid-a'][@xlink:title = 'Catalogue A'])",
            equalTo("1")).withNamespaceContext(ns));
    }

    @Test
    public void testAddSeveralFeatureCatalogues() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("relatedRecords", "uuid-a#Catalogue A#,uuid-b#Catalogue B#http://remote/b");

        String result = Xml.getString(
            Xml.transform(Xml.loadFile(xmlFile), xsl("process/fcats-add.xsl"), params));

        assertThat(result, hasXPath("count(//gmd:featureCatalogueCitation)",
            equalTo("2")).withNamespaceContext(ns));
        assertThat(result, hasXPath(
            "count(//gmd:featureCatalogueCitation[@uuidref = 'uuid-a'][@xlink:title = 'Catalogue A'])",
            equalTo("1")).withNamespaceContext(ns));
        assertThat(result, hasXPath(
            "count(//gmd:featureCatalogueCitation[@uuidref = 'uuid-b'][@xlink:href = 'http://remote/b'])",
            equalTo("1")).withNamespaceContext(ns));
    }

    /**
     * A title or URL containing a separator is escaped by the client and
     * restored by the process, including values which already contained a
     * percent escape.
     */
    @Test
    public void testSeparatorsInTitleAndUrlAreRestored() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("relatedRecords", "uuid-a#Roads%2C bridges %232 & 100%25 fun"
            + "#http://remote/a?x=1%2C2%23frag,uuid-b#A%252CB%2523C#");

        String sources = Xml.getString(Xml.transform(Xml.loadFile(xmlFile), xslFile, params));

        assertThat(sources, hasXPath(
            "count(//gmd:source[@uuidref = 'uuid-a']"
                + "[@xlink:title = 'Roads, bridges #2 & 100% fun']"
                + "[@xlink:href = 'http://remote/a?x=1,2#frag'])",
            equalTo("1")).withNamespaceContext(ns));
        assertThat(sources, hasXPath(
            "count(//gmd:source[@uuidref = 'uuid-b'][@xlink:title = 'A%2CB%23C'])",
            equalTo("1")).withNamespaceContext(ns));

        String fcats = Xml.getString(
            Xml.transform(Xml.loadFile(xmlFile), xsl("process/fcats-add.xsl"), params));

        assertThat(fcats, hasXPath(
            "count(//gmd:featureCatalogueCitation[@uuidref = 'uuid-a']"
                + "[@xlink:title = 'Roads, bridges #2 & 100% fun']"
                + "[@xlink:href = 'http://remote/a?x=1,2#frag'])",
            equalTo("1")).withNamespaceContext(ns));
        assertThat(fcats, hasXPath(
            "count(//gmd:featureCatalogueCitation[@uuidref = 'uuid-b']"
                + "[@xlink:title = 'A%2CB%23C'])",
            equalTo("1")).withNamespaceContext(ns));
    }
}
