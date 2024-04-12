//=============================================================================
//===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.specification.MetadataSpecs;

import jeeves.server.context.ServiceContext;

//=============================================================================

/**
 * Create a mapping (remote URI) -> (local ID / change date). Retrieves all metadata of a given
 * harvest uuid and puts them into an hashmap.
 */

public class UriMapper {
    private HashMap<String, List<RecordInfo>> hmUriRecords = new HashMap<>();

    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------

    public UriMapper(ServiceContext context, String harvestUuid) {
        final IMetadataUtils metadataRepository = context.getBean(IMetadataUtils.class);
        final List<? extends AbstractMetadata> metadataList = metadataRepository.findAll(MetadataSpecs.hasHarvesterUuid(harvestUuid));

        for (AbstractMetadata metadataRecord : metadataList) {
            String uri = Optional.ofNullable(metadataRecord.getHarvestInfo().getUri()).orElse("");

            List<RecordInfo> records = hmUriRecords.computeIfAbsent(uri, k -> new ArrayList<>());

            if (records == null) {
                records = new ArrayList<>();
                hmUriRecords.put(uri, records);
            }

            records.add(new RecordInfo(metadataRecord));
        }
    }

    //--------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //--------------------------------------------------------------------------

    public List<RecordInfo> getRecords(String uri) {
        return hmUriRecords.get(uri);
    }

    //--------------------------------------------------------------------------

    public Iterable<String> getUris() {
        return hmUriRecords.keySet();
    }

}

//=============================================================================

