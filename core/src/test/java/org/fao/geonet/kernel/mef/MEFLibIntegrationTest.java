package org.fao.geonet.kernel.mef;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.ZipUtil;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.IO;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        assertEquals(admin.getId(), metadata.getSourceInfo().getOwner().intValue());
    }

    @Test
    public void testDoImportMefVersion2() throws Exception {
        ServiceContext context = createServiceContext();

        final Path resource = IO.toPath(MEFLibIntegrationTest.class.getResource("mef2-example-2md.zip").toURI());

        final User admin = loginAsAdmin(context);

        Element params = new Element("request");
        final List<String> metadataIds = MEFLib.doImport(params, context, resource, getStyleSheets());
        assertEquals(2, metadataIds.size());

        for (String metadataId : metadataIds) {

            final Metadata metadata = _metadataRepo.findOne(metadataId);

            assertNotNull(metadata);
            assertEquals(admin.getId(), metadata.getSourceInfo().getOwner().intValue());
        }
    }

    public static class ImportMetadata {
        private final AbstractCoreIntegrationTest testClass;
        private ServiceContext context;
        private List<String> metadataIds = new ArrayList<>();
        private List<String> mefFilesToLoad = new ArrayList<>();
        private String uuidAction;

        public ImportMetadata(AbstractCoreIntegrationTest testClass, ServiceContext context) {
            this.context = context;
            this.testClass = testClass;
            mefFilesToLoad.add("mef1-example.mef");
            this.uuidAction = Params.NOTHING;

        }

        public void setUuidAction(String uuidAction) {
            this.uuidAction = uuidAction;
        }

        public List<String> getMetadataIds() {
            return metadataIds;
        }

        public ImportMetadata invoke() throws Exception {
            return invoke(1);
        }
        public ImportMetadata invoke(int iterations) throws Exception {
            assertTrue("iterations must be greater than 0 but was: " + iterations, iterations > 0);
            testClass.loginAsAdmin(context);

            int remainingFilesToImport = iterations * mefFilesToLoad.size();
            int numberOfImported = 0;

            for (String mefFile : mefFilesToLoad) {
                final Path mefTestFile = Files.createTempFile("mefTestFile", ".mef");
                URI uri = MEFLibIntegrationTest.class.getResource(mefFile).toURI();
                if (uri.toString().startsWith("jar:")) {
                    int exclamation = uri.toString().indexOf("!", 2);
                    URI zipFsUri = new URI(uri.toString().substring("jar:".length(), exclamation));
                    //noinspection UnusedDeclaration
                    try (FileSystem zipFS = ZipUtil.openZipFs(IO.toPath(zipFsUri))) {
                        final Path srcMefPath = IO.toPath(uri);
                        Files.write(mefTestFile, Files.readAllBytes(srcMefPath));
                    }
                } else {
                    final Path srcMefPath = IO.toPath(uri);
                    Files.write(mefTestFile, Files.readAllBytes(srcMefPath));
                }
                Element params = new Element("request");
                if (iterations > 1 && !uuidAction.equalsIgnoreCase(Params.GENERATE_UUID)) {
                    throw new AssertionError("If iterations (the number or times each mef file is imported) is greater than 1"
                                             + " then uuidAction must be " + Params.GENERATE_UUID);
                }
                params.addContent(new Element(Params.UUID_ACTION).setText(uuidAction));

                long start = System.currentTimeMillis();
                for (int i = 0; i < iterations; i++) {
                    if (System.currentTimeMillis() - start > 30000) {
                        System.out.println("Imported " + numberOfImported + " mef files.  " + remainingFilesToImport + " remaining.");
                        start = System.currentTimeMillis();
                    }
                    metadataIds.addAll(MEFLib.doImport(params, context, mefTestFile, testClass.getStyleSheets()));

                    numberOfImported++;
                    remainingFilesToImport--;
                }
            }
            return this;
        }

        public List<String> getMefFilesToLoad() {
            return mefFilesToLoad;
        }
    }
}
