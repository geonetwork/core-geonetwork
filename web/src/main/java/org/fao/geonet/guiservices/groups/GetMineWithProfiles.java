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

package org.fao.geonet.guiservices.groups;

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;

/**
 * Service used to return all groups and profiles for current user.
 */
public class GetMineWithProfiles implements Service {
	public void init(String appPath, ServiceConfig params) throws Exception {
	}

	public Element exec(Element params, ServiceContext context)
			throws Exception {
		UserSession session = context.getUserSession();

		if (!session.isAuthenticated())
			return new Element(Geonet.Elem.GROUPS);

		Dbms dbms = (Dbms) context.getResourceManager()
				.open(Geonet.Res.MAIN_DB);

		// --- retrieve user groups
		if (Geonet.Profile.ADMINISTRATOR.equals(session.getProfile())) {
			String query = "SELECT id, '' as profile FROM Groups WHERE id > 1";
			return dbms.select(query);
		} else {
			String query = "SELECT groupId as id, profile FROM UserGroups WHERE groupId > 1 "
					+ "AND userId=?";
			return dbms.select(query, session.getUserIdAsInt());
		}
	}
}