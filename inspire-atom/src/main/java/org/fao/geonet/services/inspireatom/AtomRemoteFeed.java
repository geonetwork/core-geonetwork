//=============================================================================
//===	Copyright (C) 2001-2022 Food and Agriculture Organization of the
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
package org.fao.geonet.services.inspireatom;

import com.google.common.collect.ImmutableSet;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.fao.geonet.Constants;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.es.EsHTTPProxy;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.exceptions.OperationNotAllowedEx;
import org.fao.geonet.exceptions.ResourceNotFoundEx;
import org.fao.geonet.inspireatom.InspireAtomService;
import org.fao.geonet.inspireatom.harvester.InspireAtomHarvesterService;
import org.fao.geonet.inspireatom.model.DatasetFeedInfo;
import org.fao.geonet.inspireatom.util.InspireAtomUtil;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.InspireAtomFeedRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;


/**
 * Services for converting Service and Data metadata into INSPIRE ATOM feeds
 *
 */
@Controller
@RequestMapping(value = "/{portal}/api/opensearch")
public class AtomRemoteFeed {

    @Autowired
    EsSearchManager searchManager;

    @Autowired
    EsHTTPProxy esHTTPProxy;

    @Autowired
    SettingManager settingManager;

    @Autowired
    DataManager dataManager;

    @Autowired
    InspireAtomService inspireAtomService;

    @Autowired
    InspireAtomHarvesterService inspireAtomHarvesterService;


    /**
     * Main entry point for local open search description
     *
     * @param language the language to be used for translation of title, etc. in the resulting opensearchdescription
     * @param uuid identifier of the metadata of service (this could be made optional once a system-wide top level metadata could be set)
     */
    @RequestMapping(value = "/OpenSearchDescription.xml")
    @ResponseBody
    public HttpEntity<byte[]> openSearchDescription(
        @RequestParam("uuid") String uuid,
        @RequestParam(value = "language", required = false) String language,
        HttpServletRequest request) throws Exception {

        ServiceContext context = ApiUtils.createServiceContext(request, language);

        checkInspireSettingIsEnabled();

        // Check if allowed to view the metadata
        AbstractMetadata metadata = ApiUtils.canViewRecord(uuid, request);

        Element md = metadata.getXmlData(false);
        String schema = metadata.getDataInfo().getSchemaId();

        // Check if it is a service metadata
        if (!InspireAtomUtil.isServiceMetadata(dataManager, schema, md)) {
            throw new Exception("No service metadata found with uuid:" + uuid);
        }

        String atomProtocol = settingManager.getValue(Settings.SYSTEM_INSPIRE_ATOM_PROTOCOL);

        // Get dataset identifiers referenced by service metadata.
        List<String> datasetIdentifiers = null;

        InspireAtomFeed inspireAtomFeed = inspireAtomService.findByMetadataId(metadata.getId());
        if (inspireAtomFeed == null) {
            String serviceFeedUrl = InspireAtomUtil.extractAtomFeedUrl(schema, md, dataManager, atomProtocol);

            if (StringUtils.isEmpty(serviceFeedUrl)) {
                throw new ResourceNotFoundEx("No atom feed for service metadata found with uuid:" + uuid);
            } else {
                inspireAtomHarvesterService.harvestServiceMetadata(context, String.valueOf(metadata.getId()));

                inspireAtomFeed = inspireAtomService.findByMetadataId(metadata.getId());

                if (inspireAtomFeed == null) {
                    throw new ResourceNotFoundEx("No atom feed for service metadata found with uuid:" + uuid);
                }
            }
        }


        // Check the metadata has an atom document (checks in the lucene index).
        String atomUrl = inspireAtomFeed.getAtomUrl();

        // If no atom document indexed, check if still metadata has feed url --> no processed by atom harvester yet
        if (StringUtils.isEmpty(atomUrl)) {
            atomUrl = InspireAtomUtil.extractAtomFeedUrl(schema, md, dataManager, atomProtocol);
            if (StringUtils.isEmpty(atomUrl)) throw new Exception("Metadata has no atom feed");

            inspireAtomHarvesterService.harvestServiceMetadata(context, String.valueOf(metadata.getId()));

            // Read again the feed
            inspireAtomFeed = inspireAtomService.findByMetadataId(metadata.getId());
        }

        // Dataset feeds referenced by service feed.
        List<DatasetFeedInfo> datasetsInformation = InspireAtomUtil.extractRelatedDatasetsInfoFromServiceFeed(inspireAtomFeed.getAtom(), dataManager);

        // Get information from the the service atom feed.
        String feedAuthorName = inspireAtomFeed.getAuthorName();
        String feedTitle = inspireAtomFeed.getTitle();
        String feedSubtitle = inspireAtomFeed.getSubtitle();
        String feedLang = inspireAtomFeed.getLang();
        String feedUrl = inspireAtomFeed.getAtomUrl();

        // TODOES
        List<String> keywords = new ArrayList<>();

        Set<String> FIELDLIST_KEYWORD = ImmutableSet.<String>builder()
            .add("tag").build();

        final SearchResponse searchResponse = searchManager.query(
            String.format("uuid:\"%s\"", uuid),
            esHTTPProxy.buildPermissionsFilter(context),
            FIELDLIST_KEYWORD, 0, 20);

        Arrays.asList(searchResponse.getHits().getHits()).forEach(h -> {
                ArrayList<HashMap<String, String>> mdKeywordsList =  (ArrayList<HashMap<String, String>>) h.getSourceAsMap().get("tag");

                mdKeywordsList.forEach(k -> {
                    // TODO: handle language
                    keywords.add((String) k.get("default"));
                });
            });

        // Process datasets information
        Element datasetsEl = processDatasetsInfo(datasetsInformation, uuid, context);

        Element data = new Element("response")
            .addContent(new Element("fileId").setText(uuid))
            .addContent(new Element("title").setText(feedTitle))
            .addContent(new Element("subtitle").setText(feedSubtitle))
            .addContent(new Element("lang").setText(feedLang))
            .addContent(new Element("keywords").setText(String.join(", ", keywords)))
            .addContent(new Element("authorName").setText(feedAuthorName))
            .addContent(new Element("url").setText(feedUrl))
            .addContent(datasetsEl);


        // Build response.
        Path styleSheet = context.getAppPath().resolve(Geonet.Path.XSLT_FOLDER)
            .resolve("services/inspire-atom/")
            .resolve("opensearch.xsl");
        Element openSearchDescriptionDoc = Xml.transform(data, styleSheet);

        return writeOutResponse(Xml.getString(openSearchDescriptionDoc), "application", "opensearchdescription+xml");

    }


