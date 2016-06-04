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
import org.fao.geonet.GeonetContext;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.api.API;
import org.fao.geonet.api.site.model.SettingSet;
import org.fao.geonet.api.site.model.SettingsListResponse;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.Setting;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.SettingRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.component.ProfileManager;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

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
            SettingManager.SYSTEM_SITE_NAME_PATH,
            Setting.SYSTEM_SITE_ORGANIZATION.key,
            SettingManager.SYSTEM_SITE_SITE_ID_PATH,
            "system/platform/version",
            "system/platform/subVersion"
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
        String[] key
    ) throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();
        SettingManager sm = appContext.getBean(SettingManager.class);

        ServiceContext context = ServiceContext.get();
        UserSession session = context.getUserSession();
        Profile profile = session.getProfile();

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
            if (key != null && key.length > 0){
                Collections.addAll(settingList, key);
            }
            List<org.fao.geonet.domain.Setting> settings = sm.getSettings(settingList.toArray(new String[0]));
            ListIterator<org.fao.geonet.domain.Setting> iterator = settings.listIterator();

            // Cleanup internal settings for not authenticated users.
            while(iterator.hasNext()) {
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
        path = "/information",
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
        path = "/cas",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public boolean isCasEnabled(
    ) throws Exception {
        // TODO-API: Always return true due to null ServiceContext.
        return ProfileManager.isCasEnabled();
    }

    @ApiOperation(
        value = "Is in read-only mode?",
        notes = "",
        nickname = "isReadOnly")
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
        nickname = "isIndexing")
    @RequestMapping(
        path = "/indexing",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public boolean isIndexing(
    ) throws Exception {
        return ApplicationContextHolder.get().getBean(DataManager.class).isIndexing();
    }

    @ApiOperation(
        value = "Get build details",
        notes = "To know when and how this version of the application was built.",
        nickname = "getSystemInfo")
    @RequestMapping(
        path = "/buildinfo",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public SystemInfo getSystemInfo(
    ) throws Exception {
        return ApplicationContextHolder.get().getBean(SystemInfo.class);
    }
}
