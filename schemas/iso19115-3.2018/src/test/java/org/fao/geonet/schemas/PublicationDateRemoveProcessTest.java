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

public class PublicationDateRemoveProcessTest extends XslProcessTest {

    public PublicationDateRemoveProcessTest() {
        super();
        this.setXslFilename("process/publicationdate-remove.xsl");
        this.setXmlFilename("metadata.xml");
        this.setNs(ISO19115_3_2018SchemaPlugin.allNamespaces);
    }

    @Test
    public void testRemovePublicationDate() throws Exception {
        Element inputElement = Xml.loadFile(xmlFile);

        Namespace cit = Namespace.getNamespace("cit", "http://standards.iso.org/iso/19115/-3/cit/2.0");
        Namespace gco = Namespace.getNamespace("gco", "http://standards.iso.org/iso/19115/-3/gco/1.0");

        // Add a metadata-level publication date (mdb:dateInfo). The loaded root is mdb:MD_Metadata.
        Element dateInfo = new Element("dateInfo", inputElement.getNamespace());
        Element ciDate = new Element("CI_Date", cit);
        Element dateEl = new Element("date", cit);
        Element date = new Element("Date", gco).setText("2026-07-02");
        Element dateType = new Element("dateType", cit);
        Element ciDateTypeCode = new Element("CI_DateTypeCode", cit)
                .setAttribute("codeList", "codeListLocation#CI_DateTypeCode")
                .setAttribute("codeListValue", "publication");

        dateEl.addContent(date);
        dateType.addContent(ciDateTypeCode);
        ciDate.addContent(dateEl);
        ciDate.addContent(dateType);
        dateInfo.addContent(ciDate);
        inputElement.addContent(dateInfo);

        String midString = Xml.getString(inputElement);

        Map<String, String> xslNs = new HashMap<>();
        xslNs.put("mdb", "http://standards.iso.org/iso/19115/-3/mdb/2.0");
        xslNs.put("cit", "http://standards.iso.org/iso/19115/-3/cit/2.0");

        assertThat(
            midString, hasXPath("count(//mdb:dateInfo[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication'])", equalTo("1")).withNamespaceContext(xslNs)
        );

        // 2. Now remove it
        Element resultElement = Xml.transform(inputElement, xslFile);
        String resultString = Xml.getString(resultElement);

        assertThat(
            resultString, hasXPath("count(//mdb:dateInfo[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication'])", equalTo("0")).withNamespaceContext(xslNs)
        );
    }
}
