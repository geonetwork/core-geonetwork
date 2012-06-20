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

package org.fao.geonet.kernel.harvest.harvester.cgp;

import jeeves.exceptions.BadInputEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.kernel.harvest.harvester.ErrorTracker;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.resources.Resources;
import org.jdom.Element;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

//=============================================================================

public class CGPHarvester extends AbstractHarvester
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

	public String getType()
	{
		return "cgp";
	}

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	protected void doInit(Element node) throws BadInputEx
	{
		params = new CGPParams(dataMan);
		params.create(node);
	}

	//---------------------------------------------------------------------------
	//---
	//--- doDestroy
	//---
	//---------------------------------------------------------------------------

	protected void doDestroy(Dbms dbms) throws SQLException
	{
		File icon = new File(context.getAppPath() + "images/logos", params.uuid + ".gif");

		icon.delete();
		Lib.sources.delete(dbms, params.uuid);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Add
	//---
	//---------------------------------------------------------------------------

	protected String doAdd(Dbms dbms, Element node) throws BadInputEx, SQLException
	{
		params = new CGPParams(dataMan);

		//--- retrieve/initialize information
		params.create(node);

		//--- force the creation of a new uuid
		params.uuid = UUID.randomUUID().toString();

		String id = settingMan.add(dbms, "harvesting", "node", getType());

		storeNode(dbms, params, "id:" + id);
		Lib.sources.update(dbms, params.uuid, params.name, true);
		Resources.copyLogo(context, "harvesting/" + params.icon, params.uuid);

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
		CGPParams copy = params.copy();

		//--- update variables
		copy.update(node);

		String path = "harvesting/id:" + id;

		settingMan.removeChildren(dbms, path);

		//--- update database
		storeNode(dbms, copy, path);

		//--- we update a copy first because if there is an exception Params
		//--- could be half updated and so it could be in an inconsistent state

		Lib.sources.update(dbms, copy.uuid, copy.name, true);
		Resources.copyLogo(context, "harvesting/" + copy.icon, copy.uuid);

		params = copy;
	}

	//---------------------------------------------------------------------------

	protected void storeNodeExtra(Dbms dbms, AbstractParams p, String path,
	                              String siteId, String optionsId) throws SQLException
	{
		CGPParams params = (CGPParams) p;

		settingMan.add(dbms, "id:" + siteId, "url", params.url);
		settingMan.add(dbms, "id:" + siteId, "icon", params.icon);
		settingMan.add(dbms, "id:" + optionsId, "validate", params.validate);

		//--- store search nodes

		for (Search s : params.getSearches())
		{
			String searchID = settingMan.add(dbms, path, "search", "");

			settingMan.add(dbms, "id:" + searchID, "freeText", s.freeText);
			settingMan.add(dbms, "id:" + searchID, "from", s.from);
			settingMan.add(dbms, "id:" + searchID, "until", s.until);
			settingMan.add(dbms, "id:" + searchID, "latNorth", s.latNorth);
			settingMan.add(dbms, "id:" + searchID, "latSouth", s.latSouth);
			settingMan.add(dbms, "id:" + searchID, "lonEast", s.lonEast);
			settingMan.add(dbms, "id:" + searchID, "lonWest", s.lonWest);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- AbstractParameters
	//---
	//---------------------------------------------------------------------------

	public AbstractParams getParams()
	{
		return params;
	}

	//---------------------------------------------------------------------------
	//---
	//--- AddInfo
	//---
	//---------------------------------------------------------------------------

	protected void doAddInfo(Element node)
	{
		//--- if the harvesting is not started yet, we don't have any info

		if (result == null)
		{
			return;
		}

		//--- ok, add proper info

		Element info = node.getChild("info");
		Element res = new Element("result");

		add(res, "total",          result.totalMetadata);
		add(res, "added",          result.addedMetadata);
//		add(res, "updated",        result.updatedMetadata);
//		add(res, "unchanged",      result.unchangedMetadata);
		add(res, "unknownSchema",  result.unknownSchema);
		add(res, "removed",        result.locallyRemoved);
		add(res, "unretrievable",  result.unretrievable);
		add(res, "databaseError",  result.databaseError);
		add(res, "doesNotValidate",result.doesNotValidate);
//		add(res, "thumbnails", result.thumbnails);
//		add(res, "thumbnailsFailed", result.thumbnailsFailed);

		info.addContent(res);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Harvest
	//---
	//---------------------------------------------------------------------------

	protected void doHarvest(Logger log, ResourceManager rm) throws Exception
	{
		Dbms dbms = (Dbms) rm.open(Geonet.Res.MAIN_DB);

		Harvester h = new Harvester(log, context, dbms, params);
		result = h.harvest();
	}

	//---------------------------------------------------------------------------
	//---
	//--- GetResult
	//---
	//---------------------------------------------------------------------------
	protected Element getResult() {
		Element res  = new Element("result");
		if (result != null) {
			add(res, "total",          result.totalMetadata);
			add(res, "added",          result.addedMetadata);
//			add(res, "updated",        result.updatedMetadata);
//			add(res, "unchanged",      result.unchangedMetadata);
			add(res, "unknownSchema",  result.unknownSchema);
			add(res, "removed",        result.locallyRemoved);
			add(res, "unretrievable",  result.unretrievable);
			add(res, "databaseError",  result.databaseError);
			add(res, "doesNotValidate",result.doesNotValidate);
		}
		return res;
	}


	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private CGPParams params;
	private CGPResult result;
}

//=============================================================================

class CGPResult implements ErrorTracker
{
	public int totalMetadata;			// = md for data and service (ie. data + 1)
	public int addedMetadata;			// = total
//	public int updatedMetadata;
//	public int unchangedMetadata;
	public int locallyRemoved;	// = md removed
	public int unknownSchema;	// = md with unknown schema
	public int unretrievable;	// = http connection failed
	public int databaseError;		//
	public int doesNotValidate;	// = 0 cos' not validated
    public void incrementError() {
       databaseError++;
    }
}

//=============================================================================