    /**
     * Main entry point for local service ATOM feed description
     *
     * @param language the language to be used for translation of title, etc. in the resulting service ATOM feed
     * @param uuid identifier of the metadata of service (this could be made optional once a system-wide top level metadata could be set)
     */
    @RequestMapping(value = "/service/describe")
    @ResponseBody
    public HttpEntity<byte[]> localServiceDescribe(
        @RequestParam("uuid") String uuid,
        @RequestParam(value = "language", required = false) String language,
        HttpServletRequest request) throws Exception {

        ServiceContext context = ApiUtils.createServiceContext(request, language);

        checkInspireSettingIsEnabled();

        Log.debug(Geonet.ATOM, "Processing service feed  (" + "uuid" + ": " + uuid + " )");

        // Check if allowed to view the metadata
        AbstractMetadata metadata = ApiUtils.canViewRecord(uuid, request);

        // Check if it is a service metadata
        Element md = metadata.getXmlData(false);
        String schema = metadata.getDataInfo().getSchemaId();
        if (!InspireAtomUtil.isServiceMetadata(dataManager, schema, md)) {
            throw new Exception("No service metadata found with uuid:" + uuid);
        }

        Element feed = inspireAtomService.retrieveFeed(context, metadata.getId());

        return writeOutResponse(Xml.getString(feed), "application", "atom+xml");
    }


