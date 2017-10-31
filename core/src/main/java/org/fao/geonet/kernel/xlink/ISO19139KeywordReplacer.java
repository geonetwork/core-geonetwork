package org.fao.geonet.kernel.xlink;

import com.google.common.collect.Lists;
import jeeves.xlink.XLink;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.AllThesaurus;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.schema.subtemplate.Status;
import org.fao.geonet.kernel.search.KeywordsSearcher;
import org.fao.geonet.kernel.search.keyword.KeywordSearchParamsBuilder;
import org.fao.geonet.kernel.search.keyword.KeywordSearchType;
import org.fao.geonet.kernel.search.keyword.KeywordSort;
import org.fao.geonet.kernel.search.keyword.SortDirection;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Created by fgravin on 10/26/17.
 */
public class ISO19139KeywordReplacer {

    private final String ROOT_XML_PATH = ".//gmd:MD_Keywords/parent::*";


    private static final ArrayList<Namespace> NAMESPACES =
            Lists.newArrayList(
                    ISO19139Namespaces.GMD,
                    ISO19139Namespaces.GCO);

    String localXlinkUrlPrefix = "local://srv/api/registries/vocabularies/keyword?";
    //local://srv/api/registries/vocabularies/keyword?skipdescriptivekeywords=true&amp;thesaurus=external.theme.gemet&amp;id=http://www.eionet.europa.eu/gemet/concept/4577&amp;lang=eng,ger,fre,ita

    static final Pair<Collection<Element>, Boolean> NULL = Pair.read((Collection<Element>) Collections.<Element>emptySet(), false);

    @Autowired
    private IsoLanguagesMapper isoLanguagesMapper;

    @Autowired
    private ThesaurusManager thesaurusManager;

    public ISO19139KeywordReplacer() {}


    public Element replaceKeywordsByLocalXLinks (Element md) {
        Status status = this.replaceAll(md);
        if (status.isError()) {
            throw new RuntimeException(status.getMsg());
        }
        return md;
    }

    public Status replaceAll(Element md) {
        List<Element> nodes = null;
        List<Status> status = new ArrayList<Status>();

        try {
            nodes = (List<Element>)Xml.selectNodes(md, ROOT_XML_PATH, NAMESPACES);
        } catch (JDOMException e) {
            return new Status.Failure(String.format("%s- selectNodes JDOMEx: %s", "keyword", ROOT_XML_PATH));
        }
        for (Element node : nodes) {

            try {
                Pair<Collection<Element>, Boolean> xlinks = this.replace(node);
                if(xlinks == null) {
                    status.add(new Status.Failure(String.format("No replacer for keyword %s", Xml.getString(node))));
                }
                if (xlinks.one() != null && !xlinks.one().isEmpty()) {
                    for (Element element : xlinks.one()) {
                        element.detach();
                    }
                    Element parent = node.getParentElement();
                    int index = parent.indexOf(node);
                    if (xlinks.two()) {
                        parent.setContent(index, xlinks.one());
                    } else {
                        parent.addContent(index, xlinks.one());
                    }
                    status.add(xlinks.two() ?
                        new Status() : new Status.Failure(String.format("Incomplete match for keyword %s", Xml.getString(node))));
                }
            } catch (Exception e) {
                status.add(new Status.Failure(String.format("Error during match for keyword %s", Xml.getString(node))));
            }

        }
        return status.stream()
                .collect(Status.STATUS_COLLECTOR);
    }

