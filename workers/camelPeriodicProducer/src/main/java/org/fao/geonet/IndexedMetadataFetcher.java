/*
 * Copyright (C) 2001-2015 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.elasticsearch.action.search.SearchResponse;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.search.EsSearchManager;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class IndexedMetadataFetcher {

    private EsSearchManager searchManager;
    private Map<String, Object> map;

    public IndexedMetadataFetcher(EsSearchManager searchManager) {
        this.searchManager = searchManager;
    }

    public void getApplicationProfileFromLuceneIndex(String uuid, String typeName) throws Exception {
        EsRestClient client = ApplicationContextHolder.get().getBean(EsRestClient.class);
        Set<String> fields =
            new HashSet<>(Arrays.asList(new String[]{"link"}));
        SearchResponse searchResponse = client.query(searchManager.getDefaultIndex(),
            "+linkProtocol:\"OGC:WFS\"", null,
            fields,
            0, 10000);

        searchResponse.getHits().forEach(doc -> {
            System.out.println(doc.getId());
        });
//        TermQuery query = new TermQuery(new Term("_uuid", uuid));
//
//        IndexAndTaxonomy indexAndTaxonomy = searchManager.getIndexReader(null, -1);
//
//        try {
//            GeonetworkMultiReader reader = indexAndTaxonomy.indexReader;
//
//
//            IndexSearcher searcher = new IndexSearcher(reader);
//            TopDocs tdocs = searcher.search(query, 1);
//
//            Optional<String> optAppProfile = reader.document(tdocs.scoreDocs[0].doc).getFields().stream()
//                .filter(xIndexableField -> xIndexableField.name().equalsIgnoreCase("link"))
//                .map(x -> x.stringValue().split("\\|"))
//                .filter(x -> x.length >= 7)
//                .filter(x -> x[0].equals(typeName))
//                .filter(x -> x[3].equals("OGC:WFS"))
//                .map(x -> x[6]) // will contain the applicationProfile if it is defined.
//                .findFirst();
//
//            if (optAppProfile.isPresent()) {
//                this.setIndex(optAppProfile.get());
//            }
//        }
//        finally {
//            searchManager.releaseIndexReader(indexAndTaxonomy);
//        }
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
