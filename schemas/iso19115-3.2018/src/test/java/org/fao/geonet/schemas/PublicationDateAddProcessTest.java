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
            inputString, hasXPath("count(//mdb:dateInfo[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication'])", equalTo("0")).withNamespaceContext(ns)
        );

        // Add publication date
        Map<String, Object> params = new HashMap<>();
        String newDate = "2026-07-02";
        params.put("publicationDate", newDate);

        Element resultElement = Xml.transform(inputElement, xslFile, params);
        String resultString = Xml.getString(resultElement);

        assertThat(
            resultString, hasXPath("count(//mdb:dateInfo[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication'])", equalTo("1")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("//mdb:dateInfo[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication']//gco:Date/text()", equalTo(newDate)).withNamespaceContext(ns)
        );
    }

    @Test
    public void testReplacePublicationDate() throws Exception {
        Element inputElement = Xml.loadFile(xmlFile);

        // 1. Add a publication date first
        Map<String, Object> params = new HashMap<>();
        params.put("publicationDate", "2020-01-01");
        Element midElement = Xml.transform(inputElement, xslFile, params);
        String midString = Xml.getString(midElement);

        assertThat(
            midString, hasXPath("count(//mdb:dateInfo[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication'])", equalTo("1")).withNamespaceContext(ns)
        );

        // 2. Replace it with a new one
        String newDate = "2026-07-02";
        params.put("publicationDate", newDate);
        Element resultElement = Xml.transform(midElement, xslFile, params);
        String resultString = Xml.getString(resultElement);

        assertThat(
            resultString, hasXPath("count(//mdb:dateInfo[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication'])", equalTo("1")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("//mdb:dateInfo[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication']//gco:Date/text()", equalTo(newDate)).withNamespaceContext(ns)
        );
    }

    @Test
    public void testReplaceMultiplePublicationDates() throws Exception {
        // Create an input with 2 publication dates
        Element inputElement = Xml.loadFile(xmlFile);

        Map<String, Object> params = new HashMap<>();
        params.put("publicationDate", "2020-01-01");
        Element midElement = Xml.transform(inputElement, xslFile, params);

        // Add another publication date manually at metadata level (mdb:dateInfo).
        // The transformed root element is mdb:MD_Metadata.
        midElement.addContent(newDateInfo(midElement.getNamespace(), "publication", "2021-01-01"));

        String midStringWithTwo = Xml.getString(midElement);
        assertThat(
            midStringWithTwo, hasXPath("count(//mdb:dateInfo[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication'])", equalTo("2")).withNamespaceContext(ns)
        );

        // Now run the process
        String newDate = "2026-07-02T09:23:00";
        params.put("publicationDate", newDate);
        Element resultElement = Xml.transform(midElement, xslFile, params);
        String resultString = Xml.getString(resultElement);

        assertThat(
            resultString, hasXPath("count(//mdb:dateInfo[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication'])", equalTo("1")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath("//mdb:dateInfo[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication']//gco:Date/text()", equalTo(newDate)).withNamespaceContext(ns)
        );
    }

    /**
     * Regression test: a metadata date whose CI_DateTypeCode has no codeListValue attribute
     * must be preserved when a publication date is added. A naive {@code @codeListValue != 'publication'}
     * predicate silently drops such dates (an absent attribute is not "!= 'publication'" in XPath),
     * so this guards against that data loss.
     */
    @Test
    public void testPreservesDateWithoutCodeListValueWhenAddingPublicationDate() throws Exception {
        Element inputElement = Xml.loadFile(xmlFile);

        // Add a metadata date with a CI_DateTypeCode that has no codeListValue attribute.
        // The loaded root element is mdb:MD_Metadata.
        inputElement.addContent(newDateInfo(inputElement.getNamespace(), null, "2019-05-05"));

        String inputString = Xml.getString(inputElement);
        assertThat(
            inputString, hasXPath("count(//mdb:dateInfo[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode[not(@codeListValue)]])", equalTo("1")).withNamespaceContext(ns)
        );

        // Add the publication date
        Map<String, Object> params = new HashMap<>();
        params.put("publicationDate", "2026-07-02");
        Element resultElement = Xml.transform(inputElement, xslFile, params);
        String resultString = Xml.getString(resultElement);

        // The publication date is added ...
        assertThat(
            resultString, hasXPath("count(//mdb:dateInfo[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication'])", equalTo("1")).withNamespaceContext(ns)
        );
        // ... and the date without a codeListValue is preserved (not dropped).
        assertThat(
            resultString, hasXPath("count(//mdb:dateInfo[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode[not(@codeListValue)]])", equalTo("1")).withNamespaceContext(ns)
        );
    }

    /**
     * Build an {@code mdb:dateInfo} element with the given date type code list value
     * (omitted when {@code codeListValue} is null) and date time value.
     */
    private Element newDateInfo(Namespace mdbNs, String codeListValue, String dateTime) {
        Namespace citNs = Namespace.getNamespace("cit", ns.get("cit"));
        Namespace gcoNs = Namespace.getNamespace("gco", ns.get("gco"));

        Element dateInfo = new Element("dateInfo", mdbNs);
        Element ciDate = new Element("CI_Date", citNs);

        Element dateVal = new Element("date", citNs);
        Element dateTimeEl = new Element("Date", gcoNs);
        dateTimeEl.setText(dateTime);
        dateVal.addContent(dateTimeEl);
        ciDate.addContent(dateVal);

        Element dateType = new Element("dateType", citNs);
        Element dateTypeCode = new Element("CI_DateTypeCode", citNs);
        if (codeListValue != null) {
            dateTypeCode.setAttribute("codeListValue", codeListValue);
            dateTypeCode.setAttribute("codeList", "codeListLocation#CI_DateTypeCode");
        }
        dateType.addContent(dateTypeCode);
        ciDate.addContent(dateType);

        dateInfo.addContent(ciDate);
        return dateInfo;
    }
}
