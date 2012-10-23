package org.fao.geonet.kernel.search.index;

import java.io.IOException;

import jeeves.utils.Log;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.NRTManager.TrackingIndexWriter;
import org.fao.geonet.constants.Geonet;

public class LuceneIndexWriterFactory {

    private LuceneIndexLanguageTracker tracker;

    public LuceneIndexWriterFactory( LuceneIndexLanguageTracker tracker ) {
        this.tracker = tracker;
    }

    public void commit() throws Exception {
        tracker.commit();
    }

    public void addDocument( String locale, Document doc ) throws Exception {
        if(Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
            Log.debug(Geonet.INDEX_ENGINE, "Adding document to "+locale+" index");
        }
        tracker.addDocument(locale, doc);
    }

    public void deleteDocuments( final Term term ) throws Exception {
        if(Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
            Log.debug(Geonet.INDEX_ENGINE, "deleting term '"+term+"' from index");
        }
        tracker.withWriter(new Function() {
            @Override
            public void apply(TrackingIndexWriter input) throws CorruptIndexException, IOException {
                    input.deleteDocuments(term);
            }
        });
    }

    public void createDefaultLocale() throws IOException {
        tracker.open(Geonet.DEFAULT_LANGUAGE);
    }
}