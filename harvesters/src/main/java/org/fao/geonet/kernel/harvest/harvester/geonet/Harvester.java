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
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Source;
import org.fao.geonet.exceptions.BadServerResponseEx;
import org.fao.geonet.exceptions.BadSoapResponseEx;
import org.fao.geonet.exceptions.BadXmlResponseEx;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.exceptions.UserNotFoundEx;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.IHarvester;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

//=============================================================================

class Harvester implements IHarvester<HarvestResult> {
    private final AtomicBoolean cancelMonitor;
    //--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public Harvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, GeonetParams params)
	{
        this.cancelMonitor = cancelMonitor;
		this.log    = log;
		this.context= context;
		this.params = params;
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public HarvestResult harvest(Logger log) throws Exception
	{
        this.log = log;
		XmlRequest req = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest(new URL(params.host));

        Lib.net.setupProxy(context, req);

		//--- login

		if (params.isUseAccount())
		{
			
			try {
				log.info("Login into : "+ params.getName());
				req.setCredentials(params.getUsername(), params.getPassword());
                req.setPreemptiveBasicAuth(true);
			req.setAddress(params.getServletPath() + "/" + params.getNode()
					+ "/eng/xml.info");
            req.addParam("type", "me");

				Element response = req.execute();
				if(!response.getName().equals("info") || response.getChild("me") == null) {
					pre29Login(req);
				} else if(!"true".equals(response.getChild("me").getAttributeValue("authenticated"))) {
                    log.warning("Authentication failed for user: " + params.getUsername());
                    throw new UserNotFoundEx(params.getUsername());
				}
			} catch (Exception e) {
				pre29Login(req);
			}
		}

		//--- retrieve info on categories and groups

		log.info("Retrieving information from : "+ params.host);

		req.setAddress(params.getServletPath() + "/" + params.getNode()
				+ "/en/" + Geonet.Service.XML_INFO);
		req.clearParams();
		req.addParam("type", "sources");
		req.addParam("type", "groups");

		Element remoteInfo = req.execute();

		if (!remoteInfo.getName().equals("info"))
			throw new BadServerResponseEx(remoteInfo);

		//--- perform all searches

		Set<RecordInfo> records = new HashSet<RecordInfo>();

        for (Search s : params.getSearches()) {
            if (cancelMonitor.get()) {
                return new HarvestResult();
            }

            try {
                records.addAll(search(req, s));
            } catch (Exception t) {
                log.error("Unknown error trying to harvest");
                log.error(t.getMessage());
                log.error(t);
                errors.add(new HarvestError(t, log));
            } catch (Throwable t) {
                log.fatal("Something unknown and terrible happened while harvesting");
                log.fatal(t.getMessage());
                t.printStackTrace();
                errors.add(new HarvestError(t, log));
            }
        }

        if (params.isSearchEmpty()) {
            try {
                log.debug("Doing an empty search");
                records.addAll(search(req, Search.createEmptySearch()));
            } catch (Exception t) {
                log.error("Unknown error trying to harvest");
                log.error(t.getMessage());
                log.error(t);
                errors.add(new HarvestError(t, log));
            } catch (Throwable t) {
                log.fatal("Something unknown and terrible happened while harvesting");
                log.fatal(t.getMessage());
                t.printStackTrace();
                errors.add(new HarvestError(t, log));
            }
        }

		log.info("Total records processed in all searches :"+ records.size());

		//--- align local node

		Aligner      aligner = new Aligner(cancelMonitor, log, context, req, params, remoteInfo);
		HarvestResult result  = aligner.align(records, errors);

		Map<String, String> sources = buildSources(remoteInfo);
		updateSources(records, sources);

		return result;
	}

	private void pre29Login(XmlRequest req) throws IOException, BadXmlResponseEx, BadSoapResponseEx, UserNotFoundEx {
		log.info("Failed to login using basic auth (geonetwork 2.9+) trying pre-geonetwork 2.9 login: "+ params.getName());
		// try old authentication
		req.setAddress(params.getServletPath() + "/" + params.getNode() + "/en/"+ Geonet.Service.XML_LOGIN);
		req.addParam("username", params.getUsername());
		req.addParam("password", params.getPassword());
		
		Element response = req.execute();
		
		if (!response.getName().equals("ok")) {
			throw new UserNotFoundEx(params.getUsername());
		}
	}

	//---------------------------------------------------------------------------

	private Set<RecordInfo> search(XmlRequest request, Search s) throws OperationAbortedEx
	{
		Set<RecordInfo> records = new HashSet<RecordInfo>();

		for (Object o : doSearch(request, s).getChildren("metadata"))
		{

            if (cancelMonitor.get()) {
                return Collections.emptySet();
            }

            try {
                Element md = (Element) o;
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
            } catch (Exception e) {
                HarvestError harvestError = new HarvestError(e, log);
                harvestError.setDescription("Malformed element '"
                        + o.toString() + "'");
                harvestError
                        .setHint("It seems that there was some malformed element. Check with your administrator.");
                this.errors.add(harvestError);
            }
		}

		log.info("Records added to result list : "+ records.size());

		return records;
	}

	//---------------------------------------------------------------------------

	private Element doSearch(XmlRequest request, Search s) throws OperationAbortedEx
	{
		request.setAddress(params.getServletPath() + "/" + params.getNode()
				+ "/eng/" + Geonet.Service.XML_SEARCH);
		request.clearParams();
		try
		{
			log.info("Searching on : "+ params.getName());
			Element response = request.execute(s.createRequest());
            if(log.isDebugEnabled()) log.debug("Search results:\n"+ Xml.getString(response));

			return response;
        } catch (BadSoapResponseEx e) {
            log.warning("Raised exception when searching : " + e.getMessage());
            this.errors.add(new HarvestError(e, log));
            throw new OperationAbortedEx("Raised exception when searching", e);
        } catch (BadXmlResponseEx e) {
            HarvestError harvestError = new HarvestError(e, log);
            harvestError.setDescription("Error while searching on "
                    + params.getName() + ". Excepted XML, returned: "
                    + e.getMessage());
            harvestError.setHint("Check with your administrator.");
            this.errors.add(harvestError);
            throw new OperationAbortedEx("Raised exception when searching", e);
        } catch (IOException e) {
            HarvestError harvestError = new HarvestError(e, log);
            harvestError.setDescription("Error while searching on "
                    + params.getName() + ". ");
            harvestError.setHint("Check with your administrator.");
            this.errors.add(harvestError);
            throw new OperationAbortedEx("Raised exception when searching", e);
        } catch(Exception e) {
            HarvestError harvestError = new HarvestError(e, log);
            harvestError.setDescription("Error while searching on "
                    + params.getName() + ". ");
            harvestError.setHint("Check with your administrator.");
            this.errors.add(harvestError);
            log.warning("Raised exception when searching : "+ e);
            return new Element("response");
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

	private void updateSources(Set<RecordInfo> records,
										Map<String, String> remoteSources) throws SQLException, MalformedURLException
	{
		log.info("Aligning source logos from for : "+ params.getName());

		//--- collect all different sources that have been harvested

		Set<String> sources = new HashSet<String>();

		for (RecordInfo ri : records) {
			sources.add(ri.source);
        }

		//--- update local sources and retrieve logos (if the case)

        String siteId = context.getBean(SettingManager.class).getSiteId();

		for (String sourceUuid : sources) {
 			if (!siteId.equals(sourceUuid)) {
   				String sourceName = remoteSources.get(sourceUuid);

				if (sourceName != null) {
					retrieveLogo(context, params.host, sourceUuid);
                } else {
					sourceName = "(unknown)";
					Resources.copyUnknownLogo(context, sourceUuid);
				}

                Source source = new Source(sourceUuid, sourceName, new HashMap<String, String>(), false);
                context.getBean(SourceRepository.class).save(source);
            }
        }
	}


    private void retrieveLogo(ServiceContext context, String url, String uuid) throws MalformedURLException {
        String logo = uuid + ".gif";

        XmlRequest req = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest(new URL(url));
        Lib.net.setupProxy(context, req);
        req.setAddress(req.getAddress() + "/images/logos/" + logo);

        Path logoFile = Resources.locateLogosDir(context).resolve(logo);

        try {
            req.executeLarge(logoFile);
        } catch (IOException e) {
            context.warning("Cannot retrieve logo file from : " + url);
            context.warning("  (C) Logo  : " + logo);
            context.warning("  (C) Excep : " + e.getMessage());

            IO.deleteFile(logoFile, false, Geonet.GEONETWORK);

            Resources.copyUnknownLogo(context, uuid);
        }
    }
	//---------------------------------------------------------------------------

    //---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------
	private Logger         log;
	private GeonetParams   params;
    private ServiceContext context;
    public List<HarvestError> getErrors() {
        return errors;
    }


    /**
     * Contains a list of accumulated errors during the executing of this
     * harvest.
     */
    private List<HarvestError> errors = new LinkedList<HarvestError>();
}

//=============================================================================


