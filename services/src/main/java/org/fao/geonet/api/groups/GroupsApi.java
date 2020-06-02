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
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.records.attachments.AttachmentsApi;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Group_;
import org.fao.geonet.domain.Language;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.UserGroupId_;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.LanguageRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.GroupSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.springframework.data.jpa.domain.Specifications.where;

@RequestMapping(value = {
    "/{portal}/api/groups",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/groups"
})
@Api(value = "groups",
    tags = "groups",
    description = "Groups operations")
@Controller("groups")
public class GroupsApi {
    /**
     * API logo note.
     */
    private static final String API_GET_LOGO_NOTE = "If last-modified header "
        + "is present it is used to check if the logo has been modified since "
        + "the header date. If it hasn't been modified returns an empty 304 Not"
        + " Modified response. If modified returns the image. If the group has "
        + "no logo then returns a transparent 1x1 px PNG image.";

    /**
     * Logger name.
     */
    public static final String LOGGER = Geonet.GEONETWORK + ".api.groups";
    /**
     * Six hours in seconds.
     */
    private static final int SIX_HOURS = 60 * 60 * 6;
    /**
     * Transparent 1x1 px PNG encoded in Base64.
     */
    private static final String TRANSPARENT_1_X_1_PNG_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR"
        + "42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
    /**
     * Transparent 1x1 px PNG.
     */
    private static final byte[] TRANSPARENT_1_X_1_PNG = org.apache.commons.codec.binary.Base64.decodeBase64(TRANSPARENT_1_X_1_PNG_BASE64);


    public static final String API_PARAM_GROUP_DETAILS = "Group details";
    public static final String API_PARAM_GROUP_IDENTIFIER = "Group identifier";
    public static final String MSG_GROUP_WITH_IDENTIFIER_NOT_FOUND = "Group with identifier '%d' not found";

    /**
     * Message source.
     */
    @Autowired
    @Qualifier("apiMessages")
    private ResourceBundleMessageSource messages;

    /**
     * Language utils used to detect the requested language.
     */
    @Autowired
    private LanguageUtils languageUtils;

    @Autowired
    private LanguageRepository langRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Writes the group logo image to the response. If no image is found it
     * writes a 1x1 transparent PNG. If the request contain cache related
     * headers it checks if the resource has changed and return a 304 Not
     * Modified response if not changed.
     *
     * @param groupId    the group identifier.
     * @param webRequest the web request.
     * @param request    the native HTTP Request.
     * @param response   the servlet response.
     * @throws ResourceNotFoundException if no group exists with groupId.
     */
    @ApiOperation(value = "Get the group logo image.",
        nickname = "get",
        notes = API_GET_LOGO_NOTE
    )
    @RequestMapping(value = "/{groupId}/logo", method = RequestMethod.GET)
    public void getGroupLogo(
        @ApiParam(value = "Group identifier", required = true) @PathVariable(value = "groupId") final Integer groupId,
        @ApiIgnore final WebRequest webRequest,
        HttpServletRequest request,
        HttpServletResponse response) throws ResourceNotFoundException {

        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());

        ApplicationContext context = ApplicationContextHolder.get();
        ServiceContext serviceContext = ApiUtils.createServiceContext(request, locale.getISO3Country());
        if (context == null) {
            throw new RuntimeException("ServiceContext not available");
        }

