package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.services.metadata.format.groovy.Functions;
import org.fao.geonet.services.metadata.format.groovy.util.NavBarItem;
import org.fao.geonet.utils.IO;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TemplateParserTest {

    @Test
    public void testParseIfDirective() throws Exception {
        final TemplateParser parser = createTestParser(SystemInfo.STAGE_PRODUCTION);
        final URL url = TemplateParserTest.class.getResource("non-empty-template.html");
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
    public void testTranslateDirective() throws Exception {
        final Functions mock = Mockito.mock(Functions.class);
        Mockito.when(mock.translate("testString", null)).thenReturn("translation null");
        Mockito.when(mock.translate("testString", "file")).thenReturn("translation file");
        Mockito.when(mock.translate("nameKey", null)).thenReturn("Translated After resolve");
        Mockito.when(mock.codelistTranslation("testString", null, "name")).thenReturn("translation codelist null name");
        Mockito.when(mock.codelistTranslation("testString", "context1", "desc")).thenReturn("translation codelist desc context1");
        Mockito.when(mock.codelistTranslation("testString", "context2", "desc")).thenReturn("translation codelist desc context2");
        Mockito.when(mock.nodeTranslation("testString", "context", "name")).thenReturn("translation node name context");
        Mockito.when(mock.nodeTranslation("testString", null, "desc")).thenReturn("translation node desc null");
        Functions.setThreadLocal(mock);

        final TemplateParser parser = createTestParser(SystemInfo.STAGE_TESTING);
        final URL url = TemplateParserTest.class.getResource("translate.html");
        final TNode parseTree = parser.parse(IO.toPath(url.toURI()));

        String expected = "<html>\n"
                          + "    <div>translation null</div>\n"
                          + "    <div>translation null</div>\n"
                          + "    <div>translation file</div>\n"
                          + "    <div>translation file</div>\n"
                          + "    <div>translation codelist null name</div>\n"
                          + "    <div>translation codelist desc context1</div>\n"
                          + "    <div>translation codelist desc context2</div>\n"
                          + "    <div>translation node name context</div>\n"
                          + "    <div>translation node desc null</div>\n"
                          + "    <div>Translated After resolve</div>\n"
                          + "</html>";

        assertCorrectRender(parseTree, Collections.<String, Object>singletonMap("key", "nameKey"), expected);
    }

    @Test
    public void testRepeatDirective() throws Exception {
        final TemplateParser parser = createTestParser(SystemInfo.STAGE_TESTING);
        final URL url = TemplateParserTest.class.getResource("repeat-template.html");
        final TNode parseTree = parser.parse(IO.toPath(url.toURI()));

        Map<String, Object> model = Maps.newHashMap();
        Map<String, Object> row1 = Maps.newLinkedHashMap();
        row1.put("key1", "value1");
        row1.put("key2", "value2");
        Map<String, Object> row2 = Maps.newLinkedHashMap();
        row2.put("key1", "value3");
        row2.put("key2", "value4");
        model.put("maps", Lists.<Object>newArrayList(row1, row2));
        model.put("type", "x");

        String expected = "<html>" +
                          " <div> true - false - 0<div>0 - x - key1 - value1</div><div>1 - x - key2 - value2</div> </div>" +
                          " <div> false - true - 1<div>0 - x - key1 - value3</div><div>1 - x - key2 - value4</div> </div>" +
                          "</html>";
        assertCorrectRender(parseTree, model, expected);
    }

    @Test
    public void testRepeatDirectiveObject() throws Exception {
        final Functions mock = Mockito.mock(Functions.class);
        Mockito.when(mock.translate("name1", null)).thenReturn("Name 1");
        Mockito.when(mock.translate("name2", null)).thenReturn("Name 2");
        Functions.setThreadLocal(mock);

        final TemplateParser parser = createTestParser(SystemInfo.STAGE_TESTING);
        final URL url = TemplateParserTest.class.getResource("repeat-object-template.html");
        final TNode parseTree = parser.parse(IO.toPath(url.toURI()));

        Map<String, Object> model = Maps.newHashMap();
        final NavBarItem item1 = new NavBarItem();
        item1.setRel("rel1");
        item1.setName("name1");
        final NavBarItem item2 = new NavBarItem();
        item2.setRel("rel2");
        item2.setName("name2");
        model.put("items", Lists.newArrayList(item1, item2));

        String expected = "<ul>\n"
                          + "    <li><a rel=\".rel1\">Name 1</a></li>\n"
                          + "    <li><a rel=\".rel2\">Name 2</a></li>\n"
                          + "</ul>";

        assertCorrectRender(parseTree, model, expected);
    }

    @Test(expected = TemplateException.class)
    public void testRepeatNotMapWhenMapRequired() throws Exception {
        final TemplateParser parser = createTestParser(SystemInfo.STAGE_TESTING);
        final URL url = TemplateParserTest.class.getResource("repeat-template.html");
        final TNode parseTree = parser.parse(IO.toPath(url.toURI()));

        Map<String, Object> model = Maps.newHashMap();
        model.put("maps", Lists.newArrayList(Lists.newArrayList("elem1")));
        model.put("type", "x");
        String expected = "This doesn't matter because the render should fail";
        assertCorrectRender(parseTree, model, expected);
    }

    @Test(expected = TemplateException.class)
    public void testMultipleDirectivesPerElem() throws Exception {
        final TemplateParser parser = createTestParser(SystemInfo.STAGE_TESTING);
        final URL url = TemplateParserTest.class.getResource("multiple-directives-per-el-template.html");
        parser.parse(IO.toPath(url.toURI()));
    }

    public void assertCorrectRender(TNode parseTree, Map<String, Object> model, String expected) throws IOException {
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        TRenderContext context = new TRenderContext(result, model);

        parseTree.render(context);

        assertEquals(expected + "\n" + result, expected.replaceAll("\\n|\\r|\\s+", ""), result.toString().replaceAll("\\n|\\r|\\s+", ""));
    }

    public static TemplateParser createTestParser(String systemInfoStage) {
        SystemInfo info = SystemInfo.createForTesting(systemInfoStage);
        final TemplateParser parser = new TemplateParser();
        parser.tnodeFactories = Lists.<TNodeFactory>newArrayList(new TNodeFactoryIf(info), new TNodeFactoryRepeat(info), new TNodeFactoryTranslate(info));
        return parser;
    }
}