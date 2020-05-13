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

package org.fao.geonet.kernel.harvest.harvester.csw;

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
public class CswParams extends AbstractParams {

    public String capabUrl;

    public String icon;

    public String outputSchema;

    public boolean rejectDuplicateResource;

    public String queryScope;

    public String xpathFilter;

    public Integer hopCount;

    /**
     * The filter is a process (see schema/process folder) which depends on the schema. It could be
     * composed of parameter which will be sent to XSL transformation using the following syntax :
     * <pre>
     * anonymizer?protocol=MYLOCALNETWORK:FILEPATH&email=gis@organisation.org&thesaurus=MYORGONLYTHEASURUS
     * </pre>
     */
    public String xslfilter;
    public List<Element> eltSearches = new ArrayList<Element>();

    @Override
    public String getIcon() {
        return icon;
    }

    public CswParams(DataManager dm) {
        super(dm);
    }

    /**
     * called when a new entry must be added. Reads values from the provided entry, providing
     * default values.
     */
    public void create(Element node) throws BadInputEx {
        super.create(node);

        Element site = node.getChild("site");
        Element searches = node.getChild("searches");

        capabUrl = Util.getParam(site, "capabilitiesUrl", "");
        rejectDuplicateResource = Util.getParam(site, "rejectDuplicateResource", false);
        queryScope = Util.getParam(site, "queryScope", "local");
        hopCount = Util.getParam(site, "hopCount", 2);
        xslfilter = Util.getParam(site, "xslfilter", "");
        xpathFilter = Util.getParam(site, "xpathFilter", "");
        outputSchema = Util.getParam(site, "outputSchema", outputSchema);
        icon = Util.getParam(site, "icon", "default.gif");

        if (searches != null) {
            if (searches.getChild("search") != null) {
                @SuppressWarnings("unchecked")
                List<Element> tmp = searches.getChild("search").getChildren();
                eltSearches = tmp;
            } else {
                eltSearches = new ArrayList<Element>();
            }
        }


    }

    /**
     * called when an entry has changed and variables must be updated.
     */
    public void update(Element node) throws BadInputEx {
        super.update(node);

        Element site = node.getChild("site");
        Element searches = node.getChild("searches");

        capabUrl = Util.getParam(site, "capabilitiesUrl", capabUrl);
        rejectDuplicateResource = Util.getParam(site, "rejectDuplicateResource", rejectDuplicateResource);
        queryScope = Util.getParam(site, "queryScope", queryScope);
        hopCount = Util.getParam(site, "hopCount", hopCount);
        xpathFilter = Util.getParam(site, "xpathFilter", "");
        xslfilter = Util.getParam(site, "xslfilter", "");
        outputSchema = Util.getParam(site, "outputSchema", outputSchema);

        icon = Util.getParam(site, "icon", icon);

        //--- if some search queries are given, we drop the previous ones and
        //--- set these new ones

        if (searches != null) {
            if (searches.getChild("search") != null) {
                @SuppressWarnings("unchecked")
                List<Element> tmp = searches.getChild("search").getChildren();
                eltSearches = tmp;
            }
        }

    }

    /**
     *
     * @return
     */
    public CswParams copy() {
        CswParams copy = new CswParams(dm);
        copyTo(copy);

        copy.capabUrl = capabUrl;
        copy.icon = icon;
        copy.rejectDuplicateResource = rejectDuplicateResource;
        copy.queryScope = queryScope;
        copy.hopCount = hopCount;
        copy.xpathFilter = xpathFilter;
        copy.xslfilter = xslfilter;
        copy.outputSchema = outputSchema;

        copy.eltSearches = eltSearches;

        return copy;
    }

}
