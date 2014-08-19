package org.fao.geonet.kernel.reusable;

import org.apache.lucene.search.Query;

/**
 * Interface to use for looking finding referenced metadata.
 *
 * @author Jesse on 8/19/2014.
 */
public interface FindMetadataReferences {
    /**
     * Create the search query for looking up metadata references.
     */
    Query createFindMetadataQuery(String field, String concreteId, boolean isValidated);
}
