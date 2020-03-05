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

import com.vividsolutions.jts.geom.Geometry;

import org.fao.geonet.kernel.region.Region;
import org.fao.geonet.api.regions.metadata.MetadataRegionSearchRequest.Id;
import org.fao.geonet.api.records.extent.MapRenderer;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.util.Collections;

public class MetadataRegion extends Region {

    private Geometry geometry;

    public MetadataRegion(Id mdId, String id, Geometry geometry) {
        super("metadata" + mdId.getIdentifiedId() + ":" + id, Collections.<String, String>emptyMap(), MetadataRegionDAO.CATEGORY_NAME,
            Collections.<String, String>emptyMap(), true,
            new ReferencedEnvelope(geometry.getEnvelopeInternal(), WGS84));
        this.geometry = geometry;
    }

    public Geometry getGeometry(CoordinateReferenceSystem projection) throws Exception {

        CoordinateReferenceSystem coordinateReferenceSystem = getBBox().getCoordinateReferenceSystem();
        Integer sourceCode = CRS.lookupEpsgCode(coordinateReferenceSystem, false);
        Integer desiredCode = CRS.lookupEpsgCode(projection, false);
        boolean differentCrsCode = sourceCode == null || desiredCode == null || desiredCode.intValue() != sourceCode.intValue();
        if (differentCrsCode && !CRS.equalsIgnoreMetadata(coordinateReferenceSystem, projection)) {
            geometry = MapRenderer.computeGeomInDomainOfValidity(geometry, projection);
            MathTransform transform = CRS.findMathTransform(coordinateReferenceSystem, projection, true);
            return JTS.transform(geometry, transform);
        }
        return geometry;
    }
}
