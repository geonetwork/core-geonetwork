/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
import jeeves.services.ReadWriteController;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.elasticsearch.action.search.SearchResponse;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.records.model.related.AssociatedRecord;
import org.fao.geonet.api.records.model.related.RelatedItemType;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.guiapi.search.XsltResponseWriter;
import org.fao.geonet.kernel.*;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.search.EsFilterBuilder;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.Log;
import org.fao.geonet.web.DefaultLanguage;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.fao.geonet.api.ApiParams.*;
import static org.fao.geonet.kernel.mef.MEFLib.Version.Constants.MEF_V1_ACCEPT_TYPE;
import static org.fao.geonet.kernel.mef.MEFLib.Version.Constants.MEF_V2_ACCEPT_TYPE;
import static org.fao.geonet.kernel.search.EsSearchManager.FIELDLIST_CORE;
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
            .add(Geonet.IndexFieldNames.RESOURCETITLE + "Object")
            .add(Geonet.IndexFieldNames.RESOURCEABSTRACT + "Object").build();
    }

    @Autowired
    DefaultLanguage defaultLanguage;
    @Autowired
    ThesaurusManager thesaurusManager;
    @Autowired
    MetadataRepository metadataRepository;
    @Autowired
    IMetadataUtils metadataUtils;
    @Autowired
    SchemaManager schemaManager;
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
    LanguageUtils languageUtils;
    @Autowired
    IsoLanguagesMapper isoLanguagesMapper;
    @Autowired
    private ServletContext servletContext;



    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get a set of metadata records as ZIP",
        description = "Metadata Exchange Format (MEF) is returned. MEF is a ZIP file containing " +
            "the metadata as XML and some others files depending on the version requested. " +
            "See https://docs.geonetwork-opensource.org/latest/annexes/mef-format/.")
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
        String acceptHeader = StringUtils.isBlank(request.getHeader(HttpHeaders.ACCEPT)) ? "application/x-gn-mef-2-zip" : request.getHeader(HttpHeaders.ACCEPT);
        MEFLib.Version version = MEFLib.Version.find(acceptHeader);
        if (version == MEFLib.Version.V1) {
            throw new IllegalArgumentException("MEF version 1 only support one record. Use the /records/{uuid}/formatters/zip to retrieve that format");
        } else {
            Set<String> allowedUuid = new HashSet<>();
            for (String uuid : uuidList) {
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

                Set<String> tmpUuid = new HashSet<>();
                for (String uuid : allowedUuid) {
                    Map<RelatedItemType, List<AssociatedRecord>> associated =
                        MetadataUtils.getAssociated(context,
                            metadataRepository.findOneByUuid(uuid),
                            RelatedItemType.values(), 0, maxhits);

                    associated.forEach((type, list) -> {
                        list.forEach(r -> {
                            tmpUuid.add(r.getUuid());
                        });
                    });
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
                // Delete the temporary file
                if (file != null) {
                    FileUtils.deleteQuietly(file.toFile());
                }
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
        @RequestParam(
            required = false,
            defaultValue = "eng"
        )
        String language,
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
                "uuid:(\"%s\") AND NOT draft:\"y\"", // Skip working copies as duplicate UUIDs cause the PDF xslt to fail
                String.join("\" OR \"", uuidList)),
            EsFilterBuilder.buildPermissionsFilter(ApiUtils.createServiceContext(httpRequest)),
            FIELDLIST_PDF, 0, maxhits);


        Map<String, Object> params = new HashMap<>();
        Element request = new Element("request");
        allRequestParams.forEach((key, value) -> {
            Element n = new Element(key);
            n.setText(value);
            request.addContent(n);
        });

        if (!languageUtils.getUiLanguages().contains(language)) {
            language = languageUtils.getDefaultUiLanguage();
        }

        String langCode = "lang" + language;

        Element response = new Element("response");
        Arrays.asList(searchResponse.getHits().getHits()).forEach(h -> {
            Element r = new Element("metadata");
            final Map<String, Object> source = h.getSourceAsMap();
            source.forEach((key, v) -> {
                if (v instanceof String) {
                    Element t = new Element(key);
                    t.setText((String) v);
                    r.addContent(t);
                } else if (v instanceof HashMap && key.endsWith("Object")) {
                    Element t = new Element(key);
                    Map<String, String> textFields = (HashMap) v;
                    String textValue = textFields.get(langCode) != null ? textFields.get(langCode) : textFields.get("default");
                    t.setText(textValue);
                    r.addContent(t);
                } else if (v instanceof ArrayList && key.equals("link")) {
                    //landform|Physiography of North and Central Eurasia Landform|http://geonetwork3.fao.org/ows/7386_landf|OGC:WMS-1.1.1-http-get-map|application/vnd.ogc.wms_xml
                    ((ArrayList) v).forEach(i -> {
                        Element t = new Element(key);
                        Map<String, String> linkProperties = (HashMap) i;
                        t.setText(linkProperties.get("description") + "|" + linkProperties.get("name") + "|" + linkProperties.get("url") + "|" + linkProperties.get("protocol"));
                        r.addContent(t);
                    });
                } else if (v instanceof HashMap && key.equals("overview")) {
                    Element t = new Element(key);
                    Map<String, String> overviewProperties = (HashMap) v;
                    t.setText(overviewProperties.get("url") + "|" + overviewProperties.get("name"));
                    r.addContent(t);
                } else if (v instanceof ArrayList) {
                    ((ArrayList) v).forEach(i -> {
                        if (i instanceof HashMap && key.equals("overview")) {
                            Element t = new Element(key);
                            Map<String, String> overviewProperties = (HashMap) i;
                            t.setText(overviewProperties.get("url") + "|" + overviewProperties.get("name"));
                            r.addContent(t);
                        } else if (i instanceof HashMap) {
                            Element t = new Element(key);
                            Map<String, String> tags = (HashMap) i;
                            t.setText(tags.get("default")); // TODOES: Multilingual support
                            r.addContent(t);
                        } else {
                            Element t = new Element(key);
                            t.setText((String) i);
                            r.addContent(t);
                        }
                    });
                } else if (v instanceof HashMap && key.equals("geom")) {
                    Element t = new Element(key);
                    t.setText(((HashMap) v).get("coordinates").toString());
                    r.addContent(t);
                } else if (v instanceof HashMap) {
                    // Skip.
                } else {
                    Element t = new Element(key);
                    t.setText(v.toString());
                    r.addContent(t);
                }
            });
            response.addContent(r);
        });


        String language2Code = XslUtil.twoCharLangCode(language, "eng").toLowerCase();

        new XsltResponseWriter("env", "search", language)
            .withJson(String.format("catalog/locales/%s-v4.json", language2Code))
            .withJson(String.format("catalog/locales/%s-core.json", language2Code))
            .withJson(String.format("catalog/locales/%s-search.json", language2Code))
            .withXml(response)
            .withParams(params)
            .withXsl("xslt/services/pdf/portal-present-fop.xsl")
            .asPdf(httpResponse, replaceFilenamePlaceholder(settingManager.getValue("metadata/pdfReport/pdfName"), "pdf"));

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
        @RequestParam(
            required = false,
            defaultValue = "eng"
        )
        String language,
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
            EsFilterBuilder.buildPermissionsFilter(ApiUtils.createServiceContext(httpRequest)),
            FIELDLIST_CORE, 0, maxhits);

        Element response = new Element("response");
        Arrays.asList(searchResponse.getHits().getHits()).forEach(h -> {
            try {
                response.addContent(
                    dataManager.getMetadata(
                        context,
                        (String) h.getSourceAsMap().get("id"),
                        false, false, false));
            } catch (Exception ignored) {
            }
        });

            if (!languageUtils.getUiLanguages().contains(language)) {
                language = languageUtils.getDefaultUiLanguage();
            }

            Element r = new XsltResponseWriter(null, "search", language)
                .withParams(allRequestParams.entrySet().stream()
                    .collect(Collectors.toMap(
                        Entry::getKey,
                        Entry::getValue)))
            .withXml(response)
            .withXsl("xslt/services/csv/csv-search.xsl")
            .asElement();

        // Determine filename to use
        String fileName = replaceFilenamePlaceholder(settingManager.getValue("metadata/csvReport/csvName"), "csv");

        httpResponse.setContentType("text/csv");
        httpResponse.addHeader("Content-Disposition", "attachment; filename=" + fileName);
        httpResponse.setContentLength(r.getText().length());
        httpResponse.getWriter().write(r.getText());
    }

    private String replaceFilenamePlaceholder(String fileName, String extension) {
        // Checks for a parameter documentFileName with the document file name,
        // otherwise uses a default value
        if (StringUtils.isEmpty(fileName)) {
            fileName = "document." + extension;

        } else {
            if (!fileName.endsWith("." + extension)) {
                fileName = fileName + "." + extension;
            }

            Map<String, String> values = new HashMap<String, String>();
            values.put("siteName", settingManager.getSiteName());

            Calendar c = Calendar.getInstance();
            values.put("year", c.get(Calendar.YEAR) + "");
            values.put("month", c.get(Calendar.MONTH) + "");
            values.put("day", c.get(Calendar.DAY_OF_MONTH) + "");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            values.put("date", dateFormat.format(c.getTime()));
            values.put("datetime", datetimeFormat.format(c.getTime()));

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss");
            values.put("ISOdatetime", df.format(new Date()));

            StrSubstitutor sub = new StrSubstitutor(values, "{", "}");
            fileName = sub.replace(fileName);

        }
        return fileName;
    }
}
