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

    public String queryScope;

    public Integer hopCount;


    public boolean remoteHarvesterNestedServices;

    /**
     * The filter is a process (see schema/process folder) which depends on the schema. It could be
     * composed of parameter which will be sent to XSL transformation using the following syntax :
     * <pre>
     * anonymizer?protocol=MYLOCALNETWORK:FILEPATH&email=gis@organisation.org&thesaurus=MYORGONLYTHEASURUS
     * </pre>
     */
    public String xslfilter;

    public List<Element> eltFilters = new ArrayList<Element>();

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
        queryScope = Util.getParam(site, "queryScope", "local");
        hopCount = Util.getParam(site, "hopCount", 2);
        xslfilter = Util.getParam(site, "xslfilter", "");
        outputSchema = Util.getParam(site, "outputSchema", outputSchema);
        icon = Util.getParam(site, "icon", "default.gif");
        remoteHarvesterNestedServices = Util.getParam(options, "remoteHarvesterNestedServices", false);

        if (filters != null) {
            @SuppressWarnings("unchecked")
            List<Element> tmp = filters.getChildren();
            eltFilters = tmp;
        } else {
            eltFilters = new ArrayList<Element>();
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
        queryScope = Util.getParam(site, "queryScope", queryScope);
        hopCount = Util.getParam(site, "hopCount", hopCount);
        xslfilter = Util.getParam(site, "xslfilter", "");
        outputSchema = Util.getParam(site, "outputSchema", outputSchema);
        remoteHarvesterNestedServices = Util.getParam(options, "remoteHarvesterNestedServices", remoteHarvesterNestedServices);

        icon = Util.getParam(site, "icon", icon);

        //--- if some filter queries are given, we drop the previous ones and
        //--- set these new ones
        if (filters != null) {
            @SuppressWarnings("unchecked")
            List<Element> tmp = filters.getChildren();
            eltFilters = tmp;
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
        copy.queryScope = queryScope;
        copy.hopCount = hopCount;
        copy.xslfilter = xslfilter;
        copy.outputSchema = outputSchema;
        copy.remoteHarvesterNestedServices = remoteHarvesterNestedServices;

        copy.eltFilters = eltFilters;
        copy.bboxFilter = bboxFilter;

        return copy;
    }

}
