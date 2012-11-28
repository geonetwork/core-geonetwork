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

import jeeves.constants.Jeeves;
import jeeves.exceptions.BadParameterEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ProfileManager;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.security.GeonetworkUser;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.util.z3950.RepositoryInfo;
import org.jdom.Element;

//=============================================================================

public class Info implements Service
{
	private String xslPath;
	private String xmlPath;
	private String otherSheets;
	private ServiceConfig _config;

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig config) throws Exception
	{
		xslPath = appPath + Geonet.Path.STYLESHEETS+ "/xml";
		otherSheets = appPath + Geonet.Path.STYLESHEETS;
		xmlPath = appPath + Geonet.Path.XML;
		_config = config;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element inParams, ServiceContext context) throws Exception
	{
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getSettingManager();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		Element params = (Element)inParams.clone();

		// --- if we have a parameter specified in the config then use it instead
		// --- of the usual params 
		String ptype = _config.getValue("type");
		if (ptype != null) {
			params.removeContent();
			params.addContent(new Element("type").setText(ptype));
		}

		Element result = new Element("root");

		for (Iterator i=params.getChildren("type").iterator(); i.hasNext();)
		{
			Element el = (Element) i.next();

			String name = el.getName();
			String type = el.getText();


			if (type.equals("site")) {
				result.addContent(gc.getSettingManager().get("system", -1));
			}

			else if (type.equals("inspire"))
				result.addContent(gc.getSettingManager().get("system/inspire", -1));

			else if (type.equals("categories"))
				result.addContent(Lib.local.retrieve(dbms, "Categories"));

			else if (type.equals("groups"))
				result.addContent(getGroups(context, dbms, params.getChildText("profile")));

			else if (type.equals("operations"))
				result.addContent(Lib.local.retrieve(dbms, "Operations"));

			else if (type.equals("regions"))
				result.addContent(Lib.local.retrieve(dbms, "Regions"));

            else if (type.equals("isolanguages"))
                result.addContent(Lib.local.retrieve(dbms, "IsoLanguages"));

			else if (type.equals("sources"))
				result.addContent(getSources(dbms, sm));

			else if (type.equals("users"))
				result.addContent(getUsers(context, dbms));

			else if (type.equals("templates"))
				result.addContent(getTemplates(context));

			else if (type.equals("z3950repositories"))
				result.addContent(getZRepositories(context, sm));

			else if (type.equals("me"))
				result.addContent(getMyInfo(context));
			
			else if (type.equals("auth"))
				result.addContent(getAuth(context));
			
			else
				throw new BadParameterEx("Unknown type parameter value.", type);
		}
		
		result.addContent(getEnv(context));

		return Xml.transform(result, xslPath +"/info.xsl");
	}

	private Element getAuth(ServiceContext context) {
		Element auth = new Element("auth");
		Element cas = new Element("casEnabled").setText(Boolean.toString(ProfileManager.isCasEnabled()));
		auth.addContent(cas);

		return auth;
	}

	

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------


	private Element getMyInfo(ServiceContext context) {
		Element data = new Element("me");
		UserSession userSession = context.getUserSession();
		if (userSession.isAuthenticated()) {
			data.setAttribute("authenticated","true");
			data.addContent(new Element(Geonet.Elem.PROFILE).setText(userSession.getProfile()))
				.addContent(new Element(GeonetworkUser.USERNAME_COLUMN).setText(userSession.getUsername()))
				.addContent(new Element(Geonet.Elem.ID).setText(userSession.getUserId()))
				.addContent(new Element(Geonet.Elem.NAME).setText(userSession.getName()))
				.addContent(new Element(Geonet.Elem.SURNAME).setText(userSession.getSurname()))
				.addContent(new Element(Geonet.Elem.EMAIL).setText(userSession.getEmailAddr()));
		} else {
			data.setAttribute("authenticated","false");
		}
		return data;
	}

	private Element getGroups(ServiceContext context, Dbms dbms, String profile) throws SQLException
	{
		UserSession session = context.getUserSession();

		if (!session.isAuthenticated()) {
			return Lib.local.retrieveWhereOrderBy(dbms, "Groups", "id < ?", "id", 2);
		}

		//--- retrieve user groups

		if (Geonet.Profile.ADMINISTRATOR.equals(session.getProfile()))
			return Lib.local.retrieveWhereOrderBy(dbms, "Groups", null, "id");
		else
		{
			Element list = null;
			if (profile == null) {
				String query = "SELECT groupId AS id FROM UserGroups WHERE userId=?";
				list = dbms.select(query, session.getUserIdAsInt());
			} else {
				String query = "SELECT groupId AS id FROM UserGroups WHERE userId=? and profile=?";
				list = dbms.select(query, session.getUserIdAsInt(), profile);
			}

			Set<String> ids = Lib.element.getIds(list);
			Element groups = Lib.local.retrieveWhereOrderBy(dbms, "Groups", null, "id");

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
	//--- ZRepositories
	//--------------------------------------------------------------------------

	public Element getZRepositories(ServiceContext context, SettingManager sm) throws Exception
	{
		boolean z3950Enable   = sm.getValue("system/z3950/enable").equals("true");

		List<RepositoryInfo> repoList = new ArrayList<RepositoryInfo>(RepositoryInfo.getRepositories(context));

		Element response = new Element("z3950repositories");

		for (RepositoryInfo repo : repoList) {
			if (!z3950Enable && repo.getClassName().startsWith("org.fao.geonet") ) {
				continue; // skip Local GeoNetwork Z server if not enabled
			} else {
				response.addContent(buildRecord(repo.getDn(),repo.getName(),repo.getCode(),repo.getServerCode()));
			}
		}

		return response;
	}

	//--------------------------------------------------------------------------
	//--- Templates
	//--------------------------------------------------------------------------

	private Element getTemplates(ServiceContext context) throws Exception
	{
		String styleSheet = otherSheets +"/portal-present.xsl";
		Element result = search(context).setName(Jeeves.Elem.RESPONSE);
		Element root   = new Element("root");

		root.addContent(result);

		List list = Xml.transform(root, styleSheet).getChildren();

		Element response = new Element("templates");

		for(int i=0; i<list.size(); i++)
		{
			Element elem = (Element) list.get(i);
			Element info = elem.getChild(Edit.RootChild.INFO, Edit.NAMESPACE);

			if (!elem.getName().equals("metadata"))
				continue;

			String id       = info.getChildText(Edit.Info.Elem.ID);
			String template = info.getChildText(Edit.Info.Elem.IS_TEMPLATE);
			String schema   = info.getChildText(Edit.Info.Elem.SCHEMA);

			if (template.equals("y"))
				response.addContent(buildTemplateRecord(id, elem.getChildText("title"), schema));
		}

		return response;
	}

	//--------------------------------------------------------------------------

	private Element search(ServiceContext context) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		context.info("Creating searcher");

		Element       params = buildParams();
		ServiceConfig config = new ServiceConfig();

		SearchManager searchMan = gc.getSearchmanager();
		MetaSearcher  searcher  = searchMan.newSearcher(SearchManager.LUCENE, Geonet.File.SEARCH_LUCENE);

		searcher.search(context, params, config);

		params.addContent(new Element("from").setText("1"));
		params.addContent(new Element("to").setText(searcher.getSize() +""));

		Element result = searcher.present(context, params, config);

		searcher.close();

		return result;
	}

	//--------------------------------------------------------------------------

	private Element buildParams()
	{
		Element params = new Element(Jeeves.Elem.REQUEST);
		String arParams[] = {
			"extended", "off",
			"remote",   "off",
			"attrset",  "geo",
			"template", "y",
			"any",      "",
		};

		for(int i=0; i<arParams.length/2; i++)
			params.addContent(new Element(arParams[i*2]).setText(arParams[i*2 +1]));

		return params;
	}

	//--------------------------------------------------------------------------

	private Element buildRecord(String id, String name)
	{
		return buildRecord(id, name, null, null);
	}

	//--------------------------------------------------------------------------

	private Element buildTemplateRecord(String id, String title, String schema)
	{
		return buildRecord(id, title, schema, null);
	}


	//--------------------------------------------------------------------------

	private Element buildRecord(String id, String name, String code, String serverCode)
	{
		Element el = new Element("record");

		Element idE = new Element("id").setText(id);
		if (code != null) idE.setAttribute("code", code);
		if (serverCode != null) idE.setAttribute("serverCode", serverCode);
		el.addContent(idE);
		el.addContent(new Element("name").setText(name));

		return el;
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

