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


import io.searchbox.strings.StringUtils;
import jeeves.server.context.ServiceContext;
import org.apache.commons.io.FilenameUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.resources.JCloudCredentials;
import org.jclouds.blobstore.ContainerNotFoundException;
import org.jclouds.blobstore.domain.*;
import org.jclouds.blobstore.options.CopyOptions;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.ResourceHolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;

public class JCloudStore extends AbstractStore {

    @Autowired
    JCloudCredentials jCloudCredentials;

    @Override
    public List<MetadataResource> getResources(final ServiceContext context, final String metadataUuid,
                                               final MetadataResourceVisibility visibility, String filter, Boolean approved) throws Exception {
        final int metadataId = canEdit(context, metadataUuid, approved);
        final SettingManager settingManager = context.getBean(SettingManager.class);

        final String resourceTypeDir = getMetadataDir(metadataId) + jCloudCredentials.getFolderDelimiter() + visibility.toString() + jCloudCredentials.getFolderDelimiter();

        List<MetadataResource> resourceList = new ArrayList<>();
        if (filter == null) {
            filter = FilesystemStore.DEFAULT_FILTER;
        }

        ListContainerOptions opts = new ListContainerOptions();
        opts.delimiter(jCloudCredentials.getFolderDelimiter());
        opts.prefix(resourceTypeDir);

        // Page through the data
        String marker = null;
        do {
            if (marker != null) {
                opts.afterMarker(marker);
            }

            PageSet<? extends StorageMetadata> page = jCloudCredentials.getClient().getBlobStore().list(jCloudCredentials.getContainerName(), opts);

            for (StorageMetadata storageMetadata : page) {
                // Only add to the list if it is a blob and it matches the filter.
                if (storageMetadata.getType() == StorageType.BLOB && FilenameUtils.wildcardMatch(storageMetadata.getName(), filter)) {
                    final String filename = getFilename(storageMetadata.getName());
                    MetadataResource resource = createResourceDescription(settingManager, metadataUuid, visibility, filename, storageMetadata.getSize(),
                            storageMetadata.getLastModified());
                    resourceList.add(resource);
                }
            }
            marker = page.getNextMarker();
        } while (marker != null);

        resourceList.sort(MetadataResourceVisibility.sortByFileName);

        return resourceList;
    }

    private MetadataResource createResourceDescription(final SettingManager settingManager, final String metadataUuid,
                                                       final MetadataResourceVisibility visibility, final String resourceId, long size, Date lastModification) {
        return new FilesystemStoreResource(metadataUuid, getFilename(metadataUuid, resourceId),
                settingManager.getNodeURL() + "api/records/", visibility, size, lastModification);
    }

    private static String getFilename(final String key) {
        final String[] splittedKey = key.split("/");
        return splittedKey[splittedKey.length - 1];
    }

