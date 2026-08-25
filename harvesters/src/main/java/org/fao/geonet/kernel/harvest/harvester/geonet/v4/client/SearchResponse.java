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

package org.fao.geonet.kernel.harvest.harvester.geonet.v4.client;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Represents the response from a search operation, typically involving
 * an Elasticsearch query. This response contains the total number of hits
 * and a set of individual search result hits.
 */
public class SearchResponse {
    private final long total;
    private final Set<SearchResponseHit> hits;
    private final List<String> failedHits;

    /**
     * Constructs a new SearchResponse instance with no failed hits.
     *
     * @param total the total number of search hits returned
     * @param hits  a set of individual search result hits
     */
    public SearchResponse(long total, Set<SearchResponseHit> hits) {
        this(total, hits, Collections.<String>emptyList());
    }

    /**
     * Constructs a new SearchResponse instance.
     *
     * @param total      the total number of search hits returned
     * @param hits       a set of individual search result hits that could be parsed
     * @param failedHits the identifiers (or {@code "unknown"} when not extractable) of the hits that
     *                   could not be parsed and were skipped
     */
    public SearchResponse(long total, Set<SearchResponseHit> hits, List<String> failedHits) {
        this.total = total;
        this.hits = hits;
        this.failedHits = failedHits;
    }

    public long getTotal() {
        return total;
    }

    public Set<SearchResponseHit> getHits() {
        return hits;
    }

    /**
     * Returns the identifiers of the hits that could not be parsed from the search response and were
     * therefore skipped. The list is empty when every hit was parsed successfully.
     *
     * @return the list of skipped hit identifiers (never {@code null})
     */
    public List<String> getFailedHits() {
        return failedHits;
    }
}
