package org.openwis.metadata.product;

import org.apache.lucene.document.Document;
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
import org.openwis.products.client.ProductMetadata;
import org.openwis.util.GeonetOpenwis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;

/**
 * ProductMetadata indexer for Lucene.
 *
 * @author Jose García
 */
@Component
public class ProductMetadataIndexerLucene implements IProductMetadataIndexer {
    @Autowired
    SearchManager searchManager;

    @Autowired
    LuceneIndexLanguageTracker tracker;

    @Override
    public void index(ProductMetadata pm) {
        Collection<CategoryPath> categories = new HashSet<CategoryPath>();

        final IndexAndTaxonomy indexAndTaxonomy;

        try {
            indexAndTaxonomy = searchManager.getNewIndexReader("eng");
            GeonetworkMultiReader reader = indexAndTaxonomy.indexReader;
            IndexSearcher searcher = new IndexSearcher(reader);
            TermQuery query = new TermQuery(new Term(Geonet.IndexFieldNames.UUID, pm.getUrn()));
            int numberOfHits = 1;

            Filter filter = NoFilterFilter.instance();
            TopDocs tdocs = searcher.search(query, filter, numberOfHits, Sort.INDEXORDER);

            for( ScoreDoc sdoc : tdocs.scoreDocs ) {
                Document doc = searcher.doc(sdoc.doc);

                addIndexField(doc, "_process", pm.getProcess());
                addIndexField(doc, "_gtsCategory", pm.getGtsCategory());
                addIndexField(doc, "_overriddenGtsCategory", pm.getOverridenGtsCategory());
                addIndexField(doc, "_fncPattern", pm.getFncPattern());
                addIndexField(doc, "_overriddenFncPattern", pm.getOverridenFncPattern());
                addIndexField(doc, "_fileExtension", pm.getOverridenFileExtension());
                addIndexField(doc, "_overriddenFileExtension", pm.getOverridenFileExtension());
                addIndexField(doc, "_dataPolicy", pm.getDataPolicy());
                addIndexField(doc, "_localDataResource", pm.getLocalDataSource());
                addIndexField(doc, "_originator", pm.getOriginator());
                String creationDate = (pm.getCreationDate() != null)?pm.getCreationDate().toString():"";
                addIndexField(doc, "_creationDate", creationDate);
                addIndexField(doc, "_isFed", pm.isFed().toString());
                addIndexField(doc, "_isIngested", pm.isIngested().toString());
                // TODO: Check if required, seem not used
                //addIndexField(doc, "_isStopGap", pm.isStopGap().toString());

                tracker.deleteDocuments(new Term(Geonet.IndexFieldNames.UUID, pm.getUrn()));
                tracker.addDocument(new IndexInformation("eng", doc, categories));

            }

            if (tdocs.scoreDocs.length > 0) {
                tracker.commit();
                tracker.maybeRefreshBlocking();
            }
        } catch (Exception ex) {
            Log.error(GeonetOpenwis.PRODUCT_METADATA, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }

    }

    private void addIndexField(Document doc, String fieldName, String fieldValue) {
        doc.removeField(fieldName);

        if (fieldValue == null) fieldValue = "";
        doc.add(new StoredField(fieldName, fieldValue));
    }
}
