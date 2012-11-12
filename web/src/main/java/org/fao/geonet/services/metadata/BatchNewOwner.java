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
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MdInfo;
import org.fao.geonet.kernel.SelectionManager;
import org.jdom.Element;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

//=============================================================================

/** Sets new owner for a set of metadata records
  */

public class BatchNewOwner implements Service
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
		DataManager   dm   = gc.getDataManager();
		AccessManager accessMan = gc.getAccessManager();
		UserSession   session   = context.getUserSession();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		String targetUsr = Util.getParam(params, Params.USER);
		String targetGrp = Util.getParam(params, Params.GROUP);

		Set<Integer> metadata = new HashSet<Integer>();
		Set<Integer> notFound = new HashSet<Integer>();
		Set<Integer> notOwner = new HashSet<Integer>();

		context.info("Get selected metadata");
		SelectionManager sm = SelectionManager.getManager(session);

		synchronized(sm.getSelection("metadata")) {
		for (Iterator<String> iter = sm.getSelection("metadata").iterator(); iter.hasNext();) {
			String uuid = (String) iter.next();
			String id   = dm.getMetadataId(dbms, uuid);

			context.info("Attempting to set metadata owner on: "+ id);

			//--- check existence and access
			MdInfo info = dm.getMetadataInfo(dbms, id);

			if (info == null) {
				notFound.add(new Integer(id));	
			} else if (!accessMan.isOwner(context, id)) {
				notOwner.add(new Integer(id));
			} else {

	 			//-- Get existing owner and privileges for that owner - note that 
				//-- owners don't actually have explicit permissions - only their 
				//-- group does which is why we have an ownerGroup (parameter groupid)
				String sourceUsr = info.owner; 
				String sourceGrp = info.groupOwner; 
				if (sourceGrp.equals("")) {
					context.info("Source Group for user "+sourceUsr+" was null, setting default privileges");
					dm.copyDefaultPrivForGroup(context, dbms, id, targetGrp, false);
				} else {
					Vector<String> sourcePriv = retrievePrivileges(dbms, id, sourceUsr, sourceGrp);

					// -- Set new privileges for new owner from privileges of the old  
					// -- owner, if none then set defaults
					if (sourcePriv.size() == 0) {
						dm.copyDefaultPrivForGroup(context, dbms, id, targetGrp, false);
						context.info("No privileges for user "+sourceUsr+" on metadata "+id+", so setting default privileges");
					} else {
						for (String priv : sourcePriv) {
							dm.unsetOperation(context, dbms, id, sourceGrp, priv);
							dm.setOperation(context, dbms, id, targetGrp, priv);
						}
					}
				}
				// -- set the new owner into the metadata record
				dm.updateMetadataOwner(dbms, Integer.parseInt(id), targetUsr, targetGrp);

				metadata.add(new Integer(id));
			}
		}
		}

		dbms.commit();

		// -- reindex metadata
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

	//--------------------------------------------------------------------------

	private Vector<String> retrievePrivileges(Dbms dbms, String id, String userId, String groupId) throws Exception
	{

		Object args[] = { new Integer(id), new Integer(id), new Integer(userId), new Integer(groupId) };
		String query = "SELECT * "+
							"FROM OperationAllowed, Metadata "+
							"WHERE metadataId=? AND id =? AND owner=? AND groupId=?";

		List list = dbms.select(query, args).getChildren();

		Vector<String> result = new Vector<String>();

		for (Object o : list)
		{
			Element elem = (Element) o;
			String  opId = elem.getChildText("operationid");

			result.add(opId);
		}

		return result;
	}

}

//=============================================================================

