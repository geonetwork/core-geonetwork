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
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.jdom.Element;
import org.junit.Test;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
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
            // Public and private resources alike live flat under store/ - visibility is recorded
            // per-file in info.xml's <store> element, not by a public/private physical split.
            assertTrue(Files.exists(zipFs.getPath("store/basins.zip")));
            assertTrue(Files.exists(zipFs.getPath("store/thumbnail.gif")));
            assertTrue(Files.exists(zipFs.getPath("store/thumbnail_s.gif")));
            assertFalse("No legacy public/ folder in a freshly-exported archive", Files.exists(zipFs.getPath("public")));
            assertFalse("No legacy private/ folder in a freshly-exported archive", Files.exists(zipFs.getPath("private")));
        } finally {
            Files.delete(path);
        }
        path = MEFExporter.doExport(context, "0e1943d6-64e8-4430-827c-b465c3e9e55c", MEFLib.Format.FULL, false, false, false, false, true, true);

        try (FileSystem zipFs = ZipUtil.openZipFs(path)) {
            assertTrue(Files.exists(zipFs.getPath("metadata.xml")));
            assertTrue(Files.exists(zipFs.getPath("info.xml")));
            assertTrue(Files.exists(zipFs.getPath("store")));
            assertTrue(isEmptyDir(zipFs.getPath("store")));
        } finally {
            Files.delete(path);
        }
    }

    /**
     * End-to-end verification of the MEF 3.0 writer (unified {@code <store>} element,
     * MEFLib#buildInfoFile) together with the reader's backward-compat branch
     * (MEFLib#getFilesElement, used by MEFVisitor, MEF2Visitor and MEF3Visitor): export a record with public and
     * private attachments (producing a fresh, MEF-3.0-format archive), wipe its resources, then
     * re-import that same archive and confirm the resources come back with the correct
     * visibility - proving the reader correctly understood the writer's own {@code <store>}
     * output, not just the legacy {@code <public>}/{@code <private>} format.
     */
    @Test
    public void testExportThenReimportRoundTripsAttachments() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final MEFLibIntegrationTest.ImportMetadata importMetadata = new MEFLibIntegrationTest.ImportMetadata(this, context);
        importMetadata.getMefFilesToLoad().clear();
        importMetadata.getMefFilesToLoad().add("mef2-example-2md.zip");
        importMetadata.invoke();

        final String uuid = "da165110-88fd-11da-a88f-000d939bc5d8";
        final Store store = context.getBean("resourceStore", Store.class);

        Path path = MEFExporter.doExport(context, uuid, MEFLib.Format.FULL, false, false, false, false, true, true);
        try (FileSystem zipFs = ZipUtil.openZipFs(path)) {
            String infoXml = new String(Files.readAllBytes(zipFs.getPath("info.xml")));
            assertTrue("Exported info.xml should use the MEF 3.0 <store> element", infoXml.contains("<store>"));
            assertFalse("Exported info.xml should not use the legacy <public> element", infoXml.contains("<public>"));

            // Wipe the resources so re-importing is the only way they can come back.
            store.delResources(context, uuid, true);
            List<MetadataResource> afterWipe = store.getResources(context, uuid, MetadataResourceVisibility.PUBLIC, null, true);
            assertTrue("Resources should be gone after delResources", afterWipe.isEmpty());

            Element params = new Element("request");
            params.addContent(new Element("uuidAction").setText("overwrite"));
            MEFLib.doImport(params, context, path, getStyleSheets());

            List<MetadataResource> publicResources = store.getResources(context, uuid, MetadataResourceVisibility.PUBLIC, null, true);
            List<MetadataResource> privateResources = store.getResources(context, uuid, MetadataResourceVisibility.PRIVATE, null, true);
            // Fixture has thumbnail.gif + thumbnail_s.gif (public) and basins.zip + a stray
            // .DS_Store (private) - matches the physical zip contents, not an assumption.
            assertEquals(2, publicResources.size());
            assertEquals(2, privateResources.size());
        } finally {
            Files.delete(path);
        }
    }

    /**
     * End-to-end verification of {@link MEF3Exporter} (the flat MEF 3.0 {@code store/} writer for
     * the V2 multi-record container, invoked via {@link MEFLib#doMEF3Export}) together with
     * {@link MEFLib#getMEFVersion}/{@link MEF3Visitor} (the matching reader): export a record
     * with public and private attachments, confirm the physical zip layout is the flat
     * {@code store/} folder and is detected as {@link MEFLib.Version#V3} - not the legacy
     * {@code public/}/{@code private/} split still written by {@link MEF2Exporter} - wipe its
     * resources, then re-import and confirm they come back with the correct visibility.
     */
    @Test
    public void testMef3ExportThenReimportRoundTripsAttachments() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final MEFLibIntegrationTest.ImportMetadata importMetadata = new MEFLibIntegrationTest.ImportMetadata(this, context);
        importMetadata.getMefFilesToLoad().clear();
        importMetadata.getMefFilesToLoad().add("mef2-example-2md.zip");
        importMetadata.invoke();

        final String uuid = "da165110-88fd-11da-a88f-000d939bc5d8";
        final Store store = context.getBean("resourceStore", Store.class);

        Path path = MEFLib.doMEF3Export(context, Set.of(uuid), "full", false, getStyleSheets(),
            false, false, false, false, true, true);
        try (FileSystem zipFs = ZipUtil.openZipFs(path)) {
            assertTrue(Files.exists(zipFs.getPath(uuid + "/store/thumbnail.gif")));
            assertTrue(Files.exists(zipFs.getPath(uuid + "/store/basins.zip")));
            assertFalse("MEF3Exporter should not write the legacy public/ folder",
                Files.exists(zipFs.getPath(uuid + "/public")));
            assertFalse("MEF3Exporter should not write the legacy private/ folder",
                Files.exists(zipFs.getPath(uuid + "/private")));

            assertEquals("A flat store/ layout archive should be detected as MEF version 3",
                MEFLib.Version.V3, MEFLib.getMEFVersion(path));

            // Wipe the resources so re-importing is the only way they can come back.
            store.delResources(context, uuid, true);
            List<MetadataResource> afterWipe = store.getResources(context, uuid, MetadataResourceVisibility.PUBLIC, null, true);
            assertTrue("Resources should be gone after delResources", afterWipe.isEmpty());

            Element params = new Element("request");
            params.addContent(new Element("uuidAction").setText("overwrite"));
            MEFLib.doImport(params, context, path, getStyleSheets());

            List<MetadataResource> publicResources = store.getResources(context, uuid, MetadataResourceVisibility.PUBLIC, null, true);
            List<MetadataResource> privateResources = store.getResources(context, uuid, MetadataResourceVisibility.PRIVATE, null, true);
            // Fixture has thumbnail.gif + thumbnail_s.gif (public) and basins.zip + a stray
            // .DS_Store (private) - matches the physical zip contents, not an assumption.
            assertEquals(2, publicResources.size());
            assertEquals(2, privateResources.size());
        } finally {
            Files.delete(path);
        }
    }

    /**
     * Verifies {@link MEFLib#doMEF2Export} (still backed by {@link MEF2Exporter}) writes the
     * legacy, pre-3.0 {@code public/}/{@code private/} layout, and that the result is correctly
     * detected as {@link MEFLib.Version#V2} - guards the {@link MEF2Exporter}/{@link MEF3Exporter}
     * template-method split (only {@link MEF2Exporter#getResourcesPath} should differ).
     */
    @Test
    public void testMef2ExportWritesLegacyPublicPrivateLayout() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final MEFLibIntegrationTest.ImportMetadata importMetadata = new MEFLibIntegrationTest.ImportMetadata(this, context);
        importMetadata.getMefFilesToLoad().clear();
        importMetadata.getMefFilesToLoad().add("mef2-example-2md.zip");
        importMetadata.invoke();

        final String uuid = "da165110-88fd-11da-a88f-000d939bc5d8";

        Path path = MEFLib.doMEF2Export(context, Set.of(uuid), "full", false, getStyleSheets(),
            false, false, false, false, true, true);
        try (FileSystem zipFs = ZipUtil.openZipFs(path)) {
            assertTrue(Files.exists(zipFs.getPath(uuid + "/public/thumbnail.gif")));
            assertTrue(Files.exists(zipFs.getPath(uuid + "/private/basins.zip")));
            assertFalse("MEF2Exporter should not write the flat store/ folder",
                Files.exists(zipFs.getPath(uuid + "/store")));

            assertEquals("A public/private layout archive should be detected as MEF version 2",
                MEFLib.Version.V2, MEFLib.getMEFVersion(path));
        } finally {
            Files.delete(path);
        }
    }

    /**
     * Imports a MEF 3.0 archive that was generated once and committed as a fixture (not one
     * freshly generated by this same test run's exporter), so a future regression in both the
     * writer and the reader together can't mask itself the way it could in
     * {@link #testExportThenReimportRoundTripsAttachments}, which generates and re-imports the
     * archive in the same run.
     */
    @Test
    public void testImportMef3SampleFixture() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final MEFLibIntegrationTest.ImportMetadata importMetadata = new MEFLibIntegrationTest.ImportMetadata(this, context);
        importMetadata.getMefFilesToLoad().clear();
        importMetadata.getMefFilesToLoad().add("mef3-example.zip");
        importMetadata.invoke();

        final String uuid = "da165110-88fd-11da-a88f-000d939bc5d8";
        final Store store = context.getBean("resourceStore", Store.class);

        List<MetadataResource> publicResources = store.getResources(context, uuid, MetadataResourceVisibility.PUBLIC, null, true);
        List<MetadataResource> privateResources = store.getResources(context, uuid, MetadataResourceVisibility.PRIVATE, null, true);
        // Fixture has thumbnail.gif + thumbnail_s.gif (public) and basins.zip + a stray
        // .DS_Store (private) - matches the physical zip contents, not an assumption.
        assertEquals(2, publicResources.size());
        assertEquals(2, privateResources.size());
    }

    private static boolean isEmptyDir(Path dir) throws java.io.IOException {
        if (!Files.isDirectory(dir)) return false;
        try (java.nio.file.DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            return !stream.iterator().hasNext();
        }
    }
}
