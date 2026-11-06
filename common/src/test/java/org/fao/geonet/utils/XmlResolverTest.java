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


package org.fao.geonet.utils;

import org.jdom.Element;
import org.junit.Test;
import org.w3c.dom.ls.LSInput;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class XmlResolverTest {

    @Test
    public void testHttpsSchemaIsResolvedFromCache() throws Exception {
        // Unreachable host: the schema can only come from the cache
        String url = "https://schemas.example.invalid/record.xsd";
        XmlResolver resolver = new XmlResolver(new String[0], new ProxyParams());
        resolver.addXmlToCache(url, new Element("schema", "http://www.w3.org/2001/XMLSchema"));

        LSInput input = resolver.resolveResource("http://www.w3.org/2001/XMLSchema", "urn:record", null, url, null);

        assertNotNull(input);
        assertTrue(input.getStringData().contains("XMLSchema"));
    }
}
