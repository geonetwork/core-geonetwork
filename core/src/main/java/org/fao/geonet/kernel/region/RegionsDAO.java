package org.fao.geonet.kernel.region;

import com.vividsolutions.jts.geom.Geometry;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.lang.ref.WeakReference;
import java.util.Collection;

public abstract class RegionsDAO {
    private boolean cacheAllRegionsInMemory = true;
    private WeakReference<CachedRequest> allRegions = new WeakReference<CachedRequest>(null);

    /**
     * Look up all the ids of all the categories available.
     * 
     * This should be an inexpensive operation and will typically 
     * be cached for performance.
     * 
     * @return all the ids of all the categories available
     */
    public abstract Collection<String> getRegionCategoryIds(ServiceContext context) throws Exception;

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
     * @param projection the desired projection of the geometry.  The geometry will be reprojected to desired projection
     * 
     * @return the geometry containing the CRS
     */
    public abstract Geometry getGeom(ServiceContext context, String id, boolean simplified, CoordinateReferenceSystem projection) throws Exception;
    
    /**
     * Get the geometry object for the region.  The CRS must be available with the geometry's getUserData()
     * method. 
     * @param context
     * @param id id of the region ot fetch
     * @param simplified a hint to simplify the geometry if the full geometry is very large.  This will
     * be true when the UI wants to display the geometry.  The region is simplified so the javascript can deal with it better
     * and so it downloads faster.
     * @param projectionCode the desired projection of the geometry.  The geometry will be reprojected to desired projection
     * 
     * @return the geometry containing the CRS
     */
    public final Geometry getGeom(ServiceContext context, String id, boolean simplified, String projectionCode) throws Exception {
        return getGeom(context, id, simplified, Region.decodeCRS(projectionCode));
    }
    
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
                    allRegions = new WeakReference<>(request);
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

    protected synchronized void clearCaches() {
        allRegions.clear();
    }

    public boolean includeInListing() {
        return true;
    }
}
