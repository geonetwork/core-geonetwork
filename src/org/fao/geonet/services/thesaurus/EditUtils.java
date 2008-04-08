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

package org.fao.geonet.services.thesaurus;

import java.util.Hashtable;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.ThesaurusManager;
import org.jdom.Element;

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
		ThesaurusManager thesaurusMan = gc.getThesaurusManager();

		UserSession   session   = context.getUserSession();

		String name  = Util.getParam(params, Params.NAME);
		String dir   = Util.getParam(params, Params.DIR).replace('.', '/');		
		String fname = Util.getParam(params, Params.FNAME);

		//-----------------------------------------------------------------------
		//--- check access

		if (!thesaurusMan.existsThesaurus(name))
			throw new IllegalArgumentException("Thesaurus not found --> " + name);
	}

	//--------------------------------------------------------------------------
	/** Update metadata content
	  */

	public static void updateContent(ThesaurusManager thesaurusMan, Element params, ServiceContext context) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dataMan   = gc.getDataManager();

		String name  = Util.getParam(params, Params.NAME);
		String dir   = Util.getParam(params, Params.DIR).replace('.', '/');		
		String fname = Util.getParam(params, Params.FNAME);

		//--- build hashtable with changes
		//--- each change is a couple (pos, value)

		Hashtable htChanges = new Hashtable(100);

		java.util.List list = params.getChildren();

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

//		if (!thesaurusMan.updateThesaurus(name, htChanges))
//			throw new UpdateException(name);
	}

	//--------------------------------------------------------------------------

	public static void updateContent(Element params, ServiceContext context) throws Exception
	{
		updateContent(params, context);
	}
}

//=============================================================================

