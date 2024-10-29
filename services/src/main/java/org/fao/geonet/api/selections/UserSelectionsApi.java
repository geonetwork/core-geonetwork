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
package org.fao.geonet.api.selections;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.search.submission.BatchingIndexSubmittor;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmittor;
import org.fao.geonet.repository.LanguageRepository;
import org.fao.geonet.repository.SelectionRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.UserSavedSelectionRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.fao.geonet.repository.specification.UserGroupSpecs.hasGroupIds;
import static org.fao.geonet.repository.specification.UserGroupSpecs.hasUserId;
import static org.fao.geonet.repository.specification.UserGroupSpecs.hasProfile;
import static org.springframework.data.jpa.domain.Specification.where;

/**
 * Select a list of elements stored in session.
 */
@RequestMapping(value = {
    "/{portal}/api/userselections"
})
@Tag(name = "userselections",
    description = "User selections related operations")
@Controller("userselections")
public class UserSelectionsApi {

    @Autowired
    SelectionRepository selectionRepository;

    @Autowired
    UserSavedSelectionRepository umsRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LanguageRepository langRepository;

    @Autowired
    IMetadataUtils metadataRepository;

    @Autowired
    UserGroupRepository userGroupRepository;

    @Autowired
    IMetadataIndexer metadataIndexer;

