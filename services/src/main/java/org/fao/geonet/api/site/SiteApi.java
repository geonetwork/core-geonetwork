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

import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.CountResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.component.ProfileManager;
import jeeves.config.springutil.ServerBeanPropertyUpdater;
import jeeves.server.JeevesProxyInfo;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.xlink.Processor;
import org.apache.commons.lang3.StringUtils;
import org.fao.geonet.*;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.OpenApiConfig;
import org.fao.geonet.api.exception.FeatureNotEnabledException;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.site.model.SettingSet;
import org.fao.geonet.api.site.model.SettingsListResponse;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.api.users.recaptcha.RecaptchaChecker;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.index.Status;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.index.es.EsServerStatusChecker;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataManager;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.index.BatchOpsMetadataReindexer;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.lib.ProxyConfiguration;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.util.MailUtil;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.ProxyInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static org.apache.commons.fileupload.util.Streams.checkFileName;
import static org.fao.geonet.api.ApiParams.API_CLASS_CATALOG_TAG;
import static org.fao.geonet.constants.Geonet.Path.IMPORT_STYLESHEETS_SCHEMA_PREFIX;
import static org.fao.geonet.kernel.setting.Settings.SYSTEM_FEEDBACK_EMAIL;

/**
 *
 */

@RequestMapping(value = {
    "/{portal}/api/site"
})
@Tag(name = API_CLASS_CATALOG_TAG,
    description = ApiParams.API_CLASS_CATALOG_OPS)
@Controller("site")
public class SiteApi {
    @Autowired
    GeonetworkDataDirectory dataDirectory;
    @Autowired
    SettingManager settingManager;

    @Autowired
    SchemaManager schemaManager;

    @Autowired
    NodeInfo node;

    @Autowired
    MetadataRepository metadataRepository;

    @Autowired
    MetadataDraftRepository metadataDraftRepository;

    @Autowired
    EsRestClient esRestClient;

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    private SystemInfo info;

