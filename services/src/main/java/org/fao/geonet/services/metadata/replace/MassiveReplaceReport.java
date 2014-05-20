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
package org.fao.geonet.services.metadata.replace;

import org.fao.geonet.Util;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.services.metadata.XslProcessingReport;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class to model the massive replace report.
 *
 * @author Jose García
 *
 */
public class MassiveReplaceReport extends XslProcessingReport {

    private List<MassiveReplaceReportEntry> notFoundList = new ArrayList<MassiveReplaceReportEntry>();
    private List<MassiveReplaceReportEntry> notEditableList = new ArrayList<MassiveReplaceReportEntry>();
    private List<MassiveReplaceReportEntry> changedList = new ArrayList<MassiveReplaceReportEntry>();
    private List<MassiveReplaceReportEntry> notChangedList = new ArrayList<MassiveReplaceReportEntry>();
    private List<MassiveReplaceReportEntry> noProcessFoundList = new ArrayList<MassiveReplaceReportEntry>();

    public MassiveReplaceReport(String processId) {
        super(processId);
    }

    public void addNotFound(MassiveReplaceReportEntry entry) {
        notFoundList.add(entry);
    }

    public void addNotEditable(MassiveReplaceReportEntry entry) {
        notEditableList.add(entry);
    }

    public void addChanged(MassiveReplaceReportEntry entry) {
        changedList.add(entry);
    }

    public void addNotChanged(MassiveReplaceReportEntry entry) {
        notChangedList.add(entry);
    }

    public void addNoProcessFound(MassiveReplaceReportEntry entry) {
        noProcessFoundList.add(entry);
    }

    @Override
    public synchronized int getNotFoundMetadataCount() {
        return this.notFoundList.size();
    }

    @Override
    public synchronized int getNotEditableMetadataCount() {
        return this.notEditableList.size();
    }

    /*public int getTotalRecords() {
        return notFound.size() + notOwner.size() + notValidStatus.size() + changed.size() +  notChanged.size();
    }*/


    @Override
    public synchronized Element toXml() {
        Element xmlReport = new Element("report");
        xmlReport.setAttribute("total", getTotalRecords() + "");

        xmlReport.setAttribute("startDate", startDate.toString());
        xmlReport.setAttribute("reportDate", new ISODate().toString());
        xmlReport.setAttribute("running", String.valueOf(isProcessing()));
        xmlReport.setAttribute("totalRecords", totalRecords + "");
        xmlReport.setAttribute("processedRecords", processedRecords + "");
        xmlReport.setAttribute("nullRecords", nullRecords + "");

        // Changed metadata
        Element changedEl = new Element("changed");
        for(MassiveReplaceReportEntry mdInfo: this.changedList) {
            Element metadataEl = new Element("metadata");
            metadataEl.setAttribute("uuid",  mdInfo.getMetadataUuid());
            metadataEl.setAttribute("title",  mdInfo.getMetadataTitle());

            for (MassiveReplaceReportEntryChange change : mdInfo.getElementChanges()) {
                Element changeMetadataEl = new Element("change");
                changeMetadataEl.addContent(new Element("fieldid").setText(change.getFieldId()));
                changeMetadataEl.addContent(new Element("originalval").setText(change.getOriginalVal()));
                changeMetadataEl.addContent(new Element("changedval").setText(change.getChangedVal()));

                metadataEl.addContent(changeMetadataEl);
            }

            changedEl.addContent(metadataEl);
        }
        xmlReport.addContent(changedEl);

        // Not editable metadata
        Element notOwnerEl = new Element("notOwner");
        for(MassiveReplaceReportEntry mdInfo: this.notEditableList) {
            Element metadataEl = new Element("metadata");
            metadataEl.setAttribute("uuid", mdInfo.getMetadataUuid());
            metadataEl.setAttribute("title", mdInfo.getMetadataTitle());

            notOwnerEl.addContent(metadataEl);
        }
        xmlReport.addContent(notOwnerEl);

        // Not found metadata
        Element notFoundEl = new Element("notFound");
        for(MassiveReplaceReportEntry mdInfo: this.notFoundList) {
            Element metadataEl = new Element("metadata");
            metadataEl.setAttribute("uuid", mdInfo.getMetadataUuid());
            metadataEl.setAttribute("title", mdInfo.getMetadataTitle());

            notFoundEl.addContent(metadataEl);
        }
        xmlReport.addContent(notFoundEl);

        // Not changed metadata
        Element notChangedEl = new Element("notChanged");
        for(MassiveReplaceReportEntry mdInfo: this.notChangedList) {
            Element metadataEl = new Element("metadata");
            metadataEl.setAttribute("uuid",  mdInfo.getMetadataUuid());
            metadataEl.setAttribute("title",  mdInfo.getMetadataTitle());

            notChangedEl.addContent(metadataEl);
        }
        xmlReport.addContent(notChangedEl);

        // No process found for the metadata
        Element notProcessFoundEl = new Element("noProcessFound");
        for(MassiveReplaceReportEntry mdInfo: this.noProcessFoundList) {
            Element metadataEl = new Element("metadata");
            metadataEl.setAttribute("uuid",  mdInfo.getMetadataUuid());
            metadataEl.setAttribute("title",  mdInfo.getMetadataTitle());

            notProcessFoundEl.addContent(metadataEl);
        }
        xmlReport.addContent(notProcessFoundEl);

        // Errors
        Element mdErrorReport = new Element("metadataErrorReport");
        for (Map.Entry<Integer, Exception> e : metadataErrors.entrySet()) {
            Element info = new Element("metadata");
            info.setAttribute("id", e.getKey() + "");
            info.addContent(new Element("message").setText(e.getValue().getMessage()));
            info.addContent(new Element("stack").setText(Util.getStackTrace(e.getValue())));
            mdErrorReport.addContent(info);
        }
        xmlReport.addContent(mdErrorReport);

        return xmlReport;
    }
}
