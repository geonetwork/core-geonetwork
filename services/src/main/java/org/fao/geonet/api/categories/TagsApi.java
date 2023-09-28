/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.api.categories;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.tools.i18n.TranslationPackBuilder;
import org.fao.geonet.domain.Language;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.repository.LanguageRepository;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequestMapping(value = {
    "/{portal}/api/tags"
})
@Tag(name = "tags",
    description = "Tags operations")
@Controller("tags")
public class TagsApi {

    @Autowired
    private MetadataCategoryRepository categoryRepository;

    @Autowired
    private LanguageRepository langRepository;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private TranslationPackBuilder translationPackBuilder;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get tags",
        description = "")
    @RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of tags.")
    })
    @ResponseBody
    public List<org.fao.geonet.domain.MetadataCategory> getTags(
    ) throws Exception {
        return categoryRepository.findAll();
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Create a tag",
        description = "If labels are not defined, a default label is created " +
            "with the category name for all languages.")
    @RequestMapping(
        method = RequestMethod.PUT,
        consumes = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tag created. Return the new tag identifier."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @PreAuthorize("hasAuthority('UserAdmin')")
    public ResponseEntity<Integer> putTag(
        @Parameter(
            name = "category"
        )
        @RequestBody
            MetadataCategory category
    ) throws Exception {
        Optional<MetadataCategory> existingCategory = categoryRepository.findById(category.getId());
        if (existingCategory.isPresent()) {
            throw new IllegalArgumentException(String.format(
                "A tag with id '%d' already exist", category.getId()
            ));
        }

        final MetadataCategory existingName = categoryRepository
            .findOneByName(category.getName());
        if (existingName != null) {
            throw new IllegalArgumentException(String.format(
                "A category with name '%s' already exist.",
                category.getName()
            ));
        }

        // Populate languages if not already set
        java.util.List<Language> allLanguages = langRepository.findAll();
        Map<String, String> labelTranslations = category.getLabelTranslations();
        for (Language l : allLanguages) {
            String label = labelTranslations.get(l.getId());
            category.getLabelTranslations().put(l.getId(),
                label == null ? category.getName() : label);
        }
        categoryRepository.save(category);

        translationPackBuilder.clearCache();

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get a tag",
        description = "")
    @RequestMapping(
        value = "/{tagIdentifier}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        },
        method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tag details.")
    })
    @ResponseBody
    public org.fao.geonet.domain.MetadataCategory getTag(
        @Parameter(
            description = "Tag identifier",
            required = true
        )
        @PathVariable
            Integer tagIdentifier
    ) throws Exception {
        Optional<MetadataCategory> category = categoryRepository.findById(tagIdentifier);
        if (!category.isPresent()) {
            throw new ResourceNotFoundException("Category not found");
        }
        return category.get();
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Update a tag",
        description = "")
    @RequestMapping(
        value = "/{tagIdentifier}",
        method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Tag updated."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @ResponseBody
    public ResponseEntity updateTag(
        @Parameter(
            description = "Tag identifier",
            required = true
        )
        @PathVariable
            int tagIdentifier,
        @Parameter(
            name = "category"
        )
        @RequestBody
            MetadataCategory category
    ) throws Exception {
        Optional<MetadataCategory> existingCategory = categoryRepository.findById(tagIdentifier);
        if (existingCategory.isPresent()) {
            // Rebuild translation pack cache if there are changes in the translations
            boolean clearTranslationPackCache =
                !existingCategory.get().getLabelTranslations().equals(category.getLabelTranslations());

            updateCategory(tagIdentifier, category);

            if (clearTranslationPackCache) {
                translationPackBuilder.clearCache();
            }
        } else {
            throw new ResourceNotFoundException(String.format(
                "Category with id '%d' does not exist.",
                tagIdentifier
            ));
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    private void updateCategory(
        int categoryIdentifier,
        final MetadataCategory category) {
        categoryRepository.update(categoryIdentifier, entity -> {
            entity.setName(category.getName());
            Map<String, String> labelTranslations = category.getLabelTranslations();
            if (labelTranslations != null) {
                entity.getLabelTranslations().clear();
                entity.getLabelTranslations().putAll(labelTranslations);
            }
        });
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Remove a tag",
        description = "")
    @RequestMapping(
        value = "/{tagIdentifier}",
        method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Tag removed."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_USER_ADMIN)
    })
    @PreAuthorize("hasAuthority('UserAdmin')")
    public ResponseEntity deleteTag(
        @Parameter(
            description = "Tag identifier",
            required = true
        )
        @PathVariable
            Integer tagIdentifier
    ) throws Exception {
        Optional<MetadataCategory> category = categoryRepository.findById(tagIdentifier);
        if (category.isPresent()) {
            long recordsCount = metadataRepository.count((Specification<Metadata>) MetadataSpecs.hasCategory(category.get()));
            if (recordsCount > 0l) {
                throw new IllegalArgumentException(String.format(
                    "Tag '%s' is assigned to %d records. Update records first in order to remove that tag.",
                    category.get().getName(), // TODO: Return in user language
                    recordsCount
                ));
            } else {
                categoryRepository.deleteById(tagIdentifier);

                translationPackBuilder.clearCache();
            }
        } else {
            throw new ResourceNotFoundException(String.format(
                "Category with id '%d' does not exist.",
                tagIdentifier
            ));
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
