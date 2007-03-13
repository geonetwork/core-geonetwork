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

package org.fao.geonet.kernel.harvest.harvester.webfolder;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import jeeves.exceptions.BadInputEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;
import org.fao.geonet.kernel.harvest.Common.Status;
import org.fao.geonet.kernel.harvest.Common.Type;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.jdom.Element;

//=============================================================================

public class WAFHarvester extends AbstractHarvester
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
	//--- Init
	//---
	//--------------------------------------------------------------------------

	protected void doInit(Element node) throws BadInputEx
	{
		params.init(node);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Destroy
	//---
	//---------------------------------------------------------------------------

	protected void doDestroy(Dbms dbms) throws SQLException
	{
		String query = "DELETE FROM Metadata WHERE siteId = ?";

		//TODO:
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

		//--- setup waf node

		String id   = settingMan.add(dbms, "harvesting", "node", Type.WEB_FOLDER);
		String path = "id:"+ id;

		String siteID    = settingMan.add(dbms, path, "site",       "");
		String privID    = settingMan.add(dbms, path, "privileges", "");
		String optionsID = settingMan.add(dbms, path, "options",    "");
		String infoID    = settingMan.add(dbms, path, "info",       "");

		//--- setup site node

		settingMan.add(dbms, "id:"+siteID, "name", node.getAttributeValue("name"));
		settingMan.add(dbms, "id:"+siteID, "url",  params.url);

		String useAccID = settingMan.add(dbms, "id:"+siteID, "useAccount", params.useAccount);

		settingMan.add(dbms, "id:"+useAccID, "username", params.username);
		settingMan.add(dbms, "id:"+useAccID, "password", params.password);

		//--- setup privileges   ---------------------------------------

		addPrivileges(dbms, "id:"+ privID, params);

		//--- setup options node ---------------------------------------

		settingMan.add(dbms, "id:"+optionsID, "every",      params.every);
		settingMan.add(dbms, "id:"+optionsID, "oneRunOnly", params.oneRunOnly);
		settingMan.add(dbms, "id:"+optionsID, "validate",   params.validate);
		settingMan.add(dbms, "id:"+optionsID, "structure",  params.structure);
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
		//--- update variables

		WAFParams copy = params.copy();
		copy.update(node);

		//--- update database

		Element site   = node.getChild("site");
		Element opt    = node.getChild("options");
		Element privil = node.getChild("privileges");
		Element account= (site == null) ? null : site.getChild("account");

		String path = "harvesting/id:"+ id;
		String name = node.getAttributeValue("name");

		Map<String, Object> values = new HashMap<String, Object>();

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

			addPrivileges(dbms, path +"/id:"+ privID, copy);
		}

		//--- we update a copy first because if there is an exception GeonetParams
		//--- could be half updated and so it could be in an inconsistent state

		params = copy;
	}

	//---------------------------------------------------------------------------

	private void addPrivileges(Dbms dbms, String path, WAFParams params) throws SQLException
	{
		for (Privilege p : params.getPrivileges())
		{
			String groupID = settingMan.add(dbms, path, "group", p.groupID);

			for (int oper : p.getOperations())
				settingMan.add(dbms, "id:"+ groupID, "operation", oper);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- GetTimeout
	//---
	//---------------------------------------------------------------------------

	protected int doGetEvery() { return params.every; }

	protected boolean doIsOneRunOnly() { return params.oneRunOnly; }

	//---------------------------------------------------------------------------
	//---
	//--- AddInfo
	//---
	//---------------------------------------------------------------------------

	protected void doAddInfo(Element node) {}

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

	private WAFParams params = new WAFParams();
	private WAFResult result = null;
}

//=============================================================================

class WAFResult
{
}

//=============================================================================

