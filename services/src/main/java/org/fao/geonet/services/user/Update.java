//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.user;

import com.vividsolutions.jts.util.Assert;
import jeeves.server.UserSession;
import jeeves.server.sources.http.JeevesServlet;
import jeeves.services.ReadWriteController;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import org.fao.geonet.domain.responses.OkResponse;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.repository.specification.UserSpecs;
import org.fao.geonet.util.PasswordUtil;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.List;

import static org.fao.geonet.repository.specification.UserGroupSpecs.hasProfile;
import static org.fao.geonet.repository.specification.UserGroupSpecs.hasUserId;

/**
 * Update the information of a user.
 */

@Controller("admin.user.update")
@ReadWriteController
@Deprecated
public class Update {


    @RequestMapping(value = "/{portal}/{lang}/admin.user.resetpassword", produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    @Deprecated
    OkResponse resetPassword(
        HttpSession session,
        @RequestParam(value = Params.ID) String id,
        @RequestParam(value = Params.PASSWORD) String password,
        @RequestParam(value = Params.PASSWORD + "2") String password2
    ) throws Exception {
        Assert.equals(password, password2);
        new LoadCurrentUserInfo(session, id).invoke();
        UserRepository userRepository = ApplicationContextHolder.get().getBean(UserRepository.class);

        User user = userRepository.findOne(id);
        setPassword(Params.Operation.RESETPW, password, user);
        userRepository.save(user);

        return new OkResponse();
    }

    @RequestMapping(value = "/{portal}/{lang}/admin.user.update", produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    OkResponse run(
        HttpSession session,
        HttpServletRequest request,
        @RequestParam(value = Params.OPERATION) String operation,
        @RequestParam(value = Params.ID, required = false) String id,
        @RequestParam(value = Params.USERNAME) String username,
        @RequestParam(value = Params.PASSWORD, required = false) String password,
        @RequestParam(value = Params.PROFILE, required = false) String profile_,
        @RequestParam(value = Params.SURNAME) String surname,
        @RequestParam(value = Params.NAME) String name,
        @RequestParam(value = Params.ADDRESS, required = false) String address,
        @RequestParam(value = Params.CITY, required = false) String city,
        @RequestParam(value = Params.STATE, required = false) String state,
        @RequestParam(value = Params.ZIP, required = false) String zip,
        @RequestParam(value = Params.COUNTRY, required = false) String country,
        @RequestParam(value = Params.EMAIL) String email,
        @RequestParam(value = Params.ORG, required = false) String organ,
        @RequestParam(value = Params.KIND, required = false) String kind,
        @RequestParam(value = Params.ENABLED) Boolean enabled)
        throws Exception {
        if (id == null && operation.equalsIgnoreCase(Params.Operation.NEWUSER)) {
            id = "";
        }

        List<GroupElem> groups = new LinkedList<>();
        Profile profile = Profile.findProfileIgnoreCase(profile_);

        LoadCurrentUserInfo loadCurrentUserInfo = new LoadCurrentUserInfo(session, id).invoke();
        Profile myProfile = loadCurrentUserInfo.getMyProfile();
        String myUserId = loadCurrentUserInfo.getMyUserId();

        Map<String, String[]> params = request.getParameterMap();

        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("groups_")) {
                for (String s : entry.getValue()) {
                    groups.add(new GroupElem(key.substring(7), Integer.valueOf(s)));
                }
            }
        }
        UserRepository userRepository = ApplicationContextHolder.get().getBean(UserRepository.class);

        if (profile == Profile.Administrator) {
            // Check at least 1 administrator is enabled
            if (StringUtils.isNotEmpty(id) && (enabled != null) && (enabled.equals(Boolean.FALSE))) {
                List<User> adminEnabledList = userRepository.findAll(
                    Specifications.where(UserSpecs.hasProfile(Profile.Administrator)).and(UserSpecs.hasEnabled(true)));
                if (adminEnabledList.size() == 1) {
                    User adminUser = adminEnabledList.get(0);
                    if (adminUser.getId() == Integer.parseInt(id)) {
                        throw new IllegalArgumentException(
                            "Trying to disable all administrator users is not allowed");
                    }
                }
            }
        }


        UserGroupRepository userGroupRepository = ApplicationContextHolder.get().getBean(UserGroupRepository.class);

        checkAccessRights(operation, id, username, myProfile, myUserId, groups, userGroupRepository);

        User user = getUser(userRepository, operation, id, username);


        //If it is a useradmin updating,
        //maybe we don't know all the groups the user is part of
        if (!myProfile.equals(Profile.Administrator)) {
            List<Integer> myUserAdminGroups = userGroupRepository.findGroupIds(Specifications.where(
                hasProfile(myProfile)).and(hasUserId(Integer.valueOf(myUserId))));

            List<UserGroup> usergroups =
                userGroupRepository.findAll(Specifications.where(
                    hasUserId(Integer.parseInt(id))));

            //keep unknown groups as is
            for (UserGroup ug : usergroups) {
                if (!myUserAdminGroups.contains(ug.getGroup().getId())) {
                    groups.add(new GroupElem(ug.getProfile().name(),
                        ug.getGroup().getId()));
                }
            }
        }

        setPassword(operation, password, user);
        if (operation.equalsIgnoreCase(Params.Operation.RESETPW)) {
            userRepository.save(user);
        } else {
            updateOrSave(operation, username, surname, name, address, city, state, zip, country, email, organ, kind,
                enabled, profile, myProfile, groups, user);
        }

        return new OkResponse();
    }

