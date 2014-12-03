package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.collect.Lists;
import org.fao.geonet.SystemInfo;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TNodeIfTest {
    SystemInfo info = SystemInfo.createForTesting(SystemInfo.STAGE_TESTING);
    @Test
    public void testCommentWhenFalse() throws Exception {
        TNodeFactoryIf.TNodeIf ifNode = new TNodeFactoryIf.TNodeIf(info, "div", TNode.EMPTY_ATTRIBUTES, "item");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ifNode.render(new TRenderContext(out, Collections.<String, Object>emptyMap()));
        assertEquals("<!-- fmt-if=item is null -->", out.toString());

        out = new ByteArrayOutputStream();
        ifNode.render(new TRenderContext(out, Collections.<String, Object>singletonMap("item", false)));
        assertEquals("<!-- fmt-if=item is false -->", out.toString());

        out = new ByteArrayOutputStream();
        ifNode.render(new TRenderContext(out, Collections.<String, Object>singletonMap("item", Collections.emptyList())));
        assertEquals("<!-- fmt-if=item is empty -->", out.toString());

        out = new ByteArrayOutputStream();
        ifNode.render(new TRenderContext(out, Collections.<String, Object>singletonMap("item", Collections.emptyMap())));
        assertEquals("<!-- fmt-if=item is empty -->", out.toString());

        out = new ByteArrayOutputStream();
        ifNode.render(new TRenderContext(out, Collections.<String, Object>singletonMap("item", 0)));
        assertEquals("<!-- fmt-if=item is 0 -->", out.toString());

        out = new ByteArrayOutputStream();
        ifNode.render(new TRenderContext(out, Collections.<String, Object>singletonMap("item", 0.0)));
        assertEquals("<!-- fmt-if=item is 0 -->", out.toString());

        out = new ByteArrayOutputStream();
        ifNode.render(new TRenderContext(out, Collections.<String, Object>singletonMap("item", "")));
        assertEquals("<!-- fmt-if=item is empty -->", out.toString());


        ifNode = new TNodeFactoryIf.TNodeIf(info, "div", TNode.EMPTY_ATTRIBUTES, "!item");

        out = new ByteArrayOutputStream();
        ifNode.render(new TRenderContext(out, Collections.<String, Object>singletonMap("item", "blarg")));
        assertEquals("<!-- fmt-if=!item is false (item is true) -->", out.toString());
    }

    @Test
    public void testIsTruthy() throws Exception {
        assertNull(TNodeFactoryIf.TNodeIf.isTruthy("1"));
        assertNull(TNodeFactoryIf.TNodeIf.isTruthy("hi"));
        assertNull(TNodeFactoryIf.TNodeIf.isTruthy(true));
        assertNull(TNodeFactoryIf.TNodeIf.isTruthy(1.0));
        assertNull(TNodeFactoryIf.TNodeIf.isTruthy(0.5f));
        assertNull(TNodeFactoryIf.TNodeIf.isTruthy(0.5));
        assertNull(TNodeFactoryIf.TNodeIf.isTruthy(1));
        assertNull(TNodeFactoryIf.TNodeIf.isTruthy(new String[]{"1"}));
        assertNull(TNodeFactoryIf.TNodeIf.isTruthy(Lists.newArrayList(2)));
        assertNull(TNodeFactoryIf.TNodeIf.isTruthy(Lists.newArrayList(2)));
        assertNull(TNodeFactoryIf.TNodeIf.isTruthy(Lists.newArrayList(2).iterator()));
        assertNull(TNodeFactoryIf.TNodeIf.isTruthy(Collections.singletonMap("key", 2)));


        assertNotNull(TNodeFactoryIf.TNodeIf.isTruthy(Collections.emptyMap()));
        assertNotNull(TNodeFactoryIf.TNodeIf.isTruthy(Collections.emptyEnumeration()));
        assertNotNull(TNodeFactoryIf.TNodeIf.isTruthy(Collections.emptyList()));
        assertNotNull(TNodeFactoryIf.TNodeIf.isTruthy(Collections.emptyList().iterator()));
        assertNotNull(TNodeFactoryIf.TNodeIf.isTruthy(0));
        assertNotNull(TNodeFactoryIf.TNodeIf.isTruthy(0.000000000000000001));
        assertNotNull(TNodeFactoryIf.TNodeIf.isTruthy(0.000000000000000001f));
        assertNotNull(TNodeFactoryIf.TNodeIf.isTruthy(false));
        assertNotNull(TNodeFactoryIf.TNodeIf.isTruthy(""));
        assertNotNull(TNodeFactoryIf.TNodeIf.isTruthy(null));
    }

    @Test
    public void testNot() throws Exception {
        TNodeFactoryIf.TNodeIf not = new TNodeFactoryIf.TNodeIf(info, "Node", TNode.EMPTY_ATTRIBUTES, "!expr");

        TRenderContext context = new TRenderContext(new ByteArrayOutputStream(), Collections.<String, Object>singletonMap("expr", true));
        assertTrue(not.canRender(context).isPresent());

        context = new TRenderContext(new ByteArrayOutputStream(), Collections.<String, Object>singletonMap("expr", false));
        assertFalse(not.canRender(context).isPresent());

        TNodeFactoryIf.TNodeIf normal = new TNodeFactoryIf.TNodeIf(info, "Node", TNode.EMPTY_ATTRIBUTES, "expr");

        context = new TRenderContext(new ByteArrayOutputStream(), Collections.<String, Object>singletonMap("expr", false));
        assertTrue(normal.canRender(context).isPresent());

        context = new TRenderContext(new ByteArrayOutputStream(), Collections.<String, Object>singletonMap("expr", true));
        assertFalse(normal.canRender(context).isPresent());

    }
}