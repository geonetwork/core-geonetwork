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

package org.fao.geonet.services.ownership;

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.jdom.Element;

import java.sql.SQLException;
import java.util.*;

/**
 *
 */
public class Transfer implements Service {
    /**
     *
     * @param appPath
     * @param params
     * @throws Exception
     */
	public void init(String appPath, ServiceConfig params) throws Exception {}

    /**
     *
     * @param params
     * @param context
     * @return
     * @throws Exception
     */
	public Element exec(Element params, ServiceContext context) throws Exception {
		String sourceUsr = Util.getParam(params, "sourceUser");
		String sourceGrp = Util.getParam(params, "sourceGroup");
		String targetUsr = Util.getParam(params, "targetUser");
		String targetGrp = Util.getParam(params, "targetGroup");

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dm = gc.getDataManager();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		//--- transfer privileges (if case)

		Set<String> sourcePriv = retrievePrivileges(dbms, sourceUsr, sourceGrp);
		Set<String> targetPriv = retrievePrivileges(dbms, null, targetGrp);

		//--- a commit just to release some resources

		dbms.commit();

		int privCount = 0;

		Set<String> metadata = new HashSet<String>();

		for (String priv : sourcePriv) {
			StringTokenizer st = new StringTokenizer(priv, "|");

			String opId = st.nextToken();
			String mdId = st.nextToken();

			dm.unsetOperation(context, dbms, mdId, sourceGrp, opId);

			if (!targetPriv.contains(priv))
				dbms.execute("INSERT INTO OperationAllowed(metadataId, groupId, operationId) " +
								 "VALUES(?,?,?)", mdId, targetGrp, opId);

			//***
			// dbms.execute("UPDATE Metadata SET owner=?, groupOwner=? WHERE id=?", targetUsr, targetGrp, mdId);
            dbms.execute("UPDATE Metadata SET owner=? WHERE id=?", targetUsr, mdId);

			metadata.add(mdId);
			privCount++;
		}

		dbms.commit();

		//--- reindex metadata
        List<String> list = new ArrayList<String>();
		for (String mdId : metadata) {
            list.add(mdId);
        }
        
        boolean workspace = false;
        dm.indexInThreadPool(context,list, dbms, workspace, true);

		//--- return summary
		return new Element("response")
			.addContent(new Element("privileges").setText(privCount      +""))
			.addContent(new Element("metadata")  .setText(metadata.size()+""));
	}

    /**
     *
     * @param dbms
     * @param userId can be null
     * @param groupId
     * @return
     * @throws SQLException
     */
	private Set<String> retrievePrivileges(Dbms dbms, String userId, String groupId) throws SQLException {
	    List list;
	    if(userId==null) {
            String query = "SELECT * FROM OperationAllowed WHERE groupId=?";
            list = dbms.select(query, groupId).getChildren();
	    } else {
            String query = "SELECT * FROM OperationAllowed, Metadata WHERE metadataId=id AND owner=? AND groupId=?";
            list = dbms.select(query, userId, groupId).getChildren();
	    }
		Set<String> result = new HashSet<String>();
		for (Object o : list) {
			Element elem = (Element) o;
			String  opId = elem.getChildText("operationid");
			String  mdId = elem.getChildText("metadataid");
			result.add(opId +"|"+ mdId);
		}
		return result;
	}
}
