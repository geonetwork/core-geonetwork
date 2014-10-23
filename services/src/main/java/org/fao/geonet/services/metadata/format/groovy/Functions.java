package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import com.vividsolutions.jts.util.Assert;
import groovy.lang.Closure;
import groovy.util.IndentPrinter;
import groovy.util.slurpersupport.GPathResult;
import groovy.xml.MarkupBuilder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.IsoLanguage;
import org.fao.geonet.repository.IsoLanguageRepository;
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
     * Localization files from schema plugin
     */
    private final SchemaLocalization schemaLocalizations;
    private final IsoLanguageRepository languageRepo;
    private final Environment env;

    public Functions(FormatterParams fparams, Environment env, IsoLanguageRepository languageRepo) throws Exception {
        this.languageRepo = languageRepo;
        this.schemaLocalizations = fparams.getSchemaLocalizations().get(fparams.schema);
        this.env = env;
    }


    /**
     * Obtain a translation for the given node by looking up the elements name in the the schema's labels.xml file
     * @param node the node to get a translation for.
     */
    public String nodeLabel(GPathResult node) throws Exception {
        return nodeLabel(node.name());
    }

    /**
     * Look up a translation in the schema's labels.xml file
     * @param qualifiedNodeName the name to use as a key for the lookup
     */
    public String nodeLabel(String qualifiedNodeName) throws Exception {
        String label = nodeTranslation(qualifiedNodeName, "label");

        if (label == null) {
            label = qualifiedNodeName;
        }

        return label;
    }

    /**
     * Obtain the description for the given node by looking up the elements name in the the schema's labels.xml file
     * @param node the node to get a description for.
     */
    public String nodeDesc(GPathResult node) throws Exception {
        return nodeDesc(node.name());
    }

    /**
     * Look up a description in the schema's labels.xml file
     * @param qualifiedNodeName the name to use as a key for the lookup
     */
    public String nodeDesc(String qualifiedNodeName) throws Exception {
        String label = nodeTranslation(qualifiedNodeName, "description");

        if (label == null) {
            label = qualifiedNodeName;
        }

        return label;
    }

    private String nodeTranslation(String qualifiedNodeName, String type) throws Exception {
        @SuppressWarnings("unchecked")
        final List<Element> children = this.schemaLocalizations.getLabels(this.env.getLang3()).getChildren("element");
        for (Element child : children) {
            if (qualifiedNodeName.equals(child.getAttributeValue("name"))) {
                return child.getChildText(type);
            }
        }
        return null;
    }

    /**
     * Obtain a translation for the given codelist by looking up the codelist and codelist value in the the schema's codelists.xml file
     * * @param node a node containing a codeListValue attribute and a codeList attribute
     */
    public String codelistValueLabel(GPathResult node) throws Exception {
        return codelistValueLabel(node.getProperty("@codeList").toString(), node.getProperty("@codeListValue").toString());
    }

    /**
     * Obtain a translation for the given codelist by looking up the codelist and codelist value in the the schema's codelists.xml file.
     * @param codelist the name of the codelist
     * @param value the codelist value
     */
    public String codelistValueLabel(String codelist, String value) throws Exception {
        String label = codelistTranslation(codelist, value, "label");

        if (label == null) {
            label = value;
        }

        return label;
    }

    /**
     * Obtain the description for the given codelist by looking up the codelist and codelist value in the the schema's codelists.xml file.
     * @param node a node containing a codeListValue attribute and a codeList attribute
     */
    public String codelistValueDesc(GPathResult node) throws Exception {
        return codelistValueDesc(node.getProperty("@codeList").toString(), node.getProperty("@codeListValue").toString());
    }

    /**
     * Obtain the description for the given codelist by looking up the codelist and codelist value in the the schema's codelists.xml file.
     *
     * @param codelist the name of the codelist
     * @param value the codelist value
     */
    public String codelistValueDesc(String codelist, String value) throws Exception {
        String label = codelistTranslation(codelist, value, "description");

        if (label == null) {
            label = value;
        }

        return label;
    }

    private String codelistTranslation(String codelist, String value, String type) throws Exception {

        if ("http://www.loc.gov/standards/iso639-2/".equals(codelist)) {
            return translateLanguageCode(value);
        }

        codelist = extractCodeListName(codelist);

        @SuppressWarnings("unchecked")
        final List<Element> children = this.schemaLocalizations.getCodelists(this.env.getLang3()).getChildren("codelist");
        for (Element child : children) {
            String codeListNameFromLabel = extractCodeListNameFromXml(child);

            if (codelist.equals(codeListNameFromLabel)) {
                @SuppressWarnings("unchecked")
                final List<Element> values = child.getChildren("entry");
                for (Element labelEl : values) {
                    String code = labelEl.getChildText("code");
                    if (value.equals(code)) {
                        return labelEl .getChildText(type);
                    }
                }
                break;
            }
        }
        return null;
    }

    private String translateLanguageCode(String value) {
        if (value == null) {
            return null;
        }
        List<IsoLanguage> lang;
        if (value.length() == 2) {
            lang = this.languageRepo.findAllByShortCode(value.toLowerCase());
        } else {
            lang = this.languageRepo.findAllByCode(value.toLowerCase());
        }

        if (!lang.isEmpty()) {
            final IsoLanguage isoLanguage = lang.get(0);
            String label = isoLanguage.getLabel(env.getLang3());
            if (label == null) {
                label = isoLanguage.getLabel(Geonet.DEFAULT_LANGUAGE);
            }
            if (label == null) {
                label = value;
            }

            return label;
        }
        return value;
    }

    private String extractCodeListNameFromXml(Element child) {
        String codeListNameFromLabel = child.getAttributeValue("name");
        int endOfPrefix = codeListNameFromLabel.indexOf(":");
        if (endOfPrefix > 0) {
            codeListNameFromLabel = codeListNameFromLabel.substring(endOfPrefix + 1);
        }
        return codeListNameFromLabel;
    }

    private String extractCodeListName(String codelist) {
        final int indexOfPound = codelist.lastIndexOf('#');
        if (indexOfPound > 0) {
            codelist = codelist.substring(indexOfPound + 1);
        }
        return codelist;
    }

    public Collection<String> codelist(String codelistName) throws Exception {
        List<String> codelistValues = Lists.newArrayList();

        @SuppressWarnings("unchecked")
        final List<Element> children = this.schemaLocalizations.getCodelists(this.env.getLang3()).getChildren("codelist");
        for (Element child : children) {
            if (codelistName.equals(child.getAttributeValue("name"))) {
                @SuppressWarnings("unchecked")
                final List<Element> entries = child.getChildren("entry");
                for (Element entry : entries) {
                    codelistValues.add(entry.getChildText("code"));
                }
                break;
            }
        }
        return codelistValues;
    }

    /**
     * Translate a string in the schema's strings.xml file.  Each element in the key array is one level deep in the xml tree.
     *
     * If there are two elements with the same name the second element will be ignored.
     *
     * @param key the lookup key of the codelist
     */
    public String schemaString(String... key) throws Exception {
        Assert.isTrue(key.length > 0, "There must be at least one key value");

        Element strings = this.schemaLocalizations.getStrings(this.env.getLang3());
        for (int i = 0; i < key.length; i++) {
            strings = strings.getChild(key[i]);
            if (strings == null) {
                return "[" + Joiner.on(',').join(key) + "]";
            }
        }

        return strings.getTextNormalize();
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

}
