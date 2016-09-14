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

package org.fao.geonet.api.sources;

import io.swagger.annotations.*;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.Source_;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.Updater;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

@RequestMapping(value = {
    "/api/sources",
    "/api/" + API.VERSION_0_1 +
        "/sources"
})
@Api(value = "sources",
    tags = "sources",
    description = "Sources operations")
@Controller("sources")
public class SourcesApi {

    @ApiOperation(
        value = "Get sources",
        notes = "A source is created for each harvester.",
        nickname = "getSources")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of sources.")
    })
    @ResponseBody
    public List<Source> getSources() throws Exception {
        ApplicationContext context = ApplicationContextHolder.get();
        // TODO-API: Check if site is added to normal sources ?
        return context.getBean(SourceRepository.class).findAll(SortUtils.createSort(Source_.name));
    }


    @ApiOperation(
        value = "Update a source",
        notes = "",
        nickname = "updateSource")
    @RequestMapping(
        value = "/{sourceIdentifier}",
        method = RequestMethod.PUT)
    @PreAuthorize("hasRole('UserAdmin')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Source updated."),
        @ApiResponse(code = 404, message = "Source not found."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    public ResponseEntity updateSource(
        @ApiParam(
            value = "Source identifier",
            required = true
        )
        @PathVariable
            String sourceIdentifier,
        @ApiParam(
            name = "source"
        )
        @RequestBody
            Source source
    ) throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();
        SourceRepository sourceRepository =
            appContext.getBean(SourceRepository.class);

        Source existingSource = sourceRepository.findOne(sourceIdentifier);
        if (existingSource != null) {
            updateSource(sourceIdentifier, source, sourceRepository);
        } else {
            throw new ResourceNotFoundException(String.format(
                "Source with uuid '%s' does not exist.",
                sourceIdentifier
            ));
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    private void updateSource(String sourceIdentifier,
                              final Source source,
                              SourceRepository sourceRepository) {
        sourceRepository.update(sourceIdentifier, entity -> {
            entity.setName(source.getName());
            Map<String, String> labelTranslations = source.getLabelTranslations();
            if (labelTranslations != null) {
                entity.getLabelTranslations().clear();
                entity.getLabelTranslations().putAll(labelTranslations);
            }
        });
    }
}
