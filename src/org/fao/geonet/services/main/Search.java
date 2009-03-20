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

package org.fao.geonet.services.main;


import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.fao.geonet.constants.*;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.search.*;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.services.util.MainUtil;

//=============================================================================

/** main.search service. Perform a search
  */

public class Search implements Service
{
	private ServiceConfig _config;

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig config) throws Exception
	{
		_config = config;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		SearchManager searchMan = gc.getSearchmanager();

		Element elData  = MainUtil.getDefaultSearch(context, params);
		String  sRemote = elData.getChildText(Geonet.SearchResult.REMOTE);
		boolean remote  = sRemote != null && sRemote.equals(Geonet.Text.ON);

		Element title = params.getChild(Geonet.SearchResult.TITLE);
		Element abstr = params.getChild(Geonet.SearchResult.ABSTRACT);
		Element any   = params.getChild(Geonet.SearchResult.ANY);

		if (title != null)
			title.setText(MainUtil.splitWord(title.getText()));

		if (abstr != null)
			abstr.setText(MainUtil.splitWord(abstr.getText()));

		if (any != null)
			any.setText(MainUtil.splitWord(any.getText()));

		// possibly close old searcher
		UserSession  session     = context.getUserSession();
		MetaSearcher oldSearcher = (MetaSearcher)session.getProperty(Geonet.Session.SEARCH_RESULT);

		if (oldSearcher != null)
			oldSearcher.close();
		
		// possibly close old selection
		SelectionManager oldSelection = (SelectionManager)session.getProperty(Geonet.Session.SELECTED_RESULT);
		
		if (oldSelection != null){
			oldSelection.close();
			oldSelection = null;
		}

		// perform the search and save search result into session
		MetaSearcher searcher;

		context.info("Creating searchers");

		if (remote)	searcher = searchMan.newSearcher(SearchManager.Z3950,  Geonet.File.SEARCH_Z3950_CLIENT);
		else        searcher = searchMan.newSearcher(SearchManager.LUCENE, Geonet.File.SEARCH_LUCENE);

		searcher.search(context, params, _config);
		session.setProperty(Geonet.Session.SEARCH_RESULT, searcher);

		context.info("Getting summary");

		return searcher.getSummary();
	}
}

//=============================================================================

