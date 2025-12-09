//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================


package org.fao.geonet.kernel.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.Nullable;

import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.TermNotFoundException;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.kernel.search.keyword.KeywordRelation;
import org.fao.geonet.kernel.search.keyword.KeywordSearchParams;
import org.fao.geonet.kernel.search.keyword.KeywordSearchParamsBuilder;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.jdom.Element;

import jeeves.server.context.ServiceContext;

/**
 * Select entries from SKOS thesauri.
 */
public class KeywordsSearcher {
    private final ThesaurusFinder _thesaurusFinder;
    private final IsoLanguagesMapper _isoLanguageMapper;
    private List<KeywordBean> _results = new ArrayList<KeywordBean>();

    /**
     * Create a new searcher
     *
     * @param mapper          the language mapper to use
     * @param thesaurusFinder the object for looking up thesauri
     */
    public KeywordsSearcher(IsoLanguagesMapper mapper, ThesaurusFinder thesaurusFinder) {
        _thesaurusFinder = thesaurusFinder;
        this._isoLanguageMapper = mapper;
    }

    public KeywordsSearcher(ServiceContext context, ThesaurusFinder thesaurusFinder) {
        this(context.getBean(IsoLanguagesMapper.class), thesaurusFinder);
    }

    /**
     * Convert the keyword to xml in the following format:
     *
     * <descKeys> <keyword> <selected>true/false</selected> <id>id valid only for this search</id>
     * <uri>general uri id that is always valid</uri> <value>label in default lang (first lang that
     * was defined in search params</value> <definition>definition in default lang (first lang that
     * was defined in search params</definition> <defaultLang>the default language of the bean.  It
     * is the 3 letter language code</defaulLang> <thesaurus>Thesaurus that the keyword belongs
     * to</thesaurus> <values> <value language="3 letter language code">value for this
     * translation</value> ... </values> <definitions> <definition language="3 letter language
     * code">value for this translation</definition> </definitions> <descKeys>
     *
     * @param rootEl the element to add the xml data to.
     * @param kb     the keyword to convert
     * @return rootEl with the new keyword data attached
     */
    public static Element toRawElement(Element rootEl, KeywordBean kb) {
        Element elKeyword = new Element("keyword");
        Element elSelected = new Element("selected");
        // TODO : Add Thesaurus name

        if (kb.isSelected()) {
            elSelected.addContent("true");
        } else {
            elSelected.addContent("false");
        }

        String defaultLang = kb.getDefaultLang();

        elKeyword.addContent(elSelected);
        elKeyword.addContent(new Element("id").addContent(Integer.toString(kb.getId())));
        if (defaultLang != null) {
            elKeyword.addContent(new Element("value").addContent(kb.getDefaultValue()).setAttribute("language", defaultLang));
            elKeyword.addContent(new Element("definition").addContent(kb.getDefaultDefinition()).setAttribute("language", defaultLang));
            elKeyword.addContent(new Element("defaultLang").addContent(defaultLang));
        }
        Element thesaurusElement = new Element("thesaurus");
        thesaurusElement.addContent(new Element("key").setText(kb.getThesaurusKey()));
        thesaurusElement.addContent(new Element("title").setText(kb.getThesaurusTitle()));
        thesaurusElement.addContent(new Element("date").setText(kb.getThesaurusDate()));
        thesaurusElement.addContent(new Element("type").setText(kb.getType()));
        thesaurusElement.addContent(new Element("url").setText(kb.getDownloadUrl()));
        elKeyword.addContent(thesaurusElement);
        elKeyword.addContent(new Element("uri").addContent(kb.getUriCode()));
        addBbox(kb, elKeyword);
        rootEl.addContent(elKeyword);

        elKeyword.addContent(addAllTranslations(kb, kb.getValues(), "values", "value"));
        elKeyword.addContent(addAllTranslations(kb, kb.getDefinitions(), "definitions", "definition"));

        return rootEl;
    }

