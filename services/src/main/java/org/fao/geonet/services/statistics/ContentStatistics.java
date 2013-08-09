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

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

/**
 * Service to get main statistics about the content of the catalog
 * <ul>
 * <li>total number of searches</li>
 * </ul>
 * 
 */
public class ContentStatistics extends NotInReadOnlyModeService {
    
    private static final String NUMBER_OF_METADATA_QUERY = 
            "SELECT COUNT(*) AS total " +
            "FROM metadata WHERE isTemplate = ?";
    
    private static final String NUMBER_OF_HARVESTED_METADATA_QUERY = 
            "SELECT COUNT(*) AS total " +
            "FROM metadata WHERE isTemplate = ? AND isHarvested = 'y'";
    
    private static final String NUMBER_OF_PUBLIC_METADATA_RECORD = 
            "SELECT COUNT(*) as total FROM metadata m, operationallowed o " +
            "WHERE m.id = o.metadataid AND groupid = 0 AND operationid = 0 " +
            "AND isTemplate = ?";
            
    public void init(String appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);
    }
    
    @Override
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        Element response = new Element("response");
        
        // Add parameter by source catalog
        // Add parameter by group and owner
        
        
        // Total number of metadata by type
        SearchStatistics.addSingleDBValueToElement(dbms, response, NUMBER_OF_METADATA_QUERY, "nb_metadata", "total", "n");
        SearchStatistics.addSingleDBValueToElement(dbms, response, NUMBER_OF_HARVESTED_METADATA_QUERY, "nb_harvested", "total", "n");
        SearchStatistics.addSingleDBValueToElement(dbms, response, NUMBER_OF_METADATA_QUERY, "nb_template", "total", "y");
        SearchStatistics.addSingleDBValueToElement(dbms, response, NUMBER_OF_METADATA_QUERY, "nb_subtemplate", "total", "s");
        SearchStatistics.addSingleDBValueToElement(dbms, response, NUMBER_OF_PUBLIC_METADATA_RECORD, "nb_metadata_public", "total", "n");

        return response;
    }
}