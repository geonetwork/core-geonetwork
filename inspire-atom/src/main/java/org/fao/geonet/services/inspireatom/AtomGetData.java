//=============================================================================
//===	Copyright (C) 2001-2010 Food and Agriculture Organization of the
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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.InspireAtomFeed;
import org.fao.geonet.domain.InspireAtomFeedEntry;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.exceptions.ResourceNotFoundEx;
import org.fao.geonet.inspireatom.InspireAtomService;
import org.fao.geonet.inspireatom.util.InspireAtomUtil;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.InspireAtomFeedRepository;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.HttpStatus.OK;

@RequestMapping(value = {
    "/{portal}/api/atom"
})
@Tag(name = "atom",
    description = "ATOM")
@RestController
public class AtomGetData {

    @Autowired
    InspireAtomService service;

    @Autowired
    SettingManager sm;

    @Autowired
    DataManager dm;

    @Autowired
    InspireAtomFeedRepository inspireAtomFeedRepository;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get a data file related to dataset",
        description = "This service if a dataset has only 1 download format for a CRS returns the file, otherwise " +
            "returns a feed with downloads for the dataset.")
    @GetMapping(
        value = "/download/resource",
        produces = MediaType.APPLICATION_XML_VALUE
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Get a data file related to dataset"),
        @ApiResponse(responseCode = "204", description = "Not authenticated.")
    })
    @ResponseStatus(OK)
    @ResponseBody
    public Element downloadResource(
        @Parameter(
            description = "spatial_dataset_identifier_code",
            required = true)
        @RequestParam
            String spatial_dataset_identifier_code,
        @Parameter(
            description = "spatial_dataset_identifier_namespace",
            required = true)
        @RequestParam
            String spatial_dataset_identifier_namespace,
        @Parameter(
            description = "crs",
            required = true)
        @RequestParam
            String crs,
        @Parameter(hidden = true)
            HttpServletRequest request,
        @Parameter(hidden = true)
            HttpServletResponse response
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);

        boolean inspireEnable = sm.getValueAsBool(Settings.SYSTEM_INSPIRE_ENABLE);

        if (!inspireEnable) {
            Log.info(Geonet.ATOM, "Inspire is disabled");
            throw new Exception("Inspire is disabled");
        }
        // Get the metadata uuid for the dataset
        String datasetUuid = service.retrieveDatasetUuidFromIdentifierNs(
            spatial_dataset_identifier_code, spatial_dataset_identifier_namespace);
        if (StringUtils.isEmpty(datasetUuid)) throw new MetadataNotFoundEx(datasetUuid);

        // Retrieve metadata to check existence and permissions.
        String id = dm.getMetadataId(datasetUuid);
        if (StringUtils.isEmpty(id)) throw new MetadataNotFoundEx(datasetUuid);

        Lib.resource.checkPrivilege(context, id, ReservedOperation.view);

        // Retrieve the dataset resources for specified CRS
        InspireAtomFeed inspireAtomFeed = service.findByMetadataId(Integer.parseInt(id));

        // Check the metadata has an atom document.
        String atomUrl = inspireAtomFeed.getAtomUrl();
        if (StringUtils.isEmpty(atomUrl)) throw new ResourceNotFoundEx("Metadata has no atom feed");

        Pair<Integer, InspireAtomFeedEntry> result = countDatasetsForCrs(inspireAtomFeed, crs);
        int downloadCount = result.one();
        InspireAtomFeedEntry selectedEntry = result.two();

        // No download  for the CRS specified
        if (downloadCount == 0) {
            throw new ResourceNotFoundEx("No downloads available for dataset: " + spatial_dataset_identifier_code + " and CRS: " + crs);

            // Only one download for the CRS specified
        } else if (downloadCount == 1) {

            response.setContentType(selectedEntry.getType());
            response.sendRedirect(selectedEntry.getUrl());
            return null;

            // Otherwise, return a feed with the downloads for the specified CRS
        } else {
            // Retrieve the dataset feed
            Element feed = service.retrieveFeed(context, inspireAtomFeed);

            // Filter the dataset feed by CRS code.
            InspireAtomUtil.filterDatasetFeedByCrs(feed, crs);

            return feed;
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
