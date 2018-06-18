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

package org.fao.geonet.kernel.harvest.harvester;

import java.util.HashMap;
import java.util.List;

import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.SimpleMetadata;

//=============================================================================

/**
 * Create a mapping remote ID -> local ID / change date. Retrieves all metadata of a given siteID
 * and puts them into an hashmap.
 */

public class UUIDMapper {
    private HashMap<String, String> hmUuidDate = new HashMap<String, String>();
    private HashMap<String, String> hmUuidId = new HashMap<String, String>();
    private HashMap<String, String> hmUuidTemplate = new HashMap<String, String>();

    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------

    public UUIDMapper(IMetadataUtils repo, String harvestUuid) throws Exception {

        final List<SimpleMetadata> all = repo.findAllSimple(harvestUuid);

        //This may lead to problems if we have millions of records from the same harvester...
        //If that happens, we may have to look for something that uses even less memory
        for (SimpleMetadata record : all) {
            hmUuidDate.put(record.getUuid(), record.getDate());
            hmUuidId.put(record.getUuid(), record.getId());
            hmUuidTemplate.put(record.getUuid(), record.getIsTemplate());
        }
    }

    //--------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //--------------------------------------------------------------------------

    public String getTemplate(String uuid) {
        return hmUuidTemplate.get(uuid);
    }

    //--------------------------------------------------------------------------

    public String getChangeDate(String uuid) {
        return hmUuidDate.get(uuid);
    }

    //--------------------------------------------------------------------------

    public String getID(String uuid) {
        return hmUuidId.get(uuid);
    }

    //--------------------------------------------------------------------------

    public Iterable<String> getUUIDs() {
        return hmUuidDate.keySet();
    }
}

//=============================================================================

