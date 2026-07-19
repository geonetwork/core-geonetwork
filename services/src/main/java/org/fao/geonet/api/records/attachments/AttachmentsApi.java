/*
 * =============================================================================
 * ===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.tika.Tika;
import org.apache.commons.codec.digest.DigestUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.domain.MetadataResourceVisibilityConverter;
import org.fao.geonet.events.history.AttachmentAddedEvent;
import org.fao.geonet.events.history.AttachmentDeletedEvent;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.util.FileMimetypeChecker;
import org.fao.geonet.util.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Metadata resource related operations.
 * <p>
 * Load the store with id 'resourceStore'.
 */
@EnableWebMvc
@Service
@RequestMapping(value = {"/{portal}/api/records/{metadataUuid}/attachments"})
@Tag(name = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
public class AttachmentsApi {
    public static final Integer MIN_IMAGE_SIZE = 1;
    public static final Integer MAX_IMAGE_SIZE = 2048;
    public static final Integer BUFFER_SIZE = 8192;
    private final ApplicationContext appContext = ApplicationContextHolder.get();
    private Store store;

    @Autowired
    FileMimetypeChecker fileMimetypeChecker;
    @Autowired
    SettingManager settingManager;

    public AttachmentsApi() {
    }

    public AttachmentsApi(Store store) {
        this.store = store;
    }

    /**
     * Based on the file name return an appropriate mime type.
     *
     * @return The mime type.
     */
    public static String getFileContentType(String filename) {
        Tika tika = new Tika();
        return tika.detect(filename);
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    @PostConstruct
    public void init() {
        if (appContext != null) {
            this.store = appContext.getBean("resourceStore", Store.class);
        }
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(MetadataResourceVisibility.class, new MetadataResourceVisibilityConverter());
        binder.registerCustomEditor(Sort.class, new SortConverter());
    }

    public List<MetadataResource> getResources() {
        return Collections.emptyList();
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "List all metadata attachments", description = "<a href='https://docs.geonetwork-opensource.org/latest/user-guide/associating-resources/using-filestore/'>More info</a>")
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Return the record attachments."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)})
    @ResponseBody
    public List<MetadataResource> getAllResources(
        @Parameter(description = "The metadata UUID", required = true, example = "43d7c186-2187-4bcd-8843-41e575a5ef56") @PathVariable String metadataUuid,
        @Parameter(description = "Sort by", example = "type") @RequestParam(required = false, defaultValue = "name") Sort sort,
        @Parameter(description = "Use approved version or not", example = "true") @RequestParam(required = false, defaultValue = "true") Boolean approved,
        @RequestParam(required = false, defaultValue = FilesystemStore.DEFAULT_FILTER) String filter,
        @Parameter(hidden = true) HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        return store.getResources(context, metadataUuid, sort, filter, approved);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Delete all uploaded metadata resources")
    @RequestMapping(method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Attachment added.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delResources(
        @Parameter(description = "The metadata UUID", required = true, example = "43d7c186-2187-4bcd-8843-41e575a5ef56") @PathVariable String metadataUuid,
        @Parameter(description = "Use approved version or not", example = "true") @RequestParam(required = false, defaultValue = "false") Boolean approved,
        @Parameter(hidden = true) HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        store.delResources(context, metadataUuid, approved);

        String metadataIdString = ApiUtils.getInternalId(metadataUuid, approved);
        if (metadataIdString != null) {
            long metadataId = Long.parseLong(metadataIdString);
            UserSession userSession = ApiUtils.getUserSession(request.getSession());
            new AttachmentDeletedEvent(metadataId, userSession.getUserIdAsInt(), "All attachments")
                .publish(ApplicationContextHolder.get());
        }
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new resource for a given metadata")
    @PreAuthorize("hasAuthority('Editor')")
    @RequestMapping(method = RequestMethod.POST,
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Attachment uploaded."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    @ResponseBody
    public MetadataResource putResource(
        @Parameter(description = "The metadata UUID", required = true, example = "43d7c186-2187-4bcd-8843-41e575a5ef56") @PathVariable String metadataUuid,
        @Parameter(description = "The sharing policy", example = "public") @RequestParam(required = false, defaultValue = "public") MetadataResourceVisibility visibility,
        @Parameter(description = "The file to upload") @RequestParam("file") MultipartFile file,
        @Parameter(description = "Use approved version or not", example = "true") @RequestParam(required = false, defaultValue = "false") Boolean approved,
        @Parameter(hidden = true) HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        String supportedFileMimetypes = settingManager.getValue(Settings.METADATA_EDIT_SUPPORTEDFILEMIMETYPES);
        fileMimetypeChecker.checkValidMimeType(file, supportedFileMimetypes.split("\\|"));

        MetadataResource resource = store.putResource(context, metadataUuid, file, visibility, approved);

        String metadataIdString = ApiUtils.getInternalId(metadataUuid, approved);
        if (metadataIdString != null && file != null && !file.isEmpty()) {
            long metadataId = Long.parseLong(metadataIdString);
            UserSession userSession = ApiUtils.getUserSession(request.getSession());
            new AttachmentAddedEvent(metadataId, userSession.getUserIdAsInt(), file.getOriginalFilename())
                .publish(ApplicationContextHolder.get());
        }

        return resource;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new resource from a URL for a given metadata")
    @PreAuthorize("hasAuthority('Editor')")
    @RequestMapping(method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Attachment added."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    @ResponseBody
    public MetadataResource putResourceFromURL(
        @Parameter(description = "The metadata UUID", required = true, example = "43d7c186-2187-4bcd-8843-41e575a5ef56") @PathVariable String metadataUuid,
        @Parameter(description = "The sharing policy", example = "public") @RequestParam(required = false, defaultValue = "public") MetadataResourceVisibility visibility,
        @Parameter(description = "The URL to load in the store") @RequestParam("url") URL url,
        @Parameter(description = "Use approved version or not", example = "true") @RequestParam(required = false, defaultValue = "false") Boolean approved,
        @Parameter(hidden = true) HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        MetadataResource resource = store.putResource(context, metadataUuid, url, visibility, approved);

        String metadataIdString = ApiUtils.getInternalId(metadataUuid, approved);
        if (metadataIdString != null && url != null) {
            long metadataId = Long.parseLong(metadataIdString);
            UserSession userSession = ApiUtils.getUserSession(request.getSession());
            new AttachmentAddedEvent(metadataId, userSession.getUserIdAsInt(), url.toString())
                .publish(ApplicationContextHolder.get());
        }

        return resource;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Get a metadata resource")
    // @PreAuthorize("permitAll")
    @RequestMapping(value = "/{resourceId:.+}", method = RequestMethod.GET)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Record attachment.",
            content = @Content(schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "206", description = "Partial content for resumable downloads.",
            content = @Content(schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "403", description = "Operation not allowed. "
            + "User needs to be able to download the resource.")})
    public void getResource(
        @Parameter(description = "The metadata UUID", required = true, example = "43d7c186-2187-4bcd-8843-41e575a5ef56") @PathVariable String metadataUuid,
        @Parameter(description = "The resource identifier (ie. filename)", required = true) @PathVariable String resourceId,
        @Parameter(description = "Use approved version or not", example = "true") @RequestParam(required = false, defaultValue = "true") Boolean approved,
        @Parameter(description = "Size (only applies to images). From 1px to 2048px.", example = "200") @RequestParam(required = false) Integer size,
        @Parameter(hidden = true) HttpServletRequest request,
        @Parameter(hidden = true) HttpServletResponse response
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        ApiUtils.canViewRecord(metadataUuid, request);

        // Get the resource metadata
        // We need size, last modified date and etag for the conditional headers
        MetadataResource resourceMetadata = store.getResourceMetadata(context, metadataUuid, resourceId, approved);

        if (resourceMetadata == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        String fileName = resourceMetadata.getFilename();
        long fileLastModifiedDate = resourceMetadata.getLastModification().getTime();
        long fileSize = resourceMetadata.getSize();
        MediaType fileMediaType = MediaType.parseMediaType(getFileContentType(fileName));
        String fileETag = "\"" + DigestUtils.md5Hex(fileName + fileSize + resourceMetadata.getVersion() + fileLastModifiedDate) + "\"";

        // Set common headers
        response.setHeader(HttpHeaders.CACHE_CONTROL, CacheControl.noCache().getHeaderValue());
        response.setDateHeader(HttpHeaders.LAST_MODIFIED, fileLastModifiedDate);
        response.setHeader(HttpHeaders.ETAG, fileETag);
        response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");

        // Check if the request is not modified based on ETag or Last-Modified
        if (new ServletWebRequest(request, response).checkNotModified(fileETag, fileLastModifiedDate)) {
            return;
        }

        HttpRange range = null;
        try {
            List<HttpRange> ranges = HttpRange.parseRanges(request.getHeader(HttpHeaders.RANGE));
            if (!ranges.isEmpty()) {
                range = ranges.get(0);
                if (range.getRangeStart(fileSize) >= fileSize) {
                    throw new IllegalArgumentException("Range start (" + range.getRangeStart(fileSize) +
                        ") must be lower than file size (" + fileSize + ").");
                }
            }
        } catch (IllegalArgumentException e) {
            // invalid range -> 416 with real size
            response.setStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
            response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize);
            return;
        }

        // If the request is a range request, check the If-Range header
        String ifRange = request.getHeader(HttpHeaders.IF_RANGE);
        if (range != null && ifRange != null) {
            if (ifRange.startsWith("W/")) {
                range = null; // weak ETag -> serve full resource
            } else if (ifRange.startsWith("\"")) {
                if (!fileETag.equals(ifRange)) {
                    range = null; // strong ETag mismatch -> serve full resource
                }
            } else {
                try {
                    long ifRangeMillis = ZonedDateTime
                        .parse(ifRange, java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME)
                        .toInstant().toEpochMilli();
                    if (fileLastModifiedDate > ifRangeMillis) {
                        range = null; // newer -> serve full resource
                    }
                } catch (Exception ignored) {
                    range = null; // invalid date -> full
                }
            }
        }

        // If the resource is an image and a size is requested, resize the image and return it
        if (fileMediaType.getType().equals("image") && size != null) {
            try (Store.ResourceHolder resourceHolder = store.getResource(context, metadataUuid, resourceId, approved)) {
                serveResizedImage(resourceHolder.getResource(), fileName, fileETag, size, response);
            }
            return;
        }

        // Set headers for downloads
        response.setContentType(fileMediaType.toString());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileName).build().toString());

        // Get the resource or a range of it
        if (range != null) {
            // Get the range start and end
            long start = range.getRangeStart(fileSize);
            long end = range.getRangeEnd(fileSize);
            // Get the resource for the requested range
            try (Store.ResourceHolder resourceHolder = store.getResourceWithRange(
                context, metadataUuid, resourceId, approved, start, end)) {
                response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
                response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);
                response.setContentLengthLong(end - start + 1);
                streamResource(resourceHolder.getResource(), response);
            }
        } else {
            // Get the full resource
            try (Store.ResourceHolder resourceHolder = store.getResource(context, metadataUuid, resourceId, approved)) {
                response.setStatus(HttpStatus.OK.value());
                response.setContentLengthLong(fileSize);
                streamResource(resourceHolder.getResource(), response);
            }
        }
    }


    @io.swagger.v3.oas.annotations.Operation(summary = "Update the metadata resource visibility")
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Attachment visibility updated."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    @RequestMapping(value = "/{resourceId:.+}", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    public MetadataResource patchResource(
        @Parameter(description = "The metadata UUID", required = true, example = "43d7c186-2187-4bcd-8843-41e575a5ef56") @PathVariable String metadataUuid,
        @Parameter(description = "The resource identifier (ie. filename)", required = true) @PathVariable String resourceId,
        @Parameter(description = "The visibility", required = true, example = "public") @RequestParam(required = true) MetadataResourceVisibility visibility,
        @Parameter(description = "Use approved version or not", example = "true") @RequestParam(required = false, defaultValue = "false") Boolean approved,
        @Parameter(hidden = true) HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        return store.patchResourceStatus(context, metadataUuid, resourceId, visibility, approved);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Delete a metadata resource")
    @PreAuthorize("hasAuthority('Editor')")
    @RequestMapping(value = "/{resourceId:.+}", method = RequestMethod.DELETE)
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Attachment visibility removed.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delResource(
        @Parameter(description = "The metadata UUID", required = true, example = "43d7c186-2187-4bcd-8843-41e575a5ef56") @PathVariable String metadataUuid,
        @Parameter(description = "The resource identifier (ie. filename)", required = true) @PathVariable String resourceId,
        @Parameter(description = "Use approved version or not", example = "true") @RequestParam(required = false, defaultValue = "false") Boolean approved,
        @Parameter(hidden = true) HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        store.delResource(context, metadataUuid, resourceId, approved);

        String metadataIdString = ApiUtils.getInternalId(metadataUuid, approved);
        if (metadataIdString != null) {
            long metadataId = Long.parseLong(metadataIdString);
            UserSession userSession = ApiUtils.getUserSession(request.getSession());
            new AttachmentDeletedEvent(metadataId, userSession.getUserIdAsInt(), resourceId)
                .publish(ApplicationContextHolder.get());
        }
    }

    /**
     * Serves a resized image in PNG format.
     *
     * @param resource Resource representing the original image
     * @param originalImageName Original filename of the image
     * @param originalETag Original resource ETag used to generate resized image ETag
     * @param size Desired size for the resized image (in pixels)
     * @param response Current HTTP response
     * @throws IOException If an I/O error occurs while reading or writing the image
     */
    private void serveResizedImage(Resource resource,
                                   String originalImageName,
                                   String originalETag,
                                   int size,
                                   HttpServletResponse response) throws IOException {
        if (size < MIN_IMAGE_SIZE || size > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException(String.format(
                "Image can only be resized from %d to %d. You requested %d.",
                MIN_IMAGE_SIZE, MAX_IMAGE_SIZE, size));
        }

        try (InputStream inputStream = resource.getInputStream()) {
            // Resize the image
            BufferedImage resizedImage = ImageUtil.resize(ImageIO.read(inputStream), size);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "png", outputStream);

            // Generate a new filename for the resized image
            // Use the original filename without extension and append .png
            String pngFilename = originalImageName.substring(0, originalImageName.lastIndexOf('.')) + ".png";

            // Resized image headers
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(pngFilename).build().toString());
            response.setContentLength(outputStream.size());
            response.setDateHeader(HttpHeaders.LAST_MODIFIED, System.currentTimeMillis());

            // Generate a new ETag based on the original ETag and the size
            response.setHeader(HttpHeaders.ETAG, "\"" + DigestUtils.md5Hex(originalETag + size) + "\"");

            OutputStream servletOutput = response.getOutputStream();
            outputStream.writeTo(servletOutput);
            servletOutput.flush();
        }
    }

    private void streamResource(Resource resource, HttpServletResponse response) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            OutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }
    }
}
