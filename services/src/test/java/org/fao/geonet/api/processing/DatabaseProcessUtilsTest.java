package org.fao.geonet.api.processing;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.processing.report.ErrorReport;
import org.fao.geonet.api.processing.report.MetadataReplacementProcessingReport;
import org.fao.geonet.api.processing.report.Report;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DatabaseProcessUtilsTest extends AbstractServiceIntegrationTest {

    List<String> uuids = new ArrayList();
    String metadataId = null;
    ServiceContext context;

    @Autowired
    private IMetadataUtils repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Before
    public void loadSamples() throws Exception {
        context = createServiceContext();
        loginAsAdmin(context);

        final MEFLibIntegrationTest.ImportMetadata importMetadata =
            new MEFLibIntegrationTest.ImportMetadata(this, context);
        importMetadata.getMefFilesToLoad().add("mef2-example-2md.zip");
        importMetadata.invoke();
        List<String> importedRecordIds = importMetadata.getMetadataIds();

        // Check record are imported
        for (String id : importedRecordIds) {
            final String uuid = repository.findOne(Integer.parseInt(id)).getUuid();
            uuids.add(uuid);
            if (uuid.equals("0e1943d6-64e8-4430-827c-b465c3e9e55c")) {
                metadataId = id;
            }
        }
        assertEquals(3, repository.count());
    }

    @Test
    public void testProcess() throws Exception {
        MetadataReplacementProcessingReport report = new MetadataReplacementProcessingReport("test");
        String searchValue = "Localities in Victoria";
        String replaceBy = "Localities in Hobart";
        String searchPattern = "Localities in (.*)";
        String replacePatternBy = "Points in $1";

        boolean save = false, index = false, updateDateStamp = false;
        AbstractMetadata record = repository.findOne(metadataId);
        assertTrue(record.getData().contains(searchValue));

        Element updatedRecord = DatabaseProcessUtils.process(context, metadataId,
            false, searchValue, replaceBy,
            "", save, index, updateDateStamp, report);

        assertEquals(1, report.getNumberOfRecordsProcessed());
        assertFalse(Xml.getString(updatedRecord).contains(searchValue));
        assertTrue(Xml.getString(updatedRecord).contains(replaceBy));

        AbstractMetadata recordNotSavedInDb = repository.findOne(metadataId);
        assertTrue(recordNotSavedInDb.getData().contains(searchValue));

        save = true;
        DatabaseProcessUtils.process(context, metadataId,
            false, searchValue, replaceBy,
            "", save, index, updateDateStamp, report);
        entityManager.flush();

        AbstractMetadata recordSavedInDb = repository.findOne(metadataId);
        assertFalse(recordSavedInDb.getData().contains(searchValue));
        assertTrue(recordSavedInDb.getData().contains(replaceBy));

        updatedRecord = DatabaseProcessUtils.process(context, metadataId,
            true, searchPattern, replacePatternBy,
            "", save, index, updateDateStamp, report);

        assertTrue(Xml.getString(updatedRecord).contains("Points in Hobart"));

        MetadataReplacementProcessingReport reportWithError = new MetadataReplacementProcessingReport("test");
        DatabaseProcessUtils.process(context, metadataId,
            false, "<gco:Character", "",
            "", save, index, updateDateStamp, reportWithError);

        assertEquals(1, reportWithError.getNumberOfRecordsWithErrors());
        List<Report> reports = reportWithError.getMetadataErrors().get(Integer.parseInt(metadataId));
        assertEquals(1, reports.size());
        assertTrue( reports.get(0) instanceof ErrorReport);
        String errorStack = ((ErrorReport) reports.get(0)).getStack();
        assertNotNull(errorStack);
        assertTrue("Did not contain JDOMParseException, but contained " + errorStack, errorStack.contains("JDOMParseException"));
    }
}
