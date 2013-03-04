package org.fao.geonet.services.region;

import java.util.Collections;

import org.fao.geonet.services.region.MetadataRegionSearchRequest.Id;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;

public class MetadataRegion extends Region {

    private Geometry geometry;

    public MetadataRegion(Id mdId, String id, Geometry geometry) {
        super("metadata"+mdId.getIdentifiedId()+":"+id, Collections.<String, String>emptyMap(), MetadataRegionDAO.CATEGORY_NAME, 
                Collections.<String, String>emptyMap(), true, 
                new ReferencedEnvelope(geometry.getEnvelopeInternal(), WGS84));
        this.geometry = geometry;
    }

    public Geometry getGeometry(CoordinateReferenceSystem projection) throws Exception {

        CoordinateReferenceSystem coordinateReferenceSystem = getBBox().getCoordinateReferenceSystem();
        Integer sourceCode = CRS.lookupEpsgCode(coordinateReferenceSystem, false);
        Integer desiredCode = CRS.lookupEpsgCode(projection, false);
        if ((sourceCode == null || desiredCode == null || desiredCode != sourceCode) && !CRS.equalsIgnoreMetadata(coordinateReferenceSystem, projection)) {
            MathTransform transform = CRS.findMathTransform(coordinateReferenceSystem, projection, true);
            return JTS.transform(geometry, transform);
        }
        return geometry;
    }
}
