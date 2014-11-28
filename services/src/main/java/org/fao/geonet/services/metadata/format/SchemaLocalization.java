package org.fao.geonet.services.metadata.format;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jeeves.server.dispatchers.guiservices.XmlFile;
import org.fao.geonet.domain.Pair;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains all the translation/localization files for a particular schema.
 *
 * @author Jesse on 10/15/2014.
 */
public class SchemaLocalization {
    public final String schema;
    private final Map<String, XmlFile> schemaInfo;
    private final ApplicationContext context;
    /**
     * A Map &lt;3CharlangId, Table&lt;elementName, parentElemName, Element containing label child and description child>>
     * <p/>
     * the parent may be "" if the labels.xml does not have a context attribute and thus the "" string will be a fallback
     */
    private final Map<String, ImmutableTable<String, String, Element>> labelIndex = Maps.newHashMap();
    /**
     * A Map &lt;3CharlangId, Table &lt;codeListName, code, Element containing label child and description child>>
     * <p/>
     * The codeListName has the prefix removed.
     */
    private final Map<String, ImmutableTable<String, String, Element>> codeListIndex = Maps.newHashMap();


    public SchemaLocalization(ApplicationContext context, String schema, Map<String, XmlFile> schemaInfo) {
        this.schema = schema;
        this.schemaInfo = schemaInfo;
        this.context = context;
    }

    public Element getLabels(String lang) throws Exception {
        return getXml("labels.xml", lang);
    }

    public Element getCodelists(String lang) throws Exception {
        return getXml("codelists.xml", lang);
    }

    public Element getStrings(String lang) throws Exception {
        return getXml("strings.xml", lang);
    }

    /**
     * Get a quick lookup for labels.  The returned map is
     * Table &lt;codeListName, code, Element containing label child and description child>>
     * <p/>
     * the parent may be "" if the labels.xml does not have a context attribute and thus the "" string will be a fallback
     *
     * @param lang
     * @return
     * @throws Exception
     */
    public synchronized ImmutableTable<String, String, Element> getLabelIndex(String lang) throws Exception {
        ImmutableTable<String, String, Element> index = this.labelIndex.get(lang);
        if (index == null) {
            ImmutableTable.Builder<String, String, Element> indexBuilder = ImmutableTable.builder();
            final Element labels = getLabels(lang);

            Set<Pair<String, String>> added = Sets.newHashSet();
            @SuppressWarnings("unchecked")
            final List<Element> children = labels.getChildren("element");
            for (Element element : children) {
                final String name = element.getAttributeValue("name");
                String parent = element.getAttributeValue("context");
                if (parent == null) {
                    parent = "";
                }
                final Pair<String, String> key = Pair.read(name, parent);
                if (!added.contains(key)) {
                    indexBuilder.put(name, parent, element);
                    added.add(key);
                }
            }
            index = indexBuilder.build();
            this.labelIndex.put(lang, index);
        }

        return index;
    }

    /**
     * Get a quick lookup table for finding codelist translations.  The returned table is
     * Table &lt;codeListName, code, Element containing label child and description child>
     * <p/>
     * The codeListName has the prefix removed.
     */
    public ImmutableTable<String, String, Element> getCodeListIndex(String lang) throws Exception {
        ImmutableTable<String, String, Element> index = this.codeListIndex.get(lang);
        if (index == null) {
            ImmutableTable.Builder<String, String, Element> indexBuilder = ImmutableTable.builder();
            final Element codelistEls = getCodelists(lang);

            Set<Pair<String, String>> added = Sets.newHashSet();
            @SuppressWarnings("unchecked")
            final List<Element> children = codelistEls.getChildren("codelist");
            for (Element codelist : children) {
                String codelistName = extractCodeListNameFromXml(codelist);
                @SuppressWarnings("unchecked")
                final List<Element> codes = codelist.getChildren("entry");
                for (Element codeEl : codes) {
                    String code = codeEl.getChildText("code");
                    final Pair<String, String> key = Pair.read(codelistName, code);
                    if (!added.contains(key)) {
                        indexBuilder.put(codelistName, code, codeEl);
                        added.add(key);
                    }
                }
            }

            index = indexBuilder.build();
            this.codeListIndex.put(lang, index);
        }

        return index;
    }

    private String extractCodeListNameFromXml(Element child) {
        String codeListNameFromLabel = child.getAttributeValue("name");
        int endOfPrefix = codeListNameFromLabel.indexOf(":");
        if (endOfPrefix > 0) {
            codeListNameFromLabel = codeListNameFromLabel.substring(endOfPrefix + 1);
        }
        return codeListNameFromLabel;
    }

    private Element getXml(String key, String lang) throws JDOMException, IOException {
        return schemaInfo.get(key).getXml(context, lang, false);
    }
}
