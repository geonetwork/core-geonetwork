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

package org.fao.geonet.kernel.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
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

import static org.elasticsearch.rest.RestStatus.*;
import static org.fao.geonet.constants.Geonet.IndexFieldNames.IS_TEMPLATE;


public class EsSearchManager implements ISearchManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(Geonet.INDEX_ENGINE);

    public static final String ID = "id";

    public static final String SCHEMA_INDEX_XSLT_FOLDER = "index-fields";
    public static final String SCHEMA_INDEX_XSTL_FILENAME = "index.xsl";
    public static final String SCHEMA_INDEX_SUBTEMPLATE_XSTL_FILENAME = "index-subtemplate.xsl";
    public static final String FIELDNAME = "name";
    public static final String FIELDSTRING = "string";

    public static Map<String, String> relatedIndexFields;
    public static Set<String> FIELDLIST_CORE;
    public static Set<String> FIELDLIST_UUID;

    static {
        FIELDLIST_UUID = ImmutableSet.<String>builder()
            .add(Geonet.IndexFieldNames.UUID).build();

        relatedIndexFields = ImmutableMap.<String, String>builder()
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
    public EsRestClient client;

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
                xsltForIndexing.toString()));
        }
        return xsltForIndexing;
    }

    private void addMDFields(Element doc, Path schemaDir, Element metadata, MetadataType metadataType) {
        final Path styleSheet = getXSLTForIndexing(schemaDir, metadataType);
        try {
            Element fields = Xml.transform(metadata, styleSheet);
            /* Generates something like that:
            <doc>
              <field name="toto">Contenu</field>
            </doc>*/
            for (Element field : (List<Element>) fields.getChildren()) {
                doc.addContent((Element) field.clone());
            }
        } catch (Exception e) {
            LOGGER.error("Indexing stylesheet contains errors: {} \n  Marking the metadata as _indexingError=1 in index", e.getMessage());
            doc.addContent(new Element(IndexFields.INDEXING_ERROR_FIELD).setText("true"));
            doc.addContent(new Element(IndexFields.INDEXING_ERROR_MSG).setText("GNIDX-XSL||" + e.getMessage()));
            doc.addContent(new Element(IndexFields.DRAFT).setText("n"));

            StringBuilder sb = new StringBuilder();
            allText(metadata, sb);
            doc.addContent(new Element("_text_").setText(sb.toString()));
        }
    }

    private void allText(Element metadata, StringBuilder sb) {
        String text = metadata.getText().trim();
        if (text.length() > 0) {
            if (sb.length() > 0)
                sb.append(" ");
            sb.append(text);
        }
        @SuppressWarnings("unchecked")
        List<Element> children = metadata.getChildren();
        for (Element aChildren : children) {
            allText(aChildren, sb);
        }
    }

    private void addMoreFields(Element doc, Multimap<String, Object> fields) {
        fields.entries().forEach(e -> {
            doc.addContent(new Element(e.getKey())
                .setText(String.valueOf(e.getValue())));
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
                    if (indices.isPresent() ?
                        indices.get().contains(e) :
                        true) {
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
                DeleteIndexRequest request = new DeleteIndexRequest(indexName);
                AcknowledgedResponse deleteIndexResponse = client.getClient().indices().delete(request, RequestOptions.DEFAULT);
                if (deleteIndexResponse.isAcknowledged()) {
                    LOGGER.debug("Index '{}' removed.", new Object[]{indexName});
                }
            } catch (Exception e) {
                // index does not exist ?
                LOGGER.debug("Error during index '{}' removal. Error is: {}", new Object[]{indexName, e.getMessage()});
            }
        }

        // Check index exist first
        GetIndexRequest request = new GetIndexRequest(indexName);
        try {
            boolean exists = client.getClient().indices().exists(request, RequestOptions.DEFAULT);
            if (exists && !dropIndexFirst) {
                return;
            }


            if (!exists || dropIndexFirst) {
                // Check version of the index - how ?

                // Create it if not
                Path indexConfiguration = dataDirectory.getIndexConfigDir().resolve(indexId + ".json");
                if (Files.exists(indexConfiguration)) {
                    String configuration;
                    try (InputStream is = Files.newInputStream(indexConfiguration, StandardOpenOption.READ)) {
                        configuration = IOUtils.toString(is);
                    }

                    CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
                    createIndexRequest.source(configuration, XContentType.JSON);
                    CreateIndexResponse createIndexResponse = client.getClient().indices().create(createIndexRequest, RequestOptions.DEFAULT);

                    if (createIndexResponse.isAcknowledged()) {
                        LOGGER.debug("Index '{}' created", new Object[]{indexName});
                    } else {
                        final String message = String.format("Index '%s' was not created. Error is: %s", indexName, createIndexResponse.toString());
                        LOGGER.error(message);
                        throw new IllegalStateException(message);
                    }
                } else {
                    throw new FileNotFoundException(String.format(
                        "Index configuration file '%s' not found in data directory for building index with name '%s'. Create one or copy the default one.",
                        indexConfiguration.toAbsolutePath(),
                        indexName));
                }
            }
        } catch (Exception cnce) {
            final String message = String.format("Could not connect to index '%s'. Error is %s. Is the index server  up and running?",
                defaultIndex, cnce.getMessage());
            LOGGER.error(message);
            throw new IOException(message);
        }
    }

    @Override
    public void end() {
    }

    public UpdateResponse updateFields(String id, Map<String, Object> fields) throws Exception {
        fields.put("indexingDate", new Date());
        UpdateRequest updateRequest = new UpdateRequest(defaultIndex, id).doc(fields);
        return client.getClient().update(updateRequest, RequestOptions.DEFAULT);
    }

    public BulkResponse updateFields(String id, Multimap<String, Object> fields, Set<String> fieldsToRemove) throws Exception {
        fields.put("indexingDate", new Date());
        BulkRequest bulkrequest = new BulkRequest();
        StringBuffer script = new StringBuffer();
        fieldsToRemove.forEach(f ->
            script.append(String.format("ctx._source.remove('%s');", f)));

        UpdateRequest deleteFieldRequest =
            new UpdateRequest(defaultIndex, id).script(new Script(ScriptType.INLINE,
                "painless",
                script.toString(),
                Collections.emptyMap()));
        bulkrequest.add(deleteFieldRequest);
        Map<String, Object> fieldMap = new HashMap<>();
        fields.asMap().forEach((e, v) -> fieldMap.put(e, v.toArray()));
        UpdateRequest addFieldRequest = new UpdateRequest(defaultIndex, id)
            .doc(fieldMap);
        bulkrequest.add(addFieldRequest);
        return client.getClient().bulk(bulkrequest, RequestOptions.DEFAULT);
    }

    public void updateFieldsAsynch(String id, Map<String, Object> fields) throws Exception {
        fields.put("indexingDate", new Date());
        UpdateRequest request = new UpdateRequest(defaultIndex, id).doc(fields);
        ActionListener listener = new ActionListener<UpdateResponse>() {
            @Override
            public void onResponse(UpdateResponse updateResponse) {
            }

            @Override
            public void onFailure(Exception e) {
            }
        };
        client.getClient().updateAsync(request, RequestOptions.DEFAULT, listener);
    }

    public UpdateResponse updateField(String id, String field, Object value) throws Exception {
        Map<String, Object> updates = new HashMap<>(2);
        updates.put(getPropertyName(field), value);
        updates.put("indexingDate", new Date());
        return updateFields(id, updates);
    }

    public void updateFieldAsynch(String id, String field, Object value) throws Exception {
        Map<String, Object> updates = new HashMap<>(2);
        updates.put(getPropertyName(field), value);
        updates.put("indexingDate", new Date());
        updateFieldsAsynch(id, updates);
    }

    @Autowired
    SourceRepository sourceRepository;

    @Override
    public void index(Path schemaDir, Element metadata, String id,
                      Multimap<String, Object> dbFields,
                      MetadataType metadataType, String root,
                      boolean forceRefreshReaders) throws Exception {

        Element docs = new Element("doc");
        if (schemaDir != null) {
            addMDFields(docs, schemaDir, metadata, metadataType);
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

        String jsonDocument = mapper.writeValueAsString(doc);

        if (forceRefreshReaders) {
            HashMap<String, String> document = new HashMap<>();
            document.put(id, jsonDocument);
            final BulkResponse bulkItemResponses = client.bulkRequest(defaultIndex, document);
            checkIndexResponse(bulkItemResponses, document);
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
        if (documents.size() > 0) {
            try {
                final BulkResponse bulkItemResponses = client
                    .bulkRequest(defaultIndex, documents);
                checkIndexResponse(bulkItemResponses, documents);
            } catch (Exception e) {
                LOGGER.error(
                    "An error occurred while indexing {} documents in current indexing list. Error is {}.",
                    new Object[]{listOfDocumentsToIndex.size(), e.getMessage()});
            } finally {
                //                listOfDocumentsToIndex.clear();
            }
        }
    }

    private void checkIndexResponse(BulkResponse bulkItemResponses,
                                    Map<String, String> documents) throws IOException {
        if (bulkItemResponses.hasFailures()) {
            Map<String, String> listErrorOfDocumentsToIndex = new HashMap<>(bulkItemResponses.getItems().length);
            List<String> errorDocumentIds = new ArrayList<>();
            // Add information in index that some items were not properly indexed
            Arrays.stream(bulkItemResponses.getItems()).forEach(e -> {
                if (e.status() != OK
                    && e.status() != CREATED) {
                    errorDocumentIds.add(e.getId());
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode docWithErrorInfo = mapper.createObjectNode();
                    String resourceTitle = String.format("Document #%s", e.getId());
                    String id = "";
                    String isTemplate = "";

                    String failureDoc = documents.get(e.getId());
                    try {
                        JsonNode node = mapper.readTree(failureDoc);
                        resourceTitle = node.get("resourceTitleObject").get("default").asText();
                        id = node.get(IndexFields.DBID).asText();
                        isTemplate = node.get(IS_TEMPLATE).asText();
                    } catch (Exception ignoredException) {
                    }
                    docWithErrorInfo.put(IndexFields.DBID, id);
                    docWithErrorInfo.put(IndexFields.RESOURCE_TITLE, resourceTitle);
                    docWithErrorInfo.put(IS_TEMPLATE, isTemplate);
                    docWithErrorInfo.put(IndexFields.DRAFT, "n");
                    docWithErrorInfo.put(IndexFields.INDEXING_ERROR_FIELD, "true");
                    docWithErrorInfo.put(IndexFields.INDEXING_ERROR_MSG, e.getFailureMessage());
                    // TODO: Report the JSON which was causing the error ?

                    LOGGER.error("Document with error #{}: {}.",
                        new Object[]{e.getId(), e.getFailureMessage()});
                    LOGGER.error(failureDoc);

                    try {
                        listErrorOfDocumentsToIndex.put(e.getId(), mapper.writeValueAsString(docWithErrorInfo));
                    } catch (JsonProcessingException e1) {
                        LOGGER.error("Generated document for the index is not properly formatted. Check document #{}: {}.",
                            new Object[]{e.getId(), e1.getMessage()});
                    }
                }
            });

            if (listErrorOfDocumentsToIndex.size() > 0) {
                BulkResponse response = client.bulkRequest(defaultIndex, listErrorOfDocumentsToIndex);
                if (!(response.status().getStatus() != 201)) {
                    LOGGER.error("Failed to save error documents {}.",
                        new Object[]{errorDocumentIds.toArray().toString()});
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
            .add("topic")
            .add("cat")
            .add("keyword")
            .add("resourceCredit")
            .add("resourceCreditObject")
            .add("resolutionScaleDenominator")
            .add("resolutionDistance")
            .add("extentDescription")
            .add("inspireTheme")
            .add("inspireThemeUri")
            .add("inspireTheme_syn")
            .add("inspireAnnex")
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
            .add("MD_ConstraintsUseLimitation")
            .add("MD_ConstraintsUseLimitationObject")
            .add("resourceType")
            .add("type")
            .add("resourceDate")
            .add("link")
            .add("crsDetails")
            .add("format")
            .add("contact")
            .add("contactForResource")
            .add("OrgForResource")
            .add("resourceProviderOrgForResource")
            .add("resourceVerticalRange")
            .add("resourceTemporalDateRange")
            .add("resourceTemporalExtentDateRange")
            .build();
        booleanFields = ImmutableSet.<String>builder()
            .add("hasxlinks")
            .add("hasInspireTheme")
            .add("hasOverview")
            .add(IndexFields.HAS_ATOM)
            .add(Geonet.IndexFieldNames.HASXLINKS)
            .add("isHarvested")
            .add("isPublishedToAll")
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

        List<String> elementNames = new ArrayList();
        List<Element> fields = xml.getChildren();

        // Loop on doc fields
        for (Element currentField : fields) {
            String name = currentField.getName();

            // JSON object may be generated in the XSL processing.
            // In such case an object type attribute is set.
            boolean isObject = "object".equals(currentField.getAttributeValue("type"));

            if (elementNames.contains(name)) {
                continue;
            }

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
                                mapper.readTree(node.getText()));
                        } catch (IOException e) {
                            LOGGER.error("Parsing invalid JSON node {} for property {}. Error is: {}",
                                new Object[]{node.getTextNormalize(), propertyName, e.getMessage()});
                        }
                    } else {
                        arrayNode.add(
                            booleanFields.contains(propertyName) ?
                                parseBoolean(node.getTextNormalize()) :
                                node.getText());

                    }

                }
                continue;
            }

            if (name.equals("geom")) {
                try {
                    doc.put("geom", mapper.readTree(nodeElements.get(0).getTextNormalize()));
                } catch (IOException e) {
                    LOGGER.error("Parsing invalid geometry for JSON node {}. Error is: {}",
                        new Object[]{nodeElements.get(0).getTextNormalize(), e.getMessage()});
                }
                continue;
            }

            if (!name.startsWith("conformTo_")) { // Skip some fields causing errors / TODO
                if (isObject) {
                    try {
                        doc.set(propertyName,
                            mapper.readTree(
                                nodeElements.get(0).getTextNormalize()
                            ));
                    } catch (IOException e) {
                        LOGGER.error("Parsing invalid JSON node {} for property {}. Error is: {}",
                            new Object[]{nodeElements.get(0).getTextNormalize(), propertyName, e.getMessage()});
                    }
                } else {
                    doc.put(propertyName,
                        booleanFields.contains(propertyName) ?
                            parseBoolean(nodeElements.get(0).getTextNormalize()) :
                            nodeElements.get(0).getText());
                }

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
    public boolean rebuildIndex(ServiceContext context, boolean xlinks,
                                boolean reset, String bucket) throws Exception {
        DataManager dataMan = context.getBean(DataManager.class);
        IMetadataUtils metadataRepository = context.getBean(IMetadataUtils.class);

        if (reset) {
            clearIndex();
        }

        if (StringUtils.isNotBlank(bucket)) {
            ArrayList<String> listOfIdsToIndex = new ArrayList<String>();
            UserSession session = context.getUserSession();
            SelectionManager sm = SelectionManager.getManager(session);

            synchronized (sm.getSelection(bucket)) {
                for (Iterator<String> iter = sm.getSelection(bucket).iterator();
                     iter.hasNext(); ) {
                    String uuid = (String) iter.next();
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
                dataMan.indexMetadata(id + "", false);
            }
        } else {
            final Specification<Metadata> metadataSpec =
                Specification.where((Specification<Metadata>) MetadataSpecs.isType(MetadataType.METADATA))
                    .or((Specification<Metadata>) MetadataSpecs.isType(MetadataType.TEMPLATE));
            final List<Integer> metadataIds = metadataRepository.findAllIdsBy(
                Specification.where(metadataSpec)
            );
            for (Integer id : metadataIds) {
                dataMan.indexMetadata(id + "", false);
            }
        }
        sendDocumentsToIndex();
        return true;
    }

    public Map<String, Object> getDocument(String uuid) throws Exception {
        return client.getDocument(defaultIndex, uuid);
    }

    public SearchResponse query(String luceneQuery, String filterQuery, int startPosition, int maxRecords) throws Exception {
        return client.query(defaultIndex, luceneQuery, filterQuery, new HashSet<String>(), startPosition, maxRecords);
    }

    public SearchResponse query(String luceneQuery, String filterQuery, int startPosition, int maxRecords, List<SortBuilder<FieldSortBuilder>> sort) throws Exception {
        return client.query(defaultIndex, luceneQuery, filterQuery, new HashSet<String>(), startPosition, maxRecords, sort);
    }

    public SearchResponse query(String luceneQuery, String filterQuery, Set<String> includedFields,
                                int from, int size) throws Exception {
        return client.query(defaultIndex, luceneQuery, filterQuery, includedFields, from, size);
    }

    public SearchResponse query(JsonNode jsonRequest, Set<String> includedFields,
                                int from, int size, List<SortBuilder<FieldSortBuilder>> sort) throws Exception {
        // TODO: Review postFilterBuilder
        return client.query(defaultIndex, jsonRequest, null, includedFields, from, size, sort);
    }

    public SearchResponse query(JsonNode jsonRequest, Set<String> includedFields,
                                int from, int size) throws Exception {
        // TODO: Review postFilterBuilder
        return client.query(defaultIndex, jsonRequest, null, includedFields, from, size);
    }

    public Map<String, String> getFieldsValues(String id, Set<String> fields) throws Exception {
        return client.getFieldsValues(defaultIndex, id, fields);
    }


    public void clearIndex() throws Exception {
        client.deleteByQuery(defaultIndex, "*:*");
    }

//    public void iterateQuery(SolrQuery params, final Consumer<SolrDocument> callback) throws IOException, SolrServerException {
//        final MutableLong pos = new MutableLong(0);
//        final MutableLong last = new MutableLong(1);
//        while (pos.longValue() < last.longValue()) {
//            params.setStart(pos.intValue());
//            client.queryAndStreamResponse(params, new StreamingResponseCallback() {
//                @Override
//                public void streamSolrDocument(SolrDocument doc) {
//                    pos.add(1);
//                    callback.accept(doc);
//                }
//
//                @Override
//                public void streamDocListInfo(long numFound, long start, Float maxScore) {
//                    last.setValue(numFound);
//                }
//            });
//        }
//    }

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

        int from = 0;
        SettingInfo si = ApplicationContextHolder.get().getBean(SettingInfo.class);
        int size = Integer.parseInt(si.getSelectionMaxRecords());

        final SearchResponse response = client.query(defaultIndex, "*", null, docsChangeIncludedFields, from, size);

        final Map<String, String> docs = new HashMap<>();
        response.getHits().forEach(r -> {
            docs.put(r.getId(), (String) r.getSourceAsMap().get(Geonet.IndexFieldNames.DATABASE_CHANGE_DATE));
        });
        return docs;
    }

    @Override
    public ISODate getDocChangeDate(String mdId) throws Exception {
        int from = 0;
        SettingInfo si = ApplicationContextHolder.get().getBean(SettingInfo.class);
        int size = Integer.parseInt(si.getSelectionMaxRecords());

        final SearchResponse response = client.query(defaultIndex, "_id:" + mdId, null, docsChangeIncludedFields, from, size);

        if (response.getHits().getTotalHits().value == 1) {
            String date =
                (String) response.getHits().getAt(0).getSourceAsMap().get(Geonet.IndexFieldNames.DATABASE_CHANGE_DATE);
            return date != null ? new ISODate(date) : null;
        } else {
            return null;
        }
    }

//    public SolrDocument getDocFieldValue(String query, String... field) throws IOException, SolrServerException {
//        final SolrQuery params = new SolrQuery(query);
//        params.setFilterQueries(DOC_TYPE + ":metadata");
//        params.setFields(field);
//        QueryResponse response = client.query(params);
//        final SolrDocumentList results = response.getResults();
//        if (results.size() == 0) {
//            return null;
//        } else {
//            return results.get(0);
//        }
//    }
//
//    public SolrDocumentList getDocsFieldValue(String query, String... field) throws IOException, SolrServerException {
//        final SolrQuery params = new SolrQuery(query);
//        params.setFilterQueries(DOC_TYPE + ":metadata");
//        params.setFields(field);
//        QueryResponse response = client.query(params);
//        return response.getResults();
//    }
//
//    public List<String> getDocsUuids(String query, Integer rows) throws IOException, SolrServerException {
//        final SolrQuery solrQuery = new SolrQuery(query == null ? "*:*" : query);
//        solrQuery.setFilterQueries(DOC_TYPE + ":metadata");
//        solrQuery.setFields(IndexFields.UUID);
//        if (rows != null) {
//            solrQuery.setRows(rows);
//        }
//        final List<String> result = new ArrayList<>();
//        iterateQuery(solrQuery, doc ->
//            result.add(doc.getFieldValue(IndexFields.UUID).toString()));
//        return result;
//    }

    @Override
    public Set<Integer> getDocsWithXLinks() throws Exception {
//        final SolrQuery params = new SolrQuery("*:*");
//        params.setFilterQueries(DOC_TYPE + ":metadata");
//        params.setFilterQueries(Geonet.IndexFieldNames.HASXLINKS + ":1");
//        params.setFields(ID);
//        Set<Integer> result = new HashSet<>();
//        iterateQuery(params,
//            doc -> result.add(convertInteger(doc.getFieldValue(ID))));
        return null;
    }

    @Override
    public void delete(String txt) throws Exception {
        DeleteByQueryRequest request = new DeleteByQueryRequest();
        request.indices(defaultIndex);
        request.setQuery(new QueryStringQueryBuilder(txt));
        request.setRefresh(true);
        client.getClient().deleteByQuery(request, RequestOptions.DEFAULT);
    }

    @Override
    public void delete(List<Integer> metadataIds) throws Exception {
        metadataIds.stream().forEach(metadataId -> {
            try {
                this.delete(String.format("+id:%d", metadataId));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public long getNumDocs() throws Exception {
        return getNumDocs("");
    }

    public long getNumDocs(String query) throws Exception {
        if (StringUtils.isBlank(query)) {
            query = "*:*";
        }

        int from = 0;
        SettingInfo si = ApplicationContextHolder.get().getBean(SettingInfo.class);
        int size = Integer.parseInt(si.getSelectionMaxRecords());

        final SearchResponse response = client.query(defaultIndex, query, null, docsChangeIncludedFields, from, size);
        return response.getHits().getTotalHits().value;
    }

//    public List<FacetField.Count> getDocFieldValues(String indexField,
//                                                    String query,
//                                                    boolean missing,
//                                                    Integer limit,
//                                                    String sort) throws IOException {
//        final SolrQuery solrQuery = new SolrQuery(query == null ? "*:*" : query)
//            .setFilterQueries(DOC_TYPE + ":metadata")
//            .setRows(0)
//            .setFacet(true)
//            .setFacetMissing(missing)
//            .setFacetLimit(limit != null ? limit : 1000)
//            .setFacetSort(sort != null ? sort : "count") // or index
//            .addFacetField(indexField);
//        QueryResponse response = client.query(solrQuery);
//        return response.getFacetField(indexField).getValues();
//    }
//
//    public void updateRating(int metadataId, int newValue) throws IOException, SolrServerException {
//        updateField(metadataId, Geonet.IndexFieldNames.RATING, newValue, "set");
//    }

    public EsRestClient getClient() {
        return client;
    }

    /**
     * Only for UTs
     */
    void setClient(EsRestClient client) {
        this.client = client;
    }

    public List<Element> getDocs(String query, long start, long rows) throws IOException, JDOMException {
        final List<String> result = getDocIds(query, start, rows);
        List<Element> xmlDocs = new ArrayList<>(result.size());
        IMetadataUtils metadataRepository = ApplicationContextHolder.get().getBean(IMetadataUtils.class);
        for (String id : result) {
            AbstractMetadata metadata = metadataRepository.findOne(id);
            xmlDocs.add(metadata.getXmlData(false));
        }
        return xmlDocs;
    }

    public List<String> getDocIds(String query, long start, long rows) throws IOException, JDOMException {
//        final SolrQuery solrQuery = new SolrQuery(query == null ? "*:*" : query);
//        solrQuery.setFilterQueries(DOC_TYPE + ":metadata");
//        solrQuery.setFields(SolrSearchManager.ID);
//        if (start != null) {
//            solrQuery.setStart(start);
//        }
//        if (rows != null) {
//            solrQuery.setRows(rows);
//        }
//        QueryResponse response = client.query(solrQuery);
//        SolrDocumentList results = response.getResults();
//        List<String> idList = new ArrayList<>(results.size());
//        for (SolrDocument document : results) {
//            idList.add(document.getFieldValue(SolrSearchManager.ID).toString());
//        }
//        return idList;
        return null;
    }

    public List<Element> getAllDocs(String query) throws Exception {
        long hitsNumber = getNumDocs(query);
        return getDocs(query, 0, hitsNumber);
    }

    public List<String> getAllDocIds(String query) throws Exception {
        long hitsNumber = getNumDocs(query);
        return getDocIds(query, 0, hitsNumber);
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
}
