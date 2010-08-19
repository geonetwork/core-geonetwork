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

package org.fao.geonet.kernel.harvest.harvester.webdav;

import jeeves.interfaces.Logger;
import jeeves.server.context.ServiceContext;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.webdav.lib.WebdavResource;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class WebDavRetriever implements RemoteRetriever {
	//--------------------------------------------------------------------------
	//---
	//--- RemoteRetriever interface
	//---
	//--------------------------------------------------------------------------

	public void init(Logger log, ServiceContext context, WebDavParams params) {
		this.log    = log;
		this.context= context;
		this.params = params;
	}

	//---------------------------------------------------------------------------

	public List<RemoteFile> retrieve() throws Exception {
		davRes = open(params.url);
		files.clear();
		retrieveFiles(davRes);
		return files;
	}

	//---------------------------------------------------------------------------

	public void destroy() {
		try	{
			davRes.close();
		}
		catch(Exception e) {
			log.warning("Cannot close resource : "+ e.getMessage());
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private WebdavResource open(String url) throws Exception {
		log.debug("opening webdav resource with URL: " + url);
		if (!url.endsWith("/")) {
			log.debug("URL " + url + "does not end in slash -- will be appended");
			url += "/";
		}
		try {
			log.debug("Connecting to webdav url for node : "+ params.name + " URL: " + params.url);
			WebdavResource wr = createResource(url);
			log.debug("Connected to webdav resource at : "+ url);

			//--- we are interested only in folders
			// heikki: somehow this works fine here, but see retrieveFiles()
			if (!wr.isCollection()) {
				log.error("Resource url is not a collection : "+ url);
				wr.close();
				throw new Exception("Resource url is not a collection : "+ url);
			}
			else {
				log.info("Resource path is : "+ wr.getPath());
				return wr;
			}
		}
		catch(HttpException e) {
			throw new Exception("HTTPException: " + e.getMessage());
		}
	}

	//---------------------------------------------------------------------------

	private WebdavResource createResource(String url) throws Exception {
		log.debug("Creating WebdavResource");

		HttpURL http = url.startsWith("https") ? new HttpsURL(url) : new HttpURL(url);

		if(params.useAccount) {
			log.debug("using account, username: " + params.username + " password: " + params.password);
			http.setUserinfo(params.username, params.password);
		}
		else {
			log.debug("not using account");
		}

		//--- setup proxy, if the case

		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getSettingManager();

		boolean enabled = sm.getValueAsBool("system/proxy/use", false);
		String  host    = sm.getValue("system/proxy/host");
		String  port    = sm.getValue("system/proxy/port");
		
		if (!enabled) {
			log.debug("local proxy not enabled");
			log.debug("returning a new WebdavResource");
			log.debug("using http port: " + http.getPort() + " http uri: " + http.getURI());
			WebdavResource webdavResource = new WebdavResource(http, 1);
			return webdavResource;
		}
		else {
			log.debug("local proxy enabled");			
			if (!Lib.type.isInteger(port)) {
				throw new Exception("Proxy port is not an integer : "+ port);
			}
			log.debug("returning a new WebdavResource");
			log.debug("using http proxy port: " + port + " proxy host: " + host + " http uri: " + http.getURI());
			return new WebdavResource(http, host, Integer.parseInt(port));
		}
	}

	//---------------------------------------------------------------------------

	private void retrieveFiles(WebdavResource wr) throws HttpException, IOException {
		String path = wr.getPath();
		log.debug("Scanning resource : "+ path);
		WebdavResource[] wa = wr.listWebdavResources();
		log.debug("# " + wa.length + " webdav resources found in: " + wr.getPath());
		if(wa.length > 0) {
			int startSize = files.size();
			for(WebdavResource w : wa) {
				// heikki: even though response indicates that a sub directory is a collection, according to Slide
				//         that is never the case. To determine if a resource is a sub directory, use the following
				//         trick :				
				// if(w.getIsCollection()) {
				if(w.getPath().equals(wr.getPath()) && w.getDisplayName().length() > 0) {
					if(params.recurse) {
						log.debug(w.getPath() + " is a collection, processed recursively");
						String url = w.getHttpURL().getURI();
						url = url + w.getDisplayName()+ "/";
						HttpURL http = url.startsWith("https") ? new HttpsURL(url) : new HttpURL(url);
						WebdavResource huh = new WebdavResource(http, 1);
						retrieveFiles(huh);						
					}
					else {
						log.debug(w.getPath() + " is a collection. Ignoring because recursion is disabled.");
					}
				}
				else {
					log.debug(w.getName() + " is not a collection");
					if (w.getName().toLowerCase().endsWith(".xml")) {
						log.debug("found xml file ! " + w.getName().toLowerCase());
						files.add(new WebDavRemoteFile(w));
					}
					else {
						log.debug(w.getName().toLowerCase() + " is not an xml file");
					}					
				}
			}	
			int endSize = files.size();
			int added = endSize - startSize;
			if (added == 0) {
				log.debug("No xml files found in path : "+ path);
			}
			else {
				log.debug("Found "+ added +" xml file(s) in path : "+ path);
			}			
		}		
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private Logger         log;
	private ServiceContext context;
	private WebDavParams   params;
	private WebdavResource davRes;

	private List<RemoteFile> files = new ArrayList<RemoteFile>();
}

//=============================================================================