        Group group = groupRepository.findOne(groupId);
        if (group == null) {
            throw new ResourceNotFoundException(messages.getMessage("api.groups.group_not_found", new
                Object[]{groupId}, locale));
        }
        try {
            final Resources resources = context.getBean(Resources.class);
            final String logoUUID = group.getLogo();
            if (StringUtils.isNotBlank(logoUUID) && !logoUUID.startsWith("http://") && !logoUUID.startsWith("https//")) {
                try (Resources.ResourceHolder image = getImage(resources, serviceContext, group)){
                    if (image != null) {
                        FileTime lastModifiedTime = image.getLastModifiedTime();
                        response.setDateHeader("Expires", System.currentTimeMillis() + SIX_HOURS * 1000L);
                        if (webRequest.checkNotModified(lastModifiedTime.toMillis())) {
                            // webRequest.checkNotModified sets the right HTTP headers
                            return;
                        }
                        response.setContentType(AttachmentsApi.getFileContentType(image.getPath()));
                        response.setContentLength((int) Files.size(image.getPath()));
                        response.addHeader("Cache-Control", "max-age=" + SIX_HOURS + ", public");
                        FileUtils.copyFile(image.getPath().toFile(), response.getOutputStream());
                        return;
                    }
                }
            }

            // no logo image found. Return a transparent 1x1 png
            FileTime lastModifiedTime = FileTime.fromMillis(0);
            if (webRequest.checkNotModified(lastModifiedTime.toMillis())) {
                return;
            }
            response.setContentType("image/png");
            response.setContentLength(TRANSPARENT_1_X_1_PNG.length);
            response.addHeader("Cache-Control", "max-age=" + SIX_HOURS + ", public");
            response.getOutputStream().write(TRANSPARENT_1_X_1_PNG);

        } catch (IOException e) {
            Log.error(LOGGER, String.format("There was an error accessing the logo of the group with id '%d'",
                groupId));
            throw new RuntimeException(e);
        }
    }

    private static Resources.ResourceHolder getImage(Resources resources, ServiceContext serviceContext, Group group) throws IOException {
        final Path logosDir = resources.locateLogosDir(serviceContext);
        final Path harvesterLogosDir = resources.locateHarvesterLogosDir(serviceContext);
        final String logoUUID = group.getLogo();
        Resources.ResourceHolder image = null;
        if (StringUtils.isNotBlank(logoUUID) && !logoUUID.startsWith("http://") && !logoUUID.startsWith("https//")) {
            image = resources.getImage(serviceContext, logoUUID, logosDir);
            if (image == null) {
                image = resources.getImage(serviceContext, logoUUID, harvesterLogosDir);
            }
        }
        return image;
    }

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
        @ApiResponse(code = 201, message = "Group created."),
        @ApiResponse(code = 400, message = "Group with that id or name already exist."),
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
        java.util.List<Language> allLanguages = langRepository.findAll();
        Map<String, String> labelTranslations = group.getLabelTranslations();
        for (Language l : allLanguages) {
            String label = labelTranslations.get(l.getId());
            group.getLabelTranslations().put(l.getId(),
                label == null ? group.getName() : label);
        }

        try {
            group = groupRepository.saveAndFlush(group);
        } catch (Exception ex) {
            Log.error(API.LOG_MODULE_NAME, ExceptionUtils.getStackTrace(ex));
            throw new RuntimeException(ex.getMessage());
        }

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
        @ApiResponse(code = 200, message = "List of users in that group."),
        @ApiResponse(code = 404, message = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND),
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
        final Group group = groupRepository.findOne(groupIdentifier);

        if (group == null) {
            throw new ResourceNotFoundException(String.format(
                MSG_GROUP_WITH_IDENTIFIER_NOT_FOUND, groupIdentifier
            ));
        }
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
        @ApiResponse(code = 204, message = "Group updated."),
        @ApiResponse(code = 404, message = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND),
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
        final Group existing = groupRepository.findOne(groupIdentifier);
        if (existing == null) {
            throw new ResourceNotFoundException(String.format(
                MSG_GROUP_WITH_IDENTIFIER_NOT_FOUND, groupIdentifier
            ));
        } else {
            try {
                groupRepository.saveAndFlush(group);
            } catch (Exception ex) {
                Log.error(API.LOG_MODULE_NAME, ExceptionUtils.getStackTrace(ex));
                throw new RuntimeException(ex.getMessage());
            }
        }
    }

    @Autowired
    private OperationAllowedRepository operationAllowedRepo;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private DataManager dm;

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
    @PreAuthorize("hasRole('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Group removed."),
        @ApiResponse(code = 404, message = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public void deleteGroup(
        @ApiParam(
            value = "Group identifier."
        )
        @PathVariable
            Integer groupIdentifier,
        @ApiParam(
            value = "Force removal even if records are assigned to that group."
        )
        @RequestParam(defaultValue = "false")
            boolean force,
        @ApiIgnore
            ServletRequest request
    ) throws Exception {
        Group group = groupRepository.findOne(groupIdentifier);

        if (group != null) {
            List<Integer> reindex = operationAllowedRepo.findAllIds(OperationAllowedSpecs.hasGroupId(groupIdentifier),
                OperationAllowedId_.metadataId);

            if (reindex.size() > 0 && force) {
                operationAllowedRepo.deleteAllByGroupId(groupIdentifier);

                //--- reindex affected metadata
                dm.indexMetadata(Lists.transform(reindex, Functions.toStringFunction()));
            } else if (reindex.size() > 0 && !force) {
                throw new NotAllowedException(String.format(
                    "Group %s has privileges associated with %d record(s). Add 'force' parameter to remove it or remove privileges associated with that group first.",
                    group.getName(), reindex.size()
                ));
            }

            final List<Integer> users = userGroupRepository.findUserIds(where(UserGroupSpecs.hasGroupId(group.getId())));
            if (users.size() > 0 && force) {
                userGroupRepository.deleteAllByIdAttribute(UserGroupId_.groupId, Arrays.asList(groupIdentifier));
            } else if (users.size() > 0 && !force) {
                throw new NotAllowedException(String.format(
                    "Group %s is associated with %d user(s). Add 'force' parameter to remove it or remove users associated with that group first.",
                    group.getName(), users.size()
                ));
            }

            groupRepository.delete(groupIdentifier);

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
