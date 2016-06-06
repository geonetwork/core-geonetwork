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

package org.fao.geonet.services.rdf;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.SolrSearchManager;

import java.util.List;

import jeeves.server.context.ServiceContext;

/**
 * Class to search with the lucene searcher all public metadata that fits the user filter. Used by
 * RDF harvest service to return all the public metadata from the catalogue in rdf format.
 *
 * @author Jose García
 */
public class RdfSearcher {
    String q;

    public RdfSearcher(String q) {
        this.q = q == null ? "" : q;
    }

    public List<String> search(ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SolrSearchManager searchMan = gc.getBean(SolrSearchManager.class);
        List<String> records = searchMan.getAllDocIds(String.format(
            "+docType:metadata +_isTemplate:n _op0:1 %s", q));
        return records;
    }
}
