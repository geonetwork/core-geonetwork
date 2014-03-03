package org.fao.geonet.domain;

import static org.junit.Assert.*;
import org.fao.geonet.repository.HarvestHistoryRepositoryTest;
import org.jdom.Element;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class HarvestHistoryTest{
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

        harvestHistory.setInfo((Element)null);
        harvestHistory.setParams((Element)null);
        final Element xml = harvestHistory.asXml();

        assertNull(xml.getChild("info"));
        assertNull(xml.getChild("params"));
    }
}
