package org.fao.geonet.kernel.search.spatial;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.index.SpatialIndex;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.apache.lucene.search.Query;
import org.fao.geonet.constants.Geonet;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.visitor.DefaultFilterVisitor;
import org.geotools.filter.visitor.DuplicatingFilterVisitor;
import org.geotools.filter.visitor.ExtractBoundsFilterVisitor;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xml.Parser;
import org.jdom.Element;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.And;
import org.opengis.filter.BinaryLogicOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

// -- define the featureType that will be used in the reprojectFilter
class Reproject {
		public static SimpleFeatureType fType = initialize();

		private static SimpleFeatureType initialize() {
			SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
 			AttributeTypeBuilder attBuilder  = new AttributeTypeBuilder();
			attBuilder.crs(DefaultGeographicCRS.WGS84);
			attBuilder.binding(MultiPolygon.class);
			GeometryDescriptor geomDescriptor = attBuilder.buildDescriptor(SpatialIndexWriter.GEOM_ATTRIBUTE_NAME, attBuilder.buildGeometryType());
			builder.setName("dummy");
			builder.setCRS( DefaultGeographicCRS.WGS84 );
 			builder.add(geomDescriptor);
			return builder.buildFeatureType();
		}
} 

public class OgcGenericFilters
{

    /**
     * 
     * @param query
     * @param filterExpr
     * @param sourceAccessor
     *            A lazy pair object that will obtain the required objects as
     *            needed. It is critical they do not directly reference the
     *            objects, especially the SpatialIndex because it changes
     *            frequently and can be a source of memory leakage. So it should
     *            Reference a singleton object capable of accessing the most
     *            current instance. The case is a LuceneSearcher being put on
     *            the UserSession with a spatial filter. The spatial filter
     *            references the current instance of SpatialIndex. This means if
     *            a new index is generated the filter will have an outdated
     *            version as well as keep reference to this potentially very
     *            large object.
     * @param parser
     * @return
     * @throws Exception
     */
    @SuppressWarnings("serial")
    public static SpatialFilter create(Query query, int numHits,
            Element filterExpr, Pair<FeatureSource<SimpleFeatureType, SimpleFeature>, SpatialIndex> sourceAccessor, Parser parser) throws Exception
    {
		// -- parse Filter and report any validation issues
        String string = Xml.getString(filterExpr);
				Log.debug(Geonet.SEARCH_ENGINE,"Filter string is :\n"+string);

        parser.setValidating(true);
        parser.setFailOnValidationError(false);

       	Log.debug(Geonet.SEARCH_ENGINE,"Parsing filter"); 
        Filter fullFilter = (org.opengis.filter.Filter) parser
                .parse(new StringReader(string));
        if( parser.getValidationErrors().size() > 0){
        	Log.error(Geonet.SEARCH_ENGINE,"Errors occurred when trying to parse a filter:");
        	Log.error(Geonet.SEARCH_ENGINE,"----------------------------------------------");
	        for( Object error:parser.getValidationErrors()){
	        	Log.error(Geonet.SEARCH_ENGINE,error);
	        }
        	Log.error(Geonet.SEARCH_ENGINE,"----------------------------------------------");
        }
        final FilterFactory2 filterFactory2 = CommonFactoryFinder
                .getFilterFactory2(GeoTools.getDefaultHints());

				// -- extract spatial terms from Filter expression
        FilterVisitor visitor = new GeomExtractor(filterFactory2);
        Filter trimmedFilter = (Filter) fullFilter.accept(visitor, null);
        if (trimmedFilter == null) {
            return null;
        }
       
			 	// -- rename all PropertyName elements used in Filter to match the
				// -- geometry type used in the spatial index
        Filter remappedFilter = (Filter) trimmedFilter.accept(new RenameGeometryPropertyNameVisitor(),null);

				// -- finally reproject all geometry in the Filter to match GeoNetwork
				// -- default of WGS84 (long/lat ordering)
				visitor = new ReprojectingFilterVisitor(filterFactory2, Reproject.fType);
				final Filter finalFilter = (Filter) remappedFilter.accept(visitor, null);
        Log.debug(Geonet.SEARCH_ENGINE,"Reprojected Filter is "+finalFilter);

				// -- extract an envelope/bbox for the whole filter expression
        Envelope bounds = (Envelope) finalFilter.accept(
                ExtractBoundsFilterVisitor.BOUNDS_VISITOR,
                DefaultGeographicCRS.WGS84);
				Log.debug(Geonet.SEARCH_ENGINE,"Filter Envelope is "+bounds);
        
        Boolean disjointFilter = (Boolean) finalFilter.accept(new DisjointDetector(), false);
        if( disjointFilter ){
            return new FullScanFilter(query, numHits, bounds, sourceAccessor){
                @Override
                protected Filter createFilter(FeatureSource source)
                {
                    return finalFilter;
                }
            };
        }else{
            return new SpatialFilter(query, numHits, bounds, sourceAccessor){
                @Override
                protected Filter createFilter(FeatureSource source)
                {
                    return finalFilter;
                }
            };
        }

    }


