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

import java.util.List;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;

import jeeves.services.ReadWriteController;

/**
 * Removes a category from the system.
 */
@Controller("admin.category.remove")
@ReadWriteController
@Deprecated
public class Remove {
    @RequestMapping(value = "/{portal}/{lang}/admin.category.remove", produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public CategoryUpdateResponse exec(
        @RequestParam Integer id
    ) throws Exception {
        if (id == null) {
            throw new IllegalArgumentException("parameter id is required");
        }


        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        MetadataCategoryRepository categoryRepository = appContext.getBean(MetadataCategoryRepository.class);
        IMetadataUtils metadataRepository = appContext.getBean(IMetadataUtils.class);
        DataManager dataManager = appContext.getBean(DataManager.class);

        final MetadataCategory category = categoryRepository.findOne(id);
        final List<Integer> affectedMd = metadataRepository.findAllIdsBy(MetadataSpecs.hasCategory(category));

        categoryRepository.deleteCategoryAndMetadataReferences(id);
        //--- reindex affected metadata

        dataManager.indexMetadata(Lists.transform(affectedMd, Functions.toStringFunction()));

        CategoryUpdateResponse response = new CategoryUpdateResponse();
        response.addOperation(CategoryUpdateResponse.Operation.removed);
        return response;
    }
}
