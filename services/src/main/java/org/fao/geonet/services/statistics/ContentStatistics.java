//=============================================================================
//===   Copyright (C) 2001-2013 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.services.statistics;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.repository.statistic.MetadataStatisticSpec;
import org.fao.geonet.services.statistics.response.ContentStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.fao.geonet.repository.specification.MetadataSpecs.isType;
import static org.springframework.data.jpa.domain.Specifications.where;

/**
 * Service to get main statistics about the content of the catalog
 * <ul>
 * <li>total number of searches</li>
 * </ul>
 * 
 */
@Controller("content.statistics")
public class ContentStatistics {

    @Autowired
    private MetadataRepository metadataRepository;

    @RequestMapping(value = {"/{lang}/statistics-content"}, produces = {
            MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public ContentStats exec() throws Exception {
        ContentStats response = new ContentStats();
        
        // Add parameter by source catalog
        // Add parameter by group and owner

        final Specifications<Metadata> isMetadataType = where(isType(MetadataType.METADATA));
        final long totalNonTemplateMetadata = metadataRepository.count(isMetadataType);
        final long totalTemplateMetadata = metadataRepository.count(isType(MetadataType.TEMPLATE));
        final long totalSubTemplateMetadata = metadataRepository.count(isType(MetadataType.SUB_TEMPLATE));
        final long totalHarvestedNonTemplateMetadata = metadataRepository.count(isMetadataType.and(MetadataSpecs.isHarvested(true)));
        final long totalPublicMetadata = metadataRepository.getMetadataStatistics().getStatBasedOnOperationAllowed
                (MetadataStatisticSpec.StandardSpecs.metadataCount(), OperationAllowedSpecs.isPublic(ReservedOperation.view));

        // Total number of metadata by type
        response.setTotalNonTemplateMetadata(totalNonTemplateMetadata);
        response.setTotalHarvestedNonTemplateMetadata(totalHarvestedNonTemplateMetadata);
        response.setTotalTemplateMetadata(totalTemplateMetadata);
        response.setTotalSubTemplateMetadata(totalSubTemplateMetadata);
        response.setTotalPublicMetadata(totalPublicMetadata);

        return response;
    }
}