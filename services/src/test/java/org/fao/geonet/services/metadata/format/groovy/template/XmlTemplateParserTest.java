package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class XmlTemplateParserTest {

    @Test
    public void testParseIfTemplate() throws Exception {
        final XmlTemplateParser parser = new XmlTemplateParser();
        parser.tnodeFactories = Lists.<TNodeFactory>newArrayList(new IfNodeFactory());
        final InputStream stream = XmlTemplateParserTest.class.getResourceAsStream("non-empty-template.html");
        final TNode parseTree = parser.parse(stream);

        Map<String, Object> model = Maps.newHashMap();
        model.put("title", "Title");
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
    }

    public void assertCorrectRender(TNode parseTree, Map<String, Object> model, String expected) throws IOException {
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        TRenderContext context = new TRenderContext(result, model);
        parseTree.render(context);

        assertEquals(expected, result.toString().replaceAll("\\n|\\r", "").replaceAll("\\s\\s", " "));
    }
}