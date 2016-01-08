package org.fao.geonet.services.metadata.resources;

import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

/**
 * A store allows user to upload resources (eg. files) to
 * a metadata record and retrieve them.
 */
public interface Store {
    /**
     * Retrieve all resources for a metadata. The list of resources
     * depends on current user privileges.
     *
     * @param metadataUuid The metadata UUID
     * @param sort         Sort by resource name or sharing policy {@link Sort}
     * @param filter       a {@link java.nio.file.Files#newDirectoryStream() GLOB expression} to filter resources eg. *.{png|jpg}
     * @return A list of resources
     * @throws Exception
     */
    List<Resource> getResources(String metadataUuid, Sort sort, String filter) throws Exception;

    /**
     * Retrieve all resources for a metadata having a specific sharing policy
     *
     * @param metadataUuid The metadata UUID
     * @param resourceType The type of sharing policy {@link ResourceType}
     * @param filter       a {@link java.nio.file.Files#newDirectoryStream() GLOB expression} to filter resources eg. *.{png|jpg}
     * @return A list of resources
     * @throws Exception
     */
    List<Resource> getResources(String metadataUuid, ResourceType resourceType, String filter) throws Exception;

    /**
     * Retrieve a metadata resource path.
     *
     * @param metadataUuid The metadata UUID
     * @param resourceId    The resource identifier
     * @return  The resource
     * @throws Exception
     */
    Path getResource(String metadataUuid, String resourceId) throws Exception;

    /**
     * Add a new resource from a file.
     *
     * @param metadataUuid The metadata UUID
     * @param file  The resource file
     * @param resourceType The type of sharing policy {@link ResourceType}
     * @return  The resource description
     * @throws Exception
     */
    Resource putResource(String metadataUuid, MultipartFile file, ResourceType resourceType) throws Exception;

    /**
     * Add a new resource from a local file path.
     *
     * @param metadataUuid The metadata UUID
     * @param filePath  The resource local filepath
     * @param resourceType The type of sharing policy {@link ResourceType}
     * @return  The resource description
     * @throws Exception
     */
    Resource putResource(String metadataUuid, Path filePath, ResourceType resourceType) throws Exception;

    /**
     * Add a new resource from a URL.
     *
     * @param metadataUuid The metadata UUID
     * @param fileUrl  The resource file URL
     * @param resourceType The type of sharing policy {@link ResourceType}
     * @return  The resource description
     * @throws Exception
     */
    Resource putResource(String metadataUuid, URL fileUrl, ResourceType resourceType) throws Exception;

    /**
     * Change the resource sharing policy
     *
     * @param metadataUuid The metadata UUID
     * @param resourceId    The resource identifier
     * @param resourceType The type of sharing policy {@link ResourceType}
     * @return
     * @throws Exception
     */
    Resource patchResourceStatus(String metadataUuid, String resourceId, ResourceType resourceType) throws Exception;


    /**
     * Delete all resources for a metadata
     *
     * @param metadataUuid The metadata UUID
     * @return
     * @throws Exception
     */
    String delResource(String metadataUuid) throws Exception;

    /**
     * Delete a resource from the metadata store
     *
     * @param metadataUuid The metadata UUID
     * @param resourceId    The resource identifier
     * @return
     * @throws Exception
     */
    String delResource(String metadataUuid, String resourceId) throws Exception;
}
