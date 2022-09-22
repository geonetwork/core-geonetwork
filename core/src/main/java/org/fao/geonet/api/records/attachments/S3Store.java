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

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceContainer;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.resources.S3Credentials;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;


public class S3Store extends AbstractStore {
    @Autowired
    S3Credentials s3;

    @Autowired
    SettingManager settingManager;

    @Override
    public List<MetadataResource> getResources(final ServiceContext context, final String metadataUuid,
            final MetadataResourceVisibility visibility, String filter, Boolean approved) throws Exception {
        final int metadataId = canEdit(context, metadataUuid, approved);

        final String resourceTypeDir = getMetadataDir(metadataId) + "/" + visibility.toString();

        List<MetadataResource> resourceList = new ArrayList<>();
        if (filter == null) {
            filter = FilesystemStore.DEFAULT_FILTER;
        }
        PathMatcher matcher =
                FileSystems.getDefault().getPathMatcher("glob:" + filter);

        final ListObjectsV2Result objects = s3.getClient().listObjectsV2(s3.getBucket(), resourceTypeDir);
        for (S3ObjectSummary object: objects.getObjectSummaries()) {
            final String key = object.getKey();
            final String filename = getFilename(key);
            Path keyPath = new File(filename).toPath().getFileName();
            if (matcher.matches(keyPath)) {
                MetadataResource resource = createResourceDescription(metadataUuid, visibility, filename, object.getSize(),
                                                                      object.getLastModified(), metadataId, approved);
                resourceList.add(resource);
            }
        }

        resourceList.sort(MetadataResourceVisibility.sortByFileName);

        return resourceList;
    }

    private MetadataResource createResourceDescription(final String metadataUuid,
            final MetadataResourceVisibility visibility, final String resourceId, long size, Date lastModification, int metadataId, boolean approved) {
        return new FilesystemStoreResource(metadataUuid, metadataId, getFilename(metadataUuid, resourceId),
                                           settingManager.getNodeURL() + "api/records/", visibility, size, lastModification, approved);
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
            final S3Object object = s3.getClient().getObject(
                s3.getBucket(), getKey(metadataUuid, metadataId, visibility, resourceId));
            return new ResourceHolderImpl(object, createResourceDescription(metadataUuid, visibility, resourceId,
                                                                            object.getObjectMetadata().getContentLength(),
                                                                            object.getObjectMetadata().getLastModified(), metadataId, approved));
        } catch (AmazonServiceException ignored) {
            throw new ResourceNotFoundException(
                String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
        }
    }

    @Override
    public ResourceHolder getResourceInternal(String metadataUuid, MetadataResourceVisibility visibility, String resourceId, Boolean approved) throws Exception {
        throw new UnsupportedOperationException("S3Store does not support getResourceInternal.");
    }

    private String getKey(String metadataUuid, int metadataId, MetadataResourceVisibility visibility, String resourceId) throws Exception {
        checkResourceId(resourceId);
        final String metadataDir = getMetadataDir(metadataId);
        return metadataDir + "/" + visibility.toString() + "/" + getFilename(metadataUuid, resourceId);
    }

    @Override
    public MetadataResource putResource(final ServiceContext context, final String metadataUuid, final String filename,
            final InputStream is, @Nullable final Date changeDate, final MetadataResourceVisibility visibility, Boolean approved)
            throws Exception {
        final int metadataId = canEdit(context, metadataUuid, approved);
        String key = getKey(metadataUuid, metadataId, visibility, filename);
        ObjectMetadata metadata = new ObjectMetadata();
        if (changeDate != null) {
            metadata.setLastModified(changeDate);
        }
        final PutObjectResult putAnswer = s3.getClient().putObject(s3.getBucket(), key, is, metadata);
        return createResourceDescription(metadataUuid, visibility, filename, putAnswer.getMetadata().getContentLength(),
                                         putAnswer.getMetadata().getLastModified(), metadataId, approved);
    }

