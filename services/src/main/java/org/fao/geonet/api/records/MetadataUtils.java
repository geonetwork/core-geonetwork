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

package org.fao.geonet.api.records;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.api.es.EsHTTPProxy;
import org.fao.geonet.api.records.model.related.AssociatedRecord;
import org.fao.geonet.api.records.model.related.RelatedItemOrigin;
import org.fao.geonet.api.records.model.related.RelatedItemType;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.Source;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataValidator;
import org.fao.geonet.kernel.schema.AssociatedResource;
import org.fao.geonet.kernel.schema.AssociatedResourcesSchemaPlugin;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmitter;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.specification.MetadataValidationSpecs;
import org.fao.geonet.services.relations.Get;
import org.fao.geonet.utils.Log;
import org.jdom.Content;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.stream.Collectors;

import static org.fao.geonet.kernel.search.EsFilterBuilder.buildPermissionsFilter;
import static org.fao.geonet.kernel.search.EsSearchManager.*;


/**
 *
 */
public class MetadataUtils {
    public static final boolean FOR_EDITING = false;
    public static final boolean WITH_VALIDATION_ERRORS = false;
    public static final boolean KEEP_XLINK_ATTRIBUTES = false;

    private static final Logger LOGGER = LoggerFactory.getLogger(Geonet.SEARCH_ENGINE);

    public static class RelatedTypeDetails {
        private String query;
        private Set<String> expectedRecords = new HashSet<>();
        private Set<String> remoteRecords = new HashSet<>();
        private Map<String, Map<String, String>> recordsProperties = new HashMap<>();

        public RelatedTypeDetails(String query) {
            this.query = query;
        }
        public RelatedTypeDetails(String query, Set<String> expectedRecords) {
            this.query = query;
            this.expectedRecords = expectedRecords;
        }
        public RelatedTypeDetails(String query, Set<String> expectedRecords, Map<String, Map<String, String>> recordsProperties) {
            this.query = query;
            this.expectedRecords = expectedRecords;
            this.recordsProperties = recordsProperties;
        }

        public RelatedTypeDetails(String query, Set<String> expectedRecords, Map<String, Map<String, String>> recordsProperties, Set<String> remoteRecords) {
            this.query = query;
            this.expectedRecords = expectedRecords;
            this.recordsProperties = recordsProperties;
            this.remoteRecords = remoteRecords;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public Set<String> getExpectedRecords() {
            return expectedRecords;
        }

        public void setExpectedRecords(Set<String> expectedRecords) {
            this.expectedRecords = expectedRecords;
        }

        public Map<String, Map<String, String>> getRecordsProperties() {
            return recordsProperties;
        }

        public void setRecordsProperties(Map<String, Map<String, String>> recordsProperties) {
            this.recordsProperties = recordsProperties;
        }

        public Set<String> getRemoteRecords() {
            return remoteRecords;
        }
    }

    public static Map<RelatedItemType, List<AssociatedRecord>> getAssociated(
        ServiceContext context,
        AbstractMetadata md, RelatedItemType[] types, int start, int size)
        throws Exception  {

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);
        SettingManager settingManager = gc.getBean(SettingManager.class);
        EsSearchManager searchMan = gc.getBean(EsSearchManager.class);

        Element xml = dm.getMetadata(context, md.getId() + "",
            FOR_EDITING, WITH_VALIDATION_ERRORS, KEEP_XLINK_ATTRIBUTES);

        SchemaPlugin instance = SchemaManager.getSchemaPlugin(md.getDataInfo().getSchemaId());
        final AssociatedResourcesSchemaPlugin schemaPlugin =
                instance instanceof AssociatedResourcesSchemaPlugin
                ? (AssociatedResourcesSchemaPlugin) instance : null;

        // For each type, store a query and expected list of uuids.
        Map<RelatedItemType, RelatedTypeDetails> queries = new HashMap<>();
        Set<String> allSearchedUuids = new HashSet<>();


