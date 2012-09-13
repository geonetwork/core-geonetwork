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

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;

import jeeves.utils.Xml;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.OutputSchema;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.MissingParameterValueEx;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.csw.CatalogConfiguration;
import org.fao.geonet.kernel.csw.CatalogService;
import org.fao.geonet.kernel.csw.services.getrecords.SearchController;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;

//=============================================================================

/**
 * TODO
 * OGC 07045:
 * - TYPENAME
 * - Zero or one (Optional) Default action is to describe all types known to server
 * - Optional. Must support “gmd:MD_Metadata”.
 *
 */
public class GetRecordById extends AbstractOperation implements CatalogService
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

    private SearchController _searchController;

    public GetRecordById(File summaryConfig, LuceneConfig luceneConfig) {
        _searchController = new SearchController(summaryConfig, luceneConfig);
    }


	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getName() { return "GetRecordById"; }

	//---------------------------------------------------------------------------

	public Element execute(Element request, ServiceContext context) throws CatalogException {
		checkService(request);
		checkVersion(request);
		//-- Added for CSW 2.0.2 compliance by warnock@awcubed.com
		checkOutputFormat(request);
		OutputSchema outSchema = OutputSchema.parse(request.getAttributeValue("outputSchema"));
		//--------------------------------------------------------

		ElementSetName setName = getElementSetName(request, ElementSetName.SUMMARY);

		Element response = new Element(getName() +"Response", Csw.NAMESPACE_CSW);

		Iterator ids = request.getChildren("Id", Csw.NAMESPACE_CSW).iterator();

		if (!ids.hasNext())
			throw new MissingParameterValueEx("id");

		try {
			while(ids.hasNext())
			{
				String  uuid = ((Element) ids.next()).getText();
				Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
				GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
				String id = gc.getDataManager().getMetadataId(dbms, uuid);
				
				// Metadata not found, search for next ids
				if (id == null)
					continue;
					//throw new InvalidParameterValueEx("uuid", "Can't find metadata with uuid "+uuid);


                // Apply CSW service specific constraint
                String cswServiceSpecificContraint = request.getChildText(Geonet.Elem.FILTER);

                if (StringUtils.isNotEmpty(cswServiceSpecificContraint)) {
                    if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                        Log.debug(Geonet.CSW_SEARCH, "GetRecordById (cswServiceSpecificContraint): " + cswServiceSpecificContraint);

                    cswServiceSpecificContraint = cswServiceSpecificContraint + " +_uuid: " + uuid;
                    if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                        Log.debug(Geonet.CSW_SEARCH, "GetRecordById (cswServiceSpecificContraint with uuid): " + cswServiceSpecificContraint);

                    Element filterExpr = new Element("Filter", Csw.NAMESPACE_OGC);

                    Pair<Element, Element> results= _searchController.search(context, 0, 1, ResultType.HITS, OutputSchema.OGC_CORE,
                            ElementSetName.BRIEF,  filterExpr, Csw.FILTER_VERSION_1_1, null, null, null, 0, cswServiceSpecificContraint, null);


                    if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                        Log.debug(Geonet.CSW_SEARCH, "GetRecordById cswServiceSpecificContraint result: " + Xml.getString(results.two()));

                    int numOfResults = Integer.parseInt(results.two().getAttributeValue("numberOfRecordsMatched"));

                    if (numOfResults == 0)
                        continue;
                }

				// Check if the current user has access 
			    // to the requested MD 
			    Lib.resource.checkPrivilege(context, id, AccessManager.OPER_VIEW); 
				
				Element md = SearchController.retrieveMetadata(context, id, setName, outSchema, null, null, ResultType.RESULTS, null);

				if (md != null)
					response.addContent(md);
				
				if (CatalogConfiguration.is_increasePopularity()) {
				    gc.getDataManager().increasePopularity(context, id);
				}
			}
		} catch (Exception e) {
			context.error("Raised : "+ e);
			context.error(" (C) Stacktrace is\n"+Util.getStackTrace(e));
			throw new NoApplicableCodeEx(e.toString());
		}
		return response;
	}

	//---------------------------------------------------------------------------

	public Element adaptGetRequest(Map<String, String> params)
	{
		String service     = params.get("service");
		String version     = params.get("version");
		String elemSetName = params.get("elementsetname");
		String ids         = params.get("id");

		//-- Added for CSW 2.0.2 compliance by warnock@awcubed.com
		String outputFormat = params.get("outputformat");
		String outputSchema = params.get("outputschema");
		//--------------------------------------------------------

		Element request = new Element(getName(), Csw.NAMESPACE_CSW);

		setAttrib(request, "service", service);
		setAttrib(request, "version", version);

		//-- Added for CSW 2.0.2 compliance by warnock@awcubed.com
		setAttrib(request, "outputFormat",  outputFormat);
		setAttrib(request, "outputSchema",  outputSchema);
		//--------------------------------------------------------

		fill(request, "Id", ids);

		addElement(request, "ElementSetName", elemSetName);

		return request;
	}
	
	//---------------------------------------------------------------------------
	
	public Element retrieveValues(String parameterName) throws CatalogException {
		// TODO 
		return null;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

    //-- Added for CSW 2.0.2 compliance by warnock@awcubed.com
	private void checkOutputFormat(Element request) throws InvalidParameterValueEx
	{
		String format = request.getAttributeValue("outputFormat");

		if (format == null)
			return;

		if (!format.equals("application/xml"))
			throw new InvalidParameterValueEx("outputFormat", format);
	}
}