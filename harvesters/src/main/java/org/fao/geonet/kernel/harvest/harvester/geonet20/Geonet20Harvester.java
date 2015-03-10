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

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Source;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.exceptions.UserNotFoundEx;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
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
	//--- Init
	//---
	//--------------------------------------------------------------------------

	protected void doInit(Element node, ServiceContext context) throws BadInputEx
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

	protected String doAdd(Element node) throws BadInputEx, SQLException
	{
		params = new GeonetParams(dataMan);
        super.setParams(params);

        //--- retrieve/initialize information
		params.create(node);

		//--- force the creation of a new uuid
		params.setUuid(UUID.randomUUID().toString());

		String id = settingMan.add("harvesting", "node", getType());

		storeNode(params, "id:"+id);
        Source source = new Source(params.getUuid(), params.getName(), params.getTranslations(), true);
        context.getBean(SourceRepository.class).save(source);
        Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + "gn20.gif", params.getUuid());
        
		return id;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Update
	//---
	//---------------------------------------------------------------------------

	protected void doUpdate(String id, Element node) throws BadInputEx, SQLException
	{
		//--- update variables

		GeonetParams copy = params.copy();

        //--- update variables
		copy.update(node);

		String path = "harvesting/id:"+ id;

		settingMan.removeChildren(path);

		//--- update database
		storeNode(copy, path);

		//--- we update a copy first because if there is an exception GeonetParams
		//--- could be half updated and so it could be in an inconsistent state

        Source source = new Source(copy.getUuid(), copy.getName(), copy.getTranslations(), true);
        context.getBean(SourceRepository.class).save(source);

        params = copy;
        super.setParams(params);

    }

	//---------------------------------------------------------------------------

	protected void storeNodeExtra(AbstractParams p, String path,
											String siteId, String optionsId) throws SQLException
	{
		GeonetParams params = (GeonetParams) p;
        super.setParams(params);

        settingMan.add("id:"+siteId, "host",    params.host);

		//--- store search nodes

		for (Search s : params.getSearches())
		{
			String  searchID = settingMan.add(path, "search", "");

			settingMan.add("id:"+searchID, "freeText", s.freeText);
			settingMan.add("id:"+searchID, "title",    s.title);
			settingMan.add("id:"+searchID, "abstract", s.abstrac);
			settingMan.add("id:"+searchID, "keywords", s.keywords);
			settingMan.add("id:"+searchID, "digital",  s.digital);
			settingMan.add("id:"+searchID, "hardcopy", s.hardcopy);
			settingMan.add("id:"+searchID, "siteId",   s.siteId);
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

    public Element getResult() {
		return new Element("result"); // HarvestHistory not supported for this 
		                              // old harvester
	}

	//---------------------------------------------------------------------------
	//---
	//--- Harvest
	//---
	//---------------------------------------------------------------------------

	public void doHarvest(Logger log) throws Exception
	{
		CategoryMapper localCateg = new CategoryMapper(context);

        XmlRequest req = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest(params.host);

        servletName = req.getAddress();

		Lib.net.setupProxy(context, req);

		String name = getParams().getName();

		//--- login

		if (params.isUseAccount())
		{
			log.info("Login into : "+ name);

			req.setAddress(servletName +"/srv/en/"+ Geonet.Service.XML_LOGIN);
			req.addParam("username", params.getUsername());
			req.addParam("password", params.getPassword());

			Element response = req.execute();

			if (!response.getName().equals("ok"))
				throw new UserNotFoundEx(params.getUsername());
		}
        if (cancelMonitor.get()) {
            return ;
        }

        //--- search

		result = new GeonetResult();

		Aligner aligner = new Aligner(cancelMonitor, log, req, params, dataMan, context,
												localCateg);

		for(Search s : params.getSearches())
		{
            if (cancelMonitor.get()) {
                return;
            }

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

		if (params.isUseAccount())
		{
			log.info("Logout from : "+ name);

			req.clearParams();
			req.setAddress("/"+ params.getServletPath() +"/srv/en/"+ Geonet.Service.XML_LOGOUT);
		}

        dataMan.flush();
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