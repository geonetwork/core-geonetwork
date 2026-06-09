//=============================================================================
//===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.webdav;

import org.fao.geonet.Util;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.jdom.Element;

public class WebDavParams extends AbstractParams {


    /**
     * url of webdav folder to harvest
     */
    public String url;

    /**
     * Icon to use for harvester
     */
    public String icon;

    /**
     * If true recurse into directories.
     */
    public boolean recurse;

    /**
     * Flag indicating if WAFRetriever or WebDavRetriever should be used.
     */
    public String subtype;

    /**
     * The filter is a process (see schema/process folder) which depends on the schema. It could be
     * composed of parameter which will be sent to XSL transformation using the following syntax :
     * <pre>
     * anonymizer?protocol=MYLOCALNETWORK:FILEPATH&email=gis@organisation.org&thesaurus=MYORGONLYTHEASURUS
     * </pre>
     */
    public String xslfilter;


    public WebDavParams(DataManager dm) {
        super(dm);
    }

    @Override
    public void create(Element node) throws BadInputEx {
        super.create(node);

        Element site = node.getChild("site");
        Element opt = node.getChild("options");

        url = Util.getParam(site, "url", "");
        icon = Util.getParam(site, "icon", "");
        xslfilter = Util.getParam(site, "xslfilter", "");

        recurse = Util.getParam(opt, "recurse", false);

        subtype = Util.getParam(opt, "subtype", "");
    }

    @Override
    public void update(Element node) throws BadInputEx {
        super.update(node);

        Element site = node.getChild("site");
        Element opt = node.getChild("options");

        url = Util.getParam(site, "url", url);
        icon = Util.getParam(site, "icon", icon);
        xslfilter = Util.getParam(site, "xslfilter", "");

        recurse = Util.getParam(opt, "recurse", recurse);
        subtype = Util.getParam(opt, "subtype", subtype);
    }

    public WebDavParams copy() {
        WebDavParams copy = new WebDavParams(dm);
        copyTo(copy);

        copy.url = url;
        copy.icon = icon;
        copy.xslfilter = xslfilter;

        copy.setValidate(getValidate());
        copy.recurse = recurse;

        copy.subtype = subtype;

        return copy;
    }

    @Override
    public String getIcon() {
        return icon;
    }
}
