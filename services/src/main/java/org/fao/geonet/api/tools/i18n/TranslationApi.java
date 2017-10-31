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
//==============================================================================
//===	Copyright (C) 2001-2015 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.IsoLanguage;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.Operation;
import org.fao.geonet.domain.Schematron;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.IsoLanguageRepository;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.OperationRepository;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.StatusValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 *
 */

@RequestMapping(value = {
    "/api/tools/i18n",
    "/api/" + API.VERSION_0_1 +
        "/tools/i18n"
})
@Api(value = "tools",
    tags = "tools")
@Controller("translation")
public class TranslationApi implements ApplicationContextAware {

    private static final List<String> TRANSLATION_TABLES = Arrays.asList(new String[]{
        "StatusValue", "MetadataCategory", "Group", "Operation",
        "Source", "Schematron", "IsoLanguage"
    });
    @Autowired
    SchemaManager schemaManager;
    @Autowired
    LanguageUtils languageUtils;
    private ApplicationContext context;

    public synchronized void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    /**
     * @param type The type of object to return.
     * @return A map of translations in JSON format.
     */
    @ApiOperation(value = "List translations for database description table",
        nickname = "getTranslations")
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
        String language = languageUtils.locale2gnCode(locale.getISO3Language());

        if (type == null || type.contains("StatusValue")) {
            StatusValueRepository repository =
                applicationContext.getBean(StatusValueRepository.class);
            List<StatusValue> valueList = repository.findAll();
            Iterator<StatusValue> valueIterator = valueList.iterator();
            while (valueIterator.hasNext()) {
                StatusValue entity = valueIterator.next();
                response.put("status-" + entity.getId() + "", entity.getLabel(language));
            }
        }

        if (type == null || type.contains("MetadataCategory")) {
            MetadataCategoryRepository categoryRepository =
                applicationContext.getBean(MetadataCategoryRepository.class);
            List<MetadataCategory> metadataCategoryList = categoryRepository.findAll();
            Iterator<MetadataCategory> metadataCategoryIterator = metadataCategoryList.iterator();
            while (metadataCategoryIterator.hasNext()) {
                MetadataCategory entity = metadataCategoryIterator.next();
                response.put("cat-" + entity.getName() + "", entity.getLabel(language));
            }
        }

        if (type == null || type.contains("Group")) {
            GroupRepository groupRepository =
                applicationContext.getBean(GroupRepository.class);
            List<Group> groupList = groupRepository.findAll();
            Iterator<Group> groupIterator = groupList.iterator();
            while (groupIterator.hasNext()) {
                Group entity = groupIterator.next();
                response.put("group-" + entity.getId() + "", entity.getLabel(language));
            }
        }

        if (type == null || type.contains("Operation")) {
            OperationRepository operationRepository =
                applicationContext.getBean(OperationRepository.class);
            List<Operation> operationList = operationRepository.findAll();
            Iterator<Operation> operationIterator = operationList.iterator();
            while (operationIterator.hasNext()) {
                Operation entity = operationIterator.next();
                response.put("op-" + entity.getId() + "", entity.getLabel(language));
                response.put("op-" + entity.getName() + "", entity.getLabel(language));
            }
        }

        if (type == null || type.contains("Source")) {
            SourceRepository sourceRepository =
                applicationContext.getBean(SourceRepository.class);
            List<Source> sourceList = sourceRepository.findAll();
            Iterator<Source> sourceIterator = sourceList.iterator();
            while (sourceIterator.hasNext()) {
                Source entity = sourceIterator.next();
                response.put("source-" + entity.getUuid() + "", entity.getLabel(language));
            }
        }

        if (type == null || type.contains("Schematron")) {
            SchematronRepository schematronRepository =
                applicationContext.getBean(SchematronRepository.class);
            List<Schematron> schematronList = schematronRepository.findAll();
            Iterator<Schematron> schematronIterator = schematronList.iterator();
            while (schematronIterator.hasNext()) {
                Schematron entity = schematronIterator.next();
                response.put("sch-" + entity.getRuleName() + "", entity.getLabel(language));
            }
        }

        if (type == null || type.contains("IsoLanguage")) {
            IsoLanguageRepository isoLanguageRepository =
                applicationContext.getBean(IsoLanguageRepository.class);
            List<IsoLanguage> isoLanguageList = isoLanguageRepository.findAll();
            Iterator<IsoLanguage> isoLanguageIterator = isoLanguageList.iterator();
            while (isoLanguageIterator.hasNext()) {
                IsoLanguage entity = isoLanguageIterator.next();
                response.put("lang-" + entity.getCode() + "", entity.getLabel(language));
            }
        }
        return response;
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
