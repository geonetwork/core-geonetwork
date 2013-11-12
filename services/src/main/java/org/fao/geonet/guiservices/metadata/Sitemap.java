//==============================================================================
//===
//=== Sitemap
//===
//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.guiservices.metadata;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.jdom.Element;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specifications;

import java.util.List;
import java.util.Set;

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

    public void init(String appPath, ServiceConfig config) throws Exception { }

    public Element exec(Element params, ServiceContext context) throws Exception
    {
        String format = Util.getParam(params, "format", FORMAT_XML);
        if (!((format.equalsIgnoreCase(FORMAT_HTML)) ||
           (format.equalsIgnoreCase(FORMAT_XML)))) {

            format = FORMAT_XML;
        }

        AccessManager am = context.getBean(AccessManager.class);

        Set<Integer> groups = am.getUserGroups(context.getUserSession(), context.getIpAddress(), false);

        final OperationAllowedRepository operationAllowedRepository = context.getBean(OperationAllowedRepository.class);
        Specifications<OperationAllowed> spec = Specifications.where(OperationAllowedSpecs.hasOperation(ReservedOperation.view));
        for (Integer grpId : groups) {
            spec = spec.and(OperationAllowedSpecs.hasGroupId(grpId));
        }
        final List<Integer> list = operationAllowedRepository.findAllIds(spec, OperationAllowedId_.metadataId);

        final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
        Sort sortByChangeDateDesc = new Sort(Sort.Direction.DESC, Metadata_.dataInfo.getName()+"."+MetadataDataInfo_.changeDate);

        Element result = metadataRepository.findAllAsXml(MetadataSpecs.hasMetadataIdIn(list), sortByChangeDateDesc);


        Element formatEl = new Element("format");
        formatEl.setText(format.toLowerCase());

        result.addContent(formatEl);
        
        return result;
    }
}
