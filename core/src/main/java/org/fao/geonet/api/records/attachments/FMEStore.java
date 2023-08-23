/*
 * =============================================================================
 * ===	Copyright (C) 2001-2022 Food and Agriculture Organization of the
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import jeeves.server.context.ServiceContext;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceContainer;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.DateUtil;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpResponse;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A FME store resources files in FME.
 * <p>
 * See https://docs.safe.com/fme/html/FME_REST/apidoc/v3/index.html#!/resources/add_post_14
 *
 * <pre>
 *     datadir
 *      |-{{metadata_uuid}}
 *      |    |--doc.pdf
 * </pre>
 */
public class FMEStore extends AbstractStore {
    /**
     * No support for private visibility. Once published on FME side,
     * documents are visible.
     */
    public static final MetadataResourceVisibility fmeVisibility = MetadataResourceVisibility.PUBLIC;
    public static final String ACCEPT_CONTENTS = "?accept=contents";
    public static final String DEPTH_1 = "?depth=1";
    public static final String CREATE_DIRECTORIES_TRUE_OVERWRITE_TRUE = "?createDirectories=true&overwrite=true";

    @Autowired
    SettingManager settingManager;

    @Autowired
    GeonetHttpRequestFactory httpRequestFactory;

    /**
     * Something like https://docs.safe.com/fmerest/v3/resources/connections/MyTest/filesys/.
     */
    private String fmeApiUrl;

    /**
     * A valid API token.
     */
    private String fmeToken;

    public FMEStore() {
    }

    @Override
    public List<MetadataResource> getResources(ServiceContext context, String metadataUuid,
                                               MetadataResourceVisibility visibility,
                                               String filter, Boolean approved) throws Exception {
        if (visibility == MetadataResourceVisibility.PRIVATE) {
            return new ArrayList<>();
        }
        int metadataId = canDownload(context, metadataUuid, fmeVisibility, approved);

        List<MetadataResource> resourceList = new ArrayList<>();

        String url = fmeApiUrl + metadataUuid + DEPTH_1;
        HttpGet httpGet = new HttpGet(url);
        addFmeTokenHeader(httpGet);
        try (ClientHttpResponse httpResponse = httpRequestFactory.execute(httpGet)) {
            if (httpResponse.getRawStatusCode() == 200) {
                resourceList = getResourcesFromFmeFileList(metadataUuid, filter, approved, metadataId, httpResponse);
            }
        } catch (Exception e) {
            Log.error(Geonet.RESOURCES,
                String.format("Error retrieving file lists for %s. Error is: %s",
                    metadataUuid, e.getMessage()));
        }
        return resourceList;
    }

    @Override
    public ResourceHolder getResource(ServiceContext context, String metadataUuid, MetadataResourceVisibility metadataResourceVisibility, String resourceId, Boolean approved) throws Exception {
        return null;
    }

    private List<MetadataResource> getResourcesFromFmeFileList(String metadataUuid,
                                                               String filter,
                                                               Boolean approved,
                                                               int metadataId,
                                                               ClientHttpResponse httpResponse)
        throws IOException {
        List<MetadataResource> resourceList = new ArrayList<>();
        if (filter == null) {
            filter = FilesystemStore.DEFAULT_FILTER;
        }
        PathMatcher matcher =
            FileSystems.getDefault().getPathMatcher("glob:" + filter);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode tree = objectMapper.readTree(
            CharStreams.toString(new InputStreamReader(httpResponse.getBody())));
        JsonNode files = tree.get("contents");
        if (files.isArray()) {
            for (JsonNode file : files) {
                JsonNode name = file.get("name");
                if (name != null) {
                    Path keyPath = new File(name.asText()).toPath().getFileName();
                    if (matcher.matches(keyPath)) {
                        MetadataResource resource = new FilesystemStoreResource(metadataUuid, metadataId,
                            name.asText(),
                            settingManager.getNodeURL() + "api/records/", fmeVisibility,
                            file.get("size").asLong(),
                            new Date(
                                DateUtil.parseBasicOrFullDateTime(file.get("date").asText())
                                    .toInstant().toEpochMilli()),
                            approved);
                        resourceList.add(resource);
                    }
                }
            }
        }
        return resourceList;
    }

    private void addFmeTokenHeader(HttpRequestBase request) {
        request.setHeader("Authorization",
            String.format("fmetoken token=%s", fmeToken));
    }

