package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.services.metadata.format.groovy.Functions;
import org.fao.geonet.services.metadata.format.groovy.TransformationContext;
import org.fao.geonet.services.metadata.format.groovy.util.NavBarItem;
import org.fao.geonet.utils.IO;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URL;
import java.util.Map;

/**
 * @author Jesse on 12/3/2014.
 */
public class TNodeRepeatTest extends AbstractTemplateParserTest {

    @Test
    public void testRepeatDirective() throws Exception {
        final TemplateParser parser = createTestParser(SystemInfo.STAGE_TESTING);
        final URL url = TNodeRepeatTest.class.getResource("repeat-template.html");
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

    @Test(expected = TemplateException.class)
    public void testRepeatMissingIterModelValue() throws Exception {
        final TemplateParser parser = createTestParser(SystemInfo.STAGE_TESTING);
        final String template = "<div fmt-repeat=\"item in list\" fmt-repeat-only-children=\"true\">{{item}}</div>";
        final TNode parseTree = parser.parse(template.getBytes(Constants.CHARSET), TemplateType.XML);
        assertCorrectRender(parseTree, Maps.<String, Object>newHashMap(), "");
    }
    @Test(expected = TemplateException.class)
    public void testRepeatMissingMapModelValue() throws Exception {
        final TemplateParser parser = createTestParser(SystemInfo.STAGE_TESTING);
        final String template = "<div fmt-repeat=\"(key, value) in map\" fmt-repeat-only-children=\"true\">{{key}}</div>";
        final TNode parseTree = parser.parse(template.getBytes(Constants.CHARSET), TemplateType.XML);
        assertCorrectRender(parseTree, Maps.<String, Object>newHashMap(), "");
    }
    @Test
    public void testRepeatOnlyChildren() throws Exception {
        final TemplateParser parser = createTestParser(SystemInfo.STAGE_TESTING);
        final URL url = TNodeRepeatTest.class.getResource("repeat-only-children-template.html");
        final TNode parseTree = parser.parse(IO.toPath(url.toURI()));

        Map<String, Object> model = Maps.newHashMap();
        Map<String, Object> map = Maps.newLinkedHashMap();
        map.put("key1", "value1");
        map.put("key2", "value2");
        model.put("map", map);
        model.put("list", Lists.newArrayList("item1", "item2", "item3"));

        String expected = "<html>" +
                          "  <li class=\"map\">key1 - value1</li>" +
                          "  <li class=\"map\">key2 - value2</li>" +
                          "  <li class=\"list\">item1</li>" +
                          "  <li class=\"list\">item2</li>" +
                          "  <li class=\"list\">item3</li>" +
                          "</html>";
        assertCorrectRender(parseTree, model, expected);
    }

    @Test
    public void testRepeatDirectiveObject() throws Exception {
        final Functions mock = Mockito.mock(Functions.class);
        Mockito.when(mock.translate("name1", null)).thenReturn("Name 1");
        Mockito.when(mock.translate("name2", null)).thenReturn("Name 2");
        new TransformationContext(null, mock, null).setThreadLocal();

        final TemplateParser parser = createTestParser(SystemInfo.STAGE_TESTING);
        final URL url = TNodeRepeatTest.class.getResource("repeat-object-template.html");
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
    }}
