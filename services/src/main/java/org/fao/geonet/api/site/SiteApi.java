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

import static org.apache.commons.fileupload.util.Streams.checkFileName;
import static org.fao.geonet.api.ApiParams.API_CLASS_CATALOG_TAG;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.site.model.SettingSet;
import org.fao.geonet.api.site.model.SettingsListResponse;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.doi.client.DoiManager;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataSourceInfo_;
import org.fao.geonet.domain.Metadata_;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.Setting;
import org.fao.geonet.domain.SettingDataType;
import org.fao.geonet.domain.Source;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.PathSpec;
import org.fao.geonet.repository.SettingRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.ProxyInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.component.ProfileManager;
import jeeves.config.springutil.ServerBeanPropertyUpdater;
import jeeves.server.JeevesProxyInfo;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import springfox.documentation.annotations.ApiIgnore;

/**
 *
 */

@RequestMapping(value = {
    "/{portal}/api/site",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/site"
})
@Api(value = API_CLASS_CATALOG_TAG,
    tags = API_CLASS_CATALOG_TAG,
    description = ApiParams.API_CLASS_CATALOG_OPS)
@Controller("site")
public class SiteApi {

    @Autowired
    SettingManager settingManager;

    @Autowired
    NodeInfo node;

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    LanguageUtils languageUtils;

    public static void reloadServices(ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dataMan = gc.getBean(DataManager.class);
        SettingManager settingMan = gc.getBean(SettingManager.class);
        SettingInfo si = context.getBean(SettingInfo.class);

        try {
            if (si.getLuceneIndexOptimizerSchedulerEnabled()) {
                dataMan.rescheduleOptimizer(si.getLuceneIndexOptimizerSchedulerAt(), si.getLuceneIndexOptimizerSchedulerInterval());
            } else {
                dataMan.disableOptimizer();
            }
        } catch (Exception e) {
            context.error("Reload services. Error: " + e.getMessage());
            context.error(e);
            throw new OperationAbortedEx("Parameters saved but cannot restart Lucene Index Optimizer: " + e.getMessage());
        }

        LogUtils.refreshLogConfiguration();

        try {
            // Load proxy information into Jeeves
            ProxyInfo pi = JeevesProxyInfo.getInstance();
            boolean useProxy = settingMan.getValueAsBool(Settings.SYSTEM_PROXY_USE, false);
            if (useProxy) {
                String proxyHost = settingMan.getValue(Settings.SYSTEM_PROXY_HOST);
                String proxyPort = settingMan.getValue(Settings.SYSTEM_PROXY_PORT);
                String username = settingMan.getValue(Settings.SYSTEM_PROXY_USERNAME);
                String password = settingMan.getValue(Settings.SYSTEM_PROXY_PASSWORD);
                pi.setProxyInfo(proxyHost, Integer.valueOf(proxyPort), username, password);
            } else {
                pi.setProxyInfo(null, -1, null, null);
            }

            // Update http.proxyHost, http.proxyPort and http.nonProxyHosts
            Lib.net.setupProxy(settingMan);
        } catch (Exception e) {
            context.error("Reload services. Error: " + e.getMessage());
            context.error(e);
            throw new OperationAbortedEx("Parameters saved but cannot set proxy information: " + e.getMessage());
        }
        DoiManager doiManager = gc.getBean(DoiManager.class);
        doiManager.loadConfig();
    }

