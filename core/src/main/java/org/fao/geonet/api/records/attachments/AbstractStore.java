/*
 * =============================================================================
 * ===	Copyright (C) 2024 Food and Agriculture Organization of the
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
import org.apache.commons.io.FilenameUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.exception.InputStreamLimitExceededException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.util.LimitedInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractStore implements Store {
    protected static final String RESOURCE_MANAGEMENT_EXTERNAL_PROPERTIES_SEPARATOR = ":";
    protected static final String RESOURCE_MANAGEMENT_EXTERNAL_PROPERTIES_ESCAPED_SEPARATOR = "\\:";
    private static final Logger log = LoggerFactory.getLogger(AbstractStore.class);

    @Value("${api.params.maxUploadSize}")
    protected long maxUploadSize;

    @Override
    public final List<MetadataResource> getResources(final ServiceContext context, final String metadataUuid, final Sort sort,
            final String filter) throws Exception {
        return getResources(context, metadataUuid, sort, filter, true);
    }

    @Override
    public List<MetadataResource> getResources(final ServiceContext context, final String metadataUuid,
            final MetadataResourceVisibility metadataResourceVisibility, final String filter) throws Exception {
        return getResources(context, metadataUuid, metadataResourceVisibility, filter, true);
    }

    @Override
    public List<MetadataResource> getResources(ServiceContext context, String metadataUuid, Sort sort, String filter, Boolean approved)
            throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        boolean canEdit = getAccessManager(context).canEdit(context, String.valueOf(metadataId));

        List<MetadataResource> resourceList = new ArrayList<>(
                getResources(context, metadataUuid, MetadataResourceVisibility.PUBLIC, filter, approved));
        if (canEdit) {
            resourceList.addAll(getResources(context, metadataUuid, MetadataResourceVisibility.PRIVATE, filter, approved));
        }

        if (sort == Sort.name) {
            resourceList.sort(MetadataResourceVisibility.sortByFileName);
        }

        return resourceList;
    }

    @Override
    public final ResourceHolder getResource(ServiceContext context, String metadataUuid, String resourceId) throws Exception {
        return getResource(context, metadataUuid, resourceId, true);
    }

    @Override
    public final ResourceHolder getResource(ServiceContext context, String metadataUuid, String resourceId, Boolean approved)
            throws Exception {
        try {
            return getResource(context, metadataUuid, MetadataResourceVisibility.PUBLIC, resourceId, approved);
        } catch (ResourceNotFoundException ignored) {
        }
        return getResource(context, metadataUuid, MetadataResourceVisibility.PRIVATE, resourceId, approved);
    }

    @Override
    public final MetadataResource getResourceMetadata(ServiceContext context, String metadataUuid, String resourceId, Boolean approved) throws Exception {
        try {
            return getResourceMetadata(context, metadataUuid, MetadataResourceVisibility.PUBLIC, resourceId, approved);
        } catch (ResourceNotFoundException ignored) {
        }
        return getResourceMetadata(context, metadataUuid, MetadataResourceVisibility.PRIVATE, resourceId, approved);
    }

    @Override
    public ResourceHolder getResourceWithRange(ServiceContext context, String metadataUuid, String resourceId, Boolean approved, long start, long end) throws Exception {
        try {
            return getResourceWithRange(context, metadataUuid, MetadataResourceVisibility.PUBLIC, resourceId, approved, start, end);
        } catch (ResourceNotFoundException ignored) {
        }
        return getResourceWithRange(context, metadataUuid, MetadataResourceVisibility.PRIVATE, resourceId, approved, start, end);
    }

    protected static AccessManager getAccessManager(final ServiceContext context) {
        return ApplicationContextHolder.get().getBean(AccessManager.class);
    }

    public static int getAndCheckMetadataId(String metadataUuid, Boolean approved) throws Exception {
        final ApplicationContext _appContext = ApplicationContextHolder.get();
        final AbstractMetadata metadata;
        if (approved) {
            metadata = _appContext.getBean(MetadataRepository.class).findOneByUuid(metadataUuid);
        } else {
            metadata = _appContext.getBean(IMetadataUtils.class).findOneByUuid(metadataUuid);
        }
        if (metadata == null) {
            throw new ResourceNotFoundException(String.format("Metadata with UUID '%s' not found.", metadataUuid))
                .withMessageKey("exception.resourceNotFound.metadata")
                .withDescriptionKey("exception.resourceNotFound.metadata.description", new String[]{ metadataUuid });
        }
        return metadata.getId();
    }

    protected int canEdit(ServiceContext context, String metadataUuid, Boolean approved) throws Exception {
        return canEdit(context, metadataUuid, null, approved);
    }

    protected int canEdit(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, Boolean approved)
            throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        boolean canEdit = getAccessManager(context).canEdit(context, String.valueOf(metadataId));
        if ((visibility == null && !canEdit) || (visibility == MetadataResourceVisibility.PRIVATE && !canEdit)) {
            throw new SecurityException(String.format("User '%s' does not have privileges to access '%s' resources for metadata '%s'.",
                                                      context.getUserSession() != null ?
                                                              context.getUserSession().getUsername() + "/" + context.getUserSession()
                                                                      .getProfile() :
                                                              "anonymous", visibility == null ? "any" : visibility, metadataUuid));
        }
        return metadataId;
    }

    protected int canDownload(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, Boolean approved)
            throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        if (visibility == MetadataResourceVisibility.PRIVATE) {
            boolean canDownload = getAccessManager(context).canDownload(context, String.valueOf(metadataId));
            if (!canDownload) {
                throw new SecurityException(String.format(
                        "Current user can't download resources for metadata '%s' and as such can't access the requested resource.",
                        metadataUuid));
            }
        }
        return metadataId;
    }

    protected String getFilenameFromUrl(final URL fileUrl) {
        String fileName = FilenameUtils.getName(fileUrl.getPath());
        if (fileName.contains("?")) {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }
        fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        return fileName;
    }

    @Override
    public final MetadataResource putResource(final ServiceContext context, final String metadataUuid, final MultipartFile file,
            final MetadataResourceVisibility visibility) throws Exception {
        return putResource(context, metadataUuid, file.getOriginalFilename(), file.getInputStream(), null, visibility, true);
    }

    @Override
    public final MetadataResource putResource(final ServiceContext context, final String metadataUuid, final MultipartFile file,
            final MetadataResourceVisibility visibility, Boolean approved) throws Exception {
        if (org.apache.commons.lang3.StringUtils.contains(file.getOriginalFilename(),';')) {
            throw new NotAllowedException(String.format(
                "Uploaded resource '%s' contains forbidden character ; for metadata '%s'.", file.getOriginalFilename(), metadataUuid));
        }
        return putResource(context, metadataUuid, file.getOriginalFilename(), file.getInputStream(), null, visibility, approved);
    }

    @Override
    public final MetadataResource putResource(final ServiceContext context, final String metadataUuid, final Resource resource,
            final MetadataResourceVisibility visibility) throws Exception {
        return putResource(context, metadataUuid, resource, visibility, true);
    }

    @Override
    public final MetadataResource putResource(final ServiceContext context, final String metadataUuid, final Resource resource,
            final MetadataResourceVisibility visibility, Boolean approved) throws Exception {
        try (InputStream is = resource.getInputStream()) {
            return putResource(context, metadataUuid, resource.getFilename(), is, null, visibility, approved);
        }
    }

    @Override
    public final MetadataResource putResource(ServiceContext context, String metadataUuid, URL fileUrl,
            MetadataResourceVisibility visibility) throws Exception {
        return putResource(context, metadataUuid, fileUrl, visibility, true);
    }

    @Override
    public final MetadataResource putResource(ServiceContext context, String metadataUuid, URL fileUrl,
            MetadataResourceVisibility visibility, Boolean approved) throws Exception {

        // Open a connection to the URL
        HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("GET");

        // Check if the response code is OK
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Unexpected response code: " + responseCode);
        }

        // Extract filename from Content-Disposition header if present otherwise use the filename from the URL
        String contentDisposition = connection.getHeaderField(HttpHeaders.CONTENT_DISPOSITION);
        String filename = null;
        if (contentDisposition != null) {
            filename = ContentDisposition.parse(contentDisposition).getFilename();
        }
        // If follow redirect, get the filename from the redirected URL
        if (filename == null && connection.getInstanceFollowRedirects()) {
            URL redirectUrl = connection.getURL();
            if (redirectUrl != null) {
                filename = getFilenameFromUrl(redirectUrl);
            }
        }
        if (filename == null || filename.isEmpty()) {
            filename = getFilenameFromUrl(fileUrl);
        }

        // Check if the content length is within the allowed limit
        long contentLength = connection.getContentLengthLong();
        if (contentLength > maxUploadSize) {
            throw new InputStreamLimitExceededException(maxUploadSize, contentLength);
        }

        // Upload the resource while ensuring the input stream does not exceed the maximum allowed size.
        try (LimitedInputStream is = new LimitedInputStream(connection.getInputStream(), maxUploadSize, contentLength)) {
            return putResource(context, metadataUuid, filename, is, null, visibility, approved);
        }
    }

    @Override
    public String delResources(final ServiceContext context, final String metadataUuid) throws Exception {
        return delResources(context, metadataUuid, true);
    }

    @Override
    public String delResources(final ServiceContext context, final String metadataUuid, Boolean approved) throws Exception {
        int metadataId = canEdit(context, metadataUuid, approved);
        return delResources(context, metadataId);
    }

    @Override
    public String delResource(final ServiceContext context, final String metadataUuid, final String resourceId) throws Exception {
        return delResource(context, metadataUuid, resourceId, true);
    }

    @Override
    public MetadataResource patchResourceStatus(final ServiceContext context, final String metadataUuid, final String resourceId,
            final MetadataResourceVisibility metadataResourceVisibility) throws Exception {
        return patchResourceStatus(context, metadataUuid, resourceId, metadataResourceVisibility, true);
    }

    @Override
    public void copyResources(ServiceContext context, String sourceUuid, String targetUuid, MetadataResourceVisibility metadataResourceVisibility, boolean sourceApproved, boolean targetApproved) throws Exception {
        final List<MetadataResource> resources = getResources(context, sourceUuid, metadataResourceVisibility, null, sourceApproved);
        for (MetadataResource resource: resources) {
            try (Store.ResourceHolder holder = getResource(context, sourceUuid, metadataResourceVisibility, resource.getFilename(), sourceApproved)) {
                putResource(context, targetUuid, holder.getResource(), metadataResourceVisibility, targetApproved);
            }
        }
    }

    protected String getFilename(final String metadataUuid, final String resourceId) {
        // It's not always clear when we get a resourceId or a filename
        String prefix = metadataUuid + "/attachments/";
        if (resourceId.startsWith(prefix)) {
            // It was a resourceId
            return resourceId.substring(prefix.length());
        } else {
            // It was a filename
            return resourceId;
        }
    }

    protected void checkResourceId(final String resourceId) {
        if (resourceId.contains("..") || resourceId.startsWith("/") || resourceId.startsWith("file:/")) {
            throw new SecurityException(String.format("Invalid resource identifier '%s'.", resourceId));
        }
    }

    public ResourceManagementExternalProperties getResourceManagementExternalProperties() {
        return new ResourceManagementExternalProperties() {
            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public String getWindowParameters() {
                return null;
            }

            @Override
            public boolean isModal() {
                return false;
            }

            @Override
            public boolean isFolderEnabled() {
                return false;
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

    private String escapeResourceManagementExternalProperties(String value) {
        return value.replace(RESOURCE_MANAGEMENT_EXTERNAL_PROPERTIES_SEPARATOR, RESOURCE_MANAGEMENT_EXTERNAL_PROPERTIES_ESCAPED_SEPARATOR);
    }

    /**
     * Create an encoded base 64 object id contains the following fields to uniquely identify the resource
     * The fields are separated by a colon ":"
     * @param type to identify type of storage - document/folder
     * @param visibility of the resource public/private
     * @param metadataId internal metadata id
     * @param version identifier which can be used to directly get this version.
     * @param resourceId or filename of the resource
     * @return based 64 object id
     */
    protected String getResourceManagementExternalPropertiesObjectId(final String type, final MetadataResourceVisibility visibility, final Integer metadataId, final String version,
                                                                     final String resourceId) {
        return Base64.getEncoder().encodeToString(
            ((type + RESOURCE_MANAGEMENT_EXTERNAL_PROPERTIES_SEPARATOR +
                escapeResourceManagementExternalProperties(visibility == null ? "" : visibility.toString().toLowerCase()) + RESOURCE_MANAGEMENT_EXTERNAL_PROPERTIES_SEPARATOR +
                metadataId + RESOURCE_MANAGEMENT_EXTERNAL_PROPERTIES_SEPARATOR +
                escapeResourceManagementExternalProperties(version == null ? "" : version) + RESOURCE_MANAGEMENT_EXTERNAL_PROPERTIES_SEPARATOR +
                escapeResourceManagementExternalProperties(resourceId)).getBytes()));
    }
}
