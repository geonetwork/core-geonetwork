//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.geonet.v21_3;

import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.geonet.BaseGeonetParams;
import org.fao.geonet.kernel.harvest.harvester.geonet.Group;
import org.jdom.Element;


public class GeonetParams extends BaseGeonetParams<Search> {
    public GeonetParams(DataManager dm) {
        super(dm);
    }

    @Override
    public GeonetParams copy() {
        GeonetParams copy = new GeonetParams(dm);
        copyTo(copy);

        copy.host = host;
        copy.node = node;
        copy.useChangeDateForUpdate = useChangeDateForUpdate;
        copy.createRemoteCategory = createRemoteCategory;
        copy.mefFormatFull = mefFormatFull;
        copy.xslfilter = xslfilter;

        for (Search s : alSearches)
            copy.alSearches.add(s.copy());

        for (Group g : alCopyPolicy)
            copy.alCopyPolicy.add(g.copy());

        return copy;
    }

    @Override
    protected void addSearches(Element searches) throws BadInputEx {
        alSearches.clear();

        if (searches == null)
            return;

        for (Object o : searches.getChildren("search")) {
            Element search = (Element) o;

            alSearches.add(new Search(search));
        }
    }
}
