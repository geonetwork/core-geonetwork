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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.users.model.UserDto;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.UserNotFoundEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.repository.specification.UserSpecs;
import org.fao.geonet.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.fao.geonet.kernel.setting.Settings.SYSTEM_USERS_IDENTICON;
import static org.fao.geonet.repository.specification.UserGroupSpecs.hasProfile;
import static org.fao.geonet.repository.specification.UserGroupSpecs.hasUserId;
import static org.springframework.data.jpa.domain.Specification.where;

@RequestMapping(value = {
    "/{portal}/api/users"
})
@Tag(name = "users",
    description = "User operations")
@Controller("users")
public class UsersApi {

    @Autowired
    SettingManager settingManager;

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

    private BufferedImage pixel;

    public UsersApi() {
        pixel = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        pixel.setRGB(0, 0, (0xFF));
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get users",
        description = "")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public List<User> getUsers(
        @Parameter(hidden = true)
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


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get user",
        description = "")
    @RequestMapping(
        value = "/{userIdentifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public User getUser(
        @Parameter(
            description = "User identifier."
        )
        @PathVariable
            Integer userIdentifier,
        @Parameter(hidden = true)
            HttpSession httpSession

    ) throws Exception {
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile myProfile = session.getProfile();
        String myUserId = session.getUserId();

        if (myProfile.equals(Profile.Administrator) || myProfile.equals(Profile.UserAdmin) ||
            myUserId.equals(Integer.toString(userIdentifier))) {
            Optional<User> user = userRepository.findById(userIdentifier);

            if (!user.isPresent()) {
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

            return user.get();
        } else {
            throw new IllegalArgumentException("You don't have rights to do this");
        }

    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get user identicon",
        description = "")
    @RequestMapping(
        value = "/{userIdentifier}.png",
        produces = MediaType.IMAGE_PNG_VALUE,
        method = RequestMethod.GET)
    @ResponseBody
    public void getUserIdenticon(
        @Parameter(
            description = "User identifier."
        )
        @PathVariable
            Integer userIdentifier,
        @Parameter(
            description = "Size."
        )
        @RequestParam(defaultValue = "18")
            Integer size,
        @Parameter(hidden = true)
            HttpServletResponse response
    ) throws IOException {
        String identiconType = settingManager.getValue(SYSTEM_USERS_IDENTICON);
        if (identiconType != null && identiconType.startsWith("gravatar")) {
            try {
                Optional<User> user = userRepository.findById(userIdentifier);

                if (!user.isPresent()) {
                    throw new UserNotFoundEx(Integer.toString(userIdentifier));
                }

                String[] config = identiconType.split(":");
                String dParameter = config.length > 1 ? config[1] : null;
                String fParameter = config.length == 3 ? config[2] : null;

                String email = user.get().getEmail() != null ? user.get().getEmail() : "";
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] hash = md.digest(email.getBytes());
                URL url = new URL("https://gravatar.com/avatar/" +
                    DatatypeConverter.printHexBinary(hash).toLowerCase() +
                    "?s=" + size +
                    (dParameter ==  null ? "" : "&d=" + dParameter) +
                    (fParameter ==  null ? "" : "&f=" + fParameter)
                );
                BufferedImage image = ImageIO.read(url);
                response.setStatus(HttpStatus.OK.value());
                ImageIO.write(image, "PNG", response.getOutputStream());
            } catch (NoSuchAlgorithmException e) {
                ImageIO.write(pixel, "PNG", response.getOutputStream());
            }
        } else {
            ImageIO.write(pixel, "PNG", response.getOutputStream());
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Delete a user",
        description = "Deletes a catalog user by identifier.")
    @RequestMapping(value = "/{userIdentifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public ResponseEntity<String> deleteUser(
        @Parameter(
            description = "User identifier."
        )
        @PathVariable
            Integer userIdentifier,
        @Parameter(hidden = true)
            ServletRequest request,
        @Parameter(hidden = true)
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
            final long numUserRecords = metadataRepository.count(MetadataSpecs.isOwnedByUser(userIdentifier));
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
            userRepository.deleteById(userIdentifier);
        } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
            throw new UserNotFoundEx(Integer.toString(userIdentifier));
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Check if a user property already exist",
        description = ""
        //       authorizations = {
        //           @Authorization(value = "basicAuth")
        //      })
    )
    @RequestMapping(
        value = "/properties/{property}",
        method = RequestMethod.GET
    )
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Property does not exist."),
        @ApiResponse(responseCode = "404", description = "A property with that value already exist."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    public ResponseEntity<HttpStatus> checkUserPropertyExist(
        @Parameter(
            description = "The user property to check"
        )
        @PathVariable
            String property,
        @Parameter(
            description = "The value to search"
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


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Creates a user",
        description = "Creates a catalog user.")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public ResponseEntity<String> createUser(
        @Parameter(
            name = "user"
        )
        @RequestBody
            UserDto userDto,
        @Parameter(hidden = true)
            ServletRequest request,
        @Parameter(hidden = true)
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

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Update a user",
        description = "Updates a catalog user.")
    @RequestMapping(value = "/{userIdentifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<String> updateUser(
        @Parameter(
            description = "User identifier."
        )
        @PathVariable
            Integer userIdentifier,
        @Parameter(
            name = "user"
        )
        @RequestBody
            UserDto userDto,
        @Parameter(hidden = true)
            ServletRequest request,
        @Parameter(hidden = true)
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

        User user = userRepository.findById(userIdentifier).get();
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
            List<Integer> myUserAdminGroups = userGroupRepository.findGroupIds(Specification.where(
                hasProfile(myProfile)).and(hasUserId(Integer.parseInt(myUserId))));

            List<UserGroup> usergroups =
                userGroupRepository.findAll(Specification.where(
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

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Resets user password",
        description = "Resets the user password.")
    @RequestMapping(value = "/{userIdentifier}/actions/forget-password",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<String> resetUserPassword(
        @Parameter(
            description = "User identifier."
        )
        @PathVariable
            Integer userIdentifier,
        @Parameter(
            description = "Password to change."
        )
        @RequestParam(value = Params.PASSWORD) String password,
        @Parameter(
            description = "Password to change (repeat)."
        )
        @RequestParam(value = Params.PASSWORD + "2") String password2,
        @Parameter(hidden = true)
            ServletRequest request,
        @Parameter(hidden = true)
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

        Optional<User> user = userRepository.findById(userIdentifier);
        if (!user.isPresent()) {
            throw new UserNotFoundEx(Integer.toString(userIdentifier));
        }

        String passwordHash = PasswordUtil.encoder(ApplicationContextHolder.get()).encode(
            password);
        user.get().getSecurity().setPassword(passwordHash);
        user.get().getSecurity().getSecurityNotifications().remove(UserSecurityNotification.UPDATE_HASH_REQUIRED);
        userRepository.save(user.get());

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Retrieve user groups",
        description = "Retrieve the user groups.")
    @RequestMapping(value = "/{userIdentifier}/groups",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET
    )
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public List<UserGroup> retrieveUserGroups(
        @Parameter(
            description = "User identifier."
        )
        @PathVariable
            Integer userIdentifier,
        @Parameter(hidden = true)
            ServletRequest request,
        @Parameter(hidden = true)
            HttpSession httpSession
    ) throws Exception {
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile myProfile = session.getProfile();
        String myUserId = session.getUserId();

        if (myProfile == Profile.Administrator || myProfile == Profile.UserAdmin || myUserId.equals(Integer.toString(userIdentifier))) {
            // -- get the profile of the user id supplied
            User user = userRepository.findById(userIdentifier).get();
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
        return userGroupRepository.findGroupIds(hasUserId(userId));
    }

    private void setUserGroups(final User user, List<GroupElem> userGroups)
        throws Exception {

        Collection<UserGroup> all = userGroupRepository.findAll(UserGroupSpecs
            .hasUserId(user.getId()));

        // Have a quick reference of existing groups and profiles for this user
        Set<String> listOfAddedProfiles = new HashSet<String>();
        for (UserGroup ug : all) {
            String key = ug.getProfile().name() + ug.getGroup().getId();
            listOfAddedProfiles.add(key);
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
            Group group = groupRepository.findById(groupId).get();
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
        userGroupRepository.deleteAll(toRemove);

        // Add only new usergroups (if any)
        userGroupRepository.saveAll(toAdd);

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
                Specification.where(UserSpecs.hasProfile(Profile.Administrator)).and(UserSpecs.hasEnabled(true)));
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

    private final String profile;
    private final Integer id;

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
