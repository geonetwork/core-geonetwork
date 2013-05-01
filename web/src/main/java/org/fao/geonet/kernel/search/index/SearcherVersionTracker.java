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
        
        if(versions == null) {
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
        if(versions == null) {
            versions = new HashMap<String, Long>();
            mapping.put(finalVersion, versions);
        }
        for (Map.Entry<AcquireResult, GeonetworkNRTManager> entry: searchers.entrySet()) {
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
            if(version == null || !predicate.apply(version)) {
                iter.remove();
            } 
        }
        
    }

    public int size() {
        return mapping.size();
    }
}
