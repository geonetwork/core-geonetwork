package org.fao.geonet.kernel.search.spatial;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.search.Query;
import org.geotools.data.FeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.SpatialIndex;


public class OrSpatialFilter extends SpatialFilter {

    private Collection<SpatialFilter> filters;

    public OrSpatialFilter(Query query, int numHits, Envelope bounds,
            Pair<FeatureSource<SimpleFeatureType, SimpleFeature>, SpatialIndex> sourceAccessor, Collection<SpatialFilter> filters) throws IOException {
        super(query, numHits, bounds, sourceAccessor);
        this.filters = filters;
    }

    private static final long serialVersionUID = 1L;

    @Override
    public Filter createGeomFilter(FilterFactory2 filterFactory, PropertyName geomPropertyName, Literal geomExpression) {
        List<Filter> ops = new ArrayList<Filter>(filters.size());
        
        for (SpatialFilter sfilter : filters) {
            ops.add(sfilter.createGeomFilter(filterFactory, geomPropertyName, geomExpression));
        }
        return filterFactory.or(ops);
    }
}