    public static void reloadServices(ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager settingMan = gc.getBean(SettingManager.class);

        LogUtils.refreshLogConfiguration();

        try {
            // Load proxy information into Jeeves
            ProxyInfo pi = JeevesProxyInfo.getInstance();
            boolean useProxy = Lib.net.getProxyConfiguration().isEnabled();
            if (useProxy) {
                String proxyHost = Lib.net.getProxyConfiguration().getHost();
                String proxyPort = Lib.net.getProxyConfiguration().getPort();
                String username = Lib.net.getProxyConfiguration().getUsername();
                String password = Lib.net.getProxyConfiguration().getPassword();
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

        HarvestManager harvestManager = context.getBean(HarvestManager.class);
        harvestManager.rescheduleActiveHarvesters();
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get site (or portal) description",
        description = "")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Site description.")
    })
    @ResponseBody
    public SettingsListResponse getSiteOrPortalDescription(
        @Parameter(hidden = true)
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
        Optional<Source> source;
        String nodeDefault;
        if (NodeInfo.DEFAULT_NODE.equals(node.getId())) {
            source = sourceRepository.findById(settingManager.getSiteId());
            nodeDefault = "true";
        } else {
            source = sourceRepository.findById(node.getId());
            nodeDefault = "false";
        }
        if (source.isPresent()) {
            final List<Setting> settings = response.getSettings();
            String iso3langCode = languageUtils.getIso3langCode(request.getLocales());

            settings.add(
                new Setting().setName(Settings.NODE_DEFAULT)
                    .setValue(nodeDefault));
            settings.add(
                new Setting().setName(Settings.NODE)
                    .setValue(source.get().getUuid()));
            settings.add(
                new Setting().setName(Settings.NODE_NAME)
                    .setValue(StringUtils.isEmpty(source.get().getLabel(iso3langCode))
                        ? source.get().getName() : source.get().getLabel(iso3langCode)));
        }

        // Setting for OGC API Records service enabled
        String microservicesTargetUri = (String) request.getServletContext().getAttribute("MicroServicesProxy.targetUri");

        response.getSettings().add(
            new Setting().setName(Settings.MICROSERVICES_ENABLED)
                .setValue(Boolean.toString(StringUtils.isNotBlank(microservicesTargetUri)))
                .setDataType(SettingDataType.BOOLEAN));

        return response;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get settings",
        description = "Return public settings for anonymous users, internals are allowed for authenticated.")
    @RequestMapping(
        path = "/settings",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Settings.")
    })
    @ResponseBody
    public SettingsListResponse getSettingsSet(
        @Parameter(
            description = "Setting set. A common set of settings to retrieve.",
            required = false
        )
        @RequestParam(
            required = false
        )
        SettingSet[] set,
        @Parameter(
            description = "Setting key",
            required = false
        )
        @RequestParam(
            required = false
        )
        String[] key,
        @Parameter(
            hidden = true
        )
        HttpSession httpSession
    ) throws Exception {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile profile = session == null ? null : session.getProfile();

        List<String> settingList = new ArrayList<>();
        if (set == null && key == null) {
            final SettingRepository settingRepository = appContext.getBean(SettingRepository.class);
            final List<Setting> publicSettings =
                settingRepository.findAllByInternal(false);

            // Add virtual settings based on internal settings.
            // eg. if mail server is defined, allow email interactions ...
            String mailServer = settingManager.getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_HOST);
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
            List<Setting> settings = settingManager.getSettings(settingList.toArray(new String[0]));
            ListIterator<Setting> iterator = settings.listIterator();

            // Cleanup internal settings for not authenticated users.
            while (iterator.hasNext()) {
                Setting s = iterator.next();
                if (s.isInternal() && profile == null) {
                    settings.remove(s);
                }
            }

            SettingsListResponse response = new SettingsListResponse();
            response.setSettings(settings);
            return response;
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get settings with details",
        description = "Provides also setting properties.")
    @RequestMapping(
        path = "/settings/details",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Settings with details.")
    })
    @ResponseBody
    @PreAuthorize("hasAuthority('Administrator')")
    public List<Setting> getSettingsDetails(
        @Parameter(
            description = "Setting set. A common set of settings to retrieve.",
            required = false
        )
        @RequestParam(
            required = false
        )
        SettingSet[] set,
        @Parameter(
            description = "Setting key",
            required = false
        )
        @RequestParam(
            required = false
        )
        String[] key,
        @Parameter(hidden = true)
        HttpSession httpSession
    ) throws Exception {
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile profile = session == null ? null : session.getProfile();

        List<String> settingList = new ArrayList<>();
        if (set == null && key == null) {
            return
                settingManager.getAll();
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
            List<Setting> settings = settingManager.getSettings(settingList.toArray(new String[0]));
            ListIterator<Setting> iterator = settings.listIterator();

            // Cleanup internal settings for not authenticated users.
            while (iterator.hasNext()) {
                Setting s = iterator.next();
                if (s.isInternal() && profile == null) {
                    settings.remove(s);
                }
            }
            return settings;
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Save settings",
        description = "")
    @RequestMapping(
        path = "/settings",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.POST
    )
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Settings saved.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    public void saveSettings(
        @Parameter(hidden = false)
        @RequestParam
        Map<String, String> allRequestParams,
        HttpServletRequest request
    ) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        String currentUuid = settingManager.getSiteId();
        String oldSiteName = settingManager.getSiteName();
        String oldBaseUrl = settingManager.getBaseURL();

        if (!settingManager.setValues(allRequestParams)) {
            throw new OperationAbortedEx("Cannot set all values");
        }

        String newSiteName = settingManager.getSiteName();
        // Update site source name/translations if the site name is updated
        if (!oldSiteName.equals(newSiteName)) {
            Optional<Source> siteSourceOpt = sourceRepository.findById(currentUuid);

            if (siteSourceOpt.isPresent()) {
                Source siteSource = siteSourceOpt.get();
                siteSource.setName(newSiteName);
                siteSource.getLabelTranslations().forEach(
                    (l, t) -> siteSource.getLabelTranslations().put(l, newSiteName)
                );
                sourceRepository.save(siteSource);
            }
        }
        String newBaseUrl = settingManager.getBaseURL();
        // Update SpringDoc host information if the base url is changed.
        if (!oldBaseUrl.equals(newBaseUrl)) {
            OpenApiConfig.setHostRelatedInfo();
        }

        // Update the system default timezone. If the setting is blank use the timezone user.timezone property from command line or
        // TZ environment variable
        String zoneId = StringUtils.defaultIfBlank(settingManager.getValue(Settings.SYSTEM_SERVER_TIMEZONE, true),
            SettingManager.DEFAULT_SERVER_TIMEZONE.getId());
        TimeZone.setDefault(TimeZone.getTimeZone(zoneId));


        // And reload services
        String newUuid = allRequestParams.get(Settings.SYSTEM_SITE_SITE_ID_PATH);

        if (newUuid != null && !currentUuid.equals(newUuid)) {
            final IMetadataManager metadataManager = applicationContext.getBean(IMetadataManager.class);
            final Optional<Source> sourceOpt = sourceRepository.findById(currentUuid);

            if (sourceOpt.isPresent()) {
                Source source = sourceOpt.get();
                Source newSource = new Source(newUuid, source.getName(), source.getLabelTranslations(), source.getType());
                sourceRepository.save(newSource);

                PathSpec<Metadata, String> servicesPath = root -> root.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.sourceId);
                metadataManager.createBatchUpdateQuery(servicesPath, newUuid, MetadataSpecs.isHarvested(false));
                sourceRepository.delete(source);
            }
        }

        SettingInfo settingInfo = applicationContext.getBean(SettingInfo.class);
        ServiceContext context = ApiUtils.createServiceContext(request);
        ServerBeanPropertyUpdater.updateURL(settingInfo.getSiteUrl() +
                context.getBaseUrl(),
            applicationContext);

        // Reload services affected by updated settings
        reloadServices(context);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get site informations",
        description = "")
    @RequestMapping(
        path = "/info",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Site information.")
    })
    @ResponseBody
    public SiteInformation getInformation(HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        return new SiteInformation(context, (GeonetContext) context
            .getHandlerContext(Geonet.CONTEXT_NAME));
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Is CAS enabled?",
        description = "")
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

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Update staging profile",
        description = "TODO: Needs doc")
    @RequestMapping(
        path = "/info/staging/{profile}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Staging profile saved.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    @PreAuthorize("hasAuthority('Administrator')")
    public void updateStagingProfile(
        @PathVariable
        SystemInfo.Staging profile) {
        this.info.setStagingProfile(profile.toString());
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Is in read-only mode?",
        description = "")
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


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Is indexing?",
        description = "")
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
        return BatchOpsMetadataReindexer.isIndexing();
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Index",
        description = "")
    @RequestMapping(
        path = "/index",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseBody
    public HttpEntity indexSite(
        @Parameter(description = "Drop and recreate index",
            required = false)
        @RequestParam(required = false, defaultValue = "true")
        boolean reset,
        @Parameter(description = "Index. By default only remove record index.",
            required = false)
        @RequestParam(required = false, defaultValue = "records")
        String[] indices,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
        String bucket,
        HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        EsSearchManager searchMan = ApplicationContextHolder.get().getBean(EsSearchManager.class);
        boolean isIndexing = BatchOpsMetadataReindexer.isIndexing();

        if (isIndexing) {
            throw new NotAllowedException(
                "Indexing is already in progress. Wait for the current task to complete.");
        }

        if (reset) {
            searchMan.init(true, Optional.of(Arrays.asList(indices)));
        }

        // clean XLink Cache so that cache and index remain in sync
        Processor.clearCache();

        if (StringUtils.isEmpty(bucket)) {
            BaseMetadataManager metadataManager = ApplicationContextHolder.get().getBean(BaseMetadataManager.class);
            metadataManager.synchronizeDbWithIndex(context);
        } else {
            searchMan.rebuildIndex(context, false, bucket);
        }

        return new HttpEntity<>(HttpStatus.CREATED);
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Index status",
        description = "")
    @RequestMapping(
        path = "/index/status",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseBody
    public Status indexStatus(
        HttpServletRequest request
    ) throws Exception {
        EsServerStatusChecker serverStatusChecker = ApplicationContextHolder.get().getBean(EsServerStatusChecker.class);
        return serverStatusChecker.getStatus();
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Index synchronized with database",
        description = "")
    @RequestMapping(
        path = "/index/synchronized",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> indexAndDbSynchronizationStatus(
        HttpServletRequest request
    ) throws Exception {
        Map<String, Object> infoIndexDbSynch = new HashMap<>();
        long dbCount = metadataRepository.count();

        boolean isMdWorkflowEnable = settingManager.getValueAsBool(Settings.METADATA_WORKFLOW_ENABLE);
        if (isMdWorkflowEnable) {
            dbCount += metadataDraftRepository.count();
        }

        infoIndexDbSynch.put("db.count", dbCount);

        EsSearchManager searchMan = ApplicationContextHolder.get().getBean(EsSearchManager.class);
        CountResponse countResponse = esRestClient.getClient().count(
            CountRequest.of(b -> b.index(searchMan.getDefaultIndex()))
        );
        infoIndexDbSynch.put("index.count", countResponse.count());
        return infoIndexDbSynch;
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get build details",
        description = "To know when and how this version of the application was built.")
    @RequestMapping(
        path = "/info/build",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Build info.")
    })
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseBody
    public SystemInfo getSystemInfo(
    ) throws Exception {
        return ApplicationContextHolder.get().getBean(SystemInfo.class);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get notification levels",
        description = "")
    @RequestMapping(
        path = "/info/notificationLevels",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of notification levels.")
    })
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseBody
    public StatusValueNotificationLevel[] getNotificationLevel() {
        return StatusValueNotificationLevel.values();
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get proxy configuration details",
        description = "Get the proxy configuration.")
    @RequestMapping(
        path = "/info/proxy",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Proxy configuration.")
    })
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseBody
    public ProxyConfiguration getProxyConfiguration(
    ) {
        return Lib.net.getProxyConfiguration();
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Set catalog logo",
        description = "Logos are stored in the data directory " +
            "resources/images/harvesting as PNG or GIF images. " +
            "When a logo is assigned to the catalog, a new " +
            "image is created in images/logos/<catalogUuid>.png.")
    @RequestMapping(
        path = "/logo",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Logo set.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    public void setLogo(
        @Parameter(description = "Logo to use for the catalog")
        @RequestParam("file")
        String file,
        @Parameter(
            description = "Create favicon too",
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

        String nodeUuid = settingManager.getSiteId();

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
                                OutputStream defLogoOut = Files.newOutputStream(defaultLogo.getPath())
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


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get XSL tranformations available",
        description = "XSL transformations may be applied while importing or harvesting records.")
    @RequestMapping(
        path = "/info/transforms",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "XSLT available.")
    })
    @ResponseBody
    public List<String> getXslTransformations(
    ) throws Exception {
        List<String> list = new ArrayList<>();

        schemaManager.getSchemas().forEach(schema -> {
            try (DirectoryStream<Path> sheets = Files.newDirectoryStream(
                dataDirectory.getSchemaPluginsDir()
                    .resolve(schema)
                    .resolve("convert")
            )) {
                for (Path sheet : sheets) {
                    String id = sheet.toString();
                    if (id != null && id.contains("convert" + File.separator + "from") && id.endsWith(".xsl")) {
                        String name = com.google.common.io.Files.getNameWithoutExtension(
                            sheet.getFileName().toString());
                        list.add(IMPORT_STYLESHEETS_SCHEMA_PREFIX + schema + ":convert/" + name);
                    }
                }
            } catch (IOException e) {
                Log.warning(Geonet.GEONETWORK,
                    "Error getting conversion xslt transformations for schemas: " + e.getMessage());
            }
        });

        try (DirectoryStream<Path> sheets = Files.newDirectoryStream(
            dataDirectory.getWebappDir().resolve(Geonet.Path.IMPORT_STYLESHEETS)
        )) {
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


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Send an email to catalogue administrator with feedback about the application",
        description = "")
    @PostMapping(
        value = "/userfeedback",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<String> sendApplicationUserFeedback(
        @Parameter(
            description = "Recaptcha validation key."
        )
        @RequestParam(required = false, defaultValue = "") final String recaptcha,
        @Parameter(
            description = "User name.",
            required = true
        )
        @RequestParam final String name,
        @Parameter(
            description = "User organisation.",
            required = true
        )
        @RequestParam final String org,
        @Parameter(
            description = "User email address.",
            required = true
        )
        @RequestParam final String email,
        @Parameter(
            description = "A comment or question.",
            required = true
        )
        @RequestParam final String comments,
        @Parameter(hidden = true) final HttpServletRequest request
    ) throws Exception {
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        ResourceBundle messages = ResourceBundle.getBundle("org.fao.geonet.api.Messages", locale);

        boolean feedbackEnabled = settingManager.getValueAsBool(Settings.SYSTEM_USERFEEDBACK_ENABLE, false);
        if (!feedbackEnabled) {
            throw new FeatureNotEnabledException(
                "Application feedback is not enabled.")
                .withMessageKey("exception.resourceNotEnabled.applicationFeedback")
                .withDescriptionKey("exception.resourceNotEnabled.applicationFeedback.description");
        }

        boolean recaptchaEnabled = settingManager.getValueAsBool(Settings.SYSTEM_USERSELFREGISTRATION_RECAPTCHA_ENABLE);

        if (recaptchaEnabled) {
            boolean validRecaptcha = RecaptchaChecker.verify(recaptcha,
                settingManager.getValue(Settings.SYSTEM_USERSELFREGISTRATION_RECAPTCHA_SECRETKEY));
            if (!validRecaptcha) {
                return new ResponseEntity<>(
                    messages.getString("recaptcha_not_valid"), HttpStatus.PRECONDITION_FAILED);
            }
        }

        String to = settingManager.getValue(SYSTEM_FEEDBACK_EMAIL);

        Set<String> toAddress = new HashSet<>();
        toAddress.add(to);

        MailUtil.sendMail(new ArrayList<>(toAddress),
            messages.getString("site_user_feedback_title"),
            String.format(
                messages.getString("site_user_feedback_text"),
                name, email, org, comments),
            settingManager);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
