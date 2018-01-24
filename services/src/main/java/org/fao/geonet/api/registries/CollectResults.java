//=============================================================================
//===	Copyright (C) 2001-2016 Food and Agriculture Organization of the
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
package org.fao.geonet.api.registries;

import java.util.HashMap;
import java.util.Map;

import org.fao.geonet.domain.AbstractMetadata;
import org.jdom.Element;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;


/**
 * List of entries collected in a metadata record
 */
public class CollectResults {
    boolean recordUpdated = false;
    private AbstractMetadata record;
    private Element updatedRecord;
    /**
     * Table composed of the entry identifier (extracted from the Element),
     * the UUID and the XML element.
     */
    private Table<String, String, Element> entries =
        HashBasedTable.create();
    /**
     * Map of UUID and db identifiers if the entry is saved in the database
     */
    private Map<String, Integer> entryIdentifiers = new HashMap<>();

    public CollectResults() {
    }

    public CollectResults(AbstractMetadata record) {
        this.record = record;
    }

    public boolean isRecordUpdated() {
        return recordUpdated;
    }

    public AbstractMetadata getRecord() {
        return record;
    }

    public CollectResults setRecord(AbstractMetadata record) {
        this.record = record;
        return this;
    }

    public Table<String, String, Element> getEntries() {
        return entries;
    }

    public Map<String, Integer> getEntryIdentifiers() {
        return entryIdentifiers;
    }

    public CollectResults setEntryIdentifiers(Map<String, Integer> entryIdentifiers) {
        this.entryIdentifiers = entryIdentifiers;
        return this;
    }

    public Element getUpdatedRecord() {
        return updatedRecord;
    }

    public CollectResults setUpdatedRecord(Element updatedRecord) {
        this.updatedRecord = updatedRecord;
        this.recordUpdated = updatedRecord != null;
        return this;
    }
}
