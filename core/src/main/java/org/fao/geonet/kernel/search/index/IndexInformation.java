package org.fao.geonet.kernel.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.facet.taxonomy.CategoryPath;

import java.util.Collection;

/**
 * The information that will be put in the index for searching for a particular language.
 *
 * @author Jesse on 10/8/2014.
 * @see org.fao.geonet.kernel.search.SearchManager#buildIndexDocument(String, org.jdom.Element, String, java.util.List)
 */
public class IndexInformation {
    final String language;
    final Document document;
    final Collection<CategoryPath> taxonomy;

    public IndexInformation(String language, Document document, Collection<CategoryPath> taxonomy) {
        this.language = language;
        this.document = document;
        this.taxonomy = taxonomy;
    }
}
