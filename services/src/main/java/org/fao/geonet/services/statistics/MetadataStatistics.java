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

import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

/**
 * Service to get statistics on metadata record
 */
public class MetadataStatistics extends NotInReadOnlyModeService {

    private static final String METADATA_BY_CATEGORY_QUERY = "SELECT categoryid as id, label, count(*) AS total " 
            + "FROM metadata m, metadatacateg mc, categoriesdes c " 
            + "WHERE m.id = mc.metadataid AND mc.categoryid = c.iddes AND langid = ? AND isTemplate = ? "
            + "GROUP BY categoryid, label";
    private static final String METADATA_BY_GROUP_QUERY = "SELECT groupowner as id, label, count(*) AS total " 
            + "FROM metadata m, groupsdes g " 
            + "WHERE m.groupowner = g.iddes AND langid = ? AND isTemplate = ? "
            + "GROUP BY groupowner, label";
    private static final String METADATA_BY_OWNER_QUERY = "SELECT owner as id, CONCAT(name, ' ', surname) as label, count(*) AS total "
            + "FROM metadata m, users u " 
            + "WHERE m.owner = u.id AND isTemplate like ? "
            + "GROUP BY owner, username";
    private static final String METADATA_BY_SOURCE_QUERY = "SELECT source as id, name as label, count(*) AS total " 
            + "FROM metadata m, sources s " 
            + "WHERE m.source = s.uuid AND isTemplate like ? "
            + "GROUP BY source, name";
    private static final String METADATA_BY_SCHEMA_QUERY = "SELECT schemaid as label, count(*) AS total FROM metadata " 
            + "WHERE isTemplate like ? "
            + "GROUP BY schemaid";
    private static final String METADATA_BY_TEMPLATE_QUERY = "SELECT istemplate as label, count(*) AS total FROM metadata " 
            + "WHERE isTemplate like ? "
            + "GROUP BY istemplate";
    private static final String METADATA_BY_HARVEST_QUERY = "SELECT isharvested as label, count(*) AS total FROM metadata " 
            + "WHERE isTemplate like ? "
            + "GROUP BY isharvested";
    
    // TODO: Could be relevant to add metadata with no status ?
    private static final String METADATA_BY_STATUS_QUERY = 
            "SELECT statusid, label, COUNT(*) as total FROM ( " +
                                    "SELECT statusid, label, m.id, max(s.changedate) " +
                                    "FROM metadata m, metadatastatus s, statusvaluesdes d " +
                                    "WHERE m.id = s.metadataid AND s.statusid = d.iddes " +
                                    "AND d.langid = ? AND isTemplate = ? " +
                                    "GROUP BY statusid, label, m.id " +
            ") AS Q GROUP BY statusid, label " + 
            "UNION " +
            "SELECT -1, 'nostatus' AS LABEL, COUNT(*) FROM metadata WHERE isTemplate = ? AND id NOT IN (SELECT metadataid FROM metadatastatus)";
    
    private static final String METADATA_BY_VALIDATION_QUERY = 
            "SELECT 'invalid' AS label, COUNT(*) AS total FROM (SELECT m.id FROM metadata m, validation v " +
                                    "WHERE m.id = v.metadataid AND isTemplate = ? " +
                                    "GROUP BY m.id " +
                                    "HAVING SUM(status) = 0) AS Q " +
            "UNION  " +
            "SELECT 'valid' AS label, COUNT(*) AS total FROM (SELECT m.id FROM metadata m, validation v " +
                                    "WHERE m.id = v.metadataid AND isTemplate = ? " +
                                    "GROUP BY m.id " +
                                    "HAVING SUM(status) > 0) AS Q " +
            "UNION " +
            "SELECT 'unchecked' AS label, COUNT(*) AS total FROM metadata m WHERE isTemplate = ? AND id NOT IN " +
                                    "(SELECT metadataid FROM validation)";
    
    public void init(String appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);
    }

    @Override
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        String isTemplate = Util.getParam(params, "isTemplate", "n");
        String type = Util.getParam(params, "by", "group");
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        if (type.equals("owner")) {
            return dbms.select(METADATA_BY_OWNER_QUERY, isTemplate);
        } else if (type.equals("source")) {
            return dbms.select(METADATA_BY_SOURCE_QUERY, isTemplate);
        } else if (type.equals("schema")) {
            return dbms.select(METADATA_BY_SCHEMA_QUERY, isTemplate);
        } else if (type.equals("template")) {
            return dbms.select(METADATA_BY_TEMPLATE_QUERY, isTemplate);
        } else if (type.equals("harvested")) {
            return dbms.select(METADATA_BY_HARVEST_QUERY, isTemplate);
        } else if (type.equals("category")) {
            return dbms.select(METADATA_BY_CATEGORY_QUERY, context.getLanguage(), isTemplate);
        } else if (type.equals("status")) {
            return dbms.select(METADATA_BY_STATUS_QUERY, context.getLanguage(), isTemplate, isTemplate);
        } else if (type.equals("validity")) {
            return dbms.select(METADATA_BY_VALIDATION_QUERY, isTemplate, isTemplate, isTemplate);
        } else {
            return dbms.select(METADATA_BY_GROUP_QUERY, context.getLanguage(), isTemplate);
        }
    }
}