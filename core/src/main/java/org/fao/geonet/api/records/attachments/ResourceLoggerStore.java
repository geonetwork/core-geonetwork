/*
 * =============================================================================
 * ===	Copyright (C) 2001-2016 Food and Agriculture Organization of the
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
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataFileDownload;
import org.fao.geonet.domain.MetadataFileUpload;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceContainer;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.repository.MetadataFileDownloadRepository;
import org.fao.geonet.repository.MetadataFileUploadRepository;
import org.fao.geonet.util.ThreadPool;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Decorate a store and record put/get/delete operations in database for reporting statistics.
 */
public class ResourceLoggerStore extends AbstractStore {

    private Store decoratedStore;


    @Autowired private ThreadPool threadPool;

    public ResourceLoggerStore() {
        super();
    }

    public ResourceLoggerStore(Store decoratedStore) {
        this.decoratedStore = decoratedStore;
    }

    @Override
    public List<MetadataResource> getResources(ServiceContext context, String metadataUuid,
                                               MetadataResourceVisibility metadataResourceVisibility, String filter, Boolean approved)
            throws Exception {
        if (decoratedStore != null) {
            return decoratedStore.getResources(context, metadataUuid, metadataResourceVisibility, filter, approved);
        }
        return null;
    }

    @Override
    public List<MetadataResource> getResources(ServiceContext context, String metadataUuid,
                                               MetadataResourceVisibility metadataResourceVisibility, String filter, Boolean approved, boolean includeAdditionalIndexedProperties)
        throws Exception {
        if (decoratedStore != null) {
            return decoratedStore.getResources(context, metadataUuid, metadataResourceVisibility, filter, approved, includeAdditionalIndexedProperties);
        }
        return null;
    }

    @Override
    public ResourceHolder getResource(final ServiceContext context, final String metadataUuid, final MetadataResourceVisibility visibility,
                                      final String resourceId, Boolean approved) throws Exception {
        if (decoratedStore != null) {
            ResourceHolder holder = decoratedStore.getResource(context, metadataUuid, visibility, resourceId, approved);
            if (holder != null) {
                // TODO: Add Requester details which may have been provided by a form ?
                storeGetRequest(context, metadataUuid, holder.getMetadata().getFilename(), "", "", "", "", new ISODate().toString(), approved);
            }
            return holder;
        }
        return null;
    }

    @Override
    public MetadataResource getResourceMetadata(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, String resourceId, Boolean approved) throws Exception {
        if (decoratedStore != null) {
            return decoratedStore.getResourceMetadata(context, metadataUuid, visibility, resourceId, approved);
        }
        return null;
    }

    @Override
    public ResourceHolder getResourceWithRange(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, String resourceId, Boolean approved, long start, long end) throws Exception {
        if (decoratedStore != null) {
            ResourceHolder holder = decoratedStore.getResourceWithRange(context, metadataUuid, visibility, resourceId, approved, start, end);
            if (holder != null) {
                // TODO: Add Requester details which may have been provided by a form ?
                storeGetRequest(context, metadataUuid, holder.getMetadata().getFilename(), "", "", "", "", new ISODate().toString(), approved);
            }
            return holder;
        }
        return null;
    }

    @Override
    public ResourceHolder getResourceInternal(String metadataUuid, MetadataResourceVisibility visibility, String resourceId, Boolean approved) throws Exception {
        throw new UnsupportedOperationException("ResourceLoggerStore does not support getResourceInternal.");
    }

    @Override
    public MetadataResource putResource(final ServiceContext context, final String metadataUuid, final String filename,
                                        final InputStream is, @Nullable final Date changeDate, final MetadataResourceVisibility visibility,
                                        Boolean approved) throws Exception {
        if (decoratedStore != null) {
            final MetadataResource resource = decoratedStore
                    .putResource(context, metadataUuid, filename, is, changeDate, visibility, approved);
            if (resource != null) {
                storePutRequest(context, metadataUuid, resource.getFilename(), resource.getSize(), resource.getVisibility(),
                    resource.getMimeType(), approved);
            }
            return resource;
        }
        return null;
    }

    @Override
    public MetadataResource patchResourceStatus(ServiceContext context, String metadataUuid, String resourceId,
                                                MetadataResourceVisibility metadataResourceVisibility, Boolean approved) throws Exception {
        if (decoratedStore != null) {
            MetadataResource resource = decoratedStore.patchResourceStatus(context, metadataUuid, resourceId, metadataResourceVisibility, approved);
            if (resource != null) {
                storeAccessUpdateRequest(metadataUuid, resourceId, metadataResourceVisibility, approved);
            }
            return resource;
        }
        return null;
    }

