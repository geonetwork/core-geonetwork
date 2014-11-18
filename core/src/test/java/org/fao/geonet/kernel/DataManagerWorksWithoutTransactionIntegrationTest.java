package org.fao.geonet.kernel;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test that the aspect defined work correctly.
 * <p/>
 * Created by Jesse on 3/10/14.
 */
public class DataManagerWorksWithoutTransactionIntegrationTest extends AbstractCoreIntegrationTest {
    @Autowired
    MetadataRepository metadataRepository;
    @Autowired
    DataManager dataManager;

    @Autowired
    MetadataCategoryRepository metadataCategoryRepository;

    @Test
    public void testDataManagerCutpoints() throws Exception {
        TransactionlessTesting.get().run
                (new TestTask() {
                    @Override
                    public void run() throws Exception {
                        final ServiceContext serviceContext = createServiceContext();
                        loginAsAdmin(serviceContext);

                        final String metadataCategory = metadataCategoryRepository.findAll().get(0).getName();
                        final Element sampleMetadataXml = getSampleMetadataXml();
                        final UserSession userSession = serviceContext.getUserSession();
                        final int userIdAsInt = userSession.getUserIdAsInt();
                        final DataManager dm = DataManagerWorksWithoutTransactionIntegrationTest.this.dataManager;
                        String schema = dm.autodetectSchema(sampleMetadataXml);
                        final String mdId = dm.insertMetadata(serviceContext, schema, sampleMetadataXml,
                                UUID.randomUUID().toString(), userIdAsInt, "2", "source",
                                MetadataType.METADATA.codeString, null, metadataCategory, new ISODate().getDateAndTime(),
                                new ISODate().getDateAndTime(), false, false);
                        Element newMd = new Element(sampleMetadataXml.getName(), sampleMetadataXml.getNamespace()).addContent(new Element("fileIdentifier",
                                GMD).addContent(new Element("CharacterString", GCO)));

                        Metadata updateMd = dm.updateMetadata(serviceContext, mdId, newMd, false, false, false, "eng",
                                new ISODate().getDateAndTime(), false);
                        assertNotNull(updateMd);
                        final boolean hasNext = updateMd.getCategories().iterator().hasNext();
                        assertTrue(hasNext);
                    }
                });

    }


    @Test
    public void testSetHarvesterData() throws Exception {
        TransactionlessTesting.get().run
                (new TestTask() {
                    @Override
                    public void run() throws Exception {
                        final ServiceContext serviceContext = createServiceContext();
                        loginAsAdmin(serviceContext);

                        final DataManagerWorksWithoutTransactionIntegrationTest test =
                                DataManagerWorksWithoutTransactionIntegrationTest.this;
                        final int metadataId =  DataManagerIntegrationTest.importMetadata(test, serviceContext);

                        final DataManager dm = DataManagerWorksWithoutTransactionIntegrationTest.this.dataManager;
                        DataManagerIntegrationTest.doSetHarvesterDataTest(DataManagerWorksWithoutTransactionIntegrationTest.this.metadataRepository, dm, metadataId);
                    }
                });

    }

}
