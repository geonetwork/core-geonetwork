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

import jeeves.exceptions.BadServerResponseEx;
import jeeves.exceptions.BadSoapResponseEx;
import jeeves.exceptions.BadXmlResponseEx;
import jeeves.exceptions.OperationAbortedEx;
import jeeves.exceptions.UserNotFoundEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import jeeves.utils.XmlRequest;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.resources.Resources;
import org.jdom.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//=============================================================================

class Harvester
{
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public Harvester(Logger log, ServiceContext context, Dbms dbms, GeonetParams params)
	{
		this.log    = log;
		this.context= context;
		this.dbms   = dbms;
		this.params = params;
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public GeonetResult harvest() throws Exception
	{
		XmlRequest req = new XmlRequest(new URL(params.host));

        Lib.net.setupProxy(context, req);

		//--- login

		if (params.useAccount)
		{
			
			try {
				log.info("Login into : "+ params.name);
				req.setCredentials(params.username, params.password);
				req.setAddress(params.getServletPath()+"/srv/eng/xml.info?type=me");

				Element response = req.execute();
				if(!response.getName().equals("info") || response.getChild("me") == null) {
					pre29Login(req);
				} else if(!"true".equals(response.getChild("me").getAttributeValue("authenticated"))) {
					throw new UserNotFoundEx(params.username);
				}
			} catch (Exception e) {
				pre29Login(req);
			}
		}

		//--- retrieve info on categories and groups

		log.info("Retrieving information from : "+ params.host);

		req.setAddress(params.getServletPath() +"/srv/en/"+ Geonet.Service.XML_INFO);
		req.clearParams();
		req.addParam("type", "sources");
		req.addParam("type", "groups");

		Element remoteInfo = req.execute();

		if (!remoteInfo.getName().equals("info"))
			throw new BadServerResponseEx(remoteInfo);

		//--- perform all searches

		Set<RecordInfo> records = new HashSet<RecordInfo>();

		for(Search s : params.getSearches())
			records.addAll(search(req, s));

		if (params.isSearchEmpty())
			records.addAll(search(req, Search.createEmptySearch()));

		log.info("Total records processed in all searches :"+ records.size());

		//--- align local node

		Aligner      aligner = new Aligner(log, context, dbms, req, params, remoteInfo);
		GeonetResult result  = aligner.align(records);

		Map<String, String> sources = buildSources(remoteInfo);
		updateSources(dbms, records, sources);

		return result;
	}

	private void pre29Login(XmlRequest req) throws IOException, BadXmlResponseEx, BadSoapResponseEx, UserNotFoundEx {
		log.info("Failed to login using basic auth (geonetwork 2.9+) trying pre-geonetwork 2.9 login: "+ params.name);
		// try old authentication
		req.setAddress(params.getServletPath() + "/srv/en/"+ Geonet.Service.XML_LOGIN);
		req.addParam("username", params.username);
		req.addParam("password", params.password);
		
		Element response = req.execute();
		
		if (!response.getName().equals("ok")) {
			throw new UserNotFoundEx(params.username);
		}
	}

	//---------------------------------------------------------------------------

	private Set<RecordInfo> search(XmlRequest request, Search s) throws OperationAbortedEx
	{
		Set<RecordInfo> records = new HashSet<RecordInfo>();

		for (Object o : doSearch(request, s).getChildren("metadata"))
		{
			Element md   = (Element) o;
			Element info = md.getChild("info", Edit.NAMESPACE);

			if (info == null)
				log.warning("Missing 'geonet:info' element in 'metadata' element");
			else
			{
				String uuid       = info.getChildText("uuid");
				String schema     = info.getChildText("schema");
				String changeDate = info.getChildText("changeDate");
				String source     = info.getChildText("source");

				records.add(new RecordInfo(uuid, changeDate, schema, source));
			}
		}

		log.info("Records added to result list : "+ records.size());

		return records;
	}

	//---------------------------------------------------------------------------

	private Element doSearch(XmlRequest request, Search s) throws OperationAbortedEx
	{
		request.setAddress(params.getServletPath() +"/srv/en/"+ Geonet.Service.XML_SEARCH);
		
		try
		{
			log.info("Searching on : "+ params.name);
			Element response = request.execute(s.createRequest());
            if(log.isDebugEnabled()) log.debug("Search results:\n"+ Xml.getString(response));

			return response;
		}
		catch(Exception e)
		{
			log.warning("Raised exception when searching : "+ e);
			throw new OperationAbortedEx("Raised exception when searching", e);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Sources update
	//---
	//---------------------------------------------------------------------------

	private Map<String, String> buildSources(Element info) throws BadServerResponseEx
	{
		Element sources = info.getChild("sources");

		if (sources == null)
			throw new BadServerResponseEx(info);

		Map<String, String> map = new HashMap<String, String>();

		for (Object o : sources.getChildren())
		{
			Element source = (Element) o;

			String uuid = source.getChildText("uuid");
			String name = source.getChildText("name");

			map.put(uuid, name);
		}

		return map;
	}

	//---------------------------------------------------------------------------

	private void updateSources(Dbms dbms, Set<RecordInfo> records,
										Map<String, String> remoteSources) throws SQLException, MalformedURLException
	{
		log.info("Aligning source logos from for : "+ params.name);

		//--- collect all different sources that have been harvested

		Set<String> sources = new HashSet<String>();

		for (RecordInfo ri : records)
			sources.add(ri.source);

		//--- update local sources and retrieve logos (if the case)

		String siteId = getSiteId();

		for (String sourceUuid : sources)
			if (!siteId.equals(sourceUuid))
			{
				String sourceName = remoteSources.get(sourceUuid);

				if (sourceName != null)
					Lib.sources.retrieveLogo(context, params.host, sourceUuid);
				else
				{
					sourceName = "(unknown)";
					Resources.copyUnknownLogo(context, sourceUuid);
				}

				Lib.sources.update(dbms, sourceUuid, sourceName, false);
			}
	}

	//---------------------------------------------------------------------------

	private String getSiteId()
	{
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm =gc.getSettingManager();

		return sm.getValue("system/site/siteId");
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private Logger         log;
	private Dbms           dbms;
	private GeonetParams   params;
    private ServiceContext context;
}

//=============================================================================