    @io.swagger.v3.oas.annotations.Operation(summary = "Get list of user selection sets")
    @RequestMapping(
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    List<Selection> getSelectionList()
        throws Exception {
        return selectionRepository.findAll();
    }


    @io.swagger.v3.oas.annotations.Operation(summary = "Add a user selection set")
    @RequestMapping(
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Selection created."),
        @ApiResponse(responseCode = "400", description = "A selection with that id or name already exist."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseBody
    public ResponseEntity createPersistentSelectionType(
        @Parameter(
            name = "selection"
        )
        @RequestBody
            Selection selection
    )
        throws Exception {
        Optional<Selection> existingSelectionById = selectionRepository.findById(selection.getId());
        if (existingSelectionById.isPresent()) {
            throw new IllegalArgumentException(String.format(
                "A selection with id '%d' already exist. Choose another id or unset it.",
                selection.getId()
            ));
        }

        Selection existingSelection = selectionRepository.findOneByName(selection.getName());
        if (existingSelection != null) {
            throw new IllegalArgumentException(String.format(
                "A selection with name '%s' already exist. Choose another name.", selection.getName()
            ));
        }
        // Populate languages if not already set
        java.util.List<Language> allLanguages = langRepository.findAll();
        Map<String, String> labelTranslations = selection.getLabelTranslations();
        for (Language l : allLanguages) {
            String label = labelTranslations.get(l.getId());
            selection.getLabelTranslations().put(l.getId(),
                label == null ? selection.getName() : label);
        }
        selectionRepository.save(selection);
        return new ResponseEntity<>(selection.getId(), HttpStatus.CREATED);
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Update a user selection set",
        description = "")
    @RequestMapping(
        value = "/{selectionIdentifier}",
        method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Selection updated.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "404", description = "Selection not found."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @PreAuthorize("hasAuthority('UserAdmin')")
    public ResponseEntity updateUserSelection(
        @Parameter(
            description = "Selection identifier",
            required = true
        )
        @PathVariable
            Integer selectionIdentifier,
        @Parameter(
            name = "selection"
        )
        @RequestBody
            Selection selection
    ) throws Exception {
        Optional<Selection> existingSelection = selectionRepository.findById(selectionIdentifier);
        if (existingSelection.isPresent()) {
            selection.setId(selectionIdentifier);
            selectionRepository.save(selection);
//            selectionRepository.update(selectionIdentifier, entity -> {
//                entity.setWatchable(selection.isWatchable());
//                entity.setName(selection.getName());
//                Map<String, String> labelTranslations = selection.getLabelTranslations();
//                if (labelTranslations != null) {
//
//                    entity.getLabelTranslations().putAll(labelTranslations);
//                }
//            });
        } else {
            throw new ResourceNotFoundException(String.format(
                "Selection with id '%d' does not exist.",
                selectionIdentifier
            ));
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Remove a user selection set",
        description = "")
    @RequestMapping(
        value = "/{selectionIdentifier}",
        method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Selection removed.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "404", description = "Selection not found."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @PreAuthorize("hasAuthority('UserAdmin')")
    public ResponseEntity deleteUserSelection(
        @Parameter(
            description = "Selection identifier",
            required = true
        )
        @PathVariable
            Integer selectionIdentifier
    ) throws Exception {
        Optional<Selection> selection = selectionRepository.findById(selectionIdentifier);
        if (selection.isPresent()) {
            umsRepository.deleteAllBySelection(selectionIdentifier);
            selectionRepository.deleteById(selectionIdentifier);
        } else {
            throw new ResourceNotFoundException(String.format(
                "Selection with id '%d' does not exist.",
                selectionIdentifier
            ));
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    @io.swagger.v3.oas.annotations.Operation(summary = "Get record in a user selection set")
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/{selectionIdentifier}/{userIdentifier}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasAuthority('Guest')")
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    List<String> getSelectionRecords(
        @Parameter(description = "Selection identifier",
            required = true)
        @PathVariable
            Integer selectionIdentifier,
        @Parameter(description = "User identifier",
            required = true)
        @PathVariable
            Integer userIdentifier,
        @Parameter(hidden = true)
            HttpSession httpSession
    )
        throws Exception {
        Optional<User> user = checkUserAllowed(httpSession, userIdentifier);

        Optional<Selection> selection = selectionRepository.findById(selectionIdentifier);
        if (!selection.isPresent()) {
            throw new ResourceNotFoundException(String.format(
                "Selection with id '%d' does not exist.",
                selectionIdentifier
            ));
        }

        return umsRepository.findMetadata(selectionIdentifier, userIdentifier);

    }


    @io.swagger.v3.oas.annotations.Operation(summary = "Add items to a user selection set")
    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/{selectionIdentifier}/{userIdentifier}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasAuthority('Guest')")
    public
    @ResponseBody
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Records added to selection set."),
        @ApiResponse(responseCode = "404", description = "Selection or user or at least one UUID not found."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    ResponseEntity<String> addToUserSelection(
        @Parameter(description = "Selection identifier",
            required = true)
        @PathVariable
            Integer selectionIdentifier,
        @Parameter(description = "User identifier",
            required = true)
        @PathVariable
            Integer userIdentifier,
        @Parameter(description = "One or more record UUIDs.",
            required = false)
        @RequestParam(required = false)
            String[] uuid,
        @Parameter(hidden = true)
            HttpSession httpSession
    )
        throws Exception {
        Optional<User> user = checkUserAllowed(httpSession, userIdentifier);

        Optional<Selection> selection = selectionRepository.findById(selectionIdentifier);
        if (!selection.isPresent()) {
            throw new ResourceNotFoundException(String.format(
                "Selection with id '%d' does not exist.",
                selectionIdentifier
            ));
        }

        for (String u : uuid) {
            // Check record exist
            if (metadataRepository.existsMetadataUuid(u)) {
                UserSavedSelection e = new UserSavedSelection(selection.get(), user.get(), u);
                try {
                    umsRepository.save(e);
                    metadataIndexer.indexMetadata(metadataRepository.getMetadataId(u), DirectIndexSubmittor.INSTANCE, IndexingMode.full);
                } catch (Exception e1) {
                    Log.error(API.LOG_MODULE_NAME, "UserSelectionsApi - addToUserSelection: " + e1.getMessage(), e1);
                }
            } else {
                return new ResponseEntity<>(u, HttpStatus.NOT_FOUND);
            }
        }

        return new ResponseEntity<>(HttpStatus.CREATED);

    }


    @io.swagger.v3.oas.annotations.Operation(summary = "Remove items to a user selection set")
    @RequestMapping(
        method = RequestMethod.DELETE,
        value = "/{selectionIdentifier}/{userIdentifier}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasAuthority('Guest')")
    public
    @ResponseBody
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Items removed from a set.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "404", description = "Selection or user not found."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    ResponseEntity deleteFromUserSelection(
        @Parameter(description = "Selection identifier",
            required = true)
        @PathVariable
            Integer selectionIdentifier,
        @Parameter(description = "User identifier",
            required = true)
        @PathVariable
            Integer userIdentifier,
        @Parameter(description = "One or more record UUIDs. If null, remove all.",
            required = false)
        @RequestParam(required = false)
            String[] uuid,
        @Parameter(hidden = true)
            HttpSession httpSession
    )
        throws Exception {
        Optional<User> user = checkUserAllowed(httpSession, userIdentifier);

        Optional<Selection> selection = selectionRepository.findById(selectionIdentifier);
        if (!selection.isPresent()) {
            throw new ResourceNotFoundException(String.format(
                "Selection with id '%d' does not exist.",
                selectionIdentifier
            ));
        }

        if (uuid == null || uuid.length == 0) {
            umsRepository.deleteAllBySelectionAndUser(selectionIdentifier, userIdentifier);
        } else {
            try (BatchingIndexSubmittor batchingIndexSubmittor = new BatchingIndexSubmittor()) {
                for (String u : uuid) {
                    UserSavedSelectionId e = new UserSavedSelectionId()
                        .setSelectionId(selectionIdentifier)
                        .setUserId(userIdentifier)
                        .setMetadataUuid(u);
                    umsRepository.deleteById(e);
                    metadataIndexer.indexMetadata(metadataRepository.getMetadataId(u), batchingIndexSubmittor, IndexingMode.full);
                }
            }
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }


    private Optional<User> checkUserAllowed(HttpSession httpSession, Integer userIdentifier) throws ResourceNotFoundException {
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile myProfile = session.getProfile();
        String myUserId = session.getUserId();

        if (myProfile.equals(Profile.Administrator) || myProfile.equals(Profile.UserAdmin) ||
            myUserId.equals(Integer.toString(userIdentifier))) {

            Optional<User> user = userRepository.findById(userIdentifier);
            if (!user.isPresent()) {
                throw new ResourceNotFoundException(String.format(
                    "User with id '%d' does not exist.",
                    userIdentifier
                ));
            }

            if (!(myUserId.equals(Integer.toString(userIdentifier))) && myProfile == Profile.UserAdmin) {

                //--- retrieve session user groups and check to see whether this user is
                //--- allowed to get this info
                List<Integer> myUserGroupsAsUserAdmin = userGroupRepository.findGroupIds(where(hasUserId(Integer.parseInt(myUserId))).
                    and(hasProfile(Profile.UserAdmin)));
                // Now check if the userIdentifier is part of one of those useradmin groups.
                List<Integer> adminlist = userGroupRepository.findGroupIds(where(hasGroupIds(myUserGroupsAsUserAdmin)).and(hasUserId
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
}
