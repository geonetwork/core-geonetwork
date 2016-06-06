package org.fao.geonet.wro4j;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Pattern;

import static org.fao.geonet.wro4j.ClosureRequireDependencyManager.Node.PROVIDES_PATTERN_STRING;
import static org.fao.geonet.wro4j.ClosureRequireDependencyManager.Node.REQUIRE_PATTERN_STRING;

/**
 * Remove the goog.require and provide declarations from file.
 * <p/>
 * <p/>
 * User: Jesse Date: 12/2/13 Time: 3:29 PM
 */
@SupportedResourceType(ResourceType.JS)
@Minimize
public class StripGoogProcessor
    implements ResourcePreProcessor {

    public static final String ALIAS = "stripGoog";
    private static final Pattern pattern = Pattern.compile("(" + PROVIDES_PATTERN_STRING + "\\s*;*)|(" + REQUIRE_PATTERN_STRING +
        "\\s*;*)");

    @Override
    public void process(Resource resource, Reader reader, Writer writer) throws IOException {
        String js = IOUtils.toString(reader);
        final String[] split = pattern.split(js);
        for (String s : split) {
            if (!s.isEmpty()) {
                writer.write(s);
            }
        }
    }

}
