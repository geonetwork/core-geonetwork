/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.*;

/**
 * A report related to metadata records.
 */
@XmlType(propOrder = {
    "uuid", "startIsoDateTime", "endIsoDateTime",
    "ellapsedTimeInSeconds", "totalTimeInSeconds", "infos", "errors"
})
public abstract class MetadataProcessingReport extends ProcessingReport {
    /**
     * The total number of records to process
     */
    protected int totalRecords = 0;
    /**
     * The number of records processed when the report was generated
     */
    @XmlAttribute
    protected int processedRecords = 0;
    /**
     * The number of records processed when the report was generated
     */
    @XmlAttribute
    protected int unchangedRecords = 0;
    /**
     * The number of records when a null metadata identifier is processed (may happen when a record
     * is in the selection but was deleted after the selection)
     */
    @XmlAttribute
    protected int nullRecords = 0;
    /**
     * The list of record identifiers successfully processed
     */
    protected Set<Integer> metadata = new HashSet<>();
    /**
     * The list of record identifiers not found (may be deleted)
     */
    protected Set<Integer> notFound = new HashSet<>();
    /**
     * The list of records identifiers the user who starts the process is not allowed to edit
     */
    protected Set<Integer> notEditable = new HashSet<>();

    /**
     * The list of records with error and exception details
     */
    protected Map<Integer, List<Report>> metadataErrors = new HashMap<>();
    protected Map<Integer, List<InfoReport>> metadataInfos = new HashMap<>();

    /**
     * The list of records with error and exception details
     */
    protected MetadataProcessingReport() {
        super();
    }

    @XmlElement(name = "errors")
    public Map<Integer, List<Report>> getMetadataErrors() {
        return metadataErrors;
    }

    public synchronized void addMetadataError(int metadataId, String metadataUUID, boolean draft, boolean approved,
                                              Exception error) {
        Report errorReport = new ErrorReport(error);
        errorReport.setUuid(metadataUUID);
        errorReport.setDraft(draft);
        errorReport.setApproved(approved);
        if (this.metadataErrors.get(metadataId) == null) {
            List<Report> errors = new ArrayList<>();
            errors.add(errorReport);
            this.metadataErrors.put(metadataId, errors);
        } else {
            this.metadataErrors.get(metadataId).add(errorReport);
        }
    }

    public void addMetadataError(AbstractMetadata metadata, Exception error) {
        addMetadataError(metadata.getId(), metadata.getUuid(), isMetadataDraft(metadata.getId()),
            isMetadataApproved(metadata.getId()), error);
    }

    public synchronized void addMetadataError(int metadataId, String metadataUUID, boolean draft, boolean approved,
                                              String error) {
        Report errorReport = new ErrorReport(error);
        errorReport.setUuid(metadataUUID);
        errorReport.setDraft(draft);
        errorReport.setApproved(approved);
        if (this.metadataErrors.get(metadataId) == null) {
            List<Report> errors = new ArrayList<>();
            errors.add(errorReport);
            this.metadataErrors.put(metadataId, errors);
        } else {
            this.metadataErrors.get(metadataId).add(errorReport);
        }
    }

    public void addMetadataError(AbstractMetadata metadata, String error) {
        addMetadataError(metadata.getId(), metadata.getUuid(), isMetadataDraft(metadata.getId()),
            isMetadataApproved(metadata.getId()), error);
    }

    @XmlElement(name = "infos")
    public Map<Integer, List<InfoReport>> getMetadataInfos() {
        return metadataInfos;
    }

    public void addMetadataInfos(int metadataId, String metadataUUID, boolean draft, boolean approved, String message) {
        InfoReport infoReport = new InfoReport(message);
        infoReport.setUuid(metadataUUID);
        infoReport.setDraft(draft);
        infoReport.setApproved(approved);
        if (this.metadataInfos.get(metadataId) == null) {
            List<InfoReport> infos = new ArrayList<>();
            infos.add(infoReport);
            this.metadataInfos.put(metadataId, infos);
        } else {
            this.metadataInfos.get(metadataId).add(infoReport);
        }
    }

    public void addMetadataInfos(AbstractMetadata metadata, String message) {
        addMetadataInfos(metadata.getId(), metadata.getUuid(), isMetadataDraft(metadata.getId()),
            isMetadataApproved(metadata.getId()), message);
    }

    private boolean isMetadataDraft(int metadataId) {
        boolean metadataDraft = false;
        try {
            metadataDraft = ApplicationContextHolder.get().getBean(IMetadataUtils.class).isMetadataDraft(metadataId);
        } catch (Exception e) {
            throw new RuntimeException("Error detecting if metadata is draft", e);
        }
        return metadataDraft;
    }

    private boolean isMetadataApproved(int metadataId) {
        boolean metadataApproved = false;
        try {
            metadataApproved = ApplicationContextHolder.get().getBean(IMetadataUtils.class).isMetadataApproved(metadataId);
        } catch (Exception e) {
            throw new RuntimeException("Error detecting if metadata is approved", e);
        }
        return metadataApproved;
    }

    @XmlTransient
    protected synchronized boolean isProcessing() {
        return totalRecords != processedRecords;
    }

    public synchronized int getNumberOfNullRecords() {
        return nullRecords;
    }

    public synchronized void incrementNullRecords() {
        this.nullRecords++;
    }

    @XmlAttribute
    public synchronized int getNumberOfRecords() {
        return totalRecords;
    }

    public synchronized void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public synchronized int getNumberOfRecordsProcessed() {
        return processedRecords;
    }

    public synchronized void incrementProcessedRecords() {
        this.processedRecords++;
    }

    public synchronized int getNumberOfRecordsUnchanged() {
        return unchangedRecords;
    }

    public synchronized void incrementUnchangedRecords() {
        this.unchangedRecords++;
    }

    public synchronized void addMetadataId(int metadataId) {
        this.metadata.add(metadataId);
    }

    public synchronized Set<Integer> getMetadata() {
        return this.metadata;
    }

    public synchronized void addNotFoundMetadataId(int metadataId) {
        this.notFound.add(metadataId);
    }

    public synchronized int getNumberOfRecordNotFound() {
        return this.notFound.size();
    }

    public synchronized void addNotEditableMetadataId(int metadataId) {
        this.notEditable.add(metadataId);
    }

    public synchronized int getNumberOfRecordsNotEditable() {
        return this.notEditable.size();
    }

    public synchronized int getNumberOfRecordsWithErrors() {
        return this.metadataErrors.size();
    }
}
