package org.fao.geonet.wro4j;

import junit.framework.TestCase;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by Jesse on 12/19/13.
 */
public class StripGoogProcessorTest extends TestCase {
    public void testProcess1() throws Exception {
        Reader reader = new StringReader("goog.provide('id');goog.require('id2'); console.log('hi')");
        StringWriter writer = new StringWriter();
        final StripGoogProcessor processor = new StripGoogProcessor();
        Resource resource = new Resource();
        resource.setMinimize(true);
        resource.setType(ResourceType.JS);
        resource.setUri("file://dir/file.js");
        processor.process(resource, reader, writer);

        assertEquals("console.log('hi')", writer.toString().trim());
    }

    public void testProcess2() throws Exception {
        Reader reader = new StringReader("goog . provide  ('id');goog . require('id2'); console.log('hi')");
        StringWriter writer = new StringWriter();
        final StripGoogProcessor processor = new StripGoogProcessor();
        Resource resource = new Resource();
        resource.setMinimize(true);
        resource.setType(ResourceType.JS);
        resource.setUri("file://dir/file.js");
        processor.process(resource, reader, writer);

        assertEquals("console.log('hi')", writer.toString().trim());
    }

    public void testProcess3() throws Exception {
        Reader reader = new StringReader("goog . provide  ('id')\ngoog . require('id2')\n console.log('hi')");
        StringWriter writer = new StringWriter();
        final StripGoogProcessor processor = new StripGoogProcessor();
        Resource resource = new Resource();
        resource.setMinimize(true);
        resource.setType(ResourceType.JS);
        resource.setUri("file://dir/file.js");
        processor.process(resource, reader, writer);

        assertEquals("console.log('hi')", writer.toString().trim());
    }

    public void testProcess4() throws Exception {
        Reader reader = new StringReader("goog.provide('id');goog.require('id2'); goog.require('id2'); goog.require('id2'); \n" +
            "goog.require('id2'); \ngoog.require('id2'); goog.require('id2'); \n\nconsole.log('hi')");
        StringWriter writer = new StringWriter();
        final StripGoogProcessor processor = new StripGoogProcessor();
        Resource resource = new Resource();
        resource.setMinimize(true);
        resource.setType(ResourceType.JS);
        resource.setUri("file://dir/file.js");
        processor.process(resource, reader, writer);

        assertEquals("console.log('hi')", writer.toString().trim());
    }

}