    private static Element addAllTranslations(KeywordBean kb, Map<String, String> map, String rootElemName, String leafElemName) {
        Element values = new Element(rootElemName);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            values.addContent(new Element(leafElemName).addContent(entry.getValue()).setAttribute("language", entry.getKey()));
        }
        return values;
    }

    /**
     * Adds bounding box of keyword if one available.
     *
     * @param kb        The keyword to analyze.
     * @param elKeyword The XML fragment to update.
     */
    private static void addBbox(KeywordBean kb, Element elKeyword) {
        if (kb.getCoordEast() != null && kb.getCoordWest() != null
            && kb.getCoordSouth() != null
            && kb.getCoordNorth() != null && !kb.getCoordEast().equals("")
            && !kb.getCoordWest().equals("")
            && !kb.getCoordSouth().equals("")
            && !kb.getCoordNorth().equals("")) {
            Element elBbox = new Element("geo");
            Element elEast = new Element("east");
            elEast.addContent(kb.getCoordEast());
            Element elWest = new Element("west");
            elWest.addContent(kb.getCoordWest());
            Element elSouth = new Element("south");
            elSouth.addContent(kb.getCoordSouth());
            Element elNorth = new Element("north");
            elNorth.addContent(kb.getCoordNorth());
            elBbox.addContent(elEast);
            elBbox.addContent(elWest);
            elBbox.addContent(elSouth);
            elBbox.addContent(elNorth);
            elKeyword.addContent(elBbox);
        }
    }

    /**
     * Based on the id (code) and the thesaurus name/key a keyword will be read from the thesaurus
     * The translations read will be those provided in the lang params.
     *
     * All the translation will be in the Keyword even if not present in the thesaurus.  If a
     * language is not in the thesaurus then an empty string will be in the KeywordBean
     *
     * @param id             id of the keyword to read.  The id is a URI with the format:  ns#id.
     *                       For example: http://somesite.com#4
     * @param sThesaurusName the key that identifies a thesaurus.  usually of form
     *                       type.category.thesaurus.  It is obtained by calling {@link
     *                       Thesaurus#getKey()}
     * @param languages      translations to load into the keyword bean
     * @return keywordbean with the requested translations
     */
    public KeywordBean searchById(String id, String sThesaurusName, String... languages) {
        KeywordBean result = null;

        Thesaurus thesaurus = _thesaurusFinder.getThesaurusByName(sThesaurusName);

        try {
            result = thesaurus.getKeyword(id, languages);
        } catch (Exception e) {
        }

        return result;
    }

    public void searchTopConcepts(String sThesaurusName, String... languages) throws Exception {

        _results.clear();
        Thesaurus thesaurus = _thesaurusFinder.getThesaurusByName(sThesaurusName);
        for (KeywordBean keywordBean : thesaurus.getTopConcepts(languages)) {
            _results.add(keywordBean);
        }
    }

    public void search(KeywordSearchParams params) throws Exception {
        this._results = params.search(_thesaurusFinder);
    }

    /**
     * TODO javadoc.
     *
     * @param params params
     * @throws Exception hmm
     */
    public void search(String contextLanguage, Element params) throws Exception {
        search(contextLanguage, params, null);
    }

    public void search(String contextLanguage, Element params, Comparator<KeywordBean> comparator) throws Exception {
        if (contextLanguage == null) {
            contextLanguage = Geonet.DEFAULT_LANGUAGE;
        }
        KeywordSearchParamsBuilder paramsBuilder = KeywordSearchParamsBuilder.createFromElement(_isoLanguageMapper, params);
        if (paramsBuilder.getLangs().isEmpty()) {
            paramsBuilder.addLang(contextLanguage);
        }
        paramsBuilder.setComparator(comparator);
        search(paramsBuilder.build());
    }

    /**
     * Find keywords that are related to the keyword with the provided ID.
     *
     * the parameters have to have 2 children. <ul> <li>id - the id/uri of the keyword to find</li>
     * <li>thesaurus - the id/key of the thesaurus to search</li> <ul>
     *
     * @param params     parameters
     * @param request    request
     * @param comparator the comparator to use for sorting the results.  it can be null.
     * @param languages  the languages to load
     */
    public void searchForRelated(Element params, KeywordRelation request, @Nullable Comparator<KeywordBean> comparator,
                                 String... languages) {
        //System.out.println("KeywordsSearcher searchBN");
        // TODO : Add geonetinfo elements.
        String id = Util.getParam(params, "id");
        String sThesaurusName = Util.getParam(params, "thesaurus");

        searchForRelated(id, sThesaurusName, request, comparator, languages);
    }

    /**
     * Find keywords that are related to the keyword with the provided ID
     *
     * @param id             id
     * @param sThesaurusName thesaurus name
     * @param request        request
     * @param comparator     the comparator to use for sorting the results.  it can be null.
     * @param languages      the languages to load
     */
    public void searchForRelated(String id, String sThesaurusName, KeywordRelation request, @Nullable Comparator<KeywordBean> comparator,
                                 String... languages) {
        _results.clear();
        Thesaurus thesaurus = _thesaurusFinder.getThesaurusByName(sThesaurusName);
        for (KeywordBean keywordBean : thesaurus.getRelated(id, request, languages)) {
            _results.add(keywordBean);
        }

        if (comparator != null) {
            Collections.sort(_results, comparator);
        }
    }

    /**
     * Return the number of results
     *
     * @return number of results
     */
    public int getNbResults() {
        return _results.size();
    }

    public List<KeywordBean> getResults() {
        return _results;
    }

    /**
     * Formats the keywords as XML and returns root (descKeys) Element
     *
     * General structure of Xml is defined in the {@link #toRawElement(Element, KeywordBean)}
     * method.  The root element is descKeys
     *
     * @return element
     * @throws Exception hmm
     */
    public Element getXmlResults() throws Exception {

        Element elDescKeys = new Element("descKeys");

        int nbResults = this.getNbResults();

        //for (int i = from; i <= to; i++) {
        for (int i = 0; i <= nbResults - 1; i++) {
            KeywordBean kb = _results.get(i);
            toRawElement(elDescKeys, kb);
        }

        return elDescKeys;
    }

    /**
     * Gets all children with node name pIdKeyword and toggles the selection flag on the keywords
     * with matching ids.  Id can be either UI or int id
     *
     * @param params parameters with pIdKeyword children
     */
    public void selectUnselectKeywords(Element params) {
        @SuppressWarnings("unchecked")
        List<Element> listIdKeywordsSelected = params.getChildren("pIdKeyword");

        Set<String> ids = new LinkedHashSet<String>();
        for (Element el : listIdKeywordsSelected) {
            ids.add(el.getTextTrim());
        }

        selectUnselectKeywords(ids);
    }

    /**
     * Toggles Selection on all keywords with either the uri or int id that matches one of the ids
     * provided
     *
     * @param idSet to toggleSelection
     */
    public void selectUnselectKeywords(Set<String> idSet) {
        for (KeywordBean keyword : _results) {
            if (idSet.contains("" + keyword.getId()) || idSet.contains(keyword.getUriCode())) {
                keyword.setSelected(!keyword.isSelected());
            }
        }
    }

    public void clearSelection() {
        for (KeywordBean keyword : _results) {
            keyword.setSelected(false);
        }
    }

    /**
     * Format selected keywords and return them as XML
     *
     * @return an element describing the list of selected keywords
     */
    public Element getSelectedKeywordsAsXml() {
        Element elDescKeys = new Element("descKeys");
        int nbSelectedKeywords = 0;
        for (int i = 0; i < this.getNbResults(); i++) {
            KeywordBean kb = _results.get(i);
            if (kb.isSelected()) {
                toRawElement(elDescKeys, kb);
                nbSelectedKeywords++;
            }
        }
        Element elNbTot = new Element("nbtot");
        elNbTot.addContent(Integer.toString(nbSelectedKeywords));
        elDescKeys.addContent(elNbTot);

        return elDescKeys;
    }

    /**
     * Traverse the list of results and create a new list with all the
     *
     * @return list of keywordbeans
     */
    public List<KeywordBean> getSelectedKeywordsInList() {
        List<KeywordBean> keywords = new ArrayList<KeywordBean>();
        for (int i = 0; i < this.getNbResults(); i++) {
            KeywordBean kb = _results.get(i);
            if (kb.isSelected()) {
                keywords.add(kb);
            }
        }
        return keywords;
    }

    /**
     * find the keyword with provided ID
     *
     * @param id integer id (not URI) of keyword
     * @return keywordbean
     */
    public KeywordBean getKeywordFromResultsById(int id) {
        for (KeywordBean kb : _results) {
            if (kb.getId() == id) {
                return kb;
            }
        }
        return null;
    }

    /**
     * find the keyword with provided ID
     *
     * @param id integer id (not URI) of keyword
     * @return keywordbean
     */
    public KeywordBean getKeywordFromResultsById(String id) {
        return getKeywordFromResultsById(Integer.parseInt(id));
    }

    /**
     * find the keyword with provided code/uri
     *
     * @param code uri of the keyword
     * @return keywordbean
     */
    public KeywordBean getKeywordFromResultsByUriCode(String code) {
        for (KeywordBean kb : _results) {
            if (kb.getUriCode().equals(code)) {
                return kb;
            }
        }
        return null;
    }


}
