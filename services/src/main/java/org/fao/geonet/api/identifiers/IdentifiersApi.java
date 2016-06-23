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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
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

    @ApiOperation(
        value = "Get identifier templates",
        notes = "",
        nickname = "getIdentifiers")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
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
        nickname = "addIdentifier")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT
    )
    @ResponseBody
    public ResponseEntity<Integer> addIdentifier(
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

        return new ResponseEntity<Integer>(
            metadataIdentifierTemplate.getId(), HttpStatus.CREATED);
    }

    @ApiOperation(
        value = "Update an identifier template",
        notes = "",
        nickname = "updateIdentifier")
    @RequestMapping(
        path = "/{identifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT
    )
    @ResponseBody
    public ResponseEntity updateIdentifier(
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

        metadataIdentifierTemplateRepository.update(identifier,
            new Updater<MetadataIdentifierTemplate>() {
                @Override
                public void apply(@Nonnull MetadataIdentifierTemplate entity) {
                    entity.setName(name);
                    entity.setTemplate(template);
                }
            });
        return new ResponseEntity<Integer>(HttpStatus.NO_CONTENT);
    }
    @ApiOperation(
        value = "Remove an identifier template",
        notes = "",
        nickname = "deleteIdentifier")
    @RequestMapping(
        path = "/{identifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.DELETE
    )
    @ResponseBody
    public ResponseEntity deleteIdentifier(
        @PathVariable
            int identifier
    ) throws Exception {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        MetadataIdentifierTemplateRepository metadataIdentifierTemplateRepository =
            appContext.getBean(MetadataIdentifierTemplateRepository.class);

        metadataIdentifierTemplateRepository.delete(identifier);

        return new ResponseEntity<Integer>(HttpStatus.NO_CONTENT);
    }
}
