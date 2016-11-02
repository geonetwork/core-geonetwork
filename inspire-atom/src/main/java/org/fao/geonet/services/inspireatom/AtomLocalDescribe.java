package org.fao.geonet.services.inspireatom;


import java.nio.file.Path;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.inspireatom.InspireAtomService;
import org.fao.geonet.inspireatom.util.InspireAtomUtil;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 * Service to get local Atom feed.
 *
 * @author Gustaaf Vandeboel
 */
public class AtomLocalDescribe implements Service {

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Exec
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception
    {
        SettingManager sm = context.getBean(SettingManager.class);
        boolean inspireEnable = sm.getValueAsBool("system/inspire/enable");

        if (!inspireEnable) {
            Log.info(Geonet.ATOM, "Inspire is disabled");
            throw new Exception("Inspire is disabled");
        }

        // Get request parameters: depending on parameters manage as service feed (fileIdentifier)
        // or a dataset feed (spatial_dataset_identifier_code, spatial_dataset_identifier_namespace)
        String fileIdentifier = Util.getParam(params, AtomDescribe.SERVICE_IDENTIFIER, "");

        if (StringUtils.isEmpty(fileIdentifier)) {
            DataManager dm = context.getBean(DataManager.class);
            InspireAtomService service = context.getBean(InspireAtomService.class);

            // Get request parameters
            String datasetIdCode = Util.getParam(params, AtomDescribe.DATASET_IDENTIFIER_CODE_PARAM);
            String datasetIdNs = Util.getParam(params, AtomDescribe.DATASET_IDENTIFIER_NS_PARAM);
            System.out.println("Searching for atomfeed with id datasetIdCode " + datasetIdCode + " and datasetIdNs " + datasetIdNs);
    		String datasetCrs = null;
    		try {
    			datasetCrs = Util.getParam(params,
    					AtomDescribe.DATASET_CRS_PARAM);
    		} catch (Exception e) {
    		}

    		String searchTerms = null;
    		try {
    			searchTerms = Util.getParam(params,
    					AtomDescribe.DATASET_Q_PARAM);
    		} catch (Exception e) {
    		}

    		Log.debug(Geonet.ATOM, "Processing dataset feed  (" + AtomDescribe.DATASET_IDENTIFIER_CODE_PARAM + ": " +
                datasetIdCode + ", " + AtomDescribe.DATASET_IDENTIFIER_NS_PARAM + ": " + datasetIdNs + " )");

            // Get metadata uuid
            fileIdentifier = service.retrieveDatasetUuidFromIdentifierNs(datasetIdCode, datasetIdNs);
            if (StringUtils.isEmpty(fileIdentifier)) {
            	fileIdentifier = InspireAtomUtil.retrieveDatasetUuidFromIdentifier(context, datasetIdCode, "identifier", null);
            }
            if (StringUtils.isEmpty(fileIdentifier)) {
            	throw new MetadataNotFoundEx("datasetIdCode=" + datasetIdCode);
            }

            // Retrieve metadata to check existence and permissions.
            String id = dm.getMetadataId(fileIdentifier);
            //fileIdentifier = dm.getMetadataUuid(id);
            return InspireAtomUtil.getDatasetFeed(dm, context, id, fileIdentifier, datasetIdCode, datasetIdNs, datasetCrs, searchTerms);
        } else {
            System.out.println("Searching for atomfeed with id fileIdentifier " + fileIdentifier);
            return InspireAtomUtil.getServiceFeed(fileIdentifier, context, null);
        }
    }

}