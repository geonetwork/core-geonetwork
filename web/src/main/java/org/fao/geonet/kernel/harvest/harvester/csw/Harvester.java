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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.OperationAbortedEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import jeeves.utils.XmlRequest;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.ConstraintLanguage;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.CswOperation;
import org.fao.geonet.csw.common.CswServer;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.TypeName;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.requests.CatalogRequest;
import org.fao.geonet.csw.common.requests.GetRecordsRequest;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;
import org.jdom.xpath.XPath;

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

	/**
	 * Does CSW GetCapabilities request
	 * and check that operations needed for harvesting
	 * (ie. GetRecords and GetRecordById)
	 * are available in remote node.
	 */
	private CswServer retrieveCapabilities(Logger log) throws Exception {		
		if (!Lib.net.isUrlValid(params.capabUrl))
			throw new BadParameterEx("Capabilities URL", params.capabUrl);

		XmlRequest req;
		// Support both full GetCapbilities URL or CSW entry point
		if (params.capabUrl.contains("GetCapabilities")) {
			req = new XmlRequest(new URL(params.capabUrl));
		} else {
			req = new XmlRequest(new URL(params.capabUrl + (params.capabUrl.contains("?") ? "&" : "?") + GETCAPABILITIES_PARAMETERS));
		}

		Lib.net.setupProxy(context, req);

		if (params.useAccount)
			req.setCredentials(params.username, params.password);
		
		Element capabil = req.execute();

        if(log.isDebugEnabled())
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
		CswOperation oper = server.getOperation(name);

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

	/**
	 * Does CSW GetRecordsRequest.
	 */
	private Set<RecordInfo> search(CswServer server, Search s) throws Exception
	{
		int start =  1;
		
		GetRecordsRequest request = new GetRecordsRequest(context);

		request.setResultType(ResultType.RESULTS);
		//request.setOutputSchema(OutputSchema.OGC_CORE);	// Use default value
		request.setElementSetName(ElementSetName.SUMMARY);
		request.setMaxRecords(GETRECORDS_NUMBER_OF_RESULTS_PER_PAGE +"");

		CswOperation oper = server.getOperation(CswServer.GET_RECORDS);

        // Use the preferred HTTP method and check one exist.

        configRequest(request, oper, server, s, PREFERRED_HTTP_METHOD);

        if (params.useAccount)
            request.setCredentials(params.username, params.password);

        // Simple fallback mechanism. Try search with PREFERRED_HTTP_METHOD method, if fails change it
        try {
            request.setStartPosition(start +"");
		    doSearch(request, start, 1);
        } catch(Exception ex) {
            if(log.isDebugEnabled()) {
                log.debug(ex.getMessage());
                log.debug("Changing to CSW harvester to use " + (PREFERRED_HTTP_METHOD.equals("GET")?"POST":"GET"));
            }

            configRequest(request, oper, server, s, PREFERRED_HTTP_METHOD.equals("GET")?"POST":"GET");
        }

		Set<RecordInfo> records = new HashSet<RecordInfo>();

		while (true)
		{
			request.setStartPosition(start +"");
			Element response = doSearch(request, start, GETRECORDS_NUMBER_OF_RESULTS_PER_PAGE);
            if(log.isDebugEnabled())
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
				RecordInfo recInfo= getRecordInfo((Element)record.clone());

				if (recInfo != null)
					records.add(recInfo);

				counter++;
			}

			//--- check to see if we have to perform other searches

			int recCount = getRecordCount(results);

            if(log.isDebugEnabled()) {
                log.debug("Records declared in response : "+ recCount);
			    log.debug("Records found in response    : "+ counter);
            }

			if (start+GETRECORDS_NUMBER_OF_RESULTS_PER_PAGE > recCount)
				break;

			start += GETRECORDS_NUMBER_OF_RESULTS_PER_PAGE;
		}

		log.info("Records added to result list : "+ records.size());

		return records;
	}

    //---------------------------------------------------------------------------

    /**
     * Configs the harvester request
     *
     * @param request
     * @param oper
     * @param server
     * @param s
     * @param preferredMethod
     * @throws Exception
     */
    private void configRequest(GetRecordsRequest request, CswOperation oper, CswServer server, Search s, String preferredMethod)
            throws Exception {
        if (oper.getUrl == null && oper.postUrl == null) {
            throw new OperationAbortedEx("No GET or POST DCP available in this service.");
        }
        
        // Use the preferred HTTP method and check one exist.
		if (oper.getUrl != null && preferredMethod.equals("GET") && oper.constraintLanguage.contains("cql_text")) {
			request.setUrl(oper.getUrl);
            request.setServerVersion(server.getPreferredServerVersion());
            request.setOutputSchema(oper.preferredOutputSchema);
			request.setConstraintLanguage(ConstraintLanguage.CQL);
			request.setConstraintLangVersion(CONSTRAINT_LANGUAGE_VERSION);
			request.setConstraint(getCqlConstraint(s));
			request.setMethod(CatalogRequest.Method.GET);
            for(String typeName: oper.typeNamesList) {
                request.addTypeName(TypeName.getTypeName(typeName));
            }
            request.setOutputFormat(oper.preferredOutputFormat) ;

		} else if (oper.postUrl != null && preferredMethod.equals("POST") && oper.constraintLanguage.contains("filter")) {
			request.setUrl(oper.postUrl);
            request.setServerVersion(server.getPreferredServerVersion());
            request.setOutputSchema(oper.preferredOutputSchema);
			request.setConstraintLanguage(ConstraintLanguage.FILTER);
			request.setConstraintLangVersion(CONSTRAINT_LANGUAGE_VERSION);
			request.setConstraint(getFilterConstraint(s));
			request.setMethod(CatalogRequest.Method.POST);
            for(String typeName: oper.typeNamesList) {
                request.addTypeName(TypeName.getTypeName(typeName));
            }
            request.setOutputFormat(oper.preferredOutputFormat) ;

		} else {
		    if (oper.getUrl != null && oper.constraintLanguage.contains("cql_text")) {
                request.setUrl(oper.getUrl);
                request.setServerVersion(server.getPreferredServerVersion());
                request.setOutputSchema(oper.preferredOutputSchema);
                request.setConstraintLanguage(ConstraintLanguage.CQL);
                request.setConstraintLangVersion(CONSTRAINT_LANGUAGE_VERSION);
                request.setConstraint(getCqlConstraint(s));
                request.setMethod(CatalogRequest.Method.GET);
                for(String typeName: oper.typeNamesList) {
                    request.addTypeName(TypeName.getTypeName(typeName));
                }
                request.setOutputFormat(oper.preferredOutputFormat) ;

            } else if (oper.postUrl != null && oper.constraintLanguage.contains("filter")) {
				request.setUrl(oper.postUrl);
                request.setServerVersion(server.getPreferredServerVersion());
                request.setOutputSchema(oper.preferredOutputSchema);
				request.setConstraintLanguage(ConstraintLanguage.FILTER);
				request.setConstraintLangVersion(CONSTRAINT_LANGUAGE_VERSION);
				request.setConstraint(getFilterConstraint(s));
				request.setMethod(CatalogRequest.Method.POST);

                for(String typeName: oper.typeNamesList) {
                    request.addTypeName(TypeName.getTypeName(typeName));
                }
                request.setOutputFormat(oper.preferredOutputFormat) ;

			} else {
			    // TODO : add GET+FE and POST+CQL support
			    log.warning("No GET (using CQL) or POST (using FE) DCP available in this service... Trying GET CQL anyway ...");
			    
			    request.setUrl(oper.getUrl);
                request.setServerVersion(server.getPreferredServerVersion());
                request.setOutputSchema(oper.preferredOutputSchema);
                request.setConstraintLanguage(ConstraintLanguage.CQL);
                request.setConstraintLangVersion(CONSTRAINT_LANGUAGE_VERSION);
                request.setConstraint(getCqlConstraint(s));
                request.setMethod(CatalogRequest.Method.GET);
                for(String typeName: oper.typeNamesList) {
                    request.addTypeName(TypeName.getTypeName(typeName));
                }
                request.setOutputFormat(oper.preferredOutputFormat) ;

			}
		}
    }

	//---------------------------------------------------------------------------

	private String getFilterConstraint(Search s)
	{
		//--- collect queriables

		ArrayList<Element> queriables = new ArrayList<Element>();

		//--- make sure that any freetext value has wildcards
		String freeText = s.freeText;
		if (freeText.length() > 0) {
			if (!freeText.contains("%")) freeText = "%"+freeText+"%";
		}

		// FIXME :
		// old GeoNetwork node does not understand AnyText (csw:AnyText instead).
		buildFilterQueryable(queriables, "csw:AnyText", freeText);
		buildFilterQueryable(queriables, "dc:title", s.title);
		buildFilterQueryable(queriables, "dct:abstract", s.abstrac);
		buildFilterQueryable(queriables, "dc:subject", s.subject);
		buildFilterQueryable(queriables, "dc:denominator", s.minscale, "PropertyIsGreaterThanOrEqualTo");
		buildFilterQueryable(queriables, "dc:denominator", s.maxscale, "PropertyIsLessThanOrEqualTo");

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
	private void buildFilterQueryable(List<Element> queryables, String name, String value) {
		if (value.contains("%")) { 
			buildFilterQueryable(queryables, name, value, "PropertyIsLike");
		} else {
			buildFilterQueryable(queryables, name, value, "PropertyIsEqualTo");
		}
	}
	private void buildFilterQueryable(List<Element> queryables, String name, String value, String operator)
	{
		if (value.length() == 0)
			return;

		// add Like operator
		Element prop;
		
		if (operator.equals("PropertyIsLike")) {
			prop = new Element(operator, Csw.NAMESPACE_OGC);
			prop.setAttribute("wildcard", "%");
			prop.setAttribute("singleChar", "_");
			prop.setAttribute("escapeChar", "\\");
		} else {
			prop = new Element(operator, Csw.NAMESPACE_OGC);
		}
		
		Element propName = new Element("PropertyName", Csw.NAMESPACE_OGC);
		Element literal  = new Element("Literal", Csw.NAMESPACE_OGC);
		
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

		buildCqlQueryable(queryables, "csw:AnyText", s.freeText);
		buildCqlQueryable(queryables, "dc:title", s.title);
		buildCqlQueryable(queryables, "dct:abstract", s.abstrac);
		buildCqlQueryable(queryables, "dc:subject", s.subject);
		buildCqlQueryable(queryables, "dct:denominator", s.minscale, ">=");
		buildCqlQueryable(queryables, "dct:denominator", s.maxscale, "<=");
		
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

	/**
	 * Build CQL from user entry. If parameter value
	 * contains '%', then the like operator is used.
	 */
	private void buildCqlQueryable(List<String> queryables, String name, String value)
	{
		if (value.length() != 0) {
			if (value.contains("%"))
				buildCqlQueryable(queryables, name, value, "like");
			else
				buildCqlQueryable(queryables, name, value, "=");
		}
	}
	private void buildCqlQueryable(List<String> queryables, String name, String value, String operator)
	{
		if (value.length() != 0)
			queryables.add(name +" " + operator + " '"+ value +"'");
	}
	//---------------------------------------------------------------------------

	private Element doSearch(CatalogRequest request, int start, int max) throws Exception
	{
		try
		{
			log.info("Searching on : "+ params.name +" ("+ start +".."+ (start + max) +")");
			Element response = request.execute();
            if(log.isDebugEnabled()) {
                log.debug("Sent request "+request.getSentData());
                log.debug("Search results:\n"+Xml.getString(response));
            }

			return response;
		}
		catch(Exception e)
		{
			log.warning("Raised exception when searching : "+ e);
			throw new OperationAbortedEx("Raised exception when searching: " + e.getMessage(), e);
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
        if(log.isDebugEnabled())
            log.debug("getRecordInfo (name): " + name);

		// get schema
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dm = gc.getDataManager();

		// get uuid and date modified
		try {
			String schema = dm.autodetectSchema(record);
            if(log.isDebugEnabled())
                log.debug("getRecordInfo (schema): " + schema);

			String identif  = dm.extractUUID(schema, record); 
			if (identif.length() == 0) {
      	log.warning("Record doesn't have a uuid : "+ name);
				return null; // skip this one
			}
			
			String modified = dm.extractDateModified(schema, record); 
			if (modified.length() == 0) modified = null;
            if(log.isDebugEnabled())
                log.debug("getRecordInfo: adding "+identif+" with modification date "+modified);
      return new RecordInfo(identif, modified);
		} catch (Exception e) {
      log.warning("Skipped record not in supported format : "+ name);
			e.printStackTrace();
    }

		// we get here if we didn't recognize the schema and/or couldn't get the 
		// UUID or date modified
		return null;

	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------
	// FIXME : Currently switch from POST to GET for testing mainly.
	public static final String PREFERRED_HTTP_METHOD = CatalogRequest.Method.GET.toString();
	private static int GETRECORDS_NUMBER_OF_RESULTS_PER_PAGE = 20;
	private static String CONSTRAINT_LANGUAGE_VERSION = "1.1.0";
	private static String GETCAPABILITIES_PARAMETERS = "SERVICE=CSW&REQUEST=GetCapabilities&VERSION=2.0.2";
	private Logger         log;
	private Dbms           dbms;
	private CswParams      params;
	private ServiceContext context;
}

//=============================================================================


