package org.fao.geonet.services.region;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.kernel.region.RegionsDAO;
import org.fao.geonet.kernel.region.Request;
import org.fao.geonet.services.region.metadata.MetadataRegionSearchRequest;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Parser;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.Collection;
import java.util.Collections;

/**
 * A Regions DAO that fetches geometries from a metadata.  The geometry ids are structured as follows:
 *
 * <ul>
 *     <li>metadata:@id1234 - get all the geometries in the metadata with the id 1234</li>
 *     <li>metadata:@uuid1234 - get all the geometries in the metadata with the uuid 1234</li>
 *     <li>metadata:@uuid1234:1111 - get all the geometry with the geonet:element/@ref = 1111 in the metadata with the uuid 1234</li>
 *     <li>metadata:@uuid1234:@gml1111 - get all the geometry with the @gml:id = 1111 in the metadata with the uuid 1234</li>
 * </ul>
 */
public class MetadataRegionDAO extends RegionsDAO {

    public static final String CATEGORY_NAME = "metadata";
    private final Parser parser = new Parser(new GMLConfiguration());
    private final GeometryFactory factory = new GeometryFactory();
    
    @Override
    public Collection<String> getRegionCategoryIds(ServiceContext context) throws Exception {
        return Collections.singleton(CATEGORY_NAME);
    }

    @Override
    public Request createSearchRequest(ServiceContext context) throws Exception {
        return new MetadataRegionSearchRequest(context, parser, factory);
    }

    @Override
    public Geometry getGeom(ServiceContext context, String id, boolean simplified, CoordinateReferenceSystem projection)
            throws Exception {
        MetadataRegion region = (MetadataRegion) createSearchRequest(context).id(id).get();
        if(region != null) {
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
