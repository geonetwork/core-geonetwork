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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import io.searchbox.client.JestResult;
import io.searchbox.core.Get;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Source;
import org.fao.geonet.es.EsClient;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class EsSearchManager implements ISearchManager {
    public static final String ID = "id";
    public static final String DOC_TYPE = "docType";
    public static final String SCHEMA_INDEX_XSLT_FOLDER = "index-fields";
    public static final String SCHEMA_INDEX_XSTL_FILENAME = "index.xsl";
    public static final String FIELDNAME = "name";
    public static final String FIELDSTRING = "string";

    @Value("${es.index.records:gn-records}")
    private String index = "records";

    /**
     * Index containing only public records.
     */
    @Value("${es.index.records_public:gn-records-public}")
    private String publicIndex = "records";

    @Value("${es.index.records.type:records}")
    private String indexType = "records";

    public String getIndex() {
        return index;
    }

    public String getPublicIndex() {
        return publicIndex;
    }

    public String getIndexType() {
        return indexType;
    }
    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    @Autowired
    private EsClient client;

    public static Path getXSLTForIndexing(Path schemaDir) {
        Path xsltForIndexing = schemaDir
            .resolve(SCHEMA_INDEX_XSLT_FOLDER).resolve(SCHEMA_INDEX_XSTL_FILENAME);
        if (!Files.exists(xsltForIndexing)) {
            throw new RuntimeException(String.format(
                "XSLT for schema indexing does not exist. Create file '%s'.",
                xsltForIndexing.toString()));
        }
        return xsltForIndexing;
    }

    private static void addMDFields(Element doc, Path schemaDir, Element metadata, String root) {
        final Path styleSheet = getXSLTForIndexing(schemaDir);
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
            Log.error(Geonet.INDEX_ENGINE,
                String.format("Indexing stylesheet contains errors: %s \n\t Marking the metadata as _indexingError=1 in index",
                    e.getMessage()));
            doc.addContent(new Element(IndexFields.INDEXING_ERROR_FIELD).setText("1"));
            doc.addContent(new Element(IndexFields.INDEXING_ERROR_MSG).setText("GNIDX-XSL||" + e.getMessage()));
            StringBuilder sb = new StringBuilder();
            allText(metadata, sb);
            doc.addContent(new Element("_text_").setText(sb.toString()));
        }
    }

    private static void allText(Element metadata, StringBuilder sb) {
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

    private static void addMoreFields(Element doc, List<Element> fields) {
        for (Element field : fields) {
            doc.addContent(new Element(field.getAttributeValue(FIELDNAME))
                .setText(field.getAttributeValue(FIELDSTRING)));
        }
    }

//    public static String convertDate(Object date) {
//        if (date != null) {
//            return new ISODate((Date) date).toString();
//        } else {
//            return null;
//        }
//    }

    public static Integer convertInteger(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else {
            return Integer.valueOf(value.toString());
        }
    }

    public static Element makeField(String name, String value) {
        Element field = new Element("Field");
        field.setAttribute(EsSearchManager.FIELDNAME, name);
        field.setAttribute(EsSearchManager.FIELDSTRING, value == null ? "" : value);
        return field;
    }

    /**
     * Creates a new XML field for the Lucene index and add it to the document.
     */
    public static void addField(Element xmlDoc, String name, String value, boolean store, boolean index) {
        Element field = makeField(name, value);
        xmlDoc.addContent(field);
    }

    @Override
    public void init(ServiceConfig handlerConfig) throws Exception {
    }

    @Override
    public void end() throws Exception {
    }

    @Override
    public MetaSearcher newSearcher(String stylesheetName) throws Exception {
        //TODO
        return null;
    }

    private int commitInterval = 200;
    private Map<String, String> listOfDocumentsToIndex = new HashMap<>();
    private Map<String, String> listOfPublicDocumentsToIndex = new HashMap<>();

    @Autowired
    SourceRepository sourceRepository;
    
    @Override
    public void index(Path schemaDir, Element metadata, String id, List<Element> moreFields,
                      MetadataType metadataType, String root, boolean forceRefreshReaders) throws Exception {

        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);

        Element docs = new Element("docs");
        Element allFields = new Element("doc");
        allFields.addContent(new Element(ID).setText(id));
        allFields.addContent(new Element(DOC_TYPE).setText("metadata"));
        addMDFields(allFields, schemaDir, metadata, root);
        addMoreFields(allFields, moreFields);


        docs.addContent(allFields);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode doc = documentToJson(docs).get(id);

        // ES does not allow a _source field
        String catalog = doc.get("source").asText();
        doc.remove("source");
        if (StringUtils.isNotEmpty(catalog)) {
            doc.put("sourceCatalogue", catalog);
            final Source source = sourceRepository.findOne(catalog);
            if (source != null) {
                source.getLabelTranslations()
                    .forEach((key, value) -> doc.put("sourceCatalogueName_lang" + key, value));
            }
        }
        doc.put("scope", settingManager.getSiteName());
        doc.put("harvesterUuid", settingManager.getSiteId());
        doc.put("harvesterId", settingManager.getNodeURL());
        String json = mapper.writeValueAsString(doc);
        if (doc.get("isPublishedToAll").asBoolean()) {
            listOfPublicDocumentsToIndex.put(id, json);
        }
        listOfDocumentsToIndex.put(id, json);
        if (listOfDocumentsToIndex.size() == commitInterval) {
            sendDocumentsToIndex();
        }
    }

    private void sendDocumentsToIndex() throws IOException {
        synchronized (this) {
            if (listOfDocumentsToIndex.size() > 0) {
                client.bulkRequest(index, listOfDocumentsToIndex);
                if (StringUtils.isNotEmpty(publicIndex)) {
                    client.bulkRequest(publicIndex, listOfPublicDocumentsToIndex);
                    listOfPublicDocumentsToIndex.clear();
                }
                listOfDocumentsToIndex.clear();
            }
        }
    }
    private static ImmutableSet<String> booleanFields;
    private static ImmutableSet<String> booleanValues;

    static {
        booleanFields = ImmutableSet.<String>builder()
            .add("hasxlinks")
            .add("hasInspireTheme")
            .add("hasOverview")
            .add(IndexFields.HAS_ATOM)
            .add(Geonet.IndexFieldNames.HASXLINKS)
            .add("isHarvested")
            .add("isPublishedToAll")
            .add("isTemplate")
            .add("isValid")
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
    public Map<String, ObjectNode> documentToJson(Element xml) {
        ObjectMapper mapper = new ObjectMapper();

        List<Element> records = xml.getChildren("doc");
        Map<String, ObjectNode> listOfXcb = new HashMap<>();

        // Loop on docs
        for (int i = 0; i < records.size(); i++) {
            Element record = records.get(i);
            if (record != null && record instanceof Element) {
                ObjectNode doc = mapper.createObjectNode();
                String id = null;
                List<String> elementNames = new ArrayList();
                List<Element> fields = record.getChildren();

                // Loop on doc fields
                for (int j = 0; j < fields.size(); j++) {
                    Element currentField = fields.get(j);
                    String name = currentField.getName();

                    if (!elementNames.contains(name)) {
                        // Register list of already processed names
                        elementNames.add(name);

                        // JSON object may be generated in the XSL processing.
                        // In such case an object type attribute is set.
                        boolean isObject = "object".equals(currentField.getAttributeValue("type"));

                        List<Element> nodeElements = record.getChildren(name);
                        boolean isArray = nodeElements.size() > 1;

                        // Field starting with _ not supported in Kibana
                        // Those are usually GN internal fields
                        String propertyName = name.startsWith("_") ?
                            name.substring(1) : name;

                        ArrayNode arrayNode = null;
                        if (isArray) {
                            arrayNode = doc.putArray(propertyName);
                        }

                        // Group fields in array if needed
                        for (int k = 0; k < nodeElements.size(); k++) {
                            Element node = nodeElements.get(k);

                            if (name.equals("id")) {
                                id = node.getTextNormalize();
                            }

                            if (name.equals("geom")) {
                                continue;
                            }

                            if (isArray) {
                                if (isObject) {
                                    try {
                                        arrayNode.add(
                                            mapper.readTree(node.getTextNormalize()));
                                    } catch (IOException e) {
                                        // Invalid JSON object provided
                                        Log.error(Geonet.INDEX_ENGINE, e.getMessage(), e);
                                    }
                                } else {
                                    arrayNode.add(
                                        booleanFields.contains(propertyName) ?
                                            parseBoolean(node.getTextNormalize()) :
                                            node.getTextNormalize());
                                }
                            } else if (name.equals("geojson")) {
                                doc.put("geom", node.getTextNormalize());
                                // Skip some fields causing errors / TODO
                            } else if (!name.startsWith("conformTo_")) {
                                if (isObject) {
                                    try {
                                        doc.set(propertyName,
                                            mapper.readTree(
                                                nodeElements.get(0).getTextNormalize()
                                            ));
                                    } catch (IOException e) {
                                        // Invalid JSON object provided
                                        Log.error(Geonet.INDEX_ENGINE, e.getMessage(), e);
                                    }
                                } else {
                                    doc.put(
                                        propertyName,
                                        booleanFields.contains(propertyName) ?
                                            parseBoolean(node.getTextNormalize()) :
                                            node.getTextNormalize());
                                }
                            }
                        }
                    }
                }
                listOfXcb.put(id, doc);
            }
        }
        return listOfXcb;
    }

    /*
     * Normalize various GN boolean value to only true/false allowed in boolean fields in ES
     */
    private String parseBoolean(String value) {
        return String.valueOf(booleanValues.contains(value));
    }

    @Override
    public void forceIndexChanges() throws IOException {
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
                        listOfIdsToIndex.add(metadata.getId() + "");
                    }

                    if(!metadataRepository.existsMetadataUuid(uuid)) {
                        Log.warning(Geonet.INDEX_ENGINE, String.format(
                            "Selection contains uuid '%s' not found in database", uuid));
                    }
                }
            }
            for(String id : listOfIdsToIndex) {
                dataMan.indexMetadata(id + "", false, this);
            }
            sendDocumentsToIndex();
        } else {
            final Specifications<Metadata> metadataSpec =
                Specifications.where((Specification<Metadata>)MetadataSpecs.isType(MetadataType.METADATA))
                    .or((Specification<Metadata>)MetadataSpecs.isType(MetadataType.TEMPLATE));
            final List<Integer> metadataIds = metadataRepository.findAllIdsBy(
                Specifications.where(metadataSpec)
            );
            for(Integer id : metadataIds) {
                dataMan.indexMetadata(id + "", false, this);
            }
            sendDocumentsToIndex();
        }

        return true;
    }

    public void clearIndex() throws Exception {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
        client.deleteByQuery(index,
            "harvesterUuid:\\\"" + settingManager.getSiteId() + "\\\"");
        if (StringUtils.isNotEmpty(publicIndex)) {
            client.deleteByQuery(publicIndex,
                "harvesterUuid:\\\"" + settingManager.getSiteId() + "\\\"");
        }
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

    @Override
    public Map<String, String> getDocsChangeDate() throws Exception {
        String query = "{\"query\": {\"filtered\": {\"query_string\": \"*:*\"}}}";
        Search search = new Search.Builder(query).addIndex(index).addType(indexType).build();
        // TODO: limit to needed field
//        params.setFields(ID, Geonet.IndexFieldNames.DATABASE_CHANGE_DATE);
        SearchResult searchResult = client.getClient().execute(search);

//        final Map<String, String> result = new HashMap<>();
//        iterateQuery(searchResult.getHits(), doc ->
//            result.put(doc.getFieldValue(ID).toString(),
//                convertDate(doc.getFieldValue(Geonet.IndexFieldNames.DATABASE_CHANGE_DATE))));
        return null;
    }

    @Override
    public ISODate getDocChangeDate(String mdId) throws Exception {
        // TODO: limit to needed field
        Get get = new Get.Builder(index, mdId).type(index).build();
        JestResult result = client.getClient().execute(get);
        if (result != null) {
            JsonElement date =
                result.getJsonObject().get(Geonet.IndexFieldNames.DATABASE_CHANGE_DATE);
            return date != null ? new ISODate(date.getAsString()) : null;
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
        client.deleteByQuery(index, txt);
//        client.commit();
    }

    @Override
    public void delete(List<String> txts) throws Exception {
//        client.deleteById(txts);
//        client.commit();
    }

    @Override
    public void rescheduleOptimizer(Calendar beginAt, int interval) {

    }

    @Override
    public void disableOptimizer() {

    }

    public Long getNumDocs(String query) throws Exception {
        if (StringUtils.isBlank(query)) {
            query = "*:*";
        }
        String searchQuery = "{\"query\": {\"filtered\": {\"query_string\": \"" + query + "\"}}}";
        Search search = new Search.Builder(searchQuery).addIndex(index).addType(indexType).build();
        SearchResult searchResult = client.getClient().execute(search);
        return searchResult.getTotal();
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
//
//    public void incrementPopularity(int metadataId) throws IOException, SolrServerException {
//        //TODO: check that works
//        updateField(metadataId, Geonet.IndexFieldNames.POPULARITY, 1, "inc");
//    }
//
//    private void updateField(int metadataId, String fieldName, int newValue, String operator) throws IOException, SolrServerException {
//        SolrInputDocument doc = new SolrInputDocument();
//        doc.addField(ID, metadataId);
//        Map<String, Object> fieldModifier = new HashMap<>(1);
//        fieldModifier.put(operator, newValue);
//        doc.addField(fieldName, fieldModifier);
//        client.add(doc);
//        client.commit();
//    }

    public EsClient getClient() {
        return client;
    }

    /**
     * Only for UTs
     */
    void setClient(EsClient client) {
        this.client = client;
    }

    public List<Element> getDocs(String query, Integer start, Long rows) throws IOException, JDOMException {
        final List<String> result = getDocIds(query, start, rows);
        List<Element> xmlDocs = new ArrayList<>(result.size());
        IMetadataUtils metadataRepository = ApplicationContextHolder.get().getBean(IMetadataUtils.class);
        for (String id : result) {
            AbstractMetadata metadata = metadataRepository.findOne(id);
            xmlDocs.add(metadata.getXmlData(false));
        }
        return xmlDocs;
    }

    public List<String> getDocIds(String query, Integer start, Long rows) throws IOException, JDOMException {
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
        Long hitsNumber = getNumDocs(query);
        return getDocs(query, 0, hitsNumber);
    }

    public List<String> getAllDocIds(String query) throws Exception {
        Long hitsNumber = getNumDocs(query);
        return getDocIds(query, 0, hitsNumber);
    }

    public static String analyzeField(String analyzer,
                                      String fieldValue) {

        return EsClient.analyzeField(
            ApplicationContextHolder.get().getBean(EsSearchManager.class).getIndex(),
            analyzer,
            fieldValue);
    }
}
