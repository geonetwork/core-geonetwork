/*
 * =============================================================================
 * ===	Copyright (C) 2001-2022 Food and Agriculture Organization of the
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
import org.fao.geonet.api.exception.InputStreamLimitExceededException;
import org.fao.geonet.api.exception.ResourceAlreadyExistException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceContainer;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardCopyOption;
import java.nio.file.FileSystem;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * A FileSystemStore store resources files in the catalog data directory. Each metadata record has
 * a directory in the data directory; visibility (public/private) is tracked in the database
 * ({@code MetadataFileUploads.resourceaccess}, populated by {@link ResourceLoggerStore}), not by
 * which folder a file is in - the {@code public}/{@code private} subfolders below are the legacy
 * layout, kept only as a read/write fallback for files that predate this and haven't been
 * touched since (every put, rename, or visibility change migrates a file to the flat layout).
 *
 * <pre>
 *     datadir
 *      |-{{sequence_folder}}
 *      |    |-{{metadata_id}}
 *      |    |    |--doc.pdf
 *      |    |    |-private        (legacy, pre-existing files only)
 *      |    |    |-public          (legacy, pre-existing files only)
 *      |    |        |--doc.pdf
 * </pre>
 */
public class FilesystemStore extends AbstractStore {
    public static final String DEFAULT_FILTER = "*.*";

    @Autowired
    SettingManager settingManager;

    public FilesystemStore() {
    }

    @Override
    public List<MetadataResource> getResources(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility,
                                               String filter, Boolean approved, boolean includeAdditionalIndexedProperties) throws Exception {
        int metadataId = canDownload(context, metadataUuid, visibility, approved);

        Path metadataDir = Lib.resource.getMetadataDir(getDataDirectory(context), metadataId);

        List<MetadataResource> resourceList = new ArrayList<>();
        if (filter == null) {
            filter = FilesystemStore.DEFAULT_FILTER;
        }
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + filter);

        // Legacy layout: files still sitting in the old <visibility>/ subfolder (not yet
        // touched since the flat layout was introduced) are that visibility by construction.
        Path legacyDir = metadataDir.resolve(visibility.toString());
        try (Stream<Path> paths = Files.walk(legacyDir)) {
            for (Path path : (Iterable<Path>) paths.filter(Files::isRegularFile)::iterator) {
                if (!matcher.matches(path.getFileName())) {
                    continue;
                }
                String relativeFilename = IO.toUnixStylePath(legacyDir.relativize(path));
                resourceList.add(new FilesystemStoreResource(metadataUuid, metadataId, relativeFilename,
                                                             settingManager.getNodeURL() + "api/records/", visibility,
                                                             Files.size(path),
                                                             new Date(Files.getLastModifiedTime(path).toMillis()), null, null,
                                                             approved, MimeTypeDetector.detect(path, path.getFileName().toString())));
            }
        } catch (IOException ignored) {
        }

        // Flat layout: everything else directly under the metadata folder, excluding the legacy
        // public/private subfolders (already listed above) - filtered to files whose tracked
        // access matches the requested visibility, since location alone no longer implies it.
        Map<String, MetadataResourceVisibility> trackedAccess = loadTrackedAccessByFilename(metadataId);
        try (Stream<Path> paths = Files.walk(metadataDir)) {
            for (Path path : (Iterable<Path>) paths.filter(Files::isRegularFile)::iterator) {
                String relativeFilename = IO.toUnixStylePath(metadataDir.relativize(path));
                if (isUnderLegacyVisibilityFolder(relativeFilename) || !matcher.matches(path.getFileName())) {
                    continue;
                }
                if (trackedAccess.get(relativeFilename) != visibility) {
                    continue;
                }
                resourceList.add(new FilesystemStoreResource(metadataUuid, metadataId, relativeFilename,
                                                             settingManager.getNodeURL() + "api/records/", visibility,
                                                             Files.size(path),
                                                             new Date(Files.getLastModifiedTime(path).toMillis()), null, null,
                                                             approved, MimeTypeDetector.detect(path, path.getFileName().toString())));
            }
        } catch (IOException ignored) {
        }

