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

import org.fao.geonet.Util;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.jdom.Element;

//=============================================================================

public class ThreddsParams extends AbstractParams
{
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public ThreddsParams(DataManager dm)
	{
		super(dm);
	}

	//---------------------------------------------------------------------------
	//---atomicMetadataGeneration
	//--- Create : called when a new entry must be added. Reads values from the
	//---          provided entry, providing default values
	//---
	//---------------------------------------------------------------------------

	public void create(Element node) throws BadInputEx
	{
		super.create(node);

		Element site = node.getChild("site");
		Element opt  = node.getChild("options");

		url       		= Util.getParam(site, "url",  "");
		icon      		= Util.getParam(site, "icon", "");
		lang  	  		= Util.getParam(opt, "lang",  "");
		topic  	  		= Util.getParam(opt, "topic",  "");
		createThumbnails= Util.getParam(opt, "createThumbnails",  false);
		createServiceMd = Util.getParam(opt, "createServiceMd", false);
		createCollectionDatasetMd	= Util.getParam(opt, "createCollectionDatasetMd",  false);
		createAtomicDatasetMd 		= Util.getParam(opt, "createAtomicDatasetMd",  false);
		ignoreHarvestOnCollections	= Util.getParam(opt, "ignoreHarvestOnCollections",  false);
		outputSchemaOnCollectionsDIF 	= Util.getParam(opt, "outputSchemaOnCollectionsDIF",  "");
		outputSchemaOnCollectionsFragments 	= Util.getParam(opt, "outputSchemaOnCollectionsFragments",  "");
		ignoreHarvestOnAtomics		= Util.getParam(opt, "ignoreHarvestOnAtomics",  false);
		outputSchemaOnAtomicsDIF 		= Util.getParam(opt, "outputSchemaOnAtomicsDIF",  "");
		outputSchemaOnAtomicsFragments 		= Util.getParam(opt, "outputSchemaOnAtomicsFragments",  "");
		datasetCategory				= Util.getParam(opt, "datasetCategory",  "");
		atomicMetadataGeneration			= Util.getParam(opt, "atomicGeneration", "default");
		modifiedOnly				= Util.getParam(opt, "modifiedOnly",  false);
		collectionMetadataGeneration		= Util.getParam(opt, "collectionGeneration", "default");
		createAtomicSubtemplates	= Util.getParam(opt, "createAtomicSubtemplates", false);
		createCollectionSubtemplates	= Util.getParam(opt, "createCollectionSubtemplates", false);
		atomicFragmentStylesheet		= Util.getParam(opt, "atomicFragmentStylesheet", "");
		collectionFragmentStylesheet	= Util.getParam(opt, "collectionFragmentStylesheet", "");
		atomicMetadataTemplate			= Util.getParam(opt, "atomicMetadataTemplate", "");
		collectionMetadataTemplate		= Util.getParam(opt, "collectionMetadataTemplate", "");
	}

	//---------------------------------------------------------------------------
	//---
	//--- Update : called when an entry has changed and variables must be updated
	//---
	//---------------------------------------------------------------------------

