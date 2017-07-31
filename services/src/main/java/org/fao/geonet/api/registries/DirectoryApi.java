//=============================================================================
//===	Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.api.registries;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.gml2.GMLWriter;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.ZipUtil;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;


import org.fao.geonet.domain.Profile;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.metadata.BatchOpsMetadataReindexer;
import org.fao.geonet.utils.Xml;
import org.geotools.GML;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.jdom.Element;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;

import static org.fao.geonet.api.records.MetadataInsertDeleteApi.API_PARAM_RECORD_UUID_PROCESSING;
import static org.fao.geonet.api.records.MetadataInsertDeleteApi.API_PARAP_RECORD_GROUP;

@EnableWebMvc
@Service
@RequestMapping(value = {
    "/api/registries/actions/entries",
    "/api/" + API.VERSION_0_1 +
        "/registries/actions/entries"
})
@Api(value = "registries",
    tags = "registries",
    description = "Registries related operations")
public class DirectoryApi {
    public static final String LOGGER = Geonet.GEONETWORK + ".registries.directory";
    public static final String API_SYNCHRONIZE_ENTRIES_NOTE =
        "Scan one or more records for element matching the XPath provided " +
            "and then check if this element is available in the directory. " +
            "If Found, the element from the directory update the element " +
            "in the record and optionally properties are preserved.<br/><br/>" +
            "The identifier XPath is used to find a match. An optional filter" +
            "can be added to restrict search to a subset of the directory. " +
            "If no identifier XPaths is provided, the UUID " +
            "is based on the content of the snippet (hash). It is recommended to use " +
            "an identifier for better matching (eg. ISO19139 contact with different " +
            "roles will not match on the automatic UUID mode).";
    public static final String APIURL_ACTIONS_ENTRIES_COLLECT =
        "/collect";
    public static final String APIURL_ACTIONS_ENTRIES_SYNCHRONIZE =
        "/synchronize";
    public static final String APIPARAM_XPATH =
        "XPath of the elements to extract as entry.";
    public static final String APIPARAM_IDENTIFIER_XPATH =
        "XPath of the element identifier. If not defined " +
            "a random UUID is generated and analysis will not check " +
            "for duplicates.";
    public static final String APIPARAM_PROPERTIESTOCOPY =
        "List of XPath of properties to copy from record to matching entry.";
    public static final String APIPARAM_REPLACEWITHXLINK =
        "Replace entry by XLink.";
    public static final String APIPARAM_DIRECTORYFILTERQUERY =
        "Filter query for directory search.";
    private static final String API_COLLECT_ENTRIES_NOTE =
        "Scan one or more records for element matching the XPath provided " +
            "and save them as directory entries (ie. subtemplate).<br/><br/>" +
            "Only records that the current user can edit are analyzed.";

