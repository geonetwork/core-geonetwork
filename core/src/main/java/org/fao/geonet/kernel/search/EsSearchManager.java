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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonElement;
import io.searchbox.client.JestResult;
import io.searchbox.cluster.Health;
import io.searchbox.core.Get;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.IndicesExists;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.exceptions.NotFoundEx;
import org.fao.geonet.index.es.EsClient;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specifications;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.fao.geonet.kernel.search.IndexFields.*;

public class EsSearchManager implements ISearchManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(Geonet.INDEX_ENGINE);

    public static final String ID = "id";

    public static final String SCHEMA_INDEX_XSLT_FOLDER = "index-fields";
    public static final String SCHEMA_INDEX_XSTL_FILENAME = "index.xsl";
    public static final String FIELDNAME = "name";
    public static final String FIELDSTRING = "string";

    @Value("${es.index.records}")
    private String defaultIndex = "records";

    @Autowired
    public EsClient client;

    private int commitInterval = 200;

    // public for test, to be private or protected
    public Map<String, String> listOfDocumentsToIndex = new HashMap<>();
    private Map<String, String> indexList;

    public String getDefaultIndex() {
        return defaultIndex;
    }

    private Path getXSLTForIndexing(Path schemaDir) {
        Path xsltForIndexing = schemaDir
            .resolve(SCHEMA_INDEX_XSLT_FOLDER).resolve(SCHEMA_INDEX_XSTL_FILENAME);
        if (!Files.exists(xsltForIndexing)) {
            throw new RuntimeException(String.format(
                "XSLT for schema indexing does not exist. Create file '%s'.",
                xsltForIndexing.toString()));
        }
        return xsltForIndexing;
    }

    private void addMDFields(Element doc, Path schemaDir, Element metadata) {
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
            LOGGER.error("Indexing stylesheet contains errors: {} \n\t Marking the metadata as _indexingError=1 in index", e.getMessage());
            doc.addContent(new Element(IndexFields.INDEXING_ERROR_FIELD).setText("1"));
            doc.addContent(new Element(IndexFields.INDEXING_ERROR_MSG).setText("GNIDX-XSL||" + e.getMessage()));
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

    private void addMoreFields(Element doc, List<Element> fields) {
        for (Element field : fields) {
            doc.addContent(new Element(field.getAttributeValue(FIELDNAME))
                .setText(field.getAttributeValue(FIELDSTRING)));
        }
    }

    public Element makeField(String name, String value) {
        Element field = new Element("Field");
        field.setAttribute(EsSearchManager.FIELDNAME, name);
        field.setAttribute(EsSearchManager.FIELDSTRING, value == null ? "" : value);
        return field;
    }


    @Override
    public void init() throws Exception {
        if (indexList != null) {
            indexList.keySet().forEach(e -> {
                createIndexIfNotExist(e, indexList.get(e));
            });
        }
    }

    @Autowired
    private GeonetworkDataDirectory dataDirectory;

    public static final String INDEX_DIRECTORY = "index";

    private void createIndexIfNotExist(String indexId, String indexName) {
        try {
            // Check index exist first
            final IndicesExists request = new IndicesExists.Builder(indexId)
                .build();
            JestResult result = client.getClient().execute(request);
            if (result.getResponseCode() == 200) {
                return;
            } else if (result.getResponseCode() == 404) {
                // Check version of the index - how ?

                // Create it if not
                Path indexConfiguration = dataDirectory.getConfigDir().resolve(INDEX_DIRECTORY).resolve(indexName + ".json");
                if (Files.exists(indexConfiguration)) {

                    CreateIndex createIndex = new CreateIndex.Builder(indexName)
                        .settings(FileUtils.readFileToString(indexConfiguration.toFile()))
                        .build();

                    result = client.getClient().execute(createIndex);
                    if (result.isSucceeded()) {

                    } else {
                        throw new IllegalStateException(result.getErrorMessage());
                    }
                } else {
                    throw new FileNotFoundException(String.format(
                        "Index configuration file '%s' not found in data directory for building index with name '%s'. Create one or copy the default one.",
                        indexConfiguration.toAbsolutePath(),
                        indexName));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void end() {
    }

    @Override
    public void index(Path schemaDir, Element metadata, String id, List<Element> moreFields,
                      MetadataType metadataType, String root, boolean forceRefreshReaders) throws Exception {

        Element docs = new Element("doc");
        docs.addContent(new Element(ID).setText(id));
        addMDFields(docs, schemaDir, metadata);
        addMoreFields(docs, moreFields);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode doc = documentToJson(docs);

        // ES does not allow a _source field
        JsonNode source = doc.get("source");
        if (source != null) {
            String catalog = source.asText();
            doc.remove("source");
            doc.put(SOURCE_CATALOGUE, catalog);
        }
        doc.put(DOC_TYPE,"metadata");
        listOfDocumentsToIndex.put(id, mapper.writeValueAsString(doc));

        if (listOfDocumentsToIndex.size() == commitInterval || forceRefreshReaders) {
            sendDocumentsToIndex();
        }
    }

    private void sendDocumentsToIndex() throws IOException {
        synchronized (this) {
            if (listOfDocumentsToIndex.size() > 0) {
                client.bulkRequest(defaultIndex, listOfDocumentsToIndex);
                listOfDocumentsToIndex.clear();
            }
        }
    }

    /**
     * Convert document to JSON.
     */
    public ObjectNode documentToJson(Element xml) {
        ObjectNode doc = new ObjectMapper().createObjectNode();

        List<String> elementNames = new ArrayList();
        List<Element> fields = xml.getChildren();

        // Loop on doc fields
        for (Element currentField: fields) {
            String name = currentField.getName();

            if (elementNames.contains(name)) {
                continue;
            }

            // Register list of already processed names
            elementNames.add(name);
            // Field starting with _ not supported in Kibana
            // Those are usually GN internal fields
            String propertyName = name.startsWith("_") ? name.substring(1) : name;
            List<Element> nodeElements = xml.getChildren(name);

            boolean isArray = nodeElements.size() > 1;
            if (isArray) {
                ArrayNode arrayNode = doc.putArray(propertyName);
                for (Element node : nodeElements) {
                    arrayNode.add(node.getTextNormalize());
                }
                continue;
            }
            if (name.equals("geom")) {
                continue;
            }

            if (name.equals("geojson")) {
                doc.put("geom", nodeElements.get(0).getTextNormalize());
                continue;
            }
            if (!name.startsWith("conformTo_")) { // Skip some fields causing errors / TODO
                doc.put(propertyName, nodeElements.get(0).getTextNormalize());
            }
        }
        return doc;
    }
    @Override
    public void forceIndexChanges() throws IOException {
        sendDocumentsToIndex();
    }

    @Override
    public boolean rebuildIndex(ServiceContext context, boolean xlinks,
                                boolean reset, String bucket) throws Exception {
        DataManager dataMan = context.getBean(DataManager.class);
        MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);

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
//                    String id = dataMan.getMetadataId(uuid);
                    Metadata metadata = metadataRepository.findOneByUuid(uuid);
                    if (metadata != null) {
                        listOfIdsToIndex.add(metadata.getId() + "");
                    } else {
                        LOGGER.warn("Selection contains uuid '{}' not found in database", uuid);
                    }
                }
            }
            for(String id : listOfIdsToIndex) {
                dataMan.indexMetadata(id + "", false, this);
            }
        } else {
            final Specifications<Metadata> metadataSpec =
                Specifications.where(MetadataSpecs.isType(MetadataType.METADATA))
                    .or(MetadataSpecs.isType(MetadataType.TEMPLATE));
            final List<Integer> metadataIds = metadataRepository.findAllIdsBy(
                Specifications.where(metadataSpec)
            );
            for(Integer id : metadataIds) {
                dataMan.indexMetadata(id + "", false, this);
            }
        }
        sendDocumentsToIndex();
        return true;
    }

    public void clearIndex() throws Exception {
        client.deleteByQuery(defaultIndex,"*:*");
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
        Search search = new Search.Builder(query).addIndex(defaultIndex).addType(defaultIndex).build();
        // TODO: limit to needed field
//        params.setFields(ID, Geonet.IndexFieldNames.DATABASE_CHANGE_DATE);
        SearchResult searchResult = client.getClient().execute(search);

//        final Map<String, String> result = new HashMap<>();
//        iterateQuery(searchResult.getHits(), doc ->
//            result.put(doc.getFieldValue(ID).toString(),
//                convertDate(doc.getFieldValue(Geonet.IndexFieldNames.DATABASE_CHANGE_DATE))));
        Map<String, String> docs = new HashMap<String, String>();
        return docs;
    }

    @Override
    public ISODate getDocChangeDate(String mdId) throws Exception {
        // TODO: limit to needed field
        Get get = new Get.Builder(defaultIndex, mdId).type(defaultIndex).build();
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
        client.deleteByQuery(defaultIndex, txt);
//        client.commit();
    }

    @Override
    public void delete(List<String> txts) throws Exception {
//        client.deleteById(txts);
//        client.commit();
    }

    @Override
    public long getNumDocs() throws Exception {
         return getNumDocs("");
    }

    public long getNumDocs(String query) throws Exception {
        if (StringUtils.isBlank(query)) {
            query = "*:*";
        }
        String searchQuery = String.format("{" +
            "  \"query\": {" +
            "    \"bool\": {" +
            "      \"must\": {" +
            "        \"match_all\": {}" +
            "      }," +
            "      \"filter\": {" +
            "        \"query_string\":{" +
            "         \"query\": \"%s\"" +
            "        }" +
            "      }" +
            "    }" +
            "  }" +
            "}", query);
        Search search = new Search.Builder(searchQuery).addIndex(defaultIndex).addType(defaultIndex).build();
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

    public List<Element> getDocs(String query, long start, long rows) throws IOException, JDOMException {
        final List<String> result = getDocIds(query, start, rows);
        List<Element> xmlDocs = new ArrayList<>(result.size());
        MetadataRepository metadataRepository = ApplicationContextHolder.get().getBean(MetadataRepository.class);
        for (String id : result) {
            Metadata metadata = metadataRepository.findOne(id);
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

    public void setIndexList(Map<String, String>  indexList) {
        this.indexList = indexList;
    }

    public Map<String, String>  getIndexList() {
        return indexList;
    }
}