    /**
     * Main entry point for local dataset ATOM feed description.
     * @param language
     * @param spIdentifier
     * @param spNamespace
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/dataset/describe")
    @ResponseBody
    public HttpEntity<byte[]> atomDatasetDescribe(
            @RequestParam(value = "language", required = false) String language,
            @RequestParam("spatial_dataset_identifier_code") String spIdentifier,
            @RequestParam(value = "spatial_dataset_identifier_namespace", required = false) String spNamespace,
            HttpServletRequest request) throws Exception {

        ServiceContext context = ApiUtils.createServiceContext(request, language);

        checkInspireSettingIsEnabled();

        Log.debug(Geonet.ATOM, "Processing dataset feed  (" + "spatial_dataset_identifier_code" + ": " +
            spIdentifier + ", " + "spatial_dataset_identifier_namespace" + ": " + spNamespace + " )");

        // Get metadata uuid
        String datasetUuid = inspireAtomService.retrieveDatasetUuidFromIdentifierNs(spIdentifier, spNamespace);
        if (StringUtils.isEmpty(datasetUuid)) throw new MetadataNotFoundEx(datasetUuid);

        // Check if allowed to view the metadata
        AbstractMetadata metadata = ApiUtils.canViewRecord(datasetUuid, request);

        Element feed = inspireAtomService.retrieveFeed(context, metadata.getId());

        return writeOutResponse(Xml.getString(feed), "application", "atom+xml");
    }


    /**
     * Main entry point for local dataset ATOM feed download.
     *
     * @param spIdentifier the spatial dataset identifier
     * @param spNamespace the spatial dataset namespace (not used for the moment)
     * @param crs the crs of the dataset
     * @param language the language to be used for translation of title, etc. in the resulting dataset ATOM feed
     * @param searchTerms the searchTerms for filtering of the spatial datasets
     * @param request the request object
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/dataset/download")
    @ResponseBody
    public HttpEntity<byte[]> datasetDownload(
        @RequestParam("spatial_dataset_identifier_code") String spIdentifier,
        @RequestParam(value = "spatial_dataset_identifier_namespace", required = false) String spNamespace,
        @RequestParam(value = "crs", required = false) String crs,
        @RequestParam(value = "language", required = false) String language,
        @RequestParam(value = "q", required = false) String searchTerms,
        HttpServletRequest request) throws Exception {

        ServiceContext context = ApiUtils.createServiceContext(request, language);

        checkInspireSettingIsEnabled();

        // Get the metadata uuid for the dataset
        String datasetUuid = inspireAtomService.retrieveDatasetUuidFromIdentifierNs(spIdentifier, spNamespace);
        if (StringUtils.isEmpty(datasetUuid)) throw new MetadataNotFoundEx(datasetUuid);

        // Check if allowed to view the metadata
        AbstractMetadata metadata = ApiUtils.canViewRecord(datasetUuid, request);

        // Retrieve the dataset resources for specified CRS
        InspireAtomFeed inspireAtomFeed = inspireAtomService.findByMetadataId(metadata.getId());

        // Check the metadata has an atom document.
        String atomUrl = inspireAtomFeed.getAtomUrl();
        if (StringUtils.isEmpty(atomUrl)) throw new Exception("Metadata has no atom feed");

        Pair<Integer, InspireAtomFeedEntry> result = countDatasetsForCrs(inspireAtomFeed, crs);
        int downloadCount = result.one();
        InspireAtomFeedEntry selectedEntry = result.two();

        // No download  for the CRS specified
        if (downloadCount == 0) {
            throw new Exception("No downloads available for dataset: " + spIdentifier + " and CRS: " + crs);

            // Only one download for the CRS specified
        } else if (downloadCount == 1) {
            return redirectResponse(selectedEntry.getUrl(), selectedEntry.getType());

            // Otherwise, return a feed with the downloads for the specified CRS
        } else {
            // Retrieve the dataset feed
            Element feed = inspireAtomService.retrieveFeed(context, inspireAtomFeed);

            // Filter the dataset feed by CRS code.
            InspireAtomUtil.filterDatasetFeedByCrs(feed, crs);

            return writeOutResponse(Xml.getString(feed),"application", "atom+xml");
        }
    }

    private HttpEntity<byte[]> writeOutResponse(String content, String contentType, String contentSubType) throws Exception {
        byte[] documentBody = content.getBytes(Constants.ENCODING);

        HttpHeaders header = new HttpHeaders();
        // TODO: character-set encoding ?
        header.setContentType(new MediaType(contentType, contentSubType, Charset.forName(Constants.ENCODING)));
        header.setContentLength(documentBody.length);
        return new HttpEntity<>(documentBody, header);
    }

    private HttpEntity<byte[]> redirectResponse(String location, String mimeType) throws Exception {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType(mimeType));
        header.setContentLength(0);
        header.setLocation(new URI(location));
        return new HttpEntity<>(header);
    }

    /**
     * Retrieves the information from datasets referenced in a service metadata.
     *
     * @param datasetsInformation List of dataset identifiers to process.
     * @param serviceIdentifier  Service identifier.
     * @param context            Service context.
     * @return JDOM Element with the datasets information.
     * @throws Exception Exception.
     */
    private Element processDatasetsInfo(final List<DatasetFeedInfo> datasetsInformation, final String serviceIdentifier,
                                        final ServiceContext context)
        throws Exception {
        Element datasetsEl = new Element("datasets");

        final InspireAtomFeedRepository repository = context.getBean(InspireAtomFeedRepository.class);

        DataManager dm = context.getBean(DataManager.class);

        for (DatasetFeedInfo datasetFeedInfo : datasetsInformation) {
            // Get the metadata uuid for the dataset
            String datasetUuid = repository.retrieveDatasetUuidFromIdentifier(datasetFeedInfo.identifier);

            // If dataset metadata not found, ignore
            if (StringUtils.isEmpty(datasetUuid)) {
                Log.warning(Geonet.ATOM, "AtomServiceDescription for service metadata (" + serviceIdentifier +
                    "): metadata for dataset identifier " + datasetFeedInfo.identifier + " is not found, ignoring it.");
                continue;
            }

            String id = dm.getMetadataId(datasetUuid);
            InspireAtomFeed inspireAtomFeed = repository.findByMetadataId(Integer.parseInt(id));

            String idNs = inspireAtomFeed.getAtomDatasetid();
            String namespace = inspireAtomFeed.getAtomDatasetns();

            // If dataset metadata has no identifier information, ignore
            if (StringUtils.isEmpty(idNs)) {
                Log.warning(Geonet.ATOM, "AtomServiceDescription for service metadata (" + serviceIdentifier +
                    "): dataset with uuid " + datasetUuid + " has no dataset identifier/namespace, ignoring it.");
                continue;
            }

            String atomUrl = inspireAtomFeed.getAtomUrl();
            // If the dataset has no atom feed, ignore it
            if (StringUtils.isEmpty(atomUrl)) {
                Log.warning(Geonet.ATOM, "AtomServiceDescription for service metadata (" + serviceIdentifier +
                    "): dataset with uuid " + datasetUuid + " has no dataset feed, ignoring it.");
                continue;
            }

            Element datasetEl = buildDatasetInfo(idNs, namespace);
            datasetEl.addContent(new Element("atom_url").setText(atomUrl));

            // Get dataset download info
            // From INSPIRE spec: if a CRS has multiple downloads should be returned a link to feed document with the CRS downloads.
            Map<String, Integer> downloadsCountByCrs = new HashMap<String, Integer>();
            for (InspireAtomFeedEntry entry : inspireAtomFeed.getEntryList()) {
                Integer count = downloadsCountByCrs.get(entry.getCrs());
                if (count == null) count = Integer.valueOf(0);
                downloadsCountByCrs.put(entry.getCrs(), count + 1);
            }

            for (InspireAtomFeedEntry entry : inspireAtomFeed.getEntryList()) {
                Integer count = downloadsCountByCrs.get(entry.getCrs());
                if (count != null) {
                    Element downloadEl = new Element("file");
                    downloadEl.addContent(new Element("title").setText(entry.getTitle()));
                    downloadEl.addContent(new Element("lang").setText(entry.getLang()));
                    downloadEl.addContent(new Element("url").setText(entry.getUrl()));
                    if (count > 1) {
                        downloadEl.addContent(new Element("type").setText("application/atom+xml"));
                    } else {
                        downloadEl.addContent(new Element("type").setText(entry.getType()));
                    }

                    downloadEl.addContent(new Element("crs").setText(entry.getCrs()));
                    datasetEl.addContent(downloadEl);

                    // Remove from map to not process further downloads with same CRS,
                    // only 1 entry with type= is added in result
                    downloadsCountByCrs.remove(entry.getCrs());
                }
            }

            datasetsEl.addContent(datasetEl);
        }

        return datasetsEl;
    }


