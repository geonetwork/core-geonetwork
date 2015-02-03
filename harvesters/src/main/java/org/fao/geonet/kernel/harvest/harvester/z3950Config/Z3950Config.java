//=============================================================================
//=== Copyright (C) 2001-2011 Food and Agriculture Organization of the
//=== United Nations (FAO-UN), United Nations World Food Programme (WFP)
//=== and United Nations Environment Programme (UNEP)
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

package org.fao.geonet.kernel.harvest.harvester.z3950Config;

import jeeves.constants.Jeeves;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.services.util.z3950.Repositories;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

//=============================================================================

public class Z3950Config
{
    private final AtomicBoolean cancelMonitor;
    //--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public Z3950Config(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, XmlRequest req, Z3950ConfigParams params)
	{
        this.cancelMonitor = cancelMonitor;
		this.log     = log;
		this.context = context;
		this.request = req;
		this.params  = params;

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		schemaMan = gc.getBean(SchemaManager.class);
		result  = new HarvestResult();
	}

	//--------------------------------------------------------------------------
	//---
	//--- Configer method
	//---
	//--------------------------------------------------------------------------

	public HarvestResult config(Set<RecordInfo> records) throws Exception
	{
		log.info("Start of Z3950 Config Harvest for : "+ params.getName());

		if (params.clearConfig) clearZ3950Config();

		//-----------------------------------------------------------------------
		//--- for each metadata returned by search, extract info and place in
		//--- JZKitConfig.xml.tem

		for(RecordInfo ri : records) {
            if (cancelMonitor.get()) {
                return this.result;
            }

            result.totalMetadata++;

			// get metadata from remote geonetwork machine (assume local for now)
			addServerToZ3950Config(ri.uuid);
		}

		log.info("End of Z3950 Config Harvest for : "+ params.getName());

		return result;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods 
	//---
	//--------------------------------------------------------------------------

	//--------------------------------------------------------------------------
	//--- Clear Z3950 Config from JZKitConfig.xml.tem
	//--------------------------------------------------------------------------

	private void clearZ3950Config() {
		Repositories.clearTemplate(context);
	}

	//--------------------------------------------------------------------------
	//--- Add new config to JZKitConfig.xml.tem
	//--------------------------------------------------------------------------
	
	private void addServerToZ3950Config(String uuid) throws Exception {

		request.clearParams();
		request.addParam("uuid",   uuid);
		request.setAddress(context.getBaseUrl() +"/"+ Jeeves.Prefix.SERVICE +"/en/" + Geonet.Service.XML_METADATA_GET);
		Element md = request.execute();

		// detect the schema
		String schema = schemaMan.autodetectSchema(md);

		Path convert19119ToJZKitRepo = schemaMan.getSchemaDir(schema).resolve(Geonet.Path.ISO19119TOJZKIT_STYLESHEET);

		if (Files.exists(convert19119ToJZKitRepo)) {
			Element repoElem = Xml.transform(md, convert19119ToJZKitRepo);
			if (repoElem.getName().equals("Repository")) {
				Repositories.addRepo(context, uuid, repoElem);
				result.addedMetadata++;
			} else {
				result.incompatibleMetadata++;
			}
		} else {
			result.incompatibleMetadata++;
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private Logger         log;
	private ServiceContext context;
	private XmlRequest     request;
	private Z3950ConfigParams   params;
	private SchemaManager  schemaMan;
	private HarvestResult   result;
}