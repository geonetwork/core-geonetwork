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
package org.fao.geonet.inspireatom;

import java.util.Arrays;

import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.csw.common.util.Xml;
import org.fao.geonet.domain.InspireAtomFeed;
import org.fao.geonet.inspireatom.util.InspireAtomUtil;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.InspireAtomFeedRepository;
import org.fao.geonet.services.inspireatom.AtomDescribe;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jeeves.server.context.ServiceContext;

@Component
@Transactional
public class InspireAtomService {

    @Autowired
    private InspireAtomFeedRepository _repository;

    public Element retrieveServiceFeed(DataManager dm, ServiceContext context, int metadataId, String fileIdentifier) throws Exception {
        // Check the metadata has an atom document.
        InspireAtomFeed feed = _repository.findByMetadataId(metadataId);
        String feedValue = null;
        String baseAtomUrl = InspireAtomUtil.getBaseServiceAtomUrl(context, fileIdentifier);
        String atomUrl = feed!=null ? feed.getAtomUrl():"";
        boolean bLocalUrl = atomUrl.equals(baseAtomUrl) ? true : false;
        if (bLocalUrl || StringUtils.isEmpty(atomUrl)) {
        	feedValue = InspireAtomUtil.retrieveLocalAtomFeedDocumentAsString(context, (context.getBean(SettingManager.class).getSiteURL(context.getLanguage()) + "atom.local?" + AtomDescribe.SERVICE_IDENTIFIER + "=" + fileIdentifier).replaceAll(":80/","/").replaceAll(":443/","/"));
	        if (StringUtils.isEmpty(atomUrl)) {
	            Element atomDoc = Xml.loadString(feedValue, false);
	            InspireAtomFeed inspireAtomFeed = InspireAtomFeed.build(atomDoc);
	            inspireAtomFeed.setMetadataId(metadataId);
	            inspireAtomFeed.setAtomUrl(baseAtomUrl);
	            inspireAtomFeed.setAtom(feedValue);
	            inspireAtomFeed.setAtomDatasetid("");
	            inspireAtomFeed.setAtomDatasetns("");
	
	
	//            _repository.save(inspireAtomFeed);
	            _repository.saveAndFlush(inspireAtomFeed);
	
	            // Index the metadata to store the atom feed information in the index
	            dm.indexMetadata(Arrays.asList(new String[]{String.valueOf(metadataId)}));
	            feed = _repository.findByMetadataId(metadataId);
	            atomUrl = feed.getAtomUrl();
	        }
        } else {
//            if (StringUtils.isEmpty(atomUrl)) throw new Exception("Metadata has no atom feed");

            // Retrieve the remote feed
            feedValue = InspireAtomUtil.retrieveRemoteAtomFeedDocument(context, atomUrl);
        }


        if (StringUtils.isEmpty(feedValue)) {
            feedValue = feed.getAtom();
        }

        return Xml.loadString(feedValue, false);
    }

