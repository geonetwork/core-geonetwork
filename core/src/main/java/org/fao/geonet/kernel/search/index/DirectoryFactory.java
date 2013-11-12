package org.fao.geonet.kernel.search.index;

import org.apache.lucene.store.Directory;
import org.fao.geonet.kernel.search.LuceneConfig;

import java.io.IOException;
import java.util.Set;

/**
 * A factory for creating {@link org.apache.lucene.store.Directory} objects that the Lucene readers and writers will use.
 *
 * User: Jesse
 * Date: 10/18/13
 * Time: 11:20 AM
 */
public interface DirectoryFactory {
    /**
     * Create a brand new directory object that will contain the lucene index.
     *
     *
     * @param indexId
     * @param config configuration to use for creating directory object.
     *
     * @return a brand new directory object.
     */
    Directory createIndexDirectory(String indexId, LuceneConfig config) throws IOException;

    /**
     * Create a brand new directory object that will contain the lucene taxonomy.
     *
     * @param config configuration to use for creating directory object.
     *
     * @return a brand new directory object.
     */
    Directory createTaxonomyDirectory(LuceneConfig config) throws IOException;

    /**
     * Clean out the taxonomy directory if necessary.  Delete all old index files for example.
     *
     */
    void resetTaxonomy() throws IOException;
    /**
     * Clean out the taxonomy directory if necessary.  Delete all old index files for example.
     *
     */
    void resetIndex() throws IOException;

    /**
     * Get the list of indices currently available.
     *
     * @return the ids
     */
    Set<String> listIndices();
}
