package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Closer;
import groovy.lang.Closure;
import groovy.util.slurpersupport.GPathResult;
import groovy.xml.MarkupBuilder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.metadata.format.FormatterParams;
import org.fao.geonet.services.metadata.format.SchemaLocalization;
import org.jdom.Element;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Contains several functions and properties which can be used when implementing view.groovy formatter files (or the helper classes).
 *
 * @author Jesse on 10/16/2014.
 */
public class Functions {
    /**
     * Localization files from schema plugin
     */
    private final SchemaLocalization schemaLocalizations;
    /**
     * localization files from formatter bundle's loc/lang directory.
     */
    private final Element formatterLocResources;
    private final Element defaultFormatterLocResources;

    public Functions(FormatterParams fparams) throws Exception {
        this.schemaLocalizations = fparams.getSchemaLocalizations().get(fparams.schema);
        this.formatterLocResources = fparams.getPluginLocResources(fparams.context.getLanguage());
        this.defaultFormatterLocResources = fparams.getPluginLocResources(Geonet.DEFAULT_LANGUAGE);
    }

    /**
     * Look up a translation in the schema's labels.xml file
     * @param qualifiedNodeName the name to use as a key for the lookup
     */
    public String label(String qualifiedNodeName) throws Exception {
        String label = label(qualifiedNodeName, this.schemaLocalizations);

        if (label == null) {
            label = qualifiedNodeName;
        }

        return label;
    }

    private String label(String qualifiedNodeName, SchemaLocalization localizations) throws Exception {
        @SuppressWarnings("unchecked")
        final List<Element> children = localizations.getLabels().getChildren();
        for (Element child : children) {
            if (child.getName().equals("element") && qualifiedNodeName.equals(child.getAttributeValue("name"))) {
                return child.getChildText("label");
            }
        }
        return null;
    }

    /**
     * Obtain a translation for the given node by looking up the elements name in the the schema's labels.xml file
     * @param node the node to get a translation for.
     */
    public String label(GPathResult node) throws Exception {
        return label(node.name());
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
            MarkupBuilder html = new MarkupBuilder(writer);
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

}
