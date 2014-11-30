package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class XmlTemplateParserTest {

    @Test
    public void testParseIfTemplate() throws Exception {
        final XmlTemplateParser parser = new XmlTemplateParser();
        parser.tnodeFactories = Lists.<TNodeFactory>newArrayList(new TNodeFactoryNonEmpty());
        final InputStream stream = XmlTemplateParserTest.class.getResourceAsStream("non-empty-template.html");
        final TNode parseTree = parser.parse(stream);

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
    public void testRepeat() throws Exception {
        final XmlTemplateParser parser = new XmlTemplateParser();
        parser.tnodeFactories = Lists.<TNodeFactory>newArrayList(new TNodeFactoryRepeat());
        final InputStream stream = XmlTemplateParserTest.class.getResourceAsStream("repeat-template.html");
        final TNode parseTree = parser.parse(stream);

        Map<String, Object> model = Maps.newHashMap();
        Map<String, Object> row1 = Maps.newHashMap();
        row1.put("key1", "value1");
        row1.put("key2", "value2");
        Map<String, Object> row2 = Maps.newHashMap();
        row2.put("key1", "value3");
        row2.put("key2", "value4");
        model.put("maps", Lists.<Object>newArrayList(row1, row2));
        model.put("type", "x");

        String expected = "<html>" +
                          "<div>true - false - 0<div>0 - x - key1 - value1</div><div>1 - x - key2 - value2</div></div>" +
                          "<div>false - true - 1<div>0 - x - key1 - value3</div><div>1 - x - key1 - value4</div></div>" +
                          "</html>";
        assertCorrectRender(parseTree, model, expected);
    }

    @Test(expected = TemplateException.class)
    public void testRepeatNotMapWhenMapRequired() throws Exception {
        final XmlTemplateParser parser = new XmlTemplateParser();
        parser.tnodeFactories = Lists.<TNodeFactory>newArrayList(new TNodeFactoryRepeat());
        final InputStream stream = XmlTemplateParserTest.class.getResourceAsStream("repeat-template.html");
        final TNode parseTree = parser.parse(stream);

        Map<String, Object> model = Maps.newHashMap();
        model.put("maps", Lists.newArrayList(Lists.newArrayList()));
        model.put("type", "x");
        String expected = "This doesn't matter because the render should fail";
        assertCorrectRender(parseTree, model, expected);
    }

    @Test(expected = TemplateException.class)
    public void testMultipleDirectivesPerElem() throws Exception {
        final XmlTemplateParser parser = new XmlTemplateParser();
        parser.tnodeFactories = Lists.<TNodeFactory>newArrayList(new TNodeFactoryNonEmpty(), new TNodeFactoryRepeat());
        final InputStream stream = XmlTemplateParserTest.class.getResourceAsStream("multiple-directives-per-el-template.html");
        parser.parse(stream);
    }

    public void assertCorrectRender(TNode parseTree, Map<String, Object> model, String expected) throws IOException {
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        TRenderContext context = new TRenderContext(result, model);
        parseTree.render(context);

        assertEquals(expected, result.toString().replaceAll("\\n|\\r", "").replaceAll("\\s+", " "));
    }

}