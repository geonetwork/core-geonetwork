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
import com.amazonaws.services.s3.model.*;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.exception.ResourceAlreadyExistException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceContainer;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.resources.S3Credentials;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;


/**
 * Visibility (public/private) is tracked in the database ({@code MetadataFileUploads.resourceaccess},
 * populated by {@link ResourceLoggerStore}), not by which key prefix an object is stored under -
 * the {@code <visibility>/} prefix below is the legacy layout, kept only as a read/write fallback
 * for objects that predate this and haven't been touched since (every put, rename, or visibility
 * change migrates an object to the flat key).
 */
public class S3Store extends AbstractStore {
    @Autowired
    S3Credentials s3;

    @Autowired
    SettingManager settingManager;

    @Override
    public List<MetadataResource> getResources(final ServiceContext context, final String metadataUuid,
            final MetadataResourceVisibility visibility, String filter, Boolean approved, boolean includeAdditionalIndexedProperties) throws Exception {
        final int metadataId = canEdit(context, metadataUuid, approved);

        final String metadataDir = getMetadataDir(metadataId);
        final String metadataDirPrefix = metadataDir + "/";

        List<MetadataResource> resourceList = new ArrayList<>();
        if (filter == null) {
            filter = FilesystemStore.DEFAULT_FILTER;
        }
        PathMatcher matcher =
                FileSystems.getDefault().getPathMatcher("glob:" + filter);

        // A single listing under the whole metadata prefix returns both legacy (<visibility>/...)
        // and flat keys together (S3 prefixes aren't real folders); classify each key by which
        // layout it's in rather than issuing a separate request per layout.
        Map<String, MetadataResourceVisibility> trackedAccess = loadTrackedAccessByFilename(metadataId);
        final ListObjectsV2Result objects = s3.getClient().listObjectsV2(s3.getBucket(), metadataDir);
        for (S3ObjectSummary object: objects.getObjectSummaries()) {
            final String key = object.getKey();
            final String relativeKey = key.startsWith(metadataDirPrefix) ? key.substring(metadataDirPrefix.length()) : getFilename(key);

            final MetadataResourceVisibility legacyVisibility = legacyVisibilityOf(relativeKey);
            final String filename;
            final MetadataResourceVisibility resourceVisibility;
            if (legacyVisibility != null) {
                // Legacy layout: membership under the old <visibility>/ prefix is the visibility.
                filename = relativeKey.substring(legacyVisibility.toString().length() + 1);
                resourceVisibility = legacyVisibility;
            } else {
                // Flat layout: only a tracked-access match counts, since the key alone no longer
                // implies visibility.
                filename = relativeKey;
                resourceVisibility = trackedAccess.get(filename);
            }
            if (resourceVisibility != visibility) {
                continue;
            }

            Path keyPath = new File(filename).toPath().getFileName();
            if (matcher.matches(keyPath)) {
                // S3ObjectSummary does not carry the object's Content-Type; avoid an extra
                // getObjectMetadata round trip per listed object and detect from the filename.
                resourceList.add(createResourceDescription(metadataUuid, visibility, filename, object.getSize(),
                                                            object.getLastModified(), metadataId, approved,
                                                            MimeTypeDetector.detect(filename)));
            }
        }

        resourceList.sort(MetadataResourceVisibility.sortByFileName);

        return resourceList;
    }

    /**
     * Whether a metadata-dir-relative key falls under one of the legacy {@code public}/
     * {@code private} prefixes, and if so which. A nested-path resource whose own first segment
     * happens to be literally "public" or "private" is indistinguishable from this - a narrow,
     * pre-existing ambiguity of keeping both layouts side by side.
     */
    private static MetadataResourceVisibility legacyVisibilityOf(String relativeKey) {
        for (MetadataResourceVisibility v : MetadataResourceVisibility.values()) {
            if (relativeKey.startsWith(v.toString() + "/")) {
                return v;
            }
        }
        return null;
    }

