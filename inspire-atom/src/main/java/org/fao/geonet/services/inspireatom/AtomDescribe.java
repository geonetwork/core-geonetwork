//=============================================================================
//===	Copyright (C) 2001-2010 Food and Agriculture Organization of the
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
package org.fao.geonet.services.inspireatom;


import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.guiapi.search.XsltResponseWriter;
import org.fao.geonet.inspireatom.InspireAtomService;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.InspireAtomFeedRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.Util;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.inspireatom.util.InspireAtomUtil;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Path;

import static org.springframework.http.HttpStatus.OK;

/**
 * Service to get Atom feed.
 *
 * @author Jose García
 */
@RequestMapping(value = {
    "/{portal}/api/atom"
})
@Tag(name = "atom",
    description = "ATOM")
@RestController
public class AtomDescribe {

    @Autowired
    InspireAtomService service;

    @Autowired
    SettingManager sm;

    @Autowired
    DataManager dm;

    @Autowired
    InspireAtomFeedRepository inspireAtomFeedRepository;

    /**
     * Dataset identifier param name
     **/
    private final static String DATASET_IDENTIFIER_CODE_PARAM = "spatial_dataset_identifier_code";

    /**
     * Dataset namespace param name
     **/
    private final static String DATASET_IDENTIFIER_NS_PARAM = "spatial_dataset_identifier_namespace";

    /**
     * Service identifier param name
     **/
    private final static String SERVICE_IDENTIFIER = "fileIdentifier";

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Describe resource",
        description = "")
    @GetMapping(
        value = "/describe/resource",
        produces = MediaType.APPLICATION_ATOM_XML_VALUE
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feeds."),
        @ApiResponse(responseCode = "204", description = "Not authenticated.", content = {@Content(schema = @Schema(hidden = true))})
    })
    @ResponseStatus(OK)
    @ResponseBody
    public Element describeResource(
        @Parameter(
            description = "fileIdentifier",
            required = false)
        @RequestParam(defaultValue = "")
            String fileIdentifier,
        @Parameter(
            description = "spatial_dataset_identifier_code",
            required = false)
        @RequestParam(defaultValue = "")
            String spatial_dataset_identifier_code,
        @Parameter(
            description = "spatial_dataset_identifier_namespace",
            required = false)
        @RequestParam(defaultValue = "")
            String spatial_dataset_identifier_namespace,
        @Parameter(hidden = true)
            HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        boolean inspireEnable = sm.getValueAsBool(Settings.SYSTEM_INSPIRE_ENABLE);

        if (!inspireEnable) {
            Log.info(Geonet.ATOM, "Inspire is disabled");
            throw new Exception("Inspire is disabled");
        }

        if (StringUtils.isEmpty(fileIdentifier)) {
            if (StringUtils.isEmpty(spatial_dataset_identifier_code)) {
                throw new MissingServletRequestParameterException("spatial_dataset_identifier_code", "String");
            }

            if (StringUtils.isEmpty(spatial_dataset_identifier_namespace)) {
                throw new MissingServletRequestParameterException("spatial_dataset_identifier_namespace", "String");
            }
        }

        Element response =
            StringUtils.isEmpty(fileIdentifier)
                ? processDatasetFeed(spatial_dataset_identifier_code, spatial_dataset_identifier_namespace, context)
                : processServiceFeed(fileIdentifier, context);

        return new XsltResponseWriter(null, "atom-describe")
            .withXml(response)
            .withXsl("xslt/services/inspire-atom/describe.xsl")
            .asElement();
    }

    private Element processDatasetFeed(String datasetIdCode, String datasetIdNs, ServiceContext context) throws Exception {
        DataManager dm = context.getBean(DataManager.class);
        InspireAtomService service = context.getBean(InspireAtomService.class);

        Log.debug(Geonet.ATOM, "Processing dataset feed  (" + DATASET_IDENTIFIER_CODE_PARAM + ": " +
            datasetIdCode + ", " + DATASET_IDENTIFIER_NS_PARAM + ": " + datasetIdNs + " )");

        // Get metadata uuid
        String datasetUuid = service.retrieveDatasetUuidFromIdentifierNs(datasetIdCode, datasetIdNs);
        if (StringUtils.isEmpty(datasetUuid)) throw new MetadataNotFoundEx(datasetUuid);

        // Retrieve metadata to check existence and permissions.
        String id = dm.getMetadataId(datasetUuid);
        if (StringUtils.isEmpty(id)) throw new MetadataNotFoundEx(datasetUuid);

        // Check if allowed to the metadata
        Lib.resource.checkPrivilege(context, id, ReservedOperation.view);

        return service.retrieveFeed(context, Integer.parseInt(id));
    }

    private Element processServiceFeed(String fileIdentifier, ServiceContext context) throws Exception {
        Log.debug(Geonet.ATOM, "Processing service feed  (" + SERVICE_IDENTIFIER + ": " + fileIdentifier + " )");

        InspireAtomService service = context.getBean(InspireAtomService.class);

        // Retrieve metadata to check existence and permissions.
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);

        String id = dm.getMetadataId(fileIdentifier);
        if (StringUtils.isEmpty(id)) throw new MetadataNotFoundEx(fileIdentifier);

        // Check if allowed to the metadata
        Lib.resource.checkPrivilege(context, id, ReservedOperation.view);

        // Check if it is a service metadata
        Element md = dm.getMetadata(id);
        String schema = dm.getMetadataSchema(id);
        if (!InspireAtomUtil.isServiceMetadata(dm, schema, md)) {
            throw new Exception("No service metadata found with uuid:" + fileIdentifier);
        }

        return service.retrieveFeed(context, Integer.parseInt(id));
    }
}
