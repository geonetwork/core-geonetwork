//=============================================================================
//===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.api.records.attachments;

import org.fao.geonet.api.exception.ResourceAlreadyExistException;
import org.fao.geonet.domain.ResourceUploadTask;
import org.fao.geonet.repository.ResourceUploadTaskRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests database-backed filename claims used by synchronous and asynchronous
 * resource uploads.
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceUploadTaskRegistryTest {

    @Mock
    private ResourceUploadTaskRepository repository;

    @Mock
    private ResourceUploadProgressListener synchronousListener;

    @Mock
    private AsyncResourceUploadProgressListener asynchronousListener;

    private ResourceUploadTaskRegistry registry;

    @Before
    public void setUp() {
        registry = new ResourceUploadTaskRegistry(repository);
    }

    @Test
    public void asynchronousListenerAcquiresItsOwnPersistentClaim()
        throws Exception {

        registry.resolveFilenameAndCheck(
            "metadata",
            "file.zip",
            asynchronousListener
        );

        verify(asynchronousListener).onFilenameResolved("file.zip");
        verify(repository, never()).existsByClaimKey(
            ResourceUploadTask.activeClaimKey(
                "metadata",
                "file.zip"
            )
        );
    }

    @Test
    public void synchronousUploadContinuesWhenNoClaimExists()
        throws Exception {

        String claimKey = ResourceUploadTask.activeClaimKey(
            "metadata",
            "file.zip"
        );

        when(repository.existsByClaimKey(claimKey)).thenReturn(false);

        registry.resolveFilenameAndCheck(
            "metadata",
            "file.zip",
            synchronousListener
        );

        verify(synchronousListener).onFilenameResolved("file.zip");
        verify(repository).existsByClaimKey(claimKey);
    }

    @Test
    public void synchronousUploadFailsWhenClaimAlreadyExists() {
        String claimKey = ResourceUploadTask.activeClaimKey(
            "metadata",
            "file.zip"
        );

        when(repository.existsByClaimKey(claimKey)).thenReturn(true);

        assertThrows(
            ResourceAlreadyExistException.class,
            () -> registry.resolveFilenameAndCheck(
                "metadata",
                "file.zip",
                synchronousListener
            )
        );
    }

    @Test
    public void filenameListenerFailureStopsBeforeClaimLookup()
        throws Exception {

        doThrow(new ResourceAlreadyExistException("duplicate"))
            .when(synchronousListener)
            .onFilenameResolved("file.zip");

        assertThrows(
            ResourceAlreadyExistException.class,
            () -> registry.resolveFilenameAndCheck(
                "metadata",
                "file.zip",
                synchronousListener
            )
        );

        verify(repository, never()).existsByClaimKey(
            ResourceUploadTask.activeClaimKey(
                "metadata",
                "file.zip"
            )
        );
    }
}
