package org.fao.geonet.services.metadata.format.groovy.template;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FilterLowerCaseTest {

    @Test
    public void testProcess() throws Exception {
        assertEquals("word", new FilterLowerCase().process(null, "WOrD"));
    }
}