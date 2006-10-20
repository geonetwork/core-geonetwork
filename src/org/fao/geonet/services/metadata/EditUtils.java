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

import java.util.*;
import org.jdom.*;

import jeeves.resources.dbms.*;
import jeeves.server.*;
import jeeves.server.context.*;
import jeeves.utils.*;

import org.fao.geonet.constants.*;
import org.fao.geonet.kernel.*;
import org.fao.geonet.exceptions.*;
import org.fao.geonet.*;

//=============================================================================

/** Utilities
  */

class EditUtils
{
	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	/** Perform common editor preprocessing tasks
	  */

	static void preprocessUpdate(Element params, ServiceContext context, String operation) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dataMan   = gc.getDataManager();
		AccessManager accessMan = gc.getAccessManager();
		UserSession   session   = context.getUserSession();
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		String id      = Util.getParam(params, Params.ID);

		//-----------------------------------------------------------------------
		//--- handle current tab

		Element elCurrTab = params.getChild(Params.CURRTAB);

		if (elCurrTab != null)
			session.setProperty(Geonet.Session.METADATA_SHOW, elCurrTab.getText());

		//-----------------------------------------------------------------------
		//--- check access

		if (!dataMan.existsMetadata(dbms, id))
			throw new IllegalArgumentException("Metadata not found --> " + id);

		HashSet hsOper = accessMan.getOperations(context, id, context.getIpAddress());

		if (!hsOper.contains(operation))
			throw new JeevesException(JeevesException.PRIVILEGES);
	}

	//--------------------------------------------------------------------------
	/** Update metadata content
	  */

	public static void updateContent(Element params, ServiceContext context, boolean validate) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dataMan   = gc.getDataManager();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		String id      = Util.getParam(params, Params.ID);
		String version = Util.getParam(params, Params.VERSION);

		//--- build hashtable with changes
		//--- each change is a couple (pos, value)

		Hashtable htChanges = new Hashtable(100);

		List list = params.getChildren();

		for(int i=0; i<list.size(); i++)
		{
			Element el = (Element) list.get(i);

			String sPos = el.getName();
			String sVal = el.getText();

			if (sPos.startsWith("_"))
				htChanges.put(sPos.substring(1), sVal);
		}

		//-----------------------------------------------------------------------
		//--- update element and return status

		if (!dataMan.updateMetadata(dbms, id, version, htChanges, validate))
			throw new UpdateException(id);
	}

	//--------------------------------------------------------------------------

	public static void updateContent(Element params, ServiceContext context) throws Exception
	{
		updateContent(params, context, false);
	}
}

//=============================================================================

