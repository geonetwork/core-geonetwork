/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
import org.fao.geonet.api.API;
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
import org.fao.geonet.kernel.datamanager.base.BaseMetadataUtils;
import org.fao.geonet.kernel.schema.AssociatedResource;
import org.fao.geonet.kernel.schema.AssociatedResourcesSchemaPlugin;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.specification.MetadataValidationSpecs;
import org.fao.geonet.services.relations.Get;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.DOMOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Node;

import java.util.*;
import java.util.stream.Collectors;

import static org.fao.geonet.constants.Geonet.IndexFieldNames.AGG_ASSOCIATED_REVISION_OF;
import static org.fao.geonet.kernel.search.EsFilterBuilder.buildPermissionsFilter;
import static org.fao.geonet.kernel.search.EsSearchManager.*;


public class MetadataVersionsUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Geonet.SEARCH_ENGINE);


    /**
     * For versions, order is based on the version graph.
     */
    protected static List<AssociatedRecord> reorderIndexDocBasedOnOrderedRecords(List<AssociatedRecord> records, List<String> orderedRecords) {
        if (orderedRecords == null || orderedRecords.isEmpty()) {
            return records;
        }

        Map<String, AssociatedRecord> recordsByUuid = new LinkedHashMap<>();
        for (AssociatedRecord associatedRecord : records) {
            recordsByUuid.put(associatedRecord.getUuid(), associatedRecord);
        }

        List<AssociatedRecord> reordered = new ArrayList<>();
        for (String uuid : orderedRecords) {
            AssociatedRecord associatedRecord = recordsByUuid.remove(uuid);
            if (associatedRecord != null) {
                reordered.add(associatedRecord);
            }
            // else, remote records are not supported in version list.
        }
        reordered.addAll(recordsByUuid.values());
        return reordered;
    }

    /**
     * Retrieve all versions of a record.
     * Remote records are not supported because they may not be XML document
     * so we can't assume that we will be able to extract revisionOf records.
     */
    protected static MetadataUtils.RelatedTypeDetails getAllVersions(EsSearchManager searchMan, String metadataUuid) throws Exception {
        Set<String> allVersionUuids = new LinkedHashSet<>();
        Map<String, Set<String>> previousVersionsByUuid = new LinkedHashMap<>();
        Deque<String> uuidsToProcess = new ArrayDeque<>();

        allVersionUuids.add(metadataUuid);
        uuidsToProcess.add(metadataUuid);

        while (!uuidsToProcess.isEmpty()) {
            String currentUuid = uuidsToProcess.removeFirst();

            Map<String, Object> document;
            try {
                document = searchMan.getDocument(currentUuid);
            } catch (Exception e) {
                // Referenced versions may point to records not indexed in this catalogue.
                // And we need the document to extract relation to next item.
                // This can happen on remote record link. This is not supported for now.
                // previousVersionsByUuid.putIfAbsent(currentUuid, new LinkedHashSet<>());
                allVersionUuids.remove(currentUuid);
                continue;
            }

            Set<String> previousVersions = EsSearchManager.extractFieldValues(document.get(AGG_ASSOCIATED_REVISION_OF));
            previousVersionsByUuid.computeIfAbsent(currentUuid, key -> new LinkedHashSet<>()).addAll(previousVersions);
            for (String previousUuid : previousVersions) {
                if (allVersionUuids.add(previousUuid)) {
                    uuidsToProcess.add(previousUuid);
                }
            }

            int from = 0;
            final int pageSize = 100;
            while (true) {
                SearchResponse searchResponse = searchMan.query(
                    String.format("+agg_associated_revisionOf:\"%s\" AND (draft:\"n\" OR draft:\"e\")", currentUuid),
                    null,
                    FIELDLIST_UUID,
                    from,
                    pageSize);
                List<Hit> hits = searchResponse.hits().hits();
                if (hits.isEmpty()) {
                    break;
                }

                for (Hit hit : hits) {
                    previousVersionsByUuid.computeIfAbsent(hit.id(), key -> new LinkedHashSet<>()).add(currentUuid);
                    if (allVersionUuids.add(hit.id())) {
                        uuidsToProcess.add(hit.id());
                    }
                }

                if (hits.size() < pageSize) {
                    break;
                }
                from += pageSize;
            }
        }

        List<String> orderedRecords = orderVersionsNewestToOldest(allVersionUuids, previousVersionsByUuid);
        return new MetadataUtils.RelatedTypeDetails(
            String.format("(uuid:(%s)) AND (draft:\"n\" OR draft:\"e\")",
                orderedRecords.stream().collect(Collectors.joining("\" OR \"", "\"", "\""))),
            new LinkedHashSet<>(orderedRecords),
            new HashMap<>(),
            new HashSet<>(),
            orderedRecords);
    }

    protected static MetadataUtils.RelatedTypeDetails getNextOrPrevious(
        MetadataUtils.RelatedTypeDetails versionDetails, String currentUuid, boolean isNext) {
        List<String> orderedRecords = versionDetails.getOrderedRecords();

        if (orderedRecords == null || orderedRecords.isEmpty()) {
            return new MetadataUtils.RelatedTypeDetails("(uuid:(\"\")) AND (draft:\"n\" OR draft:\"e\")",
                new HashSet<>(), new HashMap<>(), new HashSet<>(), new ArrayList<>());
        }

        // Find current record in the ordered list (newest to oldest)
        int currentIndex = orderedRecords.indexOf(currentUuid);

        // For next: we need the previous in the list (towards newer versions)
        // For previous: we need the next in the list (towards older versions)
        int targetIndex = isNext ? currentIndex - 1 : currentIndex + 1;

        if (targetIndex < 0 || targetIndex >= orderedRecords.size()) {
            // No next or previous version exists
            return new MetadataUtils.RelatedTypeDetails("(uuid:(\"\")) AND (draft:\"n\" OR draft:\"e\")",
                new HashSet<>(), new HashMap<>(), new HashSet<>(), new ArrayList<>());
        }

        String targetUuid = orderedRecords.get(targetIndex);
        List<String> result = new ArrayList<>();
        result.add(targetUuid);

        return new MetadataUtils.RelatedTypeDetails(
            String.format("(uuid:\"%s\") AND (draft:\"n\" OR draft:\"e\")", targetUuid),
            new LinkedHashSet<>(result),
            new HashMap<>(),
            new HashSet<>(),
            result);
    }

    private static List<String> orderVersionsNewestToOldest(Set<String> versionUuids,
                                                             Map<String, Set<String>> previousVersionsByUuid) {
        Map<String, Integer> incomingEdges = new LinkedHashMap<>();
        Map<String, Set<String>> edges = new LinkedHashMap<>();

        for (String uuid : versionUuids) {
            incomingEdges.put(uuid, 0);
            edges.put(uuid, new LinkedHashSet<>());
        }

        for (Map.Entry<String, Set<String>> entry : previousVersionsByUuid.entrySet()) {
            if (!versionUuids.contains(entry.getKey())) {
                continue;
            }

            for (String previousUuid : entry.getValue()) {
                if (!versionUuids.contains(previousUuid)) {
                    continue;
                }

                if (edges.get(entry.getKey()).add(previousUuid)) {
                    incomingEdges.put(previousUuid, incomingEdges.get(previousUuid) + 1);
                }
            }
        }

        Deque<String> queue = new ArrayDeque<>();
        incomingEdges.entrySet().stream()
            .filter(entry -> entry.getValue() == 0) // ie. no newer version
            .map(Map.Entry::getKey)
            .sorted()
            .forEach(queue::addLast);

        List<String> ordered = new ArrayList<>();
        while (!queue.isEmpty()) {
            String uuid = queue.removeFirst();
            ordered.add(uuid);

            for (String previousUuid : edges.getOrDefault(uuid, Collections.emptySet())) {
                int newIndegree = incomingEdges.get(previousUuid) - 1;
                incomingEdges.put(previousUuid, newIndegree);
                if (newIndegree == 0) {
                    queue.addLast(previousUuid);
                }
            }
        }

        if (ordered.size() != versionUuids.size()) {
            versionUuids.stream()
                .filter(uuid -> !ordered.contains(uuid))
                .sorted()
                .forEach(ordered::add);
        }

        return ordered;
    }
}
