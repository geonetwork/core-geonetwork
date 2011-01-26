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
package org.fao.geonet.guiservices.templates;

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.jdom.Element;

import java.util.Iterator;
import java.util.List;

/**
 * Saves display order of a list of templates.
 *
 * @author heikki doeleman
 * 
 */
public class SaveDisplayOrder implements Service {
	public void init(String appPath, ServiceConfig params) throws Exception {}

	public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        DataManager dm = gc.getDataManager() ;
        List<Element> requestParameters = params.getChildren();
        for (Element param : requestParameters) {
            // the request params come in as e.g. <displayorder-30749>5</displayorder-30749> where
            // the part after the dash is the metadata id.
            String id = param.getName().substring(param.getName().indexOf('-') + 1);
            if ("".equals(id)) {    // In some cases, GUI sends <_/> parameters,
                // If id is not an integer, exception will occur later.
	            String displayPosition = param.getText();
	            dm.updateDisplayOrder(dbms, id, displayPosition);
	            dbms.commit();
            }
        }
        return null;
    }
}