package org.fao.geonet.services.metadata.format.groovy.template;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FilterEscapeXmlAttrsTest {

    @Test
    public void testProcess() throws Exception {
        assertEquals("&amp;&lt;&gt;", new FilterEscapeXmlAttrs().process(null, "&<>"));
    }
}