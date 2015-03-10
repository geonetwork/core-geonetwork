package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.collect.Maps;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import org.fao.geonet.services.metadata.format.groovy.util.NavBarItem;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TRenderContextTest {

    @Test
    public void testGetModelValue() throws Exception {
        final TestObj item = new TestObj();
        final Map<String, Object> model = Collections.<String, Object>singletonMap("item", item);
        final TRenderContext context = new TRenderContext(new ByteArrayOutputStream(), model);
        assertEquals("KEY", context.getModelValue("item.inner.name"));
        assertEquals("REL", context.getModelValue("item.inner.rel"));
        assertEquals(item.toString(), context.getModelValue("item").toString());
        assertEquals(item.getInner().toString(), context.getModelValue("item.inner").toString());
    }

    @Test
    public void testEmbeddedMap() throws Exception {
        String itemKey = "item";
        final String expr = itemKey + ".inner.name";
        final Map<String, Object> inner = Collections.<String, Object>singletonMap("name", "KEY");
        Map<String, Object> item = Collections.<String, Object>singletonMap("inner", inner);

        final Map<String, Object> model = Collections.<String, Object>singletonMap(itemKey, item);
        final TRenderContext context = new TRenderContext(new ByteArrayOutputStream(), model);

        assertEquals(item.toString(), context.getModelValue("item").toString());
        assertEquals(item.get("inner").toString(), context.getModelValue("item.inner").toString());

        context.getModelValue(expr);
        assertEquals("KEY", context.getModelValue("item.inner.name"));
    }

    @Test
    public void testEmbeddedList() throws Exception {
        String itemKey = "item";
        final String expr = itemKey + ".0.0";
        final List<Object> inner = Collections.<Object>singletonList("KEY");
        List<Object> item = Collections.<Object>singletonList(inner);

        final Map<String, Object> model = Collections.<String, Object>singletonMap(itemKey, item);
        final TRenderContext context = new TRenderContext(new ByteArrayOutputStream(), model);
        assertEquals(item.toString(), context.getModelValue("item").toString());
        assertEquals(item.get(0).toString(), context.getModelValue("item.0").toString());

        context.getModelValue(expr);
        assertEquals("KEY", context.getModelValue("item.0.0"));
    }

    @Test
    public void testGPath() throws Exception {
        String itemKey = "item";
        final XmlSlurper xmlSlurper = new XmlSlurper(false, false);
        final String xml =
                "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\"><gmd:fileIdentifier><gco:CharacterString "
                + "xmlns:gco=\"http://www.isotc211.org/2005/gco\">da165110-88fd-11da-a88f-000d939bc5d8</gco:CharacterString>"
                + "</gmd:fileIdentifier></gmd:MD_Metadata>";

        Map<String, String> nsMapping = Maps.newHashMap();
        nsMapping.put("gmd", "http://www.isotc211.org/2005/gmd");
        nsMapping.put("gco", "http://www.isotc211.org/2005/gco");

        final GPathResult gpath = xmlSlurper.parseText(xml).declareNamespace(nsMapping);

        final Map<String, Object> model = Collections.<String, Object>singletonMap(itemKey, gpath);
        final TRenderContext context = new TRenderContext(new ByteArrayOutputStream(), model);

        assertEquals("gmd:MD_Metadata", ((GPathResult) context.getModelValue("item")).name());
        assertEquals("gmd:fileIdentifier", ((GPathResult) context.getModelValue("item.gmd:fileIdentifier")).name());
        assertEquals("gmd:fileIdentifier", context.getModelValue("item.gmd:fileIdentifier.name()"));
        assertEquals("gco:CharacterString", ((GPathResult) context.getModelValue("item.gmd:fileIdentifier.gco:CharacterString")).name());
    }

    @Test
    public void testEmptyPathSection() throws Exception {
        String itemKey = "item";
        final String expr = itemKey + "." + ".name";
        try {
            final TestObj item = new TestObj();
            final Map<String, Object> model = Collections.<String, Object>singletonMap(itemKey, item);
            final TRenderContext context = new TRenderContext(new ByteArrayOutputStream(), model);
            context.getModelValue(expr);
            fail("Expected an exception");
        } catch (TemplateException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(expr));
            assertTrue(e.getMessage(), e.getMessage().contains("empty section"));
        }
    }

    @Test
    public void testNoProperty() throws Exception {
        String itemKey = "item";
        final String expr = itemKey + "." + "name";
        try {
            final TestObj item = new TestObj();
            final Map<String, Object> model = Collections.<String, Object>singletonMap(itemKey, item);
            final TRenderContext context = new TRenderContext(new ByteArrayOutputStream(), model);
            context.getModelValue(expr);
            fail("Expected an exception");
        } catch (TemplateException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(expr));
            assertTrue(e.getMessage(), e.getMessage().contains("does not exist"));
        }
    }

    @Test
    public void testNoItem() throws Exception {
        String itemKey = "item";
        final String expr = "wrongKey.inner.name";
        try {
            final TestObj item = new TestObj();
            final Map<String, Object> model = Collections.<String, Object>singletonMap(itemKey, item);
            final TRenderContext context = new TRenderContext(new ByteArrayOutputStream(), model);
            context.getModelValue(expr);
            fail("Expected an exception");
        } catch (TemplateException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(expr));
            assertTrue(e.getMessage(), e.getMessage().contains("There is no object in the model"));
        }
    }

    @Test
    public void testNullItem() throws Exception {
        String itemKey = "item";
        final String expr = itemKey + ".other.name";
        try {
            final TestObj item = new TestObj();
            final Map<String, Object> model = Collections.<String, Object>singletonMap(itemKey, item);
            final TRenderContext context = new TRenderContext(new ByteArrayOutputStream(), model);
            context.getModelValue(expr);
            fail("Expected an exception");
        } catch (TemplateException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(expr));
            assertTrue(e.getMessage(), e.getMessage().contains("value of the property"));
            assertTrue(e.getMessage(), e.getMessage().contains("is null"));
        }
    }

    private static class TestObj {
        final NavBarItem inner = new NavBarItem();
        final NavBarItem other = null;

        private TestObj() {
            inner.setName("KEY");
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