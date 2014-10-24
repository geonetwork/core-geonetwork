package org.fao.geonet.services.metadata.format;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
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
import org.fao.geonet.services.metadata.format.groovy.TemplateCache;
import org.fao.geonet.services.metadata.format.groovy.Transformer;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
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
    private final Cache<String, Transformer> transformers = CacheBuilder.newBuilder().
            concurrencyLevel(1).
            maximumSize(40).
            initialCapacity(40).build();
    private GroovyClassLoader baseClassLoader;
    private Map<String, GroovyClassLoader> schemaClassLoaders = Maps.newHashMap();

    @VisibleForTesting
    Transformer findTransformer(final FormatterParams fparams) throws ExecutionException {
        return transformers.get(fparams.formatDir.getPath(), new Callable<Transformer>() {
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
        final String formatDirPath = fparams.formatDir.getPath();
        Transformer transformer = this.transformers.getIfPresent(formatDirPath);
//        Transformer transformer;
        if (fparams.isDevMode() || transformer == null) {
            final File baseShared = new File(this.dataDirectory.getFormatterDir(), GROOVY_SCRIPT_ROOT);
            final File schemaFormatterDir = getSchemaPluginFormatterDir(fparams.schema);
            final File schemaShared = new File(schemaFormatterDir, GROOVY_SCRIPT_ROOT);
            GroovyClassLoader cl = getParentClassLoader(fparams, baseShared, schemaShared);

            String[] roots = new String[]{
                    fparams.formatDir.getAbsoluteFile().toURI().toString()
            };
            GroovyScriptEngine groovyScriptEngine = new GroovyScriptEngine(roots, cl);

            loadScripts(fparams.formatDir, groovyScriptEngine);

            Handlers handlers = new Handlers(fparams, schemaShared.getParentFile(), baseShared.getParentFile(), this.templateCache);
            Environment env = new EnvironmentProxy();
            Functions functions = new Functions(fparams, env, isoLanguageRepository);
            Binding binding = new Binding();
            binding.setVariable("handlers", handlers);
            binding.setVariable("env", env);
            binding.setVariable("f", functions);

            final String scriptName = fparams.viewFile.getAbsoluteFile().toURI().toString();
            groovyScriptEngine.run(scriptName, binding);

            transformer = new Transformer(handlers, fparams.formatDir.getAbsolutePath());
            this.transformers.put(formatDirPath, transformer);
        }

        return transformer;
    }

    private File getSchemaPluginFormatterDir(String schema) {
        return new File(this.schemaManager.getSchemaDir(schema), SCHEMA_PLUGIN_FORMATTER_DIR);
    }

    private GroovyClassLoader getParentClassLoader(FormatterParams fparams, File baseShared, File schemaShared) throws IOException,
            ResourceException, ScriptException {
        GroovyClassLoader cl = this.schemaClassLoaders.get(fparams.schema);
        if (fparams.isDevMode() || cl == null) {
            final GroovyClassLoader parent;
            final String dependOnSchema = fparams.config.dependOn();
            if (dependOnSchema != null) {
                File dependent = new File(getSchemaPluginFormatterDir(dependOnSchema), GROOVY_SCRIPT_ROOT);
                parent = getParentClassLoader(createParamsForSchema(fparams, dependOnSchema), baseShared, dependent);
            } else {
                if (fparams.isDevMode() || this.baseClassLoader == null) {
                    String[] roots = new String[]{baseShared.toURI().toString()};
                    GroovyScriptEngine groovyScriptEngine = new GroovyScriptEngine(roots);
                    loadScripts(baseShared, groovyScriptEngine);
                    this.baseClassLoader = groovyScriptEngine.getGroovyClassLoader();
                }
                parent = this.baseClassLoader;
            }
            String[] roots = new String[]{schemaShared.toURI().toString()};
            GroovyScriptEngine groovyScriptEngine = new GroovyScriptEngine(roots, parent);


            loadScripts(schemaShared, groovyScriptEngine);
            cl = groovyScriptEngine.getGroovyClassLoader();
            this.schemaClassLoaders.put(fparams.schema, cl);
        }

        return cl;
    }

    private FormatterParams createParamsForSchema(FormatterParams fparams, String schema) throws IOException {
        ConfigFile newConfig = new ConfigFile(getSchemaPluginFormatterDir(schema), false);
        final FormatterParams formatterParams = fparams.copy();
        formatterParams.config = newConfig;
        return formatterParams;
    }

    private void loadScripts(File baseShared, GroovyScriptEngine gse) throws ResourceException, ScriptException {
        Map<File, Throwable> compileErrors = Maps.newHashMap();
        for (File file : Files.fileTreeTraverser().breadthFirstTraversal(baseShared)) {
            if (file.isFile() && file.getName().endsWith(".groovy") && !file.getName().equals(FormatterConstants.VIEW_GROOVY_FILENAME)) {
                try {
                    gse.loadScriptByName(file.toURI().toString());
                } catch (CompilationFailedException e) {
                    compileErrors.put(file, null);
                }
            }
        }

        int numErrors = 0;

        while (numErrors != compileErrors.size() && compileErrors.size() > 0) {
            numErrors = compileErrors.size();
            Iterator<File> iter = compileErrors.keySet().iterator();
            while (iter.hasNext()) {
                File file = iter.next();
                try {
                    gse.loadScriptByName(file.toURI().toString());
                    iter.remove();
                } catch (CompilationFailedException e) {
                    compileErrors.put(file, e);
                    // skip
                }
            }

            if (!compileErrors.isEmpty()) {
                StringBuilder errorMsg = new StringBuilder("Errors occurred while compiling files:");

                for (Map.Entry<File, Throwable> entry : compileErrors.entrySet()) {
                    errorMsg.append("\n\n").append(entry.getKey()).append(":\n").append(entry.getValue().getMessage());
                }

                throw new AssertionError(errorMsg.toString());
            }
        }
    }
}
