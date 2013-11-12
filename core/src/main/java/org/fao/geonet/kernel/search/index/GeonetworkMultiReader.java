package org.fao.geonet.kernel.search.index;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.fao.geonet.kernel.search.index.GeonetworkNRTManager.AcquireResult;

public class GeonetworkMultiReader extends MultiReader {

    private Map<AcquireResult, GeonetworkNRTManager> searchers;

    public GeonetworkMultiReader(IndexReader[] subReaders, Map<AcquireResult, GeonetworkNRTManager> searchers) {
        super(subReaders);
        this.searchers = searchers;
    }

    public void releaseToNRTManager() throws IOException {
        for(Map.Entry<AcquireResult, GeonetworkNRTManager> entry: searchers.entrySet()) {
            entry.getValue().release(entry.getKey().searcher);
        }
        searchers.clear();
    }

    public int numSubReaders() {
        return searchers.size();
    }
}
