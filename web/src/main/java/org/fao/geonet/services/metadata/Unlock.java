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
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.jdom.Element;

/**
 *  Unlock locked metadata.
 *
 *  @author heikki doeleman
 */
public class Unlock implements Service {
	private ServiceConfig config;

	public void init(String appPath, ServiceConfig params) throws Exception {
		config = params;
	}

	public Element exec(Element params, ServiceContext context) throws Exception {
        String id = Util.getParam(params, Params.ID);

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dataMan = gc.getDataManager();
        AccessManager accessManager = gc.getAccessManager();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

        String userId = context.getUserSession().getUserId();

        if(accessManager.unlockAllowed(userId, id, dbms)) {

            dataMan.unLockMetadata(dbms, id);
            dataMan.deleteFromWorkspace(dbms, id);
            boolean workspace = false;
            dataMan.indexMetadata(dbms, id, false, workspace, true);

            StatusActionsFactory saf = new StatusActionsFactory(gc.getStatusActionsClass());
            StatusActions sa = saf.createStatusActions(context, dbms);
            saf.onCancelEdit(sa, id);

        }

		Element elResp = new Element(Jeeves.Elem.RESPONSE);
        elResp.addContent(new Element(Geonet.Elem.ID).setText(id));

        return elResp;
    }

}