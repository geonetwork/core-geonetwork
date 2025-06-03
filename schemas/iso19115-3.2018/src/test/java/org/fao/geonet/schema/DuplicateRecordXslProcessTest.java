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

import org.fao.geonet.schema.iso19115_3_2018.ISO19115_3_2018SchemaPlugin;
import org.fao.geonet.schemas.XslProcessTest;
import org.fao.geonet.utils.Xml;
import static org.hamcrest.CoreMatchers.equalTo;
import org.jdom.Element;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class DuplicateRecordXslProcessTest extends XslProcessTest {

    public DuplicateRecordXslProcessTest() {
        super();
        this.setXslFilename("duplicate-metadata.xsl");
        this.setXmlFilename("metadata.xml");
        this.setNs(ISO19115_3_2018SchemaPlugin.allNamespaces);
    }

    private static final String XPATH_RECORD_DATE =
        "mdb:dateInfo";
    private static final String XPATH_RECORD_ONLINE_DOI =
        ".//mrd:onLine[*/cit:protocol/*/text() = 'DOI']";
    private static final String XPATH_RECORD_RESOURCE_IDENTIFIER =
        ".//mri:citation/*/cit:identifier";

    @Test
    public void testDuplicateRecord() throws Exception {
        Element inputElement = Xml.loadFile(xmlFile);
        assertThat(Xml.selectNodes(inputElement, XPATH_RECORD_DATE).size(), equalTo(2));
        assertThat(Xml.selectNodes(inputElement, XPATH_RECORD_ONLINE_DOI).size(), equalTo(1));
        assertThat(Xml.selectNodes(inputElement, XPATH_RECORD_RESOURCE_IDENTIFIER).size(), equalTo(2));

        Element resultElement = Xml.transform(inputElement, xslFile);
        assertThat(Xml.selectNodes(resultElement, XPATH_RECORD_DATE).size(), equalTo(0));
        assertThat(Xml.selectNodes(resultElement, XPATH_RECORD_ONLINE_DOI).size(), equalTo(0));
        assertThat(Xml.selectNodes(resultElement, XPATH_RECORD_RESOURCE_IDENTIFIER).size(), equalTo(1));

    }
}
