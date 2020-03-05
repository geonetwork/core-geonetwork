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

import org.fao.geonet.kernel.region.Region;
import org.jdom.Element;

/**
 * The strategy for locating the geometry in the metadata.
 *
 * @author Jesse on 1/21/2015.
 */
public abstract class MetadataRegionFinder {
    /**
     * Return true if this can handle the provided id
     */
    public abstract boolean accepts(String id);

    /**
     * Find the region and add it/them to the list of regions
     *
     * @param regions  the list to add found regions to.
     * @param mdId     the id of the metadata
     * @param id       the id identifying the XML element of the geometry
     * @param metadata the metadata
     */
    public abstract void findRegion(MetadataRegionSearchRequest searchRequest, java.util.List<Region> regions, MetadataRegionSearchRequest.Id mdId, String id, Element metadata) throws Exception;

    /**
     * Return true if the metadata should have the edit elements added to it in order to find the
     * region.
     */
    public abstract boolean needsEditData();
}
