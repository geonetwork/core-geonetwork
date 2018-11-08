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

package org.fao.geonet.guiservices.metadata;

import java.nio.file.Path;
import java.util.List;

import org.fao.geonet.Util;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDataInfo_;
import org.fao.geonet.domain.Metadata_;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.SitemapDocumentNotFoundEx;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.jdom.Element;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 * Create a sitemap document with metadata records
 *
 * Formats: xml (default if not specified), html
 *
 * See http://www.sitemaps.org/protocol.php
 */
public class Sitemap implements Service {
    private static final String FORMAT_XML = "xml";
    private static final String FORMAT_HTML = "html";

    // Max. items in page defined in spec
    private static final int MAX_ITEMS_PER_PAGE = 2500;

    private int _maxItemsPage;

    public void init(Path appPath, ServiceConfig config) throws Exception {
        String sMaxItemsPage = config.getValue("maxItemsPage", MAX_ITEMS_PER_PAGE + "");

        try {
            _maxItemsPage = Integer.parseInt(sMaxItemsPage);

            if (_maxItemsPage > MAX_ITEMS_PER_PAGE) _maxItemsPage = MAX_ITEMS_PER_PAGE;

        } catch (NumberFormatException ex) {
            _maxItemsPage = MAX_ITEMS_PER_PAGE;
        }
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        String format = Util.getParam(params, "format", FORMAT_XML);
        if (!((format.equalsIgnoreCase(FORMAT_HTML)) ||
            (format.equalsIgnoreCase(FORMAT_XML)))) {

            format = FORMAT_XML;
        }

        Integer allgroup = 1;

        Specifications<OperationAllowed> spec = Specifications.where(
            OperationAllowedSpecs.hasOperation(ReservedOperation.view));
        spec = spec.and(OperationAllowedSpecs.hasGroupId(allgroup));

        OperationAllowedRepository operationAllowedRepository =
            context.getBean(OperationAllowedRepository.class);
        MetadataRepository metadataRepository =
            context.getBean(MetadataRepository.class);

        final List<Integer> list = operationAllowedRepository.findAllIds(spec,
            OperationAllowedId_.metadataId);
        Sort sortByChangeDateDesc = new Sort(Sort.Direction.DESC, Metadata_.dataInfo.getName() + "." + MetadataDataInfo_.changeDate.getName());

        long metadatataCount = metadataRepository.count((Specification<Metadata>)MetadataSpecs.hasMetadataIdIn(list));
        long pages = (long) Math.ceil((double) metadatataCount / _maxItemsPage);

        int doc = Util.getParam(params, "doc", 0);

        Element result = null;

        if (doc > 0) {
            // Requesting a sitemap specific document
            if (doc <= pages) {

                final PageRequest pageRequest = new PageRequest(doc - 1, _maxItemsPage, sortByChangeDateDesc);
                result = metadataRepository.findAllUuidsAndChangeDatesAndSchemaId(list, pageRequest);

                Element formatEl = new Element("format");
                formatEl.setText(format.toLowerCase());
                result.addContent(formatEl);

            } else {
                throw new SitemapDocumentNotFoundEx(doc);
            }

        } else { 
            // Request the sitemap (no specific document) 
            if (metadatataCount <= _maxItemsPage) { 
                // Request the full sitemap 
                result = metadataRepository.findAllUuidsAndChangeDatesAndSchemaId(list);

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

        return result;
    }
}
