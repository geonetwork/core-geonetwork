package org.fao.geonet.services.metadata.format;

import com.google.common.io.Files;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
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
import java.util.List;

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
        final File baseShared = new File(this.dataDirectory.getFormatterDir(), FormatterConstants.GROOVY_SCRIPT_ROOT);
        final File schemaShared = new File(this.schemaManager.getSchemaDir(fparams.schema), FormatterConstants.GROOVY_SCRIPT_ROOT);
        String[] roots = new String[] {
                fparams.formatDir.getAbsoluteFile().toURI().toString(),
                baseShared.toURI().toString(),
                schemaShared.toURI().toString(),
        };
        GroovyScriptEngine gse = new GroovyScriptEngine(roots);

        Handlers handlers = new Handlers(fparams.formatDir);
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
        for (File file : Files.fileTreeTraverser().breadthFirstTraversal(baseShared)) {
            if (file.isFile() && file.getName().endsWith(".groovy") && !file.getName().equals(FormatterConstants.VIEW_GROOVY_FILENAME)) {
                gse.loadScriptByName(file.toURI().toString());
            }
        }
    }

}
