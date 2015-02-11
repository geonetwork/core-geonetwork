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

package org.fao.geonet.kernel.harvest.harvester.csw;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Logger;
import org.fao.geonet.domain.Source;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.resources.Resources;
import org.jdom.Element;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

/**
 *  Harvest metadata from other catalogues using the CSW protocol
 */
public class CswHarvester extends AbstractHarvester<HarvestResult> {
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	protected void doInit(Element node, ServiceContext context) throws BadInputEx {
		params = new CswParams(dataMan);
        super.setParams(params);
		params.create(node);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Add
	//---
	//---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     *
     * @param node
     * @return
     * @throws org.fao.geonet.exceptions.BadInputEx
     * @throws java.sql.SQLException
     */
	protected String doAdd(Element node) throws BadInputEx, SQLException {
		params = new CswParams(dataMan);
        super.setParams(params);

        //--- retrieve/initialize information
		params.create(node);

		//--- force the creation of a new uuid
		params.setUuid(UUID.randomUUID().toString());

		String id = settingMan.add("harvesting", "node", getType());

		storeNode(params, "id:"+id);
        Source source = new Source(params.getUuid(), params.getName(), params.getTranslations(), true);
        context.getBean(SourceRepository.class).save(source);
        Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + params.icon, params.getUuid());

		return id;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Update
	//---
	//---------------------------------------------------------------------------

    /**
     *
     * @param id
     * @param node
     * @throws org.fao.geonet.exceptions.BadInputEx
     * @throws java.sql.SQLException
     */
	protected void doUpdate(String id, Element node) throws BadInputEx, SQLException {
		CswParams copy = params.copy();
        super.setParams(params);

        //--- update variables
		copy.update(node);

		String path = "harvesting/id:"+ id;

		settingMan.removeChildren(path);

		//--- update database
		storeNode(copy, path);

		//--- we update a copy first because if there is an exception CswParams could be half updated and so it could be
		// in an inconsistent state

        Source source = new Source(copy.getUuid(), copy.getName(), copy.getTranslations(), true);
        context.getBean(SourceRepository.class).save(source);
        Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + copy.icon, copy.getUuid());

		params = copy;
        super.setParams(params);

    }

    /**
     *
     * @param p
     * @param path
     * @param siteId
     * @param optionsId
     * @throws java.sql.SQLException
     */
	protected void storeNodeExtra(AbstractParams p, String path, String siteId, String optionsId) throws SQLException {
		CswParams params = (CswParams) p;
		
		settingMan.add("id:"+siteId, "capabUrl", params.capabUrl);
		settingMan.add("id:"+siteId, "icon",     params.icon);
        settingMan.add("id:"+siteId, "rejectDuplicateResource", params.rejectDuplicateResource);
        settingMan.add("id:"+siteId, "queryScope", params.queryScope);
        settingMan.add("id:"+siteId, "hopCount",     params.hopCount);
        settingMan.add("id:"+siteId, "xslfilter",     params.xslfilter);
        settingMan.add("id:"+siteId, "outputSchema",     params.outputSchema);

		//--- store dynamic search nodes
		String  searchID = settingMan.add(path, "search", "");	
		
		if (params.eltSearches!=null){
			for (Element element : params.eltSearches) {
				if (!element.getName().startsWith("parser")){
					settingMan.add("id:"+searchID, element.getName(), element.getText());
				}
			}
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Harvest
	//---
	//---------------------------------------------------------------------------

    /**
     *
     * @param log
     * @throws Exception
     */
    public void doHarvest(Logger log) throws Exception {
		Harvester h = new Harvester(cancelMonitor, log, context, params);
		result = h.harvest(log);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private CswParams params;
}
