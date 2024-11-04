/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.api.site;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.SitemapDocumentNotFoundEx;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.fao.geonet.api.ApiParams.API_CLASS_CATALOG_TAG;

@RequestMapping(value = {
    "/{portal}/api"
})
@Tag(name = API_CLASS_CATALOG_TAG,
    description = ApiParams.API_CLASS_CATALOG_OPS)
@Controller("sitemap")
public class SitemapApi {
    private static final String FORMAT_XML = "xml";
    private static final String FORMAT_HTML = "html";

    // Max. items in page defined in spec
    private static final int MAX_ITEMS_PER_PAGE = 2500;

    @Autowired
    SettingManager settingManager;

    @Autowired
    NodeInfo node;

    @Autowired
    OperationAllowedRepository operationAllowedRepository;

    @Autowired
    MetadataRepository metadataRepository;

    @Autowired
    GeonetworkDataDirectory dataDirectory;


    @io.swagger.v3.oas.annotations.Operation(
        summary = "robots.txt",
        description = "")
    @RequestMapping(
        path = "/robots.txt",
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "robots.txt file for SEO.")
    })
    public void getRobotsText(HttpServletRequest request, HttpServletResponse response) throws IOException {
        StringBuilder content = new StringBuilder(256);
        String contextPath = request.getContextPath();
        content.append("User-agent: *\n");
        content.append("Disallow: ").append(contextPath).append("/catalog/\n");
        content.append("Disallow: ").append(contextPath).append("/static/\n");
        content.append("Sitemap: ").append(settingManager.getNodeURL()).append("api/sitemap\n");
        content.append("Sitemap: ").append(settingManager.getNodeURL()).append("api/sitemap?format=rdf");
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
        response.getWriter().append(content);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get sitemap",
        description = "")
    @RequestMapping(
        path = "/sitemap",
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Site map.")
    })
    public void getSitemap(
        @Parameter(
            description = "Format (xml or html).",
            required = false
        )
        @RequestParam(
            required = false,
            defaultValue = FORMAT_HTML
        )
        String format,
        @Parameter(
            description = "page.",
            required = false
        )
        @RequestParam(
            required = false,
            defaultValue = "0"
        )
        Integer doc,
        @Parameter(hidden = true)
        HttpServletResponse response
    ) throws Exception {
        if (!(format.equalsIgnoreCase(FORMAT_HTML) ||
            format.equalsIgnoreCase(FORMAT_XML))) {
            format = FORMAT_HTML;
        }

        int allGroup = 1;

        Specification<OperationAllowed> spec = Specification.where(
            OperationAllowedSpecs.hasOperation(ReservedOperation.view));
        spec = spec.and(OperationAllowedSpecs.hasGroupId(allGroup));

        final List<Integer> list = operationAllowedRepository.findAllIds(
            spec,
            OperationAllowedId_.metadataId);

        Sort sortByChangeDateDesc = Sort.by(
            Sort.Direction.DESC,
            Metadata_.dataInfo.getName() + "." + MetadataDataInfo_.changeDate.getName());

        long metadataCount = metadataRepository.count((Specification<Metadata>) MetadataSpecs.hasMetadataIdIn(list));
        long pages = (long) Math.ceil((double) metadataCount / MAX_ITEMS_PER_PAGE);

        Element result;

        if (doc > 0) {
            // Requesting a sitemap specific document
            if (doc <= pages) {
                final PageRequest pageRequest = PageRequest.of(doc - 1, MAX_ITEMS_PER_PAGE, sortByChangeDateDesc);
                result = metadataRepository.findUuidsAndChangeDatesAndSchemaId(list, pageRequest);

                Element formatEl = new Element("format");
                formatEl.setText(format.toLowerCase());
                result.addContent(formatEl);
            } else {
                throw new SitemapDocumentNotFoundEx(doc);
            }
        } else {
            // Request the sitemap (no specific document)
            if (metadataCount <= MAX_ITEMS_PER_PAGE) {
                // Request the full sitemap
                result = metadataRepository.findUuidsAndChangeDatesAndSchemaId(list);

                Element formatEl = new Element("format");
                formatEl.setText(format.toLowerCase());
                result.addContent(formatEl);
            } else {
                // Request the index
                result = new Element("response");

                Element indexDocs = new Element("indexDocs");
                indexDocs.setText(pages + "");
                result.addContent(indexDocs);

                Element changeDate = new Element("changeDate");
                changeDate.setText(new ISODate().toString());
                result.addContent(changeDate);
            }
        }
        Path xslt = dataDirectory.getWebappDir()
            .resolve("xslt/services/sitemap/sitemap.xsl");
        Element root = new Element("root");
        Element requestElt = new Element("request");
        requestElt.addContent(new Element("format").setText(format));
        root.addContent(requestElt);
        root.addContent(result);

        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
        Xml.transform(root, xslt, response.getOutputStream());
    }
}
