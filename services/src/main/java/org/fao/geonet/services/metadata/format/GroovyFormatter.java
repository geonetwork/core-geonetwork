package org.fao.geonet.services.metadata.format;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.codehaus.groovy.control.CompilationFailedException;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.repository.IsoLanguageRepository;
import org.fao.geonet.services.metadata.format.groovy.Environment;
import org.fao.geonet.services.metadata.format.groovy.EnvironmentProxy;
import org.fao.geonet.services.metadata.format.groovy.Functions;
import org.fao.geonet.services.metadata.format.groovy.Handlers;
import org.fao.geonet.services.metadata.format.groovy.template.TemplateCache;
import org.fao.geonet.services.metadata.format.groovy.Transformer;
import org.fao.geonet.utils.IO;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static org.fao.geonet.services.metadata.format.FormatterConstants.GROOVY_SCRIPT_ROOT;
import static org.fao.geonet.services.metadata.format.FormatterConstants.SCHEMA_PLUGIN_FORMATTER_DIR;

/**
 * Formatter for groovy dialect of formatters.
 *
 * @author Jesse on 10/15/2014.
 */
@Component
public class GroovyFormatter implements FormatterImpl {

    @Autowired
    GeonetworkDataDirectory dataDirectory;
    @Autowired
    SchemaManager schemaManager;
    @Autowired
    private IsoLanguagesMapper mapper;
    @Autowired
    private TemplateCache templateCache;
    @Autowired
    private IsoLanguageRepository isoLanguageRepository;
    private final Cache<Path, Transformer> transformers = CacheBuilder.newBuilder().
            concurrencyLevel(1).
            maximumSize(40).
            initialCapacity(40).build();
    private GroovyClassLoader baseClassLoader;
    private Map<String, GroovyClassLoader> schemaClassLoaders = Maps.newHashMap();

    @VisibleForTesting
    Transformer findTransformer(final FormatterParams fparams) throws ExecutionException {
        return transformers.get(fparams.formatDir, new Callable<Transformer>() {
            @Override
            public Transformer call() throws Exception {
                return createTransformer(fparams);
            }
        });
    }
    public String format(FormatterParams fparams) throws Exception {
        EnvironmentProxy.clearContext();
        final Transformer transformer = createTransformer(fparams);

        EnvironmentProxy.setCurrentEnvironment(fparams, this.mapper);
        try {
            final List<Namespace> namespaces = this.schemaManager.getSchema(fparams.schema).getNamespaces();
            return transformer.apply(fparams.metadata, namespaces);
        } finally {
            EnvironmentProxy.clearContext();
        }
    }

    private synchronized Transformer createTransformer(FormatterParams fparams) throws Exception {
        Transformer transformer = this.transformers.getIfPresent(fparams.formatDir);

        if (fparams.isDevMode() || transformer == null) {
            final Path baseShared = this.dataDirectory.getFormatterDir().resolve(GROOVY_SCRIPT_ROOT);
            final Path schemaFormatterDir = getSchemaPluginFormatterDir(fparams.schema);
            final Path schemaShared = schemaFormatterDir.resolve(GROOVY_SCRIPT_ROOT);
            GroovyClassLoader cl = getParentClassLoader(fparams, fparams.schema, baseShared, schemaShared);

            URL[] roots = new URL[]{
                    IO.toURL(fparams.formatDir)
            };
            GroovyScriptEngine groovyScriptEngine = new GroovyScriptEngine(roots, cl);

            loadScripts(fparams.formatDir, groovyScriptEngine);

            Handlers handlers = new Handlers(fparams, schemaShared.getParent(), baseShared.getParent(), this.templateCache);
            Environment env = new EnvironmentProxy();
            Functions functions = new Functions(fparams, env);
            Binding binding = new Binding();
            binding.setVariable("handlers", handlers);
            binding.setVariable("env", env);
            binding.setVariable("f", functions);

            final String scriptName = fparams.viewFile.toAbsolutePath().toUri().toString();
            groovyScriptEngine.run(scriptName, binding);

            transformer = new Transformer(handlers, functions, env, fparams.formatDir.toAbsolutePath());
            this.transformers.put(fparams.formatDir, transformer);
        }

        return transformer;
    }

    private Path getSchemaPluginFormatterDir(String schema) {
        return this.schemaManager.getSchemaDir(schema).resolve(SCHEMA_PLUGIN_FORMATTER_DIR);
    }

    private GroovyClassLoader getParentClassLoader(FormatterParams fparams, String schema, Path baseShared, Path schemaShared) throws IOException,
            ResourceException, ScriptException {
        GroovyClassLoader cl = this.schemaClassLoaders.get(schema);
        if (fparams.isDevMode() || cl == null) {
            final GroovyClassLoader parent;
            ConfigFile newConfig = new ConfigFile(getSchemaPluginFormatterDir(schema), false, null);
            final String dependOnSchema = newConfig.dependOn();
            if (dependOnSchema != null) {
                Path dependent = getSchemaPluginFormatterDir(dependOnSchema).resolve(GROOVY_SCRIPT_ROOT);
                parent = getParentClassLoader(fparams, dependOnSchema, baseShared, dependent);
            } else {
                if (fparams.isDevMode() || this.baseClassLoader == null) {
                    URL[] roots = new URL[]{IO.toURL(baseShared)};
                    GroovyScriptEngine groovyScriptEngine = new GroovyScriptEngine(roots);
                    loadScripts(baseShared, groovyScriptEngine);
                    this.baseClassLoader = groovyScriptEngine.getGroovyClassLoader();
                }
                parent = this.baseClassLoader;
            }

            URL[] roots = new URL[]{IO.toURL(schemaShared)};
            GroovyScriptEngine groovyScriptEngine = new GroovyScriptEngine(roots, parent);

            loadScripts(schemaShared, groovyScriptEngine);
            cl = groovyScriptEngine.getGroovyClassLoader();
            this.schemaClassLoaders.put(schema, cl);
        }

        return cl;
    }

    private void loadScripts(Path baseShared, final GroovyScriptEngine gse) throws ResourceException, ScriptException, IOException {
        if (!Files.exists(baseShared)) {
            return;
        }
        final Map<Path, Throwable> compileErrors = Maps.newHashMap();
        Files.walkFileTree(baseShared, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                try (DirectoryStream<Path> paths = Files.newDirectoryStream(dir, "*.groovy")) {
                    for (Path path : paths) {
                        if (!Files.isDirectory(path)) {
                            try {
                                gse.loadScriptByName(path.toUri().toString());
                            } catch (CompilationFailedException e) {
                                compileErrors.put(path, null);
                            } catch (ScriptException  | ResourceException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });

        int numErrors = 0;

        while (numErrors != compileErrors.size() && compileErrors.size() > 0) {
            numErrors = compileErrors.size();
            Iterator<Path> iter = compileErrors.keySet().iterator();
            while (iter.hasNext()) {
                Path file = iter.next();
                try {
                    gse.loadScriptByName(file.toUri().toString());
                    iter.remove();
                } catch (CompilationFailedException e) {
                    compileErrors.put(file, e);
                    // skip
                }
            }

            if (!compileErrors.isEmpty()) {
                StringBuilder errorMsg = new StringBuilder("Errors occurred while compiling files:");

                for (Map.Entry<Path, Throwable> entry : compileErrors.entrySet()) {
                    errorMsg.append("\n\n").append(entry.getKey()).append(":\n").append(entry.getValue().getMessage());
                }

                throw new AssertionError(errorMsg.toString());
            }
        }
    }
}
