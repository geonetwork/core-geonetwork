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

import java.io.File;
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
import org.fao.geonet.lib.Lib;
import org.fao.geonet.resources.Resources;
import org.jdom.Element;

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
		params = new ThreddsParams(dataMan);

		//--- retrieve/initialize information
		params.create(node);

		//--- force the creation of a new uuid
		params.uuid = UUID.randomUUID().toString();

		String id = settingMan.add(dbms, "harvesting", "node", getType(), false);

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
		Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + copy.icon, copy.uuid);
		
		params = copy;
	}

	//---------------------------------------------------------------------------

	protected void storeNodeExtra(Dbms dbms, AbstractParams p, String path,
											String siteId, String optionsId) throws SQLException
	{
		ThreddsParams params = (ThreddsParams) p;

		settingMan.add(dbms, "id:"+siteId, "url",  params.url, false);
		settingMan.add(dbms, "id:"+siteId, "icon", params.icon, false);
		settingMan.add(dbms, "id:"+optionsId, "lang",  params.lang, false);
		settingMan.add(dbms, "id:"+optionsId, "topic",  params.topic, false);
		settingMan.add(dbms, "id:"+optionsId, "createThumbnails",  params.createThumbnails, false);
		settingMan.add(dbms, "id:"+optionsId, "createServiceMd", params.createServiceMd, false);
		settingMan.add(dbms, "id:"+optionsId, "createCollectionDatasetMd",  params.createCollectionDatasetMd, false);
		settingMan.add(dbms, "id:"+optionsId, "createAtomicDatasetMd",  params.createAtomicDatasetMd, false);
		settingMan.add(dbms, "id:"+optionsId, "ignoreHarvestOnCollections",  params.ignoreHarvestOnCollections, false);
		settingMan.add(dbms, "id:"+optionsId, "collectionGeneration",  params.collectionMetadataGeneration, false);
		settingMan.add(dbms, "id:"+optionsId, "collectionFragmentStylesheet",  params.collectionFragmentStylesheet, false);
		settingMan.add(dbms, "id:"+optionsId, "collectionMetadataTemplate",  params.collectionMetadataTemplate, false);
		settingMan.add(dbms, "id:"+optionsId, "createCollectionSubtemplates",  params.createCollectionSubtemplates, false);
		settingMan.add(dbms, "id:"+optionsId, "outputSchemaOnCollectionsDIF",  params.outputSchemaOnCollectionsDIF, false);
		settingMan.add(dbms, "id:"+optionsId, "outputSchemaOnCollectionsFragments",  params.outputSchemaOnCollectionsFragments, false);
		settingMan.add(dbms, "id:"+optionsId, "ignoreHarvestOnAtomics",  params.ignoreHarvestOnAtomics, false);
		settingMan.add(dbms, "id:"+optionsId, "atomicGeneration",  params.atomicMetadataGeneration, false);
		settingMan.add(dbms, "id:"+optionsId, "modifiedOnly",  params.modifiedOnly, false);
		settingMan.add(dbms, "id:"+optionsId, "atomicFragmentStylesheet",  params.atomicFragmentStylesheet, false);
		settingMan.add(dbms, "id:"+optionsId, "atomicMetadataTemplate",  params.atomicMetadataTemplate, false);
		settingMan.add(dbms, "id:"+optionsId, "createAtomicSubtemplates",  params.createAtomicSubtemplates, false);
		settingMan.add(dbms, "id:"+optionsId, "outputSchemaOnAtomicsDIF",  params.outputSchemaOnAtomicsDIF, false);
		settingMan.add(dbms, "id:"+optionsId, "outputSchemaOnAtomicsFragments",  params.outputSchemaOnAtomicsFragments, false);
		settingMan.add(dbms, "id:"+optionsId, "createAtomicDatasetMd",  params.createAtomicDatasetMd, false);
		settingMan.add(dbms, "id:"+optionsId, "datasetCategory",  params.datasetCategory, false);
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