    /**
     * Renames all ProperyNames to the geometry attribute name thatis used by the SpatialIndex shapefile
     * 
     * @author jeichar
     */
    private static final class RenameGeometryPropertyNameVisitor extends
            DuplicatingFilterVisitor
    {
        @Override
        public Object visit(PropertyName expression, Object data)
        {
            return getFactory(data).property(SpatialIndexWriter.GEOM_ATTRIBUTE_NAME);
        }

        @Override
        public Object visit(BBOX filter, Object extraData)
        {

            double minx=filter.getMinX();
            double miny=filter.getMinY();
            double maxx=filter.getMaxX();
            double maxy=filter.getMaxY();
            String srs=filter.getSRS();
            
            return getFactory(extraData).bbox(SpatialIndexWriter.GEOM_ATTRIBUTE_NAME, minx, miny, maxx, maxy, srs);
        }
    }

    /**
     * Returns True if the Filter is testing whether the spatial index canNOT be used.  Otherwise data is returned 
     * 
     * <p>
     *  For example:  Not ( intersects) returns true.  Beyond also returns true.
     * >/p>
     * @author jeichar
     */
    private static class DisjointDetector extends DefaultFilterVisitor{
        
        @Override
        public Object visit(And filter, Object data)
        {
            for( Filter child:filter.getChildren()){
                if(child.accept(this, data)!=data){
                    return true;
                }
            }
            return super.visit(filter, data);
        }
        @Override
        public Object visit(Or filter, Object data)
        {
            for( Filter child:filter.getChildren()){
                if(child.accept(this, data)!=data){
                    return true;
                }
            }
            return super.visit(filter, data);
        }
        @Override
        public Object visit(Not filter, Object data)
        {
            if( filter.getFilter().accept(this, data)==data ){
                return true;
            }
            return data;
        }
        @Override
        public Object visit(DWithin filter, Object data)
        {
            return true;
        }
        @Override
        public Object visit(Beyond filter, Object data)
        {
            return true;
        }
        
        @Override
        public Object visit(Disjoint filter, Object data)
        {
            return true;
        }
        
        
    }
    
    /**
     * Pulls all the Geometry and Logic Filters out of the FilterVisitor.  A new filter is returned with only the GeometryFilters (and Logic Filters)
     * or null if the filter does not have any GeometryFilters
     * 
     * @author jeichar
     */
    private static class GeomExtractor extends DefaultFilterVisitor
    {
        private final FilterFactory2 _filterFactory;

        public GeomExtractor(FilterFactory2 factory)
        {
            super();
            _filterFactory = factory;
        }

        @Override
        public Filter visit(And filter, Object data)
        {
            List<Filter> newChildren = visitLogicFilter(filter, data);
            if (newChildren.isEmpty()) {
                return null;
            }
            if (newChildren.size() == 1) {
                return newChildren.get(0);
            }
            return _filterFactory.and(newChildren);
        }

        private List<Filter> visitLogicFilter(BinaryLogicOperator filter,
                Object data)
        {
            List<Filter> newChildren = new ArrayList<Filter>();
            for (Filter child : filter.getChildren()) {
                Filter newChild = (Filter) child.accept(this, data);
                if (newChild != null) {
                    newChildren.add(newChild);
                }
            }
            return newChildren;
        }

        @Override
        public Not visit(Not filter, Object data)
        {
            Filter newChild = (Filter) filter.getFilter().accept(this, data);
            if (newChild == null) {
                return null;
            }
            return _filterFactory.not(newChild);
        }

        @Override
        public Filter visit(Or filter, Object data)
        {
            List<Filter> newChildren = visitLogicFilter(filter, data);
            if (newChildren.isEmpty()) {
                return null;
            }
            if (newChildren.size() == 1) {
                return newChildren.get(0);
            }
            return _filterFactory.or(newChildren);
        }

        @Override
        public BBOX visit(BBOX filter, Object data)
        {
            return filter;
        }

        @Override
        public Beyond visit(Beyond filter, Object data)
        {
            return filter;
        }

        @Override
        public Contains visit(Contains filter, Object data)
        {
            return filter;
        }

        @Override
        public Crosses visit(Crosses filter, Object data)
        {
            return filter;
        }

        @Override
        public Disjoint visit(Disjoint filter, Object data)
        {
            return filter;
        }

        @Override
        public DWithin visit(DWithin filter, Object data)
        {
            return filter;
        }

        @Override
        public Equals visit(Equals filter, Object data)
        {
            return filter;
        }

        @Override
        public Intersects visit(Intersects filter, Object data)
        {
            return filter;
        }

        @Override
        public Within visit(Within filter, Object data)
        {
            return filter;
        }

        @Override
        public Overlaps visit(Overlaps filter, Object data)
        {
            return filter;
        }

        @Override
        public Touches visit(Touches filter, Object data)
        {
            return filter;
        }
    }
}
