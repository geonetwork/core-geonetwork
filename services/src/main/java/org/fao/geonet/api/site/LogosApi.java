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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceAlreadyExistException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.Group;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.IO;
import org.springframework.beans.factory.annotation.Autowired;
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

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */

@RequestMapping(value = {
    "/{portal}/api/logos",
    "/{portal}/api/" + API.VERSION_0_1 +
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
        @ApiResponse(code = 201, message = "Logo added."),
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
            } else if (Files.exists(filePath)) {
                throw new ResourceAlreadyExistException(f.getOriginalFilename());
            }

            filePath = Files.createFile(filePath);
            FileUtils.copyInputStreamToFile(f.getInputStream(), filePath.toFile());
        }
        return new ResponseEntity(HttpStatus.CREATED);
    }

    private void checkFileName(String fileName) throws Exception {
        FilePathChecker.verify(fileName);

        if (StringUtils.isEmpty(fileName)) {
            throw new Exception("File name is not defined.");
        }
    }

    @Autowired
    GeonetworkDataDirectory dataDirectory;

    @Autowired
    GroupRepository groupRepository;

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
        @ApiResponse(code = 204, message = "Logo removed."),
        @ApiResponse(code = 404, message = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public void deleteLogo(
        @ApiParam(value = "The logo filename to delete")
        @PathVariable
            String file
    ) throws Exception {
        checkFileName(file);
        Path nodeLogoDirectory = dataDirectory.getResourcesDir()
            .resolve("images").resolve("harvesting");

        FilePathChecker.verify(file);

        Path logoFile = nodeLogoDirectory.resolve(file);
        if (Files.exists(logoFile)) {

            final List<Group> groups = groupRepository.findByLogo(file);
            if (groups.size() > 0) {
                List<String> groupId = new ArrayList<>();
                groups.forEach(e -> groupId.add(e.getName()));
                throw new IllegalArgumentException(String.format(
                    "Logo '%s' is used by %d group(s). Assign another logo to the following groups: %s.",
                    file, groups.size(), groupId.toString()));
            }

            Files.delete(logoFile);
        } else {
            throw new ResourceNotFoundException(String.format(
                "No logo found with filename '%s'.", file));
        }
    }
}
