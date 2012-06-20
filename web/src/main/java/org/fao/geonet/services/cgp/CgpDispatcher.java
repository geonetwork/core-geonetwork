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

package org.fao.geonet.services.cgp;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Logger;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.services.gm03.ISO19139CHEtoGM03small;
import org.fao.geonet.services.gm03.ISO19139CHEtoGM03Base;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Geocat.ch Catalog Gateway Protocol (CGP) SOAP server.
 */
public class CgpDispatcher implements Service
{
	public static final Namespace NAMESPACE_ENV = Namespace.getNamespace("env", "http://schemas.xmlsoap.org/soap/envelope/");
	public static final Namespace NAMESPACE_GCH = Namespace.getNamespace("gch", "http://www.geocat.ch/2003/05/gateway/header");
	public static final Namespace NAMESPACE_GCQ = Namespace.getNamespace("gcq", "http://www.geocat.ch/2003/05/gateway/query");
	public static final Namespace NAMESPACE_GM03SMALL = Namespace.getNamespace("gm03s", "http://www.geocat.ch/2003/05/gateway/GM03Small");
	public static final String MD_ATTR_CATEGORY = "/MD_Metadata/identificationInfo/topicCategory";

	private File xsl19139CHEtoGM03;
	private File xsdGM03;
	private File xsd19139CHE;
	private String appPath;

	public void init(String appPath, ServiceConfig params) throws Exception
	{
		this.appPath = appPath;

		xsl19139CHEtoGM03 = initFile(params.getValue("xsl19139CHEtoGM03"));
		xsdGM03 = initFile(params.getValue("xsdGM03"));
		xsd19139CHE = initFile(params.getValue("xsd19139CHE"));
	}

	public File initFile(String path)
	{
		File file = new File(path);
		if (!file.isAbsolute())
		{
			file = new File(appPath + path);
		}

		if (!file.exists() || !file.canRead())
		{
			throw new IllegalArgumentException("Cannot find or read file: " + file.getAbsolutePath());
		}

		return file;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element soapReqElm, ServiceContext context) throws Exception
	{

		SoapMessage reqMsg = new SoapMessage(soapReqElm);
		SoapMessage rspMsg = new SoapMessage();

		try
		{

			Element reqElm = (Element) reqMsg.getBody().getChild("catalogGatewayRequest", NAMESPACE_GCQ).getChildren().get(0);
			String reqName = reqElm.getName();

			// Dispatch basid on request type
			if (reqName.equals("queryRequest"))
			{
				// Query (ala "CSW getRecords") request
				doQueryReq(reqMsg, rspMsg, reqElm, context);
			} else if (reqName.equals("presentationRequest"))
			{
				// Presentation (ala "CSW getRecord")
				doPresentationReq(reqMsg, rspMsg, reqElm);
			} else
			{
				// Unknown request
				rspMsg.setFault("Client", "Invalid CGP request: " + reqName);
			}

//			String s = Xml.getString(rspMsg.getEnvelope());

//			String s1 = s;

		} catch (Throwable t)
		{
			// Set fault, do not rethrow (until underlying Jeeves engine generates SOAP
			// Fault Envelope.
			t.printStackTrace();
			rspMsg.setFault("Server", "Unexpected server error: " + t);
		}

		// Always return valid SOAP Envelope
		return rspMsg.getEnvelope();
	}


	public void doPresentationReq(SoapMessage reqMsg, SoapMessage rspMsg, Element soapReqElm) throws Exception
	{
		rspMsg.setFault("Client", "Sorry presentationReq is not yet supported.");
	}

	public void doQueryReq(SoapMessage reqMsg, SoapMessage rspMsg, Element reqElm, ServiceContext context) throws Exception
	{

		// Unpack request parms/response format
		Element criteriaElm = reqElm.getChild("criteria", NAMESPACE_GCQ);

		// Do these checks since we support only a limited subset of the entire CGP

		// No concatenated expressions
		if (criteriaElm.getChildren().size() != 1)
		{
			rspMsg.setFault("Client", "Sorry only single expression is supported.");
			return;
		}

		// Only one category parameter query
		Element expressionElm = criteriaElm.getChild("expression", NAMESPACE_GCQ);
		String attribute = expressionElm.getChildText("attribute", NAMESPACE_GCQ);
		if (!MD_ATTR_CATEGORY.equals(attribute))
		{
			rspMsg.setFault("Client", "Sorry only topicCategory attribute is supported.");
			return;
		}

		
			
//		String operator = expressionElm.getChildText("operator", NAMESPACE_GCQ);
//		if (!"eq".equals(operator))
//		{
//			rspMsg.setFault("Client", "Sorry only eq operator is supported.");
//			return;
//		}
//		HACK: like operator could be supported as lucene search are quite different from 
//		CGP search.
		
		String value = expressionElm.getChildText("value", NAMESPACE_GCQ);
		if (value == null)
		{
			rspMsg.setFault("Client", "No value found for attribute.");
			return;
		}

		Element formatElm = reqElm.getChild("format", NAMESPACE_GCQ);
		String profile = formatElm.getChildText("profile", NAMESPACE_GCQ);
		if (!"GM03Small".equals(profile))
		{
			rspMsg.setFault("Client", "Sorry only GM03Small profile is supported.");
			return;
		}

		// Like operator is not supported, but topicCategory is an enumeration. 
		// Using equal is same as like operator. GatewayTester is using like
		// operator for codelist search.
		String operator = expressionElm.getChildText("operator", NAMESPACE_GCQ);
		if ("like".equals(operator))
			value = value.replace("%", "");

		// Do the Query
		List<Element> iso19139Elms = searchByCategory(value, context);

		//  Transform ISO 19139 result to GM03Small
		List<Element> gm03Elms = iso19139CHEtoGM03(iso19139Elms);

		// Prepare full response
		rspMsg.setHeader((Element) reqMsg.getHeader().clone());
		Element gwReqElm = new Element("catalogGatewayRequest", NAMESPACE_GCQ);
		Element queryResultElm = new Element("queryResult", NAMESPACE_GCQ);

		queryResultElm.addContent(gm03Elms);
		gwReqElm.addContent(queryResultElm);
		rspMsg.setBodyContent(gwReqElm);
	}

