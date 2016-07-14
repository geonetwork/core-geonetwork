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

import com.google.common.collect.Lists;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.region.Region;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.util.List;

/**
 * Find a geometry by the gml id attribute on the element.
 *
 * @author Jesse on 1/21/2015.
 */
public class FindRegionByGmlId extends MetadataRegionFinder {
    public static final String GML_ID_PREFIX = "@gml";

    @Override
    public boolean accepts(String id) {
        return id.startsWith(GML_ID_PREFIX);
    }

    @Override
    public void findRegion(MetadataRegionSearchRequest searchRequest, List<Region> regions, MetadataRegionSearchRequest.Id mdId, String id, Element metadata) throws Exception {
        String gmlId = id.substring(GML_ID_PREFIX.length());
        final Element geomEl = Xml.selectElement(metadata, "*//*[@gml:id = '" + gmlId + "']", Lists.newArrayList(Geonet.Namespaces.GML));
        if (geomEl != null) {
            searchRequest.findContainingGmdEl(regions, mdId, geomEl);
        }

    }

    @Override
    public boolean needsEditData() {
        return false;
    }
}
