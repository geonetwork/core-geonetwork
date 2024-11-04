/*
 * =============================================================================
 * ===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
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


import static org.jclouds.blobstore.options.PutOptions.Builder.multipart;

import jeeves.server.context.ServiceContext;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceContainer;
import org.fao.geonet.domain.MetadataResourceExternalManagementProperties;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.resources.JCloudConfiguration;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.jclouds.blobstore.ContainerNotFoundException;
import org.jclouds.blobstore.domain.*;
import org.jclouds.blobstore.options.CopyOptions;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JCloudStore extends AbstractStore {

    // For azure Blob ADSL hdi_isfolder property name used to identify folders
    private final static String AZURE_BLOB_IS_FOLDER_PROPERTY_NAME="hdi_isfolder";

    private Path baseMetadataDir = null;

    @Autowired
    JCloudConfiguration jCloudConfiguration;

    @Autowired
    SettingManager settingManager;

    @Override
    public List<MetadataResource> getResources(final ServiceContext context, final String metadataUuid,
                                               final MetadataResourceVisibility visibility, String filter, Boolean approved) throws Exception {
        final int metadataId = canDownload(context, metadataUuid, visibility, approved);

        final String resourceTypeDir = getMetadataDir(context, metadataId) + jCloudConfiguration.getFolderDelimiter() + visibility.toString() + jCloudConfiguration.getFolderDelimiter();

        List<MetadataResource> resourceList = new ArrayList<>();
        if (filter == null) {
            filter = FilesystemStore.DEFAULT_FILTER;
        }

        PathMatcher matcher =
            FileSystems.getDefault().getPathMatcher("glob:" + filter);

        ListContainerOptions opts = new ListContainerOptions();
        opts.delimiter(jCloudConfiguration.getFolderDelimiter()).prefix(resourceTypeDir);;

        // Page through the data
        String marker = null;
        do {
            if (marker != null) {
                opts.afterMarker(marker);
            }

            PageSet<? extends StorageMetadata> page = jCloudConfiguration.getClient().getBlobStore().list(jCloudConfiguration.getContainerName(), opts);

            for (StorageMetadata storageMetadata : page) {
                // Only add to the list if it is a blob and it matches the filter.
                Path keyPath = new File(storageMetadata.getName()).toPath().getFileName();
                if (storageMetadata.getType() == StorageType.BLOB && matcher.matches(keyPath)){
                    final String filename = getFilename(storageMetadata.getName());
                    MetadataResource resource = createResourceDescription(context, metadataUuid, visibility, filename, storageMetadata, metadataId, approved);
                    resourceList.add(resource);
                }
            }
            marker = page.getNextMarker();
        } while (marker != null);


        resourceList.sort(MetadataResourceVisibility.sortByFileName);

        return resourceList;
    }

    private MetadataResource createResourceDescription(final ServiceContext context, final String metadataUuid,
                                                       final MetadataResourceVisibility visibility, final String resourceId,
                                                       StorageMetadata storageMetadata, int metadataId, boolean approved) {
        String filename = getFilename(metadataUuid, resourceId);

        String versionValue = null;
        if (jCloudConfiguration.isVersioningEnabled()) {
            versionValue = storageMetadata.getETag(); // ETAG is cryptic may need some other value?
        }

        MetadataResourceExternalManagementProperties.ValidationStatus validationStatus = MetadataResourceExternalManagementProperties.ValidationStatus.UNKNOWN;
        if (!StringUtils.isEmpty(jCloudConfiguration.getExternalResourceManagementValidationStatusPropertyName())) {
            String validationStatusPropertyName = jCloudConfiguration.getExternalResourceManagementValidationStatusPropertyName();
            String propertyValue = null;
            if (storageMetadata.getUserMetadata().containsKey(validationStatusPropertyName)) {
                propertyValue = storageMetadata.getUserMetadata().get(validationStatusPropertyName);
            }
            if (StringUtils.isNotEmpty(propertyValue)) {
                validationStatus = MetadataResourceExternalManagementProperties.ValidationStatus.fromValue(Integer.parseInt(propertyValue));
            }
        }

        MetadataResourceExternalManagementProperties metadataResourceExternalManagementProperties =
            getMetadataResourceExternalManagementProperties(context, metadataId, metadataUuid, visibility, resourceId, filename, storageMetadata.getETag(), storageMetadata.getType(), validationStatus);

        return new FilesystemStoreResource(metadataUuid, metadataId, filename,
            settingManager.getNodeURL() + "api/records/", visibility, storageMetadata.getSize(), storageMetadata.getLastModified(), versionValue, metadataResourceExternalManagementProperties, approved);
    }

    protected static String getFilename(final String key) {
        final String[] splittedKey = key.split("/");
        return splittedKey[splittedKey.length - 1];
    }

    @Override
    public ResourceHolder getResource(final ServiceContext context, final String metadataUuid, final MetadataResourceVisibility visibility,
                                      final String resourceId, Boolean approved) throws Exception {
        // Those characters should not be allowed by URL structure
        int metadataId = canDownload(context, metadataUuid, visibility, approved);
        try {
            final Blob object = jCloudConfiguration.getClient().getBlobStore().getBlob(
                jCloudConfiguration.getContainerName(), getKey(context, metadataUuid, metadataId, visibility, resourceId));
            if (object == null) {
                throw new ResourceNotFoundException(
                    String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                    .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                    .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
            }
            return new ResourceHolderImpl(object, createResourceDescription(context, metadataUuid, visibility, resourceId,
                object.getMetadata(), metadataId, approved));
        } catch (ContainerNotFoundException e) {
            throw new ResourceNotFoundException(
                String.format("Metadata container for resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
        }
    }

    @Override
    public ResourceHolder getResourceInternal(String metadataUuid, MetadataResourceVisibility visibility, String resourceId, Boolean approved) throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        checkResourceId(resourceId);

        try {
            ServiceContext context = ServiceContext.get();
            final Blob object = jCloudConfiguration.getClient().getBlobStore().getBlob(
                jCloudConfiguration.getContainerName(), getKey(context, metadataUuid, metadataId, visibility, resourceId));
            return new ResourceHolderImpl(object, createResourceDescription(context, metadataUuid, visibility, resourceId,
                object.getMetadata(), metadataId, approved));
        } catch (ContainerNotFoundException e) {
            throw new ResourceNotFoundException(
                String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
        }

    }

    protected String getKey(final ServiceContext context, String metadataUuid, int metadataId, MetadataResourceVisibility visibility, String resourceId) {
        checkResourceId(resourceId);
        final String metadataDir = getMetadataDir(context, metadataId);
        return metadataDir + jCloudConfiguration.getFolderDelimiter() + visibility.toString() + jCloudConfiguration.getFolderDelimiter() + getFilename(metadataUuid, resourceId);
    }

    @Override
    public MetadataResource putResource(final ServiceContext context, final String metadataUuid, final String filename,
                                        final InputStream is, @Nullable final Date changeDate, final MetadataResourceVisibility visibility, Boolean approved)
            throws Exception {
        return putResource(context, metadataUuid, filename, is, changeDate, visibility, approved, null);
    }

    protected MetadataResource putResource(final ServiceContext context, final String metadataUuid, final String filename,
                                        final InputStream is, @Nullable final Date changeDate, final MetadataResourceVisibility visibility, Boolean approved, Map<String, String> additionalProperties)
        throws Exception {
        final int metadataId = canEdit(context, metadataUuid, approved);
        String key = getKey(context, metadataUuid, metadataId, visibility, filename);

        Map<String, String> properties = null;

         try {
            StorageMetadata storageMetadata = jCloudConfiguration.getClient().getBlobStore().blobMetadata(jCloudConfiguration.getContainerName(), key);
            if (storageMetadata != null) {
                properties = storageMetadata.getUserMetadata();
            }
        } catch (ContainerNotFoundException ignored) {
            // ignored
        }


        if (properties == null) {
            properties = new HashMap<>();
        }

        addProperties(metadataUuid, properties, changeDate, additionalProperties);

        Blob blob = jCloudConfiguration.getClient().getBlobStore().blobBuilder(key)
            .payload(is)
            .contentLength(is.available())
            .userMetadata(properties)
            .build();
        // Upload the Blob in multiple chunks to supports large files.
        jCloudConfiguration.getClient().getBlobStore().putBlob(jCloudConfiguration.getContainerName(), blob, multipart());
        Blob blobResults = jCloudConfiguration.getClient().getBlobStore().getBlob(jCloudConfiguration.getContainerName(), key);


        return createResourceDescription(context, metadataUuid, visibility, filename, blobResults.getMetadata(), metadataId, approved);

    }

    protected void addProperties(String metadataUuid, Map<String, String> properties, Date changeDate, Map<String, String> additionalProperties) {

        // Add additional properties if exists.
        if (MapUtils.isNotEmpty(additionalProperties)) {
            properties.putAll(additionalProperties);
        }

        // now update metadata uuid and status and change date .

        setMetadataUUID(properties, metadataUuid);

        // JCloud does not allow changing the last modified date.  So the change date will be put in defined changed date field if supplied.
        setExternalResourceManagementChangedDate(properties, changeDate);

        // If it is a new record so set the default status value property if it does not already exist as an additional property.
        if (!StringUtils.isEmpty(jCloudConfiguration.getExternalResourceManagementValidationStatusPropertyName()) &&
            !properties.containsKey(jCloudConfiguration.getExternalResourceManagementValidationStatusPropertyName())) {
            setExternalManagementResourceValidationStatus(properties, jCloudConfiguration.getValidationStatusDefaultValue());
        }

    }
    protected void setMetadataUUID(Map<String, String> properties, String metadataUuid) {
        // Don't allow users metadata uuid to be supplied as a property so let's overwrite any value that may exist.
        if (!StringUtils.isEmpty(jCloudConfiguration.getMetadataUUIDPropertyName())) {
            setProperty(properties, jCloudConfiguration.getMetadataUUIDPropertyName(), metadataUuid);
        }
    }

    protected void setExternalResourceManagementChangedDate(Map<String, String> properties, Date changeDate) {
        // Don't allow change date to be supplied as a property so let's overwrite any value that may exist.
        if (changeDate != null && !StringUtils.isEmpty(jCloudConfiguration.getExternalResourceManagementChangedDatePropertyName())) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            properties.put(jCloudConfiguration.getExternalResourceManagementChangedDatePropertyName(), dateFormat.format(changeDate));
        }
    }

    protected void setExternalManagementResourceValidationStatus(Map<String, String> properties, MetadataResourceExternalManagementProperties.ValidationStatus status) {
        if (!StringUtils.isEmpty(jCloudConfiguration.getExternalResourceManagementValidationStatusPropertyName())) {
            setProperty(properties, jCloudConfiguration.getExternalResourceManagementValidationStatusPropertyName(), String.valueOf(status.getValue()));
        }
    }

    protected void setProperty(Map<String, String> properties, String propertyName, String value) {
        if (!StringUtils.isEmpty(propertyName)) {
            properties.put(propertyName, value);
        }
    }
    @Override
    public MetadataResource patchResourceStatus(final ServiceContext context, final String metadataUuid, final String resourceId,
                                                final MetadataResourceVisibility visibility, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);

        String sourceKey = null;
        StorageMetadata storageMetadata = null;
        for (MetadataResourceVisibility sourceVisibility : MetadataResourceVisibility.values()) {
            final String key = getKey(context, metadataUuid, metadataId, sourceVisibility, resourceId);
            try {
                storageMetadata = jCloudConfiguration.getClient().getBlobStore().blobMetadata(jCloudConfiguration.getContainerName(), key);
                if (storageMetadata != null) {
                    if (sourceVisibility != visibility) {
                        sourceKey = key;
                        break;
                    } else {
                        // already the good visibility
                        return createResourceDescription(context, metadataUuid, visibility, resourceId, storageMetadata, metadataId, approved);
                    }
                }
            } catch (ContainerNotFoundException ignored) {
                // ignored
            }
        }
        if (sourceKey != null) {
            final String destKey = getKey(context, metadataUuid, metadataId, visibility, resourceId);

            jCloudConfiguration.getClient().getBlobStore().copyBlob(jCloudConfiguration.getContainerName(), sourceKey, jCloudConfiguration.getContainerName(), destKey, CopyOptions.NONE);
            jCloudConfiguration.getClient().getBlobStore().removeBlob(jCloudConfiguration.getContainerName(), sourceKey);

            Blob blobResults = jCloudConfiguration.getClient().getBlobStore().getBlob(jCloudConfiguration.getContainerName(), destKey);

            return createResourceDescription(context, metadataUuid, visibility, resourceId, blobResults.getMetadata(), metadataId, approved);
        } else {
            Log.warning(Geonet.RESOURCES,
                String.format("Could not update permissions. Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid));
            throw new ResourceNotFoundException(
                String.format("Could not update permissions. Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid));
        }
    }

    @Override
    public String delResources(final ServiceContext context, final int metadataId) throws Exception {
        try {
            ListContainerOptions opts = new ListContainerOptions();
            opts.prefix(getMetadataDir(context, metadataId) + jCloudConfiguration.getFolderDelimiter()).recursive();

            Log.info(Geonet.RESOURCES, String.format("Deleting all files from metadataId '%s'", metadataId));
            // Page through the data
            String marker = null;
            do {
                if (marker != null) {
                    opts.afterMarker(marker);
                }

                PageSet<? extends StorageMetadata> page = jCloudConfiguration.getClient().getBlobStore().list(jCloudConfiguration.getContainerName(), opts);

                for (StorageMetadata storageMetadata : page) {
                    if (!isFolder(storageMetadata))  {
                        jCloudConfiguration.getClient().getBlobStore().removeBlob(jCloudConfiguration.getContainerName(), storageMetadata.getName());
                    }
                }
                marker = page.getNextMarker();
            } while (marker != null);
            Log.info(Geonet.RESOURCES,
                String.format("Metadata '%d' directory removed.", metadataId));
            return String.format("Metadata '%d' directory removed.", metadataId);
        } catch (ContainerNotFoundException e) {
            Log.warning(Geonet.RESOURCES,
                String.format("Unable to located metadata '%d' directory to be removed.", metadataId));
            return String.format("Unable to located metadata '%d' directory to be removed.", metadataId);
        }
    }

    @Override
    public String delResource(final ServiceContext context, final String metadataUuid, final String resourceId, Boolean approved)
        throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);

        for (MetadataResourceVisibility visibility : MetadataResourceVisibility.values()) {
            if (tryDelResource(context, metadataUuid, metadataId, visibility, resourceId)) {
                return String.format("Metadata resource '%s' removed.", resourceId);
            }
        }
        return String.format("Unable to remove resource '%s'.", resourceId);
    }

    @Override
    public String delResource(final ServiceContext context, final String metadataUuid, final MetadataResourceVisibility visibility,
                              final String resourceId, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        if (tryDelResource(context, metadataUuid, metadataId, visibility, resourceId)) {
            return String.format("Metadata resource '%s' removed.", resourceId);
        }
        return String.format("Unable to remove resource '%s'.", resourceId);
    }

    protected boolean tryDelResource(final ServiceContext context, final String metadataUuid, final int metadataId, final MetadataResourceVisibility visibility,
                                   final String resourceId) throws Exception {
        final String key = getKey(context, metadataUuid, metadataId, visibility, resourceId);

        if (jCloudConfiguration.getClient().getBlobStore().blobExists(jCloudConfiguration.getContainerName(), key)) {
            jCloudConfiguration.getClient().getBlobStore().removeBlob(jCloudConfiguration.getContainerName(), key);
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
        final String key = getKey(context, metadataUuid, metadataId, visibility, filename);
        try {
            final Blob object = jCloudConfiguration.getClient().getBlobStore().getBlob(jCloudConfiguration.getContainerName(), key);
            if (object == null) {
                return null;
            } else {
                final StorageMetadata metadata = object.getMetadata();
                return createResourceDescription(context, metadataUuid, visibility, filename, metadata, metadataId, approved);
            }
        } catch (ContainerNotFoundException e) {
            return null;
        }
    }

    @Override
    public MetadataResourceContainer getResourceContainerDescription(final ServiceContext context, final String metadataUuid, Boolean approved) throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);

        final String key = getMetadataDir(context, metadataId);


        String folderRoot = jCloudConfiguration.getExternalResourceManagementFolderRoot();
        if (folderRoot == null) {
            folderRoot = "";
        }
        MetadataResourceExternalManagementProperties metadataResourceExternalManagementProperties =
            getMetadataResourceExternalManagementProperties(context, metadataId, metadataUuid, null, String.valueOf(metadataId), null, null, StorageType.FOLDER,
                MetadataResourceExternalManagementProperties.ValidationStatus.UNKNOWN);

        return new FilesystemStoreResourceContainer(metadataUuid, metadataId, metadataUuid,
            settingManager.getNodeURL() + "api/records/", metadataResourceExternalManagementProperties, approved);
    }

    private String getMetadataDir(ServiceContext context, final int metadataId) {

        Path metadataFullDir = Lib.resource.getMetadataDir(getDataDirectory(context), metadataId);
        Path baseMetadataDir = getBaseMetadataDir(context, metadataFullDir);
        Path metadataDir;
        if (baseMetadataDir.toString().equals(".")) {
            metadataDir = Paths.get(jCloudConfiguration.getBaseFolder()).resolve(metadataFullDir);
        } else {
            metadataDir = Paths.get(jCloudConfiguration.getBaseFolder()).resolve(baseMetadataDir.relativize(metadataFullDir));
        }

        String key;
        // For windows it may be "\" in which case we need to change it to folderDelimiter which is normally "/"
        if (metadataDir.getFileSystem().getSeparator().equals(jCloudConfiguration.getFolderDelimiter())) {
            key = metadataDir.toString();
        } else {
            key = metadataDir.toString().replace(metadataDir.getFileSystem().getSeparator(), jCloudConfiguration.getFolderDelimiter());
        }

        // For Windows, the pathString may start with // so remove one if this is the case.
        if (key.startsWith("//")) {
            key = key.substring(1);
        }

        // Make sure the key that is returns does not starts with "/" as it is already assumed to be relative to the container.
        if (key.startsWith(jCloudConfiguration.getFolderDelimiter())) {
            return key.substring(1);
        } else {
            return key;
        }
    }

    protected Path getBaseMetadataDir(ServiceContext context, Path metadataFullDir) {
        //If we not already figured out the base metadata dir then lets figure it out.
        if (baseMetadataDir == null) {
            Path systemFullDir = getDataDirectory(context).getSystemDataDir();

            // If the metadata full dir is relative from the system dir then use system dir as the base dir.
            if (metadataFullDir.toString().startsWith(systemFullDir.toString())) {
                baseMetadataDir = systemFullDir;
            } else {
                // If the metadata full dir is an absolute folder then use that as the base dir.
                if (getDataDirectory(context).getMetadataDataDir().isAbsolute()) {
                    baseMetadataDir = metadataFullDir.getRoot();
                } else {
                    // use it as a relative url.
                    baseMetadataDir = Paths.get(".");
                }
            }
        }
        return baseMetadataDir;
    }

    private GeonetworkDataDirectory getDataDirectory(ServiceContext context) {
        return ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class);
    }

    private boolean isFolder(StorageMetadata storageMetadata) {
        // For azure Blob ADSL if the type is folder then the storage type will be BLOB and hdi_isfolder=true so we cannot only rely on StorageType.FOLDER
        return storageMetadata.getType().equals(StorageType.FOLDER) || "true".equals(storageMetadata.getUserMetadata().get(AZURE_BLOB_IS_FOLDER_PROPERTY_NAME));
    }

    /**
     * get external resource management for the supplied resource.
     * Replace the following
     * {objectId}  type:visibility:metadataId:version:resourceId in base64 encoding
     * {id}  resource id
     * {type} // If the type is folder then type "folder" will be displayed else if document then "document" will be displayed
     * {uuid}  metadatauuid
     * {metadataid}  metadataid
     * {visibility}  visibility
     * {filename}  filename
     * {version}  version
     * {lang}  ISO639-1 2 char language
     * {iso3lang}  ISO 639-2/T language
     * <p>
     * Sample url for custom app
     * http://localhost:8080/artifact?filename={filename}&version={version}&lang={lang}
     */

    private MetadataResourceExternalManagementProperties getMetadataResourceExternalManagementProperties(ServiceContext context,
                                                    int metadataId,
                                                    final String metadataUuid,
                                                    final MetadataResourceVisibility visibility,
                                                    final String resourceId,
                                                    String filename,
                                                    String version,
                                                    StorageType type,
                                                    MetadataResourceExternalManagementProperties.ValidationStatus validationStatus
    ) {
        String metadataResourceExternalManagementPropertiesUrl = jCloudConfiguration.getExternalResourceManagementUrl();
        String objectId = getResourceManagementExternalPropertiesObjectId((type == null ? "document" : (StorageType.FOLDER.equals(type) ? "folder" : "document")), visibility, metadataId, version,
            resourceId);
        if (!StringUtils.isEmpty(metadataResourceExternalManagementPropertiesUrl)) {
            // {objectid}  objectId // It will be the type:visibility:metadataId:version:resourceId in base64
            // i.e. folder::100::100                     # Folder in resource 100
            // i.e. document:public:100:v1:sample.jpg    # public document 100 version v1 name sample.jpg
            if (metadataResourceExternalManagementPropertiesUrl.contains("{objectid}")) {
                metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{objectid\\})", objectId);
            }
            // {id}  id
            if (metadataResourceExternalManagementPropertiesUrl.contains("{id}")) {
                metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{id\\})", resourceId);
            }
            // {type} // If the type is folder then type "folder" will be displayed else if document then "document" will be displayed
            if (metadataResourceExternalManagementPropertiesUrl.contains("{type}")) {
                metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{type\\})",
                    (type == null ? "document" : (StorageType.FOLDER.equals(type) ? "folder" : "document")));
            }
            // {uuid}  metadata uuid
            if (metadataResourceExternalManagementPropertiesUrl.contains("{uuid}")) {
                metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{uuid\\})", (metadataUuid == null ? "" : metadataUuid));
            }
            // {metadataid}  metadataId
            if (metadataResourceExternalManagementPropertiesUrl.contains("{metadataid}")) {
                metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{metadataid\\})", String.valueOf(metadataId));
            }
            //    {visibility}  visibility
            if (metadataResourceExternalManagementPropertiesUrl.contains("{visibility}")) {
                metadataResourceExternalManagementPropertiesUrl =
                    metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{visibility\\})", (visibility == null ? "" : visibility.toString().toLowerCase()));
            }
            //    {filename}  filename
            if (metadataResourceExternalManagementPropertiesUrl.contains("{filename}")) {
                metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{filename\\})", (filename == null ? "" : filename));
            }
            // {version}  version
            if (metadataResourceExternalManagementPropertiesUrl.contains("{version}")) {
                metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{version\\})", (version == null ? "" : version));
            }

            if (metadataResourceExternalManagementPropertiesUrl.contains("{lang}") || metadataResourceExternalManagementPropertiesUrl.contains("{ISO3lang}")) {
                final IsoLanguagesMapper mapper = context.getBean(IsoLanguagesMapper.class);
                String contextLang = context.getLanguage() == null ? Geonet.DEFAULT_LANGUAGE : context.getLanguage();
                String lang;
                String iso3Lang;

                if (contextLang.length() == 2) {
                    lang = contextLang;
                    iso3Lang = mapper.iso639_1_to_iso639_2(contextLang);
                } else {
                    lang = mapper.iso639_2_to_iso639_1(contextLang);
                    iso3Lang = contextLang;
                }
                // {lang}  ISO639-1 2 char language
                if (metadataResourceExternalManagementPropertiesUrl.contains("{lang}")) {
                    metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{lang\\})", lang);
                }
                // {iso3lang}  ISO 639-2/T language
                if (metadataResourceExternalManagementPropertiesUrl.contains("{iso3lang}")) {
                    metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{iso3lang\\})", iso3Lang);
                }
            }
        }

        MetadataResourceExternalManagementProperties metadataResourceExternalManagementProperties
                = new MetadataResourceExternalManagementProperties(objectId, metadataResourceExternalManagementPropertiesUrl, validationStatus);

        return metadataResourceExternalManagementProperties;
    }

    public ResourceManagementExternalProperties getResourceManagementExternalProperties() {
        return new ResourceManagementExternalProperties() {
            @Override
            public boolean isEnabled() {
                // Return true if we have an external management url
                return !StringUtils.isEmpty(jCloudConfiguration.getExternalResourceManagementUrl());
            }

            @Override
            public String getWindowParameters() {
                return jCloudConfiguration.getExternalResourceManagementWindowParameters();
            }

            @Override
            public boolean isModal() {
                return jCloudConfiguration.isExternalResourceManagementModalEnabled();
            }

            @Override
            public boolean isFolderEnabled() {
                return isEnabled() && jCloudConfiguration.isExternalResourceManagementFolderEnabled();
            }

            @Override
            public String toString() {
                try {
                    return new ObjectMapper().writeValueAsString(this);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error converting ResourceManagementExternalProperties to json", e);
                }
            }
        };
    }

    protected static class ResourceHolderImpl implements ResourceHolder {
        private Path tempFolderPath;
        private Path path;
        private final MetadataResource metadataResource;

        public ResourceHolderImpl(final Blob object, MetadataResource metadataResource) throws IOException {
            // Preserve filename by putting the files into a temporary folder and using the same filename.
            tempFolderPath = Files.createTempDirectory("gn-meta-res-" + String.valueOf(metadataResource.getMetadataId() + "-"));
            tempFolderPath.toFile().deleteOnExit();
            path = tempFolderPath.resolve(getFilename(object.getMetadata().getName()));
            this.metadataResource = metadataResource;
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
            return metadataResource;
        }

        @Override
        public void close() throws IOException {
            // Delete temporary file and folder.
            IO.deleteFileOrDirectory(tempFolderPath, true);
            path=null;
            tempFolderPath = null;
        }

        @Override
        protected void finalize() throws Throwable {
            close();
            super.finalize();
        }
    }
}
