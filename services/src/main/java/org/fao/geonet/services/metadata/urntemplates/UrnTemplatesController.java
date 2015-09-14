package org.fao.geonet.services.metadata.urntemplates;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.*;
import org.fao.geonet.domain.responses.OkResponse;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataUrnTemplateSpecs;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Controller for Metadata URN templates.
 *
 * @author Jose García
 */
@Controller("urnTemplates")
public class UrnTemplatesController {
    @RequestMapping(value = "/{lang}/urntemplate", method = RequestMethod.POST)
    @ResponseBody
    public OkResponse updateUrnTemplate(
            @RequestParam final Integer id,
            @RequestParam final String name,
            @RequestParam final String template) {

        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        MetadataUrnTemplateRepository metadataUrnTemplateRepository = appContext.getBean(MetadataUrnTemplateRepository.class);

        if (id == null) {
            // Create a new metadata urn template
            MetadataUrnTemplate metadataUrnTemplate = new MetadataUrnTemplate();
            metadataUrnTemplate.setName(name);
            metadataUrnTemplate.setTemplate(template);

            metadataUrnTemplateRepository.save(metadataUrnTemplate);

        } else {
            metadataUrnTemplateRepository.update(id, new Updater<MetadataUrnTemplate>() {
                @Override
                public void apply(@Nonnull MetadataUrnTemplate entity) {
                    entity.setName(name);
                    entity.setTemplate(template);
                }
            });

        }

        return new OkResponse();
    }

    @RequestMapping(value = "/{lang}/urntemplate",
            method = RequestMethod.DELETE)
    @ResponseBody
    public OkResponse deleteMetadataURNTemplate(@RequestParam final Integer id) {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        MetadataUrnTemplateRepository metadataUrnTemplateRepository = appContext.getBean(MetadataUrnTemplateRepository.class);

        metadataUrnTemplateRepository.delete(id);

        return new OkResponse();
    }

    @RequestMapping(value = "/{lang}/urntemplate",
            method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public List<MetadataUrnTemplate> retrieveMetadataURNTemplates(@RequestParam(required = false, defaultValue = "false") final Boolean userDefinedOnly) {

        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        MetadataUrnTemplateRepository metadataUrnTemplateRepository = appContext.getBean(MetadataUrnTemplateRepository.class);

        //MetadataUrnTemplateListResponse response = new MetadataUrnTemplateListResponse();
        //response.addAll(metadataUrnTemplateRepository.findAll());

        if (userDefinedOnly) {
            return metadataUrnTemplateRepository.findAll(MetadataUrnTemplateSpecs.isDefault(false));

        } else {
            return metadataUrnTemplateRepository.findAll();

        }
    }
}