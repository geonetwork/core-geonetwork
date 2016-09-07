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

package org.fao.geonet.api.groups;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import io.swagger.annotations.*;
import jeeves.server.UserSession;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.GroupSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.*;

@RequestMapping(value = {
    "/api/groups",
    "/api/" + API.VERSION_0_1 +
        "/groups"
})
@Api(value = "groups",
    tags = "groups",
    description = "Groups operations")
@Controller("groups")
public class GroupsApi {

    public static final String API_PARAM_GROUP_DETAILS = "Group details";
    public static final String API_PARAM_GROUP_IDENTIFIER = "Group identifier";
    public static final String MSG_GROUP_WITH_IDENTIFIER_NOT_FOUND = "Group with identifier '%d' not found";
    @Autowired
    LanguageUtils languageUtils;

    @ApiOperation(
        value = "Get groups",
        notes = "The catalog contains one or more groups. By default, there is 3 reserved groups " +
            "(Internet, Intranet, Guest) and a sample group.<br/>" +
            "This service returns all catalog groups when not authenticated or " +
            "when current is user is an administrator. The list can contains or not " +
            "reserved groups depending on the parameters.<br/>" +
            "When authenticated, return user groups " +
            "optionally filtered on a specific user profile.",
        nickname = "getGroups")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<Group> getGroups(
        @ApiParam(
            value = "Including Internet, Intranet, Guest groups or not"
        )
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
            boolean withReservedGroup,
        @ApiParam(
            value = "For a specific profile"
        )
        @RequestParam(
            required = false
        )
            String profile,
        @ApiIgnore
            HttpSession httpSession
    ) throws Exception {
        UserSession session = ApiUtils.getUserSession(httpSession);

        if (!session.isAuthenticated() || profile == null) {
            return getGroups(
                session,
                null,
                withReservedGroup,
                !withReservedGroup);
        } else {
            return getGroups(
                session,
                Profile.findProfileIgnoreCase(profile),
                false, false);
        }
    }

