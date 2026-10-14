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

import org.fao.geonet.schema.iso19115_3_2018.ISO19115_3_2018Namespaces;
import org.fao.geonet.schema.iso19115_3_2018.ISO19115_3_2018SchemaPlugin;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.xpath.XPath;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath;

public class DoiRemoveProcessTest extends XslProcessTest {

    public DoiRemoveProcessTest() {
        super();
        this.setXslFilename("process/doi-remove.xsl");
        this.setXmlFilename("input_with_doi.xml");
        this.setNs(ISO19115_3_2018SchemaPlugin.allNamespaces);
        this.getNs().put("gcx", ISO19115_3_2018Namespaces.GCX.getURI());
        this.getNs().put("xlink", ISO19115_3_2018Namespaces.XLINK.getURI());
    }

    @Test
    public void mustNotAlterARecordWhenNoParameterProvided() throws Exception {
        super.testMustNotAlterARecordWhenNoParameterProvided();
    }

    @Test
    public void testRemoveDoiAndContainerElement() throws Exception {
        Element inputElement = Xml.loadFile(xmlFile);
        String inputString = Xml.getString(inputElement);

        String doi = "https://doi.org/11.1111/da165110-88fd-11da-a88f-000d939bc5d8";

        // 1. Verify preconditions
        assertThat(inputString, hasXPath("count(//mrd:transferOptions)", equalTo("2")).withNamespaceContext(ns));
        assertThat(inputString, hasXPath("count(//mrd:onLine[*/cit:linkage/gco:CharacterString = '" + doi + "'])", equalTo("1")).withNamespaceContext(ns));
        assertThat(inputString, hasXPath("count(//cit:identifier[*/mcc:code/gcx:Anchor/@xlink:href = '" + doi + "'])", equalTo("1")).withNamespaceContext(ns));


        // 2. Run the process
        Map<String, Object> params = new HashMap<>();
        params.put("doi", doi);

        Element resultElement = Xml.transform(inputElement, xslFile, params);
        String resultString = Xml.getString(resultElement);

        // 3. Verify it is removed
        assertThat(resultString, hasXPath("count(//mrd:transferOptions)", equalTo("1")).withNamespaceContext(ns));
        assertThat(resultString, hasXPath("count(//mrd:onLine[*/cit:linkage/gco:CharacterString = '" + doi + "'])", equalTo("0")).withNamespaceContext(ns));
        assertThat(resultString, hasXPath("count(//cit:identifier[*/mcc:code/gcx:Anchor/@xlink:href = '" + doi + "'])", equalTo("0")).withNamespaceContext(ns));
    }

    @Test
    public void testRemoveDoi() throws Exception {
        Element inputElement = Xml.loadFile(xmlFile);

        String doi = "https://doi.org/11.1111/da165110-88fd-11da-a88f-000d939bc5d8";

        // 1. Add a new gmd:onLine in the gmd:transferOptions of the DOI resource
        XPath xpath = XPath.newInstance(".//mrd:transferOptions/mrd:MD_DigitalTransferOptions[mrd:onLine/*/cit:linkage/gco:CharacterString = '" + doi + "']");
        Element el = (Element) xpath.selectSingleNode(inputElement);
        Element newOnline = new  Element("onLine", "mrd", this.getNs().get("mrd") );
        el.addContent(newOnline);

        String inputString = Xml.getString(inputElement);

        // 1. Verify preconditions
        assertThat(inputString, hasXPath("count(//mrd:transferOptions)", equalTo("2")).withNamespaceContext(ns));
        assertThat(inputString, hasXPath("count(//mrd:onLine[*/cit:linkage/gco:CharacterString = '" + doi + "'])", equalTo("1")).withNamespaceContext(ns));
        assertThat(inputString, hasXPath("count(//cit:identifier[*/mcc:code/gcx:Anchor/@xlink:href = '" + doi + "'])", equalTo("1")).withNamespaceContext(ns));


        // 2. Run the process
        Map<String, Object> params = new HashMap<>();
        params.put("doi", doi);

        Element resultElement = Xml.transform(inputElement, xslFile, params);
        String resultString = Xml.getString(resultElement);

        // 3. Verify it is removed the DOI resource, but the gmd:transferOptions is not removed as has an additional gmd:onLine
        assertThat(resultString, hasXPath("count(//mrd:transferOptions)", equalTo("2")).withNamespaceContext(ns));
        assertThat(resultString, hasXPath("count(//mrd:onLine[*/cit:linkage/gco:CharacterString = '" + doi + "'])", equalTo("0")).withNamespaceContext(ns));
        assertThat(resultString, hasXPath("count(//cit:identifier[*/mcc:code/gcx:Anchor/@xlink:href = '" + doi + "'])", equalTo("0")).withNamespaceContext(ns));
    }

}
