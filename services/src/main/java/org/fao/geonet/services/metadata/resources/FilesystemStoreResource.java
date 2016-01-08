package org.fao.geonet.services.metadata.resources;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by francois on 31/12/15.
 */
public class FilesystemStoreResource implements Resource {
    private String filename;
    private String url;
    private ResourceType resourceType;
    @Override
    public String getId() {
        return filename;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getType() {
        return resourceType.toString();
    }

    public FilesystemStoreResource(String id, String baseUrl, ResourceType resourceType) {
        this.filename = id;
        this.url = baseUrl + id;
        this.resourceType = resourceType;
    }
}