        // We have 3 types of links
        // * Those who are in the XML eg.
        // parent (either parent identifier or associated resources),
        // services using operatesOn
        // sources
        // feature catalogues
        //
        // * Those who requires a search to find associated records eg.
        // children
        // brothers&sisters
        //
        // * All of them could be remote records
        Arrays.stream(types).forEach(type -> {
            if (type == RelatedItemType.associated
                || type == RelatedItemType.hasfeaturecats
                || type == RelatedItemType.services
                || type == RelatedItemType.hassources) {
                queries.put(type,
                    new RelatedTypeDetails(
                        String.format("+%s:\"%s\"",
                            RELATED_INDEX_FIELDS.get(type.value()), md.getUuid())
                    ));
            } else if (schemaPlugin != null
                && (type == RelatedItemType.siblings
                || type == RelatedItemType.parent
                || type == RelatedItemType.fcats
                || type == RelatedItemType.datasets
                || type == RelatedItemType.sources)) {
                Set<AssociatedResource> listOfAssociatedResources = new HashSet<>();
                if (type == RelatedItemType.siblings) {
                    listOfAssociatedResources = schemaPlugin.getAssociatedResourcesUUIDs(xml);
                } else if (type == RelatedItemType.sources) {
                    listOfAssociatedResources = schemaPlugin.getAssociatedSources(xml);
                } else if (type == RelatedItemType.datasets) {
                    listOfAssociatedResources = schemaPlugin.getAssociatedDatasets(xml);
                } else if (type == RelatedItemType.parent) {
                    listOfAssociatedResources = schemaPlugin.getAssociatedParents(xml);
                } else if (type == RelatedItemType.fcats) {
                    listOfAssociatedResources = schemaPlugin.getAssociatedFeatureCatalogues(xml);
                }


                Set<String> remoteRecords = new HashSet<>();
                if (type == RelatedItemType.parent || !listOfAssociatedResources.isEmpty()) {
                    Set<String> listOfUUIDs = listOfAssociatedResources.stream()
                        .map(AssociatedResource::getUuid)
                        .collect(Collectors.toSet());
                    Map<String, Map<String, String>> recordsProperties = new HashMap<>();
                    for(AssociatedResource r : listOfAssociatedResources) {
                        Map<String, String> properties = new HashMap<>();
                        properties.put("associationType", r.getAssociationType());
                        properties.put("initiativeType", r.getInitiativeType());
                        properties.put("resourceTitle", r.getTitle());
                        properties.put("url", r.getUrl());
                        recordsProperties.put(r.getUuid(), properties);
                        boolean isRemote = StringUtils.isNotEmpty(r.getUrl())
                            && !r.getUrl().startsWith(settingManager.getBaseURL());
                        if (isRemote) {
                            remoteRecords.add(r.getUuid());
                        }
                    }
                    queries.put(type,
                        new RelatedTypeDetails(
                            String.format("(uuid:(%s)%s) AND (draft:\"n\" OR draft:\"e\")",
                            listOfUUIDs.stream()
                                .collect(Collectors.joining("\" OR \"", "\"", "\"")),
                                type == RelatedItemType.parent
                                    ? " OR childUuid:" + "\"" + md.getUuid() + "\""
                                    : ""),
                            listOfUUIDs,
                            recordsProperties,
                            remoteRecords
                        ));
                    allSearchedUuids.addAll(listOfUUIDs);
                }
            } else if (schemaPlugin != null && type == RelatedItemType.brothersAndSisters) {
                // Get parents
                Set<String> listOfUUIDs = schemaPlugin.getAssociatedParentUUIDs(xml);
                // and search for records associated to them
                queries.put(type,
                    new RelatedTypeDetails(
                        String.format("+%s:(%s) -uuid:\"%s\" AND (draft:\"n\" OR draft:\"e\")",
                        RELATED_INDEX_FIELDS.get(type.value()),
                        listOfUUIDs.stream()
                            .collect(Collectors.joining("\" OR \"", "\"", "\"")),
                        md.getUuid()),
                        listOfUUIDs
                    ));
                allSearchedUuids.addAll(listOfUUIDs);
            } else if (schemaPlugin != null && type == RelatedItemType.children) {
                // Get associated with isComposedOf
                Set<AssociatedResource> listOfAssociated = schemaPlugin.getAssociatedResourcesUUIDs(xml);
                Set<String> isComposedOfList = listOfAssociated.stream()
                    .filter(e -> "isComposedOf".equals(e.getAssociationType()))
                    .map(AssociatedResource::getUuid)
                    .collect(Collectors.toSet());

                // and search for records associated and records having parentUuid equal to current
                queries.put(type,
                    new RelatedTypeDetails(
                        String.format("(%s:\"%s\" OR uuid:(%s)) AND (draft:\"n\" OR draft:\"e\")",
                            RELATED_INDEX_FIELDS.get(type.value()),
                            md.getUuid(),
                            isComposedOfList.stream()
                                .collect(Collectors.joining("\" OR \"", "\"", "\""))
                            ),
                        isComposedOfList
                    ));
                allSearchedUuids.addAll(isComposedOfList);
            }
        });


