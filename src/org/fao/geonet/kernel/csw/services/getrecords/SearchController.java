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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.OutputSchema;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.TypeName;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.jdom.Element;

//=============================================================================

public class SearchController
{
	/**
	 * Perform the general search tasks
	 */
    public Element search(ServiceContext context, int startPos, int maxRecords, int hopCount,
			  ResultType resultType, OutputSchema outSchema, ElementSetName setName,
			  Set<TypeName> typeNames, Element filterExpr, List<SortField> sortFields,
			  Set<String> elemNames) throws CatalogException
    {
	Element results = new Element("SearchResults", Csw.NAMESPACE_CSW);

	CatalogSearcher searcher = new CatalogSearcher();

	List<ResultItem> searchResults = searcher.search(context, filterExpr, typeNames, sortFields);

	int counter = 0;

	if (resultType == ResultType.RESULTS)
	    for (int i=startPos; (i<startPos+maxRecords) && (i<=searchResults.size()); i++)
		{
		    counter++;

		    String  id = searchResults.get(i -1).getID();
		    Element md = retrieveMetadata(context, id, setName, outSchema, elemNames);

		    if (md == null)
			context.warning("SearchController : Metadata not found or invalid schema : "+ id);
		    else
			results.addContent(md);
		}

	results.setAttribute("numberOfRecordsMatched",  searchResults.size() +"");
	results.setAttribute("numberOfRecordsReturned", counter +"");
	results.setAttribute("elementSet",              setName.toString());

	if (searchResults.size() > counter)
	    {
		results.setAttribute("nextRecord", counter + startPos + "");
	    } 
	else 
	    {
		results.setAttribute("nextRecord","0");
	    }

	return results;
    }

    //---------------------------------------------------------------------------
    /**
     * Retrieve metadata from the database
     */
    private Element retrieveMetadata(ServiceContext context, String id,  ElementSetName setName,
				     OutputSchema outSchema, Set<String> elemNames)
	throws CatalogException
    {
	try
	    {
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		//--- get metadata from DB

		Element  res = dbms.select("SELECT schemaId, data FROM Metadata WHERE id="+id);
		Iterator i   = res.getChildren().iterator();

		dbms.commit();

		if (!i.hasNext())
		    return null;

		Element record = (Element) i.next();

		String schema = record.getChildText("schemaid");
		String data   = record.getChildText("data");
		
		// TODO : convert iso19115 metadata to iso19139
		
		//--- skip metadata with wrong schemas

		if (schema.equals("fgdc-std") || schema.equals("dublin-core") || schema.equals("iso19115"))
		    if (outSchema != OutputSchema.OGC_CORE)
			return null;

		Element md = Xml.loadString(data, false);

		//--- apply stylesheet according to setName and schema

		String FS         = File.separator;
		String schemaDir  = context.getAppPath() +"xml"+ FS +"csw"+ FS +"schemas"+ FS +schema+ FS;
		String prefix     = (outSchema == OutputSchema.OGC_CORE) ? "ogc" : "iso";
		String styleSheet = schemaDir + prefix +"-"+ setName +".xsl";

		md = Xml.transform(md, styleSheet);

		//--- needed to detach md from the document

		md.detach();

		//--- if the client has specified some ElementNames, then we remove the unwanted children

		if (elemNames != null)
		    removeElements(md, elemNames);

		return md;
	    }
	catch (Exception e)
	    {
		context.error("Error while getting metadata with id : "+ id);
		context.error("  (C) StackTrace:\n"+ Util.getStackTrace(e));

		throw new NoApplicableCodeEx("Raised exception while getting metadata :"+ e);
	    }
    }

    //---------------------------------------------------------------------------

    private void removeElements(Element md, Set<String> elemNames)
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


