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

package org.fao.geonet.kernel.harvest.harvester.geonet.v21_3;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.SourceType;
import org.fao.geonet.exceptions.BadServerResponseEx;
import org.fao.geonet.exceptions.BadSoapResponseEx;
import org.fao.geonet.exceptions.BadXmlResponseEx;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.exceptions.UserNotFoundEx;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.IHarvester;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.harvest.harvester.geonet.BaseGeoNetworkHarvester;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;

/**
 * Class representing a Harvester, which operates as an extension of the {@link BaseGeoNetworkHarvester}
 * and implements the {@link IHarvester} interface. The Harvester class is responsible for performing harvesting
 * operations to fetch metadata records, process them, and align them within the current system.
 * <p>
 * The Harvester communicates with remote GeoNetwork servers and uses the provided configuration parameters
 * to perform search operations, retrieve metadata records, and process the data accordingly.
 * This process involves handling remote requests, logging in with appropriate credentials,
 * retrieving information such as categories and groups, performing metadata searches,
 * and aligning the retrieved records into the local environment.
 */
class Harvester extends BaseGeoNetworkHarvester<GeonetParams> implements IHarvester<HarvestResult> {
    public Harvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, GeonetParams params, List<HarvestError> errors) {
        super(cancelMonitor, log, context, params, errors);
    }

    public HarvestResult harvest(Logger log) throws Exception {
        this.log = log;
        String host = params.host;
        if (new URL(host).getPath().isEmpty()) {
            // Needed to make it work when harvesting from a GN deployed at ROOT ("/")
            host += "/";
        }
        XmlRequest req = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest(new URL(host));

        Lib.net.setupProxy(context, req);

        //--- login

        if (params.isUseAccount()) {

            try {
                log.info("Login into : " + params.getName());
                req.setCredentials(params.getUsername(), params.getPassword());
                req.setPreemptiveBasicAuth(true);
                req.setAddress(params.getServletPath() + "/" + params.getNode()
                    + "/eng/xml.info");
                req.addParam("type", "me");

                Element response = req.execute();
                if (!response.getName().equals("info") || response.getChild("me") == null) {
                    pre29Login(req);
                } else if (!"true".equals(response.getChild("me").getAttributeValue("authenticated"))) {
                    log.warning("Authentication failed for user: " + params.getUsername());
                    throw new UserNotFoundEx(params.getUsername());
                }
            } catch (Exception e) {
                pre29Login(req);
            }
        }

        //--- retrieve info on categories and groups

        log.info("Retrieving information from : " + host);

        req.setAddress(params.getServletPath() + "/" + params.getNode()
            + "/eng/" + Geonet.Service.XML_INFO);
        req.clearParams();
        req.addParam("type", "sources");
        req.addParam("type", "groups");

        Element remoteInfo = req.execute();

        if (!remoteInfo.getName().equals("info"))
            throw new BadServerResponseEx(remoteInfo);

        //--- perform all searches

        // Use a TreeSet because in the align phase we need to check if a given UUID is already in the set.
        SortedSet<RecordInfo> records = new TreeSet<>(Comparator.comparing(RecordInfo::getUuid));

        // Do a search and set from=1 and to=2, try to find out maxPageSize
        // xml.search service returns maxPageSize in 3.8.x onwards. If not set, then
        // use 100. If set but is greater than the one defined by client, then use the client one.
        int pageSize = 100;

        try {
            Element getPageSizeSearch = doSearch(req, Search.createEmptySearch(1, 2));
            String sPageSize = getPageSizeSearch.getAttributeValue("maxPageSize");
            if (!StringUtils.isBlank(sPageSize)) {
                int clientPageSize = Integer.parseInt(sPageSize);
                log.info("Client said maximum page size is " + clientPageSize);
                if (clientPageSize < pageSize) {
                    pageSize = clientPageSize;
                    log.info("Page size returned by the server is smaller than the default one for the harvester. Using the remote server one.");
                } else {
                    log.info("Page size returned by the server is greater than the default one for the harvester. " +
                        "Using the client harvester default page size (" + pageSize + ")");
                }
            } else {
                log.info("Client didn't respond with page size so using page size of " + pageSize);
            }
        } catch (NumberFormatException nfe) {
            log.error("Invalid maxPageSize attribute value, using " + pageSize);
        } catch (Exception e) {
            log.error("Unable to determine pagesize, this could be fatal");
        }

        boolean error = false;
        List<Search> searches = Lists.newArrayList(params.getSearches());
        if (params.isSearchEmpty()) {
            searches.add(Search.createEmptySearch(1, 2));
        }

        for (Search s : searches) {
            if (cancelMonitor.get()) {
                return new HarvestResult();
            }
            log.info(String.format("Processing search with these parameters %s", s.toString()));
            int from = 1;
            int to = from + (pageSize - 1);
            s.setRange(from, to);

            int resultCount = Integer.MAX_VALUE;
            log.info("Searching on : " + params.getName());

            while (from < resultCount && !error) {
                try {
                    Element searchResult = doSearch(req, s);
                    Element summary = searchResult.getChild(Geonet.Elem.SUMMARY);
                    resultCount = Integer.parseInt(summary.getAttributeValue("count"));

                    @SuppressWarnings("unchecked")
                    List<Element> metadataResultList = searchResult.getChildren("metadata");
                    records.addAll(processSearchResult(metadataResultList));
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
                Aligner aligner = new Aligner(cancelMonitor, log, context, req, params, remoteInfo);
                result = aligner.align(records, errors);

                Map<String, Source> sources = buildSources(remoteInfo);
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

    private Set<RecordInfo> processSearchResult(List<Element> metadataResultList) {
        Set<RecordInfo> records = new HashSet<>(metadataResultList.size());
        for (Element md : metadataResultList) {
            if (cancelMonitor.get()) {
                return Collections.emptySet();
            }

            try {
                Element info = md.getChild("info", Edit.NAMESPACE);

                if (info == null)
                    log.warning("Missing 'geonet:info' element in 'metadata' element");
                else {
                    String uuid = info.getChildText("uuid");
                    String schema = info.getChildText("schema");
                    String changeDate = info.getChildText("changeDate");
                    String source = info.getChildText("source");

                    records.add(new RecordInfo(uuid, changeDate, schema, source));
                }
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

    private void pre29Login(XmlRequest req) throws IOException, BadXmlResponseEx, BadSoapResponseEx, UserNotFoundEx {
        log.info("Failed to login using basic auth (geonetwork 2.9+) trying pre-geonetwork 2.9 login: " + params.getName());
        // try old authentication
        req.setAddress(params.getServletPath() + "/" + params.getNode() + "/en/" + Geonet.Service.XML_LOGIN);
        req.addParam("username", params.getUsername());
        req.addParam("password", params.getPassword());

        Element response = req.execute();

        if (!response.getName().equals("ok")) {
            throw new UserNotFoundEx(params.getUsername());
        }
    }


    private Element doSearch(XmlRequest request, Search s) throws OperationAbortedEx {
        request.setAddress(params.getServletPath() + "/" + params.getNode()
            + "/eng/" + Geonet.Service.XML_SEARCH);
        request.clearParams();
        try {
            log.info(String.format("Searching on %s. From %d to %d.", params.getName(), s.from, s.to));

            Element response = request.execute(s.createRequest());

            if (log.isDebugEnabled()) {
                log.debug("Search results:\n" + Xml.getString(response));
            }

            return response;

        } catch (BadSoapResponseEx e) {
            log.warning("Raised exception when searching : " + e.getMessage());
            this.errors.add(new HarvestError(context, e));
            throw new OperationAbortedEx("Raised exception when searching", e);
        } catch (BadXmlResponseEx e) {
            HarvestError harvestError = new HarvestError(context, e);
            harvestError.setDescription("Error while searching on "
                + params.getName() + ". Excepted XML, returned: "
                + e.getMessage());
            harvestError.setHint("Check with your administrator.");
            this.errors.add(harvestError);
            throw new OperationAbortedEx("Raised exception when searching", e);
        } catch (IOException e) {
            HarvestError harvestError = new HarvestError(context, e);
            harvestError.setDescription("Error while searching on "
                + params.getName() + ". ");
            harvestError.setHint("Check with your administrator.");
            this.errors.add(harvestError);
            throw new OperationAbortedEx("Raised exception when searching", e);
        } catch (Exception e) {
            HarvestError harvestError = new HarvestError(context, e);
            harvestError.setDescription("Error while searching on "
                + params.getName() + ". ");
            harvestError.setHint("Check with your administrator.");
            this.errors.add(harvestError);
            log.warning("Raised exception when searching : " + e);
            throw new OperationAbortedEx("raised exception when searching", e);
        }
    }

    private Map<String, Source> buildSources(Element info) throws BadServerResponseEx {
        Element sources = info.getChild("sources");

        if (sources == null)
            throw new BadServerResponseEx(info);

        Map<String, Source> map = new HashMap<>();

        for (Object o : sources.getChildren()) {
            Element sourceEl = (Element) o;

            String uuid = sourceEl.getChildText("uuid");
            String name = sourceEl.getChildText("name");

            Source source = new Source(uuid, name, new HashMap<>(), SourceType.harvester);
            // If the translation element provided and has values, use it.
            // Otherwise, use the default ones from the name of the source
            if ((sourceEl.getChild("label") != null) &&
                (!sourceEl.getChild("label").getChildren().isEmpty())) {
                source.setLabelTranslationsFromElement(sourceEl.getChild("label").getChildren());
            }
            map.put(uuid, source);
        }

        return map;
    }
}
