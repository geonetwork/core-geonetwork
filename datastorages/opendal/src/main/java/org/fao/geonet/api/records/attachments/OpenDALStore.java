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
import org.apache.commons.io.IOUtils;
import org.apache.opendal.Entry;
import org.apache.opendal.Metadata;
import org.apache.opendal.OpenDALException;
import org.apache.opendal.Operator;
import org.apache.opendal.ReadOptions;
import org.apache.opendal.WriteOptions;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceContainer;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.resources.OpenDALConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OpenDALStore extends AbstractStore {

    // Object stores need multipart parts of at least 5 MiB (S3), so buffer uploads in 8 MiB chunks
    // instead of relying on the 16 KB writes coming from OperatorOutputStream.
    private static final WriteOptions UPLOAD_OPTIONS = WriteOptions.builder().chunk(8L * 1024 * 1024).build();

    @Autowired
    private OpenDALConfiguration openDALConfiguration;

    @Autowired
    private SettingManager settingManager;

    @Autowired
    private GeonetworkDataDirectory dataDirectory;

    @Override
    public List<MetadataResource> getResources(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, String filter, Boolean approved, boolean includeAdditionalIndexedProperties) throws Exception {
        final int metadataId = canDownload(context, metadataUuid, visibility, approved);
        final String path = getMetadataDir(metadataId) + "/" + visibility.toString() + "/";

        List<MetadataResource> resourceList = new ArrayList<>();
        if (filter == null) {
            filter = FilesystemStore.DEFAULT_FILTER;
        }
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + filter);

        // A missing folder lists as empty, so any exception here is a real storage error and is not swallowed
        Operator op = openDALConfiguration.getOperator();
        List<Entry> entries = op.list(path);
        for (Entry entry : entries) {
            String entryPath = entry.getPath();
            if (entryPath.endsWith("/")) {
                continue;
            }
            String filename = getFilenameFromPath(entryPath);
            Path fileNamePath = Paths.get(filename).getFileName();

            if (matcher.matches(fileNamePath)) {
                Metadata metadata = op.stat(entryPath);
                resourceList.add(createResourceDescription(metadataUuid, visibility, filename, metadata.getContentLength(),
                        new Date(metadata.getLastModified().toEpochMilli()), metadataId, approved));
            }
        }

        resourceList.sort(MetadataResourceVisibility.sortByFileName);
        return resourceList;
    }

    private String getFilenameFromPath(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1) {
            return path.substring(lastSlash + 1);
        }
        return path;
    }

    private MetadataResource createResourceDescription(final String metadataUuid,
                                                       final MetadataResourceVisibility visibility, final String resourceId, long size, Date lastModification, int metadataId, boolean approved) {
        return new FilesystemStoreResource(metadataUuid, metadataId, getFilename(metadataUuid, resourceId),
                settingManager.getNodeURL() + "api/records/", visibility, size, lastModification, approved);
    }

    @Override
    public ResourceHolder getResource(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, String resourceId, Boolean approved) throws Exception {
        int metadataId = canDownload(context, metadataUuid, visibility, approved);
        String path = getPath(metadataUuid, metadataId, visibility, resourceId);

        return openResource(path, metadataUuid, visibility, resourceId, metadataId, approved, null);
    }

    /**
     * Open a stream on the resource, or on the given range of it, without loading it into memory.
     */
    private ResourceHolder openResource(String path, String metadataUuid, MetadataResourceVisibility visibility, String resourceId,
                                        int metadataId, Boolean approved, ReadOptions readOptions) throws Exception {
        Operator op = openDALConfiguration.getOperator();
        // Stat first: reading a missing file fails with code Unexpected instead of NotFound
        Metadata metadata = stat(path, metadataUuid, resourceId);
        InputStream is = readOptions == null ? op.createInputStream(path) : op.createInputStream(path, readOptions);
        return new OpenDALResourceHolder(is, createResourceDescription(metadataUuid, visibility, resourceId,
                metadata.getContentLength(), new Date(metadata.getLastModified().toEpochMilli()), metadataId, approved));
    }

    /**
     * Stat the resource, turning only a missing file into a ResourceNotFoundException. Other storage errors are rethrown.
     */
    private Metadata stat(String path, String metadataUuid, String resourceId) throws ResourceNotFoundException {
        try {
            return openDALConfiguration.getOperator().stat(path);
        } catch (OpenDALException e) {
            if (e.getCode() == OpenDALException.Code.NotFound) {
                throw new ResourceNotFoundException(
                    String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                    .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                    .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
            }
            throw e;
        }
    }

    @Override
    public MetadataResource getResourceMetadata(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, String resourceId, Boolean approved) throws Exception {
        int metadataId = canDownload(context, metadataUuid, visibility, approved);
        String path = getPath(metadataUuid, metadataId, visibility, resourceId);

        Metadata metadata = stat(path, metadataUuid, resourceId);
        return createResourceDescription(metadataUuid, visibility, resourceId,
                metadata.getContentLength(), new Date(metadata.getLastModified().toEpochMilli()), metadataId, approved);
    }

    @Override
    public String getResourcePresignedUrl(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, String resourceId, Boolean approved) throws Exception {
        int metadataId = canDownload(context, metadataUuid, visibility, approved);
        return openDALConfiguration.presignRead(getPath(metadataUuid, metadataId, visibility, resourceId));
    }

    @Override
    public ResourceHolder getResourceWithRange(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, String resourceId, Boolean approved, long start, long end) throws Exception {
        int metadataId = canDownload(context, metadataUuid, visibility, approved);
        String path = getPath(metadataUuid, metadataId, visibility, resourceId);

        // The metadata keeps the full object length, like S3Store, not the length of the range
        return openResource(path, metadataUuid, visibility, resourceId, metadataId, approved,
                ReadOptions.builder().offset(start).length(end - start + 1).build());
    }

    @Override
    public MetadataResource putResource(ServiceContext context, String metadataUuid, String filename, InputStream is, Date changeDate, MetadataResourceVisibility visibility, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        String path = getPath(metadataUuid, metadataId, visibility, filename);

        Operator op = openDALConfiguration.getOperator();
        try (OutputStream os = op.createOutputStream(path, UPLOAD_OPTIONS)) {
            IOUtils.copy(is, os);
        }

        Metadata metadata = op.stat(path);
        return createResourceDescription(metadataUuid, visibility, filename,
                metadata.getContentLength(), new Date(metadata.getLastModified().toEpochMilli()), metadataId, approved);
    }

    @Override
    public MetadataResource patchResourceStatus(ServiceContext context, String metadataUuid, String resourceId, MetadataResourceVisibility visibility, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        MetadataResource resource = getResourceMetadata(context, metadataUuid, resourceId, approved);

        if (resource.getVisibility() != visibility) {
            String oldPath = getPath(metadataUuid, metadataId, resource.getVisibility(), resourceId);
            String newPath = getPath(metadataUuid, metadataId, visibility, resourceId);

            // Server-side copy, the data doesn't go through the JVM
            Operator op = openDALConfiguration.getOperator();
            op.copy(oldPath, newPath);
            op.delete(oldPath);

            Metadata metadata = op.stat(newPath);
            return createResourceDescription(metadataUuid, visibility, resourceId,
                    metadata.getContentLength(), new Date(metadata.getLastModified().toEpochMilli()), metadataId, approved);
        }
        return resource;
    }

    @Override
    public String delResources(ServiceContext context, int metadataId) throws Exception {
        String path = getMetadataDir(metadataId) + "/";
        openDALConfiguration.getOperator().removeAll(path);
        return "Resources deleted";
    }

    @Override
    public String delResource(ServiceContext context, String metadataUuid, String resourceId, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        for (MetadataResourceVisibility visibility : MetadataResourceVisibility.values()) {
            if (tryDelResource(getPath(metadataUuid, metadataId, visibility, resourceId))) {
                return String.format("Metadata resource '%s' removed.", resourceId);
            }
        }
        return String.format("Unable to remove resource '%s'.", resourceId);
    }

    @Override
    public String delResource(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, String resourceId, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        if (tryDelResource(getPath(metadataUuid, metadataId, visibility, resourceId))) {
            return String.format("Metadata resource '%s' removed.", resourceId);
        }
        return String.format("Unable to remove resource '%s'.", resourceId);
    }

    /**
     * Delete the file if it exists. OpenDAL's delete succeeds silently on a missing file, so check first
     * to report it, like JCloudStore. Storage errors are rethrown.
     */
    private boolean tryDelResource(String path) {
        Operator op = openDALConfiguration.getOperator();
        try {
            op.stat(path);
        } catch (OpenDALException e) {
            if (e.getCode() == OpenDALException.Code.NotFound) {
                return false;
            }
            throw e;
        }
        op.delete(path);
        return true;
    }

    @Override
    public ResourceHolder getResourceInternal(String metadataUuid, MetadataResourceVisibility visibility, String resourceId, Boolean approved) throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        checkResourceId(resourceId);

        String path = getPath(metadataUuid, metadataId, visibility, resourceId);
        return openResource(path, metadataUuid, visibility, resourceId, metadataId, approved, null);
    }

    @Override
    public MetadataResource getResourceDescription(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, String filename, Boolean approved) throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        String path = getPath(metadataUuid, metadataId, visibility, filename);
        Metadata metadata = stat(path, metadataUuid, filename);
        return createResourceDescription(metadataUuid, visibility, filename,
                metadata.getContentLength(), new Date(metadata.getLastModified().toEpochMilli()), metadataId, approved);
    }

    @Override
    public MetadataResourceContainer getResourceContainerDescription(ServiceContext context, String metadataUuid, Boolean approved) throws Exception {
        return new FilesystemStoreResourceContainer(metadataUuid, getAndCheckMetadataId(metadataUuid, approved),
                "attachments", settingManager.getNodeURL() + "api/records/", approved);
    }

    protected String getPath(String metadataUuid, int metadataId, MetadataResourceVisibility visibility, String resourceId) {
        checkResourceId(resourceId);
        return getMetadataDir(metadataId) + "/" + visibility.toString() + "/" + getFilename(metadataUuid, resourceId);
    }

    protected String getMetadataDir(int metadataId) {
        Path metadataFullDir = Lib.resource.getMetadataDir(dataDirectory, metadataId);
        Path systemDir = dataDirectory.getSystemDataDir();
        Path metadataDir;
        // Same key layout as JCloudStore: relative to the system dir, else to the filesystem root.
        if (metadataFullDir.startsWith(systemDir)) {
            metadataDir = systemDir.relativize(metadataFullDir);
        } else if (metadataFullDir.isAbsolute()) {
            metadataDir = metadataFullDir.getRoot().relativize(metadataFullDir);
        } else {
            metadataDir = metadataFullDir;
        }
        // On Windows the separator is "\", object store keys need "/"
        return metadataDir.toString().replace(metadataDir.getFileSystem().getSeparator(), "/");
    }

    public static class OpenDALResourceHolder implements ResourceHolder {
        private final InputStream inputStream;
        private final InputStreamResource resource;
        private final MetadataResource metadata;

        public OpenDALResourceHolder(InputStream inputStream, MetadataResource metadata) {
            this.inputStream = inputStream;
            this.resource = new InputStreamResource(inputStream);
            this.metadata = metadata;
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
