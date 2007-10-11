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

package org.fao.geonet.services.resources;

import java.io.File;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.constants.Params;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.metadata.Update;
import org.jdom.Element;

//=============================================================================

/** Handles the file upload
  */

public class Upload implements Service
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
		String uploadDir = context.getUploadDir();

		String id     = Util.getParam(params, Params.ID);
		String ref    = Util.getParam(params, Params.REF);
		String fname  = Util.getParam(params, Params.FNAME);
		String access = Util.getParam(params, Params.ACCESS);

		Lib.resource.checkEditPrivilege(context, id);

		// move uploaded file to destination directory
		// note: uploadDir and rootDir must be in the same volume

		File dir = new File(Lib.resource.getDir(context, access, id));
		dir.mkdirs();

		// move uploaded file to destination directory
		File oldFile = new File(uploadDir, fname);
		File newFile = new File(dir,       fname);

		if (!oldFile.renameTo(newFile))
		{
			context.warning("Cannot move uploaded file");
			context.warning(" (C) Source : "+oldFile.getAbsolutePath());
			context.warning(" (C) Destin : "+newFile.getAbsolutePath());
			oldFile.delete();
			throw new Exception("Unable to move uploaded file to destination directory");
		}
		// update the metadata
		Element elem = new Element("_" + ref);
		params.addContent(elem);
		elem.setText(fname);
		return update.exec(params, context);
	}
}

//=============================================================================

