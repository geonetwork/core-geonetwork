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

import io.swagger.annotations.*;
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
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

/**
 * Metadata resource related operations.
 *
 * Load the store with id 'resourceStore'.
 */
@EnableWebMvc
@Service
@RequestMapping(value = { "/{portal}/api/records/{metadataUuid}/attachments",
        "/{portal}/api/" + API.VERSION_0_1 + "/records/{metadataUuid}/attachments" })
@Api(value = "records", tags = "records", description = "Metadata record operations")
public class AttachmentsApi {
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

    @ApiOperation(value = "List all metadata attachments", notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/associating-resources/using-filestore.html'>More info</a>", nickname = "getAllMetadataResources")
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Return the record attachments."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW) })
    @ResponseBody
    public List<MetadataResource> getAllResources(
            @ApiParam(value = "The metadata UUID", required = true, example = "43d7c186-2187-4bcd-8843-41e575a5ef56") @PathVariable String metadataUuid,
            @ApiParam(value = "Sort by", example = "type") @RequestParam(required = false, defaultValue = "name") Sort sort,
            @ApiParam(value = "Use approved version or not", example = "true") @RequestParam(required = false, defaultValue = "true") Boolean approved,
            @RequestParam(required = false, defaultValue = FilesystemStore.DEFAULT_FILTER) String filter,
            @ApiIgnore HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        List<MetadataResource> list = store.getResources(context, metadataUuid, sort, filter, approved);
        return list;
    }

    @ApiOperation(value = "Delete all uploaded metadata resources", nickname = "deleteAllMetadataResources")
    @RequestMapping(method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Attachment added."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delResources(
            @ApiParam(value = "The metadata UUID", required = true, example = "43d7c186-2187-4bcd-8843-41e575a5ef56") @PathVariable String metadataUuid,
            @ApiParam(value = "Use approved version or not", example = "true") @RequestParam(required = false, defaultValue = "false") Boolean approved,
            @ApiIgnore HttpServletRequest request) throws Exception {
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

    @ApiOperation(value = "Create a new resource for a given metadata", nickname = "putResourceFromFile")
    @PreAuthorize("hasRole('Editor')")
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Attachment uploaded."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    @ResponseBody
    public MetadataResource putResource(
            @ApiParam(value = "The metadata UUID", required = true, example = "43d7c186-2187-4bcd-8843-41e575a5ef56") @PathVariable String metadataUuid,
            @ApiParam(value = "The sharing policy", example = "public") @RequestParam(required = false, defaultValue = "public") MetadataResourceVisibility visibility,
            @ApiParam(value = "The file to upload") @RequestParam("file") MultipartFile file,
            @ApiParam(value = "Use approved version or not", example = "true") @RequestParam(required = false, defaultValue = "false") Boolean approved,
            @ApiIgnore HttpServletRequest request) throws Exception {
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

    @ApiOperation(value = "Create a new resource from a URL for a given metadata", nickname = "putResourcesFromURL", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('Editor')")
    @RequestMapping(method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Attachment added."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    @ResponseBody
    public MetadataResource putResourceFromURL(
            @ApiParam(value = "The metadata UUID", required = true, example = "43d7c186-2187-4bcd-8843-41e575a5ef56") @PathVariable String metadataUuid,
            @ApiParam(value = "The sharing policy", example = "public") @RequestParam(required = false, defaultValue = "public") MetadataResourceVisibility visibility,
            @ApiParam(value = "The URL to load in the store") @RequestParam("url") URL url,
            @ApiParam(value = "Use approved version or not", example = "true") @RequestParam(required = false, defaultValue = "false") Boolean approved,
           @ApiIgnore HttpServletRequest request) throws Exception {
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

    @ApiOperation(value = "Get a metadata resource", nickname = "getResource")
    // @PreAuthorize("permitAll")
    @RequestMapping(value = "/{resourceId:.+}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Record attachment."),
            @ApiResponse(code = 403, message = "Operation not allowed. "
                    + "User needs to be able to download the resource.") })
    @ResponseBody
    public HttpEntity<byte[]> getResource(
            @ApiParam(value = "The metadata UUID", required = true, example = "43d7c186-2187-4bcd-8843-41e575a5ef56") @PathVariable String metadataUuid,
            @ApiParam(value = "The resource identifier (ie. filename)", required = true) @PathVariable String resourceId,
            @ApiParam(value = "Use approved version or not", example = "true") @RequestParam(required = false, defaultValue = "true") Boolean approved,
            @ApiIgnore HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        try (Store.ResourceHolder file = store.getResource(context, metadataUuid, resourceId, approved)) {

            ApiUtils.canViewRecord(metadataUuid, request);

            MultiValueMap<String, String> headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=\"" + file.getMetadata().getFilename() + "\"");
            headers.add("Cache-Control", "no-cache");
            headers.add("Content-Type", getFileContentType(file.getPath()));

            return new HttpEntity<>(Files.readAllBytes(file.getPath()), headers);
        }
    }

    @ApiOperation(value = "Update the metadata resource visibility", nickname = "patchMetadataResourceVisibility")
    @PreAuthorize("hasRole('Editor')")
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Attachment visibility updated."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    @RequestMapping(value = "/{resourceId:.+}", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    public MetadataResource patchResource(
            @ApiParam(value = "The metadata UUID", required = true, example = "43d7c186-2187-4bcd-8843-41e575a5ef56") @PathVariable String metadataUuid,
            @ApiParam(value = "The resource identifier (ie. filename)", required = true) @PathVariable String resourceId,
            @ApiParam(value = "The visibility", required = true, example = "public") @RequestParam(required = true) MetadataResourceVisibility visibility,
            @ApiParam(value = "Use approved version or not", example = "true") @RequestParam(required = false, defaultValue = "false") Boolean approved,
            @ApiIgnore HttpServletRequest request) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        return store.patchResourceStatus(context, metadataUuid, resourceId, visibility, approved);
    }

    @ApiOperation(value = "Delete a metadata resource", nickname = "deleteMetadataResource")
    @PreAuthorize("hasRole('Editor')")
    @RequestMapping(value = "/{resourceId:.+}", method = RequestMethod.DELETE)
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Attachment visibility removed."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delResource(
            @ApiParam(value = "The metadata UUID", required = true, example = "43d7c186-2187-4bcd-8843-41e575a5ef56") @PathVariable String metadataUuid,
            @ApiParam(value = "The resource identifier (ie. filename)", required = true) @PathVariable String resourceId,
            @ApiParam(value = "Use approved version or not", example = "true") @RequestParam(required = false, defaultValue = "false") Boolean approved,
           @ApiIgnore HttpServletRequest request) throws Exception {
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
