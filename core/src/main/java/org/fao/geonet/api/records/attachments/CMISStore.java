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
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.log4j.Logger;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.resources.CMISConfiguration;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.annotation.Nullable;

public class CMISStore extends AbstractStore {

    private Path baseMetadataDir = null;

    @Autowired
    CMISConfiguration CMISConfiguration;

    @Override
    public List<MetadataResource> getResources(final ServiceContext context, final String metadataUuid,
                                               final MetadataResourceVisibility visibility, String filter, Boolean approved) throws Exception {
        final int metadataId = canDownload(context, metadataUuid, visibility, approved);
        final SettingManager settingManager = context.getBean(SettingManager.class);

        final String resourceTypeDir = getMetadataDir(context, metadataId) + CMISConfiguration.getFolderDelimiter() + visibility.toString();

        List<MetadataResource> resourceList = new ArrayList<>();
        if (filter == null) {
            filter = FilesystemStore.DEFAULT_FILTER;
        }

        PathMatcher matcher =
                FileSystems.getDefault().getPathMatcher("glob:" + filter);

        try {
            Folder parentFolder = (Folder) CMISConfiguration.getClient().getObjectByPath(resourceTypeDir);

            Map<String, Document> documentMap = getCmisObjectMap(parentFolder, null);
            for (Map.Entry<String, Document> entry : documentMap.entrySet()) {
                Document object = entry.getValue();
                String cmisFilePath = entry.getKey();
                // Only add to the list if it is a document and it matches the filter.
                if (object instanceof Document) {
                    Path keyPath = new File(cmisFilePath).toPath().getFileName();
                    if (matcher.matches(keyPath)) {
                        final String filename = getFilename(cmisFilePath);
                        MetadataResource resource = createResourceDescription(context, settingManager, metadataUuid, visibility, filename, object.getContentStreamLength(),
                                object.getLastModificationDate().getTime(), object.getVersionLabel(), metadataId, approved);
                        resourceList.add(resource);
                    }
                }
            }
        } catch (CmisObjectNotFoundException e) {
            // ignore as it means that there is not data to list.
        }


        resourceList.sort(MetadataResourceVisibility.sortByFileName);

        return resourceList;
    }

    private MetadataResource createResourceDescription(final ServiceContext context, final SettingManager settingManager, final String metadataUuid,
                                                       final MetadataResourceVisibility visibility, final String resourceId, long size, Date lastModification, String version, int metadataId,
                                                       boolean approved) {
        String filename = getFilename(metadataUuid, resourceId);

        String versionValue = null;
        if (CMISConfiguration.isVersioningEnabled()) {
            versionValue = version;
        }

        MetadataResource.ExternalResourceManagementProperties externalResourceManagementProperties =
            getExternalResourceManagementProperties(context, metadataId, metadataUuid, visibility, resourceId, filename, version);

        return new FilesystemStoreResource(metadataUuid, metadataId, filename,
            settingManager.getNodeURL() + "api/records/", visibility, size, lastModification, versionValue, externalResourceManagementProperties, approved);
    }

    private static String getFilename(final String key) {
        final String[] splittedKey = key.split("/");
        return splittedKey[splittedKey.length - 1];
    }

    @Override
    public ResourceHolder getResource(final ServiceContext context, final String metadataUuid, final MetadataResourceVisibility visibility,
                                      final String resourceId, Boolean approved) throws Exception {
        // Those characters should not be allowed by URL structure
        int metadataId = canDownload(context, metadataUuid, visibility, approved);
        try {
            final CmisObject object = CMISConfiguration.getClient().getObjectByPath(getKey(context, metadataUuid, metadataId, visibility, resourceId));
            final SettingManager settingManager = context.getBean(SettingManager.class);
            return new ResourceHolderImpl(object, createResourceDescription(context, settingManager, metadataUuid, visibility, resourceId,
                ((Document) object).getContentStreamLength(),
                object.getLastModificationDate().getTime(), ((Document) object).getVersionLabel(), metadataId, approved));
        } catch (CmisObjectNotFoundException e) {
            Log.warning(Geonet.RESOURCES, String.format("Error getting metadata resource. '%s' not found for metadata '%s'", resourceId, metadataUuid));
            throw new ResourceNotFoundException(
                String.format("Error getting metadata resource. '%s' not found for metadata '%s'", resourceId, metadataUuid));
        }
    }

