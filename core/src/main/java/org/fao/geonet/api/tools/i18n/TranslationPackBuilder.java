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

import com.fasterxml.jackson.databind.ObjectMapper;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.standards.StandardsUtils;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.repository.*;
import org.fao.geonet.util.XslUtil;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class TranslationPackBuilder {
    @Autowired
    TranslationPackBuilder self; // For cache to work.

    @Autowired
    GeonetworkDataDirectory dataDirectory;
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
    @Autowired
    SchemaManager schemaManager;

    Path jsonLocaleDirectory;

    @Resource(name = "translationPacks")
    private Map<String, List<String>> packages;

    private static final String TYPE_SEPARATOR = "/";
    private static final String LIST_SEPARATOR = "\\+";

    private static final List<String> TRANSLATION_TABLES =
        Arrays.asList(new String[]{
        "StatusValue", "MetadataCategory", "Group", "Operation",
        "Source", "Schematron", "IsoLanguage", "Translations"
    });

    public TranslationPackBuilder() {
    }

    public Map<String, List<String>> getPackages() {
        return packages;
    }

    public void setPackages(Map<String, List<String>> packages) {
        this.packages = packages;
    }

    @PostConstruct
    public void init() {
        jsonLocaleDirectory =
            dataDirectory.getWebappDir().endsWith("src/main/webapp")
                // Dev mode
                ? dataDirectory.getWebappDir()
                // web/src/main/webapp
                .getParent().getParent().getParent().getParent()
                .resolve("web-ui/src/main/resources/catalog/locales")
                : dataDirectory.getWebappDir().resolve("catalog").resolve("locales");
    }

    @Cacheable(
        value = "translations",
        cacheManager = "cacheManager",
        key = "{#language, #key}")
    public Map<String, String> getPack(
        String language, String key,
        ServiceContext context) {
        Map<String, String> translations = new HashMap<>();
        if (packages.get(key) != null) {
            packages.get(key).forEach(resource -> {
                String[] config = resource.split(TYPE_SEPARATOR);
                if (config[0].equals(TranslationType.json.name())) {
                    // json/core+search+v4
                    String[] fileKeys = config[1].split(LIST_SEPARATOR);
                    translations.putAll(
                        self.getJsonTranslation(language, Arrays.asList(fileKeys))
                    );
                } else if (config[0].equals(TranslationType.db.name())) {
                    // db/MetadataCategory+Operation+Group+StatusValue+Source
                    String[] fileKeys = config[1].split(LIST_SEPARATOR);
                    translations.putAll(
                        self.getDbTranslation(language, Arrays.asList(fileKeys))
                    );
                } else if (config[0].equals(TranslationType.standards.name())
                           && config.length == 4) {
                    // standards/iso19115-3.2018/codelists/cit:CI_DateTypeCode+...
                    String[] codelistKeys = config[3].split(LIST_SEPARATOR);
                    translations.putAll(
                        self.getStandardCodelist(
                            language, config[1], Arrays.asList(codelistKeys), context)
                    );
                } else {
                    throw new IllegalArgumentException(
                        String.format(
                            "Package resource type '%s' in package '%s' is not supported. " +
                                "Choose one of %s.",
                            config[0], key,
                            Arrays.stream(TranslationType.values())
                                .map(v -> v.name())
                                .collect(Collectors.joining(", "))));
                }
            });
        } else {
            throw new IllegalArgumentException(
                String.format(
                    "Package '%s' not found. Choose one of %s or add '%s' to the config-spring-geonetwork.xml translationPacks config.",
                    key, packages.keySet().toString(), key));
        }

        // TODO: Add fallback language mechanism.
        // TODO: Report missing translations.
        TreeMap<String, String> orderedTranslations = new TreeMap<>();
        orderedTranslations.putAll(translations);
        return orderedTranslations;
    }

    @Cacheable(value = "translations",
               cacheManager = "cacheManager",
               key = "{#schema, #language, #codelist}")
    public Map<String, String> getStandardCodelist(
        String language, String schema, List<String> codelist,
        ServiceContext context) {
        Map<String, String> translations = new HashMap<>();
        context.setLanguage(language);

        for (String c : codelist) {
            Element e = null;
            try {
                e = StandardsUtils.getCodelist(c, schemaManager,
                    schema, null, null, null, context, null);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            List<Element> listOfEntry = e.getChildren("entry");
            for (Element entry : listOfEntry) {
                translations.put(entry.getChildText("code"), entry.getChildText("label"));
            }
        }
        return translations;
    }

    @Cacheable(value = "translations", cacheManager = "cacheManager")
    public Map<String, String> getDbTranslation(String language, List<String> type) {
        Map<String, String> translations = new HashMap<>();

        validateDbType(type);

        if (type == null || type.contains("StatusValue")) {
            List<StatusValue> valueList = statusValueRepository.findAll();
            Iterator<StatusValue> valueIterator = valueList.iterator();
            while (valueIterator.hasNext()) {
                StatusValue entity = valueIterator.next();
                translations.put("status-" + entity.getId() + "",
                    getLabelOrKey(entity, language, entity.getId() + ""));
            }
        }

        if (type == null || type.contains("MetadataCategory")) {
            List<MetadataCategory> metadataCategoryList = categoryRepository.findAll();
            Iterator<MetadataCategory> metadataCategoryIterator = metadataCategoryList.iterator();
            while (metadataCategoryIterator.hasNext()) {
                MetadataCategory entity = metadataCategoryIterator.next();
                translations.put("cat-" + entity.getName() + "",
                    getLabelOrKey(entity, language, entity.getName()));
            }
        }

        if (type == null || type.contains("Group")) {
            List<Group> groupList = groupRepository.findAll();
            Iterator<Group> groupIterator = groupList.iterator();
            while (groupIterator.hasNext()) {
                Group entity = groupIterator.next();
                translations.put("group-" + entity.getId() + "",
                    getLabelOrKey(entity, language, entity.getName()));
            }
        }

        if (type == null || type.contains("Operation")) {
            List<Operation> operationList = operationRepository.findAll();
            Iterator<Operation> operationIterator = operationList.iterator();
            while (operationIterator.hasNext()) {
                Operation entity = operationIterator.next();
                translations.put("op-" + entity.getId() + "",
                    getLabelOrKey(entity, language, entity.getId() + ""));
                translations.put("op-" + entity.getName() + "",
                    getLabelOrKey(entity, language, entity.getName()));
            }
        }

        if (type == null || type.contains("Source")) {
            List<Source> sourceList = sourceRepository.findAll();
            Iterator<Source> sourceIterator = sourceList.iterator();
            while (sourceIterator.hasNext()) {
                Source entity = sourceIterator.next();
                translations.put("source-" + entity.getUuid() + "",
                    getLabelOrKey(entity, language, entity.getUuid()));
            }
        }

        if (type == null || type.contains("Schematron")) {
            List<Schematron> schematronList = schematronRepository.findAll();
            Iterator<Schematron> schematronIterator = schematronList.iterator();
            while (schematronIterator.hasNext()) {
                Schematron entity = schematronIterator.next();
                translations.put("sch-" + entity.getRuleName() + "",
                    getLabelOrKey(entity, language, entity.getRuleName()));
            }
        }

        if (type == null || type.contains("IsoLanguage")) {
            List<IsoLanguage> isoLanguageList = isoLanguageRepository.findAll();
            Iterator<IsoLanguage> isoLanguageIterator = isoLanguageList.iterator();
            while (isoLanguageIterator.hasNext()) {
                IsoLanguage entity = isoLanguageIterator.next();
                translations.put("lang-" + entity.getCode() + "",
                    getLabelOrKey(entity, language, entity.getCode()));
            }
        }

        if (type == null || type.contains("Translations")) {
            translations.putAll(getAllDbTranslations(language));
        }

        return translations;
    }

    @Cacheable(value = "translations", cacheManager = "cacheManager")
    public Map<String, String> getJsonTranslation(
        String language,
        List<String> fileNameKeys) {
        Map<String, String> translations = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        String iso2letterLangCode = XslUtil.twoCharLangCode(language, "eng");

        fileNameKeys.forEach(key -> {
            if (key.equals("schemas")) {
                schemaManager.getSchemas().forEach(s -> {
                    String filename = String.format(
                        "/META-INF/catalog/locales/%s-schema-%s.json",
                        iso2letterLangCode, s);
                    ClassPathResource resource = new ClassPathResource(filename);
                    if(resource.exists()) {
                        try (InputStream stream = resource.getInputStream()){
                            translations.putAll(
                                mapper.readValue(stream, Map.class)
                            );
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                });
            } else {
                String filename = String.format("%s-%s.json", iso2letterLangCode, key);
                Path jsonFile = jsonLocaleDirectory.resolve(filename);
                if (jsonFile.toFile().exists()) {
                    try {
                        translations.putAll(
                            mapper.readValue(jsonFile.toFile(), Map.class)
                        );
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } else {
                    // Not existing file in package.
                }
            }
        });
        return translations;
    }

    @CacheEvict(value = "translations", cacheManager = "cacheManager", allEntries = true)
    public void clearCache() {
    }


    public Map<String, String> getAllDbTranslations(String language) {
        Map<String, String> translations = new HashMap<>();
        List<Translations> translationsList = translationsRepository.findAllByLangId(language);
        Iterator<Translations> translationsIterator = translationsList.iterator();
        while (translationsIterator.hasNext()) {
            Translations entity = translationsIterator.next();
            translations.put(entity.getFieldName(),
                StringUtils.isNotEmpty(entity.getValue()) ? entity.getValue() : entity.getFieldName());
        }
        return translations;
    }

    private String getLabelOrKey(Localized entity, String language, String defaultValue) {
        String value = entity.getLabel(language);
        return value != null ? value : defaultValue;
    }

    private void validateDbType(List<String> type) {
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
