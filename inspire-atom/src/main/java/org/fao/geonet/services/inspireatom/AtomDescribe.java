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

import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.inspireatom.InspireAtomService;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Log;
import org.fao.geonet.Util;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.inspireatom.util.InspireAtomUtil;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;

import java.nio.file.Path;

/**
 * Service to get Atom feed.
 *
 * @author Jose García
 */
public class AtomDescribe implements Service {

    /**
     * Dataset identifier param name
     **/
    private final static String DATASET_IDENTIFIER_CODE_PARAM = "spatial_dataset_identifier_code";

    /**
     * Dataset namespace param name
     **/
    private final static String DATASET_IDENTIFIER_NS_PARAM = "spatial_dataset_identifier_namespace";

    /**
     * Service identifier param name
     **/
    private final static String SERVICE_IDENTIFIER = "fileIdentifier";

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

        // Get request parameters: depending on parameters manage as service feed (fileIdentifier)
        // or a dataset feed (spatial_dataset_identifier_code, spatial_dataset_identifier_namespace)
        String fileIdentifier = Util.getParam(params, SERVICE_IDENTIFIER, "");

        if (StringUtils.isEmpty(fileIdentifier)) {
            return processDatasetFeed(params, context);
        } else {
            return processServiceFeed(params, context);
        }
    }

    /**
     * Process a dataset feed.
     */
    private Element processDatasetFeed(Element params, ServiceContext context) throws Exception {
        DataManager dm = context.getBean(DataManager.class);
        InspireAtomService service = context.getBean(InspireAtomService.class);

        // Get request parameters
        String datasetIdCode = Util.getParam(params, DATASET_IDENTIFIER_CODE_PARAM);
        String datasetIdNs = Util.getParam(params, DATASET_IDENTIFIER_NS_PARAM);

        Log.debug(Geonet.ATOM, "Processing dataset feed  (" + DATASET_IDENTIFIER_CODE_PARAM + ": " +
            datasetIdCode + ", " + DATASET_IDENTIFIER_NS_PARAM + ": " + datasetIdNs + " )");

        // Get metadata uuid
        String datasetUuid = service.retrieveDatasetUuidFromIdentifierNs(datasetIdCode, datasetIdNs);
        if (StringUtils.isEmpty(datasetUuid)) throw new MetadataNotFoundEx(datasetUuid);

        // Retrieve metadata to check existence and permissions.
        String id = dm.getMetadataId(datasetUuid);
        if (StringUtils.isEmpty(id)) throw new MetadataNotFoundEx(datasetUuid);

        // Check if allowed to the metadata
        Lib.resource.checkPrivilege(context, id, ReservedOperation.view);

        return service.retrieveFeed(context, Integer.parseInt(id));
    }

    /**
     * Process a service feed.
     */
    private Element processServiceFeed(Element params, ServiceContext context) throws Exception {
        String fileIdentifier = Util.getParam(params, SERVICE_IDENTIFIER);
        Log.debug(Geonet.ATOM, "Processing service feed  (" + SERVICE_IDENTIFIER + ": " + fileIdentifier + " )");

        InspireAtomService service = context.getBean(InspireAtomService.class);

        // Retrieve metadata to check existence and permissions.
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);

        String id = dm.getMetadataId(fileIdentifier);
        if (StringUtils.isEmpty(id)) throw new MetadataNotFoundEx(fileIdentifier);

        // Check if allowed to the metadata
        Lib.resource.checkPrivilege(context, id, ReservedOperation.view);

        // Check if it is a service metadata
        Element md = dm.getMetadata(id);
        String schema = dm.getMetadataSchema(id);
        if (!InspireAtomUtil.isServiceMetadata(dm, schema, md)) {
            throw new Exception("No service metadata found with uuid:" + fileIdentifier);
        }

        return service.retrieveFeed(context, Integer.parseInt(id));
    }
}
