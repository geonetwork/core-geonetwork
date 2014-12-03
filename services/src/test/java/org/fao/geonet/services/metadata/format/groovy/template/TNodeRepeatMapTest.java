package org.fao.geonet.services.metadata.format.groovy.template;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TNodeRepeatMapTest {

    @Test
    public void testEmptyRender() throws Exception {
        final TNodeRepeatMap repeat = new TNodeRepeatMap(null, TNode.EMPTY_ATTRIBUTES, "map", "key", "value");
        Map<String, Object> model = Collections.<String, Object>singletonMap("map", Collections.emptyMap());
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        repeat.render(new TRenderContext(outputStream, model));

        assertEquals("<!-- fmt-repeat: (key, value) in map is empty -->", outputStream.toString());
    }
}