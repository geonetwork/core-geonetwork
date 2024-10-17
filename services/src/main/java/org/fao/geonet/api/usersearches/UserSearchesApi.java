//=============================================================================
//===   Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.api.usersearches;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.usersearches.model.PaginatedUserSearchResponse;
import org.fao.geonet.api.usersearches.model.UserSearchDto;
import org.fao.geonet.domain.*;
import org.fao.geonet.domain.converter.UserSearchFeaturedTypeConverter;
import org.fao.geonet.exceptions.ResourceNotFoundEx;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserSearchRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.repository.specification.UserSearchSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpSession;
import java.util.*;

@EnableWebMvc
@Service
@RequestMapping(value = {
    "/{portal}/api/usersearches"
})
@Tag(name = "usersearches",
    description = "User custom searches operations")
public class UserSearchesApi {

    public static final String API_PARAM_USERSEARCH_DETAILS = "User search details";
    public static final String API_PARAM_USERSEARCH_IDENTIFIER = "User search identifier";
    public static final String MSG_USERSEARCH_WITH_IDENTIFIER_NOT_FOUND = "User search with identifier '%d' not found";


    @Autowired
    UserSearchRepository userSearchRepository;

    @Autowired
    UserGroupRepository userGroupRepository;

    @Autowired
    GroupRepository groupRepository;


    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UserSearchFeaturedType.class, new UserSearchFeaturedTypeConverter());
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get user custom searches",
        description = "")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public List<UserSearchDto> getUserCustomSearches(
        @Parameter(hidden = true)
            HttpSession httpSession
    ) {
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile myProfile = session.getProfile();

        List<UserSearch> userSearchesList;

        // Get user groups
        if (myProfile.equals(Profile.Administrator)) {
            userSearchesList = userSearchRepository.findAll();
        } else {
            List<UserGroup> userGroups = userGroupRepository.findAll(UserGroupSpecs.hasUserId(session.getPrincipal().getId()));

            Set<Group> groups = new HashSet<>();
            userGroups.forEach(us -> groups.add(us.getGroup()));
            userSearchesList = userSearchRepository.findAllByGroupsInOrCreator(groups, session.getPrincipal());
        }

        List<UserSearchDto> customSearchDtoList = new ArrayList<>();
        userSearchesList.forEach(u -> customSearchDtoList.add(UserSearchDto.from(u)));

        return customSearchDtoList;
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get user custom searches for all users (no paginated)",
        description = "")
    @RequestMapping(
        value = "/all",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseBody
    public List<UserSearchDto> getAllUserCustomSearches(
        @Parameter(
            description = "Featured type search."
        )
        @RequestParam(required = false) UserSearchFeaturedType featuredType) {

        List<UserSearch> userSearchesList;

        if (featuredType == null) {
            userSearchesList = userSearchRepository.findAll();
        } else {
            userSearchesList = userSearchRepository.findAllByFeaturedType(featuredType);
        }

        List<UserSearchDto> customSearchDtoList = new ArrayList<>();
        userSearchesList.forEach(u -> customSearchDtoList.add(UserSearchDto.from(u)));

        return customSearchDtoList;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get user custom searches for all users (paginated)",
        description = "")
    @RequestMapping(
        value = "/allpaginated",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseBody
    public PaginatedUserSearchResponse getAllUserCustomSearchesPage(
        @Parameter(
            description = "Featured  type search."
        )
        @RequestParam(required = false) UserSearchFeaturedType featuredType,
        @RequestParam(required = false, defaultValue = "")
            String search,
        @Parameter(description = "From page",
            required = false)
        @RequestParam(required = false, defaultValue = "0")
            Integer offset,
        @Parameter(description = "Number of records to return",
            required = false)
        @RequestParam(required = false, defaultValue = "10")
            Integer limit
    ) {
        PaginatedUserSearchResponse response = new PaginatedUserSearchResponse();

        List<UserSearch> userSearchesList;

        Sort sortBy = SortUtils.createSort(Sort.Direction.DESC,
            UserSearch_.creationDate);

        int page = (offset / limit);
        final PageRequest pageRequest = PageRequest.of(page, limit, sortBy);

        Specification<UserSearch> searchSpec = null;
        if (StringUtils.isNotEmpty(search)) {
            searchSpec =
                UserSearchSpecs.containsTextInCreatorOrTranslations(search);
        }

        long count = 0;

        if (featuredType == null) {
            userSearchesList = userSearchRepository.findAll(searchSpec, pageRequest).getContent();
            count = userSearchRepository.count();
        } else {
            userSearchesList =
                userSearchRepository.findAllByFeaturedType(featuredType, searchSpec, pageRequest);
            count = userSearchRepository.countByFeaturedType(featuredType);
        }

        List<UserSearchDto> customSearchDtoList = new ArrayList<>();
        userSearchesList.forEach(u -> customSearchDtoList.add(UserSearchDto.from(u)));

        response.setRows(customSearchDtoList);
        response.setTotal(count);

        return response;
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get featured user custom searches",
        description = "")
    @RequestMapping(
        value = "/featured",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<UserSearchDto> getFeaturedUserCustomSearches(
        @Parameter(description = "Number of records to return",
            required = false)
        @RequestParam(required = false)
            UserSearchFeaturedType type
    ) {
        if (type == null) {
            // Default value
            type = UserSearchFeaturedType.HOME;
        }

        List<UserSearch> userSearchesList = userSearchRepository.findAllByFeaturedType(type);

        List<UserSearchDto> customSearchDtoList = new ArrayList<>();
        userSearchesList.forEach(u -> customSearchDtoList.add(UserSearchDto.from(u)));

        return customSearchDtoList;
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get custom search",
        description = "")
    @RequestMapping(
        value = "/{searchIdentifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public UserSearchDto getUserCustomSearch(
        @Parameter(
            description = API_PARAM_USERSEARCH_IDENTIFIER
        )
        @PathVariable
            Integer searchIdentifier,
        @Parameter(hidden = true)
            HttpSession httpSession
    ) {
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile myProfile = session.getProfile();

        UserSearch userSearch = retrieveUserSearch(userSearchRepository, searchIdentifier);

        if (myProfile.equals(Profile.Administrator)) {
            return UserSearchDto.from(userSearch);
        } else {
            if (userSearch.getCreator().getId() == session.getUserIdAsInt()) {
                return UserSearchDto.from(userSearch);
            } else {
                throw new IllegalArgumentException("You don't have rights to access the user search");
            }
        }

    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Creates a user search",
        description = "Creates a user search.")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User search created.")
    })
    @ResponseBody
    public ResponseEntity<Integer> createUserCustomSearch(
        @Parameter(
            description = API_PARAM_USERSEARCH_DETAILS
        )
        @RequestBody
            UserSearchDto userSearchDto,
        @Parameter(hidden = true)
            HttpSession httpSession
    ) {
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile myProfile = session.getProfile();

        UserSearch userSearch = userSearchDto.asUserSearch();
        userSearch.setCreator(session.getPrincipal());

        // Validate groups associated with the user search
        if (!myProfile.equals(Profile.Administrator)) {
            List<UserGroup> userGroups =
                userGroupRepository.findAll(UserGroupSpecs.hasUserId(session.getPrincipal().getId()));

            Set<Group> groups = new HashSet<>();
            userGroups.forEach(us -> groups.add(us.getGroup()));

            if (!groups.containsAll(userSearch.getGroups())) {
                throw new IllegalArgumentException("Not all the groups associated with the user search are groups of the user " + session.getUsername());
            }
        }

        // Featured user searches can be created only by Administrator
        if (!myProfile.equals(Profile.Administrator)) {
            userSearch.setFeaturedType(null);
        }

        userSearch = userSearchRepository.save(userSearch);

        return new ResponseEntity<>(userSearch.getId(), HttpStatus.CREATED);
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Update a user search",
        description = ""
        //       authorizations = {
        //           @Authorization(value = "basicAuth")
        //      })
    )
    @RequestMapping(
        value = "/{searchIdentifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT
    )
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User search  updated.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND)
    })
    @ResponseBody
    public void updateCustomUserSearch(
        @Parameter(
            description = API_PARAM_USERSEARCH_IDENTIFIER
        )
        @PathVariable
            Integer searchIdentifier,
        @Parameter(
            description = API_PARAM_USERSEARCH_DETAILS
        )
        @RequestBody
            UserSearchDto userSearchDto,
        @Parameter(hidden = true)
            HttpSession httpSession
    ) throws Exception {
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile myProfile = session.getProfile();

        final Optional<UserSearch> existing = userSearchRepository.findById(searchIdentifier);
        if (!existing.isPresent()) {
            throw new ResourceNotFoundException(String.format(
                MSG_USERSEARCH_WITH_IDENTIFIER_NOT_FOUND, searchIdentifier
            ));
        } else {
            UserSearch userSearch = userSearchDto.asUserSearch();

            if (myProfile.equals(Profile.Administrator)) {
                userSearchRepository.save(userSearch);
            } else {
                if (userSearch.getCreator().getId() == session.getUserIdAsInt()) {
                    // Featured user searches can be created only by Administrator
                    userSearch.setFeaturedType(null);

                    userSearchRepository.save(userSearch);
                } else {
                    throw new IllegalArgumentException("You don't have rights to update the user search");
                }
            }

        }
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Delete a user search",
        description = "Deletes a user search by identifier.")
    @RequestMapping(value = "/{searchIdentifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<String> deleteUserCustomSerach(
        @Parameter(
            description = "Search identifier."
        )
        @PathVariable
            Integer searchIdentifier,
        @Parameter(hidden = true)
            HttpSession httpSession
    ) {
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile myProfile = session.getProfile();

        UserSearch userSearch = retrieveUserSearch(userSearchRepository, searchIdentifier);

        if (myProfile.equals(Profile.Administrator)) {
            userSearchRepository.deleteById(searchIdentifier);
        } else {
            if (userSearch.getCreator().getId() == session.getUserIdAsInt()) {
                userSearchRepository.deleteById(searchIdentifier);
            } else {
                throw new IllegalArgumentException("You don't have rights to delete the user search");
            }
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    /**
     * Retrieves an user search by identifier, if not found throws an exception.
     *
     * @param userSearchRepository
     * @param searchIdentifier
     * @return
     * @throws ResourceNotFoundEx
     */
    private UserSearch retrieveUserSearch(UserSearchRepository userSearchRepository, Integer searchIdentifier)
        throws ResourceNotFoundEx {
        Optional<UserSearch> userSearch = userSearchRepository.findById(searchIdentifier);

        if (!userSearch.isPresent()) {
            throw new ResourceNotFoundEx("User search not found");
        }

        return userSearch.get();
    }
}
