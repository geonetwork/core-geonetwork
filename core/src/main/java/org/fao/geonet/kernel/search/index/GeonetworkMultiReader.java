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
        for(Map.Entry<AcquireResult, GeonetworkNRTManager> entry: _searchers.entrySet()) {
            entry.getValue().release(entry.getKey().searcher);
        }
        _searchers.clear();
        _openReaderCount.decrementAndGet();
    }

}
