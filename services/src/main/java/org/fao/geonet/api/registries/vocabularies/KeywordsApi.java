/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.registries.vocabularies;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.constants.Jeeves;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.search.KeywordsSearcher;
import org.fao.geonet.kernel.search.keyword.*;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@EnableWebMvc
@Service
@RequestMapping(
    value = {
        "/api/registries/vocabularies",
        "/api/" + API.VERSION_0_1 +
            "/registries/vocabularies"
    })
@Api(
    value = ApiParams.API_CLASS_REGISTRIES_TAG,
    tags = ApiParams.API_CLASS_REGISTRIES_TAG,
    description = ApiParams.API_CLASS_REGISTRIES_OPS)
public class KeywordsApi {

    @Autowired
    LanguageUtils languageUtils;

    /**
     * TODO: There was some caching mechanism in previous implementation.
     */
    @ApiOperation(
        value = "Search keywords",
        nickname = "searchKeywords",
        notes = "")
    @RequestMapping(
        path = "/search",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseStatus(
        value = HttpStatus.OK)
    @ResponseBody
    public List<KeywordBean> searchKeywords(
        @ApiParam(
            value = "Query",
            required = false
        )
        @RequestParam(
            required = false
        )
            String q,
        @RequestParam(
            value = "lang",
            defaultValue = "eng"
        )
            String lang,
        @ApiParam(
            value = "Number of rows",
            required = false
        )
        @RequestParam(
            value = "Number of rows",
            required = false,
            defaultValue = "1000"
        )
            int rows,
        @ApiParam(
            value = "Start from",
            required = false
        )
        @RequestParam(
            defaultValue = "0",
            required = false
        )
            int start,
//        @RequestParam(
//            value = XmlParams.pLang,
//            defaultValue = ""
//        )
//            List<String> targetLangs,
        @ApiParam(
            value = "Thesaurus identifier",
            required = false
        )
        @RequestParam(
            required = false
        )
            String[] thesaurus,
//        @ApiParam(
//            value = "?",
//            required = false
//        )
//        @RequestParam(
//            required = false
//        )
//            String thesauriDomainName,
        @ApiParam(
            value = "Type of search",
            required = false
        )
        @RequestParam(
            defaultValue = "CONTAINS"
        )
            KeywordSearchType type,
        @ApiParam(
            value = "URI query",
            required = false
        )
        @RequestParam(
            required = false
        )
            String uri,
        @ApiParam(
            value = "Sort by",
            required = false
        )
        @RequestParam(
            required = false,
            defaultValue = "DESC"
        )
            String sort,
        NativeWebRequest webRequest,
        HttpServletRequest request,
        @ApiIgnore
            @ApiParam(hidden = true)
        HttpSession httpSession
    )
        throws Exception {ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);
        UserSession session = ApiUtils.getUserSession(httpSession);

//        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
//        lang = locale.getISO3Language();

        KeywordsSearcher searcher;
        // perform the search and save search result into session
        ThesaurusManager thesaurusMan = applicationContext.getBean(ThesaurusManager.class);

        if (Log.isDebugEnabled("KeywordsManager")) {
            Log.debug("KeywordsManager", "Creating new keywords searcher");
        }
        searcher = new KeywordsSearcher(context, thesaurusMan);

        IsoLanguagesMapper languagesMapper = applicationContext.getBean(IsoLanguagesMapper.class);
        List<String> targetLangs = new ArrayList<>();
        String thesauriDomainName = null;

        KeywordSearchParamsBuilder builder = parseBuilder(
            lang, q, rows, start,
            targetLangs, Arrays.asList(thesaurus),
            thesauriDomainName, type, uri, languagesMapper);

//            if (checkModified(webRequest, thesaurusMan, builder)) {
//                return null;
//            }

        if (q == null || q.trim().isEmpty()) {
            builder.setComparator(KeywordSort.defaultLabelSorter(SortDirection.parse(sort)));
        } else {
            builder.setComparator(KeywordSort.searchResultsSorter(q, SortDirection.parse(sort)));
        }

        searcher.search(builder.build());
        session.setProperty(Geonet.Session.SEARCH_KEYWORDS_RESULT,
            searcher);


        // get the results
        return searcher.getResults();
    }


    private KeywordSearchParamsBuilder parseBuilder(String uiLang, String q, int maxResults, int offset,
                                                    List<String> targetLangs, List<String> thesauri, String thesauriDomainName,
                                                    KeywordSearchType typeSearch, String uri, IsoLanguagesMapper mapper) {
        KeywordSearchParamsBuilder parsedParams =
            new KeywordSearchParamsBuilder(mapper)
            .lenient(true);

        if (q != null) {
            parsedParams.keyword(q, typeSearch, true);
        }

        if (uri != null) {
            parsedParams.uri(uri);
        }

        parsedParams.maxResults(maxResults);
        parsedParams.offset(offset);

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
}
