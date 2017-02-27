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

package org.fao.geonet.api.records.attachments;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by francois on 19/01/16.
 */
public class FilesystemStoreTest extends AbstractServiceIntegrationTest {

    private static String resources =
        AbstractCoreIntegrationTest.getClassFile(MetadataResourceDatabaseMigrationTest.class).getParent();
    @Autowired
    private DataManager _dataManager;
    @Autowired
    private MetadataRepository _metadataRepo;
    @Autowired
    private GeonetworkDataDirectory _dataDirectory;
    @Autowired
    private FilesystemStore _fileStore;

    public static URL getMockUrl(final String filename,
                                 final String urlParameters) throws IOException {
        final Path file = Paths.get(resources, filename);

        assertTrue("Mock file " + filename + " not found", Files.exists(file));
        final URLConnection mockConnection = Mockito.mock(URLConnection.class);

        Mockito.when(mockConnection.getInputStream()).thenReturn(
            Files.newInputStream(file)
        );

        final URLStreamHandler handler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(final URL arg0)
                throws IOException {
                return mockConnection;
            }
        };
        final URL url = new URL("http", "foo.bar", 80,
            "http://foo.bar/" + filename + urlParameters, handler);
        return url;
    }

    private String importMetadata(ServiceContext context) throws Exception {
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
        String metadataUuid = _dataManager.getMetadataUuid(metadataId);
        List<MetadataResource> resourcesList =
            _fileStore.getResources(context, metadataUuid, Sort.name, null);
        assertEquals("No resource for record", resourcesList.size(), 0);
    }

    @Test
    public void testPutPatchAndDeleteResource() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String metadataId = importMetadata(context);
        String metadataUuid = _dataManager.getMetadataUuid(metadataId);
        String filename = "record-with-old-links.xml";
        MultipartFile file = new MockMultipartFile(filename,
            filename,
            "application/xml",
            Files.newInputStream(
                Paths.get(resources, filename)
            ));
        _fileStore.putResource(context, metadataUuid, file, MetadataResourceVisibility.PUBLIC);

        List<MetadataResource> resourcesList =
            _fileStore.getResources(context, metadataUuid, Sort.name, null);
        assertEquals("1 resource for record", 1, resourcesList.size());

        MetadataResource resource = resourcesList.get(0);
        assertTrue("Resource is a FileSystemResource",
            resource instanceof FilesystemStoreResource);
        assertEquals("Resource id is correct",
            metadataUuid + "/attachments/" + filename,
            resource.getId());
        assertEquals("Resource type is correct",
            MetadataResourceVisibility.PUBLIC.toString(),
            resource.getType());
        assertEquals("Resource URL is correct",
            "http://localhost:8080/srv/api/records/" + metadataUuid + "/attachments/" + filename,
            resource.getUrl());


        MetadataResource patchedResource = _fileStore.patchResourceStatus(context, metadataUuid, filename,
            MetadataResourceVisibility.PRIVATE);
        assertEquals("Patched resource type is correct",
            MetadataResourceVisibility.PRIVATE.toString(),
            patchedResource.getType());


        _fileStore.delResource(context, metadataUuid, filename);


        resourcesList =
            _fileStore.getResources(context, metadataUuid, Sort.name, null);
        assertEquals("0 resource for record",
            0,
            resourcesList.size());
    }

    @Test
    public void testPutResourceFromURL() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String metadataId = importMetadata(context);
        String metadataUuid = _dataManager.getMetadataUuid(metadataId);
        String filename = "record-with-old-links.xml";
        URL url = getMockUrl(filename, "");
        _fileStore.putResource(context, metadataUuid, url, MetadataResourceVisibility.PUBLIC);

        List<MetadataResource> resourcesList =
            _fileStore.getResources(context, metadataUuid, Sort.name, null);
        assertEquals("1 resource for record", 1, resourcesList.size());

        MetadataResource resource = resourcesList.get(0);
        assertEquals("Resource id is correct",
            metadataUuid + "/attachments/" + filename,
            resource.getId());
        assertEquals("Resource type is correct",
            MetadataResourceVisibility.PUBLIC.toString(),
            resource.getType());
        assertEquals("Resource URL is correct",
            "http://localhost:8080/srv/api/records/" + metadataUuid + "/attachments/" + filename,
            resource.getUrl());
    }

    @Test
    public void testPutResourceFromURLWithURLParameters() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        String metadataId = importMetadata(context);
        String metadataUuid = _dataManager.getMetadataUuid(metadataId);
        String filename = "record-with-old-links.xml";
        URL url = getMockUrl(filename,
            "?someParameterToIgnoreWhenCreatingFileName&aaa=aaa");
        _fileStore.putResource(context, metadataUuid, url, MetadataResourceVisibility.PUBLIC);

        List<MetadataResource> resourcesList =
            _fileStore.getResources(context, metadataUuid, Sort.name, null);
        assertEquals("1 resource for record", 1, resourcesList.size());

        MetadataResource resource = resourcesList.get(0);
        assertEquals("Resource id is correct",
            metadataUuid + "/attachments/" + filename,
            resource.getId());
        assertEquals("Resource type is correct",
            MetadataResourceVisibility.PUBLIC.toString(),
            resource.getType());
        assertEquals("Resource URL is correct",
            "http://localhost:8080/srv/api/records/" + metadataUuid + "/attachments/" + filename,
            resource.getUrl());
    }
}
