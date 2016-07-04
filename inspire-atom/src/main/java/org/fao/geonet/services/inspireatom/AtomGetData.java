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

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.inspireatom.InspireAtomService;
import org.fao.geonet.inspireatom.util.InspireAtomUtil;
import org.fao.geonet.domain.InspireAtomFeed;
import org.fao.geonet.domain.InspireAtomFeedEntry;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

import java.nio.file.Path;

/**
 * Service to get a data file related to dataset.
 *
 * This service if a dataset has only 1 download format for a CRS returns the file, otherwise
 * returns a feed with downloads for the dataset.
 *
 * @author Jose García
 */
public class AtomGetData implements Service {

    /**
     * Dataset identifier param name
     **/
    private final static String DATASET_IDENTIFIER_CODE_PARAM = "spatial_dataset_identifier_code";

    /**
     * Dataset namespace param name
     **/
    private final static String DATASET_IDENTIFIER_NS_PARAM = "spatial_dataset_identifier_namespace";

    /**
     * Dataset crs param name
     **/
    private final static String DATASET_CRS_PARAM = "crs";

    public void init(Path appPath, ServiceConfig params) throws Exception {

    }

    //--------------------------------------------------------------------------
    //---
    //--- Exec
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        SettingManager sm = context.getBean(SettingManager.class);

        boolean inspireEnable = sm.getValueAsBool(Settings.SYSTEM_INSPIRE_ENABLE);

        if (!inspireEnable) {
            Log.info(Geonet.ATOM, "Inspire is disabled");
            throw new Exception("Inspire is disabled");
        }

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);
        InspireAtomService service = context.getBean(InspireAtomService.class);

        // Get request parameters
        String datasetIdCode = Util.getParam(params, DATASET_IDENTIFIER_CODE_PARAM);
        String datasetIdNs = Util.getParam(params, DATASET_IDENTIFIER_NS_PARAM);
        String datasetCrs = Util.getParam(params, DATASET_CRS_PARAM);

        // Get the metadata uuid for the dataset
        String datasetUuid = service.retrieveDatasetUuidFromIdentifierNs(datasetIdCode, datasetIdNs);
        if (StringUtils.isEmpty(datasetUuid)) throw new MetadataNotFoundEx(datasetUuid);

        // Retrieve metadata to check existence and permissions.
        String id = dm.getMetadataId(datasetUuid);
        if (StringUtils.isEmpty(id)) throw new MetadataNotFoundEx(datasetUuid);

        Lib.resource.checkPrivilege(context, id, ReservedOperation.view);

        // Retrieve the dataset resources for specified CRS
        InspireAtomFeed inspireAtomFeed = service.findByMetadataId(Integer.parseInt(id));

        // Check the metadata has an atom document.
        String atomUrl = inspireAtomFeed.getAtomUrl();
        if (StringUtils.isEmpty(atomUrl)) throw new Exception("Metadata has no atom feed");

        Pair<Integer, InspireAtomFeedEntry> result = countDatasetsForCrs(inspireAtomFeed, datasetCrs);
        int downloadCount = result.one();
        InspireAtomFeedEntry selectedEntry = result.two();

        // No download  for the CRS specified
        if (downloadCount == 0) {
            throw new Exception("No downloads available for dataset: " + datasetIdCode + " and CRS: " + datasetCrs);

            // Only one download for the CRS specified
        } else if (downloadCount == 1) {

            // Jeeves checks for <reponse redirect="true" url="...." mime-type="..." /> to manage about redirecting
            // to the provided file
            return new Element("response")
                .setAttribute("redirect", "true")
                .setAttribute("url", selectedEntry.getUrl())
                .setAttribute("mime-type", selectedEntry.getType());

            // Otherwise, return a feed with the downloads for the specified CRS
        } else {
            // Retrieve the dataset feed
            Element feed = service.retrieveFeed(context, inspireAtomFeed);

            // Filter the dataset feed by CRS code.
            InspireAtomUtil.filterDatasetFeedByCrs(feed, datasetCrs);

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
