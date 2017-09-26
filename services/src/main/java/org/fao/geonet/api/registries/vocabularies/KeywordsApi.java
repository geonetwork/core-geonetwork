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
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.constants.Jeeves;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.search.KeywordsSearcher;
import org.fao.geonet.kernel.search.keyword.*;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
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
import java.net.URLDecoder;
import java.nio.file.Path;
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
        @ApiParam(
                value = "Target langs",
                required = false
        )
        @RequestParam(
            value = XmlParams.pLang,
                required = false
        )
            List<String> targetLangs,
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



    @Autowired
    IsoLanguagesMapper mapper;

    @Autowired
    ThesaurusManager thesaurusManager;

    @ApiOperation(
        value = "Get keyword by id",
        nickname = "getKeywordById",
        notes = "Retrieve XML representation of keyword(s) from same thesaurus" +
            "using different transformations. " +
            "'to-iso19139-keyword' is the default and return an ISO19139 snippet." +
            "'to-iso19139-keyword-as-xlink' return an XLinked element. Custom transformation " +
            "can be create on a per schema basis."
    )
    @RequestMapping(
        path = "/keyword",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_XML_VALUE
        })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "XML snippet with requested keywords."),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Element getKeywordById(
        @ApiParam(
            value = "Keyword identifier or list of keyword identifiers comma separated.",
            required = true)
        @RequestParam (name = "id")
            String uri,
        @ApiParam(
            value = "Thesaurus to look info for the keyword(s).",
            required = true)
        @RequestParam (name = "thesaurus")
            String sThesaurusName,
        @ApiParam(
            value = "Languages.",
            required = false)
        @RequestParam (name = "lang", required = false)
            String [] langs,
        @ApiParam(
            value = "Only print the keyword, no thesaurus information.",
            required = false)
        @RequestParam (required = false, defaultValue = "false")
            boolean keywordOnly,
        @ApiParam(
            value = "XSL template to use (ISO19139 keyword by default, see convert.xsl).",
            required = false)
        @RequestParam (required = false)
            String transformation,
        @ApiIgnore
        @RequestParam
            Map<String,String> allRequestParams,
        HttpServletRequest request

    ) throws Exception {
        final String SEPARATOR = ",";
        ServiceContext context = ApiUtils.createServiceContext(request);

        if(langs == null) {
            langs = context.getLanguage().split(",");
        }
        String[] iso3langCodes = Arrays.copyOf(langs, langs.length);
        for (int i = 0; i < langs.length; i++) {
            if (StringUtils.isNotEmpty(langs[i])) {
                langs[i] = mapper.iso639_2_to_iso639_1(langs[i], langs[i].substring(2));
            }
        }

        Element descKeys;

        uri = URLDecoder.decode(uri, "UTF-8");

        if (uri == null) {
            descKeys = new Element("descKeys");
        } else {
            KeywordsSearcher searcher = new KeywordsSearcher(context, thesaurusManager);

            KeywordBean kb;
            if (!uri.contains(SEPARATOR)) {
                kb = searcher.searchById(uri, sThesaurusName, langs);
                if (kb == null) {
                    descKeys = new Element("descKeys");
                } else {
                    descKeys = KeywordsSearcher.toRawElement(new Element("descKeys"), kb);
                }
            } else {
                String[] url = uri.split(SEPARATOR);
                List<KeywordBean> kbList = new ArrayList<>();
                for (String currentUri : url) {
                    kb = searcher.searchById(currentUri, sThesaurusName, langs);
                    if (kb != null) {
                        kbList.add(kb);
                    }
                }
                descKeys = new Element("descKeys");
                for (KeywordBean keywordBean : kbList) {
                    KeywordsSearcher.toRawElement(descKeys, keywordBean);
                }
            }
        }

        GeonetworkDataDirectory dataDirectory = context.getBean(GeonetworkDataDirectory.class);
        SettingManager settingManager = context.getBean(SettingManager.class);
        Path convertXsl = dataDirectory.getWebappDir().resolve("xslt/services/thesaurus/convert.xsl");

        Element gui = new Element("gui");
        Element nodeUrl = new Element("nodeUrl").setText(settingManager.getNodeURL());
        Element nodeId = new Element("nodeId").setText(context.getNodeId());
        Element thesaurusEl = new Element("thesaurus");
        final Element root = new Element("root");

        gui.addContent(thesaurusEl);
        thesaurusEl.addContent(thesaurusManager.buildResultfromThTable(context));

        Element requestParams = new Element ("request");
        for (Map.Entry<String, String> e : allRequestParams.entrySet()) {
            if (e.getKey().equals("lang")) {
                requestParams.addContent(new Element(e.getKey())
                    .setText(String.join(",", iso3langCodes)));
            } else {
                requestParams.addContent(new Element(e.getKey()).setText(e.getValue()));
            }
        }
        root.addContent(requestParams);
        root.addContent(descKeys);
        root.addContent(gui);
        root.addContent(nodeUrl);
        root.addContent(nodeId);
        final Element transform = Xml.transform(root, convertXsl);

        return transform;
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
        if(targetLangs != null) {
            for (String targetLang : targetLangs) {
                if (!targetLang.trim().isEmpty()) {
                    parsedParams.addLang(targetLang.trim());
                    addedLang = true;
                }
            }
        }
        if (!addedLang) {
            parsedParams.addLang(uiLang);
        }

        return parsedParams;
    }
}
