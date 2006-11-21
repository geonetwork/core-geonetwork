//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet;

import java.io.File;
import jeeves.constants.Jeeves;
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
import org.fao.geonet.kernel.csw.CatalogDispatcher;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.services.util.z3950.Server;
import org.jdom.Element;

//=============================================================================

/** This is the main class. It handles http connections and inits the system
  */

public class Geonetwork implements ApplicationHandler
{
	private Logger        logger;
	private SearchManager searchMan;

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

		String path = context.getAppPath();

		logger.info("Initializing geonetwork...");

		ServiceConfig handlerConfig = new ServiceConfig(config.getChildren());

		//------------------------------------------------------------------------
		//--- initialize search and editing

		logger.info("  - Search...");

		String luceneDir = handlerConfig.getMandatoryValue(Geonet.Config.LUCENE_DIR);

		searchMan = new SearchManager(path, luceneDir);

		//------------------------------------------------------------------------
		//--- extract intranet ip/mask and initialize AccessManager

		logger.info("  - Access manager...");

		String net  = handlerConfig.getMandatoryValue(Geonet.Config.NETWORK);
		String mask = handlerConfig.getMandatoryValue(Geonet.Config.NETMASK);

		AccessManager accessMan = new AccessManager(net, mask);

		//------------------------------------------------------------------------
		//--- get edit params and initialize DataManager

		logger.info("  - Data manager...");

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		String siteURL = getSiteURL(context, handlerConfig);
		String siteID  = handlerConfig.getMandatoryValue(Geonet.Config.SITE_ID);

		DataManager dataMan = new DataManager(searchMan, accessMan, dbms, siteURL, siteID);

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

		String z3950port      = handlerConfig.getMandatoryValue(Geonet.Config.Z3950PORT);
		String schemaMappings = handlerConfig.getMandatoryValue(Geonet.Config.SCHEMA_MAPPINGS);

		logger.info("Schema mapping is : " + schemaMappings); // FIXME

		UserSession session = new UserSession();
		session.authenticate(null, "z39.50", "", "", "Guest");
		context.setUserSession(session);
		context.setIpAddress(net);
		Server.init(z3950port, path + Jeeves.Path.XML + schemaMappings, context);

		//------------------------------------------------------------------------
		//--- initialize settings subsystem

		logger.info("  - Setting manager...");

		SettingManager settingMan = new SettingManager(dbms, context.getProviderManager());

		//------------------------------------------------------------------------
		//--- initialize harvesting subsystem

		logger.info("  - Harvest manager...");

		HarvestManager harvestMan = new HarvestManager(context, settingMan, dataMan);

		//------------------------------------------------------------------------
		//--- initialize catalogue services for the web

		logger.info("  - Catalogue services for the web...");

		CatalogDispatcher catalogDis = new CatalogDispatcher();

		//------------------------------------------------------------------------
		//--- return application context

		GeonetContext gnContext = new GeonetContext();

		gnContext.accessMan  = accessMan;
		gnContext.dataMan    = dataMan;
		gnContext.searchMan  = searchMan;
		gnContext.config     = handlerConfig;
		gnContext.catalogDis = catalogDis;
		gnContext.settingMan = settingMan;
		gnContext.harvestMan = harvestMan;

		logger.info("Site ID is : " + gnContext.getSiteId());

		return gnContext;
	}

	//---------------------------------------------------------------------------

	private String getSiteURL(ServiceContext srvContext, ServiceConfig handlerConfig)
	{
		// compute site url
		String defaultLang= srvContext.getLanguage();
		String baseUrl    = srvContext.getBaseUrl();
		String publicHost = handlerConfig.getMandatoryValue(Geonet.Config.PUBLIC_HOST);
		String publicPort = handlerConfig.getMandatoryValue(Geonet.Config.PUBLIC_PORT);

		String locService = baseUrl +"/"+ Jeeves.Prefix.SERVICE +"/"+ defaultLang;
		String siteURL = "http://" + publicHost + (publicPort == "80" ? "" : ":" + publicPort) + locService;

		return siteURL;
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

