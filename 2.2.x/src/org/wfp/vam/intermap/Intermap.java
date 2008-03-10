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

package org.wfp.vam.intermap;

import jeeves.interfaces.ApplicationHandler;
import jeeves.interfaces.Logger;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.jdom.Element;
import org.wfp.vam.intermap.http.ConcurrentHTTPTransactionHandler;
import org.wfp.vam.intermap.http.cache.HttpGetFileCache;
import org.wfp.vam.intermap.kernel.GlobalTempFiles;
import org.wfp.vam.intermap.kernel.TempFiles;
import org.wfp.vam.intermap.kernel.map.DefaultMapServers;
import org.wfp.vam.intermap.kernel.map.MapMerger;
import org.wfp.vam.intermap.kernel.map.mapServices.arcims.AxlRequestBuilder;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.CapabilitiesStore;
import org.wfp.vam.intermap.services.map.MapUtil;


//=============================================================================

/** This is the main class. It handles http connections and inits the system
  */

public class Intermap implements ApplicationHandler
{
	private Logger logger;
	private String path;
	private String charset;
	private String stdName;
	private String stdVersion;
	private static TempFiles tempFiles;

	//---------------------------------------------------------------------------
	//---
	//--- GetContextName
	//---
	//---------------------------------------------------------------------------

	public String getContextName() { return "intermap"; }

	//---------------------------------------------------------------------------
	//---
	//--- Start
	//---
	//---------------------------------------------------------------------------

	/** Inits the engine, loading all needed data
	  */

//	public Object start(String appPath, Logger l, Element config, ResourceManager resMan) throws Exception
	public Object start(Element config, ServiceContext context) throws Exception
	{
		logger = context.getLogger();
		String path    = context.getAppPath();
		String baseURL = context.getBaseUrl();

		logger.info("Initializing intermap...");


		ServiceConfig handlerConfig = new ServiceConfig(config.getChildren());

		//------------------------------------------------------------------------
		//--- initialize map servers informations

		logger.info("Map servers config...");
		String mapServersPath = handlerConfig.getMandatoryValue(Constants.MAP_SERVERS_CONFIG);
		Element mapServersRoot = Xml.loadFile(path + mapServersPath);
		DefaultMapServers.init(mapServersRoot);

		//------------------------------------------------------------------------
		//--- initialize the AXL Element builder

		logger.info("axlElementBuilder config...");
		String axlPath = handlerConfig.getMandatoryValue(Constants.AXL_CONFIG);
		AxlRequestBuilder.init(path + axlPath);


//		//------------------------------------------------------------------------
//		//--- initialize WmsService
//
//		log("WmsService config...");
//		String wmsPath = handlerConfig.getMandatoryValue(Constants.WMS_CONFIG);
//		WmsService.init(path + wmsPath);

		//------------------------------------------------------------------------
		//--- set the proxy server System properties

		logger.info("Proxy server config...");
		String useProxy = handlerConfig.getMandatoryValue(Constants.USE_PROXY);
		logger.info("The useproxy attribute is set to " + useProxy);
		if ("true".equals(useProxy)) {
			String proxyHost = handlerConfig.getMandatoryValue(Constants.PROXY_HOST);
			String proxyPort = handlerConfig.getMandatoryValue(Constants.PROXY_PORT);
			System.setProperty("http.proxyHost", proxyHost);
			System.setProperty("http.proxyPort", proxyPort);

			// new http transaction handler
			ConcurrentHTTPTransactionHandler.setProxy(proxyHost, Integer.parseInt(proxyPort));
		}

		//------------------------------------------------------------------------
		//--- Start the temporary files clean up thread

		logger.info("Temporary files config...");
		String tempDir = handlerConfig.getMandatoryValue(Constants.TEMP_DIR);
		String tempDelete = handlerConfig.getMandatoryValue(Constants.TEMP_DELETE);
		int delete = Integer.parseInt(tempDelete);
		GlobalTempFiles.init(path, tempDir, delete);
		tempFiles = GlobalTempFiles.getInstance();

		//------------------------------------------------------------------------
		//--- Start the cache files clean up thread

		logger.info("HTTP Cache files config...");
		String cacheDir = handlerConfig.getMandatoryValue(Constants.CACHE_DIR);
		String cacheDelete = handlerConfig.getMandatoryValue(Constants.CACHE_DELETE);
		int deleteCache = Integer.parseInt(cacheDelete);
		MapMerger.setCache(new HttpGetFileCache(path, cacheDir, deleteCache));
		String useCache = handlerConfig.getMandatoryValue(Constants.USE_CACHE);
		CapabilitiesStore.useCache("true".equals(useCache) ? true : false);

		//------------------------------------------------------------------------
		//--- Set the temporary files URL

		logger.info("Temporary URL config...");
		String tempUrl = handlerConfig.getMandatoryValue(Constants.TEMP_URL);
		org.wfp.vam.intermap.services.map.MapUtil.setTempUrl(tempUrl);

		logger.info("Screen DPI config...");
		String dpi = handlerConfig.getMandatoryValue(Constants.DPI);
		try {
			MapMerger.setDefaultDPI(Integer.parseInt(dpi));
		} catch (NumberFormatException e) {
			MapMerger.setDefaultDPI(96);
		}

		//------------------------------------------------------------------------
		//--- Set image sizes

		String stMinh = handlerConfig.getMandatoryValue("smallImageHeight");
		String stMinw = handlerConfig.getMandatoryValue("smallImageWidth");
		String stMaxh = handlerConfig.getMandatoryValue("bigImageHeight");
		String stMaxw = handlerConfig.getMandatoryValue("bigImageWidth");
		String d = handlerConfig.getMandatoryValue("defaultImageSize");

		int minh = Integer.parseInt(stMinh);
		int minw = Integer.parseInt(stMinw);
		int maxh = Integer.parseInt(stMaxh);
		int maxw = Integer.parseInt(stMaxw);

		MapUtil.setImageSizes(minh, minw, maxh, maxw);

		logger.info("setting default image size to " + d);
		// set default image size
		if ("small".equals(d))
			org.wfp.vam.intermap.services.map.MapUtil.setDefaultImageSize("small");
		else
			org.wfp.vam.intermap.services.map.MapUtil.setDefaultImageSize("big");

		logger.info("...done");

		return new Object(); // DEBUG
	}

	//---------------------------------------------------------------------------
	//---
	//--- Stop
	//---
	//---------------------------------------------------------------------------

	public void stop()
	{
		logger.info("Stopping intermap...");

		tempFiles.end();
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

}

//=============================================================================

