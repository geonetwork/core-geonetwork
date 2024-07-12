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

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

public class JCloudConfiguration {
    private BlobStoreContext client = null;
    private ContextBuilder builder = null;

    private String DEFAULT_CLOUD_FOLDER_SEPARATOR = "/"; // not sure if this is consistent for all clouds defaulting to "/" and make it a config
    private final String DEFAULT_EXTERNAL_RESOURCE_MANAGEMENT_WINDOW_PARAMETERS = "toolbar=0,width=600,height=600";
    private final Boolean DEFAULT_EXTERNAL_RESOURCE_MANAGEMENT_MODAL_ENABLED = true;
    private final Boolean DEFAULT_EXTERNAL_RESOURCE_MANAGEMENT_FOLDER_ENABLED = true;
    private final Boolean DEFAULT_VERSIONING_ENABLED = false;

    private String provider;
    private String baseFolder;
    private String storageAccountName;
    private String storageAccountKey;
    private String containerName;
    private String endpoint;
    private String folderDelimiter = null;

    /**
     * Url used for managing enhanced resource properties related to the metadata.
     */
    private String externalResourceManagementUrl;
    private String externalResourceManagementWindowParameters;
    private Boolean externalResourceManagementModalEnabled;
    private Boolean externalResourceManagementFolderEnabled;
    private String externalResourceManagementFolderRoot;

    /*
     * Enable option to add versioning in the link to the resource.
     */
    private Boolean versioningEnabled;


    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setStorageAccountName(String storageAccountName) {
        this.storageAccountName = storageAccountName;
    }

    public void setStorageAccountKey(String storageAccountKey) {
        this.storageAccountKey = storageAccountKey;
    }

    public void setBaseFolder(String baseFolder) {
        if (this.folderDelimiter == null) {
            this.folderDelimiter = DEFAULT_CLOUD_FOLDER_SEPARATOR;
        }
        if (StringUtils.isEmpty(baseFolder)) {
            this.baseFolder = this.folderDelimiter;
        } else {
            if (baseFolder.endsWith(this.folderDelimiter)) {
                this.baseFolder = baseFolder;
            } else {
                this.baseFolder = baseFolder + this.folderDelimiter;
            }
        }
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public void setFolderDelimiter(String folderDelimiter) {
        if (this.folderDelimiter != null && !this.folderDelimiter.equals(folderDelimiter)) {
            throw new RuntimeException("Folder delimiter cannot be changed once set. Ensure that it is set prior to setting base folder");
        }
        this.folderDelimiter = folderDelimiter;
    }

    @Nonnull
    public String getExternalResourceManagementUrl() {
        return externalResourceManagementUrl;
    }

    public void setExternalResourceManagementUrl(String externalResourceManagementUrl) {
        this.externalResourceManagementUrl = externalResourceManagementUrl;
    }

    @Nonnull
    public String getExternalResourceManagementWindowParameters() {
        if (externalResourceManagementWindowParameters == null) {
            return DEFAULT_EXTERNAL_RESOURCE_MANAGEMENT_WINDOW_PARAMETERS;
        } else {
            return externalResourceManagementWindowParameters;
        }
    }

    public void setExternalResourceManagementWindowParameters(String externalResourceManagementWindowParameters) {
        this.externalResourceManagementWindowParameters = externalResourceManagementWindowParameters;
    }

    @Nonnull
    public Boolean isExternalResourceManagementModalEnabled() {
        if (externalResourceManagementModalEnabled == null) {
            return DEFAULT_EXTERNAL_RESOURCE_MANAGEMENT_MODAL_ENABLED;
        } else {
            return externalResourceManagementModalEnabled;
        }
    }

    public void setExternalResourceManagementModalEnabled(Boolean externalResourceManagementModalEnabled) {
        this.externalResourceManagementModalEnabled = externalResourceManagementModalEnabled;
    }

    public void setExternalResourceManagementModalEnabled(String externalResourceManagementModalEnabled) {
        this.externalResourceManagementModalEnabled = BooleanUtils.toBooleanObject(externalResourceManagementModalEnabled);;
    }

    public Boolean isExternalResourceManagementFolderEnabled() {
        if (externalResourceManagementFolderEnabled == null) {
            return DEFAULT_EXTERNAL_RESOURCE_MANAGEMENT_FOLDER_ENABLED;
        } else {
            return externalResourceManagementFolderEnabled;
        }
    }

    public void setExternalResourceManagementFolderEnabled(Boolean externalResourceManagementFolderEnabled) {
        this.externalResourceManagementFolderEnabled = externalResourceManagementFolderEnabled;
    }

    public String getExternalResourceManagementFolderRoot() {
        return this.externalResourceManagementFolderRoot;
    }

    public void setExternalResourceManagementFolderRoot(String externalResourceManagementFolderRoot) {
        String folderRoot = externalResourceManagementFolderRoot;
        if (folderRoot != null) {
            if (!folderRoot.startsWith(getFolderDelimiter())) {
                folderRoot = getFolderDelimiter() + folderRoot;
            }
            if (folderRoot.endsWith(getFolderDelimiter())) {
                folderRoot = folderRoot.substring(0, folderRoot.length() - 1);
            }
        }

        this.externalResourceManagementFolderRoot=folderRoot;
    }

    @Nonnull
    public Boolean isVersioningEnabled() {
        if (versioningEnabled == null) {

            return DEFAULT_VERSIONING_ENABLED;
        } else {
            return versioningEnabled;
        }
    }

    public void setVersioningEnabled(Boolean versioningEnabled) {
        this.versioningEnabled = versioningEnabled;
    }

    public void setVersioningEnabled(String versioningEnabled) {
        this.versioningEnabled = BooleanUtils.toBooleanObject(versioningEnabled);
        ;
    }

    @PostConstruct
    public void init() {
        if (folderDelimiter == null) {
            folderDelimiter = DEFAULT_CLOUD_FOLDER_SEPARATOR;
        }

        // Run the setBaseFolder following to ensure the baseFolder is formatted correctly.
        setBaseFolder(baseFolder);

        if (storageAccountName != null && provider != null) {
            builder = ContextBuilder.newBuilder(provider).credentials(storageAccountName, storageAccountKey);
            storageAccountName = null;
            storageAccountKey = null;
        }

        if (endpoint != null) {
            builder.endpoint(endpoint);
        }

        client = builder.buildView(BlobStoreContext.class);

        builder = null;
        if (containerName == null) {
            throw new RuntimeException("Missing the container Name configuration");
        }
    }

    @Nonnull
    public BlobStoreContext getClient() {
        return this.client;
    }

    @Nonnull
    public String getProvider() {
        return this.provider;
    }

    @Nonnull
    public String getContainerName() {
        return this.containerName;
    }

    public String getBaseFolder() {
        return this.baseFolder;
    }

    public String getFolderDelimiter() {
        return this.folderDelimiter;
    }
}
