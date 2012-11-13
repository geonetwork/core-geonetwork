package org.fao.geonet.services.region;

import java.lang.ref.WeakReference;
import java.util.Collection;

import jeeves.server.context.ServiceContext;

import org.jdom.Element;

import com.vividsolutions.jts.geom.Geometry;

public abstract class RegionsDAO {
    private boolean cacheAllRegionsInMemory = true;
    private WeakReference<CachedRequest> allRegions = new WeakReference<CachedRequest>(null);
    
    /**
     * Create an object for constructing a search request to find regions
     */
    public abstract Request createSearchRequest(ServiceContext context) throws Exception;

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
    public abstract Geometry getGeom(ServiceContext context, String id, boolean simplified) throws Exception;

    /**
     * Return all regions.
     * @param context 
     * 
     * @return
     * @throws Exception 
     */
    public Collection<Region> getAllRegions(ServiceContext context) throws Exception {
        return allRegions(context).execute();
    }

    /**
     * Return all regions formatted as XML.  See
     * @param context 
     * @return
     * @throws Exception 
     */
    public Element getAllRegionsAsXml(ServiceContext context) throws Exception {
        return allRegions(context).xmlResult();
    }
    
    private Request allRegions(ServiceContext context) throws Exception {
        if(cacheAllRegionsInMemory) {
            synchronized (this) {
                CachedRequest request = allRegions.get();
                if (request == null) {
                    request = new CachedRequest(createSearchRequest(context));
                    allRegions = new WeakReference<CachedRequest>(request);
                }
                return request;
            }
        } else {
            return createSearchRequest(context);
        }
    }
    
    public void setCacheAllRegionsInMemory(boolean cacheAllRegionsInMemory) {
        this.cacheAllRegionsInMemory = cacheAllRegionsInMemory;
    }

}
