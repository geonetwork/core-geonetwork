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

import com.google.gson.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.records.model.related.FCRelatedMetadataItem.FeatureType.AttributeTable;
import org.fao.geonet.api.records.model.related.*;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.fao.geonet.api.ApiParams.*;

import static org.fao.geonet.kernel.mef.MEFLib.Version.Constants.MEF_V1_ACCEPT_TYPE;
import static org.fao.geonet.kernel.mef.MEFLib.Version.Constants.MEF_V2_ACCEPT_TYPE;

@RequestMapping(value = {
    "/{portal}/api/records"
})
@Tag(name = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("records")
@ReadWriteController
public class MetadataApi {

    @Autowired
    SchemaManager schemaManager;

    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    DataManager dataManager;

    @Autowired
    MetadataRepository metadataRepository;

    @Autowired
    IMetadataUtils metadataUtils;

    @Autowired
    GeonetworkDataDirectory dataDirectory;

    @Autowired
    @Qualifier("apiMessages")
    private ResourceBundleMessageSource messages;

    private ApplicationContext context;

    @Autowired
    EsSearchManager esSearchManager;

    public static RelatedResponse getRelatedResources(
        String language, ServiceContext context,
        AbstractMetadata md, RelatedItemType[] type, int start, int rows) throws Exception {
        // TODO PERF: ByPass XSL processing and create response directly
        // At least for related metadata and keep XSL only for links
        Element raw = new Element("root").addContent(Arrays.asList(
            new Element("gui").addContent(Arrays.asList(
                new Element("language").setText(language),
                new Element("url").setText(context.getBaseUrl())
            )),
            MetadataUtils.getRelated(context, md.getId(), md.getUuid(), type, start, start + rows)
        ));
        Path relatedXsl = ApplicationContextHolder.get()
            .getBean(GeonetworkDataDirectory.class).getWebappDir()
            .resolve("xslt/services/metadata/relation.xsl");

        final Element transform = Xml.transform(raw, relatedXsl);
        RelatedResponse response = (RelatedResponse) Xml.unmarshall(transform, RelatedResponse.class);
        return response;
    }

    public synchronized void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Get a metadata record",
        description = "Accept header should indicate which is the appropriate format " +
            "to return. It could be text/html, application/xml, application/zip, ..." +
            "If no appropriate Accept header found, the XML format is returned." +
            "Depending on the accept header the appropriate formatter is used. " +
            "When requesting a ZIP, a MEF version 2 file is returned. " +
            "When requesting HTML, the default formatter is used.")
    @RequestMapping(value = "/{metadataUuid:.+}",
        method = RequestMethod.GET,
        produces = {
            MediaType.TEXT_HTML_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_XHTML_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE,
            "application/pdf",
            "application/zip",
            MEF_V1_ACCEPT_TYPE,
            MEF_V2_ACCEPT_TYPE,
            MediaType.ALL_VALUE
        })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return the record.",
            content = @Content(schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW,
            content = {
                @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "string")),
                @Content(mediaType = MediaType.APPLICATION_XML_VALUE, schema = @Schema(type = "string")),
                @Content(mediaType = MediaType.APPLICATION_XHTML_XML_VALUE, schema = @Schema(type = "string"))
            }),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND,
            content = {
                @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "string")),
                @Content(mediaType = MediaType.APPLICATION_XML_VALUE, schema = @Schema(type = "string")),
                @Content(mediaType = MediaType.APPLICATION_XHTML_XML_VALUE, schema = @Schema(type = "string"))
            })
    })
    public String getRecord(
        @Parameter(description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
        String metadataUuid,
        @Parameter(description = "Formatter to use on the record view page. " +
            "If invalid or not specified, no redirect to the record view page is performed.")
        @RequestParam(required = false)
        String recordViewFormatter,
        HttpServletResponse response,
        HttpServletRequest request
    )
        throws Exception {
        try {
            ApiUtils.canViewRecord(metadataUuid, request);
        } catch (SecurityException e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
        }

        // Check if a redirect to the record view page is required.
        String redirect = getRedirect(recordViewFormatter, languageUtils.getIso3langCode(request.getLocales()), metadataUuid);

        // If a redirect is required, perform it.
        if (redirect != null) {
            return redirect;
        }

        String acceptHeader = StringUtils.isBlank(request.getHeader(HttpHeaders.ACCEPT)) ? MediaType.APPLICATION_XML_VALUE : request.getHeader(HttpHeaders.ACCEPT);
        List<String> accept = Arrays.asList(acceptHeader.split(","));

        String defaultFormatter = "xsl-view";
        if (accept.contains(MediaType.TEXT_HTML_VALUE)
            || accept.contains(MediaType.APPLICATION_XHTML_XML_VALUE)
            || accept.contains("application/pdf")) {
            return "forward:" + (metadataUuid + "/formatters/" + defaultFormatter);
        } else if (accept.contains(MediaType.APPLICATION_XML_VALUE)) {
            return "forward:" + (metadataUuid + "/formatters/xml");
        } else if (accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return "forward:" + (metadataUuid + "/formatters/json");
        } else if (accept.contains("application/zip")
            || accept.contains(MEF_V1_ACCEPT_TYPE)
            || accept.contains(MEF_V2_ACCEPT_TYPE)) {
            return "forward:" + (metadataUuid + "/formatters/zip");
        } else {
            // FIXME this else is never reached because any of the accepted medias match one of the previous if conditions.
            response.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XHTML_XML_VALUE);
            //response.sendRedirect(metadataUuid + "/formatters/" + defaultFormatter);
            return "forward:" + (metadataUuid + "/formatters/" + defaultFormatter);
        }
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Get metadata record permalink",
        description = "Permalink is by default the landing page formatter but can be configured in the admin console > settings. If the record as a DOI and if enabled in the settings, then it takes priority.")
    @GetMapping(value = "/{metadataUuid:.+}/permalink",
        consumes = MediaType.ALL_VALUE,
        produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return the permalink URL."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND)
    })
    public ResponseEntity<String> getRecordPermalink(
        @Parameter(description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
        String metadataUuid,
        HttpServletResponse response,
        HttpServletRequest request
    )
        throws Exception {
        AbstractMetadata metadata;
        try {
            metadata = ApiUtils.canViewRecord(metadataUuid, request);
        } catch (SecurityException e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
        }

        String language = languageUtils.getIso3langCode(request.getLocales());

        return new ResponseEntity<>(metadataUtils.getPermalink(metadata.getUuid(), language), HttpStatus.OK);
    }


    @io.swagger.v3.oas.annotations.Operation(summary = "Get a metadata record as JSON")
    @RequestMapping(value = "/{metadataUuid}/formatters/json",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return the record.",
            content = @Content(schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    @ResponseBody
    public Object getRecordAsJson(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
        String metadataUuid,
        @Parameter(description = "Add XSD schema location based on standard configuration " +
            "(see schema-ident.xml).",
            required = false)
        @RequestParam(required = false, defaultValue = "true")
        boolean addSchemaLocation,
        @Parameter(description = "Increase record popularity",
            required = false)
        @RequestParam(required = false, defaultValue = "true")
        boolean increasePopularity,
        @Parameter(description = "Add geonet:info details",
            required = false)
        @RequestParam(required = false, defaultValue = "false")
        boolean withInfo,
        @Parameter(description = "Download as a file",
            required = false)
        @RequestParam(required = false, defaultValue = "false")
        boolean attachment,
        @Parameter(description = "Download the approved version",
            required = false)
        @RequestParam(required = false, defaultValue = "true")
        boolean approved,
        HttpServletResponse response,
        HttpServletRequest request
    )
        throws Exception {
        return getRecordAs(metadataUuid, addSchemaLocation, increasePopularity, withInfo, attachment, approved, response, request, MediaType.APPLICATION_JSON);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Get a metadata record as XML")
    @RequestMapping(value = "/{metadataUuid}/formatters/xml",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_XML_VALUE
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return the record.",
            content = @Content(schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    @ResponseBody
    public Object getRecordAsXml(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
        String metadataUuid,
        @Parameter(description = "Add XSD schema location based on standard configuration " +
            "(see schema-ident.xml).",
            required = false)
        @RequestParam(required = false, defaultValue = "true")
        boolean addSchemaLocation,
        @Parameter(description = "Increase record popularity",
            required = false)
        @RequestParam(required = false, defaultValue = "true")
        boolean increasePopularity,
        @Parameter(description = "Add geonet:info details",
            required = false)
        @RequestParam(required = false, defaultValue = "false")
        boolean withInfo,
        @Parameter(description = "Download as a file",
            required = false)
        @RequestParam(required = false, defaultValue = "false")
        boolean attachment,
        @Parameter(description = "Download the approved version",
            required = false)
        @RequestParam(required = false, defaultValue = "true")
        boolean approved,
        HttpServletResponse response,
        HttpServletRequest request
    )
        throws Exception {
        return getRecordAs(metadataUuid, addSchemaLocation, increasePopularity, withInfo, attachment, approved, response, request, MediaType.APPLICATION_XML);
    }

    /**
     * Get records based on media type. Supports xml or json
     * @return It will default to xml if the media type is not known.
     */

    /**
     * Get records based on media type.
     *
     * @param metadataUuid       Record UUID.
     * @param addSchemaLocation  Add XSD schema location based on standard configuration (see schema-ident.xml).
     * @param increasePopularity Increase record popularity
     * @param withInfo           Add geonet:info details
     * @param attachment         Download as a file
     * @param approved           Download the approved version
     * @param response           object
     * @param request            object
     * @param mediaType          Supports xml or json - default to xml if the media type is not known.
     * @return It will the object
     * @throws Exception if record not found of permissions denied.
     */
    private Object getRecordAs(
        String metadataUuid,
        boolean addSchemaLocation,
        boolean increasePopularity,
        boolean withInfo,
        boolean attachment,
        boolean approved,
        HttpServletResponse response,
        HttpServletRequest request,
        MediaType mediaType
    )
        throws Exception {
        AbstractMetadata metadata;
        try {
            metadata = ApiUtils.canViewRecord(metadataUuid, approved, request);
        } catch (ResourceNotFoundException e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
        }
        ServiceContext context = ApiUtils.createServiceContext(request);
        try {
            Lib.resource.checkPrivilege(context,
                String.valueOf(metadata.getId()),
                ReservedOperation.view);
        } catch (Exception e) {
            // TODO: i18n
            // TODO: Report exception in JSON format
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);

        }

        boolean withValidationErrors = false;
        boolean keepXlinkAttributes = false;
        boolean forEditing = false;

        String mdId = String.valueOf(metadata.getId());

        //Here we just care if we need the approved version explicitly.
        //ApiUtils.canViewRecord already filtered draft for non editors.
        if (approved) {
            mdId = String.valueOf(metadataRepository.findOneByUuid(metadata.getUuid()).getId());

            // Only increase popularity for the no working copy
            if (increasePopularity) {
                dataManager.increasePopularity(context, mdId + "");
            }
        }

        if (!metadataUtils.isMetadataAvailableInPortal(Integer.parseInt(mdId))) {
            throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
        }

        Element xml = withInfo ?
            dataManager.getMetadata(context, mdId, forEditing,
                withValidationErrors, keepXlinkAttributes) :
            dataManager.getMetadataNoInfo(context, mdId + "");

        if (addSchemaLocation) {
            Attribute schemaLocAtt = schemaManager.getSchemaLocation(
                metadata.getDataInfo().getSchemaId(), context);

            if (schemaLocAtt != null) {
                if (xml.getAttribute(
                    schemaLocAtt.getName(),
                    schemaLocAtt.getNamespace()) == null) {
                    xml.setAttribute(schemaLocAtt);
                    // make sure namespace declaration for schemalocation is present -
                    // remove it first (does nothing if not there) then add it
                    xml.removeNamespaceDeclaration(schemaLocAtt.getNamespace());
                    xml.addNamespaceDeclaration(schemaLocAtt.getNamespace());
                }
            }
        }

        boolean isJson = MediaType.APPLICATION_JSON.equals(mediaType);

        String mode = (attachment) ? "attachment" : "inline";
        response.setHeader("Content-Disposition", String.format(
            mode + "; filename=\"%s.%s\"",
            metadata.getUuid(),
            isJson ? "json" : "xml"
        ));
        return isJson ? Xml.getJSON(xml) : xml;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get a metadata record as ZIP",
        description = "Metadata Exchange Format (MEF) is returned. MEF is a ZIP file containing " +
            "the metadata as XML and some others files depending on the version requested. " +
            "See https://docs.geonetwork-opensource.org/latest/annexes/mef-format/.")
    @RequestMapping(value = "/{metadataUuid}/formatters/zip",
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
        @ApiResponse(responseCode = "200", description = "Return the record."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    public
    @ResponseBody
    void getRecordAsZip(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
        String metadataUuid,
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
            defaultValue = "true")
        boolean withRelated,
        @Parameter(
            description = "Whether to include file attachments in the exported MEF package."
        )
        @RequestParam(
            required = false,
            defaultValue = "true")
        boolean includeAttachments,
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
        HttpServletResponse response,
        HttpServletRequest request
    )
        throws Exception {
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        AbstractMetadata metadata;
        try {
            metadata = ApiUtils.canViewRecord(metadataUuid, request);
        } catch (SecurityException e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
        }
        // Check attachment size limit early if attachments are requested
        if (includeAttachments) {
            MEFLib.checkAttachmentsUnderSizeLimit(Set.of(metadataUuid), approved);
        }

        Path stylePath = dataDirectory.getWebappDir().resolve(Geonet.Path.SCHEMAS);
        Path file = null;
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        String acceptHeader = StringUtils.isBlank(request.getHeader(HttpHeaders.ACCEPT)) ? "application/x-gn-mef-2-zip" : request.getHeader(HttpHeaders.ACCEPT);
        MEFLib.Version version = MEFLib.Version.find(acceptHeader);
        try {
            if (version == MEFLib.Version.V1) {
                // This parameter is deprecated in v2.
                boolean skipUUID = false;

                Integer id = -1;

                if (approved) {
                    id = metadataRepository.findOneByUuid(metadataUuid).getId();
                } else {
                    id = metadataUtils.findOneByUuid(metadataUuid).getId();
                }

                file = MEFLib.doExport(
                    serviceContext, id, format.toString(),
                    skipUUID, withXLinksResolved, withXLinkAttribute, addSchemaLocation, includeAttachments
                );
                response.setContentType(MEFLib.Version.Constants.MEF_V1_ACCEPT_TYPE);
            } else {
                Set<String> uuidsToExport = new HashSet<>();
                uuidsToExport.add(metadataUuid);
                // MEF version 2 support multiple metadata record by file.
                if (withRelated) {
                    // Adding children in MEF file

                    RelatedResponse related = getRelatedResources(
                        metadataUuid, null, approved, 0, 100, request);
                    uuidsToExport.addAll(getUuidsOfAssociatedRecords(related.getParent()));
                    uuidsToExport.addAll(getUuidsOfAssociatedRecords(related.getChildren()));
                    uuidsToExport.addAll(getUuidsOfAssociatedRecords(related.getDatasets()));
                    uuidsToExport.addAll(getUuidsOfAssociatedRecords(related.getServices()));
                    uuidsToExport.addAll(getUuidsOfAssociatedRecords(related.getSiblings()));
                    uuidsToExport.addAll(getUuidsOfAssociatedRecords(related.getRelated()));
                    uuidsToExport.addAll(getUuidsOfAssociatedRecords(related.getAssociated()));
                    uuidsToExport.addAll(getUuidsOfAssociatedRecords(related.getFcats()));
                    uuidsToExport.addAll(getUuidsOfAssociatedRecords(related.getHasfeaturecats()));
                    uuidsToExport.addAll(getUuidsOfAssociatedRecords(related.getHassources()));
                }
                Log.info(Geonet.MEF, "Building MEF2 file with " + uuidsToExport.size()
                    + " records.");

                file = MEFLib.doMEF2Export(serviceContext, uuidsToExport, format.toString(), false, stylePath, withXLinksResolved, withXLinkAttribute, false, addSchemaLocation, approved, includeAttachments);

                response.setContentType(MEFLib.Version.Constants.MEF_V2_ACCEPT_TYPE);
            }
            String suffix = includeAttachments ? "" : "-" + messages.getMessage("api.metadata.export.filename.withoutAttachmentsSuffix", null, locale);
            String filename = metadata.getUuid() + suffix + ".zip";
            ContentDisposition contentDisposition = ContentDisposition.inline().filename(filename).build();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(Files.size(file)));
            FileUtils.copyFile(file.toFile(), response.getOutputStream());
        } finally {
            if (file != null) {
                FileUtils.deleteQuietly(file.toFile());
            }
        }
    }

    private List<String> getUuidsOfAssociatedRecords(IListOnlyClassToArray associatedRecords) {
        return Optional.ofNullable(associatedRecords)
            .map(r -> ((List<RelatedMetadataItem>) r.getItem()).stream())
            .orElseGet(Stream::empty)
            .map(i -> i.getId()).collect(Collectors.toList());
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Get record popularity",
        description = "")
    @GetMapping(value = "/{metadataUuid:.+}/popularity",
        consumes = MediaType.ALL_VALUE,
        produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Popularity."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND)
    })
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> getRecordPopularity(
        @Parameter(description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
        String metadataUuid,
        HttpServletRequest request
    )
        throws Exception {
        AbstractMetadata metadata;
        try {
            metadata = ApiUtils.canViewRecord(metadataUuid, request);

            // If the workflow is enabled, don't use the working copy to increase the popularity
            if (metadata instanceof MetadataDraft) {
                metadata = metadataRepository.findOneByUuid(metadataUuid);
            }
        } catch (ResourceNotFoundException e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
        }
        return new ResponseEntity<>(metadata.getDataInfo().getPopularity() + "", HttpStatus.OK);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Increase record popularity",
        description = "Used when a view is based on the search results content and does not really access the record. Record is then added to the indexing queue and popularity will be updated soon.")
    @PostMapping(value = "/{metadataUuid:.+}/popularity",
        consumes = MediaType.ALL_VALUE,
        produces = MediaType.TEXT_PLAIN_VALUE
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Popularity updated."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND)
    })
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<String> increaseRecordPopularity(
        @Parameter(description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
        String metadataUuid,
        HttpServletRequest request
    )
        throws Exception {
        AbstractMetadata metadata;
        try {
            metadata = ApiUtils.canViewRecord(metadataUuid, request);

            // If the workflow is enabled, don't use the working copy to increase the popularity
            if (metadata instanceof MetadataDraft) {
                metadata = metadataRepository.findOneByUuid(metadataUuid);
            }
        } catch (ResourceNotFoundException e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
        }
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);

        dataManager.increasePopularity(serviceContext, metadata.getId() + "");

        return new ResponseEntity<>(metadata.getDataInfo().getPopularity() + "",
            HttpStatus.CREATED);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get record related resources",
        description = "Retrieve related services, datasets, onlines, thumbnails, sources, ... " +
            "to this records.<br/>" +
            "<a href='https://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/associating-resources/index.html'>More info</a>")
    @RequestMapping(value = "/{metadataUuid:.+}/related",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return the associated resources."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    @ResponseBody
    public RelatedResponse getRelatedResources(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
        String metadataUuid,
        @Parameter(description = "Type of related resource. If none, all resources are returned.",
            required = false
        )
        @RequestParam(defaultValue = "")
        RelatedItemType[] type,
        @Parameter(description = "Use approved version or not", example = "true")
        @RequestParam(required = false, defaultValue = "true")
        Boolean approved,
        @Parameter(description = "Start offset for paging. Default 1. Only applies to related metadata records (ie. not for thumbnails).",
            required = false
        )
        @RequestParam(defaultValue = "0")
        int start,
        @Parameter(description = "Number of rows returned. Default 100.")
        @RequestParam(defaultValue = "100")
        int rows,
        HttpServletRequest request) throws Exception {

        AbstractMetadata md;
        try {
            md = ApiUtils.canViewRecord(metadataUuid, approved, request);
        } catch (SecurityException e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
        }

        String language = languageUtils.getIso3langCode(request.getLocales());
        final ServiceContext serviceContext = ApiUtils.createServiceContext(request);

        return getRelatedResources(language, serviceContext, md, type, start, rows);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Returns a map to decode attributes in a dataset (from the associated feature catalog)",
        description = "")
    @RequestMapping(value = "/{metadataUuid}/featureCatalog",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return the associated resources."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    @ResponseBody
    public FeatureResponse getFeatureCatalog(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
        String metadataUuid,
        @Parameter(description = "Use approved version or not", example = "true")
        @RequestParam(required = false, defaultValue = "true")
        Boolean approved,
        HttpServletRequest request) throws ResourceNotFoundException {

        RelatedItemType[] type = {RelatedItemType.fcats};

        FeatureResponse response = new FeatureResponse();

        Map<String, String[]> decodeMap = new HashMap<>();

        try {
            RelatedResponse related = getRelatedResources(
                metadataUuid, type, approved, 0, 100, request);

            if (isIncludedAttributeTable(related.getFcats())) {
                // TODO: Make consistent with document in index? and add codelists
                for (AttributeTable.Element element : related.getFcats().getItem().get(0).getFeatureType().getAttributeTable().getElement()) {
                    if (StringUtils.isNotBlank(element.getCode())) {
                        if (!decodeMap.containsKey(element.getCode())) {
                            String[] decodedValues = {element.getName(), element.getDefinition()};
                            decodeMap.put(element.getCode(), decodedValues);
                        }
                    } else {
                        if (!decodeMap.containsKey(element.getName())) {
                            String[] decodedValues = {element.getName(), element.getDefinition()};
                            decodeMap.put(element.getName(), decodedValues);
                        }
                    }
                }
            }

            response.setDecodeMap(decodeMap);

            return response;
        } catch (Exception e) {
            Log.error(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw new ResourceNotFoundException();
        }

    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Check if metadata field value is duplicated in another metadata",
        description = "Verifies if a metadata field value is in use. Fields supported: title (title), " +
            "alternate title (altTitle) or resource identifier (identifier)")
    @PostMapping(value = "/{metadataUuid:.+}/checkDuplicatedFieldValue",
        produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return true if the field value is duplicated in another metadata or false in other case."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    public ResponseEntity<Boolean> checkDuplicatedFieldValue(
        @Parameter(description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
        String metadataUuid,
        @Parameter(description = "Metadata field information to check",
            required = true)
        @RequestBody DuplicatedValueDto duplicatedValueDto,
        HttpServletRequest request
    ) throws Exception {
        try {
            ApiUtils.canViewRecord(metadataUuid, request);
        } catch (SecurityException e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
        }

        List<String> validFields = Arrays.asList("title", "altTitle", "identifier");

        if (!validFields.contains(duplicatedValueDto.getField())) {
            throw new IllegalArgumentException(String.format("A valid field name is required:", String.join(",", validFields)));
        }

        if (StringUtils.isEmpty(duplicatedValueDto.getValue())) {
            throw new IllegalArgumentException("A non-empty value is required.");
        }


        boolean uuidsWithSameTitle = MetadataUtils.isMetadataFieldValueExistingInOtherRecords(duplicatedValueDto.getValue(), duplicatedValueDto.getField(), metadataUuid);
        return ResponseEntity.ok(uuidsWithSameTitle);
    }

    private boolean isIncludedAttributeTable(RelatedResponse.Fcat fcat) {
        return fcat != null
            && fcat.getItem() != null
            && !fcat.getItem().isEmpty()
            && fcat.getItem().get(0).getFeatureType() != null
            && fcat.getItem().get(0).getFeatureType().getAttributeTable() != null
            && fcat.getItem().get(0).getFeatureType().getAttributeTable().getElement() != null;
    }

    private static class DuplicatedValueDto {
        private String field;
        private String value;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * Constructs a redirect string based on the provided formatter label, language, and metadata UUID
     *
     * @param recordViewFormatter The label of the formatter to use on the record view page
     * @param language            The language code to include in the URL
     * @param metadataUuid        The unique identifier of the metadata record
     * @return A redirect string if a matching formatter is found, or null if no formatter matches the provided label
     */
    private String getRedirect(@Nullable String recordViewFormatter, String language, String metadataUuid) {
        if (StringUtils.isBlank(recordViewFormatter)) {
            return null; // If no formatter is specified, return null
        }

        // Parse the UI configuration
        JsonObject uiConfig = JsonParser.parseString(XslUtil.getUiConfiguration(null)).getAsJsonObject();

        // Navigate through the JSON structure to retrieve the array of record view page formatters
        JsonArray formatterArray = Optional.ofNullable(uiConfig)
            .map(cfg -> cfg.getAsJsonObject("mods"))
            .map(mods -> mods.getAsJsonObject("search"))
            .map(search -> search.getAsJsonObject("formatter"))
            .map(search -> search.getAsJsonArray("list"))
            .orElse(null); // Return null if any of the objects in the chain are missing

        // If the formatter array is null, return null as no formatters are available
        if (formatterArray == null) return null;

        // Iterate through each element in the formatter array
        for (JsonElement element : formatterArray) {
            JsonObject obj = element.getAsJsonObject(); // Convert the element to a JSON object

            // Check if the "label" of the current formatter matches the provided formatter label
            if (recordViewFormatter.equals(obj.get("label").getAsString())) {
                // Retrieve the "url" of the matching formatter
                String url = obj.get("url").getAsString();

                // Return the constructed redirect
                return "redirect:/srv/" + language + "/catalog.search#/metadata/" + metadataUuid + url;
            }
        }

        // Return null if no matching formatter is found
        return null;
    }
}
