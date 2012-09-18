//=============================================================================
//===	Copyright (C) 2001-2012 Food and Agriculture Organization of the
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
import org.fao.geonet.kernel.SelectionManager;
import org.jdom.Element;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//=============================================================================

/** Versions all selected metadata by adding to subversion repo.
  */

public class BatchVersion implements Service
{
	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dataMan   = gc.getDataManager();
		AccessManager accessMan = gc.getAccessManager();
		UserSession   session   = context.getUserSession();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		Set<String> metadata = new HashSet<String>();
		Set<String> notFound = new HashSet<String>();
		Set<String> notOwner = new HashSet<String>();

        if(context.isDebug())
            context.debug("Get selected metadata");
		SelectionManager sm = SelectionManager.getManager(session);

		synchronized(sm.getSelection("metadata")) {
		for (Iterator<String> iter = sm.getSelection("metadata").iterator(); iter.hasNext();) {
			String uuid = iter.next();
            if(context.isDebug())
                context.debug("Deleting metadata with uuid:"+ uuid);

			String id   = dataMan.getMetadataId(dbms, uuid);
			//--- Metadata may have been deleted since selection
			if (id != null) {
				//-----------------------------------------------------------------------
				//--- check access
				Element md = dataMan.getMetadataNoInfo(context, id);
	
				if (md == null) {
					notFound.add(id);
				} else if (!accessMan.isOwner(context, id)) {
					notOwner.add(id);
				} else {
	
					//--- now set metadata into subversion repo
					dataMan.versionMetadata(context, id, md);
                    if(context.isDebug())
                        context.debug("  Metadata with id " + id + " added to subversion repo.");
					metadata.add(id);
				}
			} else
            if(context.isDebug())
                context.debug("  Metadata not found in db:"+ uuid);
				// TODO : add to notFound set

		}
		}

		// -- for the moment just return the sizes - we could return the ids
		// -- at a later stage for some sort of result display
		return new Element(Jeeves.Elem.RESPONSE)
			.addContent(new Element("done")    .setText(metadata.size()+""))
			.addContent(new Element("notOwner").setText(notOwner.size()+""))
			.addContent(new Element("notFound").setText(notFound.size()+""));
	}

}

//=============================================================================