    @ApiOperation(value = "Preview directory entries extracted from records",
        nickname = "previewExtractedEntries",
        notes = API_COLLECT_ENTRIES_NOTE)
    @RequestMapping(
        value = APIURL_ACTIONS_ENTRIES_COLLECT,
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('Reviewer')")
    public ResponseEntity<Object> previewExtractedEntries(
        @ApiParam(value = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @ApiParam(
            value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @ApiParam(value = APIPARAM_XPATH,
            required = true,
            example = ".//gmd:CI_ResponsibleParty")
        @RequestParam(required = true)
            String xpath,
        @ApiParam(value = APIPARAM_IDENTIFIER_XPATH,
            required = false,
            example = "@uuid")
        @RequestParam(required = false)
            String identifierXpath,
        HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        return collectEntries(context, uuids, bucket, xpath, identifierXpath, false, null);
    }


    @ApiOperation(value = "Extracts directory entries from records",
        nickname = "extractEntries",
        authorizations = {
            @Authorization(value = "basicAuth")
        },
        notes = API_COLLECT_ENTRIES_NOTE)
    @RequestMapping(
        value = APIURL_ACTIONS_ENTRIES_COLLECT,
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('Reviewer')")
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<Object> extractEntries(
        @ApiParam(value = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @ApiParam(
            value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @ApiParam(value = APIPARAM_XPATH,
            required = true,
            example = ".//gmd:CI_ResponsibleParty")
        @RequestParam(required = true)
            String xpath,
        @ApiParam(value = APIPARAM_IDENTIFIER_XPATH,
            required = false,
            example = "@uuid")
        @RequestParam(required = false)
            String identifierXpath,
        HttpServletRequest request
        // TODO: Add an option to set categories ?
        // TODO: Add an option to set groupOwner ?
        // TODO: Add an option to set privileges ?
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        return collectEntries(context, uuids, bucket, xpath, identifierXpath, true, null);
    }


    private ResponseEntity<Object> collectEntries(
        ServiceContext context,
        String[] uuids,
        String bucket,
        String xpath,
        String identifierXpath,
        boolean save, String directoryFilterQuery) throws Exception {

        UserSession session = context.getUserSession();

        // Check which records to analyse
        final Set<String> setOfUuidsToEdit = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, session);

        DataManager dataMan = context.getBean(DataManager.class);
        AccessManager accessMan = context.getBean(AccessManager.class);
        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();

        // List of identifier to check for duplicates
        Set<Element> listOfEntries = new HashSet<>();
        Set<Integer> listOfEntriesInternalId = new HashSet<>();
        final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
        final int user = context.getUserSession().getUserIdAsInt();
        final String siteId = context.getBean(SettingManager.class).getSiteId();

        for (String recordUuid : setOfUuidsToEdit) {
            Metadata record = metadataRepository.findOneByUuid(recordUuid);
            if (record == null) {
                report.incrementNullRecords();
            } else if (!accessMan.canEdit(context, String.valueOf(record.getId()))) {
                report.addNotEditableMetadataId(record.getId());
            } else {
                // Processing
                try {
                    CollectResults collectResults =
                        DirectoryUtils.collectEntries(context,
                            record, xpath, identifierXpath);
                    if (save) {
                        DirectoryUtils.saveEntries(
                            context,
                            collectResults,
                            siteId, user,
                            1, // TODO: Define group or take a default one
                            false);
                        listOfEntriesInternalId.addAll(
                            collectResults.getEntryIdentifiers().values()
                        );
                        report.incrementProcessedRecords();
                        report.addMetadataInfos(record.getId(), String.format(
                            "%d entry(ies) extracted from record '%s'. UUID(s): %s",
                            collectResults.getEntryIdentifiers().size(),
                            record.getUuid(),
                            collectResults.getEntryIdentifiers().toString()
                        ));
                    } else {
                        listOfEntries.addAll(collectResults.getEntries().values());
                    }
                } catch (Exception ex) {
                    report.addMetadataError(record.getId(), ex);
                }
            }
        }

        if (save) {
            dataMan.flush();
            BatchOpsMetadataReindexer r = new BatchOpsMetadataReindexer(dataMan, listOfEntriesInternalId);
            r.process();
            report.close();
            return new ResponseEntity<>((Object) report, HttpStatus.CREATED);
        } else {
            Element response = new Element("entries");
            for (Element e : listOfEntries) {
                response.addContent(e);
            }
            return new ResponseEntity<>((Object) response, HttpStatus.OK);
        }
    }


    @ApiOperation(value = "Preview updated matching entries in records",
        nickname = "previewUpdatedRecordEntries",
        notes = API_SYNCHRONIZE_ENTRIES_NOTE)
    @RequestMapping(
        value = APIURL_ACTIONS_ENTRIES_SYNCHRONIZE,
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<Object> previewUpdatedRecordEntries(
        @ApiParam(value = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @ApiParam(
            value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @ApiParam(value = APIPARAM_XPATH,
            required = true,
            example = ".//gmd:CI_ResponsibleParty")
        @RequestParam(required = true)
            String xpath,
        @ApiParam(value = APIPARAM_IDENTIFIER_XPATH,
            required = false,
            example = "@uuid or .//gmd:electronicMailAddress/gco:CharacterString/text()")
        @RequestParam(required = false)
            String identifierXpath,
        @ApiParam(value = APIPARAM_PROPERTIESTOCOPY,
            required = false,
            example = "./gmd:role/*/@codeListValue")
        @RequestParam(required = false)
            List<String> propertiesToCopy,
        @ApiParam(value = APIPARAM_REPLACEWITHXLINK,
            required = false,
            example = "@uuid")
        @RequestParam(required = false, defaultValue = "false")
            boolean substituteAsXLink,
        @ApiParam(value = APIPARAM_DIRECTORYFILTERQUERY,
            required = false,
            example = "groupPublished:IFREMER")
        @RequestParam(required = false)
            String fq,
        HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        return updateRecordEntries(context, uuids, bucket, xpath, identifierXpath, propertiesToCopy, substituteAsXLink, false, fq);
    }


    @ApiOperation(value = "Update matching entries in records",
        nickname = "updateRecordEntries",
        notes = API_SYNCHRONIZE_ENTRIES_NOTE)
    @RequestMapping(
        value = APIURL_ACTIONS_ENTRIES_SYNCHRONIZE,
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("hasRole('Reviewer')")
    @ResponseBody
    public ResponseEntity<Object> updateRecordEntries(
        @ApiParam(value = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @ApiParam(
            value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @ApiParam(value = APIPARAM_XPATH,
            required = true,
            example = ".//gmd:CI_ResponsibleParty")
        @RequestParam(required = true)
            String xpath,
        @ApiParam(value = APIPARAM_IDENTIFIER_XPATH,
            required = false,
            example = "@uuid")
        @RequestParam(required = false)
            String identifierXpath,
        @ApiParam(value = APIPARAM_PROPERTIESTOCOPY,
            required = false,
            example = "./gmd:role/*/@codeListValue")
        @RequestParam(required = false)
            List<String> propertiesToCopy,
        @ApiParam(value = APIPARAM_REPLACEWITHXLINK,
            required = false)
        @RequestParam(required = false, defaultValue = "false")
            boolean substituteAsXLink,
        @ApiParam(value = APIPARAM_DIRECTORYFILTERQUERY,
            required = false,
            example = "groupPublished:IFREMER")
        @RequestParam(required = false)
            String fq,
        HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        return updateRecordEntries(context, uuids, bucket, xpath, identifierXpath, propertiesToCopy, substituteAsXLink, true, fq);
    }


    private ResponseEntity<Object> updateRecordEntries(
        ServiceContext context,
        String[] uuids,
        String bucket,
        String xpath,
        String identifierXpath,
        List<String> propertiesToCopy,
        boolean substituteAsXLink,
        boolean save, String directoryFilterQuery) throws Exception {

        UserSession session = context.getUserSession();
        Profile profile = session.getProfile();

        // Check which records to analyse
        final Set<String> setOfUuidsToEdit = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, session);

        DataManager dataMan = context.getBean(DataManager.class);
        AccessManager accessMan = context.getBean(AccessManager.class);

        // List of identifier to check for duplicates
        Set<Element> listOfUpdatedRecord = new HashSet<>();
        Set<Integer> listOfRecordInternalId = new HashSet<>();
        final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();

        boolean validate = false, ufo = false, index = false;
        report.setTotalRecords(setOfUuidsToEdit.size());
        for (String recordUuid : setOfUuidsToEdit) {
            Metadata record = metadataRepository.findOneByUuid(recordUuid);
            if (record == null) {
                report.incrementNullRecords();
            } else if (!accessMan.canEdit(context, String.valueOf(record.getId()))) {
                report.addNotEditableMetadataId(record.getId());
            } else {
                // Processing
                try {
                    CollectResults collectResults =
                        DirectoryUtils.synchronizeEntries(
                            context,
                            record, xpath, identifierXpath,
                            propertiesToCopy, substituteAsXLink, directoryFilterQuery);
                    listOfRecordInternalId.add(record.getId());
                    if (save && collectResults.isRecordUpdated()) {
                        // TODO: Only if there was a change
                        try {
                            // TODO: Should we update date stamp ?
                            dataMan.updateMetadata(
                                context, "" + record.getId(),
                                collectResults.getUpdatedRecord(),
                                validate, ufo, index, context.getLanguage(),
                                new ISODate().toString(), true);
                            listOfRecordInternalId.add(record.getId());
                            report.addMetadataInfos(record.getId(), "Metadata updated.");
                        } catch (Exception e) {
                            report.addMetadataError(record.getId(), e);
                        }
                    } else {
                        if (collectResults.isRecordUpdated()) {
                            listOfUpdatedRecord.add(collectResults.getUpdatedRecord());
                        }
                    }
                    report.incrementProcessedRecords();
                } catch (Exception e) {
                    report.addMetadataError(record.getId(), e);
                }
            }
        }

        if (save) {
            dataMan.flush();
            BatchOpsMetadataReindexer r =
                new BatchOpsMetadataReindexer(dataMan, listOfRecordInternalId);
            r.process();
            report.close();
            return new ResponseEntity<>((Object) report, HttpStatus.CREATED);
        } else {
            // TODO: Limite size of large response ?
            Element response = new Element("records");
            for (Element e : listOfUpdatedRecord) {
                response.addContent(e);
            }
            report.close();
            return new ResponseEntity<>((Object) response, HttpStatus.OK);
        }
    }



    @ApiOperation(value = "Import spatial directory entries",
        nickname = "importSpatialEntries",
        notes = "Directory entry (AKA subtemplates) are XML fragments that can be " +
            "inserted in metadata records. Use this service to import geographic extent entries " +
            "from an ESRI Shapefile format.")
    @RequestMapping(
        value = "/import/spatial",
        method = RequestMethod.POST,
        consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE
        },
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Directory entries imported."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_REVIEWER)
    })
    @PreAuthorize("hasRole('Reviewer')")
    @ResponseBody
    public SimpleMetadataProcessingReport importSpatialEntries(
        @ApiParam(
            value = "The ZIP file to upload containing the Shapefile.",
            required = true
        )
        @RequestParam("file")
            MultipartFile file,
        @ApiParam(
            value = "Attribute to use for UUID. If none, random UUID are generated.",
            required = false)
        @RequestParam(
            required = false
        )
            String uuidAttribute,
        @ApiParam(
            value = "Attribute to use for extent description. " +
                "If none, no extent description defined. TODO: Add per language desc ?",
            required = false)
        @RequestParam(
            required = false
        )
            String descriptionAttribute,
        @ApiParam(
            value = "geomProjectionTo",
            defaultValue = "",
            required = false
        )
        @RequestParam(
            required = false
        )
            String geomProjectionTo,
        @ApiParam(
            value = "lenient",
            defaultValue = "false",
            required = false
        )
        @RequestParam(
            required = false
        )
            boolean lenient,
        @ApiParam(
            value = "Create only bounding box for each spatial objects.",
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "true")
            boolean onlyBoundingBox,
        @ApiParam(
            value = "Process",
            defaultValue = "build-extent-subtemplate",
            required = false
        )
        @RequestParam(
            required = false
        )
            String process,
        @ApiParam(
            value = "Schema identifier",
            defaultValue = "iso19139",
            required = false
        )
        @RequestParam(
            required = false
        )
            String schema,
        @ApiParam(
            value = API_PARAM_RECORD_UUID_PROCESSING,
            required = false,
            defaultValue = "NOTHING"
        )
        @RequestParam(
            required = false,
            defaultValue = "NOTHING"
        )
        final MEFLib.UuidAction uuidProcessing,
        @ApiParam(
            value = API_PARAP_RECORD_GROUP,
            required = false
        )
        @RequestParam(
            required = false
        )
        final Integer group,
        @ApiIgnore
            MultipartHttpServletRequest request)
        throws Exception {

        final ApplicationContext applicationContext = ApplicationContextHolder.get();
        final ServiceContext context = ApiUtils.createServiceContext(request);
        final int user = context.getUserSession().getUserIdAsInt();
        final String siteId = context.getBean(SettingManager.class).getSiteId();
        final DataManager dm = applicationContext.getBean(DataManager.class);

        Set<Integer> listOfRecordInternalId = new HashSet<>();
        CollectResults collectResults = new CollectResults();

        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();

        // Extract ZIP file content
        File zipFile = new File(file.getOriginalFilename());
        file.transferTo(zipFile);
        Path toDirectory = Files.createTempDirectory("gn-imported-entries-");
        ZipUtil.extract(zipFile.toPath(), toDirectory);


        // Search shapefiles
        File [] shapefiles = toDirectory.toFile().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".shp");
            }
        });

        GML gml = new GML(org.geotools.GML.Version.GML3);


        CoordinateReferenceSystem geomProjection = null;
        if (StringUtils.isNotEmpty(geomProjectionTo)) {
            try {
                geomProjection = CRS.getAuthorityFactory(true)
                    .createCoordinateReferenceSystem(geomProjectionTo);

            } catch (NoSuchAuthorityCodeException ex) {
                throw new ResourceNotFoundException(String.format(
                    "Projection '%s' to convert geometry to not foundin EPSG database",
                    geomProjectionTo));
            }
        }

        for (File shapefile : shapefiles) {
            Map<String, Object> map = new HashMap<>();
            map.put("url", shapefile.toURI().toURL());

            DataStore dataStore = DataStoreFinder.getDataStore(map);
            String typeName = dataStore.getTypeNames()[0];

            FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore
                .getFeatureSource(typeName);
            Filter filter = Filter.INCLUDE;

            FeatureCollection<SimpleFeatureType, SimpleFeature> collection =
                source.getFeatures(filter);

            int counter = 0;

            try (FeatureIterator<SimpleFeature> features = collection.features()) {

                MetadataSchema metadataSchema = dm.getSchema(schema);
                Path xslProcessing = metadataSchema.getSchemaDir()
                                        .resolve("process").resolve(process + ".xsl");

                boolean validate = false, ufo = false, index = false;
                report.setTotalRecords(collection.size());

                CoordinateReferenceSystem wgs84 = CRS.getAuthorityFactory(true)
                    .createCoordinateReferenceSystem("urn:x-ogc:def:crs:EPSG::4326");
                while (features.hasNext()) {
                    SimpleFeature feature = features.next();

                    // Collect feature info and add them to a map for the XSL conversion
                    Map<String, Object> parameters = new HashMap<>();
                    String featureUuidValue = null;
                    String featureDescriptionValue = "";
                    if (StringUtils.isNotEmpty(uuidAttribute)) {
                        final Object attribute = feature.getAttribute(uuidAttribute);
                        if (attribute != null) {
                            featureUuidValue = attribute.toString();
                        }
                    }
                    if (StringUtils.isNotEmpty(descriptionAttribute)) {
                        final Object attribute = feature.getAttribute(descriptionAttribute);
                        if (attribute != null) {
                            featureDescriptionValue = attribute.toString();
                        }
                    }
                    String uuid = StringUtils.isNotEmpty(featureUuidValue) ?
                        featureUuidValue : UUID.randomUUID().toString();
                    parameters.put("uuid", uuid);
                    parameters.put("description",
                        StringUtils.isNotEmpty(featureDescriptionValue) ?
                            featureDescriptionValue : "");

                    CoordinateReferenceSystem dataCrs = feature.getDefaultGeometryProperty().getDescriptor().getCoordinateReferenceSystem();

                    BoundingBox bounds = feature.getBounds();
                    com.vividsolutions.jts.geom.Envelope wgsEnvelope = JTS.toGeographic(
                        new com.vividsolutions.jts.geom.Envelope(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY()),
                        dataCrs);
                    parameters.put("east", wgsEnvelope.getMaxX());
                    parameters.put("north", wgsEnvelope.getMaxY());
                    parameters.put("west", wgsEnvelope.getMinX());
                    parameters.put("south", wgsEnvelope.getMinY());
                    parameters.put("onlyBoundingBox", onlyBoundingBox);

                    // Reproject geometry if needed and get GML encoding
                    Geometry featureGeometry = (Geometry) feature.getDefaultGeometry();
                    if (geomProjection != null) {
                        MathTransform transform = CRS.findMathTransform(
                            dataCrs, geomProjection, lenient);
                        featureGeometry = JTS.transform(featureGeometry, transform);
                    }
                    WKTReader wktR = new WKTReader();
                    Geometry geom = wktR.read(featureGeometry.toString());
                    GMLWriter gmlW = new GMLWriter(true);
                    String gmlGeom = gmlW.write(geom);
                    parameters.put("geometry", gmlGeom);

                    // A dummy XML to transform with to build the output
                    // subtemplate
                    Element subtemplate = new Element("root");


                    // XSL transformation building the subtemplate snippet
                    Element snippet = Xml.transform(subtemplate, xslProcessing, parameters);

                    collectResults.getEntries().put(uuid, uuid, snippet);
                    counter ++;
                }
            }

            report.addInfos(String.format(
                "%d entries extracted from shapefile '%s'.",
                counter,
                shapefile.getName()
            ));
        }

        // Save the snippets and index
        if (collectResults.getEntries().size() > 0) {
            // Create an empty record providing schema information
            // about collected subtemplates
            Metadata record = new Metadata();
            record.getDataInfo().setSchemaId(schema);
            collectResults.setRecord(record);

            DirectoryUtils.saveEntries(
                context,
                collectResults,
                siteId, user,
                group,
                false);

            dm.flush();

            listOfRecordInternalId.addAll(
                collectResults.getEntryIdentifiers().values()
            );

            BatchOpsMetadataReindexer r =
                new BatchOpsMetadataReindexer(dm, listOfRecordInternalId);
            r.process();

            report.close();
        } else {
            report.addInfos(String.format("No entry found in ZIP file '%s'",
                file.getOriginalFilename()));
            report.close();
        }
        return report;
    }
}