        Map<RelatedItemType, List<AssociatedRecord>> associated =
            new HashMap<>();
        Set<String> allCatalogueUuids = new HashSet<>();

        String privilegesFilter = buildPermissionsFilter(context);
        ObjectMapper mapper = new ObjectMapper();

        for (Map.Entry<RelatedItemType,RelatedTypeDetails> entry : queries.entrySet()) {
            // TODO: Use msearch ?
            RelatedTypeDetails relatedTypeDetails = entry.getValue();
            final SearchResponse result = searchMan.query(
                relatedTypeDetails.getQuery(),
                privilegesFilter,
                FIELDLIST_RELATED,
                FIELDLIST_RELATED_SCRIPTED,
                start, size);
            Set<String> expectedUuids = relatedTypeDetails.getExpectedRecords();
            Set<String> remoteRecords = relatedTypeDetails.getRemoteRecords();

            List<AssociatedRecord> records = new ArrayList<>();
            if (!result.hits().hits().isEmpty()) {
                for (Hit e : (List<Hit>) result.hits().hits()) {
                    allCatalogueUuids.add(e.id());
                    AssociatedRecord associatedRecord = new AssociatedRecord();
                    associatedRecord.setUuid(e.id());
                    // Set properties eg. remote, associationType, ...
                    associatedRecord.setProperties(relatedTypeDetails.recordsProperties.get(e.id()));

                    // Add scripted field values to the properties of the record
                    if (!e.fields().isEmpty()) {
                        FIELDLIST_RELATED_SCRIPTED.keySet().forEach(f -> {
                            JsonData dc = (JsonData) e.fields().get(f);

                            if (dc != null) {
                                if (associatedRecord.getProperties() == null) {
                                    associatedRecord.setProperties(new HashMap<>());
                                }
                                associatedRecord.getProperties().put(f, dc.toJson().asJsonArray().get(0).toString().replaceAll("^\"|\"$", ""));
                            }
                        });
                    }

                    JsonNode source = mapper.convertValue(e.source(), JsonNode.class);
                    ObjectNode doc = mapper.createObjectNode();
                    doc.set("_source", source);
                    EsHTTPProxy.addUserInfo(doc, context);
                    Iterator<String> fieldNames = doc.fieldNames();
                    while (fieldNames.hasNext()) {
                        String field = fieldNames.next();
                        if (!"_source".equals(field)) {
                            ((ObjectNode) source).set(field, doc.get(field));
                        }
                    }
                    associatedRecord.setRecord(source);
                    associatedRecord.setOrigin(RelatedItemOrigin.catalog.name());
                    records.add(associatedRecord);
                    if (expectedUuids.contains(e.id())) {
                        expectedUuids.remove(e.id());
                    }
                    // Remote records may be found in current catalogue (eg. if harvested)
                    if (remoteRecords.contains(e.id())) {
                        remoteRecords.remove(e.id());
                    }
                }
            }

            buildRemoteRecords(mapper, relatedTypeDetails, records);
            associated.put(entry.getKey(), records);
        }

        assignPortalOrigin(start, size, searchMan, associated, allCatalogueUuids);

        // TODO: Editable relation
        return associated;
    }

    private static void buildRemoteRecords(ObjectMapper mapper,
                                           RelatedTypeDetails relatedTypeDetails,
                                           List<AssociatedRecord> records) throws JsonProcessingException {
        for(String uuid : relatedTypeDetails.getRemoteRecords()) {
            AssociatedRecord associatedRecord = new AssociatedRecord();
            associatedRecord.setUuid(uuid);
            // Set properties eg. remote, url, title, associationType, ...
            associatedRecord.setProperties(relatedTypeDetails.recordsProperties.get(uuid));
            associatedRecord.setRecord(mapper.readTree(buildRemoteRecord(relatedTypeDetails.recordsProperties.get(uuid))));
            associatedRecord.setOrigin(RelatedItemOrigin.remote.name());
            records.add(associatedRecord);
        }
    }

