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
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

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
public class TranslationApi implements ApplicationContextAware {

    @Autowired
    SchemaManager schemaManager;
    @Autowired
    LanguageUtils languageUtils;
    @Autowired
    TranslationsRepository translationsRepository;
    @Autowired
    TranslationPackBuilder translationPackBuilder;

    private ApplicationContext context;

    public synchronized void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Add or update database translations.")
    @PutMapping(value = "/db/translations/{key}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseStatus(CREATED)
    public ResponseEntity addTranslations(
        @PathVariable
        final String key,
        @Parameter(
            name = "values"
        )
        @RequestBody(required = true)
        final Map<String, String> values,
        @RequestParam(required = false)
        final boolean replace,
        ServletRequest request
    ) throws Exception {
        if (replace) {
            translationsRepository.deleteAll(
                translationsRepository.findAllByFieldName(key)
            );
        }
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
            translations.forEach(e -> {
                if (values.containsKey(e.getLangId())) {
                    e.setValue(values.get(e.getLangId()));
                }
            });
            translationsRepository.saveAll(translations);
        }
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Delete database translations.")
    @DeleteMapping(value = "/db/translations/{key}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseStatus(OK)
    public void deleteTranslations(
        @PathVariable
        final String key,
        ServletRequest request
    ) throws Exception {
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        String language = languageUtils.locale2gnCode(locale.getISO3Language());
        List<Translations> translations = translationsRepository.findAllByFieldName(key);
        if(translations.size() == 0) {
            throw new ResourceNotFoundException(String.format(
                        "Translation with key '%s' in language '%s' not found.",
                key, language));
        } else {
            translationsRepository.deleteInBatch(translations);
        }
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "List database translations (used to overrides client application translations).")
    @GetMapping(value = "/db/translations",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    public Map<String, String> getDbTranslations(
        ServletRequest request
    ) throws Exception {
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        String language = languageUtils.locale2gnCode(locale.getISO3Language());
        return translationPackBuilder.getAllDbTranslations(language);

    }


    /**
     * @param type The type of object to return.
     * @return A map of translations in JSON format.
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "List translations for database description table")
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
        String language = languageUtils.locale2gnCode(locale.getISO3Language());
        return translationPackBuilder.getDbTranslation(language, type);
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
    public Map<String, List<String>> getTranslationsPackage(
    ) throws Exception {
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
    ) throws Exception {
        final ServiceContext context = ApiUtils.createServiceContext(httpRequest);
        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        String language = languageUtils.locale2gnCode(locale.getISO3Language());
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
    public void cleanTranslationsPackagesCache(
        ServletRequest request
    ) throws Exception {
        translationPackBuilder.clearCache();
    }
}
