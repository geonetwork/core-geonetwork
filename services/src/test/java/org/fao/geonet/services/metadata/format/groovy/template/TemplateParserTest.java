package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.utils.IO;
import org.junit.Test;

import java.net.URL;
import java.util.Map;

/**
 * @author Jesse on 12/3/2014.
 */
public class TemplateParserTest extends AbstractTemplateParserTest {
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
}
