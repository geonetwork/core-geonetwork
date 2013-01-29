package org.fao.geonet.kernel.search.spatial;

import org.geotools.filter.spatial.WithinImpl;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

import com.vividsolutions.jts.geom.Geometry;

final class WithinOrEquals extends WithinImpl {
    WithinOrEquals(FilterFactory factory, Expression e1, Expression e2) {
        super(factory, e1, e2);
    }

    @Override
    public boolean evaluateInternal(Geometry leftGeom, Geometry rightGeom) {
        boolean equals2 = leftGeom.getBoundary().norm().equalsExact(rightGeom.getBoundary().norm(), 0.01);
        return equals2 || super.evaluateInternal(leftGeom, rightGeom);
    }
    
    @Override
    public String toString() {
        return super.toString().replace("within", "within or equal");
    }
}