    private static void assignPortalOrigin(int start, int size, EsSearchManager searchMan, Map<RelatedItemType, List<AssociatedRecord>> associated, Set<String> allCatalogueUuids) throws Exception {
        String portalFilter;
        SourceRepository sourceRepository = ApplicationContextHolder.get().getBean(SourceRepository.class);
        NodeInfo node = ApplicationContextHolder.get().getBean(NodeInfo.class);
        if (node != null && !NodeInfo.DEFAULT_NODE.equals(node.getId())) {
            final Optional<Source> portal = sourceRepository.findById(node.getId());
            if (portal.isPresent() && StringUtils.isNotEmpty(portal.get().getFilter())) {
                portalFilter = portal.get().getFilter();

                final SearchResponse recordsInPortal = searchMan.query(
                    String.format("+uuid:(%s)",
                        allCatalogueUuids.stream()
                            .collect(Collectors.joining("\" OR \"", "\"", "\""))),
                    portalFilter,
                    FIELDLIST_UUID,
                    start, size);

                Set<String> allPortalUuids = new HashSet<>();
                if (!recordsInPortal.hits().hits().isEmpty()) {
                    for (Hit e : (List<Hit>) recordsInPortal.hits().hits()) {
                        allPortalUuids.add(e.id());
                    }
                }

                if (!allPortalUuids.isEmpty()) {
                    associated.forEach((t, records) -> records.stream()
                        .filter(r -> allPortalUuids.contains(r.getUuid()))
                        .forEach(r -> r.setOrigin(RelatedItemOrigin.portal.name())));
                }
            }
        }
    }

    private static String buildRemoteRecord(Map<String, String> props) {
        return props == null || props.get("resourceTitle") == null
            ? "{}"
            : String.format("{\"resourceTitleObject\": {\"default\": \"%s\"}}",
                StringEscapeUtils.escapeJson(props.get("resourceTitle")));
    }

    @Deprecated
    public static Element getRelated(ServiceContext context, int iId, String uuid,
                                     RelatedItemType[] type,
                                     int fromRecord, int toRecord)
        throws Exception {
        final String id = String.valueOf(iId);
        final String from = "" + fromRecord;
        final String to = "" + toRecord;
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);
        EsSearchManager searchMan = gc.getBean(EsSearchManager.class);

        Element relatedRecords = new Element("relations");


        String portalFilter = "";

        NodeInfo node = ApplicationContextHolder.get().getBean(NodeInfo.class);
        SourceRepository sourceRepository = ApplicationContextHolder.get().getBean(SourceRepository.class);
        if (node != null && !NodeInfo.DEFAULT_NODE.equals(node.getId())) {
            final Source portal = sourceRepository.findOneByUuid(node.getId());

            if (portal != null) {
                portalFilter = portal.getFilter();
            }
        }


        if(type == null || type.length == 0) {
            type = RelatedItemType.class.getEnumConstants();
        }
        List<RelatedItemType> listOfTypes = new ArrayList<>(Arrays.asList(type));

        Element md = dm.getMetadata(context, id, FOR_EDITING, WITH_VALIDATION_ERRORS, KEEP_XLINK_ATTRIBUTES);
        Map<String, Object> mdIndexFields = searchMan.getDocument(uuid);

        String schemaIdentifier = dm.getMetadataSchema(id);
        SchemaPlugin instance = SchemaManager.getSchemaPlugin(schemaIdentifier);
        AssociatedResourcesSchemaPlugin schemaPlugin = null;
        if (instance instanceof AssociatedResourcesSchemaPlugin) {
            schemaPlugin = (AssociatedResourcesSchemaPlugin) instance;
        }

        // Search for children of this record
        if (listOfTypes.isEmpty() ||
            listOfTypes.contains(RelatedItemType.children)) {
            relatedRecords.addContent(calculateResults("\"" + uuid + "\"", "children", from, to, null, portalFilter));
        }

        // Get parent record from this record
        if (schemaPlugin != null && (listOfTypes.isEmpty() ||
            listOfTypes.contains(RelatedItemType.parent))) {
            Set<String> listOfUUIDs = schemaPlugin.getAssociatedParentUUIDs(md);
            if (!listOfUUIDs.isEmpty()) {
                // Collect local record info (taking into account privileges)
                String joinedUUIDs = "\"" + Joiner.on("\" or \"").join(listOfUUIDs) + "\"";
                relatedRecords.addContent(calculateResults(joinedUUIDs, "parent", from, to, null, portalFilter));
            } else {
                relatedRecords.addContent(new Element("parent"));
            }
            appendRemoteRecord("parent", mdIndexFields, relatedRecords.getChild("parent"));
        }

