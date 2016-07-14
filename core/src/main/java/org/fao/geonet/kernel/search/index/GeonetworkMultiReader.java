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

package org.fao.geonet.kernel.search.index;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.fao.geonet.kernel.search.index.GeonetworkNRTManager.AcquireResult;

public class GeonetworkMultiReader extends MultiReader {

    private final AtomicInteger _openReaderCount;
    private Map<AcquireResult, GeonetworkNRTManager> _searchers;

    public GeonetworkMultiReader(AtomicInteger openReaderCounter, IndexReader[] subReaders, Map<AcquireResult, GeonetworkNRTManager> searchers) {
        super(subReaders);
        openReaderCounter.incrementAndGet();
        this._openReaderCount = openReaderCounter;
        this._searchers = searchers;
    }

    public void releaseToNRTManager() throws IOException {
        for (Map.Entry<AcquireResult, GeonetworkNRTManager> entry : _searchers.entrySet()) {
            entry.getValue().release(entry.getKey().searcher);
        }
        _searchers.clear();
        _openReaderCount.decrementAndGet();
    }

}
