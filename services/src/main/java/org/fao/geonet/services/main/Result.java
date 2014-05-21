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
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.jdom.Element;

//=============================================================================

/** main.result service. shows search results
  */

public class Result implements Service
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
		// build result data
		UserSession session = context.getUserSession();

		MetaSearcher searcher = (MetaSearcher) session.getProperty(Geonet.Session.SEARCH_RESULT);

        String fast = _config.getValue("fast","");

        if (StringUtils.isNotEmpty(fast)) {
            params.addContent(new Element("fast").setText(fast));
        }

		String range = _config.getValue("range");

		if (range != null)
			if (range.equals("all"))
			{
				params.addContent(new Element("from").setText("1"));
				params.addContent(new Element("to").setText(searcher.getSize() +""));
			} else {
				params.addContent(new Element("from").setText("1"));
				params.addContent(new Element("to").setText(range));
			}


		Element result = searcher.present(context, params, _config);

		// Update result elements to present
		SelectionManager.updateMDResult(context.getUserSession(), result);

		// Restore last search if set
		String restoreLastSearch = params.getChildText(Geonet.SearchResult.RESTORELASTSEARCH);
		if (restoreLastSearch != null && restoreLastSearch.equals("yes")) {
			Object oldSearcher = session.getProperty(Geonet.Session.LAST_SEARCH_RESULT);
			if (oldSearcher != null) {
				context.info("Restoring last search");
				if (oldSearcher instanceof LuceneSearcher) ((LuceneSearcher)searcher).close();
				session.setProperty(Geonet.Session.SEARCH_RESULT, oldSearcher);
			}
		}
 
		return result;
	}
}

//=============================================================================