    /**
     * Builds JDOM element for dataset information.
     *
     * @param identifier Dataset identifier.
     * @param namespace  Dataset namespace.
     */
    private Element buildDatasetInfo(final String identifier, final String namespace) {
        Element datasetEl = new Element("dataset");

        Element codeEl = new Element("code");
        codeEl.setText(identifier);

        Element namespaceEl = new Element("namespace");
        namespaceEl.setText(namespace);

        datasetEl.addContent(codeEl);
        datasetEl.addContent(namespaceEl);

        return datasetEl;
    }


    private void checkInspireSettingIsEnabled() {
        boolean inspireEnable = settingManager.getValueAsBool(Settings.SYSTEM_INSPIRE_ENABLE);
        if (!inspireEnable) {
            Log.info(Geonet.ATOM, "INSPIRE is disabled");
            throw new OperationNotAllowedEx("INSPIRE option is not enabled on this catalog.");
        }
    }

    /**
     * Calculates the downloads for the specified crs.
     *
     * @return Pair of number of downloads and selected download for the crs (only used if downloads
     * for crs = 1)
     */
    private Pair<Integer, InspireAtomFeedEntry> countDatasetsForCrs(InspireAtomFeed inspireAtomFeed, String datasetCrs) {
        int downloadCount = 0;
        InspireAtomFeedEntry selectedEntry = null;
        for (InspireAtomFeedEntry entry : inspireAtomFeed.getEntryList()) {
            if (datasetCrs.equals(entry.getCrs())) {
                selectedEntry = entry;
                downloadCount++;
            }
        }

        return Pair.write(downloadCount, selectedEntry);
    }
}
