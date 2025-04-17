/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
import org.fao.geonet.domain.MetadataResourceExternalManagementProperties;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

public class JCloudConfiguration {

    private BlobStoreContext client = null;

    private static final String DEFAULT_CLOUD_FOLDER_SEPARATOR = "/"; // not sure if this is consistent for all clouds defaulting to "/" and make it a config
    private static final String DEFAULT_EXTERNAL_RESOURCE_MANAGEMENT_WINDOW_PARAMETERS = "toolbar=0,width=600,height=600";
    private static final Boolean DEFAULT_EXTERNAL_RESOURCE_MANAGEMENT_MODAL_ENABLED = true;
    private static final Boolean DEFAULT_EXTERNAL_RESOURCE_MANAGEMENT_FOLDER_ENABLED = true;
    private static final Boolean DEFAULT_VERSIONING_ENABLED = false;

    private String provider;
    private String baseFolder;
    private String storageAccountName;
    private String storageAccountKey;
    private String containerName;
    private String endpoint;
    private String folderDelimiter = null;

    /**
     * Property name for storing the metadata uuid that is expected to be a String
     */
    private String metadataUUIDPropertyName;
    /**
     * Url used for managing enhanced resource properties related to the metadata.
     */
    private String externalResourceManagementUrl;
    private String externalResourceManagementWindowParameters;
    private Boolean externalResourceManagementModalEnabled;
    private Boolean externalResourceManagementFolderEnabled;
    private String externalResourceManagementFolderRoot;

    /**
     * Property name for storing the changed date as JCloud does not allow changing the last modified date.
     */
    private String externalResourceManagementChangedDatePropertyName;

    /**
     * Property name for storing the creation date of the record.
     */
    private String externalResourceManagementCreatedDatePropertyName;

    /**
     * Property name for validation status that is expected to be an integer with values of null, 0, 1, 2
     * (See MetadataResourceExternalManagementProperties.ValidationStatus for code meaning)
     * If null then validation status will default to UNKNOWN.
     */
    private String externalResourceManagementValidationStatusPropertyName;
    /**
     * Default value to be used for the validation status.
     * If null then it will use INCOMPLETE as the default.
     * Note that if property name is not supplied then it will always default to UNKNOWN
     */
    private String externalResourceManagementValidationStatusDefaultValue;
    private MetadataResourceExternalManagementProperties.ValidationStatus defaultStatus = null;

    /*
     * Enable option to add versioning in the link to the resource.
     */
    private Boolean versioningEnabled;
    /**
     * Property name for storing the version information JCloud does not support versioning.
     */
    private String externalResourceManagementVersionPropertyName;

    /**
     * Property to identify the version strategy to be used.
     */
    public enum VersioningStrategy {
        /**
         * Each new resource change should generate a new version
         * i.e. All new uploads will increase the version including draft and working copy.
         * For workflow, this could cause confusion on working copies which would increase the version in the working copy
         * but when merged only the last version would be merged and could make it look like there are missing versions.
         */
        ALL,
        /**
         * Each new resource change should generate a new version, But working copies will only increase by one version.
         * This will avoid working copy version increases more than one to avoid the issues from ALL (lost versions on merge)
         * This option may be preferred to ALL when workflow is enabled.
         */
        DRAFT,
        /**
         * Add a new version each time a metadata is approved.
         * i.e. draft will remain as version 1 until approved and working copy will only increase by 1 which is what would be used once approved.
         */
        APPROVED
    }

    /**
     * Version strategy to use when generating new versions
     */
    private VersioningStrategy versioningStrategy = VersioningStrategy.ALL;

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
        if (!StringUtils.hasLength(baseFolder)) {
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
        this.externalResourceManagementModalEnabled = BooleanUtils.toBooleanObject(externalResourceManagementModalEnabled);
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

        this.externalResourceManagementFolderRoot = folderRoot;
    }

    public String getExternalResourceManagementValidationStatusDefaultValue() {
        return externalResourceManagementValidationStatusDefaultValue;
    }

    public void setExternalResourceManagementValidationStatusDefaultValue(String externalResourceManagementValidationStatusDefaultValue) {
        this.externalResourceManagementValidationStatusDefaultValue = externalResourceManagementValidationStatusDefaultValue;
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
    }

    public String getExternalResourceManagementVersionPropertyName() {
        return externalResourceManagementVersionPropertyName;
    }

