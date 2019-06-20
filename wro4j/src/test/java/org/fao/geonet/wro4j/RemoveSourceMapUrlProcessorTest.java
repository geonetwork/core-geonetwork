package org.fao.geonet.wro4j;

import org.junit.Test;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test the processor correctly removes the annotation.
 */
public class RemoveSourceMapUrlProcessorTest {
    @Test
    public void removeAnnotation() throws Exception {
        final String sourceMapAnnotation = "//# sourceMappingURL=this-is-a-test.map";
        final String bundledCode = "goog.provide('id');\ngoog.require('id2');\nconsole.log('hello'); console.log('bye');";
        Reader reader = new StringReader(bundledCode + "\n" + sourceMapAnnotation);
        StringWriter writer = new StringWriter();
        final RemoveSourceMapUrlProcessor processor = new RemoveSourceMapUrlProcessor();
        Resource resource = new Resource();
        resource.setMinimize(false);
        resource.setType(ResourceType.JS);
        resource.setUri("file://dir/file.js");
        processor.process(resource, reader, writer);

        assertTrue(writer.toString().contains(bundledCode));
        assertFalse(writer.toString().contains("//# sourceMappingURL=this-is-a-test.map"));
    }
}