    private MetadataResource createResourceDescription(final String metadataUuid,
            final MetadataResourceVisibility visibility, final String resourceId, long size, Date lastModification, int metadataId,
            boolean approved, String mimeType) {
        return new FilesystemStoreResource(metadataUuid, metadataId, getFilename(metadataUuid, resourceId),
                                           settingManager.getNodeURL() + "api/records/", visibility, size, lastModification, null, null,
                                           approved, mimeType);
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
        String key = resolveExistingKey(metadataUuid, metadataId, visibility, getFilename(metadataUuid, resourceId), approved);
        if (key == null) {
            throw new ResourceNotFoundException(
                String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
        }
        try {
            final S3Object object = s3.getClient().getObject(s3.getBucket(), key);
            return new S3ResourceHolder(object, createResourceDescription(metadataUuid, visibility, resourceId,
                                                                            object.getObjectMetadata().getContentLength(),
                                                                            object.getObjectMetadata().getLastModified(), metadataId, approved,
                                                                            object.getObjectMetadata().getContentType()));
        } catch (AmazonServiceException ignored) {
            throw new ResourceNotFoundException(
                String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
        }
    }

    @Override
    public MetadataResource getResourceMetadata(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, String resourceId, Boolean approved) throws Exception {
        // Those characters should not be allowed by URL structure
        int metadataId = canDownload(context, metadataUuid, visibility, approved);
        String key = resolveExistingKey(metadataUuid, metadataId, visibility, getFilename(metadataUuid, resourceId), approved);
        if (key == null) {
            throw new ResourceNotFoundException(
                String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
        }
        try {
            final ObjectMetadata objectMetadata = s3.getClient().getObjectMetadata(s3.getBucket(), key);
            return createResourceDescription(metadataUuid, visibility, resourceId,
                objectMetadata.getContentLength(),
                objectMetadata.getLastModified(), metadataId, approved, objectMetadata.getContentType());
        } catch (AmazonServiceException ignored) {
            throw new ResourceNotFoundException(
                String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
        }
    }

    @Override
    public ResourceHolder getResourceWithRange(ServiceContext context, String metadataUuid, MetadataResourceVisibility metadataResourceVisibility, String resourceId, Boolean approved, long start, long end) throws Exception {
        // Those characters should not be allowed by URL structure
        int metadataId = canDownload(context, metadataUuid, metadataResourceVisibility, approved);
        String key = resolveExistingKey(metadataUuid, metadataId, metadataResourceVisibility, getFilename(metadataUuid, resourceId), approved);
        if (key == null) {
            throw new ResourceNotFoundException(
                String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
        }
        try {
            GetObjectRequest rangeGetObjectRequest = new GetObjectRequest(s3.getBucket(), key).withRange(start, end);
            final S3Object object = s3.getClient().getObject(rangeGetObjectRequest);
            // We use getInstanceLength here to get the full length of the object not the length of the range
            return new S3ResourceHolder(object, createResourceDescription(metadataUuid, metadataResourceVisibility, resourceId,
                object.getObjectMetadata().getInstanceLength(),
                object.getObjectMetadata().getLastModified(), metadataId, approved, object.getObjectMetadata().getContentType()));
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

    /** The legacy, pre-flattening key: {@code <metadataDir>/<visibility>/<filename>}. */
    private String getLegacyKey(String metadataUuid, int metadataId, MetadataResourceVisibility visibility, String resourceId) throws Exception {
        checkResourceId(resourceId);
        return getMetadataDir(metadataId) + "/" + visibility.toString() + "/" + getFilename(metadataUuid, resourceId);
    }

    /** The flat, visibility-less key: {@code <metadataDir>/<filename>}. */
    private String getFlatKey(String metadataUuid, int metadataId, String resourceId) throws Exception {
        checkResourceId(resourceId);
        return getMetadataDir(metadataId) + "/" + getFilename(metadataUuid, resourceId);
    }

    /**
     * Resolve the key of an existing resource. Prefers the flat, visibility-less key and falls
     * back to the legacy {@code <visibility>/} prefix for objects that predate it and haven't
     * been touched since.
     * <p>
     * Once flat, an object's key no longer enforces which visibility it may be fetched as - that
     * was the prefix split's job. So a flat match is only honoured if its <em>tracked</em> access
     * agrees with {@code visibility}; a mismatch (or an untracked flat object, which shouldn't
     * happen since every write path logs a tracking row) is treated as not found at this
     * visibility, exactly as an actually-private object can't be read today by asking for the
     * public one.
     *
     * @return the resolved key, or {@code null} if not found at all, or not at this visibility.
     */
    private String resolveExistingKey(String metadataUuid, int metadataId, MetadataResourceVisibility visibility, String filename,
                                      Boolean approved) throws Exception {
        String flatKey = getFlatKey(metadataUuid, metadataId, filename);
        if (s3.getClient().doesObjectExist(s3.getBucket(), flatKey)) {
            return visibility == resolveVisibility(metadataUuid, approved, filename) ? flatKey : null;
        }
        String legacyKey = getLegacyKey(metadataUuid, metadataId, visibility, filename);
        return s3.getClient().doesObjectExist(s3.getBucket(), legacyKey) ? legacyKey : null;
    }

    @Override
    public MetadataResource putResource(final ServiceContext context, final String metadataUuid, final String filename,
            final InputStream is, @Nullable final Date changeDate, final MetadataResourceVisibility visibility, Boolean approved)
            throws Exception {
        final int metadataId = canEdit(context, metadataUuid, approved);
        String key = getFlatKey(metadataUuid, metadataId, filename);
        ObjectMetadata metadata = new ObjectMetadata();
        if (changeDate != null) {
            metadata.setLastModified(changeDate);
        }
        String mimeType = MimeTypeDetector.detect(filename);
        metadata.setContentType(mimeType);
        final PutObjectResult putAnswer = s3.getClient().putObject(s3.getBucket(), key, is, metadata);
        return createResourceDescription(metadataUuid, visibility, filename, putAnswer.getMetadata().getContentLength(),
                                         putAnswer.getMetadata().getLastModified(), metadataId, approved, mimeType);
    }

    @Override
    public MetadataResource patchResourceStatus(final ServiceContext context, final String metadataUuid, final String resourceId,
            final MetadataResourceVisibility visibility, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        String filename = getFilename(metadataUuid, resourceId);
        String flatKey = getFlatKey(metadataUuid, metadataId, filename);

        try {
            ObjectMetadata metadata = s3.getClient().getObjectMetadata(s3.getBucket(), flatKey);
            // Already flat: visibility is purely a database attribute there, updated by
            // ResourceLoggerStore regardless of what happens here - no S3 call needed.
            return createResourceDescription(metadataUuid, visibility, resourceId, metadata.getContentLength(),
                                             metadata.getLastModified(), metadataId, approved, metadata.getContentType());
        } catch (AmazonServiceException ignored) {
            // Not flat yet - look for it under a legacy visibility prefix instead.
        }

        for (MetadataResourceVisibility sourceVisibility: visibilityCandidates(metadataUuid, approved, resourceId)) {
            final String legacyKey = getLegacyKey(metadataUuid, metadataId, sourceVisibility, resourceId);
            try {
                ObjectMetadata metadata = s3.getClient().getObjectMetadata(s3.getBucket(), legacyKey);
                // Migrate to the flat layout as a side effect of this move, same as put/rename.
                final CopyObjectResult copyResult = s3.getClient().copyObject(s3.getBucket(), legacyKey, s3.getBucket(), flatKey);
                s3.getClient().deleteObject(s3.getBucket(), legacyKey);
                return createResourceDescription(metadataUuid, visibility, resourceId, metadata.getContentLength(),
                                                 copyResult.getLastModifiedDate(), metadataId, approved, metadata.getContentType());
            } catch (AmazonServiceException ignored) {
                // try next candidate visibility
            }
        }
        throw new ResourceNotFoundException(
                String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid));
    }

    @Override
    public MetadataResource renameResource(ServiceContext context, String metadataUuid, String resourceId, String newName, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        checkResourceId(newName);
        String filename = getFilename(metadataUuid, resourceId);
        String flatKey = getFlatKey(metadataUuid, metadataId, filename);

        String sourceKey = null;
        ObjectMetadata objectMetadata = null;
        MetadataResourceVisibility resolvedVisibility = null;
        try {
            objectMetadata = s3.getClient().getObjectMetadata(s3.getBucket(), flatKey);
            sourceKey = flatKey;
            resolvedVisibility = resolveVisibility(metadataUuid, approved, filename);
        } catch (AmazonServiceException ignored) {
            for (MetadataResourceVisibility candidate : visibilityCandidates(metadataUuid, approved, resourceId)) {
                final String legacyKey = getLegacyKey(metadataUuid, metadataId, candidate, resourceId);
                try {
                    objectMetadata = s3.getClient().getObjectMetadata(s3.getBucket(), legacyKey);
                    sourceKey = legacyKey;
                    resolvedVisibility = candidate;
                    break;
                } catch (AmazonServiceException ignored2) {
                    // try next candidate
                }
            }
        }

        if (sourceKey == null) {
            throw new ResourceNotFoundException(
                    String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                    .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                    .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
        }

        final String destKey = getFlatKey(metadataUuid, metadataId, newName);
        if (sourceKey.equals(destKey)) {
            return createResourceDescription(metadataUuid, resolvedVisibility, newName, objectMetadata.getContentLength(),
                                             objectMetadata.getLastModified(), metadataId, approved, objectMetadata.getContentType());
        }
        try {
            s3.getClient().getObjectMetadata(s3.getBucket(), destKey);
            throw new ResourceAlreadyExistException(
                String.format("A resource with name '%s' and status '%s' already exists for metadata '%s'.",
                    newName, resolvedVisibility, metadataUuid));
        } catch (AmazonServiceException ignored) {
            // destination does not exist, safe to proceed
        }
        // Migrates to the flat layout as a side effect of the rename, same as patchResourceStatus.
        final CopyObjectResult copyResult = s3.getClient().copyObject(
            s3.getBucket(), sourceKey, s3.getBucket(), destKey);
        s3.getClient().deleteObject(s3.getBucket(), sourceKey);
        return createResourceDescription(metadataUuid, resolvedVisibility, newName, objectMetadata.getContentLength(),
                                         copyResult.getLastModifiedDate(), metadataId, approved, objectMetadata.getContentType());
    }

    @Override
    public String delResources(final ServiceContext context, final int metadataId) throws Exception {
        try {
            final ListObjectsV2Result objects = s3.getClient().listObjectsV2(
                s3.getBucket(), getMetadataDir(metadataId));

            Log.info(Geonet.RESOURCES, String.format("Deleting all files from metadataId '%s'", metadataId));
            for (S3ObjectSummary object: objects.getObjectSummaries()) {
                s3.getClient().deleteObject(s3.getBucket(), object.getKey());
            }
            Log.info(Geonet.RESOURCES,
                String.format("Metadata '%d' directory removed.", metadataId));
            return String.format("Metadata '%d' directory removed.", metadataId);
        } catch (AmazonServiceException e) {
            Log.warning(Geonet.RESOURCES,
                String.format("Unable to remove metadata '%d' directory. %s", metadataId, e.getMessage()));
            return String.format("Unable to remove metadata '%d' directory.", metadataId);
        }
    }

    @Override
    public String delResource(final ServiceContext context, final String metadataUuid, final String resourceId, Boolean approved)
            throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);

        for (MetadataResourceVisibility visibility: visibilityCandidates(metadataUuid, approved, resourceId)) {
            if (tryDelResource(metadataUuid, metadataId, visibility, resourceId, approved)) {
                return String.format("Metadata resource '%s' removed.", resourceId);
            }
        }
        return String.format("Unable to remove resource '%s'.", resourceId);
    }

    @Override
    public String delResource(final ServiceContext context, final String metadataUuid, final MetadataResourceVisibility visibility,
            final String resourceId, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        if (tryDelResource(metadataUuid, metadataId, visibility, resourceId, approved)) {
            return String.format("Metadata resource '%s' removed.", resourceId);
        }
        return String.format("Unable to remove resource '%s'.", resourceId);
    }

    private boolean tryDelResource(final String metadataUuid, final int metadataId, final MetadataResourceVisibility visibility,
            final String resourceId, Boolean approved) throws Exception {
        String key = resolveExistingKey(metadataUuid, metadataId, visibility, getFilename(metadataUuid, resourceId), approved);
        if (key != null) {
            s3.getClient().deleteObject(s3.getBucket(), key);
            Log.info(Geonet.RESOURCES,
                String.format("Resource '%s' removed for metadata %d (%s).", resourceId, metadataId, metadataUuid));
            return true;
        }
        Log.info(Geonet.RESOURCES,
            String.format("Unable to remove resource '%s' for metadata %d (%s).", resourceId, metadataId, metadataUuid));
        return false;
    }

    @Override
    public MetadataResource getResourceDescription(final ServiceContext context, final String metadataUuid,
            final MetadataResourceVisibility visibility, final String filename, Boolean approved) throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        String key = resolveExistingKey(metadataUuid, metadataId, visibility, filename, approved);
        if (key == null) {
            return null;
        }
        try {
            final ObjectMetadata metadata = s3.getClient().getObjectMetadata(s3.getBucket(), key);
            return createResourceDescription(metadataUuid, visibility, filename, metadata.getContentLength(),
                                             metadata.getLastModified(), metadataId, approved, metadata.getContentType());
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

    private static class S3ResourceHolder implements ResourceHolder {
        private final InputStreamResource resource;
        private final MetadataResource metadata;
        private final InputStream inputStream;

        public S3ResourceHolder(final S3Object object, MetadataResource metadata) {
            this.metadata = metadata;
            this.inputStream = object.getObjectContent();
            this.resource = new InputStreamResource(inputStream);
        }

        @Override
        public Resource getResource() {
            return resource;
        }

        @Override
        public MetadataResource getMetadata() {
            return metadata;
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
        }
    }
}
