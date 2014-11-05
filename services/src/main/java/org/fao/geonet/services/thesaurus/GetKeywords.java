//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.services.thesaurus;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.utils.Log;
import org.fao.geonet.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.search.KeywordsSearcher;
import org.fao.geonet.kernel.search.keyword.KeywordSort;
import org.fao.geonet.kernel.search.keyword.SortDirection;
import org.jdom.Element;

import java.nio.file.Path;

/**
 * Returns a list of keywords given a list of thesaurus
 */

public class GetKeywords implements Service {
	public void init(Path appPath, ServiceConfig params) throws Exception {
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context)
			throws Exception {
		Element response = new Element(Jeeves.Elem.RESPONSE);
		UserSession session = context.getUserSession();

		KeywordsSearcher searcher = null;
		
		boolean newSearch = Util.getParam(params, "pNewSearch").equals("true");
		if (newSearch) {			
			// perform the search and save search result into session
			GeonetContext gc = (GeonetContext) context
					.getHandlerContext(Geonet.CONTEXT_NAME);
			ThesaurusManager thesaurusMan = gc.getBean(ThesaurusManager.class);

            if(Log.isDebugEnabled("KeywordsManager")) Log.debug("KeywordsManager","Creating new keywords searcher");
			searcher = new KeywordsSearcher(context, thesaurusMan);
			searcher.search(context.getLanguage(), params);
			searcher.sortResults(KeywordSort.defaultLabelSorter(SortDirection.DESC));
			session
					.setProperty(Geonet.Session.SEARCH_KEYWORDS_RESULT,
							searcher);
		} else {
			searcher = (KeywordsSearcher) session
					.getProperty(Geonet.Session.SEARCH_KEYWORDS_RESULT);
		}

		// get the results
		response.addContent(searcher.getXmlResults());

		
		
		// If editing
		if (params.getChild("pMode") != null) {
			String mode = Util.getParam(params, "pMode");
			if (mode.equals("edit") || mode.equals("consult")) {
				String thesaurus = Util.getParam(params, "pThesauri","");
				
				response.addContent(new Element("thesaurus")
						.addContent(thesaurus));
				
				response.addContent((new Element("mode")).addContent(mode));
			}
		}

		return response;
	}
}

// =============================================================================