    @Override
    public ResourceHolder getResource(final ServiceContext context, final String metadataUuid, final MetadataResourceVisibility visibility,
                                      final String resourceId, Boolean approved) throws Exception {
        // Those characters should not be allowed by URL structure
        int metadataId = canDownload(context, metadataUuid, visibility, approved);
        try {
            final Blob object = jCloudCredentials.getClient().getBlobStore().getBlob(
                jCloudCredentials.getContainerName(), getKey(metadataUuid, metadataId, visibility, resourceId));
            final SettingManager settingManager = context.getBean(SettingManager.class);
            return new ResourceHolderImpl(object, createResourceDescription(settingManager, metadataUuid, visibility, resourceId,
                                                                            object.getMetadata().getSize(),
                                                                            object.getMetadata().getLastModified()));
        } catch (ContainerNotFoundException ignored) {
            throw new ResourceNotFoundException(
                    String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid));
        }
    }

    private String getKey(String metadataUuid, int metadataId, MetadataResourceVisibility visibility, String resourceId) {
        checkResourceId(resourceId);
        final String metadataDir = getMetadataDir(metadataId);
        return metadataDir + jCloudCredentials.getFolderDelimiter() + visibility.toString() + jCloudCredentials.getFolderDelimiter() + getFilename(metadataUuid, resourceId);
    }

    @Override
    public MetadataResource putResource(final ServiceContext context, final String metadataUuid, final String filename,
                                        final InputStream is, @Nullable final Date changeDate, final MetadataResourceVisibility visibility, Boolean approved)
            throws Exception {
        final SettingManager settingManager = context.getBean(SettingManager.class);
        final int metadataId = canEdit(context, metadataUuid, approved);
        String key = getKey(metadataUuid, metadataId, visibility, filename);

        // Todo - jcloud does not seem to allow setting the last modified date. May need to investigate to see if there are other options.
        //if (changeDate != null) {
        //    metadata.setLastModified(changeDate);

        Blob blob = jCloudCredentials.getClient().getBlobStore().blobBuilder(key)
                .payload(is)
                .contentLength(is.available())
                .build();
        // Upload the Blob
        jCloudCredentials.getClient().getBlobStore().putBlob(jCloudCredentials.getContainerName(), blob);
        Blob blobResults = jCloudCredentials.getClient().getBlobStore().getBlob(jCloudCredentials.getContainerName(), key);

        return createResourceDescription(settingManager, metadataUuid, visibility, filename, blobResults.getMetadata().getSize(),
                blobResults.getMetadata().getLastModified());

    }

    @Override
    public MetadataResource patchResourceStatus(final ServiceContext context, final String metadataUuid, final String resourceId,
                                                final MetadataResourceVisibility visibility, Boolean approved) throws Exception {
        SettingManager settingManager = context.getBean(SettingManager.class);
        int metadataId = canEdit(context, metadataUuid, approved);

        String sourceKey = null;
        StorageMetadata metadata = null;
        for (MetadataResourceVisibility sourceVisibility : MetadataResourceVisibility.values()) {
            final String key = getKey(metadataUuid, metadataId, sourceVisibility, resourceId);
            try {
                metadata = jCloudCredentials.getClient().getBlobStore().blobMetadata(jCloudCredentials.getContainerName(), key);
                if (metadata != null) {
                    if (sourceVisibility != visibility) {
                        sourceKey = key;
                        break;
                    } else {
                        // already the good visibility
                        return createResourceDescription(settingManager, metadataUuid, visibility, resourceId, metadata.getSize(),
                                metadata.getLastModified());
                    }
                }
            } catch (ContainerNotFoundException ignored) {
                // ignored
            }
        }
        if (sourceKey != null) {
            final String destKey = getKey(metadataUuid, metadataId, visibility, resourceId);

            jCloudCredentials.getClient().getBlobStore().copyBlob(jCloudCredentials.getContainerName(), sourceKey, jCloudCredentials.getContainerName(), destKey, CopyOptions.NONE);
            jCloudCredentials.getClient().getBlobStore().removeBlob(jCloudCredentials.getContainerName(), sourceKey);

            Blob blobResults = jCloudCredentials.getClient().getBlobStore().getBlob(jCloudCredentials.getContainerName(), destKey);

            return createResourceDescription(settingManager, metadataUuid, visibility, resourceId, blobResults.getMetadata().getSize(),
                    blobResults.getMetadata().getLastModified());
        } else {
            throw new ResourceNotFoundException(
                    String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid));
        }
    }

    @Override
    public String delResources(final ServiceContext context, final String metadataUuid, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        try {
            ListContainerOptions opts = new ListContainerOptions();
            opts.delimiter(jCloudCredentials.getFolderDelimiter());
            opts.prefix(getMetadataDir(metadataId));

            // Page through the data
            String marker = null;
            do {
                if (marker != null) {
                    opts.afterMarker(marker);
                }

                PageSet<? extends StorageMetadata> page = jCloudCredentials.getClient().getBlobStore().list(jCloudCredentials.getContainerName(), opts);

                for (StorageMetadata storageMetadata : page) {
                    System.out.println("removing = " + jCloudCredentials.getContainerName() + ":" + storageMetadata.getName());
// Todo uncomment and test
//                    jCloudCredentials.getClient().getBlobStore().removeBlob(jCloudCredentials.getContainerName(), storageMetadata.getName());
                }
                marker = page.getNextMarker();
            } while (marker != null);
            return String.format("Metadata '%s' directory removed.", metadataId);
        } catch (ContainerNotFoundException e) {
            return String.format("Unable to remove metadata '%s' directory.", metadataId);
        }
    }

    @Override
    public String delResource(final ServiceContext context, final String metadataUuid, final String resourceId, Boolean approved)
            throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);

        for (MetadataResourceVisibility visibility : MetadataResourceVisibility.values()) {
            if (tryDelResource(metadataUuid, metadataId, visibility, resourceId)) {
                return String.format("MetadataResource '%s' removed.", resourceId);
            }
        }
        return String.format("Unable to remove resource '%s'.", resourceId);
    }

    @Override
    public String delResource(final ServiceContext context, final String metadataUuid, final MetadataResourceVisibility visibility,
                              final String resourceId, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        if (tryDelResource(metadataUuid, metadataId, visibility, resourceId)) {
            return String.format("MetadataResource '%s' removed.", resourceId);
        }
        return String.format("Unable to remove resource '%s'.", resourceId);
    }

    private boolean tryDelResource(final String metadataUuid, final int metadataId, final MetadataResourceVisibility visibility,
                                   final String resourceId) throws Exception {
        final String key = getKey(metadataUuid, metadataId, visibility, resourceId);

        if (jCloudCredentials.getClient().getBlobStore().blobExists(jCloudCredentials.getContainerName(), key)) {
            jCloudCredentials.getClient().getBlobStore().removeBlob(jCloudCredentials.getContainerName(), key);
            return true;
        }
        return false;
    }

    @Override
    public MetadataResource getResourceDescription(final ServiceContext context, final String metadataUuid,
                                                   final MetadataResourceVisibility visibility, final String filename, Boolean approved) throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        final String key = getKey(metadataUuid, metadataId, visibility, filename);
        SettingManager settingManager = context.getBean(SettingManager.class);
        try {
            final Blob object = jCloudCredentials.getClient().getBlobStore().getBlob(jCloudCredentials.getContainerName(), key);
            if (object==null) {
                return null;
            } else {
                final StorageMetadata metadata = object.getMetadata();
                return createResourceDescription(settingManager, metadataUuid, visibility, filename, metadata.getSize(),
                        metadata.getLastModified());
            }
        } catch (ContainerNotFoundException e) {
            return null;
        }
    }

    private String getMetadataDir(final int metadataId) {
        return jCloudCredentials.getBaseFolder() + metadataId;
    }

    private static class ResourceHolderImpl implements ResourceHolder {
        private Path path;
        private final MetadataResource metadata;

        public ResourceHolderImpl(final Blob object, MetadataResource metadata) throws IOException {
            path = Files.createTempFile("", getFilename(object.getMetadata().getName()));
            this.metadata = metadata;
            try (InputStream in = object.getPayload().openStream()) {
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        @Override
        public Path getPath() {
            return path;
        }

        @Override
        public MetadataResource getMetadata() {
            return metadata;
        }

        @Override
        public void close() throws IOException {
            if (path != null) {
                Files.delete(path);
                path = null;
            }
        }

        @Override
        protected void finalize() throws Throwable {
            close();
            super.finalize();
        }
    }
}
