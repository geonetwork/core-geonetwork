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
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.springframework.web.multipart.MultipartFile;

import java.io.Closeable;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;

/**
 * A store allows user to upload resources (eg. files) to a metadata record and retrieve them.
 */
public interface Store {
    /**
     * Retrieve all resources for a metadata. The list of resources depends on current user
     * privileges.
     *
     *
     * @param context
     * @param metadataUuid The metadata UUID
     * @param sort         Sort by resource name or sharing policy {@link Sort}
     * @param filter       a {@link java.nio.file.Files#newDirectoryStream(Path)} GLOB expression}
     *                     to filter resources eg. *.{png|jpg}
     * @return A list of resources
     */
	@Deprecated
    List<MetadataResource> getResources(ServiceContext context, String metadataUuid, Sort sort, String filter) throws Exception;

    /**
     * Retrieve all resources for a metadata. The list of resources depends on current user
     * privileges.
     *
     *
     * @param context
     * @param metadataUuid The metadata UUID
     * @param sort         Sort by resource name or sharing policy {@link Sort}
     * @param filter       a {@link java.nio.file.Files#newDirectoryStream(Path)} GLOB expression}
     *                     to filter resources eg. *.{png|jpg}
     * @param approved   Return the approved version or not
     * @return A list of resources
     */
    List<MetadataResource> getResources(ServiceContext context, String metadataUuid, Sort sort, String filter, Boolean approved) throws Exception;

    /**
     * Retrieve all resources for a metadata having a specific sharing policy
     *
     *
     * @param context
     * @param metadataUuid               The metadata UUID
     * @param metadataResourceVisibility The type of sharing policy {@link MetadataResourceVisibility}
     * @param filter                     a {@link java.nio.file.Files#newDirectoryStream(Path) GLOB
     *                                   expression} to filter resources eg. *.{png|jpg}
     * @return A list of resources
     */
	@Deprecated
    List<MetadataResource> getResources(ServiceContext context, String metadataUuid, MetadataResourceVisibility metadataResourceVisibility, String filter) throws Exception;
    /**
     * Retrieve all resources for a metadata having a specific sharing policy
     *
     *
     * @param context
     * @param metadataUuid               The metadata UUID
     * @param metadataResourceVisibility The type of sharing policy {@link MetadataResourceVisibility}
     * @param filter                     a {@link java.nio.file.Files#newDirectoryStream(Path) GLOB
     *                                   expression} to filter resources eg. *.{png|jpg}
     * @param approved   Return the approved version or not
     * @return A list of resources
     */
    List<MetadataResource> getResources(ServiceContext context, String metadataUuid, MetadataResourceVisibility metadataResourceVisibility, String filter, Boolean approved) throws Exception;

    /**
     * Retrieve a metadata resource path.
     *
     *
     * @param context
     * @param metadataUuid The metadata UUID
     * @param resourceId   The resource identifier of its filename
     * @return The resource
     */
	@Deprecated
    ResourceHolder getResource(ServiceContext context, String metadataUuid, String resourceId) throws Exception;

    /**
     * Retrieve a metadata resource path.
     *
     *
     * @param context
     * @param metadataUuid The metadata UUID
     * @param metadataResourceVisibility The type of sharing policy {@link MetadataResourceVisibility}
     * @param resourceId   The resource identifier of its filename
     * @return The resource
     */
    ResourceHolder getResource(ServiceContext context, String metadataUuid, MetadataResourceVisibility metadataResourceVisibility,
            String resourceId, Boolean approved) throws Exception;

    /**
     * Retrieve a metadata resource path.
     *
     *
     * @param context
     * @param metadataUuid The metadata UUID
     * @param resourceId   The resource identifier
     * @param approved   Return the approved version or not
     * @return The resource
     */
    ResourceHolder getResource(ServiceContext context, String metadataUuid, String resourceId, Boolean approved) throws Exception;

    /**
     * Add a new resource from a file.
     *
     * @param context
     * @param metadataUuid               The metadata UUID
     * @param file                       The resource file
     * @param metadataResourceVisibility The type of sharing policy {@link MetadataResourceVisibility}
     * @return The resource description
     */
    @Deprecated
    MetadataResource putResource(ServiceContext context, String metadataUuid, MultipartFile file, MetadataResourceVisibility metadataResourceVisibility) throws Exception;

    /**
     * Add a new resource from a file.
     *
     * @param context
     * @param metadataUuid               The metadata UUID
     * @param file                       The resource file
     * @param metadataResourceVisibility The type of sharing policy {@link MetadataResourceVisibility}
     * @param approved   Put the approved version or not
     * @return The resource description
     */
    MetadataResource putResource(ServiceContext context, String metadataUuid, MultipartFile file,
                                 MetadataResourceVisibility metadataResourceVisibility, Boolean approved) throws Exception;

    /**
     * Add a new resource from a file.
     *
     *
     * @param context
     * @param metadataUuid               The metadata UUID
     * @param filename                   The resource filename
     * @param is                         The input stream
     * @param changeDate                 The optional change date
     * @param metadataResourceVisibility The type of sharing policy {@link MetadataResourceVisibility}
     * @param approved   Put the approved version or not
     * @return The resource description
     */
    MetadataResource putResource(ServiceContext context, String metadataUuid, String filename, InputStream is,
                                 @Nullable Date changeDate,
                                 MetadataResourceVisibility metadataResourceVisibility, Boolean approved)
        throws Exception;

