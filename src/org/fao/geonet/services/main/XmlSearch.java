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

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.services.util.SearchDefaults;
import org.jdom.Element;

//=============================================================================

public class XmlSearch implements Service
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

		Element elData  = SearchDefaults.getDefaultSearch(context, params);
		String  sRemote = elData.getChildText(Geonet.SearchResult.REMOTE);
		boolean remote  = sRemote != null && sRemote.equals(Geonet.Text.ON);

		// possibly close old searcher
		UserSession  session     = context.getUserSession();
		Object oldSearcher = session.getProperty(Geonet.Session.SEARCH_RESULT);
		
 		if (oldSearcher != null)
 			if (oldSearcher instanceof LuceneSearcher)
 				((LuceneSearcher)oldSearcher).close();
		
		// perform the search and save search result into session
		MetaSearcher searcher;

		context.info("Creating searchers");

		if (remote)	 searcher = searchMan.newSearcher(SearchManager.Z3950,  Geonet.File.SEARCH_Z3950_CLIENT);
			else      searcher = searchMan.newSearcher(SearchManager.LUCENE, Geonet.File.SEARCH_LUCENE);

		searcher.search(context, params, _config);

		params.addContent(new Element("fast").setText("true"));
		params.addContent(new Element("from").setText("1"));
		params.addContent(new Element("to").setText(searcher.getSize() +""));

		return searcher.present(context, params, _config);
	}
}

//=============================================================================

