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

package org.fao.geonet.services.main;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import jeeves.exceptions.BadParameterEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;

//=============================================================================

public class Info implements Service
{
	private String xslPath;

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig config) throws Exception
	{
		xslPath = appPath + Geonet.Path.STYLESHEETS+ "/xml";
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getSettingManager();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		Element result = new Element("root");

		for (Iterator i=params.getChildren().iterator(); i.hasNext();)
		{
			Element el = (Element) i.next();

			String name = el.getName();
			String type = el.getText();

			if (!name.equals("type"))
				throw new BadParameterEx(name, type);

			if (type.equals("site"))
				result.addContent(gc.getSettingManager().get("system", -1));

			else if (type.equals("categories"))
				result.addContent(Lib.local.retrieve(dbms, "Categories"));

			else if (type.equals("groups"))
				result.addContent(getGroups(context, dbms));

			else if (type.equals("operations"))
				result.addContent(Lib.local.retrieve(dbms, "Operations"));

			else if (type.equals("regions"))
				result.addContent(Lib.local.retrieve(dbms, "Regions"));

			else if (type.equals("sources"))
				result.addContent(getSources(dbms, sm));

			else if (type.equals("users"))
				result.addContent(getUsers(context, dbms));

			else
				throw new BadParameterEx("type", type);
		}

		result.addContent(getEnv(context));

		return Xml.transform(result, xslPath +"/info.xsl");
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private Element getGroups(ServiceContext context, Dbms dbms) throws SQLException
	{
		UserSession session = context.getUserSession();

		if (!session.isAuthenticated())
			return Lib.local.retrieve(dbms, "Groups", "id < 2", "id");

		//--- retrieve user groups

		if (Geonet.Profile.ADMINISTRATOR.equals(session.getProfile()))
			return Lib.local.retrieve(dbms, "Groups", null, "id");
		else
		{
			String query = "SELECT groupId as id FROM UserGroups WHERE "+
								"userId=" + session.getUserId();

			Set<String> ids = Lib.element.getIds(dbms.select(query));
			Element groups = Lib.local.retrieve(dbms, "Groups", null, "id");

			return Lib.element.pruneChildren(groups, ids);
		}
	}

	//--------------------------------------------------------------------------

	private Element getSources(Dbms dbms, SettingManager sm) throws SQLException
	{
		String  query   = "SELECT * FROM Sources ORDER BY name";
		Element sources = new Element("sources");

		String siteId   = sm.getValue("system/site/siteId");
		String siteName = sm.getValue("system/site/name");

		add(sources, siteId, siteName);

		for (Object o : dbms.select(query).getChildren())
		{
			Element rec = (Element) o;

			String uuid = rec.getChildText("uuid");
			String name = rec.getChildText("name");

			add(sources, uuid, name);
		}

		return sources;
	}

	//--------------------------------------------------------------------------
	//--- Users
	//--------------------------------------------------------------------------

	private Element getUsers(ServiceContext context, Dbms dbms) throws SQLException
	{
		UserSession us   = context.getUserSession();
		List        list = getUsers(context, us, dbms);

		Element users = new Element("users");

		for (Object o : list)
		{
			Element user = (Element) o;

			user = (Element) user.clone();
			user.removeChild("password");
			user.setName("user");

			users.addContent(user);
		}

		return users;
	}

	//--------------------------------------------------------------------------

	private List getUsers(ServiceContext context, UserSession us, Dbms dbms) throws SQLException
	{
		if (!us.isAuthenticated())
			return new ArrayList<Element>();

		int id = Integer.parseInt(us.getUserId());

		if (us.getProfile().equals(Geonet.Profile.ADMINISTRATOR))
			return dbms.select("SELECT * FROM Users").getChildren();

		if (!us.getProfile().equals(Geonet.Profile.USER_ADMIN))
			return dbms.select("SELECT * FROM Users WHERE id=?", id).getChildren();

		//--- we have a user admin

		Set<String> hsMyGroups = getUserGroups(dbms, id);

		Set profileSet = context.getProfileManager().getProfilesSet(us.getProfile());

		//--- retrieve all users

		Element elUsers = dbms.select("SELECT * FROM Users ORDER BY username");

		//--- now filter them

		ArrayList<Element> alToRemove = new ArrayList<Element>();

		for(Object o : elUsers.getChildren())
		{
			Element elRec = (Element) o;

			String userId = elRec.getChildText("id");
			String profile= elRec.getChildText("profile");

			if (!profileSet.contains(profile))
				alToRemove.add(elRec);

			else if (!hsMyGroups.containsAll(getUserGroups(dbms, Integer.parseInt(userId))))
				alToRemove.add(elRec);
		}

		//--- remove unwanted users

		for(int i=0; i<alToRemove.size(); i++)
			alToRemove.get(i).detach();

		//--- return result

		return elUsers.getChildren();
	}

	//--------------------------------------------------------------------------

	private Set<String> getUserGroups(Dbms dbms, int id) throws SQLException
	{
		String query = "SELECT groupId AS id FROM UserGroups WHERE userId=?";

		List list = dbms.select(query, id).getChildren();

		HashSet<String> hs = new HashSet<String>();

		for(int i=0; i<list.size(); i++)
		{
			Element el = (Element) list.get(i);
			hs.add(el.getChildText("id"));
		}

		return hs;
	}

	//--------------------------------------------------------------------------
	//---
	//--- General purpose methods
	//---
	//--------------------------------------------------------------------------

	private void add(Element sources, String uuid, String name)
	{
		Element source = new Element("source")
					.addContent(new Element("uuid").setText(uuid))
					.addContent(new Element("name").setText(name));

		sources.addContent(source);
	}

	//--------------------------------------------------------------------------

	private Element getEnv(ServiceContext context)
	{
		return new Element("env")
						.addContent(new Element("baseURL").setText(context.getBaseUrl()));
	}
}

//=============================================================================

