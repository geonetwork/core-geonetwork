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
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.Iterator;

import static org.fao.geonet.kernel.SelectionManager.SELECTION_BUCKET;
import static org.fao.geonet.kernel.SelectionManager.SELECTION_METADATA;

@Deprecated
public class SelectionSearch implements Service {
    private ServiceConfig _config;

    public void init(Path appPath, ServiceConfig config) throws Exception {
        _config = config;
    }

    /*
     * Get the current search and add an uuid params
     * with the list of records contain in the
     * selection manager.
     *
     */
    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        SearchManager searchMan = gc.getBean(SearchManager.class);

        String bucket = Util.getParam(params, SELECTION_BUCKET, SELECTION_METADATA);
        String formatter = Util.getParam(params, "formatter", "");
        params.removeChild(SELECTION_BUCKET);
        params.removeChild("formatter");

        // store or possibly close old searcher
        UserSession session = context.getUserSession();

        SelectionManager sm = SelectionManager.getManager(session);

        // Get the sortBy params in order to apply on new result list.
        if (StringUtils.isNotEmpty(params.getChildText(Geonet.SearchResult.SORT_BY))) {
            params.addContent(new Element(Geonet.SearchResult.SORT_BY).setText(params.getChildText(Geonet.SearchResult.SORT_BY)));
        }

        if (StringUtils.isNotEmpty(params.getChildText(Geonet.SearchResult.SORT_ORDER))) {
            params.addContent(new Element(Geonet.SearchResult.SORT_ORDER).setText(params.getChildText(Geonet.SearchResult.SORT_ORDER)));
        }

        if (sm != null) {
            String uuids = "";
            boolean first = true;
            synchronized (sm.getSelection(bucket)) {
                for (Iterator<String> iter = sm.getSelection(bucket).iterator(); iter.hasNext(); ) {
                    String uuid = (String) iter.next();
                    if (first) {
                        uuids = (String) uuid;
                        first = false;
                    } else
                        uuids = uuids + " or " + uuid;
                }
            }
            if (context.isDebugEnabled())
                context.debug("List of selected uuids: " + uuids);
            params.addContent(new Element(Geonet.SearchResult.UUID).setText(uuids));

        }

        // perform the search and save search result into session
        MetaSearcher searcher;

        context.info("Creating searchers");

        searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);

        searcher.search(context, params, _config);

        session.setProperty(Geonet.Session.SEARCH_RESULT + bucket, searcher);

        context.info("Getting summary");

        Element summary = searcher.getSummary();
        summary.addContent(new Element(SELECTION_BUCKET).setText(bucket));
        summary.addContent(new Element("formatter").setText(formatter));
        return summary;

    }
}