    public String delResources(ServiceContext context, String metadataUuid, Boolean approved) throws Exception {
        if (decoratedStore != null) {
            return decoratedStore.delResources(context, metadataUuid, approved);
        }
        return null;
    }

    public String delResources(ServiceContext context, int metadataId) throws Exception {
        if (decoratedStore != null) {
            return decoratedStore.delResources(context, metadataId);
        }
        return null;
    }

    @Override
    public String delResource(ServiceContext context, String metadataUuid, String resourceId, Boolean approved) throws Exception {
        if (decoratedStore != null) {
            String response = decoratedStore.delResource(context, metadataUuid, resourceId, approved);
            if (response != null) {
                storeDeleteRequest(metadataUuid, getFilename(metadataUuid, resourceId), approved);
            }
        }
        return null;
    }

    @Override
    public String delResource(final ServiceContext context, final String metadataUuid,
                              final MetadataResourceVisibility metadataResourceVisibility, final String resourceId, final Boolean approved)
            throws Exception {
        if (decoratedStore != null) {
            String response = decoratedStore.delResource(context, metadataUuid, metadataResourceVisibility, resourceId, approved);
            if (response != null) {
                storeDeleteRequest(metadataUuid, getFilename(metadataUuid, resourceId), approved);
            }
        }
        return null;
    }

    @Override
    public MetadataResource getResourceDescription(final ServiceContext context, final String metadataUuid,
                                                   final MetadataResourceVisibility visibility, final String filename, Boolean approved)
            throws Exception {
        if (decoratedStore != null) {
            return decoratedStore.getResourceDescription(context, metadataUuid, visibility, filename, approved);
        }
        return null;
    }

    @Override
    public MetadataResourceContainer getResourceContainerDescription(ServiceContext context, String metadataUuid, Boolean approved) throws Exception {
        if (decoratedStore != null) {
            return decoratedStore.getResourceContainerDescription(context, metadataUuid, approved);
        }
        return null;
    }

