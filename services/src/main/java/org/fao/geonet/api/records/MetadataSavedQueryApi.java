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

package org.fao.geonet.api.records;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.SavedQuery;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.api.API;
import org.fao.geonet.api.exception.NoResultsFoundException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by francois on 29/01/16.
 */
@Service
@RequestMapping(value = {
        "/api/metadata/{metadataUuid}",
        "/api/" + API.VERSION_0_1 + "/metadata/{metadataUuid}"
})
@Api(value = "metadata",
        tags= "metadata",
        description = "Metadata operations")
public class MetadataSavedQueryApi {
    private static final String LOG_MODULE = "MetadataApi";

    @Autowired
    private SchemaManager schemaManager;


    @ApiOperation(value = "List saved queries for this metadata",
                  nickname = "getMetadataSavedQueries")
    @RequestMapping(value = "/query",
                    method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public List<SavedQuery> getSavedQueries(
            @ApiParam(value = "The metadata UUID",
                      required = true,
                      example = "43d7c186-2187-4bcd-8843-41e575a5ef56")
            @PathVariable final String metadataUuid
            ) throws ResourceNotFoundException {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        MetadataRepository metadataRepository = appContext.getBean(MetadataRepository.class);

        Metadata metadata = metadataRepository.findOneByUuid(metadataUuid);
        if (metadata == null) {
            throw new ResourceNotFoundException(String.format(
                    "Metadata '%s' not found.",
                    metadataUuid));
        }

        String schemaIdentifier = metadata.getDataInfo().getSchemaId();
        SchemaPlugin schemaPlugin = schemaManager.getSchema(schemaIdentifier).getSchemaPlugin();
        if (schemaPlugin == null) {
            return new ArrayList<>();
        }
        try {
            MetadataSchema schema = schemaManager.getSchema(schemaIdentifier);
            return schema.getSchemaPlugin().getSavedQueries();
        } catch (IllegalArgumentException e) {
            return new ArrayList<>();
        }
    }


    // TODO: Api is query xpath
    @ApiOperation(value = "Apply a saved query for this metadata",
                  nickname = "getMetadataSavedQueries",
                  notes = "All parameters will be substituted to the XPath query. eg. {{protocol}} in the XPath expression will be replaced by the protocol parameter provided in the request body.")
    @RequestMapping(value = "/query/{savedQuery}",
                    method = RequestMethod.POST,
                    consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public String applyQuery(
            @ApiParam(value = "The metadata UUID",
                      required = true,
                      example = "43d7c186-2187-4bcd-8843-41e575a5ef56")
            @PathVariable final String metadataUuid,
            @ApiParam(value = "The saved query to apply",
                      required = true,
                      example = "wfs-indexing-config")
            @PathVariable final String savedQuery,
            @ApiParam(value = "The query parameters")
            @RequestBody(required = false) final HashMap<String,String> parameters) throws Exception {

        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        MetadataRepository metadataRepository = appContext.getBean(MetadataRepository.class);

        Metadata metadata = metadataRepository.findOneByUuid(metadataUuid);
        if (metadata == null) {
            throw new ResourceNotFoundException(String.format(
                    "Metadata '%s' not found.",
                    metadataUuid));
        }

        String schemaIdentifier = metadata.getDataInfo().getSchemaId();
        SchemaPlugin schemaPlugin = schemaManager.getSchema(schemaIdentifier).getSchemaPlugin();
        if (schemaPlugin == null) {
            throw new ResourceNotFoundException(String.format(
                    "Saved query '%s' for schema '%s' not found.",
                    savedQuery, schemaIdentifier));
        }

        SavedQuery query = schemaPlugin.getSavedQuery(savedQuery);
        if (query == null) {
            throw new ResourceNotFoundException(String.format(
                    "Saved query '%s' for schema '%s' not found. Available queries are '%s'.",
                    savedQuery, schemaIdentifier, schemaPlugin.getSavedQueries()));
        }


        String xpath = query.getXpath();
        if (Log.isDebugEnabled(LOG_MODULE)) {
            Log.debug(LOG_MODULE, String.format(
                    "Saved query XPath: %s", xpath));
        }
        if (parameters != null) {
            Iterator<String> parametersIterator = parameters.keySet().iterator();
            while (parametersIterator.hasNext()) {
                String parameter = parametersIterator.next();
                xpath = xpath.replaceAll("\\{\\{" + parameter + "\\}\\}", parameters.get(parameter));
            }
        }
        if (Log.isDebugEnabled(LOG_MODULE)) {
            Log.debug(LOG_MODULE, String.format(
                    "Saved query XPath after URL parameters substitution %s", xpath));
        }


        // TODO: Could return any kind of object
        // TODO: Could select multiple nodes
        try {
            final Element matchingElement =
                    (Element) Xml.selectSingle(metadata.getXmlData(false),
                            xpath,
                            new ArrayList<>(schemaPlugin.getNamespaces()));

            if (matchingElement != null) {
                return matchingElement.getText();
            }

            throw new NoResultsFoundException(String.format(
                    "No results found in metadata '%s' for query '%s'.",
                    metadataUuid, xpath));
        } catch (JDOMException e) {
            throw new IllegalArgumentException(String.format(
                    "Error in query: %s. Saved query parameters are '%s'.",
                    e.getMessage(), query.getParameters()));
        }
    }
}
