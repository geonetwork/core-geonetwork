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

package org.fao.geonet.domain;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests resource upload task creation, progress calculations, claim keys, and
 * status predicates.
 */
public class ResourceUploadTaskTest {

    @Test
    public void createInitializesPendingTask() {
        ResourceUploadTask task = ResourceUploadTask.create(
            "metadata-uuid",
            42,
            "https://example.org/file.zip",
            MetadataResourceVisibility.PUBLIC,
            false,
            "worker-id"
        );

        assertNotNull(task.getId());
        assertEquals("metadata-uuid", task.getMetadataUuid());
        assertEquals(Integer.valueOf(42), task.getOwnerUserId());
        assertEquals(
            "https://example.org/file.zip",
            task.getSourceUrl()
        );
        assertEquals(
            MetadataResourceVisibility.PUBLIC,
            task.getVisibility()
        );
        assertEquals(Boolean.FALSE, task.getApproved());
        assertEquals(ResourceUploadTaskStatus.PENDING, task.getStatus());
        assertEquals(0, task.getBytesTransferred());
        assertEquals(-1, task.getTotalBytes());
        assertNotNull(task.getSubmittedDateTime());
        assertNotNull(task.getLastHeartbeatDateTime());
        assertEquals("worker-id", task.getWorkerId());
        assertEquals(
            ResourceUploadTask.releasedClaimKey(task.getId()),
            task.getClaimKey()
        );
        assertFalse(task.isTerminal());
    }

    @Test
    public void releasedClaimKeyIsUniquePerTask() {
        assertEquals(
            "task:first",
            ResourceUploadTask.releasedClaimKey("first")
        );

        assertNotEquals(
            ResourceUploadTask.releasedClaimKey("first"),
            ResourceUploadTask.releasedClaimKey("second")
        );
    }

    @Test
    public void activeClaimKeyIsDeterministic() {
        String first = ResourceUploadTask.activeClaimKey(
            "metadata",
            "file.zip"
        );

        String second = ResourceUploadTask.activeClaimKey(
            "metadata",
            "file.zip"
        );

        assertEquals(first, second);
        assertTrue(first.startsWith("upload:"));
        assertTrue(first.length() <= 80);
    }

    @Test
    public void activeClaimKeySeparatesMetadataAndFilename() {
        assertNotEquals(
            ResourceUploadTask.activeClaimKey("ab", "c"),
            ResourceUploadTask.activeClaimKey("a", "bc")
        );

        assertNotEquals(
            ResourceUploadTask.activeClaimKey("metadata-1", "file.zip"),
            ResourceUploadTask.activeClaimKey("metadata-2", "file.zip")
        );

        assertNotEquals(
            ResourceUploadTask.activeClaimKey("metadata", "first.zip"),
            ResourceUploadTask.activeClaimKey("metadata", "second.zip")
        );
    }

    @Test
    public void terminalStatusIsDelegatedToStatusDefinition() {
        ResourceUploadTask task = task();

        for (ResourceUploadTaskStatus status :
            ResourceUploadTaskStatus.values()) {

            task.setStatus(status);
            assertEquals(status.isTerminal(), task.isTerminal());
        }
    }

    @Test
    public void statusClassificationsAreComplete() {
        assertEquals(
            Arrays.asList(
                ResourceUploadTaskStatus.PENDING,
                ResourceUploadTaskStatus.UPLOADING,
                ResourceUploadTaskStatus.FINALIZING,
                ResourceUploadTaskStatus.CANCELLING
            ),
            ResourceUploadTaskStatus.getNonTerminalStatuses()
        );

        assertEquals(
            Arrays.asList(
                ResourceUploadTaskStatus.COMPLETED,
                ResourceUploadTaskStatus.FAILED,
                ResourceUploadTaskStatus.CANCELLED
            ),
            ResourceUploadTaskStatus.getTerminalStatuses()
        );

        assertEquals(
            Arrays.asList(
                ResourceUploadTaskStatus.PENDING,
                ResourceUploadTaskStatus.UPLOADING,
                ResourceUploadTaskStatus.FINALIZING
            ),
            ResourceUploadTaskStatus.getFailableStatuses()
        );

    }

    private ResourceUploadTask task() {
        return ResourceUploadTask.create(
            "metadata",
            42,
            "https://example.org/file.zip",
            MetadataResourceVisibility.PUBLIC,
            false,
            "worker"
        );
    }
}
