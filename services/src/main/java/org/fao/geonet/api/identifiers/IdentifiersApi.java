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

package org.fao.geonet.api.identifiers;

import io.swagger.annotations.*;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.MetadataIdentifierTemplate;
import org.fao.geonet.repository.MetadataIdentifierTemplateRepository;
import org.fao.geonet.repository.specification.MetadataIdentifierTemplateSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = {
    "/{portal}/api/identifiers",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/identifiers"
})
@Api(value = "identifiers",
    tags = "identifiers",
    description = "Identifiers operations")
@Controller("identifiers")
@PreAuthorize("hasRole('Editor')")
public class IdentifiersApi {

    private static final String API_PARAM_IDENTIFIER = "Identifier template identifier";
    private static final String API_PARAM_IDENTIFIER_TEMPLATE_DETAILS = "Identifier template details";
    public static final String MSG_NO_METADATA_IDENTIFIER_FOUND_WITH_ID = "No metadata identifier found with id '%d'.";

    @Autowired
    private MetadataIdentifierTemplateRepository metadataIdentifierTemplateRepository;

    @ApiOperation(
        value = "Get identifier templates",
        notes = "Identifier templates are used to create record UUIDs " +
            "havind a particular structure. The template will be used " +
            "when user creates a new record. The template identifier to " +
            "use is defined in the administration > settings.",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "getIdentifiers")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of identifier templates.") ,
        @ApiResponse(code = 403, message = "Operation not allowed. Only Editor can access it.")
    })
    @PreAuthorize("hasRole('Editor') or hasRole('Reviewer') or hasRole('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public List<MetadataIdentifierTemplate> getIdentifiers(
        @ApiParam(
            value = "Only user defined ones",
            required = false
        )
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
            boolean userDefinedOnly
    ) throws Exception {
        if (userDefinedOnly) {
            return metadataIdentifierTemplateRepository
                .findAll(MetadataIdentifierTemplateSpecs.isSystemProvided(false));
        } else {
            return metadataIdentifierTemplateRepository.findAll();
        }
    }


    @ApiOperation(
        value = "Add an identifier template",
        notes = "",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "addIdentifier")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT
    )
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Identifier template created.") ,
        @ApiResponse(code = 403, message = "Operation not allowed. Only Editor can access it.")
    })
    @PreAuthorize("hasRole('Editor') or hasRole('Reviewer') or hasRole('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public  ResponseEntity<Integer> addIdentifier(
        @ApiParam(
            value = API_PARAM_IDENTIFIER_TEMPLATE_DETAILS
        )
        @RequestBody
            MetadataIdentifierTemplate metadataIdentifierTemplate
    ) throws Exception {
        final MetadataIdentifierTemplate existingId = metadataIdentifierTemplateRepository
            .findOne(metadataIdentifierTemplate.getId());

        if (existingId != null) {
            throw new IllegalArgumentException(String.format(
                "A metadata identifier template with id '%d' already exist.",
                metadataIdentifierTemplate.getId()
            ));
        }

        metadataIdentifierTemplate =
            metadataIdentifierTemplateRepository.save(metadataIdentifierTemplate);

        return new ResponseEntity<>(metadataIdentifierTemplate.getId(), HttpStatus.CREATED);
    }

    @ApiOperation(
        value = "Update an identifier template",
        notes = "",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "updateIdentifier")
    @RequestMapping(
        path = "/{identifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Identifier template updated.") ,
        @ApiResponse(code = 404, message = "Resource not found.") ,
        @ApiResponse(code = 403, message = "Operation not allowed. Only Editor can access it.")
    })
    @PreAuthorize("hasRole('Editor') or hasRole('Reviewer') or hasRole('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public void updateIdentifier(
        @ApiParam(
            value = API_PARAM_IDENTIFIER
        )
        @PathVariable
            Integer identifier,
        @ApiParam(
            value = API_PARAM_IDENTIFIER_TEMPLATE_DETAILS
        )
        @RequestBody
            MetadataIdentifierTemplate metadataIdentifierTemplate
    ) throws Exception {
        MetadataIdentifierTemplate existing =
            metadataIdentifierTemplateRepository.findOne(identifier);
        if (existing == null) {
            throw new ResourceNotFoundException(String.format(
                MSG_NO_METADATA_IDENTIFIER_FOUND_WITH_ID,
                identifier
            ));
        } else {
            metadataIdentifierTemplateRepository.save(metadataIdentifierTemplate);
        }
    }



    @ApiOperation(
        value = "Remove an identifier template",
        notes = "",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        nickname = "deleteIdentifier")
    @RequestMapping(
        path = "/{identifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.DELETE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Template identifier removed.") ,
        @ApiResponse(code = 404, message = "Resource not found.") ,
        @ApiResponse(code = 403, message = "Operation not allowed. Only Editor can access it.")
    })
    @PreAuthorize("hasRole('Editor') or hasRole('Reviewer') or hasRole('UserAdmin') or hasRole('Administrator')")
    public void deleteIdentifier(
        @ApiParam(
            value = API_PARAM_IDENTIFIER,
            required = true
        )
        @PathVariable
            int identifier
    ) throws Exception {
        MetadataIdentifierTemplate existing =
            metadataIdentifierTemplateRepository.findOne(identifier);
        if (existing == null) {
            throw new ResourceNotFoundException(String.format(
                MSG_NO_METADATA_IDENTIFIER_FOUND_WITH_ID,
                identifier
            ));
        } else {
            metadataIdentifierTemplateRepository.delete(identifier);
        }
    }
}
