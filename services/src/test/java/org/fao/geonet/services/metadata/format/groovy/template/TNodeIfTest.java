package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.utils.IO;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TNodeIfTest extends AbstractTemplateParserTest {
    SystemInfo info = SystemInfo.createForTesting(SystemInfo.STAGE_TESTING);

    @Test
    public void testParseIfDirective() throws Exception {
        final TemplateParser parser = createTestParser(SystemInfo.STAGE_PRODUCTION);
        final URL url = AbstractTemplateParserTest.class.getResource("non-empty-template.html");
        final TNode parseTree = parser.parse(IO.toPath(url.toURI()));

        Map<String, Object> model = Maps.newHashMap();
        model.put("title", "Title");
        model.put("lang", "en");
        model.put("body", "y");
        model.put("div", "Div Data");

        String expected = "<html><head lang=\"en\"><title>Title</title></head>"
                          + "<body><div>Div Data</div></body></html>";
        assertCorrectRender(parseTree, model, expected);

        model.remove("title");
        expected = "<html><head lang=\"en\"></head><body><div>Div Data</div></body></html>";
        assertCorrectRender(parseTree, model, expected);

        model.remove("body");
        expected = "<html><head lang=\"en\"></head></html>";
        assertCorrectRender(parseTree, model, expected);

        model.put("title", "Title");
        expected = "<html><head lang=\"en\"><title>Title</title></head></html>";
        assertCorrectRender(parseTree, model, expected);

        model.put("body", "y");
        model.remove("div");
        expected = "<html><head lang=\"en\"><title>Title</title></head><body></body></html>";
        assertCorrectRender(parseTree, model, expected);

        model.clear();
        final ArrayList<Object> list = Lists.<Object>newArrayList(1, "2");
        model.put("title", list);

        expected = "<html><head lang=\"lang\"><title>" + list + "</title></head></html>";
        assertCorrectRender(parseTree, model, expected);

        final GPathResult gPathResult = new XmlSlurper().parseText("<xml>gpath</xml>");
        model.put("title", gPathResult);
        expected = "<html><head lang=\"lang\"><title>" + gPathResult + "</title></head></html>";
        assertCorrectRender(parseTree, model, expected);
    }

    @Test
    public void testCommentWhenFalse() throws Exception {
        TNodeIf ifNode = new TNodeIf(info, TextContentParserTest.createTestTextContentParser(), "div", TNode.EMPTY_ATTRIBUTES, "item", false);
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


        ifNode = new TNodeIf(info, TextContentParserTest.createTestTextContentParser(), "div", TNode.EMPTY_ATTRIBUTES, "!item", false);

        out = new ByteArrayOutputStream();
        ifNode.render(new TRenderContext(out, Collections.<String, Object>singletonMap("item", "blarg")));
        assertEquals("<!-- fmt-if=!item is false (item is true) -->", out.toString());
    }

    @Test
    public void testEquals() throws Exception {
        TNodeIf div = new TNodeIf(null, TextContentParserTest.createTestTextContentParser(), "div", TNode.EMPTY_ATTRIBUTES, "{{label}} == 'one'", false);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertFalse(div.canRender(new TRenderContext(out, Collections.<String, Object>singletonMap("label", "one"))).isPresent());
        assertTrue(div.canRender(new TRenderContext(out, Collections.<String, Object>singletonMap("label", "two"))).isPresent());
        assertTrue(div.canRender(new TRenderContext(out, Collections.<String, Object>emptyMap())).isPresent());

       div = new TNodeIf(null, TextContentParserTest.createTestTextContentParser(), "div", TNode.EMPTY_ATTRIBUTES, "'one' == {{label}}", false);

        assertFalse(div.canRender(new TRenderContext(out, Collections.<String, Object>singletonMap("label", "one"))).isPresent());
        assertTrue(div.canRender(new TRenderContext(out, Collections.<String, Object>singletonMap("label", "two"))).isPresent());
        assertTrue(div.canRender(new TRenderContext(out, Collections.<String, Object>emptyMap())).isPresent());


    }

    @Test
    public void testIsTruthy() throws Exception {
        assertNull(TNodeIf.isTruthy("1"));
        assertNull(TNodeIf.isTruthy("hi"));
        assertNull(TNodeIf.isTruthy(true));
        assertNull(TNodeIf.isTruthy(1.0));
        assertNull(TNodeIf.isTruthy(0.5f));
        assertNull(TNodeIf.isTruthy(0.5));
        assertNull(TNodeIf.isTruthy(1));
        assertNull(TNodeIf.isTruthy(new String[]{"1"}));
        assertNull(TNodeIf.isTruthy(Lists.newArrayList(2)));
        assertNull(TNodeIf.isTruthy(Lists.newArrayList(2)));
        assertNull(TNodeIf.isTruthy(Lists.newArrayList(2).iterator()));
        assertNull(TNodeIf.isTruthy(Collections.singletonMap("key", 2)));


        assertNotNull(TNodeIf.isTruthy(Collections.emptyMap()));
        assertNotNull(TNodeIf.isTruthy(Collections.emptyEnumeration()));
        assertNotNull(TNodeIf.isTruthy(Collections.emptyList()));
        assertNotNull(TNodeIf.isTruthy(Collections.emptyList().iterator()));
        assertNotNull(TNodeIf.isTruthy(0));
        assertNotNull(TNodeIf.isTruthy(0.000000000000000001));
        assertNotNull(TNodeIf.isTruthy(0.000000000000000001f));
        assertNotNull(TNodeIf.isTruthy(false));
        assertNotNull(TNodeIf.isTruthy(""));
        assertNotNull(TNodeIf.isTruthy(null));
    }

    @Test
    public void testNot() throws Exception {
        TNodeIf not = new TNodeIf(info, TextContentParserTest.createTestTextContentParser(), "Node", TNode.EMPTY_ATTRIBUTES, "!expr", false);

        TRenderContext context = new TRenderContext(new ByteArrayOutputStream(), Collections.<String, Object>singletonMap("expr", true));
        assertTrue(not.canRender(context).isPresent());

        context = new TRenderContext(new ByteArrayOutputStream(), Collections.<String, Object>singletonMap("expr", false));
        assertFalse(not.canRender(context).isPresent());

        TNodeIf normal = new TNodeIf(info, TextContentParserTest.createTestTextContentParser(), "Node", TNode.EMPTY_ATTRIBUTES, "expr", false);

        context = new TRenderContext(new ByteArrayOutputStream(), Collections.<String, Object>singletonMap("expr", false));
        assertTrue(normal.canRender(context).isPresent());

        context = new TRenderContext(new ByteArrayOutputStream(), Collections.<String, Object>singletonMap("expr", true));
        assertFalse(normal.canRender(context).isPresent());

    }


    @Test
    public void testOnlyChildren() throws Exception {
        final TemplateParser parser = createTestParser(SystemInfo.STAGE_PRODUCTION);
        final URL url = AbstractTemplateParserTest.class.getResource("if-children-only.html");
        final TNode parseTree = parser.parse(IO.toPath(url.toURI()));

        Map<String, Object> model = Maps.newHashMap();
        model.put("title", "Title");

        String expected = "<head>\n"
                          + "    <title >Title</title>\n"
                          + "</head>";
        assertCorrectRender(parseTree, model, expected);
    }
}