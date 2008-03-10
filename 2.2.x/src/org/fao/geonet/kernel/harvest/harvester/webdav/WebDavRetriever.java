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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jeeves.interfaces.Logger;
import jeeves.server.context.ServiceContext;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.webdav.lib.WebdavResource;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;

//=============================================================================

class WebDavRetriever implements RemoteRetriever
{
	//--------------------------------------------------------------------------
	//---
	//--- RemoteRetriever interface
	//---
	//--------------------------------------------------------------------------

	public void init(Logger log, ServiceContext context, WebDavParams params)
	{
		this.log    = log;
		this.context= context;
		this.params = params;
	}

	//---------------------------------------------------------------------------

	public List<RemoteFile> retrieve() throws Exception
	{
		davRes = open(params.url);

		files.clear();
		retrieveFiles(davRes);

		return files;
	}

	//---------------------------------------------------------------------------

	public void destroy()
	{
		try
		{
			davRes.close();
		}
		catch(Exception e)
		{
			log.warning("Cannot close resource : "+ e.getMessage());
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private WebdavResource open(String url) throws Exception
	{
		if (!url.endsWith("/"))
			url += "/";

		try
		{
			log.info("Connecting to webdav url for node : "+ params.name);

			WebdavResource wr = createResource(params.url);

			log.info("Connected to webdav resource at : "+ url);

			//--- we are interested only in folders
			if (!wr.isCollection())
			{
				log.error("Resource url is not a collection : "+ url);
				wr.close();

				throw new Exception("Resource url is not a collection : "+ url);
			}

			log.info("Resource path is : "+ wr.getPath());

			return wr;
		}

		catch(HttpException e)
		{
			int code = e.getReasonCode();

			if (code == HttpStatus.SC_METHOD_NOT_ALLOWED)
				throw new Exception("Server does not support WebDAV");

			if (code == HttpStatus.SC_UNAUTHORIZED)
				throw new Exception("Unauthorized to access resource");

			throw new Exception("Received unknown response from server : "+ e.getReason());
		}
	}

	//---------------------------------------------------------------------------

	private WebdavResource createResource(String url) throws Exception
	{
		HttpURL http = url.startsWith("https") ? new HttpsURL(url) : new HttpURL(url);

		if (params.useAccount)
			http.setUserinfo(params.username, params.password);

		//--- setup proxy, if the case

		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getSettingManager();

		boolean enabled = sm.getValueAsBool("system/proxy/use", false);
		String  host    = sm.getValue("system/proxy/host");
		String  port    = sm.getValue("system/proxy/port");

		if (!enabled)
			return new WebdavResource(http);

		if (!Lib.type.isInteger(port))
			throw new Exception("Proxy port is not an integer : "+ port);

		return new WebdavResource(http, host, Integer.parseInt(port));
	}

	//---------------------------------------------------------------------------

	private void retrieveFiles(WebdavResource wr) throws HttpException, IOException
	{
		String path = wr.getPath();

		log.debug("Scanning resource : "+ path);

		String[] children = wr.list();

		if (children == null)
			log.warning("Cannot scan resource. Error is : "+ wr.getStatusMessage());
		else
		{
			int startSize = files.size();

			for (String name : children)
			{
				wr.setPath(path +"/"+ name);

				if (wr.exists())
				{
					if (wr.isCollection())
					{
						//--- this test is needed to fix a slide bug

						if (!wr.getPath().equals(path))
							if (params.recurse)
								retrieveFiles(wr);
					}

					else if (wr.getName().toLowerCase().endsWith(".xml"))
						files.add(new WebDavRemoteFile(wr));
				}
			}

			int endSize = files.size();
			int added   = endSize - startSize;

			if (added == 0) log.debug("No xml files found in path : "+ path);
				else			 log.debug("Found "+ added +" xml file(s) in path : "+ path);
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


