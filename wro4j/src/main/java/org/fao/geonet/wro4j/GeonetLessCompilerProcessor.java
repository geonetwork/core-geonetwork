package org.fao.geonet.wro4j;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.DefaultLessCompiler;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.css.Less4jProcessor;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.util.WroUtil;

/**
 * Custom Geonetwork implementation for importing stylesheets. Main factor is to make it more forgiving.
 * <p/>
 * User: Jesse Date: 12/2/13 Time: 3:29 PM
 */
@SupportedResourceType(ResourceType.CSS)
public class GeonetLessCompilerProcessor extends Less4jProcessor {

    /** The Constant ALIAS. */
    public static final String ALIAS = "geonetLessCompiler";
    
    /** The Constant LESS_EXT. */
    private static final String LESS_EXT = ".less";

    /** The Constant LOG. */
    private static final Logger LOG = Logger.getLogger(GeonetLessCompilerProcessor.class.getPackage().getName());

    /**
     * Required to use the less4j import mechanism.
     */
    private static class RelativeAwareLessSource extends LessSource.StringSource {
        
        /** The resource. */
        private final Resource resource;
        
        /** The locator factory. */
        private final UriLocatorFactory locatorFactory;

        /**
         * Instantiates a new relative aware less source.
         *
         * @param resource the resource
         * @param content the content
         * @param locatorFactory the locator factory
         */
        public RelativeAwareLessSource(final Resource resource, final String content, final UriLocatorFactory locatorFactory) {
            super(content);
            this.resource = resource;
            Validate.notNull(locatorFactory);
            this.locatorFactory = locatorFactory;
        }

        /**
         * Relative source.
         *
         * @param relativePath the relative path
         * @return the less source
         * @throws StringSourceException the string source exception
         */
        @Override
        public LessSource relativeSource(final String relativePath) throws StringSourceException {
            return resource != null ? computeRelative(resource, relativePath) : super.relativeSource(relativePath);
        }

        /**
         * Compute relative.
         *
         * @param resource the resource
         * @param relativePath the relative path
         * @return the less source
         * @throws StringSourceException the string source exception
         */
        private LessSource computeRelative(final Resource resource, final String relativePath) throws StringSourceException {
            try {
                final String relativeResourceUri = computeRelativeResourceUri(resource.getUri(), relativePath);
                final Resource relativeResource = Resource.create(relativeResourceUri, ResourceType.CSS);
                final String relativeResourceContent = IOUtils.toString(locatorFactory.locate(relativeResourceUri), "UTF-8");
                return new RelativeAwareLessSource(relativeResource, relativeResourceContent, locatorFactory);
            } catch (final IOException e) {
                GeonetLessCompilerProcessor.LOG.fine("Failed to compute relative resource: " + resource);
                throw new StringSourceException();
            }
        }

        /**
         * Compute relative resource uri.
         *
         * @param originalResourceUri the original resource uri
         * @param relativePath the relative path
         * @return the string
         */
        public String computeRelativeResourceUri(final String originalResourceUri, final String relativePath) {
            final String fullPath = WroUtil.getFullPath(originalResourceUri) + relativePath;
            return WroUtil.normalize(fullPath);
        }
    }

    /** The locator factory. */
    @Inject
    private UriLocatorFactory locatorFactory;

    /** The compiler. */
    private final LessCompiler compiler = new DefaultLessCompiler();

