package org.fao.geonet.wro4j;

import static org.junit.Assert.*;
import org.junit.Test;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Test the processor adds the correct comments.
 *
 * Created by Jesse on 12/20/13.
 */
public class AddFileUriCommentProcessorTest {
    @Test
    public void testProcess1() throws Exception {
        Reader reader = new StringReader("goog.provide('id');goog.require('id2'); console.log('hi')");
        StringWriter writer = new StringWriter();
        final AddFileUriCommentProcessor processor = new AddFileUriCommentProcessor();
        Resource resource = new Resource();
        resource.setMinimize(true);
        resource.setType(ResourceType.JS);
        resource.setUri("file://dir/file.js");
        processor.process(resource, reader, writer);

        assertTrue(writer.toString().contains("Start "+resource.getUri()));
        assertTrue(writer.toString().contains("End " + resource.getUri()));
    }
}
