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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.ZipUtil;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.search.index.BatchOpsMetadataReindexer;
import org.fao.geonet.utils.Xml;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.api.data.Query;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.util.factory.Hints;
import org.geotools.wfs.GML;
import org.jdom.Element;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.filter.Filter;
import org.geotools.api.geometry.BoundingBox;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.fao.geonet.api.records.MetadataInsertDeleteApi.API_PARAM_RECORD_GROUP;
import static org.fao.geonet.api.records.MetadataInsertDeleteApi.API_PARAM_RECORD_UUID_PROCESSING;

@EnableWebMvc
@Service
@RequestMapping(value = {
    "/{portal}/api/registries/actions/entries"
})
@Tag(name = "registries",
    description = "Registries related operations")
public class DirectoryApi {
    public static final String LOGGER = Geonet.GEONETWORK + ".registries.directory";
    public static final String API_SYNCHRONIZE_ENTRIES_NOTE =
        "Scan one or more records for element matching the XPath provided " +
            "and then check if this element is available in the directory. " +
            "If Found, the element from the directory update the element " +
            "in the record and optionally text or attribute value properties are preserved using propertiesToCopy. Elements can be lost if not existing in the directory entry (eg. from an gex:Extent directory containing only description and bounding polygon, the update can remove temporal element).<br/><br/>" +
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
        "List of XPath of properties to copy from record to matching entry. Only support text or attribute eg. ./gmd:role/*/@codeListValue";
    public static final String APIPARAM_REPLACEWITHXLINK =
        "Replace entry by XLink.";
    public static final String APIPARAM_DIRECTORYFILTERQUERY =
        "Filter query for directory search.";
    private static final String API_COLLECT_ENTRIES_NOTE =
        "Scan one or more records for element matching the XPath provided " +
            "and save them as directory entries (ie. subtemplate).<br/><br/>" +
            "Only records that the current user can edit are analyzed.<br/><br/>" +
            "Examples:<br/>" +
            "For ISO19115-3 records, use .//cit:CI_Responsibility and compute identifier based on email with .//cit:electronicMailAddress/*/text() to create a contact directory.";


    @Autowired
    DataManager dataManager;

    @Autowired
    SettingManager settingManager;

    @Autowired
    IMetadataUtils metadataRepository;

    @Autowired
    AccessManager accessManager;


