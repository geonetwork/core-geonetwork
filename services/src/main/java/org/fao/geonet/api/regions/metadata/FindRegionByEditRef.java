/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.regions.metadata;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.region.Region;
import org.jdom.Element;

import java.util.Iterator;
import java.util.List;

/**
 * Find the element using the edit information on the elements.
 *
 * @author Jesse on 1/21/2015.
 */
public class FindRegionByEditRef extends MetadataRegionFinder {
    @Override
    public boolean accepts(String id) {
        return true;
    }

    @Override
    public void findRegion(MetadataRegionSearchRequest searchRequest, List<Region> regions, MetadataRegionSearchRequest.Id mdId, String id, Element metadata) throws Exception {
        Iterator<?> iter = metadata.getDescendants();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if (obj instanceof Element) {
                Element el = (Element) obj;
                Element geonet = el.getChild("element", Geonet.Namespaces.GEONET);
                if (geonet != null && id.equals(geonet.getAttributeValue("ref"))) {
                    Iterator<?> extent = searchRequest.descentOrSelf(el);
                    if (extent.hasNext()) {
                        regions.add(searchRequest.parseRegion(mdId, (Element) extent.next()));
                        return;
                    } else {
                        if (searchRequest.findContainingGmdEl(regions, mdId, el)) return;
                    }
                }
            }
        }
    }

    @Override
    public boolean needsEditData() {
        return true;
    }
}
