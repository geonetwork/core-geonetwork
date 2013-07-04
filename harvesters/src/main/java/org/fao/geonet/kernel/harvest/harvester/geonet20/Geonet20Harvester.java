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

package org.fao.geonet.kernel.harvest.harvester.geonet20;

import jeeves.exceptions.BadInputEx;
import jeeves.exceptions.UserNotFoundEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Xml;
import jeeves.utils.XmlRequest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.resources.Resources;
import org.jdom.Element;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

//=============================================================================

public class Geonet20Harvester extends AbstractHarvester
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

	public String getType() { return "geonetwork20"; }

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	protected void doInit(Element node) throws BadInputEx
	{
		params = new GeonetParams(dataMan);
        super.setParams(params);
        params.create(node);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Add
	//---
	//---------------------------------------------------------------------------

	protected String doAdd(Dbms dbms, Element node) throws BadInputEx, SQLException
	{
		params = new GeonetParams(dataMan);
        super.setParams(params);

        //--- retrieve/initialize information
		params.create(node);

		//--- force the creation of a new uuid
		params.uuid = UUID.randomUUID().toString();

		String id = settingMan.add(dbms, "harvesting", "node", getType());

		storeNode(dbms, params, "id:"+id);
		Lib.sources.update(dbms, params.uuid, params.name, true);
		Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + "gn20.gif", params.uuid);
        
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

		Lib.sources.update(dbms, copy.uuid, copy.name, true);

		params = copy;
        super.setParams(params);

    }

	//---------------------------------------------------------------------------

	protected void storeNodeExtra(Dbms dbms, AbstractParams p, String path,
											String siteId, String optionsId) throws SQLException
	{
		GeonetParams params = (GeonetParams) p;
        super.setParams(params);

        settingMan.add(dbms, "id:"+siteId, "host",    params.host);

		//--- store search nodes

		for (Search s : params.getSearches())
		{
			String  searchID = settingMan.add(dbms, path, "search", "");

			settingMan.add(dbms, "id:"+searchID, "freeText", s.freeText);
			settingMan.add(dbms, "id:"+searchID, "title",    s.title);
			settingMan.add(dbms, "id:"+searchID, "abstract", s.abstrac);
			settingMan.add(dbms, "id:"+searchID, "keywords", s.keywords);
			settingMan.add(dbms, "id:"+searchID, "digital",  s.digital);
			settingMan.add(dbms, "id:"+searchID, "hardcopy", s.hardcopy);
			settingMan.add(dbms, "id:"+searchID, "siteId",   s.siteId);
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

		String small = params.host +
							"/srv/en/resources.get2?access=public&uuid="+uuid+"&fname=";

		info.addContent(new Element("smallThumbnail").setText(small));
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
			return;

		//--- ok, add proper info

		Element info = node.getChild("info");

		for (HarvestResult ar : result.alResult)
		{
			Element site = new Element("search");
			site.setAttribute("siteId", ar.siteId);

			add(site, "total",     ar.totalMetadata);
			add(site, "added",     ar.addedMetadata);
			add(site, "updated",   ar.updatedMetadata);
			add(site, "unchanged", ar.unchangedMetadata);
			add(site, "skipped",   ar.schemaSkipped+ ar.uuidSkipped);
			add(site, "removed",   ar.locallyRemoved);
            add(site, "doesNotValidate",ar.doesNotValidate);

			info.addContent(site);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- GetResult
	//---
	//---------------------------------------------------------------------------

	protected Element getResult() {
		return new Element("result"); // HarvestHistory not supported for this 
		                              // old harvester
	}

	//---------------------------------------------------------------------------
	//---
	//--- Harvest
	//---
	//---------------------------------------------------------------------------

	protected void doHarvest(Logger log, ResourceManager rm) throws Exception
	{
		Dbms dbms = (Dbms) rm.open(Geonet.Res.MAIN_DB);

		CategoryMapper localCateg = new CategoryMapper(dbms);

        XmlRequest req = new XmlRequest(params.host);

        servletName = req.getAddress();

		Lib.net.setupProxy(context, req);

		String name = getParams().name;

		//--- login

		if (params.useAccount)
		{
			log.info("Login into : "+ name);

			req.setAddress(servletName +"/srv/en/"+ Geonet.Service.XML_LOGIN);
			req.addParam("username", params.username);
			req.addParam("password", params.password);

			Element response = req.execute();

			if (!response.getName().equals("ok"))
				throw new UserNotFoundEx(params.username);
		}

		//--- search

		result = new GeonetResult();

		Aligner aligner = new Aligner(log, req, params, dataMan, dbms, context,
												localCateg);

		for(Search s : params.getSearches())
		{
			log.info("Searching on : "+ name +"/"+ s.siteId);

			req.setAddress(servletName +"/srv/en/"+ Geonet.Service.XML_SEARCH);

			Element searchResult = req.execute(s.createRequest());

            if(log.isDebugEnabled()) log.debug("Obtained:\n"+Xml.getString(searchResult));

			//--- site alignment
			HarvestResult ar = aligner.align(searchResult, s.siteId);

			//--- collect some stats
			result.alResult.add(ar);
		}

		//--- logout

		if (params.useAccount)
		{
			log.info("Logout from : "+ name);

			req.clearParams();
			req.setAddress("/"+ params.getServletPath() +"/srv/en/"+ Geonet.Service.XML_LOGOUT);
		}

		dbms.commit();
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private GeonetParams params;
	private GeonetResult result;

    private String servletName;
}

//=============================================================================

class GeonetResult
{
	public ArrayList<HarvestResult> alResult = new ArrayList<HarvestResult>();
}