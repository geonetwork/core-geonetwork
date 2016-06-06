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

package org.fao.geonet.kernel.harvest.harvester.oaipmh;

import org.fao.geonet.domain.ISODate;
import org.fao.oaipmh.responses.Header;

//=============================================================================

public class RecordInfo {
    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    public String id;

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------
    public ISODate changeDate;

    //---------------------------------------------------------------------------
    public String prefix;

    //---------------------------------------------------------------------------

    public RecordInfo(Header h, String mdPrefix) {
        id = h.getIdentifier();
        changeDate = h.getDateStamp();
        prefix = mdPrefix;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    public int hashCode() {
        return id.hashCode();
    }

    public boolean isMoreRecentThan(String localChangeDate) {
        ISODate localDate = new ISODate(localChangeDate);

        //--- accept if remote date is greater than local date

        return (changeDate.timeDifferenceInSeconds(localDate) > 0);
    }

    public boolean equals(Object o) {
        if (o instanceof RecordInfo) {
            RecordInfo ri = (RecordInfo) o;

            return id.equals(ri.id);
        }

        return false;
    }

}

//=============================================================================

