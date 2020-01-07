package org.fao.geonet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.index.GeonetworkMultiReader;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class IndexedMetadataFetcher {

    private SearchManager searchManager;
    private Map<String, Object> map;

    public IndexedMetadataFetcher(SearchManager searchManager) {
        this.searchManager = searchManager;
    }

    public void getApplicationProfileFromLuceneIndex(String uuid, String typeName) throws IOException {
        TermQuery query = new TermQuery(new Term("_uuid", uuid));

        IndexAndTaxonomy indexAndTaxonomy = searchManager.getIndexReader(null, -1);
        GeonetworkMultiReader reader = indexAndTaxonomy.indexReader;

        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs tdocs = searcher.search(query, 1);

        Optional<String> optAppProfile = reader.document(tdocs.scoreDocs[0].doc).getFields().stream()
            .filter(xIndexableField -> xIndexableField.name().equalsIgnoreCase("link"))
            .map(x -> x.stringValue().split("\\|"))
            .filter(x -> x.length >= 6)
            .filter(x -> x[0].equals(typeName))
            .filter(x -> x[3].equals("OGC:WFS"))
            .map(x -> x[6]) // will contain the applicationProfile if it is defined.
            .findFirst();

        if (optAppProfile.isPresent()) {
            this.setIndex(optAppProfile.get());
        }
    }

    public List<String> getTreeField() {
        return (List<String>) map.get("treeFields");
    }

    public Map<String, String> getTokenizedField() {
        return (Map<String, String>) map.get("tokenizedFields");
    }

    protected void setIndex(String toParse) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(toParse.getBytes(Charset.forName("UTF-8")));
        DataInput mdIndexAsDataInput = new DataInputStream(inputStream);
        ObjectReader reader = new ObjectMapper().readerFor(Map.class);
        map = reader.readValue(mdIndexAsDataInput);
    }
}
