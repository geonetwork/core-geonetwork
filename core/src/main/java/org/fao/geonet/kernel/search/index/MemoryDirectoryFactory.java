package org.fao.geonet.kernel.search.index;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.fao.geonet.kernel.search.LuceneConfig;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * An in memory-only directory factory.
 * <p/>
 * User: Jesse
 * Date: 10/18/13
 * Time: 1:41 PM
 */
public class MemoryDirectoryFactory implements DirectoryFactory {
    @Override
    public Directory createIndexDirectory(final String indexId, final LuceneConfig config) throws IOException {
        return new RAMDirectory();
    }

    @Override
    public Directory createTaxonomyDirectory(final LuceneConfig config) throws IOException {
        return new RAMDirectory();
    }

    @Override
    public void resetTaxonomy() throws IOException {
        // nothing needed
    }

    @Override
    public void resetIndex() throws IOException {
        // nothing needed
    }

    @Override
    public Set<String> listIndices() {
        return Collections.emptySet();
    }
}
