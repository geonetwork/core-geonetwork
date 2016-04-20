/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.search;

import org.apache.commons.lang.NotImplementedException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.StreamingResponseCallback;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SolrSearchManagerTest {
    private SolrSearchManager manager;
    private MockSolrClient client;

    @BeforeClass
    public static void setupSaxon() {
        TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");
    }

    @AfterClass
    public static void shutdownSaxon() {
        TransformerFactoryFactory.init(null);
    }

    @Before
    public void setupManager() {
        manager = new SolrSearchManager();
        client = new MockSolrClient();
        manager.setClient(client);
    }

    private static Path getSchemaDir() {
        Path tmp = AbstractCoreIntegrationTest.getClassFile(SolrSearchManagerTest.class).toPath();
        while (tmp != null && !Files.exists(tmp.resolve("schemas/iso19139"))) {
            tmp = tmp.getParent();
        }
        return tmp.resolve("schemas/iso19139/src/main/plugin/iso19139");
    }

    @Test
    public void testIndex() throws Exception {
        Element metadata = Xml.loadFile(SolrSearchManagerTest.class.getResource("templated-keyword.iso19139.xml"));
        Element moreFields = new Element("toto");
        SolrSearchManager.addField(moreFields, "test1", "TEST1", true, false);

        manager.index(getSchemaDir(), metadata, "tutu", moreFields.getChildren(), null, "", true);

        assertEquals(1, client.addedDocs.size());
        SolrInputDocument doc = client.addedDocs.get(0);
        assertNull(doc.getFieldValue(IndexFields.INDEXING_ERROR_FIELD));
        Assert.assertEquals("TEST1", doc.getFieldValue("test1"));
        Assert.assertEquals("tutu", doc.getFieldValue(SolrSearchManager.ID));
        Assert.assertEquals("metadata", doc.getFieldValue(SolrSearchManager.DOC_TYPE));
        assertEquals("Hierarchical facet test template", doc.getFieldValue("resourceTitle"));
    }

    @Test
    public void testGetDocsChangeDate() throws Exception {
        Map<String, String> map = manager.getDocsChangeDate();
        Map<String, String> expected = new HashMap<>();
        expected.put("toto", "1970");
        expected.put("tutu", "1971");
        assertEquals(expected, map);
    }

    @Test
    public void testConvertInteger() throws IOException, SolrServerException {
        assertEquals(Integer.valueOf(23), SolrSearchManager.convertInteger(Integer.valueOf(23)));
        assertEquals(Integer.valueOf(23), SolrSearchManager.convertInteger(Long.valueOf(23)));
        assertEquals(Integer.valueOf(23), SolrSearchManager.convertInteger(Integer.valueOf(23)));
        assertEquals(null, SolrSearchManager.convertInteger(null));
    }

    @Test
    public void testGetDocField() throws Exception {
        String value = (String) manager.getDocFieldValue(
                                            "+" + SolrSearchManager.ID + ":toto",
                                            SolrSearchManager.ID).getFieldValue(SolrSearchManager.ID);
        assertEquals("toto", value);
    }

    private static class MockSolrClient extends SolrClient {
        private final List<SolrInputDocument> addedDocs = new ArrayList<>();

        @Override
        public NamedList<Object> request(SolrRequest request, String collection) throws SolrServerException, IOException {
            throw new NotImplementedException();
        }

        @Override
        public void shutdown() {
        }

        @Override
        public UpdateResponse add(String collection, SolrInputDocument doc, int commitWithinMs) throws SolrServerException, IOException {
            addedDocs.add(doc);
            return null;
        }

        @Override
        public UpdateResponse commit(String collection, boolean waitFlush, boolean waitSearcher) throws SolrServerException, IOException {
            return null;
        }

        @Override
        public QueryResponse queryAndStreamResponse(String collection, SolrParams params, StreamingResponseCallback callback) {
            SolrQuery query = (SolrQuery) params;
            Integer start = query.getStart();
            callback.streamDocListInfo(2, start, 0.0f);
            SolrDocument doc = new SolrDocument();
            if (start == 0) {
                doc.setField(SolrSearchManager.ID, "toto");
                doc.setField("_changeDate", "1970");
                callback.streamSolrDocument(doc);
            } else {
                doc.setField(SolrSearchManager.ID, "tutu");
                doc.setField("_changeDate", "1971");
                callback.streamSolrDocument(doc);
            }
            return null;
        }
    }
}
