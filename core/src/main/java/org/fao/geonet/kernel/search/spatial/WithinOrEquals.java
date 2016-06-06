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

import org.geotools.filter.spatial.WithinImpl;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

import com.vividsolutions.jts.geom.Geometry;

final class WithinOrEquals extends WithinImpl {
    WithinOrEquals(FilterFactory factory, Expression e1, Expression e2) {
        super(e1, e2);
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