    public Element retrieveDatasetFeed(ServiceContext context, int metadataId, String fileIdentifier, String datasetIdCode, String datasetIdNs, String crs, String searchTerms) throws Exception {
        // Check the metadata has an atom document.
        InspireAtomFeed feed = _repository.findByMetadataId(metadataId);
        String feedValue = null;
        String atomUrl = feed!=null ? feed.getAtomUrl():"";
//        NodeInfo nodeInfo = ApplicationContextHolder.get().getBean(NodeInfo.class);
        String baseAtomUrl = InspireAtomUtil.getBaseDatasetAtomUrl(context);
        boolean bLocalUrl = atomUrl.startsWith(baseAtomUrl) ? true : false;
        if (bLocalUrl || StringUtils.isEmpty(atomUrl)) {
        	
        	feedValue = InspireAtomUtil.retrieveLocalAtomFeedDocumentAsString(context, (context.getBean(SettingManager.class).getSiteURL(context.getLanguage()) + "atom.local?" +
        			AtomDescribe.DATASET_IDENTIFIER_CODE_PARAM + "=" + datasetIdCode + "&" +
        			AtomDescribe.DATASET_IDENTIFIER_NS_PARAM + "=" + datasetIdNs +
        			(StringUtils.isEmpty(crs) ? "" : "&" + AtomDescribe.DATASET_CRS_PARAM + "=" + crs) +
        			(StringUtils.isEmpty(searchTerms) ? "" : "&" + AtomDescribe.DATASET_Q_PARAM + "=" + searchTerms)).replaceAll(":80/","/").replaceAll(":443/","/"));
	        if (StringUtils.isEmpty(atomUrl)) {
		        DataManager dm = context.getBean(DataManager.class);
	            Element atomDoc = Xml.loadString(feedValue, false);
	            InspireAtomFeed inspireAtomFeed = InspireAtomFeed.build(atomDoc);
	            inspireAtomFeed.setMetadataId(metadataId);
	            inspireAtomFeed.setAtomUrl(baseAtomUrl + "?" +
	        			AtomDescribe.DATASET_IDENTIFIER_CODE_PARAM + "=" + datasetIdCode + "&" +
	        			AtomDescribe.DATASET_IDENTIFIER_NS_PARAM + "=" + datasetIdNs +
	        			(StringUtils.isEmpty(crs) ? "" : "&" + AtomDescribe.DATASET_CRS_PARAM + "=" + crs) +
	        			(StringUtils.isEmpty(searchTerms) ? "" : "&" + AtomDescribe.DATASET_Q_PARAM + "=" + searchTerms));
	            inspireAtomFeed.setAtom(feedValue);
	            inspireAtomFeed.setAtomDatasetid(datasetIdCode);
	            inspireAtomFeed.setAtomDatasetns(datasetIdNs);
	
	
	//            _repository.save(inspireAtomFeed);
	            _repository.saveAndFlush(inspireAtomFeed);
	
	            // Index the metadata to store the atom feed information in the index
	            dm.indexMetadata(Arrays.asList(new String[]{String.valueOf(metadataId)}));
	            feed = _repository.findByMetadataId(metadataId);
	            atomUrl = feed.getAtomUrl();
	        }
        } else {
//            if (StringUtils.isEmpty(atomUrl)) throw new Exception("Metadata has no atom feed");

            // Retrieve the remote feed
            feedValue = InspireAtomUtil.retrieveRemoteAtomFeedDocument(context, atomUrl);
        }


        if (StringUtils.isEmpty(feedValue)) {
            feedValue = feed.getAtom();
        }

        return Xml.loadString(feedValue, false);
    }

/*    
    public Element retrieveFeed(ServiceContext context, int metadataId) throws Exception {
        // Check the metadata has an atom document.
        InspireAtomFeed feed = _repository.findByMetadataId(metadataId);
        String atomUrl = feed.getAtomUrl();

        if (StringUtils.isEmpty(atomUrl)) throw new Exception("Metadata has no atom feed");

        // Retrieve the remote feed
        String feedValue = InspireAtomUtil.retrieveRemoteAtomFeedDocument(context, atomUrl);

        if (StringUtils.isEmpty(feedValue)) {
            feedValue = feed.getAtom();
        }

        return Xml.loadString(feedValue, false);
    }

    public Element retrieveFeed(ServiceContext context, InspireAtomFeed feed) throws Exception {
        // Check the metadata has an atom document.
        return retrieveFeed(context, feed.getMetadataId());
    }
*/
    public String retrieveDatasetUuidFromIdentifierNs(String datasetIdCode, String datasetIdNs) {
        return _repository.retrieveDatasetUuidFromIdentifierNs(datasetIdCode, datasetIdNs);
    }
    
    public InspireAtomFeed findByMetadataId(int metadataId) {
        return _repository.findByMetadataId(metadataId);
    }
}
