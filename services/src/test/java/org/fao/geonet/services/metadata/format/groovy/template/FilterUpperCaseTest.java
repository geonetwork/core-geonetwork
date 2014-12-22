package org.fao.geonet.services.metadata.format.groovy.template;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FilterUpperCaseTest {

    @Test
    public void testProcess() throws Exception {
        assertEquals("WORD", new FilterUpperCase().process(null, "woRd"));
    }
}