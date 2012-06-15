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

package org.fao.geonet.guiservices.metadata;

import java.util.List;
import java.util.Random;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.jdom.Element;

//=============================================================================

/**
 * Perform a search and return most popular metadata records.
 */

public class GetMostPopular implements Service {
	private ServiceConfig _config;
	private Element _response;
	private int _maxItems;
	private long _timeBetweenUpdates;
	private long _lastUpdateTime;
	
	public void init(String appPath, ServiceConfig config) throws Exception {
		_config = config;

		String sMaxItems = config.getValue("maxItems", "10");
		_maxItems = Integer.parseInt(sMaxItems);
		String sTimeBetweenUpdates = config
				.getValue("timeBetweenUpdates", "60");
		_timeBetweenUpdates = Integer.parseInt(sTimeBetweenUpdates) * 1000;

	}

	public Element exec(Element params, ServiceContext context)
			throws Exception {
		if (System.currentTimeMillis() > _lastUpdateTime + _timeBetweenUpdates) {
			GeonetContext gc = (GeonetContext) context
					.getHandlerContext(Geonet.CONTEXT_NAME);
			DataManager dataMan = gc.getDataManager();
			SearchManager searchMan = gc.getSearchmanager();
			MetaSearcher searcher = searchMan.newSearcher(SearchManager.LUCENE,
					Geonet.File.SEARCH_LUCENE);
			try {
				Element searchRequest = new Element(Jeeves.Elem.REQUEST);
				searchRequest.addContent(new Element(
						Geonet.SearchResult.SORT_BY)
						.setText(Geonet.SearchResult.SortBy.POPULARITY));
				searcher.search(context, searchRequest, _config);

				Element presentRequest = new Element(Jeeves.Elem.REQUEST);
				presentRequest.addContent(new Element("fast").setText("true"));
				presentRequest.addContent(new Element("from").setText("1"));
				presentRequest.addContent(new Element("to").setText(searcher
						.getSize()
						+ ""));
				List results = searcher.present(context, presentRequest,
						_config).getChildren();

				_response = new Element("response");
				//-- Skip summary element
				for (int i = 1; i < _maxItems + 1 && results.size() > 1; i++) {

					Element mdInfo = (Element) results.remove(1);
					mdInfo.detach();
					Element info = mdInfo.getChild("info", Edit.NAMESPACE);
					String id = info.getChildText("id");
					Element md = dataMan.getMetadata(context, id, false, true,true);

					_response.addContent(md);
				}
				_lastUpdateTime = System.currentTimeMillis();
			} finally {
				searcher.close();
			}
		}
		return (Element) _response.clone();
	}
}

// =============================================================================

