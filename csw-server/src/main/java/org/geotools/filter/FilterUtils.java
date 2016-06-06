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

package org.geotools.filter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;

import org.geotools.feature.NameImpl;
import org.geotools.filter.spatial.BBOXImpl;
import org.geotools.filter.spatial.ContainsImpl;
import org.geotools.filter.spatial.CrossesImpl;
import org.geotools.filter.spatial.DisjointImpl;
import org.geotools.filter.spatial.EqualsImpl;
import org.geotools.filter.spatial.IntersectsImpl;
import org.geotools.filter.spatial.OverlapsImpl;
import org.geotools.filter.spatial.WithinImpl;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.MultiValuedFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Within;
import org.opengis.referencing.FactoryException;

import java.util.List;

public abstract class FilterUtils {
    private static final GeometryFactory GEOMETRY_FACTORY = JTSFactoryFinder.getGeometryFactory();


    public static Filter createEqualsTo(String name, Object value) {
        return new IsEqualsToImpl(new AttributeExpressionImpl(new NameImpl(name)), new LiteralExpressionImpl(value));
    }

    public static And createAnd(List<Filter> filters) {
        return new AndImpl(filters);
    }

    public static Or createOr(List<Filter> filters) {
        return new OrImpl(filters);
    }

    public static Filter createNotEqualsTo(String name, Object value) {
        return new IsNotEqualToImpl(new AttributeExpressionImpl(new NameImpl(name)), new LiteralExpressionImpl(value));

    }

    public static Not createNot(Filter filter) {
        return new NotImpl(filter);
    }

    public static PropertyIsGreaterThan createIsGreaterThanImpl(String name, Object value) {
        return new IsGreaterThanImpl(new AttributeExpressionImpl(new NameImpl(name)), new LiteralExpressionImpl(value));
    }

    public static PropertyIsGreaterThanOrEqualTo createIsGreaterThanImplOrEqualTo(String name, Object value) {
        return new IsGreaterThanOrEqualToImpl(new AttributeExpressionImpl(new NameImpl(name)), new LiteralExpressionImpl(value));
    }


    public static PropertyIsLessThan createIsLessThenImpl(String name, Object value) {
        return new IsLessThenImpl(new AttributeExpressionImpl(new NameImpl(name)), new LiteralExpressionImpl(value));
    }

    public static PropertyIsLessThanOrEqualTo createIsLessThenImplOrEqualTo(String name, Object value) {
        return new IsLessThenOrEqualToImpl(new AttributeExpressionImpl(new NameImpl(name)), new LiteralExpressionImpl(value));
    }

    public static PropertyIsLike createPropertyIsLike(String name, String pattern, String wildcard,
                                                      String singleChar, String escape) {
        return new LikeFilterImpl(new AttributeExpressionImpl(new NameImpl(name)), pattern, wildcard,
            singleChar, escape);
    }

    public static PropertyIsBetween createPropertyIsBetween(String name, Object min, Object max) {
        return new IsBetweenImpl(new LiteralExpressionImpl(min), new AttributeExpressionImpl(new NameImpl(name)),
            new LiteralExpressionImpl(max));
    }

    public static BBOX createBbox(String name, double minX, double maxX, double maxY, double minY) {
        return new BBOXImpl(new AttributeExpressionImpl(new NameImpl(name)), minX, minY, maxX, maxY, "EPSG:4326", MultiValuedFilter.MatchAction.ANY);
    }

    public static Contains createContains(String name, double minX, double maxX, double maxY, double minY) throws FactoryException {
        Polygon polygon = boundingPolygon(minX, maxX, maxY, minY);
        return new ContainsImpl(new AttributeExpressionImpl(new NameImpl(name)), new LiteralExpressionImpl(polygon));
    }

    public static Disjoint createDisjoint(String name, double minX, double maxX, double maxY, double minY) throws FactoryException {
        Polygon polygon = boundingPolygon(minX, maxX, maxY, minY);
        return new DisjointImpl(new AttributeExpressionImpl(new NameImpl(name)), new LiteralExpressionImpl(polygon));
    }

    public static Equals createEquals(String name, double minX, double maxX, double maxY, double minY) throws FactoryException {
        Polygon polygon = boundingPolygon(minX, maxX, maxY, minY);
        return new EqualsImpl(new AttributeExpressionImpl(new NameImpl(name)), new LiteralExpressionImpl(polygon));
    }

    public static Intersects createIntersects(String name, double minX, double maxX, double maxY, double minY) throws FactoryException {
        Polygon polygon = boundingPolygon(minX, maxX, maxY, minY);
        return new IntersectsImpl(new AttributeExpressionImpl(new NameImpl(name)), new LiteralExpressionImpl(polygon));
    }

    public static Overlaps createOverlaps(String name, double minX, double maxX, double maxY, double minY) throws FactoryException {
        Polygon polygon = boundingPolygon(minX, maxX, maxY, minY);
        return new OverlapsImpl(new AttributeExpressionImpl(new NameImpl(name)), new LiteralExpressionImpl(polygon));
    }

    public static Within createWithin(String name, double minX, double maxX, double maxY, double minY) throws FactoryException {
        Polygon polygon = boundingPolygon(minX, maxX, maxY, minY);
        return new WithinImpl(new AttributeExpressionImpl(new NameImpl(name)), new LiteralExpressionImpl(polygon));
    }

    public static Crosses createCrosses(String name, double minX, double maxX, double maxY, double minY) throws FactoryException {
        Polygon polygon = boundingPolygon(minX, maxX, maxY, minY);
        return new CrossesImpl(new AttributeExpressionImpl(new NameImpl(name)), new LiteralExpressionImpl(polygon));
    }


    public static Polygon boundingPolygon(double minX, double maxX, double maxY, double minY) throws FactoryException {
        Coordinate[] coords = new Coordinate[]{new Coordinate(minX, minY), new Coordinate(minX, maxY), new Coordinate(maxX, maxY), new Coordinate(maxX, minY), new Coordinate(minX, minY)};
        final LinearRing ring;

        try {
            ring = GEOMETRY_FACTORY.createLinearRing(coords);
        } catch (TopologyException var5) {
            throw new IllegalFilterException(var5.toString());
        }

        Polygon polygon = GEOMETRY_FACTORY.createPolygon(ring, (LinearRing[]) null);
        polygon.setUserData(CRS.decode("EPSG:4326"));
        return polygon;
    }
}
