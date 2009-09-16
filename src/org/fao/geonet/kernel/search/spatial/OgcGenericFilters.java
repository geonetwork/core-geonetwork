package org.fao.geonet.kernel.search.spatial;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import jeeves.utils.Log;
import jeeves.utils.Xml;

import org.apache.lucene.search.Query;
import org.fao.geonet.constants.Geonet;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.visitor.DefaultFilterVisitor;
import org.geotools.filter.visitor.DuplicatingFilterVisitor;
import org.geotools.filter.visitor.ExtractBoundsFilterVisitor;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xml.Parser;
import org.jdom.Element;
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
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.SpatialIndex;

public class OgcGenericFilters
{

    @SuppressWarnings("serial")
    public static SpatialFilter create(Query query,
            Element filterExpr, FeatureSource featureSource,
            SpatialIndex index, Parser parser) throws Exception
    {
        String string = Xml.getString(filterExpr);
//        FIXME : Hack to temporary remove srsName information
//        which are not resolved correctly by the parser.
//        This assume all filter are using WGS84
        string = string.replaceAll(" srsName=\"(.*)\"", "");
//        is this related to http://jira.codehaus.org/browse/GEOT-1388 ?
//        System.setProperty("org.geotools.referencing.forceXY", "true");
//        CoordinateReferenceSystem urn = CRS.decode("");
//        System.out.println(urn);
//        CoordinateReferenceSystem epsg = CRS.decode("EPSG:4326");
//        System.out.println(epsg);

        parser.setValidating(true);
        parser.setFailOnValidationError(false);

        
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
        FilterVisitor visitor = new GeomExtractor(filterFactory2);

        Filter trimmedFilter = (Filter) fullFilter.accept(visitor, null);

        if (trimmedFilter == null) {
            return null;
        }
        
        final Filter finalFilter = (Filter) trimmedFilter.accept(new RenameGeometryPropertyNameVisitor(),null);

        Envelope bounds = (Envelope) trimmedFilter.accept(
                ExtractBoundsFilterVisitor.BOUNDS_VISITOR,
                DefaultGeographicCRS.WGS84);
        
        Boolean disjointFilter = (Boolean) finalFilter.accept(new DisjointDetector(), false);
        if( disjointFilter ){
            return new FullScanFilter(query, bounds,
                    featureSource, index){
                @Override
                protected Filter createFilter(FeatureSource source)
                {
                    return finalFilter;
                }
            };
        }else{
            return new SpatialFilter(query, bounds,
                    featureSource, index){
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
            Filter newChild = (Filter) filter.accept(this, null);
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
