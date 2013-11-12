package org.fao.geonet.services.region;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.kernel.region.RegionsDAO;
import org.fao.geonet.kernel.region.Request;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Parser;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.Collection;
import java.util.Collections;

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
