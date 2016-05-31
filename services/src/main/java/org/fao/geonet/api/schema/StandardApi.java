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

package org.fao.geonet.api.schema;

import org.fao.geonet.api.API;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.editorconfig.BatchEditing;
import org.fao.geonet.kernel.schema.editorconfig.Editor;
import org.fao.geonet.services.schema.Info;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.server.context.ServiceContext;

/**
 *
 */

@RequestMapping(value = {
    "/api/standard",
    "/api/" + API.VERSION_0_1 +
        "/standard"
})
@Api(value = "standard",
    tags = "standard",
    description = "Standard related operations")
@Controller("standard")
public class StandardApi implements ApplicationContextAware {

    @Autowired
    SchemaManager schemaManager;

    @Autowired
    LanguageUtils languageUtils;

    private ApplicationContext context;

    public synchronized void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }


    @ApiOperation(value = "Get batch editor configuration for standards",
        nickname = "getBatchConfigurations")
    @RequestMapping(value = "/actions/batchconfiguration",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    Map<String, BatchEditing> getConfigurations(
        @ApiParam(value = "Schema identifiers",
            required = false,
            example = "iso19139")
        @RequestParam(required = false)
            String[] schema
    ) throws Exception {
        List<String> listOfRequestedSchema = schema == null ? new ArrayList<String>() : Arrays.asList(schema);
        Set<String> listOfSchemas = schemaManager.getSchemas();
        Map<String, BatchEditing> schemasConfig = new HashMap<>();
        for (String schemaIdentifier : listOfSchemas) {
            if (listOfRequestedSchema.size() == 0 || listOfRequestedSchema.contains(schemaIdentifier)) {
                MetadataSchema metadataSchema = schemaManager.getSchema(schemaIdentifier);
                Editor editorConfiguration = metadataSchema.getConfigEditor();
                if (editorConfiguration != null) {
                    schemasConfig.put(schemaIdentifier,
                        editorConfiguration.getBatchEditing());
                }
            }
        }
        return schemasConfig;
    }


    @ApiOperation(value = "Get batch editor configuration for a standard",
        nickname = "getBatchConfiguration")
    @RequestMapping(value = "/{schema}/actions/batchconfiguration",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    Map<String, BatchEditing> getConfiguration(
        @ApiParam(value = "Schema identifier",
            required = true,
            example = "iso19139")
        @PathVariable
            String schema
    ) throws Exception {
        Map<String, BatchEditing> schemasConfig = new HashMap<>();
        MetadataSchema metadataSchema = schemaManager.getSchema(schema);
        Editor editorConfiguration = metadataSchema.getConfigEditor();
        if (editorConfiguration != null) {
            schemasConfig.put(schema,
                editorConfiguration.getBatchEditing());
        }
        return schemasConfig;
    }


    @ApiOperation(value = "List a codelist entries",
        nickname = "getSchemaTranslations")
    @RequestMapping(value = "/{schema}/codelists/{codelist}",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    public Map<String, String> getSchemaTranslations(
        @ApiParam(value = "Schema identifier",
            required = true,
            example = "iso19139")
        @PathVariable String schema,
        @PathVariable String codelist,
        @RequestParam(required = false) String parent,
        @RequestParam(required = false) String xpath,
        @RequestParam(required = false) String isoType,
        ServletRequest request
    ) throws Exception {
        Map<String, String> response = new LinkedHashMap<String, String>();
        final ServiceContext context = ServiceContext.get();
        Locale language = languageUtils.parseAcceptLanguage(request.getLocales());
        context.setLanguage(language.getISO3Language());

        String elementName = Info.findNamespace(codelist, schemaManager, schema);
        Element e = Info.getHelp(schemaManager, "codelists.xml",
            schema, elementName, parent, xpath, isoType, context);
        if (e == null) {
            throw new ResourceNotFoundException(String.format(
                "'%s' not found.", codelist));
        }
        List<Element> listOfEntry = e.getChildren("entry");
        for (Element entry : listOfEntry) {
            response.put(entry.getChildText("code"), entry.getChildText("label"));
        }
        return response;
    }
}
