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

import java.util.ArrayList;

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
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

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
                folderCache.invalidate(folderKey);
            }
        }
    }

    public void invalidateFolderCacheItem(String folderKey) {
        folderCache.invalidate(folderKey);
    }

    public Folder getFolderCache(String folderKey) throws ResourceNotFoundException, CmisPermissionDeniedException {
        return getFolderCache(folderKey, false);
    }

    public Folder getFolderCache(String folderKey, boolean refresh) throws ResourceNotFoundException, CmisPermissionDeniedException {
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
                        String parentFolderKey = folderKey.substring(0, folderKey.lastIndexOf(cmisConfiguration.getFolderDelimiter()));
                        Folder subFolder = getFolderCache(parentFolderKey, refresh);

                        // synchronize folder creation.
                        // This will prevent cases where multiple files are uploaded on the interface
                        // In this case there will be a race condition to create the same folder.
                        // And if this is not synchronized then there will be a lot or CmisContentAlreadyExistsException errors.
                        Folder folder;
                        synchronized (this) {
                            ObjectId objectId = cmisConfiguration.getClient().createPath(subFolder, folderKey, "cmis:folder");
                            folder = (Folder) cmisConfiguration.getClient().getObject(objectId);
                        }
                        if (refresh) {
                            foundWithoutCache[0] = true;
                        }
                        return folder;
                    }
                }
            });
        } catch (ExecutionException e) {
            throw new ResourceNotFoundException("Error getting resource from cache: " + folderKey, e);
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
        return getCmisObjectMap(folder, baseFolder, null);
    }
    
    public Map<String, Document> getCmisObjectMap(Folder folder, String baseFolder, String suffixlessKeyFilename) {
        if (baseFolder == null) {
            baseFolder="";
        }
        Map<String, Document> documentMap = new HashMap<>();
        for (CmisObject cmisObject : folder.getChildren()) {
            if (cmisObject instanceof Folder) {
                documentMap.putAll(getCmisObjectMap((Folder)cmisObject, baseFolder + cmisConfiguration.getFolderDelimiter() + cmisObject.getName(), suffixlessKeyFilename));
                return documentMap;
            } else {
                if (cmisObject instanceof Document && (suffixlessKeyFilename == null || cmisObject.getName().startsWith(suffixlessKeyFilename))) {
                    documentMap.put(baseFolder + cmisConfiguration.getFolderDelimiter()  + cmisObject.getName(), (Document)cmisObject);
                }
            }
        }
        return documentMap;
    }

}
