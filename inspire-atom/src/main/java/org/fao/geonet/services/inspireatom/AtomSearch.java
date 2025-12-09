//=============================================================================
//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
package org.fao.geonet.services.inspireatom;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.FeatureNotEnabledException;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.InspireAtomFeed;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.guiapi.search.XsltResponseWriter;
import org.fao.geonet.inspireatom.InspireAtomService;
import org.fao.geonet.inspireatom.util.InspireAtomUtil;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.InspireAtomFeedRepository;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static org.fao.geonet.kernel.search.EsFilterBuilder.buildPermissionsFilter;
import static org.fao.geonet.kernel.search.EsSearchManager.FIELDLIST_CORE;
import static org.springframework.http.HttpStatus.OK;


@RequestMapping(value = {
    "/{portal}/api/atom"
})
@Tag(name = "atom",
    description = "ATOM")
@RestController
public class AtomSearch {

    @Autowired
    InspireAtomService service;

    @Autowired
    SettingManager sm;

    @Autowired
    DataManager dm;

    @Autowired
    EsSearchManager searchMan;

    @Autowired
    InspireAtomFeedRepository inspireAtomFeedRepository;

    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    IsoLanguagesMapper isoLanguagesMapper;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get ATOM feeds",
        description = "")
    @GetMapping(
        value = "/feeds",
        produces = {
            MediaType.APPLICATION_XML_VALUE,
            MediaType.TEXT_HTML_VALUE
        }
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Get a list of feeds."),
        @ApiResponse(responseCode = "204", description = "Not authenticated.", content = {@io.swagger.v3.oas.annotations.media.Content(schema = @Schema(hidden = true))})
    })
    @ResponseStatus(OK)
    public Object feeds(
        @Parameter(
            description = "fileIdentifier",
            required = false)
        @RequestParam(defaultValue = "")
        String fileIdentifier,
        @Parameter(hidden = true)
        HttpServletRequest request) throws Exception {

        String acceptHeader = StringUtils.isBlank(request.getHeader(HttpHeaders.ACCEPT))?MediaType.APPLICATION_XML_VALUE:request.getHeader(HttpHeaders.ACCEPT);
        List<String> accept = Arrays.asList(acceptHeader.split(","));

        if (accept.contains(MediaType.TEXT_HTML_VALUE)) {
            return feedsAsHtml(fileIdentifier, request);
        } else{
            return feedsAsXml(fileIdentifier, request);
        }
    }
    private Element feedsAsXml(
        String fileIdentifier,
        HttpServletRequest request) throws Exception {

        ServiceContext context = ApiUtils.createServiceContext(request);

        boolean inspireEnable = sm.getValueAsBool(Settings.SYSTEM_INSPIRE_ENABLE);

        if (!inspireEnable) {
            Log.info(Geonet.ATOM, "Inspire is disabled");
            throw new FeatureNotEnabledException("Inspire is disabled");
        }

        List<String> datasetIdentifiers;

        String datasetIdentifiersFilter = "";

        // If fileIdentifier is provided search only in the related datasets
        if (StringUtils.isNotEmpty(fileIdentifier)) {
            String id = dm.getMetadataId(fileIdentifier);
            if (id == null) throw new MetadataNotFoundEx("Metadata not found.");

            Element md = dm.getMetadata(id);
            String schema = dm.getMetadataSchema(id);

            // Check if allowed to the metadata
            Lib.resource.checkPrivilege(context, id, ReservedOperation.view);

            // Retrieve the datasets related to the service metadata
            datasetIdentifiers = InspireAtomUtil.extractRelatedDatasetsIdentifiers(schema, md, dm);

            String datasets = datasetIdentifiers.stream().map(StringEscapeUtils::escapeJson)
                .collect(Collectors.joining("\",\"", "\"", "\""));

            datasetIdentifiersFilter = String.format(", {\"terms\": {\n" +
                "      \"resourceIdentifier.code\": [%s]\n" +
                "    }}", datasets);
        }

        String privilegesFilter = buildPermissionsFilter(context);
        String jsonQuery = "{" +
            "    \"bool\": {" +
            "      \"must\": [" +
            "        {" +
            "          \"exists\": {" +
            "            \"field\": \"atomfeed\"" +
            "          }" +
            "        }" +
            "      ]," +
            "      \"filter\": [{" +
            "          \"query_string\": {" +
            "            \"query\": \"%s\"" +
            "        }" +
            "      }%s]" +
            "    }" +
            "}";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode esJsonQuery = objectMapper.readTree(String.format(jsonQuery, privilegesFilter, datasetIdentifiersFilter));

        final SearchResponse result = searchMan.query(
            esJsonQuery,
            FIELDLIST_CORE,
            0, 1000);

        Element feeds = new Element("feeds");

        // Loop over the results and retrieve feeds to add in results
        // First element in results (pos=0) is the summary, ignore it
        for (Hit hit : (List<Hit>) result.hits().hits()) {
            String id = objectMapper.convertValue(hit.source(), Map.class).get(Geonet.IndexFieldNames.ID).toString();
            InspireAtomFeed feed = service.findByMetadataId(Integer.parseInt(id));
            if (feed != null) {
                Element feedEl = Xml.loadString(feed.getAtom(), false);
                feeds.addContent((Content) feedEl.clone());
            } else {
                Log.debug(Geonet.ATOM, String.format("No feed available for %s", hit.id()));
            }
        }
        return feeds;
    }


    private String feedsAsHtml(
        String fileIdentifier,
        HttpServletRequest request) throws Exception {
        Element feeds = feedsAsXml(fileIdentifier, request);

        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        String language = IsoLanguagesMapper.iso639_2T_to_iso639_2B(locale.getISO3Language());
        language = XslUtil.twoCharLangCode(language, "eng").toLowerCase();

        return new XsltResponseWriter(null, "atom-feeds")
            .withXml(feeds)
            .withJson(String.format("catalog/locales/%s-v4.json", language))
            .withXsl("xslt/services/inspire-atom/search-results.xsl")
            .asHtml();
    }
}
