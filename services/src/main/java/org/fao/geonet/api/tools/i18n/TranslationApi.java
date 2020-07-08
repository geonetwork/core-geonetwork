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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import java.util.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 *
 */
@RequestMapping(value = {
    "/{portal}/api/tools/i18n"
})
@Tag(name = "tools")
@RestController
public class TranslationApi implements ApplicationContextAware {

    private static final List<String> TRANSLATION_TABLES = Arrays.asList(new String[]{
        "StatusValue", "MetadataCategory", "Group", "Operation",
        "Source", "Schematron", "IsoLanguage", "Translations"
    });

    @Autowired
    SchemaManager schemaManager;
    @Autowired
    LanguageUtils languageUtils;
    @Autowired
    StatusValueRepository statusValueRepository;
    @Autowired
    MetadataCategoryRepository categoryRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    OperationRepository operationRepository;
    @Autowired
    SourceRepository sourceRepository;
    @Autowired
    SchematronRepository schematronRepository;
    @Autowired
    IsoLanguageRepository isoLanguageRepository;
    @Autowired
    TranslationsRepository translationsRepository;

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
        Map<String, String> response = new LinkedHashMap<String, String>();

        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        String language = languageUtils.locale2gnCode(locale.getISO3Language());

        getAllDbTranslations(response, language);

        return response;
    }

    private void getAllDbTranslations(Map<String, String> response, String language) {
        List<Translations> translationsList = translationsRepository.findAllByLangId(language);
        Iterator<Translations> translationsIterator = translationsList.iterator();
        while (translationsIterator.hasNext()) {
            Translations entity = translationsIterator.next();
            response.put(entity.getFieldName(),
                StringUtils.isNotEmpty(entity.getValue()) ? entity.getValue() : entity.getFieldName());
        }
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
        Map<String, String> response = new LinkedHashMap<String, String>();
        final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();

        validateParameters(type);

        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());
        String language = LanguageUtils.locale2gnCode(locale.getISO3Language());

        if (type == null || type.contains("StatusValue")) {
            List<StatusValue> valueList = statusValueRepository.findAll();
            Iterator<StatusValue> valueIterator = valueList.iterator();
            while (valueIterator.hasNext()) {
                StatusValue entity = valueIterator.next();
                response.put("status-" + entity.getId() + "",
                    getLabelOrKey(entity, language, entity.getId() + ""));
            }
        }

        if (type == null || type.contains("MetadataCategory")) {
            List<MetadataCategory> metadataCategoryList = categoryRepository.findAll();
            Iterator<MetadataCategory> metadataCategoryIterator = metadataCategoryList.iterator();
            while (metadataCategoryIterator.hasNext()) {
                MetadataCategory entity = metadataCategoryIterator.next();
                response.put("cat-" + entity.getName() + "",
                    getLabelOrKey(entity, language, entity.getName()));
            }
        }

        if (type == null || type.contains("Group")) {
            List<Group> groupList = groupRepository.findAll();
            Iterator<Group> groupIterator = groupList.iterator();
            while (groupIterator.hasNext()) {
                Group entity = groupIterator.next();
                response.put("group-" + entity.getId() + "",
                    getLabelOrKey(entity, language, entity.getName()));
            }
        }

        if (type == null || type.contains("Operation")) {
            List<Operation> operationList = operationRepository.findAll();
            Iterator<Operation> operationIterator = operationList.iterator();
            while (operationIterator.hasNext()) {
                Operation entity = operationIterator.next();
                response.put("op-" + entity.getId() + "",
                    getLabelOrKey(entity, language, entity.getId() + ""));
                response.put("op-" + entity.getName() + "",
                    getLabelOrKey(entity, language, entity.getName()));
            }
        }

        if (type == null || type.contains("Source")) {
            List<Source> sourceList = sourceRepository.findAll();
            Iterator<Source> sourceIterator = sourceList.iterator();
            while (sourceIterator.hasNext()) {
                Source entity = sourceIterator.next();
                response.put("source-" + entity.getUuid() + "",
                    getLabelOrKey(entity, language, entity.getUuid()));
            }
        }

        if (type == null || type.contains("Schematron")) {
            List<Schematron> schematronList = schematronRepository.findAll();
            Iterator<Schematron> schematronIterator = schematronList.iterator();
            while (schematronIterator.hasNext()) {
                Schematron entity = schematronIterator.next();
                response.put("sch-" + entity.getRuleName() + "",
                    getLabelOrKey(entity, language, entity.getRuleName()));
            }
        }

        if (type == null || type.contains("IsoLanguage")) {
            List<IsoLanguage> isoLanguageList = isoLanguageRepository.findAll();
            Iterator<IsoLanguage> isoLanguageIterator = isoLanguageList.iterator();
            while (isoLanguageIterator.hasNext()) {
                IsoLanguage entity = isoLanguageIterator.next();
                response.put("lang-" + entity.getCode() + "",
                    getLabelOrKey(entity, language, entity.getCode()));
            }
        }

        if (type == null || type.contains("Translations")) {
            getAllDbTranslations(response, language);
        }
        return response;
    }

    private String getLabelOrKey(Localized entity, String language, String defaultValue) {
        String value = entity.getLabel(language);
        return value != null ? value : defaultValue;
    }


    private void validateParameters(List<String> type) {
        if (type != null) {
            if (type.size() == 0) {
                throw new IllegalArgumentException(
                    String.format(
                        "Empty type is not allowed. Remove the parameter or choose one or more types in %s",
                        TRANSLATION_TABLES));
            }
            for (String value : type) {
                if (!TRANSLATION_TABLES.contains(value)) {
                    throw new IllegalArgumentException(
                        String.format(
                            "Type '%s' is not allowed. Choose one or more types in %s",
                            value, TRANSLATION_TABLES));
                }
            }
        }
    }
}