    public void updateOrSave(String operation, String username, String surname, String name, String address,
                             String city, String state, String zip, String country, String email,
                             String organ, String kind, Boolean enabled, Profile profile,
                             Profile myProfile, List<GroupElem> groups, User user) throws Exception {
        if (username != null) {
            user.setUsername(username);
        }

        if (name != null) {
            user.setName(name);
        }
        if (surname != null) {
            user.setSurname(surname);
        }

        if (profile != null) {
            if (!myProfile.getAll().contains(profile)) {
                throw new IllegalArgumentException(
                    "Trying to set profile to " + profile
                        + " max profile permitted is: " + myProfile);
            }
            user.setProfile(profile);
        }
        if (kind != null) {
            user.setKind(kind);
        }
        if (organ != null) {
            user.setOrganisation(organ);
        }

        if (enabled != null) {
            user.setEnabled(enabled);
        }

        Address addressEntity;
        boolean hasNoAddress = user.getAddresses().isEmpty();
        if (hasNoAddress) {
            addressEntity = new Address();
        } else {
            addressEntity = user.getAddresses().iterator().next();

        }
        if (address != null) {
            addressEntity.setAddress(address);
        }
        if (city != null) {
            addressEntity.setCity(city);
        }
        if (state != null) {
            addressEntity.setState(state);
        }
        if (zip != null) {
            addressEntity.setZip(zip);
        }
        if (country != null) {
            addressEntity.setCountry(country);
        }

        if (hasNoAddress) {
            user.getAddresses().add(addressEntity);
        }

        if (email != null) {
            String[] emails = email.indexOf("|") >= 0 ? email.split("|") : new String[]{email};
            for (String mail : emails) {
                user.getEmailAddresses().clear();
                user.getEmailAddresses().add(mail);
            }
        }
        UserRepository userRepository = ApplicationContextHolder.get().getBean(UserRepository.class);

        // -- For adding new user
        if (operation.equals(Params.Operation.NEWUSER)
            || operation.equals(Params.Operation.FULLUPDATE)
            || operation.equals(Params.Operation.EDITINFO)) {
            user = userRepository.save(user);
            setUserGroups(user, groups);
        } else {
            throw new IllegalArgumentException(
                "unknown user update operation " + operation);
        }
    }

    public void setPassword(String operation, String password, User user) {
        if (password != null) {
            user.getSecurity().setPassword(
                PasswordUtil.encoder(ApplicationContextHolder.get()).encode(
                    password));
        } else if (operation.equals(Params.Operation.RESETPW)
            || operation.equals(Params.Operation.NEWUSER)) {
            throw new IllegalArgumentException(
                "password is a required parameter for operation: "
                    + Params.Operation.RESETPW);
        }
    }

