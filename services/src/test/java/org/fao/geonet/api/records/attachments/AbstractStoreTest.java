/*
 * Copyright (C) 2001-2019 Food and Agriculture Organization of the
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

package org.fao.geonet.api.records.attachments;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLStreamHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by francois on 19/01/16.
 */
import org.fao.geonet.domain.MetadataFileUpload;
import org.fao.geonet.repository.MetadataFileUploadRepository;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractStoreTest extends AbstractServiceIntegrationTest {

    protected static String resources =
        AbstractCoreIntegrationTest.getClassFile(MetadataResourceDatabaseMigrationTest.class).getParent();
    @Autowired
    protected IMetadataUtils metadataUtils;
    @Autowired
    private MetadataRepository _metadataRepo;
    @Autowired
    protected IMetadataManager metadataManager;
    @Autowired
    protected IMetadataIndexer metadataIndexer;
    @Autowired
    protected MetadataFileUploadRepository uploadRepository;

    protected abstract Store getStore();

    public static URL getMockUrl(final String filename,
                                 final String urlParameters) throws IOException {
        final Path file = Paths.get(resources, filename);

        assertTrue("Mock file " + filename + " not found", Files.exists(file));
        final HttpURLConnection mockConnection = Mockito.mock(HttpURLConnection.class);

        Mockito.when(mockConnection.getInputStream()).thenReturn(
            Files.newInputStream(file)
        );

        Mockito.when(mockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

        Mockito.when(mockConnection.getContentLengthLong()).thenReturn(-1L);

        final URLStreamHandler handler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(final URL arg0) {
                return mockConnection;
            }
        };
        return new URL("http", "foo.bar", 80,
                       "http://foo.bar/" + filename + urlParameters, handler);
    }

    protected String importMetadata(ServiceContext context) throws Exception {
        final MEFLibIntegrationTest.ImportMetadata importMetadata =
            new MEFLibIntegrationTest.ImportMetadata(this, context).invoke();

        assertEquals(1, _metadataRepo.count());
        return importMetadata.getMetadataIds().get(0);
    }

    @Test
    public void testGetResources() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String metadataId = importMetadata(context);
        String metadataUuid = metadataUtils.getMetadataUuid(metadataId);
        List<MetadataResource> resourcesList =
            getStore().getResources(context, metadataUuid, Sort.name, null, true);
        assertEquals("No resource for record", resourcesList.size(), 0);
    }

    @Test
    public void testPutPatchAndDeleteResource() throws Exception {
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

        List<MetadataResource> resourcesList =
            getStore().getResources(context, metadataUuid, Sort.name, null, true);
        assertEquals("1 resource for record", 1, resourcesList.size());

        MetadataResource resource = resourcesList.get(0);
        assertTrue("Resource is a FileSystemResource",
            resource instanceof FilesystemStoreResource);
        assertEquals("Resource id is correct",
            metadataUuid + "/attachments/" + filename,
            resource.getId());
        assertEquals("Resource type is correct",
            MetadataResourceVisibility.PUBLIC,
            resource.getVisibility());
        assertEquals("Resource URL is correct",
            "http://localhost:8080/srv/api/records/" + metadataUuid + "/attachments/" + filename,
            resource.getUrl());

        MetadataResource patchedResource = getStore().patchResourceStatus(context, metadataUuid, filename,
                                                                      MetadataResourceVisibility.PRIVATE, true);
        assertEquals("Patched resource type is correct",
            MetadataResourceVisibility.PRIVATE,
            patchedResource.getVisibility());

        getStore().delResource(context, metadataUuid, MetadataResourceVisibility.PRIVATE, filename, true);


        resourcesList =
            getStore().getResources(context, metadataUuid, Sort.name, null, true);
        assertEquals("0 resource for record",
            0,
            resourcesList.size());
    }

    @Test
    public void testNestedPathResource() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String metadataId = importMetadata(context);
        String metadataUuid = metadataUtils.getMetadataUuid(metadataId);

        getStore().delResources(context, metadataUuid, true);

        String nestedFilename = "folder1/subfolder/nested-file.xml";
        String sourceFilename = "record-with-old-links.xml";
        MultipartFile file = new MockMultipartFile(nestedFilename,
            nestedFilename,
            "application/xml",
            Files.newInputStream(
                Paths.get(resources, sourceFilename)
            ));
        getStore().putResource(context, metadataUuid, file, MetadataResourceVisibility.PUBLIC, true);

        List<MetadataResource> resourcesList =
            getStore().getResources(context, metadataUuid, Sort.name, null, true);
        assertEquals("1 resource for record", 1, resourcesList.size());

        MetadataResource resource = resourcesList.get(0);
        assertEquals("Nested filename is preserved through listing",
            nestedFilename, resource.getFilename());
        assertEquals("Resource id is correct",
            metadataUuid + "/attachments/" + nestedFilename,
            resource.getId());

        try (Store.ResourceHolder holder = getStore().getResource(context, metadataUuid,
                MetadataResourceVisibility.PUBLIC, nestedFilename, true)) {
            assertNotNull("Resource is fetchable by its nested filename", holder);
            assertEquals("Fetched resource's filename is preserved",
                nestedFilename, holder.getMetadata().getFilename());
        }

        MetadataResource patchedResource = getStore().patchResourceStatus(context, metadataUuid, nestedFilename,
                                                                      MetadataResourceVisibility.PRIVATE, true);
        assertEquals("Patched resource type is correct",
            MetadataResourceVisibility.PRIVATE,
            patchedResource.getVisibility());
        assertEquals("Nested filename is preserved through a visibility change",
            nestedFilename, patchedResource.getFilename());

        getStore().delResource(context, metadataUuid, MetadataResourceVisibility.PRIVATE, nestedFilename, true);

        resourcesList =
            getStore().getResources(context, metadataUuid, Sort.name, null, true);
        assertEquals("0 resource for record",
            0,
            resourcesList.size());
    }

    /**
     * Now that visibility (public/private) is tracked in the database rather than always
     * implied by which folder/prefix a file is stored under, that tracked value must be checked
     * against the visibility the caller is asking for - otherwise a caller could read an
     * actually-private resource by simply asking for the public one, bypassing the
     * {@code canDownload} check for its real visibility. This is the regression this test
     * guards against.
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testResourceRejectsVisibilityMismatch() throws Exception {
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

        // Tracked as public - asking for it as private must behave as if it doesn't exist,
        // exactly as an actually-private resource can't be read today by asking for the public one.
        try (Store.ResourceHolder ignored = getStore().getResource(context, metadataUuid,
                MetadataResourceVisibility.PRIVATE, filename, true)) {
            fail("A resource tracked as public must not be returned when asked for as private");
        }
    }

    @Test
    public void testRenameResource() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String metadataId = importMetadata(context);
        String metadataUuid = metadataUtils.getMetadataUuid(metadataId);

        getStore().delResources(context, metadataUuid, true);

        String filename = "record-with-old-links.xml";
        String newFilename = "renamed-record.xml";
        MultipartFile file = new MockMultipartFile(filename,
            filename,
            "application/xml",
            Files.newInputStream(
                Paths.get(resources, filename)
            ));
        getStore().putResource(context, metadataUuid, file, MetadataResourceVisibility.PUBLIC, true);

        MetadataResource renamedResource = getStore().renameResource(context, metadataUuid, filename, newFilename, true);
        assertEquals("Renamed resource id is correct",
            metadataUuid + "/attachments/" + newFilename,
            renamedResource.getId());
        assertEquals("Renamed resource URL is correct",
            "http://localhost:8080/srv/api/records/" + metadataUuid + "/attachments/" + newFilename,
            renamedResource.getUrl());

        List<MetadataResource> resourcesList =
            getStore().getResources(context, metadataUuid, Sort.name, null, true);
        assertEquals("1 resource for record after rename", 1, resourcesList.size());
        assertEquals("Resource in list has new filename", newFilename, resourcesList.get(0).getFilename());

        getStore().delResources(context, metadataUuid, true);
    }

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

        AttachmentsApi api = new AttachmentsApi(getStore(), metadataManager, metadataIndexer);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("PATCH");
        request.setRequestURI("/srv/api/records/" + metadataUuid + "/attachments/" + filename);

        MetadataResource result = api.patchResource(metadataUuid, filename, null, newFilename, true, request);
        assertNotNull("Renamed metadata resource should not be null", result);
        assertEquals("Renamed resource filename is updated", newFilename, result.getFilename());

        getStore().delResources(context, metadataUuid, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttachmentsApiPatchResourceValidation() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String metadataId = importMetadata(context);
        String metadataUuid = metadataUtils.getMetadataUuid(metadataId);

        AttachmentsApi api = new AttachmentsApi(getStore(), metadataManager, metadataIndexer);
        MockHttpServletRequest request = new MockHttpServletRequest();

        api.patchResource(metadataUuid, "somefile.xml", null, null, true, request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRenameResourceExceedsMaxLength() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String metadataId = importMetadata(context);
        String metadataUuid = metadataUtils.getMetadataUuid(metadataId);

        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < MetadataFileUpload.FILENAME_MAX_LENGTH + 1; i++) {
            longName.append("a");
        }
        longName.append(".xml");

        getStore().renameResource(context, metadataUuid, "somefile.xml", longName.toString(), true);
    }

    @Test
    public void testResourceLoggerStoreRenameUploadRecord() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String metadataIdStr = importMetadata(context);
        int metadataId = Integer.parseInt(metadataIdStr);
        String metadataUuid = metadataUtils.getMetadataUuid(metadataIdStr);

        Store loggerStore = new ResourceLoggerStore(getStore());

        String filename = "record-with-old-links.xml";
        String newFilename = "logger-renamed-record.xml";
        MultipartFile file = new MockMultipartFile(filename,
            filename,
            "application/xml",
            Files.newInputStream(
                Paths.get(resources, filename)
            ));
        loggerStore.putResource(context, metadataUuid, file, MetadataResourceVisibility.PUBLIC, true);

        // ResourceLoggerStore now consistently logs the bare filename (never the composite
        // "uuid/attachments/filename" id), so a single direct lookup is enough.
        MetadataFileUpload initialUpload = uploadRepository.findByMetadataIdAndFileNameNotDeleted(metadataId, filename);
        assertNotNull("Initial upload record in MetadataFileUploads should exist", initialUpload);
        assertTrue("Initial upload record has old filename", initialUpload.getFileName().endsWith(filename));

        MetadataResource renamedResource = loggerStore.renameResource(context, metadataUuid, filename, newFilename, true);
        assertNotNull("Renamed resource should not be null", renamedResource);

        MetadataFileUpload updatedUpload = uploadRepository.findByMetadataIdAndFileNameNotDeleted(metadataId, newFilename);
        assertNotNull("Updated upload record in MetadataFileUploads should exist with new filename", updatedUpload);
        assertEquals("Updated upload record ID matches initial record ID", initialUpload.getId(), updatedUpload.getId());
        assertTrue("Updated upload record has new filename", updatedUpload.getFileName().endsWith(newFilename));

        loggerStore.delResources(context, metadataUuid, true);
    }

    @Test
    public void testPutResourceFromURL() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String metadataId = importMetadata(context);
        String metadataUuid = metadataUtils.getMetadataUuid(metadataId);

        getStore().delResources(context, metadataUuid, true);

        String filename = "record-with-old-links.xml";
        URL url = getMockUrl(filename, "");
        try {
            getStore().putResource(context, metadataUuid, url, MetadataResourceVisibility.PUBLIC, true);

            List<MetadataResource> resourcesList =
                getStore().getResources(context, metadataUuid, Sort.name, null, true);
            assertEquals("1 resource for record", 1, resourcesList.size());

            MetadataResource resource = resourcesList.get(0);
            assertEquals("Resource id is correct",
                         metadataUuid + "/attachments/" + filename,
                         resource.getId());
            assertEquals("Resource type is correct",
                         MetadataResourceVisibility.PUBLIC,
                         resource.getVisibility());
            assertEquals("Resource URL is correct",
                         "http://localhost:8080/srv/api/records/" + metadataUuid + "/attachments/" + filename,
                         resource.getUrl());
        } finally {
            getStore().delResources(context, metadataUuid, true);
        }
    }

    @Test
    public void testPutResourceFromURLWithURLParameters() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String metadataId = importMetadata(context);
        String metadataUuid = metadataUtils.getMetadataUuid(metadataId);

        getStore().delResources(context, metadataUuid, true);

        String filename = "record-with-old-links.xml";
        URL url = getMockUrl(filename,
            "?someParameterToIgnoreWhenCreatingFileName&aaa=aaa");
        try {
            getStore().putResource(context, metadataUuid, url, MetadataResourceVisibility.PUBLIC, true);

            List<MetadataResource> resourcesList =
                getStore().getResources(context, metadataUuid, Sort.name, null, true);
            assertEquals("1 resource for record", 1, resourcesList.size());

            MetadataResource resource = resourcesList.get(0);
            assertEquals("Resource id is correct",
                         metadataUuid + "/attachments/" + filename,
                         resource.getId());
            assertEquals("Resource type is correct",
                         MetadataResourceVisibility.PUBLIC,
                         resource.getVisibility());
            assertEquals("Resource URL is correct",
                         "http://localhost:8080/srv/api/records/" + metadataUuid + "/attachments/" + filename,
                         resource.getUrl());
        } finally {
            getStore().delResources(context, metadataUuid, true);
        }
    }
}
