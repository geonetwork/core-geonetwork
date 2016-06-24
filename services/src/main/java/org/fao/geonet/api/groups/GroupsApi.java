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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.server.UserSession;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
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

    @Autowired
    LanguageUtils languageUtils;

    @ApiOperation(
        value = "Get groups",
        notes = "Return all catalog groups when not authenticated or " +
            "administrator with or without reserved groups. " +
            "When authenticated, return user groups " +
            "optionnaly filtered on a specific" +
            "user profile.",
        nickname = "getGroups")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<Group> getGroups(
        @ApiParam(
            value = "Including Internet, Intranet, Guest groups"
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
        notes = "",
        nickname = "addGroup")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT
    )
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('UserAdmin')")
    @ResponseBody
    public ResponseEntity<Integer> addGroup(
        @ApiParam(
            value = "Group details"
        )
        @RequestBody
            Group group
    ) throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();
        GroupRepository groupRepository = appContext.getBean(GroupRepository.class);
        final Group existing = groupRepository.findOne(group.getId());
        if (existing != null) {
            throw new IllegalArgumentException(String.format(
                "A group with id '%d' already exist.",
                group.getId()
            ));
        } else {
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
        }
        return new ResponseEntity<>(group.getId(), HttpStatus.CREATED);
    }


    @ApiOperation(
        value = "Get group",
        notes = "Return catalog group .",
        nickname = "getGroup")
    @RequestMapping(
        value = "/{groupIdentifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public Group getGroup(
        @ApiParam(
            value = "Group identifier"
        )
        @PathVariable
            Integer groupIdentifier
    ) throws Exception {
        GroupRepository groupRepository = ApplicationContextHolder.get().getBean(GroupRepository.class);

        final Group group = groupRepository.findOne(groupIdentifier);

        if (group == null) {
            throw new ResourceNotFoundException(String.format("Group not found"));
        }

        return group;

    }

    @ApiOperation(
        value = "Update a group",
        notes = "",
        nickname = "updateGroup")
    @RequestMapping(
        value = "/{groupIdentifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT
    )
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('UserAdmin')")
    @ResponseBody
    public ResponseEntity updateGroup(
        @ApiParam(
            value = "Group identifier"
        )
        @PathVariable
            Integer groupIdentifier,
        @ApiParam(
            value = "Group details"
        )
        @RequestBody
            Group group
    ) throws Exception {
        GroupRepository groupRepository = ApplicationContextHolder.get().getBean(GroupRepository.class);

        final Group existing = groupRepository.findOne(groupIdentifier);
        if (existing == null) {
            throw new ResourceNotFoundException(String.format(
                "No group found with id '%d'.",
                groupIdentifier
            ));
        } else {
            groupRepository.save(group);
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    @ApiOperation(
        value = "Delete a group",
        notes = "Deletes a catalog group by identifier.",
        nickname = "deleteGroup")
    @RequestMapping(value = "/{groupIdentifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public ResponseEntity<String> deleteGroup(
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
                "Group with id '%d' does not exist.",
                groupIdentifier
            ));
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
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
