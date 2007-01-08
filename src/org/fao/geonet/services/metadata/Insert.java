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

package org.fao.geonet.services.metadata;

import java.util.UUID;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.jdom.Element;

//=============================================================================

/** Inserts a new metadata to the system (data is validated)
  */

public class Insert implements Service
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

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		DataManager dataMan = gc.getDataManager();

		String data       = Util.getParam(params, Params.DATA);
		String group      = Util.getParam(params, Params.GROUP);
		String schema     = Util.getParam(params, Params.SCHEMA);
		String isTemplate = Util.getParam(params, Params.TEMPLATE, "n");
		String title      = Util.getParam(params, Params.TITLE);

		boolean validate = Util.getParam(params, Params.VALIDATE, "off").equals("on");

		//-----------------------------------------------------------------------
		//--- add the DTD to the input xml to perform validation

		Element xml = Xml.loadString(data, false);

		if (validate)
			dataMan.validate(schema, xml);

		//-----------------------------------------------------------------------
		//--- if the uuid does not exist and is not a template we generate it
		
		String uuid;
		if (isTemplate.equals("n"))
		{
			uuid = dataMan.extractUUID(schema, xml);
			if (uuid.length() == 0) uuid = UUID.randomUUID().toString();
		}
		else uuid = UUID.randomUUID().toString();
		
		//-----------------------------------------------------------------------
		//--- insert metadata into the system

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		String id = dataMan.insertMetadata(dbms, schema, group, xml,
													  context.getSerialFactory(), gc.getSiteId(), uuid, isTemplate, title);

		Element response = new Element(Jeeves.Elem.RESPONSE);
		response.addContent(new Element(Params.ID).setText(id));

		return response;
	};
}

//=============================================================================


