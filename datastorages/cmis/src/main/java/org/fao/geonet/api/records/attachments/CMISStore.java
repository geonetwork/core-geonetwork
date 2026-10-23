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


import jeeves.server.context.ServiceContext;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.commons.collections.MapUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.exception.NotAllowedException;
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
import org.fao.geonet.resources.CMISConfiguration;
import org.fao.geonet.resources.CMISUtils;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Visibility (public/private) is tracked in the database ({@code MetadataFileUploads.resourceaccess},
 * populated by {@link ResourceLoggerStore}), not by which CMIS folder a document is filed under -
 * the {@code <visibility>} subfolder below is the legacy layout, kept only as a read/write
 * fallback for documents that predate this and haven't been touched since (every put, rename, or
 * visibility change migrates a document to the flat folder).
 */
public class CMISStore extends AbstractStore {

    private Path baseMetadataDir = null;

    private static final String CMIS_PROPERTY_PREFIX = "cmis:";

    @Autowired
    CMISConfiguration cmisConfiguration;

    @Autowired
    CMISUtils cmisUtils;

    @Autowired
    SettingManager settingManager;

    @Override
    public List<MetadataResource> getResources(final ServiceContext context, final String metadataUuid,
                                               final MetadataResourceVisibility visibility, String filter, Boolean approved, boolean includeAdditionalIndexedProperties) throws Exception {
        final int metadataId = canDownload(context, metadataUuid, visibility, approved);

        final String metadataDir = getMetadataDir(context, metadataId);
        final String delimiter = cmisConfiguration.getFolderDelimiter();

        List<MetadataResource> resourceList = new ArrayList<>();
        if (filter == null) {
            filter = FilesystemStore.DEFAULT_FILTER;
        }

        PathMatcher matcher =
                FileSystems.getDefault().getPathMatcher("glob:" + filter);

        try {
            // A single listing under the whole metadata folder returns both legacy
            // (<visibility>/...) and flat documents together; classify each by which layout
            // it's in rather than resolving a separate folder per layout.
            Folder parentFolder = cmisUtils.getFolderCache(metadataDir);

            OperationContext oc = cmisUtils.createOperationContext();
            if (cmisConfiguration.existExternalResourceManagementValidationStatusSecondaryProperty()) {
                // Reset Filter from the default operationalContext to include all fields because we may need secondary properties.
                oc.setFilter(null);
            }

            Map<String, Document> documentMap = cmisUtils.getCmisObjectMap(parentFolder, null, oc);
            Map<String, MetadataResourceVisibility> trackedAccess = loadTrackedAccessByFilename(metadataId);
            for (Map.Entry<String, Document> entry : documentMap.entrySet()) {
                Document object = entry.getValue();
                String cmisFilePath = entry.getKey();
                // Only add to the list if it is a document and it matches the filter.
                if (!(object instanceof Document)) {
                    continue;
                }
                // cmisFilePath is relative to the metadata folder and always starts with the
                // folder delimiter (eg. "/file.png", or "/sub/file.png" for a nested-path
                // resource); keep the whole relative path - not just the last segment - so
                // subfolder structure is preserved.
                final String relativeKey = cmisFilePath.startsWith(delimiter)
                    ? cmisFilePath.substring(delimiter.length()) : cmisFilePath;

                final MetadataResourceVisibility legacyVisibility = legacyVisibilityOf(relativeKey, delimiter);
                final String filename;
                final MetadataResourceVisibility resourceVisibility;
                if (legacyVisibility != null) {
                    // Legacy layout: membership under the old <visibility>/ folder is the visibility.
                    filename = relativeKey.substring(legacyVisibility.toString().length() + delimiter.length());
                    resourceVisibility = legacyVisibility;
                } else {
                    // Flat layout: only a tracked-access match counts, since folder membership
                    // alone no longer implies visibility.
                    filename = relativeKey;
                    resourceVisibility = trackedAccess.get(filename);
                }
                if (resourceVisibility != visibility) {
                    continue;
                }

                Path keyPath = new File(filename).toPath().getFileName();
                if (matcher.matches(keyPath)) {
                    resourceList.add(createResourceDescription(context, metadataUuid, visibility, filename, object, metadataId, approved));
                }
            }
        } catch (CmisObjectNotFoundException | ResourceNotFoundException e) {
            // ignore as it means that there is no data to list.
        }


        resourceList.sort(MetadataResourceVisibility.sortByFileName);

        return resourceList;
    }

    /**
     * Whether a metadata-folder-relative key falls under one of the legacy {@code public}/
     * {@code private} folders, and if so which. A nested-path resource whose own first segment
     * happens to be literally "public" or "private" is indistinguishable from this - a narrow,
     * pre-existing ambiguity of keeping both layouts side by side.
     */
    private static MetadataResourceVisibility legacyVisibilityOf(String relativeKey, String delimiter) {
        for (MetadataResourceVisibility v : MetadataResourceVisibility.values()) {
            if (relativeKey.startsWith(v.toString() + delimiter)) {
                return v;
            }
        }
        return null;
    }

