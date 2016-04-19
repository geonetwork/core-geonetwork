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

import org.geotools.feature.NameImpl;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;

import java.util.List;

public abstract  class FilterUtils {
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
}
