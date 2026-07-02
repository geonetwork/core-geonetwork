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

import org.fao.geonet.schema.iso19115_3_2018.ISO19115_3_2018SchemaPlugin;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath;

public class PublicationDateAddProcessTest extends XslProcessTest {

    public PublicationDateAddProcessTest() {
        super();
        this.setXslFilename("process/publicationdate-add.xsl");
        this.setXmlFilename("metadata.xml");
        this.setNs(ISO19115_3_2018SchemaPlugin.allNamespaces);
    }

    @Test
    public void mustNotAlterARecordWhenNoParameterProvided() throws Exception {
        super.testMustNotAlterARecordWhenNoParameterProvided();
    }

    @Test
    public void testAddPublicationDate() throws Exception {
        Element inputElement = Xml.loadFile(xmlFile);
        String inputString = Xml.getString(inputElement);

        // Check no publication date exists
        assertThat(
            inputString, hasXPath("count(//cit:date[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication'])", equalTo("0")).withNamespaceContext(ns)
        );

        // Add publication date
        Map<String, Object> params = new HashMap<>();
        String newDate = "2026-07-02T09:23:00";
        params.put("publicationDate", newDate);

        Element resultElement = Xml.transform(inputElement, xslFile, params);
        String resultString = Xml.getString(resultElement);

        assertThat(
            resultString, hasXPath("count(//cit:date[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication'])", equalTo("1")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("//cit:date[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication']//gco:DateTime/text()", equalTo(newDate)).withNamespaceContext(ns)
        );
    }

    @Test
    public void testReplacePublicationDate() throws Exception {
        Element inputElement = Xml.loadFile(xmlFile);

        // 1. Add a publication date first
        Map<String, Object> params = new HashMap<>();
        params.put("publicationDate", "2020-01-01T00:00:00");
        Element midElement = Xml.transform(inputElement, xslFile, params);
        String midString = Xml.getString(midElement);

        assertThat(
            midString, hasXPath("count(//cit:date[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication'])", equalTo("1")).withNamespaceContext(ns)
        );

        // 2. Replace it with a new one
        String newDate = "2026-07-02T09:23:00";
        params.put("publicationDate", newDate);
        Element resultElement = Xml.transform(midElement, xslFile, params);
        String resultString = Xml.getString(resultElement);

        assertThat(
            resultString, hasXPath("count(//cit:date[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication'])", equalTo("1")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("//cit:date[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication']//gco:DateTime/text()", equalTo(newDate)).withNamespaceContext(ns)
        );
    }

    @Test
    public void testReplaceMultiplePublicationDates() throws Exception {
        // Create an input with 2 publication dates
        Element inputElement = Xml.loadFile(xmlFile);

        Map<String, Object> params = new HashMap<>();
        params.put("publicationDate", "2020-01-01T00:00:00");
        Element midElement = Xml.transform(inputElement, xslFile, params);

        // Add another one manually
        Element citation = Xml.selectElement(midElement, "mdb:identificationInfo/*/mri:citation/cit:CI_Citation", new ArrayList<>(ISO19115_3_2018SchemaPlugin.allNamespaces));
        Namespace citNs = citation.getNamespace();
        Element date2 = new Element("date", citNs);
        Element ciDate2 = new Element("CI_Date", citNs);
        Element dateVal2 = new Element("date", citNs);
        Element dateTime2 = new Element("DateTime", Namespace.getNamespace("gco", ns.get("gco")));
        dateTime2.setText("2021-01-01T00:00:00");
        dateVal2.addContent(dateTime2);
        ciDate2.addContent(dateVal2);
        Element dateType2 = new Element("dateType", citNs);
        Element dateTypeCode2 = new Element("CI_DateTypeCode", citNs);
        dateTypeCode2.setAttribute("codeListValue", "publication");
        dateTypeCode2.setAttribute("codeList", "someLocation");
        dateType2.addContent(dateTypeCode2);
        ciDate2.addContent(dateType2);
        date2.addContent(ciDate2);
        citation.addContent(date2);

        String midStringWithTwo = Xml.getString(midElement);
        assertThat(
            midStringWithTwo, hasXPath("count(//cit:date[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication'])", equalTo("2")).withNamespaceContext(ns)
        );

        // Now run the process
        String newDate = "2026-07-02T09:23:00";
        params.put("publicationDate", newDate);
        Element resultElement = Xml.transform(midElement, xslFile, params);
        String resultString = Xml.getString(resultElement);

        assertThat(
            resultString, hasXPath("count(//cit:date[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication'])", equalTo("1")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("//cit:date[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication']//gco:DateTime/text()", equalTo(newDate)).withNamespaceContext(ns)
        );
    }
}
