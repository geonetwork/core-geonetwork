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
package org.fao.geonet.api.records.data;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.IProcessingReport;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.api.records.editing.BatchEditsApi;
import org.fao.geonet.data.GdalMetadataExtractor;
import org.fao.geonet.data.model.gdal.GdalDataset;
import org.fao.geonet.data.model.gdal.GdalField;
import org.fao.geonet.data.model.gdal.GdalLayer;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.BatchEditParameter;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fao.geonet.api.ApiParams.*;

/**
 * Data analysis
 */
@RequestMapping(value = {
        "/{portal}/api/data"
})
@Tag(name = API_CLASS_RECORD_TAG,
        description = API_CLASS_RECORD_OPS)
@Controller("data")
@PreAuthorize("hasAuthority('Editor')")
@ReadWriteController
public class DataApi {

    public static final String CONFIG_GDAL_ANALYSIS_XML = "config-gdal-analysis.xml";
    private Store store;

    @Autowired
    private GdalMetadataExtractor gdalMetadataExtractor;

    @Autowired
    private GeonetworkDataDirectory geonetworkDataDir;

    @Autowired
    BatchEditsApi batchEditsApi;

    private Element configuration;

    private final ApplicationContext appContext = ApplicationContextHolder.get();

    public DataApi() {
    }

    public DataApi(Store store) {
        this.store = store;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    @PostConstruct
    public void init() {
        if (appContext != null) {
            this.store = appContext.getBean("resourceStore", Store.class);
        }

        Path configFile = geonetworkDataDir.getWebappDir()
                .resolve("WEB-INF").resolve(CONFIG_GDAL_ANALYSIS_XML);
        try {
            configuration = Xml.loadFile(configFile);
        } catch (JDOMException e) {
            Log.error(GdalMetadataExtractor.LOGGER_NAME, String.format(
                    "Invalid configuration %s. %s", CONFIG_GDAL_ANALYSIS_XML, e.getMessage()));
        } catch (NoSuchFileException e) {
            Log.error(GdalMetadataExtractor.LOGGER_NAME, String.format(
                    "Configuration %s not found. %s", CONFIG_GDAL_ANALYSIS_XML, e.getMessage()));
        }

    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Return status of analyzer.")
    @RequestMapping(value = "/analyzer/status",
            method = RequestMethod.GET,
            produces = {
                    MediaType.APPLICATION_JSON_VALUE
            }
    )
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analyzer status.")
    })
    public
    @ResponseBody
    ResponseEntity<Map<String, String>> analyzerStatus() {
        Map<String, String> status = new HashMap<>();
        try {
            status.put("gdalMetadataExtractor", gdalMetadataExtractor.getVersion());
        } catch (IOException e) {
            status.put("gdalMetadataExtractor", null);
        }
        return new ResponseEntity<>(status, HttpStatus.OK);
    }


