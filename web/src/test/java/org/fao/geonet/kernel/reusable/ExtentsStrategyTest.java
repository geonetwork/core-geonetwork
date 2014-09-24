package org.fao.geonet.kernel.reusable;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExtentsStrategyTest {

    @Test
    public void testUpdateHrefId() throws Exception {
        final ExtentsStrategy strategy = new ExtentsStrategy("/", "/", null, "eng");
        assertEquals("local://xml.extent.get?id=3&wfs=default&typename=gn:xlinks&format=GMD_COMPLETE&extentTypeCode=true",
                strategy.updateHrefId("local://xml.extent.get?id=5&wfs=default&typename=gn:non_validated&" +
                                      "format=GMD_COMPLETE&extentTypeCode=true", "3", null));
        assertEquals("local://xml.extent.get?wfs=default&id=3&typename=gn:xlinks&format=GMD_COMPLETE&extentTypeCode=true",
                strategy.updateHrefId("local://xml.extent.get?wfs=default&id=5&typename=gn:non_validated&" +
                                      "format=GMD_COMPLETE&extentTypeCode=true", "3", null));
    }
}