	private List<Element> queryByCategory(String category, ServiceContext context) throws Exception
	{

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		String query = "SELECT id FROM Categories WHERE name=?";
		List<Element> list = dbms.select(query, category).getChildren();
		if (list.size() != 1)
		{
			return new ArrayList(0);
		}

		String categoryId = list.get(0).getChildText("id");
		if (categoryId == null)
		{
			return new ArrayList(0);
		}

		query = "SELECT Metadata.data FROM Metadata INNER JOIN MetadataCateg ON Metadata.id=MetadataCateg.metadataId WHERE MetadataCateg.categoryId=?";
		List<Element> mdStringElms = dbms.select(query, categoryId).getChildren();

		List<Element> mdXMLElms = new ArrayList<Element>(mdStringElms.size());
		Element mdXMLElm;
		String iso19139Str;
		for (Element mdStringElm : mdStringElms)
		{
			iso19139Str = mdStringElm.getChildText("data");
			mdXMLElm = Xml.loadString(iso19139Str, false);
			mdXMLElms.add(mdXMLElm);
		}

		return mdXMLElms;
	}

	/**
	 * Search MD by category.
	 *
	 * @param category che category (not iso) e.g. "biota"
	 * @param context GeonetContext
	 * @return list of GM03Small elements
	 * @throws Exception
	 */
	private List<Element> searchByCategory(String category, ServiceContext context) throws Exception
	{

		Logger logger = context.getLogger();

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dataMan = gc.getDataManager();

		// Setup Lucene search
		SearchManager searchMan = gc.getSearchmanager();
		MetaSearcher searcher = searchMan.newSearcher(SearchManager.LUCENE, Geonet.File.SEARCH_LUCENE);
		Element params = new Element(Jeeves.Elem.REQUEST)
				.addContent(new Element("_schema").setText("iso19139.che"))
				.addContent(new Element("topicCat").setText(category))
				.addContent(new Element("maxSummaryKeys").setText("99999"));

		// Execute search
		searcher.search(context, params, null);
		logger.info("searchByCategory; records found : " + searcher.getSize());

		// Get MD summary info elements (for getting id's)
		Element presentRequest = new Element("request");
		presentRequest.addContent(new Element("fast").setText("true"));
		presentRequest.addContent(new Element("from").setText("1"));
		presentRequest.addContent(new Element("to").setText(searcher.getSize()+ ""));

		List<Element> mdInfoElms = searcher.present(context, presentRequest, null).getChildren();
		List<Element> mdElms = new ArrayList<Element>(mdInfoElms.size());

		// Get the actual MD from DB for each info element
		for (Element mdInfoElm: mdInfoElms) {
			Element info = mdInfoElm.getChild("info", Edit.NAMESPACE);
			if (info == null) {
				// First elm may be summary
				continue;
			}

			// Info has id
			String id = info.getChildText("id");

			// Get the actual MD as iso19139.che
			Element md = dataMan.getMetadata(context, id, false, false, false);

			if (md == null) {
				logger.warning("Cannot get Metadata with id=" + id);
				continue;
			}

			// Remove geonet-specific info elm
			md.removeChild("info", Edit.NAMESPACE);
			mdElms.add(md);
		}

		return mdElms;
	}

	private List<Element> iso19139CHEtoGM03(List<Element> iso19139Elms) throws Exception
	{
		// Setup XSL transform helper
		ISO19139CHEtoGM03Base toGm03 = new ISO19139CHEtoGM03small(null, xsl19139CHEtoGM03.getAbsolutePath());

		// We need w3c XML DOM Documents for XSL transform
		org.w3c.dom.Document domIn, domOut;

		// JDOM Helpers
		DOMBuilder builder = new DOMBuilder();
		DOMOutputter outputter = new DOMOutputter();

		// Walk through List and transform each Element
		List<Element> gm03Elms = new ArrayList(iso19139Elms.size());
		Element gm03Element;
		for (Element iso19139Elm : iso19139Elms)
		{
			// Convert JDOM to W3C DOM
			iso19139Elm = (Element) iso19139Elm.clone();

//			String s = Xml.getString(iso19139Elm);

			// Convert to GM03Small (CGP specific profile)
			org.jdom.Document doc = new org.jdom.Document(iso19139Elm);
			domIn = outputter.output(doc);

			// Do XSL transform from ISO 19139.che to GM03Small
			domOut = toGm03.convert(domIn);

			// Convert W3C DOM to JDOM
			gm03Element = builder.build(domOut).getRootElement();

			// Add to result
			gm03Elms.add((Element) gm03Element.detach());  
		}

		// Final result
		return gm03Elms;
	}
}

//=============================================================================

