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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.fao.geonet.kernel.search.index.GeonetworkNRTManager.AcquireResult;

import com.google.common.base.Predicate;

public class SearcherVersionTracker {

    private final AtomicLong version = new AtomicLong(0);
    private final Map<Long, Map<String, Long>> mapping = new HashMap<Long, Map<String, Long>>();

    public long get(String lang, long version) {
        Map<String, Long> versions = mapping.get(version);

        if (versions == null) {
            return -1;
        }
        Long actualVersion = versions.get(lang);
        if (actualVersion == null) {
            return -1L;
        } else {
            return actualVersion;
        }
    }

    public long lastVersion() {
        return version.get();
    }

    public long register(Map<AcquireResult, GeonetworkNRTManager> searchers) {
        long finalVersion = version.incrementAndGet();

        Map<String, Long> versions = mapping.get(finalVersion);
        if (versions == null) {
            versions = new HashMap<String, Long>();
            mapping.put(finalVersion, versions);
        }
        for (Map.Entry<AcquireResult, GeonetworkNRTManager> entry : searchers.entrySet()) {
            versions.put(entry.getValue().getLanguage(), entry.getKey().version);
        }
        return finalVersion;
    }

    public Long last(String language) {
        return get(language, lastVersion());
    }

    public void prune(String language, Predicate<Long> predicate) {
        for (Iterator<Entry<Long, Map<String, Long>>> iter = mapping.entrySet().iterator(); iter.hasNext(); ) {
            Entry<Long, Map<String, Long>> entry = iter.next();
            Long version = entry.getValue().get(language);
            if (version == null || !predicate.apply(version)) {
                iter.remove();
            }
        }

    }

    public int size() {
        return mapping.size();
    }
}
