package org.fao.geonet.services.metadata;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import jeeves.constants.Jeeves;
import jeeves.server.context.ServiceContext;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest.ImportMetadata;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.specification.GroupSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.services.metadata.Publish.PublishReport;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PublishTest extends AbstractServiceIntegrationTest {

    private List<String> metadataIds;
    @Autowired
    private Publish publishService;
    @Autowired
    private OperationAllowedRepository allowedRepository;
    @Autowired
    private GroupRepository groupRepository;

    private int sampleGroupId;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private SearchManager searchManager;


    @Before
    public void setUp() throws Exception {
        final List<Group> all = groupRepository.findAll(Specifications.not(GroupSpecs.isReserved()));
        this.sampleGroupId = all.get(0).getId();
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final ImportMetadata importMetadata = new ImportMetadata(this, serviceContext);
        importMetadata.getMefFilesToLoad().add("mef2-example-2md.zip");
        importMetadata.invoke();

        this.metadataIds = importMetadata.getMetadataIds();
        publishService.testing = true;
    }

    @Test
    public void testPublishSingle() throws Exception {
        final String metadataId = metadataIds.get(0);

        allowedRepository.deleteAll();
        dataManager.indexMetadata(metadataId, true);

        MockHttpServletRequest request = new MockHttpServletRequest();

        PublishReport report = publishService.publish("eng", request, metadataId, false);
        assertCorrectReport(report, 1, 0, 0, 0);
        assertPublishedInIndex(true, metadataId);

        report = publishService.publish("eng", request, metadataId, false);
        assertCorrectReport(report, 0, 0, 1, 0);
        assertPublishedInIndex(true, metadataId);

        allowedRepository.deleteAll();
        dataManager.indexMetadata(metadataId, true);
        assertPublishedInIndex(false, metadataId);

        SecurityContextHolder.clearContext();

        report = publishService.publish("eng", request, metadataId, false);
        assertCorrectReport(report, 0, 0, 0, 1);
        assertPublishedInIndex(false, metadataId);

        int iMetadataId = Integer.parseInt(metadataId);
        final int downloadId = ReservedOperation.download.getId();
        final int allGroupId = ReservedGroup.all.getId();
        allowedRepository.save(new OperationAllowed(new OperationAllowedId(iMetadataId, allGroupId, downloadId)));
        final int featuredId = ReservedOperation.featured.getId();
        allowedRepository.save(new OperationAllowed(new OperationAllowedId(iMetadataId, allGroupId, featuredId)));
        allowedRepository.save(new OperationAllowed(new OperationAllowedId(iMetadataId, sampleGroupId, featuredId)));

        report = publishService.publish("eng", request, metadataId, false);
        assertCorrectReport(report, 0, 0, 0, 1);
        assertEquals(3, allowedRepository.count());
        assertNotNull(allowedRepository.findOneById_GroupIdAndId_MetadataIdAndId_OperationId(allGroupId, iMetadataId, downloadId));
        assertNotNull(allowedRepository.findOneById_GroupIdAndId_MetadataIdAndId_OperationId(allGroupId, iMetadataId, featuredId));
        assertNotNull(allowedRepository.findOneById_GroupIdAndId_MetadataIdAndId_OperationId(sampleGroupId, iMetadataId, featuredId));
    }

    @Test
    public void testPublishMultiple() throws Exception {
        allowedRepository.deleteAll();

        MockHttpServletRequest request = new MockHttpServletRequest();

        String ids = Joiner.on(",").join(this.metadataIds);

        PublishReport report = publishService.publish("eng", request, ids, false);
        assertCorrectReport(report, 3, 0, 0, 0);

        report = publishService.publish("eng", request, ids, false);
        assertCorrectReport(report, 0, 0, 3, 0);

        allowedRepository.deleteAll(OperationAllowedSpecs.hasMetadataId(this.metadataIds.get(0)));

        report = publishService.publish("eng", request, ids, false);
        assertCorrectReport(report, 1, 0, 2, 0);

        allowedRepository.deleteAll(OperationAllowedSpecs.hasMetadataId(this.metadataIds.get(0)));

        SecurityContextHolder.clearContext();

        report = publishService.publish("eng", request, ids, false);
        assertCorrectReport(report, 0, 0, 2, 1);

    }

    @Test
    public void testUnpublishSingle() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        final String metadataId = metadataIds.get(0);

        PublishReport report = publishService.unpublish("eng", request, metadataId, false);
        assertCorrectReport(report, 0, 1, 0, 0);


        report = publishService.unpublish("eng", request, metadataId, false);
        assertCorrectReport(report, 0, 0, 1, 0);

        report = publishService.publish("eng", request, metadataId, false);
        assertCorrectReport(report, 1, 0, 0, 0);

        SecurityContextHolder.clearContext();

        report = publishService.unpublish("eng", request, metadataId, false);
        assertCorrectReport(report, 0, 0, 0, 1);
    }
    @Test
    public void testUnpublishMultiple() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String ids = Joiner.on(",").join(this.metadataIds);

        PublishReport report = publishService.unpublish("eng", request, ids, false);
        assertCorrectReport(report, 0, 3, 0, 0);

        report = publishService.unpublish("eng", request, ids, false);
        assertCorrectReport(report, 0, 0, 3, 0);

        report = publishService.publish("eng", request, this.metadataIds.get(0), false);
        assertCorrectReport(report, 1, 0, 0, 0);

        report = publishService.unpublish("eng", request, ids, false);
        assertCorrectReport(report, 0, 1, 2, 0);

        report = publishService.publish("eng", request, ids, false);
        assertCorrectReport(report, 3, 0, 0, 0);

        SecurityContextHolder.clearContext();

        report = publishService.unpublish("eng", request, ids, false);
        assertCorrectReport(report, 0, 0, 0, 3);
    }

    @Test
    public void testUnpublishSelection() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        request.getSession(true).setAttribute(Jeeves.Elem.SESSION, context.getUserSession());

        SelectionManager sm = SelectionManager.getManager(context.getUserSession());
        final HashSet<String> selection = Sets.newHashSet(toUUID(metadataIds.get(0)), toUUID(metadataIds.get(1)));
        sm.addAllSelection(SelectionManager.SELECTION_METADATA, selection);

        PublishReport report = publishService.unpublish("eng", request, null, false);
        assertCorrectReport(report, 0, 2, 0, 0);

        report = publishService.unpublish("eng", request, null, false);
        assertCorrectReport(report, 0, 0, 2, 0);

        report = publishService.publish("eng", request, this.metadataIds.get(0), false);
        assertCorrectReport(report, 1, 0, 0, 0);

        report = publishService.unpublish("eng", request, null, false);
        assertCorrectReport(report, 0, 1, 1, 0);

        report = publishService.publish("eng", request, null, false);
        assertCorrectReport(report, 2, 0, 0, 0);

        SecurityContextHolder.clearContext();

        report = publishService.unpublish("eng", request, null, false);
        assertCorrectReport(report, 0, 0, 0, 2);
    }

    private String toUUID(String mdId) throws Exception {
        return this.dataManager.getMetadataUuid(mdId);
    }

    private void assertCorrectReport(PublishReport report, int published, int unpublished, int unmodified, int disallowed) {
        assertEquals(report.toString(), published, report.getPublished());
        assertEquals(report.toString(), unmodified, report.getUnmodified());
        assertEquals(report.toString(), unpublished, report.getUnpublished());
        assertEquals(report.toString(), disallowed, report.getDisallowed());
    }

    private void assertPublishedInIndex(boolean published, String metadataId) throws IOException {
        try (IndexAndTaxonomy indexReader = this.searchManager.getIndexReader(null, -1)) {
            final IndexSearcher searcher = new IndexSearcher(indexReader.indexReader);
            final TopDocs docs = searcher.search(new TermQuery(new Term(Geonet.IndexFieldNames.ID, metadataId)), 1);
            final Document document = indexReader.indexReader.document(docs.scoreDocs[0].doc);
            for (ReservedGroup reservedGroup : Lists.newArrayList(ReservedGroup.all, ReservedGroup.intranet)) {
                    final String[] values = document.getValues(Geonet.IndexFieldNames.GROUP_PUBLISHED);
                    final String expectedInIndex = Geonet.IndexFieldNames.GROUP_PUBLISHED + ":" + reservedGroup;
                    assertEquals(expectedInIndex + " is not in " + Arrays.asList(values),
                            published, Arrays.asList(values).contains("" + reservedGroup.name()));
            }
        }
    }
}