package org.fao.geonet.wro4j;

import org.junit.Test;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import static java.lang.String.format;
import static org.fao.geonet.wro4j.AddFileUriCommentProcessor.END_CSS_BLOCK;
import static org.fao.geonet.wro4j.AddFileUriCommentProcessor.START_CSS_BLOCK;
import static org.junit.Assert.assertTrue;

/**
 * Test the processor adds the correct comments.
 * <p/>
 * Created by Jesse on 12/20/13.
 */
public class AddFileUriCommentProcessorTest {
    @Test
    public void testProcessJavascript() throws Exception {
        final String expectedTextWithWhitespace = "\nt;\n";
        Reader reader = new StringReader("goog.provide('id');goog.require('id2'); console.log('hi');" + expectedTextWithWhitespace);
        StringWriter writer = new StringWriter();
        final AddFileUriCommentProcessor processor = new AddFileUriCommentProcessor();
        Resource resource = new Resource();
        resource.setMinimize(true);
        resource.setType(ResourceType.JS);
        resource.setUri("file://dir/file.js");
        processor.process(resource, reader, writer);

        assertTrue(writer.toString().contains(format(AddFileUriCommentProcessor.START_JS_COMMENT, resource.getUri())));
        assertTrue(writer.toString().contains(format(AddFileUriCommentProcessor.END_JS_COMMENT, resource.getUri())));
        assertTrue(writer.toString().contains(expectedTextWithWhitespace));
    }

    @Test
    public void testProcessCss() throws Exception {
        String expectedTextWithWhitespace = "\nt {} \n";
        Reader reader = new StringReader("a {background-color: RED}" + expectedTextWithWhitespace);
        StringWriter writer = new StringWriter();
        final AddFileUriCommentProcessor processor = new AddFileUriCommentProcessor();
        Resource resource = new Resource();
        resource.setMinimize(true);
        resource.setType(ResourceType.CSS);
        resource.setUri("file://dir/file.css");
        processor.process(resource, reader, writer);

        assertTrue(writer.toString().contains(format(START_CSS_BLOCK, resource.getUri())));
        assertTrue(writer.toString().contains(format(END_CSS_BLOCK, resource.getUri())));
        assertTrue(writer.toString().contains(expectedTextWithWhitespace));
    }

}
