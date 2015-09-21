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
@Controller("metadataIdentifierTemplates")
public class MetadataIdentifierTemplatesController {
    @RequestMapping(value = "/{lang}/metadataIdentifierTemplates", method = RequestMethod.POST)
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

    @RequestMapping(value = "/{lang}/metadataIdentifierTemplates",
            method = RequestMethod.DELETE)
    @ResponseBody
    public OkResponse deleteMetadataURNTemplate(@RequestParam final Integer id) {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        MetadataIdentifierTemplateRepository metadataIdentifierTemplateRepository =
                appContext.getBean(MetadataIdentifierTemplateRepository.class);

        metadataIdentifierTemplateRepository.delete(id);

        return new OkResponse();
    }

    @RequestMapping(value = "/{lang}/metadataIdentifierTemplates",
            method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
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