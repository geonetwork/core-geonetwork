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

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Logger;
import org.fao.geonet.domain.Source;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.repository.SourceRepository;
import org.jdom.Element;

import java.sql.SQLException;
import java.util.UUID;

//=============================================================================

public class GeonetHarvester extends AbstractHarvester<HarvestResult>
{
	public static final String TYPE = "geonetwork";

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
        Source source = new Source(params.getUuid(), params.getName(), params.getTranslations(), false);
        context.getBean(SourceRepository.class).save(source);

        return id;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Update
	//---
	//---------------------------------------------------------------------------

	protected void doUpdate(String id, Element node) throws BadInputEx, SQLException
	{
		GeonetParams copy = params.copy();
        super.setParams(params);

        //--- update variables
		copy.update(node);

		String path = "harvesting/id:"+ id;

		settingMan.removeChildren(path);

		//--- update database
		storeNode(copy, path);

		//--- we update a copy first because if there is an exception GeonetParams
		//--- could be half updated and so it could be in an inconsistent state

        Source source = new Source(copy.getUuid(), copy.getName(), copy.getTranslations(), false);
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
        settingMan.add("id:"+siteId, "node", params.getNode());
        settingMan.add("id:"+siteId, "useChangeDateForUpdate", params.useChangeDateForUpdate());
		settingMan.add("id:"+siteId, "createRemoteCategory", params.createRemoteCategory);
		settingMan.add("id:"+siteId, "mefFormatFull", params.mefFormatFull);
		settingMan.add("id:"+siteId, "xslfilter", params.xslfilter);

		//--- store search nodes

		for (Search s : params.getSearches())
		{
			String  searchID = settingMan.add(path, "search", "");

			settingMan.add("id:"+searchID, "freeText",   s.freeText);
			settingMan.add("id:"+searchID, "title",      s.title);
			settingMan.add("id:"+searchID, "abstract",   s.abstrac);
			settingMan.add("id:"+searchID, "keywords",   s.keywords);
			settingMan.add("id:"+searchID, "digital",    s.digital);
			settingMan.add("id:"+searchID, "hardcopy",   s.hardcopy);
			settingMan.add("id:"+searchID, "sourceUuid", s.sourceUuid);
			settingMan.add("id:"+searchID, "sourceName", s.sourceName);
			settingMan.add("id:"+searchID, "anyField",   s.anyField);
			settingMan.add("id:"+searchID, "anyValue",   s.anyValue);
		}

		//--- store group mapping

		for (Group g : params.getGroupCopyPolicy())
		{
			String  groupID = settingMan.add(path, "groupCopyPolicy", "");

			settingMan.add("id:"+groupID, "name",   g.name);
			settingMan.add("id:"+groupID, "policy", g.policy);
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
	}

	//---------------------------------------------------------------------------
	//---
	//--- Harvest
	//---
	//---------------------------------------------------------------------------

	public void doHarvest(Logger log) throws Exception
	{
		Harvester h = new Harvester(cancelMonitor, log, context, params);
		result = h.harvest(log);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private GeonetParams params;
}
