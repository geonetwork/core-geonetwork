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

import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to model the massive replace report.
 *
 * @author Jose García
 *
 */
public class MassiveReplaceReport {
    private List<MassiveReplaceReportEntry> notFound;
    private List<MassiveReplaceReportEntry> notOwner;
    private List<MassiveReplaceReportEntry> notValidStatus;
    private List<MassiveReplaceReportEntry> changed;
    private List<MassiveReplaceReportEntry> notChanged;

    private List<String> errors;
    private List<String> warnings;

    public List<MassiveReplaceReportEntry> getNotFound() {
        return notFound;
    }

    public List<MassiveReplaceReportEntry> getNotOwner() {
        return notOwner;
    }

    public List<MassiveReplaceReportEntry> getChanged() {
        return changed;
    }

    public List<MassiveReplaceReportEntry> getNotChanged() {
        return notChanged;
    }

    public MassiveReplaceReport() {
        notFound = new ArrayList<MassiveReplaceReportEntry>();
        notOwner = new ArrayList<MassiveReplaceReportEntry>();
        notValidStatus = new ArrayList<MassiveReplaceReportEntry>();
        changed = new ArrayList<MassiveReplaceReportEntry>();
        notChanged = new ArrayList<MassiveReplaceReportEntry>();
        errors = new ArrayList<String>();
        warnings = new ArrayList<String>();
    }

    public void addNotFound(MassiveReplaceReportEntry entry) {
        notFound.add(entry);
    }

    public void addNotOwner(MassiveReplaceReportEntry entry) {
        notOwner.add(entry);
    }

    public void addNotValidStatus(MassiveReplaceReportEntry entry) {
        notValidStatus.add(entry);
    }


    public void addChanged(MassiveReplaceReportEntry entry) {
        changed.add(entry);
    }

    public void addNotChanged(MassiveReplaceReportEntry entry) {
        notChanged.add(entry);
    }

    public void addError(String error) {
        errors.add(error);
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }

    public int getTotalRecords() {
        return notFound.size() + notOwner.size() + notValidStatus.size() + changed.size() +  notChanged.size();
    }


    public Element toXml() {
        Element report = new Element("report");
        report.setAttribute("total", getTotalRecords() + "");

        Element changedEl = new Element("changed");
        int i = 1;
        for(MassiveReplaceReportEntry mdInfo: this.changed) {
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
            i++;
        }
        report.addContent(changedEl);

        Element notOwnerEl = new Element("notOwner");
        i = 1;
        for(MassiveReplaceReportEntry mdInfo: this.notOwner) {
            Element metadataEl = new Element("metadata");
            metadataEl.setAttribute("uuid", mdInfo.getMetadataUuid());
            metadataEl.setAttribute("title", mdInfo.getMetadataTitle());

            notOwnerEl.addContent(metadataEl);
            i++;
        }
        report.addContent(notOwnerEl);

        Element notFoundEl = new Element("notFound");
        i = 1;
        for(MassiveReplaceReportEntry mdInfo: this.notFound) {
            Element metadataEl = new Element("metadata");
            metadataEl.setAttribute("uuid", mdInfo.getMetadataUuid());
            metadataEl.setAttribute("title", mdInfo.getMetadataTitle());

            notFoundEl.addContent(metadataEl);
            i++;
        }
        report.addContent(notFoundEl);

        Element notChangedEl = new Element("notChanged");
        i = 1;
        for(MassiveReplaceReportEntry mdInfo: this.notChanged) {
            Element metadataEl = new Element("metadata");
            metadataEl.setAttribute("uuid",  mdInfo.getMetadataUuid());
            metadataEl.setAttribute("title",  mdInfo.getMetadataTitle());

            notChangedEl.addContent(metadataEl);
            i++;
        }
        report.addContent(notChangedEl);

        Element errorsEl = new Element("errors");

        for(String error: this.errors) {
            Element errorEl = new Element("error");
            errorEl.setText(error);

            errorsEl.addContent(errorEl);
        }
        report.addContent(errorsEl);

        Element warningsEl = new Element("warnings");

        for(String error: this.warnings) {
            Element warningEl = new Element("warning");
            warningEl.setText(error);

            warningsEl.addContent(warningEl);
        }
        report.addContent(warningsEl);

        return report;
    }
}
