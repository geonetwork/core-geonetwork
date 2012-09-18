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
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MdInfo;
import org.fao.geonet.kernel.SelectionManager;
import org.jdom.Element;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

//=============================================================================

/** Stores all operations allowed for a metadata
  */

public class BatchUpdatePrivileges implements Service
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
		DataManager   dm = gc.getDataManager();
		AccessManager accessMan = gc.getAccessManager();
		UserSession   us = context.getUserSession();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		context.info("Get selected metadata");
		SelectionManager sm = SelectionManager.getManager(us);

		Set<String> metadata = new HashSet<String>();
		Set<String> notFound = new HashSet<String>();
		Set<String> notOwner = new HashSet<String>();

		synchronized(sm.getSelection("metadata")) {
		for (Iterator<String> iter = sm.getSelection("metadata").iterator(); iter.hasNext();) {
			String uuid = (String) iter.next();
			String id   = dm.getMetadataId(dbms, uuid);

			//--- check access
			MdInfo info = dm.getMetadataInfo(dbms, id);
			if (info == null) {
				notFound.add(id);
			}
            else if (!accessMan.isOwner(context, id)) {
				notOwner.add(id);
			}
            else {

				//--- remove old operations
				boolean skip = false;

				//--- in case of owner, privileges for groups 0,1 and GUEST are 
				//--- disabled and are not sent to the server. So we cannot remove them
				boolean isAdmin = Geonet.Profile.ADMINISTRATOR.equals(us.getProfile());
				boolean isReviewer= Geonet.Profile.REVIEWER.equals(us.getProfile());

				if (us.getUserId().equals(info.owner) && !isAdmin && !isReviewer)
					skip = true;

				dm.deleteMetadataOper(dbms, id, skip);

				//--- set new ones

				List list = params.getChildren();

				for(int i=0; i<list.size(); i++) {
					Element el = (Element) list.get(i);

					String name  = el.getName();

					if (name.startsWith("_")) {
						StringTokenizer st = new StringTokenizer(name, "_");

						String groupId = st.nextToken();
						String operId  = st.nextToken();

						dm.setOperation(context, dbms, id, groupId, operId);
					}
				}
				metadata.add(id);
			}
		}
		}

		//--- reindex metadata
		context.info("Re-indexing metadata");
		BatchOpsMetadataReindexer r = new BatchOpsMetadataReindexer(dm, dbms, metadata);
		r.processWithFastIndexing();

		// -- for the moment just return the sizes - we could return the ids
		// -- at a later stage for some sort of result display
		return new Element(Jeeves.Elem.RESPONSE)
					.addContent(new Element("done")    .setText(metadata.size()+""))
					.addContent(new Element("notOwner").setText(notOwner.size()+""))
					.addContent(new Element("notFound").setText(notFound.size()+""));
	}
}

//=============================================================================

