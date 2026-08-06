/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
package org.fao.geonet;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.domain.MetadataFileUpload;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.repository.MetadataFileUploadRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Verifies {@link MetadataFileUploadBackfillTask} against a real record with attachments: one
 * upload row is downgraded to look like it predates the access/mimetype columns (both null), and
 * another is deleted entirely while leaving its physical file in place, to simulate a file that
 * was never tracked at all. After running the task, the first row should be filled in from the
 * resource's actual physical visibility/detected mimetype, and a new row should exist for the
 * previously-untracked file.
 */
public class MetadataFileUploadBackfillTaskIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private MetadataFileUploadRepository uploadRepository;

    @Autowired
    private IMetadataUtils metadataUtils;

    @Test
    public void backfillsLegacyRowsAndCreatesRowsForUntrackedFiles() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final MEFLibIntegrationTest.ImportMetadata importMetadata = new MEFLibIntegrationTest.ImportMetadata(this, context);
        importMetadata.getMefFilesToLoad().clear();
        importMetadata.getMefFilesToLoad().add("mef2-example-2md.zip");
        importMetadata.invoke();

        String metadataIdStr = importMetadata.getMetadataIds().get(0);
        int metadataId = Integer.parseInt(metadataIdStr);
        String metadataUuid = metadataUtils.getMetadataUuid(metadataIdStr);

        final Store store = context.getBean("resourceStore", Store.class);
        store.delResources(context, metadataUuid, true);

        String legacyFilename = "legacy-file.txt";
        String untrackedFilename = "untracked-file.txt";
        store.putResource(context, metadataUuid,
            new MockMultipartFile(legacyFilename, legacyFilename, "text/plain", "legacy content".getBytes()),
            MetadataResourceVisibility.PUBLIC, true);
        store.putResource(context, metadataUuid,
            new MockMultipartFile(untrackedFilename, untrackedFilename, "text/plain", "untracked content".getBytes()),
            MetadataResourceVisibility.PRIVATE, true);

        // Simulate a row that predates the access/mimetype columns.
        MetadataFileUpload legacyUpload = uploadRepository.findByMetadataIdAndFileNameNotDeleted(metadataId, legacyFilename);
        legacyUpload.setAccess(null);
        legacyUpload.setMimeType(null);
        uploadRepository.save(legacyUpload);

        // Simulate a file that was never tracked at all: remove its row, leave the physical file.
        MetadataFileUpload untrackedUpload = uploadRepository.findByMetadataIdAndFileNameNotDeleted(metadataId, untrackedFilename);
        uploadRepository.deleteById(untrackedUpload.getId());
        uploadRepository.flush();

        new MetadataFileUploadBackfillTask().run(_applicationContext);

        MetadataFileUpload backfilledLegacy = uploadRepository.findByMetadataIdAndFileNameNotDeleted(metadataId, legacyFilename);
        assertEquals("Legacy row's access should be backfilled from its physical visibility",
            MetadataResourceVisibility.PUBLIC, backfilledLegacy.getAccess());
        assertNotNull("Legacy row's mimetype should be backfilled", backfilledLegacy.getMimeType());

        MetadataFileUpload backfilledUntracked = uploadRepository.findByMetadataIdAndFileNameNotDeleted(metadataId, untrackedFilename);
        assertNotNull("A tracking row should have been created for the previously untracked file", backfilledUntracked);
        assertEquals("Created row's access should match the file's physical visibility",
            MetadataResourceVisibility.PRIVATE, backfilledUntracked.getAccess());
        assertNotNull("Created row's mimetype should be detected", backfilledUntracked.getMimeType());
    }
}
