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
import org.fao.geonet.exceptions.UnAuthorizedException;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.services.Utils;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * Stores status on a metadata.
 */
public class UpdateStatus extends NotInReadOnlyModeService{

    /**
     *
     * @param appPath
     * @param params
     * @throws Exception
     */
	public void init(String appPath, ServiceConfig params) throws Exception {}

    /**
     *
     * @param params
     * @param context
     * @return
     * @throws Exception
     */
	public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		DataManager dataMan = gc.getDataManager();
		AccessManager am = gc.getAccessManager();
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		String id = Utils.getIdentifierFromParameters(params, context);

		//--- check access
		int iLocalId = Integer.parseInt(id);
		if (!dataMan.existsMetadata(dbms, iLocalId))
			throw new IllegalArgumentException("Metadata not found --> " + id);

		//--- only allow the owner of the record to set its status
		if (!am.isOwner(context, id)) {
			throw new UnAuthorizedException("Only the owner of the metadata can set the status. User is not the owner of the metadata", null);
		}

		String status = Util.getParam(params, Params.STATUS);
		String changeMessage = Util.getParam(params, Params.CHANGE_MESSAGE);
		String changeDate = new ISODate().toString();

		//--- use StatusActionsFactory and StatusActions class to 
		//--- change status and carry out behaviours for status changes
		StatusActionsFactory saf = new StatusActionsFactory(gc.getStatusActionsClass());

		StatusActions sa = saf.createStatusActions(context, dbms);

		Set<Integer> metadataIds = new HashSet<Integer>();
		metadataIds.add(iLocalId);

		Set<Integer> unchanged = saf.statusChange(sa, status, metadataIds, changeDate, changeMessage);

		//--- reindex metadata
		dataMan.indexMetadata(dbms, id);

		//--- return id for showing
		return new Element(Jeeves.Elem.RESPONSE).addContent(new Element(Geonet.Elem.ID).setText(id));
	}
}