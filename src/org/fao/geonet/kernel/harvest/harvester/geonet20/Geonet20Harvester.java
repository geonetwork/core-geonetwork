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

package org.fao.geonet.kernel.harvest.harvester.geonet20;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import jeeves.exceptions.BadInputEx;
import jeeves.exceptions.BadServerResponseEx;
import jeeves.exceptions.UserNotFoundEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Xml;
import jeeves.utils.XmlRequest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.Common.Status;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;

//=============================================================================

public class Geonet20Harvester extends AbstractHarvester
{
	//--------------------------------------------------------------------------
	//---
	//--- Static init
	//---
	//--------------------------------------------------------------------------

	public static void init(ServiceContext context) throws Exception
	{
	}

	//--------------------------------------------------------------------------
	//---
	//--- Harvesting type
	//---
	//--------------------------------------------------------------------------

	public String getType() { return "geonetwork20"; }

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	protected void doInit(Element node)
	{
		params.init(node);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Destroy
	//---
	//---------------------------------------------------------------------------

	protected void doDestroy(Dbms dbms) throws Exception
	{
		String getQuery = "SELECT id FROM Metadata WHERE source = ?";

		for (Search s : params.getSearches())
		{
			for (Object o : dbms.select(getQuery, s.siteId).getChildren())
			{
				Element el = (Element) o;
				String  id = (String)  el.getChildText("id");

				dataMan.deleteMetadata(dbms, id);
				dbms.commit();
			}
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Add
	//---
	//---------------------------------------------------------------------------

	protected String doAdd(Dbms dbms, Element node) throws BadInputEx, SQLException
	{
		//--- retrieve/initialize information

		params.create(node);

		//--- setup geonetwork node

		String id   = settingMan.add(dbms, "harvesting", "node", getType());
		String path = "id:"+ id;

		String siteID    = settingMan.add(dbms, path, "site",    "");
		String optionsID = settingMan.add(dbms, path, "options", "");
		String infoID    = settingMan.add(dbms, path, "info",    "");

		//--- setup site node ----------------------------------------

		settingMan.add(dbms, "id:"+siteID, "name",    node.getAttributeValue("name"));
		settingMan.add(dbms, "id:"+siteID, "host",    params.host);
		settingMan.add(dbms, "id:"+siteID, "port",    params.port);
		settingMan.add(dbms, "id:"+siteID, "servlet", params.servlet);

		String useAccID = settingMan.add(dbms, "id:"+siteID, "useAccount", params.useAccount);

		settingMan.add(dbms, "id:"+useAccID, "username", params.username);
		settingMan.add(dbms, "id:"+useAccID, "password", params.password);

		//--- setup search nodes ---------------------------------------

		addSearches(dbms, path, params);

		//--- setup options node ---------------------------------------

		settingMan.add(dbms, "id:"+optionsID, "every",        params.every);
		settingMan.add(dbms, "id:"+optionsID, "oneRunOnly",   params.oneRunOnly);
		settingMan.add(dbms, "id:"+optionsID, "status",       Status.INACTIVE);

		//--- setup stats node ----------------------------------------

		settingMan.add(dbms, "id:"+infoID, "lastRun", "");

		return id;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Update
	//---
	//---------------------------------------------------------------------------

	protected void doUpdate(Dbms dbms, String id, Element node) throws BadInputEx, SQLException
	{
		//--- update variables

		GeonetParams copy = params.copy();
		copy.update(node);

		//--- update database

		Element site     = node.getChild("site");
		Element opt      = node.getChild("options");
		Element searches = node.getChild("searches");
		Element account  = (site == null) ? null : site.getChild("account");

		String path = "harvesting/id:"+ id;
		String name = node.getAttributeValue("name");

		Map<String, Object> values = new HashMap<String, Object>();

		if (name != null)
			values.put(path +"/site/name", name);

		setValue(values, path +"/site/host",                site,    "host");
		setValue(values, path +"/site/port",                site,    "port");
		setValue(values, path +"/site/servlet",             site,    "servlet");

		setValue(values, path +"/site/useAccount",          account, "use");
		setValue(values, path +"/site/useAccount/username", account, "username");
		setValue(values, path +"/site/useAccount/password", account, "password");

		setValue(values, path +"/options/every",            opt, "every");
		setValue(values, path +"/options/oneRunOnly",       opt, "oneRunOnly");

		settingMan.setValues(dbms, values);

		//--- update the search entry if some 'search' elements are provided

		if (searches != null)
		{
			//--- remove all search entries

			Iterator oldSearches = settingMan.get(path ,1).getChild("children").getChildren("search").iterator();

			while (oldSearches.hasNext())
			{
				Element search   = (Element) oldSearches.next();
				String  searchId = search.getAttributeValue("id");

				settingMan.remove(dbms, path +"/id:"+ searchId);
			}

			//--- add new search entries

			addSearches(dbms, path, copy);
		}

		//--- we update a copy first because if there is an exception GeonetParams
		//--- could be half updated and so it could be in an inconsistent state

		params = copy;
	}

	//---------------------------------------------------------------------------

	private void addSearches(Dbms dbms, String path, GeonetParams params) throws SQLException
	{
		for (Search s : params.getSearches())
		{
			String  searchID = settingMan.add(dbms, path, "search", "");

			settingMan.add(dbms, "id:"+searchID, "freeText", s.freeText);
			settingMan.add(dbms, "id:"+searchID, "title",    s.title);
			settingMan.add(dbms, "id:"+searchID, "abstract", s.abstrac);
			settingMan.add(dbms, "id:"+searchID, "keywords", s.keywords);
			settingMan.add(dbms, "id:"+searchID, "digital",  s.digital);
			settingMan.add(dbms, "id:"+searchID, "hardcopy", s.hardcopy);
			settingMan.add(dbms, "id:"+searchID, "siteId",   s.siteId);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- GetTimeout
	//---
	//---------------------------------------------------------------------------

	protected int doGetEvery() { return params.every; }

	//---------------------------------------------------------------------------

	protected boolean doIsOneRunOnly() { return params.oneRunOnly; }

	//---------------------------------------------------------------------------
	//---
	//--- AddInfo
	//---
	//---------------------------------------------------------------------------

	protected void doAddInfo(Element node)
	{
		Element info     = node.getChild("info");
		Element searches = node.getChild("searches");

		//--- if the harvesting is not started yet, we don't have any info

		if (result == null)
			return;

		//--- ok, add proper info

		for (AlignerResult ar : result.alResult)
		{
			Element site = new Element("search");
			site.setAttribute("siteId", ar.siteId);

			add(site, "total",     ar.totalMetadata);
			add(site, "added",     ar.addedMetadata);
			add(site, "updated",   ar.updatedMetadata);
			add(site, "unchanged", ar.unchangedMetadata);
			add(site, "skipped",   ar.schemaSkipped+ ar.uuidSkipped);
			add(site, "removed",   ar.locallyRemoved);

			info.addContent(site);
		}
	}

	//---------------------------------------------------------------------------

	private void add(Element el, String name, int value)
	{
		el.addContent(new Element(name).setText(Integer.toString(value)));
	}

	//---------------------------------------------------------------------------
	//---
	//--- Harvest
	//---
	//---------------------------------------------------------------------------

	protected void doHarvest(Logger log, ResourceManager rm) throws Exception
	{
		Dbms dbms = (Dbms) rm.open(Geonet.Res.MAIN_DB);

		CategoryMapper localCateg = new CategoryMapper(dbms);
		GroupMapper    localGroups= new GroupMapper(dbms);

		XmlRequest req = new XmlRequest(params.host, params.port);

		Lib.net.setupProxy(context, req);

		//--- login

		if (params.useAccount)
		{
			log.info("Login into : "+ getName());

			req.setAddress("/"+ params.servlet +"/srv/en/"+ Geonet.Service.XML_LOGIN);
			req.addParam("username", params.username);
			req.addParam("password", params.password);

			Element response = req.execute();

			if (!response.getName().equals("ok"))
				throw new UserNotFoundEx(params.username);
		}

		//--- search

		result = new GeonetResult();

		Aligner aligner = new Aligner(log, req, params, dataMan, dbms, context,
												localCateg, localGroups);

		for(Search s : params.getSearches())
		{
			log.info("Searching on : "+ getName() +"/"+ s.siteId);

			req.setAddress("/"+ params.servlet +"/srv/en/"+ Geonet.Service.XML_SEARCH);

			Element searchResult = req.execute(s.createRequest());

			log.debug("Obtained:\n"+Xml.getString(searchResult));

			//--- site alignment
			AlignerResult ar = aligner.align(searchResult, s.siteId);

			//--- collect some stats
			result.alResult.add(ar);
		}

		//--- logout

		if (params.useAccount)
		{
			log.info("Logout from : "+ getName());

			req.clearParams();
			req.setAddress("/"+ params.servlet +"/srv/en/"+ Geonet.Service.XML_LOGOUT);
		}

		dbms.commit();
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private GeonetParams params = new GeonetParams();
	private GeonetResult result = null;
}

//=============================================================================

class GeonetResult
{
	public ArrayList<AlignerResult> alResult = new ArrayList<AlignerResult>();
}

//=============================================================================

