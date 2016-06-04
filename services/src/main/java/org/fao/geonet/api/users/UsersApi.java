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

import org.fao.geonet.api.API;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.User_;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.repository.specification.UserSpecs;
import org.jdom.Element;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

@RequestMapping(value = {
    "/api/users",
    "/api/" + API.VERSION_0_1 +
        "/users"
})
@Api(value = "users",
    tags = "users",
    description = "User operations")
@Controller("users")
public class UsersApi {

    @ApiOperation(
        value = "Get users",
        notes = "",
        nickname = "getUsers")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<User> getUsers() throws Exception {
        ServiceContext context = ServiceContext.get();
        UserSession session = context.getUserSession();
        if (session.isAuthenticated()) {
            Profile profile = session.getProfile();
            UserRepository userRepository = context.getBean(UserRepository.class);
            if (profile == Profile.Administrator) {
                return userRepository.findAll(SortUtils.createSort(User_.name));
            } else if (profile != Profile.UserAdmin) {
                return userRepository.findAll(UserSpecs.hasUserId(session.getUserIdAsInt()));
            } else if (profile == Profile.UserAdmin) {
                int userId = session.getUserIdAsInt();
                final List<Integer> userGroupIds =
                    getGroupIds(userId);

                List<User> allUsers = userRepository.findAll(SortUtils.createSort(User_.name));

                // Filter users which are not in current user admin groups
                allUsers.removeIf(u -> userGroupIds.containsAll(getGroupIds(u.getId())));
//              TODO-API: Check why there was this check on profiles ?
//                    if (!profileSet.contains(profile))
//                        alToRemove.add(elRec);

                return allUsers;
            }
        } else {
            return null;
        }
        return null;
    }

    private List<Integer> getGroupIds(int userId) {
        ServiceContext context = ServiceContext.get();
        return context.getBean(UserGroupRepository.class)
            .findGroupIds(UserGroupSpecs.hasUserId(userId));
    }
}
