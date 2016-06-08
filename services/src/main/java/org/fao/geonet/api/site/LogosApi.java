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

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.resources.Resources;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

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
        value = "Get logos",
        notes = "Logos are used for groups and harvester icons",
        nickname = "getLogos")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public Set<String> get(
        HttpServletRequest request
    ) throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();
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
        nickname = "addLogo")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.POST)
    @PreAuthorize("hasRole('UserAdmin')")
    @ResponseStatus(value = HttpStatus.OK)
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
        if (fileName.contains("..")) {
            throw new BadParameterEx(
                "Invalid character found in resource name.",
                fileName);
        }

        if ("".equals(fileName)) {
            throw new Exception("File name is not defined.");
        }
    }


    @ApiOperation(
        value = "Remove a logo",
        notes = "",
        nickname = "removeLogo")
    @RequestMapping(
        path = "/{file:.+}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('UserAdmin')")
    @ResponseBody
    public ResponseEntity removeLogo(
        @ApiParam(value = "The logo filename to delete")
        @PathVariable
            String file
    ) throws Exception {
        checkFileName(file);

        ApplicationContext appContext = ApplicationContextHolder.get();
        GeonetworkDataDirectory dataDirectory = appContext.getBean(GeonetworkDataDirectory.class);
        Path nodeLogoDirectory = dataDirectory.getResourcesDir()
            .resolve("images").resolve("harvesting");
        Path logoFile = nodeLogoDirectory.resolve(file);
        if (Files.exists(logoFile)) {
            Files.delete(logoFile);
        } else {
            throw new ResourceNotFoundException(String.format(
                "No logo found with filename '%s'.", file));
        }
        return new ResponseEntity(HttpStatus.CREATED);
    }
}
