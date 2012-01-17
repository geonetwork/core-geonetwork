package org.fao.geonet.kernel.search.function;

import org.apache.lucene.document.Document;

/**
 * A class which compute a document boost factor
 * based on the document fields analysis.
 * 
 * @author francois
 */
public interface DocumentBoosting {

    /**
     * Compute the boost factor.
     * 
     * @param doc   The document to analyze
     * @return  The computed boost factor
     */
    public abstract Float getBoost(Document doc);

}