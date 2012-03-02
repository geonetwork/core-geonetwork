package org.fao.geonet.kernel.search;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;

import java.io.IOException;

@SuppressWarnings("serial")
public class NoFilterFilter extends Filter {

    private final static NoFilterFilter instance = new NoFilterFilter();
    @Override
    public DocIdSet getDocIdSet( IndexReader reader ) throws IOException {
        OpenBitSet set = new OpenBitSet(reader.maxDoc());
        set.set(0, reader.maxDoc());
        return set;
    }

    public static Filter instance() {
        return instance;
    }

}
