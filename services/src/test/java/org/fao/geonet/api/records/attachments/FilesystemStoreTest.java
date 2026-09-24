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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FilesystemStoreTest extends AbstractStoreTest {
    @Autowired
    private FilesystemStore _store;
    @Autowired
    private WebApplicationContext wac;

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

    /**
     * Phase 1b regression guard: {@code {resourceId:.+}} cannot span multiple "/"-separated URL
     * segments under this app's {@code AntPathMatcher}-based routing, so a request for a
     * genuinely nested resource (eg. the reported {@code GET .../attachments/data/file.pdf})
     * 404'd even though the {@code Store} layer has always fully supported nested filenames. This
     * dispatches real HTTP requests through {@link MockMvc} (unlike the Store-layer-only tests
     * elsewhere in this class) specifically because that routing bug is invisible to a direct
     * Java method call on {@link AttachmentsApi}.
     */
    @Test
    public void testNestedPathResourceGetPatchDeleteViaHttp() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String metadataId = importMetadata(context);
        String metadataUuid = metadataUtils.getMetadataUuid(metadataId);

        getStore().delResources(context, metadataUuid, true);

        String nestedFilename = "data/nested-http.xml";
        MultipartFile file = new MockMultipartFile(nestedFilename,
            nestedFilename,
            "application/xml",
            Files.newInputStream(
                Paths.get(resources, "record-with-old-links.xml")
            ));
        getStore().putResource(context, metadataUuid, file, MetadataResourceVisibility.PUBLIC, true);

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession session = loginAsAdmin();
        String url = "/srv/api/records/" + metadataUuid + "/attachments/" + nestedFilename;

        mockMvc.perform(get(url).session(session))
            .andExpect(status().isOk());

        mockMvc.perform(patch(url).session(session)
                .accept(MediaType.APPLICATION_JSON)
                .param("visibility", "private"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.visibility").value("PRIVATE"));

        mockMvc.perform(delete(url).session(session))
            .andExpect(status().isNoContent());

        List<MetadataResource> remaining = getStore().getResources(context, metadataUuid, Sort.name, null, true);
        assertEquals("Nested resource is gone after the HTTP delete", 0, remaining.size());
    }

    /**
     * Phase 1b regression guard, upload side: a single-segment folder (eg. {@code data}) happened
     * to work before this fix, since one segment is exactly what a {@code {folder:.+}} path
     * variable can match - which is why Phase 3's upload succeeded while the read-back that
     * inspired Phase 1b failed. A multi-level folder never worked at all; this proves it does now.
     */
    @Test
    public void testUploadIntoMultiLevelFolderViaHttp() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String metadataId = importMetadata(context);
        String metadataUuid = metadataUtils.getMetadataUuid(metadataId);

        getStore().delResources(context, metadataUuid, true);

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession session = loginAsAdmin();

        MockMultipartFile file = new MockMultipartFile("file", "deep.xml", "application/xml",
            Files.newInputStream(Paths.get(resources, "record-with-old-links.xml")));

        mockMvc.perform(multipart("/srv/api/records/" + metadataUuid + "/attachments/a/b/c")
                .file(file)
                .session(session)
                .accept(MediaType.APPLICATION_JSON)
                .param("visibility", "public"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.filename").value("a/b/c/deep.xml"));

        List<MetadataResource> uploaded = getStore().getResources(context, metadataUuid, Sort.name, null, true);
        assertEquals(1, uploaded.size());
        assertEquals("a/b/c/deep.xml", uploaded.get(0).getFilename());
    }

    /**
     * Phase 1b regression guard: {@code getResource}/{@code delResource} were originally changed
     * to map bare {@code /**}, which (like {@code {resourceId:.+}} before it) also matches the
     * bare {@code .../attachments} URL with an empty tail - the same shape
     * {@code getAllResources}/{@code delResources} map exactly. This turned out to actually
     * dispatch to the wrong handler in this app: it registers two separate
     * {@code RequestMappingHandlerMapping} beans (a pre-existing, unrelated quirk), so the two
     * candidate mappings never even got compared against each other by Spring's usual
     * pattern-specificity logic - whichever handler mapping bean got consulted first simply won,
     * regardless of specificity. The fix was to require at least one path segment
     * ({@code /*}{@code /**}, not {@code /**}) on {@code getResource}/{@code patchResource}/
     * {@code delResource}, removing the overlap entirely rather than relying on comparator
     * behavior. This test locks that outcome in against the real, fully-wired application
     * context.
     */
    @Test
    public void testBaseMappingsNotShadowedByWildcardResourceMappings() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String metadataId = importMetadata(context);
        String metadataUuid = metadataUtils.getMetadataUuid(metadataId);

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession session = loginAsAdmin();
        String baseUrl = "/srv/api/records/" + metadataUuid + "/attachments";

        mockMvc.perform(get(baseUrl).session(session).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(handler().methodName("getAllResources"));

        mockMvc.perform(delete(baseUrl).session(session).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(handler().methodName("delResources"));
    }

    /**
     * Companion to {@link #testAttachmentsApiPatchResourceValidation}: verifies the successful
     * rename path via a real HTTP dispatch (the direct-Java-method-call version of this test
     * that used to live in {@code AbstractStoreTest} stopped being meaningful once
     * {@code patchResource} started reading {@code resourceId} from request attributes that only
     * a genuine Spring MVC dispatch sets - see {@code AttachmentsApi#extractPathWithinMapping}).
     */
    @Test
    public void testAttachmentsApiPatchResourceRename() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String metadataId = importMetadata(context);
        String metadataUuid = metadataUtils.getMetadataUuid(metadataId);

        getStore().delResources(context, metadataUuid, true);

        String filename = "record-with-old-links.xml";
        String newFilename = "api-renamed-record.xml";
        MultipartFile file = new MockMultipartFile(filename,
            filename,
            "application/xml",
            Files.newInputStream(
                Paths.get(resources, filename)
            ));
        getStore().putResource(context, metadataUuid, file, MetadataResourceVisibility.PUBLIC, true);

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession session = loginAsAdmin();
        String url = "/srv/api/records/" + metadataUuid + "/attachments/" + filename;

        mockMvc.perform(patch(url).session(session)
                .accept(MediaType.APPLICATION_JSON)
                .param("newResourceName", newFilename))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.filename").value(newFilename));

        getStore().delResources(context, metadataUuid, true);
    }

    /**
     * {@code patchResource}'s own "either visibility or newResourceName must be provided"
     * validation, exercised as an HTTP round trip: {@code IllegalArgumentException} is mapped to
     * a 400 by {@code GlobalExceptionController}.
     */
    @Test
    public void testAttachmentsApiPatchResourceValidation() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String metadataId = importMetadata(context);
        String metadataUuid = metadataUtils.getMetadataUuid(metadataId);

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession session = loginAsAdmin();
        String url = "/srv/api/records/" + metadataUuid + "/attachments/somefile.xml";

        mockMvc.perform(patch(url).session(session).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}