    /**
     * Process.
     *
     * @param reader the reader
     * @param writer the writer
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void process(final Reader reader, final Writer writer) throws IOException {
        try {
            process(null, reader, writer);
        } catch (final Throwable t) {
            GeonetLessCompilerProcessor.LOG.fine("Unable to compile a stylesheet");
        }
    }

    /**
     * Open file with custom style.
     *
     * @param path the path
     * @return the path
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private Path openFileWithCustomStyle(String path) throws IOException {
        final Path lessPath = Paths.get(path + "/gn_dynamic_style.json");
        if (Files.exists(lessPath)) {
            return lessPath;
        } else {
            return null;
        }
    }

    /**
     * Process.
     *
     * @param resource the resource
     * @param reader the reader
     * @param writer the writer
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void process(final Resource resource, final Reader reader, final Writer writer) throws IOException {
        if (resource.getUri().endsWith(GeonetLessCompilerProcessor.LESS_EXT)) {
            try {
                final String customLessVariables = getCustomLessVariables();
                final String stringReader = IOUtils.toString(reader) + customLessVariables;
                final LessSource lessSource = new RelativeAwareLessSource(resource, stringReader, locatorFactory);
                final CompilationResult result = compiler.compile(lessSource);
                logWarnings(result);
                writer.write(result.getCss());
            } catch (final Less4jException e) {
                GeonetLessCompilerProcessor.LOG.fine("Failed to compile less resource: {}.");
                for (final Problem problem : e.getErrors()) {
                    GeonetLessCompilerProcessor.LOG.fine(problemAsString(problem));
                }
                throw WroRuntimeException.wrap(e);
            } catch (final Exception e) {
                GeonetLessCompilerProcessor.LOG.fine("Exception while compiling less resource: {}.");
                throw WroRuntimeException.wrap(e);
            }
        } else {
            writer.write(IOUtils.toString(reader));
        }
    }

    /**
     * Gets the custom less variables.
     *
     * @return the custom less variables
     * @throws IOException Signals that an I/O exception has occurred.
     */

    private String getCustomLessVariables() throws IOException {

        final Path customLessFile = getCustomLessFile();
        return readCustomLessFile(customLessFile);
    }

    /**
     * Read custom less file.
     *
     * @param customLessFile the custom less file
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */

    private String readCustomLessFile(Path customLessFile) throws IOException {
        String customLessVariables = new String();
        if (customLessFile != null) {
            final String content = new String(Files.readAllBytes(customLessFile));
            try {
                final JSONObject jObject = new JSONObject(content);
                final Iterator<String> iter = jObject.keys();
                while (iter.hasNext()) {
                    final String key = iter.next();
                    if (jObject.getString(key) != null && !jObject.getString(key).trim().equals("")) {
                        customLessVariables += "\n @" + key.trim() + " : " + jObject.getString(key).trim() + ";";
                    }
                }
            } catch (final JSONException e) {
                throw WroRuntimeException.wrap(e);
            }
        }
        return customLessVariables;
    }

    /**
     * Gets the custom less file.
     *
     * @return the custom less file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private Path getCustomLessFile() throws IOException {
        
        try {
            final GeonetworkDataDirectory geonetworkDataDirectory = ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class);
            
            if(geonetworkDataDirectory!=null && geonetworkDataDirectory.getSystemDataDir()!=null) {
                final String path = geonetworkDataDirectory.getSystemDataDir().resolve(Geonet.Config.NODE_LESS_DIR).toString();
                final Path customLessFile = openFileWithCustomStyle(path);
                return customLessFile;
            } else {
                return null;
            }
        } catch (NoSuchBeanDefinitionException e) {
           LOG.fine("org.fao.geonet.kernel.GeonetworkDataDirectory bean not found"); 
        }
        
        return null;
    }

    /**
     * Log warnings.
     *
     * @param result the result
     */
    private void logWarnings(final CompilationResult result) {
        if (!result.getWarnings().isEmpty()) {
            GeonetLessCompilerProcessor.LOG.fine("Less warnings are:");
            for (final Problem problem : result.getWarnings()) {
                GeonetLessCompilerProcessor.LOG.fine(problemAsString(problem));
            }
        }
    }

    /**
     * Problem as string.
     *
     * @param problem the problem
     * @return the string
     */
    private String problemAsString(final Problem problem) {
        return String.format("%s:%s %s.", problem.getLine(), problem.getCharacter(), problem.getMessage());
    }

}
