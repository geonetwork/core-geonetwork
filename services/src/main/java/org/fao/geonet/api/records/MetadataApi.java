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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import jeeves.constants.Jeeves;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.records.model.related.RelatedItemType;
import org.fao.geonet.api.records.model.related.RelatedResponse;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.fao.geonet.api.ApiParams.*;
import static org.fao.geonet.kernel.mef.MEFLib.Version.Constants.MEF_V1_ACCEPT_TYPE;
import static org.fao.geonet.kernel.mef.MEFLib.Version.Constants.MEF_V2_ACCEPT_TYPE;

@RequestMapping(value = {
    "/api/records",
    "/api/" + API.VERSION_0_1 +
        "/records"
})
@Api(value = API_CLASS_RECORD_TAG,
    tags = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("records")
@ReadWriteController
public class MetadataApi implements ApplicationContextAware {

    @Autowired
    SchemaManager _schemaManager;

    @Autowired
    LanguageUtils languageUtils;

    private ApplicationContext context;

    public synchronized void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }


    @ApiOperation(value = "Get a metadata record",
        notes = "Depending on the accept header the appropriate formatter is used. " +
            "When requesting a ZIP, a MEF version 2 file is returned. " +
            "When requesting HTML, the default formatter is used.",
        nickname = "getRecord")
    @RequestMapping(value = "/{metadataUuid}",
        method = RequestMethod.GET,
        consumes = {
            MediaType.ALL_VALUE
        },
        produces = {
            MediaType.TEXT_HTML_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_XHTML_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE,
            "application/pdf",
            "application/zip",
            MEF_V1_ACCEPT_TYPE,
            MEF_V2_ACCEPT_TYPE
        })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Return the record."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW),
        @ApiResponse(code = 404, message = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND)
    })
    public String getRecord(
        @ApiParam(value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(value = "Accept header should indicate which is the appropriate format " +
            "to return. It could be text/html, application/xml, application/zip, ..." +
            "If no appropriate Accept header found, the XML format is returned.",
            required = true)
        @RequestHeader(
            value = HttpHeaders.ACCEPT,
            defaultValue = MediaType.APPLICATION_XML_VALUE,
            required = false
        )
            String acceptHeader,
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
        List<String> accept = Arrays.asList(acceptHeader.split(","));

        String defaultFormatter = "xsl-view";
        if (accept.contains(MediaType.TEXT_HTML_VALUE)
            || accept.contains(MediaType.APPLICATION_XHTML_XML_VALUE)
            || accept.contains("application/pdf")) {
            return "forward:" + (metadataUuid + "/formatters/" + defaultFormatter);
        } else if (accept.contains(MediaType.APPLICATION_XML_VALUE)
                || accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return "forward:" + (metadataUuid + "/formatters/xml");
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


    @ApiOperation(value = "Get a metadata record as XML or JSON",
        notes = "",
        nickname = "getRecordAsXmlOrJSON")
    @RequestMapping(value =
        {
            "/{metadataUuid}/formatters/xml",
            "/{metadataUuid}/formatters/json"
        },
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE
        })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Return the record."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    public
    @ResponseBody
    Object getRecordAsXML(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(value = "Add XSD schema location based on standard configuration " +
            "(see schema-ident.xml).",
            required = false)
        @RequestParam(required = false, defaultValue = "true")
            boolean addSchemaLocation,
        @ApiParam(value = "Increase record popularity",
            required = false)
        @RequestParam(required = false, defaultValue = "true")
            boolean increasePopularity,
        @ApiParam(value = "Add geonet:info details",
            required = false)
        @RequestParam(required = false, defaultValue = "false")
            boolean withInfo,
        @RequestHeader(
            value = HttpHeaders.ACCEPT,
            defaultValue = MediaType.APPLICATION_XML_VALUE
        )
            String acceptHeader,
        HttpServletResponse response,
        HttpServletRequest request
    )
        throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();
        DataManager dataManager = appContext.getBean(DataManager.class);
        Metadata metadata;
        try {
            metadata = ApiUtils.canViewRecord(metadataUuid, request);
        } catch (ResourceNotFoundException e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
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

        if (increasePopularity) {
            dataManager.increasePopularity(context, metadata.getId() + "");
        }


        boolean withValidationErrors = false, keepXlinkAttributes = false, forEditing = false;
        Element xml  = withInfo ?
            dataManager.getMetadata(context,
            metadata.getId() + "", forEditing, withValidationErrors, keepXlinkAttributes) :
            dataManager.getMetadataNoInfo(context, metadata.getId() + "");

        if (addSchemaLocation) {
            Attribute schemaLocAtt = _schemaManager.getSchemaLocation(
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

        boolean isJson = acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE);

        response.setHeader("Content-Disposition", String.format(
            "inline; filename=\"%s.%s\"",
            metadata.getUuid(),
            isJson ? "json" : "xml"
        ));
        return isJson ? Xml.getJSON(xml) : xml;
        //return xml;
    }

    @ApiOperation(
        value = "Get a metadata record as ZIP",
        notes = "Metadata Exchange Format (MEF) is returned. MEF is a ZIP file containing " +
            "the metadata as XML and some others files depending on the version requested. " +
            "See http://geonetwork-opensource.org/manuals/trunk/eng/users/annexes/mef-format.html.",
        nickname = "getRecordAsZip")
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
        @ApiResponse(code = 200, message = "Return the record."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    public
    @ResponseBody
    void getRecordAsZip(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(
            value = "MEF file format.",
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "FULL")
            MEFLib.Format format,
        @ApiParam(
            value = "With related records (parent and service).",
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "true")
            boolean withRelated,
        @ApiParam(
            value = "Resolve XLinks in the records.",
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "true")
            boolean withXLinksResolved,
        @ApiParam(
            value = "Preserve XLink URLs in the records.",
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "false")
            boolean withXLinkAttribute,
        @RequestHeader(
            value = HttpHeaders.ACCEPT,
            defaultValue = "application/x-gn-mef-2-zip"
        )
            String acceptHeader,
        HttpServletResponse response,
        HttpServletRequest request
    )
        throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();
        GeonetworkDataDirectory dataDirectory = appContext.getBean(GeonetworkDataDirectory.class);

        Metadata metadata;
        try {
            metadata = ApiUtils.canViewRecord(metadataUuid, request);
        } catch (SecurityException e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
        }
        Path stylePath = dataDirectory.getWebappDir().resolve(Geonet.Path.SCHEMAS);
        Path file = null;
        ServiceContext context = ApiUtils.createServiceContext(request);
        MEFLib.Version version = MEFLib.Version.find(acceptHeader);
        if (version == MEFLib.Version.V1) {
            // This parameter is deprecated in v2.
            boolean skipUUID = false;
            file = MEFLib.doExport(
                context, metadataUuid, format.toString(),
                skipUUID, withXLinksResolved, withXLinkAttribute
            );
        } else {
            Set<String> tmpUuid = new HashSet<String>();
            tmpUuid.add(metadataUuid);
            // MEF version 2 support multiple metadata record by file.
            if (withRelated) {
                // Adding children in MEF file

                // Creating request for services search
                Element childRequest = new Element("request");
                childRequest.addContent(new Element("parentUuid").setText(metadataUuid));
                childRequest.addContent(new Element("to").setText("1000"));

                // Get children to export - It could be better to use GetRelated service TODO
                Set<String> childs = MetadataUtils.getUuidsToExport(
                    metadataUuid, request, childRequest);
                if (childs.size() != 0) {
                    tmpUuid.addAll(childs);
                }

                // Creating request for services search
                Element servicesRequest = new Element(Jeeves.Elem.REQUEST);
                servicesRequest.addContent(new Element(
                    org.fao.geonet.constants.Params.OPERATES_ON)
                    .setText(metadataUuid));
                servicesRequest.addContent(new Element(
                    org.fao.geonet.constants.Params.TYPE)
                    .setText("service"));

                // Get linked services for export
                Set<String> services = MetadataUtils.getUuidsToExport(
                    metadataUuid, request, servicesRequest);
                if (services.size() != 0) {
                    tmpUuid.addAll(services);
                }
            }
            Log.info(Geonet.MEF, "Building MEF2 file with " + tmpUuid.size()
                + " records.");

            file = MEFLib.doMEF2Export(context, tmpUuid, format.toString(), false, stylePath, withXLinksResolved, withXLinkAttribute);
        }
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format(
            "inline; filename=\"%s.zip\"",
            metadata.getUuid()
        ));
        response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(Files.size(file)));
        response.setContentType(acceptHeader);
        FileUtils.copyFile(file.toFile(), response.getOutputStream());
    }


    @ApiOperation(
        value = "Get record related resources",
        nickname = "getAssociated",
        notes = "Retrieve related services, datasets, onlines, thumbnails, sources, ... " +
            "to this records.<br/>" +
            "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/associating-resources/index.html'>More info</a>")
    @RequestMapping(value = "/{metadataUuid}/related",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Return the associated resources."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    @ResponseBody
    public RelatedResponse getRelated(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(value = "Type of related resource. If none, all resources are returned.",
            required = false
        )
        @RequestParam(defaultValue = "")
            RelatedItemType[] type,
        @ApiParam(value = "Start offset for paging. Default 1. Only applies to related metadata records (ie. not for thumbnails).",
            required = false
        )
        @RequestParam(defaultValue = "1")
            int start,
        @ApiParam(value = "Number of rows returned. Default 100.")
        @RequestParam(defaultValue = "100")
            int rows,
        HttpServletRequest request) throws Exception {


        Metadata md;
        try{
            md = ApiUtils.canViewRecord(metadataUuid, request);
        } catch (SecurityException e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
        }

        Locale language = languageUtils.parseAcceptLanguage(request.getLocales());

        // TODO PERF: ByPass XSL processing and create response directly
        // At least for related metadata and keep XSL only for links
        final ServiceContext context = ApiUtils.createServiceContext(request);
        Element raw = new Element("root").addContent(Arrays.asList(
            new Element("gui").addContent(Arrays.asList(
                new Element("language").setText(language.getISO3Language()),
                new Element("url").setText(context.getBaseUrl())
            )),
            MetadataUtils.getRelated(context, md.getId(), md.getUuid(), type, start, start + rows, true)
        ));
        GeonetworkDataDirectory dataDirectory = context.getBean(GeonetworkDataDirectory.class);
        Path relatedXsl = dataDirectory.getWebappDir().resolve("xslt/services/metadata/relation.xsl");

        final Element transform = Xml.transform(raw, relatedXsl);
        RelatedResponse response = (RelatedResponse) Xml.unmarshall(transform, RelatedResponse.class);
        return response;
    }
}
