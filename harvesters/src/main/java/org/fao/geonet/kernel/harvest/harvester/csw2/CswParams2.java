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

package org.fao.geonet.kernel.harvest.harvester.csw2;

import org.fao.geonet.Util;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CswParams2 extends AbstractParams {

    public String capabUrl;

    public String icon;

    public String outputSchema;

    public boolean remoteHarvesterNestedServices;

    public int numberOfRecordsPerRequest;

    public boolean errorConfigNextRecordsNotZero = false;
    public boolean errorConfigNextRecordsBadValue = true;
    public boolean errorConfigFewerRecordsThanRequested = true;
    public boolean errorConfigTotalRecordsChanged = true;
    public int errorConfigMaxPercentTotalRecordsChangedAllowed = 1;
    public boolean errorConfigDuplicatedUuids = true;

    public boolean doNotSort = false;

    public String processQueueType = "auto";

    public boolean executeLinkChecker = true;

    public boolean skipHarvesting = false;

    public List<Element> eltFilters = new ArrayList<Element>();

    public String rawFilter = "";

    public Element bboxFilter;

    @Override
    public String getIcon() {
        return icon;
    }

    public CswParams2(DataManager dm) {
        super(dm);
    }

    /**
     * called when a new entry must be added. Reads values from the provided entry, providing
     * default values.
     */
    public void create(Element node) throws BadInputEx {
        super.create(node);

        Element site = node.getChild("site");
        Element filters = node.getChild("filters");
        Element options = node.getChild("options");

        capabUrl = Util.getParam(site, "capabilitiesUrl", "");
        outputSchema = Util.getParam(site, "outputSchema", outputSchema);
        icon = Util.getParam(site, "icon", "default.gif");
        remoteHarvesterNestedServices = Util.getParam(options, "remoteHarvesterNestedServices", false);
        numberOfRecordsPerRequest = Util.getParam(options, "numberOfRecordsPerRequest", 20);

        errorConfigNextRecordsNotZero = Util.getParam(options, "errorConfigNextRecordsNotZero", true);
        errorConfigNextRecordsBadValue = Util.getParam(options, "errorConfigNextRecordsBadValue", true);
        errorConfigFewerRecordsThanRequested = Util.getParam(options, "errorConfigFewerRecordsThanRequested", true);
        errorConfigTotalRecordsChanged = Util.getParam(options, "errorConfigTotalRecordsChanged", true);
        errorConfigMaxPercentTotalRecordsChangedAllowed = Util.getParam(options, "errorConfigMaxPercentTotalRecordsChangedAllowed", 5);
        errorConfigDuplicatedUuids = Util.getParam(options, "errorConfigDuplicatedUuids", true);

        processQueueType = Util.getParam(options, "processQueueType", "auto");

        doNotSort = Util.getParam(options, "doNotSort", false);

        executeLinkChecker = Util.getParam(options, "executeLinkChecker", true);
        skipHarvesting = Util.getParam(options, "skipHarvesting", false);

        if (filters != null) {
            @SuppressWarnings("unchecked")
            List<Element> tmp = filters.getChildren();
            eltFilters = tmp;
        } else {
            eltFilters = new ArrayList<Element>();
        }

        if ((node.getChild("rawFilter") != null) &&
            (node.getChild("rawFilter").getChildren().size() > 0)) {
            rawFilter = Xml.getString((Element) node.getChild("rawFilter").getChildren().get(0));
        } else {
            rawFilter = "";
        }

        bboxFilter = node.getChild("bboxFilter");
    }

    /**
     * called when an entry has changed and variables must be updated.
     */
    public void update(Element node) throws BadInputEx {
        super.update(node);

        Element site = node.getChild("site");
        Element filters = node.getChild("filters");
        Element options = node.getChild("options");

        capabUrl = Util.getParam(site, "capabilitiesUrl", capabUrl);
        outputSchema = Util.getParam(site, "outputSchema", outputSchema);
        remoteHarvesterNestedServices = Util.getParam(options, "remoteHarvesterNestedServices", remoteHarvesterNestedServices);
        numberOfRecordsPerRequest = Util.getParam(options, "numberOfRecordsPerRequest", numberOfRecordsPerRequest);

        errorConfigNextRecordsNotZero = Util.getParam(options, "errorConfigNextRecordsNotZero", errorConfigNextRecordsNotZero);
        errorConfigNextRecordsBadValue = Util.getParam(options, "errorConfigNextRecordsBadValue", errorConfigNextRecordsBadValue);
        errorConfigFewerRecordsThanRequested = Util.getParam(options, "errorConfigFewerRecordsThanRequested", errorConfigFewerRecordsThanRequested);
        errorConfigTotalRecordsChanged = Util.getParam(options, "errorConfigTotalRecordsChanged", errorConfigTotalRecordsChanged);
        errorConfigMaxPercentTotalRecordsChangedAllowed = Util.getParam(options, "errorConfigMaxPercentTotalRecordsChangedAllowed", errorConfigMaxPercentTotalRecordsChangedAllowed);
        errorConfigDuplicatedUuids = Util.getParam(options, "errorConfigDuplicatedUuids", errorConfigDuplicatedUuids);

        processQueueType = Util.getParam(options, "processQueueType", processQueueType);

        doNotSort = Util.getParam(options, "doNotSort", doNotSort);

        executeLinkChecker = Util.getParam(options, "executeLinkChecker", executeLinkChecker);
        skipHarvesting = Util.getParam(options, "skipHarvesting", skipHarvesting);

        icon = Util.getParam(site, "icon", icon);

        //--- if some filter queries are given, we drop the previous ones and
        //--- set these new ones
        if (filters != null) {
            @SuppressWarnings("unchecked")
            List<Element> tmp = filters.getChildren();
            eltFilters = tmp;
        }

        if ((node.getChild("rawFilter") != null) &&
            (node.getChild("rawFilter").getChildren().size() > 0)) {
            rawFilter = Xml.getString((Element) node.getChild("rawFilter").getChildren().get(0));
        } else {
            rawFilter = "";
        }

        bboxFilter = node.getChild("bboxFilter");
    }

    /**
     *
     * @return
     */
    public CswParams2 copy() {
        CswParams2 copy = new CswParams2(dm);
        copyTo(copy);

        copy.capabUrl = capabUrl;
        copy.icon = icon;
        copy.outputSchema = outputSchema;
        copy.remoteHarvesterNestedServices = remoteHarvesterNestedServices;
        copy.numberOfRecordsPerRequest = numberOfRecordsPerRequest;

        copy.errorConfigNextRecordsNotZero = errorConfigNextRecordsNotZero;
        copy.errorConfigNextRecordsBadValue = errorConfigNextRecordsBadValue;
        copy.errorConfigFewerRecordsThanRequested = errorConfigFewerRecordsThanRequested;
        copy.errorConfigTotalRecordsChanged = errorConfigTotalRecordsChanged;
        copy.errorConfigMaxPercentTotalRecordsChangedAllowed = errorConfigMaxPercentTotalRecordsChangedAllowed;
        copy.errorConfigDuplicatedUuids = errorConfigDuplicatedUuids;

        copy.processQueueType = processQueueType;
        copy.doNotSort = doNotSort;
        copy.executeLinkChecker = executeLinkChecker;
        copy.skipHarvesting = skipHarvesting;

        copy.eltFilters = eltFilters;
        copy.rawFilter = rawFilter;
        copy.bboxFilter = bboxFilter;

        return copy;
    }

}
