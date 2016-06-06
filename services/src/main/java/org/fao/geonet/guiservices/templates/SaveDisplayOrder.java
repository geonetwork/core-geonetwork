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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.jdom.Element;

/**
 * Saves display order of a list of templates.
 *
 * @author heikki doeleman
 */
@Deprecated
public class SaveDisplayOrder implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);
        @SuppressWarnings("unchecked")
        List<Element> requestParameters = params.getChildren();
        List<String> ids = new ArrayList<String>();
        for (Element param : requestParameters) {
            // the request params come in as e.g. <displayorder-30749>5</displayorder-30749> where
            // the part after the dash is the metadata id.
            String id = param.getName().substring(param.getName().indexOf('-') + 1);
            if (StringUtils.isNotEmpty(id) && !"_".equals(id)) {    // In Chrome with POST method, Ajax.Request sends <_/> parameters,
                // If id is not an integer, exception will occur later.
                String displayPosition = param.getText();
                dm.updateDisplayOrder(id, displayPosition);
                ids.add(id);
            }
        }
        dm.indexMetadata(ids);
        return null;
    }
}
