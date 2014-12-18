package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TextContentParserTest {

    @Test
    public void testParse() throws Exception {
        final TextContentParser parser = new TextContentParser();

        final TextBlock contents = parser.parse("Hi {{name}}\n\n\tThis is a great test\n\nFrom {{from}}");

        Map<String, Object> model = Maps.newHashMap();
        model.put("name", "Name");
        model.put("from", "From");
        assertCorrectRender(contents, model, "Hi Name\n\n\tThis is a great test\n\nFrom From");

        model.remove("from");
        assertCorrectRender(contents, model, "Hi Name\n\n\tThis is a great test\n\nFrom from");

        model.remove("name");
        assertCorrectRender(contents, model, "Hi name\n\n\tThis is a great test\n\nFrom from");

        model.put("name", "Name");
        model.put("from", "From");
        assertCorrectRender(parser.parse("{{name}}{{from}}"), model, "NameFrom");
    }

    @Test
    public void testParseAmp() throws Exception {
        final TextContentParser parser = new TextContentParser();

        final TextBlock contents = parser.parse("{{name}}");

        Map<String, Object> model = Maps.newHashMap();
        model.put("name", "&Name");
        assertCorrectRender(contents, model, "&amp;Name");
    }

    public void assertCorrectRender(TextBlock contents, Map<String, Object> model, String expected) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TRenderContext context = new TRenderContext(out, model);
        contents.render(context);

        assertEquals(expected, out.toString());
    }
}