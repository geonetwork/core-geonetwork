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

package org.fao.geonet.api.records;

import com.google.common.collect.ImmutableSet;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.sources.http.ServletPathFinder;
import jeeves.services.ReadWriteController;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.es.EsHTTPProxy;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.records.rdf.RdfOutputManager;
import org.fao.geonet.api.records.rdf.RdfSearcher;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.guiapi.search.XsltResponseWriter;
import org.fao.geonet.kernel.*;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import static org.fao.geonet.api.ApiParams.*;
import static org.fao.geonet.kernel.mef.MEFLib.Version.Constants.MEF_V1_ACCEPT_TYPE;
import static org.fao.geonet.kernel.mef.MEFLib.Version.Constants.MEF_V2_ACCEPT_TYPE;
import static org.fao.geonet.kernel.search.EsSearchManager.FIELDLIST_CORE;
import static org.fao.geonet.kernel.search.EsSearchManager.FIELDLIST_UUID;
import static org.fao.geonet.kernel.search.IndexFields.SOURCE_CATALOGUE;

@RequestMapping(value = {
    "/{portal}/api/records"
})
@Tag(name = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("catalogs")
@ReadWriteController
public class CatalogApi {

    public static Set<String> FIELDLIST_PDF;

    static {
        FIELDLIST_PDF = ImmutableSet.<String>builder()
            .add(Geonet.IndexFieldNames.ID)
            .add(Geonet.IndexFieldNames.UUID)
            .add("tag")
            .add("codelist_spatialRepresentationType_text")
            .add("codelist_maintenanceAndUpdateFrequency_text")
            .add("format")
            .add("overview")
            .add("link")
            .add("standardName")
            .add("schema")
            .add("geom")
            .add(SOURCE_CATALOGUE)
            .add(Geonet.IndexFieldNames.DATABASE_CHANGE_DATE)
            .add("resourceTitleObject.default") // TODOES multilingual
            .add("resourceAbstractObject.default").build();
    }

    @Autowired
    ThesaurusManager thesaurusManager;
    @Autowired
    MetadataRepository metadataRepository;
    @Autowired
    IMetadataUtils metadataUtils;
    @Autowired
    DataManager dataManager;
    @Autowired
    GeonetworkDataDirectory dataDirectory;
    @Autowired
    SettingManager settingManager;
    @Autowired
    EsSearchManager searchManager;
    @Autowired
    AccessManager accessManage;
    @Autowired
    SettingInfo settingInfo;
    @Autowired
    private ServletContext servletContext;
    @Autowired
    EsHTTPProxy esHTTPProxy;
    @Autowired
    LanguageUtils languageUtils;

    /*
     * <p>Retrieve all parameters (except paging parameters) as a string.</p>
     */
    private static String paramsAsString(Map<String, String> requestParams) {
        String paramNonPaging = "";
        Iterator<Entry<String, String>> it = requestParams.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pair = it.next();
            if (!pair.getKey().equals("from") && !pair.getKey().equals("to")) {
                paramNonPaging = paramNonPaging + (paramNonPaging.equals("") ? "" : "&") + pair.getKey() + "=" + pair.getValue();
            }
        }
        return paramNonPaging;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get a set of metadata records as ZIP",
        description = "Metadata Exchange Format (MEF) is returned. MEF is a ZIP file containing " +
            "the metadata as XML and some others files depending on the version requested. " +
            "See http://geonetwork-opensource.org/manuals/trunk/eng/users/annexes/mef-format.html.")
    @RequestMapping(value = "/zip",
        method = RequestMethod.GET,
        consumes = {
            MediaType.ALL_VALUE
        },
        produces = {
            "application/zip",
            MEF_V1_ACCEPT_TYPE,
            MEF_V2_ACCEPT_TYPE
        })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return requested records as ZIP."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    @ResponseBody
    public void exportAsMef(
        @Parameter(description = API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @Parameter(
            description = "MEF file format.",
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "FULL")
            MEFLib.Format format,
        @Parameter(
            description = "With related records (parent and service).",
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "false")
            boolean withRelated,
        @Parameter(
            description = "Resolve XLinks in the records.",
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "true")
            boolean withXLinksResolved,
        @Parameter(
            description = "Preserve XLink URLs in the records.",
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "false")
            boolean withXLinkAttribute,
        @RequestParam(
            required = false,
            defaultValue = "true")
            boolean addSchemaLocation,
        @Parameter(description = "Download the approved version",
            required = false)
        @RequestParam(required = false, defaultValue = "true")
            boolean approved,
        @RequestHeader(
            value = HttpHeaders.ACCEPT,
            defaultValue = "application/x-gn-mef-2-zip"
        )
            String acceptHeader,
        @Parameter(hidden = true)
            HttpSession httpSession,
        @Parameter(hidden = true)
            HttpServletResponse response,
        @Parameter(hidden = true)
            HttpServletRequest request)
        throws Exception {

        // Get parameters
        Path file = null;
        Path stylePath = dataDirectory.getWebappDir().resolve(Geonet.Path.SCHEMAS);

        final UserSession session = ApiUtils.getUserSession(httpSession);
        Set<String> uuidList = ApiUtils.getUuidsParameterOrSelection(
            uuids, bucket, session);

        Log.info(Geonet.MEF, "Create export task for selected metadata(s).");
        SelectionManager selectionManger = SelectionManager.getManager(session);
        Log.info(Geonet.MEF, "Current record(s) in selection: " + uuidList.size());

        ServiceContext context = ApiUtils.createServiceContext(request);
        MEFLib.Version version = MEFLib.Version.find(acceptHeader);
        if (version == MEFLib.Version.V1) {
            throw new IllegalArgumentException("MEF version 1 only support one record. Use the /records/{uuid}/formatters/zip to retrieve that format");
        } else {
            Set<String> allowedUuid = new HashSet<String>();
            for (Iterator<String> iter = uuidList.iterator(); iter.hasNext(); ) {
                String uuid = iter.next();
                try {
                    ApiUtils.canViewRecord(uuid, request);
                    allowedUuid.add(uuid);
                } catch (Exception e) {
                    Log.debug(API.LOG_MODULE_NAME, String.format(
                        "Not allowed to export record '%s'.", uuid));
                }
            }

            // If provided uuid, export the metadata record only
            selectionManger.close(SelectionManager.SELECTION_METADATA);
            selectionManger.addAllSelection(SelectionManager.SELECTION_METADATA,
                allowedUuid);

            // MEF version 2 support multiple metadata record by file.
            if (withRelated) {
                int maxhits = Integer.parseInt(settingInfo.getSelectionMaxRecords());

                Set<String> tmpUuid = new HashSet<String>();
                for (Iterator<String> iter = allowedUuid.iterator(); iter.hasNext(); ) {
                    String uuid = iter.next();

                    // Search for children records
                    // and service record. At some point this might be extended to all type of relations.
                    final SearchResponse searchResponse = searchManager.query(
                        String.format("parentUuid:\"%s\" recordOperateOn:\"%s\"", uuid, uuid),
                        esHTTPProxy.buildPermissionsFilter(ApiUtils.createServiceContext(request)),
                        FIELDLIST_UUID, 0, maxhits);

                    Arrays.asList(searchResponse.getHits().getHits()).forEach(h ->
                        tmpUuid.add((String) h.getSourceAsMap().get(Geonet.IndexFieldNames.UUID)));
                }

                if (selectionManger.addAllSelection(SelectionManager.SELECTION_METADATA, tmpUuid)) {
                    Log.info(Geonet.MEF, "Child and services added into the selection");
                }
                allowedUuid = selectionManger.getSelection(SelectionManager.SELECTION_METADATA);
            }

            Log.info(Geonet.MEF, "Building MEF2 file with " + uuidList.size()
                + " records.");
            try {
                file = MEFLib.doMEF2Export(context, allowedUuid, format.toString(),
                    false, stylePath,
                    withXLinksResolved, withXLinkAttribute,
                    false, addSchemaLocation, approved);

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss");
                String fileName = String.format("%s-%s.zip",
                    settingManager.getSiteName().replace(" ", ""),
                    df.format(new Date()));

                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format(
                    "inline; filename=\"%s\"",
                    fileName
                ));
                response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(Files.size(file)));
                response.setContentType(MEFLib.Version.Constants.MEF_V2_ACCEPT_TYPE);
                FileUtils.copyFile(file.toFile(), response.getOutputStream());
            } finally {
                // -- Reset selection manager
                selectionManger.close(SelectionManager.SELECTION_METADATA);
            }
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get a set of metadata records as PDF",
        description = "The PDF is a short summary of each records with links to the complete metadata record in different format (ie. landing page on the portal, XML)")
    @RequestMapping(value = "/pdf",
        method = RequestMethod.GET,
        consumes = {
            MediaType.ALL_VALUE
        },
        produces = {
            "application/pdf"
        })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return requested records as PDF."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    @ResponseBody
    public void exportAsPdf(
        @Parameter(description = API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @Parameter(hidden = true)
        @RequestParam
            Map<String, String> allRequestParams,
        @Parameter(hidden = true)
            HttpSession httpSession,
        @Parameter(hidden = true)
            HttpServletResponse httpResponse,
        @Parameter(hidden = true)
            HttpServletRequest httpRequest)
        throws Exception {


        final UserSession session = ApiUtils.getUserSession(httpSession);
        Set<String> uuidList = ApiUtils.getUuidsParameterOrSelection(
            uuids, bucket, session);

        int maxhits = Integer.parseInt(settingInfo.getSelectionMaxRecords());

        final SearchResponse searchResponse = searchManager.query(
            String.format(
                "uuid:(\"%s\")",
                String.join("\" or \"", uuidList)),
            esHTTPProxy.buildPermissionsFilter(ApiUtils.createServiceContext(httpRequest)),
            FIELDLIST_PDF, 0, maxhits);


        Map<String, Object> params = new HashMap<>();
        Element request = new Element("request");
        allRequestParams.entrySet().forEach(e -> {
            Element n = new Element(e.getKey());
            n.setText(e.getValue());
            request.addContent(n);
        });

        Element response = new Element("response");
        Arrays.asList(searchResponse.getHits().getHits()).forEach(h -> {
            Element r = new Element("metadata");
            final Map<String, Object> source = h.getSourceAsMap();
            source.entrySet().forEach(e -> {
                Object v = e.getValue();
                if (v instanceof String) {
                    Element t = new Element(e.getKey());
                    t.setText((String) v);
                    r.addContent(t);
                } else if (v instanceof HashMap && e.getKey().endsWith("Object")) {
                    Element t = new Element(e.getKey());
                    Map<String, String> textFields = (HashMap) e.getValue();
                    t.setText(textFields.get("default"));
                    r.addContent(t);
                } else if (v instanceof ArrayList && e.getKey().equals("link")) {
                    //landform|Physiography of North and Central Eurasia Landform|http://geonetwork3.fao.org/ows/7386_landf|OGC:WMS-1.1.1-http-get-map|application/vnd.ogc.wms_xml
                    ((ArrayList) v).forEach(i -> {
                        Element t = new Element(e.getKey());
                        Map<String, String> linkProperties = (HashMap) i;
                        t.setText(linkProperties.get("description") + "|" + linkProperties.get("name") + "|" + linkProperties.get("url") + "|" + linkProperties.get("protocol"));
                        r.addContent(t);
                    });
                } else if (v instanceof HashMap && e.getKey().equals("overview")) {
                    Element t = new Element(e.getKey());
                    Map<String, String> overviewProperties = (HashMap) v;
                    t.setText(overviewProperties.get("url") + "|" + overviewProperties.get("name"));
                    r.addContent(t);
                } else if (v instanceof ArrayList) {
                    ((ArrayList) v).forEach(i -> {
                        if (i instanceof HashMap && e.getKey().equals("overview")) {
                            Element t = new Element(e.getKey());
                            Map<String, String> overviewProperties = (HashMap) i;
                            t.setText(overviewProperties.get("url") + "|" + overviewProperties.get("name"));
                            r.addContent(t);
                        } else {
                            Element t = new Element(e.getKey());
                            t.setText((String) i);
                            r.addContent(t);
                        }
                    });
                } else {
                    Element t = new Element(e.getKey());
                    t.setText(v.toString());
                    r.addContent(t);
                }
            });
            response.addContent(r);
        });

        Locale locale = languageUtils.parseAcceptLanguage(httpRequest.getLocales());
        String language = LanguageUtils.locale2gnCode(locale.getISO3Language());
        language = XslUtil.twoCharLangCode(language, "eng").toLowerCase();

        new XsltResponseWriter("env", "search")
            .withJson(String.format("catalog/locales/%s-core.json", language))
            .withJson(String.format("catalog/locales/%s-search.json", language))
            .withXml(response)
            .withParams(params)
            .withXsl("xslt/services/pdf/portal-present-fop.xsl")
            .asPdf(httpResponse, settingManager.getValue("metadata/pdfReport/pdfName"));
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get a set of metadata records as CSV",
        description = "The CSV is a short summary of each records.")
    @RequestMapping(value = "/csv",
        method = RequestMethod.GET,
        consumes = {
            MediaType.ALL_VALUE
        },
        produces = {
            "text/csv"
        })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return requested records as CSV."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    @ResponseBody
    public void exportAsCsv(
        @Parameter(description = API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @Parameter(hidden = true)
        @RequestParam
            Map<String, String> allRequestParams,
        @Parameter(hidden = true)
            HttpSession httpSession,
        @Parameter(hidden = true)
            HttpServletResponse httpResponse,
        @Parameter(hidden = true)
            HttpServletRequest httpRequest)
        throws Exception {
        final UserSession session = ApiUtils.getUserSession(httpSession);
        Set<String> uuidList = ApiUtils.getUuidsParameterOrSelection(
            uuids, bucket, session);

        int maxhits = Integer.parseInt(settingInfo.getSelectionMaxRecords());
        ServiceContext context = ApiUtils.createServiceContext(httpRequest);

        final SearchResponse searchResponse = searchManager.query(
            String.format("uuid:(\"%s\")", String.join("\" or \"", uuidList)),
            esHTTPProxy.buildPermissionsFilter(ApiUtils.createServiceContext(httpRequest)),
            FIELDLIST_CORE, 0, maxhits);

        Element response = new Element("response");
        Arrays.asList(searchResponse.getHits().getHits()).forEach(h -> {
            try {
                response.addContent(
                    dataManager.getMetadata(
                        context,
                        (String) h.getSourceAsMap().get("id"),
                        false, false, false));
            } catch (Exception e) {
            }
        });

        Element r = new XsltResponseWriter(null,"search")
            .withXml(response)
            .withXsl("xslt/services/csv/csv-search.xsl")
            .asElement();
        httpResponse.getWriter().write(r.getText());
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get catalog content as RDF. This endpoint supports the same Lucene query parameters as for the GUI search.",
        description = ".")
    @RequestMapping(
        method = RequestMethod.GET,
        consumes = {
            MediaType.ALL_VALUE
        },
        produces = {
            "application/rdf+xml", "*"
        })
    @Parameters({
        @Parameter(name = "from", description = "Indicates the start position in a sorted list of matches that the client wants to use as the beginning of a page result.", required = false,
            in = ParameterIn.QUERY, schema = @Schema(type = "integer", format = "int32", defaultValue = "1")),
        @Parameter(name = "hitsPerPage", description = "Indicates the number of hits per page.", required = false,
            in = ParameterIn.QUERY, schema = @Schema(type = "integer", format = "int32")),
        //@Parameter(name="to", value = "Indicates the end position in a sorted list of matches that the client wants to use as the ending of a page result", required = false, defaultValue ="10", dataType = "int", paramType = "query"),
        @Parameter(name = "any", description = "Search key", required = false,
            in = ParameterIn.QUERY, schema = @Schema(type = "string")),
        @Parameter(name = "title", description = "A search key for the title.", required = false,
            in = ParameterIn.QUERY, schema = @Schema(type = "string")),
        @Parameter(name = "facet.q", description = "A search facet in the Lucene index. Use the GeoNetwork GUI search to generate the suitable filter values. Example: standard/dcat-ap&createDateYear/2018&sourceCatalog/6d93613e-2b76-4e26-94af-4b4c420a1758 (filter by creation year and source catalog).", required = false,
            in = ParameterIn.QUERY, schema = @Schema(type = "string")),
        @Parameter(name = "sortBy", description = "Lucene sortBy criteria. Relevant values: relevance, title, changeDate.", required = false,
            in = ParameterIn.QUERY, schema = @Schema(type = "string")),
        @Parameter(name = "sortOrder", description = "Sort order. Possible values: reverse.", required = false,
            in = ParameterIn.QUERY, schema = @Schema(type = "string")),
        @Parameter(name = "similarity", description = "Use the Lucene FuzzyQuery. Values range from 0.0 to 1.0 and defaults to 0.8.", required = false,
            in = ParameterIn.QUERY, schema = @Schema(type = "number", format = "float", defaultValue = "0.8"))
    })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return the catalog content as RDF."
//            responseHeaders = {
//                @ResponseHeader(name = "Link", description = " This response header parameter is used to indicate any of the links defined by LDP Paging: first page links, next page links, last page links, previous page links. " +
//                    "First page link: " +
//                    "a link to the first in-sequence page resource P1 (first) of a page sequence. The first page is the one that a LDP Paging server redirects to (303 response) in response to a retrieval request for the paged resource's URI. Syntactically, a HTTP Link <P1>; rel=\"first\" header [RFC5988]. " +
//                    "Next page link: " +
//                    "a link to the next in-sequence page resource of a page sequence. Syntactically, a HTTP Link <Pi>; rel=\"next\" header [RFC5988] where the context URI identifies some Pi=1 (first)...n-1 (next to last) and the target URI identifies Pi+1. " +
//                    "Last page link: " +
//                    "a link to the last in-sequence page resource Pn (last) of a page sequence. The last page is the page that terminates a forward traversal, because it contains no next page link. Syntactically, a HTTP Link <Pn>; rel=\"last\" header [RFC5988]. " +
//                    "Previous page link: " +
//                    "a link to the previous in-sequence page resource of a page sequence Syntactically, a HTTP Link <Pi>; rel=\"prev\" header [RFC5988] where the context URI identifies some Pi=2...n (last) and the target URI identifies Pi-1. "
//                    , response = String.class),
//                @ResponseHeader(name = "ETag", description = "The ETag HTTP response header is an identifier for a specific version of a resource. If the resource at a given URL changes, a new Etag value must be generated. On this API, the ETag value is the version token of the Lucene index. ")
//            }
        ),
        @ApiResponse(responseCode = "303", description = "Redirect the client to the first in-sequence page resource. This happens when the paging parameters (from, hitsPerPage) are not included in the request.")
    })
    public
    @ResponseBody
    void getAsRdf(
        @Parameter(hidden = true)
        @RequestParam
            Map<String, String> allRequestParams,
        HttpServletResponse response,
        HttpServletRequest request
    ) throws Exception {
        //Retrieve the host URL from the GeoNetwork settings
        String hostURL = getHostURL();

        //Retrieve the paging parameter values (if present)
        int hitsPerPage = (allRequestParams.get("hitsPerPage") != null ? Integer.parseInt(allRequestParams.get("hitsPerPage")) : 0);
        int from = (allRequestParams.get("from") != null ? Integer.parseInt(allRequestParams.get("from")) : 0);
        int to = (allRequestParams.get("to") != null ? Integer.parseInt(allRequestParams.get("to")) : 0);

        //If the paging parameters (from, hitsPerPage) are not included in the request, redirect the client to the first in-sequence page resource. Use default paging parameter values.
        if (hitsPerPage <= 0 || from <= 0) {
            if (hitsPerPage <= 0) {
                hitsPerPage = 10;
                allRequestParams.put("hitsPerPage", Integer.toString(hitsPerPage));
            }
            if (from <= 0) {
                from = 1;
                allRequestParams.put("from", Integer.toString(from));
            }
            response.setStatus(303);
            response.setHeader("Location", hostURL + request.getRequestURI() + "?" + paramsAsString(allRequestParams) + "&from=1&to=" + hitsPerPage);
            return;
        }

        //Lower 'from' to the greatest multiple of hitsPerPage (by substracting the modulus).
        if (hitsPerPage > 1) {
            from = from - (from % hitsPerPage) + 1;
        }
        //Check if the constraint to=from+hitsPerPage-1 holds. Otherwise, force it.
        if (to <= 0) {
            if (from + hitsPerPage - 1 > 0) {
                to = from + hitsPerPage - 1;
            } else {
                to = 10;
            }
        }
        allRequestParams.put("to", Integer.toString(to));
        allRequestParams.put("hitsPerPage", Integer.toString(hitsPerPage));
        allRequestParams.put("from", Integer.toString(from));

        ServiceContext context = ApiUtils.createServiceContext(request);
        RdfOutputManager manager = new RdfOutputManager(
            thesaurusManager.buildResultfromThTable(context), hitsPerPage);

        // Copy all request parameters
        /// Mimic old Jeeves param style
        Element params = new Element("params");
        allRequestParams.forEach((k, v) -> {
            params.addContent(new Element(k).setText(v));
        });

        // Perform the search on the Lucene Index
        RdfSearcher rdfSearcher = new RdfSearcher(params, context);
        List results = rdfSearcher.search(context);
        rdfSearcher.close();

        // Calculates the pagination information, needed for the LDP Paging and Hydra Paging
        int numberMatched = rdfSearcher.getSize();
        int firstPageFrom = numberMatched > 0 ? 1 : 0;
        int firstPageTo = numberMatched > hitsPerPage ? hitsPerPage : numberMatched;
        int nextFrom = to < numberMatched ? to + 1 : to;
        int nextTo = to + hitsPerPage < numberMatched ? to + hitsPerPage : numberMatched;
        int prevFrom = from - hitsPerPage > 0 ? from - hitsPerPage : 1;
        int prevTo = to - hitsPerPage > 0 ? to - hitsPerPage : numberMatched;
        int lastPageFrom = 0 < (numberMatched % hitsPerPage) ? numberMatched - (numberMatched % hitsPerPage) + 1 : (numberMatched - hitsPerPage + 1 > 0 ? numberMatched - hitsPerPage + 1 : numberMatched);
        long versionTokenETag = rdfSearcher.getVersionToken();
        String canonicalURL = hostURL + request.getRequestURI();
        String currentPage = canonicalURL + "?" + paramsAsString(allRequestParams) + "&from=" + from + "&to=" + to;
        String lastPage = canonicalURL + "?" + paramsAsString(allRequestParams) + "&from=" + lastPageFrom + "&to=" + numberMatched;
        String firstPage = canonicalURL + "?" + paramsAsString(allRequestParams) + "&from=" + firstPageFrom + "&to=" + firstPageTo;
        String previousPage = canonicalURL + "?" + paramsAsString(allRequestParams) + "&from=" + prevFrom + "&to=" + prevTo;
        String nextPage = canonicalURL + "?" + paramsAsString(allRequestParams) + "&from=" + nextFrom + "&to=" + nextTo;

        // Hydra Paging information (see also: http://www.hydra-cg.com/spec/latest/core/)
        String hydraPagedCollection = "<hydra:PagedCollection xmlns:hydra=\"http://www.w3.org/ns/hydra/core#\" rdf:about=\"" + currentPage.replaceAll("&", "&amp;") + "\">\n" +
            "<rdf:type rdf:resource=\"hydra:PartialCollectionView\"/>" +
            "<hydra:lastPage>" + lastPage.replaceAll("&", "&amp;") + "</hydra:lastPage>\n" +
            "<hydra:totalItems rdf:datatype=\"http://www.w3.org/2001/XMLSchema#integer\">" + numberMatched + "</hydra:totalItems>\n" +
            ((prevFrom <= prevTo && prevFrom < from && prevTo < to) ? "<hydra:previousPage>" + previousPage.replaceAll("&", "&amp;") + "</hydra:previousPage>\n" : "") +
            ((nextFrom <= nextTo && from < nextFrom && to < nextTo) ? "<hydra:nextPage>" + nextPage.replaceAll("&", "&amp;") + "</hydra:nextPage>\n" : "") +
            "<hydra:firstPage>" + firstPage.replaceAll("&", "&amp;") + "</hydra:firstPage>\n" +
            "<hydra:itemsPerPage rdf:datatype=\"http://www.w3.org/2001/XMLSchema#integer\">" + hitsPerPage + "</hydra:itemsPerPage>\n" +
            "</hydra:PagedCollection>";
        // Construct the RDF output
        File rdfFile = manager.createRdfFile(context, results, 1, hydraPagedCollection);

        try (
            ServletOutputStream out = response.getOutputStream();
            InputStream in = new FileInputStream(rdfFile)
        ) {
            byte[] bytes = new byte[1024];
            int bytesRead;

            response.setContentType("application/rdf+xml");

            //Set the Lucene versionToken as ETag response header parameter
            response.addHeader("ETag", Long.toString(versionTokenETag));
            //Include the response header "link" parameters as suggested by the W3C Linked Data Platform paging specification (see also: https://www.w3.org/2012/ldp/hg/ldp-paging.html).
            response.addHeader("Link", "<http://www.w3.org/ns/ldp#Page>; rel=\"type\"");
            response.addHeader("Link", canonicalURL + "; rel=\"canonical\"; etag=" + versionTokenETag);

            response.addHeader("Link", "<" + firstPage + "> ; rel=\"first\"");
            if (nextFrom <= nextTo && from < nextFrom && to < nextTo) {
                response.addHeader("Link", "<" + nextPage + "> ; rel=\"next\"");
            }
            if (prevFrom <= prevTo && prevFrom < from && prevTo < to) {
                response.addHeader("Link", "<" + previousPage + "> ; rel=\"prev\"");
            }
            response.addHeader("Link", "<" + lastPage + "> ; rel=\"last\"");

            //Write the paged RDF result to the message body
            while ((bytesRead = in.read(bytes)) != -1) {
                out.write(bytes, 0, bytesRead);
            }
        } catch (FileNotFoundException e) {
            Log.error(API.LOG_MODULE_NAME, "Get catalog content as RDF. Error: " + e.getMessage(), e);
        } catch (IOException e) {
            Log.error(API.LOG_MODULE_NAME, "Get catalog content as RDF. Error: " + e.getMessage(), e);
        }
    }

    /*
     * <p>Retrieve the base URL from the GeoNetwork settings.</p>
     */
    private String getHostURL() {
        //Retrieve the base URL from the GeoNetwork settings
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        SettingManager sm = applicationContext.getBean(SettingManager.class);
        ServletPathFinder pathFinder = new ServletPathFinder(servletContext);
        return sm.getBaseURL().replaceAll(pathFinder.getBaseUrl() + "/", "");

    }
}
