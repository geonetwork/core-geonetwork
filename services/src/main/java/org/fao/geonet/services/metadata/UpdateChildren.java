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

package org.fao.geonet.services.metadata;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple service that returns a small report about children update process.
 *
 * @author m.coudert
 */
@Deprecated
public class UpdateChildren extends NotInReadOnlyModeService {
    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- API
    //---
    //--------------------------------------------------------------------------

    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        String parentUuid = Util.getParam(params, "parentUuid");
        String childrenIds = Util.getParam(params, "childrenIds");

        // Transform params element into Map<String, String> for xsl transformation
        @SuppressWarnings("unchecked")
        List<Element> lstParams = params.getChildren();
        Map<String, Object> parameters = new HashMap<String, Object>();
        for (Element param : lstParams) {
            parameters.put(param.getName(), param.getTextTrim());
        }

        // Handle children IDs.
        String[] children = childrenIds.split(",");

        // Update children
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);

        Set<String> untreatedChildren = dm.updateChildren(context, parentUuid, children, parameters);

        Element response = new Element(Jeeves.Elem.RESPONSE);
        int treatedChildren = children.length;
        StringBuilder untreatedReport = new StringBuilder();
        if (untreatedChildren.size() != 0) {
            treatedChildren = children.length - untreatedChildren.size();
            untreatedReport.setLength(0);
            untreatedReport.append(untreatedChildren.size()).append(" child/children not updated");
            for (String id : untreatedChildren)
                untreatedReport.append(", ").append(id);
        }

        String report = treatedChildren + " child/children updated for metadata " + parentUuid + ". " + untreatedReport;

        return response.setText(report);
    }
}
