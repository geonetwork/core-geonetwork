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

package org.fao.geonet.services.thumbnail;

import jeeves.exceptions.OperationAbortedEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.ConcurrentUpdateEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

import java.io.File;

public class Unset extends NotInReadOnlyModeService {
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

	public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
		String id      = Util.getParam(params, Params.ID);
		String type    = Util.getParam(params, Params.TYPE);
		String version = Util.getParam(params, Params.VERSION);

		Lib.resource.checkEditPrivilege(context, id);

		//-----------------------------------------------------------------------
		//--- extract thumbnail filename

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		DataManager dataMan = gc.getDataManager();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		//--- check if the metadata has been modified from last time

		if (version != null && !dataMan.getVersion(id).equals(version))
			throw new ConcurrentUpdateEx(id);

		Element result = dataMan.getThumbnails(dbms, id, context);

		if (result == null)
			throw new OperationAbortedEx("Metadata not found", id);

		result = result.getChild(type);

		if (result == null)
			throw new OperationAbortedEx("Metadata has no thumbnail", id);

		String file = Lib.resource.getDir(context, Params.Access.PUBLIC, id) + getFileName(result.getText());

		//-----------------------------------------------------------------------
		//--- remove thumbnail

		dataMan.unsetThumbnail(context, dbms, id, type.equals("small"), true);
		
		
		File thumbnail = new File(file);
		if (thumbnail.exists()) {
			if (!thumbnail.delete()) {
				context.error("Error while deleting thumbnail: " + file);
			}
		} else {
            if(context.isDebug())
			    context.debug("Thumbnail does not exist: " + file);
		}
		
		//-----------------------------------------------------------------------

		Element response = new Element("a");
		response.addContent(new Element("id").setText(id));
		response.addContent(new Element("version").setText(dataMan.getNewVersion(id)));

		return response;
	}

	/*
	 * Remove thumbnail images. 
	 * (Useful for harvester which can not edit metadata but could have
	 * set up a thumbnail on harvesting
	 * )
	 */
	public void removeThumbnailFile (String id,  String type, ServiceContext context) throws Exception {
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		DataManager dataMan = gc.getDataManager();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		
		Element result = dataMan.getThumbnails(dbms, id, context);
		
		if (result == null)
			throw new OperationAbortedEx("Metadata not found", id);
		
		if (type == null) {
			remove (result, "thumbnail", id, context);
			remove (result, "large_thumbnail", id, context);
		} else {
			remove (result, type, id, context);
		}
		
	}
	
	private void remove (Element result, String type, String id, ServiceContext context) throws Exception {
		
		result = result.getChild(type);
		
		if (result == null)
			throw new OperationAbortedEx("Metadata has no thumbnail", id);

		String file = Lib.resource.getDir(context, Params.Access.PUBLIC, id) + getFileName(result.getText());
		
		if (!new File(file).delete())
			context.error("Error while deleting thumbnail : "+file);
		
		
	} 
	
	//--------------------------------------------------------------------------
	
	/**
	 * Return file name from full url thumbnail formated as
	 * http://wwwmyCatalogue.com:8080/srv/eng/resources.get?uuid=34baff6e-3880-4589-a5e9-4aa376ecd2a5&fname=snapshot3.png
	 * @param file
	 * @return
	 */
	private String getFileName(String file)
	{
		if(file.indexOf(FNAME_PARAM) < 0) {
			return file;
		}
		else {
			return file.substring(file.lastIndexOf(FNAME_PARAM)+FNAME_PARAM.length());
		}
	}
	
	//--------------------------------------------------------------------------
		//---
		//--- Variables
		//---
		//--------------------------------------------------------------------------

		private static final String FNAME_PARAM   = "fname=";

}
