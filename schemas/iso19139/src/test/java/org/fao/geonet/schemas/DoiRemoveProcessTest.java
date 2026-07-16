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

import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
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
        this.setXmlFilename("schemas/xsl/process/input_with_doi.xml");
        this.setNs(ISO19139SchemaPlugin.allNamespaces);
        this.getNs().put("gmx", ISO19139Namespaces.GMX.getURI());
        this.getNs().put("xlink", ISO19139Namespaces.XLINK.getURI());
    }

    @Test
    public void mustNotAlterARecordWhenNoParameterProvided() throws Exception {
        super.testMustNotAlterARecordWhenNoParameterProvided();
    }

    @Test
    public void testRemoveDoi() throws Exception {
        Element inputElement = Xml.loadFile(xmlFile);
        String inputString = Xml.getString(inputElement);

        String doi = "https://doi.org/11.1111/da165110-88fd-11da-a88f-000d939bc5d8";

        assertThat(inputString, hasXPath("count(//gmd:onLine[*/gmd:linkage/gmd:URL = '" + doi + "'])", equalTo("1")).withNamespaceContext(ns));
        assertThat(inputString, hasXPath("count(//gmd:identifier[*/gmd:code/gmx:Anchor/@xlink:href = '" + doi + "'])", equalTo("1")).withNamespaceContext(ns));


        // 2. Run the process
        Map<String, Object> params = new HashMap<>();
        params.put("doi", doi);

        Element resultElement = Xml.transform(inputElement, xslFile, params);
        String resultString = Xml.getString(resultElement);

        // 3. Verify it is removed

        assertThat(resultString, hasXPath("count(//gmd:onLine[*/gmd:linkage/gmd:URL = '" + doi + "'])", equalTo("0")).withNamespaceContext(ns));
        assertThat(resultString, hasXPath("count(//gmd:identifier[*/gmd:code/gmx:Anchor/@xlink:href = '" + doi + "'])", equalTo("0")).withNamespaceContext(ns));
    }
}
