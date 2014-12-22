package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TextContentReplacementTest {

    @Test
    public void testText() throws Exception {
        final TextContentReplacement textContentReplacement = new TextContentReplacement("key", Lists.<TextContentFilter>newArrayList());


        String expected = "value";
        Map<String, Object> model = Maps.newHashMap();
        model.put("key", expected);

        assertCorrectText(textContentReplacement, expected, model);
    }

    @Test
    public void testTextAttributeEscape() throws Exception {
        final TextContentReplacement textContentReplacement = new TextContentReplacement("key",
                Lists.newArrayList(new FilterEscapeXmlAttrs()));


        String expected = "value";
        Map<String, Object> model = Maps.newHashMap();
        model.put("key", expected);

        assertCorrectText(textContentReplacement, expected, model);
    }

    public void assertCorrectText(TextContentReplacement textContentReplacement, String expected, Map<String, Object> model) throws UnsupportedEncodingException {
        TRenderContext context = new TRenderContext(new ByteArrayOutputStream(), model);
        final String text = textContentReplacement.text(context);
        assertEquals(expected, text);
    }
}