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

package org.fao.geonet.services.metadata.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.fao.geonet.ApplicationContextHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@EnableWebMvc
@Controller
@Service
@RequestMapping(value = "/api/metadata/{metadataUuid}/resources")
@Api(value = "metadata/resources",
     tags= "metadata/resources",
     description = "Manage metadata datastore (ie. all uploaded document).")
public class ResourcesService {
    public ResourcesService() {
    }
    public ResourcesService(Store store) {
        this.store = store;
    }

    private Store store;

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    ApplicationContext appContext = ApplicationContextHolder.get();

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        if (appContext != null) {
            this.store = appContext.getBean("resourceStore", Store.class);
        }
    }



    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(ResourceType.class, new ResourceTypeConverter());
        binder.registerCustomEditor(Sort.class, new SortConverter());
    }


    public List<Resource> getResources() {
        return null;
    }



    @ApiOperation(value = "Get the list of uploaded resources " +
                          "available in the datastore for this metadata",
                  nickname = "getAllMetadataResources")
    @RequestMapping(method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiParam(name = "body", value = "The list of resources")
    @ResponseBody
    public List<Resource> getResources(@ApiParam(value = "The metadata UUID",
                                                 example = "43d7c186-2187-4bcd-8843-41e575a5ef56")
                                       @PathVariable
                                       String metadataUuid,
                                       @ApiParam(value = "Sort by",
                                                 example = "type")
                                       @RequestParam(required = false, defaultValue = "name")
                                       Sort sort,
                                       @RequestParam(required = false, defaultValue = "*.*")
                                       String filter) throws Exception {
        return store.getResources(metadataUuid, sort, filter);
    }



    @ApiOperation(value = "Delete all uploaded resources " +
                          "available in the datastore for this metadata.",
                  nickname = "deleteAllMetadataResources")
    @RequestMapping(method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('Editor')")
    @ResponseBody
    public boolean delResources(@ApiParam(value = "The metadata UUID",
                                          example = "43d7c186-2187-4bcd-8843-41e575a5ef56")
                                String metadataUuid) throws Exception {
        try {
            store.delResource(metadataUuid);
        } catch (Exception e) {
            throw e;
        }
        return true;
    }




    @RequestMapping(method = RequestMethod.POST)
    @PreAuthorize("hasRole('Editor')")
    @ResponseBody
    public List<Resource> putResource(
                                @PathVariable String metadataUuid,
                                @RequestParam(required = false, defaultValue = "public") ResourceType share,
                                @RequestParam("file") List<MultipartFile> files)
            throws Exception {
        List<Resource> resources = new ArrayList<>();
        for(MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    resources.add(store.putResource(metadataUuid, file, share));
                } catch (Exception e) {
                    throw e;
                }
            }
        }
        return resources;
    }

    @RequestMapping(method = RequestMethod.PUT)
    @PreAuthorize("hasRole('Editor')")
    @ResponseBody
    public List<Resource> putResourceFromURL(
            @PathVariable String metadataUuid,
            @RequestParam(required = false, defaultValue = "public") ResourceType share,
            @RequestParam("url") List<String> urls)
            throws Exception {
        List<Resource> resources = new ArrayList<>();
        for(String url : urls) {
            if (!StringUtils.isEmpty(url)) {
                try {
                    resources.add(store.putResource(metadataUuid, new URL(url), share));
                } catch (Exception e) {
                    throw e;
                }
            }
        }
        return resources;
    }



    @RequestMapping(value = "/{resourceId:.+}",
                    method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> getResource(@PathVariable String metadataUuid,
                                          @PathVariable String resourceId
    ) throws Exception {
        Path file = store.getResource(metadataUuid, resourceId);

        // TODO: Check user privileges

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Content-Disposition",
                    "inline; filename=\"" + file.getFileName() + "\"");
        headers.add("Cache-Control",
                    "no-cache");
        headers.add("Content-Type", getFileContentType(file));

        return new HttpEntity<>(Files.readAllBytes(file), headers);
    }





    @RequestMapping(value = "/{resourceId:.+}",
                    method = RequestMethod.PATCH)
    @PreAuthorize("hasRole('Editor')")
    @ResponseBody
    public Resource patchResource(@PathVariable String metadataUuid,
                                  @PathVariable String resourceId,
                                  @RequestParam(required = true)
                                               ResourceType share) throws Exception {
        try {
            return store.patchResourceStatus(metadataUuid, resourceId, share);
        } catch (Exception e) {
            throw e;
        }
    }




    @RequestMapping(value = "/{resourceId:.+}",
                    method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('Editor')")
    @ResponseBody
    public boolean delResource(
                             @PathVariable String metadataUuid,
                             @PathVariable String resourceId) throws Exception {
        try {
            // TODO: handle overwrite
            store.delResource(metadataUuid, resourceId);
        } catch (Exception e) {
            throw e;
        }
        return true;
    }




    /**
     * Based on the file content or file extension return
     * an appropiate mime type.
     *
     * @param file
     * @return  The mime type or application/{{file_extension}} if none found.
     * @throws IOException
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




    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
//            Exception.class,
            MaxUploadSizeExceededException.class,
//            RuntimeException.class,
            FileNotFoundException.class})
    public Object fileNotFoundHandler(final Exception exception) {
        exception.printStackTrace();
        return  new HashMap() {{
            put("result", "failed");
            put("type", "file_not_found");
            put("message", exception.getClass() + " " + exception.getMessage());
        }};
    }
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object missingParameterHandler(final Exception exception) {
        return  new HashMap() {{
            put("result", "failed");
            put("type", "required_parameter_missing");
            put("message", exception.getMessage());
        }};
    }
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UnsatisfiedServletRequestParameterException.class)
    public Object unsatisfiedParameterHandler(final Exception exception) {
        return  new HashMap() {{
            put("result", "failed");
            put("type", "unsatisfied_request_parameter");
            put("message", exception.getMessage());
        }};
    }
}
