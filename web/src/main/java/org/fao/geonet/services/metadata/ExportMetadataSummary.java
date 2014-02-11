package org.fao.geonet.services.metadata;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.util.PriorityQueue;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.jdom.Element;
import scala.xml.Atom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipOutputStream;

/**
 * Export a summary.
 *
 * Created by Jesse on 2/11/14.
 */
public class ExportMetadataSummary implements Service {
    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {

    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        final SearchManager searchManager = context.getApplicationContext().getBean(SearchManager.class);
        final SelectionManager selectionManager = SelectionManager.getManager(context.getUserSession());

        IndexAndTaxonomy newIndexReader = null;

        final File summaryFile = File.createTempFile("summary", ".zip");
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(summaryFile));
            newIndexReader = searchManager.getNewIndexReader(context.getLanguage());

            IndexSearcher searcher = new IndexSearcher(newIndexReader.indexReader);

            final Set<String> fields = new LinkedHashSet<String>();
            fields.add("uuid");
            fields.add("identifier");
            fields.add("_defaultTitle");
            fields.add("owner");
            fields.add("groupowner");
            fields.add("_userinfo");


            StringBuilder builder = new StringBuilder();

            for (String field : fields) {
                if (builder.length() > 0) {
                    builder.append(',');
                }
                builder.append(field);
            }
            builder.append('\n');

            final Set<String> selection = selectionManager.getSelection(SelectionManager.SELECTION_METADATA);
            for (String metadataUUID : selection) {
                final ZipOutputStream finalOut = out;
                final PriorityQueue<ScoreDoc> queue = new PriorityQueue<ScoreDoc>(10) {
                    @Override
                    protected boolean lessThan(ScoreDoc a, ScoreDoc b) {
                        return false;
                    }
                };

                searcher.search(new TermQuery(new Term("uuid", metadataUUID)), new TopDocsCollector<ScoreDoc>(queue) {
                    public AtomicReaderContext context;

                    @Override
                    public void setScorer(Scorer scorer) throws IOException {
                        // ignore
                    }

                    @Override
                    public void collect(int doc) throws IOException {
                        final Document document = context.reader().document(doc, fields);

                        StringBuilder builder = new StringBuilder();

                        for (String field : fields) {
                            if (builder.length() > 0) {
                                builder.append(',');
                            }
                            builder.append(document.get(field));
                        }
                        builder.append('\n');
                        finalOut.write(builder.toString().getBytes("UTF-8"));
                    }

                    @Override
                    public void setNextReader(AtomicReaderContext context) throws IOException {
                        this.context = context;
                    }

                    @Override
                    public boolean acceptsDocsOutOfOrder() {
                        return true;
                    }
                });
            }

            return BinaryFile.encode(200, summaryFile.getPath(), true);
        } finally {
            IOUtils.closeQuietly(out);
            searchManager.releaseIndexReader(newIndexReader);
        }
    }
}