        // Brothers and sisters are not returned by default
        // It is only on demand and output as siblings.
        if (schemaPlugin != null && listOfTypes.contains(RelatedItemType.brothersAndSisters)) {
            Set<String> listOfUUIDs = schemaPlugin.getAssociatedParentUUIDs(md);
            if (!listOfUUIDs.isEmpty()) {
                String joinedUUIDs = "\"" + Joiner.on("\" or \"").join(listOfUUIDs) + "\"";
                relatedRecords.addContent(calculateResults(joinedUUIDs, RelatedItemType.brothersAndSisters.value(), from, to, uuid, portalFilter));
            }
        }

        // Get aggregates from this record
        if (schemaPlugin != null && (listOfTypes.isEmpty() ||
            listOfTypes.contains(RelatedItemType.siblings))) {
            Element response = new Element("response");

            Set<AssociatedResource> listOfAssociatedResources = schemaPlugin.getAssociatedResourcesUUIDs(md);

            if (listOfAssociatedResources != null) {
                for (AssociatedResource resource : listOfAssociatedResources) {

                    String origin;
                    // Search in the index to use the portal filter and verify the metadata is available for the portal
                    Element searchResult = search("\"" + resource.getUuid() + "\"", RelatedItemType.siblings.value(), from, to, null, false);
                    // If can't be find, skip the result.
                    if (hasResult(searchResult)) {
                        origin = RelatedItemOrigin.portal.name();
                    } else {
                        origin = RelatedItemOrigin.catalog.name();
                    }

                    Element sibContent = getRecord(resource.getUuid(), context, dm);

                    if (sibContent != null) {
                        Element sibling = new Element("sibling");
                        sibling.setAttribute("origin", origin);
                        sibling.setAttribute("initiative", resource.getInitiativeType());
                        sibling.setAttribute("association", resource.getAssociationType());
                        response.addContent(sibling.addContent(sibContent));
                    }
                }
            }
            // May have been added by brothersAndSisters step above.
            Element container = relatedRecords.getChild("siblings");
            if (container == null) {
                container = new Element("siblings");
                relatedRecords.addContent(container);
            }
            container.addContent(response);
            appendRemoteRecord("siblings", mdIndexFields, relatedRecords.getChild("siblings"));
        }

        // Search for records where an aggregate point to this record
        if (listOfTypes.isEmpty() ||
            listOfTypes.contains(RelatedItemType.associated)) {
            relatedRecords.addContent(calculateResults("\"" + uuid + "\"", "associated", from, to, null, portalFilter));
        }

        // Search for services
        if (listOfTypes.isEmpty() ||
            listOfTypes.contains(RelatedItemType.services)) {
            relatedRecords.addContent(calculateResults("\"" + uuid + "\"", "services", from, to, null, portalFilter));
        }

        // Related record from uuiref attributes in metadata record
        if (schemaPlugin != null && (
            listOfTypes.isEmpty() ||
                listOfTypes.contains(RelatedItemType.datasets) ||
                listOfTypes.contains(RelatedItemType.fcats) ||
                listOfTypes.contains(RelatedItemType.sources)
        )) {
            // Get datasets related to service search
            if (listOfTypes.isEmpty() ||
                listOfTypes.contains(RelatedItemType.datasets)) {
                Set<String> listOfUUIDs = schemaPlugin.getAssociatedDatasetUUIDs(md);
                if (listOfUUIDs != null && !listOfUUIDs.isEmpty()) {
                    String joinedUUIDs = "\"" + Joiner.on("\" or \"").join(listOfUUIDs) + "\"";
                    relatedRecords.addContent(calculateResults(joinedUUIDs, "datasets", from, to, null, portalFilter));
                } else {
                    relatedRecords.addContent(new Element("datasets"));
                }
                appendRemoteRecord("datasets", mdIndexFields, relatedRecords.getChild("datasets"));

            }
            // if source, return source datasets defined in the current record
            if (listOfTypes.isEmpty() ||
                listOfTypes.contains(RelatedItemType.sources)) {
                Set<String> listOfUUIDs = schemaPlugin.getAssociatedSourceUUIDs(md);
                if (listOfUUIDs != null && !listOfUUIDs.isEmpty()) {
                    String joinedUUIDs = "\"" + Joiner.on("\" or \"").join(listOfUUIDs) + "\"";
                    relatedRecords.addContent(calculateResults(joinedUUIDs, "sources", from, to, null, portalFilter));
                } else {
                    relatedRecords.addContent(new Element("sources"));
                }
                appendRemoteRecord("sources", mdIndexFields, relatedRecords.getChild("sources"));
            }
            // if fcat
            if (listOfTypes.isEmpty() ||
                listOfTypes.contains(RelatedItemType.fcats)) {
                Set<String> listOfUUIDs = schemaPlugin.getAssociatedFeatureCatalogueUUIDs(md);
                Element fcat = new Element("fcats");

                if (listOfUUIDs != null && !listOfUUIDs.isEmpty()) {
                    for (String fcat_uuid : listOfUUIDs) {
                        String origin;
                        // Search in the index to use the portal filter and verify the metadata is available for the portal
                        Element searchResult = search("\"" + fcat_uuid + "\"", RelatedItemType.fcats.value(), from, to, null, false);
                        // If can't be find, skip the result.
                        if (hasResult(searchResult)) {
                            origin = RelatedItemOrigin.portal.name();
                        } else {
                            origin = RelatedItemOrigin.catalog.name();
                        }

                        Element metadata = new Element("metadata");
                        Element response = new Element("response");
                        Element current = getRecord(fcat_uuid, context, dm);
                        if (current != null) {
                            metadata.setAttribute("origin", origin);
                            metadata.addContent(current);
                        } else {
                            LOGGER.error("Feature catalogue with UUID {} referenced in {} was not found.", fcat_uuid, uuid);
                        }
                        response.addContent(metadata);
                        fcat.addContent(response);
                    }
                }
                relatedRecords.addContent(fcat);
                appendRemoteRecord("fcats", mdIndexFields, fcat);
            }
        }

