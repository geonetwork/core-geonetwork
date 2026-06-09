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
package org.fao.geonet.services.inspireatom;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.InspireAtomFeed;
import org.fao.geonet.domain.InspireAtomFeedEntry;
import org.fao.geonet.exceptions.ResourceNotFoundEx;
import org.fao.geonet.guiapi.search.XsltResponseWriter;
import org.fao.geonet.inspireatom.InspireAtomService;
import org.fao.geonet.inspireatom.harvester.InspireAtomHarvester;
import org.fao.geonet.inspireatom.model.DatasetFeedInfo;
import org.fao.geonet.inspireatom.util.InspireAtomUtil;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.InspireAtomFeedRepository;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fao.geonet.inspireatom.util.InspireAtomUtil.retrieveKeywordsFromFileIdentifier;
import static org.springframework.http.HttpStatus.OK;

@RequestMapping(value = {
    "/{portal}/api/atom"
})
@Tag(name = "atom",
    description = "ATOM")
@RestController
public class AtomServiceDescription {

    @Autowired
    InspireAtomService service;

    @Autowired
    SettingManager sm;

    @Autowired
    DataManager dm;

    @Autowired
    InspireAtomFeedRepository inspireAtomFeedRepository;


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Describe service",
        description = "")
    @GetMapping(
        value = "/describe/service/{metadataUuid}",
        produces = MediaType.APPLICATION_XML_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feeds."),
        @ApiResponse(responseCode = "204", description = "Not authenticated.", content = {@Content(schema = @Schema(hidden = true))})
    })
    @ResponseStatus(OK)
    @ResponseBody
    public Element describe(
        @Parameter(
            description = "metadataUuid",
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(hidden = true)
        HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        boolean inspireEnable = sm.getValueAsBool(Settings.SYSTEM_INSPIRE_ENABLE);

        if (!inspireEnable) {
            Log.info(Geonet.ATOM, "Inspire is disabled");
            throw new Exception("Inspire is disabled");
        }

        AbstractMetadata metadataRecord;
        try {
            metadataRecord = ApiUtils.canViewRecord(metadataUuid, request);
        } catch (ResourceNotFoundException e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
        }

        Element md = metadataRecord.getXmlData(false);
        String schema = metadataRecord.getDataInfo().getSchemaId();
        String id = String.valueOf(metadataRecord.getId());

        String atomProtocol = sm.getValue(Settings.SYSTEM_INSPIRE_ATOM_PROTOCOL);

        // Check if it is a service metadata
        if (!InspireAtomUtil.isServiceMetadata(dm, schema, md)) {
            throw new Exception("No service metadata found with uuid:" + metadataUuid);
        }

        // Get dataset identifiers referenced by service metadata.
        InspireAtomFeed inspireAtomFeed = service.findByMetadataId(Integer.parseInt(id));
        if (inspireAtomFeed == null) {
            String serviceFeedUrl = InspireAtomUtil.extractAtomFeedUrl(schema, md, dm, atomProtocol);

            if (StringUtils.isEmpty(serviceFeedUrl)) {
                throw new ResourceNotFoundEx("No atom feed for service metadata found with uuid:" + metadataUuid);
            } else {
                InspireAtomHarvester inspireAtomHarvester = new InspireAtomHarvester(gc);
                inspireAtomHarvester.harvestServiceMetadata(context, id);

                inspireAtomFeed = service.findByMetadataId(Integer.parseInt(id));

                if (inspireAtomFeed == null) {
                    throw new ResourceNotFoundEx("No atom feed for service metadata found with uuid:" + metadataUuid);
                }
            }
        }


        // Check the metadata has an atom document (checks in the lucene index).
        String atomUrl = inspireAtomFeed.getAtomUrl();

        // If no atom document indexed, check if still metadata has feed url --> no processed by atom harvester yet
        if (StringUtils.isEmpty(atomUrl)) {
            atomUrl = InspireAtomUtil.extractAtomFeedUrl(schema, md, dm, atomProtocol);
            if (StringUtils.isEmpty(atomUrl)) throw new Exception("Metadata has no atom feed");

            InspireAtomHarvester inspireAtomHarvester = new InspireAtomHarvester(gc);
            inspireAtomHarvester.harvestServiceMetadata(context, id);

            // Read again the feed
            inspireAtomFeed = service.findByMetadataId(Integer.parseInt(id));
        }

        // Dataset feeds referenced by service feed.
        List<DatasetFeedInfo> datasetsInformation = InspireAtomUtil.extractRelatedDatasetsInfoFromServiceFeed(inspireAtomFeed.getAtom(), dm);

        // Get information from the service atom feed.
        String feedAuthorName = inspireAtomFeed.getAuthorName();
        String feedTitle = inspireAtomFeed.getTitle();
        String feedSubtitle = inspireAtomFeed.getSubtitle();
        String feedLang = inspireAtomFeed.getLang();
        String feedUrl = inspireAtomFeed.getAtomUrl();
        List<String> keywords = retrieveKeywordsFromFileIdentifier(context, metadataUuid);

        // Process datasets information
        Element datasetsEl = processDatasetsInfo(datasetsInformation, metadataUuid);

        Element response = new Element("response")
            .addContent(new Element("fileId").setText(metadataUuid))
            .addContent(new Element("title").setText(feedTitle))
            .addContent(new Element("subtitle").setText(feedSubtitle))
            .addContent(new Element("lang").setText(feedLang))
            .addContent(new Element("keywords").setText(StringUtils.join(keywords, ", ")))
            .addContent(new Element("authorName").setText(feedAuthorName))
            .addContent(new Element("url").setText(feedUrl))
            .addContent(datasetsEl);

        return new XsltResponseWriter(null, "opensearch")
            .withXml(response)
            .withXsl("xslt/services/inspire-atom/opensearch.xsl")
            .asElement();
    }


    /**
     * Retrieves the information from datasets referenced in a service metadata.
     *
     * @param datasetsInformation List of dataset identifiers to process.
     * @param serviceIdentifier  Service identifier.
     * @return JDOM Element with the datasets information.
     * @throws Exception Exception.
     */
    private Element processDatasetsInfo(final List<DatasetFeedInfo> datasetsInformation, final String serviceIdentifier)
        throws Exception {
        Element datasetsEl = new Element("datasets");

        for (DatasetFeedInfo datasetFeedInfo : datasetsInformation) {
            // Get the metadata id for the dataset
            InspireAtomFeed inspireAtomFeed;
            List<InspireAtomFeed> inspireAtomFeedList = inspireAtomFeedRepository.findAllByAtomDatasetid(datasetFeedInfo.identifier);
            if (!inspireAtomFeedList.isEmpty()) {
                inspireAtomFeed = inspireAtomFeedList.get(0);
            } else {
                // If dataset metadata not found, ignore
                Log.warning(Geonet.ATOM, String.format("AtomServiceDescription for service metadata (%s): metadata "
                    + "for dataset identifier %s was not found, ignoring it.",
                    serviceIdentifier, datasetFeedInfo.identifier));
                continue;
            }

            String datasetUuid = dm.getMetadataUuid(String.valueOf(inspireAtomFeed.getMetadataId()));

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
            Map<String, Integer> downloadsCountByCrs = new HashMap<>();
            for (InspireAtomFeedEntry entry : inspireAtomFeed.getEntryList()) {
                Integer count = downloadsCountByCrs.get(entry.getCrs());
                if (count == null) count = 0;
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
}
