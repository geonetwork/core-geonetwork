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
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.exception.InputStreamLimitExceededException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.resources.JCloudConfiguration;
import org.fao.geonet.util.LimitedInputStream;
import org.fao.geonet.utils.Log;
import org.jclouds.blobstore.ContainerNotFoundException;
import org.jclouds.blobstore.domain.*;
import org.jclouds.blobstore.options.CopyOptions;
import org.jclouds.blobstore.options.GetOptions;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.http.HttpResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JCloudStore extends AbstractStore {

    private static final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    private static final String FIRST_VERSION = "1";

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    static {
        DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    // For azure Blob ADSL hdi_isfolder property name used to identify folders
    private static final String AZURE_BLOB_IS_FOLDER_PROPERTY_NAME="hdi_isfolder";

    private Path baseMetadataDir = null;

    @Autowired
    JCloudConfiguration jCloudConfiguration;

    @Autowired
    SettingManager settingManager;

    @Override
    public List<MetadataResource> getResources(final ServiceContext context, final String metadataUuid,
                                               final MetadataResourceVisibility visibility, String filter, Boolean approved, boolean includeAdditionalIndexedProperties) throws Exception {
        final int metadataId = canDownload(context, metadataUuid, visibility, approved);

        final String resourceTypeDir = getMetadataDir(context, metadataId) + jCloudConfiguration.getFolderDelimiter() + visibility.toString() + jCloudConfiguration.getFolderDelimiter();

        List<MetadataResource> resourceList = new ArrayList<>();
        if (filter == null) {
            filter = FilesystemStore.DEFAULT_FILTER;
        }

        PathMatcher matcher =
            FileSystems.getDefault().getPathMatcher("glob:" + filter);

        ListContainerOptions opts = new ListContainerOptions();
        opts.delimiter(jCloudConfiguration.getFolderDelimiter()).prefix(resourceTypeDir);

        // Page through the data
        String marker = null;
        do {
            if (marker != null) {
                opts.afterMarker(marker);
            }

            PageSet<? extends StorageMetadata> page = jCloudConfiguration.getClient().getBlobStore().list(jCloudConfiguration.getContainerName(), opts);

            for (StorageMetadata storageMetadata : page) {
                // Only add to the list if it is a blob, and it matches the filter.
                Path keyPath = new File(storageMetadata.getName()).toPath().getFileName();
                if (storageMetadata.getType() == StorageType.BLOB && matcher.matches(keyPath)){
                    final String filename = getFilename(storageMetadata.getName());
                    MetadataResource resource = createResourceDescription(context, metadataUuid, visibility, filename, storageMetadata, metadataId, approved, includeAdditionalIndexedProperties);
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
                                                       StorageMetadata storageMetadata, int metadataId, boolean approved, boolean includeAdditionalIndexedProperties) {
        String filename = getFilename(metadataUuid, resourceId);

        Date changedDate;
        String changedDatePropertyName = jCloudConfiguration.getExternalResourceManagementChangedDatePropertyName();
        if (storageMetadata.getUserMetadata().containsKey(changedDatePropertyName)) {
            String changedDateValue = storageMetadata.getUserMetadata().get(changedDatePropertyName);
            try {
                changedDate = DATE_FORMATTER.parse(changedDateValue);
            } catch (ParseException e) {
                Log.warning(Geonet.RESOURCES, String.format("Unable to parse date '%s' into format pattern '%s' on resource '%s' for metadata %d(%s). Will use resource last modified date",
                    changedDateValue, DATE_FORMATTER.toPattern(), resourceId, metadataId, metadataUuid), e);
                changedDate = storageMetadata.getLastModified();
            }
        } else {
            changedDate = storageMetadata.getLastModified();
        }


        String versionValue = null;
        if (jCloudConfiguration.isVersioningEnabled()) {
            String versionPropertyName = jCloudConfiguration.getExternalResourceManagementVersionPropertyName();
            if (StringUtils.hasLength(versionPropertyName)) {
                if (storageMetadata.getUserMetadata().containsKey(versionPropertyName)) {
                    versionValue = storageMetadata.getUserMetadata().get(versionPropertyName);
                } else {
                    Log.warning(Geonet.RESOURCES, String.format("Expecting property '%s' on resource '%s' for metadata %d(%s) but the property was not found.",
                        versionPropertyName, resourceId, metadataId, metadataUuid));
                    versionValue = "";
                }
            } else {
                versionValue = storageMetadata.getETag();
            }
        }

        MetadataResourceExternalManagementProperties.ValidationStatus validationStatus = MetadataResourceExternalManagementProperties.ValidationStatus.UNKNOWN;
        if (StringUtils.hasLength(jCloudConfiguration.getExternalResourceManagementValidationStatusPropertyName())) {
            String validationStatusPropertyName = jCloudConfiguration.getExternalResourceManagementValidationStatusPropertyName();
            String propertyValue = null;
            if (storageMetadata.getUserMetadata().containsKey(validationStatusPropertyName)) {
                propertyValue = storageMetadata.getUserMetadata().get(validationStatusPropertyName);
            }
            if (StringUtils.hasLength(propertyValue)) {
                validationStatus = MetadataResourceExternalManagementProperties.ValidationStatus.fromValue(Integer.parseInt(propertyValue));
            }
        }

        MetadataResourceExternalManagementProperties metadataResourceExternalManagementProperties;
        if (includeAdditionalIndexedProperties) {
            Map<String, Object> additionalProperties = new HashMap<>();
            if (jCloudConfiguration.getAdditionalProperties() != null && !jCloudConfiguration.getAdditionalProperties().isEmpty()) {
                for (String propertyName : jCloudConfiguration.getAdditionalProperties()) {
                    String propertyValue = null;
                    if (storageMetadata.getUserMetadata().containsKey(propertyName)) {
                        propertyValue = storageMetadata.getUserMetadata().get(propertyName);
                    }
                    if (StringUtils.hasLength(propertyValue)) {
                        additionalProperties.put(propertyName, propertyValue);
                    }
                }
            }
            metadataResourceExternalManagementProperties =
                getIndexedMetadataResourceExternalManagementProperties(context, metadataId, metadataUuid, visibility, resourceId, filename, storageMetadata.getETag(), storageMetadata.getType(),
                    validationStatus, additionalProperties);
        } else {
            metadataResourceExternalManagementProperties =
                getMetadataResourceExternalManagementProperties(context, metadataId, metadataUuid, visibility, resourceId, filename, storageMetadata.getETag(), storageMetadata.getType(),
                    validationStatus);
        }

        return new FilesystemStoreResource(metadataUuid, metadataId, filename,
            settingManager.getNodeURL() + "api/records/", visibility, storageMetadata.getSize(), changedDate, versionValue, metadataResourceExternalManagementProperties, approved);
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
            return new JCloudResourceHolder(object, createResourceDescription(context, metadataUuid, visibility, resourceId,
                object.getMetadata(), metadataId, approved, false));
        } catch (ContainerNotFoundException e) {
            throw new ResourceNotFoundException(
                String.format("Metadata container for resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
        }
    }

    @Override
    public MetadataResource getResourceMetadata(final ServiceContext context, final String metadataUuid, final MetadataResourceVisibility visibility,
                                                final String resourceId, Boolean approved) throws Exception {
        // Those characters should not be allowed by URL structure
        int metadataId = canDownload(context, metadataUuid, visibility, approved);
        try {
            final BlobMetadata metadata = jCloudConfiguration.getClient().getBlobStore().blobMetadata(
                jCloudConfiguration.getContainerName(), getKey(context, metadataUuid, metadataId, visibility, resourceId));
            if (metadata == null) {
                throw new ResourceNotFoundException(
                    String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                    .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                    .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
            }
            return createResourceDescription(context, metadataUuid, visibility, resourceId,
                metadata, metadataId, approved, false);
        } catch (ContainerNotFoundException e) {
            throw new ResourceNotFoundException(
                String.format("Metadata container for resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
        }
    }

    @Override
    public ResourceHolder getResourceWithRange(ServiceContext context, String metadataUuid, MetadataResourceVisibility metadataResourceVisibility, String resourceId, Boolean approved, long start, long end) throws Exception {
        GetOptions getOptions = new GetOptions().range(start, end);

        // Those characters should not be allowed by URL structure
        int metadataId = canDownload(context, metadataUuid, metadataResourceVisibility, approved);
        try {
            final Blob object = jCloudConfiguration.getClient().getBlobStore().getBlob(
                jCloudConfiguration.getContainerName(), getKey(context, metadataUuid, metadataId, metadataResourceVisibility, resourceId), getOptions);
            if (object == null) {
                throw new ResourceNotFoundException(
                    String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                    .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                    .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
            }
            return new JCloudResourceHolder(object, createResourceDescription(context, metadataUuid, metadataResourceVisibility, resourceId,
                object.getMetadata(), metadataId, approved, false));
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
            return new JCloudResourceHolder(object, createResourceDescription(context, metadataUuid, visibility, resourceId,
                object.getMetadata(), metadataId, approved, false));
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
                                        final InputStream is, @Nullable final Date changeDate, final MetadataResourceVisibility visibility, final Boolean approved)
            throws Exception {
        return putResource(context, metadataUuid, filename, is, changeDate, visibility, approved, null);
    }

    protected MetadataResource putResource(final ServiceContext context, final String metadataUuid, final String filename,
                                        final InputStream is, @Nullable final Date changeDate, final MetadataResourceVisibility visibility, final Boolean approved,
                                           Map<String, String> additionalProperties)
        throws Exception {
        final int metadataId = canEdit(context, metadataUuid, approved);
        String key = getKey(context, metadataUuid, metadataId, visibility, filename);

        // Get or create a lock object
        Object lock = locks.computeIfAbsent(key, k -> new Object());

        // Avoid multiple updates on the same file at the same time. otherwise the properties could get messed up.
        // Especially the version number.
        synchronized (lock) {
            try {
                Map<String, String> properties = null;
                boolean isNewResource = true;

                try {
                    StorageMetadata storageMetadata = jCloudConfiguration.getClient().getBlobStore().blobMetadata(jCloudConfiguration.getContainerName(), key);
                    if (storageMetadata != null) {
                        isNewResource = false;

                        // Copy existing properties
                        properties = new HashMap<>(storageMetadata.getUserMetadata());
                    }
                } catch (ContainerNotFoundException ignored) {
                    // ignored
                }

                if (properties == null) {
                    properties = new HashMap<>();
                }

                setProperties(properties, metadataUuid, changeDate, additionalProperties);

                // Update/set version
                setPropertiesVersion(context, properties, isNewResource, metadataUuid, metadataId, visibility, approved, filename);

                long contentLength;
                // If the input stream is a LimitedInputStream and the file size is known then use that value otherwise use the available value.
                if (is instanceof LimitedInputStream && ((LimitedInputStream) is).getFileSize() > 0) {
                    contentLength = ((LimitedInputStream) is).getFileSize();
                } else {
                    contentLength = is.available();
                }

                Blob blob = jCloudConfiguration.getClient().getBlobStore().blobBuilder(key)
                    .payload(is)
                    .contentLength(contentLength)
                    .userMetadata(properties)
                    .build();

                Log.info(Geonet.RESOURCES,
                    String.format("Put(2) blob '%s' with version label '%s'.", key, properties.get(jCloudConfiguration.getExternalResourceManagementVersionPropertyName())));

                try {
                    // Upload the Blob in multiple chunks to supports large files.
                    jCloudConfiguration.getClient().getBlobStore().putBlob(jCloudConfiguration.getContainerName(), blob, multipart());
                } catch (HttpResponseException e) {
                    // This is special logic for the jcloud store as the InputStreamLimitExceededException gets wrapped in a HttpResponseException
                    Throwable cause = e.getCause();
                    if (cause instanceof InputStreamLimitExceededException) {
                        throw (InputStreamLimitExceededException) cause;
                    }
                    throw e;
                }
                Blob blobResults = jCloudConfiguration.getClient().getBlobStore().getBlob(jCloudConfiguration.getContainerName(), key);

                return createResourceDescription(context, metadataUuid, visibility, filename, blobResults.getMetadata(), metadataId, approved, false);
            } finally {
                locks.remove(key);
            }
        }
    }

    protected void setProperties(Map<String, String> properties, String metadataUuid, Date changeDate, Map<String, String> additionalProperties) {

        // Add additional properties if exists.
        if (MapUtils.isNotEmpty(additionalProperties)) {
            properties.putAll(additionalProperties);
        }

        // now update metadata uuid and status and change date .
        setMetadataUUID(properties, metadataUuid);

        // JCloud does not allow changing the last modified date or creation date.  So the change date/created date will be put in defined changed date/created date field if supplied.
        setExternalResourceManagementDates(properties, changeDate);

        // If it is a new record so set the default status value property if it does not already exist as an additional property.
        if (StringUtils.hasLength(jCloudConfiguration.getExternalResourceManagementValidationStatusPropertyName()) &&
            !properties.containsKey(jCloudConfiguration.getExternalResourceManagementValidationStatusPropertyName())) {
            setExternalManagementResourceValidationStatus(properties, jCloudConfiguration.getValidationStatusDefaultValue());
        }
    }
    protected void setMetadataUUID(Map<String, String> properties, String metadataUuid) {
        // Don't allow users metadata uuid to be supplied as a property so let's overwrite any value that may exist.
        if (StringUtils.hasLength(jCloudConfiguration.getMetadataUUIDPropertyName())) {
            setPropertyValue(properties, jCloudConfiguration.getMetadataUUIDPropertyName(), metadataUuid);
        }
    }

    protected void setExternalResourceManagementDates(Map<String, String> properties, Date changeDate) {
        // If changeDate was not supplied then default to now.
        if (changeDate == null) {
            changeDate = new Date();
        }

        // JCloud does not allow created date to be set so we may supply the value we want as a property so assign the value.
        // Only assign the value if we currently don't have a creation date, and we don't have a version assigned either because if either of these exists then
        // it will indicate that this is not the first version.
        String createdDatePropertyName = jCloudConfiguration.getExternalResourceManagementCreatedDatePropertyName();
        String versionPropertyName = jCloudConfiguration.getExternalResourceManagementVersionPropertyName();
        if (StringUtils.hasLength(createdDatePropertyName) &&
            !properties.containsKey(createdDatePropertyName) &&
            (!StringUtils.hasLength(versionPropertyName) || (!properties.containsKey(versionPropertyName)))
        ) {
            properties.put(jCloudConfiguration.getExternalResourceManagementCreatedDatePropertyName(), DATE_FORMATTER.format(changeDate));
        }

        // JCloud does not allow last modified date to be changed so we may supply the value we want as a property so let's overwrite any value that may exist.
        if (StringUtils.hasLength(jCloudConfiguration.getExternalResourceManagementChangedDatePropertyName())) {
            properties.put(jCloudConfiguration.getExternalResourceManagementChangedDatePropertyName(), DATE_FORMATTER.format(changeDate));
        }
    }

    protected void setExternalManagementResourceValidationStatus(Map<String, String> properties, MetadataResourceExternalManagementProperties.ValidationStatus status) {
        if (StringUtils.hasLength(jCloudConfiguration.getExternalResourceManagementValidationStatusPropertyName())) {
            setPropertyValue(properties, jCloudConfiguration.getExternalResourceManagementValidationStatusPropertyName(), String.valueOf(status.getValue()));
        }
    }

    /**
     * Set the new version if this is a new record and if updating then bump the version up by 1.
     * @param context need to get metadata if metadata id is a working copy.
     * @param properties containing all the properties.  The version field should be in the properties map.
     * @param isNewResource flag to indicate that this is a new resource of if updating existing resource.
     * @param metadataUuid uuid of the related metadata record that contains the resource being versioned.
     * @param metadataId id of the related metadata record that contains the resource being versioned.
     * @param visibility of the resource being versioned.
     * @param approved status of the approved record.
     * @param filename or resource of the resource being versioned.
     * @throws Exception if there are errors.
     */
    protected void setPropertiesVersion(final ServiceContext context, final Map<String, String> properties, boolean isNewResource, String metadataUuid, int metadataId,
                                        final MetadataResourceVisibility visibility, final Boolean approved, final String filename) throws Exception {
        if (StringUtils.hasLength(jCloudConfiguration.getExternalResourceManagementVersionPropertyName())) {
            String versionPropertyName = jCloudConfiguration.getExternalResourceManagementVersionPropertyName();

            final int approvedMetadataId = Boolean.TRUE.equals(approved) ? metadataId : canEdit(context, metadataUuid, true);
            // if the current record id equal to the approved record id then it has not been approved and is a draft otherwise we are editing a working copy
            final boolean draft = (metadataId == approvedMetadataId);

            String newVersionLabel = null;
            if (!isNewResource && !draft &&
                (jCloudConfiguration.getVersioningStrategy().equals(JCloudConfiguration.VersioningStrategy.DRAFT) ||
                jCloudConfiguration.getVersioningStrategy().equals(JCloudConfiguration.VersioningStrategy.APPROVED))) {
                String approveKey = getKey(context, metadataUuid, approvedMetadataId, visibility, filename);

                try {
                    StorageMetadata storageMetadata = jCloudConfiguration.getClient().getBlobStore().blobMetadata(jCloudConfiguration.getContainerName(), approveKey);
                    if (storageMetadata != null) {
                        if (storageMetadata.getUserMetadata().containsKey(versionPropertyName)) {
                            newVersionLabel = bumpVersion(storageMetadata.getUserMetadata().get(versionPropertyName));
                        }
                    }
                } catch (ContainerNotFoundException ignored) {
                    // ignored
                }
                if (newVersionLabel == null) {
                    newVersionLabel = FIRST_VERSION;
                }
            }

            if (properties.containsKey(versionPropertyName)) {
                if (isNewResource) {
                    throw new RuntimeException(String.format("Found property '%s' while adding new resource '%s' for metadata %d(%s).  This is unexpected.",
                        versionPropertyName, filename, metadataId, metadataUuid));
                }
                if (newVersionLabel == null) {
                    if (jCloudConfiguration.getVersioningStrategy().equals(JCloudConfiguration.VersioningStrategy.DRAFT) ||
                        jCloudConfiguration.getVersioningStrategy().equals(JCloudConfiguration.VersioningStrategy.ALL)) {
                        newVersionLabel = bumpVersion(properties.get(versionPropertyName));
                    } else {
                        newVersionLabel = properties.get(versionPropertyName);
                    }
                }
            } else {
                if (!isNewResource) {
                    // If the version was not found then it means that it will be starting from version 1 when there could be previous versions.
                    // This could be a data problem and should be investigated.
                    Log.error(Geonet.RESOURCES,
                        String.format("Expecting property '%s' while modifying existing resource '%s' for metadata %d(%s) but the property was not found. Version being set to '%s'",
                            versionPropertyName, filename, metadataId, metadataUuid, FIRST_VERSION));
                }
                newVersionLabel = FIRST_VERSION;
            }

            setPropertyValue(properties, versionPropertyName, newVersionLabel);
        }
    }

    /**
     * Bump the version string up one version.
     * @param currentVersionLabel to be increased
     * @return new version label
     */
    protected String bumpVersion(String currentVersionLabel) {
        int majorVersion = Integer.parseInt(currentVersionLabel);
        majorVersion++;
        return String.valueOf(majorVersion);
    }

    protected void setPropertyValue(Map<String, String> properties, String propertyName, String value) {
        if (StringUtils.hasLength(propertyName)) {
            properties.put(propertyName, value);
        }
    }
    @Override
    public MetadataResource patchResourceStatus(final ServiceContext context, final String metadataUuid, final String resourceId,
                                                final MetadataResourceVisibility visibility, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);

        String sourceKey = null;
        StorageMetadata storageMetadata;
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
                        return createResourceDescription(context, metadataUuid, visibility, resourceId, storageMetadata, metadataId, approved, false);
                    }
                }
            } catch (ContainerNotFoundException ignored) {
                // ignored
            }
        }
        if (sourceKey != null) {
            final String targetKey = getKey(context, metadataUuid, metadataId, visibility, resourceId);

            jCloudConfiguration.getClient().getBlobStore().copyBlob(jCloudConfiguration.getContainerName(), sourceKey, jCloudConfiguration.getContainerName(), targetKey, CopyOptions.NONE);
            jCloudConfiguration.getClient().getBlobStore().removeBlob(jCloudConfiguration.getContainerName(), sourceKey);

            Blob blobResults = jCloudConfiguration.getClient().getBlobStore().getBlob(jCloudConfiguration.getContainerName(), targetKey);

            return createResourceDescription(context, metadataUuid, visibility, resourceId, blobResults.getMetadata(), metadataId, approved, false);
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
        Log.info(Geonet.RESOURCES,
                String.format("Unable to remove resource '%s'.", resourceId));
        return String.format("Unable to remove resource '%s'.", resourceId);
    }

    @Override
    public String delResource(final ServiceContext context, final String metadataUuid, final MetadataResourceVisibility visibility,
                              final String resourceId, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        if (tryDelResource(context, metadataUuid, metadataId, visibility, resourceId)) {
            return String.format("Metadata resource '%s' removed.", resourceId);
        }
        Log.info(Geonet.RESOURCES,
                String.format("Unable to remove resource '%s'.", resourceId));
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
    public void copyResources(ServiceContext context, String sourceUuid, String targetUuid, MetadataResourceVisibility metadataResourceVisibility, boolean sourceApproved, boolean targetApproved) throws Exception {
        final int sourceMetadataId = canEdit(context, sourceUuid, metadataResourceVisibility, sourceApproved);
        final int targetMetadataId = canEdit(context, targetUuid, metadataResourceVisibility, targetApproved);
        final String sourceResourceTypeDir = getMetadataDir(context, sourceMetadataId) + jCloudConfiguration.getFolderDelimiter() + metadataResourceVisibility + jCloudConfiguration.getFolderDelimiter();
        final String targetResourceTypeDir = getMetadataDir(context, targetMetadataId) + jCloudConfiguration.getFolderDelimiter() + metadataResourceVisibility + jCloudConfiguration.getFolderDelimiter();

        Log.debug(Geonet.RESOURCES, String.format("Copying resources from '%s' (approved=%s) to '%s' (approved=%s)",
            sourceResourceTypeDir, sourceApproved, targetResourceTypeDir, targetApproved));

        String versionPropertyName = null;
        if (jCloudConfiguration.isVersioningEnabled()) {
            versionPropertyName = jCloudConfiguration.getExternalResourceManagementVersionPropertyName();
        }

        try {
            ListContainerOptions opts = new ListContainerOptions();
            opts.prefix(sourceResourceTypeDir).recursive();

            // Page through the data
            String marker = null;
            do {
                if (marker != null) {
                    opts.afterMarker(marker);
                }

                PageSet<? extends StorageMetadata> page = jCloudConfiguration.getClient().getBlobStore().list(jCloudConfiguration.getContainerName(), opts);

                for (StorageMetadata sourceStorageMetadata : page) {
                    if (!isFolder(sourceStorageMetadata)) {
                        String sourceBlobName = sourceStorageMetadata.getName();
                        String targetBlobName = targetResourceTypeDir + sourceBlobName.substring(sourceResourceTypeDir.length());

                        BlobMetadata blobMetadata = jCloudConfiguration.getClient().getBlobStore().blobMetadata(jCloudConfiguration.getContainerName(), sourceBlobName);

                        // Copy existing properties.
                        Map<String, String> targetProperties = new HashMap<>(blobMetadata.getUserMetadata());

                        setMetadataUUID(targetProperties, targetUuid);

                        // Check if target exists.
                        StorageMetadata targetStorageMetadata = null;

                        try {
                            targetStorageMetadata = jCloudConfiguration.getClient().getBlobStore().blobMetadata(jCloudConfiguration.getContainerName(), targetBlobName);

                        } catch (ContainerNotFoundException ignored) {
                            // ignored
                        }

                        Log.debug(Geonet.RESOURCES, String.format("Copying resource from '%s' to '%s' (new=%s)", sourceBlobName, targetBlobName, targetStorageMetadata==null));

                        if (jCloudConfiguration.isVersioningEnabled() && StringUtils.hasLength(versionPropertyName)) {
                            if (targetStorageMetadata != null &&
                                targetProperties.containsKey(versionPropertyName) &&
                                targetStorageMetadata.getUserMetadata().containsKey(versionPropertyName) &&
                                !targetProperties.get(versionPropertyName).equals(targetStorageMetadata.getUserMetadata().get(versionPropertyName))) {

                                String targetVersionCurrentLabel;
                                if (jCloudConfiguration.getVersioningStrategy().equals(JCloudConfiguration.VersioningStrategy.DRAFT) ||
                                    jCloudConfiguration.getVersioningStrategy().equals(JCloudConfiguration.VersioningStrategy.APPROVED)) {
                                    // If draft or approved, then we only bump the target version up by one version only.
                                    targetVersionCurrentLabel = targetStorageMetadata.getUserMetadata().get(versionPropertyName);
                                    if (StringUtils.hasLength(targetVersionCurrentLabel)) {
                                        targetVersionCurrentLabel = bumpVersion(targetVersionCurrentLabel);
                                    } else {
                                        targetVersionCurrentLabel = FIRST_VERSION;
                                        // Log warning as this could be an issue if the version property is being lost.
                                        Log.warning(Geonet.RESOURCES, String.format("Target version for resource '%s' was empty. Setting version to '%s'", targetBlobName, targetVersionCurrentLabel));
                                    }
                                } else {
                                    // If versioning all then we will use the current version.
                                    targetVersionCurrentLabel = targetProperties.get(versionPropertyName);
                                    Log.debug(Geonet.RESOURCES, String.format("Keeping version '%s' for source for resource '%s'", targetVersionCurrentLabel, targetBlobName));
                                    if (!StringUtils.hasLength(targetVersionCurrentLabel)) {
                                        targetVersionCurrentLabel = FIRST_VERSION;
                                        // Log warning as this could be an issue if the version property is being lost.
                                        Log.warning(Geonet.RESOURCES, String.format("Version resource '%s' was empty. Setting version to '%s'", targetBlobName, targetVersionCurrentLabel));
                                    }
                                }
                                targetProperties.put(versionPropertyName, targetVersionCurrentLabel);
                            } else if (targetApproved && (targetStorageMetadata == null || !targetStorageMetadata.getUserMetadata().containsKey(versionPropertyName))) {
                                // If the targetApproved is true then it is a new draft so if target resource did not exist
                                // then this will be added as a first version item. Otherwise, we keep the version unchanged from the approved copy.
                                targetProperties.put(versionPropertyName, FIRST_VERSION);
                            }

                            // If version is still not set then lets set it.
                            if (!targetProperties.containsKey(versionPropertyName) || !StringUtils.hasLength(targetProperties.get(versionPropertyName))) {
                                targetProperties.put(versionPropertyName, FIRST_VERSION);
                                // There seems to have been an issue detecting the version so log a warning
                                Log.warning(Geonet.RESOURCES, String.format("Version was not set for resource '%s'. Setting version to '%s'", targetBlobName,
                                    targetProperties.get(versionPropertyName)));
                            }
                        }

                        // Use the copyBlob to copy the resource with updated metadata.
                        jCloudConfiguration.getClient().getBlobStore().copyBlob(
                            jCloudConfiguration.getContainerName(),
                            sourceBlobName,
                            jCloudConfiguration.getContainerName(),
                            targetBlobName,
                            CopyOptions.builder().userMetadata(targetProperties).build());
                    }
                }
                marker = page.getNextMarker();
            } while (marker != null);
        } catch (ContainerNotFoundException e) {
            Log.warning(Geonet.RESOURCES,
                String.format("Unable to located metadata '%s' directory to be copied.", sourceMetadataId));
        }
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
                return createResourceDescription(context, metadataUuid, visibility, filename, metadata, metadataId, approved, false);
            }
        } catch (ContainerNotFoundException e) {
            return null;
        }
    }

    @Override
    public MetadataResourceContainer getResourceContainerDescription(final ServiceContext context, final String metadataUuid, Boolean approved) throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);

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
        // For windows, it may be "\" in which case we need to change it to folderDelimiter which is normally "/"
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
        if (this.baseMetadataDir == null) {
            Path systemFullDir = getDataDirectory(context).getSystemDataDir();

            // If the metadata full dir is relative from the system dir then use system dir as the base dir.
            if (metadataFullDir.toString().startsWith(systemFullDir.toString())) {
                this.baseMetadataDir = systemFullDir;
            } else {
                // If the metadata full dir is an absolute folder then use that as the base dir.
                if (getDataDirectory(context).getMetadataDataDir().isAbsolute()) {
                    this.baseMetadataDir = metadataFullDir.getRoot();
                } else {
                    // use it as a relative url.
                    this.baseMetadataDir = Paths.get(".");
                }
            }
        }
        return this.baseMetadataDir;
    }

    private GeonetworkDataDirectory getDataDirectory(ServiceContext context) {
        return ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class);
    }

    private boolean isFolder(StorageMetadata storageMetadata) {
        // For azure Blob ADSL if the type is folder then the storage type will be BLOB and hdi_isfolder=true so we cannot only rely on StorageType.FOLDER
        return storageMetadata.getType().equals(StorageType.FOLDER) || "true".equals(storageMetadata.getUserMetadata().get(AZURE_BLOB_IS_FOLDER_PROPERTY_NAME));
    }

    /**
     * Creates external resource management properties for the specified resource.
     *
     * <p>This method generates an object ID and constructs an external management URL
     * for a metadata resource, encapsulating them in a {@link MetadataResourceExternalManagementProperties} object.</p>
     *
     * @param context the service context providing access to application services
     * @param metadataId the unique identifier of the metadata record
     * @param metadataUuid the UUID of the metadata record
     * @param visibility the visibility level of the resource (e.g., public, private)
     * @param resourceId the unique identifier of the resource
     * @param filename the name of the file resource (may be null)
     * @param version the version identifier of the resource (may be null)
     * @param type the storage type (FOLDER or document); null defaults to document
     * @param validationStatus the validation status of the resource
     * @return a new {@link MetadataResourceExternalManagementProperties} instance containing
     *         the object ID, URL, and validation status
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
        String objectId = getResourceManagementExternalPropertiesObjectId((type == null ? "document" : (StorageType.FOLDER.equals(type) ? "folder" : "document")), visibility, metadataId, version,
            resourceId);
        String url = buildExternalManagementUrl(context, metadataId, metadataUuid, visibility, resourceId, filename, version, type);
        return new MetadataResourceExternalManagementProperties(objectId, url, validationStatus);
    }

    /**
     * Creates indexed external resource management properties for the specified resource.
     *
     * <p>This method extends the functionality of {@link #getMetadataResourceExternalManagementProperties}
     * by including additional custom properties that can be indexed. It generates an object ID
     * and constructs an external management URL for a metadata resource, along with custom
     * metadata properties.</p>
     *
     * @param context the service context providing access to application services
     * @param metadataId the unique identifier of the metadata record
     * @param metadataUuid the UUID of the metadata record
     * @param visibility the visibility level of the resource (e.g., public, private)
     * @param resourceId the unique identifier of the resource
     * @param filename the name of the file resource (may be null)
     * @param version the version identifier of the resource (may be null)
     * @param type the storage type (FOLDER or document); null defaults to document
     * @param validationStatus the validation status of the resource
     * @param additionalProperties a map of custom properties to be included with the resource metadata
     * @return a new {@link IndexedMetadataResourceExternalManagementProperties} instance containing
     *         the object ID, URL, validation status, and additional indexed properties
     */
    private IndexedMetadataResourceExternalManagementProperties getIndexedMetadataResourceExternalManagementProperties(ServiceContext context,
                                                                                                         int metadataId,
                                                                                                         final String metadataUuid,
                                                                                                         final MetadataResourceVisibility visibility,
                                                                                                         final String resourceId,
                                                                                                         String filename,
                                                                                                         String version,
                                                                                                         StorageType type,
                                                                                                         MetadataResourceExternalManagementProperties.ValidationStatus validationStatus,
                                                                                                         Map<String, Object> additionalProperties
    ) {
        String objectId = getResourceManagementExternalPropertiesObjectId((type == null ? "document" : (StorageType.FOLDER.equals(type) ? "folder" : "document")), visibility, metadataId, version,
            resourceId);
        String url = buildExternalManagementUrl(context, metadataId, metadataUuid, visibility, resourceId, filename, version, type);
        return new IndexedMetadataResourceExternalManagementProperties(objectId, url, validationStatus, additionalProperties);
    }

    /**
     * Builds the external management URL by replacing template placeholders with actual values.
     *
     * <p>This method constructs a URL from a configured template by performing token substitution
     * for various metadata and resource properties. If no external resource management URL is
     * configured, an empty string is returned.</p>
     *
     * <h3>Supported Placeholders:</h3>
     * <ul>
     *   <li><strong>{objectid}</strong> - Base64-encoded string in format: {@code type:visibility:metadataId:version:resourceId}
     *       <br>Examples:
     *       <ul>
     *         <li>{@code folder::100::100} - Folder in resource 100</li>
     *         <li>{@code document:public:100:v1:sample.jpg} - Public document 100, version v1, name sample.jpg</li>
     *       </ul>
     *   </li>
     *   <li><strong>{id}</strong> - The resource identifier</li>
     *   <li><strong>{type}</strong> - Resource type: "folder" for folders, "document" for documents</li>
     *   <li><strong>{uuid}</strong> - The metadata UUID</li>
     *   <li><strong>{metadataid}</strong> - The numeric metadata identifier</li>
     *   <li><strong>{visibility}</strong> - The resource visibility level (lowercase)</li>
     *   <li><strong>{filename}</strong> - The resource filename</li>
     *   <li><strong>{version}</strong> - The resource version identifier</li>
     *   <li><strong>{lang}</strong> - ISO 639-1 two-character language code</li>
     *   <li><strong>{iso3lang}</strong> - ISO 639-2/T three-character language code</li>
     * </ul>
     *
     * <h3>Example URL Templates:</h3>
     * <pre>
     * http://localhost:8080/artifact?filename={filename}&amp;version={version}&amp;lang={lang}
     * https://example.com/resources/{uuid}/{id}?type={type}&amp;visibility={visibility}
     * </pre>
     *
     * @param context the service context providing access to application services and language settings
     * @param metadataId the unique identifier of the metadata record
     * @param metadataUuid the UUID of the metadata record
     * @param visibility the visibility level of the resource (e.g., public, private)
     * @param resourceId the unique identifier of the resource
     * @param filename the name of the file resource (may be null, replaced with empty string)
     * @param version the version identifier of the resource (may be null, replaced with empty string)
     * @param type the storage type (FOLDER or document); null defaults to document
     * @return the constructed URL with all placeholders replaced, or an empty string if no URL template is configured
     */
    private String buildExternalManagementUrl(ServiceContext context,
                                             int metadataId,
                                             final String metadataUuid,
                                             final MetadataResourceVisibility visibility,
                                             final String resourceId,
                                             String filename,
                                             String version,
                                             StorageType type) {
        String url = jCloudConfiguration.getExternalResourceManagementUrl();

        if (!StringUtils.hasLength(url)) {
            return url;
        }

        String typeString = type == null ? "document" : (StorageType.FOLDER.equals(type) ? "folder" : "document");
        String objectId = getResourceManagementExternalPropertiesObjectId((typeString), visibility, metadataId, version,
            resourceId);

        // {objectid}  objectId // It will be the type:visibility:metadataId:version:resourceId in base64
        // i.e. folder::100::100                     # Folder in resource 100
        // i.e. document:public:100:v1:sample.jpg    # public document 100 version v1 name sample.jpg
        if (url.contains("{objectid}")) {
            url = url.replaceAll("(\\{objectid\\})", objectId);
        }
        // {id}  id
        if (url.contains("{id}")) {
            url = url.replaceAll("(\\{id\\})", resourceId);
        }
        // {type} // If the type is folder then type "folder" will be displayed else if document then "document" will be displayed
        if (url.contains("{type}")) {
            url = url.replaceAll("(\\{type\\})", typeString);
        }
        // {uuid}  metadata uuid
        if (url.contains("{uuid}")) {
            url = url.replaceAll("(\\{uuid\\})", (metadataUuid == null ? "" : metadataUuid));
        }
        // {metadataid}  metadataId
        if (url.contains("{metadataid}")) {
            url = url.replaceAll("(\\{metadataid\\})", String.valueOf(metadataId));
        }
        //    {visibility}  visibility
        if (url.contains("{visibility}")) {
            url = url.replaceAll("(\\{visibility\\})", (visibility == null ? "" : visibility.toString().toLowerCase()));
        }
        //    {filename}  filename
        if (url.contains("{filename}")) {
            url = url.replaceAll("(\\{filename\\})", (filename == null ? "" : filename));
        }
        // {version}  version
        if (url.contains("{version}")) {
            url = url.replaceAll("(\\{version\\})", (version == null ? "" : version));
        }

        if (url.contains("{lang}") || url.contains("{ISO3lang}")) {
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
            if (url.contains("{lang}")) {
                url = url.replaceAll("(\\{lang\\})", lang);
            }
            // {iso3lang}  ISO 639-2/T language
            if (url.contains("{iso3lang}")) {
                url = url.replaceAll("(\\{iso3lang\\})", iso3Lang);
            }
        }

        return url;
    }

    public ResourceManagementExternalProperties getResourceManagementExternalProperties() {
        return new ResourceManagementExternalProperties() {
            @Override
            public boolean isEnabled() {
                // Return true if we have an external management url
                return StringUtils.hasLength(jCloudConfiguration.getExternalResourceManagementUrl());
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

    protected static class JCloudResourceHolder implements ResourceHolder {
        private final InputStreamResource resource;
        private final MetadataResource metadata;
        private final InputStream inputStream;

        public JCloudResourceHolder(final Blob object, MetadataResource metadata) throws IOException {
            this.metadata = metadata;
            this.inputStream = object.getPayload().openStream();
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
