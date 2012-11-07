package org.fao.geonet.services.region;

import jeeves.server.context.ServiceContext;

import com.vividsolutions.jts.geom.Geometry;

public interface RegionsDAO {

    Request createSearchRequest(ServiceContext context) throws Exception;

    Geometry getGeom(ServiceContext context, String id, boolean simplified) throws Exception;

}
