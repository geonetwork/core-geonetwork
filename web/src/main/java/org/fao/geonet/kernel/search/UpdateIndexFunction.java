package org.fao.geonet.kernel.search;

import org.apache.lucene.document.Document;

/**
 * Strategy for updating the documents related to a particular metadata.  
 * See {@link SearchManager#updateIndex(String, UpdateIndexFunction)}
 * 
 * @author jeichar
 */
public interface UpdateIndexFunction {

    /**
     * Executed just before loading the document to be updated.  If
     * there is any database access or other blocking behaviour it should be done
     * here rather that in update because there is not locks held
     * when this is called
     */
    void prepareForUpdate();
    /**
     * Return updated document to update the document.
     * 
     * This method must NOT block (access database etc...) because this
     * method is called within a synchronize block and if this method blocks
     * it will block all searches and updates in the system
     * 
     * @param indexLanguage language of index that the document comes from
     * @param currentDocument the document loaded from the index
     * 
     * @return the updated document or null. null indicates that the document should not
     *         be updated for this index
     */
    Document update(String indexLanguage, Document currentDocument);

}
