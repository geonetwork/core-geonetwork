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
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.specification.MetadataIdentifierTemplateSpecs;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.util.List;

@RequestMapping(value = {
    "/api/identifiers",
    "/api/" + API.VERSION_0_1 +
        "/identifiers"
})
@Api(value = "identifiers",
    tags = "identifiers",
    description = "Identifiers operations")
@Controller("identifiers")
@PreAuthorize("hasRole('Editor')")
public class IdentifiersApi {

    public static final String API_PARAM_IDENTIFIER_TEMPLATE_NAME = "Identifier template name";
    private static final String API_PARAM_IDENTIFIER_TEMPLATE = "Identifier template";
    private static final String API_PARAM_IDENTIFIER = "Identifier template identifier";
    public static final String MSG_NO_METADATA_IDENTIFIER_FOUND_WITH_ID = "No metadata identifier found with id '%d'.";

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
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        MetadataIdentifierTemplateRepository metadataIdentifierTemplateRepository =
            appContext.getBean(MetadataIdentifierTemplateRepository.class);

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
    @ResponseBody
    public Integer addIdentifier(
        @ApiParam(
            value = API_PARAM_IDENTIFIER_TEMPLATE_NAME,
            required = false
        )
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
            String name,
        @ApiParam(
            value = API_PARAM_IDENTIFIER_TEMPLATE,
            required = false
        )
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
            String template
    ) throws Exception {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        MetadataIdentifierTemplateRepository metadataIdentifierTemplateRepository =
            appContext.getBean(MetadataIdentifierTemplateRepository.class);

        MetadataIdentifierTemplate metadataIdentifierTemplate = new MetadataIdentifierTemplate();
        metadataIdentifierTemplate.setName(name);
        metadataIdentifierTemplate.setTemplate(template);

        metadataIdentifierTemplate =
            metadataIdentifierTemplateRepository.save(metadataIdentifierTemplate);

        return metadataIdentifierTemplate.getId();
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
    public void updateIdentifier(
        @ApiParam(
            value = API_PARAM_IDENTIFIER,
            required = true
        )
        @PathVariable
            int identifier,
        @ApiParam(
            value = API_PARAM_IDENTIFIER_TEMPLATE_NAME,
            required = false
        )
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
            String name,
        @ApiParam(
            value = API_PARAM_IDENTIFIER_TEMPLATE,
            required = false
        )
        @RequestParam(
            required = false,
            defaultValue = "false"
        )
            String template
    ) throws Exception {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        MetadataIdentifierTemplateRepository metadataIdentifierTemplateRepository =
            appContext.getBean(MetadataIdentifierTemplateRepository.class);

        MetadataIdentifierTemplate existing =
            metadataIdentifierTemplateRepository.findOne(identifier);
        if (existing != null) {
            throw new ResourceNotFoundException(String.format(
                MSG_NO_METADATA_IDENTIFIER_FOUND_WITH_ID,
                identifier
            ));
        } else {
            metadataIdentifierTemplateRepository.update(identifier,
                new Updater<MetadataIdentifierTemplate>() {
                    @Override
                    public void apply(@Nonnull MetadataIdentifierTemplate entity) {
                        entity.setName(name);
                        entity.setTemplate(template);
                    }
                });
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
    public void deleteIdentifier(
        @ApiParam(
            value = API_PARAM_IDENTIFIER,
            required = true
        )
        @PathVariable
            int identifier
    ) throws Exception {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        MetadataIdentifierTemplateRepository metadataIdentifierTemplateRepository =
            appContext.getBean(MetadataIdentifierTemplateRepository.class);

        MetadataIdentifierTemplate existing =
            metadataIdentifierTemplateRepository.findOne(identifier);
        if (existing != null) {
            throw new ResourceNotFoundException(String.format(
                MSG_NO_METADATA_IDENTIFIER_FOUND_WITH_ID,
                identifier
            ));
        } else {
            metadataIdentifierTemplateRepository.delete(identifier);
        }
    }
}
