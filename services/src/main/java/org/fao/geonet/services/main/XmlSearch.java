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
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.search.LuceneIndexField;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;
import org.fao.geonet.services.util.SearchDefaults;
import org.jdom.Element;

import java.nio.file.Path;

import static org.fao.geonet.kernel.SelectionManager.SELECTION_BUCKET;
import static org.fao.geonet.kernel.SelectionManager.SELECTION_METADATA;

//=============================================================================

public class XmlSearch implements Service {
    private ServiceConfig _config;
    private String _searchFast; //true, false, index
    // Initialized here for testing purposes
    private int maxRecordValue = 100;
    private boolean allowUnboundedQueries = false;

    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig config) throws Exception {
        _config = config;
        _searchFast = config.getValue(Geonet.SearchResult.FAST, "true");
        maxRecordValue = Integer.parseInt(config.getValue(Geonet.SearchResult.MAX_RECORDS, "100"));
        allowUnboundedQueries = Boolean.parseBoolean(config.getValue(Geonet.SearchResult.ALLOW_UNBOUNDED_QUERIES, "false"));
    }

    /**
     * This method ensures that the user-provided boundaries will not harm the
     * application, e.g. on huge catalogue (80k MDs), calling the search service
     * without passing the from/to parameters (or giving weird parameters
     * combinations) can generate a response document reaching several MB (80k
     * MDs gives 400MB of JSON, which is not really parseable in the UI anyway,
     * and can DoS the webapp).
     *
     * This method may modify the object params passed as argument. In this
     * case, it returns true.
     *
     * @param params the parameters coming from the request.
     * @return true if the boundaries have been modified, false otherwise.
     */
    private boolean setSafeBoundaries(Element params) {
        boolean fromUndefined = params.getChild("from") == null;
        int from = Util.getParam(params, "from", 0);
        boolean toUndefined = params.getChild("to") == null;
        int to = Util.getParam(params, "to", Integer.MAX_VALUE);
        if ((to - from) < 0) {
            throw new BadParameterEx("Bad range requested, check the from/to parameters");
        }
        boolean boundariesSet = false;

        // from / to undefined
        if (fromUndefined && toUndefined) {
            params.addContent(new Element("from").setText("1"));
            params.addContent(new Element("to").setText(Integer.toString(this.maxRecordValue)));
            boundariesSet = true;
        }
        // from undefined, to defined
        else if (fromUndefined && !toUndefined) {
            params.addContent(new Element("from").setText(Integer.toString(Math.max(1, to - this.maxRecordValue))));
            boundariesSet = true;
        }
        // from defined, to undefined
        else if (!fromUndefined && toUndefined) {
            params.addContent(new Element("to").setText(Integer.toString(from + this.maxRecordValue - 1)));
            boundariesSet = true;
        }
        // from defined, to defined
        else {
            // if the range is unacceptable, fix it. Otherwise all good
            if ((to - from) >= this.maxRecordValue) {
                params.removeChildren("to");
                params.addContent(new Element("to").setText(Integer.toString(from + this.maxRecordValue - 1)));
                boundariesSet = true;
            }
        }
        return boundariesSet;
    }

    /**
     * Run a search and return results as XML.
     *
     * @param params All search parameters defined in {@link LuceneIndexField}. <br/> To return only
     *               results summary, set summaryOnly parameter to 1. Default is 0 (ie.results and
     *               summary).
     */
    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        SearchManager searchMan = gc.getBean(SearchManager.class);

        String bucket = Util.getParam(params, SELECTION_BUCKET, SELECTION_METADATA);
        params.removeChild(SELECTION_BUCKET);

        // Sets the boundaries (from/to) if needed
        boolean boundariesSet = false;
        if (!this.allowUnboundedQueries) {
            boundariesSet = setSafeBoundaries(params);
        }

        Element elData = SearchDefaults.getDefaultSearch(context, params);

        // possibly close old searcher
        UserSession session = context.getUserSession();

        // perform the search and save search result into session
        MetaSearcher searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);
        try {

            // Check if user asked for summary only without building summary
            String summaryOnly = Util.getParam(params, Geonet.SearchResult.SUMMARY_ONLY, "0");
            String sBuildSummary = params.getChildText(Geonet.SearchResult.BUILD_SUMMARY);
            if (sBuildSummary != null && sBuildSummary.equals("false") && !"0".equals(summaryOnly)) {
                elData.getChild(Geonet.SearchResult.BUILD_SUMMARY).setText("true");
            }

            session.setProperty(Geonet.Session.SEARCH_REQUEST + bucket, elData.clone());
            searcher.search(context, elData, _config);

            if (!"0".equals(summaryOnly)) {
                return searcher.getSummary();
            } else {

                elData.addContent(new Element(Geonet.SearchResult.FAST).setText(_searchFast));
                if (!boundariesSet) {
                    elData.addContent(new Element("from").setText("1"));
                    elData.addContent(new Element("to").setText(searcher.getSize() + ""));
                }
                Element result = searcher.present(context, elData, _config);

                // Update result elements to present
                SelectionManager.updateMDResult(context.getUserSession(), result, bucket);
                if (!this.allowUnboundedQueries) { // return maxRecordValue for users of this service to request pages
                    result = result.setAttribute("maxPageSize", getMaxRecordValue() + "");
                }
                return result;
            }
        } finally {
            searcher.close();
        }
    }

    /**
     * get the max record to be searched. This parameter is meant to alter the
     * user-provided "from" and "to" parameters.
     *
     * @return the max allowed records.
     */
    public int getMaxRecordValue() {
        return maxRecordValue;
    }
}

//=============================================================================

