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

package org.fao.geonet.api.tools.i18n;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 *
 */
@RequestMapping(value = {
    "/{portal}/api/i18n"
})
@Tag(name = "tools")
@RestController
public class TranslationApi {

    @Autowired
    SchemaManager schemaManager;
    @Autowired
    LanguageUtils languageUtils;
    @Autowired
    TranslationsRepository translationsRepository;
    @Autowired
    TranslationPackBuilder translationPackBuilder;
    @Autowired
    IsoLanguagesMapper isoLanguagesMapper;

    /**
     * @param type The type of object to return.
     * @return A map of translations in JSON format.
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "List translations for database description table")
    @RequestMapping(value = "/db",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    public Map<String, String> getTranslations(
        @RequestParam(required = false) final List<String> type,
        ServletRequest request
    ) throws Exception {
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        String language = isoLanguagesMapper.iso639_2T_to_iso639_2B(locale.getISO3Language());
        return translationPackBuilder.getDbTranslation(language, type);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "List custom user translations")
    @RequestMapping(value = "/db/custom",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    public List<Translations> getCustomTranslations(
        @RequestParam(required = false) final List<String> type,
        ServletRequest request
    ) throws Exception {
        final Sort sort = SortUtils.createSort(Translations_.fieldName);

        return translationsRepository.findAll(sort);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Add or update all database translations.")
    @PutMapping(value = "/db/translations",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseStatus(CREATED)
    public ResponseEntity addTranslations(
        @Parameter(
            name = "values"
        )
        @RequestBody(required = true)
        final List<Translations> values,
        @RequestParam(required = false)
        final boolean replace
    ) throws Exception {
        if (replace) {
            translationsRepository.deleteAll();
        }

        Map<String, Map<String, String>> translations = values.stream()
            .filter(Objects::nonNull)
            .collect(groupingBy(Translations::getFieldName,
                Collectors.toMap(Translations::getLangId, Translations::getValue)));

        translations.forEach((key, t) -> {
            updateTranslation(key, t);
        });
        translationPackBuilder.clearCache();
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Remove all database translations.")
    @DeleteMapping(value = "/db/translations",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseStatus(CREATED)
    public void removeAllTranslations() throws Exception {
        translationsRepository.deleteAll();
        translationPackBuilder.clearCache();
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Add or update database translations for a key.",
        description = "Database translations can be used to customize labels in the UI for different languages.")
    @PutMapping(value = "/db/translations/{key:.+}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseStatus(CREATED)
    public ResponseEntity addTranslationsFor(
        @PathVariable
        @Parameter(
            name = "key",
            description = "Untranslated key for which translations are provided."
        )
        final String key,
        @Parameter(
            name = "values"
        )
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "An object where keys are valid 3-letter language codes (e.g. `eng` or `fre`) and values are custom translations for the corresponding language.",
            content = @Content(examples = {
                @ExampleObject(value =
                    "{" +
                        "  \"eng\": \"my translation\",\n" +
                        "  \"ger\": \"meine Übersetzung\",\n" +
                        "  \"fre\": \"ma traduction\"\n" +
                        "}")
            })
        )
        @RequestBody(required = true)
        final Map<String, String> values,
        @Parameter(
            name = "replace",
            description = "Set to `true` to erase all existing translations for that key"
        )
        @RequestParam(required = false)
        final boolean replace
    ) throws Exception {
        if (replace) {
            translationsRepository.deleteAll(
                translationsRepository.findAllByFieldName(key)
            );
        }
        updateTranslation(key, values);
        translationPackBuilder.clearCache();
        return new ResponseEntity(HttpStatus.CREATED);
    }


    private void updateTranslation(String key, Map<String, String> values) {
        List<Translations> translations = translationsRepository.findAllByFieldName(key);
        if(translations.size() == 0) {
            values.forEach((l, v) -> {
                Translations t = new Translations();
                t.setLangId(l);
                t.setFieldName(key);
                t.setValue(v);
                translationsRepository.save(t);
            });
        } else {
            Set<String> addedLanguages = new HashSet<>();
            translations.forEach(e -> {
                if (values.containsKey(e.getLangId())) {
                    e.setValue(values.get(e.getLangId()));
                    addedLanguages.add(e.getLangId());
                }
            });
            values.keySet().stream().filter(l -> !addedLanguages.contains(l))
                    .forEach(l -> {
                        Translations t = new Translations();
                        t.setLangId(l);
                        t.setFieldName(key);
                        t.setValue(values.get(l));
                        translations.add(t);
                    });
            translationsRepository.saveAll(translations);
        }
        translationPackBuilder.clearCache();
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Delete database translations.",
        description = "Delete custom translations stored in the database.")
    @DeleteMapping(value = "/db/translations/{key:.+}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseStatus(OK)
    public void deleteTranslations(
        @Parameter(
            name = "key",
            description = "Untranslated key for which all translations will be deleted."
        )
        @PathVariable
        final String key,
        ServletRequest request
    ) throws Exception {
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        String language = isoLanguagesMapper.iso639_2T_to_iso639_2B(locale.getISO3Language());
        List<Translations> translations = translationsRepository.findAllByFieldName(key);
        if(translations.size() == 0) {
            throw new ResourceNotFoundException(String.format(
                        "Translation with key '%s' in language '%s' not found.",
                key, language));
        } else {
            translationsRepository.deleteInBatch(translations);
            translationPackBuilder.clearCache();
        }
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "List database translations (used to overrides client application translations).")
    @GetMapping(value = "/db/translations",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ApiResponse(
        responseCode = "200",
        content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(value =
                "{" +
                    "  \"translationKey1\": \"Translated Key One\",\n" +
                    "  \"translationKey2\": \"Translated Key Two\",\n" +
                    "  \"translationKey3\": \"Translated Key Two\"\n" +
                    "}")
        }, schema = @Schema(type = "{ < * >: string }"))
    )
    @ResponseBody
    public Map<String, String> getDbTranslations(
        ServletRequest request
    ) {
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        String language = isoLanguagesMapper.iso639_2T_to_iso639_2B(locale.getISO3Language());
        return translationPackBuilder.getAllDbTranslations(language);

    }


    /**
     * Get list of packages.
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get list of translation packages."
    )
    @RequestMapping(value = "/packages",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    public Map<String, List<String>> getTranslationsPackage() {
        return translationPackBuilder.getPackages();
    }


    /**
     * Get a translation package.
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get a translation package."
    )
    @RequestMapping(value = "/packages/{pack}",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    public Map<String, String> getTranslationsPackage(
        @PathVariable
        final String pack,
        ServletRequest request,
        HttpServletRequest httpRequest
    ) {
        final ServiceContext context = ApiUtils.createServiceContext(httpRequest);
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        String language = isoLanguagesMapper.iso639_2T_to_iso639_2B(locale.getISO3Language());
        return translationPackBuilder.getPack(language, pack, context);
    }


    /**
     * Get a translation package.
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Clean translation packages cache."
    )
    @RequestMapping(value = "/cache",
        method = RequestMethod.DELETE,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseStatus(OK)
    @ResponseBody
    public void cleanTranslationsPackagesCache() {
        translationPackBuilder.clearCache();
    }
}
