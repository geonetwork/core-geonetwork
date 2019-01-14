package org.fao.geonet.wro4j;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * This processor removes the `//# sourceMappingURL=...` annotations from the final bundle.
 * These annotations are not relevant when several files are appended, and would
 * cause errors in the log (see https://github.com/geonetwork/core-geonetwork/issues/2406).
 */
@SupportedResourceType(ResourceType.JS)
public class RemoveSourceMapUrlProcessor implements ResourcePreProcessor {
    public static final String ALIAS = "removeSourceMapUrl";
    protected static final String SOURCE_MAP_URL_PREFIX = "//# sourceMappingURL=";

    @Override
    public void process(Resource resource, Reader reader, Writer writer) throws IOException {
        removeAnnotation(reader, writer);
    }

    private void removeAnnotation(Reader reader, Writer writer) throws IOException {
        LineIterator it = IOUtils.lineIterator(reader);
        String line;
        while(it.hasNext()) {
            line = it.next();
            if (!line.startsWith(SOURCE_MAP_URL_PREFIX)) {
                writer.append(line);
                writer.append(IOUtils.LINE_SEPARATOR);
            }
        }
    }
}
