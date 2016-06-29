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

import io.swagger.annotations.*;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.users.model.MeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpSession;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import springfox.documentation.annotations.ApiIgnore;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RequestMapping(value = {
    "/api/me",
    "/api/" + API.VERSION_0_1 +
        "/me"
})
@Api(value = "me",
    tags = "me",
    description = "Me operations")
@Controller("me")
public class MeApi {

    @ApiOperation(
        value = "Get information about me",
        notes = "If not authenticated, return status 204 (NO_CONTENT), " +
            "else return basic user information. This operation is usually used to " +
            "know if current user is authenticated or not.",
        nickname = "getMe")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Authenticated. Return user details."),
        @ApiResponse(code = 204, message = "Not authenticated.")
    })
    @ResponseBody
    public ResponseEntity<MeResponse> getMe(
        @ApiIgnore
        @ApiParam(
            hidden = true
        )
        HttpSession session
    ) throws Exception {
        UserSession userSession = ApiUtils.getUserSession(session);
        return userSession.isAuthenticated() ?
            new ResponseEntity<>(new MeResponse()
                .setId(userSession.getUserId())
                .setProfile(userSession.getProfile().name())
                .setUsername(userSession.getUsername())
                .setName(userSession.getName())
                .setSurname(userSession.getSurname())
                .setEmail(userSession.getEmailAddr())
                .setOrganisation(userSession.getOrganisation()),
                    HttpStatus.OK) :
            new ResponseEntity<>(NO_CONTENT);
    }
}
