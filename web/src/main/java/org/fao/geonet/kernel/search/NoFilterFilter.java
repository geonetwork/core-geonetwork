package org.fao.geonet.kernel.search;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.OpenBitSet;

import java.io.IOException;

public class NoFilterFilter extends Filter {

    private final static NoFilterFilter instance = new NoFilterFilter();
    @Override
    public DocIdSet getDocIdSet(AtomicReaderContext context, Bits acceptDocs) throws IOException {
        OpenBitSet set = new OpenBitSet(context.reader().maxDoc());
        set.set(0, context.reader().maxDoc());
        return set;
    }

    public static Filter instance() {
        return instance;
    }

}
