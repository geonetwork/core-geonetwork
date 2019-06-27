/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.records.rdf;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;
import org.fao.geonet.services.util.SearchDefaults;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.util.List;

/**
 * Class to search with the lucene searcher all public metadata that fits the user filter. Used by
 * RDF harvest service to return all the public metadata from the catalogue in rdf format.
 *
 * @author Jose García
 */
public class RdfSearcher {
    private MetaSearcher searcher;
    private Element searchRequest;
    private long _versionToken = -1;

    public RdfSearcher(Element params, ServiceContext context) {
        searchRequest = SearchDefaults.getDefaultSearch(context, params);
        searchRequest.addContent(new Element(Geonet.SearchResult.BUILD_SUMMARY).setText("false"));
        searchRequest.addContent(new Element("_isTemplate").setText("n"));
        searchRequest.addContent(new Element("_op0").setText("1"));
        if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "PUBLIC METADATA SEARCH CRITERIA:\n" + Xml.getString(searchRequest));

    }

    public List search(ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SearchManager searchMan = gc.getBean(SearchManager.class);
        searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);

        ServiceConfig config = new ServiceConfig();

      
        searcher.search(context, searchRequest, config);
        
        numberMatched = searcher.getSize();
        _versionToken = searcher.getVersionToken(); 
        
        searchRequest.addContent(new Element(Geonet.SearchResult.BUILD_SUMMARY).setText("false"));
                  
        return searcher.present(context, searchRequest, config).getChildren();
    }

    public void close() {
        try {
            if (searcher != null) searcher.close();
        } catch (Exception ex) {
            // Ignore exception
        }
    }
    
    private int numberMatched;
    public int getSize() {
        return numberMatched;
    }
    
    /**
     * <p> Gets the Lucene version token. Can be used as ETag. </p>
     */      
    public long getVersionToken(){
    	return _versionToken;
    };    
}
