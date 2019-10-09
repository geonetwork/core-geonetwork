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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Language;
import org.fao.geonet.domain.Selection;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserSavedSelection;
import org.fao.geonet.domain.UserSavedSelectionId;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.LanguageRepository;
import org.fao.geonet.repository.SelectionRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.UserSavedSelectionRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Select a list of elements stored in session.
 */
@RequestMapping(value = {
    "/{portal}/api/userselections",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/userselections"
})
@Api(value = "userselections",
    tags = "userselections",
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

    @ApiOperation(value = "Get list of user selection sets",
        nickname = "getUserSelectionType")
    @RequestMapping(
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    List<Selection> get(
        @ApiIgnore
            HttpSession httpSession
    )
        throws Exception {
        return selectionRepository.findAll();
    }


    @ApiOperation(value = "Add a user selection set",
        nickname = "createUserSelectionType")
    @RequestMapping(
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Selection created."),
        @ApiResponse(code = 400, message = "A selection with that id or name already exist."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @PreAuthorize("hasRole('UserAdmin')")
    @ResponseBody
    public ResponseEntity createPersistentSelectionType(
        @ApiParam(
            name = "selection"
        )
        @RequestBody
            Selection selection
    )
        throws Exception {
        Selection existingSelection = selectionRepository.findOne(selection.getId());
        if (existingSelection != null) {
            throw new IllegalArgumentException(String.format(
                "A selection with id '%d' already exist. Choose another id or unset it.",
                selection.getId()
            ));
        }

        existingSelection = selectionRepository.findOneByName(selection.getName());
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



    @ApiOperation(
        value = "Update a user selection set",
        notes = "",
        nickname = "updateUserSelection")
    @RequestMapping(
        value = "/{selectionIdentifier}",
        method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Selection updated."),
        @ApiResponse(code = 404, message = "Selection not found."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @PreAuthorize("hasRole('UserAdmin')")
    public ResponseEntity updateUserSelection(
        @ApiParam(
            value = "Selection identifier",
            required = true
        )
        @PathVariable
            Integer selectionIdentifier,
        @ApiParam(
            name = "selection"
        )
        @RequestBody
            Selection selection
    ) throws Exception {
        Selection existingSelection = selectionRepository.findOne(selectionIdentifier);
        if (existingSelection != null) {
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


    @ApiOperation(
        value = "Remove a user selection set",
        notes = "",
        nickname = "deleteUserSelection")
    @RequestMapping(
        value = "/{selectionIdentifier}",
        method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Selection removed."),
        @ApiResponse(code = 404, message = "Selection not found."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @PreAuthorize("hasRole('UserAdmin')")
    public ResponseEntity deleteUserSelection(
        @ApiParam(
            value = "Selection identifier",
            required = true
        )
        @PathVariable
            Integer selectionIdentifier
    ) throws Exception {
        Selection selection = selectionRepository.findOne(selectionIdentifier);
        if (selection != null) {
            umsRepository.deleteAllBySelection(selectionIdentifier);
            selectionRepository.delete(selectionIdentifier);
        } else {
            throw new ResourceNotFoundException(String.format(
                "Selection with id '%d' does not exist.",
                selectionIdentifier
            ));
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    @ApiOperation(value = "Get record in a user selection set",
        nickname = "getSelection")
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/{selectionIdentifier}/{userIdentifier}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasRole('Guest')")
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    List<String> get(
        @ApiParam(value = "Selection identifier",
            required = true)
        @PathVariable
            Integer selectionIdentifier,
        @ApiParam(value = "User identifier",
            required = true)
        @PathVariable
            Integer userIdentifier,
        @ApiIgnore
        HttpSession httpSession
    )
        throws Exception {
        Selection selection = selectionRepository.findOne(selectionIdentifier);
        if (selection == null) {
            throw new ResourceNotFoundException(String.format(
                "Selection with id '%d' does not exist.",
                selectionIdentifier
            ));
        }

        User user = userRepository.findOne(userIdentifier);
        if (user == null) {
            throw new ResourceNotFoundException(String.format(
                "User with id '%d' does not exist.",
                selectionIdentifier
            ));
        }

        if (selection != null) {
            return umsRepository.findMetadata(selectionIdentifier, userIdentifier);
        }
        return null;
    }



    @ApiOperation(value = "Add items to a user selection set",
        nickname = "addToUserSelection")
    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/{selectionIdentifier}/{userIdentifier}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasRole('Guest')")
    public
    @ResponseBody
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Records added to selection set."),
        @ApiResponse(code = 404, message = "Selection or user or at least one UUID not found."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    ResponseEntity<String> addToUserSelection(
        @ApiParam(value = "Selection identifier",
            required = true)
        @PathVariable
            Integer selectionIdentifier,
        @ApiParam(value = "User identifier",
            required = true)
        @PathVariable
            Integer userIdentifier,
        @ApiParam(value = "One or more record UUIDs.",
            required = false)
        @RequestParam(required = false)
            String[] uuid,
        @ApiIgnore
            HttpSession httpSession
    )
        throws Exception {
        Selection selection = selectionRepository.findOne(selectionIdentifier);
        if (selection == null) {
            throw new ResourceNotFoundException(String.format(
                "Selection with id '%d' does not exist.",
                selectionIdentifier
            ));
        }

        User user = userRepository.findOne(userIdentifier);
        if (user == null) {
            throw new ResourceNotFoundException(String.format(
                "User with id '%d' does not exist.",
                selectionIdentifier
            ));
        }

        for (String u : uuid) {
            // Check record exist
            if (metadataRepository.existsMetadataUuid(u)) {
                UserSavedSelection e = new UserSavedSelection(selection, user, u);
                try {
                    umsRepository.save(e);
                } catch (Exception e1) {
                    Log.error(API.LOG_MODULE_NAME, "UserSelectionsApi - addToUserSelection: " + e1.getMessage(), e1);
                }
            } else {
                return new ResponseEntity<>(u, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @ApiOperation(value = "Remove items to a user selection set",
        nickname = "deleteFromUserSelection")
    @RequestMapping(
        method = RequestMethod.DELETE,
        value = "/{selectionIdentifier}/{userIdentifier}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasRole('Guest')")
    public
    @ResponseBody
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Items removed from a set."),
        @ApiResponse(code = 404, message = "Selection or user not found."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    ResponseEntity deleteFromUserSelection(
        @ApiParam(value = "Selection identifier",
            required = true)
        @PathVariable
            Integer selectionIdentifier,
        @ApiParam(value = "User identifier",
            required = true)
        @PathVariable
            Integer userIdentifier,
        @ApiParam(value = "One or more record UUIDs. If null, remove all.",
            required = false)
        @RequestParam(required = false)
            String[] uuid,
        @ApiIgnore
            HttpSession httpSession
    )
        throws Exception {
        Selection selection = selectionRepository.findOne(selectionIdentifier);
        if (selection == null) {
            throw new ResourceNotFoundException(String.format(
                "Selection with id '%d' does not exist.",
                selectionIdentifier
            ));
        }

        User user = userRepository.findOne(userIdentifier);
        if (user == null) {
            throw new ResourceNotFoundException(String.format(
                "User with id '%d' does not exist.",
                selectionIdentifier
            ));
        }

        if (uuid == null || uuid.length == 0) {
            umsRepository.deleteAllBySelectionAndUser(selectionIdentifier, userIdentifier);
        } else {
            for (String u : uuid) {
                UserSavedSelectionId e = new UserSavedSelectionId()
                    .setSelectionId(selectionIdentifier)
                    .setUserId(userIdentifier)
                    .setMetadataUuid(u);
                umsRepository.delete(e);
            }
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
