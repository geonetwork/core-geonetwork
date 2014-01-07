package org.fao.geonet.wro4j;

import com.google.javascript.jscomp.CompilationLevel;
import org.apache.commons.io.IOUtils;
import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import java.io.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static org.fao.geonet.wro4j.ClosureRequireDependencyManager.Node.*;

/**
 * Remove the goog.require and provide declarations from file.
 *
 * <p/>
 * User: Jesse
 * Date: 12/2/13
 * Time: 3:29 PM
 */
@SupportedResourceType(ResourceType.JS)
@Minimize
public class StripGoogProcessor
        implements ResourcePreProcessor {

    public static final String ALIAS = "stripGoog";
    private static final Pattern pattern = Pattern.compile("(" + PROVIDES_PATTERN_STRING + "\\s*;*)|(" + REQUIRE_PATTERN_STRING + "\\s*;*)");
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
