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

import jeeves.server.context.ServiceContext;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.csw.common.util.Xml;
import org.fao.geonet.domain.InspireAtomFeed;
import org.fao.geonet.inspireatom.util.InspireAtomUtil;
import org.fao.geonet.repository.InspireAtomFeedRepository;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Component
@Transactional
public class InspireAtomService {

    @Autowired
    private InspireAtomFeedRepository _repository;

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

    public String retrieveDatasetUuidFromIdentifierNs(String datasetIdCode, String datasetIdNs) {
        return _repository.retrieveDatasetUuidFromIdentifierNs(datasetIdCode, datasetIdNs);
    }

    public InspireAtomFeed findByMetadataId(int metadataId) {
        return _repository.findByMetadataId(metadataId);
    }
}
