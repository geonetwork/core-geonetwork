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

package org.fao.geonet.domain;

import static org.junit.Assert.*;

import org.fao.geonet.repository.HarvestHistoryRepositoryTest;
import org.jdom.Element;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class HarvestHistoryTest {
    AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testAsXml() throws Exception {
        final HarvestHistory harvestHistory = HarvestHistoryRepositoryTest.createHarvestHistory(_inc);
        harvestHistory.setHarvestDate(new ISODate());
        harvestHistory.setInfo(new Element("rootInfo").addContent(new Element("child").setText("text")));
        harvestHistory.setParams(new Element("rootParam").addContent(new Element("child").setText("text")));
        final Element xml = harvestHistory.asXml();

        assertEquals(harvestHistory.getHarvestDate(), new ISODate(xml.getChildText("harvestdate")));
        final Element info = xml.getChild("info");
        assertTrue(info.getChildren().get(0) instanceof Element);
        final Element params = xml.getChild("params");
        assertTrue(params.getChildren().get(0) instanceof Element);
    }

    @Test
    public void testAsXmlWithNulls() throws Exception {
        final HarvestHistory harvestHistory = HarvestHistoryRepositoryTest.createHarvestHistory(_inc);
        harvestHistory.setHarvestDate(new ISODate());

        harvestHistory.setInfo((Element) null);
        harvestHistory.setParams((Element) null);
        final Element xml = harvestHistory.asXml();

        assertNull(xml.getChild("info"));
        assertNull(xml.getChild("params"));
    }
}
