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

package org.fao.geonet.schemas;

import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath;

public class OnlineSrcAddProcessTest extends XslProcessTest {

    public OnlineSrcAddProcessTest() {
        super();
        this.setXslFilename("process/onlinesrc-add.xsl");
        this.setXmlFilename("schemas/xsl/process/input.xml");
        this.setNs(ISO19139SchemaPlugin.allNamespaces);
    }

    @Test
    public void mustNotAlterARecordWhenNoParameterProvided() throws Exception {
        super.testMustNotAlterARecordWhenNoParameterProvided();
    }

    @Test
    public void testAddSimpleLinkAndUpdate() throws Exception {

        Element inputElement = Xml.loadFile(xmlFile);

        String resultString = Xml.getString(inputElement);

        assertThat(
            resultString, hasXPath("count(//gmd:onLine)", equalTo("3")).withNamespaceContext(ns)
        );

        // Add a simple link with name and description
        Map<String, Object> params = new HashMap<>(3);
        params.put("name", "Website name");
        params.put("desc", "Website description");
        params.put("url", "http://www.geonetwork-opensource.org");

        Element resultElement = Xml.transform(
            inputElement,
            xslFile,
            params);
        resultString = Xml.getString(resultElement);

        assertThat(
            resultString, hasXPath("count(//gmd:onLine)", equalTo("4")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("count(//gmd:onLine" +
                    "[*/gmd:name/*/text() = 'Website name'])",
                equalTo("1")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("count(//gmd:onLine" +
                    "[*/gmd:description/*/text() = 'Website description'])",
                equalTo("1")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("count(//gmd:onLine" +
                    "[*/gmd:linkage/*/text() = 'http://www.geonetwork-opensource.org'])",
                equalTo("1")).withNamespaceContext(ns)
        );


        // Add a simple link with name and description
        params.clear();
        params.put("updateKey", "http://www.geonetwork-opensource.orgWWW:LINK-1.0-http--linkWebsite name");
        params.put("name", "Website name updated");
        params.put("desc", "Website description updated");
        params.put("url", "http://www.geonetwork-opensource.org/contact");

        // Copy previous as control element to be updated
        Element controlElement = resultElement;
        String controlString = resultString;

        resultElement = Xml.transform(
            controlElement,
            xslFile,
            params);
        resultString = Xml.getString(resultElement);

        assertThat(
            controlString, hasXPath("count(//gmd:onLine)", equalTo("4")).withNamespaceContext(ns));
        assertThat(
            resultString, hasXPath("count(//gmd:onLine)", equalTo("4")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("count(//gmd:onLine" +
                    "[*/gmd:name/*/text() = 'Website name'])",
                equalTo("0")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("count(//gmd:onLine" +
                    "[*/gmd:name/*/text() = 'Website name updated'])",
                equalTo("1")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("count(//gmd:onLine" +
                    "[*/gmd:description/*/text() = 'Website description updated'])",
                equalTo("1")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("count(//gmd:onLine" +
                    "[*/gmd:linkage/*/text() = 'http://www.geonetwork-opensource.org/contact'])",
                equalTo("1")).withNamespaceContext(ns)
        );
    }


    @Test
    public void testAddMultilingualLinkAndUpdate() throws Exception {

        Element controlElement = Xml.loadFile(testClass.getClassLoader().getResource("schemas/xsl/process/onlinesrc-add-multilingual.xml"));
        String controlString = Xml.getString(controlElement);

        assertThat(
            controlString, hasXPath("count(//gmd:onLine)", equalTo("2")).withNamespaceContext(ns));

        // Add a simple link with name and description
        Map<String, Object> params = new HashMap<>(3);
        params.put("name", "fre#Le monde en français|EN#English website");
        params.put("desc", "fre#Description du site|EN#Website description");
        params.put("url", "http://www.geonetwork-opensource.org");

        Element resultElement = Xml.transform(
            controlElement,
            this.xslFile,
            params);
        String resultString = Xml.getString(resultElement);

        assertThat(
            resultString, hasXPath("count(//gmd:onLine)", equalTo("3")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("count(//gmd:onLine" +
                    "[*/gmd:name/gco:CharacterString/text() = 'Le monde en français'])",
                equalTo("1")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("count(//gmd:onLine" +
                    "[*/gmd:name/gmd:PT_FreeText/*/gmd:LocalisedCharacterString[@locale='#EN']/text() = 'English website'])",
                equalTo("1")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("count(//gmd:onLine" +
                    "[*/gmd:description/gco:CharacterString/text() = 'Description du site'])",
                equalTo("1")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("count(//gmd:onLine" +
                    "[*/gmd:description/gmd:PT_FreeText/*/gmd:LocalisedCharacterString[@locale='#EN']/text() = 'Website description'])",
                equalTo("1")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("count(//gmd:onLine" +
                    "[*/gmd:linkage/*/text() = 'http://www.geonetwork-opensource.org'])",
                equalTo("1")).withNamespaceContext(ns)
        );


        // Add a simple link with name and description
        params.clear();
        params.put("updateKey", "http://www.geonetwork-opensource.orgWWW:LINK-1.0-http--linkLe monde en français");
        params.put("name", "fre#Le monde en français 2|EN#English website 2");
        params.put("desc", "fre#Description du site 2|EN#Website description 2");
        params.put("url", "http://www.geonetwork-opensource.org2");

        // Copy previous as control element to be updated
        controlElement = resultElement;
        controlString = resultString;

        resultElement = Xml.transform(
            controlElement,
            xslFile,
            params);
        resultString = Xml.getString(resultElement);

        assertThat(
            resultString, hasXPath("count(//gmd:onLine" +
                    "[*/gmd:name/gco:CharacterString/text() = 'Le monde en français 2'])",
                equalTo("1")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("count(//gmd:onLine" +
                    "[*/gmd:name/gmd:PT_FreeText/*/gmd:LocalisedCharacterString[@locale='#EN']/text() = 'English website 2'])",
                equalTo("1")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("count(//gmd:onLine" +
                    "[*/gmd:description/gco:CharacterString/text() = 'Description du site 2'])",
                equalTo("1")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("count(//gmd:onLine" +
                    "[*/gmd:description/gmd:PT_FreeText/*/gmd:LocalisedCharacterString[@locale='#EN']/text() = 'Website description 2'])",
                equalTo("1")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("count(//gmd:onLine" +
                    "[*/gmd:linkage/*/text() = 'http://www.geonetwork-opensource.org2'])",
                equalTo("1")).withNamespaceContext(ns)
        );
    }
}
