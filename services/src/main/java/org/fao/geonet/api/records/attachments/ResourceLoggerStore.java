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

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataFileDownload;
import org.fao.geonet.domain.MetadataFileUpload;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.MetadataFileDownloadRepository;
import org.fao.geonet.repository.MetadataFileUploadRepository;
import org.fao.geonet.util.ThreadPool;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import jeeves.server.context.ServiceContext;

/**
 * Decorate a store and record put/get/delete operations in database for reporting statistics.
 */
public class ResourceLoggerStore implements Store {

    private Store decoratedStore;

    @Autowired
    private ThreadPool threadPool;

    public ResourceLoggerStore() {
        super();
    }

    public ResourceLoggerStore(Store decoratedStore) {
        this.decoratedStore = decoratedStore;
    }

    @Override
    public List<MetadataResource> getResources(String metadataUuid, Sort sort, String filter) throws Exception {
        if (decoratedStore != null) {
            return decoratedStore.getResources(metadataUuid, sort, filter);
        }
        return null;
    }

    @Override
    public List<MetadataResource> getResources(String metadataUuid, MetadataResourceVisibility metadataResourceVisibility, String filter) throws Exception {
        if (decoratedStore != null) {
            return decoratedStore.getResources(metadataUuid, metadataResourceVisibility, filter);
        }
        return null;
    }

    @Override
    public Path getResource(String metadataUuid, String resourceId) throws Exception {
        if (decoratedStore != null) {
            Path filePath = decoratedStore.getResource(metadataUuid, resourceId);
            if (filePath != null) {
                // TODO: Add Requester details which may have been provided by a form ?
                storeGetRequest(metadataUuid, resourceId,
                    "", "", "", "",
                    new ISODate().toString());
            }
            return filePath;
        }
        return null;
    }

    @Override
    public MetadataResource putResource(String metadataUuid, MultipartFile file, MetadataResourceVisibility metadataResourceVisibility) throws Exception {
        if (decoratedStore != null) {
            MetadataResource resource = decoratedStore.putResource(metadataUuid, file, metadataResourceVisibility);
            if (resource != null) {
                storePutRequest(metadataUuid,
                    resource.getId(),
                    resource.getSize());
            }
            return resource;
        }
        return null;
    }

    @Override
    public MetadataResource putResource(String metadataUuid, Path filePath, MetadataResourceVisibility metadataResourceVisibility) throws Exception {
        if (decoratedStore != null) {
            return decoratedStore.putResource(metadataUuid, filePath, metadataResourceVisibility);
        }
        return null;
    }

    @Override
    public MetadataResource putResource(String metadataUuid, URL fileUrl, MetadataResourceVisibility metadataResourceVisibility) throws Exception {
        if (decoratedStore != null) {
            return decoratedStore.putResource(metadataUuid, fileUrl, metadataResourceVisibility);
        }
        return null;
    }

    @Override
    public MetadataResource patchResourceStatus(String metadataUuid, String resourceId, MetadataResourceVisibility metadataResourceVisibility) throws Exception {
        if (decoratedStore != null) {
            return decoratedStore.patchResourceStatus(metadataUuid, resourceId, metadataResourceVisibility);
        }
        return null;
    }

    @Override
    public String delResource(String metadataUuid) throws Exception {
        if (decoratedStore != null) {
            return decoratedStore.delResource(metadataUuid);
        }
        return null;
    }

    @Override
    public String delResource(String metadataUuid, String resourceId) throws Exception {
        if (decoratedStore != null) {
            String response = decoratedStore.delResource(metadataUuid, resourceId);
            if (response != null) {
                storeDeleteRequest(metadataUuid, resourceId);
            }
        }
        return null;
    }


    /**
     * * Stores a file download request in the MetadataFileDownloads table.
     */
    private void storeGetRequest(final String metadataUuid,
                                 final String resourceId,
                                 final String requesterName,
                                 final String requesterMail,
                                 final String requesterOrg,
                                 final String requesterComments,
                                 final String downloadDate) throws Exception {
        ServiceContext context = ServiceContext.get();

        final int metadataId =
            Integer.valueOf(context.getBean(DataManager.class).getMetadataId(metadataUuid));
        final MetadataFileUploadRepository uploadRepository =
            context.getBean(MetadataFileUploadRepository.class);
        final MetadataFileDownloadRepository repo =
            context.getBean(MetadataFileDownloadRepository.class);
        final String userName = context.getUserSession().getUsername();

        threadPool.runTask(new Runnable() {
            @Override
            public void run() {
                MetadataFileUpload metadataFileUpload;

                // Each download is related to a file upload record
                try {
                    metadataFileUpload = uploadRepository.findByMetadataIdAndFileNameNotDeleted(metadataId, resourceId);

                } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
                    Log.debug(Geonet.RESOURCES,
                        String.format("No references in FileNameNotDeleted repository for metadata '%s', resource id '%s'. Get request will not be saved.",
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
    private void storeDeleteRequest(final String metadataUuid,
                                    final String fileName) throws Exception {
        ServiceContext context = ServiceContext.get();
        final int metadataId =
            Integer.valueOf(context.getBean(DataManager.class).getMetadataId(metadataUuid));

        MetadataFileUploadRepository repo = context.getBean(MetadataFileUploadRepository.class);

        try {
            MetadataFileUpload metadataFileUpload =
                repo.findByMetadataIdAndFileNameNotDeleted(metadataId, fileName);
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
    private void storePutRequest(final String metadataUuid,
                                 final String fileName,
                                 final double fileSize) throws Exception {
        ServiceContext context = ServiceContext.get();
        final MetadataFileUploadRepository repo =
            context.getBean(MetadataFileUploadRepository.class);
        final int metadataId =
            Integer.valueOf(context.getBean(DataManager.class).getMetadataId(metadataUuid));

        MetadataFileUpload metadataFileUpload = new MetadataFileUpload();

        metadataFileUpload.setMetadataId(metadataId);
        metadataFileUpload.setFileName(fileName);
        metadataFileUpload.setFileSize(fileSize);
        metadataFileUpload.setUploadDate(new ISODate().toString());
        metadataFileUpload.setUserName(context.getUserSession().getUsername());

        repo.save(metadataFileUpload);
    }
}
