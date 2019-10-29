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

package org.fao.geonet.services.metadata;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.*;
import org.fao.geonet.domain.responses.OkResponse;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataIdentifierTemplateSpecs;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;

import java.util.List;

/**
 * Controller for Metadata identifier templates.
 *
 * @author Jose García
 */
@Deprecated
@Controller("metadataIdentifierTemplates")
public class MetadataIdentifierTemplatesController {
    @RequestMapping(value = "/{portal}/{lang}/metadataIdentifierTemplates", method = RequestMethod.POST)
    @ResponseBody
    public OkResponse updateUrnTemplate(
        @RequestParam final Integer id,
        @RequestParam final String name,
        @RequestParam final String template) {

        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        MetadataIdentifierTemplateRepository metadataIdentifierTemplateRepository =
            appContext.getBean(MetadataIdentifierTemplateRepository.class);

        if (id == null) {
            // Create a new metadata identifier template
            MetadataIdentifierTemplate metadataIdentifierTemplate = new MetadataIdentifierTemplate();
            metadataIdentifierTemplate.setName(name);
            metadataIdentifierTemplate.setTemplate(template);

            metadataIdentifierTemplateRepository.save(metadataIdentifierTemplate);

        } else {
            metadataIdentifierTemplateRepository.update(id, new Updater<MetadataIdentifierTemplate>() {
                @Override
                public void apply(@Nonnull MetadataIdentifierTemplate entity) {
                    entity.setName(name);
                    entity.setTemplate(template);
                }
            });

        }

        return new OkResponse();
    }

    @RequestMapping(value = "/{portal}/{lang}/metadataIdentifierTemplates",
        method = RequestMethod.DELETE)
    @ResponseBody
    public OkResponse deleteMetadataURNTemplate(@RequestParam final Integer id) {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        MetadataIdentifierTemplateRepository metadataIdentifierTemplateRepository =
            appContext.getBean(MetadataIdentifierTemplateRepository.class);

        metadataIdentifierTemplateRepository.delete(id);

        return new OkResponse();
    }

    @RequestMapping(value = "/{portal}/{lang}/metadataIdentifierTemplates",
        method = RequestMethod.GET, produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public List<MetadataIdentifierTemplate> retrieveMetadataURNTemplates(
        @RequestParam(required = false, defaultValue = "false") final Boolean userDefinedOnly) {

        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        MetadataIdentifierTemplateRepository metadataIdentifierTemplateRepository = appContext.getBean(MetadataIdentifierTemplateRepository.class);

        if (userDefinedOnly) {
            return metadataIdentifierTemplateRepository.findAll(MetadataIdentifierTemplateSpecs.isSystemProvided(false));

        } else {
            return metadataIdentifierTemplateRepository.findAll();

        }
    }
}
