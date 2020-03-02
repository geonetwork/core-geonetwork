/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.site;

import io.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.IO;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */

@RequestMapping(value = {
    "/api/logos",
    "/api/" + API.VERSION_0_1 +
        "/logos"
})
@Api(value = "logos",
    tags = "logos",
    description = "Logos operations")
@Controller("siteLogos")
public class LogosApi {
    private static final String iconExt[] = {".gif", ".png", ".jpg", ".jpeg"};
    private DirectoryStream.Filter<Path> iconFilter = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path file) throws IOException {
            if (file == null || !Files.isRegularFile(file))
                return false;
            if (file.getFileName() != null) {
                String name = file.getFileName().toString();
                for (String ext : iconExt)
                    if (name.endsWith(ext))
                        return true;
            }
            return false;
        }
    };

    @ApiOperation(
        value = "Get all logos",
        notes = "Logos are used for the catalog, the groups logos, and harvester icons. " +
            "Logos are stored in the data directory in " +
            "<dataDirectory>/resources/images/harvesting.<br/> " +
            "Records are attached to a source. A source can be the local catalog " +
            "or a harvester node. When a source is created, its logo is located " +
            "in the images/logos folder with the source UUID as filename. For some " +
            "sources the logo can be automatically retrieved (eg. when harvesting GeoNetwork " +
            "catalogs). For others, the logo is usually manually defined when configuring the " +
            "harvester.",
        nickname = "getLogos")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public Set<String> get(
        HttpServletRequest request
    ) throws Exception {
        Set<Path> icons = Resources.listFiles(
            ApiUtils.createServiceContext(request),
            "harvesting",
            iconFilter);
        Set<String> iconsList = new HashSet<>(icons.size());
        for (Path i : icons) {
            iconsList.add(i.getFileName().toString());
        }
        return iconsList;
    }

    private volatile Path logoDirectory;

    @ApiOperation(
        value = "Add a logo",
        notes = "",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "addLogo")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.POST)
    @PreAuthorize("hasRole('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Logo added.") ,
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity addLogo(
        @ApiParam(value = "The logo image to upload")
        @RequestParam("file")
            MultipartFile[] file,
        @ApiParam(
            value = "Overwrite if exists",
            required = false
        )
        @RequestParam(
            defaultValue = "false",
            required = false
        )
            boolean overwrite
    ) throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();
        Path directoryPath;
        synchronized (this) {
            if (this.logoDirectory == null) {
                this.logoDirectory = Resources.locateHarvesterLogosDirSMVC(appContext);
            }
            directoryPath = this.logoDirectory;
        }

        for (MultipartFile f : file) {
            String fileName = f.getName();

            checkFileName(fileName);

            Path filePath = directoryPath.resolve(f.getOriginalFilename());
            if (Files.exists(filePath) && overwrite) {
                IO.deleteFile(filePath, true, "Deleting file");
                filePath = directoryPath.resolve(f.getOriginalFilename());
            }

            filePath = Files.createFile(filePath);

            try (OutputStream stream = Files.newOutputStream(filePath)) {
                int read;
                byte[] bytes = new byte[1024];
                InputStream is = f.getInputStream();
                while ((read = is.read(bytes)) != -1) {
                    stream.write(bytes, 0, read);
                }
            }
        }
        return new ResponseEntity(HttpStatus.CREATED);
    }

    private void checkFileName(String fileName) throws Exception {
        FilePathChecker.verify(fileName);

        if (StringUtils.isEmpty(fileName)) {
            throw new Exception("File name is not defined.");
        }
    }


    @ApiOperation(
        value = "Remove a logo",
        notes = "",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "deleteLogo")
    @RequestMapping(
        path = "/{file:.+}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Logo removed.") ,
        @ApiResponse(code = 404, message = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND) ,
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public void deleteLogo(
        @ApiParam(value = "The logo filename to delete")
        @PathVariable
            String file
    ) throws Exception {
        checkFileName(file);

        ApplicationContext appContext = ApplicationContextHolder.get();
        GeonetworkDataDirectory dataDirectory = appContext.getBean(GeonetworkDataDirectory.class);
        Path nodeLogoDirectory = dataDirectory.getResourcesDir()
            .resolve("images").resolve("harvesting");

        FilePathChecker.verify(file);

        Path logoFile = nodeLogoDirectory.resolve(file);
        if (Files.exists(logoFile)) {
            Files.delete(logoFile);
        } else {
            throw new ResourceNotFoundException(String.format(
                "No logo found with filename '%s'.", file));
        }
    }
}
