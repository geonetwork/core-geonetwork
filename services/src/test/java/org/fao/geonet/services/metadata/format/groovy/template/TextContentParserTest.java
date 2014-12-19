package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TextContentParserTest {

    @Test
    public void testParse() throws Exception {
        final TextContentParser parser = createTestTextContentParser();

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
        final TextContentParser parser = createTestTextContentParser();

        TextBlock contents = parser.parse("{{name}}");

        Map<String, Object> model = Maps.newHashMap();
        model.put("name", "&Name");
        assertCorrectRender(contents, model, "&Name");

        final Component annotation = FilterEscapeXmlAttrs.class.getAnnotation(Component.class);
        contents = parser.parse("{{name | " + annotation.value() + "}}");
        model.put("name", "&Name");
        assertCorrectRender(contents, model, "&amp;Name");
    }

    public static void assertCorrectRender(TextBlock contents, Map<String, Object> model, String expected) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TRenderContext context = new TRenderContext(out, model);
        contents.render(context);

        assertEquals(expected, out.toString());
    }

    public static TextContentParser createTestTextContentParser() throws InstantiationException, IllegalAccessException {
        final TextContentParser parser = new TextContentParser();
        addFilters(parser, FilterCapitalize.class, FilterEscapeXmlAttrs.class, FilterEscapeXmlContent.class, FilterLowerCase.class,
                FilterUpperCase.class);
        return parser;
    }

    private static void addFilters(TextContentParser parser, Class<? extends TextContentFilter>... filters) throws IllegalAccessException,
            InstantiationException {
        for (Class<? extends TextContentFilter> filter : filters) {
            final Component componentAnnotation = filter.getAnnotation(Component.class);
            parser.filters.put(componentAnnotation.value(), filter.newInstance());
        }
    }
}