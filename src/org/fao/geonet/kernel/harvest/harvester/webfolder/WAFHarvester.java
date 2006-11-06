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

package org.fao.geonet.kernel.harvest.harvester.webfolder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import jeeves.exceptions.BadInputEx;
import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.MissingParameterEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.resources.ResourceManager;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.harvest.Common.Status;
import org.fao.geonet.kernel.harvest.Common.Type;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.jdom.Element;

//=============================================================================

public class WAFHarvester extends AbstractHarvester
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	protected void doInit(Element node) throws BadInputEx
	{
		Element site   = node.getChild("site");
		Element opt    = node.getChild("options");
		Element privil = node.getChild("privileges");
		Element account= site.getChild("account");

		url = site.getChildText("url");

		useAccount = account.getChildText("use").equals("true");
		username   = account.getChildText("username");
		password   = account.getChildText("password");

		every      = opt.getChildText("every");
		oneRunOnly = opt.getChildText("oneRunOnly").equals("true");
		validate   = opt.getChildText("validate")  .equals("true");
		structure  = opt.getChildText("structure") .equals("true");

		//--- add privileges

		alPrivileges.clear();

		Iterator i = privil.getChildren("group").iterator();

		while (i.hasNext())
		{
			Element group   = (Element) i.next();
			String  groupID = group.getAttributeValue("id");

			Privilege p = new Privilege();
			p.groupId = groupID;

			Iterator operList = group.getChildren("operation").iterator();

			while (operList.hasNext())
			{
				Element oper = (Element) operList.next();

				p.operations.add(getOperationID(oper));
			}

			alPrivileges.add(p);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Add
	//---
	//---------------------------------------------------------------------------

	protected String doAdd(Dbms dbms, Element node) throws BadInputEx, SQLException
	{
		Element site   = node.getChild("site");
		Element opt    = node.getChild("options");
		Element privil = node.getChild("privileges");
		Element account= (site == null) ? null : site.getChild("account");

		String id   = settingMan.add(dbms, "harvesting", "node", Type.WEB_FOLDER);
		String path = "id:"+ id;

		//--- retrieve information

		url        = getValue(site,    "url",      "");
		useAccount = getValue(account, "use",      false);
		username   = getValue(account, "username", "");
		password   = getValue(account, "password", "");

		every      = getValue(opt, "every",      "90");
		oneRunOnly = getValue(opt, "oneRunOnly", false);
		validate   = getValue(opt, "validate",   false);
		structure  = getValue(opt, "structure",  false);

		//--- setup waf node

		String siteID    = settingMan.add(dbms, path, "site",       "");
		String privID    = settingMan.add(dbms, path, "privileges", "");
		String optionsID = settingMan.add(dbms, path, "options",    "");
		String infoID    = settingMan.add(dbms, path, "info",       "");

		//--- setup site node

		settingMan.add(dbms, "id:"+siteID, "name", node.getAttributeValue("name"));
		settingMan.add(dbms, "id:"+siteID, "url",  url);

		String useAccID = settingMan.add(dbms, "id:"+siteID, "useAccount", useAccount);

		settingMan.add(dbms, "id:"+useAccID, "username", username);
		settingMan.add(dbms, "id:"+useAccID, "password", password);

		//--- setup privileges   ---------------------------------------

		if (privil != null)
			addPrivileges(dbms, "id:"+ privID, privil);

		//--- setup options node ---------------------------------------

		settingMan.add(dbms, "id:"+optionsID, "every",      every);
		settingMan.add(dbms, "id:"+optionsID, "oneRunOnly", oneRunOnly);
		settingMan.add(dbms, "id:"+optionsID, "validate",   validate);
		settingMan.add(dbms, "id:"+optionsID, "structure",  structure);
		settingMan.add(dbms, "id:"+optionsID, "status",     Status.INACTIVE);

		//--- setup stats node ----------------------------------------

		settingMan.add(dbms, "id:"+infoID, "lastRun", "");

		return id;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Update
	//---
	//---------------------------------------------------------------------------

	protected void doUpdate(Dbms dbms, String id, Element node)
									throws BadInputEx, SQLException
	{
		Element site   = node.getChild("site");
		Element opt    = node.getChild("options");
		Element privil = node.getChild("privileges");
		Element account= (site == null) ? null : site.getChild("account");

		Map<String, Object> values = new HashMap<String, Object>();

		String path = "harvesting/id:"+ id;
		String name = node.getAttributeValue("name");

		//--- update variables

		url        = getValue(site,    "url",      url);
		useAccount = getValue(account, "use",      useAccount);
		username   = getValue(account, "username", username);
		password   = getValue(account, "password", password);

		every      = getValue(opt, "every",      every);
		oneRunOnly = getValue(opt, "oneRunOnly", oneRunOnly);
		validate   = getValue(opt, "validate",   validate);
		structure  = getValue(opt, "structure",  structure);

		//--- update database

		if (name != null)
			values.put(path +"/site/name", name);

		setValue(values, path +"/site/url",                 site,    "url");
		setValue(values, path +"/site/useAccount",          account, "use");
		setValue(values, path +"/site/useAccount/username", account, "username");
		setValue(values, path +"/site/useAccount/password", account, "password");

		setValue(values, path +"/options/every",            opt, "every");
		setValue(values, path +"/options/oneRunOnly",       opt, "oneRunOnly");
		setValue(values, path +"/options/validate",         opt, "validate");
		setValue(values, path +"/options/structure",        opt, "structure");

		settingMan.setValues(dbms, values);

		//--- update privileges if the 'privileges' element is provided

		if (privil != null)
		{
			//--- remove all previous privileges

			Element setPrivil = settingMan.get(path ,1).getChild("children").getChild("privileges");
			String  privID    = setPrivil.getAttributeValue("id");

			settingMan.removeChildren(dbms, path +"/id:"+ privID);

			//--- add new privileges entries

			addPrivileges(dbms, path +"/id:"+ privID, privil);
		}
	}

	//---------------------------------------------------------------------------

	private void addPrivileges(Dbms dbms, String path, Element privil)
										throws BadInputEx, SQLException
	{
		alPrivileges.clear();

		Iterator groupList = privil.getChildren("group").iterator();

		while (groupList.hasNext())
		{
			Element group   = (Element) groupList.next();
			String  groupID = group.getAttributeValue("id");

			if (groupID == null)
				throw new MissingParameterEx("attribute:id", group);

			Privilege p = new Privilege();
			p.groupId = groupID;

			groupID = settingMan.add(dbms, path, "group", groupID);

			Iterator operList = group.getChildren("operation").iterator();

			while (operList.hasNext())
			{
				Element oper = (Element) operList.next();
				int     op   = getOperationID(oper);

				p.operations.add(op);
				settingMan.add(dbms, "id:"+ groupID, "operation", op);
			}

			alPrivileges.add(p);
		}
	}

	//---------------------------------------------------------------------------

	private int getOperationID(Element oper) throws BadInputEx
	{
		String operName = oper.getAttributeValue("name");

		if (operName == null)
			throw new MissingParameterEx("attribute:name", oper);

		int operID = AccessManager.getPrivilegeId(operName);

		if (operID == -1)
			throw new BadParameterEx("attribute:name", operName);

		if (operID == 2 || operID == 4)
			throw new BadParameterEx("attribute:name", operName);

		return operID;
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

	private String  url;

	private boolean useAccount;
	private String  username;
	private String  password;

	private String  every;
	private boolean oneRunOnly;
	private boolean validate;
	private boolean structure;

	private ArrayList<Privilege> alPrivileges = new ArrayList<Privilege>();
}

//=============================================================================

class Privilege
{
	public String groupId;

	public ArrayList<Integer> operations = new ArrayList<Integer>();
}

//=============================================================================

