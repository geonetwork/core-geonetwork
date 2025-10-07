//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.geonet.v4.client;

/**
 * Represents an individual result from a search operation. This class encapsulates
 * metadata about a specific search hit, including its unique identifier, associated
 * schema, the date of the last change, and the source of the record.
 */
public class SearchResponseHit {
    private final String uuid;
    private final String schema;
    private final String changeDate;
    private final String source;

    public String getUuid() {
        return uuid;
    }

    public String getSchema() {
        return schema;
    }

    public String getChangeDate() {
        return changeDate;
    }

    public String getSource() {
        return source;
    }

    /**
     * Constructs a new instance of SearchResponseHit, representing an individual result
     * from a search operation, and encapsulates metadata about the search hit.
     *
     * @param uuid       the unique identifier of the search hit
     * @param schema     the schema associated with the search hit
     * @param changeDate the last modification date of the search hit
     * @param source     the source of the search hit
     */
    public SearchResponseHit(String uuid, String schema, String changeDate, String source) {
        this.uuid = uuid;
        this.schema = schema;
        this.changeDate = changeDate;
        this.source = source;
    }
}
