package org.fao.geonet.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PrefixUrlRewriteTest {

    @Test
    public void testRewrite() throws Exception {
        final String replacement = "replacement:";
        final PrefixUrlRewrite directive = new PrefixUrlRewrite("prefix", replacement);
        assertTrue(directive.appliesTo("prefix--hi"));
        assertFalse(directive.appliesTo("nn--hi"));

        assertEquals(replacement + "--hi", directive.rewrite("prefix--hi"));
    }


}