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
