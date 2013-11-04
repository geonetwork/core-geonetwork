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

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Language;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.repository.LanguageRepository;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

import javax.annotation.Nonnull;

/**
 * Update the information of a category.
 */
public class Update extends NotInReadOnlyModeService {
    public void init(String appPath, ServiceConfig params) throws Exception {
        super.init(appPath, params);
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    @Override
    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        String id = params.getChildText(Params.ID);
        final String name = Util.getParam(params, Params.NAME);

        Element elRes = new Element(Jeeves.Elem.RESPONSE);
        final MetadataCategoryRepository categoryRepository = context.getBean(MetadataCategoryRepository.class);

        MetadataCategory category;
        if (id == null || "".equals(id))    // For Adding new category
        {
            category = new MetadataCategory();
            category.setName(name);

            final LanguageRepository langRepository = context.getBean(LanguageRepository.class);
            java.util.List<Language> allLanguages = langRepository.findAll();
            for (Language l : allLanguages) {
                category.getLabelTranslations().put(l.getId(), name);
            }

            categoryRepository.save(category);
            elRes.addContent(new Element(Jeeves.Elem.OPERATION).setText(Jeeves.Text.ADDED));
        } else {
            categoryRepository.update(Integer.valueOf(id), new Updater<MetadataCategory>() {
                @Override
                public void apply(@Nonnull MetadataCategory entity) {
                    entity.setName(name);
                }
            });

            elRes.addContent(new Element(Jeeves.Elem.OPERATION).setText(Jeeves.Text.UPDATED));
        }

        return elRes;
    }
}