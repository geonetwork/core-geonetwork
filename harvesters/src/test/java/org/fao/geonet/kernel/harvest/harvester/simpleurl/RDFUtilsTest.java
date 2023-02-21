package org.fao.geonet.kernel.harvest.harvester.simpleurl;

import org.jdom.Element;
import org.junit.Test;

import java.util.Map;

import static junit.framework.Assert.assertEquals;

public class RDFUtilsTest {
    @Test
    public void test_getAllUuidsFromFeed() throws Exception {
        Map<String, Element> records = RDFUtils.getAllUuids(this.getClass().getResource("dcat-feed-mow.rdf").toString());
        assertEquals(22, records.size());

        records = RDFUtils.getAllUuids(this.getClass().getResource("ogcapirecords-dcat-output.rdf").toString());
        assertEquals(1, records.size());
    }
}
