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

import jeeves.exceptions.BadInputEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.resources.Resources;
import org.jdom.Element;

import javax.servlet.ServletContext;
import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

//=============================================================================

public class GeonetHarvester extends AbstractHarvester
{
	public static final String TYPE = "geonetwork";

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

	public String getType() { return TYPE; }

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

	protected void doDestroy(Dbms dbms) throws SQLException
	{
        File icon = new File(Resources.locateLogosDir(context), params.uuid +".gif");

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
		params = new GeonetParams(dataMan);

		//--- retrieve/initialize information
		params.create(node);

		//--- force the creation of a new uuid
		params.uuid = UUID.randomUUID().toString();

		String id = settingMan.add(dbms, "harvesting", "node", getType(), false);

		storeNode(dbms, params, "id:"+id);
		Lib.sources.update(dbms, params.uuid, params.name, false);

		return id;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Update
	//---
	//---------------------------------------------------------------------------

	protected void doUpdate(Dbms dbms, String id, Element node) throws BadInputEx, SQLException
	{
		GeonetParams copy = params.copy();

		//--- update variables
		copy.update(node);

		String path = "harvesting/id:"+ id;

		settingMan.removeChildren(dbms, path);

		//--- update database
		storeNode(dbms, copy, path);

		//--- we update a copy first because if there is an exception GeonetParams
		//--- could be half updated and so it could be in an inconsistent state

		Lib.sources.update(dbms, copy.uuid, copy.name, false);

		params = copy;
	}

	//---------------------------------------------------------------------------

	protected void storeNodeExtra(Dbms dbms, AbstractParams p, String path,
											String siteId, String optionsId) throws SQLException
	{
		GeonetParams params = (GeonetParams) p;

		settingMan.add(dbms, "id:"+siteId, "host",    params.host, false);
		settingMan.add(dbms, "id:"+siteId, "createRemoteCategory", params.createRemoteCategory, false);
		settingMan.add(dbms, "id:"+siteId, "mefFormatFull", params.mefFormatFull, false);
		settingMan.add(dbms, "id:"+siteId, "xslfilter", params.xslfilter, false);

		//--- store search nodes

		for (Search s : params.getSearches())
		{
			String  searchID = settingMan.add(dbms, path, "search", "", false);

			settingMan.add(dbms, "id:"+searchID, "freeText",   s.freeText, false);
			settingMan.add(dbms, "id:"+searchID, "title",      s.title, false);
			settingMan.add(dbms, "id:"+searchID, "abstract",   s.abstrac, false);
			settingMan.add(dbms, "id:"+searchID, "keywords",   s.keywords, false);
			settingMan.add(dbms, "id:"+searchID, "digital",    s.digital, false);
			settingMan.add(dbms, "id:"+searchID, "hardcopy",   s.hardcopy, false);
			settingMan.add(dbms, "id:"+searchID, "sourceUuid", s.sourceUuid, false);
			settingMan.add(dbms, "id:"+searchID, "sourceName", s.sourceName, false);
		}

		//--- store group mapping

		for (Group g : params.getGroupCopyPolicy())
		{
			String  groupID = settingMan.add(dbms, path, "groupCopyPolicy", "", false);

			settingMan.add(dbms, "id:"+groupID, "name",   g.name, false);
			settingMan.add(dbms, "id:"+groupID, "policy", g.policy, false);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- addHarvestInfo
	//---
	//---------------------------------------------------------------------------

	public void addHarvestInfo(Element info, String id, String uuid)
	{
		super.addHarvestInfo(info, id, uuid);

		String small = context.getBaseUrl() +
							"/srv/en/resources.get?access=public&id="+id+"&fname=";

		String large = context.getBaseUrl() +
							"/srv/en/graphover.show?access=public&id="+id+"&fname=";

		info.addContent(new Element("smallThumbnail").setText(small));
		info.addContent(new Element("largeThumbnail").setText(large));
	}

	//---------------------------------------------------------------------------
	//---
	//--- AbstractParameters
	//---
	//---------------------------------------------------------------------------

	public AbstractParams getParams() { return params; }

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
		Element res  = getResult();
		info.addContent(res);
	}

	//---------------------------------------------------------------------------
	//---
	//--- GetResult
	//---
	//---------------------------------------------------------------------------

	protected Element getResult() {
		Element res  = new Element("result");

		if (result != null) {
			add(res, "total",         result.totalMetadata);
			add(res, "added",         result.addedMetadata);
			add(res, "updated",       result.updatedMetadata);
			add(res, "unchanged",     result.unchangedMetadata);
			add(res, "unknownSchema", result.unknownSchema);
			add(res, "removed",       result.locallyRemoved);
			add(res, "unretrievable", result.unretrievable);
            add(res, "doesNotValidate", result.doesNotValidate);
		}

		return res;
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
    public int doesNotValidate;
    
}

//=============================================================================

