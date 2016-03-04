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

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class InspireAtomFeedTest {
    @Test
    public void testAddFeedEntries() {
        InspireAtomFeed feed = new InspireAtomFeed();

        InspireAtomFeedEntry feedEntry1 = new InspireAtomFeedEntry();
        feedEntry1.setType("type1");
        feedEntry1.setLang("eng");
        feedEntry1.setCrs("EPSG:4326");
        feedEntry1.setUrl("http://entry1");

        feed.addEntry(feedEntry1);

        assertEquals(1, feed.getEntryList().size());

        InspireAtomFeedEntry feedEntry2 = new InspireAtomFeedEntry();
        feedEntry2.setType("type2");
        feedEntry2.setLang("eng");
        feedEntry2.setCrs("EPSG:4326");
        feedEntry2.setUrl("http://entry2");

        feed.addEntry(feedEntry2);

        assertEquals(2, feed.getEntryList().size());
    }
}
