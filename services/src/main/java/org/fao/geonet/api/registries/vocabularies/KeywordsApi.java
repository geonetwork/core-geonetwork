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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.exception.WebApplicationException;
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
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.ThesaurusActivationRepository;
import org.fao.geonet.utils.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * The Class KeywordsApi.
 */
@EnableWebMvc
@Service
@RequestMapping(
    value = {
        "/{portal}/api/registries/vocabularies"
    })
@Tag(
    name = ApiParams.API_CLASS_REGISTRIES_TAG,
    description = ApiParams.API_CLASS_REGISTRIES_OPS)
public class KeywordsApi {

    /**
     * The language utils.
     */
    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    SettingManager settingManager;

    @Autowired
    ThesaurusManager thesaurusMan;

    @Autowired
    IsoLanguagesMapper languagesMapper;

    @Autowired
    GeonetworkDataDirectory dataDirectory;

    @Autowired
    ThesaurusActivationRepository thesaurusActivationRepository;

    @Autowired
    GeonetHttpRequestFactory httpRequestFactory;
    /**
     * The mapper.
     */
    @Autowired
    IsoLanguagesMapper mapper;
    /**
     * The thesaurus manager.
     */
    @Autowired
    ThesaurusManager thesaurusManager;

    /**
     * Search keywords.
     *
     * @param q           the q
     * @param lang        the lang
     * @param rows        the rows
     * @param start       the start
     * @param targetLangs the target langs
     * @param thesaurus   the thesaurus
     * @param type        the type
     * @param uri         the uri
     * @param sort        the sort
     * @param request     the request
     * @param httpSession the http session
     * @return the list
     * @throws Exception the exception
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Search keywords",
        description = "")
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
        @Parameter(
            description = "Query",
            required = false
        )
        @RequestParam(
            required = false
        )
            String q,
        @Parameter(
            description = "Query in that language",
            required = false
        )
        @RequestParam(
            value = "lang",
            defaultValue = "eng"
        )
            String lang,
        @Parameter(
            description = "Number of rows",
            required = false
        )
        @RequestParam(
            required = false,
            defaultValue = "1000"
        )
            int rows,
        @Parameter(
            description = "Start from",
            required = false
        )
        @RequestParam(
            defaultValue = "0",
            required = false
        )
            int start,
        @Parameter(
            description = "Return keyword information in one or more languages",
            required = false
        )
        @RequestParam(
            value = XmlParams.pLang,
            required = false
        )
            List<String> targetLangs,
        @Parameter(
            description = "Thesaurus identifier",
            required = false
        )
        @RequestParam(
            required = false
        )
            String[] thesaurus,
//        @Parameter(
//            value = "?",
//            required = false
//        )
//        @RequestParam(
//            required = false
//        )
//            String thesauriDomainName,
        @Parameter(
            description = "Type of search",
            required = false
        )
        @RequestParam(
            defaultValue = "CONTAINS"
        )
            KeywordSearchType type,
        @Parameter(
            description = "URI query",
            required = false
        )
        @RequestParam(
            required = false
        )
            String uri,
        @Parameter(
            description = "Sort by",
            required = false
        )
        @RequestParam(
            required = false,
            defaultValue = "DESC"
        )
            String sort,
        @Parameter(hidden = true)
            HttpServletRequest request,
        @Parameter(hidden = true)
            HttpSession httpSession
    )
        throws Exception {
        ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);
        UserSession session = ApiUtils.getUserSession(httpSession);

//        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
//        lang = locale.getISO3Language();

        KeywordsSearcher searcher;
        // perform the search and save search result into session
        if (Log.isDebugEnabled("KeywordsManager")) {
            Log.debug("KeywordsManager", "Creating new keywords searcher");
        }
        searcher = new KeywordsSearcher(context, thesaurusMan);

        String thesauriDomainName = null;

        List<String> thesauri=null;
        if (thesaurus!=null)
            thesauri=Arrays.asList(thesaurus);

        KeywordSearchParamsBuilder builder = parseBuilder(
            lang, q, rows, start,
            targetLangs, thesauri,
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

    /**
     * Gets the keyword by id.
     *
     * @param uri              the uri
     * @param sThesaurusName   the s thesaurus name
     * @param langs            the langs
     * @param keywordOnly      the keyword only
     * @param transformation   the transformation
     * @param allRequestParams the all request params
     * @param request          the request
     * @return the keyword by id
     * @throws Exception the exception
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get keyword by id",
        description = "Retrieve XML representation of keyword(s) from same thesaurus" +
            "using different transformations. " +
            "'to-iso19139-keyword' is the default and return an ISO19139 snippet." +
            "'to-iso19139-keyword-as-xlink' return an XLinked element. Custom transformation " +
            "can be create on a per schema basis."
    )
    @RequestMapping(
        path = "/keyword",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE
        })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "XML snippet with requested keywords."),
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Object getKeywordById(
        @Parameter(
            description = "Keyword identifier or list of keyword identifiers comma separated.",
            required = true)
        @RequestParam(name = "id")
            String uri,
        @Parameter(
            description = "Thesaurus to look info for the keyword(s).",
            required = true)
        @RequestParam(name = "thesaurus")
            String sThesaurusName,
        @Parameter(
            description = "Languages.",
            required = false)
        @RequestParam(name = "lang", required = false)
            String[] langs,
        @Parameter(
            description = "Only print the keyword, no thesaurus information.",
            required = false)
        @RequestParam(required = false, defaultValue = "false")
            boolean keywordOnly,
        @Parameter(
            description = "XSL template to use (ISO19139 keyword by default, see convert.xsl).",
            required = false)
        @RequestParam(required = false)
            String transformation,
        @Parameter(
            description = "langMap, that converts the values in the 'lang' parameter to how they will be actually represented in the record. {'fre':'fra'} or {'fre':'fr'}.  Missing/empty means to convert to iso 2 letter.",
            required = false)
        @RequestParam (name = "langMap", required = false)
            String  langMapJson,
        @Parameter(hidden = true)
        @RequestParam
            Map<String, String> allRequestParams,
        @RequestHeader(
            value = "Accept",
            defaultValue = MediaType.APPLICATION_XML_VALUE
        )
        String accept,
        @Parameter(hidden = true)
        HttpServletRequest request
        ) throws Exception {
        final String SEPARATOR = ",";
        ServiceContext context = ApiUtils.createServiceContext(request);
        boolean isJson = MediaType.APPLICATION_JSON_VALUE.equals(accept);

        // Search thesaurus by name (as facet key only contains the name of the thesaurus)
        Thesaurus thesaurus = thesaurusManager.getThesaurusByName(sThesaurusName);
        if (thesaurus == null) {
            String finalSThesaurusName = sThesaurusName;
            Optional<Thesaurus> thesaurusEntry = thesaurusManager.getThesauriMap().values().stream().filter(t -> t.getKey().endsWith(finalSThesaurusName)).findFirst();
            if (!thesaurusEntry.isPresent()) {
                throw new IllegalArgumentException(String.format(
                    "Thesaurus '%s' not found.", sThesaurusName));
            } else {
                thesaurus = thesaurusEntry.get();
                sThesaurusName = thesaurusEntry.get().getKey();
            }
        }


        if (langs == null) {
            langs = context.getLanguage().split(",");
        }
        String[] iso3langCodes = Arrays.copyOf(langs, langs.length);
        for (int i = 0; i < langs.length; i++) {
            if (StringUtils.isNotEmpty(langs[i])) {
                langs[i] = mapper.iso639_2_to_iso639_1(langs[i], langs[i].substring(0,2));  //default: fra -> fr
            }
        }

        Element descKeys;
        Map<String, String> jsonResponse = new HashMap<>();

        uri = URLDecoder.decode(uri, "UTF-8");

        if (uri == null) {
            descKeys = new Element("descKeys");
        } else {
            KeywordsSearcher searcher = new KeywordsSearcher(context, thesaurusManager);

            KeywordBean kb;
            String[] url;
            if (!uri.contains(SEPARATOR)) {
                url = new String[]{uri};
            }
            else {
                url = uri.split(SEPARATOR);
            }
            List<KeywordBean> kbList = new ArrayList<>();
            for (String currentUri : url) {
                kb = searcher.searchById(currentUri, sThesaurusName, iso3langCodes);
                if (kb == null) {
                    kb = searcher.searchById(currentUri, sThesaurusName, langs);
                }
                if (kb == null) {
                    kb = searcher.searchById(ApiUtils.fixURIFragment(currentUri), sThesaurusName, iso3langCodes);
                }
                if (kb == null) {
                    kb = searcher.searchById(ApiUtils.fixURIFragment(currentUri), sThesaurusName, langs);
                }
                if (kb != null) {
                    kbList.add(kb);
                }
            }
            descKeys = new Element("descKeys");
            for (KeywordBean keywordBean : kbList) {
                if (isJson) {
                    jsonResponse.put(
                        keywordBean.getUriCode(),
                        // Requested lang or the first non empty value
                        keywordBean.getDefaultValue()
                    );
                } else {
                    KeywordsSearcher.toRawElement(descKeys, keywordBean);
                }
            }
        }

       Element langConversion = null;
        if ( (langMapJson != null) && (!langMapJson.isEmpty()) ){
            JSONObject obj = JSONObject.fromObject(langMapJson);
            langConversion = new Element("languageConversions");
            for(Object entry : obj.entrySet()) {
                String key = ((Map.Entry) entry).getKey().toString();
                String value = ((Map.Entry) entry).getValue().toString();
                Element conv = new Element("conversion");
                conv.setAttribute("from",key.toString());
                conv.setAttribute("to",value.toString().replace("#",""));
                langConversion.addContent(conv);
            }

        }

        if (isJson) {
            return jsonResponse;
        } else {
            Path convertXsl = dataDirectory.getWebappDir().resolve("xslt/services/thesaurus/convert.xsl");

            Element gui = new Element("gui");
            Element nodeUrl = new Element("nodeUrl").setText(settingManager.getNodeURL());
            Element nodeId = new Element("nodeId").setText(context.getNodeId());
            Element thesaurusEl = new Element("thesaurus");
            final Element root = new Element("root");

            gui.addContent(thesaurusEl);
            thesaurusEl.addContent(thesaurusManager.buildResultfromThTable(context));

            Element requestParams = new Element("request");
            for (Map.Entry<String, String> e : allRequestParams.entrySet()) {
                if (e.getKey().equals("lang")) {
                    requestParams.addContent(new Element(e.getKey())
                        .setText(String.join(",", iso3langCodes)));
                } else {
                    requestParams.addContent(new Element(e.getKey()).setText(e.getValue()));
                }
            }
            if (langConversion != null) {
                requestParams.addContent(langConversion);
            }

            root.addContent(requestParams);
            root.addContent(descKeys);
            root.addContent(gui);
            root.addContent(nodeUrl);
            root.addContent(nodeId);
            final Element transform = Xml.transform(root, convertXsl);

            return transform;
        }
    }


    /**
     * Gets the thesaurus.
     *
     * @param thesaurus the thesaurus
     * @param response  the response
     * @return the thesaurus
     * @throws Exception the exception
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Download a thesaurus by name",
        description = "Download the thesaurus in SKOS format."
    )
    @RequestMapping(
        value = "/{thesaurus:.+}",
        method = RequestMethod.GET,
        produces = {
            MediaType.TEXT_XML_VALUE
        })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Thesaurus in SKOS format."),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND)
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void getThesaurus(
        @Parameter(
            description = "Thesaurus to download.",
            required = true)
        @PathVariable(value = "thesaurus")
            String thesaurus,
        HttpServletResponse response
    ) throws Exception {

        Thesaurus directory = thesaurusMan.getThesaurusByName(thesaurus);
        if (directory == null)
            throw new IllegalArgumentException("Thesaurus not found --> " + thesaurus);

        Path directoryFile = directory.getFile();
        if (!Files.exists(directoryFile))
            throw new IllegalArgumentException("Thesaurus file not found --> " + thesaurus);

        response.setContentType("text/xml");
        response.setHeader("Content-Disposition", "attachment;filename=" + directoryFile.getFileName());
        ServletOutputStream out = response.getOutputStream();
        BufferedReader reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(directoryFile.toFile()), StandardCharsets.UTF_8));
        IOUtils.copy(reader1, out);
        out.flush();
        out.close();
    }


    /**
     * Delete thesaurus.
     *
     * @param thesaurus the thesaurus
     * @return the element
     * @throws Exception the exception
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Delete a thesaurus by name",
        description = "Delete a thesaurus."
    )
    @RequestMapping(
        value = "/{thesaurus:.+}",
        method = RequestMethod.DELETE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Thesaurus deleted."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND)
    })
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void deleteThesaurus(
        @Parameter(
            description = "Thesaurus to delete.",
            required = true)
        @PathVariable(value = "thesaurus")
            String thesaurus
    ) throws Exception {

        Thesaurus thesaurusObject = thesaurusMan.getThesaurusByName(thesaurus);
        if (thesaurusObject == null) {
            throw new ResourceNotFoundException(String.format(
                "Thesaurus with identifier '%s' not found in the catalogue. Should be one of: %s",
                thesaurus,
                thesaurusMan.getThesauriMap().keySet().toString()
            ));
        }
        Path item = thesaurusObject.getFile();

        // Remove old file from thesaurus manager
        thesaurusMan.remove(thesaurus);

        // Remove file
        if (Files.exists(item)) {
            IO.deleteFile(item, true, Geonet.THESAURUS);

            // Delete thesaurus record in the database
            String thesaurusId = thesaurusObject.getFname();
            if (thesaurusActivationRepository.existsById(thesaurusId)) {
                thesaurusActivationRepository.deleteById(thesaurusId);
            }
        } else {
            throw new IllegalArgumentException(String.format(
                "Thesaurus RDF file was not found for thesaurus with identifier '%s'.",
                thesaurus));
        }
    }


    /**
     * Upload thesaurus.
     *
     * @param file       the file
     * @param type       the type
     * @param dir        the dir
     * @param stylesheet the stylesheet
     * @param request    the request
     * @return the element
     * @throws Exception the exception
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Uploads a new thesaurus from a file",
        description = "Uploads a new thesaurus."
    )
    @RequestMapping(
        method = RequestMethod.POST,
        produces = MediaType.TEXT_XML_VALUE
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Thesaurus uploaded in SKOS format."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_REVIEWER)
    })

    @PreAuthorize("hasAuthority('Reviewer')")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
    public String uploadThesaurus(
        @Parameter(
            description = "If set, do a file upload.")
        @RequestParam(value = "file", required = false)
            MultipartFile file,
        @Parameter(
            description = "Local or external (default).")
        @RequestParam(value = "type", defaultValue = "external")
            String type,
        @Parameter(
            description = "Type of thesaurus, usually one of the ISO thesaurus type codelist value. Default is theme.")
        @RequestParam(value = "dir", defaultValue = "theme")
            String dir,
        @Parameter(
            description = "XSL to be use to convert the thesaurus before load. Default _none_.")
        @RequestParam(value = "stylesheet", defaultValue = "_none_")
            String stylesheet,
        HttpServletRequest request
    ) throws Exception {

        long start = System.currentTimeMillis();
        ServiceContext context = ApiUtils.createServiceContext(request);

        // Different options for upload
        boolean fileUpload = file != null && !file.isEmpty();

        // Upload RDF file
        Path rdfFile = null;
        String fname = null;
        File tempDir = null;

        if (fileUpload) {

            Log.debug(Geonet.THESAURUS, "Uploading thesaurus file: " + file.getOriginalFilename());

            tempDir = Files.createTempDirectory("thesaurus").toFile();

            Path tempFilePath = tempDir.toPath().resolve(file.getOriginalFilename());
            File convFile = tempFilePath.toFile();
            file.transferTo(convFile);

            rdfFile = convFile.toPath();
            fname = file.getOriginalFilename();
        } else {

            Log.debug(Geonet.THESAURUS, "No file provided for thesaurus upload.");
            throw new MissingServletRequestParameterException("Thesaurus source not provided", "file");
        }

        try {
            if (StringUtils.isEmpty(fname)) {
                throw new Exception("File upload from URL or file return null");
            }

            long fsize;
            if (rdfFile != null && Files.exists(rdfFile)) {
                fsize = Files.size(rdfFile);
            } else {
                throw new MissingServletRequestParameterException("Thesaurus file doesn't exist", "file");
            }

            // -- check that the archive actually has something in it
            if (fsize == 0) {
                throw new MissingServletRequestParameterException("Thesaurus file has zero size", "file");
            }

            String extension = FilenameUtils.getExtension(fname);

            if (extension.equalsIgnoreCase("rdf") ||
                extension.equalsIgnoreCase("xml")) {
                Log.debug(Geonet.THESAURUS, "Uploading thesaurus: " + fname);

                // Rename .xml to .rdf for all thesaurus
                fname = fname.replace(extension, "rdf");
                uploadThesaurus(rdfFile, stylesheet, context, fname, type, dir);
            } else {
                Log.debug(Geonet.THESAURUS, "Incorrect extension for thesaurus named: " + fname);
                throw new Exception("Incorrect extension for thesaurus named: "
                    + fname);
            }

            long end = System.currentTimeMillis();
            long duration = (end - start) / 1000;

            return String.format("Thesaurus '%s' loaded in %d sec.",
                fname, duration);
        } finally {
            if (tempDir != null) {
                FileUtils.deleteQuietly(tempDir);
            }
        }
    }


    public enum REGISTRY_TYPE {
        re3gistry,
        ldRegistry
    }

    /**
     * Upload thesaurus.
     *
     * @param url              the url
     * @param registryUrl      the registry url
     * @param registryLanguage the languages to retrieve from the registry
     * @param type             the type
     * @param dir              the dir
     * @param stylesheet       the stylesheet
     * @param request          the request
     * @return the element
     * @throws Exception the exception
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Uploads a new thesaurus from URL or Registry",
        description = "Uploads a new thesaurus."
    )
    @RequestMapping(
        method = RequestMethod.PUT,
        produces = MediaType.TEXT_XML_VALUE
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Thesaurus uploaded in SKOS format."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_REVIEWER)
    })
    @PreAuthorize("hasAuthority('Reviewer')")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
    public String uploadThesaurusFromUrl(
        @Parameter(
            description = "If set, try to download from the Internet.")
        @RequestParam(value = "url", required = false)
            String url,
        @Parameter(
            description = "If set, try to download from a registry.")
        @RequestParam(value = "registryUrl", required = false)
            String registryUrl,
        @Parameter(
            description = "If using registryUrl, then define the type of registry." +
                " If not set, default mode is re3gistry.")
        @RequestParam(value = "registryType", required = false)
            REGISTRY_TYPE registryType,
        @Parameter(
            description = "Languages to download from a registry.")
        @RequestParam(value = "registryLanguage", required = false)
            String[] registryLanguage,
        @Parameter(
            description = "Local or external (default).")
        @RequestParam(value = "type", defaultValue = "external")
            String type,
        @Parameter(
            description = "Type of thesaurus, usually one of the ISO thesaurus type codelist value. Default is theme.")
        @RequestParam(value = "dir", defaultValue = "theme")
            String dir,
        @Parameter(
            description = "XSL to be use to convert the thesaurus before load. Default _none_.")
        @RequestParam(value = "stylesheet", defaultValue = "_none_")
            String stylesheet,
        HttpServletRequest request
    ) throws Exception {

        long start = System.currentTimeMillis();
        ServiceContext context = ApiUtils.createServiceContext(request);

        boolean urlUpload = !StringUtils.isEmpty(url);
        boolean registryUpload = !StringUtils.isEmpty(registryUrl);

        // Upload RDF file
        Path rdfFile = null;
        String fname = null;

        // Specific upload steps
        if (urlUpload) {

            Log.debug(Geonet.THESAURUS, "Uploading thesaurus from URL: " + url);

            rdfFile = getXMLContentFromUrl(url, context);
            fname = url.substring(url.lastIndexOf("/") + 1).replaceAll("\\s+", "");

            // File with no extension in URL
            if (fname.lastIndexOf('.') == -1) {
                fname += ".rdf";
            }


        } else if (registryUpload) {
            if (ArrayUtils.isEmpty(registryLanguage)) {
                throw new MissingServletRequestParameterException("Select at least one language.", "language");
            }

            Log.debug(Geonet.THESAURUS, "Uploading thesaurus from registry : " + registryUrl);

            String itemName = registryUrl.substring((registryUrl.lastIndexOf("/") + 1));

            rdfFile = extractSKOSFromRegistry(registryUrl, registryType, itemName, registryLanguage, context);
            fname = registryUrl.replaceAll("[^A-Za-z]+", "") +
                "-" +
                itemName + ".rdf";

        } else {

            Log.debug(Geonet.THESAURUS, "No URL or file name provided for thesaurus upload.");
            throw new MissingServletRequestParameterException("Thesaurus source not provided", "url");

        }

        if (StringUtils.isEmpty(fname)) {
            throw new ResourceNotFoundException("File upload from URL or file return null");
        }

        long fsize;
        if (rdfFile != null && Files.exists(rdfFile)) {
            fsize = Files.size(rdfFile);
        } else {
            throw new ResourceNotFoundException("Thesaurus file doesn't exist");
        }

        // -- check that the archive actually has something in it
        if (fsize == 0) {
            throw new ResourceNotFoundException("Thesaurus file has zero size");
        }

        String extension = FilenameUtils.getExtension(fname);

        if (extension.equalsIgnoreCase("rdf") ||
            extension.equalsIgnoreCase("xml")) {
            Log.debug(Geonet.THESAURUS, "Uploading thesaurus: " + fname);

            // Rename .xml to .rdf for all thesaurus
            fname = fname.replace(extension, "rdf");
            uploadThesaurus(rdfFile, stylesheet, context, fname, type, dir);
        } else {
            Log.debug(Geonet.THESAURUS, "Incorrect extension for thesaurus named: " + fname);
            throw new MissingServletRequestParameterException("Incorrect extension for thesaurus", fname);
        }

        long end = System.currentTimeMillis();
        long duration = (end - start) / 1000;

        return String.format("Thesaurus '%s' loaded in %d sec.",
            fname, duration);
    }

    /**
     * Extract SKOS from registry.
     * <p>
     * Download for each language the codelist from the registry. Combine
     * them into one XML document which is then XSLT processed for SKOS conversion.
     *
     * @param registryUrl the registry url
     * @param registryType
     * @param itemName    the item name
     * @param lang        the selected languages
     * @param context     the context
     * @return the path
     * @throws Exception the exception
     */
    private Path extractSKOSFromRegistry(String registryUrl, REGISTRY_TYPE registryType, String itemName, String[] lang, ServiceContext context)
        throws Exception {
        if (lang != null) {
            Element documents = new Element("documents");
            if (registryType == REGISTRY_TYPE.ldRegistry) {
                Path localRdf = getXMLContentFromUrl(registryUrl + "?_view=with_metadata&_format=rdf", context);
                Element ldRegistryCodelistAsRdf = Xml.loadFile(localRdf);
                documents.addContent(ldRegistryCodelistAsRdf);
            } else {
                for (String language : lang) {
                    try {
                        String languageFileUrl = registryUrl + "/" + itemName + "." + language + ".xml";
                        Path localRdf = getXMLContentFromUrl(languageFileUrl, context);
                        Element codeList = Xml.loadFile(localRdf);
                        documents.addContent(codeList);
                    } catch (Exception e) {
                        Log.debug(Geonet.THESAURUS, "Thesaurus not found for the requested translation: " + itemName + " " + language);
                        throw new ResourceNotFoundException("Thesaurus not found for the requested translation: " + itemName + " " + language);
                    }
                }
            }

            // Convert to SKOS
            Path skosTransform = dataDirectory.getWebappDir().resolve(String.format(
                "xslt/services/thesaurus/%s-to-skos.xsl",
                registryType == REGISTRY_TYPE.ldRegistry ? "ldregistry" : "registry"));
            Element transform = Xml.transform(documents, skosTransform);
            // Convert to file and return
            Path rdfFile = Files.createTempFile("thesaurus", ".rdf");
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getCompactFormat());
            xmlOutput.output(transform,
                new OutputStreamWriter(new FileOutputStream(rdfFile.toFile().getCanonicalPath()),
                    StandardCharsets.UTF_8));
            return rdfFile;

        }

