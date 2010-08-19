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

package org.fao.geonet.kernel.harvest.harvester.thredds;

import jeeves.exceptions.BadInputEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

//=============================================================================

public class ThreddsHarvester extends AbstractHarvester
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

	public String getType() { return "thredds"; }

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	protected void doInit(Element node) throws BadInputEx
	{
		params = new ThreddsParams(dataMan);
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
		params = new ThreddsParams(dataMan);

		//--- retrieve/initialize information
		params.create(node);

		//--- force the creation of a new uuid
		params.uuid = UUID.randomUUID().toString();

		String id = settingMan.add(dbms, "harvesting", "node", getType());

		storeNode(dbms, params, "id:"+id);
		Lib.sources.update(dbms, params.uuid, params.name, true);
		Lib.sources.copyLogo(context, "/images/harvesting/"+ params.icon, params.uuid);

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
		ThreddsParams copy = params.copy();

		//--- update variables
		copy.update(node);

		String path = "harvesting/id:"+ id;

		settingMan.removeChildren(dbms, path);

		//--- update database
		storeNode(dbms, copy, path);

		//--- we update a copy first because if there is an exception Params
		//--- could be half updated and so it could be in an inconsistent state

		Lib.sources.update(dbms, copy.uuid, copy.name, true);
		Lib.sources.copyLogo(context, "/images/harvesting/"+ copy.icon, copy.uuid);

		params = copy;
	}

	//---------------------------------------------------------------------------

	protected void storeNodeExtra(Dbms dbms, AbstractParams p, String path,
											String siteId, String optionsId) throws SQLException
	{
		ThreddsParams params = (ThreddsParams) p;

		settingMan.add(dbms, "id:"+siteId, "url",  params.url);
		settingMan.add(dbms, "id:"+siteId, "icon", params.icon);
		settingMan.add(dbms, "id:"+optionsId, "lang",  params.lang);
		settingMan.add(dbms, "id:"+optionsId, "topic",  params.topic);
		settingMan.add(dbms, "id:"+optionsId, "createThumbnails",  params.createThumbnails);
		settingMan.add(dbms, "id:"+optionsId, "createServiceMd", params.createServiceMd);
		settingMan.add(dbms, "id:"+optionsId, "createCollectionDatasetMd",  params.createCollectionDatasetMd);
		settingMan.add(dbms, "id:"+optionsId, "createAtomicDatasetMd",  params.createAtomicDatasetMd);
		settingMan.add(dbms, "id:"+optionsId, "ignoreHarvestOnCollections",  params.ignoreHarvestOnCollections);
		settingMan.add(dbms, "id:"+optionsId, "collectionGeneration",  params.collectionMetadataGeneration);
		settingMan.add(dbms, "id:"+optionsId, "collectionFragmentStylesheet",  params.collectionFragmentStylesheet);
		settingMan.add(dbms, "id:"+optionsId, "collectionMetadataTemplate",  params.collectionMetadataTemplate);
		settingMan.add(dbms, "id:"+optionsId, "createCollectionSubtemplates",  params.createCollectionSubtemplates);
		settingMan.add(dbms, "id:"+optionsId, "outputSchemaOnCollections",  params.outputSchemaOnCollections);
		settingMan.add(dbms, "id:"+optionsId, "ignoreHarvestOnAtomics",  params.ignoreHarvestOnAtomics);
		settingMan.add(dbms, "id:"+optionsId, "atomicGeneration",  params.atomicMetadataGeneration);
		settingMan.add(dbms, "id:"+optionsId, "atomicFragmentStylesheet",  params.atomicFragmentStylesheet);
		settingMan.add(dbms, "id:"+optionsId, "atomicMetadataTemplate",  params.atomicMetadataTemplate);
		settingMan.add(dbms, "id:"+optionsId, "createAtomicSubtemplates",  params.createAtomicSubtemplates);
		settingMan.add(dbms, "id:"+optionsId, "outputSchemaOnAtomics",  params.outputSchemaOnAtomics);
		settingMan.add(dbms, "id:"+optionsId, "createAtomicDatasetMd",  params.createAtomicDatasetMd);
		settingMan.add(dbms, "id:"+optionsId, "datasetCategory",  params.datasetCategory);
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
		Element res  = new Element("result");

		add(res, "total",          		result.total);
		add(res, "serviceRecords",       					result.serviceRecords);
		add(res, "subtemplatesRemoved",		result.subtemplatesRemoved);
		add(res, "fragmentsReturned",		result.fragmentsReturned);
		add(res, "fragmentsUnknownSchema",	result.fragmentsUnknownSchema);
		add(res, "subtemplatesAdded",		result.subtemplatesAdded);
		add(res, "fragmentsMatched",		result.fragmentsMatched);
		add(res, "collectionDatasetRecords",     	result.collectionDatasetRecords);
		add(res, "atomicDatasetRecords",      		result.atomicDatasetRecords);
		add(res, "datasetUuidExist",	result.datasetUuidExist);
		add(res, "unknownSchema",  		result.unknownSchema);
		add(res, "removed",        		result.locallyRemoved);
		add(res, "unretrievable",  		result.unretrievable);
		add(res, "badFormat",      		result.badFormat);
		add(res, "doesNotValidate",		result.doesNotValidate);
		add(res, "thumbnails",        result.thumbnails);
		add(res, "thumbnailsFailed",  result.thumbnailsFailed);
		
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

	private ThreddsParams params;
	private ThreddsResult result;
}

//=============================================================================

class ThreddsResult
{
	public int total;			// = md for datasets and service
	public int serviceRecords;			// = md for services
	public int subtemplatesRemoved;	// = fragments generated
	public int fragmentsReturned;	// = fragments generated
	public int fragmentsUnknownSchema;	// = fragments with unknown schema
	public int subtemplatesAdded;		// = subtemplates for collection datasets
	public int fragmentsMatched;	// = fragments matched in md templates
	public int collectionDatasetRecords;	// = md for collection datasets
	public int atomicDatasetRecords;		// = md for atomic datasets
	public int datasetUuidExist;	// = uuid already in catalogue
	public int locallyRemoved;	// = md removed
	public int unknownSchema;	// = md with unknown schema (should be 0 if no dataset loaded using md url)
	public int unretrievable;	// = http connection failed
	public int badFormat;		// 
	public int doesNotValidate;	// = 0 cos' not validated
	public int thumbnails;    // = number of thumbnail generated
	public int thumbnailsFailed;// = number of thumbnail creation which failed
}

//=============================================================================

