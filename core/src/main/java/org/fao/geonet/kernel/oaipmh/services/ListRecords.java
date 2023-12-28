//=============================================================================
//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
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


import static org.fao.geonet.repository.specification.MetadataSpecs.hasMetadataId;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.oaipmh.ResumptionTokenCache;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.oaipmh.exceptions.CannotDisseminateFormatException;
import org.fao.oaipmh.exceptions.IdDoesNotExistException;
import org.fao.oaipmh.requests.ListRecordsRequest;
import org.fao.oaipmh.requests.TokenListRequest;
import org.fao.oaipmh.responses.ListRecordsResponse;
import org.fao.oaipmh.responses.Record;
import org.fao.oaipmh.util.SearchResult;
import org.springframework.data.jpa.domain.Specification;

import jeeves.server.context.ServiceContext;

//=============================================================================

public class ListRecords extends AbstractTokenLister {


    public ListRecords(ResumptionTokenCache cache, SettingManager sm, SchemaManager scm) {
        super(cache, sm, scm);
    }

    public String getVerb() {
        return ListRecordsRequest.VERB;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //---------------------------------------------------------------------------


    public ListRecordsResponse processRequest(TokenListRequest req, int pos, SearchResult result, ServiceContext context) throws Exception {

        int num = 0;
        ListRecordsResponse res = new ListRecordsResponse();

        //--- loop to retrieve metadata

        while (num < getMaxRecords() && pos < result.getIds().size()) {
            int id = result.getIds().get(pos);

            Record r = buildRecord(context, id, result.prefix);

            if (r != null) {
                res.addRecord(r);
                num++;
            }

            pos++;
        }

        return res;

    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    private Record buildRecord(ServiceContext context, int id, String prefix) throws Exception {

        // have to catch exceptions and return null because this function can
        // be called several times for a list of MD records
        // and we do not want to stop because of one error
        try {
            return GetRecord.buildRecordStat(context, (Specification<Metadata>)hasMetadataId(id), prefix);
        } catch (IdDoesNotExistException | CannotDisseminateFormatException e) {
            return null;
        } catch (Exception e3) {
            throw e3;
        }
    }
}

//=============================================================================

