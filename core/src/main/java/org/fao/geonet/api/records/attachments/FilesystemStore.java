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
import org.fao.geonet.api.exception.ResourceAlreadyExistException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * A FileSystemStore store resources files in the catalog data directory. Each metadata record as a directory in the data directory
 * containing a public and a private folder.
 *
 * <pre>
 *     datadir
 *      |-{{sequence_folder}}
 *      |    |-{{metadata_id}}
 *      |    |    |-private
 *      |    |    |-public
 *      |    |        |--doc.pdf
 * </pre>
 */
public class FilesystemStore extends AbstractStore {
    public static final String DEFAULT_FILTER = "*.*";

    public FilesystemStore() {
    }

    @Override
    public List<MetadataResource> getResources(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility,
                                               String filter, Boolean approved) throws Exception {
        int metadataId = canDownload(context, metadataUuid, visibility, approved);
        SettingManager settingManager = context.getBean(SettingManager.class);

        Path metadataDir = Lib.resource.getMetadataDir(getDataDirectory(context), metadataId);
        Path resourceTypeDir = metadataDir.resolve(visibility.toString());

        List<MetadataResource> resourceList = new ArrayList<>();
        if (filter == null) {
            filter = FilesystemStore.DEFAULT_FILTER;
        }
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(resourceTypeDir, filter)) {
            for (Path path: directoryStream) {
                MetadataResource resource = new FilesystemStoreResource(metadataUuid, metadataId, path.getFileName().toString(),
                                                                        settingManager.getNodeURL() + "api/records/", visibility,
                                                                        Files.size(path),
                                                                        new Date(Files.getLastModifiedTime(path).toMillis()), approved);
                resourceList.add(resource);
            }
        } catch (IOException ignored) {
        }

        resourceList.sort(MetadataResourceVisibility.sortByFileName);

