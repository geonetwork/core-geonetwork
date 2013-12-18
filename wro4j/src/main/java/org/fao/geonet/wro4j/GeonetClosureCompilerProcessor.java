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

/**
 * Custom Geonetwork implementation for importing stylesheets.  Main factor is to make it more forgiving.
 * <p/>
 * User: Jesse
 * Date: 12/2/13
 * Time: 3:29 PM
 */
@SupportedResourceType(ResourceType.JS)
@Minimize
public class GeonetClosureCompilerProcessor
        implements ResourcePreProcessor {

    public static final String ALIAS = "geonetClosureCompiler";
    private static final String EXCEPTIONS_FILE = "closure-compiler-exception.properties";

    @Inject
    private Injector injector;

    private volatile boolean initialized = false;
    private GoogleClosureCompressorProcessor googleClosureWhitespace = new GoogleClosureCompressorProcessor(CompilationLevel
            .WHITESPACE_ONLY);
    private GoogleClosureCompressorProcessor googleClosureSimple = new GoogleClosureCompressorProcessor(CompilationLevel
            .SIMPLE_OPTIMIZATIONS);

    {
        googleClosureWhitespace.setEncoding("UTF-8");
        googleClosureSimple.setEncoding("UTF-8");
    }

    private Set<String> whitespaceOnly = new HashSet<String>();
    private Set<String> unmodified = new HashSet<String>();

    @Override
    public void process(Resource resource, Reader reader, Writer writer) throws IOException {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    injector.inject(googleClosureWhitespace);
                    injector.inject(googleClosureSimple);

                    final ClassLoader classLoader = GeonetClosureCompilerProcessor.class.getClassLoader();

                    InputStream stream = null;
                    InputStreamReader propertiesReader = null;
                    try {
                        stream = classLoader.getResourceAsStream(EXCEPTIONS_FILE);
                        propertiesReader = new InputStreamReader(stream, "UTF-8");

                        final Properties properties = new Properties();
                        properties.load(propertiesReader);

                        final String[] whitespace = properties.getProperty("whitespace", "").split(",");
                        for (String s : whitespace) {
                            if (!s.trim().isEmpty()) {
                                whitespaceOnly.add(s.trim());
                            }
                        }
                        final String[] unmodified = properties.getProperty("unmodified", "").split(",");
                        for (String s : unmodified) {
                            if (!s.trim().isEmpty()) {
                                this.unmodified.add(s.trim());
                            }
                        }
                    } finally {
                        IOUtils.closeQuietly(propertiesReader);
                        IOUtils.closeQuietly(stream);
                    }
                    initialized = true;
                }
            }
        }
        GoogleClosureCompressorProcessor processor = googleClosureSimple;
        for (String pathName : whitespaceOnly) {
            if (resource.getUri().endsWith(pathName)) {
                processor = googleClosureWhitespace;
                break;
            }
        }
        if (processor == googleClosureSimple) {
            for (String pathName : unmodified) {
                if (resource.getUri().endsWith(pathName)) {
                    writer.write(IOUtils.toString(reader));
                    return;
                }
            }
        }
        try {
            StringWriter out = new StringWriter();
            processor.process(reader, out);
            writer.write(out.toString());
        } catch (Throwable t) {
            String level = processor == googleClosureSimple ? "Google Closure Simple Compiler" : "Google Closure Whitespace Compiler";
            final String msg = "\n\nUnable to use " + level +
                               " to compile \n\t'" + resource.getUri() + "'\n\nAdd the file to the " + EXCEPTIONS_FILE +
                               " that is in wro4j/src/main/resources.";
            Logger.getLogger(getClass().getPackage().getName()).log(Level.WARNING, msg);
        }
    }

}
