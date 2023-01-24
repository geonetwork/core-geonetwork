//=============================================================================
//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.wro4j;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.DefaultLessCompiler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.css.Less4jProcessor;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.util.WroUtil;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Custom Geonetwork implementation for importing stylesheets. Main factor is to make it more forgiving.
 * <p/>
 * User: Jesse Date: 12/2/13 Time: 3:29 PM
 */
@SupportedResourceType(ResourceType.CSS)
public class GeonetLessCompilerProcessor extends Less4jProcessor {

    /**
     * The Constant ALIAS.
     */
    public static final String ALIAS = "geonetLessCompiler";

    /**
     * The Constant LESS_EXT.
     */
    private static final String LESS_EXT = ".less";

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = Logger.getLogger(GeonetLessCompilerProcessor.class.getPackage().getName());
    /**
     * The compiler.
     */
    private final LessCompiler compiler = new DefaultLessCompiler();
    /**
     * The locator factory.
     */
    @Inject
    private UriLocatorFactory locatorFactory;

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
     * @param reader   the reader
     * @param writer   the writer
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
                GeonetLessCompilerProcessor.LOG.warning(String.format(
                    "Failed to compile less resource: %s.",
                    resource.getUri()));
                for (final Problem problem : e.getErrors()) {
                    GeonetLessCompilerProcessor.LOG.warning(problemAsString(problem));
                }
                throw WroRuntimeException.wrap(e);
            } catch (final Exception e) {
                GeonetLessCompilerProcessor.LOG.warning(String.format(
                    "Exception while compiling less resource: %s.",
                    resource.getUri()));
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
     * @return the content of the custom less file originally in JSON format transformed in a less file content.
     * @throws IOException Signals that an I/O exception has occurred.
     */

    private String readCustomLessFile(Path customLessFile) throws IOException {
        StringBuilder customLessVariables = new StringBuilder();
        if (customLessFile != null) {
            final String content = new String(Files.readAllBytes(customLessFile));
            try {
                final JSONObject jObject = new JSONObject(content);
                final Iterator<String> iter = jObject.keys();
                while (iter.hasNext()) {
                    final String key = iter.next();
                    if (jObject.getString(key) != null && !jObject.getString(key).trim().equals("")) {
                        customLessVariables.append("\n @").append(key.trim()).append(" : ")
                            .append(jObject.getString(key).trim()).append(";");
                    }
                }
            } catch (final JSONException e) {
                throw WroRuntimeException.wrap(e);
            }
        }
        return customLessVariables.toString();
    }

    /**
     * Gets the custom less file from the GeoNetwork data directory. Returns null if running from a maven build.
     *
     * @return the path to the custom less file.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private Path getCustomLessFile() throws IOException {

        Path customLessFile = null;
        try {
            ApplicationContext applicationContext = ApplicationContextHolder.get();
            if (applicationContext != null) {
                final GeonetworkDataDirectory geonetworkDataDirectory = ApplicationContextHolder.get()
                    .getBean(GeonetworkDataDirectory.class);

                if (geonetworkDataDirectory.getSystemDataDir() != null) {
                    final String path = geonetworkDataDirectory.getSystemDataDir().resolve(Geonet.Config.NODE_LESS_DIR).toString();
                    customLessFile = openFileWithCustomStyle(path);
                }
            } else {
                LOG.warning("Executing Less Compiler from Maven. Cannot locate the custom less variable files at " +
                    "$DATA_DIR/" + Geonet.Config.NODE_LESS_DIR);
            }
        } catch (NoSuchBeanDefinitionException e) {
            LOG.severe("org.fao.geonet.kernel.GeonetworkDataDirectory bean not found");
        }

        return customLessFile;
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

    /**
     * Required to use the less4j import mechanism.
     */
    private static class RelativeAwareLessSource extends LessSource.StringSource {

        /**
         * The resource.
         */
        private final Resource resource;

        /**
         * The locator factory.
         */
        private final UriLocatorFactory locatorFactory;

        /**
         * Instantiates a new relative aware less source.
         *
         * @param resource       the resource
         * @param content        the content
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
         * @param resource     the resource
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
         * @param relativePath        the relative path
         * @return the string
         */
        public String computeRelativeResourceUri(final String originalResourceUri, final String relativePath) {
            final String fullPath = WroUtil.getFullPath(originalResourceUri) + relativePath;
            return WroUtil.normalize(fullPath);
        }
    }

}