        return resourceList;
    }

    @Override
    public ResourceHolder getResource(final ServiceContext context, final String metadataUuid, final MetadataResourceVisibility visibility,
                                      final String resourceId, Boolean approved) throws Exception {
        int metadataId = canDownload(context, metadataUuid, visibility, approved);
        checkResourceId(resourceId);

        final Path resourceFile = Lib.resource.getDir(visibility.toString(), metadataId).
                resolve(getFilename(metadataUuid, resourceId));

        if (Files.exists(resourceFile)) {
            return new ResourceHolderImpl(resourceFile, getResourceDescription(context, metadataUuid, visibility, resourceFile, approved));
        } else {
            throw new ResourceNotFoundException(
                    String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid));
        }
    }


    @Override
    public ResourceHolder getResourceInternal(
        final String metadataUuid,
        final MetadataResourceVisibility visibility,
        final String resourceId,
        Boolean approved) throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        checkResourceId(resourceId);

        final Path resourceFile = Lib.resource.getDir(visibility.toString(), metadataId).
            resolve(getFilename(metadataUuid, resourceId));

        if (Files.exists(resourceFile)) {
            return new ResourceHolderImpl(resourceFile, null);
        } else {
            throw new ResourceNotFoundException(
                String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid));
        }
    }

    public MetadataResource getResourceDescription(final ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility,
                                                   String filename, Boolean approved) throws Exception {
        Path path = getPath(context, metadataUuid, visibility, filename, approved);
        return getResourceDescription(context, metadataUuid, visibility, path, approved);
    }

    private MetadataResource getResourceDescription(final ServiceContext context, final String metadataUuid,
                                                    final MetadataResourceVisibility visibility, final Path filePath, Boolean approved)
            throws IOException {
        SettingManager settingManager = context.getBean(SettingManager.class);
        Integer metadataId = null;

        try {
            metadataId = getAndCheckMetadataId(metadataUuid, approved);
        } catch (Exception e) {
            Log.error(Geonet.RESOURCES, e.getMessage(), e);
        }

        long fileSize = -1;
        try {
            fileSize = Files.size(filePath);
        } catch (IOException e) {
            Log.error(Geonet.RESOURCES, e.getMessage(), e);
        }
        return new FilesystemStoreResource(metadataUuid, metadataId, filePath.getFileName().toString(), settingManager.getNodeURL() + "api/records/",
                                           visibility, fileSize, new Date(Files.getLastModifiedTime(filePath).toMillis()), approved);
    }

    @Override
    public MetadataResource putResource(final ServiceContext context, final String metadataUuid, final String filename,
                                        final InputStream is, @Nullable final Date changeDate, final MetadataResourceVisibility visibility,
                                        Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        Path filePath = getPath(context, metadataId, visibility, filename, approved);
        Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
        if (changeDate != null) {
            IO.touch(filePath, FileTime.from(changeDate.getTime(), TimeUnit.MILLISECONDS));
        }

        return getResourceDescription(context, metadataUuid, visibility, filePath, approved);
    }

    private Path getPath(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, String fileName,
                         Boolean approved) throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        return getPath(context, metadataId, visibility, fileName, approved);
    }

    private Path getPath(ServiceContext context, int metadataId, MetadataResourceVisibility visibility, String fileName,
                         Boolean approved) throws Exception {
        final Path folderPath = ensureDirectory(context, metadataId, fileName, visibility);
        Path filePath = folderPath.resolve(fileName);
        if (Files.exists(filePath) && !approved) {
            throw new ResourceAlreadyExistException(
                    String.format("A resource with name '%s' and status '%s' already exists for metadata '%d'.", fileName, visibility,
                                  metadataId));
        }
        return filePath;
    }

    @Override
    public String delResources(ServiceContext context, String metadataUuid, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        Path metadataDir = Lib.resource.getMetadataDir(getDataDirectory(context), metadataId);
        try {
            IO.deleteFileOrDirectory(metadataDir, true);
            return String.format("Metadata '%s' directory removed.", metadataId);
        } catch (Exception e) {
            return String.format("Unable to remove metadata '%s' directory.", metadataId);
        }
    }

    @Override
    public String delResource(ServiceContext context, String metadataUuid, String resourceId, Boolean approved) throws Exception {
        canEdit(context, metadataUuid, approved);

        try (ResourceHolder filePath = getResource(context, metadataUuid, resourceId, approved)) {
            Files.deleteIfExists(filePath.getPath());
            return String.format("MetadataResource '%s' removed.", resourceId);
        } catch (IOException e) {
            return String.format("Unable to remove resource '%s'.", resourceId);
        }
    }

    @Override
    public String delResource(final ServiceContext context, final String metadataUuid, final MetadataResourceVisibility visibility,
                              final String resourceId, Boolean approved) throws Exception {
        canEdit(context, metadataUuid, approved);

        try (ResourceHolder filePath = getResource(context, metadataUuid, visibility, resourceId, approved)) {
            Files.deleteIfExists(filePath.getPath());
            return String.format("MetadataResource '%s' removed.", resourceId);
        } catch (IOException e) {
            return String.format("Unable to remove resource '%s'.", resourceId);
        }
    }

    @Override
    public MetadataResource patchResourceStatus(ServiceContext context, String metadataUuid, String resourceId,

                                                MetadataResourceVisibility visibility, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);

        ResourceHolder filePath = getResource(context, metadataUuid, resourceId, approved);
        if (filePath.getMetadata().getVisibility() == visibility) {
            // already the wanted visibility
            return filePath.getMetadata();
        }
        final Path newFolderPath = ensureDirectory(context, metadataId, resourceId, visibility);
        Path newFilePath = newFolderPath.resolve(filePath.getPath().getFileName());
        Files.move(filePath.getPath(), newFilePath);
        return getResourceDescription(context, metadataUuid, visibility, newFilePath, approved);
    }

    private Path ensureDirectory(final ServiceContext context, final int metadataId, final String resourceId,
                                 final MetadataResourceVisibility visibility) throws IOException {
        final Path metadataDir = Lib.resource.getMetadataDir(getDataDirectory(context), metadataId);
        final Path newFolderPath = metadataDir.resolve(visibility.toString());
        if (!Files.exists(newFolderPath)) {
            try {
                Files.createDirectories(newFolderPath);
            } catch (Exception e) {
                throw new IOException(
                        String.format("Can't create folder '%s' to store resource with name '%s' for metadata '%d'.", visibility,
                                      resourceId, metadataId));
            }
        }
        return newFolderPath;
    }

    private GeonetworkDataDirectory getDataDirectory(ServiceContext context) {
        return context.getBean(GeonetworkDataDirectory.class);
    }

    private static class ResourceHolderImpl implements ResourceHolder {
        private final Path path;
        private MetadataResource metadata;

        private ResourceHolderImpl(Path path, final MetadataResource metadata) {
            this.path = path;
            this.metadata = metadata;
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
        public void close() {
            // nothing to do
        }
    }
}
