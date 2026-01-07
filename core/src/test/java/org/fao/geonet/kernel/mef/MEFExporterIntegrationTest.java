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

        Path path = MEFExporter.doExport(context, "da165110-88fd-11da-a88f-000d939bc5d8", MEFLib.Format.FULL, false, false, false, false, true, true);

        try (FileSystem zipFs = ZipUtil.openZipFs(path)) {
            assertTrue(Files.exists(zipFs.getPath("metadata.xml")));
            assertTrue(Files.exists(zipFs.getPath("info.xml")));
            assertTrue(Files.exists(zipFs.getPath("private/basins.zip")));
            assertTrue(Files.exists(zipFs.getPath("public/thumbnail.gif")));
            assertTrue(Files.exists(zipFs.getPath("public/thumbnail_s.gif")));
        } finally {
            Files.delete(path);
        }
        path = MEFExporter.doExport(context, "0e1943d6-64e8-4430-827c-b465c3e9e55c", MEFLib.Format.FULL, false, false, false, false, true, true);

        try (FileSystem zipFs = ZipUtil.openZipFs(path)) {
            assertTrue(Files.exists(zipFs.getPath("metadata.xml")));
            assertTrue(Files.exists(zipFs.getPath("info.xml")));
            assertFalse(Files.exists(zipFs.getPath("private")));
            assertFalse(Files.exists(zipFs.getPath("public")));
        } finally {
            Files.delete(path);
        }
    }
}
