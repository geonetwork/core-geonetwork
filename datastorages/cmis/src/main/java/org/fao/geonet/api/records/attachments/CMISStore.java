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
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
                                               final MetadataResourceVisibility visibility, String filter, Boolean approved) throws Exception {
        final int metadataId = canDownload(context, metadataUuid, visibility, approved);

        final String resourceTypeDir = getMetadataDir(context, metadataId) + cmisConfiguration.getFolderDelimiter() + visibility.toString();

        List<MetadataResource> resourceList = new ArrayList<>();
        if (filter == null) {
            filter = FilesystemStore.DEFAULT_FILTER;
        }

        PathMatcher matcher =
                FileSystems.getDefault().getPathMatcher("glob:" + filter);

        try {
            Folder parentFolder = cmisUtils.getFolderCache(resourceTypeDir);

            OperationContext oc = cmisUtils.createOperationContext();
            if (cmisConfiguration.existExternalResourceManagementValidationStatusSecondaryProperty()) {
                // Reset Filter from the default operationalContext to include all fields because we may need secondary properties.
                oc.setFilter(null);
            }

            Map<String, Document> documentMap = cmisUtils.getCmisObjectMap(parentFolder, null, oc);
            for (Map.Entry<String, Document> entry : documentMap.entrySet()) {
                Document object = entry.getValue();
                String cmisFilePath = entry.getKey();
                // Only add to the list if it is a document and it matches the filter.
                if (object instanceof Document) {
                    Path keyPath = new File(cmisFilePath).toPath().getFileName();
                    if (matcher.matches(keyPath)) {
                        final String filename = getFilename(cmisFilePath);
                        MetadataResource resource = createResourceDescription(context, metadataUuid, visibility, filename, object, metadataId, approved);
                        resourceList.add(resource);
                    }
                }
            }
        } catch (CmisObjectNotFoundException | ResourceNotFoundException e) {
            // ignore as it means that there is no data to list.
        }


        resourceList.sort(MetadataResourceVisibility.sortByFileName);

        return resourceList;
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

        return new FilesystemStoreResource(metadataUuid, metadataId, filename,
            settingManager.getNodeURL() + "api/records/", visibility, document.getContentStreamLength(), document.getLastModificationDate().getTime(), versionValue, metadataResourceExternalManagementProperties, approved);
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
            final CmisObject object = cmisConfiguration.getClient().getObjectByPath(getKey(context, metadataUuid, metadataId, visibility, resourceId));
            return new ResourceHolderImpl(object, createResourceDescription(context, metadataUuid, visibility, resourceId,
                (Document) object, metadataId, approved));
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
            final CmisObject object = cmisConfiguration.getClient().getObjectByPath(getKey(context, metadataUuid, metadataId, visibility, resourceId));
            return new ResourceHolderImpl(object, createResourceDescription(context, metadataUuid, visibility, resourceId,
                (Document) object, metadataId, approved));
        } catch (CmisObjectNotFoundException e) {
            throw new ResourceNotFoundException(
                String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
        }
    }

    protected String getKey(final ServiceContext context, String metadataUuid, int metadataId, MetadataResourceVisibility visibility, String resourceId) {
        checkResourceId(resourceId);
        final String metadataDir = getMetadataDir(context, metadataId);
        return metadataDir + cmisConfiguration.getFolderDelimiter() + visibility.toString() + cmisConfiguration.getFolderDelimiter() + getFilename(metadataUuid, resourceId);
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
        String key = getKey(context, metadataUuid, metadataId, visibility, filename);

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

        // Don't use caching for this process.
        OperationContext oc = cmisUtils.createOperationContext();
        oc.setCacheEnabled(false);

        String sourceKey = null;
        CmisObject sourceObject =  null;
        for (MetadataResourceVisibility sourceVisibility : MetadataResourceVisibility.values()) {
            final String key = getKey(context, metadataUuid, metadataId, sourceVisibility, resourceId);
            try {
                final CmisObject object = cmisConfiguration.getClient().getObjectByPath(key, oc);
                if (sourceVisibility != visibility) {
                    sourceKey = key;
                    sourceObject = object;
                    break;
                } else {
                    // already the good visibility
                    return createResourceDescription(context, metadataUuid, visibility, resourceId, (Document) object, metadataId, approved);
                }
            } catch (CmisObjectNotFoundException ignored) {
                // ignored
            }
        }
        if (sourceKey != null) {
            final String destKey = getKey(context, metadataUuid, metadataId, visibility, resourceId);

            // Get the parent folder object id.
            int lastFolderDelimiterSourceKeyIndex = sourceKey.lastIndexOf(cmisConfiguration.getFolderDelimiter());
            String parentSourceKey = sourceKey.substring(0, lastFolderDelimiterSourceKeyIndex);

            Folder parentSourceFolder = cmisUtils.getFolderCache(parentSourceKey);

            // Get the parent destination folder id.
            int lastFolderDelimiterDestKeyIndex = destKey.lastIndexOf(cmisConfiguration.getFolderDelimiter());
            String parentDestFolderKey = destKey.substring(0, lastFolderDelimiterDestKeyIndex);

            Folder parentDestFolder = cmisUtils.getFolderCache(parentDestFolderKey, true, true);

            // Move the object from source to destination
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
        } else {
            Log.warning(Geonet.RESOURCES,
                    String.format("Could not update permissions. Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid));
            throw new ResourceNotFoundException(
                    String.format("Could not update permissions. Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid));
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

        for (MetadataResourceVisibility visibility : MetadataResourceVisibility.values()) {
            if (tryDelResource(context, metadataUuid, metadataId, visibility, resourceId)) {
                Log.info(Geonet.RESOURCES,
                        String.format("MetadataResource '%s' removed.", resourceId));
                return String.format("MetadataResource '%s' removed.", resourceId);
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
            Log.info(Geonet.RESOURCES,
                    String.format("MetadataResource '%s' removed.", resourceId));
            return String.format("MetadataResource '%s' removed.", resourceId);
        }
        Log.info(Geonet.RESOURCES,
                String.format("Unable to remove resource '%s'.", resourceId));
        return String.format("Unable to remove resource '%s'.", resourceId);
    }

    protected boolean tryDelResource(final ServiceContext context, final String metadataUuid, final int metadataId, final MetadataResourceVisibility visibility,
                                   final String resourceId) throws Exception {
        final String key = getKey(context, metadataUuid, metadataId, visibility, resourceId);

        // Don't use caching for this process.
        OperationContext oc = cmisUtils.createOperationContext();
        oc.setCacheEnabled(false);

        try {
            final CmisObject object = cmisConfiguration.getClient().getObjectByPath(key, oc);
            object.delete();
            if (object instanceof Folder) {
                cmisUtils.invalidateFolderCacheItem(key);
            }
            return true;
            //CmisObjectNotFoundException when file not found
            //CmisPermissionDeniedException when user does not have permissions.
            //CmisConstraintException when there is a lock on the file from a checkout.
        } catch (CmisObjectNotFoundException | CmisPermissionDeniedException | CmisConstraintException e) {
            return false;
        }
    }

    @Override
    public MetadataResource getResourceDescription(final ServiceContext context, final String metadataUuid,
                                                   final MetadataResourceVisibility visibility, final String filename, Boolean approved) throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        final String key = getKey(context, metadataUuid, metadataId, visibility, filename);

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
        final String sourceResourceTypeDir = getMetadataDir(context, sourceMetadataId) + cmisConfiguration.getFolderDelimiter() + metadataResourceVisibility.toString();
        final String targetResourceTypeDir = getMetadataDir(context, targetMetadataId) + cmisConfiguration.getFolderDelimiter() + metadataResourceVisibility.toString();
        try {
            Folder sourceParentFolder = cmisUtils.getFolderCache(sourceResourceTypeDir, true);

            OperationContext oc = cmisUtils.createOperationContext();
            // Reset Filter from the default operationalContext to include all fields because we may need secondary properties.
            oc.setFilter(null);

            Map<String, Document> sourceDocumentMap = cmisUtils.getCmisObjectMap(sourceParentFolder, null, oc);


            for (Map.Entry<String, Document> sourceEntry : sourceDocumentMap.entrySet()) {
                Document sourceDocument = sourceEntry.getValue();


                Log.info(Geonet.RESOURCES, String.format("Copying %s to %s" , sourceResourceTypeDir+cmisConfiguration.getFolderDelimiter()+sourceDocument.getName(), targetResourceTypeDir));
                // Get cmis properties from the source document
                Map<String, Object> sourceProperties = getProperties(sourceDocument);
                putResource(context, targetUuid, sourceDocument.getName(), sourceDocument.getContentStream().getStream(), null, metadataResourceVisibility, targetApproved, sourceProperties);

            }
        } catch (CmisObjectNotFoundException | ResourceNotFoundException e) {
            Log.warning(Geonet.RESOURCES, "Cannot find folder object from CMIS ... Abort copping resources from " + sourceResourceTypeDir);
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
     * {id}  resource id
     * {type:folder:document} // If the type is folder then type "folder" will be displayed else if document then "document" will be displayed
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
            // {id}  id
            if (metadataResourceExternalManagementPropertiesUrl.contains("{id}")) {
                metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("(\\{id\\})", (resourceId==null?"":resourceId));
            }
            // {type:folder:document} // If the type is folder then type "folder" will be displayed else if document then "document" will be displayed
            if (metadataResourceExternalManagementPropertiesUrl.contains("{type:")) {
                metadataResourceExternalManagementPropertiesUrl = metadataResourceExternalManagementPropertiesUrl.replaceAll("\\{type:([a-zA-Z0-9]*?):([a-zA-Z0-9]*?)\\}",
                    (type==null?"":(type instanceof Folder?"$1":"$2")));
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

    protected static class ResourceHolderImpl implements ResourceHolder {
        private CmisObject cmisObject;
        private Path tempFolderPath;
        private Path path;
        private final MetadataResource metadataResource;

        public ResourceHolderImpl(final CmisObject cmisObject, MetadataResource metadataResource) throws IOException {
            // Preserve filename by putting the files into a temporary folder and using the same filename.
            tempFolderPath = Files.createTempDirectory("gn-meta-res-" + String.valueOf(metadataResource.getMetadataId() + "-"));
            tempFolderPath.toFile().deleteOnExit();
            path = tempFolderPath.resolve(getFilename(cmisObject.getName()));
            this.metadataResource = metadataResource;
            this.cmisObject = cmisObject;
            try (InputStream in = ((Document) cmisObject).getContentStream().getStream()) {
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        @Override
        public Path getPath() {
            return path;
        }

        public CmisObject getCmisObject() {
            return cmisObject;
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
