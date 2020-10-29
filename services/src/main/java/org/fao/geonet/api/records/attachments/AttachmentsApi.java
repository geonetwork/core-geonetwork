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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.domain.MetadataResourceVisibilityConverter;
import org.fao.geonet.events.history.AttachmentAddedEvent;
import org.fao.geonet.events.history.AttachmentDeletedEvent;
import org.fao.geonet.util.ImageUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Metadata resource related operations.
 * <p>
 * Load the store with id 'resourceStore'.
 */
@EnableWebMvc
@Service
@RequestMapping(value = {"/{portal}/api/records/{metadataUuid}/attachments"})
@Tag(name = "records", description = "Metadata record operations")
public class AttachmentsApi {
    public static final Integer MIN_IMAGE_SIZE = 1;
    public static final Integer MAX_IMAGE_SIZE = 2048;
    private final ApplicationContext appContext = ApplicationContextHolder.get();
    private Store store;

    public AttachmentsApi() {
    }

    public AttachmentsApi(Store store) {
        this.store = store;
    }

    /**
     * Based on the file content or file extension return an appropiate mime type.
     *
     * @return The mime type or application/{{file_extension}} if none found.
     */
    public static String getFileContentType(Path file) throws IOException {
        String contentType = Files.probeContentType(file);
        if (contentType == null) {
            String ext = com.google.common.io.Files.getFileExtension(file.getFileName().toString()).toLowerCase();
            switch (ext) {
                case "png":
                case "gif":
                case "bmp":
                case "tif":
                case "tiff":
                case "jpg":
                case "jpeg":
                    contentType = "image/" + ext;
                    break;
                case "txt":
                case "html":
                    contentType = "text/" + ext;
                    break;
                default:
                    contentType = "application/" + ext;
            }
        }
        return contentType;
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
        return null;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "List all metadata attachments", description = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/associating-resources/using-filestore.html'>More info</a>")
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
        List<MetadataResource> list = store.getResources(context, metadataUuid, sort, filter, approved);
        return list;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Delete all uploaded metadata resources")
    @RequestMapping(method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Attachment added."),
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
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
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
    @RequestMapping(method = RequestMethod.PUT)
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
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Record attachment."),
        @ApiResponse(responseCode = "403", description = "Operation not allowed. "
            + "User needs to be able to download the resource.")})
    @ResponseBody
    public HttpEntity<byte[]> getResource(
        @Parameter(description = "The metadata UUID", required = true, example = "43d7c186-2187-4bcd-8843-41e575a5ef56") @PathVariable String metadataUuid,
        @Parameter(description = "The resource identifier (ie. filename)", required = true) @PathVariable String resourceId,
        @Parameter(description = "Use approved version or not", example = "true") @RequestParam(required = false, defaultValue = "true") Boolean approved,
        @Parameter(description = "Size (only applies to images). From 1px to 2048px.", example = "200") @RequestParam(required = false) Integer size,
        @Parameter(hidden = true) HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        try (Store.ResourceHolder file = store.getResource(context, metadataUuid, resourceId, approved)) {

            ApiUtils.canViewRecord(metadataUuid, request);

            MultiValueMap<String, String> headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=\"" + file.getMetadata().getFilename() + "\"");
            headers.add("Cache-Control", "no-cache");
            String contentType = getFileContentType(file.getPath());
            headers.add("Content-Type", contentType);

            if (contentType.startsWith("image/") && size != null) {
                if (size >= MIN_IMAGE_SIZE && size <= MAX_IMAGE_SIZE) {
                    BufferedImage image = ImageIO.read(file.getPath().toFile());
                    BufferedImage resized = ImageUtil.resize(image, size);
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    ImageIO.write(resized, "png", output);
                    output.flush();
                    byte[] imagesB = output.toByteArray();
                    output.close();
                    return new HttpEntity<>(imagesB, headers);
                } else {
                    throw new IllegalArgumentException(String.format(
                        "Image can only be resized from %d to %d. You requested %d.",
                        MIN_IMAGE_SIZE, MAX_IMAGE_SIZE, size));
                }
            } else {
                return new HttpEntity<>(Files.readAllBytes(file.getPath()), headers);
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
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Attachment visibility removed."),
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
}