	public void update(Element node) throws BadInputEx
	{
		super.update(node);

		Element site = node.getChild("site");
		Element opt  = node.getChild("options");

		url       		= Util.getParam(site,  "url",  url);
		icon      		= Util.getParam(site,  "icon", icon);
		
		lang  	  		= Util.getParam(opt, "lang",  lang);
		topic  	  		= Util.getParam(opt, "topic",  topic);
		createThumbnails= Util.getParam(opt, "createThumbnails",  createThumbnails);
		createServiceMd = Util.getParam(opt, "createServiceMd", false);
		createCollectionDatasetMd		= Util.getParam(opt, "createCollectionDatasetMd",  createCollectionDatasetMd);
		createAtomicDatasetMd			= Util.getParam(opt, "createAtomicDatasetMd",  createAtomicDatasetMd);
		ignoreHarvestOnCollections 		= Util.getParam(opt, "ignoreHarvestOnCollections",  ignoreHarvestOnCollections);
		outputSchemaOnCollectionsDIF		= Util.getParam(opt, "outputSchemaOnCollectionsDIF",  outputSchemaOnCollectionsDIF);
		outputSchemaOnCollectionsFragments		= Util.getParam(opt, "outputSchemaOnCollectionsFragments",  outputSchemaOnCollectionsFragments);
		ignoreHarvestOnAtomics 			= Util.getParam(opt, "ignoreHarvestOnAtomics", ignoreHarvestOnAtomics);
		modifiedOnly		 			= Util.getParam(opt, "modifiedOnly", modifiedOnly);
		outputSchemaOnAtomicsDIF			= Util.getParam(opt, "outputSchemaOnAtomicsDIF", outputSchemaOnAtomicsDIF);
		outputSchemaOnAtomicsFragments			= Util.getParam(opt, "outputSchemaOnAtomicsFragments", outputSchemaOnAtomicsFragments);
		datasetCategory 				= Util.getParam(opt, "datasetCategory",  datasetCategory);
		atomicMetadataGeneration				= Util.getParam(opt, "atomicGeneration", atomicMetadataGeneration);
		collectionMetadataGeneration			= Util.getParam(opt, "collectionGeneration", collectionMetadataGeneration);
		createAtomicSubtemplates		= Util.getParam(opt, "createAtomicSubtemplates", createAtomicSubtemplates);
		createCollectionSubtemplates	= Util.getParam(opt, "createCollectionSubtemplates", createCollectionSubtemplates);
		atomicFragmentStylesheet		= Util.getParam(opt, "atomicFragmentStylesheet", atomicFragmentStylesheet);
		collectionFragmentStylesheet	= Util.getParam(opt, "collectionFragmentStylesheet", collectionFragmentStylesheet);
		atomicMetadataTemplate			= Util.getParam(opt, "atomicMetadataTemplate", atomicMetadataTemplate);
		collectionMetadataTemplate		= Util.getParam(opt, "collectionMetadataTemplate", collectionMetadataTemplate);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Other API methods
	//---
	//---------------------------------------------------------------------------

	public ThreddsParams copy()
	{
		ThreddsParams copy = new ThreddsParams(dm);
		copyTo(copy);

		copy.url  				= url;
		copy.icon 				= icon;
		copy.lang 				= lang;
		copy.topic 				= topic;
		copy.createThumbnails   			= createThumbnails;
		copy.createServiceMd				= createServiceMd;
		copy.createCollectionDatasetMd 		= createCollectionDatasetMd;
		copy.createAtomicDatasetMd 			= createAtomicDatasetMd;
		copy.ignoreHarvestOnCollections 	= ignoreHarvestOnCollections;
		copy.atomicMetadataGeneration				= atomicMetadataGeneration;
		copy.collectionMetadataGeneration			= collectionMetadataGeneration;
		copy.modifiedOnly					= modifiedOnly;
		copy.createAtomicSubtemplates		= createAtomicSubtemplates;
		copy.createCollectionSubtemplates	= createCollectionSubtemplates;
		copy.atomicFragmentStylesheet		= atomicFragmentStylesheet;
		copy.collectionFragmentStylesheet	= collectionFragmentStylesheet;
		copy.atomicMetadataTemplate			= atomicMetadataTemplate;
		copy.collectionMetadataTemplate		= collectionMetadataTemplate;
		copy.outputSchemaOnCollectionsDIF		= outputSchemaOnCollectionsDIF;
		copy.outputSchemaOnCollectionsFragments		= outputSchemaOnCollectionsFragments;
		copy.ignoreHarvestOnAtomics			= ignoreHarvestOnAtomics;
		copy.outputSchemaOnAtomicsDIF 			= outputSchemaOnAtomicsDIF;
		copy.outputSchemaOnAtomicsFragments 			= outputSchemaOnAtomicsFragments;
		copy.datasetCategory    			= datasetCategory;
		return copy;
	}
	
	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	public String url;
	public String icon;
	public String lang;
	public String topic;
	public boolean createThumbnails;
	public boolean createServiceMd;
	public boolean createCollectionDatasetMd;
	public boolean createAtomicDatasetMd;
	public boolean modifiedOnly;
	public boolean ignoreHarvestOnCollections;
	public boolean ignoreHarvestOnAtomics;
	public String collectionMetadataGeneration;
	public String atomicMetadataGeneration;
	public String collectionFragmentStylesheet;
	public String atomicFragmentStylesheet;
	public String collectionMetadataTemplate;
	public String atomicMetadataTemplate;
	public boolean createCollectionSubtemplates;
	public boolean createAtomicSubtemplates;
	public String	outputSchemaOnAtomicsDIF;
	public String	outputSchemaOnCollectionsDIF;
	public String	outputSchemaOnAtomicsFragments;
	public String	outputSchemaOnCollectionsFragments;
	public String datasetCategory;
	
	//---------------------------------------------------------------------------
	//---
	//--- Constants
	//---
	//---------------------------------------------------------------------------

	static public final String DEFAULT = "default";
	static public final String FRAGMENTS = "fragments";
}

//=============================================================================