    private User getUser(final UserRepository repo, final String operation,
                         final String id, final String username) {
        if (Params.Operation.NEWUSER.equalsIgnoreCase(operation)) {
            if (username == null) {
                throw new IllegalArgumentException(Params.USERNAME
                    + " is a required parameter for "
                    + Params.Operation.NEWUSER + " " + "operation");
            }

            List<User> existingUsers = repo.findByUsernameIgnoreCase(username);
            if (!existingUsers.isEmpty()) {
                throw new IllegalArgumentException("Users with username "
                    + username + " ignore case already exists");
            }

            User user = repo.findOneByUsername(username);

            return new User();
        } else {
            User user = repo.findOne(id);
            if (user == null) {
                throw new IllegalArgumentException("No user found with id: "
                    + id);
            }

            // Check no duplicated username and if we are adding a duplicate existing name with other case combination
            List<User> usersWithUsernameIgnoreCase = repo.findByUsernameIgnoreCase(username);
            if (usersWithUsernameIgnoreCase.size() != 0 &&
                (!usersWithUsernameIgnoreCase.stream().anyMatch(u -> u.getId() == Integer.parseInt(id))
                    || usersWithUsernameIgnoreCase.stream().anyMatch(u ->
                    u.getUsername().equals(username) && u.getId() != Integer.parseInt(id))
                )) {
                throw new IllegalArgumentException(String.format(
                    "Another user with username '%s' ignore case already exists", user.getUsername()));
            }


            return user;
        }
    }

    private void checkAccessRights(final String operation, final String id,
                                   final String username, final Profile myProfile,
                                   final String myUserId, final List<GroupElem> userGroups,
                                   final UserGroupRepository groupRepository) {
        // Before we do anything check (for UserAdmin) that they are not trying
        // to add a user to any group outside of their own - if they are then
        // raise an exception - this shouldn't happen unless someone has
        // constructed their own malicious URL!
        //
        if (operation.equals(Params.Operation.NEWUSER)
            || operation.equals(Params.Operation.EDITINFO)
            || operation.equals(Params.Operation.FULLUPDATE)) {
            if (!(myUserId.equals(id)) && myProfile == Profile.UserAdmin) {
                final List<Integer> groupIds = groupRepository
                    .findGroupIds(UserGroupSpecs.hasUserId(Integer
                        .parseInt(myUserId)));
                for (GroupElem userGroup : userGroups) {
                    boolean found = false;
                    for (int myGroup : groupIds) {
                        if (userGroup.getId() == myGroup) {
                            found = true;
                        }
                    }
                    if (!found) {
                        throw new IllegalArgumentException(
                            "Tried to add group id "
                                + userGroup.getId()
                                + " to user "
                                + username
                                + " - not allowed "
                                + "because you are not a member of that group!");
                    }
                }
            }
        }
    }

    private void setUserGroups(final User user, List<GroupElem> userGroups)
        throws Exception {
        UserGroupRepository userGroupRepository = ApplicationContextHolder.get().getBean(UserGroupRepository.class);
        GroupRepository groupRepository = ApplicationContextHolder.get().getBean(GroupRepository.class);

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

    private static class LoadCurrentUserInfo {
        private HttpSession session;
        private String id;
        private Profile myProfile;
        private String myUserId;

        public LoadCurrentUserInfo(HttpSession session, String id) {
            this.session = session;
            this.id = id;
        }

        public Profile getMyProfile() {
            return myProfile;
        }

        public String getMyUserId() {
            return myUserId;
        }

        public LoadCurrentUserInfo invoke() {
            myProfile = Profile.Guest;

            myUserId = null;
            Object tmp = session.getAttribute(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY);
            if (tmp instanceof UserSession) {
                UserSession usrSess = (UserSession) tmp;
                myProfile = usrSess.getProfile();
                myUserId = usrSess.getUserId();
            } else if (tmp == null) {
                Object securityContext = session.getAttribute("SPRING_SECURITY_CONTEXT");
                if (securityContext instanceof SecurityContext) {
                    Object principal = ((SecurityContext) securityContext).getAuthentication().getPrincipal();
                    if (principal instanceof User) {
                        User user = (User) principal;
                        myProfile = user.getProfile();
                        myUserId = user.getId() + "";
                    }
                }
            }

            if (myProfile != Profile.Administrator && myProfile != Profile.UserAdmin && !myUserId.equals(id)) {
                throw new IllegalArgumentException("You don't have rights to do this");
            }
            return this;
        }
    }
}

@Deprecated
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