    private String getKey(final ServiceContext context, String metadataUuid, int metadataId, MetadataResourceVisibility visibility, String resourceId) {
        checkResourceId(resourceId);
        final String metadataDir = getMetadataDir(context, metadataId);
        return metadataDir + CMISConfiguration.getFolderDelimiter() + visibility.toString() + CMISConfiguration.getFolderDelimiter() + getFilename(metadataUuid, resourceId);
    }

    @Override
    public MetadataResource putResource(final ServiceContext context, final String metadataUuid, final String filename,
                                        final InputStream is, @Nullable final Date changeDate, final MetadataResourceVisibility visibility, Boolean approved)
            throws Exception {
        final SettingManager settingManager = context.getBean(SettingManager.class);
        final int metadataId = canEdit(context, metadataUuid, approved);
        String key = getKey(context, metadataUuid, metadataId, visibility, filename);

        // Don't use caching for this process.
        OperationContext oc = CMISConfiguration.getClient().createOperationContext();
        oc.setCacheEnabled(false);

        // Split the filename and parent folder from the key.
        int lastFolderDelimiterKeyIndex = key.lastIndexOf(CMISConfiguration.getFolderDelimiter());
        String filenameKey = key.substring(lastFolderDelimiterKeyIndex + 1);
        String parentKey = key.substring(0, lastFolderDelimiterKeyIndex);

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        properties.put(PropertyIds.NAME, filenameKey);
        if (changeDate != null) {
            properties.put(PropertyIds.LAST_MODIFICATION_DATE, changeDate);
        }
        int isLength=is.available();
        ContentStream contentStream = CMISConfiguration.getClient().getObjectFactory().createContentStream(key, isLength, Files.probeContentType(new File(key).toPath()), is);

        Document doc;
        try {
            // If the document is found then we are updating the existing document.
            doc = (Document) CMISConfiguration.getClient().getObjectByPath(key, oc);
            doc.updateProperties(properties, true);
            doc.setContentStream(contentStream, true, true);
            //           }
            // Avoid CMIS API call is info is not enabled.
            if (Logger.getLogger(Geonet.RESOURCES).isInfoEnabled()) {
                Log.info(Geonet.RESOURCES,
                        String.format("Updated metadata resource '%s' for metadata '%s'. Current version '%s'.", key, metadataUuid, doc.getVersionLabel()));
            }
        } catch (CmisPermissionDeniedException ex) {
            Log.warning(Geonet.RESOURCES, String.format(
                    "No permissions to update metadata resource '%s' for metadata '%s' due to constraint violation or lock.", key, metadataUuid));
            throw new NotAllowedException(String.format(
                    "No permissions to update metadata resource '%s' for metadata '%s' due to constraint violation or lock.", key, metadataUuid));

        } catch (CmisConstraintException e) {
            Log.warning(Geonet.RESOURCES, String.format(
                    "No allowed to modify existing metadata resource '%s' for metadata '%s' due to constraint violation or lock.", key, metadataUuid));
            throw new NotAllowedException(String.format(
                    "No allowed to modify existing metadata resource '%s' for metadata '%s' due to constraint violation or lock.", key, metadataUuid));
        } catch (CmisObjectNotFoundException e) {
            // If the document is not found then we are adding a new document.

            // Get parent folder.
            Folder parentFolder;
            // synchronize folder creation.
            // This will prevent cases where multiple files are uploaded on the interface
            // In this case there will be a race condition to create the same folder.
            // And if this is not synchronized then there will be a lot or CmisContentAlreadyExistsException errors.
            synchronized (this) {
                try {
                    parentFolder = (Folder) CMISConfiguration.getClient().getObjectByPath(parentKey, oc);
                } catch (CmisObjectNotFoundException ex) {
                    // Create parent folder if it does not exists.
                    ObjectId objectId = CMISConfiguration.getClient().createPath(parentKey, "cmis:folder");
                    parentFolder = (Folder) CMISConfiguration.getClient().getObject(objectId, oc);
                }
            }
            try {
                doc = parentFolder.createDocument(properties, contentStream, VersioningState.MAJOR);
                // Avoid CMIS API call is info is not enabled.
                if (Logger.getLogger(Geonet.RESOURCES).isInfoEnabled()) {
                    Log.info(Geonet.RESOURCES,
                            String.format("Added resource metadata resource '%s' for metadata '%s'.", doc.getPaths().get(0), metadataUuid));
                }
            } catch (CmisPermissionDeniedException ex) {
                Log.warning(Geonet.RESOURCES, String.format(
                        "No permissions to add metadata resource '%s' for metadata '%s'.", key, metadataUuid));
                throw new NotAllowedException(String.format(
                        "No permissions to add metadata resource '%s' for metadata '%s'.", key, metadataUuid));
            }
        }

        return createResourceDescription(context, settingManager, metadataUuid, visibility, filename, isLength,
                doc.getLastModificationDate().getTime(), doc.getVersionLabel(), metadataId, approved);
    }