        return null;
    }

    /**
     * Gets the XML content from url.
     *
     * @param url     the url
     * @param context the context
     * @return the rdf content from url
     * @throws URISyntaxException    the URI syntax exception
     * @throws IOException           Signals that an I/O exception has occurred.
     * @throws MalformedURLException the malformed URL exception
     */
    private Path getXMLContentFromUrl(String url, ServiceContext context) throws URISyntaxException, IOException, MalformedURLException {
        Path rdfFile;
        URI uri = new URI(url);
        rdfFile = Files.createTempFile("thesaurus", ".rdf");
        XmlRequest httpReq = httpRequestFactory.createXmlRequest(uri.toURL());
        httpReq.setAddress(uri.getPath());
        Lib.net.setupProxy(context, httpReq);
        httpReq.executeLarge(rdfFile);
        return rdfFile;
    }


    /**
     * Load a thesaurus in the catalogue and optionnaly convert it using XSL.
     *
     * @param rdfFile the rdf file
     * @param style   the style
     * @param context the context
     * @param fname   the fname
     * @param type    the type
     * @param dir     the dir
     * @return Element thesaurus uploaded
     * @throws Exception the exception
     */
    private void uploadThesaurus(Path rdfFile, String style,
                                 ServiceContext context, String fname, String type, String dir)
        throws Exception {

        Path stylePath = context.getAppPath().resolve(Geonet.Path.STYLESHEETS);

        Element tsXml;
        Element xml = Xml.loadFile(rdfFile);
        xml.detach();

        if (!"_none_".equals(style)) {
            FilePathChecker.verify(style);

            tsXml = Xml.transform(xml, stylePath.resolve(style));
            tsXml.detach();
        } else {
            tsXml = xml;
        }

        // Load document and check namespace
        if (tsXml.getNamespacePrefix().equals("rdf")
            && tsXml.getName().equals("RDF")) {

            // copy to directory according to type
            Path path = thesaurusMan.buildThesaurusFilePath(fname, type, dir);
            try (OutputStream out = Files.newOutputStream(path)) {
                Xml.writeResponse(new Document(tsXml), out);
            }

            final String siteURL = settingManager.getSiteURL(context);
            Thesaurus gst = new Thesaurus(languagesMapper, fname, type, dir, path, siteURL);
            thesaurusMan.addThesaurus(gst, false);
        } else {
            IO.deleteFile(rdfFile, false, Geonet.THESAURUS);
            throw new WebApplicationException("Unknown format (Must be in SKOS format).");
        }
    }

    /**
     * Parses the builder.
     *
     * @param uiLang             the ui lang
     * @param q                  the q
     * @param maxResults         the max results
     * @param offset             the offset
     * @param targetLangs        the target langs
     * @param thesauri           the thesauri
     * @param thesauriDomainName the thesauri domain name
     * @param typeSearch         the type search
     * @param uri                the uri
     * @param mapper             the mapper
     * @return the keyword search params builder
     */
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
        if (targetLangs != null) {
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