    @Override
    public void streamResource(final ServiceContext context,
                               final String metadataUuid,
                               final String resourceId, Boolean approved,
                               OutputStream out) throws Exception {
        canDownload(context, metadataUuid, fmeVisibility, approved);
        checkResourceId(resourceId);

        String url = fmeApiUrl + metadataUuid + "/" + resourceId + ACCEPT_CONTENTS;
        HttpGet httpGet = new HttpGet(url);
        addFmeTokenHeader(httpGet);
        try (ClientHttpResponse httpResponse = httpRequestFactory.execute(httpGet)) {
            if (httpResponse.getRawStatusCode() == 200) {
                IOUtils.copy(httpResponse.getBody(), out);
                out.flush();
                out.close();
            } else {
                throw new ResourceNotFoundException(
                    String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid))
                    .withMessageKey("exception.resourceNotFound.resource", new String[]{resourceId})
                    .withDescriptionKey("exception.resourceNotFound.resource.description", new String[]{resourceId, metadataUuid});
            }
        }
    }


    @Override
    public ResourceHolder getResourceInternal(
        final String metadataUuid,
        final MetadataResourceVisibility visibility,
        final String resourceId,
        Boolean approved) throws Exception {
        String url = fmeApiUrl + metadataUuid + "/" + resourceId + ACCEPT_CONTENTS;
        HttpGet httpGet = new HttpGet(url);
        addFmeTokenHeader(httpGet);
        try (ClientHttpResponse httpResponse = httpRequestFactory.execute(httpGet)) {
            if (httpResponse.getRawStatusCode() == 200) {
                return new ResourceHolderImpl(null);
            } else {
                throw new ResourceNotFoundException(
                    String.format("Metadata resource '%s' not found for metadata '%s'", resourceId, metadataUuid));
            }
        }
    }

    /**
     * Get the resource description or null if the file doesn't exist.
     *
     * @param context      the service context.
     * @param metadataUuid the uuid of the owner metadata record.
     * @param visibility   is the resource is public or not.
     * @param fileName     the path to the resource.
     * @param approved     if the metadata draft has been approved or not
     * @return the resource description or {@code null} if there is any problem accessing the file.
     */
    public MetadataResource getResourceDescription(final ServiceContext context, final String metadataUuid,
                                                   final MetadataResourceVisibility visibility, final String fileName, Boolean approved) {
        try {
            getAndCheckMetadataId(metadataUuid, approved);

            List<MetadataResource> resources =
                getResources(
                    context, metadataUuid, fmeVisibility,
                    fileName, approved);
            if (resources.size() == 1) {
                return resources.get(0);
            }
        } catch (IOException e) {
            Log.error(Geonet.RESOURCES, "Error getting size of file " + fileName + ": "
                + e.getMessage(), e);
        } catch (Exception e) {
            Log.error(Geonet.RESOURCES, "Error in getResourceDescription: "
                + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public MetadataResourceContainer getResourceContainerDescription(ServiceContext context, String metadataUuid, Boolean approved) throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        return new FilesystemStoreResourceContainer(metadataUuid, metadataId, metadataUuid, settingManager.getNodeURL() + "api/records/", approved);
    }


    @Override
    public MetadataResource putResource(final ServiceContext context, final String metadataUuid, final String filename,
                                        final InputStream is, @Nullable final Date changeDate, final MetadataResourceVisibility visibility,
                                        Boolean approved) throws Exception {
        canEdit(context, metadataUuid, approved);
        checkResourceId(filename);

        String url = fmeApiUrl + metadataUuid
            + CREATE_DIRECTORIES_TRUE_OVERWRITE_TRUE;
        HttpPost httpPost = new HttpPost(url);
        addFmeTokenHeader(httpPost);
        httpPost.addHeader("Content-Disposition",
            String.format("attachment; filename=\"%s\"", filename));
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addBinaryBody("file", is, ContentType.APPLICATION_OCTET_STREAM, filename);
        HttpEntity entity = builder.build();
        httpPost.setEntity(entity);

        try (ClientHttpResponse httpResponse = httpRequestFactory.execute(httpPost)) {
            if (httpResponse.getRawStatusCode() == 200) {
                return getResourceDescription(context, metadataUuid, visibility, filename, approved);
            } else {
                throw new ResourceNotFoundException(
                    String.format("Failed to add resource '%s' for metadata '%s'", filename, metadataUuid));
            }
        }
    }

    @Override
    public String delResources(ServiceContext context, String metadataUuid, Boolean approved) throws Exception {
        canEdit(context, metadataUuid, approved);

        String url = fmeApiUrl + metadataUuid;
        HttpDelete httpDelete = new HttpDelete(url);
        addFmeTokenHeader(httpDelete);
        try (ClientHttpResponse httpResponse = httpRequestFactory.execute(httpDelete)) {
            if (httpResponse.getRawStatusCode() == 204) {
                return String.format("Metadata '%s' directory removed.", metadataUuid);
            } else {
                return String.format("Unable to remove metadata '%s' directory.", metadataUuid);
            }
        }
    }

    @Override
    public String delResource(ServiceContext context, String metadataUuid, String resourceId, Boolean approved) throws Exception {
        canEdit(context, metadataUuid, approved);

        String url = fmeApiUrl + metadataUuid + "/" + resourceId;
        HttpDelete httpDelete = new HttpDelete(url);
        addFmeTokenHeader(httpDelete);
        try (ClientHttpResponse httpResponse = httpRequestFactory.execute(httpDelete)) {
            if (httpResponse.getRawStatusCode() == 204) {
                return String.format("MetadataResource '%s' removed.", resourceId);
            } else {
                return String.format("Unable to remove resource '%s'.", resourceId);
            }
        }
    }

    @Override
    public String delResource(final ServiceContext context, final String metadataUuid,
                              final MetadataResourceVisibility visibility,
                              final String resourceId, Boolean approved) throws Exception {
        canEdit(context, metadataUuid, approved);
        return delResource(context, metadataUuid, resourceId, approved);
    }

    @Override
    public MetadataResource patchResourceStatus(ServiceContext context,
                                                String metadataUuid, String resourceId,
                                                MetadataResourceVisibility visibility, Boolean approved) throws Exception {
        throw new UnsupportedOperationException("FME store does not support changing status of resources.");
    }

    public String getFmeToken() {
        return fmeToken;
    }

    public void setFmeToken(String fmeToken) {
        this.fmeToken = fmeToken;
    }

    public String getFmeApiUrl() {
        return fmeApiUrl;
    }

    public void setFmeApiUrl(String fmeApiUrl) {
        this.fmeApiUrl = fmeApiUrl + (fmeApiUrl.endsWith("/") ? "" : "/");
    }

    private static class ResourceHolderImpl implements ResourceHolder {
        private final MetadataResource metadataResource;

        public ResourceHolderImpl(MetadataResource metadataResource) {
            this.metadataResource = metadataResource;
        }

        @Override
        public Path getPath() {
            return null;
        }

        @Override
        public MetadataResource getMetadata() {
            return metadataResource;
        }

        @Override
        public void close() throws IOException {
        }
    }
}
