package org.fao.geonet.services.metadata.format.groovy;

import groovy.util.slurpersupport.GPathResult;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.metadata.format.FormatterParams;
import org.fao.geonet.services.metadata.format.SchemaLocalization;
import org.jdom.Element;

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

    public Functions(FormatterParams fparams, IsoLanguagesMapper mapper) throws Exception {
        this.lang3 = fparams.context.getLanguage();
        this.lang2 = mapper.iso639_2_to_iso639_1(lang3, "en");

        this.resourceUrl = fparams.getLocUrl();
        this.mapper = mapper;
        this.schemaLocalizations = fparams.getSchemaLocalizations().get(fparams.schema);
        this.formatterLocResources = fparams.getPluginLocResources(fparams.context.getLanguage());
        this.defaultFormatterLocResources = fparams.getPluginLocResources(Geonet.DEFAULT_LANGUAGE);
    }

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

    public String label(GPathResult node) throws Exception {
        return label(node.name());
    }
}
