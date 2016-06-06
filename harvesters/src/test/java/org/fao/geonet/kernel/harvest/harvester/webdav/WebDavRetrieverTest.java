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

package org.fao.geonet.kernel.harvest.harvester.webdav;

import com.github.sardine.DavResource;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * Some tests for {@link org.fao.geonet.kernel.harvest.harvester.webdav.WebDavRetriever} Created by
 * Jesse on 1/24/14.
 */
public class WebDavRetrieverTest {
    @Test
    public void testCalculateBaseURL() throws Exception {
        List<DavResource> resources = new ArrayList<DavResource>(2);
        final DavResource baseResource = Mockito.mock(DavResource.class);
        Mockito.when(baseResource.getPath()).thenReturn("/webdav/");

        final DavResource otherResource = Mockito.mock(DavResource.class);
        Mockito.when(otherResource.getPath()).thenReturn("/webdav/metadata.xml");

        resources.add(baseResource);
        resources.add(otherResource);

        final String baseURL = WebDavRetriever.calculateBaseURL(new AtomicBoolean(), "http://geonetwork.net/webdav/", resources);

        assertEquals("http://geonetwork.net", baseURL);
        assertEquals(1, resources.size());
        assertFalse(resources.contains(baseResource));
        assertTrue(resources.contains(otherResource));
    }
}
