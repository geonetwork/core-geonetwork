package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Closer;
import com.vividsolutions.jts.util.Assert;
import groovy.lang.Closure;
import groovy.util.IndentPrinter;
import groovy.util.slurpersupport.GPathResult;
import groovy.xml.MarkupBuilder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.IsoLanguage;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.repository.IsoLanguageRepository;
import org.fao.geonet.services.metadata.format.ConfigFile;
import org.fao.geonet.services.metadata.format.FormatterParams;
import org.fao.geonet.services.metadata.format.SchemaLocalization;
import org.jdom.Element;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains several functions and properties which can be used when implementing view.groovy formatter files (or the helper classes).
 *
 * @author Jesse on 10/16/2014.
 */
public class Functions {
    protected static final String LANG_CODELIST_NS = "http://www.loc.gov/standards/iso639-2/";
    /**
     * Localization files from schema plugin and the parents.  each file should be tried from the first to last.
     */
    private final List<SchemaLocalization> schemaLocalizations;
    private final IsoLanguageRepository languageRepo;
    private final Environment env;

    public Functions(FormatterParams fparams, Environment env, IsoLanguageRepository languageRepo, SchemaManager schemaManager) throws Exception {
        this.languageRepo = languageRepo;
        ArrayList<SchemaLocalization> tmpLocalizations = Lists.newArrayList();
        final Map<String, SchemaLocalization> allLocalizations = fparams.getSchemaLocalizations();
        tmpLocalizations.add(allLocalizations.get(fparams.schema));
        addParentLocalizations(schemaManager, allLocalizations, tmpLocalizations, fparams.config);
        this.schemaLocalizations = Collections.unmodifiableList(tmpLocalizations);
        this.env = env;
    }

    private void addParentLocalizations(SchemaManager schemaManager, Map<String, SchemaLocalization> allLocalizations, ArrayList<SchemaLocalization> tmpLocalizations, ConfigFile config) throws IOException {
        if (config.dependOn() == null) {
            return;
        }

        final SchemaLocalization schemaLocalization = allLocalizations.get(config.dependOn());
        if (schemaLocalization != null) {
            tmpLocalizations.add(schemaLocalization);
        }

        final File schemaDir = new File(schemaManager.getSchemaDir(config.dependOn()));
        final ConfigFile parentConfig = new ConfigFile(schemaDir, false, null);
        addParentLocalizations(schemaManager, allLocalizations, tmpLocalizations, parentConfig);
    }


    /**
     * Obtain a translation for the given node by looking up the elements name in the the schema's labels.xml file
     * @param node the node to get a translation for.
     */
    public String nodeLabel(GPathResult node) throws Exception {
        String parentNodeName = parentNodeName(node);
        return nodeLabel(node.name(), parentNodeName);
    }

    /**
     * Look up a translation in the schema's labels.xml file
     * @param qualifiedNodeName the name to use as a key for the lookup
     * @param qualifiedParentNodeName the name of the parent, used as the second lookup key.  This can be null and the default value will
     *                                be returned
     */
    public String nodeLabel(String qualifiedNodeName, String qualifiedParentNodeName) throws Exception {
        return nodeTranslation(qualifiedNodeName, qualifiedParentNodeName, "label");
    }

    /**
     * Obtain the description for the given node by looking up the elements name in the the schema's labels.xml file
     * @param node the node to get a description for.
     */
    public String nodeDesc(GPathResult node) throws Exception {
        String parentNodeName = parentNodeName(node);
        return nodeDesc(node.name(), parentNodeName);
    }

    protected String parentNodeName(GPathResult node) {
        GPathResult parentNode = node.parent();
        String parentNodeName = null;
        if (parentNode != node) {
            parentNodeName = parentNode.name();
        }
        return parentNodeName;
    }

    /**
     * Look up a description in the schema's labels.xml file
     * @param qualifiedNodeName the name to use as a key for the lookup
     * @param qualifiedParentNodeName the name of the parent, used as the second lookup key.  This can be null and the default value will
     *                                be returned
     */
    public String nodeDesc(String qualifiedNodeName, String qualifiedParentNodeName) throws Exception {

        return nodeTranslation(qualifiedNodeName, qualifiedParentNodeName, "description");
    }

    private String nodeTranslation(String qualifiedNodeName, String qualifiedParentNodeName,  String type) throws Exception {
        if (qualifiedParentNodeName == null) {
            qualifiedParentNodeName = "";
        }

        for (SchemaLocalization schemaLocalization : this.schemaLocalizations) {
            final ImmutableTable<String, String, Element> labelIndex = schemaLocalization.getLabelIndex(this.env.getLang3());
            Element element = labelIndex.get(qualifiedNodeName, qualifiedParentNodeName);
            if (element == null) {
                element = labelIndex.get(qualifiedNodeName, "");
            }
            if (element == null) {
                final ImmutableCollection<Element> values = labelIndex.row(qualifiedNodeName).values();
                if (!values.isEmpty()) {
                    element = values.iterator().next();
                }
            }
            if (element != null) {
                return element.getChildText(type);
            }
        }


        return qualifiedNodeName;
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
        return codelistTranslation(codelist, value, "label");
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
        return codelistTranslation(codelist, value, "description");
    }

    private String codelistTranslation(String codelist, String value, String type) throws Exception {

        if (LANG_CODELIST_NS.equals(codelist)) {
            return translateLanguageCode(value);
        }

        codelist = extractCodeListName(codelist);

        for (SchemaLocalization schemaLocalization : this.schemaLocalizations) {
            Element codelistEl = schemaLocalization.getCodeListIndex(this.env.getLang3()).get(codelist, value);
            if (codelistEl != null) {
                return codelistEl.getChildText(type);
            }
        }
        return value;
    }

    private String translateLanguageCode(String value) {
        if (value == null) {
            return null;
        }
        List<IsoLanguage> lang;
        if (value.equals("deu")) {
            value = "ger";
        }

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

    private String extractCodeListName(String codelist) {
        final int indexOfPound = codelist.lastIndexOf('#');
        if (indexOfPound > 0) {
            codelist = codelist.substring(indexOfPound + 1);
        }
        return codelist;
    }

    public Collection<String> codelist(String codelistName) throws Exception {
        int prefix = codelistName.indexOf(':');
        if (prefix > -1) {
            codelistName = codelistName.substring(prefix + 1);
        }

        Set<String> codelists = Sets.newHashSet();
        for (SchemaLocalization schemaLocalization : this.schemaLocalizations) {
            final ImmutableTable<String, String, Element> codeListIndex = schemaLocalization.getCodeListIndex(this.env.getLang3());
            codelists.addAll(codeListIndex.row(codelistName).keySet());
        }
        return codelists;
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


        for (SchemaLocalization schemaLocalization : this.schemaLocalizations) {
            Element strings = schemaLocalization.getStrings(this.env.getLang3());
            for (int i = 0; i < key.length; i++) {
                strings = strings.getChild(key[i]);
                if (strings == null) {
                    break;
                }
            }
            if (strings != null) {
                return strings.getTextNormalize();
            }
        }

        return "[" + Joiner.on(',').join(key) + "]";
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
