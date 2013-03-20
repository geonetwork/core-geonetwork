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
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MdInfo;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.util.FileCopyMgr;
import org.jdom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Removes a metadata from the system.
 */
public class BatchDelete extends NotInReadOnlyModeService {
	public void init(String appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);
    }

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dataMan   = gc.getDataManager();
		AccessManager accessMan = gc.getAccessManager();
		UserSession   session   = context.getUserSession();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		Set<Integer> metadata = new HashSet<Integer>();
		Set<Integer> notFound = new HashSet<Integer>();
		Set<Integer> notOwner = new HashSet<Integer>();

        if(context.isDebug())
            context.debug("Get selected metadata");
		SelectionManager sm = SelectionManager.getManager(session);

		synchronized(sm.getSelection("metadata")) {
		for (Iterator<String> iter = sm.getSelection("metadata").iterator(); iter.hasNext();) {
			String uuid = (String) iter.next();
            if(context.isDebug())
                context.debug("Deleting metadata with uuid:"+ uuid);

			String id   = dataMan.getMetadataId(dbms, uuid);
			//--- Metadata may have been deleted since selection
			if (id != null) {
				//-----------------------------------------------------------------------
				//--- check access
	
				MdInfo info = dataMan.getMetadataInfo(dbms, id);
	
				if (info == null) {
					notFound.add(new Integer(id));
				} else if (!accessMan.isOwner(context, id)) {
					notOwner.add(new Integer(id));
				} else {
	
					//--- backup metadata in 'removed' folder
					if (info.template != MdInfo.Template.SUBTEMPLATE) {
						backupFile(context, id, info.uuid, MEFLib.doExport(context, info.uuid, "full", false, true, false));
					}
			
					//--- remove the metadata directory
					File pb = new File(Lib.resource.getMetadataDir(context, id));
					FileCopyMgr.removeDirectoryOrFile(pb);
	
					//--- delete metadata and return status
					dataMan.deleteMetadata(context, dbms, id);
                    if(context.isDebug())
                        context.debug("  Metadata with id " + id + " deleted.");
					metadata.add(new Integer(id));
				}
			} else
            if(context.isDebug())
                context.debug("  Metadata not found in db:"+ uuid);
				// TODO : add to notFound set
			}
		}
		// Clear the selection after delete
		SelectionManager.updateSelection("metadata", session, params.addContent(new Element("selected").setText("remove-all")), context);
		
		// -- for the moment just return the sizes - we could return the ids
		// -- at a later stage for some sort of result display
		return new Element(Jeeves.Elem.RESPONSE)
			.addContent(new Element("done")    .setText(metadata.size()+""))
			.addContent(new Element("notOwner").setText(notOwner.size()+""))
			.addContent(new Element("notFound").setText(notFound.size()+""));
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private void backupFile(ServiceContext context, String id, String uuid, String file)
	{
		String outDir = Lib.resource.getRemovedDir(context, id);
		String outFile= outDir + uuid +".mef";

		new File(outDir).mkdirs();

		try
		{
			FileInputStream  is = new FileInputStream(file);
			FileOutputStream os = new FileOutputStream(outFile);

			BinaryFile.copy(is, os, true, true);
		}
		catch(Exception e)
		{
			context.warning("Cannot backup mef file : "+e.getMessage());
			e.printStackTrace();
		}

		new File(file).delete();
	}
}

//=============================================================================

