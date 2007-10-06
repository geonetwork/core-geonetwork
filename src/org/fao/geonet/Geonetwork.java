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

package org.fao.geonet;

import java.io.File;
import jeeves.interfaces.ApplicationHandler;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.csw.CatalogDispatcher;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.oaipmh.OaiPmhDispatcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.services.util.z3950.Server;
import org.jdom.Element;

//=============================================================================

/** This is the main class. It handles http connections and inits the system
  */

public class Geonetwork implements ApplicationHandler
{
	private Logger        		logger;
	private SearchManager 		searchMan;
	private ThesaurusManager 	thesaurusMan;

	//---------------------------------------------------------------------------
	//---
	//--- GetContextName
	//---
	//---------------------------------------------------------------------------

	public String getContextName() { return Geonet.CONTEXT_NAME; }

	//---------------------------------------------------------------------------
	//---
	//--- Start
	//---
	//---------------------------------------------------------------------------

	/** Inits the engine, loading all needed data
	  */

	public Object start(Element config, ServiceContext context) throws Exception
	{
		logger = context.getLogger();

		String path    = context.getAppPath();
		String baseURL = context.getBaseUrl();

		logger.info("Initializing geonetwork...");

		ServiceConfig handlerConfig = new ServiceConfig(config.getChildren());

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		//------------------------------------------------------------------------
		//--- initialize settings subsystem

		logger.info("  - Setting manager...");

		SettingManager settingMan = new SettingManager(dbms, context.getProviderManager());

		//------------------------------------------------------------------------
		//--- Initialize thesaurus

		logger.info("  - Thesaurus...");

		String thesauriDir = handlerConfig.getMandatoryValue(Geonet.Config.CODELIST_DIR);

		thesaurusMan = new ThesaurusManager(path, thesauriDir);


		//------------------------------------------------------------------------
		//--- initialize search and editing

		logger.info("  - Search...");

		String luceneDir = handlerConfig.getMandatoryValue(Geonet.Config.LUCENE_DIR);

		searchMan = new SearchManager(path, luceneDir);

		//------------------------------------------------------------------------
		//--- extract intranet ip/mask and initialize AccessManager

		logger.info("  - Access manager...");

		AccessManager accessMan = new AccessManager(dbms, settingMan);

		//------------------------------------------------------------------------
		//--- get edit params and initialize DataManager

		logger.info("  - Data manager...");

		DataManager dataMan = new DataManager(searchMan, accessMan, dbms, settingMan, baseURL);

		String schemasDir = path + Geonet.Path.SCHEMAS;
		String saSchemas[] = new File(schemasDir).list();

		if (saSchemas == null)
			throw new Exception("Cannot scan schemas directory : " +schemasDir);
		else
		{
			for(int i=0; i<saSchemas.length; i++)
				if (!saSchemas[i].equals("CVS") && !saSchemas[i].startsWith("."))
				{
					logger.info("    Adding xml schema : " +saSchemas[i]);
					String schemaFile  = schemasDir + saSchemas[i] +"/"+ Geonet.File.SCHEMA;
					String suggestFile = schemasDir + saSchemas[i] +"/"+ Geonet.File.SCHEMA_SUGGESTIONS;

					dataMan.addSchema(saSchemas[i], schemaFile, suggestFile);
				}
		}

		//------------------------------------------------------------------------
		//--- initialize Z39.50

		logger.info("  - Z39.50...");

		// FIXME: should I move these to elSearch?

		boolean z3950Enable    = settingMan.getValueAsBool("system/z3950/enable", false);
		String  z3950port      = settingMan.getValue("system/z3950/port");
		String  host           = settingMan.getValue("system/server/host");
		String  schemaMappings = handlerConfig.getMandatoryValue(Geonet.Config.SCHEMA_MAPPINGS);

		if (!z3950Enable)
			logger.info("     disabled");
		else
		{
			logger.info("     Enabled. Schema mappings is : " + schemaMappings);

			UserSession session = new UserSession();
			session.authenticate(null, "z39.50", "", "", "Guest");
			context.setUserSession(session);
			context.setIpAddress("127.0.0.1");
			Server.init(host, z3950port, path, schemaMappings, context);
		}

		//------------------------------------------------------------------------
		//--- initialize harvesting subsystem

		logger.info("  - Harvest manager...");

		HarvestManager harvestMan = new HarvestManager(context, settingMan, dataMan);

		//------------------------------------------------------------------------
		//--- initialize catalogue services for the web

		logger.info("  - Catalogue services for the web...");

		CatalogDispatcher catalogDis = new CatalogDispatcher();

		//------------------------------------------------------------------------
		//--- initialize catalogue services for the web

		logger.info("  - Open Archive Initiative (OAI-PMH) server...");

		OaiPmhDispatcher oaipmhDis = new OaiPmhDispatcher();

		//------------------------------------------------------------------------
		//--- return application context

		GeonetContext gnContext = new GeonetContext();

		gnContext.accessMan   = accessMan;
		gnContext.dataMan     = dataMan;
		gnContext.searchMan   = searchMan;
		gnContext.config      = handlerConfig;
		gnContext.catalogDis  = catalogDis;
		gnContext.settingMan  = settingMan;
		gnContext.harvestMan  = harvestMan;
		gnContext.thesaurusMan= thesaurusMan;
		gnContext.oaipmhDis   = oaipmhDis;

		logger.info("Site ID is : " + gnContext.getSiteId());

		return gnContext;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Stop
	//---
	//---------------------------------------------------------------------------

	public void stop()
	{
		logger.info("Stopping geonetwork...");

		//------------------------------------------------------------------------
		//--- end search

		logger.info("  - search...");

		try
		{
			searchMan.end();
		}
		catch (Exception e)
		{
			logger.error("Raised exception while stopping search");
			logger.error("  Exception : " +e);
			logger.error("  Message   : " +e.getMessage());
			logger.error("  Stack     : " +Util.getStackTrace(e));
		}

		//------------------------------------------------------------------------
		//--- end Z39.50

		logger.info("  - Z39.50...");
		Server.end();
	}
}

//=============================================================================

