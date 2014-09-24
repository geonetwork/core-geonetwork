package org.fao.geonet.kernel.search.facet;

import static org.junit.Assert.*;

import org.junit.Test;

public class FormatTest {

    @Test
    public void testTypeTags() {
        Formatter formatter = Format.DIMENSION.getFormatter(null);
        assertTrue(formatter instanceof DimensionFormatter);
    }

    @Test
    public void testValueTags() {
        Formatter formatter = Format.FACET_NAME.getFormatter(null);
        assertTrue(formatter instanceof FacetNameFormatter);
    }
}
