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
 * Class to model a metadata entry in the massive replace report.
 *
 * @author Jose García
 *
 */
public class MassiveReplaceReportEntry {
    private String uuid;
    private String title;

    private List<MassiveReplaceReportEntryChange> elementChanges;

    public String getMetadataUuid() {
        return uuid;
    }

    public String getMetadataTitle() {
        return title;
    }


    public List<MassiveReplaceReportEntryChange> getElementChanges() {
        return elementChanges;
    }

    public MassiveReplaceReportEntry(String uuid, String title, List<Element> changes) {
        this.uuid = uuid;
        this.title = title;

        elementChanges = new ArrayList<MassiveReplaceReportEntryChange>();

        if (changes != null) {
            for (Element change : changes) {
                MassiveReplaceReportEntryChange elementChange =
                        new MassiveReplaceReportEntryChange(change.getChildText("fieldid"),
                                change.getChildText("originalval"), change.getChildText("changedval"));

                elementChanges.add(elementChange);
            }
        }
    }
}
