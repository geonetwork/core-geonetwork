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

package org.fao.geonet.api.users;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.users.model.MeResponse;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.kernel.AccessManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RequestMapping(value = {
    "/{portal}/api/me"
})
@Tag(name = "me",
    description = "Me operations")
@RestController
public class MeApi {

    @Autowired
    AccessManager accessManager;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get information about me",
        description = "If not authenticated, return status 204 (NO_CONTENT), " +
            "else return basic user information. This operation is usually used to " +
            "know if current user is authenticated or not." +
            "It returns also info about groups and profiles.")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authenticated. Return user details."),
        @ApiResponse(responseCode = "204", description = "Not authenticated.", content = {@Content(schema = @Schema(hidden = true))})
    })
    @ResponseStatus(OK)
    @ResponseBody
    public ResponseEntity<MeResponse> getMe(
        @Parameter(hidden = true)
            HttpSession session
    ) throws Exception {

        UserSession userSession = ApiUtils.getUserSession(session);

        if (userSession.isAuthenticated()) {

            MeResponse myInfos = new MeResponse().setId(userSession.getUserId()).setProfile(userSession.getProfile().name())
                .setUsername(userSession.getUsername()).setName(userSession.getName()).setSurname(userSession.getSurname())
                .setEmail(userSession.getEmailAddr()).setOrganisation(userSession.getOrganisation())
                .setAdmin(userSession.getProfile().equals(Profile.Administrator))
                .setGroupsWithRegisteredUser(accessManager.getGroups(userSession, Profile.RegisteredUser))
                .setGroupsWithEditor(accessManager.getGroups(userSession, Profile.Editor))
                .setGroupsWithReviewer(accessManager.getGroups(userSession, Profile.Reviewer))
                .setGroupsWithUserAdmin(accessManager.getGroups(userSession, Profile.UserAdmin));

            return new ResponseEntity<>(myInfos, HttpStatus.OK);
        }

        return new ResponseEntity<>(NO_CONTENT);
    }
}
