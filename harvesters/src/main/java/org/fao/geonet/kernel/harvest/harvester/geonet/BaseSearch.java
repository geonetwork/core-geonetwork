//=============================================================================
//===    Copyright (C) 2001-2025 Food and Agriculture Organization of the
//===    United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===    and United Nations Environment Programme (UNEP)
//===
//===    This program is free software; you can redistribute it and/or modify
//===    it under the terms of the GNU General Public License as published by
//===    the Free Software Foundation; either version 2 of the License, or (at
//===    your option) any later version.
//===
//===    This program is distributed in the hope that it will be useful, but
//===    WITHOUT ANY WARRANTY; without even the implied warranty of
//===    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===    General Public License for more details.
//===
//===    You should have received a copy of the GNU General Public License
//===    along with this program; if not, write to the Free Software
//===    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===    Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===    Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.kernel.harvest.harvester.geonet;

import org.fao.geonet.Util;
import org.fao.geonet.exceptions.BadParameterEx;
import org.jdom.Element;

public class BaseSearch {
    public int from;
    public int to;
    public String freeText;
    public String title;
    // Can't use abstract as the name because it's a reserved word in Java.
    public String abstractText;
    public String keywords;
    public String description;
    public String sourceUuid;

    public BaseSearch() {

    }

    public BaseSearch(Element search) throws BadParameterEx {
        freeText = Util.getParam(search, "freeText", "");
        title = Util.getParam(search, "title", "");
        abstractText = Util.getParam(search, "abstract", "");
        keywords = Util.getParam(search, "keywords", "");

        Element source = search.getChild("source");

        sourceUuid = Util.getParam(source, "uuid", "");

        from = Util.getParam(search, "from", 0);
        to = Util.getParam(search, "to", 0);

        if (from < 0 || to < 0) {
            throw new BadParameterEx("from/to", "must be >= 0");
        }
    }
}
