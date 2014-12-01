package org.fao.geonet.services.metadata.format.groovy.template;

import org.fao.geonet.services.metadata.format.groovy.util.NavBarItem;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TRenderContextTest {

    @Test
    public void testGetModelValue() throws Exception {
        final TestObj item = new TestObj();
        final Map<String, Object> model = Collections.<String, Object>singletonMap("item", item);
        final TRenderContext context = new TRenderContext(new ByteArrayOutputStream(), model);
        assertEquals("KEY", context.getModelValue("item.inner.nameKey"));
        assertEquals("REL", context.getModelValue("item.inner.rel"));
        assertEquals(item.toString(), context.getModelValue("item").toString());
        assertEquals(item.getInner().toString(), context.getModelValue("item.inner").toString());
    }

    private static class TestObj {
        final NavBarItem inner = new NavBarItem();
        private TestObj() {
            inner.setNameKey("KEY");
            inner.setRel("REL");
        }

        public NavBarItem getInner() {
            return inner;
        }

        @Override
        public String toString() {
            return "TestObj";
        }
    }
}