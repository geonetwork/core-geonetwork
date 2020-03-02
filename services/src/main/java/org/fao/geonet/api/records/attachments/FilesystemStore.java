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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.exception.ResourceAlreadyExistException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.IO;
import org.springframework.context.ApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jeeves.server.context.ServiceContext;

/**
 * A FileSystemStore store resources files in the catalog data directory. Each metadata record as a
 * directory in the data directory containing a public and a private folder.
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
public class FilesystemStore implements Store {
    public static final String DEFAULT_FILTER = "*.*";

    public FilesystemStore() {
    }

    @Override
    public List<MetadataResource> getResources(ServiceContext context, String metadataUuid,
                                               Sort sort,
                                               String filter) throws Exception {
        List<MetadataResource> resourceList = new ArrayList<>();
        ApplicationContext _appContext = ApplicationContextHolder.get();
        String metadataId = getAndCheckMetadataId(metadataUuid);
        AccessManager accessManager = _appContext.getBean(AccessManager.class);
        boolean canEdit = accessManager.canEdit(context, metadataId);

        resourceList.addAll(getResources(context, metadataUuid, MetadataResourceVisibility.PUBLIC, filter));
        if (canEdit) {
            resourceList.addAll(getResources(context, metadataUuid, MetadataResourceVisibility.PRIVATE, filter));
        }

        if (sort == Sort.name) {
            Collections.sort(resourceList, MetadataResourceVisibility.sortByFileName);
        }

        return resourceList;
    }


    @Override
    public List<MetadataResource> getResources(ServiceContext context, String metadataUuid,
                                               MetadataResourceVisibility visibility,
                                               String filter) throws Exception {
        ApplicationContext _appContext = ApplicationContextHolder.get();
        String metadataId = getAndCheckMetadataId(metadataUuid);
        GeonetworkDataDirectory dataDirectory =
            _appContext.getBean(GeonetworkDataDirectory.class);
        SettingManager settingManager = _appContext.getBean(SettingManager.class);
        AccessManager accessManager = _appContext.getBean(AccessManager.class);

        boolean canEdit = accessManager.canEdit(context, metadataId);
        if (visibility == MetadataResourceVisibility.PRIVATE && !canEdit) {
            throw new SecurityException(String.format(
                "User does not have privileges to get the list of '%s' resources for metadata '%s'.",
                visibility, metadataUuid));
        }

        Path metadataDir = Lib.resource.getMetadataDir(dataDirectory, metadataId);
        Path resourceTypeDir = metadataDir.resolve(visibility.toString());

        List<MetadataResource> resourceList = new ArrayList<>();
        if (filter == null) {
            filter = FilesystemStore.DEFAULT_FILTER;
        }
        try (DirectoryStream<Path> directoryStream =
                 Files.newDirectoryStream(resourceTypeDir, filter)) {
            for (Path path : directoryStream) {
                MetadataResource resource = new FilesystemStoreResource(
                    metadataUuid + "/attachments/" + path.getFileName(),
                    settingManager.getNodeURL() + "api/records/",
                    visibility,
                    Files.size(path));
                resourceList.add(resource);
            }
        } catch (IOException ignored) {
        }

        Collections.sort(resourceList, MetadataResourceVisibility.sortByFileName);

        return resourceList;
    }


    @Override
    public Path getResource(ServiceContext context, String metadataUuid, String resourceId) throws Exception {
        // Those characters should not be allowed by URL structure
        if (resourceId.contains("..") ||
            resourceId.startsWith("/") ||
            resourceId.startsWith("file:/")) {
            throw new SecurityException(String.format(
                "Invalid resource identifier '%s'.",
                resourceId));
        }
        ApplicationContext _appContext = ApplicationContextHolder.get();
        AccessManager accessManager = _appContext.getBean(AccessManager.class);
        GeonetworkDataDirectory dataDirectory = _appContext.getBean(GeonetworkDataDirectory.class);
        String metadataId = getAndCheckMetadataId(metadataUuid);
        Path metadataDir = Lib.resource.getMetadataDir(dataDirectory, metadataId);

        Path resourceFile = null;

        boolean canDownload = accessManager.canDownload(context, metadataId);
        for (MetadataResourceVisibility r : MetadataResourceVisibility.values()) {
            try (DirectoryStream<Path> directoryStream =
                     Files.newDirectoryStream(metadataDir.resolve(r.toString()),
                         resourceId)) {
                for (Path path : directoryStream) {
                    if (Files.isRegularFile(path)) {
                        resourceFile = path;
                    }
                }
            } catch (IOException ignored) {
            }
        }

        if (resourceFile != null && Files.exists(resourceFile)) {
            if (resourceFile.getParent().getFileName().toString().equals(
                MetadataResourceVisibility.PRIVATE.toString()) && !canDownload) {
                throw new SecurityException(String.format(
                    "Current user can't download resources for metadata '%s' and as such can't access the requested resource '%s'.",
                    metadataUuid, resourceId));
            }
            return resourceFile;
        } else {
            throw new ResourceNotFoundException(String.format(
                "Metadata resource '%s' not found for metadata '%s'",
                resourceId, metadataUuid));
        }
    }


    private MetadataResource getResourceDescription(String metadataUuid, MetadataResourceVisibility visibility, Path filePath) {
        ApplicationContext _appContext = ApplicationContextHolder.get();
        SettingManager settingManager = _appContext.getBean(SettingManager.class);

        double fileSize = Double.NaN;
        try {
            fileSize = Files.size(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new FilesystemStoreResource(
            metadataUuid + "/attachments/" + filePath.getFileName(),
            settingManager.getNodeURL() + "api/records/",
            visibility,
            fileSize);
    }


    @Override
    public MetadataResource putResource(ServiceContext context, String metadataUuid,
                                        MultipartFile file,
                                        MetadataResourceVisibility visibility) throws Exception {
        canEdit(context, metadataUuid);
        Path filePath = getPath(metadataUuid, visibility, file.getOriginalFilename());

        BufferedOutputStream stream =
            new BufferedOutputStream(
                Files.newOutputStream(filePath)
            );
        byte[] bytes = file.getBytes();
        stream.write(bytes);
        stream.close();

        return getResourceDescription(metadataUuid, visibility, filePath);
    }

    @Override
    public MetadataResource putResource(ServiceContext context, String metadataUuid, Path file, MetadataResourceVisibility visibility) throws Exception {
        canEdit(context, metadataUuid);
        Path filePath = getPath(metadataUuid, visibility, file.getFileName().toString());

        FileUtils.copyFile(file.toFile(), filePath.toFile());

        return getResourceDescription(metadataUuid, visibility, filePath);
    }


    @Override
    public MetadataResource putResource(ServiceContext context, String metadataUuid, URL fileUrl, MetadataResourceVisibility visibility) throws Exception {
        canEdit(context, metadataUuid);
        String fileName = FilenameUtils.getName(fileUrl.getPath());
        if (fileName.contains("?")) {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }

        Path filePath = getPath(metadataUuid, visibility, fileName);

        Files.copy(fileUrl.openStream(), filePath);

        return getResourceDescription(metadataUuid, visibility, filePath);
    }

    private Path getPath(String metadataUuid, MetadataResourceVisibility visibility, String fileName) throws Exception {
        ApplicationContext _appContext = ApplicationContextHolder.get();
        GeonetworkDataDirectory dataDirectory = _appContext.getBean(GeonetworkDataDirectory.class);
        String metadataId = getAndCheckMetadataId(metadataUuid);
        Path metadataDir = Lib.resource.getMetadataDir(dataDirectory, metadataId);

        Path folderPath = metadataDir.resolve(visibility.toString());
        if (!Files.exists(folderPath)) {
            try {
                Files.createDirectories(folderPath);
            } catch (Exception e) {
                throw new IOException(String.format(
                    "Can't create folder '%s' to store resource with name '%s' for metadata '%s'.",
                    visibility, fileName, metadataUuid));
            }
        }


        Path filePath = folderPath.resolve(fileName);
        if (Files.exists(filePath)) {
            throw new ResourceAlreadyExistException(String.format(
                "A resource with name '%s' and status '%s' already exists for metadata '%s'.",
                fileName, visibility, metadataUuid));
        }
        return filePath;
    }


    @Override
    public String delResource(ServiceContext context, String metadataUuid) throws Exception {
        ApplicationContext _appContext = ApplicationContextHolder.get();
        String metadataId = getAndCheckMetadataId(metadataUuid);

        canEdit(context, metadataUuid);

        GeonetworkDataDirectory dataDirectory = _appContext.getBean(GeonetworkDataDirectory.class);
        Path metadataDir = Lib.resource.getMetadataDir(dataDirectory, metadataId);
        try {
            IO.deleteFileOrDirectory(metadataDir, true);
            return String.format("Metadata '%s' directory removed.", metadataUuid);
        } catch (Exception e) {
            return String.format("Unable to remove metadata '%s' directory.", metadataUuid);
        }
    }


    @Override
    public String delResource(ServiceContext context, String metadataUuid, String resourceId) throws Exception {
        canEdit(context, metadataUuid);

        Path filePath = getResource(context, metadataUuid, resourceId);

        try {
            Files.deleteIfExists(filePath);
            return String.format("MetadataResource '%s' removed.", resourceId);
        } catch (IOException e) {
            return String.format("Unable to remove resource '%s'.", resourceId);
        }
    }


    @Override
    public MetadataResource patchResourceStatus(ServiceContext context, String metadataUuid,
                                                String resourceId,
                                                MetadataResourceVisibility visibility) throws Exception {
        ApplicationContext _appContext = ApplicationContextHolder.get();
        AccessManager accessManager = _appContext.getBean(AccessManager.class);
        String metadataId = getAndCheckMetadataId(metadataUuid);

        if (accessManager.canEdit(context, metadataId)) {
            Path filePath = getResource(context, metadataUuid, resourceId);

            GeonetworkDataDirectory dataDirectory = _appContext.getBean(GeonetworkDataDirectory.class);
            Path metadataDir = Lib.resource.getMetadataDir(dataDirectory, metadataId);
            Path newFolderPath = metadataDir
                .resolve(visibility.toString());
            if (!Files.exists(newFolderPath)) {
                try {
                    Files.createDirectories(newFolderPath);
                } catch (Exception e) {
                    throw new IOException(String.format(
                        "Can't create folder '%s' to store resource with name '%s' for metadata '%s'.",
                        visibility, resourceId, metadataUuid));
                }
            }
            Path newFilePath = newFolderPath
                .resolve(filePath.getFileName());
            Files.move(filePath, newFilePath);
            return getResourceDescription(metadataUuid, visibility, newFilePath);
        } else {
            throw new SecurityException(String.format(
                "Current user can't edit metadata '%s' and as such can't change the resource status for '%s'.",
                metadataUuid, resourceId));
        }
    }

    /**
     * TODO: To be improve
     */
    private String getAndCheckMetadataId(String metadataUuid) throws Exception {
        ApplicationContext _appContext = ApplicationContextHolder.get();
        String metadataId = _appContext.getBean(DataManager.class).getMetadataId(metadataUuid);
        if (metadataId == null) {
            throw new ResourceNotFoundException(String.format(
                "Metadata with UUID '%s' not found.", metadataUuid
            ));
        }
        return metadataId;
    }

    private void canEdit(ServiceContext context, String metadataUuid) throws Exception {
        canEdit(context, metadataUuid, null);
    }

    private void canEdit(ServiceContext context, String metadataUuid,
                         MetadataResourceVisibility visibility) throws Exception {
        ApplicationContext _appContext = ApplicationContextHolder.get();
        String metadataId = getAndCheckMetadataId(metadataUuid);
        AccessManager accessManager = _appContext.getBean(AccessManager.class);
        boolean canEdit = accessManager.canEdit(context, metadataId);
        if ((visibility == null && !canEdit) ||
            (visibility == MetadataResourceVisibility.PRIVATE && !canEdit)) {
            throw new SecurityException(String.format(
                "User does not have privileges to access '%s' resources for metadata '%s'.",
                visibility == null ? "any" : visibility,
                metadataUuid));
        }
    }
}
