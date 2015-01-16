package org.fao.geonet.kernel.search;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopFieldCollector;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.Assert.assertNull;

public class LuceneSearcherPresentTest extends AbstractCoreIntegrationTest {
    @Autowired
    private SearchManager searchManager;
    @Autowired
    private DataManager dataManager;

    @Test
    public void testBuildPrivilegesMetadataInfo() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        final MEFLibIntegrationTest.ImportMetadata importMetadata = new MEFLibIntegrationTest.ImportMetadata(this, serviceContext);
        importMetadata.invoke();

        Element info = new Element("info", Geonet.Namespaces.GEONET);
        IndexAndTaxonomy indexAndTaxonomy = searchManager.getNewIndexReader("eng");

        try {
            TopFieldCollector tfc = TopFieldCollector.create(Sort.INDEXORDER, 1000, true, false, false, true);
            IndexSearcher searcher = new IndexSearcher(indexAndTaxonomy.indexReader);
            searcher.search(new MatchAllDocsQuery(), tfc);

            final Document doc = indexAndTaxonomy.indexReader.document(tfc.topDocs().scoreDocs[0].doc);
            LuceneSearcher.buildPrivilegesMetadataInfo(serviceContext, doc, info);

            assertEqualsText("true", info, "edit");
            assertEqualsText("true", info, "owner");
            try {
                assertEqualsText("true", info, "isPublishedToAll");
            } catch (Exception e) {
                throw new AssertionError("Expected isPublishToAll to be true :" + doc);
            }
            assertEqualsText("true", info, "view");
            assertEqualsText("true", info, "notify");
            assertEqualsText("true", info, "download");
            assertEqualsText("true", info, "dynamic");
            assertEqualsText("true", info, "featured");
        } finally {
            searchManager.releaseIndexReader(indexAndTaxonomy);
        }


        final String mdId = importMetadata.getMetadataIds().get(0);
        dataManager.unsetOperation(serviceContext, mdId, "" + ReservedGroup.all.getId(), ReservedOperation.editing);
        dataManager.indexMetadata(mdId, true);

        indexAndTaxonomy = searchManager.getNewIndexReader("eng");
        try {
            TopFieldCollector tfc = TopFieldCollector.create(Sort.INDEXORDER, 1000, true, false, false, true);
            IndexSearcher searcher = new IndexSearcher(indexAndTaxonomy.indexReader);
            searcher.search(new MatchAllDocsQuery(), tfc);

            final Document doc = indexAndTaxonomy.indexReader.document(tfc.topDocs().scoreDocs[0].doc);

            serviceContext.setUserSession(new UserSession());
            SecurityContextHolder.clearContext();
            info.removeContent();
            LuceneSearcher.buildPrivilegesMetadataInfo(serviceContext, doc, info);

            assertNull(Xml.selectElement(info, "edit"));
            assertNull(Xml.selectElement(info, "owner"));

            assertEqualsText("false", info, "guestdownload");
            assertEqualsText("true", info, "isPublishedToAll");
            assertEqualsText("true", info, "view");
            assertEqualsText("false", info, "notify");
            assertEqualsText("false", info, "download");
            assertEqualsText("false", info, "dynamic");
            assertEqualsText("false", info, "featured");
        } finally {
            searchManager.releaseIndexReader(indexAndTaxonomy);
        }
    }


}