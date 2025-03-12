/*
 * Copyright (C) 2001-2022 Food and Agriculture Organization of the
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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.fao.geonet.api.ApiError;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.tools.i18n.TranslationPackBuilder;
import org.fao.geonet.domain.*;
import org.fao.geonet.guiapi.search.XsltResponseWriter;
import org.fao.geonet.repository.LanguageRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.resources.Resources;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.fao.geonet.api.ApiUtils.setHeaderVaryOnAccept;


@RequestMapping(value = {
    "/{portal}/api/sources"
})
@Tag(name = "sources",
    description = "Source catalogue operations")
@Controller("sources")
public class SourcesApi {

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    LanguageRepository langRepository;

    @Autowired
    private TranslationPackBuilder translationPackBuilder;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get all sources",
        description = "Sources are the local catalogue, subportal, external catalogue (when importing MEF files) or harvesters.")
    @RequestMapping(
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_HTML_VALUE
        },
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of source catalogues.",
            content = {
                @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Source.class))),
                @Content(mediaType = MediaType.TEXT_HTML_VALUE, schema = @Schema(type = "string"))
            })
    })
    @ResponseBody
    public ResponseEntity<?> getSources(
        @Parameter(
            description = "Group owner of the source (only applies to subportal)."
        )
        @RequestParam(
            value = "group",
            required = false)
        Integer group,
        @RequestParam(
            value = "type",
            required = false)
        SourceType type,
        @Parameter(hidden = true)
        HttpServletResponse response,
        @Parameter(hidden = true)
        HttpServletRequest request
    ) throws Exception {
        setHeaderVaryOnAccept(response);
        List<Source> sources;
        if (group != null && type != null) {
            sources = sourceRepository.findByGroupOwnerAndType(group, type, SortUtils.createSort(Source_.name));
        } else if (group != null) {
            sources = sourceRepository.findByGroupOwner(group, SortUtils.createSort(Source_.name));
        } else if (type != null) {
            sources = sourceRepository.findByType(type, SortUtils.createSort(Source_.name));
        } else {
            sources = sourceRepository.findAll(SortUtils.createSort(Source_.name));
        }

        if (sources == null) {
            return ResponseEntity.notFound().build();
        }
        String acceptHeader = StringUtils.isBlank(request.getHeader(HttpHeaders.ACCEPT))?MediaType.APPLICATION_JSON_VALUE:request.getHeader(HttpHeaders.ACCEPT);
        if (acceptHeader.contains(MediaType.TEXT_HTML_VALUE)) {
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(getSourcesAsHtml(sources));
        } else {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(sources);
        }
    }

    private String getSourcesAsHtml(List<Source> sources) throws Exception {
        Element sourcesList = new Element("sources");
        sources.stream().map(GeonetEntity::asXml).forEach(sourcesList::addContent);
        return new XsltResponseWriter(null, "portal")
                .withJson("catalog/locales/en-core.json")
                .withJson("catalog/locales/en-search.json")
                .withXml(sourcesList)
                .withParam("cssClass", "gn-portal")
                .withXsl("xslt/ui-search/portal-list.xsl")
                .asHtml();
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get a source",
        description = "")
    @RequestMapping(
        value = "/{sourceIdentifier}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Source found."),
        @ApiResponse(responseCode = "404", description = "Source not found.",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @ResponseBody
    public Source getSource(
        @Parameter(
            description = "Source identifier",
            required = true
        )
        @PathVariable
        String sourceIdentifier) throws Exception {
        Optional<Source> existingSource = sourceRepository.findById(sourceIdentifier);
        if (existingSource.isPresent()) {
            return existingSource.get();
        } else {
            throw new ResourceNotFoundException(String.format(
                "Source with uuid '%s' does not exist.",
                sourceIdentifier
            ));
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Add a source",
        description = "")
    @RequestMapping(
        method = RequestMethod.PUT,
        produces = {
            MediaType.TEXT_PLAIN_VALUE
        })
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Source created."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    @ResponseBody
    public ResponseEntity addSource(
        @Parameter(
            name = "source"
        )
        @RequestBody
        Source source,
        @Parameter(hidden = true)
        HttpServletRequest request) {
        Optional<Source> existing = sourceRepository.findById(source.getUuid());
        if (existing.isPresent()) {
            throw new IllegalArgumentException(String.format(
                "A source with uuid '%s' already exist", source.getUuid()
            ));
        }

        Source existingWithSameName = sourceRepository.findOneByName(source.getName());
        if (existingWithSameName != null) {
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

        translationPackBuilder.clearCache();

        return new ResponseEntity(sourceCreated.getUuid(), HttpStatus.CREATED);
    }

    private void copySourceLogo(Source source, HttpServletRequest request) {
        if (source.getLogo() != null) {
            ServiceContext context = ApiUtils.createServiceContext(request);
            context.getBean(Resources.class).copyLogo(context, "images" + File.separator + "harvesting" + File.separator + source.getLogo(),
                source.getUuid());
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Update a source",
        description = "")
    @RequestMapping(
        value = "/{sourceIdentifier}",
        method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Source updated.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "404", description = "Source not found."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public ResponseEntity updateSource(
        @Parameter(
            description = "Source identifier",
            required = true
        )
        @PathVariable
        String sourceIdentifier,
        @Parameter(
            name = "source"
        )
        @RequestBody
        Source source,
        @Parameter(hidden = true)
        HttpServletRequest request) throws Exception {
        Optional<Source> existingSource = sourceRepository.findById(sourceIdentifier);
        if (existingSource.isPresent()) {
            // Rebuild translation pack cache if there are changes in the translations
            boolean clearTranslationPackCache =
                !existingSource.get().getLabelTranslations().equals(source.getLabelTranslations());

            updateSource(sourceIdentifier, source, sourceRepository);
            copySourceLogo(source, request);

            if (clearTranslationPackCache) {
                translationPackBuilder.clearCache();
            }
        } else {
            throw new ResourceNotFoundException(String.format(
                "Source with uuid '%s' does not exist.",
                sourceIdentifier
            ));
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Remove a source",
        description = "")
    @RequestMapping(
        value = "/{sourceIdentifier}",
        method = RequestMethod.DELETE
    )
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Source deleted.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    @ResponseBody
    public ResponseEntity deleteSource(
        @Parameter(
            description = "Source identifier",
            required = true
        )
        @PathVariable
        String sourceIdentifier,
        @Parameter(hidden = true)
        HttpServletRequest request
    ) throws ResourceNotFoundException {
        Optional<Source> existingSource = sourceRepository.findById(sourceIdentifier);
        if (existingSource.isPresent()) {
            if (existingSource.get().getLogo() != null) {
                ServiceContext context = ApiUtils.createServiceContext(request);
                final Resources resources = context.getBean(Resources.class);
                final Path logoDir = resources.locateLogosDir(context);
                try {
                    resources.deleteImageIfExists(existingSource.get().getUuid() + "." +
                            FilenameUtils.getExtension(existingSource.get().getLogo()),
                        logoDir);
                } catch (IOException ignored) {
                }
            }
            sourceRepository.delete(existingSource.get());
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
            entity.setListableInHeaderSelector(source.isListableInHeaderSelector());
            Map<String, String> labelTranslations = source.getLabelTranslations();
            entity.getLabelTranslations().clear();
            entity.getLabelTranslations().putAll(labelTranslations);

        });
    }
}
