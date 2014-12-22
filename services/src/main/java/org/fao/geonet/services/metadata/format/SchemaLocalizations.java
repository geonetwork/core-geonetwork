package org.fao.geonet.services.metadata.format;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.util.Assert;
import groovy.util.slurpersupport.GPathResult;
import jeeves.server.dispatchers.guiservices.XmlFile;
import jeeves.server.sources.ServiceRequestFactory;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.IsoLanguage;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.repository.IsoLanguageRepository;
import org.fao.geonet.services.metadata.format.groovy.CurrentLanguageHolder;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import static jeeves.config.springutil.JeevesDelegatingFilterProxy.getApplicationContextFromServletContext;

/**
 * Contains methods for efficiently accessing the translations in a schema's labels and codelists files.
 *
 * @author Jesse on 11/28/2014.
 */
public class SchemaLocalizations {
    public static final String LANG_CODELIST_NS = "http://www.loc.gov/standards/iso639-2/";
    /**
     * Localization files from schema plugin and the parents.  each file should be tried from the first to last.
     */
    private final List<SchemaLocalization> schemaLocalizations;
    private final CurrentLanguageHolder languageHolder;
    private final IsoLanguageRepository languageRepo;

    public SchemaLocalizations(ApplicationContext context, CurrentLanguageHolder languageHolder,
                               String schema, String extraSchemas) throws IOException, JDOMException {
        this.languageHolder = languageHolder;
        this.languageRepo = context.getBean(IsoLanguageRepository.class);

        ArrayList <SchemaLocalization> tmpLocalizations = Lists.newArrayList();
        final SchemaManager schemaManager = context.getBean(SchemaManager.class);
        final Map<String, SchemaLocalization> allLocalizations = getSchemaLocalizations(context, schemaManager);
        tmpLocalizations.add(allLocalizations.get(schema));
        addParentLocalizations(schemaManager, allLocalizations, tmpLocalizations, getConfigFile(schemaManager, schema).dependOn());
        addParentLocalizations(schemaManager, allLocalizations, tmpLocalizations, extraSchemas);
        this.schemaLocalizations = Collections.unmodifiableList(tmpLocalizations);
    }

    private void addParentLocalizations(SchemaManager schemaManager, Map<String, SchemaLocalization> allLocalizations,
                                        ArrayList<SchemaLocalization> tmpLocalizations, String dependantSchema) throws IOException {
        if (dependantSchema == null) {
            return;
        }

        final SchemaLocalization schemaLocalization = allLocalizations.get(dependantSchema);
        if (schemaLocalization != null) {
            tmpLocalizations.add(schemaLocalization);
        }

        final ConfigFile parentConfig = getConfigFile(schemaManager, dependantSchema);
        addParentLocalizations(schemaManager, allLocalizations, tmpLocalizations, parentConfig.dependOn());
    }

    @VisibleForTesting
    protected ConfigFile getConfigFile(SchemaManager schemaManager, String schema) throws IOException {
        final Path schemaDir = schemaManager.getSchemaDir(schema).resolve(FormatterConstants.SCHEMA_PLUGIN_FORMATTER_DIR);
        return new ConfigFile(schemaDir, false, null);
    }

    @VisibleForTesting
    protected Map<String, SchemaLocalization> getSchemaLocalizations(ApplicationContext context, SchemaManager schemaManager) throws
            IOException, JDOMException {
        return loadSchemaLocalizations(context, schemaManager);
    }
    /**
     * Get the strings.xml, codelists.xml and labels.xml for the correct language from the schema plugin
     *
     * @return Map(SchemaName, SchemaLocalizations)
     */
    static Map<String, SchemaLocalization> loadSchemaLocalizations(ApplicationContext context, SchemaManager schemaManager)
            throws IOException, JDOMException {

        Map<String, SchemaLocalization> localization = Maps.newHashMap();
        final Set<String> allSchemas = schemaManager.getSchemas();
        for (String schema : allSchemas) {
            Map<String, XmlFile> schemaInfo = schemaManager.getSchemaInfo(schema);
            localization.put(schema, new SchemaLocalization(context, schema, schemaInfo));
        }

        return localization;
    }


    public static SchemaLocalizations create(String schema) throws IOException, JDOMException {
        Object obj = RequestContextHolder.getRequestAttributes();

        ServletRequestAttributes attributes = (ServletRequestAttributes) obj;
        HttpServletRequest request = attributes.getRequest();

        ServletContext context = request.getSession().getServletContext();
        final ApplicationContext appContext = getApplicationContextFromServletContext(context);
        final String lang3 = ServiceRequestFactory.extractLanguage(request.getPathInfo());
        final String lang2 = appContext.getBean(IsoLanguagesMapper.class).iso639_2_to_iso639_1(lang3);
        CurrentLanguageHolder languageHolder = new CurrentLanguageHolder() {
            @Override
            public String getLang3() {
                return lang3;
            }

            @Override
            public String getLang2() {
                return lang2;
            }
        };
        return new SchemaLocalizations(appContext, languageHolder, schema, null);
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

    public String nodeTranslation(String qualifiedNodeName, String qualifiedParentNodeName,  String type) throws Exception {
        if (qualifiedParentNodeName == null) {
            qualifiedParentNodeName = "";
        }

        for (SchemaLocalization schemaLocalization : this.schemaLocalizations) {
            final ImmutableTable<String, String, Element> labelIndex = schemaLocalization.getLabelIndex(this.languageHolder.getLang3());
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

    public String codelistTranslation(String codelist, String context, String type) throws Exception {

        if (LANG_CODELIST_NS.equals(codelist) || "#LanguageCode".equals(codelist)) {
            return translateLanguageCode(context);
        }

        codelist = extractCodeListName(codelist);

        for (SchemaLocalization schemaLocalization : this.schemaLocalizations) {
            Element codelistEl = schemaLocalization.getCodeListIndex(this.languageHolder.getLang3()).get(codelist, context);
            if (codelistEl != null) {
                return codelistEl.getChildText(type);
            }
        }
        return context;
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
            String label = isoLanguage.getLabel(languageHolder.getLang3());
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
        if (indexOfPound > -1) {
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
            final ImmutableTable<String, String, Element> codeListIndex = schemaLocalization.getCodeListIndex(this.languageHolder.getLang3());
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
            Element strings = schemaLocalization.getStrings(this.languageHolder.getLang3());
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

}
