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

package org.fao.geonet.api.records.formatters.cache;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.api.records.formatters.FormatType;
import org.fao.geonet.api.records.formatters.FormatterWidth;
import org.junit.After;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KeyTest {

    @After
    public void clearContext() {
        ApplicationContextHolder.clear();
    }

    private Key keyForNode(String nodeId) {
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setId(nodeId);
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        when(context.getBean(NodeInfo.class)).thenReturn(nodeInfo);
        ApplicationContextHolder.set(context);

        return new Key(1, "eng", FormatType.html, "full_view", true, FormatterWidth._100);
    }

    @Test
    public void keysOfDifferentPortalsAreNotEqual() {
        Key main = keyForNode(NodeInfo.DEFAULT_NODE);
        Key subportal = keyForNode("testportal");

        assertNotEquals(main, subportal);
        assertNotEquals(main.hashCode(), subportal.hashCode());
    }

    @Test
    public void keysOfTheSamePortalAreEqual() {
        Key first = keyForNode("testportal");
        Key second = keyForNode("testportal");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void keyWithoutApplicationContextUsesDefaultPortal() {
        ApplicationContextHolder.clear();

        Key key = new Key(1, "eng", FormatType.html, "full_view", true, FormatterWidth._100);

        assertEquals(NodeInfo.DEFAULT_NODE, key.nodeId);
    }
}