    /**
     * * Stores a file download request in the MetadataFileDownloads table.
     */
    private void storeGetRequest(ServiceContext context, final String metadataUuid, final String resourceId, final String requesterName,
                                 final String requesterMail, final String requesterOrg, final String requesterComments,
                                 final String downloadDate, Boolean approved) throws Exception {
        final int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        final MetadataFileUploadRepository uploadRepository = context.getBean(MetadataFileUploadRepository.class);
        final MetadataFileDownloadRepository repo = context.getBean(MetadataFileDownloadRepository.class);
        final String userName = context.getUserSession().getUsername();

        threadPool.runTask(new Runnable() {
            @Override
            public void run() {
                MetadataFileUpload metadataFileUpload;

                // Each download is related to a file upload record
                try {
                    metadataFileUpload = uploadRepository.findByMetadataIdAndFileNameNotDeleted(metadataId, resourceId);

                } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
                    Log.debug(Geonet.RESOURCES, String.format(
                            "No references in FileNameNotDeleted repository for metadata '%s', resource id '%s'. Get request will not be "
                                    + "saved.",
                            metadataUuid, resourceId));

                    // No related upload is found
                    metadataFileUpload = null;
                }

                if (metadataFileUpload != null) {
                    MetadataFileDownload metadataFileDownload = new MetadataFileDownload();

                    metadataFileDownload.setMetadataId(metadataId);
                    metadataFileDownload.setFileName(resourceId);
                    metadataFileDownload.setRequesterName(requesterName);
                    metadataFileDownload.setRequesterMail(requesterMail);

                    metadataFileDownload.setRequesterOrg(requesterOrg);
                    metadataFileDownload.setRequesterComments(requesterComments);
                    metadataFileDownload.setDownloadDate(downloadDate);
                    metadataFileDownload.setUserName(userName);
                    metadataFileDownload.setFileUploadId(metadataFileUpload.getId());

                    repo.save(metadataFileDownload);
                }
            }
        });
    }

    /**
     * Stores a file upload delete request in the MetadataFileUploads table.
     */
    private void storeDeleteRequest(final String metadataUuid, final String fileName, Boolean approved) throws Exception {
        final ConfigurableApplicationContext context = ApplicationContextHolder.get();
        final int metadataId = getAndCheckMetadataId(metadataUuid, approved);

        MetadataFileUploadRepository repo = context.getBean(MetadataFileUploadRepository.class);

        try {
            MetadataFileUpload metadataFileUpload = repo.findByMetadataIdAndFileNameNotDeleted(metadataId, fileName);
            metadataFileUpload.setDeletedDate(new ISODate().toString());
            repo.save(metadataFileUpload);

        } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
            // File was deleted and the catalog never stored an upload request
            // This may happen if upload was done in an old catalog without upload logger.
        }
    }

    /**
     * Stores a file upload request in the MetadataFileUploads table.
     */
    private void storePutRequest(ServiceContext context, final String metadataUuid, final String fileName, final double fileSize,
                                 final MetadataResourceVisibility access, final String mimeType, Boolean approved)
            throws Exception {
        final MetadataFileUploadRepository repo = context.getBean(MetadataFileUploadRepository.class);
        final int metadataId = getAndCheckMetadataId(metadataUuid, approved);

        // In some stores, it allows the same file to be uploaded and overwritten if this occurs then we should delete the old and add the new.
        storeDeleteRequest(metadataUuid,fileName, approved);

        MetadataFileUpload metadataFileUpload = new MetadataFileUpload();

        metadataFileUpload.setMetadataId(metadataId);
        metadataFileUpload.setFileName(fileName);
        metadataFileUpload.setFileSize(fileSize);
        metadataFileUpload.setUploadDate(new ISODate().toString());
        metadataFileUpload.setUserName(context.getUserSession().getUsername());
        metadataFileUpload.setAccess(access);
        metadataFileUpload.setMimeType(mimeType);

        repo.save(metadataFileUpload);
    }

    /**
     * Updates the access/visibility of a previously logged file upload in the MetadataFileUploads table.
     */
    private void storeAccessUpdateRequest(final String metadataUuid, final String resourceId, final MetadataResourceVisibility access,
                                          Boolean approved) throws Exception {
        final ConfigurableApplicationContext context = ApplicationContextHolder.get();
        final int metadataId = getAndCheckMetadataId(metadataUuid, approved);

        MetadataFileUploadRepository repo = context.getBean(MetadataFileUploadRepository.class);

        try {
            MetadataFileUpload metadataFileUpload = repo.findByMetadataIdAndFileNameNotDeleted(metadataId, getFilename(metadataUuid, resourceId));
            metadataFileUpload.setAccess(access);
            repo.save(metadataFileUpload);
        } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
            // No upload record to update. May happen for resources uploaded before this logger tracked access.
        }
    }

    @Override
    public void copyResources(ServiceContext context, String sourceUuid, String targetUuid,
                              MetadataResourceVisibility metadataResourceVisibility, boolean sourceApproved, boolean targetApproved) throws Exception {
        if (decoratedStore != null) {
            decoratedStore.copyResources(context, sourceUuid, targetUuid, metadataResourceVisibility, sourceApproved, targetApproved);
        }

    }

    /**
     * Stores a file upload rename request in the MetadataFileUploads table.
     */
    private void storeRenameRequest(final String metadataUuid, final String resourceId, final MetadataResource renamedResource, Boolean approved) throws Exception {
        final ConfigurableApplicationContext context = ApplicationContextHolder.get();
        final int metadataId = getAndCheckMetadataId(metadataUuid, approved);

        MetadataFileUploadRepository repo = context.getBean(MetadataFileUploadRepository.class);
        String oldFileName = getFilename(metadataUuid, resourceId);
        String fullResourceId = metadataUuid + "/attachments/" + oldFileName;

        MetadataFileUpload metadataFileUpload = null;
        try {
            metadataFileUpload = repo.findByMetadataIdAndFileNameNotDeleted(metadataId, fullResourceId);
        } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
            try {
                metadataFileUpload = repo.findByMetadataIdAndFileNameNotDeleted(metadataId, resourceId);
            } catch (org.springframework.dao.EmptyResultDataAccessException ex2) {
                try {
                    metadataFileUpload = repo.findByMetadataIdAndFileNameNotDeleted(metadataId, oldFileName);
                } catch (org.springframework.dao.EmptyResultDataAccessException ex3) {
                    Log.debug(Geonet.RESOURCES, String.format(
                        "No references in MetadataFileUploads repository for metadata '%s', resource '%s' when renaming to '%s'.",
                        metadataUuid, resourceId, renamedResource.getId()));
                }
            }
        }

        if (metadataFileUpload != null) {
            String targetName = metadataFileUpload.getFileName().contains("/") ? renamedResource.getId() : renamedResource.getFilename();
            metadataFileUpload.setFileName(targetName);
            repo.save(metadataFileUpload);
        }
    }

    @Override
    public MetadataResource renameResource(ServiceContext context, String metadataUuid, String resourceId, String newName, Boolean approved) throws Exception {
        if (decoratedStore != null) {
            MetadataResource renamedResource = decoratedStore.renameResource(context, metadataUuid, resourceId, newName, approved);
            if (renamedResource != null) {
                storeRenameRequest(metadataUuid, resourceId, renamedResource, approved);
            }
            return renamedResource;
        }

        return null;
    }
}
