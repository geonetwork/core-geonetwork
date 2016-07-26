//=============================================================================
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
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.services.thesaurus;

import com.google.common.collect.Sets;

import jeeves.constants.Jeeves;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Constants;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.search.KeywordsSearcher;
import org.fao.geonet.kernel.search.keyword.KeywordSearchParamsBuilder;
import org.fao.geonet.kernel.search.keyword.KeywordSearchType;
import org.fao.geonet.kernel.search.keyword.KeywordSort;
import org.fao.geonet.kernel.search.keyword.SortDirection;
import org.fao.geonet.kernel.search.keyword.XmlParams;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Returns a list of keywords given a list of thesaurus
 */
@Controller
public class GetKeywords {

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------
    @RequestMapping(value = "/{uiLang}/xml.search.keywords")
    @ResponseBody
    public HttpEntity<byte[]> deprecatedAPI(
        @PathVariable String uiLang,
        @RequestParam(value = "pNewSearch", defaultValue = "true") boolean newSearch,
        @RequestParam(value = "pMode", defaultValue = "true") String mode,
        @RequestParam(value = XmlParams.pKeyword, required = false) String searchTerm,
        @RequestParam(value = XmlParams.maxResults, required = false, defaultValue = "1000") String maxResults,
        @RequestParam(value = XmlParams.offset, required = false) String offset,
        @RequestParam(value = XmlParams.pLang, defaultValue = "") java.util.List<String> targetLangs,
        @RequestParam(value = XmlParams.pThesauri, required = false) java.util.List<String> thesauri,
        @RequestParam(value = XmlParams.pType, required = false) String thesauriDomainName,
        @RequestParam(value = XmlParams.pTypeSearch, defaultValue = "2") String typeSearch,
        @RequestParam(value = XmlParams.pUri, required = false) String keywordUriCode,
        @RequestParam(value = XmlParams.sort, required = false, defaultValue = "DESC") String sort,
        NativeWebRequest webRequest) throws Exception {
        return exec(uiLang, newSearch, mode, searchTerm, maxResults, offset, targetLangs, thesauri,
            thesauriDomainName, typeSearch, keywordUriCode, sort, webRequest);
    }

    @RequestMapping(value = "/{uiLang}/keywords")
    @ResponseBody
    public HttpEntity<byte[]> exec(
        @PathVariable String uiLang,
        @RequestParam(value = "pNewSearch", defaultValue = "true") boolean newSearch,
        @RequestParam(value = "pMode", defaultValue = "true") String mode,
        @RequestParam(value = XmlParams.pKeyword, required = false) String searchTerm,
        @RequestParam(value = XmlParams.maxResults, required = false, defaultValue = "1000") String maxResults,
        @RequestParam(value = XmlParams.offset, required = false) String offset,
        @RequestParam(value = XmlParams.pLang, defaultValue = "") java.util.List<String> targetLangs,
        @RequestParam(value = XmlParams.pThesauri, required = false) java.util.List<String> thesauri,
        @RequestParam(value = XmlParams.pType, required = false) String thesauriDomainName,
        @RequestParam(value = XmlParams.pTypeSearch, defaultValue = "2") String typeSearch,
        @RequestParam(value = XmlParams.pUri, required = false) String keywordUriCode,
        @RequestParam(value = XmlParams.sort, required = false, defaultValue = "DESC") String sort,
        NativeWebRequest webRequest)
        throws Exception {
        ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
        ServiceContext context = applicationContext.getBean(ServiceManager.class).createServiceContext("keywords", uiLang,
            webRequest.getNativeRequest(HttpServletRequest.class));
        Element responseXml = new Element(Jeeves.Elem.RESPONSE);
        UserSession session = context.getUserSession();


        KeywordsSearcher searcher;
        if (newSearch) {
            // perform the search and save search result into session
            ThesaurusManager thesaurusMan = applicationContext.getBean(ThesaurusManager.class);

            if (Log.isDebugEnabled("KeywordsManager")) {
                Log.debug("KeywordsManager", "Creating new keywords searcher");
            }
            searcher = new KeywordsSearcher(context, thesaurusMan);

            IsoLanguagesMapper languagesMapper = applicationContext.getBean(IsoLanguagesMapper.class);
            KeywordSearchParamsBuilder builder = parseBuilder(uiLang, searchTerm, maxResults, offset,
                targetLangs, thesauri, thesauriDomainName, typeSearch, keywordUriCode, languagesMapper);

            if (checkModified(webRequest, thesaurusMan, builder)) {
                return null;
            }

            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                builder.setComparator(KeywordSort.defaultLabelSorter(SortDirection.parse(sort)));
            } else {
                builder.setComparator(KeywordSort.searchResultsSorter(searchTerm, SortDirection.parse(sort)));
            }

            searcher.search(builder.build());
            session.setProperty(Geonet.Session.SEARCH_KEYWORDS_RESULT,
                searcher);
        } else {
            searcher = (KeywordsSearcher) session
                .getProperty(Geonet.Session.SEARCH_KEYWORDS_RESULT);
        }

