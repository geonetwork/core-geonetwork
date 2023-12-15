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

package org.fao.geonet.api.regions;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.regions.metadata.MetadataRegionSearchRequest;
import org.fao.geonet.kernel.region.RegionsDAO;
import org.fao.geonet.kernel.region.Request;
import org.fao.geonet.util.GMLParsers;
import org.geotools.xsd.Parser;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import java.util.Collection;
import java.util.Collections;

/**
 * A Regions DAO that fetches geometries from a metadata.  The geometry ids are structured as
 * follows:
 *
 * <ul>
 *     <li>metadata:@id1234 - get all the geometries in the metadata with the id 1234</li>
 * </ul>
 */
public class MetadataRegionDAO extends RegionsDAO {

    public static final String CATEGORY_NAME = "metadata";

    private final GeometryFactory factory = new GeometryFactory();

    @Override
    public Collection<String> getRegionCategoryIds(ServiceContext context) throws Exception {
        return Collections.singleton(CATEGORY_NAME);
    }

    @Override
    public Request createSearchRequest(ServiceContext context) throws Exception {
        return new MetadataRegionSearchRequest(context, factory);
    }

    @Override
    public Geometry getGeom(ServiceContext context, String id, boolean simplified, CoordinateReferenceSystem projection)
        throws Exception {
        MetadataRegion region = (MetadataRegion) createSearchRequest(context).id(id).get();
        if (region != null) {
            return region.getGeometry(projection);
        } else {
            return null;
        }
    }

    @Override
    public boolean includeInListing() {
        return false;
    }

}
