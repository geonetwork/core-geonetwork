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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.fao.geonet.api.API;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.MetadataIdentifierTemplate;
import org.fao.geonet.repository.MetadataIdentifierTemplateRepository;
import org.fao.geonet.repository.specification.MetadataIdentifierTemplateSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequestMapping(value = {
    "/{portal}/api/identifiers"
})
@Tag(name = "identifiers",
    description = "Identifiers operations")
@Controller("identifiers")
@PreAuthorize("hasAuthority('Editor')")
public class IdentifiersApi {

    public static final String MSG_NO_METADATA_IDENTIFIER_FOUND_WITH_ID = "No metadata identifier found with id '%d'.";
    private static final String API_PARAM_IDENTIFIER = "Identifier template identifier";
    private static final String API_PARAM_IDENTIFIER_TEMPLATE_DETAILS = "Identifier template details";
    @Autowired
    private MetadataIdentifierTemplateRepository metadataIdentifierTemplateRepository;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get identifier templates",
        description = "Identifier templates are used to create record UUIDs " +
            "having a particular structure. The template will be used " +
            "when user creates a new record. The identifier template to " +
            "use is defined in the administration > settings."
        //       authorizations = {
        //           @Authorization(value = "basicAuth")
        //      })
    )
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of identifier templates."),
        @ApiResponse(responseCode = "403", description = "Operation not allowed. Only Editor can access it.")
    })
    @PreAuthorize("hasAuthority('Editor') or hasRole('Reviewer') or hasRole('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public List<MetadataIdentifierTemplate> getIdentifiers(
        @Parameter(
            description = "Only user defined ones",
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


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Add an identifier template",
        description = ""
        //       authorizations = {
        //           @Authorization(value = "basicAuth")
        //      })
    )
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT
    )
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Identifier template created."),
        @ApiResponse(responseCode = "403", description = "Operation not allowed. Only Editor can access it.")
    })
    @PreAuthorize("hasAuthority('Editor') or hasRole('Reviewer') or hasRole('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public ResponseEntity<Integer> addIdentifier(
        @Parameter(
            description = API_PARAM_IDENTIFIER_TEMPLATE_DETAILS
        )
        @RequestBody
            MetadataIdentifierTemplate metadataIdentifierTemplate
    ) throws Exception {
        final Optional<MetadataIdentifierTemplate> existingId = metadataIdentifierTemplateRepository
            .findById(metadataIdentifierTemplate.getId());

        if (existingId.isPresent()) {
            throw new IllegalArgumentException(String.format(
                "A metadata identifier template with id '%d' already exist.",
                metadataIdentifierTemplate.getId()
            ));
        }

        metadataIdentifierTemplate =
            metadataIdentifierTemplateRepository.save(metadataIdentifierTemplate);

        return new ResponseEntity<>(metadataIdentifierTemplate.getId(), HttpStatus.CREATED);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Update an identifier template",
        description = ""
        //       authorizations = {
        //           @Authorization(value = "basicAuth")
        //      })
    )
    @RequestMapping(
        path = "/{identifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Identifier template updated.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "404", description = "Resource not found."),
        @ApiResponse(responseCode = "403", description = "Operation not allowed. Only Editor can access it.")
    })
    @PreAuthorize("hasAuthority('Editor') or hasRole('Reviewer') or hasRole('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public void updateIdentifier(
        @Parameter(
            description = API_PARAM_IDENTIFIER
        )
        @PathVariable
            Integer identifier,
        @Parameter(
            description = API_PARAM_IDENTIFIER_TEMPLATE_DETAILS
        )
        @RequestBody
            MetadataIdentifierTemplate metadataIdentifierTemplate
    ) throws Exception {
        Optional<MetadataIdentifierTemplate> existing =
            metadataIdentifierTemplateRepository.findById(identifier);
        if (!existing.isPresent()) {
            throw new ResourceNotFoundException(String.format(
                MSG_NO_METADATA_IDENTIFIER_FOUND_WITH_ID,
                identifier
            ));
        } else {
            metadataIdentifierTemplateRepository.save(metadataIdentifierTemplate);
        }
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Remove an identifier template",
        description = ""
        //       authorizations = {
        //           @Authorization(value = "basicAuth")
        //      })
    )
    @RequestMapping(
        path = "/{identifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.DELETE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Template identifier removed.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "404", description = "Resource not found."),
        @ApiResponse(responseCode = "403", description = "Operation not allowed. Only Editor can access it.")
    })
    @PreAuthorize("hasAuthority('Editor') or hasRole('Reviewer') or hasRole('UserAdmin') or hasRole('Administrator')")
    @ResponseBody
    public void deleteIdentifier(
        @Parameter(
            description = API_PARAM_IDENTIFIER,
            required = true
        )
        @PathVariable
            int identifier
    ) throws Exception {
        Optional<MetadataIdentifierTemplate> existing =
            metadataIdentifierTemplateRepository.findById(identifier);
        if (!existing.isPresent()) {
            throw new ResourceNotFoundException(String.format(
                MSG_NO_METADATA_IDENTIFIER_FOUND_WITH_ID,
                identifier
            ));
        } else {
            metadataIdentifierTemplateRepository.deleteById(identifier);
        }
    }
}
