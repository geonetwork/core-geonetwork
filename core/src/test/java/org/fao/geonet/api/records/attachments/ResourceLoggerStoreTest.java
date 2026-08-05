/*
 * =============================================================================
 * ===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataFileUpload;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.repository.MetadataFileUploadRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regression tests for {@link ResourceLoggerStore#patchResourceStatus}: it must update the access
 * of the existing {@link MetadataFileUpload} row when the decorated store actually changes a
 * resource's visibility, looking the row up by the bare filename - not the composite
 * {@code uuid/attachments/filename} resource id a caller may pass in - since the upload log
 * consistently keys rows by bare filename (see {@link ResourceLoggerStore#storePutRequest}).
 */
public class ResourceLoggerStoreTest {

    private static final String METADATA_UUID = "uuid-1";
    private static final int METADATA_ID = 42;

    // The decorated store never dereferences the ServiceContext it is forwarded here (Mockito
    // can't mock ServiceContext itself under this JDK's Byte Buddy version), so null stands in.
    private static final ServiceContext CONTEXT = null;

    private Store decoratedStore;
    private ResourceLoggerStore resourceLoggerStore;
    private MetadataFileUploadRepository metadataFileUploadRepository;

    @Before
    public void setUp() {
        decoratedStore = mock(Store.class);
        resourceLoggerStore = new ResourceLoggerStore(decoratedStore);

        ConfigurableApplicationContext appContext = mock(ConfigurableApplicationContext.class);
        MetadataRepository metadataRepository = mock(MetadataRepository.class);
        metadataFileUploadRepository = mock(MetadataFileUploadRepository.class);

        when(metadataRepository.findOneByUuid(METADATA_UUID)).thenReturn((Metadata) new Metadata().setId(METADATA_ID));
        when(appContext.getBean(MetadataRepository.class)).thenReturn(metadataRepository);
        when(appContext.getBean(MetadataFileUploadRepository.class)).thenReturn(metadataFileUploadRepository);

        ApplicationContextHolder.set(appContext);
    }

    @After
    public void tearDown() {
        ApplicationContextHolder.clear();
    }

    @Test
    public void patchResourceStatusUpdatesAccessLookingUpByBareFilename() throws Exception {
        MetadataResource patchedResource = mock(MetadataResource.class);
        String compositeResourceId = METADATA_UUID + "/attachments/foo.txt";
        when(decoratedStore.patchResourceStatus(CONTEXT, METADATA_UUID, compositeResourceId,
            MetadataResourceVisibility.PRIVATE, true)).thenReturn(patchedResource);

        MetadataFileUpload existingUpload = new MetadataFileUpload();
        existingUpload.setMetadataId(METADATA_ID);
        existingUpload.setFileName("foo.txt");
        when(metadataFileUploadRepository.findByMetadataIdAndFileNameNotDeleted(METADATA_ID, "foo.txt"))
            .thenReturn(existingUpload);

        MetadataResource result = resourceLoggerStore.patchResourceStatus(CONTEXT, METADATA_UUID, compositeResourceId,
            MetadataResourceVisibility.PRIVATE, true);

        assertEquals(patchedResource, result);
        assertEquals("The upload log entry's access should reflect the new visibility",
            MetadataResourceVisibility.PRIVATE, existingUpload.getAccess());
        verify(metadataFileUploadRepository).save(existingUpload);
    }

    @Test
    public void patchResourceStatusDoesNotTouchUploadLogWhenDelegateDeclinesTheChange() throws Exception {
        when(decoratedStore.patchResourceStatus(CONTEXT, METADATA_UUID, "foo.txt",
            MetadataResourceVisibility.PRIVATE, true)).thenReturn(null);

        MetadataResource result = resourceLoggerStore.patchResourceStatus(CONTEXT, METADATA_UUID, "foo.txt",
            MetadataResourceVisibility.PRIVATE, true);

        assertNull(result);
        verify(metadataFileUploadRepository, never()).findByMetadataIdAndFileNameNotDeleted(anyInt(), anyString());
        verify(metadataFileUploadRepository, never()).save(any());
    }

    @Test
    public void patchResourceStatusStillSucceedsWhenNoUploadLogEntryExists() throws Exception {
        MetadataResource patchedResource = mock(MetadataResource.class);
        when(decoratedStore.patchResourceStatus(CONTEXT, METADATA_UUID, "foo.txt",
            MetadataResourceVisibility.PRIVATE, true)).thenReturn(patchedResource);

        when(metadataFileUploadRepository.findByMetadataIdAndFileNameNotDeleted(METADATA_ID, "foo.txt"))
            .thenThrow(new EmptyResultDataAccessException(1));

        MetadataResource result = resourceLoggerStore.patchResourceStatus(CONTEXT, METADATA_UUID, "foo.txt",
            MetadataResourceVisibility.PRIVATE, true);

        assertEquals("A resource uploaded before the logger tracked access should still get patched",
            patchedResource, result);
        verify(metadataFileUploadRepository, never()).save(any());
    }
}
