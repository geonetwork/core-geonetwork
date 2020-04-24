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

package org.fao.geonet.api.uisetting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.server.UserSession;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.UiSetting;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.UiSettingsRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequestMapping(value = {
    "/{portal}/api/ui",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/ui"
})
@Api(value = "ui",
    tags = "ui",
    description = "User interface configuration operations")
@Controller("ui")
public class UiSettingApi {

    @Autowired
    UiSettingsRepository uiSettingsRepository;

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    UserGroupRepository userGroupRepository;

    @ApiOperation(
        value = "Get UI configuration",
        notes = "",
        nickname = "getUiConfigurations")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of configuration.")
    })
    @ResponseBody
    public List<UiSetting> getUiConfigurations(
    ) throws Exception {
        return uiSettingsRepository.findAll();
    }


    @ApiOperation(
        value = "Create a UI configuration",
        notes = "",
        nickname = "putUiConfiguration")
    @RequestMapping(
        method = RequestMethod.PUT,
        consumes = {
            MediaType.APPLICATION_JSON_VALUE
        },
        produces = {
            MediaType.TEXT_PLAIN_VALUE
        }
    )
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "UI configuration created. Return the new UI configuration identifier."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @PreAuthorize("hasRole('UserAdmin')")
    public ResponseEntity<String> putUiConfiguration(
        @ApiParam(
            name = "uiConfiguration"
        )
        @RequestBody
            UiSetting uiConfiguration
    ) throws Exception {
        if (StringUtils.isEmpty(uiConfiguration.getId())) {
            throw new IllegalArgumentException(String.format(
                "A UI configuration MUST have an id. The id could be a string to easily identify the configuration.", uiConfiguration.getId()
            ));
        }

        UiSetting one = uiSettingsRepository.findOne(uiConfiguration.getId());
        if (one != null) {
            throw new IllegalArgumentException(String.format(
                "A UI configuration with id '%d' already exist", uiConfiguration.getId()
            ));
        } else {
            // TODO: Validate JSON

            final UiSetting save = uiSettingsRepository.save(uiConfiguration);
            return new ResponseEntity(save.getId(), HttpStatus.CREATED);
        }
    }


    @ApiOperation(
        value = "Get a UI configuration",
        notes = "",
        nickname = "getUiConfiguration")
    @RequestMapping(
        value = "/{uiIdentifier}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        },
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "UI configuration.")
    })
    @ResponseBody
    public UiSetting getUiConfiguration(
        @ApiParam(
            value = "UI identifier",
            required = true
        )
        @PathVariable
            String uiIdentifier
    ) throws Exception {
        UiSetting uiConfiguration = uiSettingsRepository.findOne(uiIdentifier);
        if (uiConfiguration == null) {
            throw new ResourceNotFoundException(String.format(
                "UI configuration with id '%s' does not exist.",
                uiIdentifier
            ));
        }
        return uiConfiguration;
    }


    @ApiOperation(
        value = "Update a UI configuration",
        notes = "",
        nickname = "updateUiConfiguration")
    @RequestMapping(
        value = "/{uiIdentifier}",
        method = RequestMethod.PUT)
    @PreAuthorize("hasRole('UserAdmin')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "UI configuration updated."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public ResponseEntity updateUiConfiguration(
        @ApiParam(
            value = "UI configuration identifier",
            required = true
        )
        @PathVariable
            String uiIdentifier,
        @ApiParam(
            name = "UI configuration"
        )
        @RequestBody
        UiSetting uiConfiguration,
        @ApiIgnore @ApiParam(hidden = true) HttpSession httpSession
    ) throws Exception {
        UiSetting one = uiSettingsRepository.findOne(uiIdentifier);
        if (one != null) {
            // For user admin, check that the UI is used by a portal managed by the user.
            UserSession session = ApiUtils.getUserSession(httpSession);
            boolean isUserAdmin = session.getProfile().equals(Profile.UserAdmin);
            if (isUserAdmin) {
                Specifications<UserGroup> spec =
                    Specifications.where(UserGroupSpecs.hasUserId(session.getUserIdAsInt()));
                spec = spec.and(UserGroupSpecs.hasProfile(Profile.UserAdmin));

                Set<Integer> ids = new HashSet<Integer>(userGroupRepository.findGroupIds(spec));

                final List<Source> sources = sourceRepository.findByGroupOwnerIn(ids);
                boolean isUiConfigForOneOfUserPortal = false;
                for(Source s : sources) {
                    if (uiIdentifier.equals(s.getUiConfig())) {
                        uiSettingsRepository.save(uiConfiguration);
                        return new ResponseEntity(HttpStatus.NO_CONTENT);
                    }
                }
                throw new NotAllowedException(String.format(
                    "UI configuration with id '%s' is not used in any portal managed by current user. You are not allowed to update this configuration.",
                    uiIdentifier
                ));
            } else {
                uiSettingsRepository.save(uiConfiguration);
            }
        } else {
            throw new ResourceNotFoundException(String.format(
                "UI configuration with id '%s' does not exist.",
                uiIdentifier
            ));
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }



    @ApiOperation(
        value = "Remove a UI Configuration",
        notes = "",
        nickname = "deleteUiConfiguration")
    @RequestMapping(
        value = "/{uiIdentifier}",
        method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "UI Configuration removed."),
        @ApiResponse(code = 404, message = "UI Configuration not found."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @PreAuthorize("hasRole('UserAdmin')")
    public ResponseEntity deleteUiConfiguration(
        @ApiParam(
            value = "UI configuration identifier",
            required = true
        )
        @PathVariable
        String uiIdentifier
    ) throws Exception {
        UiSetting one = uiSettingsRepository.findOne(uiIdentifier);
        if (one != null) {
            uiSettingsRepository.delete(uiIdentifier);
        } else {
            throw new ResourceNotFoundException(String.format(
                "UI Configuration with id '%s' does not exist.",
                uiIdentifier
            ));
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