    public void setExternalResourceManagementVersionPropertyName(String externalResourceManagementVersionPropertyName) {
        this.externalResourceManagementVersionPropertyName = externalResourceManagementVersionPropertyName;
    }

    public VersioningStrategy getVersioningStrategy() {
        return versioningStrategy;
    }

    public void setVersioningStrategy(String versioningStrategy) {
        if (StringUtils.hasLength(versioningStrategy)) {
            this.versioningStrategy = VersioningStrategy.valueOf(versioningStrategy);
        }
    }

    public String getMetadataUUIDPropertyName() {
       return metadataUUIDPropertyName;
    }

    public void setMetadataUUIDPropertyName(String metadataUUIDPropertyName) {
        this.metadataUUIDPropertyName = metadataUUIDPropertyName;
    }

    public String getExternalResourceManagementChangedDatePropertyName() {
        return externalResourceManagementChangedDatePropertyName;
    }

    public void setExternalResourceManagementChangedDatePropertyName(String externalResourceManagementChangedDatePropertyName) {
        this.externalResourceManagementChangedDatePropertyName = externalResourceManagementChangedDatePropertyName;
    }

    public String getExternalResourceManagementCreatedDatePropertyName() {
        return externalResourceManagementCreatedDatePropertyName;
    }

    public void setExternalResourceManagementCreatedDatePropertyName(String externalResourceManagementCreatedDatePropertyName) {
        this.externalResourceManagementCreatedDatePropertyName = externalResourceManagementCreatedDatePropertyName;
    }

    public String getExternalResourceManagementValidationStatusPropertyName() {
        return externalResourceManagementValidationStatusPropertyName;
    }

    public void setExternalResourceManagementValidationStatusPropertyName(String externalResourceManagementValidationStatusPropertyName) {
        this.externalResourceManagementValidationStatusPropertyName = externalResourceManagementValidationStatusPropertyName;
    }

    public MetadataResourceExternalManagementProperties.ValidationStatus getValidationStatusDefaultValue() {
        // We only need to set the default if there is a status property supplied, and it is not already set
        if (this.defaultStatus == null &&  StringUtils.hasLength(getExternalResourceManagementValidationStatusPropertyName())) {
            if (getExternalResourceManagementValidationStatusDefaultValue() != null) {
                // If a default property name does exist then use it
                this.defaultStatus = MetadataResourceExternalManagementProperties.ValidationStatus.valueOf(getExternalResourceManagementValidationStatusDefaultValue());
            } else {
                // Otherwise let's default to incomplete.
                // Reason - as the administrator decided to use the status, it most likely means that there are extra properties that need to be set after a file is uploaded so defaulting it to
                // incomplete seems reasonable.
                this.defaultStatus = MetadataResourceExternalManagementProperties.ValidationStatus.INCOMPLETE;
            }
        }
        return this.defaultStatus;
    }

    @PostConstruct
    public void init() {
        if (folderDelimiter == null) {
            folderDelimiter = DEFAULT_CLOUD_FOLDER_SEPARATOR;
        }

        // Run the setBaseFolder following to ensure the baseFolder is formatted correctly.
        setBaseFolder(baseFolder);

        validateMetadataPropertyNames();

        ContextBuilder builder;
        if (storageAccountName != null && provider != null) {
            builder = ContextBuilder.newBuilder(provider).credentials(storageAccountName, storageAccountKey);
            storageAccountName = null;
            storageAccountKey = null;
        } else {
            throw new RuntimeException("Need to supply storage account name and provider for JCloud configuration");
        }


        if (endpoint != null) {
            builder.endpoint(endpoint);
        }

        client = builder.buildView(BlobStoreContext.class);

        if (containerName == null) {
            throw new RuntimeException("Missing the container Name configuration");
        }
    }

    /**
     * Checks if the metadata names that were supplied are correct.
     *
     * @throws IllegalArgumentException is any of the metadata property names are invalid.
     */
    private void validateMetadataPropertyNames() throws  IllegalArgumentException {

        // If provider not supplied then nothing to check.
        if (this.provider == null) {
            return;
        }

        String[] names = {
            getMetadataUUIDPropertyName(),
            getExternalResourceManagementChangedDatePropertyName(),
            getExternalResourceManagementValidationStatusPropertyName(),
            getExternalResourceManagementCreatedDatePropertyName(),
            getExternalResourceManagementVersionPropertyName()
        };

        JCloudMetadataNameValidator.validateMetadataNamesForProvider(provider, names);
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
