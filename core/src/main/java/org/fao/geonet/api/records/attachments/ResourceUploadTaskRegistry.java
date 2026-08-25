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

import org.fao.geonet.api.exception.ResourceAlreadyExistException;
import org.fao.geonet.domain.ResourceUploadTask;
import org.fao.geonet.repository.ResourceUploadTaskRepository;
import org.springframework.stereotype.Component;

/**
 * Resolves the filename for a resource upload and checks for duplicates.
 *
 * <p>When the caller is an asynchronous, node-local execution (identified by
 * {@link AsyncResourceUploadProgressListener}), the filename resolution and claim
 * acquisition is performed in a separate transaction and persisted in the
 * database. The caller is responsible for ensuring that the claim is released
 * when the upload is complete or cancelled.
 */
@Component
public class ResourceUploadTaskRegistry {
    private final ResourceUploadTaskRepository taskRepository;

    /**
     * Creates a registry backed by the persistent upload-task repository.
     *
     * @param taskRepository repository used to inspect active filename claims
     */
    public ResourceUploadTaskRegistry(
        ResourceUploadTaskRepository taskRepository
    ) {
        this.taskRepository = taskRepository;
    }

    /**
     * Reports the resolved filename and prevents it from conflicting with an
     * active asynchronous upload.
     *
     * <p>Asynchronous listeners acquire their own atomic database claim from
     * {@link ResourceUploadProgressListener#onFilenameResolved(String)}.
     * Synchronous callers are checked against existing claims after their
     * filename callback completes.
     *
     * @param metadataUuid UUID of the metadata record receiving the resource
     * @param filename resolved resource filename
     * @param progressListener listener associated with the upload
     * @throws ResourceAlreadyExistException if the filename is already claimed
     *         or the listener rejects it
     */
    public void resolveFilenameAndCheck(
        String metadataUuid,
        String filename,
        ResourceUploadProgressListener progressListener
    ) throws ResourceAlreadyExistException {
        progressListener.onFilenameResolved(filename);

        // The asynchronous listener persisted and acquired its claim in
        // onFilenameResolved().
        if (progressListener
            instanceof AsyncResourceUploadProgressListener) {
            return;
        }

        String claimKey = ResourceUploadTask.activeClaimKey(
            metadataUuid,
            filename
        );

        if (taskRepository.existsByClaimKey(claimKey)) {
            throw new ResourceAlreadyExistException(
                String.format(
                    "An upload for filename '%s' is already in progress "
                        + "for record '%s'.",
                    filename,
                    metadataUuid
                )
            );
        }
    }
}

