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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import org.apache.commons.lang.StringUtils;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequestMapping(value = {
    "/{portal}/api/ui"
})
@Tag(name = "ui",
    description = "User interface configuration operations")
@Controller("ui")
public class UiSettingApi {

    @Autowired
    UiSettingsRepository uiSettingsRepository;

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    UserGroupRepository userGroupRepository;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get UI configuration",
        description = "")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of configuration.")
    })
    @ResponseBody
    public List<UiSetting> getUiConfigurations(
    ) throws Exception {
        return uiSettingsRepository.findAll();
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Create a UI configuration",
        description = "")
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
        @ApiResponse(responseCode = "201", description = "UI configuration created. Return the new UI configuration identifier."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @PreAuthorize("hasAuthority('UserAdmin')")
    public ResponseEntity<String> putUiConfiguration(
        @Parameter(
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

        Optional<UiSetting> one = uiSettingsRepository.findById(uiConfiguration.getId());
        if (one.isPresent()) {
            throw new IllegalArgumentException(String.format(
                "A UI configuration with id '%d' already exist", uiConfiguration.getId()
            ));
        } else {
            // TODO: Validate JSON

            final UiSetting save = uiSettingsRepository.save(uiConfiguration);
            return new ResponseEntity(save.getId(), HttpStatus.CREATED);
        }
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get a UI configuration",
        description = "")
    @RequestMapping(
        value = "/{uiIdentifier}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        },
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "UI configuration.")
    })
    @ResponseBody
    public UiSetting getUiConfiguration(
        @Parameter(
            description = "UI identifier",
            required = true
        )
        @PathVariable
            String uiIdentifier
    ) throws Exception {
        Optional<UiSetting> uiConfiguration = uiSettingsRepository.findById(uiIdentifier);
        if (!uiConfiguration.isPresent()) {
            throw new ResourceNotFoundException(String.format(
                "UI configuration with id '%s' does not exist.",
                uiIdentifier
            ));
        }
        return uiConfiguration.get();
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Update a UI configuration",
        description = "")
    @RequestMapping(
        value = "/{uiIdentifier}",
        method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "UI configuration updated."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public ResponseEntity updateUiConfiguration(
        @Parameter(
            description = "UI configuration identifier",
            required = true
        )
        @PathVariable
            String uiIdentifier,
        @Parameter(
            name = "UI configuration"
        )
        @RequestBody
            UiSetting uiConfiguration,
        @Parameter(hidden = true) HttpSession httpSession
    ) throws Exception {
        Optional<UiSetting> one = uiSettingsRepository.findById(uiIdentifier);
        if (one.isPresent()) {
            // For user admin, check that the UI is used by a portal managed by the user.
            UserSession session = ApiUtils.getUserSession(httpSession);
            boolean isUserAdmin = session.getProfile().equals(Profile.UserAdmin);
            if (isUserAdmin) {
                if (canManageUISettings(uiIdentifier, session)) {
                    uiSettingsRepository.save(uiConfiguration);
                    return new ResponseEntity(HttpStatus.NO_CONTENT);
                } else {
                    throw new NotAllowedException(String.format(
                        "UI configuration with id '%s' is not used in any portal managed by current user. You are not allowed to update this configuration.",
                        uiIdentifier
                    ));
                }
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


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Remove a UI Configuration",
        description = "")
    @RequestMapping(
        value = "/{uiIdentifier}",
        method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "UI Configuration removed."),
        @ApiResponse(responseCode = "404", description = "UI Configuration not found."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @PreAuthorize("hasAuthority('UserAdmin')")
    public ResponseEntity deleteUiConfiguration(
        @Parameter(
            description = "UI configuration identifier",
            required = true
        )
        @PathVariable
            String uiIdentifier,
        @Parameter(hidden = true) HttpSession httpSession
    ) throws Exception {
        Optional<UiSetting> one = uiSettingsRepository.findById(uiIdentifier);
        if (one.isPresent()) {
            UserSession session = ApiUtils.getUserSession(httpSession);
            boolean isUserAdmin = session.getProfile().equals(Profile.UserAdmin);
            if (isUserAdmin) {
                if (canManageUISettings(uiIdentifier, session)) {
                    uiSettingsRepository.deleteById(uiIdentifier);
                    return new ResponseEntity(HttpStatus.NO_CONTENT);
                }
                throw new NotAllowedException(String.format(
                    "UI configuration with id '%s' is not used in any portal managed by current user. You are not allowed to update this configuration.",
                    uiIdentifier
                ));
            } else {
                uiSettingsRepository.deleteById(uiIdentifier);
            }

        } else {
            throw new ResourceNotFoundException(String.format(
                "UI Configuration with id '%s' does not exist.",
                uiIdentifier
            ));
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    private boolean canManageUISettings(String uiIdentifier, UserSession session) {
        Specification<UserGroup> spec =
            Specification.where(UserGroupSpecs.hasUserId(session.getUserIdAsInt()));
        spec = spec.and(UserGroupSpecs.hasProfile(Profile.UserAdmin));

        Set<Integer> ids = new HashSet<Integer>(userGroupRepository.findGroupIds(spec));

        final List<Source> sources = sourceRepository.findByGroupOwnerIn(ids);

        boolean isUiConfigForOneOfUserPortal = sources.stream().anyMatch(source -> uiIdentifier.equals(source.getUiConfig()));

        return isUiConfigForOneOfUserPortal;
    }
}