    @ApiOperation(
        value = "Get site (or portal) description",
        notes = "",
        nickname = "getDescription")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Site description.")
    })
    @ResponseBody
    public SettingsListResponse get(
        @ApiIgnore
        HttpServletRequest request
    ) throws Exception {
        SettingsListResponse response = new SettingsListResponse();
        response.setSettings(settingManager.getSettings(new String[]{
            Settings.SYSTEM_SITE_NAME_PATH,
            Settings.SYSTEM_SITE_ORGANIZATION,
            Settings.SYSTEM_SITE_SITE_ID_PATH,
            Settings.SYSTEM_PLATFORM_VERSION,
            Settings.SYSTEM_PLATFORM_SUBVERSION
        }));
        if (!NodeInfo.DEFAULT_NODE.equals(node.getId())) {
            Source source = sourceRepository.findOne(node.getId());
            if (source != null) {
                String iso3langCode = languageUtils.getIso3langCode(request.getLocales());
                final List<Setting> settings = response.getSettings();
                settings.add(
                    new Setting().setName(Settings.NODE_DEFAULT)
                        .setValue("false"));
                settings.add(
                    new Setting().setName(Settings.NODE)
                        .setValue(source.getUuid()));
                settings.add(
                    new Setting().setName(Settings.NODE_NAME)
                        .setValue(source != null ? source.getLabel(iso3langCode) : source.getName()));
            }
        }
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
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Settings.")
    })
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
        @ApiIgnore
        @ApiParam(
            hidden = true
        )
            HttpSession httpSession
    ) throws Exception {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        SettingManager sm = appContext.getBean(SettingManager.class);
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile profile = session == null ? null : session.getProfile();

        List<String> settingList = new ArrayList<>();
        if (set == null && key == null) {
            final SettingRepository settingRepository = appContext.getBean(SettingRepository.class);
            final List<org.fao.geonet.domain.Setting> publicSettings =
                settingRepository.findAllByInternal(false);

            // Add virtual settings based on internal settings.
            // eg. if mail server is defined, allow email interactions ...
            String mailServer = sm.getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_HOST);
            publicSettings.add(new Setting()
                .setName(Settings.SYSTEM_FEEDBACK_MAILSERVER_HOST + Settings.VIRTUAL_SETTINGS_SUFFIX_ISDEFINED)
                .setDataType(SettingDataType.BOOLEAN)
                .setValue(StringUtils.isNotEmpty(mailServer) + ""));


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
        value = "Get settings with details",
        notes = "Provides also setting properties.",
        nickname = "getSettingsDetails")
    @RequestMapping(
        path = "/settings/details",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Settings with details.")
    })
    @ResponseBody
    @PreAuthorize("hasRole('Administrator')")
    public List<Setting> getSettingsDetails(
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
        @ApiIgnore
        @ApiParam(
            hidden = true
        )
            HttpSession httpSession
    ) throws Exception {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        SettingManager sm = appContext.getBean(SettingManager.class);
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile profile = session == null ? null : session.getProfile();

        List<String> settingList = new ArrayList<>();
        if (set == null && key == null) {
            return
                sm.getAll();
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
            return settings;
        }
    }

    @ApiOperation(
        value = "Save settings",
        notes = "",
        nickname = "saveSettingsDetails")
    @RequestMapping(
        path = "/settings",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.POST
    )
    @PreAuthorize("hasRole('Administrator')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Settings saved."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    public void saveSettings(
        @ApiIgnore
        @ApiParam(hidden = false)
        @RequestParam
            Map<String, String> allRequestParams,
        HttpServletRequest request,
        @ApiIgnore
        @ApiParam(
            hidden = true
        )
            HttpSession httpSession
    ) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        SettingManager sm = applicationContext.getBean(SettingManager.class);
        String currentUuid = sm.getSiteId();
        String oldSiteName = sm.getSiteName();

        if (!sm.setValues(allRequestParams)) {
            throw new OperationAbortedEx("Cannot set all values");
        }

        String newSiteName = sm.getSiteName();
        // Update site source name/translations if the site name is updated
        if (!oldSiteName.equals(newSiteName)) {
            SourceRepository sourceRepository = applicationContext.getBean(SourceRepository.class);
            Source siteSource = sourceRepository.findOne(currentUuid);

            if (siteSource != null) {
                siteSource.setName(newSiteName);
                siteSource.getLabelTranslations().forEach(
                    (l, t) -> siteSource.getLabelTranslations().put(l, newSiteName)
                );
                sourceRepository.save(siteSource);
            }
        }

        // And reload services
        String newUuid = allRequestParams.get(Settings.SYSTEM_SITE_SITE_ID_PATH);

        if (newUuid != null && !currentUuid.equals(newUuid)) {
            final IMetadataManager metadataRepository = applicationContext.getBean(IMetadataManager.class);
            final SourceRepository sourceRepository = applicationContext.getBean(SourceRepository.class);
            final Source source = sourceRepository.findOne(currentUuid);
            Source newSource = new Source(newUuid, source.getName(), source.getLabelTranslations(), source.getType());
            sourceRepository.save(newSource);

            PathSpec<Metadata, String> servicesPath = new PathSpec<Metadata, String>() {
                @Override
                public javax.persistence.criteria.Path<String> getPath(Root<Metadata> root) {
                    return root.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.sourceId);
                }
            };
            metadataRepository.createBatchUpdateQuery(servicesPath, newUuid, MetadataSpecs.isHarvested(false));
            sourceRepository.delete(source);
        }

        SettingInfo info = applicationContext.getBean(SettingInfo.class);
        ServiceContext context = ApiUtils.createServiceContext(request);
        ServerBeanPropertyUpdater.updateURL(info.getSiteUrl(true) +
                context.getBaseUrl(),
            applicationContext);

        // Reload services affected by updated settings
        reloadServices(context);
    }

    @ApiOperation(
        value = "Get site informations",
        notes = "",
        nickname = "getInformation")
    @RequestMapping(
        path = "/info",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Site information.")
    })
    @ResponseBody
    public SiteInformation getInformation(HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
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
    // This service returns a boolean, not encapsulated in json
    // TODO: Review to return a valid JSON, check also similar methods
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Staging profile saved."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    @PreAuthorize("hasRole('Administrator')")
    public void updateStagingProfile(
        @PathVariable
            SystemInfo.Staging profile) {
        this.info.setStagingProfile(profile.toString());
    }

    @ApiOperation(
        value = "Is in read-only mode?",
        notes = "",
        nickname = "isReadOnly")
    @RequestMapping(
        path = "/info/readonly",
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
        nickname = "isIndexing")
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
        value = "Index",
        notes = "",
        nickname = "index")
    @RequestMapping(
        path = "/index",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT)
    @PreAuthorize("hasRole('Editor')")
    @ResponseBody
    public HttpEntity index(
        @ApiParam(value = "Drop and recreate index",
            required = false)
        @RequestParam(required = false, defaultValue = "true")
        boolean reset,
        @ApiParam(value = "Records having only XLinks",
            required = false)
        @RequestParam(required = false, defaultValue = "false")
            boolean havingXlinkOnly,
//        @ApiParam(value = API_PARAM_RECORD_UUIDS_OR_SELECTION,
//            required = false,
//            example = "")
//        @RequestParam(required = false)
//        String[] uuids,
        @ApiParam(
            value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
        String bucket,
        HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        SearchManager searchMan = ApplicationContextHolder.get().getBean(SearchManager.class);

        searchMan.rebuildIndex(context, havingXlinkOnly, reset, bucket);

        return new HttpEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(
        value = "Index in Elastic",
        notes = "",
        nickname = "indexes")
    @RequestMapping(
        path = "/index/es",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT)
    @PreAuthorize("hasRole('Editor')")
    @ResponseBody
    public HttpEntity indexEs(
        @ApiParam(value = "Drop and recreate index",
            required = false)
        @RequestParam(required = false, defaultValue = "true")
            boolean reset,
        @ApiParam(value = "Records having only XLinks",
            required = false)
        @RequestParam(required = false, defaultValue = "false")
            boolean havingXlinkOnly,
//        @ApiParam(value = API_PARAM_RECORD_UUIDS_OR_SELECTION,
//            required = false,
//            example = "")
//        @RequestParam(required = false)
//        String[] uuids,
        @ApiParam(
            value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        EsSearchManager searchMan = ApplicationContextHolder.get().getBean(EsSearchManager.class);

        searchMan.rebuildIndex(context, havingXlinkOnly, reset, bucket);

        return new HttpEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation(
        value = "Delete index in Elastic",
        notes = "",
        nickname = "deleteIndexes")
    @RequestMapping(
        path = "/index/es",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('Editor')")
    @ResponseBody
    public HttpEntity deleteIndexEs() throws Exception {
        EsSearchManager searchMan = ApplicationContextHolder.get().getBean(EsSearchManager.class);
        searchMan.clearIndex();
        return new HttpEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(
        value = "Get build details",
        notes = "To know when and how this version of the application was built.",
        nickname = "getSystemInfo")
    @RequestMapping(
        path = "/info/build",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Build info.")
    })
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
    @PreAuthorize("hasRole('UserAdmin')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Logo set."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    public void setLogo(
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
            boolean asFavicon,
        HttpServletRequest request

    ) throws Exception {
        final ApplicationContext appContext = ApplicationContextHolder.get();
        final Resources resources = appContext.getBean(Resources.class);
        final Path logoDirectory = resources.locateHarvesterLogosDirSMVC(appContext);
        final ServiceContext serviceContext = ApiUtils.createServiceContext(request);

        checkFileName(file);
        FilePathChecker.verify(file);

        SettingManager settingMan = appContext.getBean(SettingManager.class);
        String nodeUuid = settingMan.getSiteId();

        Resources.ResourceHolder holder = resources.getImage(serviceContext, file, logoDirectory);
        final Path resourcesDir =
            resources.locateResourcesDir(request.getServletContext(), serviceContext.getApplicationContext());
        if (holder == null || holder.getPath() == null) {
            holder = resources.getImage(serviceContext, "images/harvesting/" + file, resourcesDir);
        }
        try {
            try (InputStream inputStream = Files.newInputStream(holder.getPath())) {
                BufferedImage source = ImageIO.read(inputStream);

                if (asFavicon) {
                    try (Resources.ResourceHolder favicon =
                             resources.getWritableImage(serviceContext, "images/logos/favicon.png",
                                                        resourcesDir)) {
                        ApiUtils.createFavicon(source, favicon.getPath());
                    }
                } else {
                    try (Resources.ResourceHolder logo =
                             resources.getWritableImage(serviceContext,
                                                        "images/logos/" + nodeUuid + ".png",
                                                        resourcesDir);
                         Resources.ResourceHolder defaultLogo =
                             resources.getWritableImage(serviceContext,
                                                        "images/logo.png", resourcesDir)) {
                        if (!file.endsWith(".png")) {
                            try (
                                OutputStream logoOut = Files.newOutputStream(logo.getPath());
                                OutputStream defLogoOut = Files.newOutputStream(defaultLogo.getPath());
                            ) {
                                ImageIO.write(source, "png", logoOut);
                                ImageIO.write(source, "png", defLogoOut);
                            }
                        } else {
                            Files.copy(holder.getPath(), logo.getPath(), StandardCopyOption.REPLACE_EXISTING);
                            Files.copy(holder.getPath(), defaultLogo.getPath(),
                                       StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(
                "Unable to move uploaded thumbnail to destination directory. Error: " + e.getMessage());
        } finally {
            holder.close();
        }
    }


    @ApiOperation(
        value = "Get XSL tranformations available",
        notes = "XSL transformations may be applied while importing or harvesting records.",
        nickname = "getXslTransformations")
    @RequestMapping(
        path = "/info/transforms",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "XSLT available.")
    })
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
