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

import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import jeeves.server.context.ServiceContext;

import static org.junit.Assert.assertEquals;

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

        assertEqualsText("false", result, Settings.SYSTEM_CSW_METADATA_PUBLIC);
        assertEqualsText("-1", result, Settings.SYSTEM_CSW_CAPABILITY_RECORD_UUID);
        assertEquals(Settings.SYSTEM_CSW_CAPABILITY_RECORD_UUID, Xml.selectElement(result, Settings.SYSTEM_CSW_CAPABILITY_RECORD_UUID).getAttributeValue("name"));
        assertEquals("system/csw", Xml.selectElement(result, "system/csw").getAttributeValue("name"));
        assertEqualsText("true", result, Settings.SYSTEM_CSW_ENABLE);
    }
}