        // get the results
        responseXml.addContent(searcher.getXmlResults());

        // If editing
        if (mode != null) {
            if (mode.equals("edit") || mode.equals("consult")) {
                responseXml.addContent(new Element("thesaurus")
                    .addContent(thesauri));

                responseXml.addContent((new Element("mode")).addContent(mode));
            }
        }
        String[] acceptHeader = webRequest.getHeaderValues("Accept");
        final Set<String> acceptContentType = Sets.newHashSet(
            acceptHeader != null ? acceptHeader : new String[]{"text/plain"});

        byte[] response;
        String contentType;
        if (acceptsType(acceptContentType, "json") ||
            "json".equals(webRequest.getParameter("_content_type"))) {
            response = Xml.getJSON(responseXml).getBytes(Constants.CHARSET);
            contentType = "application/json";
        } else if (acceptContentType.isEmpty() ||
            acceptsType(acceptContentType, "xml") ||
            acceptContentType.contains("*/*") ||
            acceptContentType.contains("text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2") ||
            acceptContentType.contains("text/plain")) {
            response = Xml.getString(responseXml).getBytes(Constants.CHARSET);
            contentType = "application/xml";
        } else {
            throw new IllegalArgumentException(acceptContentType + " is not supported");
        }

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Content-Type", contentType);
        headers.add("Cache-Control", "no-cache");

        return new HttpEntity<>(response, headers);
    }

    private boolean checkModified(NativeWebRequest webRequest, ThesaurusManager thesaurusMan, KeywordSearchParamsBuilder builder) {
        long latestLastModified = -1;
        for (String thesName : builder.getThesauriNames()) {
            Thesaurus thesaurus = thesaurusMan.getThesaurusByName(thesName);
            if (thesaurus == null) {
                return false;
            }
            long thesLastModified = thesaurus.getLastModifiedTime().toMillis();
            if (latestLastModified < thesLastModified) {
                latestLastModified = thesLastModified;
            }
        }

        long roundedChangeDate = latestLastModified / 1000 * 1000;
        return webRequest.checkNotModified(roundedChangeDate);

    }

    private KeywordSearchParamsBuilder parseBuilder(String uiLang, String searchTerm, String maxResults, String offset,
                                                    List<String> targetLangs, List<String> thesauri, String thesauriDomainName,
                                                    String typeSearch, String keywordUriCode, IsoLanguagesMapper mapper) {
        KeywordSearchParamsBuilder parsedParams = new KeywordSearchParamsBuilder(mapper).lenient(true);

        if (searchTerm != null) {
            KeywordSearchType searchType = KeywordSearchType.parseString(typeSearch);
            parsedParams.keyword(searchTerm, searchType, true);
        }

        if (keywordUriCode != null) {
            parsedParams.uri(keywordUriCode);
        }

        if (maxResults != null) {
            parsedParams.maxResults(Integer.parseInt(maxResults));
        }

        if (offset != null) {
            parsedParams.offset(Integer.parseInt(offset));
        }

        if (thesauriDomainName != null) {
            parsedParams.thesauriDomainName(thesauriDomainName);
        }

        if (thesauri == null) {
            ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
            ThesaurusManager thesaurusMan = applicationContext.getBean(ThesaurusManager.class);
            Map<String, Thesaurus> listOfThesaurus = thesaurusMan.getThesauriMap();
            for (String t : listOfThesaurus.keySet()) {
                parsedParams.addThesaurus(listOfThesaurus.get(t).getKey());
            }
        } else {
            for (String thesaurusName : thesauri) {
                if (!thesaurusName.trim().isEmpty()) {
                    parsedParams.addThesaurus(thesaurusName.trim());
                }
            }
        }

        boolean addedLang = false;
        for (String targetLang : targetLangs) {
            if (!targetLang.trim().isEmpty()) {
                parsedParams.addLang(targetLang.trim());
                addedLang = true;
            }
        }
        if (!addedLang) {
            parsedParams.addLang(uiLang);
        }

        return parsedParams;
    }

    private boolean acceptsType(Set<String> acceptContentType, String toCheck) {
        for (String acceptable : acceptContentType) {
            if (acceptable.contains(toCheck)) {
                return true;
            }
        }
        return false;
    }

}

// =============================================================================

