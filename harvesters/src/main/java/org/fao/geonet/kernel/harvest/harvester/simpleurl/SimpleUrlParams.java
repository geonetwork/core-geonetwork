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

package org.fao.geonet.kernel.harvest.harvester.simpleurl;

import org.fao.geonet.Util;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.jdom.Element;

import java.util.Locale;

public class SimpleUrlParams extends AbstractParams {
    public String url;
    public String icon;
    public String loopElement;
    public String numberOfRecordPath;
    public String recordIdPath;
    public SimpleUrlPathMode recordIdPathMode;
    public String pageSizeParam;
    public String pageFromParam;
    public String toISOConversion;

    public SimpleUrlParams(DataManager dm) {
        super(dm);
    }

    /**
     * called when a new entry must be added. Read values from the provided entry, providing
     * default values.
     */
    public void create(Element node) throws BadInputEx {
        super.create(node);

        Element site = node.getChild("site");

        url = Util.getParam(site, "url", "");
        loopElement = Util.getParam(site, "loopElement", "");
        numberOfRecordPath = Util.getParam(site, "numberOfRecordPath", "");
        recordIdPath = Util.getParam(site, "recordIdPath", "");
        var recordIdPathModeString = Util.getParam(site, "recordIdPathMode", SimpleUrlPathMode.AUTO.name());
        recordIdPathMode = SimpleUrlPathMode.valueOf(recordIdPathModeString.toUpperCase(Locale.ROOT));
        pageSizeParam = Util.getParam(site, "pageSizeParam", "");
        pageFromParam = Util.getParam(site, "pageFromParam", "");
        toISOConversion = Util.getParam(site, "toISOConversion", "");
        icon = Util.getParam(site, "icon", "");
    }

    /**
     * called when an entry has changed and variables must be updated.
     */
    public void update(Element node) throws BadInputEx {
        super.update(node);

        Element site = node.getChild("site");

        url = Util.getParam(site, "url", url);
        loopElement = Util.getParam(site, "loopElement", "");
        numberOfRecordPath = Util.getParam(site, "numberOfRecordPath", "");
        recordIdPath = Util.getParam(site, "recordIdPath", "");
        var recordIdPathModeString = Util.getParam(site, "recordIdPathMode", SimpleUrlPathMode.AUTO.name());
        recordIdPathMode = SimpleUrlPathMode.valueOf(recordIdPathModeString.toUpperCase(Locale.ROOT));
        pageSizeParam = Util.getParam(site, "pageSizeParam", "");
        pageFromParam = Util.getParam(site, "pageFromParam", "");
        toISOConversion = Util.getParam(site, "toISOConversion", "");
        icon = Util.getParam(site, "icon", icon);
    }

    @Override
    public String getIcon() {
        return icon;
    }

    public SimpleUrlParams copy() {
        SimpleUrlParams copy = new SimpleUrlParams(dm);
        copyTo(copy);

        copy.url = url;
        copy.icon = icon;
        copy.loopElement = loopElement;
        copy.numberOfRecordPath = numberOfRecordPath;
        copy.pageSizeParam = pageSizeParam;
        copy.pageFromParam = pageFromParam;
        copy.recordIdPath = recordIdPath;
        copy.recordIdPathMode = recordIdPathMode;
        copy.toISOConversion = toISOConversion;

        return copy;
    }
}
