//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.geonet40.client;

import java.util.Set;

/**
 * Stores the information used by the harvester, for an Elasticsearch search.
 */
public class SearchResponse {
    private final long total;
    private final Set<SearchResponseHit> hits;

    public long getTotal() {
        return total;
    }

    public Set<SearchResponseHit> getHits() {
        return hits;
    }

    /**
     * Constructor for SearchResponse.
     *
     * @param total the total number of hits found
     * @param hits  the set of hits returned in the response
     */
    public SearchResponse(long total, Set<SearchResponseHit> hits) {
        this.total = total;
        this.hits = hits;
    }
}
