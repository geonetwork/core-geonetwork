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

import static org.fao.geonet.repository.statistic.MetadataStatisticSpec.StandardSpecs.metadataCount;

import java.util.Map;
import java.util.Map.Entry;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.MetadataValidationStatus;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.domain.User;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

/**
 * Service to get statistics on metadata record
 */
public class MetadataStatistics extends NotInReadOnlyModeService {

    public void init(String appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);
    }

    @Override
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
      
        // FIXME: Should be able to get stats only on template
//        String isTemplate = Util.getParam(params, "isTemplate", "n");
        String type = Util.getParam(params, "by", "group");
        Element response = new Element("statistics");
        MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);

        if (type.equals("owner")) {
          Map<User, Integer> statsMap = metadataRepository.getMetadataStatistics().getOwnerToStatMap(metadataCount());
          for (Entry<User, Integer> stat : statsMap.entrySet()) {
            Element entry = new Element("stat");
            entry.addContent(new Element("label").setText(stat.getKey().getName()));
            entry.addContent(new Element("total").setText(stat.getValue() + ""));
            response.addContent(entry);
          }
        } else if (type.equals("source")) {
          Map<Source, Integer> statsMap = metadataRepository.getMetadataStatistics().getSourceToStatMap(metadataCount());
          for (Entry<Source, Integer> stat : statsMap.entrySet()) {
            Element entry = new Element("stat");
            entry.addContent(new Element("label").setText(stat.getKey().getName()));
            entry.addContent(new Element("total").setText(stat.getValue() + ""));
            response.addContent(entry);
          }
        } else if (type.equals("schema")) {
          Map<String, Integer> statsMap = metadataRepository.getMetadataStatistics().getSchemaToStatMap(metadataCount());
          for (Entry<String, Integer> stat : statsMap.entrySet()) {
            Element entry = new Element("stat");
            entry.addContent(new Element("label").setText(stat.getKey()));
            entry.addContent(new Element("total").setText(stat.getValue() + ""));
            response.addContent(entry);
          }
        } else if (type.equals("template")) {
          Map<MetadataType, Integer> statsMap = metadataRepository.getMetadataStatistics().getMetadataTypeToStatMap(metadataCount());
          for (Entry<MetadataType, Integer> stat : statsMap.entrySet()) {
            Element entry = new Element("stat");
            entry.addContent(new Element("label").setText(stat.getKey().toString()));
            entry.addContent(new Element("total").setText(stat.getValue() + ""));
            response.addContent(entry);
          }
        } else if (type.equals("harvested")) {
          Map<Boolean, Integer> statsMap = metadataRepository.getMetadataStatistics().getIsHarvestedToStatMap(metadataCount());
          for (Entry<Boolean, Integer> stat : statsMap.entrySet()) {
            Element entry = new Element("stat");
            entry.addContent(new Element("label").setText(stat.getKey().toString()));
            entry.addContent(new Element("total").setText(stat.getValue() + ""));
            response.addContent(entry);
          }
        } else if (type.equals("category")) {
          Map<MetadataCategory, Integer> statsMap = metadataRepository.getMetadataStatistics().getMetadataCategoryToStatMap(metadataCount());
          for (Entry<MetadataCategory, Integer> stat : statsMap.entrySet()) {
            Element entry = new Element("stat");
            entry.addContent(new Element("label").setText(stat.getKey().getLabel(context.getLanguage())));
            entry.addContent(new Element("total").setText(stat.getValue() + ""));
            response.addContent(entry);
          }
        } else if (type.equals("status")) {
          Map<StatusValue, Integer> statsMap = metadataRepository.getMetadataStatistics().getStatusValueToStatMap(metadataCount());
          for (Entry<StatusValue, Integer> stat : statsMap.entrySet()) {
            Element entry = new Element("stat");
            entry.addContent(new Element("label").setText(stat.getKey().getLabel(context.getLanguage())));
            entry.addContent(new Element("total").setText(stat.getValue() + ""));
            response.addContent(entry);
          }
        } else if (type.equals("validity")) {
          Map<MetadataValidationStatus, Integer> statsMap = metadataRepository.getMetadataStatistics().getMetadataValidationStatusToStatMap(metadataCount());
          for (Entry<MetadataValidationStatus, Integer> stat : statsMap.entrySet()) {
            Element entry = new Element("stat");
            entry.addContent(new Element("label").setText(stat.getKey().toString()));
            entry.addContent(new Element("total").setText(stat.getValue() + ""));
            response.addContent(entry);
          }
        } else {
          Map<Group, Integer> statsMap = metadataRepository.getMetadataStatistics().getGroupOwnerToStatMap(metadataCount());
          for (Entry<Group, Integer> stat : statsMap.entrySet()) {
            Element entry = new Element("stat");
            entry.addContent(new Element("label").setText(stat.getKey().getLabel(context.getLanguage())));
            entry.addContent(new Element("total").setText(stat.getValue() + ""));
            response.addContent(entry);
          }
        }
        return response;
    }
}