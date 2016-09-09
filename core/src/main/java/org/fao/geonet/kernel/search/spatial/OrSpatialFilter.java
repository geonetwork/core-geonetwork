/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel.search.spatial;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.search.Query;
import org.fao.geonet.domain.Pair;
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

    @Override
    protected Filter createFilter(FeatureSource<SimpleFeatureType, SimpleFeature> source) {
        List<Filter> ops = new ArrayList<Filter>(filters.size());

        for (SpatialFilter sfilter : filters) {
            ops.add(sfilter.createFilter(source));
        }
        return _filterFactory.or(ops);

    }

    @Override
    public Filter createGeomFilter(FilterFactory2 filterFactory, PropertyName geomPropertyName, Literal geomExpression) {
        throw new UnsupportedOperationException();
    }
}