    protected MetadataResource createResourceDescription(final ServiceContext context, final String metadataUuid,
                                                       final MetadataResourceVisibility visibility, final String resourceId,
                                                       Document document, int metadataId, boolean approved) {

        String filename = getFilename(metadataUuid, resourceId);

        String versionValue = null;
        if (cmisConfiguration.isVersioningEnabled()) {
            versionValue = document.getVersionLabel();
        }

        MetadataResourceExternalManagementProperties.ValidationStatus validationStatus = MetadataResourceExternalManagementProperties.ValidationStatus.UNKNOWN;
        if (!StringUtils.isEmpty(cmisConfiguration.getExternalResourceManagementValidationStatusPropertyName())) {
            Object propertyValue = null;
            if (cmisConfiguration.existExternalResourceManagementValidationStatusSecondaryProperty()) {
                propertyValue = getSecondaryProperty(document, cmisConfiguration.getExternalResourceManagementValidationStatusPropertyName());
            } else {
                Property property = document.getProperty(cmisConfiguration.getExternalResourceManagementValidationStatusPropertyName());
                if (property != null) {
                    propertyValue = property.getValue();
                }
            }
            if (propertyValue != null) {
                int propertyInt;
                // If the fields is a string field then try to convert it to a integer
                if (propertyValue instanceof String) {
                    propertyInt = Integer.valueOf((String) (propertyValue));
                } else {
                    propertyInt = ((Number)propertyValue).intValue();
                }

                validationStatus = MetadataResourceExternalManagementProperties.ValidationStatus.fromValue(propertyInt);
            }
        }

        MetadataResourceExternalManagementProperties metadataResourceExternalManagementProperties =
            getMetadataResourceExternalManagementProperties(context, metadataId, metadataUuid, visibility, resourceId, filename, document.getVersionLabel(), document.getVersionSeriesId(), document.getType(), validationStatus);

        String mimeType = document.getContentStreamMimeType();
        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = MimeTypeDetector.detect(filename);
        }

