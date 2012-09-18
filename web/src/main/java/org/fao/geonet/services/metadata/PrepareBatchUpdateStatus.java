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
import jeeves.server.UserSession;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//=============================================================================

/** Returns all status values.
  */

public class PrepareBatchUpdateStatus implements Service
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
		AccessManager am = gc.getAccessManager();
		UserSession us = context.getUserSession();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		context.info("Get selected metadata");
		SelectionManager sm = SelectionManager.getManager(us);

		Set<String> ids = new HashSet<String>();

		//-----------------------------------------------------------------------
		//--- run through the selected set of metadata records 
		synchronized(sm.getSelection("metadata")) {
		for (Iterator<String> iter = sm.getSelection("metadata").iterator(); iter.hasNext();) {
			String uuid = iter.next();
			String id   = dataMan.getMetadataId(dbms, uuid);

			//--- check access, if owner then process 
			
			if (am.isOwner(context, id)) {
				ids.add(id);
			}
		}
		}

		//-----------------------------------------------------------------------
		//--- retrieve status values
		Element elStatus = Lib.local.retrieve(dbms, "StatusValues");
		List<Element> list = elStatus.getChildren();

		for (Element el : list) {
			el.setName(Geonet.Elem.STATUS);
		}

		//-----------------------------------------------------------------------
		//--- get the list of content reviewers for this metadata record
		Element cRevs = am.getContentReviewers(dbms, ids);
		cRevs.setName("contentReviewers");

		//-----------------------------------------------------------------------
		//--- put all together
		Element elRes = new Element(Jeeves.Elem.RESPONSE)
										.addContent(elStatus)
										.addContent(cRevs);

		return elRes;
	}
}

//=============================================================================


