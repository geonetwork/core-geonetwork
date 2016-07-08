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

import com.vividsolutions.jts.util.Assert;
import io.swagger.annotations.ApiParam;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.OperationNotAllowedEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.repository.specification.UserSpecs;
import org.fao.geonet.util.PasswordUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jeeves.server.UserSession;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import static org.fao.geonet.repository.specification.UserGroupSpecs.hasUserId;
import static org.springframework.data.jpa.domain.Specifications.where;

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
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public List<User> getUsers(
        @ApiIgnore
            HttpSession httpSession
    ) throws Exception {
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile profile = session.getProfile();

        UserRepository userRepository = ApplicationContextHolder.get().getBean(UserRepository.class);

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

        return null;
    }


    @ApiOperation(
        value = "Get user",
        notes = "",
        nickname = "getUser")
    @RequestMapping(
        value = "/{userIdentifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public User getUser(
        @ApiParam(
            value = "User identifier."
        )
        @PathVariable
            Integer userIdentifier,
        @ApiIgnore
            HttpSession httpSession

    ) throws Exception {
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile myProfile = session.getProfile();
        String myUserId = session.getUserId();

        if (myProfile == Profile.Administrator || myProfile == Profile.UserAdmin || myUserId.equals(userIdentifier)) {
            UserRepository userRepository = ApplicationContextHolder.get().getBean(UserRepository.class);

            User user = userRepository.findOne(Integer.valueOf(userIdentifier));

            return user;
        } else {
            throw new IllegalArgumentException("You don't have rights to do this");
        }

    }

    @ApiOperation(
        value = "Delete a user",
        notes = "Deletes a catalog user by identifier.",
        nickname = "deleteUser")
    @RequestMapping(value = "/{userIdentifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public ResponseEntity<String> deleteUser(
        @ApiParam(
            value = "User identifier."
        )
        @PathVariable
            Integer userIdentifier,
        @ApiIgnore
            ServletRequest request,
        @ApiIgnore
        HttpSession httpSession
    ) throws Exception {
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile myProfile = session.getProfile();
        String myUserId = session.getUserId();

        UserRepository userRepository = ApplicationContextHolder.get().getBean(UserRepository.class);
        UserGroupRepository userGroupRepository = ApplicationContextHolder.get().getBean(UserGroupRepository.class);

        if (myUserId == null || new Integer(myUserId).equals(userIdentifier)) {
            throw new IllegalArgumentException(
                "You cannot delete yourself from the user database");
        }


        if (myProfile == Profile.UserAdmin) {
            final Integer iMyUserId = Integer.valueOf(myUserId);
            final List<Integer> groupIds = userGroupRepository
                .findGroupIds(where(hasUserId(iMyUserId)).or(
                    hasUserId(userIdentifier)));

            if (groupIds.isEmpty()) {
                throw new IllegalArgumentException(
                    "You don't have rights to delete this user because the user is not part of your group");
            }
        }

        DataManager dataManager = ApplicationContextHolder.get().getBean(DataManager.class);

        // Before processing DELETE check that the user is not referenced
        // elsewhere in the GeoNetwork database - an exception is thrown if
        // this is the case
        if (dataManager.isUserMetadataOwner(userIdentifier)) {
            throw new IllegalArgumentException(
                "Cannot delete a user that is also a metadata owner");
        }

        if (dataManager.isUserMetadataStatus(userIdentifier)) {
            throw new IllegalArgumentException(
                "Cannot delete a user that has set a metadata status");
        }

        userGroupRepository.deleteAllByIdAttribute(UserGroupId_.userId,
            Arrays.asList(userIdentifier));
        userRepository.delete(userIdentifier);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    @ApiOperation(
        value = "Resets user password",
        notes = "Resets the user password.",
        nickname = "resetUserPassword")
    @RequestMapping(value = "/{userIdentifier}/actions/forget-password",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<String> resetUserPassword(
        @ApiParam(
            value = "User identifier."
        )
        @PathVariable
            Integer userIdentifier,
        @ApiParam(
            value = "Password to change."
        )
        @RequestParam(value = Params.PASSWORD) String password,
        @ApiParam(
            value = "Password to change (repeat)."
        )
        @RequestParam(value = Params.PASSWORD + "2") String password2,
        @ApiIgnore
            ServletRequest request,
        @ApiIgnore
            HttpSession httpSession
    ) throws Exception {
        Assert.equals(password, password2);

        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile myProfile = session.getProfile();
        String myUserId = session.getUserId();

        if (myProfile != Profile.Administrator && myProfile != Profile.UserAdmin && !myUserId.equals(userIdentifier)) {
            throw new IllegalArgumentException("You don't have rights to do this");
        }

        UserRepository userRepository = ApplicationContextHolder.get().getBean(UserRepository.class);

        User user = userRepository.findOne(userIdentifier);
        user.getSecurity().setPassword(
            PasswordUtil.encoder(ApplicationContextHolder.get()).encode(
                password));
        userRepository.save(user);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(
        value = "Retrieve user groups",
        notes = "",
        nickname = "retrieveUserGroups")
    @RequestMapping(value = "/{userIdentifier}/groups",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET
    )
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public List<UserGroup> retrieveUserGroups(
        @ApiParam(
            value = "User identifier."
        )
        @PathVariable
            Integer userIdentifier,
        @ApiIgnore
            ServletRequest request,
        @ApiIgnore
            HttpSession httpSession
    ) throws Exception {
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile myProfile = session.getProfile();
        String myUserId = session.getUserId();

        final UserRepository userRepository = ApplicationContextHolder.get().getBean(UserRepository.class);
        final UserGroupRepository userGroupRepository = ApplicationContextHolder.get().getBean(UserGroupRepository.class);

        if (myProfile == Profile.Administrator || myProfile == Profile.UserAdmin || myUserId.equals(userIdentifier)) {
            // -- get the profile of the user id supplied
            User user = userRepository.findOne(userIdentifier);
            if (user == null) {
                throw new IllegalArgumentException("user " + userIdentifier + " doesn't exist");
            }

            String userProfile = user.getProfile().name();

            List<UserGroup> userGroups;

            if (myProfile == Profile.Administrator && userProfile.equals(Profile.Administrator.name())) {
                // Return all groups for administrator.
                // TODO: Check if a better option returning instead of UserGroup a customised GroupDTO
                // containing all group properties and user profile
                userGroups = new ArrayList<UserGroup>();
                final GroupRepository groupRepository = ApplicationContextHolder.get().getBean(GroupRepository.class);

                List<Group> groups = groupRepository.findAll();

                for (Group g : groups) {
                    UserGroup ug = new UserGroup();
                    UserGroupId ugId = new UserGroupId();
                    ugId.setProfile(Profile.Administrator);
                    ugId.setGroupId(g.getId());
                    ugId.setUserId(userIdentifier);

                    ug.setGroup(g);
                    ug.setUser(user);
                    ug.setProfile(Profile.Administrator);
                    ug.setId(ugId);

                    userGroups.add(ug);
                }
            } else {
                if (!(myUserId.equals(userIdentifier)) && myProfile == Profile.UserAdmin) {

                    //--- retrieve session user groups and check to see whether this user is
                    //--- allowed to get this info
                    List<Integer> adminList = userGroupRepository.findGroupIds(where(UserGroupSpecs.hasUserId(Integer.valueOf(myUserId)))
                        .or(UserGroupSpecs.hasUserId(Integer.valueOf(userIdentifier))));
                    if (adminList.isEmpty()) {
                        throw new SecurityException("You don't have rights to do this because the user you want is not part of your group");
                    }
                }

                //--- retrieve user groups of the user id supplied
                userGroups = userGroupRepository.findAll(UserGroupSpecs.hasUserId(Integer.valueOf(userIdentifier)));
            }

            return userGroups;
        } else {
            throw new SecurityException("You don't have rights to do get the groups for this user");
        }
    }

    private List<Integer> getGroupIds(int userId) {
        return ApplicationContextHolder.get().getBean(UserGroupRepository.class)
            .findGroupIds(hasUserId(userId));
    }
}