        return new FilesystemStoreResource(metadataUuid, metadataId, filename,
            settingManager.getNodeURL() + "api/records/", visibility, document.getContentStreamLength(), document.getLastModificationDate().getTime(), versionValue, metadataResourceExternalManagementProperties, approved, mimeType);
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
        String key = resolveExistingKey(context, metadataUuid, metadataId, visibility, getFilename(metadataUuid, resourceId), approved);
        if (key == null) {
            throw new ResourceNotFoundException(
                String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
        }
        try {
            final CmisObject object = cmisConfiguration.getClient().getObjectByPath(key);
            return new CMISResourceHolder(object, createResourceDescription(context, metadataUuid, visibility, resourceId,
                (Document) object, metadataId, approved));
        } catch (CmisObjectNotFoundException e) {
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
        String key = resolveExistingKey(context, metadataUuid, metadataId, visibility, getFilename(metadataUuid, resourceId), approved);
        if (key == null) {
            throw new ResourceNotFoundException(
                String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
        }
        try {
            final CmisObject object = cmisConfiguration.getClient().getObjectByPath(key);
            return createResourceDescription(context, metadataUuid, visibility, resourceId,
                (Document) object, metadataId, approved);
        } catch (CmisObjectNotFoundException e) {
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
        String key = resolveExistingKey(context, metadataUuid, metadataId, metadataResourceVisibility, getFilename(metadataUuid, resourceId), approved);
        if (key == null) {
            throw new ResourceNotFoundException(
                String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
        }
        try {
            final CmisObject object = cmisConfiguration.getClient().getObjectByPath(key);
            return new CMISResourceHolder(object, createResourceDescription(context, metadataUuid, metadataResourceVisibility, resourceId,
                (Document) object, metadataId, approved), start, end);
        } catch (CmisObjectNotFoundException e) {
            throw new ResourceNotFoundException(
                String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
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
            String key = resolveExistingKey(context, metadataUuid, metadataId, visibility, getFilename(metadataUuid, resourceId), approved);
            if (key == null) {
                throw new ResourceNotFoundException(
                    String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid));
            }
            final CmisObject object = cmisConfiguration.getClient().getObjectByPath(key);
            return new CMISResourceHolder(object, createResourceDescription(context, metadataUuid, visibility, resourceId,
                (Document) object, metadataId, approved));
        } catch (CmisObjectNotFoundException e) {
            throw new ResourceNotFoundException(
                String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
        }
    }

    /** The legacy, pre-flattening key: {@code <metadataDir>/<visibility>/<filename>}. */
    protected String getLegacyKey(final ServiceContext context, String metadataUuid, int metadataId, MetadataResourceVisibility visibility, String resourceId) {
        checkResourceId(resourceId);
        final String metadataDir = getMetadataDir(context, metadataId);
        return metadataDir + cmisConfiguration.getFolderDelimiter() + visibility.toString() + cmisConfiguration.getFolderDelimiter() + getFilename(metadataUuid, resourceId);
    }

    /** The flat, visibility-less key: {@code <metadataDir>/<filename>}. */
    protected String getFlatKey(final ServiceContext context, String metadataUuid, int metadataId, String resourceId) {
        checkResourceId(resourceId);
        final String metadataDir = getMetadataDir(context, metadataId);
        return metadataDir + cmisConfiguration.getFolderDelimiter() + getFilename(metadataUuid, resourceId);
    }

    /**
     * Resolve the key of an existing resource. Prefers the flat, visibility-less key and falls
     * back to the legacy {@code <visibility>/} folder for documents that predate it and haven't
     * been touched since.
     * <p>
     * Once flat, a document's key no longer enforces which visibility it may be fetched as -
     * that was the folder split's job. So a flat match is only honoured if its <em>tracked</em>
     * access agrees with {@code visibility}; a mismatch (or an untracked flat document, which
     * shouldn't happen since every write path logs a tracking row) is treated as not found at
     * this visibility, exactly as an actually-private document can't be read today by asking for
     * the public one.
     *
     * @return the resolved key, or {@code null} if not found at all, or not at this visibility.
     */
    private String resolveExistingKey(final ServiceContext context, String metadataUuid, int metadataId,
                                      MetadataResourceVisibility visibility, String filename, Boolean approved) {
        String flatKey = getFlatKey(context, metadataUuid, metadataId, filename);
        try {
            cmisConfiguration.getClient().getObjectByPath(flatKey);
            return visibility == resolveVisibility(metadataUuid, approved, filename) ? flatKey : null;
        } catch (CmisObjectNotFoundException ignored) {
            // not flat yet - try the legacy folder instead.
        }
        String legacyKey = getLegacyKey(context, metadataUuid, metadataId, visibility, filename);
        try {
            cmisConfiguration.getClient().getObjectByPath(legacyKey);
            return legacyKey;
        } catch (CmisObjectNotFoundException ignored) {
            return null;
        }
    }

    @Override
    public MetadataResource putResource(final ServiceContext context, final String metadataUuid, final String filename,
                                        final InputStream is, @Nullable final Date changeDate, final MetadataResourceVisibility visibility, Boolean approved)
            throws Exception {
        return putResource(context, metadataUuid, filename, is, changeDate, visibility, approved, null);
    }

    protected MetadataResource putResource(final ServiceContext context, final String metadataUuid, final String filename,
                                        final InputStream is, @Nullable final Date changeDate, final MetadataResourceVisibility visibility, Boolean approved, Map<String, Object> additionalProperties)
        throws Exception {
        final int metadataId = canEdit(context, metadataUuid, approved);
        String key = getFlatKey(context, metadataUuid, metadataId, filename);

        OperationContext oc = cmisUtils.createOperationContext();
        // Reset Filter from the default operationalContext to include all fields because we may need secondary properties.
        oc.setFilter(null);

        Map<String, Object> properties = new HashMap<String, Object>();
        Document doc;
        try {
            doc = (Document) cmisConfiguration.getClient().getObjectByPath(key, oc);

            // Update existing document
            setCmisProperties(metadataUuid, properties, doc, additionalProperties);
            doc = cmisUtils.saveDocument(key, doc, properties, is, changeDate);
        } catch (CmisObjectNotFoundException e) {
            // add new document
            setCmisProperties(metadataUuid, properties, null, additionalProperties);
            doc = cmisUtils.saveDocument(key, null, properties, is, changeDate);
        }

        return createResourceDescription(context, metadataUuid, visibility, filename,
            doc, metadataId, approved);
    }

    protected void setCmisProperties(String metadataUuid, Map<String, Object> properties, Document doc, Map<String, Object> additionalProperties) {

        // Add additional properties if exists.
        if (MapUtils.isNotEmpty(additionalProperties)) {
            properties.putAll(additionalProperties);
        }

        // now update metadata uuid and status within primary cmis fields if needed.

        // Don't allow users metadata uuid to be supplied as a property so let's overwrite any value that may exist.
        if (!StringUtils.isEmpty(cmisConfiguration.getCmisMetadataUUIDPropertyName())) {
            setCmisMetadataUUIDPrimary(properties, metadataUuid);
        }
        // If document is empty it is a new record so set the default status value property if it does not already exist as an additional property.
        if (doc == null &&
            !StringUtils.isEmpty(cmisConfiguration.getExternalResourceManagementValidationStatusPropertyName()) &&
            !properties.containsKey(cmisConfiguration.getExternalResourceManagementValidationStatusPropertyName())) {
            setCmisExternalManagementResourceStatusPrimary(properties, cmisConfiguration.getValidationStatusDefaultValue());
        }

        // If we have secondary properties then lets apply those changes as well.
        if (cmisConfiguration.existSecondaryProperty()) {
            Property secondaryProperties = null;
            if (doc != null) {
                secondaryProperties = doc.getProperty(PropertyIds.SECONDARY_OBJECT_TYPE_IDS);
            }

            // Don't allow users metadata uuid to be supplied as a property so let's overwrite any value that may exist.
            if (cmisConfiguration.existMetadataUUIDSecondaryProperty()) {
                setCmisMetadataUUIDSecondary(secondaryProperties, properties, metadataUuid);
            }
            // If document is empty it is a new record so set the default status value property if it does not already exist as an additional secondary property.
            if (doc == null &&
                cmisConfiguration.existExternalResourceManagementValidationStatusSecondaryProperty() &&
                !properties.containsKey(cmisConfiguration.getExternalResourceManagementValidationStatusPropertyName().split(CMISConfiguration.CMIS_SECONDARY_PROPERTY_SEPARATOR)[1])) {
                setCmisExternalManagementResourceStatusSecondary(secondaryProperties, properties, cmisConfiguration.getValidationStatusDefaultValue());
            }
        }

    }
    protected void setCmisMetadataUUIDPrimary(Map<String, Object> properties, String metadataUuid) {
        setCmisPrimaryProperty(properties, cmisConfiguration.getCmisMetadataUUIDPropertyName(), metadataUuid);
    }

    protected void setCmisExternalManagementResourceStatusPrimary(Map<String, Object> properties, MetadataResourceExternalManagementProperties.ValidationStatus status) {
        setCmisPrimaryProperty(properties, cmisConfiguration.getExternalResourceManagementValidationStatusPropertyName(), status.getValue());
    }

    protected void setCmisPrimaryProperty(Map<String, Object> properties, String propertyName, Object value) {
        if (!StringUtils.isEmpty(propertyName) &&
            !propertyName.contains(cmisConfiguration.getSecondaryPropertySeparator())) {
            properties.put(propertyName, value);
        }
    }

    protected void setCmisExternalManagementResourceStatusSecondary(Property secondaryProperty, Map<String, Object> properties, MetadataResourceExternalManagementProperties.ValidationStatus status) {
        setCmisSecondaryProperty(secondaryProperty, properties, cmisConfiguration.getExternalResourceManagementValidationStatusPropertyName(), status.getValue());
    }

    protected void setCmisMetadataUUIDSecondary(Property secondaryProperty, Map<String, Object> properties, String metadataUuid) {
        setCmisSecondaryProperty(secondaryProperty, properties, cmisConfiguration.getCmisMetadataUUIDPropertyName(), metadataUuid);
    }

    protected void setCmisSecondaryProperty(Property secondaryProperty, Map<String, Object> properties, String propertyName, Object value) {
        if (!StringUtils.isEmpty(propertyName) &&
            propertyName.contains(cmisConfiguration.getSecondaryPropertySeparator())) {
            String[] splitPropertyNames = propertyName.split(Pattern.quote(cmisConfiguration.getSecondaryPropertySeparator()));
            String aspectName = splitPropertyNames[0];
            String secondaryPropertyName = splitPropertyNames[1];
            List<Object> aspects = null;
            if (secondaryProperty != null) {
                // It may return an unmodifiable list and we need to potentially modify the list so lets make a copy of the list.
                aspects = new ArrayList<>(secondaryProperty.getValues());
            }
            if (aspects == null) {
                aspects = new ArrayList<>();
            }
            if (!aspects.contains(aspectName)) {
                aspects.add(aspectName);
            }

            properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, aspects);
            properties.put(secondaryPropertyName, value);
        }
    }

    @Override
    public MetadataResource patchResourceStatus(final ServiceContext context, final String metadataUuid, final String resourceId,
                                                final MetadataResourceVisibility visibility, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        String filename = getFilename(metadataUuid, resourceId);
        String flatKey = getFlatKey(context, metadataUuid, metadataId, filename);

        // Don't use caching for this process.
        OperationContext oc = cmisUtils.createOperationContext();
        oc.setCacheEnabled(false);

        try {
            final CmisObject object = cmisConfiguration.getClient().getObjectByPath(flatKey, oc);
            // Already flat: visibility is purely a database attribute there, updated by
            // ResourceLoggerStore regardless of what happens here - no CMIS move needed.
            return createResourceDescription(context, metadataUuid, visibility, resourceId, (Document) object, metadataId, approved);
        } catch (CmisObjectNotFoundException ignored) {
            // Not flat yet - look for it under a legacy visibility folder instead.
        }

        for (MetadataResourceVisibility sourceVisibility : visibilityCandidates(metadataUuid, approved, resourceId)) {
            final String legacyKey = getLegacyKey(context, metadataUuid, metadataId, sourceVisibility, resourceId);
            CmisObject sourceObject;
            try {
                sourceObject = cmisConfiguration.getClient().getObjectByPath(legacyKey, oc);
            } catch (CmisObjectNotFoundException ignored) {
                continue;
            }

            // Get the parent source folder.
            int lastFolderDelimiterSourceKeyIndex = legacyKey.lastIndexOf(cmisConfiguration.getFolderDelimiter());
            String parentSourceKey = legacyKey.substring(0, lastFolderDelimiterSourceKeyIndex);
            Folder parentSourceFolder = cmisUtils.getFolderCache(parentSourceKey);

            // The flat destination's parent is simply the metadata folder itself.
            Folder parentDestFolder = cmisUtils.getFolderCache(getMetadataDir(context, metadataId), true, true);

            // Migrate to the flat layout as a side effect of this move, same as put/rename.
            CmisObject object;
            try {
                object = ((Document) sourceObject).move(parentSourceFolder, parentDestFolder, oc);
                Log.info(Geonet.RESOURCES,
                    String.format("moved resource '%s' to '%s'.", parentSourceFolder.getPaths().get(0), parentDestFolder.getPaths().get(0)));
            } catch (CmisPermissionDeniedException e) {
                Log.warning(Geonet.RESOURCES, String.format(
                        "No permissions to modify metadata resource '%s' for metadata '%s'.", resourceId, metadataUuid));
                throw new NotAllowedException(String.format(
                        "No permissions to modify metadata resource '%s' for metadata '%s'.", resourceId, metadataUuid));
            }

            return createResourceDescription(context, metadataUuid, visibility, resourceId, (Document) object, metadataId, approved);
        }

        Log.warning(Geonet.RESOURCES,
                String.format("Could not update permissions. Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid));
        throw new ResourceNotFoundException(
                String.format("Could not update permissions. Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid));
    }

    @Override
    public void migrateResourceToFlatLayout(ServiceContext context, MetadataResource resource) throws Exception {
        int metadataId = resource.getMetadataId();
        String metadataUuid = resource.getMetadataUuid();
        String filename = resource.getFilename();

        // Don't use caching for this process.
        OperationContext oc = cmisUtils.createOperationContext();
        oc.setCacheEnabled(false);

        String flatKey = getFlatKey(context, metadataUuid, metadataId, filename);
        try {
            cmisConfiguration.getClient().getObjectByPath(flatKey, oc);
            // A flat document already occupies this name (eg. re-uploaded after the legacy copy
            // was orphaned, or the same filename also exists under the other legacy visibility
            // folder). Leave the legacy copy in place rather than overwrite or lose data.
            return;
        } catch (CmisObjectNotFoundException ignored) {
            // Nothing flat yet - proceed with the move.
        }

        String legacyKey = getLegacyKey(context, metadataUuid, metadataId, resource.getVisibility(), filename);
        CmisObject sourceObject;
        try {
            sourceObject = cmisConfiguration.getClient().getObjectByPath(legacyKey, oc);
        } catch (CmisObjectNotFoundException ignored) {
            // Already flat, or this particular resource isn't a legacy one.
            return;
        }

        int lastFolderDelimiterSourceKeyIndex = legacyKey.lastIndexOf(cmisConfiguration.getFolderDelimiter());
        String parentSourceKey = legacyKey.substring(0, lastFolderDelimiterSourceKeyIndex);
        Folder parentSourceFolder = cmisUtils.getFolderCache(parentSourceKey);
        Folder parentDestFolder = cmisUtils.getFolderCache(getMetadataDir(context, metadataId), true, true);

        try {
            ((Document) sourceObject).move(parentSourceFolder, parentDestFolder, oc);
            cmisUtils.invalidateFolderCache(parentSourceKey);
        } catch (CmisPermissionDeniedException | CmisConstraintException e) {
            Log.warning(Geonet.RESOURCES, String.format(
                "Unable to migrate legacy resource '%s' for metadata %d (%s) to the flat layout: %s",
                filename, metadataId, metadataUuid, e.getMessage()));
        }
    }

    @Override
    public void deleteLegacyVisibilityFolderIfEmpty(ServiceContext context, int metadataId, MetadataResourceVisibility visibility)
            throws Exception {
        String legacyFolderKey = getMetadataDir(context, metadataId) + cmisConfiguration.getFolderDelimiter() + visibility.toString();
        Folder legacyFolder;
        try {
            legacyFolder = cmisUtils.getFolderCache(legacyFolderKey, true);
        } catch (ResourceNotFoundException e) {
            // No legacy folder for this visibility - nothing to do.
            return;
        }

        OperationContext oc = cmisUtils.createOperationContext();
        oc.setCacheEnabled(false);
        if (legacyFolder.getChildren(oc).iterator().hasNext()) {
            // Not empty - a resource wasn't migrated (eg. a same-named flat document already
            // existed) or a nested subfolder is still there.
            return;
        }

        try {
            legacyFolder.delete();
            cmisUtils.invalidateFolderCache(legacyFolderKey);
        } catch (CmisPermissionDeniedException | CmisConstraintException e) {
            Log.warning(Geonet.RESOURCES, String.format(
                "Unable to remove empty legacy folder '%s' for metadata %d: %s", legacyFolderKey, metadataId, e.getMessage()));
        }
    }

    @Override
    public String delResources(final ServiceContext context, final int metadataId) throws Exception {
        String folderKey = null;
        try {
            folderKey = getMetadataDir(context, metadataId);
            final Folder folder = cmisUtils.getFolderCache(folderKey, true);

            Log.info(Geonet.RESOURCES, String.format("Deleting the folder of '%s' and the files within the folder", folderKey));
            folder.deleteTree(true, UnfileObject.DELETE, true);
            cmisUtils.invalidateFolderCache(folderKey);

            Log.info(Geonet.RESOURCES,
                    String.format("Metadata '%d' directory '%s' removed.", metadataId, folderKey));
            return String.format("Metadata '%d' directory '%s' removed.", metadataId, folderKey);
        } catch (CmisObjectNotFoundException e) {
            Log.warning(Geonet.RESOURCES,
                    String.format("Unable to located metadata '%d' directory '%s' to be removed.", metadataId, folderKey));
            return String.format("Unable to located metadata '%d' directory '%s' to be removed.", metadataId, folderKey);
        } catch (ResourceNotFoundException e) {
            Log.warning(Geonet.RESOURCES,
                String.format("Unable to located metadata '%d' directory '%s' to be removed.", metadataId, folderKey));
            return String.format("Unable to located metadata '%d' directory '%s' to be removed.", metadataId, folderKey);
        } catch (CmisPermissionDeniedException e) {
            Log.warning(Geonet.RESOURCES,
                    String.format("Insufficient privileges, unable to remove metadata '%d' directory '%s'.", metadataId, folderKey));
            return String.format("Insufficient privileges, unable to remove metadata '%d' directory '%s'.", metadataId, folderKey);
        } catch (CmisConstraintException e) {
            Log.warning(Geonet.RESOURCES,
                    String.format("Unable to remove metadata '%d' directory '%s' due so constraint violation or locks.", metadataId, folderKey));
            return String.format("Unable to remove metadata '%d' directory '%s' due so constraint violation or locks.", metadataId, folderKey);
        }
    }

    @Override
    public String delResource(final ServiceContext context, final String metadataUuid, final String resourceId, Boolean approved)
            throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);

        for (MetadataResourceVisibility visibility : visibilityCandidates(metadataUuid, approved, resourceId)) {
            if (tryDelResource(context, metadataUuid, metadataId, visibility, resourceId, approved)) {
                return String.format("Metadata resource '%s' removed.", resourceId);
            }
        }
        return String.format("Unable to remove resource '%s'.", resourceId);
    }

    @Override
    public String delResource(final ServiceContext context, final String metadataUuid, final MetadataResourceVisibility visibility,
                              final String resourceId, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        if (tryDelResource(context, metadataUuid, metadataId, visibility, resourceId, approved)) {
            return String.format("Metadata resource '%s' removed.", resourceId);
        }
        return String.format("Unable to remove resource '%s'.", resourceId);
    }

    protected boolean tryDelResource(final ServiceContext context, final String metadataUuid, final int metadataId, final MetadataResourceVisibility visibility,
                                   final String resourceId, Boolean approved) throws Exception {
        String key = resolveExistingKey(context, metadataUuid, metadataId, visibility, getFilename(metadataUuid, resourceId), approved);
        if (key == null) {
            Log.info(Geonet.RESOURCES,
                String.format("Unable to remove resource '%s' for metadata %d (%s).", resourceId, metadataId, metadataUuid));
            return false;
        }

        // Don't use caching for this process.
        OperationContext oc = cmisUtils.createOperationContext();
        oc.setCacheEnabled(false);

        try {
            final CmisObject object = cmisConfiguration.getClient().getObjectByPath(key, oc);
            object.delete();
            Log.info(Geonet.RESOURCES,
                String.format("Resource '%s' removed for metadata %d (%s).", resourceId, metadataId, metadataUuid));
            if (object instanceof Folder) {
                cmisUtils.invalidateFolderCacheItem(key);
            }
            return true;
            //CmisObjectNotFoundException when file not found
            //CmisPermissionDeniedException when user does not have permissions.
            //CmisConstraintException when there is a lock on the file from a checkout.
        } catch (CmisObjectNotFoundException | CmisPermissionDeniedException | CmisConstraintException e) {
            Log.info(Geonet.RESOURCES,
                String.format("Unable to remove resource '%s' for metadata %d (%s). %s", resourceId, metadataId, metadataUuid, e.getMessage()));
            return false;
        }
    }

    @Override
    public MetadataResource getResourceDescription(final ServiceContext context, final String metadataUuid,
                                                   final MetadataResourceVisibility visibility, final String filename, Boolean approved) throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        String key = resolveExistingKey(context, metadataUuid, metadataId, visibility, filename, approved);
        if (key == null) {
            return null;
        }

        try {
            final CmisObject object = cmisConfiguration.getClient().getObjectByPath(key);
            return createResourceDescription(context, metadataUuid, visibility, filename, (Document)object, metadataId, approved);
        } catch (CmisObjectNotFoundException e) {
            return null;
        }
    }

    @Override
    public MetadataResourceContainer getResourceContainerDescription(final ServiceContext context, final String metadataUuid, Boolean approved) throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);

        final String key = getMetadataDir(context, metadataId);


        String folderRoot = cmisConfiguration.getExternalResourceManagementFolderRoot();
        if (folderRoot == null) {
            folderRoot = "";
        }
        Folder parentFolder = cmisUtils.getFolderCache(key + folderRoot, false, true);
        MetadataResourceExternalManagementProperties metadataResourceExternalManagementProperties =
            getMetadataResourceExternalManagementProperties(context, metadataId, metadataUuid, null, String.valueOf(metadataId), null, null, parentFolder.getId(), parentFolder.getType(), MetadataResourceExternalManagementProperties.ValidationStatus.UNKNOWN);

        return new FilesystemStoreResourceContainer(metadataUuid, metadataId, metadataUuid,
            settingManager.getNodeURL() + "api/records/", metadataResourceExternalManagementProperties, approved);


    }

    @Override
    public void copyResources(ServiceContext context, String sourceUuid, String targetUuid, MetadataResourceVisibility metadataResourceVisibility, boolean sourceApproved, boolean targetApproved) throws Exception {
        final int sourceMetadataId = canEdit(context, sourceUuid, metadataResourceVisibility, sourceApproved);
        final int targetMetadataId = canEdit(context, sourceUuid, metadataResourceVisibility, targetApproved);
        final String sourceMetadataDir = getMetadataDir(context, sourceMetadataId);
        final String delimiter = cmisConfiguration.getFolderDelimiter();
        try {
            // A single listing under the whole source metadata folder returns both legacy and
            // flat documents together, same as getResources.
            Folder sourceParentFolder = cmisUtils.getFolderCache(sourceMetadataDir, true);

            OperationContext oc = cmisUtils.createOperationContext();
            // Reset Filter from the default operationalContext to include all fields because we may need secondary properties.
            oc.setFilter(null);

            Map<String, Document> sourceDocumentMap = cmisUtils.getCmisObjectMap(sourceParentFolder, null, oc);
            Map<String, MetadataResourceVisibility> trackedAccess = loadTrackedAccessByFilename(sourceMetadataId);

            for (Map.Entry<String, Document> sourceEntry : sourceDocumentMap.entrySet()) {
                Document sourceDocument = sourceEntry.getValue();
                // The map key is relative to sourceParentFolder and always starts with the
                // folder delimiter; keep the whole relative path (not just sourceDocument's own
                // bare name) so a nested-path resource's subfolder is preserved on copy.
                String sourceKey = sourceEntry.getKey();
                String relativeKey = sourceKey.startsWith(delimiter) ? sourceKey.substring(delimiter.length()) : sourceKey;

                final MetadataResourceVisibility legacyVisibility = legacyVisibilityOf(relativeKey, delimiter);
                final String relativeFilename;
                final MetadataResourceVisibility resourceVisibility;
                if (legacyVisibility != null) {
                    relativeFilename = relativeKey.substring(legacyVisibility.toString().length() + delimiter.length());
                    resourceVisibility = legacyVisibility;
                } else {
                    relativeFilename = relativeKey;
                    resourceVisibility = trackedAccess.get(relativeFilename);
                }
                if (resourceVisibility != metadataResourceVisibility) {
                    continue;
                }

                Log.info(Geonet.RESOURCES, String.format("Copying %s to %s" , sourceMetadataDir + delimiter + relativeKey, getMetadataDir(context, targetMetadataId)));
                // Get cmis properties from the source document
                Map<String, Object> sourceProperties = getProperties(sourceDocument);

                setCmisMetadataUUIDPrimary(sourceProperties, targetUuid);

                putResource(context, targetUuid, relativeFilename, sourceDocument.getContentStream().getStream(), null, metadataResourceVisibility, targetApproved, sourceProperties);

            }
        } catch (CmisObjectNotFoundException | ResourceNotFoundException e) {
            Log.warning(Geonet.RESOURCES, "Cannot find folder object from CMIS ... Abort copping resources from " + sourceMetadataDir);
        }
    }

    protected Map<String, Object> getProperties(Document document) {
        Map<String, Object> properties = new HashMap<>();

        // Get secondary properties aspect if it exists.
        String aspectId = null;
        Property aspectProperty = document.getProperty(PropertyIds.SECONDARY_OBJECT_TYPE_IDS);
        if (aspectProperty != null && !StringUtils.isEmpty(aspectProperty.getValueAsString())) {
            aspectId = aspectProperty.getValueAsString();
        }

        for (Property<?> property : document.getProperties()) {
            // Add secondary properties if exists.
            if (aspectId != null && property.getId().startsWith(aspectId) && property.getValue() != null) {
                properties.put(property.getId(), property.getValue());
            }
            // Add other common cmis properties.
            if (property.getId().startsWith(CMIS_PROPERTY_PREFIX) && property.getValue() != null) {
                properties.put(property.getId(), property.getValue());
            }
        }

        return properties;
    }

    protected Object getSecondaryProperty(Document document, String propertyName) {
        Object propertyValue = null;

        String aspectId = null;
        Property aspectProperty = document.getProperty(PropertyIds.SECONDARY_OBJECT_TYPE_IDS);
        if (aspectProperty != null) {
            aspectId = aspectProperty.getValueAsString();
        }

        if (!StringUtils.isEmpty(aspectId)) {
            Property<?> property = document.getProperty(propertyName.split(CMISConfiguration.CMIS_SECONDARY_PROPERTY_SEPARATOR)[1]);
            if (property != null && property.getValue() != null) {
                propertyValue = property.getValue();
            }
        }

        return propertyValue;
    }

    protected String getMetadataDir(ServiceContext context, final int metadataId) {

        Path metadataFullDir = Lib.resource.getMetadataDir(getDataDirectory(context), metadataId);
        Path baseMetadataDir = getBaseMetadataDir(context, metadataFullDir);
        Path metadataDir;
        if (baseMetadataDir.toString().equals(".")) {
            metadataDir = Paths.get(cmisConfiguration.getBaseRepositoryPath()).resolve(metadataFullDir);
        } else {
            metadataDir = Paths.get(cmisConfiguration.getBaseRepositoryPath()).resolve(baseMetadataDir.relativize(metadataFullDir));
        }

        // For windows it may be "\" in which case we need to change it to folderDelimiter which is normally "/"
        if (metadataDir.getFileSystem().getSeparator().equals(cmisConfiguration.getFolderDelimiter())) {
            return metadataDir.toString();
        } else {
            return metadataDir.toString().replace(metadataDir.getFileSystem().getSeparator(), cmisConfiguration.getFolderDelimiter());
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

    /**
     * get external resource management for the supplied resource.
     * Replace the following
     * {objectId}  type:visibility:metadataId:version:resourceId in base64 encoding
     * {id}  resource id
     * {type:folder:document} // Custom return type based on type. If the type is folder then type "folder" will be displayed else if document then "document" will be displayed
     * {type} // If the type is folder then type "folder" will be displayed else if document then "document" will be displayed
     * {uuid}  metadatauuid
     * {metadataid}  metadataid
     * {visibility}  visibility
     * {filename}  filename
     * {version}  version
     * {cmisobjectid}  cmis object id
     * {lang}  ISO639-1 2 char language
     * {iso3lang}  ISO 639-2/T language
     * <p>
     * Sample Url Alfresco
     * http://localhost:8080/share/page/{type:folder:document}-details?nodeRef=workspace://SpacesStore/{cmisobjectid}
     * Sample Url Open Text
     * http://localhost:8080/livelink/cs?func=ll&objaction=overview&objid={cmisobjectid}&vernum={version}
     */

    protected MetadataResourceExternalManagementProperties getMetadataResourceExternalManagementProperties(ServiceContext context,
                                                                                                         int metadataId,
                                                                                                         final String metadataUuid,
                                                                                                         final MetadataResourceVisibility visibility,
                                                                                                         final String resourceId,
                                                                                                         String filename,
                                                                                                         String version,
                                                                                                         String cmisObjectId,
                                                                                                         ObjectType type,
                                                                                                         MetadataResourceExternalManagementProperties.ValidationStatus validationStatus
    ) {
        String metadataResourceExternalManagementPropertiesUrl = cmisConfiguration.getExternalResourceManagementUrl();
        if (!StringUtils.isEmpty(metadataResourceExternalManagementPropertiesUrl)) {
            // {objectid}  objectId // It will be the type:visibility:metadataId:version:resourceId in base64
            // i.e. folder::100::100                     # Folder in resource 100
            // i.e. document:public:100:v1:sample.jpg    # public document 100 version v1 name sample.jpg
            if (metadataResourceExternalManagementPropertiesUrl.contains("{objectid}")) {
                metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{objectid\\})",
                    getResourceManagementExternalPropertiesObjectId((type == null ? "document" : (type instanceof Folder ? "folder" : "document")), visibility, metadataId, version, resourceId));
            }
            // {id}  id
            if (metadataResourceExternalManagementPropertiesUrl.contains("{id}")) {
                metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{id\\})", (resourceId==null?"":resourceId));
            }
            // {type:folder:document} // Custom return type based on type. If the type is folder then type "folder" will be displayed else if document then "document" will be displayed
            if (metadataResourceExternalManagementPropertiesUrl.contains("{type:")) {
                metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("\\{type:([a-zA-Z0-9]*?):([a-zA-Z0-9]*?)\\}",
                    (type==null?"":(type instanceof Folder?"$1":"$2")));
            }
            // {type} // If the type is folder then type "folder" will be displayed else if document then "document" will be displayed
            if (metadataResourceExternalManagementPropertiesUrl.contains("{type}")) {
                metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{type\\})",
                    (type == null ? "document" : (type instanceof Folder ? "folder" : "document")));
            }
            // {uuid}  metadatauuid
            if (metadataResourceExternalManagementPropertiesUrl.contains("{uuid}")) {
                metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{uuid\\})", (metadataUuid==null?"":metadataUuid));
            }
            // {metadataid}  metadataid
            if (metadataResourceExternalManagementPropertiesUrl.contains("{metadataid}")) {
                metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{metadataid\\})", String.valueOf(metadataId));
            }
            //    {visibility}  visibility
            if (metadataResourceExternalManagementPropertiesUrl.contains("{visibility}")) {
                metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{visibility\\})", (visibility==null?"":visibility.toString().toLowerCase()));
            }
            //    {filename}  filename
            if (metadataResourceExternalManagementPropertiesUrl.contains("{filename}")) {
                metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{filename\\})", (filename==null?"":filename));
            }
            // {version}  version
            if (metadataResourceExternalManagementPropertiesUrl.contains("{version}")) {
                metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{version\\})", (version==null?"":version));
            }
            // {cmisobjectid}  cmis object id
            if (metadataResourceExternalManagementPropertiesUrl.contains("{cmisobjectid}")) {
                metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{cmisobjectid\\})",  (cmisObjectId==null?"":cmisObjectId));
            }

            if (metadataResourceExternalManagementPropertiesUrl.contains("{lang}") || metadataResourceExternalManagementPropertiesUrl.contains("{ISO3lang}")) {
                final IsoLanguagesMapper mapper = ApplicationContextHolder.get().getBean(IsoLanguagesMapper.class);
                String contextLang = context==null || context.getLanguage() == null ? Geonet.DEFAULT_LANGUAGE : context.getLanguage();
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
                = new MetadataResourceExternalManagementProperties(cmisObjectId, metadataResourceExternalManagementPropertiesUrl, validationStatus);

        return metadataResourceExternalManagementProperties;
    }

    public ResourceManagementExternalProperties getResourceManagementExternalProperties() {
        return new ResourceManagementExternalProperties() {
            @Override
            public boolean isEnabled() {
                // Return true if we have an external management url
                return !StringUtils.isEmpty(cmisConfiguration.getExternalResourceManagementUrl());
            }

            @Override
            public String getWindowParameters() {
                return cmisConfiguration.getExternalResourceManagementWindowParameters();
            }

            @Override
            public boolean isModal() {
                return cmisConfiguration.isExternalResourceManagementModalEnabled();
            }

            @Override
            public boolean isFolderEnabled() {
                return isEnabled() && cmisConfiguration.isExternalResourceManagementFolderEnabled();
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

    protected static class CMISResourceHolder implements ResourceHolder {
        private final InputStreamResource resource;
        private final MetadataResource metadata;
        private final CmisObject cmisObject;
        private final InputStream inputStream;

        public CMISResourceHolder(final CmisObject cmisObject, MetadataResource metadata) throws IOException {
            this.metadata = metadata;
            this.cmisObject = cmisObject;
            this.inputStream = ((Document) cmisObject).getContentStream().getStream();
            this.resource = new InputStreamResource(inputStream);
        }

        public CMISResourceHolder(final CmisObject cmisObject, MetadataResource metadata, long start, long end) throws IOException {
            this.metadata = metadata;
            this.cmisObject = cmisObject;
            this.inputStream = ((Document) cmisObject).getContentStream(BigInteger.valueOf(start), BigInteger.valueOf(end - start + 1)).getStream();
            this.resource = new InputStreamResource(inputStream);
        }

        @Override
        public Resource getResource() {
            return resource;
        }

        public CmisObject getCmisObject() {
            return cmisObject;
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
