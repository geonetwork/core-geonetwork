package org.fao.geonet.services.region;

import jeeves.server.context.ServiceContext;

import com.vividsolutions.jts.geom.Geometry;

public interface RegionsDAO {

    /**
     * Create an object for constructing a search request to find regions
     */
    Request createSearchRequest(ServiceContext context) throws Exception;

    /**
     * Get the geometry object for the region.  The CRS must be available with the geometry's getUserData()
     * method. 
     * @param context
     * @param id id of the region ot fetch
     * @param simplified a hint to simplify the geometry if the full geometry is very large.  This will
     * be true when the UI wants to display the geometry.  The region is simplified so the javascript can deal with it better
     * and so it downloads faster.
     * 
     * @return the geometry containing the CRS
     */
    Geometry getGeom(ServiceContext context, String id, boolean simplified) throws Exception;

}
