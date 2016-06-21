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

import jeeves.constants.Jeeves;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.site.model.SettingSet;
import org.fao.geonet.api.site.model.SettingsListResponse;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.SettingRepository;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.IO;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
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

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.component.ProfileManager;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import static org.apache.commons.fileupload.util.Streams.checkFileName;

/**
 *
 */

@RequestMapping(value = {
    "/api/site",
    "/api/" + API.VERSION_0_1 +
        "/site"
})
@Api(value = "site",
    tags = "site",
    description = "Catalog operations")
@Controller("site")
public class SiteApi {

    @ApiOperation(
        value = "Get site description",
        notes = "",
        nickname = "getDescription")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public SettingsListResponse get(
    ) throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();
        SettingManager sm = appContext.getBean(SettingManager.class);

        SettingsListResponse response = new SettingsListResponse();
        response.setSettings(sm.getSettings(new String[]{
            Settings.SYSTEM_SITE_NAME_PATH,
            Settings.SYSTEM_SITE_ORGANIZATION,
            Settings.SYSTEM_SITE_SITE_ID_PATH,
            Settings.SYSTEM_PLATFORM_VERSION,
            Settings.SYSTEM_PLATFORM_SUBVERSION
        }));
        return response;
    }

    @ApiOperation(
        value = "Get settings",
        notes = "Return public settings for anonymous users, internals are allowed for authenticated.",
        nickname = "getSettings")
    @RequestMapping(
        path = "/settings",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public SettingsListResponse getSettingsSet(
        @ApiParam(
            value = "Setting set. A common set of settings to retrieve.",
            required = false
        )
        @RequestParam(
            required = false
        )
            SettingSet[] set,
        @ApiParam(
            value = "Setting key",
            required = false
        )
        @RequestParam(
            required = false
        )
            String[] key,
        HttpServletRequest request,
        HttpSession httpSession
    ) throws Exception {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        SettingManager sm = appContext.getBean(SettingManager.class);
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile profile = session == null ? null : session.getProfile();

        List<String> settingList = new ArrayList<>();
        if (set == null && key == null) {
            final List<org.fao.geonet.domain.Setting> publicSettings =
                appContext.getBean(SettingRepository.class).findAllByInternal(false);

            SettingsListResponse response = new SettingsListResponse();
            response.setSettings(publicSettings);
            return response;
        } else {
            if (set != null && set.length > 0) {
                for (SettingSet s : set) {
                    String[] props = s.getListOfSettings();
                    if (props != null) {
                        Collections.addAll(settingList, props);
                    }
                }
            }
            if (key != null && key.length > 0) {
                Collections.addAll(settingList, key);
            }
            List<org.fao.geonet.domain.Setting> settings = sm.getSettings(settingList.toArray(new String[0]));
            ListIterator<org.fao.geonet.domain.Setting> iterator = settings.listIterator();

            // Cleanup internal settings for not authenticated users.
            while (iterator.hasNext()) {
                org.fao.geonet.domain.Setting s = iterator.next();
                if (s.isInternal() && profile == null) {
                    settings.remove(s);
                }
            }

            SettingsListResponse response = new SettingsListResponse();
            response.setSettings(settings);
            return response;
        }
    }


    @ApiOperation(
        value = "Get site informations",
        notes = "",
        nickname = "getInformation")
    @RequestMapping(
        path = "/info",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public SiteInformation getInformation(
    ) throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();
        ServiceContext context = ServiceContext.get();
        return new SiteInformation(context, (GeonetContext) context
            .getHandlerContext(Geonet.CONTEXT_NAME));
    }


    @ApiOperation(
        value = "Is CAS enabled?",
        notes = "",
        nickname = "isCasEnabled")
    @RequestMapping(
        path = "/info/isCasEnabled",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public boolean isCasEnabled(
        HttpServletRequest request
    ) throws Exception {
        ApiUtils.createServiceContext(request);
        return ProfileManager.isCasEnabled();
    }

    @Autowired
    private SystemInfo info;

    @ApiOperation(
        value = "Update staging profile",
        notes = "TODO: Needs doc",
        nickname = "updateStagingProfile")
    @RequestMapping(
        path = "/info/staging/{profile}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT)
    @PreAuthorize("hasRole('Administrator')")
    public ResponseEntity updateStagingProfile(
        @PathVariable
            SystemInfo.Staging profile) {
        this.info.setStagingProfile(profile.toString());
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @ApiOperation(
        value = "Is in read-only mode?",
        notes = "",
        nickname = "/info/isReadOnly")
    @RequestMapping(
        path = "/readonly",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public boolean isReadOnly(
    ) throws Exception {
        return ApplicationContextHolder.get().getBean(NodeInfo.class).isReadOnly();
    }


    @ApiOperation(
        value = "Is indexing?",
        notes = "",
        nickname = "/info/isIndexing")
    @RequestMapping(
        path = "/indexing",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public boolean isIndexing(
        HttpServletRequest request
    ) throws Exception {
        ApiUtils.createServiceContext(request);
        return ApplicationContextHolder.get().getBean(DataManager.class).isIndexing();
    }

    @ApiOperation(
        value = "Get build details",
        notes = "To know when and how this version of the application was built.",
        nickname = "getSystemInfo")
    @RequestMapping(
        path = "/info/build",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public SystemInfo getSystemInfo(
    ) throws Exception {
        return ApplicationContextHolder.get().getBean(SystemInfo.class);
    }


    @ApiOperation(
        value = "Set catalog logo",
        notes = "Logos are stored in the data directory " +
            "resources/images/harvesting as PNG or GIF images. " +
            "When a logo is assigned to the catalog, a new " +
            "image is created in images/logos/<catalogUuid>.png.",
        nickname = "setLogo")
    @RequestMapping(
        path = "/logo",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('UserAdmin')")
    @ResponseBody
    public ResponseEntity setLogo(
        @ApiParam(value = "Logo to use for the catalog")
        @RequestParam("file")
            String file,
        @ApiParam(
            value = "Create favicon too",
            required = false
        )
        @RequestParam(
            defaultValue = "false",
            required = false
        )
            boolean asFavicon

    ) throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();
        Path logoDirectory = Resources.locateHarvesterLogosDirSMVC(appContext);

        checkFileName(file);

        SettingManager settingMan = appContext.getBean(SettingManager.class);
        GeonetworkDataDirectory dataDirectory = appContext.getBean(GeonetworkDataDirectory.class);
        String nodeUuid = settingMan.getSiteId();

        try {
            Path logoFilePath = logoDirectory.resolve(file);
            Path nodeLogoDirectory = dataDirectory.getResourcesDir()
                .resolve("images");
            if (!Files.exists(logoFilePath)) {
                logoFilePath = nodeLogoDirectory.resolve("harvesting").resolve(file);
            }
            try (InputStream inputStream = Files.newInputStream(logoFilePath)) {
                BufferedImage source = ImageIO.read(inputStream);

                if (asFavicon) {
                    ApiUtils.createFavicon(
                        source,
                        dataDirectory.getResourcesDir().resolve("images").resolve("favicon.png"));
                } else {
                    Path logo = nodeLogoDirectory.resolve("logos").resolve(nodeUuid + ".png");
                    Path defaultLogo = nodeLogoDirectory.resolve("images").resolve("logo.png");

                    if (!file.endsWith(".png")) {
                        try (
                            OutputStream logoOut = Files.newOutputStream(logo);
                            OutputStream defLogoOut = Files.newOutputStream(defaultLogo);
                        ) {
                            ImageIO.write(source, "png", logoOut);
                            ImageIO.write(source, "png", defLogoOut);
                        }
                    } else {
                        Files.deleteIfExists(logo);
                        IO.copyDirectoryOrFile(logoFilePath, logo, false);
                        Files.deleteIfExists(defaultLogo);
                        IO.copyDirectoryOrFile(logoFilePath, defaultLogo, false);
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(
                "Unable to move uploaded thumbnail to destination directory. Error: " + e.getMessage());
        }
        return new ResponseEntity(HttpStatus.CREATED);
    }


    @ApiOperation(
        value = "Get XSL tranformations available",
        notes = "XSL transformations may be applied while importing or harvesting records.",
        nickname = "getXslTransformations")
    @RequestMapping(
        path = "/info/transforms",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<String> getXslTransformations(
    ) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        GeonetworkDataDirectory dataDirectory = applicationContext.getBean(GeonetworkDataDirectory.class);

        try (DirectoryStream<Path> sheets = Files.newDirectoryStream(
            dataDirectory.getWebappDir().resolve(Geonet.Path.IMPORT_STYLESHEETS)
        )) {
            List<String> list = new ArrayList<>();
            for (Path sheet : sheets) {
                String id = sheet.toString();
                if (id != null && id.endsWith(".xsl")) {
                    String name = com.google.common.io.Files.getNameWithoutExtension(
                        sheet.getFileName().toString());
                    list.add(name);
                }
            }
            return list;
        }
    }
}
