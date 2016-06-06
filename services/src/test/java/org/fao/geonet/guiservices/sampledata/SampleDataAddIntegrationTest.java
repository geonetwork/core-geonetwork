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

package org.fao.geonet.guiservices.sampledata;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test add sample data service User: Jesse Date: 10/16/13 Time: 12:47 PM
 */
public class SampleDataAddIntegrationTest extends AbstractServiceIntegrationTest {
    @Test
    public void testExec() throws Exception {
        final Add add = new Add();
        ServiceContext context = createServiceContext();

        final Collection<String> schemas = Arrays.asList("iso19139", "dublin-core");
        StringBuilder builder = new StringBuilder();

        for (String schema : schemas) {
            if (builder.length() > 0) {
                builder.append(',');
            }

            builder.append(schema);
        }
        Element params = createParams(Pair.read(Params.SCHEMA, builder.toString()));
        loginAsAdmin(context);

        final Element response = add.exec(params, context);

        assertEquals("true", response.getAttributeValue("status"));
        assertEquals("", response.getAttributeValue("error"));
        assertTrue(Integer.parseInt(response.getAttributeValue("total")) > 0);

        for (String schema : schemas) {
            final String schemaCount = response.getChildText(schema);
            final String responseText = Xml.getString(response);
            assertNotNull("No element: schema count: " + responseText, schemaCount);
            assertTrue("expected schemaCount to be > 0: " + schema, Integer.parseInt(schemaCount) > 0);
        }
    }
}
