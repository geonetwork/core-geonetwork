//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.schedules;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Schedule;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ScheduleContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import jeeves.xml.XmlRequest;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;

//=============================================================================

public class MetadataSync implements Schedule
{
	private class Attr
	{
		private static final String ID   = "id";
		private static final String NAME = "name";
	}

	//--------------------------------------------------------------------------

	private class Elem
	{
		private static final String SITES          ="sites";
		private static final String    GENERAL     = "general";
		private static final String       HOST     = "host";
		private static final String       PORT     = "port";
		private static final String       LANGUAGE = "lang";
		private static final String       USERNAME = "username";
		private static final String       PASSWORD = "password";
		private static final String    SERVICES    = "services";
		private static final String       LOGIN    = "login";
		private static final String       LOGOUT   = "logout";
	//	private static final String       SEARCH   = "search"; // same as SEARCH
		private static final String       GET      = "get";
		private static final String    SEARCH      = "search";
		private static final String    GROUP       = "group";
		private static final String    	PRIVILEGE = "privilege";
	};

	//--------------------------------------------------------------------------

	private Vector veSites = new Vector();

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception
	{
		for(Iterator sites = params.getChildren(Elem.SITES); sites.hasNext();)
		{
			Element  site = (Element) sites.next();
			SiteInfo si   = new SiteInfo();

			Element general  = Util.getChild(site, Elem.GENERAL);
			Element services = Util.getChild(site, Elem.SERVICES);

			si.name     = Util.getAttrib(site, Attr.NAME);
			si.host     = Util.getParam(general, Elem.HOST);
			si.port     = Integer.parseInt(Util.getParam(general, Elem.PORT, "80"));
			si.language = Util.getParam(general, Elem.LANGUAGE, "en");
			si.username = Util.getParam(general, Elem.USERNAME, "");
			si.password = Util.getParam(general, Elem.PASSWORD, "");

			si.login  = Util.getParam(services, Elem.LOGIN);
			si.logout = Util.getParam(services, Elem.LOGOUT);
			si.search = Util.getParam(services, Elem.SEARCH);
			si.get    = Util.getParam(services, Elem.GET);

			//--- setup search queries

			for(Iterator q=site.getChildren(Elem.SEARCH).iterator(); q.hasNext(); )
			{
				Element query = (Element) q.next();

				query = (Element) query.clone();
				query.setName(Jeeves.Elem.REQUEST);
				si.queries.add(query);
			}

			//--- setup group privileges

			for(Iterator g=site.getChildren(Elem.GROUP).iterator(); g.hasNext(); )
			{
				Element group = (Element) g.next();

				Group grp = new Group();
				grp.groupId = Util.getAttrib(group, Attr.ID);
				si.groups.add(grp);

				for(Iterator p=group.getChildren(Elem.PRIVILEGE).iterator(); p.hasNext();)
				{
					String priv = ((Element) p.next()).getValue();

					int numPriv = AccessManager.getPrivilegeId(priv);

					if (numPriv == -1)
						throw new IllegalArgumentException("Unknown privilege : "+ priv);

					grp.privileges.add(new Integer(numPriv));
				}
			}

			veSites.add(si);
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Schedule
	//---
	//--------------------------------------------------------------------------

	public void exec(ScheduleContext context) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		DataManager dataMan = gc.getDataManager();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		CategMapping mapCategories = new CategMapping(dbms);

		for(Iterator i=veSites.iterator(); i.hasNext();)
		{
			SiteInfo si = (SiteInfo) i.next();

			XmlRequest req = new XmlRequest(si.host, si.port);

			req.setSiteName(si.name);
			req.setLanguage(si.language);

			//--- login

			if (!si.username.equals(""))
			{
				context.info("Logging in to : "+ si.name);

				req.addParam("username", si.username);
				req.addParam("password", si.password);
				req.execute(si.login);
			}

			//--- search

			for(Iterator j=si.queries.iterator(); j.hasNext(); )
			{
				Element params = (Element) j.next();

				context.info("Searching on : "+ si.name);
				req.clearParams();
				req.setParams(params);

				Element result = req.execute(si.search);

				context.debug("Obtained:\n"+Xml.getString(result));

				//--- alignment

				String siteId = params.getChildText("siteId");

				if (siteId == null)
				{
					context.error("Missing 'siteId' parameter in search query");
					throw new IllegalArgumentException("Missing 'siteId' parameter in search query");
				}

				alignSite(context, dataMan, dbms, result, si, req, mapCategories, siteId);
			}

			//--- logout

			if (!si.username.equals(""))
			{
				context.info("Logging out from : "+ si.name);

				req.clearParams();
				req.execute(si.logout);
			}
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Alignment method
	//---
	//--------------------------------------------------------------------------

	private void alignSite(ScheduleContext context, DataManager dm, Dbms dbms,
								  Element result, SiteInfo si, XmlRequest req,
								  CategMapping mapCategories, String siteId) throws Exception
	{
		context.info("Start of alignment for site-id : "+ siteId);

		List mdList = result.getChildren("metadata");

		//-----------------------------------------------------------------------
		//--- retrieve local uuids for given site-id

		UuidMapping mapUuid = new UuidMapping(dbms, siteId);

		//-----------------------------------------------------------------------
		//--- remove old metadata

		for (Iterator i=mapUuid.getUuids(); i.hasNext();)
		{
			String uuid = (String) i.next();

			if (!exists(mdList, uuid))
			{
				String id = mapUuid.getId(uuid);

				dm.deleteMetadata(dbms, id);
				dbms.commit();
				context.debug("  - Removed metadata with id="+ id);
			}
		}

		//-----------------------------------------------------------------------
		//--- insert/update new metadata

		for(Iterator i=mdList.iterator(); i.hasNext(); )
		{
			Element elInfo = ((Element) i.next()).getChild("info", Edit.NAMESPACE);

			String remoteId  = elInfo.getChildText("id");
			String remoteUuid= elInfo.getChildText("uuid");
			String schema    = elInfo.getChildText("schema");
			String createDate= elInfo.getChildText("createDate");
			String changeDate= elInfo.getChildText("changeDate");

			List catList = elInfo.getChildren("category");

			context.debug("Obtained remote id="+ remoteId +", changeDate="+ changeDate);

			if (!dm.existsSchema(schema))
				context.debug("  - Skipping unsupported schema : "+ schema);
			else
			{
				String id = getLocalId(dbms, remoteUuid);

				if (id == null)
				{
					//--- inserting new metadata

					id = dm.insertMetadataExt(dbms, schema, getRemoteMetadata(req, remoteId, si),
													  context.getSerialFactory(), siteId,
													  createDate, changeDate, remoteUuid, null);

					context.debug("  - Added metadata with id="+ id);

					//--- store privileges for group

					for(Iterator g=si.groups.iterator(); g.hasNext();)
					{
						Group group = (Group) g.next();

						context.debug("    - Setting privileges for group : "+group.groupId);

						for(Iterator p=group.privileges.iterator(); p.hasNext(); )
						{
							int priv = ((Integer) p.next()).intValue();

							dm.setOperation(dbms, id, group.groupId, priv +"");
							context.debug("       --> "+ AccessManager.getPrivilegeName(priv));
						}
					}

					//--- adding categories

					for(Iterator j=catList.iterator(); j.hasNext();)
					{
						String catName = ((Element) j.next()).getText();
						String catId   = mapCategories.getId(catName);

						if (catId != null)
						{
							dm.setCategory(dbms, id, catId);
							context.debug("    - Set category : "+ catName);
						}
					}
				}
				else
				{
					//--- updating an existing metadata

					if (mapUuid.getId(remoteUuid) == null)
					{
						context.error("  - Warning! The remote uuid '"+ remoteUuid +"' does not belong to site '"+ siteId+"'");
						context.error("     - The site id of this metadata has been changed.");
						context.error("     - The metadata update will be skipped.");
					}
					else
					{
						String date = mapUuid.getChangeDate(remoteUuid);

						if (updateCondition(date, changeDate))
						{
							dm.updateMetadataExt(dbms, id, getRemoteMetadata(req, remoteId, si), changeDate);

							context.debug("  - Updated local metadata with id="+ id);
						}
						else
						{
							context.debug("  - Nothing to do to local metadata with id="+ id);
						}

						//--- removing categories

						List locCateg = dm.getCategories(dbms, id).getChildren();

						for(int c=0; c<locCateg.size(); c++)
						{
							Element el = (Element) locCateg.get(c);

							String catId   = el.getChildText("id");
							String catName = el.getChildText("name").toLowerCase();

							if (!existsCategory(catList, catName))
								if (dm.isCategorySet(dbms, id, catId))
								{
									dm.unsetCategory(dbms, id, catId);
									context.debug("    - Unset category : "+ catName +" for id : "+id);
								}
						}

						//--- checking categories

						for(Iterator j=catList.iterator(); j.hasNext();)
						{
							String catName = ((Element) j.next()).getText();
							String catId   = mapCategories.getId(catName);

							if (catId != null)
								if (!dm.isCategorySet(dbms, id, catId))
								{
									dm.setCategory(dbms, id, catId);
									context.debug("    - Set category : "+ catName +" for id : "+id);
								}
						}
					}
				}

				dbms.commit();
				dm.indexMetadata(dbms, id);
			}
		}

		context.info("End of alignment for site-id : "+ siteId);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Return true if the sourceId is present in the remote site
	//---
	//--------------------------------------------------------------------------

	private boolean exists(List mdList, String uuid)
	{
		for(Iterator i=mdList.iterator(); i.hasNext(); )
		{
			Element elInfo = ((Element) i.next()).getChild("info", Edit.NAMESPACE);

			if (uuid.equals(elInfo.getChildText("uuid")))
				return true;
		}

		return false;
	}

	//--------------------------------------------------------------------------

	private Element getRemoteMetadata(XmlRequest req, String id, SiteInfo si) throws Exception
	{
		req.clearParams();
		req.addParam("id", id);

		Element elMetadata = req.execute(si.get);

		Element elInfo = elMetadata.getChild("info", Edit.NAMESPACE);

		if (elInfo != null)
			elInfo.detach();

		return elMetadata;

	}

	//--------------------------------------------------------------------------

	private boolean existsCategory(List catList, String name)
	{
		for(Iterator i=catList.iterator(); i.hasNext();)
		{
			String catName = ((Element) i.next()).getText();

			if (catName.equals(name))
				return true;
		}

		return false;
	}

	//--------------------------------------------------------------------------

	private String getLocalId(Dbms dbms, String uuid) throws Exception
	{
		String query = "SELECT id "+
							"FROM   Metadata "+
							"WHERE  uuid='"+uuid+"'";

		List idsList = dbms.select(query).getChildren();

		if (idsList.size() == 0)
			return null;
		else
		{
			Element record = (Element) idsList.get(0);

			return record.getChildText("id");
		}
	}

	//--------------------------------------------------------------------------

	private boolean updateCondition(String localDate, String remoteDate)
	{
		ISODate local = new ISODate(localDate);
		ISODate remote= new ISODate(remoteDate);

		//--- accept if remote date is greater than local date

		return (remote.sub(local) > 0);
	}
}

//=============================================================================
//===
//=== Store site information
//===
//=============================================================================

class SiteInfo
{
	//--- general

	public String name;
	public String host;
	public int    port;
	public String language;
	public String username;
	public String password;

	//--- services

	public String login;
	public String logout;
	public String search;
	public String get;

	//--- search

	public Vector queries = new Vector();

	//--- group

	public Vector groups = new Vector();
}

//=============================================================================

class Group
{
	public String  groupId;
	public HashSet privileges = new HashSet();
}

//=============================================================================
//===
//=== Create a mapping remote ID -> local ID / change date
//===
//=============================================================================

class UuidMapping
{
	private Hashtable htUuidDate = new Hashtable();
	private Hashtable htUuidId   = new Hashtable();

	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public UuidMapping(Dbms dbms, String siteId) throws Exception
	{
		String query = "SELECT id, uuid, changeDate "+
							"FROM   Metadata "+
							"WHERE  source='"+siteId+"'";

		List idsList = dbms.select(query).getChildren();

		for (int i=0; i<idsList.size(); i++)
		{
			Element record = (Element) idsList.get(i);

			String id   = record.getChildText("id");
			String uuid = record.getChildText("uuid");
			String date = record.getChildText("changedate");

			htUuidDate.put(uuid, date);
			htUuidId  .put(uuid, id);
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public String getChangeDate(String uuid) { return (String) htUuidDate.get(uuid); }

	//--------------------------------------------------------------------------

	public String getId(String uuid) { return (String) htUuidId.get(uuid); }

	//--------------------------------------------------------------------------

	public Iterator getUuids() { return htUuidDate.keySet().iterator(); }
}

//=============================================================================

