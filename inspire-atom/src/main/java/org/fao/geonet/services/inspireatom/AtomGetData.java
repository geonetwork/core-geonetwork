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

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.inspireatom.InspireAtomService;
import org.fao.geonet.inspireatom.util.InspireAtomUtil;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.jdom.Namespace;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

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

	/** Query param name **/
	private static final String DATASET_Q_PARAM = "q";

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

        String fileIdentifier = Util.getParam(params, AtomDescribe.SERVICE_IDENTIFIER, "");
        String datasetUuid = null;
        String datasetIdCode = null;
        String datasetIdNs = null;
		String datasetCrs = null;
		String searchTerms = null;
		String id = null;
        if (StringUtils.isEmpty(fileIdentifier)) {
            // Get request parameters
            datasetIdCode = Util.getParam(params, DATASET_IDENTIFIER_CODE_PARAM);
            datasetIdNs = Util.getParam(params, DATASET_IDENTIFIER_NS_PARAM);
    		datasetCrs = null;
    		try {
    			datasetCrs = Util.getParam(params, DATASET_CRS_PARAM);
    		} catch (Exception e) {
    		}

    		try {
    			searchTerms = Util.getParam(params,
    					DATASET_Q_PARAM);
    		} catch (Exception e) {
    		}
    		// Get the metadata uuid for the dataset
            datasetUuid = service.retrieveDatasetUuidFromIdentifierNs(datasetIdCode, datasetIdNs);
            if (StringUtils.isEmpty(datasetUuid)) {
            	datasetUuid = InspireAtomUtil.retrieveDatasetUuidFromIdentifier(context, datasetIdCode, "identifier", null);
            }
            if (StringUtils.isEmpty(datasetUuid)) {
            	throw new MetadataNotFoundEx(datasetUuid);
            }

            // Retrieve metadata to check existence and permissions.
            id = dm.getMetadataId(datasetUuid);
        } else {
        	datasetUuid = fileIdentifier;
            id = dm.getMetadataId(datasetUuid);
			Element md = dm.getMetadata(id);
			String schema = dm.getMetadataSchema(id);
			Element datasetIdentifier = dm.extractDatasetCodeAndCodeSpace(schema, md); 
			datasetIdCode = datasetIdentifier.getChildText("code").trim();
			datasetIdNs = datasetIdentifier.getChildText("codeSpace").trim();
        }        	

        if (StringUtils.isEmpty(id)) throw new MetadataNotFoundEx(datasetUuid);

        Lib.resource.checkPrivilege(context, id, ReservedOperation.view);

    	Element datasetAtomFeed = InspireAtomUtil.retrieveLocalAtomFeedDocument(context, (context.getBean(SettingManager.class).getSiteURL(context.getLanguage()) + "atom.local?" +
    			AtomDescribe.DATASET_IDENTIFIER_CODE_PARAM + "=" + datasetIdCode + "&" +
    			AtomDescribe.DATASET_IDENTIFIER_NS_PARAM + "=" + datasetIdNs +
    			(StringUtils.isEmpty(datasetCrs) ? "" : "&" + AtomDescribe.DATASET_CRS_PARAM + "=" + datasetCrs) +
    			(StringUtils.isEmpty(searchTerms) ? "" : "&" + AtomDescribe.DATASET_Q_PARAM + "=" + searchTerms)).replaceAll(":80/","/").replaceAll(":443/","/"));
    	
        Namespace ns = datasetAtomFeed.getNamespace();
        Map<Integer, Element> crsCounts = new HashMap<Integer, Element>();;
        if (datasetCrs!=null) {
            crsCounts = countDatasetsForCrs(datasetAtomFeed, datasetCrs, ns);        	
        } else {
            List<Element> entries = (datasetAtomFeed.getChildren("entry", ns));
            if (entries.size()==1) {
                crsCounts.put(1, entries.get(0));
            }
        }
        int downloadCount = crsCounts.size()>0 ? crsCounts.keySet().iterator().next() : 0;
        Element selectedEntry = crsCounts.get(downloadCount);

        // No download  for the CRS specified
        if (downloadCount == 0) {
            throw new Exception("No downloads available for dataset: " + datasetIdCode + " and CRS: " + datasetCrs);

            // Only one download for the CRS specified
        } else if (downloadCount == 1) {

        	String type = null;
        	Element link = selectedEntry.getChild("link", ns);
        	if (link!=null) {
        		type = link.getAttributeValue("type");
        	}
            // Jeeves checks for <reponse redirect="true" url="...." mime-type="..." /> to manage about redirecting
            // to the provided file
            return new Element("response")
                .setAttribute("redirect", "true")
                .setAttribute("url", selectedEntry.getChildText("id",ns))
                .setAttribute("mime-type",type);
            // Otherwise, return a feed with the downloads for the specified CRS
        } else {
            // Retrieve the dataset feed
        	Element feed = service.retrieveDatasetFeed(context, Integer.parseInt(id), datasetUuid, datasetIdCode, datasetIdNs, datasetCrs, searchTerms);
//            Element feed = service.retrieveFeed(context, inspireAtomFeed);

            // Filter the dataset feed by CRS code.
            InspireAtomUtil.filterDatasetFeedByCrs(feed, datasetCrs);

            return feed;
        }
    }

    private Map<Integer,Element> countDatasetsForCrs(Element datasetAtomFeed, String datasetCrs, Namespace ns) {
        int downloadCount = 0;
        Map<Integer,Element> entryMap = new HashMap<Integer, Element>();
        Element selectedEntry = null;
        Iterator<Element> entries = (datasetAtomFeed.getChildren("entry", ns)).iterator();
        while(entries.hasNext()) {
        	Element entry = entries.next();
        	Element category = entry.getChild("category",ns);
        	if (category!=null) {
            	String term = category.getAttributeValue("term");
	           if (datasetCrs.equals(term)) {
	                selectedEntry = entry;
	                downloadCount++;
	            }
        	}
        }
        entryMap.put(downloadCount, selectedEntry);
        return entryMap;
    }
}
