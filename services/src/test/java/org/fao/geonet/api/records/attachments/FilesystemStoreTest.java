/*
 * =============================================================================
 * ===	Copyright (C) 2019 Food and Agriculture Organization of the
 * ===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * ===	and United Nations Environment Programme (UNEP)
 * ===
 * ===	This program is free software; you can redistribute it and/or modify
 * ===	it under the terms of the GNU General Public License as published by
 * ===	the Free Software Foundation; either version 2 of the License, or (at
 * ===	your option) any later version.
 * ===
 * ===	This program is distributed in the hope that it will be useful, but
 * ===	WITHOUT ANY WARRANTY; without even the implied warranty of
 * ===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * ===	General Public License for more details.
 * ===
 * ===	You should have received a copy of the GNU General Public License
 * ===	along with this program; if not, write to the Free Software
 * ===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * ===
 * ===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * ===	Rome - Italy. email: geonetwork@osgeo.org
 * ==============================================================================
 */
package org.fao.geonet.api.records.attachments;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.lib.Lib;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FilesystemStoreTest extends AbstractStoreTest {
    @Autowired
    private FilesystemStore _store;

    /**
     * Now that visibility is tracked in the database rather than always implied by which folder
     * a file is in, a resource's visibility can only be reliably round-tripped through the same
     * decorator production actually uses ({@code resourceStore}, a {@link ResourceLoggerStore}
     * wrapping this store - see {@code config-store/config-default.xml}) - it's the one that
     * writes {@code MetadataFileUploads.resourceaccess}. The test context doesn't define that
     * bean, so it's constructed here and autowired by hand rather than via a raw {@code new}.
     */
    public Store getStore() {
        ResourceLoggerStore store = new ResourceLoggerStore(_store);
        _applicationContext.getAutowireCapableBeanFactory().autowireBean(store);
        return store;
    }

    @Test
    public void testPutResource_resourceExistsOnTheDisk() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String metadataId = importMetadata(context);
        String metadataUuid = metadataUtils.getMetadataUuid(metadataId);

        getStore().delResources(context, metadataUuid, true);

        String filename = "record-with-old-links.xml";
        MultipartFile file = new MockMultipartFile(filename,
            filename,
            "application/xml",
            Files.newInputStream(
                Paths.get(resources, filename)
            ));
        getStore().putResource(context, metadataUuid, file, MetadataResourceVisibility.PUBLIC, true);

        try (final Store.ResourceHolder resourceHolder = getStore().getResource(
            context, metadataUuid, MetadataResourceVisibility.PUBLIC, filename, true)) {
            Path filePath = FilesystemStore.getResourcePath(resourceHolder.getResource(), context);
            assertTrue("File exists on the disk", Files.isRegularFile(filePath));
        }
    }

    /**
     * A file sitting in the legacy {@code public}/{@code private} subfolder (as every resource
     * was stored before the flat, visibility-in-the-database layout) has no
     * {@code MetadataFileUploads} row at all - it predates that table's use for this purpose.
     * It must still be found by asking for its actual (folder-implied) visibility, must not be
     * found by asking for the other one, and must migrate to the flat layout - with no physical
     * move needed beyond that first touch - the moment it's touched by a visibility change.
     */
    @Test
    public void testLegacyVisibilityFolderFallbackAndMigrateOnTouch() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String metadataId = importMetadata(context);
        String metadataUuid = metadataUtils.getMetadataUuid(metadataId);
        int mdId = Integer.parseInt(metadataId);

        getStore().delResources(context, metadataUuid, true);

        String filename = "legacy.xml";
        Path legacyDir = Lib.resource.getDir("public", mdId);
        Files.createDirectories(legacyDir);
        Files.copy(Paths.get(resources, "record-with-old-links.xml"), legacyDir.resolve(filename));

        try (Store.ResourceHolder holder = getStore().getResource(context, metadataUuid,
                MetadataResourceVisibility.PUBLIC, filename, true)) {
            assertNotNull("Legacy file is found via the fallback to its folder", holder);
        }

        try (Store.ResourceHolder ignored = getStore().getResource(context, metadataUuid,
                MetadataResourceVisibility.PRIVATE, filename, true)) {
            fail("A legacy public file must not be reachable by asking for private");
        } catch (ResourceNotFoundException expected) {
            // expected
        }

        MetadataResource patched = getStore().patchResourceStatus(context, metadataUuid, filename,
            MetadataResourceVisibility.PRIVATE, true);
        assertEquals("Patch reports the new visibility", MetadataResourceVisibility.PRIVATE, patched.getVisibility());

        assertFalse("Legacy file is migrated out of the old subfolder", Files.exists(legacyDir.resolve(filename)));
        Path flatPath = Lib.resource.getMetadataDir(context.getBean(GeonetworkDataDirectory.class), mdId).resolve(filename);
        assertTrue("Legacy file now lives in the flat layout", Files.exists(flatPath));
    }
}
