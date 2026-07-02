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

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath;

public class PublicationDateRemoveProcessTest extends XslProcessTest {

    public PublicationDateRemoveProcessTest() {
        super();
        this.setXslFilename("process/publicationdate-remove.xsl");
        this.setXmlFilename("schemas/xsl/process/input.xml");
        this.setNs(ISO19139SchemaPlugin.allNamespaces);
    }

    @Test
    public void testRemovePublicationDate() throws Exception {
        Element inputElement = Xml.loadFile(xmlFile);

        // 1. Add a publication date first using the add process (we assume it works as it has its own tests)
        // Or just manually check it doesn't exist and add it if we want to be sure.
        // For this test, let's just use the add XSL to prepare the input.
        java.nio.file.Path addXsl = xslFile.getParent().resolve("publicationdate-add.xsl");
        Map<String, Object> params = new HashMap<>();
        params.put("publicationDate", "2026-07-02T10:00:00");
        Element midElement = Xml.transform(inputElement, addXsl, params);

        String midString = Xml.getString(midElement);
        assertThat(
            midString, hasXPath("count(//gmd:date[gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'publication'])", equalTo("1")).withNamespaceContext(ns)
        );

        // 2. Now remove it
        Element resultElement = Xml.transform(midElement, xslFile);
        String resultString = Xml.getString(resultElement);

        assertThat(
            resultString, hasXPath("count(//gmd:date[gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'publication'])", equalTo("0")).withNamespaceContext(ns)
        );
    }
}