        resourceList.sort(MetadataResourceVisibility.sortByFileName);

        return resourceList;
    }

    /**
     * Whether a flat-tree-relative filename falls under one of the legacy {@code public}/
     * {@code private} subfolders (already covered by the legacy-layout listing in
     * {@link #getResources}, so must be skipped when walking the flat tree to avoid double
     * counting). A nested-path resource whose own first segment happens to be literally
     * "public" or "private" is indistinguishable from this and will also be skipped here - a
     * narrow, pre-existing ambiguity of keeping both layouts side by side.
     */
    private static boolean isUnderLegacyVisibilityFolder(String relativeFilename) {
        for (MetadataResourceVisibility v : MetadataResourceVisibility.values()) {
            if (relativeFilename.equals(v.toString()) || relativeFilename.startsWith(v.toString() + "/")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ResourceHolder getResource(final ServiceContext context, final String metadataUuid, final MetadataResourceVisibility visibility,
                                      final String resourceId, Boolean approved) throws Exception {
        int metadataId = canDownload(context, metadataUuid, visibility, approved);
        checkResourceId(resourceId);

        String filename = getFilename(metadataUuid, resourceId);
        final Path resourceFile = resolveExistingPath(metadataUuid, metadataId, visibility, filename, approved);

        if (resourceFile != null) {
            return new FilesystemResourceHolder(resourceFile, getResourceDescription(context, metadataUuid, visibility, filename, resourceFile, approved));
        } else {
            throw new ResourceNotFoundException(
                String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                .withMessageKey("exception.resourceNotFound.resource", new String[]{ resourceId })
                .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{ resourceId, metadataUuid });
        }
    }

    @Override
    public MetadataResource getResourceMetadata(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, String resourceId, Boolean approved) throws Exception {
        int metadataId = canDownload(context, metadataUuid, visibility, approved);
        checkResourceId(resourceId);

        String filename = getFilename(metadataUuid, resourceId);
        final Path resourceFile = resolveExistingPath(metadataUuid, metadataId, visibility, filename, approved);

        if (resourceFile != null) {
            return getResourceDescription(context, metadataUuid, visibility, filename, resourceFile, approved);
        } else {
            throw new ResourceNotFoundException(
                String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                .withMessageKey("exception.resourceNotFound.resource", new String[]{ resourceId })
                .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{ resourceId, metadataUuid });
        }
    }

    @Override
    public ResourceHolder getResourceWithRange(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, String resourceId, Boolean approved, long start, long end) throws Exception {
        // For filesystem store the range is handled by spring so we just return the resource
        return getResource(context, metadataUuid, visibility, resourceId, approved);
    }


    @Override
    public ResourceHolder getResourceInternal(
        final String metadataUuid,
        final MetadataResourceVisibility visibility,
        final String resourceId,
        Boolean approved) throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        checkResourceId(resourceId);

        final Path resourceFile = resolveExistingPath(metadataUuid, metadataId, visibility, getFilename(metadataUuid, resourceId), approved);

        if (resourceFile != null) {
            return new FilesystemResourceHolder(resourceFile, null);
        } else {
            throw new ResourceNotFoundException(
                String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid));
        }
    }

    public MetadataResource getResourceDescription(final ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility,
                                                   String filename, Boolean approved) throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        Path path = resolveExistingPath(metadataUuid, metadataId, visibility, filename, approved);
        if (path == null) {
            return null;
        }
        return getResourceDescription(context, metadataUuid, visibility, filename, path, approved);
    }

    /**
     * Resolve the on-disk path of an existing resource. Prefers the flat, visibility-less
     * layout (see the class Javadoc) and falls back to the legacy {@code <visibility>/}
     * subfolder for files that predate it and haven't been touched since.
     * <p>
     * Once flat, a file's location no longer enforces which visibility it may be fetched as -
     * that was the folder split's job. So a flat match is only honoured if its <em>tracked</em>
     * access agrees with {@code visibility}; a mismatch (or an untracked flat file, which
     * shouldn't happen since every write path logs a tracking row) is treated as not found at
     * this visibility, exactly as an actually-private file can't be read today by asking for
     * the public one.
     * <p>
     * Takes no {@link ServiceContext} since {@link #getResourceInternal} - one of this method's
     * callers - doesn't have one to give; the data directory is resolved the same context-free
     * way {@code ResourceLib.getRemovedDir} already does.
     *
     * @return the resolved path, or {@code null} if not found at all, or not at this visibility.
     */
    private Path resolveExistingPath(String metadataUuid, int metadataId, MetadataResourceVisibility visibility, String filename,
                                     Boolean approved) {
        Path metadataDir = Lib.resource.getMetadataDir(ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class), metadataId);
        Path flatPath = metadataDir.resolve(filename);
        if (Files.exists(flatPath)) {
            return visibility == resolveVisibility(metadataUuid, approved, filename) ? flatPath : null;
        }
        Path legacyPath = Lib.resource.getDir(visibility.toString(), metadataId).resolve(filename);
        return Files.exists(legacyPath) ? legacyPath : null;
    }

    /**
     * Get the resource description or null if the file doesn't exist.
     * @param context the service context.
     * @param metadataUuid the uuid of the owner metadata record.
     * @param visibility is the resource is public or not.
     * @param filename the resource's filename (may include nested-path subfolder segments),
     *                 already resolved relative to {@code filePath} - not re-derived from
     *                 {@code filePath} since {@link Path#getFileName()} would only give the last segment.
     * @param filePath the path to the resource.
     * @param approved if the metadata draft has been approved or not
     * @return the resource description or {@code null} if there is any problem accessing the file.
     */
    private MetadataResource getResourceDescription(final ServiceContext context, final String metadataUuid,
                                                    final MetadataResourceVisibility visibility, final String filename,
                                                    final Path filePath, Boolean approved) {
        FilesystemStoreResource result = null;

        try {
            int metadataId = getAndCheckMetadataId(metadataUuid, approved);
            long fileSize = Files.size(filePath);
            result = new FilesystemStoreResource(metadataUuid, metadataId, filename,
                settingManager.getNodeURL() + "api/records/", visibility, fileSize,
                new Date(Files.getLastModifiedTime(filePath).toMillis()), null, null, approved,
                MimeTypeDetector.detect(filePath, filePath.getFileName().toString()));
        } catch (IOException e) {
            Log.error(Geonet.RESOURCES, "Error getting size of file " + filePath + ": "
                + e.getMessage(), e);
        } catch (Exception e) {
            Log.error(Geonet.RESOURCES, "Error in getResourceDescription: "
                + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public MetadataResourceContainer getResourceContainerDescription(ServiceContext context, String metadataUuid, Boolean approved) throws Exception {

        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        final Path metadataDir = Lib.resource.getMetadataDir(getDataDirectory(context), metadataId);
        if (!Files.exists(metadataDir)) {
            try {
                Files.createDirectories(metadataDir);
            } catch (Exception e) {
                throw new IOException(
                    String.format("Can't create folder '%s' for metadata '%d'.", metadataDir, metadataId));
            }
        }

        return new FilesystemStoreResourceContainer(metadataUuid, metadataId, metadataUuid, settingManager.getNodeURL() + "api/records/", approved);
    }

    @Override
    public MetadataResource renameResource(ServiceContext context, String metadataUuid, String resourceId, String newName, Boolean approved) throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        checkResourceId(newName);
        try (ResourceHolder resourceHolder = getResource(context, metadataUuid, resourceId, approved)) {
            MetadataResourceVisibility visibility = resourceHolder.getMetadata().getVisibility();
            Path currentFilePath = getResourcePath(resourceHolder.getResource(), context);
            Path newFilePath = getPath(context, metadataId, visibility, newName, approved);
            if (Files.exists(newFilePath)) {
                throw new ResourceAlreadyExistException(
                    String.format("A resource with name '%s' and status '%s' already exists for metadata '%d'.", newName, visibility, metadataId));
            }
            Files.move(currentFilePath, newFilePath);
            return getResourceDescription(context, metadataUuid, visibility, newName, newFilePath, approved);
        } catch (IOException e) {
            throw new IOException(
                String.format("Unable to rename resource '%s' for metadata %d (%s). %s", resourceId, metadataId, metadataUuid, e.getMessage()), e);
        }
    }


    @Override
    public MetadataResource putResource(final ServiceContext context, final String metadataUuid, final String filename,
                                        final InputStream is, @Nullable final Date changeDate, final MetadataResourceVisibility visibility,
                                        Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        checkResourceId(filename);
        Path filePath = getPath(context, metadataId, visibility, filename, approved);
        try {
            Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (InputStreamLimitExceededException e) {
            Files.deleteIfExists(filePath);
            throw e;
        }
        if (changeDate != null) {
            IO.touch(filePath, FileTime.from(changeDate.getTime(), TimeUnit.MILLISECONDS));
        }

        return getResourceDescription(context, metadataUuid, visibility, filename, filePath, approved);
    }

    private Path getPath(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, String fileName,
                         Boolean approved) throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        return getPath(context, metadataId, visibility, fileName, approved);
    }

    private Path getPath(ServiceContext context, int metadataId, MetadataResourceVisibility visibility, String fileName,
                         Boolean approved) throws Exception {
        final Path folderPath = ensureDirectory(context, metadataId, fileName);
        Path filePath = folderPath.resolve(fileName);
        // A same-named legacy file for this visibility (not yet migrated to the flat layout)
        // counts as "already exists" too, matching today's per-visibility uniqueness.
        boolean alreadyExists = Files.exists(filePath)
            || Files.exists(Lib.resource.getDir(visibility.toString(), metadataId).resolve(fileName));
        if (alreadyExists && !approved) {
            throw new ResourceAlreadyExistException(
                    String.format("A resource with name '%s' and status '%s' already exists for metadata '%d'.", fileName, visibility,
                                  metadataId));
        }
        return filePath;
    }

    @Override
    public String delResources(ServiceContext context, int metadataId) throws Exception {
        Path metadataDir = Lib.resource.getMetadataDir(getDataDirectory(context), metadataId);
        try {
            Log.info(Geonet.RESOURCES, String.format("Deleting all files from metadataId '%d'", metadataId));
            IO.deleteFileOrDirectory(metadataDir, true);
            Log.info(Geonet.RESOURCES,
                String.format("Metadata '%d' directory removed.", metadataId));
            return String.format("Metadata '%d' directory removed.", metadataId);
        } catch (Exception e) {
            return String.format("Unable to remove metadata '%d' directory.", metadataId);
        }
    }

    @Override
    public String delResource(ServiceContext context, String metadataUuid, String resourceId, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);

        try (ResourceHolder resourceHolder = getResource(context, metadataUuid, resourceId, approved)) {
            Files.deleteIfExists(getResourcePath(resourceHolder.getResource(), context));
            Log.info(Geonet.RESOURCES,
                String.format("Resource '%s' removed for metadata %d (%s).", resourceId, metadataId, metadataUuid));
            return String.format("Metadata resource '%s' removed.", resourceId);
        } catch (IOException e) {
            Log.warning(Geonet.RESOURCES,
                String.format("Unable to remove resource '%s' for metadata %d (%s). %s", resourceId, metadataId, metadataUuid, e.getMessage()));
            return String.format("Unable to remove resource '%s'.", resourceId);
        }
    }

    @Override
    public String delResource(final ServiceContext context, final String metadataUuid, final MetadataResourceVisibility visibility,
                              final String resourceId, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);

        try (ResourceHolder resourceHolder = getResource(context, metadataUuid, visibility, resourceId, approved)) {
            Files.deleteIfExists(getResourcePath(resourceHolder.getResource(), context));
            Log.info(Geonet.RESOURCES,
                String.format("Resource '%s' removed for metadata %d (%s).", resourceId, metadataId, metadataUuid));
            return String.format("Metadata resource '%s' removed.", resourceId);
        } catch (IOException e) {
            Log.warning(Geonet.RESOURCES,
                String.format("Unable to remove resource '%s' for metadata %d (%s). %s", resourceId, metadataId, metadataUuid, e.getMessage()));
            return String.format("Unable to remove resource '%s'.", resourceId);
        }
    }

    @Override
    public MetadataResource patchResourceStatus(ServiceContext context, String metadataUuid, String resourceId,

                                                MetadataResourceVisibility visibility, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);

        ResourceHolder resourceHolder = getResource(context, metadataUuid, resourceId, approved);
        if (resourceHolder.getMetadata().getVisibility() == visibility) {
            // already the wanted visibility
            return resourceHolder.getMetadata();
        }
        String filename = resourceHolder.getMetadata().getFilename();
        final Path newFolderPath = ensureDirectory(context, metadataId, filename);
        Path newFilePath = newFolderPath.resolve(filename);
        Path currentFilePath = getResourcePath(resourceHolder.getResource(), context);
        // A resource already in the flat layout doesn't need to move at all - visibility is
        // purely a database attribute there (updated by ResourceLoggerStore regardless of what
        // happens here). A legacy resource (still in its old <visibility>/ subfolder) is
        // migrated to the flat layout as a side effect of this move, same as put/rename.
        if (!currentFilePath.equals(newFilePath)) {
            Files.move(currentFilePath, newFilePath);
        }
        return getResourceDescription(context, metadataUuid, visibility, filename, newFilePath, approved);
    }

    private Path ensureDirectory(final ServiceContext context, final int metadataId, final String resourceId) throws IOException {
        final Path metadataDir = Lib.resource.getMetadataDir(getDataDirectory(context), metadataId);
        // resourceId may contain subfolder segments (nested paths); make sure their parent
        // directories exist too, not just the metadata folder itself.
        final Path targetParent = metadataDir.resolve(resourceId).getParent();
        if (targetParent != null && !Files.exists(targetParent)) {
            try {
                Files.createDirectories(targetParent);
            } catch (Exception e) {
                throw new IOException(
                        String.format("Can't create folder to store resource with name '%s' for metadata '%d'.",
                                      resourceId, metadataId));
            }
        }
        return metadataDir;
    }

    private static GeonetworkDataDirectory getDataDirectory(ServiceContext context) {
        return context.getBean(GeonetworkDataDirectory.class);
    }

    /**
     * Retrieves the file system path of a given resource.
     * This method is required for unit tests which use an in-memory filesystem.
     *
     * @param resource The resource to retrieve the path for. Must be of type `PathResource`.
     * @param context The service context, used to access the data directory.
     * @return The `Path` object representing the file system path of the resource.
     * @throws IOException If an I/O error occurs while retrieving the path.
     * @throws IllegalArgumentException If the provided resource is not of type `PathResource`.
     */
    public static Path getResourcePath(Resource resource, ServiceContext context) throws IOException {
        if (!(resource instanceof PathResource)) {
            // This should never happen
            throw new IllegalArgumentException(
                "The resource should be of type PathResource. It is of type " + resource.getClass().getName());
        }

        PathResource pathResource = (PathResource) resource;
        FileSystem fileSystem = getDataDirectory(context).getMetadataDataDir().getFileSystem();
        return  fileSystem.getPath(pathResource.getPath());
    }

    private static class FilesystemResourceHolder implements ResourceHolder {
        private final PathResource resource;
        private final MetadataResource metadata;

        private FilesystemResourceHolder(Path path, final MetadataResource metadata) {
            this.resource = new PathResource(path);
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
        public void close() {
            // nothing to do
        }
    }
}
