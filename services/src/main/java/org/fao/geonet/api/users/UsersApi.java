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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.users.model.UserDto;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.UserNotFoundEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.UserSavedSelectionRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.repository.specification.UserSpecs;
import org.fao.geonet.util.PasswordUtil;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

import static org.fao.geonet.repository.specification.UserGroupSpecs.hasProfile;
import static org.fao.geonet.repository.specification.UserGroupSpecs.hasUserId;
import static org.springframework.data.jpa.domain.Specifications.where;

@RequestMapping(value = {
    "/{portal}/api/users",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/users"
})
@Api(value = "users",
    tags = "users",
    description = "User operations")
@Controller("users")
public class UsersApi {

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    UserGroupRepository userGroupRepository;

    @Autowired
    UserSavedSelectionRepository userSavedSelectionRepository;

    @Autowired
    DataManager dataManager;


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
            allUsers.removeIf(u -> !userGroupIds.containsAll(getGroupIds(u.getId())) ||
                u.getProfile().equals(Profile.Administrator));
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

        if (myProfile.equals(Profile.Administrator) || myProfile.equals(Profile.UserAdmin) ||
            myUserId.equals(Integer.toString(userIdentifier))) {
            User user = userRepository.findOne(userIdentifier);

            if (user == null) {
                throw new UserNotFoundEx(Integer.toString(userIdentifier));
            }

            if (!(myUserId.equals(Integer.toString(userIdentifier))) && myProfile == Profile.UserAdmin) {

                //--- retrieve session user groups and check to see whether this user is
                //--- allowed to get this info
                List<Integer> adminlist = userGroupRepository.findGroupIds(where(hasUserId(Integer.parseInt(myUserId))).or(hasUserId
                    (userIdentifier)));
                if (adminlist.isEmpty()) {
                    throw new IllegalArgumentException("You don't have rights to do this because the user you want to edit is not part of your group");
                }
            }

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


        if (myUserId == null || myUserId.equals(Integer.toString(userIdentifier))) {
            throw new IllegalArgumentException(
                "You cannot delete yourself from the user database");
        }


        if (myProfile == Profile.UserAdmin) {
            final Integer iMyUserId = Integer.parseInt(myUserId);
            final List<Integer> groupIdsSessionUser = userGroupRepository
                .findGroupIds(where(hasUserId(iMyUserId)));

            final List<Integer> groupIdsUserToDelete = userGroupRepository
                .findGroupIds(where(hasUserId(userIdentifier)));

            if (CollectionUtils.intersection(groupIdsSessionUser, groupIdsUserToDelete).isEmpty()) {
                throw new IllegalArgumentException(
                    "You don't have rights to delete this user because the user is not part of your group");
            }
        }
        // Before processing DELETE check that the user is not referenced
        // elsewhere in the GeoNetwork database - an exception is thrown if
        // this is the case
        if (dataManager.isUserMetadataOwner(userIdentifier)) {
            IMetadataUtils metadataRepository = ApplicationContextHolder.get().getBean(IMetadataUtils.class);
            final long numUserRecords =  metadataRepository.count(MetadataSpecs.isOwnedByUser(userIdentifier));
            throw new IllegalArgumentException(
                String.format(
                    "Cannot delete a user that is also metadata owner of %d record(s) (can be records, templates, subtemplates). Change owner of those records or remove them first.",
                    numUserRecords));
        }

        if (dataManager.isUserMetadataStatus(userIdentifier)) {
            throw new IllegalArgumentException(
                "Cannot delete a user that has set a metadata status");
        }

        userGroupRepository.deleteAllByIdAttribute(UserGroupId_.userId,
            Arrays.asList(userIdentifier));

        userSavedSelectionRepository.deleteAllByUser(userIdentifier);

        try {
            userRepository.delete(userIdentifier);
        } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
            throw new UserNotFoundEx(Integer.toString(userIdentifier));
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(
        value = "Check if a user property already exist",
        notes = "",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "checkUserPropertyExist")
    @RequestMapping(
        value = "/properties/{property}",
        method = RequestMethod.GET
    )
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Property does not exist."),
        @ApiResponse(code = 404, message = "A property with that value already exist."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    public ResponseEntity<HttpStatus> checkUserPropertyExist(
        @ApiParam(
            value = "The user property to check"
        )
        @PathVariable
            String property,
        @ApiParam(
            value = "The value to search"
        )
        @RequestParam
            String exist) {
        if ("userid".equals(property)) {
            if (userRepository.count(where(UserSpecs.hasUserName(exist))) > 0) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } else if ("email".equals(property)) {
            if (userRepository.count(where(UserSpecs.hasEmail(exist))) > 0) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } else {
            throw new IllegalArgumentException(String.format("Property '%s' is not supported. You can only check username and email"));
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    @ApiOperation(
        value = "Creates a user",
        notes = "Creates a catalog user.",
        nickname = "createUser")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public ResponseEntity<String> createUser(
        @ApiParam(
            name = "user"
        )
        @RequestBody
            UserDto userDto,
        @ApiIgnore
            ServletRequest request,
        @ApiIgnore
            HttpSession httpSession
    ) throws Exception {
        Profile profile = Profile.findProfileIgnoreCase(userDto.getProfile());
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile myProfile = session.getProfile();

        if (profile == Profile.Administrator) {
            checkIfAtLeastOneAdminIsEnabled(userDto, userRepository);
        }

        // TODO: CheckAccessRights

        if (!myProfile.getAll().contains(profile)) {
            throw new IllegalArgumentException(
                "Trying to set profile to " + profile
                    + " max profile permitted is: " + myProfile);
        }

        if (StringUtils.isEmpty(userDto.getUsername())) {
            throw new IllegalArgumentException(Params.USERNAME
                + " is a required parameter for "
                + Params.Operation.NEWUSER + " " + "operation");
        }

        List<User> existingUsers = userRepository.findByUsernameIgnoreCase(userDto.getUsername());
        if (!existingUsers.isEmpty()) {
            throw new IllegalArgumentException("Users with username "
                + userDto.getUsername() + " ignore case already exists");
        }

        List<GroupElem> groups = new LinkedList<>();

        groups.addAll(processGroups(userDto.getGroupsRegisteredUser(), Profile.RegisteredUser));
        groups.addAll(processGroups(userDto.getGroupsEditor(), Profile.Editor));
        groups.addAll(processGroups(userDto.getGroupsReviewer(), Profile.Reviewer));
        groups.addAll(processGroups(userDto.getGroupsUserAdmin(), Profile.UserAdmin));

        User user = new User();
        user.getSecurity().setPassword(
            PasswordUtil.encoder(ApplicationContextHolder.get()).encode(
                userDto.getPassword()));

        fillUserFromParams(user, userDto);

        user = userRepository.save(user);
        setUserGroups(user, groups);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(
        value = "Update a user",
        notes = "Updates a catalog user.",
        nickname = "updateUser")
    @RequestMapping(value = "/{userIdentifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<String> updateUser(
        @ApiParam(
            value = "User identifier."
        )
        @PathVariable
            Integer userIdentifier,
        @ApiParam(
            name = "user"
        )
        @RequestBody
            UserDto userDto,
        @ApiIgnore
            ServletRequest request,
        @ApiIgnore
            HttpSession httpSession
    ) throws Exception {

        Profile profile = Profile.findProfileIgnoreCase(userDto.getProfile());

        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile myProfile = session.getProfile();
        String myUserId = session.getUserId();

        if (myProfile != Profile.Administrator && myProfile != Profile.UserAdmin && !myUserId.equals(Integer.toString(userIdentifier))) {
            throw new IllegalArgumentException("You don't have rights to do this");
        }

        if (profile == Profile.Administrator) {
            checkIfAtLeastOneAdminIsEnabled(userDto, userRepository);
        }

        // TODO: CheckAccessRights

        User user = userRepository.findOne(userIdentifier);
        if (user == null) {
            throw new IllegalArgumentException("No user found with id: "
                + userDto.getId());
        }

        // Check no duplicated username and if we are adding a duplicate existing name with other case combination
        List<User> usersWithUsernameIgnoreCase = userRepository.findByUsernameIgnoreCase(userDto.getUsername());
        if (usersWithUsernameIgnoreCase.size() != 0 &&
            (!usersWithUsernameIgnoreCase.stream().anyMatch(u -> u.getId() == userIdentifier)
                || usersWithUsernameIgnoreCase.stream().anyMatch(u ->
                u.getUsername().equals(userDto.getUsername()) && u.getId() != userIdentifier)
            )) {
            throw new IllegalArgumentException(String.format(
                "Another user with username '%s' ignore case already exists", user.getUsername()));
        }


        if (!myProfile.getAll().contains(profile)) {
            throw new IllegalArgumentException(
                "Trying to set profile to " + profile
                    + " max profile permitted is: " + myProfile);
        }

        List<GroupElem> groups = new LinkedList<>();

        groups.addAll(processGroups(userDto.getGroupsRegisteredUser(), Profile.RegisteredUser));
        groups.addAll(processGroups(userDto.getGroupsEditor(), Profile.Editor));
        groups.addAll(processGroups(userDto.getGroupsReviewer(), Profile.Reviewer));
        groups.addAll(processGroups(userDto.getGroupsUserAdmin(), Profile.UserAdmin));

        //If it is a useradmin updating,
        //maybe we don't know all the groups the user is part of
        if (!myProfile.equals(Profile.Administrator)) {
            List<Integer> myUserAdminGroups = userGroupRepository.findGroupIds(Specifications.where(
                hasProfile(myProfile)).and(hasUserId(Integer.parseInt(myUserId))));

            List<UserGroup> usergroups =
                userGroupRepository.findAll(Specifications.where(
                    hasUserId(Integer.parseInt(userDto.getId()))));

            //keep unknown groups as is
            for (UserGroup ug : usergroups) {
                if (!myUserAdminGroups.contains(ug.getGroup().getId())) {
                    groups.add(new GroupElem(ug.getProfile().name(),
                        ug.getGroup().getId()));
                }
            }
        }

        fillUserFromParams(user, userDto);

        user = userRepository.save(user);
        setUserGroups(user, groups);

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

        if (!password.equals(password2)) {
            throw new IllegalArgumentException("Passwords should be equal");
        }

        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile myProfile = session.getProfile();
        String myUserId = session.getUserId();

        if (myProfile != Profile.Administrator && myProfile != Profile.UserAdmin && !myUserId.equals(Integer.toString(userIdentifier))) {
            throw new IllegalArgumentException("You don't have rights to do this");
        }

        User user = userRepository.findOne(userIdentifier);
        if (user == null) {
            throw new UserNotFoundEx(Integer.toString(userIdentifier));
        }

        String passwordHash = PasswordUtil.encoder(ApplicationContextHolder.get()).encode(
            password);
        user.getSecurity().setPassword(passwordHash);
        user.getSecurity().getSecurityNotifications().remove(UserSecurityNotification.UPDATE_HASH_REQUIRED);
        userRepository.save(user);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(
        value = "Retrieve user groups",
        notes = "Retrieve the user groups.",
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

        if (myProfile == Profile.Administrator || myProfile == Profile.UserAdmin || myUserId.equals(Integer.toString(userIdentifier))) {
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
                if (!(myUserId.equals(Integer.toString(userIdentifier))) && myProfile == Profile.UserAdmin) {

                    //--- retrieve session user groups and check to see whether this user is
                    //--- allowed to get this info
                    List<Integer> adminList = userGroupRepository.findGroupIds(where(UserGroupSpecs.hasUserId(Integer.parseInt(myUserId)))
                        .or(UserGroupSpecs.hasUserId(userIdentifier)));
                    if (adminList.isEmpty()) {
                        throw new SecurityException("You don't have rights to do this because the user you want is not part of your group");
                    }
                }

                //--- retrieve user groups of the user id supplied
                userGroups = userGroupRepository.findAll(UserGroupSpecs.hasUserId(userIdentifier));
            }

            return userGroups;
        } else {
            throw new SecurityException("You don't have rights to do get the groups for this user");
        }
    }

    private List<Integer> getGroupIds(int userId) {
        return  userGroupRepository.findGroupIds(hasUserId(userId));
    }

    private void setUserGroups(final User user, List<GroupElem> userGroups)
        throws Exception {

        Collection<UserGroup> all = userGroupRepository.findAll(UserGroupSpecs
            .hasUserId(user.getId()));

        // Have a quick reference of existing groups and profiles for this user
        Set<String> listOfAddedProfiles = new HashSet<String>();
        for (UserGroup ug : all) {
            String key = ug.getProfile().name() + ug.getGroup().getId();
            if (!listOfAddedProfiles.contains(key)) {
                listOfAddedProfiles.add(key);
            }
        }

        // We start removing all old usergroup objects. We will remove the
        // explicitly defined for this call
        Collection<UserGroup> toRemove = new ArrayList<UserGroup>();
        toRemove.addAll(all);

        // New pairs of group-profile we need to add
        Collection<UserGroup> toAdd = new ArrayList<UserGroup>();

        // For each of the parameters on the request, make sure the group is
        // updated.
        for (GroupElem element : userGroups) {
            Integer groupId = element.getId();
            Group group = groupRepository.findOne(groupId);
            String profile = element.getProfile();
            // The user has a new group and profile

            // Combine all groups editor and reviewer groups
            if (profile.equals(Profile.Reviewer.name())) {
                final UserGroup userGroup = new UserGroup().setGroup(group)
                    .setProfile(Profile.Editor).setUser(user);
                String key = Profile.Editor.toString() + group.getId();
                if (!listOfAddedProfiles.contains(key)) {
                    toAdd.add(userGroup);
                    listOfAddedProfiles.add(key);
                }

                // If the user is already part of this group with this profile,
                // leave it alone:
                for (UserGroup g : all) {
                    if (g.getGroup().getId() == groupId
                        && g.getProfile().equals(Profile.Editor)) {
                        toRemove.remove(g);
                    }
                }
            }

            final UserGroup userGroup = new UserGroup().setGroup(group)
                .setProfile(Profile.findProfileIgnoreCase(profile))
                .setUser(user);
            String key = profile + group.getId();
            if (!listOfAddedProfiles.contains(key)) {
                toAdd.add(userGroup);
                listOfAddedProfiles.add(key);

            }

            // If the user is already part of this group with this profile,
            // leave it alone:
            for (UserGroup g : all) {
                if (g.getGroup().getId() == groupId
                    && g.getProfile().name().equalsIgnoreCase(profile)) {
                    toRemove.remove(g);
                }
            }
        }

        // Remove deprecated usergroups (if any)
        userGroupRepository.delete(toRemove);

        // Add only new usergroups (if any)
        userGroupRepository.save(toAdd);

    }


    private List<GroupElem> processGroups(List<String> groupsToProcessList, Profile profile) {
        List<GroupElem> groups = new LinkedList<GroupElem>();
        for (String g : groupsToProcessList) {
            groups.add(new GroupElem(profile.name(), Integer.parseInt(g)));
        }

        return groups;
    }


    private void fillUserFromParams(User user, UserDto userDto) {
        user.setEnabled(userDto.isEnabled());

        if (StringUtils.isNotEmpty(userDto.getUsername())) {
            user.setUsername(userDto.getUsername());
        }

        user.setProfile(Profile.valueOf(userDto.getProfile()));
        user.setName(userDto.getName());
        user.setSurname(userDto.getSurname());
        user.setOrganisation(userDto.getOrganisation());
        user.setName(userDto.getName());
        user.setKind(userDto.getKind());

        if (!userDto.getAddresses().isEmpty()) {
//            Trigger constraint exception.
//            Updating only the first (as only one supported on client side)
//            user.getAddresses().clear();
//            for (Address address : userDto.getAddresses()) {
//                address.setZip(null);
//                user.getAddresses().add(address);
//            }

            // Updating only the first (as only one supported on client side)
            // TODO: Support multiple addresses
            Set<Address> userAddresses = user.getAddresses();
            Address userAddress;

            if (userAddresses.isEmpty()) {
                userAddress = new Address();
                userAddresses.add(userAddress);
            } else {
                userAddress = (Address) userAddresses.toArray()[0];
            }

            for (Address address : userDto.getAddresses()) {
                userAddress.setAddress(address.getAddress());
                userAddress.setCity(address.getCity());
                userAddress.setCountry(address.getCountry());
                userAddress.setState(address.getState());
                userAddress.setZip(address.getZip());
            }
        }

        if (!userDto.getEmailAddresses().isEmpty()) {
            user.getEmailAddresses().clear();
            for (String mail : userDto.getEmailAddresses()) {
                user.getEmailAddresses().add(mail);
            }
        }
    }

    /**
     * Check if removing userDto from the admins there are still at least one user administrator in the system. .
     *
     * @param userDto        the user to check.
     * @param userRepository user repository to retrieve users from.
     * @throws IllegalArgumentException thrown if userDto is the last administrator user in the system.
     */
    private void checkIfAtLeastOneAdminIsEnabled(UserDto userDto, UserRepository userRepository) {
        // Check at least 1 administrator is enabled
        if (StringUtils.isNotEmpty(userDto.getId()) && (!userDto.isEnabled())) {
            List<User> adminEnabledList = userRepository.findAll(
                Specifications.where(UserSpecs.hasProfile(Profile.Administrator)).and(UserSpecs.hasEnabled(true)));
            if (adminEnabledList.size() == 1) {
                User adminUser = adminEnabledList.get(0);
                if (adminUser.getId() == Integer.parseInt(userDto.getId())) {
                    throw new IllegalArgumentException(
                        "Trying to disable all administrator users is not allowed");
                }
            }
        }
    }
}


class GroupElem {

    private String profile;
    private Integer id;

    public GroupElem(String profile, Integer id) {
        this.id = id;
        this.profile = profile;
    }

    public String getProfile() {
        return profile;
    }

    public Integer getId() {
        return id;
    }
}
