package org.fao.geonet.kernel.search.index;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NRTManager;
import org.fao.geonet.kernel.search.spatial.Pair;

public class GeonetworkMultiReader extends MultiReader {

    private Map<Pair<Long, IndexSearcher>, GeonetworkNRTManager> searchers;

    public GeonetworkMultiReader(IndexReader[] subReaders, Map<Pair<Long, IndexSearcher>, GeonetworkNRTManager> searchers) {
        super(subReaders);
        this.searchers = searchers;
    }

    void releaseToNRTManager() throws IOException {
        for(Map.Entry<Pair<Long, IndexSearcher>, GeonetworkNRTManager> entry: searchers.entrySet()) {
            entry.getValue().release(entry.getKey().two());
        }
        searchers.clear();
    }
}
