package org.fao.geonet.kernel.mef;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;
import org.fao.geonet.repository.MetadataRepository;
import org.jdom.Element;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
        final File resource = new File(MEFLibIntegrationTest.class.getResource("mef1-example.mef").getFile());
        final User admin = loginAsAdmin(context);

        Element params = new Element("request");
        final List<String> metadataIds = MEFLib.doImport(params, context, resource, getStyleSheets());

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
}
