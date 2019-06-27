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

package org.fao.geonet.kernel.search;

import jeeves.server.context.ServiceContext;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.log.SearcherLogger;
import org.fao.geonet.utils.Log;

/**
 * Task to launch a new thread for search logging.
 *
 * Other idea: Another approach could be to use JMS, to send an asynchronous message with search
 * info in order to log them.
 *
 * @author francois
 */
public class SearchLoggerTask implements Runnable {
    Query query;
    int numHits;
    Sort sort;
    String geomWKT;
    String value;
    private ServiceContext srvContext;

    public void configure(ServiceContext srvContext,
                          Query query, int numHits, Sort sort, String geomWKT,
                          String value) {
        this.srvContext = srvContext;
        this.query = query;
        this.numHits = numHits;
        this.sort = sort;
        this.geomWKT = geomWKT;
        this.value = value;
    }

    public void run() {
        try {
            SearcherLogger searchLogger = ApplicationContextHolder.get().getBean(SearcherLogger.class);
            searchLogger.logSearch(srvContext, query, numHits, sort, geomWKT, value);
        } catch (Exception e) {
            Log.error(Geonet.SEARCH_LOGGER, "SearchLogger task error:" + e.getMessage(), e);
        }
    }
}
