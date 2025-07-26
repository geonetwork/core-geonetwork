//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
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
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Logger;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.SourceType;
import org.fao.geonet.exceptions.*;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.IHarvester;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.harvest.harvester.geonet.v4.client.GeoNetwork4ApiClient;
import org.fao.geonet.kernel.harvest.harvester.geonet.v4.client.SearchResponse;
import org.fao.geonet.kernel.harvest.harvester.geonet.v4.client.SearchResponseHit;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.XmlRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

class Harvester implements IHarvester<HarvestResult> {
    public static final String LOGGER_NAME = "geonetwork.harvester.geonetwork40";

    private final AtomicBoolean cancelMonitor;
    private Logger log;

    private GeonetParams params;
    private ServiceContext context;

    private GeoNetwork4ApiClient geoNetworkApiClient;

    /**
     * Contains a list of accumulated errors during the executing of this harvest.
     */
    private List<HarvestError> errors = new LinkedList<>();

    //---------------------------------------------------------------------------

    public Harvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, GeonetParams params) {
        this.cancelMonitor = cancelMonitor;
        this.log = log;
        this.context = context;
        this.params = params;
    }

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

        List<org.fao.geonet.domain.Group> groupList = geoNetworkApiClient.retrieveGroups(serverUrl, username, password);

        //--- perform all searches

        // Use a TreeSet because in the align phase we need to check if a given UUID is already in the set.
        SortedSet<RecordInfo> records = new TreeSet<>(Comparator.comparing(RecordInfo::getUuid));

        boolean error = false;
        List<Search> searches = Lists.newArrayList(params.getSearches());
        if (params.isSearchEmpty()) {
            searches.add(Search.createEmptySearch(1, 2));
        }

        int pageSize = 30;
        for (Search s : searches) {
            if (cancelMonitor.get()) {
                return new HarvestResult();
            }
            log.info(String.format("Processing search with these parameters %s", s.toString()));
            int from = 0;
            int to = from + (pageSize - 1);
            s.setRange(from, to);

            long resultCount = Integer.MAX_VALUE;
            log.info("Searching on : " + params.getName());

            while (from < resultCount && !error) {
                try {
                    SearchResponse searchResponse = doSearch(s);
                    resultCount = searchResponse.getTotal();

                    records.addAll(processSearchResult(searchResponse.getHits()));
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

                from = from + pageSize;
                to = to + pageSize;
                s.setRange(from, to);

            }
        }

        log.info("Total records processed from this search :" + records.size());

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

        return result;
    }

    private Set<RecordInfo> processSearchResult(Set<SearchResponseHit> searchHits) throws Exception {
        Set<RecordInfo> records = new HashSet<>(searchHits.size()); //(searchHits.length);
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
                HarvestError harvestError = new HarvestError(context, e);
                harvestError.setDescription("Malformed element '"
                    + md.toString() + "'");
                harvestError
                    .setHint("It seems that there was some malformed element. Check with your administrator.");
                this.errors.add(harvestError);
            }

        }
        return records;
    }

    //---------------------------------------------------------------------------

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
            return geoNetworkApiClient.query(getServerUrl(), queryBody, username, password);
        } catch (Exception ex) {
            Log.error(LOGGER_NAME, ex.getMessage(), ex);
            HarvestError harvestError = new HarvestError(context, ex);
            harvestError.setDescription("Error while searching on "
                + params.getName() + ". ");
            harvestError.setHint("Check with your administrator.");
            this.errors.add(harvestError);
            throw new OperationAbortedEx("Raised exception when searching", ex);
        }
    }

    private void updateSources(SortedSet<RecordInfo> records,
                               Map<String, Source> remoteSources) throws MalformedURLException {
        log.info("Aligning source logos from for : " + params.getName());

        //--- collect all different sources that have been harvested

        Set<String> sources = new HashSet<>();

        for (RecordInfo ri : records) {
            sources.add(ri.source);
        }

        //--- update local sources and retrieve logos (if the case)

        String siteId = context.getBean(SettingManager.class).getSiteId();
        final Resources resources = context.getBean(Resources.class);

        for (String sourceUuid : sources) {
            if (!siteId.equals(sourceUuid)) {
                Source source = remoteSources.get(sourceUuid);

                if (source != null) {
                    retrieveLogo(context, resources, params.host, sourceUuid);
                } else {
                    String sourceName = "(unknown)";
                    source = new Source(sourceUuid, sourceName, new HashMap<>(), SourceType.harvester);
                    resources.copyUnknownLogo(context, sourceUuid);
                }

                context.getBean(SourceRepository.class).save(source);
            }
        }
    }

    private void retrieveLogo(ServiceContext context, final Resources resources, String url, String uuid) throws MalformedURLException {
        String logo = uuid + ".gif";
        String baseUrl = url;
        if (!new URL(baseUrl).getPath().endsWith("/")) {
            // Needed to make it work when harvesting from a GN deployed at ROOT ("/")
            baseUrl += "/";
        }
        XmlRequest req = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest(new URL(baseUrl));
        Lib.net.setupProxy(context, req);
        req.setAddress(req.getAddress() + "images/logos/" + logo);

        final Path logoDir = resources.locateLogosDir(context);

        try {
            resources.createImageFromReq(context, logoDir, logo, req);
        } catch (IOException e) {
            context.warning("Cannot retrieve logo file from : " + url);
            context.warning("  (C) Logo  : " + logo);
            context.warning("  (C) Excep : " + e.getMessage());

            resources.copyUnknownLogo(context, uuid);
        }
    }


    public List<HarvestError> getErrors() {
        return errors;
    }

    private String getServerUrl() {
        return params.host + (params.host.endsWith("/") ? "" : "/") + params.getNode();
    }
}
