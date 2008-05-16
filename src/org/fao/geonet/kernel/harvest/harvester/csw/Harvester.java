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

package org.fao.geonet.kernel.harvest.harvester.csw;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.OperationAbortedEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import jeeves.utils.XmlRequest;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.Csw.ConstraintLanguage;
import org.fao.geonet.csw.common.Csw.ElementSetName;
import org.fao.geonet.csw.common.Csw.ResultType;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.requests.CatalogRequest;
import org.fao.geonet.csw.common.requests.GetRecordsRequest;
import org.fao.geonet.csw.common.util.CswServer;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;
import org.jdom.Namespace;

//=============================================================================

class Harvester
{
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public Harvester(Logger log, ServiceContext context, Dbms dbms, CswParams params)
	{
		this.log    = log;
		this.context= context;
		this.dbms   = dbms;
		this.params = params;
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public CswResult harvest() throws Exception
	{
		log.info("Retrieving capabilities file for : "+ params.name);

		CswServer server = retrieveCapabilities(log);

		//--- perform all searches

		Set<RecordInfo> records = new HashSet<RecordInfo>();

		for(Search s : params.getSearches())
			records.addAll(search(server, s));

		if (params.isSearchEmpty())
			records.addAll(search(server, Search.createEmptySearch()));

		log.info("Total records processed in all searches :"+ records.size());

		//--- align local node

		Aligner aligner = new Aligner(log, context, dbms, server, params);

		return aligner.align(records);
	}

	//---------------------------------------------------------------------------

	private CswServer retrieveCapabilities(Logger log) throws Exception
	{
		if (!Lib.net.isUrlValid(params.capabUrl))
			throw new BadParameterEx("Capabilities URL", params.capabUrl);

		XmlRequest req = new XmlRequest(new URL(params.capabUrl));

		Lib.net.setupProxy(context, req);

		if (params.useAccount)
			req.setCredentials(params.username, params.password);

		Element capabil = req.execute();

		log.debug("Capabilities:\n"+Xml.getString(capabil));

		if (capabil.getName().equals("ExceptionReport"))
			CatalogException.unmarshal(capabil);

		CswServer server = new CswServer(capabil);

		if (!checkOperation(log, server, "GetRecords"))
			throw new OperationAbortedEx("GetRecords operation not found");

		if (!checkOperation(log, server, "GetRecordById"))
			throw new OperationAbortedEx("GetRecordById operation not found");

		return server;
	}

	//---------------------------------------------------------------------------

	private boolean checkOperation(Logger log, CswServer server, String name)
	{
		CswServer.Operation oper = server.getOperation(name);

		if (oper == null)
		{
			log.warning("Operation not present in capabilities : "+ name);
			return false;
		}

		if (oper.getUrl == null && oper.postUrl == null)
		{
			log.warning("Operation has no GET and POST bindings : "+ name);
			return false;
		}

		return true;
	}

	//---------------------------------------------------------------------------

	private Set<RecordInfo> search(CswServer server, Search s) throws Exception
	{
		int start =  1;
		int max   = 10;

		GetRecordsRequest request = new GetRecordsRequest();

		request.setResultType(ResultType.RESULTS);
		request.setElementSetName(ElementSetName.SUMMARY);
		request.setMaxRecords(max +"");

		CswServer.Operation oper = server.getOperation(CswServer.GET_RECORDS);

		if (oper.postUrl != null)
		{
			request.setUrl(oper.postUrl);
			request.setConstraintLanguage(ConstraintLanguage.FILTER);
			request.setConstraintLangVersion("1.1.0");
			request.setConstraint(getFilterConstraint(s));
			request.setMethod(CatalogRequest.Method.POST);
		}
		else
		{
			request.setUrl(oper.getUrl);
			request.setConstraintLanguage(ConstraintLanguage.CQL);
			request.setConstraintLangVersion("1.0");
			request.setConstraint(getCqlConstraint(s));
			request.setMethod(CatalogRequest.Method.GET);
		}

		if (params.useAccount)
			request.setCredentials(params.username, params.password);

		Set<RecordInfo> records = new HashSet<RecordInfo>();

		while (true)
		{
			request.setStartPosition(start +"");
			log.debug("Request: " + request.toString());			 
			Element response = doSearch(request, start, max);			
			log.debug("Number of child elements in response: " + response.getChildren().size());
			
			Element results  = response.getChild("SearchResults", Csw.NAMESPACE_CSW);
			// heikki: some providers forget to update their CSW namespace to the CSW 2.0.2 specification
			if(results == null) {
				// in that case, try to accommodate them anyway:
				results = response.getChild("SearchResults", Csw.NAMESPACE_CSW_OLD);
				if (results == null) {
					throw new OperationAbortedEx("Missing 'SearchResults'", response);
				}
				else {
					log.warning("Received GetRecords response with incorrect namespace: " + Csw.NAMESPACE_CSW_OLD);
				}
			}

			List list = results.getChildren();
			int counter = 0;

			for (Object e :list)
			{
				Element    record = (Element) e;
				RecordInfo recInfo= getRecordInfo(record);

				if (recInfo != null)
					records.add(recInfo);

				counter++;
			}

			//--- check to see if we have to perform other searches

			int recCount = getRecordCount(results);

			log.debug("Records declared in response : "+ recCount);
			log.debug("Records found in response    : "+ counter);

			if (start+max > recCount)
				break;

			start += max;
		}

		log.info("Records added to result list : "+ records.size());

		return records;
	}

	//---------------------------------------------------------------------------

	private String getFilterConstraint(Search s)
	{
		//--- collect queriables

		ArrayList<Element> queriables = new ArrayList<Element>();

		buildFilterQueryable(queriables, "AnyText",      s.freeText);
		buildFilterQueryable(queriables, "dc:title",     s.title);
		buildFilterQueryable(queriables, "dct:abstract", s.abstrac);
		buildFilterQueryable(queriables, "dc:subject",   s.subject);

		//--- build filter expression

		if (queriables.isEmpty())
			return null;

		Element filter = new Element("Filter", Csw.NAMESPACE_OGC);

		if (queriables.size() == 1)
			filter.addContent(queriables.get(0));
		else
		{
			Element and = new Element("And", Csw.NAMESPACE_OGC);

			for(Element prop : queriables)
				and.addContent(prop);

			filter.addContent(and);
		}

		return Xml.getString(filter);
	}

	//---------------------------------------------------------------------------

	private void buildFilterQueryable(List<Element> queryables, String name, String value)
	{
		if (value.length() == 0)
			return;

		Element prop     = new Element("PropertyIsEqualTo", Csw.NAMESPACE_OGC);
		Element propName = new Element("PropertyName",      Csw.NAMESPACE_OGC);
		Element literal  = new Element("Literal",           Csw.NAMESPACE_OGC);

		propName.setText(name);
		literal .setText(value);

		prop.addContent(propName);
		prop.addContent(literal);

		queryables.add(prop);
	}

	//---------------------------------------------------------------------------

	private String getCqlConstraint(Search s)
	{
		//--- collect queriables

		ArrayList<String> queryables = new ArrayList<String>();

		buildCqlQueryable(queryables, "AnyText",      s.freeText);
		buildCqlQueryable(queryables, "dc:title",     s.title);
		buildCqlQueryable(queryables, "dct:abstract", s.abstrac);
		buildCqlQueryable(queryables, "dc:subject",   s.subject);

		//--- build CQL query

		StringBuffer sb = new StringBuffer();

		for (int i=0; i<queryables.size(); i++)
		{
			sb.append(queryables.get(i));

			if (i < queryables.size() -1)
				sb.append(" AND ");
		}

		return (queryables.size() == 0) ? null : sb.toString();
	}

	//---------------------------------------------------------------------------

	private void buildCqlQueryable(List<String> queryables, String name, String value)
	{
		if (value.length() != 0)
			queryables.add("("+ name +" = "+ value +")");
	}

	//---------------------------------------------------------------------------

	private Element doSearch(CatalogRequest request, int start, int max) throws Exception
	{
		try
		{
			log.info("Searching on : "+ params.name +" ("+ start +".."+ max +")");
			Element response = request.execute();
			log.debug("Search results:\n"+Xml.getString(response));

			return response;
		}
		catch(Exception e)
		{
			log.warning("Raised exception when searching : "+ e);
			throw new OperationAbortedEx("Raised exception when searching", e);
		}
	}

	//---------------------------------------------------------------------------

	private int getRecordCount(Element results) throws OperationAbortedEx
	{
		String numRec = results.getAttributeValue("numberOfRecordsMatched");

		if (numRec == null)
			throw new OperationAbortedEx("Missing 'numberOfRecordsMatched' in 'SearchResults'");

		if (!Lib.type.isInteger(numRec))
			throw new OperationAbortedEx("Bad value for 'numberOfRecordsMatched'", numRec);

		return Integer.parseInt(numRec);
	}

	//---------------------------------------------------------------------------

	private RecordInfo getRecordInfo(Element record)
	{
		String name = record.getName();

		if (!name.equals("SummaryRecord"))
		{
			log.warning("Skipped record not in 'SummaryRecord' format : "+ name);
			return null;
		}

		Namespace dc  = Namespace.getNamespace("http://purl.org/dc/elements/1.1/");
		Namespace dct = Namespace.getNamespace("http://purl.org/dc/terms/");

		String identif  = record.getChildText("identifier", dc);
		String modified = record.getChildText("modified",   dct);

		if (identif == null)
		{
			log.warning("Skipped record with no 'dc:identifier' element : "+ name);
			return null;
		}

		return new RecordInfo(identif, modified);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private Logger         log;
	private Dbms           dbms;
	private CswParams      params;
	private ServiceContext context;
}

//=============================================================================


