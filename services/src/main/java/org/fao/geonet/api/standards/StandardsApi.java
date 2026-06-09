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

package org.fao.geonet.api.standards;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.exception.WebApplicationException;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.editorconfig.BatchEditing;
import org.fao.geonet.kernel.schema.editorconfig.Editor;
import org.fao.geonet.kernel.schema.labels.Codelists;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.util.UserUtil;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 *
 */

@RequestMapping(value = {
    "/{portal}/api/standards"
})
@Tag(name = "standards",
    description = "Standard related operations")
@Controller("standards")
public class StandardsApi implements ApplicationContextAware {

    @Autowired
    SchemaManager schemaManager;

    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    SettingManager settingManager;

    @Autowired
    RoleHierarchy roleHierarchy;

    private ApplicationContext context;

    public synchronized void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }


    @io.swagger.v3.oas.annotations.Operation(summary = "Get standards")
    @RequestMapping(
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of standards.")
    })
    List<MetadataSchema> getStandardConfigurations() throws Exception {
        Set<String> schemaIds = schemaManager.getSchemas();
        List<MetadataSchema> schemaList = new ArrayList<>(schemaIds.size());
        schemaIds.stream().forEach(id -> schemaList.add(schemaManager.getSchema(id)));
        return schemaList;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Reload standards")
    @RequestMapping(
        value = "/reload",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Standards reloaded.")
    })
    void reloadSchema() throws Exception {
        Set<String> schemaIds = schemaManager.getSchemas();
        schemaIds.stream().forEach(id -> schemaManager.reloadSchema(id));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Get batch editor configuration for standards")
    @RequestMapping(value = "/batchconfiguration",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Batch editor configuration.")
    })
    public
    @ResponseBody
    Map<String, BatchEditing> getConfigurations(
        @Parameter(description = ApiParams.API_PARAM_SCHEMA_IDENTIFIERS,
            required = false,
            example = "iso19139")
        @RequestParam(required = false)
            String[] schema,
        HttpServletRequest request
    ) throws Exception {

        ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        UserUtil.checkUserProfileLevel(serviceContext.getUserSession(), settingManager, roleHierarchy, Settings.METADATA_BATCH_EDITING_ACCESS_LEVEL, Profile.Editor, "batch edit metadata");

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


    @io.swagger.v3.oas.annotations.Operation(summary = "Get batch editor configuration for a standard")
    @RequestMapping(value = "/{schema}/batchconfiguration",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    Map<String, BatchEditing> getConfiguration(
        @Parameter(description = "Schema identifier",
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


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get multiple codelist translations",
        description = "All translations are combined in the same object. " +
            "No distinction by codelist. This is useful if you need " +
            "lots of codelists terms to be loaded.")
    @RequestMapping(value = "/{schema}/codelists",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    public Map<String, String> getCodelistsTranslations(
        @Parameter(description = "Schema identifier",
            required = true,
            example = "iso19139")
        @PathVariable String schema,
        @Parameter(
            description = "Codelist element name or alias"
        )
        @RequestParam String[] codelist,
        HttpServletRequest request
    ) throws Exception {
        Map<String, String> response = new LinkedHashMap<String, String>();
        final ServiceContext context = ApiUtils.createServiceContext(request);
        Locale language = languageUtils.parseAcceptLanguage(request.getLocales());
        context.setLanguage(language.getISO3Language());

        for (String c : codelist) {
            Element e = StandardsUtils.getCodelist(c, schemaManager,
                schema, null, null, null, context, null);

            List<Element> listOfEntry = e.getChildren("entry");
            for (Element entry : listOfEntry) {
                response.put(entry.getChildText("code"), entry.getChildText("label"));
            }
        }
        return response;
    }


    @io.swagger.v3.oas.annotations.Operation(summary = "Get codelist translations")
    @RequestMapping(value = "/{schema}/codelists/{codelist}",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    public Map<String, String> getSchemaTranslations(
        @Parameter(description = "Schema identifier",
            required = true,
            example = "iso19139")
        @PathVariable String schema,
        @Parameter(
            description = "Codelist element name or alias"
        )
        @PathVariable String codelist,
        @RequestParam(required = false) String parent,
        @RequestParam(required = false) String displayIf,
        @RequestParam(required = false) String xpath,
        @RequestParam(required = false) String isoType,
        HttpServletRequest request
    ) throws Exception {
        Map<String, String> response = new LinkedHashMap<String, String>();
        final ServiceContext context = ApiUtils.createServiceContext(request);
        Locale language = languageUtils.parseAcceptLanguage(request.getLocales());
        context.setLanguage(language.getISO3Language());

        Element e = StandardsUtils.getCodelist(codelist, schemaManager,
            schema, parent, xpath, isoType, context, displayIf);

        List<Element> listOfEntry = e.getChildren("entry");
        for (Element entry : listOfEntry) {
            response.put(entry.getChildText("code"), entry.getChildText("label"));
        }
        return response;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Get codelist details")
    @RequestMapping(value = "/{schema}/codelists/{codelist}/details",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE
        })
    @ResponseBody
    public Codelists.Codelist getSchemaCodelistsWithDetails(
        @Parameter(description = "Schema identifier",
            required = true,
            example = "iso19139")
        @PathVariable String schema,
        @Parameter(
            description = "Codelist element name or alias"
        )
        @PathVariable String codelist,
        @Parameter(
            description = "Parent name with namespace which may indicate a more precise label as defined in context attribute."
        )
        @RequestParam(required = false) String parent,
        @Parameter(
            description = "Display if condition as defined in the codelist.xml file. Allows to select a more precise codelist when more than one is defined for same name."
        )
        @RequestParam(required = false) String displayIf,
        @Parameter(
            description = "XPath of the element to target which may indicate a more precise label as defined in context attribute."
        )
        @RequestParam(required = false) String xpath,
        @Parameter(
            description = "ISO type of the element to target which may indicate a more precise label as defined in context attribute. (Same as context. TODO: Deprecate ?)"
        )
        @RequestParam(required = false) String isoType,
        HttpServletRequest request
    ) throws Exception {
        final ServiceContext context = ApiUtils.createServiceContext(request);
        Locale language = languageUtils.parseAcceptLanguage(request.getLocales());
        context.setLanguage(language.getISO3Language());

        Element e = StandardsUtils.getCodelist(codelist, schemaManager,
            schema, parent, xpath, isoType, context, displayIf);

        return (Codelists.Codelist) Xml.unmarshall(e, Codelists.Codelist.class);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Get descriptor details")
    @RequestMapping(value = "/{schema}/descriptors/{element}/details",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE
        })
    @ResponseBody
    public org.fao.geonet.kernel.schema.labels.Element getElementDetails(
        @Parameter(description = "Schema identifier",
            required = true,
            example = "iso19139")
        @PathVariable String schema,
        @Parameter(
            description = "Descriptor name",
            required = true
        )
        @PathVariable String element,
        @RequestParam(required = false) String parent,
        @RequestParam(required = false) String displayIf,
        @RequestParam(required = false) String xpath,
        @RequestParam(required = false) String isoType,
        HttpServletRequest request
    ) throws Exception {
        final ServiceContext context = ApiUtils.createServiceContext(request);
        Locale language = languageUtils.parseAcceptLanguage(request.getLocales());
        context.setLanguage(language.getISO3Language());

        Element e = StandardsUtils.getLabel(element, schemaManager,
            schema, parent, xpath, isoType, displayIf, context);

        return (org.fao.geonet.kernel.schema.labels.Element) Xml.unmarshall(e, org.fao.geonet.kernel.schema.labels.Element.class);
    }


    @io.swagger.v3.oas.annotations.Operation(summary = "Get editor associated resources panel configuration")
    @RequestMapping(value = "/{schema}/editor/associatedpanel/config/{name:[a-zA-Z]+}.json",
        method = RequestMethod.GET,
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseBody
    public String getEditorAssociatedPanelConfiguration(
        @Parameter(description = "Schema identifier",
            required = true,
            example = "iso19139")
        @PathVariable String schema,
        @Parameter(description = "Configuration identifier",
            required = true,
            example = "default")
        @PathVariable String name
    ) throws Exception {
        // Store processed schemas to avoid loops
        Set<String> schemasProcessed = new HashSet<>();

        while (StringUtils.isNotEmpty(schema) &&
            !schemasProcessed.contains(schema)) {

            schemasProcessed.add(schema);

            MetadataSchema metadataSchema = schemaManager.getSchema(schema);

            Path schemaDir = metadataSchema.getSchemaDir();

            Path configFile = schemaDir.resolve("config").
                resolve("associated-panel").
                resolve(name + ".json");

            if (Files.exists(configFile)) {
                try {
                    String jsonConfig = new String(Files.readAllBytes(configFile));

                    // Parse JSON file to check is valid
                    new JSONObject(jsonConfig);
                    return jsonConfig;
                } catch (Exception e) {
                    throw new WebApplicationException(String.format(
                        "Associated panel configuration '%s' for schema '%s' is invalid. Error is: %s",
                        name, metadataSchema.getName(), e.getMessage()));
                }
            } else {
                // Use the file from dependent schema if available
                schema = metadataSchema.getDependsOn();
            }
        }

        throw new ResourceNotFoundException(String.format(
            "Associated panel '%s' configuration not found for schema and its dependency '%s'.",
            name, schemasProcessed.toString()));

    }

}
