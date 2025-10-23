/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.collections4.CollectionUtils;
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
import org.fao.geonet.api.tools.i18n.TranslationPackBuilder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.domain.page.Page;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.page.PageRepository;
import org.fao.geonet.repository.specification.GroupSpecs;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

@RequestMapping(value = {
    "/{portal}/api/groups"
})
@Tag(name = "groups",
    description = "Groups operations")
@Controller("groups")
public class GroupsApi {
    /**
     * Logger name.
     */
    public static final String LOGGER = Geonet.GEONETWORK + ".api.groups";
    public static final String API_PARAM_GROUP_DETAILS = "Group details";
    public static final String API_PARAM_GROUP_IDENTIFIER = "Group identifier";
    public static final String MSG_GROUP_WITH_IDENTIFIER_NOT_FOUND = "Group with identifier '%d' not found";
    /**
     * Group name pattern with allowed chars. Group name may only contain alphanumeric characters or single hyphens.
     * Cannot begin or end with a hyphen.
     */
    private static final String GROUPNAME_PATTERN = "^[a-zA-Z0-9]+([-_]?[a-zA-Z0-9]+)*$";
    /**
     * API logo note.
     */
    private static final String API_GET_LOGO_NOTE = "If last-modified header "
        + "is present it is used to check if the logo has been modified since "
        + "the header date. If it hasn't been modified returns an empty 304 Not"
        + " Modified response. If modified returns the image. If the group has "
        + "no logo then returns a transparent 1x1 px PNG image.";
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
    @Autowired
    private OperationAllowedRepository operationAllowedRepo;
    @Autowired
    private UserGroupRepository userGroupRepository;
    @Autowired
    private DataManager dm;

    @Autowired
    private TranslationPackBuilder translationPackBuilder;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private PageRepository pageRepository;

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
    @io.swagger.v3.oas.annotations.Operation(summary = "Get the group logo image.",
        description = API_GET_LOGO_NOTE
    )
    @RequestMapping(value = "/{groupId}/logo", method = RequestMethod.GET)
    public void getGroupLogo(
        @Parameter(description = "Group identifier", required = true) @PathVariable(value = "groupId") final Integer groupId,
        @Parameter(hidden = true) final WebRequest webRequest,
        HttpServletRequest request,
        HttpServletResponse response) throws ResourceNotFoundException {

        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());

        ApplicationContext context = ApplicationContextHolder.get();
        ServiceContext serviceContext = ApiUtils.createServiceContext(request, locale.getISO3Country());
        if (context == null) {
            throw new RuntimeException("ServiceContext not available");
        }

