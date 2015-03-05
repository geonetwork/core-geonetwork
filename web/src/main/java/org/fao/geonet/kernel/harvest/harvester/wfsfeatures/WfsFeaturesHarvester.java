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

package org.fao.geonet.kernel.harvest.harvester.wfsfeatures;

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

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

//=============================================================================

public class WfsFeaturesHarvester extends AbstractHarvester
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

	public String getType() { return "wfsfeatures"; }

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	protected void doInit(Element node) throws BadInputEx
	{
		params = new WfsFeaturesParams(dataMan);
		params.create(node);
	}

	//---------------------------------------------------------------------------
	//---
	//--- doDestroy
	//---
	//---------------------------------------------------------------------------

	protected void doDestroy(Dbms dbms) throws SQLException
	{
		File icon = new File(context.getAppPath() +"images/logos", params.uuid +".gif");
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
		params = new WfsFeaturesParams(dataMan);

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

	protected void doUpdate(Dbms dbms, String id, Element node)
									throws BadInputEx, SQLException
	{
		WfsFeaturesParams copy = params.copy();

		//--- update variables
		copy.update(node);

		String path = "harvesting/id:"+ id;

		settingMan.removeChildren(dbms, path);

		//--- update database
		storeNode(dbms, copy, path);

		//--- we update a copy first because if there is an exception Params
		//--- could be half updated and so it could be in an inconsistent state

		Lib.sources.update(dbms, copy.uuid, copy.name, true);
		Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + params.icon, params.uuid);

		params = copy;
	}

	//---------------------------------------------------------------------------

	protected void storeNodeExtra(Dbms dbms, AbstractParams p, String path,
											String siteId, String optionsId) throws SQLException
	{
		WfsFeaturesParams params = (WfsFeaturesParams) p;

		settingMan.add(dbms, "id:"+siteId, "url",  params.url);
		settingMan.add(dbms, "id:"+siteId, "icon", params.icon);
		settingMan.add(dbms, "id:"+optionsId, "lang",  params.lang);
		settingMan.add(dbms, "id:"+optionsId, "query",  params.query);
		settingMan.add(dbms, "id:"+optionsId, "outputSchema",  params.outputSchema);
		settingMan.add(dbms, "id:"+optionsId, "stylesheet",  params.stylesheet);
		settingMan.add(dbms, "id:"+optionsId, "streamFeatures",  params.streamFeatures);
		settingMan.add(dbms, "id:"+optionsId, "createSubtemplates",  params.createSubtemplates);
		settingMan.add(dbms, "id:"+optionsId, "templateId",  params.templateId);
		settingMan.add(dbms, "id:"+optionsId, "recordsCategory",  params.recordsCategory);
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

	protected Element getResult()
	{
		Element res  = new Element("result");
		if (result != null) {
			add(res, "total",          					result.total);
			add(res, "subtemplatesAdded",       result.subtemplatesAdded);
			add(res, "subtemplatesUpdated",     result.subtemplatesUpdated);
			add(res, "subtemplatesRemoved",  		result.subtemplatesRemoved);
			add(res, "fragmentsUnknownSchema", 	result.fragmentsUnknownSchema);
			add(res, "fragmentsReturned",				result.fragmentsReturned);
			add(res, "fragmentsMatched",				result.fragmentsMatched);
			add(res, "recordsBuilt",						result.recordsBuilt);
			add(res, "recordsUpdated",					result.recordsUpdated);
			add(res, "doesNotValidate",					result.doesNotValidate);
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
		Dbms dbms = null;
		
		try {
			dbms = (Dbms) rm.openDirect(Geonet.Res.MAIN_DB);

			Harvester h = new Harvester(log, context, dbms, params);
			result = h.harvest();
		} finally {
			if (dbms != null) rm.close(Geonet.Res.MAIN_DB, dbms);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private WfsFeaturesParams params;
	private WfsFeaturesResult result;
}

//=============================================================================

class WfsFeaturesResult
{
	public int total;										// = recordsBuilt + subtemplatesAdded 
	public int subtemplatesAdded;				// = fragments added to database
	public int subtemplatesRemoved;			// = fragments removed to database
	public int subtemplatesUpdated;			// = fragments updated in database
	public int fragmentsReturned;				// = fragments returned
	public int fragmentsMatched;				// = fragments matched to template
	public int fragmentsUnknownSchema;	// = fragments with unknown schema
	public int recordsBuilt;				// = records built from template
	public int recordsUpdated;		// = records built from template and updated
	public int doesNotValidate;			// = completed records that didn't validate
	public int recordsRemoved;		// = records removed
}

//=============================================================================

