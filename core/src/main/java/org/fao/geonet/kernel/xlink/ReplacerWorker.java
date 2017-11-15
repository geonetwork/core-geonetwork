//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.xlink;

import com.google.common.collect.Lists;
import jeeves.xlink.XLink;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.kernel.AllThesaurus;
import org.fao.geonet.kernel.KeywordBean;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.fao.geonet.kernel.schema.subtemplate.Status.STATUS_COLLECTOR;
import static org.fao.geonet.kernel.xlink.ISO19139KeywordReplacer.ROOT_XML_PATH;

public class ReplacerWorker {

    private static final ArrayList<Namespace> NAMESPACES =
            Lists.newArrayList(
                    ISO19139Namespaces.GMD,
                    ISO19139Namespaces.GCO);

    private IsoLanguagesMapper isoLanguagesMapper;

    private ThesaurusManager thesaurusManager;

    java.util.Set<String> addedIds = new HashSet<>();

    public ReplacerWorker(IsoLanguagesMapper isoLanguagesMapper, ThesaurusManager thesaurusManager) {
        this.isoLanguagesMapper = isoLanguagesMapper;
        this.thesaurusManager = thesaurusManager;
    }

    public Status replaceAll(Element md) {
        try {
            List<Element> nodes = (List<Element>) Xml.selectNodes(md, ROOT_XML_PATH, NAMESPACES);
            return nodes.stream()
                    .map(node -> {return this.replace(node);})
                    .collect(Status.STATUS_COLLECTOR);
        } catch (JDOMException e) {
            return new Status.Failure(String.format("%s- selectNodes JDOMEx: %s", "keyword", ROOT_XML_PATH));
        }
    }

    private Status replace(Element keywordElt) {
        Element parent = keywordElt.getParentElement();
        int index = parent.indexOf(keywordElt);
        keywordElt.detach();
        return getElementStreamFromKeyWordBlock(keywordElt).map(elem -> {
            return getKeywordsStreamFromElement(elem).map(keyword -> {
                return ifAvailableReplaceInPlace(parent, index, elem, keyword);
            }).collect(Status.getOneSufficientCollector(String.format("Incomplete match for keyword ")));
        }).collect(STATUS_COLLECTOR);
    }

    private Status ifAvailableReplaceInPlace(Element parent, int index, Element elem, String keyword) {
        KeywordBean keywordBean = searchInAnyThesaurus(keyword);
        if (keywordBean != null) {
            String thesaurus = keywordBean.getThesaurusKey();
            String uriCode = keywordBean.getUriCode();

            // do not add if a keyword with the same ID and thesaurus has previously been added
            if (addedIds.add(thesaurus + "@@" + uriCode)) {
                Element descriptiveKeywords = xlinkIt(thesaurus, uriCode);
                parent.addContent(index, descriptiveKeywords);
            }
            return new Status();
        } else {
            return new Status.Failure(keyword);
        }
    }

    private Stream<Element> getElementStreamFromKeyWordBlock(Element originalElem) {
        Iterator<Element> originalElemsIt = originalElem.getDescendants(new ElementFinder("keyword", GMD, "MD_Keywords"));
        return streamFromIterator(originalElemsIt);
    }

    private Stream<String> getKeywordsStreamFromElement(Element element) {
        Iterator<Element> charStringElemIt = element.getDescendants(new ElementFinder("CharacterString", GCO, "keyword"));
        Iterator<Element> localisedStringElemIt = element.getDescendants(new ElementFinder("LocalisedCharacterString", GMD, "textGroup"));
        Stream<String> bothKindOfString = Stream.concat(
                streamFromIterator(charStringElemIt), streamFromIterator(localisedStringElemIt)).
                map(elem -> {return elem.getTextTrim();});
        return bothKindOfString;
    }

    protected KeywordBean searchInAnyThesaurus(String keyword) {
        KeywordSearchParamsBuilder builder = new KeywordSearchParamsBuilder(this.isoLanguagesMapper);
        builder.setComparator(KeywordSort.defaultLabelSorter(SortDirection.DESC));
        builder.addLang("eng")
                .addLang("ger")
                .addLang("fre")
                .addLang("ita")
                .maxResults(1)
                .keyword(keyword, KeywordSearchType.MATCH, true);

        thesaurusManager.getThesauriMap().values().stream()
                .filter(thesaurus -> {return !(thesaurus instanceof AllThesaurus);})
                .forEach(thesaurus -> { builder.addThesaurus(thesaurus.getKey());});

        KeywordsSearcher searcher = new KeywordsSearcher(this.isoLanguagesMapper, thesaurusManager);
        try {
            searcher.search(builder.build());
            List<KeywordBean> results = searcher.getResults();
            if (!results.isEmpty()) return results.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Element xlinkIt(String thesaurus, String keywordUris) {
        String[] keywords = keywordUris.split(",");
        Element descriptiveKeywords = new Element("descriptiveKeywords", GMD);
        descriptiveKeywords.setAttribute(XLink.SHOW, XLink.SHOW_EMBED, XLink.NAMESPACE_XLINK);
        descriptiveKeywords.setAttribute(XLink.HREF,
                ISO19139KeywordReplacer.localXlinkUrlPrefix + "thesaurus=" + thesaurus +

                        "&id=" + StringUtils.join(keywords, ",") +
                        "&multiple=false&lang=fre,eng,ger,ita,roh&textgroupOnly&skipdescriptivekeywords");

        return descriptiveKeywords;
    }

    private <T> Stream<T> streamFromIterator(Iterator<T> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
    }
}