    @io.swagger.v3.oas.annotations.Operation(
            summary = "Analyze a file or datasource related to that record and return GDAL information.")
    @RequestMapping(value = "/{metadataUuid}/data/analyze",
            method = RequestMethod.GET,
            produces = {
                    MediaType.APPLICATION_JSON_VALUE
            }
    )
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Datasource analysis."),
            @ApiResponse(responseCode = "404", description = "Metadata not found."),
            @ApiResponse(responseCode = "400", description = "Record does not meet preconditions. Check error message."),
            @ApiResponse(responseCode = "500", description = "Service unavailable."),
            @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    public
    @ResponseBody
    ResponseEntity<GdalDataset> analyze(
            @Parameter(
                    description = API_PARAM_RECORD_UUID,
                    required = true)
            @PathVariable
            String metadataUuid,
            @Parameter(
                    description = "Datasource",
                    required = true)
            @RequestParam(name = "datasource")
            String datasource,
            @Parameter(
                    description = "Layer",
                    required = false)
            @RequestParam(name = "layer", required = false)
            String layer,
            @Parameter(hidden = true)
            HttpServletRequest request
    ) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);

        if (datasource.startsWith("attachments")) {
            try (Store.ResourceHolder file = store.getResource(serviceContext, metadataUuid, datasource.replace("attachments/", ""), true)) {
                datasource = file.getPath().toString();
            }
        }

        return new ResponseEntity<>(gdalMetadataExtractor.analyze(datasource, layer), HttpStatus.OK);
    }


    @io.swagger.v3.oas.annotations.Operation(
            summary = "Apply GDAL information to a record and preview result.")
    @GetMapping(value = "/{metadataUuid}/data/analysis/preview",
            produces = {
                    MediaType.APPLICATION_XML_VALUE
            }
    )
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Record updated preview."),
            @ApiResponse(responseCode = "404", description = "Metadata not found."),
            @ApiResponse(responseCode = "400", description = "Record does not meet preconditions. Check error message."),
            @ApiResponse(responseCode = "500", description = "Service unavailable."),
            @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    public
    @ResponseBody
    Object previewAnalysis(
            @Parameter(
                    description = API_PARAM_RECORD_UUID,
                    required = true)
            @PathVariable
            String metadataUuid,
            @Parameter(
                    description = "Datasource",
                    required = true)
            @RequestParam(name = "datasource")
            String datasource,
            @Parameter(
                    description = "Layer",
                    required = false)
            @RequestParam(name = "layer", required = false)
            String layer,
            @Parameter(hidden = true)
            HttpServletRequest request
    ) throws Exception {
        Pair<SimpleMetadataProcessingReport, Element> batchEdits = applyGdalAnalysis(metadataUuid, datasource, layer, request, true);
        return batchEdits.two();
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Apply GDAL information to record and save it.")
    @RequestMapping(value = "/{metadataUuid}/data/analysis/apply",
            method = RequestMethod.PUT,
            produces = {
                    MediaType.APPLICATION_JSON_VALUE
            }
    )
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Record updated."),
            @ApiResponse(responseCode = "404", description = "Metadata not found."),
            @ApiResponse(responseCode = "400", description = "Record does not meet preconditions. Check error message."),
            @ApiResponse(responseCode = "500", description = "Service unavailable."),
            @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public IProcessingReport applyAnalysis(
            @Parameter(
                    description = API_PARAM_RECORD_UUID,
                    required = true)
            @PathVariable
            String metadataUuid,
            @Parameter(
                    description = "Datasource",
                    required = true)
            @RequestParam(name = "datasource")
            String datasource,
            @Parameter(
                    description = "Layer"
            )
            @RequestParam(name = "layer", required = false)
            String layer,
            @Parameter(hidden = true)
            HttpServletRequest request
    ) throws Exception {
        Pair<SimpleMetadataProcessingReport, Element> batchEdits = applyGdalAnalysis(metadataUuid, datasource, layer, request, false);
        return batchEdits.one();
    }


    private Pair<SimpleMetadataProcessingReport, Element> applyGdalAnalysis(String metadataUuid, String datasource, String layer, HttpServletRequest request, Boolean previewOnly) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);

        if (datasource.startsWith("attachments")) {
            try (Store.ResourceHolder file = store.getResource(serviceContext, metadataUuid, datasource.replace("attachments/", ""), true)) {
                datasource = file.getPath().toString();
            }
        }


        GdalDataset data = gdalMetadataExtractor.analyze(datasource, layer);

        List<BatchEditParameter> edits = new ArrayList<>();
        // Remove first?
        for (Object type : configuration.getChildren()) {
            if (type instanceof Element) {
                Element typeConfiguration = (Element) type;

                addBatchEditAction(typeConfiguration.getName(), typeConfiguration, ActionTypes.delete, metadata.getDataInfo().getSchemaId(), edits, data);
                addBatchEditAction(typeConfiguration.getName(), typeConfiguration, ActionTypes.add, metadata.getDataInfo().getSchemaId(), edits, data);
            }
        }
        return batchEditsApi.applyBatchEdits(new String[]{metadataUuid}, null, true, edits.toArray(BatchEditParameter[]::new), request, previewOnly, null);
    }

    private enum ActionTypes {
        add,
        delete
    }

    private static void addBatchEditAction(String type, Element typeConfiguration, ActionTypes actionType, String schemaId,
                                           List<BatchEditParameter> edits, GdalDataset data) {
        Element actionConfig = typeConfiguration.getChild(actionType.name());
        if (actionConfig != null) {
            Element schemaActionConfiguration = actionConfig.getChild(schemaId);
            if (schemaActionConfiguration != null) {
                BatchEditParameter action = new BatchEditParameter();
                action.setXpath(schemaActionConfiguration.getAttributeValue("xpath"));
                String condition = schemaActionConfiguration.getAttributeValue("condition");
                if (condition != null) {
                    action.setCondition(condition);
                }
                if (actionType == ActionTypes.add) {
                    action.setValue(replacePlaceholderFromDataProperties(type, data, schemaActionConfiguration));
                } else {
                    String xmlFragment = Xml.getString(schemaActionConfiguration.getChild("gn_delete"));
                    action.setValue(xmlFragment);
                }
                edits.add(action);
            } else {
                // Not supported for this schema
            }
        }
    }

    private static String replacePlaceholderFromDataProperties(String type, GdalDataset data, Element configuration) {
        String addFragment = Xml.getString(configuration.getChild("gn_add"));
        if (type.equals("spatialRepresentation")) {
            addFragment = addFragment.replace("${featureCount}",
                    String.valueOf(data.getLayers().get(0).getFeatureCount().longValue()));
        } else if (type.equals("title")) {
            addFragment = addFragment.replace("${layerName}",
                    String.valueOf(data.getLayers().get(0).getName()));
        } else if (type.equals("distributionFormat")) {
            addFragment = addFragment.replace("${driverLongName}",
                    String.valueOf(data.getDriverLongName()));
        }  else if (type.equals("geographicBoundingBox") && !data.getLayers().get(0).getGeometryFields().isEmpty()) {
            // TODO: For each layers ?
            List<Double> extent = data.getLayers().get(0).getGeometryFields().get(0).getExtent();
            // TODO: Convert to WGS84
            addFragment = addFragment
                    .replace("${east}", extent.get(0).toString())
                    .replace("${west}", extent.get(2).toString())
                    .replace("${south}", extent.get(1).toString())
                    .replace("${north}", extent.get(3).toString());
        } else if (type.equals("featureCatalogue")) {
            String addPerLayerTpl = Xml.getString((Element) configuration.getChild("gn_add-per-layer").getChildren().get(0));
            String addPerColumnTpl = Xml.getString((Element) configuration.getChild("gn_add-per-column").getChildren().get(0));
            StringBuilder layerFragments = new StringBuilder();
            for (GdalLayer layer : data.getLayers()) {
                StringBuilder columnFragments = new StringBuilder();
                for (GdalField field : layer.getFields()) {
                    columnFragments.append(addPerColumnTpl
                            .replace("${fieldName}", field.getName())
                            .replace("${fieldDescription}", StringUtils.isNotEmpty(field.getComment()) ? field.getComment() : "")
                            .replace("${fieldType}", field.getType().value()));
                }
                layerFragments.append(addPerLayerTpl
                        .replace("${featureTypeName}", String.valueOf(layer.getName()))
                        .replace("${gn_add-per-column}", columnFragments.toString())
                );
            }
            addFragment = addFragment
                    .replace("${gn_add-per-layer}", layerFragments.toString());
        }
        return addFragment;
    }
}
