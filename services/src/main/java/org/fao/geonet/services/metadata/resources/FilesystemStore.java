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

package org.fao.geonet.services.metadata.resources;

import jeeves.server.context.ServiceContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A FileSystemStore store resources files in the
 * catalog data directory. Each metadata record
 * as a directory in the data directory containing
 * a public and a private folder.
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
    public FilesystemStore() {
    }

    private static Comparator<Resource> sortByFileName =
            new Comparator<Resource>() {
                public int compare(Resource o1, Resource o2) {
                    return o1.getId().compareTo(
                            o2.getId());
                }
            };

    @Override
    public List<Resource> getResources(String metadataUuid,
                                       Sort sort,
                                       String filter) throws Exception {
        List<Resource> resourceList = new ArrayList<>();
        ApplicationContext _appContext = ApplicationContextHolder.get();
        String metadataId = _appContext.getBean(DataManager.class)
                .getMetadataId(metadataUuid);
        AccessManager accessManager = _appContext.getBean(AccessManager.class);
        boolean canEdit = accessManager.canEdit(ServiceContext.get(), metadataId);

        resourceList.addAll(getResources(metadataUuid, ResourceType.PUBLIC, filter));
        if (canEdit) {
            resourceList.addAll(getResources(metadataUuid, ResourceType.PRIVATE, filter));
        }

        if (sort == Sort.name) {
            Collections.sort(resourceList, sortByFileName);
        }

        return resourceList;
    }



    @Override
    public List<Resource> getResources(String metadataUuid,
                                       ResourceType resourceType,
                                       String filter) throws Exception {
        ApplicationContext _appContext = ApplicationContextHolder.get();
        String metadataId = _appContext.getBean(DataManager.class)
                .getMetadataId(metadataUuid);
        GeonetworkDataDirectory dataDirectory =
                _appContext.getBean(GeonetworkDataDirectory.class);
        SettingManager settingManager = _appContext.getBean(SettingManager.class);
        AccessManager accessManager = _appContext.getBean(AccessManager.class);

        Path metadataDir = Lib.resource.getMetadataDir(dataDirectory, metadataId);

        boolean canEdit = accessManager.canEdit(ServiceContext.get(), metadataId);
        if (resourceType == ResourceType.PRIVATE && !canEdit) {
            throw new SecurityException(String.format(
                    "User does not have privileges to get the list of '%s' resources for metadata '%s'.",
                    resourceType, metadataUuid));
        }


        Path resourceTypeDir = metadataDir.resolve(resourceType.toString());

        List<Resource> resourceList = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream =
                     Files.newDirectoryStream(resourceTypeDir, filter)) {
            for (Path path : directoryStream) {
                Resource resource = new FilesystemStoreResource(
                        metadataUuid + "/resources/" + path.getFileName(),
                        settingManager.getNodeURL() + "api/metadata/",
                        resourceType);
                resourceList.add(resource);
            }
        } catch (IOException ex) {
        }

        Collections.sort(resourceList, sortByFileName);

        return resourceList;
    }



    @Override
    public Path getResource(String metadataUuid, String resourceId) throws Exception {
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
        String metadataId = _appContext.getBean(DataManager.class).getMetadataId(metadataUuid);
        Path metadataDir = Lib.resource.getMetadataDir(dataDirectory, metadataId);

        Path resourceFile = null;

        boolean canEdit = accessManager.canEdit(ServiceContext.get(), metadataId);
        for (ResourceType r : ResourceType.values()) {
            if (r == ResourceType.PRIVATE && !canEdit) {
                continue;
            }
            try (DirectoryStream<Path> directoryStream =
                         Files.newDirectoryStream(metadataDir.resolve(r.toString()),
                                 resourceId)) {
                for (Path path : directoryStream) {
                    if (Files.isRegularFile(path)) {
                        resourceFile = path;
                    }
                }
            } catch (IOException ex) {
            }
        }

        if (resourceFile != null && Files.exists(resourceFile)) {
            return resourceFile;
        } else {
            throw new FileNotFoundException(String.format(
                    "Resource '%s' not found for metadata '%s'",
                    resourceId, metadataUuid));
        }
    }


    private Resource getResourceDescription(String metadataUuid, ResourceType resourceType, Path filePath) {
        ApplicationContext _appContext = ApplicationContextHolder.get();
        SettingManager settingManager = _appContext.getBean(SettingManager.class);
        return new FilesystemStoreResource(
                metadataUuid + "/resources/" + filePath.getFileName(),
                settingManager.getNodeURL() + "api/metadata/",
                resourceType);
    }



    @Override
    public Resource putResource(String metadataUuid,
                                MultipartFile file,
                                ResourceType resourceType) throws Exception {
        ApplicationContext _appContext = ApplicationContextHolder.get();
        String metadataId = _appContext.getBean(DataManager.class).getMetadataId(metadataUuid);
        GeonetworkDataDirectory dataDirectory = _appContext.getBean(GeonetworkDataDirectory.class);
        Path metadataDir = Lib.resource.getMetadataDir(dataDirectory, metadataId);

        Path folderPath = metadataDir.resolve(resourceType.toString());
        if (!Files.exists(folderPath)) {
            folderPath.toFile().mkdirs();
        }

        Path filePath = folderPath.resolve(file.getOriginalFilename());
        if (Files.exists(filePath)) {
            throw new IOException(String.format(
                    "A resource with name '%s' and status '%s' already exists for metadata '%s'.",
                    file.getOriginalFilename(), resourceType, metadataUuid));
        }

        BufferedOutputStream stream =
                new BufferedOutputStream(
                        new FileOutputStream(filePath.toFile()));
        byte[] bytes = file.getBytes();
        stream.write(bytes);
        stream.close();

        return getResourceDescription(metadataUuid, resourceType, filePath);
    }

    @Override
    public Resource putResource(String metadataUuid, Path file, ResourceType resourceType) throws Exception {
        ApplicationContext _appContext = ApplicationContextHolder.get();
        String metadataId = _appContext.getBean(DataManager.class).getMetadataId(metadataUuid);
        GeonetworkDataDirectory dataDirectory = _appContext.getBean(GeonetworkDataDirectory.class);
        Path metadataDir = Lib.resource.getMetadataDir(dataDirectory, metadataId);

        Path folderPath = metadataDir.resolve(resourceType.toString());
        if (!Files.exists(folderPath)) {
            folderPath.toFile().mkdirs();
        }

        Path filePath = folderPath.resolve(file.getFileName());
        if (Files.exists(filePath)) {
            throw new IOException(String.format(
                    "A resource with name '%s' and status '%s' already exists for metadata '%s'.",
                    file.getFileName(), resourceType, metadataUuid));
        }

        try {
            FileUtils.copyFile(file.toFile(), filePath.toFile());
            return getResourceDescription(metadataUuid, resourceType, filePath);
        } catch (Exception e) {
            throw e;
        }
    }


    @Override
    public Resource putResource(String metadataUuid, URL fileUrl, ResourceType resourceType) throws Exception {
        ApplicationContext _appContext = ApplicationContextHolder.get();
        String metadataId = _appContext.getBean(DataManager.class).getMetadataId(metadataUuid);
        GeonetworkDataDirectory dataDirectory = _appContext.getBean(GeonetworkDataDirectory.class);
        Path metadataDir = Lib.resource.getMetadataDir(dataDirectory, metadataId);

        Path folderPath = metadataDir.resolve(resourceType.toString());
        if (!Files.exists(folderPath)) {
            folderPath.toFile().mkdirs();
        }
        String fileName = "todo.txt";
        Path filePath = folderPath.resolve(fileName);
        if (Files.exists(filePath)) {
            throw new IOException(String.format(
                    "A resource with name '%s' and status '%s' already exists for metadata '%s'.",
                    fileName, resourceType, metadataUuid));
        }

        FileUtils.copyURLToFile(fileUrl, filePath.toFile());
        return getResourceDescription(metadataUuid, resourceType, filePath);
    }


    @Override
    public String delResource(String metadataUuid) throws Exception {
        ApplicationContext _appContext = ApplicationContextHolder.get();
        String metadataId = _appContext.getBean(DataManager.class).getMetadataId(metadataUuid);
        AccessManager accessManager = _appContext.getBean(AccessManager.class);
        if (accessManager.canEdit(ServiceContext.get(), metadataId)) {
            GeonetworkDataDirectory dataDirectory = _appContext.getBean(GeonetworkDataDirectory.class);
            Path metadataDir = Lib.resource.getMetadataDir(dataDirectory, metadataId);
            boolean deleted = metadataDir.toFile().delete();
            if (deleted) {
                return String.format("Metadata '%s' directory removed.", metadataUuid);
            } else {
                return String.format("Unable to remove metadata '%s' directory.", metadataUuid);
            }
        } else {
            throw new SecurityException(String.format(
                    "Current user can't edit metadata '%s' and as such can't remove a resource.",
                    metadataUuid));
        }
    }


    @Override
    public String delResource(String metadataUuid, String resourceId) throws Exception {
        ApplicationContext _appContext = ApplicationContextHolder.get();
        AccessManager accessManager = _appContext.getBean(AccessManager.class);
        String metadataId = _appContext.getBean(DataManager.class).getMetadataId(metadataUuid);
        if (accessManager.canEdit(ServiceContext.get(), metadataId)) {
            Path filePath = getResource(metadataUuid, resourceId);

            try {
                Files.deleteIfExists(filePath);
                return String.format("Resource '%s' removed.", resourceId);
            } catch (IOException e) {
                return String.format("Unable to remove resource '%s'.", resourceId);
            }
        } else {
            throw new SecurityException(String.format(
                    "Current user can't edit metadata '%s' and as such can't remove the resource '%s'.",
                    metadataUuid, resourceId));
        }
    }


    @Override
    public Resource patchResourceStatus(String metadataUuid,
                                        String resourceId,
                                        ResourceType newStatus) throws Exception {
        ApplicationContext _appContext = ApplicationContextHolder.get();
        AccessManager accessManager = _appContext.getBean(AccessManager.class);
        String metadataId = _appContext.getBean(DataManager.class).getMetadataId(metadataUuid);
        if (accessManager.canEdit(ServiceContext.get(), metadataId)) {
            Path filePath = getResource(metadataUuid, resourceId);

            GeonetworkDataDirectory dataDirectory = _appContext.getBean(GeonetworkDataDirectory.class);
            Path metadataDir = Lib.resource.getMetadataDir(dataDirectory, metadataId);
            Path newFilePath = metadataDir
                    .resolve(newStatus.toString())
                    .resolve(filePath.getFileName());
            FileUtils.moveFile(filePath.toFile(), newFilePath.toFile());
            return getResourceDescription(metadataUuid, newStatus, newFilePath);
        } else {
            throw new SecurityException(String.format(
                    "Current user can't edit metadata '%s' and as such can't change the resource status for '%s'.",
                    metadataUuid, resourceId));
        }
    }
}
