package org.fao.geonet.kernel.mef;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.ZipUtil;
import org.junit.Test;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MEFExporterIntegrationTest extends AbstractCoreIntegrationTest {

    @Test
    public void testDoExport() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final MEFLibIntegrationTest.ImportMetadata importMetadata = new MEFLibIntegrationTest.ImportMetadata(this, context);
        importMetadata.getMefFilesToLoad().clear();
        importMetadata.getMefFilesToLoad().add("mef2-example-2md.zip");
        importMetadata.invoke();

        Path path = MEFExporter.doExport(context, "da165110-88fd-11da-a88f-000d939bc5d8", MEFLib.Format.FULL, false, false, false);

        try(FileSystem zipFs = ZipUtil.openZipFs(path)) {
            assertTrue(Files.exists(zipFs.getPath("metadata.xml")));
            assertTrue(Files.exists(zipFs.getPath("info.xml")));
            assertTrue(Files.exists(zipFs.getPath("private/basins.zip")));
            assertTrue(Files.exists(zipFs.getPath("public/thumbnail.gif")));
            assertTrue(Files.exists(zipFs.getPath("public/thumbnail_s.gif")));
        }
        path = MEFExporter.doExport(context, "0e1943d6-64e8-4430-827c-b465c3e9e55c", MEFLib.Format.FULL, false, false, false);

        try(FileSystem zipFs = ZipUtil.openZipFs(path)) {
            assertTrue(Files.exists(zipFs.getPath("metadata.xml")));
            assertTrue(Files.exists(zipFs.getPath("info.xml")));
            assertFalse(Files.exists(zipFs.getPath("private")));
            assertFalse(Files.exists(zipFs.getPath("public")));
        }
    }
}