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

import static org.springframework.http.HttpStatus.NO_CONTENT;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.users.model.MeResponse;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.server.UserSession;
import springfox.documentation.annotations.ApiIgnore;

@RequestMapping(value = {
    "/{portal}/api/me",
    "/{portal}/api/" + API.VERSION_0_1 +
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
            "know if current user is authenticated or not." +
            "It returns also info about groups and profiles.",
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

        if (userSession.isAuthenticated()) {

            MeResponse myInfos = new MeResponse().setId(userSession.getUserId()).setProfile(userSession.getProfile().name())
                .setUsername(userSession.getUsername()).setName(userSession.getName()).setSurname(userSession.getSurname())
                .setEmail(userSession.getEmailAddr()).setOrganisation(userSession.getOrganisation())
                .setAdmin(userSession.getProfile().equals(Profile.Administrator))
                .setGroupsWithRegisteredUser(getGroups(userSession, Profile.RegisteredUser))
                .setGroupsWithEditor(getGroups(userSession, Profile.Editor))
                .setGroupsWithReviewer(getGroups(userSession, Profile.Reviewer))
                .setGroupsWithUserAdmin(getGroups(userSession, Profile.UserAdmin));

            return new ResponseEntity<>(myInfos, HttpStatus.OK);
        }

        return new ResponseEntity<>(NO_CONTENT);
    }

    /**
     *  Retrieves the user's groups ids
     * @param session
     * @param profile
     * @return
     * @throws SQLException
     */
    private List<Integer> getGroups(UserSession session, Profile profile) throws SQLException {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        final UserGroupRepository userGroupRepository = applicationContext.getBean(UserGroupRepository.class);

        Specifications<UserGroup> spec = Specifications.where(UserGroupSpecs.hasUserId(session.getUserIdAsInt()));
        spec = spec.and(UserGroupSpecs.hasProfile(profile));

        return userGroupRepository.findGroupIds(spec);
    }
}
