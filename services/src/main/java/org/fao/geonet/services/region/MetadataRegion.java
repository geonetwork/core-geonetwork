package org.fao.geonet.services.region;

import com.vividsolutions.jts.geom.Geometry;

import org.fao.geonet.kernel.region.Region;
import org.fao.geonet.services.region.metadata.MetadataRegionSearchRequest.Id;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.util.Collections;

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
        boolean differentCrsCode = sourceCode == null || desiredCode == null || desiredCode.intValue() != sourceCode.intValue();
        if (differentCrsCode && !CRS.equalsIgnoreMetadata(coordinateReferenceSystem, projection)) {
            MathTransform transform = CRS.findMathTransform(coordinateReferenceSystem, projection, true);
            return JTS.transform(geometry, transform);
        }
        return geometry;
    }
}
