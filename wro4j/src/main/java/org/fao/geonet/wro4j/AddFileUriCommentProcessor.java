package org.fao.geonet.wro4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import ro.isdc.wro.extensions.processor.css.Less4jProcessor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Logger;

/**
 * Custom Geonetwork implementation for importing stylesheets.  Main factor is to make it more forgiving.
 *
 * User: Jesse
 * Date: 12/2/13
 * Time: 3:29 PM
 */
public class AddFileUriCommentProcessor implements ResourcePreProcessor {

    public static final String ALIAS = "addFileUriComment";
    private static final int MAX_FILE_LENGTH = 50;

    @Override
    public void process(Resource resource, Reader reader, Writer writer) throws IOException {
        String uri = resource.getUri();
        if (resource.getUri().length() > MAX_FILE_LENGTH) {
            uri = "..." + uri.substring(uri.length() - MAX_FILE_LENGTH);
        }
        writer.write("// ---------------  Start ");
        writer.write(uri);
        writer.write("  --------------- //\n\n\n\n");
        IOUtils.copy(reader, writer);
        writer.write("\n\n\n\n// ---------------  End ");
        writer.write(uri);
        writer.write("  --------------- //\n");
    }
}
