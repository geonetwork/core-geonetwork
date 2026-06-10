//=============================================================================
//===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.geonet.v4;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Logger;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Source;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.IHarvester;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.harvest.harvester.geonet.BaseGeoNetworkHarvester;
import org.fao.geonet.kernel.harvest.harvester.geonet.v4.client.GeoNetwork4ApiClient;
import org.fao.geonet.kernel.harvest.harvester.geonet.v4.client.SearchResponse;
import org.fao.geonet.kernel.harvest.harvester.geonet.v4.client.SearchResponseDeserializer;
import org.fao.geonet.kernel.harvest.harvester.geonet.v4.client.SearchResponseHit;

/**
 * The Harvester class is responsible for managing and executing the harvest
 * process, including retrieving information about sources, groups, and records,
 * handling search requests, processing the search results, and aligning the
 * harvested records with the local node.
 * <p>
 * This class extends {@link BaseGeoNetworkHarvester} using {@link GeonetParams}
 * as the parameter type, and implements the {@link IHarvester} interface with
 * {@link HarvestResult} as the return type.
 * <p>
 * The Harvester interacts with the GeoNetwork API client and supports advanced
 * operations like search execution, processing of large data sets, and managing
 * errors during the harvest. It also supports cancellation through a monitor.
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Performing searches based on parameters.</li>
 * <li>Processing search results to extract record information.</li>
 * <li>Handling errors during various phases of the harvest process.</li>
 * <li>Aligning harvested data with the local database and updating sources.</li>
 * </ul>
 */
class Harvester extends BaseGeoNetworkHarvester<GeonetParams> implements IHarvester<HarvestResult> {
    private GeoNetwork4ApiClient geoNetworkApiClient;


