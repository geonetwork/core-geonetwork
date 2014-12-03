package org.fao.geonet.services.metadata.format.groovy.template;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TNodeRepeatIterTest {

    @Test
    public void testEmptyRender() throws Exception {
        final TNodeRepeatIter repeat = new TNodeRepeatIter(null, TNode.EMPTY_ATTRIBUTES, "iter", "key");
        Map<String, Object> model = Collections.<String, Object>singletonMap("iter", Collections.emptyList());
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        repeat.render(new TRenderContext(outputStream, model));

        assertEquals("<!-- fmt-repeat: key in iter is empty -->", outputStream.toString());
    }
}