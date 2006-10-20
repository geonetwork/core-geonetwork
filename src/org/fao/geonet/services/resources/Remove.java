//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.services.resources;

import java.io.File;
import java.util.Hashtable;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.JeevesException;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.exceptions.GeoNetException;
import org.fao.geonet.services.metadata.Update;
import org.fao.geonet.util.ResUtil;
import org.jdom.Element;

//=============================================================================

/** Deletes an uploaded file from the database
  */

public class Remove implements Service
{
	private Element config;
	private Update  update = new Update();

	//-----------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//-----------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception
	{
		update.init(appPath, params);
	}

	//-----------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//-----------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		DataManager   dataMan   = gc.getDataManager();

		String id     = Util.getParam(params, Params.ID);
		String ref    = Util.getParam(params, Params.REF);
		String access = Util.getParam(params, Params.ACCESS);

		ResUtil.checkPrivilege(context, id, AccessManager.OPER_EDIT);

		// get online resource name
		Element metadata = dataMan.getMetadata(context, id, true);
		Element elem     = dataMan.getElementByRef(metadata, ref);

		if (elem == null)
			throw new GeoNetException("element with ref='" + ref + "' not found", GeoNetException.ERROR);

		String fname = elem.getText();

		// delete online resource
		File dir  = new File(ResUtil.getResDir(context, access, id));
		File file = new File(dir, fname);

		if (!file.exists())
			throw new GeoNetException("resource '" + fname + "' not found", GeoNetException.FILE_NOT_FOUND);

		if (!file.delete())
			throw new IllegalArgumentException("unable to delete resource");

		// update the metadata
		params.addContent(new Element("_" + ref));
		Element version = params.getChild("version");
		version.setText((Integer.parseInt(version.getText()) + 1)  + "");
		return update.exec(params, context);
	}
}

//=============================================================================