    public Harvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, GeonetParams params, List<HarvestError> errors) {
        super(cancelMonitor, log, context, params, errors);
    }

    /**
     * Orchestrates harvest: searches, aligns, and updates sources
     */
    public HarvestResult harvest(Logger log) throws Exception {
        this.log = log;

        geoNetworkApiClient = context.getBean(GeoNetwork4ApiClient.class);

        //--- login
        String username;
        String password;

        if (params.isUseAccount()) {
            username = params.getUsername();
            password = params.getPassword();
        } else {
            username = "";
            password = "";
        }

        //--- retrieve info on categories and groups

        log.info("Retrieving information from : " + params.host);

        String serverUrl = getServerUrl();
        Map<String, Source> sources = geoNetworkApiClient.retrieveSources(serverUrl, username, password);

        List<Group> groupList = geoNetworkApiClient.retrieveGroups(serverUrl, username, password);

        //--- perform all searches

        // Use a TreeSet because in the align phase we need to check if a given UUID is already in the set.
        SortedSet<RecordInfo> records = new TreeSet<>(Comparator.comparing(RecordInfo::getUuid));

        boolean error = false;
        List<Search> searches = Lists.newArrayList(params.getSearches());
        if (params.isSearchEmpty()) {
            searches.add(Search.createEmptySearch(1, 2));
        }

        // Records that could not be turned into something harvestable. They are skipped so a single
        // bad record never aborts the whole harvest, and reported afterwards so they are not lost.
        // unparseableRecordIds: failed while parsing the search response JSON (stage 1).
        // malformedRecordIds: parsed but failed while building the record info (stage 2).
        List<String> unparseableRecordIds = new ArrayList<>();
        List<String> malformedRecordIds = new ArrayList<>();

        int pageSize = 30;
        for (Search s : searches) {
            if (cancelMonitor.get()) {
                return new HarvestResult();
            }
            log.info(String.format("Processing search with these parameters %s", s.toString()));
            int from = 0;
            s.setRange(from, pageSize);

            long resultCount = Integer.MAX_VALUE;
            log.info("Searching on : " + params.getName());

            while (from < resultCount && !error) {
                try {
                    SearchResponse searchResponse = doSearch(s);
                    resultCount = searchResponse.getTotal();

                    records.addAll(processSearchResult(searchResponse.getHits(), malformedRecordIds));
                    unparseableRecordIds.addAll(searchResponse.getFailedHits());
                } catch (Exception t) {
                    error = true;
                    log.error("Unknown error trying to harvest");
                    log.error(t.getMessage());
                    log.error(t);
                    errors.add(new HarvestError(context, t));
                } catch (Throwable t) {
                    error = true;
                    log.fatal("Something unknown and terrible happened while harvesting");
                    log.fatal(t.getMessage());
                    log.error(t);
                    errors.add(new HarvestError(context, t));
                }

                // Always advance the page cursor, even when some hits were skipped, so that the loop
                // terminates once the remote total (resultCount) has been paged through.
                from = from + pageSize;
                s.setRange(from, pageSize);

            }
        }

        // Report skipped records (logged in the harvester log and added to the harvester history).
        reportSkippedRecords(unparseableRecordIds);
        int badRecords = unparseableRecordIds.size() + malformedRecordIds.size();

        log.info("Total records processed from this search :" + records.size());
        if (badRecords > 0) {
            log.warning("Skipped " + badRecords
                + " record(s) that could not be harvested from " + params.getName() + ".");
        }

        //--- align local node
        HarvestResult result = new HarvestResult();
        if (!error) {
            try {
                Aligner aligner = new Aligner(cancelMonitor, log, context, params, groupList);
                result = aligner.align(records, errors);

                updateSources(records, sources);
            } catch (Exception t) {
                log.error("Unknown error trying to harvest");
                log.error(t.getMessage());
                errors.add(new HarvestError(this.context, t));
            } catch (Throwable t) {
                log.fatal("Something unknown and terrible happened while harvesting");
                log.fatal(t.getMessage());
                errors.add(new HarvestError(this.context, t));
            }
        } else {
            log.warning("Due to previous errors the align process has not been called");
        }

        // Account for the records skipped while parsing the search response or building the record
        // info so the harvest report totals reconcile (they are surfaced under badFormat).
        result.badFormat += badRecords;

        return result;
    }

    private Set<RecordInfo> processSearchResult(Set<SearchResponseHit> searchHits, List<String> malformedRecordIds) {
        Set<RecordInfo> records = new HashSet<>(searchHits.size());

        for (SearchResponseHit md : searchHits) {
            if (cancelMonitor.get()) {
                return Collections.emptySet();
            }

            try {
                String uuid = md.getUuid();
                String schema = md.getSchema();
                String changeDate = md.getChangeDate();
                String source = md.getSource();
                records.add(new RecordInfo(uuid, changeDate, schema, source));

            } catch (Exception e) {
                malformedRecordIds.add(md.getUuid() != null ? md.getUuid() : SearchResponseDeserializer.UNKNOWN_ID);
                HarvestError harvestError = new HarvestError(context, e);
                harvestError.setDescription("Malformed element '"
                    + md + "'");
                harvestError
                    .setHint("It seems that there was some malformed element. Check with your administrator.");
                this.errors.add(harvestError);
            }

        }
        return records;
    }

    /**
     * Reports the records that could not be parsed from the search response. A summary is written to
     * the harvester log and a {@link HarvestError} is added to the harvester history so the skipped
     * documents are visible to the administrator instead of being silently dropped.
     *
     * @param unparseableRecordIds the identifiers (or {@code "unknown"} when not extractable) of the
     *                             records skipped while parsing the search response
     */
    private void reportSkippedRecords(List<String> unparseableRecordIds) {
        if (unparseableRecordIds.isEmpty()) {
            return;
        }

        String ids = String.join(", ", unparseableRecordIds);
        String message = String.format("Skipped %d record(s) that could not be parsed from the search response of %s. Document id(s): %s",
            unparseableRecordIds.size(), params.getName(), ids);
        log.warning(message);

        HarvestError harvestError = new HarvestError(context, new Exception(message));
        harvestError.setDescription(message);
        harvestError.setHint("These records were skipped so the harvest could continue. "
            + "Check the identified records on the remote catalog.");
        this.errors.add(harvestError);
    }


    private SearchResponse doSearch(Search s) throws OperationAbortedEx {
        try {
            String username;
            String password;

            if (params.isUseAccount()) {
                username = params.getUsername();
                password = params.getPassword();
            } else {
                username = "";
                password = "";
            }

            String queryBody = s.createElasticsearchQuery();
            if (log.isDebugEnabled()) {
                log.debug(String.format("GeoNetwork 4 harvester sending search request to %s/api/search/records/_search%nRequest body:%n %s",
                    getServerUrl(), queryBody));
            }
            return geoNetworkApiClient.query(getServerUrl(), queryBody, username, password);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            HarvestError harvestError = new HarvestError(context, ex);
            harvestError.setDescription("Error while searching on "
                + params.getName() + ". ");
            harvestError.setHint("Check with your administrator.");
            this.errors.add(harvestError);
            throw new OperationAbortedEx("Raised exception when searching", ex);
        }
    }

    private String getServerUrl() {
        return params.host + (params.host.endsWith("/") ? "" : "/") + params.getNode();
    }
}
