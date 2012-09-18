//=============================================================================
//===	Copyright (C) 2001-2012 Food and Agriculture Organization of the
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
import jeeves.exceptions.OperationNotAllowedEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MdInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.services.Utils;
import org.jdom.Element;

//=============================================================================

/** Adds a metadata to the subversion repository.
  */

public class Version implements Service
{
	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		DataManager   dataMan   = gc.getDataManager();
		AccessManager accessMan = gc.getAccessManager();
		UserSession   session   = context.getUserSession();
        String userId = session.getUserId();

		String id = Utils.getIdentifierFromParameters(params, context);
		
		//-----------------------------------------------------------------------
		//--- check access

		Element md = dataMan.getMetadataNoInfo(context, id);

		if (md == null)
			throw new IllegalArgumentException("Metadata not found --> " + id);
        MdInfo info = dataMan.getMetadataInfo(dbms, id);
        SettingManager settingManager = gc.getSettingManager();
        boolean harvesterEditing = settingManager.getValueAsBool("system/harvester/enableEditing");
        // TODO also check svnmanager is enabled
        // allow if not harvested or harvested metadata can be edited, AND it's not locked and you can edit or it's locked by you
        if(info.isHarvested && !harvesterEditing) {
            throw new OperationNotAllowedEx("You can not version this because it is harvested and editing of harvested metadata is not enabled.");
        }
        else if(!accessMan.canEdit(context, id)) {
            throw new OperationNotAllowedEx("You can not version this because you do not have editing rights to this metadata.");
        }
        else if(info.isLocked && !info.lockedBy.equals(userId)) {
            throw new OperationNotAllowedEx("You can not version this because this metadata is locked and you don't own the lock.");
        }

		//-----------------------------------------------------------------------
		//--- set metadata into the subversion repo

		dataMan.versionMetadata(context, id, md);

		Element elResp = new Element(Jeeves.Elem.RESPONSE);
		elResp.addContent(new Element(Geonet.Elem.ID).setText(id));

		return elResp;
	}
}

//=============================================================================

