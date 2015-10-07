package org.openwis.metadata.product;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.NoFilterFilter;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.index.GeonetworkMultiReader;
import org.fao.geonet.kernel.search.index.IndexInformation;
import org.fao.geonet.kernel.search.index.LuceneIndexLanguageTracker;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.openwis.products.client.ProductMetadata;
import org.openwis.util.GeonetOpenwis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

/**
 * ProductMetadata indexer for Lucene.
 *
 * @author Jose García
 */
@Component
public class ProductMetadataIndexerLucene implements IProductMetadataIndexer {
    @Autowired
    SearchManager searchManager;

    @Override
    public Vector<Element> index(ProductMetadata pm) {
        Vector<Element> indexFields = new Vector<>();

        indexFields.add(SearchManager.makeField("_process", pm.getProcess(), true, true));
        indexFields.add(SearchManager.makeField("_gtsCategory", pm.getGtsCategory(), true, true));
        indexFields.add(SearchManager.makeField("_overriddenGtsCategory", pm.getOverridenGtsCategory(), true, true));
        indexFields.add(SearchManager.makeField("_fncPattern", pm.getFncPattern(), true, true));
        indexFields.add(SearchManager.makeField("_overriddenFncPattern", pm.getOverridenFncPattern(), true, true));
        indexFields.add(SearchManager.makeField("_fileExtension", pm.getFileExtension(), true, true));
        indexFields.add(SearchManager.makeField("_overriddenFileExtension", pm.getOverridenFileExtension(), true, true));
        indexFields.add(SearchManager.makeField("_dataPolicy", pm.getDataPolicy(), true, true));

        indexFields.add(SearchManager.makeField("_localDataResource", pm.getLocalDataSource(), true, true));
        indexFields.add(SearchManager.makeField("_originator", pm.getOriginator(), true, true));
        String creationDate = (pm.getCreationDate() != null)?pm.getCreationDate().toString():"";
        indexFields.add(SearchManager.makeField("_creationDate", creationDate, true, true));
        indexFields.add(SearchManager.makeField("_isFed", pm.isIngested().toString(), true, true));
        indexFields.add(SearchManager.makeField("_priority", pm.getPriority().toString(), true, true));

        return indexFields;

    }
}
