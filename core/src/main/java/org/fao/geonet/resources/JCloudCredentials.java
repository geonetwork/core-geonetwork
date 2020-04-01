package org.fao.geonet.resources;


import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

public class JCloudCredentials {
    private BlobStoreContext client = null;
    private ContextBuilder builder = null;

    private String CLOUD_FOLDER_SEPARATOR = "/"; // not sure if this is consistent for all clouds defaulting to "/" and make it a config

    private String provider = System.getenv("JCLOUD_PROVIDER");
    private String baseFolder = System.getenv("JCLOUD_BASEFOLDER");;
    private String storageAccountName = System.getenv("JCLOUD_STORAGEACCOUNTNAME");
    private String storageAccountKey = System.getenv("JCLOUD_STORAGEACCOUNTKEY");
    private String containerName = System.getenv("JCLOUD_CONTAINERNAME");
    private String endpoint = System.getenv("JCLOUD_ENDPOINT");
    private String folderDelimiter = System.getenv("JCLOUD_FOLDERDELIMITER");

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
        if (getFolderDelimiter() == null) {
            throw new RuntimeException("Folder delimiter must be set prior to setting base folder");
        }
        if (baseFolder.endsWith(getFolderDelimiter())) {
            this.baseFolder = baseFolder;
        } else {
            this.baseFolder = baseFolder + getFolderDelimiter();
        }
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public void setFolderDelimiter(String folderDelimiter) {
        this.folderDelimiter = folderDelimiter;
    }

    @PostConstruct
    public void init() {
        if (folderDelimiter == null) {
            folderDelimiter=CLOUD_FOLDER_SEPARATOR;
        }

        // If the base folder was set then run the following to ensure it is formatted correctly.
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
        return client;
    }

    @Nonnull
    public String getProvider() {
        return provider;
    }

    @Nonnull
    public String getContainerName() {
        return containerName;
    }

    public String getBaseFolder() {
        return baseFolder;
    }

    public String getFolderDelimiter() {
        return folderDelimiter;
    }
}
