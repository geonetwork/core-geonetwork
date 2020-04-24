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
import org.apache.commons.io.FilenameUtils;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.GeonetEntity;
import org.fao.geonet.domain.Language;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.Source_;
import org.fao.geonet.guiapi.search.XsltResponseWriter;
import org.fao.geonet.repository.LanguageRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jeeves.server.context.ServiceContext;
import springfox.documentation.annotations.ApiIgnore;

@RequestMapping(value = {
    "/{portal}/api/sources",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/sources"
})
@Api(value = "sources",
    tags = "sources",
    description = "Source catalogue operations")
@Controller("sources")
public class SourcesApi {

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    LanguageRepository langRepository;

    @ApiOperation(
        value = "Get all sources",
        notes = "Sources are the local catalogue, subportal, external catalogue (when importing MEF files) or harvesters.",
        nickname = "getSources")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of source catalogues.")
    })
    @ResponseBody
    public List<Source> getSources(
        @ApiParam(
            value = "Group owner of the source (only applies to subportal)."
        )
        @RequestParam(
            value = "group",
            required = false)
        Integer group
    ) throws Exception {
        if (group != null) {
            return sourceRepository.findByGroupOwner(group);
        } else {
            return sourceRepository.findAll(SortUtils.createSort(Source_.name));
        }
    }

    @ApiOperation(
        value = "Get portal list",
        notes = "List all subportal available.",
        nickname = "getSubPortal")
    @RequestMapping(
        produces = MediaType.TEXT_HTML_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of portals.")
    })
    @ResponseBody
    public void getSubPortal(
        @ApiIgnore
            HttpServletResponse response
    ) throws Exception {
        final List<Source> sources = sourceRepository.findAll(SortUtils.createSort(Source_.name));
        Element sourcesList = new Element("sources");
        sources.stream().map(GeonetEntity::asXml).forEach(sourcesList::addContent);
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.getWriter().write(
            new XsltResponseWriter()
                .withJson("catalog/locales/en-core.json")
                .withJson("catalog/locales/en-search.json")
                .withXml(sourcesList)
                .withParam("cssClass", "gn-portal")
                .withXsl("xslt/ui-search/portal-list.xsl")
                .asHtml());
    }


    @ApiOperation(
        value = "Add a source",
        notes = "",
        nickname = "addSource")
    @RequestMapping(
        method = RequestMethod.PUT,
        produces = {
                MediaType.TEXT_PLAIN_VALUE
        })
    @PreAuthorize("hasRole('Administrator')")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Source created."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    public ResponseEntity addSource(
        @ApiParam(
            name = "source"
        )
        @RequestBody
            Source source,
        @ApiIgnore
        HttpServletRequest request) {
        Source existing = sourceRepository.findOne(source.getUuid());
        if (existing != null) {
            throw new IllegalArgumentException(String.format(
                "A source with uuid '%s' already exist", source.getUuid()
            ));
        }

        existing = sourceRepository.findOneByName(source.getName());
        if (existing != null) {
            throw new IllegalArgumentException(String.format(
                "A source with name '%s' already exist", source.getName()
            ));
        }

        // Populate languages if not already set
        java.util.List<Language> allLanguages = langRepository.findAll();
        Map<String, String> labelTranslations = source.getLabelTranslations();
        for (Language l : allLanguages) {
            String label = labelTranslations.get(l.getId());
            source.getLabelTranslations().put(l.getId(),
                label == null ? source.getName() : label);
        }

        Source sourceCreated = sourceRepository.save(source);
        copySourceLogo(source, request);
        return new ResponseEntity(sourceCreated.getUuid(), HttpStatus.CREATED);
    }

    private void copySourceLogo(Source source, HttpServletRequest request) {
        if (source.getLogo() != null) {
            ServiceContext context = ApiUtils.createServiceContext(request);
            context.getBean(Resources.class).copyLogo(context, "images" + File.separator + "harvesting" + File.separator + source.getLogo(),
                                                      source.getUuid());
        }
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
            Source source,
        @ApiIgnore
        HttpServletRequest request) throws Exception {
        Source existingSource = sourceRepository.findOne(sourceIdentifier);
        if (existingSource != null) {
            updateSource(sourceIdentifier, source, sourceRepository);
            copySourceLogo(source, request);
        } else {
            throw new ResourceNotFoundException(String.format(
                "Source with uuid '%s' does not exist.",
                sourceIdentifier
            ));
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    @ApiOperation(
        value = "Remove a source",
        notes = "",
        nickname = "deleteSource")
    @RequestMapping(
        value = "/{sourceIdentifier}",
        method = RequestMethod.DELETE
    )
    @PreAuthorize("hasRole('Administrator')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Source deleted."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    public ResponseEntity deleteSource(
        @ApiParam(
            value = "Source identifier",
            required = true
        )
        @PathVariable
            String sourceIdentifier,
        @ApiIgnore
            HttpServletRequest request
    ) throws ResourceNotFoundException {
        Source existingSource = sourceRepository.findOne(sourceIdentifier);
        if (existingSource != null) {
            if (existingSource.getLogo() != null) {
                ServiceContext context = ApiUtils.createServiceContext(request);
                final Resources resources = context.getBean(Resources.class);
                final Path logoDir = resources.locateLogosDir(context);
                try {
                    resources.deleteImageIfExists(existingSource.getUuid() + "." +
                                                          FilenameUtils.getExtension(existingSource.getLogo()),
                                                  logoDir);
                } catch (IOException ignored) {
                }
            }
            sourceRepository.delete(existingSource);
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
            entity.setUuid(source.getUuid());
            entity.setType(source.getType());
            entity.setFilter(source.getFilter());
            entity.setGroupOwner(source.getGroupOwner());
            entity.setServiceRecord(source.getServiceRecord());
            entity.setUiConfig(source.getUiConfig());
            entity.setLogo(source.getLogo());
            Map<String, String> labelTranslations = source.getLabelTranslations();
            if (labelTranslations != null) {
                entity.getLabelTranslations().clear();
                entity.getLabelTranslations().putAll(labelTranslations);
            }
        });
    }
}
