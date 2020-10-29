package org.fao.geonet.kernel.search.spatial;

import java.util.List;

import org.locationtech.jts.geom.Polygon;

public interface ErrorHandler {
    void handleParseException(Exception e, String gml);

    void handleBuildException(Exception e, List<Polygon> polygons);
}
