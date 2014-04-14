package org.fao.geonet.kernel.mef;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jeeves.server.context.ServiceContext;

import org.apache.commons.io.FileUtils;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;
import org.fao.geonet.repository.MetadataRepository;
import org.jdom.Element;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test MEF.
 *
 * User: Jesse
 * Date: 10/15/13
 * Time: 8:53 PM
 */
public class MEFLibIntegrationTest extends AbstractCoreIntegrationTest {
    @Autowired
    MetadataRepository _metadataRepo;

    @Test
    public void testDoImportMefVersion1() throws Exception {
        ServiceContext context = createServiceContext();
        User admin = loginAsAdmin(context);
        ImportMetadata importMetadata = new ImportMetadata(this, context).invoke();
        List<String> metadataIds = importMetadata.getMetadataIds();

        assertEquals(1, metadataIds.size());

        final Metadata metadata = _metadataRepo.findOne(metadataIds.get(0));

        assertNotNull(metadata);
        assertEquals(admin.getId(), metadata.getSourceInfo().getOwner());
    }

    @Test
    public void testDoImportMefVersion2() throws Exception {
        ServiceContext context = createServiceContext();

        final File resource = new File(MEFLibIntegrationTest.class.getResource("mef2-example-2md.zip").getFile());

        final User admin = loginAsAdmin(context);

        Element params = new Element("request");
        final List<String> metadataIds = MEFLib.doImport(params, context, resource, getStyleSheets());
        assertEquals(2, metadataIds.size());

        for (String metadataId : metadataIds) {

            final Metadata metadata = _metadataRepo.findOne(metadataId);

            assertNotNull(metadata);
            assertEquals(admin.getId(), metadata.getSourceInfo().getOwner());
        }
    }

    @Test
    @Ignore
    public void testDoExport() throws Exception {
        fail("to implement");
    }

    @Test
    @Ignore
    public void testDoMEF2Export() throws Exception {
        fail("to implement");
    }

    public static class ImportMetadata {
        private final AbstractCoreIntegrationTest testClass;
        private ServiceContext context;
        private List<String> metadataIds = new ArrayList<String>();
        private List<String> mefFilesToLoad = new ArrayList<String>();

        public ImportMetadata(AbstractCoreIntegrationTest testClass, ServiceContext context) {
            this.context = context;
            this.testClass = testClass;
            mefFilesToLoad.add("mef1-example.mef");

        }

        public List<String> getMetadataIds() {
            return metadataIds;
        }

        public ImportMetadata invoke() throws Exception {
            testClass.loginAsAdmin(context);

            for (String mefFile : mefFilesToLoad) {
                InputStream stream = MEFLibIntegrationTest.class.getResourceAsStream(mefFile);
                final File mefTestFile = File.createTempFile("mefTestFile", ".mef");
                FileUtils.copyInputStreamToFile(stream, mefTestFile);
                stream.close();

                Element params = new Element("request");
                metadataIds.addAll(MEFLib.doImport(params, context, mefTestFile, testClass.getStyleSheets()));
            }
            return this;
        }

        public List<String> getMefFilesToLoad() {
            return mefFilesToLoad;
        }
    }
}
