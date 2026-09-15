/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceAlreadyExistException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.Group;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.util.FileMimetypeChecker;
import org.fao.geonet.utils.FilePathChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequestMapping(value = {
    "/{portal}/api/logos"
})
@Tag(name = "logos",
    description = "Logos operations")
@Controller("siteLogos")
public class LogosApi {
    private static final String[] iconExt = {".gif", ".png", ".jpg", ".jpeg"};

    @Autowired
    GeonetworkDataDirectory dataDirectory;
    @Autowired
    SettingManager settingManager;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    FileMimetypeChecker fileMimetypeChecker;

    private final DirectoryStream.Filter<Path> iconFilter = file -> {
        if (file == null || (Files.exists(file) && !Files.isRegularFile(file)))
            return false;
        if (file.getFileName() != null) {
            String name = file.getFileName().toString();
            for (String ext : iconExt)
                if (name.endsWith(ext))
                    return true;
        }
        return false;
    };

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get all logos",
        description = "Logos are used for the catalog, the groups logos, and harvester icons. " +
            "Logos are stored in the data directory in " +
            "<dataDirectory>/resources/images/harvesting.<br/> " +
            "Records are attached to a source. A source can be the local catalog " +
            "or a harvester node. When a source is created, its logo is located " +
            "in the images/logos folder with the source UUID as filename. For some " +
            "sources the logo can be automatically retrieved (eg. when harvesting GeoNetwork " +
            "catalogs). For others, the logo is usually manually defined when configuring the " +
            "harvester.")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public Set<String> getLogos(
        HttpServletRequest request
    ) {
        ApplicationContext context = ApplicationContextHolder.get();
        Set<Path> icons = context.getBean(Resources.class).listFiles(
            ApiUtils.createServiceContext(request),
            "harvesting",
            iconFilter);
        Set<String> iconsList = new HashSet<>(icons.size());
        for (Path i : icons) {
            iconsList.add(i.getFileName().toString());
        }
        return iconsList;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Add a logo",
        description = ""
        //       authorizations = {
        //           @Authorization(value = "basicAuth")
        //      })
    )
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Logo added."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity addLogo(
        @Parameter(description = "The logo image to upload")
        @RequestParam("file")
            MultipartFile[] file,
        @Parameter(
            description = "Overwrite if exists",
            required = false
        )
        @RequestParam(
            defaultValue = "false",
            required = false
        )
            boolean overwrite,
        HttpServletRequest request
    ) throws Exception {
        final ApplicationContext appContext = ApplicationContextHolder.get();
        final Resources resources = appContext.getBean(Resources.class);
        final ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        final Path directoryPath = resources.locateHarvesterLogosDirSMVC(appContext);

        for (MultipartFile f : file) {
            fileMimetypeChecker.checkValidImageMimeType(f);

            String fileName = f.getOriginalFilename();
            checkFileName(fileName);

            try (Resources.ResourceHolder holder = resources.getWritableImage(serviceContext, fileName, directoryPath)) {
                if (Files.exists(holder.getPath()) && !overwrite) {
                    holder.abort();
                    throw new ResourceAlreadyExistException(fileName);
                }
                Files.copy(f.getInputStream(), holder.getPath());
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


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get a logo",
        description = ""
    )
    @RequestMapping(
        path = "/{file:.+}",
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logo returned."),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public void getLogo(
        @Parameter(description = "The logo filename")
        @PathVariable
            String file,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws Exception {
        checkFileName(file);
        FilePathChecker.verify(file);

        final ApplicationContext appContext = ApplicationContextHolder.get();
        final Resources resources = appContext.getBean(Resources.class);
        final ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        final Path logoDirectory = resources.locateHarvesterLogosDirSMVC(appContext);

        try (Resources.ResourceHolder image = resources.getImage(serviceContext, file, logoDirectory)) {
            if (image != null) {
                response.sendRedirect(String.format(
                    "%simages/harvesting/%s",
                    settingManager.getBaseURL(),
                    URLEncoder.encode(file, "UTF-8")));
            } else {
                throw new ResourceNotFoundException(String.format(
                    "No logo found with filename '%s'.", file));
            }
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Remove a logo",
        description = ""
        //       authorizations = {
        //           @Authorization(value = "basicAuth")
        //      })
    )
    @RequestMapping(
        path = "/{file:.+}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Logo removed.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public void deleteLogo(
        @Parameter(description = "The logo filename to delete")
        @PathVariable
            String file,
        HttpServletRequest request
    ) throws Exception {
        checkFileName(file);
        FilePathChecker.verify(file);

        final ApplicationContext appContext = ApplicationContextHolder.get();
        final Resources resources = appContext.getBean(Resources.class);
        final ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        final Path nodeLogoDirectory = resources.locateHarvesterLogosDirSMVC(appContext);

        try (Resources.ResourceHolder image = resources.getImage(serviceContext, file, nodeLogoDirectory)) {
            if (image != null) {
                final List<Group> groups = groupRepository.findByLogo(file);
                if (groups != null && !groups.isEmpty()) {
                    final List<String> groupIds =
                        groups.stream().map(Group::getName).collect(Collectors.toList());
                    throw new IllegalArgumentException(String.format(
                        "Logo '%s' is used by %d group(s). Assign another logo to the following groups: %s.",
                        file, groups.size(), groupIds.toString()));
                }

                resources.deleteImageIfExists(file, nodeLogoDirectory);
            } else {
                throw new ResourceNotFoundException(String.format(
                    "No logo found with filename '%s'.", file));
            }
        }
    }
}
