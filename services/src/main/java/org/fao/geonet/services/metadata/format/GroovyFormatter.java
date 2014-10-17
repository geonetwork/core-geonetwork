package org.fao.geonet.services.metadata.format;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.codehaus.groovy.control.CompilationFailedException;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.metadata.format.groovy.Functions;
import org.fao.geonet.services.metadata.format.groovy.Handlers;
import org.fao.geonet.services.metadata.format.groovy.Transformer;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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

    public Element format(FormatterParams fparams) throws Exception {
        final Transformer transformer = createTransformer(fparams);

        final List<Namespace> namespaces = this.schemaManager.getSchema(fparams.schema).getNamespaces();
        return transformer.apply(fparams.metadata, namespaces);
    }

    private Transformer createTransformer(FormatterParams fparams) throws Exception {
        final File baseShared = new File(this.dataDirectory.getFormatterDir(), GROOVY_SCRIPT_ROOT);
        final File schemaFormatterDir = new File(this.schemaManager.getSchemaDir(fparams.schema), SCHEMA_PLUGIN_FORMATTER_DIR);
        final File schemaShared = new File(schemaFormatterDir, GROOVY_SCRIPT_ROOT);
        String[] roots = new String[] {
                fparams.formatDir.getAbsoluteFile().toURI().toString(),
                baseShared.toURI().toString(),
                schemaShared.toURI().toString(),
        };
        GroovyScriptEngine gse = new GroovyScriptEngine(roots);

        Handlers handlers = new Handlers(fparams, schemaShared.getParentFile(), baseShared.getParentFile());
        Functions functions = new Functions(fparams, this.mapper);
        Binding binding = new Binding();
        binding.setVariable("handlers", handlers);
        binding.setVariable("f", functions);

        loadScripts(baseShared, gse);
        loadScripts(schemaShared, gse);
        loadScripts(fparams.formatDir, gse);

        final String scriptName = fparams.viewFile.getAbsoluteFile().toURI().toString();
        gse.run(scriptName, binding);

        return new Transformer(handlers, fparams.formatDir.getAbsolutePath());

    }

    private void loadScripts(File baseShared, GroovyScriptEngine gse) throws ResourceException, ScriptException {
        Collection<File> compileErrors = Lists.newArrayList();
        for (File file : Files.fileTreeTraverser().breadthFirstTraversal(baseShared)) {
            if (file.isFile() && file.getName().endsWith(".groovy") && !file.getName().equals(FormatterConstants.VIEW_GROOVY_FILENAME)) {
                try {
                    gse.loadScriptByName(file.toURI().toString());
                } catch (CompilationFailedException e) {
                    compileErrors.add(file);
                }
            }
        }

        int numErrors = 0;

        while (numErrors != compileErrors.size() && compileErrors.size() > 0) {
            numErrors = compileErrors.size();
            Iterator<File> iter = compileErrors.iterator();
            while (iter.hasNext()) {
                File file = iter.next();
                try {
                    gse.loadScriptByName(file.toURI().toString());
                    iter.remove();
                } catch (CompilationFailedException e) {
                    // skip
                }
            }
        }
    }

}
