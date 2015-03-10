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

package org.fao.geonet.services.schema;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.List;

//=============================================================================

public class Delete implements Service {
	// --------------------------------------------------------------------------
	// ---
	// --- Init
	// ---
	// --------------------------------------------------------------------------

	public void init(Path appPath, ServiceConfig params) throws Exception {}

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception {
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SchemaManager scm = gc.getBean(SchemaManager.class);

		String schema = Util.getParam(params, Params.SCHEMA);

		// see if the schema to be deleted actually exists
		Element response = new Element("response");
		if (!scm.existsSchema(schema)) {
			response.setAttribute("status", "error");
			response.setAttribute("message", "Schema does not exist");
			return response;
		}

		// fast search to see if any records are present that use this schema
		ServiceConfig config = new ServiceConfig();

    SearchManager searchMan = gc.getBean(SearchManager.class);
		Element searchParams = new Element("parameters");
    searchParams.addContent(new Element("_schema").setText(schema));

   	MetaSearcher  searcher  = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);
		try {
   		searcher.search(context, searchParams, config);
			int results = searcher.getSize();
			if (results == 0) { // check for templates
    		searchParams.addContent(new Element("_isTemplate").setText("y"));
   			searcher.search(context, searchParams, config);
				results = searcher.getSize();
			}
    	if (results > 0) {
				String errStr = "Cannot remove schema "+schema+" because there are records that belong to this schema in the catalog";
				context.error(errStr);
				response.setAttribute("status", "error");
				response.setAttribute("message", errStr);
				return response;
			}
		} catch (Exception e) {
			e.printStackTrace();
			String errStr = "Cannot remove schema "+schema+" because the search for records that belong to this schema FAILED ("+e.getMessage()+")";
      context.error(errStr);
      response.setAttribute("status", "error");
      response.setAttribute("message", errStr);
      return response;
		} finally {
   		searcher.close();
		}

		// check for any schemas that may be dependent on the schema to be deleted
		List<String> dependsOnMe = scm.getSchemasThatDependOnMe(schema);
		if (dependsOnMe.size() > 0) {
			String errStr = "Cannot remove schema "+schema+" because the following schemas list it as a dependency: "+dependsOnMe;

			context.error(errStr);
			response.setAttribute("status", "error");
			response.setAttribute("message", errStr);
			return response;
		}

		// finally, try to delete the schema
		try {
			scm.deletePluginSchema(schema);
			response.setAttribute("status", "ok");
			response.setAttribute("message", "Schema "+schema+" deleted");
		} catch (Exception e) {
			e.printStackTrace();
			response.setAttribute("status", "error");
			response.setAttribute("message", "Could not delete schema, error if any was "+e.getMessage());
		}
		return response;
	}

}

// =============================================================================

