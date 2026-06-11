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
import org.apache.opendal.Entry;
import org.apache.opendal.Metadata;
import org.apache.opendal.Operator;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceContainer;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.resources.OpenDALConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OpenDALStore extends AbstractStore {

    @Autowired
    private OpenDALConfiguration openDALConfiguration;

    @Autowired
    private SettingManager settingManager;

    @Override
    public List<MetadataResource> getResources(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, String filter, Boolean approved) throws Exception {
        final int metadataId = canEdit(context, metadataUuid, approved);
        final String path = getMetadataDir(metadataId) + "/" + visibility.toString() + "/";

        List<MetadataResource> resourceList = new ArrayList<>();
        if (filter == null) {
            filter = FilesystemStore.DEFAULT_FILTER;
        }
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + filter);

        Operator op = openDALConfiguration.getOperator();
        try {
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
        } catch (Exception e) {
            // If path doesn't exist, OpenDAL might throw exception depending on service
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

        Operator op = openDALConfiguration.getOperator();
        try {
            Metadata metadata = op.stat(path);
            byte[] data = op.read(path);
            return new OpenDALResourceHolder(data, createResourceDescription(metadataUuid, visibility, resourceId,
                    metadata.getContentLength(), new Date(metadata.getLastModified().toEpochMilli()), metadataId, approved));
        } catch (Exception e) {
            throw new ResourceNotFoundException("Resource " + resourceId + " not found in OpenDAL store.");
        }
    }

    @Override
    public MetadataResource getResourceMetadata(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, String resourceId, Boolean approved) throws Exception {
        int metadataId = canDownload(context, metadataUuid, visibility, approved);
        String path = getPath(metadataUuid, metadataId, visibility, resourceId);

        Operator op = openDALConfiguration.getOperator();
        try {
            Metadata metadata = op.stat(path);
            return createResourceDescription(metadataUuid, visibility, resourceId,
                    metadata.getContentLength(), new Date(metadata.getLastModified().toEpochMilli()), metadataId, approved);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Resource " + resourceId + " not found in OpenDAL store.");
        }
    }

    @Override
    public ResourceHolder getResourceWithRange(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, String resourceId, Boolean approved, long start, long end) throws Exception {
        int metadataId = canDownload(context, metadataUuid, visibility, approved);
        String path = getPath(metadataUuid, metadataId, visibility, resourceId);

        Operator op = openDALConfiguration.getOperator();
        try {
            Metadata metadata = op.stat(path);
            // OpenDAL read with range is possible, but the Java binding API might vary.
            // Standard read(path) reads all. For range we might need to use op.read(path).range(start, end) if available.
            // As of 0.46.4, op.read(path) returns byte[].
            // In OpenDAL Java, range read might be done via Operator.reader(path) but let's check if it exists.
            // For now, let's do a simple implementation reading all and subsetting if needed,
            // or better, if the API supports it.
            byte[] data = op.read(path); // Simplification
            int length = (int) (end - start + 1);
            byte[] rangeData = new byte[length];
            System.arraycopy(data, (int) start, rangeData, 0, length);

            return new OpenDALResourceHolder(rangeData, createResourceDescription(metadataUuid, visibility, resourceId,
                    metadata.getContentLength(), new Date(metadata.getLastModified().toEpochMilli()), metadataId, approved));
        } catch (Exception e) {
            throw new ResourceNotFoundException("Resource " + resourceId + " not found in OpenDAL store.");
        }
    }

    @Override
    public MetadataResource putResource(ServiceContext context, String metadataUuid, String filename, InputStream is, Date changeDate, MetadataResourceVisibility visibility, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, visibility, approved);
        String path = getPath(metadataUuid, metadataId, visibility, filename);

        Operator op = openDALConfiguration.getOperator();
        byte[] data = org.apache.commons.io.IOUtils.toByteArray(is);
        op.write(path, data);

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

            Operator op = openDALConfiguration.getOperator();
            byte[] data = op.read(oldPath);
            op.write(newPath, data);
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
        Operator op = openDALConfiguration.getOperator();
        deleteRecursive(op, path);
        return "Resources deleted";
    }

    private void deleteRecursive(Operator op, String path) throws Exception {
        List<Entry> entries = op.list(path);
        for (Entry entry : entries) {
            if (entry.getPath().endsWith("/")) {
                deleteRecursive(op, entry.getPath());
            } else {
                op.delete(entry.getPath());
            }
        }
        op.delete(path);
    }

    @Override
    public String delResource(ServiceContext context, String metadataUuid, String resourceId, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        try {
            delResource(context, metadataUuid, MetadataResourceVisibility.PUBLIC, resourceId, approved);
        } catch (Exception e) {
            // ignore
        }
        try {
            delResource(context, metadataUuid, MetadataResourceVisibility.PRIVATE, resourceId, approved);
        } catch (Exception e) {
            // ignore
        }
        return "Resource deleted";
    }

    @Override
    public String delResource(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, String resourceId, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        String path = getPath(metadataUuid, metadataId, visibility, resourceId);
        openDALConfiguration.getOperator().delete(path);
        return "Resource deleted";
    }

    @Override
    public ResourceHolder getResourceInternal(String metadataUuid, MetadataResourceVisibility visibility, String resourceId, Boolean approved) throws Exception {
        return getResource(null, metadataUuid, visibility, resourceId, approved);
    }

    @Override
    public MetadataResource getResourceDescription(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, String filename, Boolean approved) throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        String path = getPath(metadataUuid, metadataId, visibility, filename);
        Operator op = openDALConfiguration.getOperator();
        Metadata metadata = op.stat(path);
        return createResourceDescription(metadataUuid, visibility, filename,
                metadata.getContentLength(), new Date(metadata.getLastModified().toEpochMilli()), metadataId, approved);
    }

    @Override
    public MetadataResourceContainer getResourceContainerDescription(ServiceContext context, String metadataUuid, Boolean approved) throws Exception {
        return new FilesystemStoreResourceContainer(metadataUuid, getAndCheckMetadataId(metadataUuid, approved),
                "attachments", settingManager.getNodeURL() + "api/records/", approved);
    }

    protected String getPath(String metadataUuid, int metadataId, MetadataResourceVisibility visibility, String resourceId) {
        return getMetadataDir(metadataId) + "/" + visibility.toString() + "/" + getFilename(metadataUuid, resourceId);
    }

    protected String getMetadataDir(int metadataId) {
        return "metadata/" + metadataId;
    }

    public static class OpenDALResourceHolder implements ResourceHolder {
        private final byte[] data;
        private final MetadataResource metadata;

        public OpenDALResourceHolder(byte[] data, MetadataResource metadata) {
            this.data = data;
            this.metadata = metadata;
        }

        @Override
        public Resource getResource() {
            return new InputStreamResource(new ByteArrayInputStream(data));
        }

        @Override
        public MetadataResource getMetadata() {
            return metadata;
        }

        @Override
        public void close() throws IOException {
            // Nothing to do
        }
    }
}
