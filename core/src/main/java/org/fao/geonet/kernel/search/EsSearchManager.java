/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.search;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.UpdateOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.index.OverviewIndexFieldUpdater;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static org.fao.geonet.constants.Geonet.IndexFieldNames.IS_TEMPLATE;
import static org.fao.geonet.kernel.search.IndexFields.INDEXING_ERROR_FIELD;
import static org.fao.geonet.kernel.search.IndexFields.INDEXING_ERROR_MSG;


public class EsSearchManager implements ISearchManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(Geonet.INDEX_ENGINE);

    public static final String ID = "id";

    public static final String SCHEMA_INDEX_XSLT_FOLDER = "index-fields";
    public static final String SCHEMA_INDEX_XSTL_FILENAME = "index.xsl";
    public static final String SCHEMA_INDEX_SUBTEMPLATE_XSTL_FILENAME = "index-subtemplate.xsl";
    public static final String FIELDNAME = "name";
    public static final String FIELDSTRING = "string";

    public static final Map<String, String> RELATED_INDEX_FIELDS;
    public static final Set<String> FIELDLIST_CORE;
    public static final Set<String> FIELDLIST_RELATED;
    public static final Map<String, String> FIELDLIST_RELATED_SCRIPTED;
    public static final Set<String> FIELDLIST_UUID;

    static {
        FIELDLIST_UUID = ImmutableSet.<String>builder()
            .add(Geonet.IndexFieldNames.UUID).build();

        RELATED_INDEX_FIELDS = ImmutableMap.<String, String>builder()
            .put("children", "parentUuid")
            .put("brothersAndSisters", "parentUuid")
            .put("services", "recordOperateOn")
            .put("hasfeaturecats", "hasfeaturecat")
            .put("hassources", "hassource")
            .put("associated", "agg_associated")
            .put("datasets", "uuid")
            .put("fcats", "uuid")
            .put("sources", "uuid")
            .put("siblings", "uuid")
            .put("parent", "uuid")
            .put("uuid", "uuid")
            .build();

        FIELDLIST_CORE = ImmutableSet.<String>builder()
            .add(Geonet.IndexFieldNames.ID)
            .add(Geonet.IndexFieldNames.UUID)
            .add(Geonet.IndexFieldNames.RESOURCETITLE)
            .add(Geonet.IndexFieldNames.RESOURCETITLE + "Object")
            .add(Geonet.IndexFieldNames.RESOURCEABSTRACT)
            .add(Geonet.IndexFieldNames.RESOURCEABSTRACT + "Object")
            .add("operatesOn")
            .build();

        FIELDLIST_RELATED = ImmutableSet.<String>builder()
            .add(Geonet.IndexFieldNames.ID)
            .add(Geonet.IndexFieldNames.UUID)
            .add(Geonet.IndexFieldNames.RESOURCETITLE)
            .add(Geonet.IndexFieldNames.RESOURCETITLE + "Object")
            //.add("overview.*")
            .add("link")
            .add("format")
            .add("resourceType")
            .add("cl_status.key")
            .add(Geonet.IndexFieldNames.OP_PREFIX + "*")
            .add(Geonet.IndexFieldNames.GROUP_OWNER)
            .add(Geonet.IndexFieldNames.RESOURCEABSTRACT)
            .add(Geonet.IndexFieldNames.RESOURCEABSTRACT + "Object")
            .add("operatesOn")
            .build();

        FIELDLIST_RELATED_SCRIPTED = ImmutableMap.<String, String>builder()
            // Elasticsearch scripted field to get the first overview url. Scripted fields must return single values.
            .put("overview", "return params['_source'].overview == null ? [] : params['_source'].overview.stream().map(f -> f.url).findFirst().orElse('');")
            .build();
    }

    @Value("${es.index.records:gn-records}")
    private String defaultIndex = "records";

    @Value("${es.index.records.type:records}")
    private String indexType = "records";

    public String getDefaultIndex() {
        return defaultIndex;
    }

    public void setDefaultIndex(String defaultIndex) {
        this.defaultIndex = defaultIndex;
    }

    public String getIndexType() {
        return indexType;
    }

    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    @Autowired
    private EsRestClient client;

    @Autowired
    private OverviewIndexFieldUpdater overviewFieldUpdater;

    private int commitInterval = 200;

    // public for test, to be private or protected
    public Map<String, String> listOfDocumentsToIndex =
        Collections.synchronizedMap(new HashMap<>());
    private Map<String, String> indexList;

    private Path getXSLTForIndexing(Path schemaDir, MetadataType metadataType) {
        Path xsltForIndexing = schemaDir
            .resolve(SCHEMA_INDEX_XSLT_FOLDER)
            .resolve(
                metadataType.equals(MetadataType.SUB_TEMPLATE) || metadataType.equals(MetadataType.TEMPLATE_OF_SUB_TEMPLATE) ?
                    SCHEMA_INDEX_SUBTEMPLATE_XSTL_FILENAME : SCHEMA_INDEX_XSTL_FILENAME);
        if (!Files.exists(xsltForIndexing)) {
            throw new RuntimeException(String.format(
                "XSLT for schema indexing does not exist. Create file '%s'.",
                xsltForIndexing));
        }
        return xsltForIndexing;
    }

    private void addMDFields(Element doc, Path schemaDir,
                             Element metadata, MetadataType metadataType,
                             IndexingMode indexingMode) {
        final Path styleSheet = getXSLTForIndexing(schemaDir, metadataType);
        try {
            Map<String, Object> indexParams = new HashMap<>();
            indexParams.put("fastIndexMode", indexingMode.equals(IndexingMode.core));

            Element fields = Xml.transform(metadata, styleSheet, indexParams);
            /* Generates something like that:
            <doc>
              <field name="toto">Contenu</field>
            </doc>*/
            for (Element field : (List<Element>) fields.getChildren()) {
                doc.addContent((Element) field.clone());
            }
        } catch (Exception e) {
            LOGGER.error("Indexing stylesheet contains errors: {} \n  Marking the metadata as _indexingError=1 in index", e.getMessage());
            doc.addContent(new Element(INDEXING_ERROR_FIELD).setText("true"));
            doc.addContent(createIndexingErrorMsgElement("indexingErrorMsg-indexingStyleSheetError", "error",
                Map.of("message", e.getMessage())));
            doc.addContent(new Element(IndexFields.DRAFT).setText("n"));
        }
    }

    private void addMoreFields(Element doc, Multimap<String, Object> fields) {
        ArrayList<String> objectFields = Lists.newArrayList(INDEXING_ERROR_MSG);
        fields.entries().forEach(e -> {
            Element newElement = new Element(e.getKey())
                .setText(String.valueOf(e.getValue()));
            if(objectFields.contains(e.getKey())) {
                newElement.setAttribute("type", "object");
            }
            doc.addContent(newElement);
        });
    }

    public Element makeField(String name, String value) {
        Element field = new Element("Field");
        field.setAttribute(EsSearchManager.FIELDNAME, name);
        field.setAttribute(EsSearchManager.FIELDSTRING, value == null ? "" : value);
        return field;
    }


    @Override
    public void init(boolean dropIndexFirst, Optional<List<String>> indices) throws Exception {
        if (indexList != null) {
            indexList.keySet().forEach(e -> {
                try {
                    if (!indices.isPresent() ||
                        indices.get().contains(e)) {
                        createIndex(e, indexList.get(e), dropIndexFirst);
                    }
                } catch (IOException ex) {
                    LOGGER.error("Error during index creation. Error is: {}", ex.getMessage());
                }
            });
        }
    }

    @Autowired
    private GeonetworkDataDirectory dataDirectory;


    private void createIndex(String indexId, String indexName, boolean dropIndexFirst) throws IOException {
        if (dropIndexFirst) {
            try {
                DeleteIndexRequest deleteIndexRequest = DeleteIndexRequest.of(
                    b -> b.index(indexName)
                );

                DeleteIndexResponse deleteIndexResponse = client.getClient().indices().delete(deleteIndexRequest);
                if (deleteIndexResponse.acknowledged()) {
                    LOGGER.debug("Index '{}' removed.", indexName);
                }
            } catch (Exception e) {
                // index does not exist ?
                LOGGER.debug("Error during index '{}' removal. Error is: {}", indexName, e.getMessage());
            }
        }

        // Check index exist first
        ExistsRequest existsRequest = ExistsRequest.of(
            b -> b.index(indexName)
        );

        try {
            BooleanResponse exists = client.getClient().indices().exists(existsRequest);
            if (exists.value() && !dropIndexFirst) {
                return;
            }

            if (!exists.value() || dropIndexFirst) {
                // Check version of the index - how ?

                // Create it if not
                Path indexConfiguration = dataDirectory.getIndexConfigDir().resolve(indexId + ".json");
                if (Files.exists(indexConfiguration)) {

                    try (InputStream is = Files.newInputStream(indexConfiguration, StandardOpenOption.READ)) {
                        CreateIndexRequest createIndexRequest = CreateIndexRequest.of(
                            b -> b.index(indexName).withJson(is)
                        );

                        CreateIndexResponse createIndexResponse = client.getClient().indices().create(createIndexRequest);

                        if (createIndexResponse.acknowledged()) {
                            LOGGER.debug("Index '{}' created", indexName);
                        } else {
                            final String message = String.format("Index '%s' was not created. Error is: %s", indexName, createIndexResponse);
                            LOGGER.error(message);
                            throw new IllegalStateException(message);
                        }
                    }
                } else {
                    throw new FileNotFoundException(String.format(
                        "Index configuration file '%s' not found in data directory for building index with name '%s'. Create one or copy the default one.",
                        indexConfiguration.toAbsolutePath(),
                        indexName));
                }
            }
        } catch (ElasticsearchException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new IOException(ex.getMessage());
        } catch (Exception cnce) {
            final String message = String.format("Could not connect to index '%s'. Error is %s. Is the index server  up and running?",
                defaultIndex, cnce.getMessage());
            LOGGER.error(message, cnce);
            throw new IOException(message);
        }
    }

    @Override
    public void end() {
    }

    public UpdateResponse updateFields(String id, Map<String, Object> fields) throws IOException {
        fields.put(Geonet.IndexFieldNames.INDEXING_DATE, new Date());

        UpdateRequest updateRequest = UpdateRequest.of(
            b -> b.index(defaultIndex)
                .id(id)
                .doc(fields)
        );

        return client.getClient().update(updateRequest, Void.class);
    }

    public BulkResponse updateFields(String id, Multimap<String, Object> fields, Set<String> fieldsToRemove) throws IOException {
        Map<String, Object> fieldMap = new HashMap<>();
        fields.asMap().forEach((e, v) -> fieldMap.put(e, v.toArray()));
        return updateFields(id, fieldMap, fieldsToRemove);
    }
    public BulkResponse updateFields(String id, Map<String, Object> fieldMap, Set<String> fieldsToRemove) throws IOException {
        fieldMap.put(Geonet.IndexFieldNames.INDEXING_DATE, new Date());

        StringBuilder scriptSource = new StringBuilder();
        fieldsToRemove.forEach(f ->
            scriptSource.append(String.format("ctx._source.remove('%s');", f)));

        UpdateOperation deleteFieldsOperation = UpdateOperation.of(
            b -> b.id(id)
                .index(defaultIndex)
                .action(action -> action
                    .scriptedUpsert(true)
                    .upsert(Map.of())
                    .script(script -> script
                        .inline(inlineScript -> inlineScript
                            .lang("painless")
                            .source(scriptSource.toString())
                        )
                    )
                )
        );

        UpdateOperation addFieldRequestOperation = UpdateOperation.of(
            b -> b.id(id)
                .index(defaultIndex)
                .action(action -> action.doc(fieldMap))
        );

        List<BulkOperation> bulkOperationList = new ArrayList<>();
        bulkOperationList.add(BulkOperation.of(b -> b.update(deleteFieldsOperation)));
        bulkOperationList.add(BulkOperation.of(b -> b.update(addFieldRequestOperation)));

        BulkRequest bulkRequest = BulkRequest.of(
            b -> b.index(defaultIndex)
                .operations(bulkOperationList)
        );

        return client.getClient().bulk(bulkRequest);
    }

    public void updateFieldsAsynch(String id, Map<String, Object> fields) {
        fields.put(Geonet.IndexFieldNames.INDEXING_DATE, new Date());

        UpdateRequest updateRequest = UpdateRequest.of(
            b -> b.index(defaultIndex)
                .id(id)
                .doc(fields)
        );

        client.getAsynchClient()
            .update(updateRequest, ObjectNode.class)
            .whenComplete((response, exception) -> {
                if (exception != null) {
                    LOGGER.error("Failed to index {}", exception);
                } else {
                    LOGGER.info("Updated fields for document {}",  id);
                }
            });
    }

    public UpdateResponse updateField(String id, String field, Object value) throws Exception {
        Map<String, Object> updates = new HashMap<>(2);
        updates.put(getPropertyName(field), value);
        return updateFields(id, updates);
    }

    public void updateFieldAsynch(String id, String field, Object value) {
        Map<String, Object> updates = new HashMap<>(2);
        updates.put(getPropertyName(field), value);
        updateFieldsAsynch(id, updates);
    }

    @Autowired
    SourceRepository sourceRepository;

    @Override
    public void index(Path schemaDir, Element metadata, String id,
                      Multimap<String, Object> dbFields,
                      MetadataType metadataType,
                      boolean forceRefreshReaders,
                      IndexingMode indexingMode) throws Exception {

        Element docs = new Element("doc");
        if (schemaDir != null) {
            addMDFields(docs, schemaDir, metadata, metadataType, indexingMode);
        }
        addMoreFields(docs, dbFields);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode doc = documentToJson(docs);

        // ES does not allow a _source field
        String catalog = doc.get("source").asText();
        doc.remove("source");
        if (StringUtils.isNotEmpty(catalog)) {
            doc.put("sourceCatalogue", catalog);
        }

        JsonNode errors = doc.get(INDEXING_ERROR_MSG);
        if (errors != null) {
            doc.put(INDEXING_ERROR_FIELD, "true");
        }

        String jsonDocument = mapper.writeValueAsString(doc);

        if (forceRefreshReaders) {
            Map<String, String> document = new HashMap<>();
            document.put(id, jsonDocument);
            final BulkResponse bulkItemResponses = client.bulkRequest(defaultIndex, document);
            checkIndexResponse(bulkItemResponses, document);
            overviewFieldUpdater.process(id);
        } else {
            listOfDocumentsToIndex.put(id, jsonDocument);
            if (listOfDocumentsToIndex.size() == commitInterval) {
                sendDocumentsToIndex();
            }
        }
    }

    private void sendDocumentsToIndex() {
        Map<String, String> documents = new HashMap<>(listOfDocumentsToIndex);
        listOfDocumentsToIndex.clear();
        if (!documents.isEmpty()) {
            try {
                final BulkResponse bulkItemResponses = client
                    .bulkRequest(defaultIndex, documents);
                checkIndexResponse(bulkItemResponses, documents);
            } catch (Exception e) {
                LOGGER.error(
                    "An error occurred while indexing {} documents in current indexing list. Error is {}.",
                        listOfDocumentsToIndex.size(), e.getMessage());
            } finally {
                // TODO: Trigger this async ?
                documents.keySet().forEach(uuid -> overviewFieldUpdater.process(uuid));
            }
        }
    }

    private void checkIndexResponse(BulkResponse bulkItemResponses,
                                    Map<String, String> documents) throws IOException {
        if (bulkItemResponses.errors()) {
            Map<String, String> listErrorOfDocumentsToIndex = new HashMap<>(bulkItemResponses.items().size());
            List<String> errorDocumentIds = new ArrayList<>();
            // Add information in index that some items were not properly indexed
            bulkItemResponses.items().forEach(e -> {
                if (e.error() != null) {
                    errorDocumentIds.add(e.id());
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode docWithErrorInfo = mapper.createObjectNode();
                    String resourceTitle = String.format("Document #%s", e.id());
                    String id = "";
                    String uuid = "";
                    String isTemplate = "";

                    String failureDoc = documents.get(e.id());
                    try {
                        JsonNode node = mapper.readTree(failureDoc);
                        resourceTitle = node.get("resourceTitleObject").get("default").asText();
                        id = node.get(IndexFields.DBID).asText();
                        uuid = node.get("uuid").asText();
                        isTemplate = node.get(IS_TEMPLATE).asText();
                    } catch (Exception ignoredException) {
                    }
                    docWithErrorInfo.put(IndexFields.DBID, id);
                    docWithErrorInfo.put("uuid", uuid);
                    docWithErrorInfo.put(IndexFields.RESOURCE_TITLE, resourceTitle);
                    docWithErrorInfo.put(IS_TEMPLATE, isTemplate);
                    docWithErrorInfo.put(IndexFields.DRAFT, "n");
                    docWithErrorInfo.put(INDEXING_ERROR_FIELD, true);
                    ArrayNode errors = docWithErrorInfo.putArray(INDEXING_ERROR_MSG);
                    errors.add(createIndexingErrorMsgObject(e.error().reason(), "error", Map.of()));
                    // TODO: Report the JSON which was causing the error ?

                    LOGGER.error("Document with error #{}: {}.",
                        e.id(), e.error().reason());
                    LOGGER.error(failureDoc);

                    try {
                        listErrorOfDocumentsToIndex.put(e.id(), mapper.writeValueAsString(docWithErrorInfo));
                    } catch (JsonProcessingException e1) {
                        LOGGER.error("Generated document for the index is not properly formatted. Check document #{}: {}.",
                            e.id(), e1.getMessage());
                    }
                }
            });

            if (!listErrorOfDocumentsToIndex.isEmpty()) {
                BulkResponse response = client.bulkRequest(defaultIndex, listErrorOfDocumentsToIndex);
                if (response.errors()) {
                    LOGGER.error("Failed to save error documents {}.",
                            Arrays.toString(errorDocumentIds.toArray()));
                }
            }
        }
    }

    private static ImmutableSet<String> booleanFields;
    private static ImmutableSet<String> arrayFields;
    private static ImmutableSet<String> booleanValues;

    static {
        arrayFields = ImmutableSet.<String>builder()
            .add(Geonet.IndexFieldNames.RECORDLINK)
            .add("geom")
            .add("topic")
            .add("cat")
            .add("keyword")
            .add("extentDescriptionObject")
            .add("extentIdentifierObject")
            .add("resourceAltTitleObject")
            .add("resourceCredit")
            .add("resourceCreditObject")
            .add("resolutionScaleDenominator")
            .add("resolutionDistance")
            .add("extentDescription")
            .add("inspireTheme")
            .add("inspireThemeUri")
            .add("inspireTheme_syn")
            .add("inspireAnnex")
            .add("indexingErrorMsg")
            .add("status")
            .add("status_text")
            .add("coordinateSystem")
            .add("identifier")
            .add("responsibleParty")
            .add("mdLanguage")
            .add("otherLanguage")
            .add("resourceLanguage")
            .add("resourceIdentifier")
            .add("MD_LegalConstraintsOtherConstraints")
            .add("MD_LegalConstraintsOtherConstraintsObject")
            .add("MD_LegalConstraintsUseLimitation")
            .add("MD_LegalConstraintsUseLimitationObject")
            .add("MD_SecurityConstraintsUseLimitation")
            .add("MD_SecurityConstraintsUseLimitationObject")
            .add("overview")
            .add("sourceDescription")
            .add("MD_ConstraintsUseLimitation")
            .add("MD_ConstraintsUseLimitationObject")
            .add("resourceType")
            .add("type")
            .add("resourceDate")
            .add("link")
            .add("linkProtocol")
            .add("crsDetails")
            .add("format")
            .add("orderingInstructionsObject")
            .add("contact")
            .add("contactForResource")
            .add("contactForProcessing")
            .add("contactForDistribution")
            .add("OrgForResource")
            .add("specificationConformance")
            .add("processSteps")
            .add("measure")
            .add("resourceProviderOrgForResource")
            .add("resourceVerticalRange")
            .add("resourceTemporalDateRange")
            .add("resourceTemporalExtentDateRange")
            .add("resourceTemporalExtentDetails")
            .add("licenseObject")
            .build();
        booleanFields = ImmutableSet.<String>builder()
            .add("hasxlinks")
            .add("hasInspireTheme")
            .add("hasOverview")
            .add(Geonet.IndexFieldNames.HASXLINKS)
            .add(INDEXING_ERROR_FIELD)
            .add("isHarvested")
            .add("isPublishedToAll")
            .add("isPublishedToIntranet")
            .add("isPublishedToGuest")
            .add("isSchemaValid")
            .add("isAboveThreshold")
            .add("isOpenData")
            .build();
        booleanValues = ImmutableSet.<String>builder()
            .add("1")
            .add("y")
            .add("true")
            .build();
    }

    /**
     * Convert document to JSON.
     */
    public ObjectNode documentToJson(Element xml) {
        ObjectNode doc = new ObjectMapper().createObjectNode();
        ObjectMapper mapper = new ObjectMapper();

        List<String> elementNames = new ArrayList<>();
        List<Element> fields = xml.getChildren();

        // Loop on doc fields
        for (Element currentField : fields) {
            String name = currentField.getName();

            if (elementNames.contains(name)) {
                continue;
            }

            // JSON object may be generated in the XSL processing.
            // In such case an object type attribute is set.
            boolean isObject = "object".equals(currentField.getAttributeValue("type"));

            // Register list of already processed names
            elementNames.add(name);

            String propertyName = getPropertyName(name);
            List<Element> nodeElements = xml.getChildren(name);

            boolean isArray = nodeElements.size() > 1
                || arrayFields.contains(propertyName)
                || propertyName.endsWith("DateForResource")
                || propertyName.startsWith("cl_");

            if (isArray) {
                ArrayNode arrayNode = doc.putArray(propertyName);
                for (Element node : nodeElements) {
                    if (isObject) {
                        try {
                            arrayNode.add(
                                mapper.readTree(node.getTextNormalize()));
                        } catch (IOException e) {
                            LOGGER.error("Parsing invalid JSON node {} for property {}. Error is: {}",
                                    node.getTextNormalize(), propertyName, e.getMessage());
                        }
                    } else {
                        arrayNode.add(
                            booleanFields.contains(propertyName) ?
                                parseBoolean(node.getTextNormalize()) :
                                node.getText());

                    }

                }
            } else if (isObject) {
                try {
                    doc.set(propertyName,
                        mapper.readTree(
                            nodeElements.get(0).getTextNormalize()
                        ));
                } catch (IOException e) {
                    LOGGER.error("Parsing invalid JSON node {} for property {}. Error is: {}",
                            nodeElements.get(0).getTextNormalize(), propertyName, e.getMessage());
                }
            } else {
                doc.put(propertyName,
                    booleanFields.contains(propertyName) ?
                        parseBoolean(nodeElements.get(0).getTextNormalize()) :
                        nodeElements.get(0).getText());
            }
        }
        return doc;
    }


    /** Field starting with _ not supported in Kibana
     * Those are usually GN internal fields
     */
    private String getPropertyName(String name) {
        return name.startsWith("_") ? name.substring(1) : name;
    }

    /*
     * Normalize various GN boolean value to only true/false allowed in boolean fields in ES
     */
    private String parseBoolean(String value) {
        return String.valueOf(booleanValues.contains(value));
    }

    @Override
    public void forceIndexChanges() {
        sendDocumentsToIndex();
    }

    @Override
    public boolean rebuildIndex(ServiceContext context,
                                boolean reset, String bucket) throws Exception {
        IMetadataIndexer metadataIndexer = context.getBean(IMetadataIndexer.class);
        IMetadataUtils metadataRepository = context.getBean(IMetadataUtils.class);

        if (reset) {
            clearIndex();
        }

        if (StringUtils.isNotBlank(bucket)) {
            ArrayList<String> listOfIdsToIndex = new ArrayList<>();
            UserSession session = context.getUserSession();
            SelectionManager sm = SelectionManager.getManager(session);

            synchronized (sm.getSelection(bucket)) {
                for (Iterator<String> iter = sm.getSelection(bucket).iterator();
                     iter.hasNext(); ) {
                    String uuid = iter.next();
                    for (AbstractMetadata metadata : metadataRepository.findAllByUuid(uuid)) {
                        String indexKey = uuid;
                        if (metadata instanceof MetadataDraft) {
                            indexKey += "-draft";
                        }

                        listOfIdsToIndex.add(indexKey);
                    }

                    if (!metadataRepository.existsMetadataUuid(uuid)) {
                        LOGGER.warn("Selection contains uuid '{}' not found in database", uuid);
                    }
                }
            }
            for (String id : listOfIdsToIndex) {
                metadataIndexer.indexMetadata(id, false, IndexingMode.full);
            }
        } else {
            final Specification<Metadata> metadataSpec =
                Specification.where((Specification<Metadata>) MetadataSpecs.isType(MetadataType.METADATA))
                    .or((Specification<Metadata>) MetadataSpecs.isType(MetadataType.TEMPLATE));
            final List<Integer> metadataIds = metadataRepository.findAllIdsBy(
                Specification.where(metadataSpec)
            );
            for (Integer id : metadataIds) {
                metadataIndexer.indexMetadata(id + "", false, IndexingMode.full);
            }
        }
        sendDocumentsToIndex();
        return true;
    }

    public Map<String, Object> getDocument(String uuid) throws Exception {
        return client.getDocument(defaultIndex, uuid);
    }

    public SearchResponse query(String luceneQuery, String filterQuery, int startPosition, int maxRecords) throws Exception {
        return client.query(defaultIndex, luceneQuery, filterQuery, new HashSet<>(), new HashMap<>(), startPosition, maxRecords);
    }

    public SearchResponse query(String luceneQuery, String filterQuery, int startPosition, int maxRecords, List<SortOptions> sort) throws Exception {
        return client.query(defaultIndex, luceneQuery, filterQuery, new HashSet<>(), new HashMap<>(), startPosition, maxRecords, sort);
    }

    public SearchResponse query(String luceneQuery, String filterQuery, Set<String> includedFields,
                                int from, int size) throws Exception {
        return client.query(defaultIndex, luceneQuery, filterQuery, includedFields, from, size);
    }

    public SearchResponse query(String luceneQuery, String filterQuery, Set<String> includedFields,
                                Map<String, String> scriptedFields,
                                int from, int size) throws Exception {
        return client.query(defaultIndex, luceneQuery, filterQuery, includedFields, scriptedFields, from, size);
    }

    public SearchResponse query(JsonNode jsonRequest, Set<String> includedFields,
                                int from, int size, List<SortOptions> sort) throws Exception {
        // TODO: Review postFilterBuilder
        return client.query(defaultIndex, jsonRequest, null, includedFields, new HashMap<>(), from, size, sort);
    }

    public SearchResponse query(JsonNode jsonRequest, Set<String> includedFields,
                                int from, int size) throws Exception {
        // TODO: Review postFilterBuilder
        return client.query(defaultIndex, jsonRequest, null, includedFields, from, size);
    }

    /**
     * Get the values for a set of index fields.
     *
     * @param id     The record to lookup
     * @param fields The fields to lookup
     */
    public Map<String, String> getFieldsValues(String id, Set<String> fields) throws IOException {
        return client.getFieldsValues(defaultIndex, id, fields);
    }

    /**
     * Get the values for a set of index fields and a specified language for multilingual fields.
     * If there is no match for the language the default is used.
     *
     * @param id       The record to lookup
     * @param fields   The fields to lookup
     * @param language The language code to lookup for multilingual fields.
     */
    public Map<String, String> getFieldsValues(String id, Set<String> fields, String language) throws IOException {
        return client.getFieldsValues(defaultIndex, id, fields, language);
    }

    public void clearIndex() throws Exception {
        client.deleteByQuery(defaultIndex, "*:*");
    }

    static ImmutableSet<String> docsChangeIncludedFields;

    static {
        docsChangeIncludedFields = ImmutableSet.<String>builder()
            .add(Geonet.IndexFieldNames.ID)
            .add(Geonet.IndexFieldNames.DATABASE_CHANGE_DATE).build();
    }

    @Override
    public Map<String, String> getDocsChangeDate() throws Exception {
        // TODO: Response could be large
        // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/master/java-rest-high-search-scroll.html
        final Map<String, String> docs = new HashMap<>();
        try {
            int from = 0;
            SettingInfo si = ApplicationContextHolder.get().getBean(SettingInfo.class);
            int size = Integer.parseInt(si.getSelectionMaxRecords());

            final SearchResponse response = client.query(defaultIndex, "*", null, docsChangeIncludedFields, from, size);

            ObjectMapper objectMapper = new ObjectMapper();

            response.hits().hits().forEach(r -> {
                Hit h = (Hit) r;

                docs.put(h.id(), (String) objectMapper.convertValue(h.source(), Map.class).get(Geonet.IndexFieldNames.DATABASE_CHANGE_DATE));
            });
        } catch (Exception e) {
            LOGGER.error("Error while collecting all documents: {}", e.getMessage(), e);
            throw e;
        }
        return docs;
    }

    @Override
    public ISODate getDocChangeDate(String mdId) throws Exception {
        int from = 0;
        SettingInfo si = ApplicationContextHolder.get().getBean(SettingInfo.class);
        int size = Integer.parseInt(si.getSelectionMaxRecords());

        final SearchResponse response = client.query(defaultIndex, "_id:" + mdId, null, docsChangeIncludedFields, from, size);

        if (response.hits().hits().size() == 1) {
            Hit hit = (Hit) response.hits().hits().get(0);
            ObjectMapper objectMapper = new ObjectMapper();

            String date =
                (String) objectMapper.convertValue(hit.source(), Map.class).get(Geonet.IndexFieldNames.DATABASE_CHANGE_DATE);
            return date != null ? new ISODate(date) : null;
        } else {
            return null;
        }
    }

    @Override
    public void delete(String txt) throws Exception {
        client.deleteByQuery(defaultIndex, txt);
    }

    @Override
    public void delete(List<Integer> metadataIds) throws Exception {
        metadataIds.forEach(metadataId -> {
            try {
                this.delete(String.format("+id:%d", metadataId));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public long getNumDocs(String query) throws Exception {
        if (StringUtils.isBlank(query)) {
            query = "*:*";
        }
        return client.query(defaultIndex, query, null, new HashSet<>(), 0, 0)
            .hits().total().value();
    }

    public EsRestClient getClient() {
        return client;
    }

    /**
     * Only for UTs
     */
    void setClient(EsRestClient client) {
        this.client = client;
    }

    public void setIndexList(Map<String, String> indexList) {
        this.indexList = indexList;
    }

    public Map<String, String> getIndexList() {
        return indexList;
    }

    public static String analyzeField(String analyzer,
                                      String fieldValue) {

        return EsRestClient.analyzeField(
            ApplicationContextHolder.get().getBean(EsSearchManager.class).getDefaultIndex(),
            analyzer,
            fieldValue);
    }

    public boolean isIndexing() {
        return listOfDocumentsToIndex.size() > 0;
    }

    public boolean isIndexWritable(String indexName) throws IOException, ElasticsearchException {
        String indexBlockRead = "index.blocks.read_only_allow_delete";

        GetIndicesSettingsRequest request = GetIndicesSettingsRequest.of(
                b -> b.index(indexName)
                        .name(indexBlockRead)
        );

        GetIndicesSettingsResponse settings = this.client.getClient()
                .indices().getSettings(request);

        IndexState indexState = settings.get(indexBlockRead);

        return (indexState != null) && "true".equals(indexState.toString());
    }


    /**
     * Make a JSON Object that properly represents an indexingErrorMsg, to be used in the index.
     *
     * @param type either 'error' or 'warning'
     * @param string a string that is translatable (see, e.g., en-search.json)
     * @param values values that replace the placeholders in the `string` parameter
     * @return a json object that represents an indexingErrorMsg
     */
    public ObjectNode createIndexingErrorMsgObject(String string, String type, Map<String, Object> values) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode indexingErrorMsg = objectMapper.createObjectNode();
        indexingErrorMsg.put("string", string);
        indexingErrorMsg.put("type", type);
        ObjectNode valuesObject = objectMapper.createObjectNode();
        values.forEach((k,v) -> valuesObject.put(k, String.valueOf(v)));
        indexingErrorMsg.set("values", valuesObject);
        return indexingErrorMsg;
    }

    /**
     * Create an Element that represents an indexingErrorMsg object, to be used in the index.
     *
     * @param type either 'error' or 'warning'
     * @param string a string that is translatable (see, e.g., en-search.json)
     * @param values values that replace the placeholders in the `string` parameter
     * @return an Element that represents an indexingErrorMsg
     */
    public Element createIndexingErrorMsgElement(String string, String type, Map<String, Object> values) {
        return new Element(INDEXING_ERROR_MSG)
            .setText(createIndexingErrorMsgObject(string, type, values).toString())
            .setAttribute("type", "object");
    }
}
