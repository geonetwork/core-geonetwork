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

package org.fao.geonet.kernel.reusable;

import static org.fao.geonet.kernel.reusable.Utils.addChild;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.utils.Xml;
import jeeves.xlink.XLink;

import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.search.KeywordsSearcher;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.util.ElementFinder;
import org.fao.geonet.util.XslUtil;
import org.jdom.Element;
import org.openrdf.model.GraphException;
import org.openrdf.model.URI;
import org.openrdf.sesame.config.AccessDeniedException;

import com.google.common.base.Function;

public final class KeywordsStrategy extends ReplacementStrategy
{
    private static final String NAMESPACE = "http://custom.shared.obj.ch/concept#";
    private static final String GEOCAT_THESAURUS_NAME = "local._none_.geocat.ch";
    public static final String NON_VALID_THESAURUS_NAME = "local._none_.non_validated";

    private final ThesaurusManager _thesaurusMan;
    private final String           _styleSheet;
    private final String           _currentLocale;

    public KeywordsStrategy(ThesaurusManager thesaurusMan, String appPath, String baseURL, String currentLocale)
    {
        this._thesaurusMan = thesaurusMan;
        _styleSheet = appPath + Utils.XSL_REUSABLE_OBJECT_DATA_XSL;

        _currentLocale = currentLocale;
    }

