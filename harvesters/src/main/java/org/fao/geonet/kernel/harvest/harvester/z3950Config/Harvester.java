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
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.IHarvester;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

//=============================================================================

class Harvester implements IHarvester<HarvestResult>
{
    private final AtomicBoolean cancelMonitor;
    //--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public Harvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, Z3950ConfigParams params)
	{
        this.cancelMonitor = cancelMonitor;
		this.log    = log;
		this.params = params;
		this.context = context;
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	public HarvestResult harvest(Logger log) throws Exception
	{
	    this.log = log;

        XmlRequest req = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest(params.host, Integer.valueOf(params.port));

		Lib.net.setupProxy(context, req);

		//--- perform all searches

		Set<RecordInfo> records = new HashSet<RecordInfo>();

		for(Search s : params.getSearches()) {
            if (cancelMonitor.get()) {
                return new HarvestResult();
            }

            records.addAll(search(req, s));
        }

		if (params.isSearchEmpty())
			records.addAll(search(req, Search.createEmptySearch()));

		log.info("Total records processed in all searches :"+ records.size());

		//--- config local node

		Z3950Config  configer = new Z3950Config(cancelMonitor, log, context, req, params);
		HarvestResult result  = configer.config(records);

		return result;
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
		request.setAddress(context.getBaseUrl() +"/"+ Jeeves.Prefix.SERVICE +"/en/" + Geonet.Service.XML_SEARCH);

		try
		{
			log.info("Searching on : "+ params.getName());
			Element response = request.execute(s.createRequest());
            if(log.isDebugEnabled()) log.debug("Search results:\n"+ Xml.getString(response));

			return response;
		}
		catch(Exception e)
		{
            HarvestError error = new HarvestError(e, log);
            error.setDescription("Raised exception when searching : "+ e);
            this.errors.add(error);
            error.printLog(log);
			throw new OperationAbortedEx("Raised exception when searching", e);
		}
	}

    public List<HarvestError> getErrors() {
        return errors;
    }

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private Logger         log;
	private Z3950ConfigParams   params;
	private ServiceContext      context;
   /**
     * Contains a list of accumulated errors during the executing of this harvest.
     */
    private List<HarvestError> errors = new LinkedList<HarvestError>();
}

//=============================================================================


