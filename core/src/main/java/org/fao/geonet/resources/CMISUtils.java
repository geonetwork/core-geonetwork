/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;

public class CMISUtils {
    @Autowired
    CMISConfiguration cmisConfiguration;

    // Folder cache of 1000 to help speed up folder creation due slow performance executing CMIS multilevel folder.
    private Cache<String, Folder> folderCache = CacheBuilder.newBuilder().maximumSize(1000).build();

    public void invalidateFolderCache(String folderKey) throws ResourceNotFoundException {
        // using for-each loop for iteration over folder cache and remove entries related to the folder.
        // Need to create a copy of the folderCache keys instead of using the keySet() so that removals will not affect the looping of the list.
        List<String> cachedKeys = new ArrayList<String>(folderCache.asMap().keySet());
        for (String cachedKey : cachedKeys) {
            if (cachedKey.startsWith(folderKey)) {
                folderCache.invalidate(cachedKey);
            }
        }
    }

    public void invalidateFolderCacheItem(String folderKey) {
        folderCache.invalidate(folderKey);
    }

    public Folder getFolderCache(String folderKey) throws ResourceNotFoundException, CmisPermissionDeniedException {
        return getFolderCache(folderKey, false, false);
    }

    public Folder getFolderCache(String folderKey, boolean refresh) throws ResourceNotFoundException, CmisPermissionDeniedException {
        return getFolderCache(folderKey, refresh, false);
    }


    public Folder getFolderCache(String folderKey, boolean refresh, boolean createMissing) throws ResourceNotFoundException, CmisPermissionDeniedException {
        Folder folder = null;
        // Primitive object declared as an object array so that it can be marked as final so it can be used in calling class.
        // If it is set to true then it was already fetched without using cache so there is no need to refresh()
        final boolean[] foundWithoutCache = {false};

        try {
            folder = folderCache.get(folderKey, new Callable<Folder>() {
                @Override
                public Folder call() throws Exception {
                    try {
                        OperationContext oc = cmisConfiguration.getClient().getDefaultContext();
                        if (refresh) {
                            oc = createOperationContext();
                            oc.setCacheEnabled(false);
                        }
                        Folder folder = (Folder) cmisConfiguration.getClient().getObjectByPath(folderKey, oc);
                        if (refresh) {
                            foundWithoutCache[0] = true;
                        }
                        return folder;
                    } catch (CmisObjectNotFoundException e) {
                        if (createMissing) {
                            String parentFolderKey = folderKey.substring(0, folderKey.lastIndexOf(cmisConfiguration.getFolderDelimiter()));
                            Folder subFolder = getFolderCache(parentFolderKey, refresh, createMissing);

                            // synchronize folder creation.
                            // This will prevent cases where multiple files are uploaded on the interface
                            // In this case there will be a race condition to create the same folder.
                            // And if this is not synchronized then there will be a lot or CmisContentAlreadyExistsException errors.
                            Folder folder;
                            synchronized (this) {
                                Map<String, Object> properties = new HashMap();
                                properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
                                properties.put(PropertyIds.NAME, folderKey.substring(folderKey.lastIndexOf(cmisConfiguration.getFolderDelimiter())+1));
                                folder = subFolder.createFolder(properties);
                            }
                            if (refresh) {
                                foundWithoutCache[0] = true;
                            }
                            return folder;
                        } else {
                            throw e;
                        }
                    }
                }
            });
        } catch (ExecutionException | UncheckedExecutionException e) {
            if (e.getCause() instanceof CmisObjectNotFoundException) {
                throw new ResourceNotFoundException("Error getting folder resource from cache: " + folderKey, e);
            } else if (e.getCause() instanceof CmisPermissionDeniedException) {
                throw new CmisPermissionDeniedException("Error getting folder resource from cache: " + folderKey, e);
            } else if (e.getCause() instanceof CmisConstraintException) {
                throw new CmisConstraintException("Error getting folder resource from cache: " + folderKey, e);
            } else {
                Log.error(Geonet.RESOURCES, String.format(
                    "\"Error getting resource from cache: '%s'.", folderKey), e);
                throw new RuntimeException(e.getCause());
            }
        }
        if (refresh && !foundWithoutCache[0]) {
            try {
                folder.refresh();
            } catch (CmisObjectNotFoundException e) {
                folderCache.invalidate(folderKey);
                folder = getFolderCache(folderKey, refresh);
            }
        }
        return folder;
    }


    public OperationContext createOperationContext() {
        // Create operational context based on defaults.
        return cmisConfiguration.getClient().createOperationContext(
            cmisConfiguration.getClient().getDefaultContext().getFilter(),
            cmisConfiguration.getClient().getDefaultContext().isIncludeAcls(),
            cmisConfiguration.getClient().getDefaultContext().isIncludeAllowableActions(),
            cmisConfiguration.getClient().getDefaultContext().isIncludePolicies(),
            cmisConfiguration.getClient().getDefaultContext().getIncludeRelationships(),
            cmisConfiguration.getClient().getDefaultContext().getRenditionFilter(),
            cmisConfiguration.getClient().getDefaultContext().isIncludePathSegments(),
            cmisConfiguration.getClient().getDefaultContext().getOrderBy(),
            cmisConfiguration.getClient().getDefaultContext().isCacheEnabled(),
            cmisConfiguration.getClient().getDefaultContext().getMaxItemsPerPage()
        );
    }

    public Map<String, Document> getCmisObjectMap(Folder folder, String baseFolder) {
        return getCmisObjectMap(folder, baseFolder, createOperationContext(), null);
    }

