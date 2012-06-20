//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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

package org.fao.geonet.services.reusable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.reusable.ReusableObjManager;
import org.fao.geonet.services.metadata.IndexRebuild;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;

/**
 * Searches through the selected metadata and replaces all known reusable
 * objects that are identified in the metadata with the corresponding xlink
 *
 * @author jeichar
 */
public class Replace implements Service
{

    public Element exec(Element params, ServiceContext context) throws Exception
    {
        SelectionManager selectionManager = SelectionManager.getManager(context.getUserSession());

        if (selectionManager == null) {
            return null;
        }

        String all = Util.getParamText(params, "all");
        boolean email = "true".equalsIgnoreCase(Util.getParamText(params, "email"));
        boolean rebuildIndex = "true".equalsIgnoreCase(Util.getParamText(params, "rebuildIndex"));
        boolean publish = "true".equalsIgnoreCase(Util.getParamText(params, "publish"));
        String ignoreErrorParam = Util.getParamText(params, "ignoreErrors");
		boolean ignoreErrors = ignoreErrorParam != null && ("true".equalsIgnoreCase(ignoreErrorParam) ||
			"".equals(ignoreErrorParam.trim()));

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

        Set<String> elements;
        boolean processAllRecords = "true".equalsIgnoreCase(all);
        if (processAllRecords) {
            elements = new HashSet<String>();

            String query = "SELECT id FROM Metadata";
            List<Element> ids = dbms.select(query).getChildren("record");
            for (Element record : ids) {
                elements.add(record.getChildText("id"));
            }

        } else {
            elements = selectionManager.getSelection(SelectionManager.SELECTION_METADATA);
        }

        int count = gc.getReusableObjMan().process(context, elements, gc.getDataManager(), email, processAllRecords, ignoreErrors);

        if (publish){
            String sql = "INSERT INTO operationallowed VALUES (?,?,0)";
            for (String id : elements) {
                int intId = Integer.parseInt(id);
                dbms.execute(sql, 0, intId);
                dbms.execute(sql, 1, intId);
            }
        }

        dbms.commit();
        if( rebuildIndex ){
            new IndexRebuild().exec(params, context);
        } else {
            for (String uuid : elements) {
            	String id = ReusableObjManager.uuidToId(dbms, gc.getDataManager(), uuid, processAllRecords);
				gc.getDataManager().indexInThreadPoolIfPossible(dbms, id, false);
            }
        	
        }

        Element success = new Element("success");
        success.setText(count + " metadata elements have been analyzed and updated");
        return success;
    }

    public void init(String appPath, ServiceConfig params) throws Exception
    {
    }

}