    @ApiOperation(
        value = "Add a group",
        notes = "Return the identifier of the group created.",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "addGroup")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT
    )
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Group created.") ,
        @ApiResponse(code = 400, message = "Group with that id or name already exist.") ,
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public ResponseEntity<Integer> addGroup(
        @ApiParam(
            value = API_PARAM_GROUP_DETAILS
        )
        @RequestBody
            Group group
    ) throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();
        GroupRepository groupRepository = appContext.getBean(GroupRepository.class);

        final Group existingId = groupRepository
            .findOne(group.getId());

        if (existingId != null) {
            throw new IllegalArgumentException(String.format(
                "A group with id '%d' already exist.",
                group.getId()
            ));
        }

        final Group existingName = groupRepository
            .findByName(group.getName());
        if (existingName != null) {
            throw new IllegalArgumentException(String.format(
                "A group with name '%s' already exist.",
                group.getName()
            ));
        }

        // Populate languages if not already set
        LanguageRepository langRepository = appContext.getBean(LanguageRepository.class);
        java.util.List<Language> allLanguages = langRepository.findAll();
        Map<String, String> labelTranslations = group.getLabelTranslations();
        for (Language l : allLanguages) {
            String label = labelTranslations.get(l.getId());
            group.getLabelTranslations().put(l.getId(),
                label == null ? group.getName() : label);
        }
        groupRepository.save(group);

        return new ResponseEntity<>(group.getId(), HttpStatus.CREATED);
    }


    @ApiOperation(
        value = "Get group",
        notes = "Return the requested group details.",
        nickname = "getGroup")
    @RequestMapping(
        value = "/{groupIdentifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND)
    })
    @ResponseBody
    public Group getGroup(
        @ApiParam(
            value = API_PARAM_GROUP_IDENTIFIER
        )
        @PathVariable
            Integer groupIdentifier
    ) throws Exception {
        final GroupRepository groupRepository = ApplicationContextHolder.get().getBean(GroupRepository.class);
        final Group group = groupRepository.findOne(groupIdentifier);

        if (group == null) {
            throw new ResourceNotFoundException(String.format(
                MSG_GROUP_WITH_IDENTIFIER_NOT_FOUND, groupIdentifier
            ));
        }
        return group;
    }


    @ApiOperation(
        value = "Get group users",
        notes = "",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "getGroupUsers")
    @RequestMapping(
        value = "/{groupIdentifier}/users",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of users in that group.") ,
        @ApiResponse(code = 404, message = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND) ,
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public List<User> getGroupUsers(
        @ApiParam(
            value = API_PARAM_GROUP_IDENTIFIER
        )
        @PathVariable
            Integer groupIdentifier
    ) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        GroupRepository groupRepository = applicationContext.getBean(GroupRepository.class);
        final Group group = groupRepository.findOne(groupIdentifier);

        if (group == null) {
            throw new ResourceNotFoundException(String.format(
                MSG_GROUP_WITH_IDENTIFIER_NOT_FOUND, groupIdentifier
            ));
        }
        UserRepository userRepository = applicationContext.getBean(UserRepository.class);
        return userRepository.findAllUsersInUserGroups(
            UserGroupSpecs.hasGroupId(groupIdentifier));
    }


    @ApiOperation(
        value = "Update a group",
        notes = "",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "updateGroup")
    @RequestMapping(
        value = "/{groupIdentifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT
    )
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Group updated.") ,
        @ApiResponse(code = 404, message = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND) ,
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public void updateGroup(
        @ApiParam(
            value = API_PARAM_GROUP_IDENTIFIER
        )
        @PathVariable
            Integer groupIdentifier,
        @ApiParam(
            value = API_PARAM_GROUP_DETAILS
        )
        @RequestBody
            Group group
    ) throws Exception {
        GroupRepository groupRepository = ApplicationContextHolder.get().getBean(GroupRepository.class);

        final Group existing = groupRepository.findOne(groupIdentifier);
        if (existing == null) {
            throw new ResourceNotFoundException(String.format(
                MSG_GROUP_WITH_IDENTIFIER_NOT_FOUND, groupIdentifier
            ));
        } else {
            groupRepository.save(group);
        }
    }


    @ApiOperation(
        value = "Remove a group",
        notes = "Remove a group by first removing sharing settings, link to users and " +
            "finally reindex all affected records.",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "deleteGroup")
    @RequestMapping(value = "/{groupIdentifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Group removed.") ,
        @ApiResponse(code = 404, message = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND) ,
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public void deleteGroup(
        @ApiParam(
            value = "Group identifier."
        )
        @PathVariable
            Integer groupIdentifier,
        @ApiIgnore
            ServletRequest request
    ) throws Exception {
        GroupRepository groupRepository = ApplicationContextHolder.get().getBean(GroupRepository.class);

        Group group = groupRepository.findOne(groupIdentifier);

        if (group != null) {
            OperationAllowedRepository operationAllowedRepo = ApplicationContextHolder.get().getBean(OperationAllowedRepository.class);
            UserGroupRepository userGroupRepo = ApplicationContextHolder.get().getBean(UserGroupRepository.class);

            List<Integer> reindex = operationAllowedRepo.findAllIds(OperationAllowedSpecs.hasGroupId(groupIdentifier),
                OperationAllowedId_.metadataId);

            operationAllowedRepo.deleteAllByIdAttribute(OperationAllowedId_.groupId, groupIdentifier);
            userGroupRepo.deleteAllByIdAttribute(UserGroupId_.groupId, Arrays.asList(groupIdentifier));
            groupRepository.delete(groupIdentifier);

            //--- reindex affected metadata
            DataManager dm = ApplicationContextHolder.get().getBean(DataManager.class);
            dm.indexMetadata(Lists.transform(reindex, Functions.toStringFunction()));

        } else {
            throw new ResourceNotFoundException(String.format(
                MSG_GROUP_WITH_IDENTIFIER_NOT_FOUND, groupIdentifier
            ));
        }
    }

    /**
     * Retrieves a user's groups.
     *
     * @param includingSystemGroups if true, also returns the system groups ('GUEST', 'intranet',
     *                              'all')
     * @param all                   if true returns all the groups, even those the user doesn't
     *                              belongs to
     */
    private List<Group> getGroups(
        UserSession session,
        Profile profile,
        boolean includingSystemGroups,
        boolean all)
        throws SQLException {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        final GroupRepository groupRepository = applicationContext.getBean(GroupRepository.class);
        final UserGroupRepository userGroupRepository = applicationContext.getBean(UserGroupRepository.class);
        final Sort sort = SortUtils.createSort(Group_.id);

        if (all || !session.isAuthenticated() || Profile.Administrator == session.getProfile()) {
            if (includingSystemGroups) {
                return groupRepository.findAll(null, sort);
            } else {
                return groupRepository.findAll(Specifications.not(GroupSpecs.isReserved()), sort);
            }
        } else {
            Specifications<UserGroup> spec = Specifications.where(UserGroupSpecs.hasUserId(session.getUserIdAsInt()));
            // you're no Administrator
            // retrieve your groups
            if (profile != null) {
                spec = spec.and(UserGroupSpecs.hasProfile(profile));
            }
            Set<Integer> ids = new HashSet<Integer>(userGroupRepository.findGroupIds(spec));

            // include system groups if requested (used in harvesters)
            if (includingSystemGroups) {
                // these DB keys of system groups are hardcoded !
                for (ReservedGroup reservedGroup : ReservedGroup.values()) {
                    ids.add(reservedGroup.getId());
                }
            }

            // retrieve all groups and filter to only user one
            List<Group> groups = groupRepository
                .findAll(null, sort);
            groups.removeIf(g -> !ids.contains(g.getId()));
            return groups;
        }
    }
}
