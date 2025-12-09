/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.processing.report;

import jakarta.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report for search and replace processing.
 * <p>
 * The report also advertised the list of changes for each records.
 */
public class MetadataReplacementProcessingReport extends XsltMetadataProcessingReport {

    /**
     * The list of record changes
     */
    protected Map<Integer, ReplaceReport> metadataChanges = new HashMap<>();

    public MetadataReplacementProcessingReport(String process) {
        super(process);
    }

    @XmlElement(name = "changes")
    public Map<Integer, ReplaceReport> getMetadataChanges() {
        return metadataChanges;
    }

    public synchronized void addMetadataChanges(int metadataId, List<org.jdom.Element> changes) {
        this.metadataChanges.put(metadataId, new ReplaceReport(changes));
    }

    public synchronized int getNumberOfRecordsChanged() {
        return this.metadataChanges.size();
    }

    public synchronized int getNumberOfRecordsNotChanged() {
        return this.metadataInfos.size();
    }
}