        Optional<Group> group = groupRepository.findById(groupId);
        if (!group.isPresent()) {
            throw new ResourceNotFoundException(messages.getMessage("api.groups.group_not_found", new
                Object[]{groupId}, locale));
        }
        try {
            final Resources resources = context.getBean(Resources.class);
            final String logoUUID = group.get().getLogo();
            if (StringUtils.isNotBlank(logoUUID) && !logoUUID.startsWith("http://") && !logoUUID.startsWith("https//")) {
                try (Resources.ResourceHolder image = getImage(resources, serviceContext, group.get())) {
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

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get groups",
        description = "The catalog contains one or more groups. By default, there is 3 reserved groups " +
            "(Internet, Intranet, Guest) and a sample group.<br/>" +
            "This service returns all catalog groups when not authenticated or " +
            "when current is user is an administrator. The list can contains or not " +
            "reserved groups depending on the parameters.<br/>" +
            "When authenticated, return user groups " +
            "optionally filtered on a specific user profile.")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<Group> getGroups(
        @Parameter(
            description = "Including Internet, Intranet, Guest groups or not"
        )
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
            boolean withReservedGroup,
        @Parameter(
            description = "For a specific profile"
        )
        @RequestParam(
            required = false
        )
            String profile,
        @Parameter(hidden = true)
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

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Add a group",
        description = "Return the identifier of the group created."
        //       authorizations = {
        //           @Authorization(value = "basicAuth")
        //      })
    )
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT
    )
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Group created."),
        @ApiResponse(responseCode = "400", description = "Group with that id or name already exist."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public ResponseEntity<Integer> addGroup(
        @Parameter(
            description = API_PARAM_GROUP_DETAILS
        )
        @RequestBody
            Group group
    ) throws Exception {
        if (groupRepository.findById(group.getId()).isPresent()) {
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

        if (!group.getName().matches(GROUPNAME_PATTERN)) {
            throw new IllegalArgumentException("Group name may only contain alphanumeric characters "
                + "or single hyphens. Cannot begin or end with a hyphen."
            );
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

        translationPackBuilder.clearCache();

        return new ResponseEntity<>(group.getId(), HttpStatus.CREATED);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get group",
        description = "Return the requested group details.")
    @RequestMapping(
        value = "/{groupIdentifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Group information for the group id supplied."),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND)
    })
    @ResponseBody
    public Group getGroup(
        @Parameter(
            description = API_PARAM_GROUP_IDENTIFIER
        )
        @PathVariable
            Integer groupIdentifier
    ) throws Exception {
        final Optional<Group> group = groupRepository.findById(groupIdentifier);

        if (!group.isPresent()) {
            throw new ResourceNotFoundException(String.format(
                MSG_GROUP_WITH_IDENTIFIER_NOT_FOUND, groupIdentifier
            ));
        }
        return group.get();
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get group users",
        description = ""
        //       authorizations = {
        //           @Authorization(value = "basicAuth")
        //      })
    )
    @RequestMapping(
        value = "/{groupIdentifier}/users",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of users in that group."),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public List<User> getGroupUsers(
        @Parameter(
            description = API_PARAM_GROUP_IDENTIFIER
        )
        @PathVariable
            Integer groupIdentifier
    ) throws Exception {
        final Optional<Group> group = groupRepository.findById(groupIdentifier);

        if (!group.isPresent()) {
            throw new ResourceNotFoundException(String.format(
                MSG_GROUP_WITH_IDENTIFIER_NOT_FOUND, groupIdentifier
            ));
        }
        return userRepository.findAllUsersInUserGroups(
            UserGroupSpecs.hasGroupId(groupIdentifier));
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Update a group",
        description = ""
        //       authorizations = {
        //           @Authorization(value = "basicAuth")
        //      })
    )
    @RequestMapping(
        value = "/{groupIdentifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT
    )
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Group updated."),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public void updateGroup(
        @Parameter(
            description = API_PARAM_GROUP_IDENTIFIER
        )
        @PathVariable
            Integer groupIdentifier,
        @Parameter(
            description = API_PARAM_GROUP_DETAILS
        )
        @RequestBody
            Group group
    ) throws Exception {
        final Optional<Group> existing = groupRepository.findById(groupIdentifier);
        if (!existing.isPresent()) {
            throw new ResourceNotFoundException(String.format(
                MSG_GROUP_WITH_IDENTIFIER_NOT_FOUND, groupIdentifier
            ));
        } else {
            if (!group.getName().matches(GROUPNAME_PATTERN)) {
                throw new IllegalArgumentException("Group name may only contain alphanumeric characters "
                    + "or single hyphens. Cannot begin or end with a hyphen."
                );
            }

            // Rebuild translation pack cache if there are changes in the translations
            boolean clearTranslationPackCache =
                !existing.get().getLabelTranslations().equals(group.getLabelTranslations());

            try {
                groupRepository.saveAndFlush(group);
            } catch (Exception ex) {
                Log.error(API.LOG_MODULE_NAME, ExceptionUtils.getStackTrace(ex));
                throw new RuntimeException(ex.getMessage());
            }


            if (clearTranslationPackCache) {
                translationPackBuilder.clearCache();
            }
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Remove a group",
        description = "Remove a group by first removing sharing settings, link to users and " +
            "finally reindex all affected records."
        //       authorizations = {
        //           @Authorization(value = "basicAuth")
        //      })
    )
    @RequestMapping(value = "/{groupIdentifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Group removed."),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public void deleteGroup(
        @Parameter(
            description = "Group identifier."
        )
        @PathVariable
            Integer groupIdentifier,
        @Parameter(
            description = "Force removal even if records are assigned to that group."
        )
        @RequestParam(defaultValue = "false")
            boolean force,
        @Parameter(hidden = true)
            ServletRequest request
    ) throws Exception {
        Optional<Group> group = groupRepository.findById(groupIdentifier);

        if (group.isPresent()) {
            final long metadataCount = metadataRepository.count(where((Specification<Metadata>)
                MetadataSpecs.isOwnedByOneOfFollowingGroups(Arrays.asList(group.get().getId()))));
            if (metadataCount > 0) {
                throw new NotAllowedException(String.format(
                    "Group %s owns metadata. To remove the group you should transfer first the metadata to another group.",
                    group.get().getName()
                ));
            }

            List<Integer> reindex = operationAllowedRepo.findAllIds(OperationAllowedSpecs.hasGroupId(groupIdentifier),
                OperationAllowedId_.metadataId);

            if (reindex.size() > 0 && force) {
                operationAllowedRepo.deleteAllByGroupId(groupIdentifier);

                //--- reindex affected metadata
                dm.indexMetadata(Lists.transform(reindex, Functions.toStringFunction()));
            } else if (reindex.size() > 0 && !force) {
                throw new NotAllowedException(String.format(
                    "Group %s has privileges associated with %d record(s). Add 'force' parameter to remove it or remove privileges associated with that group first.",
                    group.get().getName(), reindex.size()
                ));
            }

            final List<Integer> users = userGroupRepository.findUserIds(where(UserGroupSpecs.hasGroupId(group.get().getId())));
            if (users.size() > 0 && force) {
                userGroupRepository.deleteAllByIdAttribute(UserGroupId_.groupId, Arrays.asList(groupIdentifier));
            } else if (users.size() > 0 && !force) {
                throw new NotAllowedException(String.format(
                    "Group %s is associated with %d user(s). Add 'force' parameter to remove it or remove users associated with that group first.",
                    group.get().getName(), users.size()
                ));
            }

            List<Page> staticPages = pageRepository.findPageByStatus(Page.PageStatus.GROUPS);
            List<Page> staticPagesAssignedToGroup =
                staticPages.stream().filter(p ->
                    !p.getGroups().stream().filter(g -> g.getId() == groupIdentifier).collect(Collectors.toList()).isEmpty())
                    .collect(Collectors.toList());

            if (!staticPagesAssignedToGroup.isEmpty()) {
                throw new NotAllowedException(String.format(
                    "Group %s is associated with '%s' static page(s). Please remove the static page(s) associated with that group first.",
                    group.get().getName(), staticPagesAssignedToGroup.stream().map(p -> p.getLabel()).collect(Collectors.joining())
                ));
            }

            groupRepository.deleteById(groupIdentifier);

            translationPackBuilder.clearCache();
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
                return groupRepository.findAll(sort);
            } else {
                return groupRepository.findAll(Specification.not(GroupSpecs.isReserved()), sort);
            }
        } else {
            Specification<UserGroup> spec = Specification.where(UserGroupSpecs.hasUserId(session.getUserIdAsInt()));
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
                .findAll(sort);
            groups.removeIf(g -> !ids.contains(g.getId()));
            return groups;
        }
    }
}