    public Pair<Collection<Element>, Boolean> find(Element placeholder, Element originalElem, String defaultMetadataLang)
            throws Exception
    {

        if (XLink.isXLink(originalElem))
            return NULL;

        Collection<Element> results = new ArrayList<Element>();

        List<Pair<Element, String>> allKeywords = getAllKeywords(originalElem);
        java.util.Set<String> addedIds = new HashSet<String>();
        for (Pair<Element, String> elem : allKeywords) {
            if(elem.one().getParent() == null || elem.two() == null || elem.two().trim().isEmpty()){
                // already processed by another translation.
                continue;
            }

            KeywordsSearcher searcher = search(elem.two());

            List keywords = searcher.getResults(new Element("params")).getChildren();
            if (!keywords.isEmpty()) {
                Element keyword = (Element) keywords.get(0);
                elem.one().detach();
                String thesaurus = keyword.getChildTextTrim("thesaurus");
                String id = keyword.getChildTextTrim("uri");

                // do not add if a keyword with the same ID and thesaurus has previously been added
                if(addedIds.add(thesaurus+"@@"+id)) {
                    boolean validated = !thesaurus.equalsIgnoreCase(NON_VALID_THESAURUS_NAME);
                    Element descriptiveKeywords = xlinkIt(thesaurus, id, validated);
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
        if (getKeywordElements(originalElem).size() > 0) {
            // still have some elements that need to be made re-usable
            done = false;
        }
        return Pair.read(results, done);
    }

    private List<Pair<Element,String>> getAllKeywords(Element originalElem)
    {
        List<Element> elKeywords = getKeywordElements(originalElem);
        List<Pair<Element,String>> allKeywords = new ArrayList<Pair<Element,String>>();
        for (Element element : elKeywords) {
            allKeywords.addAll(zip(element, Utils.convertToList(originalElem.getDescendants(new ElementFinder(
                    "CharacterString", XslUtil.GCO_NAMESPACE, "keyword")), Element.class)));
            allKeywords.addAll(zip(element, Utils.convertToList(originalElem.getDescendants(new ElementFinder(
                    "LocalisedCharacterString", XslUtil.GMD_NAMESPACE, "textGroup")), Element.class)));
        }
        return allKeywords;
    }

    private Collection<? extends Pair<Element, String>> zip(Element keywordElem, List<Element> convertToList)
    {
        List<Pair<Element,String>> zipped = new ArrayList<Pair<Element,String>>();
        for (Element word : convertToList) {
            zipped.add(Pair.read(keywordElem, word.getTextTrim()));
        }
        return zipped;
    }

    private List<Element> getKeywordElements(Element originalElem)
    {
        List<Element> allKeywords = Utils.convertToList(originalElem.getDescendants(new ElementFinder(
                "keyword", XslUtil.GMD_NAMESPACE, "MD_Keywords")), Element.class);
        return allKeywords;
    }

    private KeywordsSearcher search(String keyword) throws Exception
    {
        Element searchParams = new Element("params");
        Element pKeyword = new Element("pKeyword");
        Element pTypeSearch = new Element("pTypeSearch");
        Element pLanguage = new Element("pLanguage");
        searchParams.addContent(pKeyword);
        searchParams.addContent(pTypeSearch);
        searchParams.addContent(pLanguage);

        pKeyword.setText(keyword);
        pTypeSearch.setText("2");
        pLanguage.setText("*");

        KeywordsSearcher searcher = new KeywordsSearcher(_thesaurusMan);

        searcher.search(null, searchParams);
        searcher.sortResults("label");
        return searcher;
    }

    public Element findNonValidated(UserSession session) throws Exception
    {
        KeywordsSearcher searcher = new KeywordsSearcher(_thesaurusMan);

        Element params = new Element("params");
        Element thesParam = new Element("pThesauri");
        thesParam.setText(NON_VALID_THESAURUS_NAME);
        params.addContent(thesParam);
        params.addContent(new Element("pTypeSearch").setText("2"));
        params.addContent(new Element("pKeyword").setText("*"));
        
        searcher.search(null, params);
        searcher.sortResults("label");
        session.setProperty(Geonet.Session.SEARCH_KEYWORDS_RESULT, searcher);

        List<Element> results = searcher.getResults(new Element("params")).getChildren();

        Element keywords = new Element(REPORT_ROOT);
        for (Element element : results) {
            String id = element.getChildTextTrim("id");
            Element e = new Element(REPORT_ELEMENT);
            Pair<String/*lang*/,String/*name*/> desc = keyWordValueFromSearchResult(element);
            StringBuilder uriBuilder = new StringBuilder();
            uriBuilder.append(XLink.LOCAL_PROTOCOL);
            uriBuilder.append("thesaurus.admin?thesaurus=");
            uriBuilder.append(NON_VALID_THESAURUS_NAME);
            uriBuilder.append("&id=");
            uriBuilder.append(URLEncoder.encode(element.getChildText("uri"), "UTF-8"));
            uriBuilder.append("&lang=");
            String lang = desc.one();
            if(lang != null) {
                if(lang.equalsIgnoreCase("fra")) {
                    lang = "fre";
                } else if(lang.equalsIgnoreCase("deu")) {
                    lang = "ger";
                }
                
            }
            uriBuilder.append(lang);
            
            addChild(e, REPORT_ID, id);
            addChild(e, REPORT_URL, uriBuilder.toString());
            addChild(e, REPORT_DESC, desc.two());
            keywords.addContent(e);
        }

        return keywords;
    }

    private Pair<String, String> keyWordValueFromSearchResult(Element element) throws Exception {
        @SuppressWarnings("unchecked")
        List<Element> values = element.getChildren("value");
        String lang = _currentLocale;
        String val = "...";
        for (Element value : values) {
            if(!value.getTextTrim().equals("")) {
                val = value.getTextTrim();
                String code = value.getAttributeValue("lang").toLowerCase();
                lang = IsoLanguagesMapper.getInstance().iso639_1_to_iso639_2(code);
                if(lang.equalsIgnoreCase(_currentLocale)) {
                    break;
                }
            }
        }
        return Pair.read(lang, val);
    }

    public String createXlinkHref(String id, UserSession session, String thesaurusName) throws Exception
    {
        String thesaurus = validateName(thesaurusName);
        KeywordBean concept = lookup(id, session);
        String uri = concept.getCode();
        return XLink.LOCAL_PROTOCOL+"che.keyword.get?thesaurus=" + thesaurus + "&id=" + URLEncoder.encode(uri, "utf-8");
    }

    public void performDelete(String[] ids, Dbms dbms, UserSession session, String thesaurusName) throws Exception
    {
       Thesaurus thesaurus = _thesaurusMan.getThesaurusByName(validateName(thesaurusName));

        for (String id : ids) {
            try {
                // A test to see if id is from a previous search or 
                Integer.parseInt(id);
                KeywordBean concept = lookup(id, session);
                thesaurus.removeElement(concept);
            } catch (NumberFormatException e) {
                thesaurus.removeElement(NAMESPACE, extractCode(id));
            }
        }

    }

    private String validateName(String thesaurusName)
    {
        if (thesaurusName == null) {
            return NON_VALID_THESAURUS_NAME;
        } else {
            return thesaurusName;
        }
    }

    private KeywordBean lookup(String id, UserSession session)
    {
        KeywordsSearcher searcher = (KeywordsSearcher) session.getProperty(Geonet.Session.SEARCH_KEYWORDS_RESULT);
        KeywordBean concept = searcher.existsResult(id);
        return concept;
    }

    public String updateHrefId(String oldHref, String id, UserSession session)
            throws UnsupportedEncodingException
    {
        String base = oldHref.substring(0, oldHref.indexOf('?'));
        KeywordsSearcher searcher = (KeywordsSearcher) session.getProperty(Geonet.Session.SEARCH_KEYWORDS_RESULT);
        KeywordBean concept = searcher.existsResult(id);
        String encoded = URLEncoder.encode(concept.getCode(), "utf-8");
        return base + "?thesaurus=" + GEOCAT_THESAURUS_NAME + "&id=" + encoded + "&locales=en,it,de,fr";
    }

    public Map<String, String> markAsValidated(String[] ids, Dbms dbms, UserSession session) throws Exception
    {
        KeywordsSearcher searcher = (KeywordsSearcher) session.getProperty(Geonet.Session.SEARCH_KEYWORDS_RESULT);

        Thesaurus geocatThesaurus = _thesaurusMan.getThesaurusByName(GEOCAT_THESAURUS_NAME);
        Thesaurus nonValidThesaurus = _thesaurusMan.getThesaurusByName(NON_VALID_THESAURUS_NAME);

        Map<String, String> idMap = new HashMap<String, String>();
        String[] langs = {"en", "fr","de", "it"};
        for (String id : ids) {
            idMap.put(id, id);
            KeywordBean concept = searcher.existsResult(id);
            String code = concept.getCode();
            for(String lang : langs){
                KeywordBean translation = searcher.searchById(code, NON_VALID_THESAURUS_NAME, lang, false);

                if(translation != null) {
                    geocatThesaurus.addElement(translation.getNameSpaceCode(), translation.getRelativeCode(), translation.getValue(), translation.getDefinition(), lang);
                }
            }
            
            nonValidThesaurus.removeElement(concept);
        }
        return idMap;
    }

    private Element xlinkIt(String thesaurus, String id, boolean validated) throws UnsupportedEncodingException
    {
        String encoded = URLEncoder.encode(id, "UTF-8");
        Element descriptiveKeywords = new Element("descriptiveKeywords", XslUtil.GMD_NAMESPACE);

        descriptiveKeywords.setAttribute(XLink.HREF, XLink.LOCAL_PROTOCOL+"che.keyword.get?thesaurus=" + thesaurus
                + "&id=" + encoded + "&locales=en,it,de,fr", XLink.NAMESPACE_XLINK);

        if (!validated) {
            descriptiveKeywords.setAttribute(XLink.ROLE, ReusableObjManager.NON_VALID_ROLE,
                    XLink.NAMESPACE_XLINK);
        }

        descriptiveKeywords.setAttribute(XLink.SHOW, XLink.SHOW_EMBED, XLink.NAMESPACE_XLINK);

        return descriptiveKeywords;
    }

    public Collection<Element> add(Element placeholder, Element originalElem, Dbms dbms, String metadataLang)
            throws Exception
    {

        String nonValidThesaurusName = NON_VALID_THESAURUS_NAME;
        String code = UUID.randomUUID().toString();

        URI uri = doUpdateKeyword(originalElem, nonValidThesaurusName, code, metadataLang, false);
        if (uri == null) {
            return Collections.emptyList();
        } else {
            return Collections.singleton(xlinkIt(NON_VALID_THESAURUS_NAME, uri.toString(), false));
        }
    }

    private URI doUpdateKeyword(Element originalElem, String nonValidThesaurusName, String code, String metadataLang,
            boolean update) throws Exception, GraphException, IOException, AccessDeniedException
    {
        List<Element> xml = Xml.transform((Element) originalElem.clone(), _styleSheet).getChildren("keyword");

        Thesaurus thesaurus = _thesaurusMan.getThesaurusByName(nonValidThesaurusName);

        List<Pair<String/* code */, String/* locale */>> words = new ArrayList<Pair<String, String>>();
        for (Element keywordElement : xml) {
            String keyword = keywordElement.getTextTrim();
            String locale = keywordElement.getAttributeValue("locale");
            if (locale == null || locale.trim().length() < 2) {
                locale = metadataLang;
            } else {
                locale = locale.toLowerCase();
            }

            locale = locale.toLowerCase().substring(0, 2);

            words.add(Pair.read(keyword, locale));
        }
        URI uri = null;
        final String namespace = NAMESPACE;
        for (Pair<String, String> pair : words) {
            if (update) {
                thesaurus.updateElement(namespace, code, pair.one(), pair.one(), pair.two());
            } else {
                uri = thesaurus.addElement(namespace, code, pair.one(), pair.one(), pair.two());
            }
        }
        return uri;
    }

    public Collection<Element> updateObject(Element xlink, Dbms dbms, String metadataLang) throws Exception
    {
        String thesaurusName = Utils.extractUrlParam(xlink, "thesaurus");
        if (!NON_VALID_THESAURUS_NAME.equals(thesaurusName)) {
            return Collections.emptySet();
        }
        String id = Utils.extractUrlParam(xlink, "id");

        String code = extractCode(id);

        doUpdateKeyword(xlink, thesaurusName, code, metadataLang, true);

        return Collections.emptySet();
    }

    private String extractCode(String code) throws UnsupportedEncodingException {
        code = URLDecoder.decode(code,"UTF-8");
        int hashIndex = code.indexOf("#",1)+1;

        if (hashIndex > 2) {
            code = code.substring(hashIndex);
        }
        return code;
    }

    public boolean isValidated(Dbms dbms, String href) throws Exception
    {
        return !href.contains("thesaurus=local._none_.non_validated");
    }

    @Override
    public String toString()
    {
        return "Reusable Keyword";
    }

    @Override
    public String[] getInvalidXlinkLuceneField() {
        return new String[]{"invalid_xlink_keyword"};
    }

    @Override
    public String createAsNeeded(String href, UserSession session) throws Exception {

        String startId = Utils.id(href);
        if(startId!=null) return href;
         
        String code = UUID.randomUUID().toString();
        Thesaurus thesaurus = _thesaurusMan.getThesaurusByName(NON_VALID_THESAURUS_NAME);
        
        String keyword = "";
        String id = URLEncoder.encode(thesaurus.addElement(NAMESPACE, code, keyword , keyword, Geocat.DEFAULT_LANG).toString(), "UTF-8");
        
        return XLink.LOCAL_PROTOCOL+"che.keyword.get?thesaurus=" + NON_VALID_THESAURUS_NAME + "&id=" + id + "&locales=en,it,de,fr";
    }

    @Override
    public Function<String,String> numericIdToConcreteId(final UserSession session) {
        return new Function<String,String>() {
            public String apply(String id) {
                try {
                    KeywordsSearcher searcher = (KeywordsSearcher) session.getProperty(Geonet.Session.SEARCH_KEYWORDS_RESULT);
                    try {
                        Integer.parseInt(id);
                        KeywordBean concept = searcher.existsResult(id);
                        return URLEncoder.encode(concept.getCode(), "UTF-8");
                    } catch(NumberFormatException e) {
                        return  URLEncoder.encode(NAMESPACE+id, "utf-8");
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
