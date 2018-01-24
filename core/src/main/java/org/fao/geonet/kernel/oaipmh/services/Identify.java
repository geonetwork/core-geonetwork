//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.oaipmh.services;

import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.kernel.oaipmh.OaiPmhService;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.oaipmh.requests.AbstractRequest;
import org.fao.oaipmh.requests.IdentifyRequest;
import org.fao.oaipmh.responses.AbstractResponse;
import org.fao.oaipmh.responses.IdentifyResponse;
import org.fao.oaipmh.responses.IdentifyResponse.DeletedRecord;
import org.fao.oaipmh.responses.IdentifyResponse.Granularity;

import jeeves.constants.Jeeves;
import jeeves.server.context.ServiceContext;

//=============================================================================

public class Identify implements OaiPmhService {
    public String getVerb() {
        return IdentifyRequest.VERB;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //---------------------------------------------------------------------------

    public AbstractResponse execute(AbstractRequest request, ServiceContext context) throws Exception {
        IdentifyResponse res = new IdentifyResponse();
        SettingInfo si = context.getBean(SettingInfo.class);

        String baseUrl = si.getSiteUrl() + context.getBaseUrl() + "/" + Jeeves.Prefix.SERVICE + "/en/" + context.getService();

        res.setRepositoryName(si.getSiteName());
        res.setBaseUrl(baseUrl);
        res.setEarliestDateStamp(getEarliestDS(context));
        res.setDeletedRecord(DeletedRecord.NO);
        res.setGranularity(Granularity.LONG);
        res.addAdminEmail(si.getFeedbackEmail());

        return res;
    }

    //---------------------------------------------------------------------------

    private ISODate getEarliestDS(ServiceContext context) throws Exception {
        final AbstractMetadata oldestByChangeDate = context.getBean(MetadataRepository.class).findOneOldestByChangeDate();

        //--- if we don't have metadata, just return 'now'
        if (oldestByChangeDate == null)
            return new ISODate();

        return oldestByChangeDate.getDataInfo().getChangeDate();
    }
}

//=============================================================================

