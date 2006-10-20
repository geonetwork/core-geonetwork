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

package org.fao.geonet.kernel.harvest.harvester.geonet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.resources.ResourceManager;
import org.fao.geonet.kernel.harvest.Common.Status;
import org.fao.geonet.kernel.harvest.Common.Type;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.jdom.Element;

//=============================================================================

public class GeonetHarvester extends AbstractHarvester
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	protected void doInit(Element node)
	{
		Element site     = node.getChild("site");
		Element opt      = node.getChild("options");
		Element searches = node.getChild("searches");
		Element account  = site.getChild("account");

		host    = site.getChildText("host");
		port    = site.getChildText("port");
		servlet = site.getChildText("servlet");

		useAccount = account.getChildText("use").equals("true");
		username   = account.getChildText("username");
		password   = account.getChildText("password");

		every        = opt.getChildText("every");
		createGroups = opt.getChildText("createGroups").equals("true");
		oneRunOnly   = opt.getChildText("oneRunOnly")  .equals("true");

		//--- add searches

		alSearches.clear();

		Iterator i = searches.getChildren("search").iterator();

		while (i.hasNext())
		{
			Element search = (Element) i.next();

			Search s = new Search();

			s.freeText = search.getChildText("freeText");
			s.title    = search.getChildText("title");
			s.abstrac  = search.getChildText("abstract");
			s.keywords = search.getChildText("keywords");
			s.digital  = search.getChildText("digital");
			s.hardcopy = search.getChildText("hardcopy");
			s.siteId   = search.getChildText("siteId");

			alSearches.add(s);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Add
	//---
	//---------------------------------------------------------------------------

	protected String doAdd(Dbms dbms, Element node) throws SQLException
	{
		Element site     = node.getChild("site");
		Element opt      = node.getChild("options");
		Element searches = node.getChild("searches");
		Element account  = (site == null) ? null : site.getChild("account");

		String id   = settingMan.add(dbms, "harvesting", "node", Type.GEONETWORK);
		String path = "id:"+ id;

		//--- retrieve information

		host    = getValue(site, "host",    "");
		port    = getValue(site, "port",    "80");
		servlet = getValue(site, "servlet", "geonetwork");

		useAccount = getValue(account, "use",      false);
		username   = getValue(account, "username", "");
		password   = getValue(account, "password", "");

		every        = getValue(opt, "every",        "90" );
		createGroups = getValue(opt, "createGroups", true );
		oneRunOnly   = getValue(opt, "oneRunOnly",   false);

		//--- setup geonetwork node

		String siteID    = settingMan.add(dbms, path, "site",    "");
		String optionsID = settingMan.add(dbms, path, "options", "");
		String infoID    = settingMan.add(dbms, path, "info",    "");

		//--- setup site node ----------------------------------------

		settingMan.add(dbms, "id:"+siteID, "name",    node.getAttributeValue("name"));
		settingMan.add(dbms, "id:"+siteID, "host",    host);
		settingMan.add(dbms, "id:"+siteID, "port",    port);
		settingMan.add(dbms, "id:"+siteID, "servlet", servlet);

		String useAccID = settingMan.add(dbms, "id:"+siteID, "useAccount", useAccount);

		settingMan.add(dbms, "id:"+useAccID, "username", username);
		settingMan.add(dbms, "id:"+useAccID, "password", password);

		//--- setup search nodes ---------------------------------------

		if (searches != null)
			addSearches(dbms, path, searches);

		//--- setup options node ---------------------------------------

		settingMan.add(dbms, "id:"+optionsID, "every",        every);
		settingMan.add(dbms, "id:"+optionsID, "createGroups", createGroups);
		settingMan.add(dbms, "id:"+optionsID, "oneRunOnly",   oneRunOnly);
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

	protected void doUpdate(Dbms dbms, String id, Element node) throws SQLException
	{
		Element site     = node.getChild("site");
		Element opt      = node.getChild("options");
		Element searches = node.getChild("searches");
		Element account  = (site == null) ? null : site.getChild("account");

		Map<String, Object> values = new HashMap<String, Object>();

		String path = "harvesting/id:"+ id;
		String name = node.getAttributeValue("name");

		//--- update variables

		host    = getValue(site, "host",    host);
		port    = getValue(site, "port",    port);
		servlet = getValue(site, "servlet", servlet);

		useAccount = getValue(account, "use",      useAccount);
		username   = getValue(account, "username", username);
		password   = getValue(account, "password", password);

		every        = getValue(opt, "every",        every);
		createGroups = getValue(opt, "createGroups", createGroups);
		oneRunOnly   = getValue(opt, "oneRunOnly",   oneRunOnly);

		//--- update database

		if (name != null)
			values.put(path +"/site/name", name);

		setValue(values, path +"/site/host",                site,    "host");
		setValue(values, path +"/site/port",                site,    "port");
		setValue(values, path +"/site/servlet",             site,    "servlet");

		setValue(values, path +"/site/useAccount",          account, "use");
		setValue(values, path +"/site/useAccount/username", account, "username");
		setValue(values, path +"/site/useAccount/password", account, "password");

		setValue(values, path +"/options/every",            opt, "every");
		setValue(values, path +"/options/createGroups",     opt, "createGroups");
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

			addSearches(dbms, path, searches);
		}
	}

	//---------------------------------------------------------------------------

	private void addSearches(Dbms dbms, String path, Element searches) throws SQLException
	{
		alSearches.clear();

		Iterator searchList = searches.getChildren("search").iterator();

		while (searchList.hasNext())
		{
			Element search   = (Element) searchList.next();
			String  searchID = settingMan.add(dbms, path, "search", "");

			Search s = new Search();

			s.freeText = getValue(search, "freeText", "");
			s.title    = getValue(search, "title",    "");
			s.abstrac  = getValue(search, "abstract", "");
			s.keywords = getValue(search, "keywords", "");
			s.digital  = getValue(search, "digital",  "false");
			s.hardcopy = getValue(search, "hardcopy", "false");
			s.siteId   = getValue(search, "siteId",   "");

			settingMan.add(dbms, "id:"+searchID, "freeText", s.freeText);
			settingMan.add(dbms, "id:"+searchID, "title",    s.title);
			settingMan.add(dbms, "id:"+searchID, "abstract", s.abstrac);
			settingMan.add(dbms, "id:"+searchID, "keywords", s.keywords);
			settingMan.add(dbms, "id:"+searchID, "digital",  s.digital);
			settingMan.add(dbms, "id:"+searchID, "hardcopy", s.hardcopy);
			settingMan.add(dbms, "id:"+searchID, "siteId",   s.siteId);

			alSearches.add(s);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- GetTimeout
	//---
	//---------------------------------------------------------------------------

	protected String doGetEvery() { return every; }

	protected boolean doIsOneRunOnly() { return oneRunOnly; }

	//---------------------------------------------------------------------------
	//---
	//--- AddInfo
	//---
	//---------------------------------------------------------------------------

	protected void doAddInfo(Element info) {}

	//---------------------------------------------------------------------------
	//---
	//--- Harvest
	//---
	//---------------------------------------------------------------------------

	protected void doHarvest(Logger l, ResourceManager rm) {}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private String  host;
	private String  port;
	private String  servlet;

	private boolean useAccount;
	private String  username;
	private String  password;

	private String  every;
	private boolean createGroups;
	private boolean oneRunOnly;

	private ArrayList<Search> alSearches = new ArrayList<Search>();

}

//=============================================================================

class Search
{
	public String freeText;
	public String title;
	public String abstrac;
	public String keywords;
	public String digital;
	public String hardcopy;
	public String siteId;
}

//=============================================================================

