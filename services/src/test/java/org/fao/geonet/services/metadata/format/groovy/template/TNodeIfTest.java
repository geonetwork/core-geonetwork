package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TNodeIfTest {

    @Test
    public void testIsTruthy() throws Exception {
        assertTrue(TNodeFactoryIf.TNodeIf.isTruthy("1"));
        assertTrue(TNodeFactoryIf.TNodeIf.isTruthy("hi"));
        assertTrue(TNodeFactoryIf.TNodeIf.isTruthy(true));
        assertTrue(TNodeFactoryIf.TNodeIf.isTruthy(1.0));
        assertTrue(TNodeFactoryIf.TNodeIf.isTruthy(0.5f));
        assertTrue(TNodeFactoryIf.TNodeIf.isTruthy(0.5));
        assertTrue(TNodeFactoryIf.TNodeIf.isTruthy(1));
        assertTrue(TNodeFactoryIf.TNodeIf.isTruthy(new String[]{"1"}));
        assertTrue(TNodeFactoryIf.TNodeIf.isTruthy(Lists.newArrayList(2)));
        assertTrue(TNodeFactoryIf.TNodeIf.isTruthy(Lists.newArrayList(2)));
        assertTrue(TNodeFactoryIf.TNodeIf.isTruthy(Lists.newArrayList(2).iterator()));
        assertTrue(TNodeFactoryIf.TNodeIf.isTruthy(Collections.singletonMap("key", 2)));


        assertFalse(TNodeFactoryIf.TNodeIf.isTruthy(Collections.emptyMap()));
        assertFalse(TNodeFactoryIf.TNodeIf.isTruthy(Collections.emptyEnumeration()));
        assertFalse(TNodeFactoryIf.TNodeIf.isTruthy(Collections.emptyList()));
        assertFalse(TNodeFactoryIf.TNodeIf.isTruthy(Collections.emptyList().iterator()));
        assertFalse(TNodeFactoryIf.TNodeIf.isTruthy(0));
        assertFalse(TNodeFactoryIf.TNodeIf.isTruthy(0.000000000000000001));
        assertFalse(TNodeFactoryIf.TNodeIf.isTruthy(0.000000000000000001f));
        assertFalse(TNodeFactoryIf.TNodeIf.isTruthy(false));
        assertFalse(TNodeFactoryIf.TNodeIf.isTruthy(""));
        assertFalse(TNodeFactoryIf.TNodeIf.isTruthy(null));
    }

    @Test
    public void testNot() throws Exception {
        TNodeFactoryIf.TNodeIf not = new TNodeFactoryIf.TNodeIf("Node", TNode.EMPTY_ATTRIBUTES, "!expr");

        TRenderContext context = new TRenderContext(new ByteArrayOutputStream(), Collections.<String, Object>singletonMap("expr", true));
        assertFalse(not.canRender(context));

        context = new TRenderContext(new ByteArrayOutputStream(), Collections.<String, Object>singletonMap("expr", false));
        assertTrue(not.canRender(context));

        TNodeFactoryIf.TNodeIf normal = new TNodeFactoryIf.TNodeIf("Node", TNode.EMPTY_ATTRIBUTES, "expr");

        context = new TRenderContext(new ByteArrayOutputStream(), Collections.<String, Object>singletonMap("expr", false));
        assertFalse(normal.canRender(context));

        context = new TRenderContext(new ByteArrayOutputStream(), Collections.<String, Object>singletonMap("expr", true));
        assertTrue(normal.canRender(context));

    }
}