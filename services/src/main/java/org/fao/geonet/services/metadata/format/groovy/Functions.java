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
import java.util.Collection;
import java.util.List;

/**
 * Contains several functions and properties which can be used when implementing view.groovy formatter files (or the helper classes).
 *
 * @author Jesse on 10/16/2014.
 */
public class Functions {
    /**
     * 3 letter language code of the UI.
     */
    public final String lang3;
    /**
     * 2 letter language code of the UI.
     */
    public final String lang2;
    /**
     * The URL to use in the html when accessing resources in the formatter resource package.  Simply append the relative path of the
     * resource from the formatter bundle directory.
     */
    public final String resourceUrl;
    private final IsoLanguagesMapper mapper;
    /**
     * Localization files from schema plugin
     */
    private final SchemaLocalization schemaLocalizations;
    /**
     * localization files from formatter bundle's loc/lang directory.
     */
    private final Element formatterLocResources;
    private final Element defaultFormatterLocResources;
    private final Multimap<String, ParamValue> params = ArrayListMultimap.create();

    public Functions(FormatterParams fparams, IsoLanguagesMapper mapper) throws Exception {
        this.lang3 = fparams.context.getLanguage();
        this.lang2 = mapper.iso639_2_to_iso639_1(lang3, "en");

        this.resourceUrl = fparams.getLocUrl();
        this.mapper = mapper;
        this.schemaLocalizations = fparams.getSchemaLocalizations().get(fparams.schema);
        this.formatterLocResources = fparams.getPluginLocResources(fparams.context.getLanguage());
        this.defaultFormatterLocResources = fparams.getPluginLocResources(Geonet.DEFAULT_LANGUAGE);

        @SuppressWarnings("unchecked")
        final List<Element> paramEls = fparams.params.getChildren();

        for (Element paramEl : paramEls) {
            this.params.put(paramEl.getName(), new ParamValue(paramEl.getTextTrim()));
        }

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

    /**
     * Return the map of all parameters passed to the Format service.
     */
    public Multimap<String, ParamValue> params() {
        return this.params;
    }

    /**
     * Return the value of the first parameter with the provided name.  Null is returned if there is no parameter with the given name.
     */
    public ParamValue param(String paramName) {
        final Collection<ParamValue> paramValues = this.params.get(paramName);
        if (paramValues.isEmpty()) {
            return null;
        }
        return paramValues.iterator().next();
    }
    /**
     * Return ALL values of parameter with the provided name.
     */
    public Collection<ParamValue> paramValues(String paramName) {
        return this.params.get(paramName);
    }
}
