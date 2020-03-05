package org.fao.geonet.kernel.search.spatial;

import com.vividsolutions.jts.geom.Polygon;

import java.util.List;

public interface ErrorHandler {
    void handleParseException(Exception e, String gml);

    void handleBuildException(Exception e, List<Polygon> polygons);
}
