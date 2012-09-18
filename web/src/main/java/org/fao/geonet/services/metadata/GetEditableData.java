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

import jeeves.exceptions.OperationNotAllowedEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MdInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.services.Utils;
import org.jdom.Element;

//=============================================================================

/** Retrieves a particular metadata with editing information. Access is restricted
  */

public class GetEditableData implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception {

		String id = Utils.getIdentifierFromParameters(params, context);
		boolean showValidationErrors = Util.getParam(params, Params.SHOWVALIDATIONERRORS, false);
        String justCreated = Util.getParam(params, Geonet.Elem.JUSTCREATED, null);

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        DataManager dataMan = gc.getDataManager();
        AccessManager accessMan = gc.getAccessManager();
        UserSession session = context.getUserSession();
        String userId = session.getUserId();

        //
        // check access
        //

        MdInfo info = dataMan.getMetadataInfo(dbms, id);
        if (info == null) {
            throw new IllegalArgumentException("Metadata not found --> " + id);
        }
        boolean canEdit = accessMan.canEdit(context, id);
        SettingManager settingManager = gc.getSettingManager();
        boolean harvesterEditing = settingManager.getValueAsBool("system/harvester/enableEditing");

        if(! canEdit) {
            throw new OperationNotAllowedEx("You are not authorized to edit this.");
        }
        else if(info.isHarvested && !harvesterEditing) {
            throw new OperationNotAllowedEx("You can not edit this because it is harvested and editing harvested metadata is not enabled.");
        }
        else if(info.isLocked && !info.lockedBy.equals(userId)) {
            throw new OperationNotAllowedEx("You can not edit this because it is locked and you do not own the lock.");
        }


        // Set current tab for new editing session if defined.
        Element elCurrTab = params.getChild(Params.CURRTAB);
        if (elCurrTab != null) {
            context.getUserSession().setProperty(Geonet.Session.METADATA_SHOW, elCurrTab.getText());
        }
		
        //-----------------------------------------------------------------------
		//--- get metadata

        // try to get from Workspace
        Element md = new AjaxEditUtils(context).getMetadataEmbeddedFromWorkspace(context, id, true, showValidationErrors);
        // not in workspace; try to get from metadata
        if (md == null)  {
            md = new AjaxEditUtils(context).getMetadataEmbedded(context, id, true, showValidationErrors);
            if (md == null)  {
			throw new IllegalArgumentException("Metadata not found --> " + id);
            }
            else {
                // lock metadata
                dataMan.lockMetadata(dbms, userId, id);
                // copy to workspace
                dataMan.saveWorkspace(dbms, id);
                // re-index md to index lock
                boolean workspace = false;
                dataMan.indexMetadata(dbms, id, false, workspace, true);
                workspace = true;
                dataMan.indexMetadata(dbms, id, false, workspace, true);
            }
        }
        else {
            //System.out.println("*** metadata found in workspace, id: " + id);
        }

        if(justCreated != null) {
       //   elMd.addContent(new Element("JUSTCREATED").setText("true"));
        }

		//--- return metadata
		return md;
	}
}

//=============================================================================

