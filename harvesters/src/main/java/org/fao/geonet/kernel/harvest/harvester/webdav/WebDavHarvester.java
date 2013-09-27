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
package org.fao.geonet.kernel.harvest.harvester.webdav;

import jeeves.exceptions.BadInputEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.resources.Resources;
import org.jdom.Element;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

//=============================================================================

public class WebDavHarvester extends AbstractHarvester<HarvestResult> {
	//--------------------------------------------------------------------------
	//---
	//--- Static init
	//---
	//--------------------------------------------------------------------------

	public static void init(ServiceContext context) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Harvesting type
	//---
	//--------------------------------------------------------------------------
	public String getType() { return "webdav"; }

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------
	protected void doInit(Element node) throws BadInputEx {
		params = new WebDavParams(dataMan);
        super.setParams(params);

        params.create(node);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Add
	//---
	//---------------------------------------------------------------------------
	protected String doAdd(Dbms dbms, Element node) throws BadInputEx, SQLException {
		params = new WebDavParams(dataMan);
        super.setParams(params);

        //--- retrieve/initialize information
		params.create(node);
		//--- force the creation of a new uuid
		params.uuid = UUID.randomUUID().toString();
		String id = settingMan.add(dbms, "harvesting", "node", getType());
		storeNode(dbms, params, "id:"+id);
		Lib.sources.update(dbms, params.uuid, params.name, true);
		Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + params.icon, params.uuid);
		return id;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Update
	//---
	//---------------------------------------------------------------------------
	protected void doUpdate(Dbms dbms, String id, Element node) throws BadInputEx, SQLException {
		WebDavParams copy = params.copy();
		//--- update variables
		copy.update(node);
		String path = "harvesting/id:"+ id;
		settingMan.removeChildren(dbms, path);
		//--- update database
		storeNode(dbms, copy, path);
		//--- we update a copy first because if there is an exception CswParams
		//--- could be half updated and so it could be in an inconsistent state
		Lib.sources.update(dbms, copy.uuid, copy.name, true);
		Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + copy.icon, copy.uuid);
		
		params = copy;
        super.setParams(params);

    }

	//---------------------------------------------------------------------------
	protected void storeNodeExtra(Dbms dbms, AbstractParams p, String path, String siteId, String optionsId) throws SQLException {
		WebDavParams params = (WebDavParams) p;
		settingMan.add(dbms, "id:"+siteId, "url",  params.url);
		settingMan.add(dbms, "id:"+siteId, "icon", params.icon);
		settingMan.add(dbms, "id:"+optionsId, "validate", params.validate);
		settingMan.add(dbms, "id:"+optionsId, "recurse",  params.recurse);
		settingMan.add(dbms, "id:"+optionsId, "subtype", params.subtype);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Harvest
	//---
	//---------------------------------------------------------------------------
	protected void doHarvest(Logger log, ResourceManager rm) throws Exception {
		log.info("WebDav doHarvest start");
		Dbms dbms = (Dbms) rm.open(Geonet.Res.MAIN_DB);
		h = new Harvester(log, context, dbms, params);
		result = h.harvest(log);
		log.info("WebDav doHarvest end");
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private WebDavParams params;
}