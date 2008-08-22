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

package org.fao.geonet.kernel.csw.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.ConstraintLanguage;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.OutputSchema;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.TypeName;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.MissingParameterValueEx;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.kernel.csw.CatalogService;
import org.fao.geonet.kernel.csw.services.getrecords.SearchController;
import org.fao.geonet.kernel.csw.services.getrecords.SortField;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLParser;

//=============================================================================

public class GetRecords extends AbstractOperation implements CatalogService
{
    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    public GetRecords() {}

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public String getName() { return "GetRecords"; }

    //---------------------------------------------------------------------------

    public Element execute(Element request, ServiceContext context) throws CatalogException
    {
	checkService(request);
	checkVersion(request);
	checkOutputFormat(request);

	String timeStamp = new ISODate().toString();

	int startPos   = getStartPosition(request);
	int maxRecords = getMaxRecords(request);
	int hopCount   = getHopCount(request);

	Element query = request.getChild("Query", Csw.NAMESPACE_CSW);

	ResultType      resultType  = ResultType  .parse(request.getAttributeValue("resultType"));;
	OutputSchema    outSchema   = OutputSchema.parse(request.getAttributeValue("outputSchema"));
	Set<String>     elemNames   = getElementNames(query);
	ElementSetName  setName = ElementSetName.FULL;

	// If any element names are specified, it's an ad hoc query and overrides the
	// element set name default.  In that case, we set setName to FULL instead of
	// SUMMARY so that we can retrieve a CSW:Record and trim out the elements that
	// aren't in the elemNames set.
	if ((elemNames == null) || (elemNames.size() == 0))
	    setName = getElementSetName(query , ElementSetName.SUMMARY);

	Set<TypeName>   typeNames   = getTypeNames(request);
	Element         filterExpr  = getFilterExpression(request, context);
	List<SortField> sortFields  = getSortFields(request);

	Element response;

	if (resultType == ResultType.VALIDATE)
	    {
		String schema = context.getAppPath() + Geonet.Path.VALIDATION + "csw/2.0.2/csw-2.0.2.xsd";
		System.out.println("Validating against " + schema);

		try
		    {
			Xml.validate(schema, request);
		    }

		catch (Exception e)
		    {
			System.out.println("Request validation failed");
			throw new NoApplicableCodeEx("Request failed validation:" + e.toString());
		    }

		response = new Element("Acknowledgement", Csw.NAMESPACE_CSW);
		response.setAttribute("timeStamp",timeStamp);

		Element echoedRequest = new Element("EchoedRequest", Csw.NAMESPACE_CSW);
		echoedRequest.addContent(request);

		response.addContent(echoedRequest);
	    }
	else
	    {

		SearchController sc = new SearchController();
		
		response = new Element(getName() +"Response", Csw.NAMESPACE_CSW);

		Element status = new Element("SearchStatus", Csw.NAMESPACE_CSW);
		status.setAttribute("timestamp",timeStamp);

		response.addContent(status);
		response.addContent(sc.search(context, startPos, maxRecords, hopCount, resultType,
					      outSchema, setName, typeNames, filterExpr, sortFields,
					      elemNames));
	    }

	return response;
    }

    //---------------------------------------------------------------------------

