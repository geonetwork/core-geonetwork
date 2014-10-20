package org.fao.geonet.services.metadata.format;

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

import static org.fao.geonet.services.metadata.format.FormatterConstants.GROOVY_SCRIPT_ROOT;
import static org.fao.geonet.services.metadata.format.FormatterConstants.SCHEMA_PLUGIN_FORMATTER_DIR;

/**
 * Formatter for groovy dialect of formatters.
 *
 * @author Jesse on 10/15/2014.
 */
@Component
public class GroovyFormatter {

    @Autowired
    GeonetworkDataDirectory dataDirectory;
    @Autowired
    SchemaManager schemaManager;
    @Autowired
    private IsoLanguagesMapper mapper;
    @Autowired
    private TemplateCache templateCache;
    private final Cache<String, Transformer> transformers = CacheBuilder.newBuilder().
            concurrencyLevel(1).
            maximumSize(40).
            initialCapacity(40).build();
    private GroovyClassLoader baseClassLoader;
    private Map<String, GroovyClassLoader> schemaClassLoaders = Maps.newHashMap();

    public String format(FormatterParams fparams) throws Exception {
        final Transformer transformer = createTransformer(fparams);

        EnvironmentProxy.setCurrentEnvironment(fparams, this.mapper);
        final List<Namespace> namespaces = this.schemaManager.getSchema(fparams.schema).getNamespaces();
        return transformer.apply(fparams.metadata, namespaces);
    }

    private synchronized Transformer createTransformer(FormatterParams fparams) throws Exception {
        final String formatDirPath = fparams.formatDir.getPath();
        Transformer transformer = this.transformers.getIfPresent(formatDirPath);
//        Transformer transformer;
        if (transformer == null) {
            final File baseShared = new File(this.dataDirectory.getFormatterDir(), GROOVY_SCRIPT_ROOT);
            final File schemaFormatterDir = new File(this.schemaManager.getSchemaDir(fparams.schema), SCHEMA_PLUGIN_FORMATTER_DIR);
            final File schemaShared = new File(schemaFormatterDir, GROOVY_SCRIPT_ROOT);
            GroovyClassLoader cl = getParentClassLoader(fparams, baseShared, schemaShared);

            String[] roots = new String[]{
                    fparams.formatDir.getAbsoluteFile().toURI().toString()
            };
            GroovyScriptEngine groovyScriptEngine = new GroovyScriptEngine(roots, cl);

            loadScripts(fparams.formatDir, groovyScriptEngine);

            Handlers handlers = new Handlers(fparams, schemaShared.getParentFile(), baseShared.getParentFile(), this.templateCache);
            Functions functions = new Functions(fparams);
            Environment env = new EnvironmentProxy();
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

    private GroovyClassLoader getParentClassLoader(FormatterParams fparams, File baseShared, File schemaShared) throws IOException,
            ResourceException, ScriptException {
        GroovyClassLoader cl = this.schemaClassLoaders.get(fparams.schema);
        if (cl == null) {
            String[] roots = new String[]{baseShared.toURI().toString()};
            GroovyScriptEngine groovyScriptEngine = new GroovyScriptEngine(roots);
            loadScripts(baseShared, groovyScriptEngine);
            this.baseClassLoader = groovyScriptEngine.getGroovyClassLoader();

            roots = new String[]{schemaShared.toURI().toString()};
            groovyScriptEngine = new GroovyScriptEngine(roots, this.baseClassLoader);


            loadScripts(schemaShared, groovyScriptEngine);
            cl = groovyScriptEngine.getGroovyClassLoader();
            this.schemaClassLoaders.put(fparams.schema, cl);
        }

        return cl;
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
