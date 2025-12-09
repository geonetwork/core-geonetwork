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

import jakarta.xml.bind.annotation.XmlAttribute;
import java.util.HashSet;
import java.util.Set;

/**
 * A report about XSLT processing.
 * <p>
 * This report also advertised the XSL used (based on process identifier), and the records in a
 * standard not providing the process.
 */
public class XsltMetadataProcessingReport extends MetadataProcessingReport {

    protected final String processId;
    /**
     * The list of record identifiers for which the schema does not provide process with that
     * process id
     */
    protected Set<Integer> noProcessFound = new HashSet<Integer>();

    public XsltMetadataProcessingReport(String processId) {
        super();
        this.processId = processId;
    }

    @XmlAttribute(name = "process")
    public String getProcessId() {
        return processId;
    }

    public synchronized void addNoProcessFoundMetadataId(int metadataId) {
        this.noProcessFound.add(metadataId);
    }

    public synchronized int getNoProcessFoundCount() {
        return this.noProcessFound.size();
    }

}