        //
        if (listOfTypes.isEmpty() ||
            listOfTypes.contains(RelatedItemType.hassources)) {
            // Return records where this record is a source dataset
            relatedRecords.addContent(calculateResults("\"" + uuid + "\"", "hassources", from, to, null, portalFilter));
        }

        // Relation table is preserved for backward compatibility but should not be used anymore.
        if (listOfTypes.isEmpty() ||
            listOfTypes.contains(RelatedItemType.related)) {
            // Related records could be feature catalogue defined in relation table
            relatedRecords.addContent(new Element("related").addContent(Get.getRelation(iId, "full", context)));
            // Or feature catalogue define in feature catalogue citation
            relatedRecords.addContent(calculateResults("\"" + uuid + "\"", "hasfeaturecats", from, to, null, portalFilter));
        }

        // XSL transformation is used on the metadata record to extract
        // distribution information or thumbnails
        if (md != null && (listOfTypes.isEmpty() ||
            listOfTypes.contains(RelatedItemType.onlines) ||
            listOfTypes.contains(RelatedItemType.thumbnails))) {
            relatedRecords.addContent(new Element("metadata").addContent((Content) md.clone()));
        }

        return relatedRecords;
    }

    private static Element search(String uuidQueryValue, String type, String from, String to,
                                  String exclude, boolean ignorePortalFilter) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        EsSearchManager searchMan = applicationContext.getBean(EsSearchManager.class);

        if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "Searching for: " + type);

        int fromValue = Integer.parseInt(from);
        int toValue = Integer.parseInt(to);


        String excludeQuery = "";
        if (exclude != null) {
            excludeQuery = String.format(" -uuid:%s", exclude);
        }

        String portalFilter = null;
        if (!ignorePortalFilter) {
            SourceRepository sourceRepository = ApplicationContextHolder.get().getBean(SourceRepository.class);
            NodeInfo node = ApplicationContextHolder.get().getBean(NodeInfo.class);
            if (node != null && !NodeInfo.DEFAULT_NODE.equals(node.getId())) {
                final Optional<Source> portal = sourceRepository.findById(node.getId());

                if (portal.isPresent() && StringUtils.isNotEmpty(portal.get().getFilter())) {
                    portalFilter = portal.get().getFilter();
                }
            }
        }

        final SearchResponse result = searchMan.query(
            String.format("+%s:(%s)%s", RELATED_INDEX_FIELDS.get(type), uuidQueryValue, excludeQuery),
            ignorePortalFilter ? null : portalFilter,
            FIELDLIST_CORE,
            fromValue, (toValue - fromValue));

        Element typeResponse = new Element(type.equals("brothersAndSisters") ? "siblings" : type);
        if (!result.hits().hits().isEmpty()) {
            // Build the old search service response format
            Element response = new Element("response");

            ObjectMapper objectMapper = new ObjectMapper();

            result.hits().hits().forEach(e1 -> {
                Hit e = (Hit) e1;

                Element recordMetadata = new Element("metadata");
                final Map<String, Object> source = objectMapper.convertValue(e.source(), Map.class);
                recordMetadata.addContent(new Element("id").setText((String) source.get(Geonet.IndexFieldNames.ID)));
                recordMetadata.addContent(new Element("uuid").setText((String) source.get(Geonet.IndexFieldNames.UUID)));
                if (type.equals("brothersAndSisters")) {
                    recordMetadata.setAttribute("association", "brothersAndSisters");
                }

                setFieldFromIndexDocument(recordMetadata, source, Geonet.IndexFieldNames.RESOURCETITLE, "title");
                setFieldFromIndexDocument(recordMetadata, source, Geonet.IndexFieldNames.RESOURCEABSTRACT, "abstract");
                setFieldFromIndexDocument(recordMetadata, source, "operatesOn", "operatesOn");
                response.addContent(recordMetadata);
            });
            typeResponse.addContent(response);
        }
        return typeResponse;
    }

    private static void setFieldFromIndexDocument(Element recordMetadata, Map<String, Object> source, String fieldName, String elementName) {
        // TODOES : multilingual records
        Object fields = source.get(fieldName + "Object");
        if (fields == null) {
            fields = source.get(fieldName);
        }
        if (fields instanceof ArrayList) {
            ((ArrayList) fields).forEach(field -> recordMetadata.addContent(new Element(elementName).setText((String) field)));
        } else if (fields instanceof Map) {
            recordMetadata.addContent(new Element(elementName).setText((String) ((Map) fields).get("default")));
        } else if (fields instanceof String) {
            recordMetadata.addContent(new Element(elementName).setText((String) fields));
        }
    }

    /**
     * Run a Lucene query expression and return a list of UUIDs.
     *
     * @param query
     * @return List of UUIDs to export
     */
    public static Set<String> getUuidsToExport(String query) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        EsSearchManager searchMan = applicationContext.getBean(EsSearchManager.class);

        Set<String> uuids = new HashSet<>();
        Set<String> field = new HashSet<>(1);
        field.add(Geonet.IndexFieldNames.UUID);

        int from = 0;
        SettingInfo si = applicationContext.getBean(SettingInfo.class);
        int size = Integer.parseInt(si.getSelectionMaxRecords());

        final SearchResponse result = searchMan.query(query, null, from, size);
        if (!result.hits().hits().isEmpty()) {
            final List<Hit> elements = result.hits().hits();
            ObjectMapper objectMapper = new ObjectMapper();
            elements.forEach(e -> uuids.add((String) objectMapper.convertValue(e.source(), Map.class).get(Geonet.IndexFieldNames.UUID)));
        }
        Log.info(Geonet.MEF, "  Found " + uuids.size() + " record(s).");
        return uuids;
    }

    /**
     * TODO-API : replace by ApiUtils.
     */
    private static Element getRecord(String uuid, ServiceContext context, DataManager dm) {
        Element content = null;
        try {
            String id = dm.getMetadataId(uuid);
            Lib.resource.checkPrivilege(context, id, ReservedOperation.view);
            content = dm.getMetadata(context, id, FOR_EDITING, WITH_VALIDATION_ERRORS, KEEP_XLINK_ATTRIBUTES);
        } catch (Exception e) {
            if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
                Log.debug(Geonet.SEARCH_ENGINE, "Metadata " + uuid + " record is not visible for user.");
        }
        return content;
    }

    /**
     * Returns the metadata validation status from the database, calculating/storing the validation if not stored.
     *
     * @param metadata
     * @param context
     * @return
     */
    public static boolean retrieveMetadataValidationStatus(AbstractMetadata metadata, ServiceContext context) throws Exception {
        MetadataValidationRepository metadataValidationRepository = context.getBean(MetadataValidationRepository.class);
        IMetadataValidator validator = context.getBean(IMetadataValidator.class);
        DataManager dataManager = context.getBean(DataManager.class);

        boolean hasValidation =
            (metadataValidationRepository.count(MetadataValidationSpecs.hasMetadataId(metadata.getId())) > 0);

        if (!hasValidation) {
            validator.doValidate(metadata, context.getLanguage());
            dataManager.indexMetadata(metadata.getId() + "", DirectIndexSubmitter.INSTANCE);
        }

        boolean isInvalid =
            (metadataValidationRepository.count(MetadataValidationSpecs.isInvalidAndRequiredForMetadata(metadata.getId())) > 0);

        return isInvalid;
    }


    /**
     * Checks if a result for a search query has results.
     * <p>
     * Response examples:
     *
     * <siblings>
     * <response from="1" to="0" />
     * </siblings>
     *
     *
     * <siblings>
     * <response from="1" to="1">
     * <metadata>...</metadata>
     * </response>
     * </siblings>
     *
     * @param searchResponse
     * @return True it the response has results, False in other cases.
     */
    private static boolean hasResult(Element searchResponse) {

        if (!searchResponse.getChildren().isEmpty()) {
            Element containerResults = (Element) searchResponse.getChildren().get(0);
            return !containerResults.getChildren().isEmpty();
        }

        return false;
    }


    /**
     * Process search results to add the origin of the metadata:
     *
     *  - portal: the metadata is available in the current portal.
     *  - catalog: the metadata is not available in the current portal, but is available in the local catalog.
     *
     * @param uuidQueryValue
     * @param type
     * @param from
     * @param to
     * @param exclude
     * @param portalFilter
     * @return
     * @throws Exception
     */
    private static Element calculateResults(String uuidQueryValue, String type, String from, String to,
                                            String exclude,
                                            String portalFilter) throws Exception {

        // Search related resources ignoring portal filter
        Element results = search(uuidQueryValue, type, from, to, exclude, true);

        // Check if the portal has a filter
        if (StringUtils.isNotEmpty(portalFilter)) {
            // Search related resources with the portal filter
            Element resultsForPortal = search(uuidQueryValue, type, from, to, exclude, false);

            // Build the set of uuids from portal results
            HashSet<String> portalResultsUuids = new HashSet<>();

            if (resultsForPortal.getChild("response") != null) {
                for (Element r : (List<Element>) resultsForPortal.getChild("response").getChildren()) {
                    String uuidValue = r.getChildText("uuid");
                    portalResultsUuids.add(uuidValue);
                }
            }
            // Process the full results to add the origin depending if are available in the portal or not
            if (results.getChild("response") != null) {
                for (Element r : (List<Element>) results.getChild("response").getChildren()) {
                    String origin = RelatedItemOrigin.catalog.name();

                    String uuidValue = r.getChildText("uuid");

                    // Is the result available in the portal?
                    if (portalResultsUuids.contains(uuidValue)) {
                        origin = RelatedItemOrigin.portal.name();
                    }

                    r.setAttribute("origin", origin);
                }
            }
        } else {
            // No portal filter: set origin to portal
            if (results.getChild("response") != null) {
                for (Element r : (List<Element>) results.getChild("response").getChildren()) {
                    r.setAttribute("origin", RelatedItemOrigin.portal.name());
                }
            }
        }

        return results;
    }


    private static void appendRemoteRecord(String type,
                                           Map<String, Object> mdIndexFields,
                                           Element typeRoot) {
        Object values = mdIndexFields.get(Geonet.IndexFieldNames.RECORDLINK);

        if (values instanceof ArrayList) {
            Element responseRoot = null;
            if (typeRoot.getChild("response") != null) {
                responseRoot = typeRoot.getChild("response");
            } else {
                responseRoot = new Element("response");
                typeRoot.addContent(responseRoot);
            }

            Element finalResponseRoot = responseRoot;
            ((ArrayList) values).forEach(recordLink -> {
                if (recordLink instanceof Map) {
                    Map<String, String> linkProperties = (Map) recordLink;
                    if (type.equals(linkProperties.get(Geonet.IndexFieldNames.RecordLink.TYPE))
                        && "remote".equals(linkProperties.get("origin"))) {
                        Element recordMetadata = new Element("metadata");
                        recordMetadata.setAttribute("origin", RelatedItemOrigin.remote.name());

                        if (type.equals(RelatedItemType.siblings.value())) {
                            if (linkProperties.get("associationType") != null) {
                                recordMetadata.setAttribute("association", linkProperties.get("associationType"));
                            }
                            if (linkProperties.get("initiativeType") != null) {
                                recordMetadata.setAttribute("initiative", linkProperties.get("initiativeType"));
                            }
                        }

                        recordMetadata.addContent(new Element("id")
                            .setText(linkProperties.get(Geonet.IndexFieldNames.RecordLink.TO)));
                        recordMetadata.addContent(new Element("uuid")
                            .setText(linkProperties.get(Geonet.IndexFieldNames.RecordLink.TO)));
                        recordMetadata.addContent(new Element("title")
                            .setText(linkProperties.get(Geonet.IndexFieldNames.RecordLink.TITLE)));
                        recordMetadata.addContent(new Element("url")
                            .setText(linkProperties.get(Geonet.IndexFieldNames.RecordLink.URL)));


                        finalResponseRoot.addContent(recordMetadata);
                    }
                }
            });
        }
    }

}
