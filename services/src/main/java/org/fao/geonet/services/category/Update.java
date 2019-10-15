//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.services.category;

import jeeves.services.ReadWriteController;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.Language;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.exceptions.MissingParameterEx;
import org.fao.geonet.repository.LanguageRepository;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.Updater;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Nonnull;

/**
 * Update the information of a category.
 */
@Controller("admin.category.update")
@ReadWriteController
@Deprecated
public class Update {

    @RequestMapping(value = "/{portal}/{lang}/admin.category.update", produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public CategoryUpdateResponse exec(
        @RequestParam final Integer id,
        @RequestParam final String name
    ) throws Exception {
        if (name == null) {
            throw new MissingParameterEx("name");
        }

        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        MetadataCategoryRepository categoryRepository = appContext.getBean(MetadataCategoryRepository.class);
        LanguageRepository langRepository = appContext.getBean(LanguageRepository.class);

        CategoryUpdateResponse response = new CategoryUpdateResponse();

        MetadataCategory category;
        if (id == null) {
            // Adding new category
            category = new MetadataCategory();
            category.setName(name);

            java.util.List<Language> allLanguages = langRepository.findAll();
            for (Language l : allLanguages) {
                category.getLabelTranslations().put(l.getId(), name);
            }

            categoryRepository.save(category);
            response.addOperation(CategoryUpdateResponse.Operation.added);
        } else {
            categoryRepository.update(id, new Updater<MetadataCategory>() {
                @Override
                public void apply(@Nonnull MetadataCategory entity) {
                    entity.setName(name);
                }
            });

            response.addOperation(CategoryUpdateResponse.Operation.updated);
        }

        return response;
    }
}