    public Pair<Collection<Element>, Boolean> replace(Element keywordElt) throws Exception {

        if (XLink.isXLink(keywordElt))
            return NULL;

        Collection<Element> results = new ArrayList<>();

        // get all keywords from the gmd:descripteKeyword block
        List<Pair<Element, String>> allKeywords = getAllKeywords(keywordElt);
        java.util.Set<String> addedIds = new HashSet<>();
        for (Pair<Element, String> elem : allKeywords) {

            if (elem.one().getParent() == null || elem.two() == null || elem.two().trim().isEmpty()) {
                // already processed by another translation.
                continue;
            }
            // Find the keywords in any thesaurus
            KeywordsSearcher searcher = search(elem.two());
            List<KeywordBean> keywords = searcher.getResults();
            if (!keywords.isEmpty()) {
                KeywordBean keyword = keywords.get(0);
                elem.one().detach();
                String thesaurus = keyword.getThesaurusKey();
                String uriCode = keyword.getUriCode();

                // do not add if a keyword with the same ID and thesaurus has previously been added
                if (addedIds.add(thesaurus + "@@" + uriCode)) {
                    Element descriptiveKeywords = xlinkIt(thesaurus, uriCode);
                    results.add(descriptiveKeywords);
                }
            }
        }

        // need to return null if not matche are found so the calling class
        // knows there is not changes made
        if (results.isEmpty()) {
            return NULL;
        }

        boolean done = true;
        List<Element> allKeywords1 = Utils.convertToList(keywordElt.getDescendants(new ElementFinder(
                "keyword", Geonet.Namespaces.GMD, "MD_Keywords")), Element.class);
        if (allKeywords1.size() > 0) {
            // still have some elements that need to be made re-usable
            done = false;
        }
        return Pair.read(results, done);
    }

    public List<Pair<Element, String>> getAllKeywords(Element originalElem) {
        List<Element> allKeywords1 = Utils.convertToList(originalElem.getDescendants(new ElementFinder(
                "keyword", Geonet.Namespaces.GMD, "MD_Keywords")), Element.class);
        List<Pair<Element, String>> allKeywords = new ArrayList<>();
        for (Element element : allKeywords1) {
            allKeywords.addAll(zip(element, Utils.convertToList(element.getDescendants(new ElementFinder(
                    "CharacterString", Geonet.Namespaces.GCO, "keyword")), Element.class)));
            allKeywords.addAll(zip(element, Utils.convertToList(element.getDescendants(new ElementFinder(
                    "LocalisedCharacterString", Geonet.Namespaces.GMD, "textGroup")), Element.class)));
        }
        return allKeywords;
    }

    private Collection<? extends Pair<Element, String>> zip(Element keywordElem, List<Element> convertToList) {
        List<Pair<Element, String>> zipped = new ArrayList<>();
        for (Element word : convertToList) {
            zipped.add(Pair.read(keywordElem, word.getTextTrim()));
        }
        return zipped;
    }

    /**
     * Search in all thesauri the given keyword
     * @param keyword
     * @return
     * @throws Exception
     */
    private KeywordsSearcher search(String keyword) throws Exception {
        KeywordSearchParamsBuilder builder = new KeywordSearchParamsBuilder(this.isoLanguagesMapper);
        builder.addLang("eng")
                .addLang("ger")
                .addLang("fre")
                .addLang("ita")
                .maxResults(1)
                .keyword(keyword, KeywordSearchType.MATCH, true);

        Collection<Thesaurus> thesauri = new ArrayList<>(thesaurusManager.getThesauriMap().values());
        for (Iterator<Thesaurus> iterator = thesauri.iterator(); iterator.hasNext(); ) {
            Thesaurus thesaurus = iterator.next();
            if (!(thesaurus instanceof AllThesaurus)) {
                builder.addThesaurus(thesaurus.getKey());
            }
        }

        for (Thesaurus thesaurus : thesauri) {
            if (thesaurus instanceof AllThesaurus) {
                continue;
            }
            builder.addThesaurus(thesaurus.getKey());
        }

        KeywordsSearcher searcher = new KeywordsSearcher(this.isoLanguagesMapper, thesaurusManager);
        builder.setComparator(KeywordSort.defaultLabelSorter(SortDirection.DESC));
        searcher.search(builder.build());
        return searcher;
    }

    public Element xlinkIt(String thesaurus, String keywordUris) {
        String[] keywords = keywordUris.split(",");
        Element descriptiveKeywords = new Element("descriptiveKeywords", Geonet.Namespaces.GMD);
        descriptiveKeywords.setAttribute(XLink.SHOW, XLink.SHOW_EMBED, XLink.NAMESPACE_XLINK);
        descriptiveKeywords.setAttribute(XLink.HREF,
                localXlinkUrlPrefix + "thesaurus=" + thesaurus +
                        "&id=" + StringUtils.join(keywords, ",") + "&multiple=false&lang=fre,eng,ger,ita,roh&textgroupOnly&skipdescriptivekeywords");

        return descriptiveKeywords;
    }
}