    /**
     * Add a new resource from a local file path.
     *
     *
     * @param context
     * @param metadataUuid               The metadata UUID
     * @param filePath                   The resource local filepath
     * @param metadataResourceVisibility The type of sharing policy {@link MetadataResourceVisibility}
     * @param approved   Return the approved version or not
     * @return The resource description
     */
    MetadataResource putResource(ServiceContext context, String metadataUuid, Path filePath, MetadataResourceVisibility metadataResourceVisibility, Boolean approved) throws Exception;

    /**
     * Add a new resource from a local file path.
     *
     *
     * @param context
     * @param metadataUuid               The metadata UUID
     * @param filePath                   The resource local filepath
     * @param metadataResourceVisibility The type of sharing policy {@link MetadataResourceVisibility}
     * @return The resource description
     */
	@Deprecated
    MetadataResource putResource(ServiceContext context, String metadataUuid, Path filePath, MetadataResourceVisibility metadataResourceVisibility) throws Exception;

    /**
     * Add a new resource from a URL.
     *
     *
     * @param context
     * @param metadataUuid               The metadata UUID
     * @param fileUrl                    The resource file URL
     * @param metadataResourceVisibility The type of sharing policy {@link MetadataResourceVisibility}
     * @return The resource description
     */
	@Deprecated
    MetadataResource putResource(ServiceContext context, String metadataUuid, URL fileUrl, MetadataResourceVisibility metadataResourceVisibility) throws Exception;

    /**
     * Add a new resource from a URL.
     *
     *
     * @param context
     * @param metadataUuid               The metadata UUID
     * @param fileUrl                    The resource file URL
     * @param metadataResourceVisibility The type of sharing policy {@link MetadataResourceVisibility}
     * @param approved   Return the approved version or not
     * @return The resource description
     */
    MetadataResource putResource(ServiceContext context, String metadataUuid, URL fileUrl, MetadataResourceVisibility metadataResourceVisibility, Boolean approved) throws Exception;

    /**
     * Change the resource sharing policy
     *
     * @param context
     * @param metadataUuid               The metadata UUID
     * @param resourceId                 The resource identifier
     * @param metadataResourceVisibility The type of sharing policy {@link MetadataResourceVisibility}
     */
	@Deprecated
    MetadataResource patchResourceStatus(ServiceContext context, String metadataUuid, String resourceId, MetadataResourceVisibility metadataResourceVisibility) throws Exception;

    /**
     * Change the resource sharing policy
     *
     * @param context
     * @param metadataUuid               The metadata UUID
     * @param resourceId                 The resource identifier
     * @param metadataResourceVisibility The type of sharing policy {@link MetadataResourceVisibility}
     * @param approved   Return the approved version or not
     */
    MetadataResource patchResourceStatus(ServiceContext context, String metadataUuid, String resourceId, MetadataResourceVisibility metadataResourceVisibility, Boolean approved) throws Exception;


    /**
     * Delete all resources for a metadata
     *
     * @param context
     * @param metadataUuid The metadata UUID
     */
	@Deprecated
    String delResources(ServiceContext context, String metadataUuid) throws Exception;

    /**
     * Delete all resources for a metadata
     *
     * @param context
     * @param metadataUuid The metadata UUID
     * @param approved   Return the approved version or not
     */
    String delResources(ServiceContext context, String metadataUuid, Boolean approved) throws Exception;

    /**
     * Delete a resource from the metadata store
     *
     * @param context
     * @param metadataUuid The metadata UUID
     * @param resourceId   The resource identifier
     */
	@Deprecated
    String delResource(ServiceContext context, String metadataUuid, String resourceId) throws Exception;

    /**
     * Delete a resource from the metadata store
     *
     * @param context
     * @param metadataUuid The metadata UUID
     * @param resourceId   The resource identifier
     * @param approved   Return the approved version or not
     */
    String delResource(ServiceContext context, String metadataUuid, String resourceId, Boolean approved) throws Exception;

    /**
     * Delete a resource from the metadata store
     *
     * @param context
     * @param metadataUuid The metadata UUID
     * @param metadataResourceVisibility The type of sharing policy {@link MetadataResourceVisibility}
     * @param resourceId   The resource identifier
     * @param approved   Return the approved version or not
     */
    String delResource(ServiceContext context, String metadataUuid,  MetadataResourceVisibility metadataResourceVisibility,
                       String resourceId, Boolean approved) throws Exception;

    /**
     * Get the resource description.
     * @param context
     * @param metadataUuid The metadata UUID
     * @param visibility The type of sharing policy {@link MetadataResourceVisibility}
     * @param filename The filename
     * @return The description or null if the file doesn't exist
     */
    MetadataResource getResourceDescription(final ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility,
                                            String filename, Boolean approved) throws Exception;

    interface ResourceHolder extends Closeable {
        Path getPath();
        MetadataResource getMetadata();
    }
}
