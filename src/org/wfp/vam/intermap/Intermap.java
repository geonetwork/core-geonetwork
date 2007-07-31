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

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.resources.*;
import jeeves.utils.*;

import jeeves.server.*;

import org.wfp.vam.intermap.http.ConcurrentHTTPTransactionHandler;
import org.wfp.vam.intermap.http.cache.HttpGetFileCache;
import org.wfp.vam.intermap.kernel.map.DefaultMapServers;
import org.wfp.vam.intermap.kernel.map.mapServices.arcims.*;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.*;
import org.wfp.vam.intermap.kernel.map.MapMerger;
import org.wfp.vam.intermap.kernel.TempFiles;
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

	public Object start(String appPath, Logger l, Element config, ResourceManager resMan) throws Exception
	{
		logger = l;
		path   = appPath;

		log("Initializing intermap...");


		ServiceConfig handlerConfig = new ServiceConfig(config.getChildren());

		//------------------------------------------------------------------------
		//--- initialize map servers informations

		log("Map servers config...");
		String mapServersPath = handlerConfig.getMandatoryValue(Constants.MAP_SERVERS_CONFIG);
		Element mapServersRoot = Xml.loadFile(path + mapServersPath);
		DefaultMapServers.init(mapServersRoot);

		//------------------------------------------------------------------------
		//--- initialize the AXL Element builder

		log("axlElementBuilder config...");
		String axlPath = handlerConfig.getMandatoryValue(Constants.AXL_CONFIG);
		AxlRequestBuilder.init(path + axlPath);


		//------------------------------------------------------------------------
		//--- initialize WmsService

		log("WmsService config...");
		String wmsPath = handlerConfig.getMandatoryValue(Constants.WMS_CONFIG);
		WmsService.init(path + wmsPath);

		//------------------------------------------------------------------------
		//--- set the proxy server System properties

		log("Proxy server config...");
		String useProxy = handlerConfig.getMandatoryValue(Constants.USE_PROXY);
		log("The useproxy attribute is set to " + useProxy);
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

		log("Temporary files config...");
		String tempDir = handlerConfig.getMandatoryValue(Constants.TEMP_DIR);
		String tempDelete = handlerConfig.getMandatoryValue(Constants.TEMP_DELETE);
		int delete = Integer.parseInt(tempDelete);
		tempFiles = new TempFiles(tempDir, delete);

		//------------------------------------------------------------------------
		//--- Start the cache files clean up thread

		log("HTTP Cache files config...");
		String cacheDir = handlerConfig.getMandatoryValue(Constants.CACHE_DIR);
		String cacheDelete = handlerConfig.getMandatoryValue(Constants.CACHE_DELETE);
		int deleteCache = Integer.parseInt(cacheDelete);
		MapMerger.setCache(new HttpGetFileCache(cacheDir, deleteCache));
		String useCache = handlerConfig.getMandatoryValue(Constants.USE_CACHE);
		WmsGetCapClient.useCache("true".equals(useCache) ? true : false);

		//------------------------------------------------------------------------
		//--- Set the temporary files URL

		log("Temporary URL config...");
		String tempUrl = handlerConfig.getMandatoryValue(Constants.TEMP_URL);
		org.wfp.vam.intermap.services.map.MapUtil.setTempUrl(tempUrl);

		log("Screen DPI config...");
		String dpi = handlerConfig.getMandatoryValue(Constants.DPI);
		try {
			MapMerger.setDpi(Integer.parseInt(dpi));
		} catch (NumberFormatException e) {
			MapMerger.setDpi(96);
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

		log("setting default image size to " + d);
		// set default image size
		if ("small".equals(d))
			org.wfp.vam.intermap.services.map.MapUtil.setDefaultImageSize("small");
		else
			org.wfp.vam.intermap.services.map.MapUtil.setDefaultImageSize("big");

		log("...done");

		return new Object(); // DEBUG
	}

	//---------------------------------------------------------------------------
	//---
	//--- Stop
	//---
	//---------------------------------------------------------------------------

	public void stop()
	{
		log("Stopping intermap...");

		tempFiles.end();
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	/** Logs a message
	  */

	private void log(String message)
	{
		logger.log(message);
	}
}

//=============================================================================

