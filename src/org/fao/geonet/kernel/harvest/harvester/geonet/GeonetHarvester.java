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

package org.fao.geonet.kernel.harvest.harvester.geonet;

import java.sql.SQLException;
import java.util.UUID;
import jeeves.exceptions.BadInputEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.jdom.Element;

//=============================================================================

public class GeonetHarvester extends AbstractHarvester
{
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

	public String getType() { return "geonetwork"; }

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	protected void doInit(Element node) throws BadInputEx
	{
		params = new GeonetParams(dataMan);
		params.create(node);
	}

	//---------------------------------------------------------------------------
	//---
	//--- doDestroy
	//---
	//---------------------------------------------------------------------------

	protected void doDestroy(Dbms dbms) {}

	//---------------------------------------------------------------------------
	//---
	//--- Add
	//---
	//---------------------------------------------------------------------------

	protected String doAdd(Dbms dbms, Element node) throws BadInputEx, SQLException
	{
		params = new GeonetParams(dataMan);

		//--- retrieve/initialize information
		params.create(node);

		//--- force the creation of a new uuid
		params.uuid = UUID.randomUUID().toString();

		String id = settingMan.add(dbms, "harvesting", "node", getType());

		storeNode(dbms, params, "id:"+id);

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

		//--- update variables
		copy.update(node);

		String path = "harvesting/id:"+ id;

		settingMan.removeChildren(dbms, path);

		//--- update database
		storeNode(dbms, copy, path);

		//--- we update a copy first because if there is an exception GeonetParams
		//--- could be half updated and so it could be in an inconsistent state

		params = copy;
	}

	//---------------------------------------------------------------------------

	protected void storeNodeExtra(Dbms dbms, AbstractParams p, String path,
											String siteId, String optionsId) throws SQLException
	{
		GeonetParams params = (GeonetParams) p;

		settingMan.add(dbms, "id:"+siteId, "host",    params.host);
		settingMan.add(dbms, "id:"+siteId, "port",    params.port);
		settingMan.add(dbms, "id:"+siteId, "servlet", params.servlet);

		//--- store search nodes

		for (Search s : params.getSearches())
		{
			String  searchID = settingMan.add(dbms, path, "search", "");

			settingMan.add(dbms, "id:"+searchID, "freeText",   s.freeText);
			settingMan.add(dbms, "id:"+searchID, "title",      s.title);
			settingMan.add(dbms, "id:"+searchID, "abstract",   s.abstrac);
			settingMan.add(dbms, "id:"+searchID, "keywords",   s.keywords);
			settingMan.add(dbms, "id:"+searchID, "digital",    s.digital);
			settingMan.add(dbms, "id:"+searchID, "hardcopy",   s.hardcopy);
			settingMan.add(dbms, "id:"+searchID, "sourceUuid", s.sourceUuid);
			settingMan.add(dbms, "id:"+searchID, "sourceName", s.sourceName);
		}

		//--- store group mapping

		for (Group g : params.getGroupCopyPolicy())
		{
			String  groupID = settingMan.add(dbms, path, "groupCopyPolicy", "");

			settingMan.add(dbms, "id:"+groupID, "name",   g.name);
			settingMan.add(dbms, "id:"+groupID, "policy", g.policy);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- AbstractParameters
	//---
	//---------------------------------------------------------------------------

	protected AbstractParams getParams() { return params; }

	//---------------------------------------------------------------------------
	//---
	//--- AddInfo
	//---
	//---------------------------------------------------------------------------

	protected void doAddInfo(Element node)
	{
		//--- if the harvesting is not started yet, we don't have any info

		if (result == null)
			return;

		//--- ok, add proper info

		Element info = node.getChild("info");
		Element res  = new Element("result");

		add(res, "total",         result.totalMetadata);
		add(res, "added",         result.addedMetadata);
		add(res, "updated",       result.updatedMetadata);
		add(res, "unchanged",     result.unchangedMetadata);
		add(res, "unknownSchema", result.unknownSchema);
		add(res, "removed",       result.locallyRemoved);
		add(res, "unretrievable", result.unretrievable);

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
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private GeonetParams params;
	private GeonetResult result;
}

//=============================================================================

class GeonetResult
{
	public int totalMetadata;
	public int addedMetadata;
	public int updatedMetadata;
	public int unchangedMetadata;
	public int locallyRemoved;
	public int unknownSchema;
	public int unretrievable;
}

//=============================================================================

