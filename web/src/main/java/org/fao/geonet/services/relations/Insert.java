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

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.services.Utils;
import org.jdom.Element;

//=============================================================================

/**
 * Insert the relation between two metadata records. Input parameters could be
 * UUID or internal id.
 * 
 * TODO : Should we add a relation type to store different kind of relation. For
 * the time being, relation table is used to store link between iso19139 and
 * iso19110 metadata records.
 */
public class Insert extends NotInReadOnlyModeService {

	public void init(String appPath, ServiceConfig params) throws Exception {
	}

	/*
	 * Insert the relation between two metadata records.
	 * If it already exist, bypass insert statement and an alreadyExist flag.
	 * 
	 * @see jeeves.interfaces.Service#exec(org.jdom.Element,
	 * jeeves.server.context.ServiceContext) Parameter name: parentId - Parent
	 * metadata identifier Parameter name: childId - Child metadata identifier
	 */
	public Element serviceSpecificExec(Element params, ServiceContext context)
			throws Exception {
		int parentId = Integer.parseInt(Utils.getIdentifierFromParameters(
				params, context, Params.PARENT_UUID, Params.PARENT_ID));
		int childId = Integer.parseInt(Utils.getIdentifierFromParameters(
				params, context, Params.CHILD_UUID, Params.CHILD_ID));

		Dbms dbms = (Dbms) context.getResourceManager()
				.open(Geonet.Res.MAIN_DB);

		String query = "Select count(*) as exist from Relations where id=? and relatedId=?";
		Element record = dbms.select(query, parentId, childId).getChild("record");
		boolean exist = false;
		if (record.getChild("exist").getText().equals("1")) {
			exist = true;
		} else {
			// Add new relation
			query = "INSERT INTO Relations (id, relatedId) "
					+ "VALUES (?, ?)";
	
			dbms.execute(query, parentId, childId);
		}
		
		Element response = new Element(Jeeves.Elem.RESPONSE)
				.setAttribute("alreadyExist", String.valueOf(exist))
				.addContent(
						new Element("parentId").setText(String
								.valueOf(parentId)))
				.addContent(
						new Element("childId").setText(String.valueOf(childId)));

		return response;
	}
}