    @Override
    public MetadataResource patchResourceStatus(final ServiceContext context, final String metadataUuid, final String resourceId,
            final MetadataResourceVisibility visibility, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);

        String sourceKey = null;
        ObjectMetadata metadata = null;
        for (MetadataResourceVisibility sourceVisibility: MetadataResourceVisibility.values()) {
            final String key = getKey(metadataUuid, metadataId, sourceVisibility, resourceId);
            try {
                metadata = s3.getClient().getObjectMetadata(s3.getBucket(), key);
                if (sourceVisibility != visibility) {
                    sourceKey = key;
                    break;
                } else {
                    // already the good visibility
                    return createResourceDescription(metadataUuid, visibility, resourceId, metadata.getContentLength(),
                                                     metadata.getLastModified(), metadataId, approved);
                }
            } catch (AmazonServiceException ignored) {
                // ignored
            }
        }
        if (sourceKey != null) {
            final String destKey = getKey(metadataUuid, metadataId, visibility, resourceId);
            final CopyObjectResult copyResult = s3.getClient().copyObject(
                s3.getBucket(), sourceKey, s3.getBucket(), destKey);
            s3.getClient().deleteObject(s3.getBucket(), sourceKey);
            return createResourceDescription(metadataUuid, visibility, resourceId, metadata.getContentLength(),
                                             copyResult.getLastModifiedDate(), metadataId, approved);
        } else {
            throw new ResourceNotFoundException(
                    String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid));
        }
    }

    @Override
    public String delResources(final ServiceContext context, final String metadataUuid, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        try {
            final ListObjectsV2Result objects = s3.getClient().listObjectsV2(
                s3.getBucket(), getMetadataDir(metadataId));
            for (S3ObjectSummary object: objects.getObjectSummaries()) {
                s3.getClient().deleteObject(s3.getBucket(), object.getKey());
            }
            return String.format("Metadata '%s' directory removed.", metadataId);
        } catch (AmazonServiceException e) {
            return String.format("Unable to remove metadata '%s' directory.", metadataId);
        }
    }

    @Override
    public String delResource(final ServiceContext context, final String metadataUuid, final String resourceId, Boolean approved)
            throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);

        for (MetadataResourceVisibility visibility: MetadataResourceVisibility.values()) {
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
        if (s3.getClient().doesObjectExist(s3.getBucket(), key)) {
            s3.getClient().deleteObject(s3.getBucket(), key);
            return true;
        }
        return false;
    }

    @Override
    public MetadataResource getResourceDescription(final ServiceContext context, final String metadataUuid,
            final MetadataResourceVisibility visibility, final String filename, Boolean approved) throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        final String key = getKey(metadataUuid, metadataId, visibility, filename);
        try {
            final ObjectMetadata metadata = s3.getClient().getObjectMetadata(s3.getBucket(), key);
            return createResourceDescription(metadataUuid, visibility, filename, metadata.getContentLength(),
                                             metadata.getLastModified(), metadataId, approved);
        } catch (AmazonServiceException e) {
            return null;
        }
    }

    @Override
    public MetadataResourceContainer getResourceContainerDescription(ServiceContext context, String metadataUuid, Boolean approved) throws Exception {

        int metadataId = getAndCheckMetadataId(metadataUuid, approved);

        return new FilesystemStoreResourceContainer(metadataUuid, metadataId, metadataUuid, settingManager.getNodeURL() + "api/records/", approved);
    }

    private String getMetadataDir(final int metadataId) {
        return s3.getKeyPrefix() + metadataId;
    }

    private static class ResourceHolderImpl implements ResourceHolder {
        private Path path;
        private final MetadataResource metadata;

        public ResourceHolderImpl(final S3Object object, MetadataResource metadata) throws IOException {
            path = Files.createTempFile("", getFilename(object.getKey()));
            this.metadata = metadata;
            try (S3ObjectInputStream in = object.getObjectContent()) {
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