    @Override
    public MetadataResource patchResourceStatus(final ServiceContext context, final String metadataUuid, final String resourceId,
                                                final MetadataResourceVisibility visibility, Boolean approved) throws Exception {
        SettingManager settingManager = context.getBean(SettingManager.class);
        int metadataId = canEdit(context, metadataUuid, approved);

        // Don't use caching for this process.
        OperationContext oc = CMISConfiguration.getClient().createOperationContext();
        oc.setCacheEnabled(false);

        String sourceKey = null;
        for (MetadataResourceVisibility sourceVisibility : MetadataResourceVisibility.values()) {
            final String key = getKey(context, metadataUuid, metadataId, sourceVisibility, resourceId);
            try {
                final CmisObject object = CMISConfiguration.getClient().getObjectByPath(key, oc);
                if (sourceVisibility != visibility) {
                    sourceKey = key;
                    break;
                } else {
                    // already the good visibility
                    return createResourceDescription(context, settingManager, metadataUuid, visibility, resourceId, ((Document) object).getContentStreamLength(),
                        object.getLastModificationDate().getTime(), ((Document) object).getVersionLabel(), metadataId, approved);
                }
            } catch (CmisObjectNotFoundException ignored) {
                // ignored
            }
        }
        if (sourceKey != null) {
            final String destKey = getKey(context, metadataUuid, metadataId, visibility, resourceId);

            final CmisObject sourceObject = CMISConfiguration.getClient().getObjectByPath(sourceKey, oc);

            // Get the parent folder object id.
            int lastFolderDelimiterSourceKeyIndex = sourceKey.lastIndexOf(CMISConfiguration.getFolderDelimiter());
            String parentSourceKey = sourceKey.substring(0, lastFolderDelimiterSourceKeyIndex);

            // Get the parent source folder id.
            Folder parentSourceFolder;
            try {
                parentSourceFolder = (Folder) CMISConfiguration.getClient().getObjectByPath(parentSourceKey, oc);
            } catch (CmisObjectNotFoundException e) {
                // Create parent folder if it does not exists.
                ObjectId objectId = CMISConfiguration.getClient().createPath(parentSourceKey, "cmis:folder");
                parentSourceFolder = (Folder) CMISConfiguration.getClient().getObject(objectId, oc);
            }

            // Get the parent destination folder id.
            int lastFolderDelimiterDestKeyIndex = destKey.lastIndexOf(CMISConfiguration.getFolderDelimiter());
            String parentDestKey = destKey.substring(0, lastFolderDelimiterDestKeyIndex);

            // Get parent folder.
            Folder parentDestFolder;
            try {
                parentDestFolder = (Folder) CMISConfiguration.getClient().getObjectByPath(parentDestKey, oc);
            } catch (CmisObjectNotFoundException e) {
                // Create parent folder if it does not exists.
                ObjectId objectId = CMISConfiguration.getClient().createPath(parentDestKey, "cmis:folder");
                parentDestFolder = (Folder) CMISConfiguration.getClient().getObject(objectId, oc);
            }

            // Move the object from source to destination
            try {
                ((Document) sourceObject).move(parentSourceFolder, parentDestFolder);
            } catch (CmisPermissionDeniedException e) {
                Log.warning(Geonet.RESOURCES, String.format(
                        "No permissions to modify metadata resource '%s' for metadata '%s'.", resourceId, metadataUuid));
                throw new NotAllowedException(String.format(
                        "No permissions to modify metadata resource '%s' for metadata '%s'.", resourceId, metadataUuid));
            }

            final CmisObject object = CMISConfiguration.getClient().getObjectByPath(destKey, oc);

            return createResourceDescription(context, settingManager, metadataUuid, visibility, resourceId, ((Document) object).getContentStreamLength(),
                    object.getLastModificationDate().getTime(), ((Document) object).getVersionLabel(), metadataId, approved);
        } else {
            Log.warning(Geonet.RESOURCES,
                    String.format("Could not update permissions. Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid));
            throw new ResourceNotFoundException(
                    String.format("Could not update permissions. Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid));
        }
    }

    @Override
    public String delResources(final ServiceContext context, final String metadataUuid, Boolean approved) throws Exception {
        // Don't use caching for this process.
        OperationContext oc = CMISConfiguration.getClient().createOperationContext();
        oc.setCacheEnabled(false);

        int metadataId = canEdit(context, metadataUuid, approved);
        try {
            final CmisObject folderObject = CMISConfiguration.getClient().getObjectByPath(getMetadataDir(context, metadataId), oc);

            ((Folder) folderObject).deleteTree(true, UnfileObject.DELETE, true);
            Log.info(Geonet.RESOURCES,
                    String.format("Metadata '%s' directory removed.", metadataId));
            return String.format("Metadata '%s' directory removed.", metadataId);
        } catch (CmisObjectNotFoundException e) {
            Log.warning(Geonet.RESOURCES,
                    String.format("Unable to located metadata '%s' directory to be removed.", metadataId));
            return String.format("Unable to located metadata '%s' directory to be removed.", metadataId);
        } catch (CmisPermissionDeniedException e) {
            Log.warning(Geonet.RESOURCES,
                    String.format("Insufficient privileges, unable to remove metadata '%s' directory.", metadataId));
            return String.format("Insufficient privileges, unable to remove metadata '%s' directory.", metadataId);
        } catch (CmisConstraintException e) {
            Log.warning(Geonet.RESOURCES,
                    String.format("Unable to remove metadata '%s' directory due so constraint violation or locks.", metadataId));
            return String.format("Unable to remove metadata '%s' directory due so constraint violation or locks.", metadataId);
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

    private boolean tryDelResource(final ServiceContext context, final String metadataUuid, final int metadataId, final MetadataResourceVisibility visibility,
                                   final String resourceId) throws Exception {
        final String key = getKey(context, metadataUuid, metadataId, visibility, resourceId);

        // Don't use caching for this process.
        OperationContext oc = CMISConfiguration.getClient().createOperationContext();
        oc.setCacheEnabled(false);

        try {
            final CmisObject object = CMISConfiguration.getClient().getObjectByPath(key, oc);
            object.delete();
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
        SettingManager settingManager = context.getBean(SettingManager.class);
        try {
            final CmisObject object = CMISConfiguration.getClient().getObjectByPath(key);
            return createResourceDescription(context, settingManager, metadataUuid, visibility, filename, ((Document) object).getContentStreamLength(),
                object.getLastModificationDate().getTime(), ((Document) object).getVersionLabel(), metadataId, approved);
        } catch (CmisObjectNotFoundException e) {
            return null;
        }
    }

    private String getMetadataDir(ServiceContext context, final int metadataId) {

        Path metadataFullDir = Lib.resource.getMetadataDir(getDataDirectory(context), metadataId);
        Path baseMetadataDir = getBaseMetadataDir(context, metadataFullDir);
        Path metadataDir;
        if (baseMetadataDir.toString().equals(".")) {
            metadataDir = Paths.get(CMISConfiguration.getBaseRepositoryPath()).resolve(metadataFullDir);
        } else {
            metadataDir = Paths.get(CMISConfiguration.getBaseRepositoryPath()).resolve(baseMetadataDir.relativize(metadataFullDir));
        }

        // For windows it may be "\" in which case we need to change it to folderDelimiter which is normally "/"
        if (metadataDir.getFileSystem().getSeparator().equals(CMISConfiguration.getFolderDelimiter())) {
            return metadataDir.toString();
        } else {
            return metadataDir.toString().replace(metadataDir.getFileSystem().getSeparator(), CMISConfiguration.getFolderDelimiter());
        }
    }

    private Path getBaseMetadataDir(ServiceContext context, Path metadataFullDir) {
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
        return context.getBean(GeonetworkDataDirectory.class);
    }

    /**
     * get external resource management for the supplied resource.
     * Replace the following
     * {id}  resource id
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
     * http://localhost:8080/share/page/document-details?nodeRef=workspace://SpacesStore/{cmisobjectid}
     * Sample Url Open Text
     * http://localhost:8080/livelink/cs?func=ll&objaction=overview&objid={cmisobjectid}&vernum={version}
     */

    private MetadataResource.ExternalResourceManagementProperties getExternalResourceManagementProperties(ServiceContext context,
                                                    int metadataId,
                                                    final String metadataUuid,
                                                    final MetadataResourceVisibility visibility,
                                                    final String resourceId,
                                                    String filename,
                                                    String version
    ) {
        String externalResourceManagementUrl = CMISConfiguration.getExternalResourceManagementUrl();
        if (!StringUtils.isEmpty(externalResourceManagementUrl)) {
            // {id}  id
            if (externalResourceManagementUrl.contains("{id}")) {
                externalResourceManagementUrl = externalResourceManagementUrl.replaceAll("(\\{id\\})", resourceId);
            }
            // {uuid}  metadatauuid
            if (externalResourceManagementUrl.contains("{uuid}")) {
                externalResourceManagementUrl = externalResourceManagementUrl.replaceAll("(\\{uuid\\})", metadataUuid);
            }
            // {metadataid}  metadataid
            if (externalResourceManagementUrl.contains("{metadataid}")) {
                externalResourceManagementUrl = externalResourceManagementUrl.replaceAll("(\\{metadataid\\})", String.valueOf(metadataId));
            }
            //    {visibility}  visibility
            if (externalResourceManagementUrl.contains("{visibility}")) {
                externalResourceManagementUrl = externalResourceManagementUrl.replaceAll("(\\{visibility\\})", visibility.toString().toLowerCase());
            }
            //    {filename}  filename
            if (externalResourceManagementUrl.contains("{filename}")) {
                externalResourceManagementUrl = externalResourceManagementUrl.replaceAll("(\\{filename\\})", filename);
            }
            // {version}  version
            if (externalResourceManagementUrl.contains("{version}")) {
                externalResourceManagementUrl = externalResourceManagementUrl.replaceAll("(\\{version\\})", version);
            }
            // {cmisobjectid}  cmis object id
            if (externalResourceManagementUrl.contains("{cmisobjectid}")) {
                final CmisObject cmisObject = CMISConfiguration.getClient().getObjectByPath(getKey(context, metadataUuid, metadataId, visibility, resourceId));
                externalResourceManagementUrl = externalResourceManagementUrl.replaceAll("(\\{cmisobjectid\\})", ((Document) cmisObject).getVersionSeriesId());
            }

            if (externalResourceManagementUrl.contains("{lang}") || externalResourceManagementUrl.contains("{ISO3lang}")) {
                final IsoLanguagesMapper mapper = ApplicationContextHolder.get().getBean(IsoLanguagesMapper.class);
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
                if (externalResourceManagementUrl.contains("{lang}")) {
                    externalResourceManagementUrl = externalResourceManagementUrl.replaceAll("(\\{lang\\})", lang);
                }
                // {iso3lang}  ISO 639-2/T language
                if (externalResourceManagementUrl.contains("{iso3lang}")) {
                    externalResourceManagementUrl = externalResourceManagementUrl.replaceAll("(\\{iso3lang\\})", iso3Lang);
                }
            }
        }

        MetadataResource.ExternalResourceManagementProperties externalResourceManagementProperties
                = new MetadataResource.ExternalResourceManagementProperties(externalResourceManagementUrl,
                CMISConfiguration.getExternalResourceManagementWindowParameters(), CMISConfiguration.isExternalResourceManagementModal());

        return externalResourceManagementProperties;
    }

    private static class ResourceHolderImpl implements ResourceHolder {
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

    private Map<String, Document> getCmisObjectMap(Folder folder, String baseFolder) {
        if (baseFolder == null) {
            baseFolder = "";
        }
        Map<String, Document> documentMap = new HashMap<>();
        for (CmisObject cmisObject : folder.getChildren()) {
            if (cmisObject instanceof Folder) {
                documentMap.putAll(getCmisObjectMap((Folder) cmisObject, baseFolder + CMISConfiguration.getFolderDelimiter() + cmisObject.getName()));
                return documentMap;
            } else {
                if (cmisObject instanceof Document) {
                    documentMap.put(baseFolder + CMISConfiguration.getFolderDelimiter() + cmisObject.getName(), (Document) cmisObject);
                }
            }
        }
        return documentMap;
    }
}
