//=============================================================================
//===  Copyright (C) 2001-2013 Food and Agriculture Organization of the
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
 
package org.fao.geonet.services.metadata;
 
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
 
import jeeves.constants.Jeeves;
import org.fao.geonet.Util;

import org.fao.geonet.domain.ISODate;
import org.jdom.Element;
 
/**
 * Batch processing report containing information about the current process and the metadata records affected.
 * 
 * @author francois
 * 
 */
public class XslProcessingReport {
 
    protected final String processId;
    protected final ISODate startDate;
 
    /**
     * The total number of records to process
     */
    protected int totalRecords = 0;
    /**
     * The number of records processed when the report was generated
     */
    protected int processedRecords = 0;
    /**
     * The number of records when a null metadata identifier is processed (may happen when a record is in the selection but was deleted
     * after the selection)
     */
    protected int nullRecords = 0;
 
    /**
     * The list of record identifiers successfully processed
     */
    protected Set<Integer> metadata = new HashSet<Integer>();
 
    /**
     * The list of record identifiers not found (may be deleted)
     */
    protected Set<Integer> notFound = new HashSet<Integer>();
 
    /**
     * The list of records identifiers the user who starts the process is not allowed to edit
     */
    protected Set<Integer> notEditable = new HashSet<Integer>();
 
    /**
     * The list of record identifiers for which the schema does not provide process with that process id
     */
    protected Set<Integer> noProcessFound = new HashSet<Integer>();
    /**
     * The list of records with error and exception details
     */
    protected Map<Integer, Exception> metadataErrors = new HashMap<Integer, Exception>();
 
    /**
     * Initialize a report and its start date.
     * 
     * @param processId
     */
    public XslProcessingReport(String processId) {
        this.processId = processId;
        this.startDate = new ISODate();
    }

    protected synchronized boolean isProcessing() {
        return totalRecords != processedRecords;
    }
 
    /**
     * Return report as XML
     * 
     * @return
     */
    public synchronized Element toXml() {
        Element xmlReport = new Element(Jeeves.Elem.RESPONSE);
 
        xmlReport.setAttribute("process", processId);
 
        xmlReport.addContent(new Element("done").setText(metadata.size() + ""));
        xmlReport.addContent(new Element("notProcessFound").setText(noProcessFound.size() + ""));
        xmlReport.addContent(new Element("notOwner").setText(notEditable.size() + ""));
        xmlReport.addContent(new Element("notFound").setText(notFound.size() + ""));
 
        xmlReport.setAttribute("startDate", startDate.toString());
        xmlReport.setAttribute("reportDate", new ISODate().toString());
        xmlReport.setAttribute("running", String.valueOf(isProcessing()));
        xmlReport.setAttribute("totalRecords", totalRecords + "");
        xmlReport.setAttribute("processedRecords", processedRecords + "");
        xmlReport.setAttribute("nullRecords", nullRecords + "");
 
        Element mdErrorReport = new Element("metadataErrorReport");
        for (Entry<Integer, Exception> e : metadataErrors.entrySet()) {
            Element info = new Element("metadata");
            info.setAttribute("id", e.getKey() + "");
            info.addContent(new Element("message").setText(e.getValue().getMessage()));
            info.addContent(new Element("stack").setText(Util.getStackTrace(e.getValue())));
            mdErrorReport.addContent(info);
        }
        xmlReport.addContent(mdErrorReport);
 
        return xmlReport;
    }
 
    public synchronized int getNullRecords() {
        return nullRecords;
    }
 
    public synchronized void incrementNullRecords() {
        this.nullRecords ++;
    }
 
    public synchronized String getProcessId() {
        return processId;
    }
 
    public synchronized ISODate getStartDate() {
        return startDate.clone();
    }
 
    public synchronized int getTotalRecords() {
        return totalRecords;
    }
 
    public synchronized void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }
 
    public synchronized int getProcessedRecords() {
        return processedRecords;
    }
 
    public synchronized void incrementProcessedRecords() {
        this.processedRecords ++;
    }
 
    public synchronized void addMetadataId(int metadataId) {
        this.metadata.add(metadataId);
    }
 
    public synchronized void addNotFoundMetadataId(int metadataId) {
        this.notFound.add(metadataId);
    }
    
    public synchronized int getNotFoundMetadataCount() {
        return this.notFound.size();
    }
    
    public synchronized void addNotEditableMetadataId(int metadataId) {
        this.notEditable.add(metadataId);
    }
    
    public synchronized int getNotEditableMetadataCount() {
        return this.notEditable.size();
    }
    
    public synchronized void addNoProcessFoundMetadataId(int metadataId) {
        this.noProcessFound.add(metadataId);
    }

    public synchronized int getNoProcessFoundCount() {
        return this.noProcessFound.size();
    }
    
    public synchronized void addMetadataError(int metadataId, Exception error) {
        this.metadataErrors.put(metadataId, error);
    }
    
}