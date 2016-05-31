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

import org.fao.geonet.api.API;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.records.model.related.RelatedItemType;
import org.fao.geonet.api.records.model.related.RelatedResponse;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.services.ReadWriteController;

@RequestMapping(value = {
    "/api/records",
    "/api/" + API.VERSION_0_1 +
        "/records"
})
@Api(value = "records",
    tags = "records",
    description = "Metadata record operations")
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
        nickname = "get")
    @RequestMapping(value = "/{metadataUuid}",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_XML_VALUE
        })
    public
    @ResponseBody
    Element serviceSpecificExec(
        @ApiParam(value = "Record UUID.",
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(value = "Add XSD schema location based on standard configuration",
            required = false)
        @RequestParam(required = false, defaultValue = "true")
            boolean addSchemaLocation,
        @ApiParam(value = "Increase record popularity",
            required = false)
        @RequestParam(required = false, defaultValue = "true")
            boolean increasePopularity,
        @ApiParam(hidden = true)
            HttpServletResponse response)
        throws Exception {
        ServiceContext context = ServiceContext.get();
        DataManager dataManager = context.getBean(DataManager.class);
        MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
        Metadata metadata = metadataRepository.findOneByUuid(metadataUuid);
        if (metadata == null) {
            // TODO: i18n
            throw new ResourceNotFoundException(String.format(
                "Metadata with UUID '%s' not found in this catalog.",
                metadataUuid
            ));
        }
        try {
            Lib.resource.checkPrivilege(context,
                metadata.getId() + "",
                ReservedOperation.view);
        } catch (Exception e) {
            // TODO: i18n
            // TODO: Report exception in JSON format
            throw new SecurityException(String.format(
                "Metadata with UUID '%s' not shared with you.",
                metadataUuid
            ));
        }

        Element xml = metadata.getXmlData(false);
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
        if (increasePopularity) {
            dataManager.increasePopularity(context, metadata.getId() + "");
        }

        response.setHeader("Content-Disposition", String.format(
            "inline; filename=\"%s.xml\"",
            metadata.getUuid()
        ));
        return xml;
    }


    @ApiOperation(
        value = "Get record related resources",
        nickname = "get",
        notes = "Retrieve related services, datasets, onlines, thumbnails, sources, ... " +
            "to this records.")
    @RequestMapping(value = "/{metadataUuid}/related",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    public RelatedResponse getRelated(
        @ApiParam(value = "Record UUID.",
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

        final ServiceContext context = ServiceContext.get();
        ServiceManager serviceManager = context.getBean(ServiceManager.class);
        GeonetworkDataDirectory dataDirectory = context.getBean(GeonetworkDataDirectory.class);
        MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
        // TODO: Move to Utils
        Metadata md = metadataRepository.findOneByUuid(metadataUuid);
        if (md == null) {
            md = metadataRepository.findOne(metadataUuid);
            if (md == null) {
                throw new IllegalArgumentException(String.format(
                    "No Metadata found with uuid or id '%s'.", metadataUuid
                ));
            }
        }

        Locale language = languageUtils.parseAcceptLanguage(request.getLocales());

        // TODO PERF: ByPass XSL processing and create response directly
        // At least for related metadata and keep XSL only for links
        Element raw = new Element("root").addContent(Arrays.asList(
            new Element("gui").addContent(Arrays.asList(
                new Element("language").setText(language.getISO3Language()),
                new Element("url").setText(context.getBaseUrl())
            )),
            MetadataUtils.getRelated(context, md.getId(), md.getUuid(), type, start, start + rows, true)
        ));
        Path relatedXsl = dataDirectory.getWebappDir().resolve("xslt/services/metadata/relation.xsl");

        final Element transform = Xml.transform(raw, relatedXsl);
        RelatedResponse response = (RelatedResponse) Xml.unmarshall(transform, RelatedResponse.class);
        return response;
    }
}
