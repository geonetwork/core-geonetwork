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

package org.fao.geonet.kernel.csw.services.getrecords;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;

import org.apache.lucene.search.Sort;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.OutputSchema;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.TypeName;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.jdom.Element;

//=============================================================================

public class SearchController
{
    
	private final CatalogSearcher _searcher;
    public SearchController(File summaryConfig)
    {
        _searcher = new CatalogSearcher(summaryConfig);
    }
	
	//---------------------------------------------------------------------------
    //---
    //--- Single public method to perform the general search tasks
    //---
    //---------------------------------------------------------------------------

    /**
	 * Perform the general search tasks
	 */
    public Pair<Element, Element> search(ServiceContext context, int startPos, int maxRecords, int hopCount,
			  ResultType resultType, OutputSchema outSchema, ElementSetName setName,
			  Set<TypeName> typeNames, Element filterExpr, String filterVersion, Sort sort,
			  Set<String> elemNames) throws CatalogException
    {
	Element results = new Element("SearchResults", Csw.NAMESPACE_CSW);

	Pair<Element, List<ResultItem>> summaryAndSearchResults = _searcher.search(context, filterExpr, filterVersion, typeNames, sort, resultType, maxRecords);

	int counter = 0;

	List<ResultItem> resultsList = summaryAndSearchResults.two();
	if (resultType == ResultType.RESULTS || resultType == ResultType.RESULTS_WITH_SUMMARY)
	    for (int i=startPos; (i<startPos+maxRecords) && (i<=resultsList.size()); i++)
		{
		    counter++;

		    String  id = resultsList.get(i -1).getID();
		    Element md = retrieveMetadata(context, id, setName, outSchema, elemNames, resultType);

		    if (md == null)
			context.warning("SearchController : Metadata not found or invalid schema : "+ id);
		    else
			results.addContent(md);
		}

	results.setAttribute("numberOfRecordsMatched",  resultsList.size() +"");
	results.setAttribute("numberOfRecordsReturned", counter +"");
	results.setAttribute("elementSet",              setName.toString());

	if (resultsList.size() > counter)
	    {
		results.setAttribute("nextRecord", counter + startPos + "");
	    } 
	else 
	    {
		results.setAttribute("nextRecord","0");
	    }
	
	Element summary = summaryAndSearchResults.one();
	return Pair.read(summary, results);
    }

    //---------------------------------------------------------------------------
    /**
     * Retrieve metadata from the database
     */
    public static Element retrieveMetadata(ServiceContext context, String id,  ElementSetName setName,
				     OutputSchema outSchema, Set<String> elemNames, ResultType resultType)
	throws CatalogException
    {
	try
	    {
		//--- get metadata from DB
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		Element  res = gc.getDataManager().getMetadata(context, id, false); 
		
		if (res==null)
		    return null;

		String schema = res.getChild(Edit.RootChild.INFO, Edit.NAMESPACE)
					.getChildText(Edit.Info.Elem.SCHEMA);

		String FS         = File.separator;
		
		// --- transform iso19115 record to iso19139
		// --- If this occur user should probably migrate the catalogue from iso19115 to iso19139.
		// --- But sometimes you could harvest remote node in iso19115 and make them available through CSW
		if (schema.equals("iso19115")) {
			res = Xml.transform(res, context.getAppPath() + "xsl" + FS
					+ "conversion" + FS + "import" + FS + "ISO19115-to-ISO19139.xsl");
			schema = "iso19139";
		}
		
		//--- skip metadata with wrong schemas

		if (schema.equals("fgdc-std") || schema.equals("dublin-core"))
		    if (outSchema != OutputSchema.OGC_CORE)
			return null;

		//--- apply stylesheet according to setName and schema

		String prefix ; 
		if (outSchema == OutputSchema.OGC_CORE)
			prefix = "ogc";
		else if (outSchema == OutputSchema.ISO_PROFILE)
			prefix = "iso";
		else {
			// FIXME ISO PROFIL : Use declared primeNS in current node.
			prefix = "fra";
			if (!schema.contains("iso19139")){
				// FIXME : should we return null or an exception in that case and which exception
				throw new InvalidParameterValueEx("outputSchema not supported for metadata " + 
						id + " schema.", schema);
			}
		}
		
		if (schema.contains("iso19139"))
			schema = "iso19139";
		
		String schemaDir  = context.getAppPath() +"xml"+ FS +"csw"+ FS +"schemas"+ FS +schema+ FS;
		String styleSheet = schemaDir + prefix +"-"+ setName +".xsl";

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("displayInfo", 
				resultType == ResultType.RESULTS_WITH_SUMMARY ? "true" : "false");
		
		res = Xml.transform(res, styleSheet, params);

		//--- if the client has specified some ElementNames, then we remove the unwanted children

		if (elemNames != null)
		    removeElements(res, elemNames);

		return res;
	    }
	catch (Exception e)
	    {
		context.error("Error while getting metadata with id : "+ id);
		context.error("  (C) StackTrace:\n"+ Util.getStackTrace(e));

		throw new NoApplicableCodeEx("Raised exception while getting metadata :"+ e);
	    }
    }

    //---------------------------------------------------------------------------

    private static void removeElements(Element md, Set<String> elemNames)
    {
	Iterator i=md.getChildren().iterator();

	while (i.hasNext())
	    {
		Element elem = (Element) i.next();

		if (!FieldMapper.match(elem, elemNames))
		    i.remove();
	    }
    }
}

//=============================================================================


