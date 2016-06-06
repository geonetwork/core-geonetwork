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

package org.fao.geonet.services.config;

import static org.junit.Assert.*;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

/**
 * Test the get config service User: Jesse Date: 11/6/13 Time: 12:15 PM
 */
public class GetTest extends AbstractServiceIntegrationTest {
    @Test
    public void testExec() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final Get getService = new Get();

        final Element result = getService.exec(createParams(), context);

        assertEqualsText("false", result, "system/csw/metadataPublic");
        assertEqualsText("", result, "system/csw/contactId");
        assertEquals("system/csw/contactId", Xml.selectElement(result, "system/csw/contactId").getAttributeValue("name"));
        assertEquals("system/csw", Xml.selectElement(result, "system/csw").getAttributeValue("name"));
        assertEqualsText("true", result, "system/csw/enable");
    }
}
