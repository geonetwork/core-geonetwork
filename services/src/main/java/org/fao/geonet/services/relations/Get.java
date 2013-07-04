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

package org.fao.geonet.services.relations;

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.services.Utils;
import org.jdom.Element;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//=============================================================================

public class Get implements Service {
	public void init(String appPath, ServiceConfig params) throws Exception {
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context)
			throws Exception {
		int id = Integer.parseInt(Utils.getIdentifierFromParameters(params,
                context));
		String relation = Util.getParam(params, "relation", "normal");

		return getRelation(id, relation, context);
	}

	/**
	 * TODO : should we move relation management in DataManager or in a specific
	 * relation management class ?
	 * 
	 * @param id
	 * @param relation
	 * @param context
	 * @return
	 * @throws Exception 
	 */
	public static Element getRelation(int id, String relation, ServiceContext context) throws Exception {
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dm = gc.getBean(DataManager.class);
		
		Set<String> result = getRelationIds(id, relation, context);
		
				// --- retrieve metadata and return result
		Element response = new Element("response");

		for (String mdId : result) {
            boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
            Element md = dm.getMetadata(context, mdId, forEditing, withValidationErrors, keepXlinkAttributes);

			// --- we could have a race condition so, just perform a simple check
			if (md != null)
				response.addContent(md);
		}

		return response;
	}

	
	/**
	 * Method to query Relation table and get a Set of identifiers of related
	 * metadata
	 * 
	 * @param id
	 * @param relation
	 * @param context
	 * @return
	 * @throws Exception 
	 */
	public static Set<String> getRelationIds(int id, String relation, ServiceContext context) throws Exception {
		Dbms dbms = (Dbms) context.getResourceManager()
		.open(Geonet.Res.MAIN_DB);
		
		Set<String> result = new HashSet<String>();
		
		// --- perform proper queries to retrieve the id set
		if (relation.equals("normal") || relation.equals("full")) {
			String query = "SELECT relatedId FROM Relations WHERE id=?";
			result.addAll(retrieveIds(dbms, query, "relatedid", id));
		}

		if (relation.equals("reverse") || relation.equals("full")) {
			String query = "SELECT id FROM Relations WHERE relatedId=?";
			result.addAll(retrieveIds(dbms, query, "id", id));
		}

		return result;
	}
	
	/**
	 * Run the query and load a Set based on query results.
	 * 
	 * @param dbms
	 * @param query
	 * @param field
	 * @param id
	 * @return
	 * @throws java.sql.SQLException
	 */
	private static Set<String> retrieveIds(Dbms dbms, String query,
			String field, int id) throws SQLException {
		@SuppressWarnings("unchecked")
        List<Element> records = dbms.select(query, Integer.valueOf(id)).getChildren();
		Set<String> results = new HashSet<String>();

		for (Element rec : records) {
			String val = rec.getChildText(field);

			results.add(val);
		}

		return results;
	}
}