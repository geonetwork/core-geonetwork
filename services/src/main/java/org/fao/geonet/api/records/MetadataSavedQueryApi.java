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

import io.swagger.annotations.*;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.NoResultsFoundException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.SavedQuery;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;

/**
 * Created by francois on 29/01/16.
 */
@Service
@RequestMapping(value = {
    "/{portal}/api/records/{metadataUuid}",
    "/{portal}/api/" + API.VERSION_0_1 + "/records/{metadataUuid}"
})
@Api(value = API_CLASS_RECORD_TAG,
    tags = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
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
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Saved query available."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    public List<SavedQuery> getSavedQueries(
        @ApiParam(
            value = ApiParams.API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable final String metadataUuid,
        HttpServletRequest request
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canViewRecord(metadataUuid, request);
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
    @ApiOperation(
        value = "Apply a saved query for this metadata",
        nickname = "applyQuery",
        notes = "All parameters will be substituted to the XPath query. " +
            "eg. {{protocol}} in the XPath expression will be replaced by " +
            "the protocol parameter provided in the request body.")
    @RequestMapping(
        value = "/query/{savedQuery}",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of matching elements. " +
            "If element are nodes, then they are returned as string."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    public Map<String, String> applyQuery(
        @ApiParam(value = "The metadata UUID",
            required = true,
            example = "43d7c186-2187-4bcd-8843-41e575a5ef56")
        @PathVariable final String metadataUuid,
        @ApiParam(value = "The saved query to apply",
            required = true,
            example = "wfs-indexing-config")
        @PathVariable final String savedQuery,
        HttpServletRequest request,
        @ApiParam(value = "The query parameters")
        @RequestBody(required = false) final HashMap<String, String> parameters) throws Exception {

        AbstractMetadata metadata = ApiUtils.canViewRecord(metadataUuid, request);

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


        Map<String, String> response = new HashMap<>();
        try {
            ArrayList<Namespace> nss = new ArrayList<>(schemaPlugin.getNamespaces());
            final List<?> matchingElement = Xml.selectNodes(metadata.getXmlData(false),
                xpath,
                nss);
            int counter = 0;
            String queryCleanValues = query.getCleanValues();
            if (matchingElement != null) {
                for (Object o : matchingElement) {
                    String key = String.valueOf(counter), value = null;
                    if (o instanceof Element) {
                        Element e = (Element) o;
                        if (query.getLabel() != null) {
                            String label = Xml.selectString(e, query.getLabel(), nss);
                            if (label != null) {
                                key = label;
                            }
                        }


                        if (queryCleanValues != null) {
                            final List<?> valuesToClean = Xml.selectNodes(e,
                                queryCleanValues,
                                nss);
                            if (valuesToClean != null) {
                                for (Object v : valuesToClean) {
                                    if (v instanceof Element) {
                                        ((Element) v).setText("");
                                    } else if (v instanceof Attribute) {
                                        ((Attribute) v).setValue("");
                                    } else if (v instanceof Text) {
                                        ((Text) v).setText("");
                                    }
                                }
                            }
                        }

                        value = Xml.getString(e);
                    } else if (o instanceof Attribute) {
                        value = ((Attribute) o).getValue();
                    } else if (o instanceof Text) {
                        value = ((Text) o).getText();
                    }

                    response.put(key, value);
                    counter++;
                }
            }
            if (response.size() > 0) {
                return response;
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