    public Map<String, Document> getCmisObjectMap(Folder folder, String baseFolder, OperationContext oc) {
        return getCmisObjectMap(folder, baseFolder,  oc, null);
    }

    public Map<String, Document> getCmisObjectMap(Folder folder, String baseFolder, String suffixlessKeyFilename) {
        return getCmisObjectMap(folder, baseFolder,  createOperationContext(), null);
    }

    public Map<String, Document> getCmisObjectMap(Folder folder, String baseFolder, OperationContext oc, String suffixlessKeyFilename) {
        if (baseFolder == null) {
            baseFolder = "";
        }
        Map<String, Document> documentMap = new HashMap<>();
        for (CmisObject cmisObject : folder.getChildren(oc)) {
            if (cmisObject instanceof Folder) {
                documentMap.putAll(getCmisObjectMap((Folder) cmisObject, baseFolder + cmisConfiguration.getFolderDelimiter() + cmisObject.getName(), suffixlessKeyFilename));
                return documentMap;
            } else {
                if (cmisObject instanceof Document && (suffixlessKeyFilename == null || cmisObject.getName().startsWith(suffixlessKeyFilename))) {
                    documentMap.put(baseFolder + cmisConfiguration.getFolderDelimiter() + cmisObject.getName(), (Document) cmisObject);
                }
            }
        }
        return documentMap;
    }

    public Document saveDocument(String key, CmisObject cmisObject, Map<String, Object> properties, InputStream is, final Date changeDate) throws IOException {
        // Don't use caching for this process.
        OperationContext oc = createOperationContext();
        oc.setFilter(null);
        oc.setCacheEnabled(false);

        // Split the filename and parent folder from the key.
        int lastFolderDelimiterKeyIndex = key.lastIndexOf(cmisConfiguration.getFolderDelimiter());
        String filenameKey = key.substring(lastFolderDelimiterKeyIndex + 1);


        //Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());
        properties.put(PropertyIds.NAME, filenameKey);

        if (changeDate != null) {
            properties.put(PropertyIds.LAST_MODIFICATION_DATE, changeDate);
        }

        int isLength = is.available();
        ContentStream contentStream = cmisConfiguration.getClient().getObjectFactory().createContentStream(key, isLength, Files.probeContentType(new File(key).toPath()), is);

        // If we have a cmisObject then lets refresh it to make sure it still exists.
        if (cmisObject != null) {
            try {
                cmisObject.refresh();
            } catch (CmisObjectNotFoundException e) {
                cmisObject = null;
            }
        }
        Document doc;
        if (cmisObject != null) {
            try {
                // If the document is found then we are updating the existing document.
                doc = (Document) cmisObject;

                // If using major versioning then we have the option of making next version a minor or major.
                // The CMIS default it to create minor versions on updates.  If we are to create major versions on update then we need to update the document a little different.
                if (cmisConfiguration.getVersioningState().equals(VersioningState.MAJOR) && cmisConfiguration.isVersioningMajorOnUpdate() && doc.isVersionable() && doc.isMajorVersion()) {
                    // If there is an existing checkout then cancel it.
                    if (doc.isVersionSeriesCheckedOut()) {
                        doc.cancelCheckOut();
                    }

                    ObjectId objectID = doc.checkOut();
                    CmisObject o = cmisConfiguration.getClient().getObject(objectID, oc);
                    ((Document) o).checkIn(true, properties, contentStream, null);
                } else {
                    doc.updateProperties(properties, true);
                    doc.setContentStream(contentStream, true, true);
                }

                if (cmisConfiguration.existSecondaryProperty()) {
                    //need to reload document to avoid  "Document is not the latest version" when updating secondary types.
                    doc.refresh();
                }
                // Avoid CMIS API call is info is not enabled.
                if (LogManager.getLogger(Geonet.RESOURCES).isInfoEnabled()) {
                    Log.info(Geonet.RESOURCES,
                        String.format("Updated resource '%s'. Current version '%s'.", key, doc.getVersionLabel()));
                }
            } catch (CmisConstraintException e) {
                Log.warning(Geonet.RESOURCES, String.format(
                    "No allowed to modify existing resource '%s' due to constraint violation or lock.", key));
                throw new NotAllowedException(String.format(
                    "No allowed to modify existing resource '%s' due to constraint violation or lock.", key));
            } catch (CmisPermissionDeniedException e) {
                Log.warning(Geonet.RESOURCES, String.format(
                    "No permissions to update resource '%s'.", key));
                throw new NotAllowedException(String.format(
                    "No permissions to update resource '%s'.", key));
            }
        } else {
            // If the document is not found then we are adding a new document.

            // Get parent folder.
            String parentKey = key.substring(0, lastFolderDelimiterKeyIndex);
            try {
                Folder parentFolder = getFolderCache(parentKey, true, true);

                doc = parentFolder.createDocument(properties, contentStream, cmisConfiguration.getVersioningState(), (List)null, (List)null, (List)null, oc);
                // Avoid CMIS API call is info is not enabled.
                if (LogManager.getLogger(Geonet.RESOURCES).isInfoEnabled()) {
                    Log.info(Geonet.RESOURCES,
                        String.format("Added resource '%s'.", doc.getPaths().get(0)));
                }
            } catch (CmisPermissionDeniedException ex) {
                Log.warning(Geonet.RESOURCES, String.format(
                    "No permissions to add resource '%s'.", key));
                throw new NotAllowedException(String.format(
                    "No permissions to add resource '%s'.", key));
            } catch (ResourceNotFoundException e) {
                throw new IOException("Error getting resource from cache: " + parentKey, e);
            }
        }
        return doc;
    }
}