    public Element adaptGetRequest(Map<String, String> params) throws CatalogException
    {
	String service      = params.get("service");
	String version      = params.get("version");
	String resultType   = params.get("resulttype");
	String outputFormat = params.get("outputformat");
	String outputSchema = params.get("outputschema");
	String startPosition= params.get("startposition");
	String maxRecords   = params.get("maxrecords");
	String hopCount     = params.get("hopcount");
	String distribSearch= params.get("distributedsearch");
	String typeNames    = params.get("typenames");
	String elemSetName  = params.get("elementsetname");
	String elemName     = params.get("elementname");
	String constraint   = params.get("constraint");
	String constrLang   = params.get("constraintlanguage");
	String constrLangVer= params.get("constraint_language_version");
	String sortby       = params.get("sortby");

	//--- build POST request

	Element request = new Element(getName(), Csw.NAMESPACE_CSW);

	setAttrib(request, "service",       service);
	setAttrib(request, "version",       version);
	setAttrib(request, "resultType",    resultType);
	setAttrib(request, "outputFormat",  outputFormat);
	setAttrib(request, "outputSchema",  outputSchema);
	setAttrib(request, "startPosition", startPosition);
	setAttrib(request, "maxRecords",    maxRecords);

	if (distribSearch != null && distribSearch.equals("true"))
	    {
		Element ds = new Element("DistributedSearch", Csw.NAMESPACE_CSW);
		ds.setText("TRUE");

		if (hopCount != null)
		    ds.setAttribute("hopCount", hopCount);

		request.addContent(ds);
	    }

	//------------------------------------------------------------------------
	//--- build query element

	Element query = new Element("Query", Csw.NAMESPACE_CSW);
	request.addContent(query);

	if (typeNames != null)
	    setAttrib(query, "typeNames", typeNames.replace(',',' '));

	//--- these 2 are in mutual exclusion

	addElement(query, "ElementSetName", elemSetName);
	fill(query, "ElementName", elemName);

	//------------------------------------------------------------------------
	//--- handle constraint

	ConstraintLanguage language = ConstraintLanguage.parse(constrLang);

	if (constraint != null)
	    {
		Element constr = new Element("Constraint", Csw.NAMESPACE_CSW);
		query.addContent(constr);

		if (language == ConstraintLanguage.CQL)
		    addElement(constr, "CqlText", constraint);
		else
		    try
			{
			    constr.addContent(Xml.loadString(constraint, false));
			}
		    catch (Exception e)
			{
			    e.printStackTrace();
			    throw new NoApplicableCodeEx("Constraint is not a valid xml");
			}

		setAttrib(constr, "version", constrLangVer);
	    }

	//------------------------------------------------------------------------
	//--- handle sortby

	if (sortby != null)
	    {
		Element sortBy = new Element("SortBy", Csw.NAMESPACE_OGC);
		query.addContent(sortBy);

		StringTokenizer st = new StringTokenizer(sortby, ",");

		while (st.hasMoreTokens())
		    {
			String  sortInfo = st.nextToken();
			String  field    = sortInfo.substring(0, sortInfo.length() -2);
			boolean ascen    = sortInfo.endsWith(":A");

			Element sortProp = new Element("SortProperty", Csw.NAMESPACE_OGC);
			sortBy.addContent(sortProp);

			Element propName  = new Element("PropertyName", Csw.NAMESPACE_OGC).setText(field);
			Element sortOrder = new Element("SortOrder",    Csw.NAMESPACE_OGC).setText(ascen ? "ASC" : "DESC");

			sortProp.addContent(propName);
			sortProp.addContent(sortOrder);
		    }
	    }

	return request;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    private void checkOutputFormat(Element request) throws InvalidParameterValueEx
    {
	String format = request.getAttributeValue("outputFormat");

	if (format == null)
	    return;

	if (!format.equals("application/xml"))
	    throw new InvalidParameterValueEx("outputFormat", format);
    }

    //---------------------------------------------------------------------------

    private int getStartPosition(Element request) throws InvalidParameterValueEx
    {
	String start = request.getAttributeValue("startPosition");

	if (start == null)
	    return 1;

	try
	    {
		int value = Integer.parseInt(start);

		if (value >= 1)
		    return value;
	    }
	catch (NumberFormatException e) {}

	throw new InvalidParameterValueEx("startPosition", start);
    }

    //---------------------------------------------------------------------------

    private int getMaxRecords(Element request) throws InvalidParameterValueEx
    {
	String max = request.getAttributeValue("maxRecords");

	if (max == null)
	    return 10;

	try
	    {
		int value = Integer.parseInt(max);

		if (value >= 1)
		    return value;
	    }
	catch (NumberFormatException e) {}

	throw new InvalidParameterValueEx("maxRecords", max);
    }

    //---------------------------------------------------------------------------
    //--- a return value >= 0 means that a distributed search was requested
    //--- otherwise the method returns -1

    private int getHopCount(Element request) throws InvalidParameterValueEx
    {
	Element ds = request.getChild("DistributedSearch", Csw.NAMESPACE_CSW);

	if (ds == null)
	    return -1;

	String hopCount = ds.getAttributeValue("hopCount");

	if (hopCount == null)
	    return 2;

	try
	    {
		int value = Integer.parseInt(hopCount);

		if (value >= 0)
		    return value;
	    }
	catch (NumberFormatException e) {}

	throw new InvalidParameterValueEx("hopCount", hopCount);
    }

    //---------------------------------------------------------------------------

    private Set<TypeName> getTypeNames(Element request) throws CatalogException
    {
	Element query = request.getChild("Query", Csw.NAMESPACE_CSW);
	return TypeName.parse(query.getAttributeValue("typeNames"));
    }

    //---------------------------------------------------------------------------

    private Element getFilterExpression(Element request, ServiceContext context) throws CatalogException
    {
	Element query  = request.getChild("Query",      Csw.NAMESPACE_CSW);
	Element constr = query  .getChild("Constraint", Csw.NAMESPACE_CSW);

	if (constr == null)
	    return null;
	Element filter = constr.getChild("Filter",  Csw.NAMESPACE_OGC);
	Element cql    = constr.getChild("CqlText", Csw.NAMESPACE_CSW);

	if (filter == null && cql == null)
	    throw new NoApplicableCodeEx("Missing filter expression or cql query");

	String version = constr.getAttributeValue("version");

	if (version == null)
	    throw new MissingParameterValueEx("version");

	if (filter != null && !version.equals(Csw.FILTER_VERSION))
	    throw new InvalidParameterValueEx("version", version);

	return (filter != null) ? filter : convertCQL(cql.getText(), context);
    }

    //---------------------------------------------------------------------------

    private List<SortField> getSortFields(Element request)
    {
	ArrayList<SortField> al = new ArrayList<SortField>();

	Element query = request.getChild("Query", Csw.NAMESPACE_CSW);

	if (query == null)
	    return al;

	Element sortBy = query.getChild("SortBy", Csw.NAMESPACE_OGC);

	if (sortBy == null)
	    return al;

	List list = sortBy.getChildren();

	for(int i=0; i<list.size(); i++)
	    {
		Element el = (Element) list.get(i);

		String field = el.getChildText("PropertyName", Csw.NAMESPACE_OGC);
		String order = el.getChildText("SortOrder",    Csw.NAMESPACE_OGC);

		al.add(new SortField(field, "DESC".equals(order)));
	    }

	return al;
    }

    //---------------------------------------------------------------------------

    private Element convertCQL(String cql, ServiceContext context) throws CatalogException
    {
	String xmlCql = getCqlXmlString(cql, context);
	context.debug("Received CQL:\n"+ xmlCql);

	Element xml        = getCqlXmlElement(xmlCql, context);
	String  styleSheet = context.getAppPath() + Geonet.Path.CSW + Geonet.File.CQL_TO_FILTER;

	Element filter = getFilter(xml, styleSheet, context);
	context.debug("Transformed CQL gives the following filter:\n"+Xml.getString(filter));

	return filter;
    }

    //---------------------------------------------------------------------------

    private String getCqlXmlString(String cql, ServiceContext context) throws InvalidParameterValueEx
    {
	try
	    {
		CQLParser parser = new CQLParser();
		CQLNode   root   = parser.parse(cql);

		return root.toXCQL(0);
	    }
	catch (Exception e)
	    {
		context.error("Error parsing CQL : "+ e);
		context.error("  (C) CQL is :\n"+ cql);

		throw new InvalidParameterValueEx("CqlText", cql);
	    }
    }

    //---------------------------------------------------------------------------

    private Element getCqlXmlElement(String cqlXml, ServiceContext context) throws NoApplicableCodeEx
    {
	try
	    {
		return Xml.loadString(cqlXml, false);
	    }
	catch (Exception e)
	    {
		context.error("Bad CQL XML : "+ e);
		context.error("  (C) CQL XML is :\n"+ cqlXml);

		throw new NoApplicableCodeEx("Bad CQL XML : "+ cqlXml);
	    }
    }

    //---------------------------------------------------------------------------

    private Element getFilter(Element cql, String styleSheet, ServiceContext context) throws NoApplicableCodeEx
    {
	try
	    {
		return Xml.transform(cql, styleSheet);
	    }
	catch (Exception e)
	    {
		context.error("Error during CQL to Filter conversion : "+ e);
		context.error("  (C) StackTrace\n"+ Util.getStackTrace(e));

		throw new NoApplicableCodeEx("Error during CQL to Filter conversion : "+ e);
	    }
    }

    //---------------------------------------------------------------------------

    private Set<String> getElementNames(Element query)
    {
	if (query == null)
	    return null;

	Iterator i = query.getChildren("ElementName", query.getNamespace()).iterator();

	if (!i.hasNext())
	    return null;

	HashSet<String> hs = new HashSet<String>();

	while (i.hasNext())
	    {
		Element elem = (Element) i.next();

		hs.add(elem.getText());
	    }

	return hs;
    }
}

//=============================================================================


