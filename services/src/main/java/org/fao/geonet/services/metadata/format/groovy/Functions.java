package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.io.Closer;
import groovy.lang.Closure;
import groovy.util.IndentPrinter;
import groovy.util.slurpersupport.GPathResult;
import groovy.xml.MarkupBuilder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.services.metadata.format.ConfigFile;
import org.fao.geonet.services.metadata.format.FormatterConstants;
import org.fao.geonet.services.metadata.format.FormatterParams;
import org.fao.geonet.services.metadata.format.SchemaLocalizations;
import org.fao.geonet.util.LangUtils;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Contains several functions and properties which can be used when implementing view.groovy formatter files (or the helper classes).
 *
 * @author Jesse on 10/16/2014.
 */
public class Functions extends SchemaLocalizations {

    private final Environment env;
    private final FormatterParams fparams;
    private SchemaPlugin schemaPlugin;

    public Functions(FormatterParams fparams, Environment env) throws Exception {
        super(fparams.context.getApplicationContext(), env, fparams.schema, fparams.config.dependOn());

        this.env = env;
        this.schemaPlugin = SchemaManager.getSchemaPlugin(fparams.context, fparams.schema);
        this.fparams = fparams;
    }

    /**
     * Creates a groovy.xml.MarkupBuilder object and executes the closure with the MarkupBuilder as its parameter.
     *
     * The xml created with the MarkupBuilder is returned as a string.
     *
     * @param htmlFunction function that uses the MarkupBuilder to create xml or html
     */
    public String html(Closure htmlFunction) {
        Closer closer = Closer.create();
        try {
            final StringWriter writer = closer.register(new StringWriter());
            final IndentPrinter indentPrinter = new IndentPrinter(writer, " ", true, false);
            MarkupBuilder html = new MarkupBuilder(indentPrinter);
            htmlFunction.call(html);
            return writer.toString();
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
                throw new Error(e);
            }
        }
    }

    /**
     * Obtain strings from the formatter, the formatter's schema shared translations or web/src/main/webapp/loc/???/formatter.xml .
     *
     * @param key the key to use for looking up the translation
     */
    public String translate(String key) throws Exception {
        return translate(key, null);
    }
    /**
     * Obtain strings from the formatter, the formatter's schema shared translations or web/src/main/webapp/loc/???/formatter.xml.
     *
     * @param key the key to use for looking up the translation
     * @param file the name of the xml file to get the strings from (without the .xml).  This may be null in which case all files will
     *             be searched. if null then for the shared translations the formatter.xml file will be used
     *            (web/src/main/webapp/loc/???/formatter.xml) and all the formatter translation files will be searched.
     *             If this is non-null then the formatter translation file will be used if found in the file and the method will
     *             fallback to searching web/src/main/webapp/loc/???/&amp;file>.xml
     */
    public String translate(String key, String file) throws Exception {
        String value = translateFromFormatterResources(key, file);
        if (value == null || value.isEmpty()) {
            value = translateFromSchema(fparams.schemaDir, key, file);
        }
        if (value == null || value.isEmpty()) {
            value = translateFromShared(key, file == null ? "formatter" : file);
        }
        if (value == null || value.isEmpty()) {
            return key;
        }
        return value;
    }

    private String translateFromSchema(Path schemaDir, String key, String file) throws Exception {
        final Path formatterDir = schemaDir.resolve(FormatterConstants.SCHEMA_PLUGIN_FORMATTER_DIR);
        final Element translations = getFormatterTranslations(formatterDir);
        Element currLanTranslations = translations.getChild(env.getLang3());
        String translation = translateFrom(key, file, currLanTranslations);
        if (translation == null) {
            final ConfigFile configFile = new ConfigFile(formatterDir, false, schemaDir);
            if (configFile.dependOn() != null) {
                final SchemaManager schemaManager = fparams.context.getBean(SchemaManager.class);
                final Path parentSchemaDir = schemaManager.getSchemaDir(configFile.dependOn());
                translation = translateFromSchema(parentSchemaDir, key, file);
            }
        }
        return translation;
    }

    private String translateFromFormatterResources(String key, String file) throws Exception {
        final Element translations = getFormatterTranslations(fparams.formatDir);
        Element currLanTranslations = translations.getChild(env.getLang3());
        return translateFrom(key, file, currLanTranslations);
    }

    private String translateFrom(String key, String file, Element currLanTranslations) {
        if (currLanTranslations == null) {
            return null;
        }
        String translation = null;
        if (file == null) {
            @SuppressWarnings("unchecked")
            List<Element> files = currLanTranslations.getChildren();
            for (Element element : files) {
                translation = element.getChildText(key);
                if (translation != null) {
                    break;
                }
            }
        } else {
            final Element fileTranslations = currLanTranslations.getChild(file);
            if (fileTranslations != null) {
                translation = fileTranslations.getChildText(key);
            }
        }
        return translation;
    }

    protected String translateFromShared(String key, String file) throws JDOMException, IOException {
        final Map<String, String> translations = LangUtils.translate(fparams.context.getApplicationContext(), file, key);
        String value = translations.get(env.getLang3());
        if (value == null) {
            value = translations.get(Geonet.DEFAULT_LANGUAGE);
        }
        if (value == null && !translations.isEmpty()) {
            value = translations.values().iterator().next();
        }
        return value;
    }

    public SchemaPlugin getSchemaPlugin() {
        return this.schemaPlugin;
    }

    private Element getFormatterTranslations(Path dir) throws Exception {
       return fparams.format.getPluginLocResources(fparams.context, dir);
    }

    public String getXPathFrom(GPathResult path) {
        if (path == null) {
            return "";
        }

        final GPathResult parent = path.parent();
        if (parent == path) {
            return "";
        }

        StringBuilder currPath = new StringBuilder(getXPathFrom(parent));
        if (currPath.length() > 0) {
            currPath.append("/");
        }
        currPath.append(path.name());

        final GPathResult singlingsAndSelf = (GPathResult) parent.getProperty(path.name());
        if (singlingsAndSelf.size() > 1) {
            final int i = singlingsAndSelf.list().indexOf(path);
            currPath.append('[').append(i + 1).append(']');
        }

        return currPath.toString();
    }
}