    @io.swagger.v3.oas.annotations.Operation(summary = "Preview directory entries extracted from records",
        description = API_COLLECT_ENTRIES_NOTE)
    @RequestMapping(
        value = APIURL_ACTIONS_ENTRIES_COLLECT,
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('Reviewer')")
    public ResponseEntity<Object> previewExtractedEntries(
        @Parameter(description = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @Parameter(description = APIPARAM_XPATH,
            required = true,
            example = ".//gmd:CI_ResponsibleParty")
        @RequestParam(required = true)
            String xpath,
        @Parameter(description = APIPARAM_IDENTIFIER_XPATH,
            required = false,
            example = "@uuid")
        @RequestParam(required = false)
            String identifierXpath,
        HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        return collectEntries(context, uuids, bucket, xpath, identifierXpath, false, null);
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Extracts directory entries from records",
//        authorizations = {
//            @Authorization(value = "basicAuth")
//        },
        description = API_COLLECT_ENTRIES_NOTE)
    @RequestMapping(
        value = APIURL_ACTIONS_ENTRIES_COLLECT,
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('Reviewer')")
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<Object> extractEntries(
        @Parameter(description = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @Parameter(description = APIPARAM_XPATH,
            required = true,
            example = ".//gmd:CI_ResponsibleParty")
        @RequestParam(required = true)
            String xpath,
        @Parameter(description = APIPARAM_IDENTIFIER_XPATH,
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

        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();

        // List of identifier to check for duplicates
        Set<Element> listOfEntries = new HashSet<>();
        Set<Integer> listOfEntriesInternalId = new HashSet<>();

        final int user = context.getUserSession().getUserIdAsInt();
        final String siteId = settingManager.getSiteId();

        for (String recordUuid : setOfUuidsToEdit) {
            AbstractMetadata record = metadataRepository.findOneByUuid(recordUuid);
            if (record == null) {
                report.incrementNullRecords();
            } else if (!accessManager.canEdit(context, String.valueOf(record.getId()))) {
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
                        report.addMetadataInfos(record, String.format(
                            "%d entry(ies) extracted from record '%s'. UUID(s): %s",
                            collectResults.getEntryIdentifiers().size(),
                            record.getUuid(),
                            collectResults.getEntryIdentifiers().toString()
                        ));
                    } else {
                        listOfEntries.addAll(collectResults.getEntries().values());
                    }
                } catch (Exception ex) {
                    report.addMetadataError(record, ex);
                }
            }
        }

        if (save) {
            dataManager.flush();
            BatchOpsMetadataReindexer r = new BatchOpsMetadataReindexer(dataManager, listOfEntriesInternalId);
            r.process(settingManager.getSiteId());
            report.close();
            return new ResponseEntity<>(report, HttpStatus.CREATED);
        } else {
            Element response = new Element("entries");
            for (Element e : listOfEntries) {
                response.addContent(e);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }


    @io.swagger.v3.oas.annotations.Operation(summary = "Preview updated matching entries in records",
        description = API_SYNCHRONIZE_ENTRIES_NOTE)
    @RequestMapping(
        value = APIURL_ACTIONS_ENTRIES_SYNCHRONIZE,
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<Object> previewUpdatedRecordEntries(
        @Parameter(description = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @Parameter(description = APIPARAM_XPATH,
            required = true,
            example = ".//gmd:CI_ResponsibleParty")
        @RequestParam(required = true)
            String xpath,
        @Parameter(description = APIPARAM_IDENTIFIER_XPATH,
            required = false,
            example = "@uuid or .//gmd:electronicMailAddress/gco:CharacterString/text()")
        @RequestParam(required = false)
            String identifierXpath,
        @Parameter(description = APIPARAM_PROPERTIESTOCOPY,
            required = false,
            example = "./gmd:role/*/@codeListValue")
        @RequestParam(required = false)
            List<String> propertiesToCopy,
        @Parameter(description = APIPARAM_REPLACEWITHXLINK,
            required = false,
            example = "@uuid")
        @RequestParam(required = false, defaultValue = "false")
            boolean substituteAsXLink,
        @Parameter(description = APIPARAM_DIRECTORYFILTERQUERY,
            required = false,
            example = "groupPublished:IFREMER")
        @RequestParam(required = false)
            String fq,
        HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        return updateRecordEntries(context, uuids, bucket, xpath, identifierXpath, propertiesToCopy, substituteAsXLink, false, fq);
    }


    @io.swagger.v3.oas.annotations.Operation(summary = "Update matching entries in records",
        description = API_SYNCHRONIZE_ENTRIES_NOTE)
    @RequestMapping(
        value = APIURL_ACTIONS_ENTRIES_SYNCHRONIZE,
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('Reviewer')")
    @ResponseBody
    public ResponseEntity<Object> updateRecordEntries(
        @Parameter(description = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestParam(required = false)
            String[] uuids,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @Parameter(description = APIPARAM_XPATH,
            required = true,
            example = ".//gmd:CI_ResponsibleParty")
        @RequestParam(required = true)
            String xpath,
        @Parameter(description = APIPARAM_IDENTIFIER_XPATH,
            required = false,
            example = "@uuid")
        @RequestParam(required = false)
            String identifierXpath,
        @Parameter(description = APIPARAM_PROPERTIESTOCOPY,
            required = false,
            example = "./gmd:role/*/@codeListValue")
        @RequestParam(required = false)
            List<String> propertiesToCopy,
        @Parameter(description = APIPARAM_REPLACEWITHXLINK,
            required = false)
        @RequestParam(required = false, defaultValue = "false")
            boolean substituteAsXLink,
        @Parameter(description = APIPARAM_DIRECTORYFILTERQUERY,
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

        // List of identifier to check for duplicates
        Set<Element> listOfUpdatedRecord = new HashSet<>();
        Set<Integer> listOfRecordInternalId = new HashSet<>();
        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();

        boolean validate = false, ufo = false;
        report.setTotalRecords(setOfUuidsToEdit.size());
        for (String recordUuid : setOfUuidsToEdit) {
            AbstractMetadata record = metadataRepository.findOneByUuid(recordUuid);
            if (record == null) {
                report.incrementNullRecords();
            } else if (!accessManager.canEdit(context, String.valueOf(record.getId()))) {
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
                            dataManager.updateMetadata(
                                context, "" + record.getId(),
                                collectResults.getUpdatedRecord(),
                                validate, ufo, context.getLanguage(),
                                new ISODate().toString(), true, IndexingMode.none);
                            listOfRecordInternalId.add(record.getId());
                            report.addMetadataInfos(record, "Metadata updated.");
                        } catch (Exception e) {
                            report.addMetadataError(record, e);
                        }
                    } else {
                        if (collectResults.isRecordUpdated()) {
                            listOfUpdatedRecord.add(collectResults.getUpdatedRecord());
                        }
                    }
                    report.incrementProcessedRecords();
                } catch (Exception e) {
                    report.addMetadataError(record, e);
                }
            }
        }

        if (save) {
            dataManager.flush();
            BatchOpsMetadataReindexer r =
                new BatchOpsMetadataReindexer(dataManager, listOfRecordInternalId);
            r.process(settingManager.getSiteId());
            report.close();
            return new ResponseEntity<>(report, HttpStatus.CREATED);
        } else {
            // TODO: Limite size of large response ?
            Element response = new Element("records");
            for (Element e : listOfUpdatedRecord) {
                response.addContent(e);
            }
            report.close();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }


    @io.swagger.v3.oas.annotations.Operation(summary = "Import spatial directory entries",
        description = "Directory entry (AKA subtemplates) are XML fragments that can be " +
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
        @ApiResponse(responseCode = "201", description = "Directory entries imported."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_REVIEWER)
    })
    @PreAuthorize("hasAuthority('Reviewer')")
    @ResponseBody
    public SimpleMetadataProcessingReport importSpatialEntries(
        @Parameter(
            description = "The ZIP file to upload containing the Shapefile.",
            required = true
        )
        @RequestParam("file")
            MultipartFile file,
        @Parameter(
            description = "Attribute to use for UUID. If none, random UUID are generated.",
            required = false)
        @RequestParam(
            required = false
        )
            String uuidAttribute,
        @Parameter(
            description = "Pattern to build UUID from. Default is '{{uuid}}'.",
            required = false)
        @RequestParam(
            defaultValue = "{{uuid}}",
            required = false
        )
            String uuidPattern,
        @Parameter(
            description = "Attribute to use for extent description. " +
                "If none, no extent description defined. TODO: Add per language desc ?",
            required = false)
        @RequestParam(
            required = false
        )
            String descriptionAttribute,
        @Parameter(
            description = "geomProjectionTo",
            required = false
        )
        @RequestParam(
            required = false
        )
            String geomProjectionTo,
        @Parameter(
            description = "lenient",
            required = false
        )
        @RequestParam(
            required = false
        )
            boolean lenient,
        @Parameter(
            description = "Attribute table charset",
            required = false
        )
        @RequestParam(
            required = false,
            defaultValue = ""
        )
            String charset,
        @Parameter(
            description = "Create only bounding box for each spatial objects.",
            required = false)
        @RequestParam(
            required = false,
            defaultValue = "true")
            boolean onlyBoundingBox,
        @Parameter(
            description = "Process",
            required = false
        )
        @RequestParam(
            required = false
        )
            String process,
        @Parameter(
            description = "Schema identifier",
            required = false
        )
        @RequestParam(
            required = false
        )
            String schema,
        @Parameter(
            description = API_PARAM_RECORD_UUID_PROCESSING,
            required = false
        )
        @RequestParam(
            required = false,
            defaultValue = "NOTHING"
        ) final MEFLib.UuidAction uuidProcessing,
        @Parameter(
            description = API_PARAM_RECORD_GROUP,
            required = false
        )
        @RequestParam(
            required = false
        ) final Integer group,
        @Parameter(hidden = true)
            MultipartHttpServletRequest request)
        throws Exception {

        ServiceContext context = ApiUtils.createServiceContext(request);
        ApplicationContext applicationContext = ApplicationContextHolder.get();

        MetadataSchema metadataSchema = dataManager.getSchema(schema);
        Path xslProcessing = metadataSchema.getSchemaDir().resolve("process").resolve(process + ".xsl");

        File[] shapeFiles = unzipAndFilterShp(file);

        CollectResults collectResults = new CollectResults();

        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();

        for (File shapeFile : shapeFiles) {

            SimpleFeatureCollection collection = shapeFileToFeatureCollection(shapeFile, charset);

            try (FeatureIterator<SimpleFeature> features = collection.features()) {


                while (features.hasNext()) {
                    SimpleFeature feature = features.next();

                    String uuid = computeUuid(uuidAttribute, uuidPattern, feature);
                    String description = computeDescription(descriptionAttribute, feature);
                    Envelope wgsEnvelope = computeEnvelope(feature);
                    Geometry featureGeometry = reprojGeom(geomProjectionTo, lenient, feature);
                    String xmlGeometry = geometryToXml(featureGeometry, collection.getSchema());

                    Map<String, Object> parameters = new HashMap<>();

                    parameters.put("uuid", uuid);
                    parameters.put("description", description);
                    parameters.put("east", wgsEnvelope.getMaxX());
                    parameters.put("north", wgsEnvelope.getMaxY());
                    parameters.put("west", wgsEnvelope.getMinX());
                    parameters.put("south", wgsEnvelope.getMinY());
                    parameters.put("onlyBoundingBox", onlyBoundingBox);
                    parameters.put("geometry", xmlGeometry);

                    Element subtemplate = new Element("root");
                    Element snippet = Xml.transform(subtemplate, xslProcessing, parameters);

                    collectResults.getEntries().put(uuid, uuid, snippet);
                }
            }

            report.addInfos(String.format(
                "%d entries extracted from shapefile '%s'.",
                collection.size(),
                shapeFile.getName()
            ));
        }

        report.setTotalRecords(collectResults.getEntries().size());

        // Save the snippets and index
        if (collectResults.getEntries().size() > 0) {
            // Create an empty record providing schema information
            // about collected subtemplates
            Metadata record = new Metadata();
            record.getDataInfo().setSchemaId(schema);
            collectResults.setRecord(record);

            int user = context.getUserSession().getUserIdAsInt();
            String siteId = settingManager.getSiteId();

            Map<String, Exception> errors = DirectoryUtils.saveEntries(
                context,
                collectResults,
                siteId, user,
                group,
                false);

            dataManager.flush();

            Set<Integer> listOfRecordInternalId = new HashSet<>();
            listOfRecordInternalId.addAll(
                collectResults.getEntryIdentifiers().values()
            );

            report.addInfos(String.format(
                "%d entries saved.",
                listOfRecordInternalId.size()
            ));

            BatchOpsMetadataReindexer r = new BatchOpsMetadataReindexer(dataManager, listOfRecordInternalId);
            r.process(settingManager.getSiteId());

            errors.forEach((k, v) ->
                report.addError(v)
            );

            report.close();
        } else {
            report.addInfos(String.format("No entry found in ZIP file '%s'",
                file.getOriginalFilename()));
            report.close();
        }
        return report;
    }

    private Geometry reprojGeom(String geomProjectionTo, boolean lenient, SimpleFeature feature)
        throws FactoryException, ResourceNotFoundException, TransformException {
        CoordinateReferenceSystem fromCrs = feature.getDefaultGeometryProperty().getDescriptor().getCoordinateReferenceSystem();
        CoordinateReferenceSystem toCrs = null;
        if (StringUtils.isNotEmpty(geomProjectionTo)) {
            try {
                toCrs = CRS.getAuthorityFactory(true).createCoordinateReferenceSystem(geomProjectionTo);

            } catch (NoSuchAuthorityCodeException ex) {
                throw new ResourceNotFoundException(String.format("Projection '%s' to convert geometry to not foundin EPSG database",
                    geomProjectionTo));
            }
        }

        if (toCrs != null) {
            MathTransform transform = CRS.findMathTransform(fromCrs, toCrs, lenient);
            return JTS.transform((Geometry) feature.getDefaultGeometry(), transform);
        } else {
            return (Geometry) feature.getDefaultGeometry();
        }
    }

    private String geometryToXml(Object geometry, SimpleFeatureType simpleFeatureType)
        throws IOException, SchemaException {
        GML gmlEncoder = new GML(GML.Version.WFS1_1);
        gmlEncoder.setNamespace("gn", "http://geonetwork-opensource.org");
        gmlEncoder.setBaseURL(new URL("http://geonetwork-opensource.org"));
        gmlEncoder.setEncoding(StandardCharsets.UTF_8);

        List<SimpleFeature> c = new LinkedList<SimpleFeature>();
        SimpleFeatureType TYPE = DataUtilities.createType(
            "http://geonetwork-opensource.org",
            "the_geom",
            "geom:Geometry");
        TYPE.getUserData().put("prefix", "gn");
        c.add(SimpleFeatureBuilder.build(TYPE, new Object[]{geometry}, null));
        ByteArrayOutputStream outXml = new ByteArrayOutputStream();
        gmlEncoder.encode(outXml, new ListFeatureCollection(simpleFeatureType, c));
        outXml.close();
        return outXml.toString();
    }

    private Envelope computeEnvelope(SimpleFeature feature) throws TransformException {
        BoundingBox bounds = feature.getBounds();
        return JTS.toGeographic(
            new Envelope(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY()),
            feature.getDefaultGeometryProperty().getDescriptor().getCoordinateReferenceSystem());
    }

    private String computeUuid(String uuidAttribute, String uuidPattern, SimpleFeature feature) {
        String featureUuidValue = null;
        if (StringUtils.isNotEmpty(uuidAttribute)) {
            Object attribute = feature.getAttribute(uuidAttribute);
            if (attribute != null) {
                featureUuidValue = attribute.toString();
            }
        }
        String uuid = StringUtils.isNotEmpty(featureUuidValue) ? featureUuidValue : UUID.randomUUID().toString();
        return uuidPattern.replace("{{uuid}}", uuid);
    }

    private String computeDescription(String descriptionAttribute, SimpleFeature feature) {
        String featureDescriptionValue = "";
        if (StringUtils.isNotEmpty(descriptionAttribute)) {
            Object attribute = feature.getAttribute(descriptionAttribute);
            if (attribute != null) {
                featureDescriptionValue = attribute.toString();
            }
        }
        return StringUtils.isNotEmpty(featureDescriptionValue) ? featureDescriptionValue : "";
    }

    private SimpleFeatureCollection shapeFileToFeatureCollection(File shapefile, String charset) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("url", shapefile.toURI().toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        if (dataStore instanceof ShapefileDataStore && StringUtils.isNotEmpty(charset)) {
            ((ShapefileDataStore)dataStore).setCharset(Charset.forName(charset));
        }
        String typeName = dataStore.getTypeNames()[0];
        SimpleFeatureSource source = dataStore.getFeatureSource(typeName);
        Query query = new Query(typeName, Filter.INCLUDE);
        query.setHints(new Hints(Hints.FEATURE_2D, true));
        return source.getFeatures(query);
    }

    private File[] unzipAndFilterShp(MultipartFile file) throws IOException, URISyntaxException {
        Path toDirectory = Files.createTempDirectory("gn-imported-entries-");
        toDirectory.toFile().deleteOnExit();
        File zipFile = new File(Paths.get(toDirectory.toString(), file.getOriginalFilename()).toString());
        file.transferTo(zipFile);
        ZipUtil.extract(zipFile.toPath(), toDirectory);
        File[] shapefiles = toDirectory.toFile().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".shp");
            }
        });
        return shapefiles;
    }
}
