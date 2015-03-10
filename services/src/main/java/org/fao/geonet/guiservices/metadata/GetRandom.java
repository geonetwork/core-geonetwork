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

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.kernel.search.SearcherType;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;

//=============================================================================

/** main.search service. Perform a search
  */

public class GetRandom implements Service
{
	private int     _maxItems;
	private long    _timeBetweenUpdates;
	private String  _relation;
	private String  _northBL;
	private String  _southBL;
	private String  _eastBL;
	private String  _westBL;

	private Element _response;
	private long    _lastUpdateTime;

	private ServiceConfig _config;

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(Path appPath, ServiceConfig config) throws Exception
	{
		_config = config;

		String sMaxItems = config.getValue("maxItems", "10");
		_maxItems = Integer.parseInt(sMaxItems);
		String sTimeBetweenUpdates = config.getValue("timeBetweenUpdates", "60");
		_timeBetweenUpdates = Integer.parseInt(sTimeBetweenUpdates) * 1000;

		// Allow the random search to be restricted to a geographic region
		_relation = config.getValue("relation", "overlaps");
		_northBL = config.getValue("northBL", "90");
		_southBL = config.getValue("southBL", "-90");
		_eastBL = config.getValue("eastBL", "180");
		_westBL = config.getValue("westBL", "-180");
        
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		if (System.currentTimeMillis() > _lastUpdateTime + _timeBetweenUpdates)
		{
			GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
			SearchManager searchMan = gc.getBean(SearchManager.class);
			MetaSearcher  searcher  = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);

			try {
				// FIXME: featured should be at metadata level, not at group level
				Element searchRequest = new Element("request");
				searchRequest.addContent(new Element(Geonet.SearchResult.BUILD_SUMMARY).setText("false"));
				searchRequest.addContent(new Element("featured").setText("true"));
				searchRequest.addContent(new Element("relation").setText(_relation));
				searchRequest.addContent(new Element("northBL").setText(_northBL));
				searchRequest.addContent(new Element("southBL").setText(_southBL));
				searchRequest.addContent(new Element("eastBL").setText(_eastBL));
				searchRequest.addContent(new Element("westBL").setText(_westBL));
	
                if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
				    Log.debug(Geonet.SEARCH_ENGINE, "RANDOM SEARCH CRITERIA:\n"+ Xml.getString(searchRequest));
				
				searcher.search(context, searchRequest, _config);
	
				Element presentRequest = new Element("request");
				presentRequest.addContent(new Element("fast").setText("true"));
				presentRequest.addContent(new Element("from").setText("1"));
				presentRequest.addContent(new Element("to").setText(searcher.getSize()+""));
           	 
				@SuppressWarnings("unchecked")
                List<Element> results = searcher.present(context, presentRequest, _config).getChildren();
	
				_response = new Element("response");
				for (int i = 0; i < _maxItems && results.size() > 1; i++) {
					Random rnd = new Random();
					int r = rnd.nextInt(results.size() - 1) + 1;  // skip summary
	
					Element mdInfo = (Element)results.remove(r);
					mdInfo.detach();
	
					Element info = mdInfo.getChild("info", Edit.NAMESPACE);
					String id = info.getChildText("id");
                    boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
                    Element md = gc.getBean(DataManager.class).getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);
					_response.addContent(md);
				}
				_lastUpdateTime = System.currentTimeMillis();
			}
            finally {
				searcher.close();
			}
		}
		return (Element)_response.clone();
	}
}