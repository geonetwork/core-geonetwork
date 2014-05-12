package org.fao.geonet.wro4j;

import org.apache.commons.io.IOUtils;
import ro.isdc.wro.extensions.processor.css.Less4jProcessor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Logger;

/**
 * Custom Geonetwork implementation for importing stylesheets.  Main factor is to make it more forgiving.
 * <p/>
 * User: Jesse
 * Date: 12/2/13
 * Time: 3:29 PM
 */
@SupportedResourceType(ResourceType.CSS)
public class GeonetLessCompilerProcessor
        extends Less4jProcessor {

    public static final String ALIAS = "geonetLessCompiler";
    private static final String LESS_EXT = ".less";
    private static final String CSS_EXT = ".css";

    @Override
    public void process(Reader reader, Writer writer) throws IOException {
        try {
            super.process(reader, writer);
        } catch (Throwable t) {
            Logger.getLogger(getClass().getPackage().getName()).fine("Unable to compile a stylesheet");
        }
    }

    @Override
    public void process(Resource resource, Reader reader, Writer writer) throws IOException {
        if (resource.getUri().endsWith(LESS_EXT)) {
            super.process(resource, reader, writer);
        } else {
            writer.write(IOUtils.toString(reader));
        }
    }
}
