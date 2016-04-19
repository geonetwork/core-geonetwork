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

package org.fao.geonet.kernel.csw.services.getrecords.solr;

import org.apache.commons.lang.NotImplementedException;
import org.fao.geonet.kernel.csw.services.getrecords.IFieldMapper;
import org.geotools.filter.visitor.AbstractFilterVisitor;
import org.opengis.filter.And;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.BinaryLogicOperator;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNil;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;
import org.opengis.filter.temporal.After;
import org.opengis.filter.temporal.AnyInteracts;
import org.opengis.filter.temporal.Before;
import org.opengis.filter.temporal.Begins;
import org.opengis.filter.temporal.BegunBy;
import org.opengis.filter.temporal.During;
import org.opengis.filter.temporal.EndedBy;
import org.opengis.filter.temporal.Ends;
import org.opengis.filter.temporal.Meets;
import org.opengis.filter.temporal.MetBy;
import org.opengis.filter.temporal.OverlappedBy;
import org.opengis.filter.temporal.TContains;
import org.opengis.filter.temporal.TEquals;
import org.opengis.filter.temporal.TOverlaps;

import java.util.regex.Pattern;

/**
 * Manages the translation from CSW &lt;Filter&gt; into a Solr expression.
 */
public class CswFilter2Solr extends AbstractFilterVisitor {
    private final StringBuilder out = new StringBuilder();
    private final Expression2CswVisitor expressionVisitor;

    public CswFilter2Solr(IFieldMapper fieldMapper) {
        expressionVisitor = new Expression2CswVisitor(out, fieldMapper);
    }

    public static String translate(Filter filter, IFieldMapper fieldMapper) {
        if (filter == null) {
            return null;
        }
        CswFilter2Solr translator = new CswFilter2Solr(fieldMapper);
        filter.accept(translator, translator);
        return translator.out.toString();
    }

    @Override
    public Object visitNullFilter(Object extraData) {
        return super.visitNullFilter(extraData);
    }

    @Override
    public Object visit(ExcludeFilter filter, Object extraData) {
        return super.visit(filter, extraData);
    }

    @Override
    public Object visit(IncludeFilter filter, Object extraData) {
        return super.visit(filter, extraData);
    }

    @Override
    public Object visit(And filter, Object extraData) {
        return visitBinaryLogic(filter, "AND", extraData);
    }

    private Object visitBinaryLogic(BinaryLogicOperator filter, String operator, Object extraData) {
        out.append("(");
        boolean first = true;
        for (Filter sub : filter.getChildren()) {
            if (first) {
                first = false;
            } else {
                out.append(" ").append(operator).append(" ");
            }
            sub.accept(this, extraData);
        }
        out.append(")");
        return this;
    }

    @Override
    public Object visit(Id filter, Object extraData) {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public Object visit(Not filter, Object extraData) {
        out.append("(NOT ");
        filter.getFilter().accept(this, extraData);
        out.append(")");
        return this;
    }

    @Override
    public Object visit(Or filter, Object extraData) {
        return visitBinaryLogic(filter, "OR", extraData);
    }

    @Override
    public Object visit(PropertyIsBetween filter, Object extraData) {
        out.append("(");
        filter.getExpression().accept(expressionVisitor, extraData);
        out.append(":[");
        filter.getLowerBoundary().accept(expressionVisitor, extraData);
        out.append(" TO ");
        filter.getUpperBoundary().accept(expressionVisitor, extraData);
        out.append("])");
        return this;
    }

    @Override
    public Object visit(PropertyIsEqualTo filter, Object extraData) {
        out.append("(");
        assert filter.getExpression1() instanceof PropertyName;
        filter.getExpression1().accept(expressionVisitor, extraData);
        out.append(":");
        assert filter.getExpression2() instanceof Literal;
        filter.getExpression2().accept(expressionVisitor, extraData);
        out.append(")");
        return this;
    }

    @Override
    public Object visit(PropertyIsNotEqualTo filter, Object extraData) {
        out.append("(-");
        assert filter.getExpression1() instanceof PropertyName;
        filter.getExpression1().accept(expressionVisitor, extraData);
        out.append(":");
        assert filter.getExpression2() instanceof Literal;
        filter.getExpression2().accept(expressionVisitor, extraData);
        out.append(")");
        return this;
    }

    public Object visitRange(BinaryComparisonOperator filter, String start, String end, Object extraData) {
        out.append("(");
        assert filter.getExpression1() instanceof PropertyName;
        filter.getExpression1().accept(expressionVisitor, extraData);
        out.append(":").append(start);
        assert filter.getExpression2() instanceof Literal;
        filter.getExpression2().accept(expressionVisitor, extraData);
        out.append(end).append(")");
        return this;
    }


    @Override
    public Object visit(PropertyIsGreaterThan filter, Object extraData) {
        return visitRange(filter, "{", " TO *]", extraData);
    }

    @Override
    public Object visit(PropertyIsGreaterThanOrEqualTo filter, Object extraData) {
        return visitRange(filter, "[", " TO *]", extraData);
    }

    @Override
    public Object visit(PropertyIsLessThan filter, Object extraData) {
        return visitRange(filter, "[* TO ", "}", extraData);
    }

    @Override
    public Object visit(PropertyIsLessThanOrEqualTo filter, Object extraData) {
        return visitRange(filter, "[* TO ", "]", extraData);
    }

    static final String SPECIAL_RE = "([" + Pattern.quote("+-&|!(){}[]^\\\"~*?:/") + "])";

    protected static String escapeLiteral(String text) {
        return text.replaceAll(SPECIAL_RE, "\\\\$1");
    }

    static final String SPECIAL_LIKE_RE = "(?<!\\\\)([" + Pattern.quote("+-&|!(){}[]^\"~:/") + "])";

    protected static String escapeLikeLiteral(String text) {
        return text.replaceAll(SPECIAL_LIKE_RE, "\\\\$1");
    }

    protected static String[] convertLikePattern(PropertyIsLike filter) {
        String result = filter.getLiteral();
        if (!filter.getWildCard().equals("*")) {
            final String wildcardRe = "(?<!" + Pattern.quote(filter.getEscape()) + ")" + Pattern.quote(filter.getWildCard());
            result = result.replaceAll(wildcardRe, "*");
        }
        if (!filter.getSingleChar().equals("?")) {
            final String singleCharRe = "(?<!" + Pattern.quote(filter.getEscape()) + ")" + Pattern.quote(filter.getSingleChar());
            result = result.replaceAll(singleCharRe, "?");
        }
        if (!filter.getEscape().equals("\\")) {
            final String escapeRe = Pattern.quote(filter.getEscape()) + "(.)";
            result = result.replaceAll(escapeRe, "\\\\$1");
        }
        return escapeLikeLiteral(result).split(" +");
    }

    @Override
    public Object visit(PropertyIsLike filter, Object extraData) {
        out.append("(");
        boolean first = true;
        for (String expression : convertLikePattern(filter)) {
            if (first) {
                first = false;
            } else {
                out.append(" ");
            }
            filter.getExpression().accept(expressionVisitor, extraData);
            out.append(":").append(expression);
        }
        out.append(")");
        return this;
    }

    @Override
    public Object visit(PropertyIsNull filter, Object extraData) {
        out.append("(-");
        filter.getExpression().accept(expressionVisitor, extraData);
        out.append(":[* TO *])");
        return this;
    }

    @Override
    public Object visit(PropertyIsNil filter, Object extraData) {
        out.append("(-");
        filter.getExpression().accept(expressionVisitor, extraData);
        out.append(":[* TO *])");
        return this;
    }

    @Override
    public Object visit(BBOX filter, Object extraData) {
        // Intersects matches only if the Polygon's border intersects with the BBOX's border.
        return addGeomFilter(filter, "Intersects", extraData);
    }

    private Object addGeomFilter(BinarySpatialOperator filter, String geoOperator, Object extraData) {
        //{!field f=geom}Intersects(ENVELOPE(minX, maxX, maxY, minY))
        out.append("(");
        if (filter.getExpression2() == null || filter.getExpression1() == null) {
            out.append("geom");
        } else {
            filter.getExpression1().accept(expressionVisitor, extraData);
        }
        out.append(":\"").append(geoOperator).append("(");
        final Expression geoExpression = filter.getExpression2() == null ?
            filter.getExpression1() : filter.getExpression2();
        geoExpression.accept(expressionVisitor, extraData);
        out.append(")\")");
        return this;
    }

    @Override
    public Object visit(Beyond filter, Object extraData) {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public Object visit(Contains filter, Object extraData) {
        return addGeomFilter(filter, "Contains", extraData);
    }

    @Override
    public Object visit(Crosses filter, Object extraData) {
        //best match...
        return addGeomFilter(filter, "Intersects", extraData);
    }

    @Override
    public Object visit(Disjoint filter, Object extraData) {
        out.append("(NOT ");
        addGeomFilter(filter, "Intersects", extraData);
        out.append(")");
        return this;
    }

    @Override
    public Object visit(DWithin filter, Object extraData) {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public Object visit(Equals filter, Object extraData) {
        out.append("(");
        filter.getExpression1().accept(expressionVisitor, extraData);
        out.append("==");
        filter.getExpression2().accept(expressionVisitor, extraData);
        out.append(")");
        return this;
    }

    @Override
    public Object visit(Intersects filter, Object extraData) {
        return addGeomFilter(filter, "Intersects", extraData);
    }

    @Override
    public Object visit(Overlaps filter, Object extraData) {
        //best match
        return addGeomFilter(filter, "Intersects", extraData);
    }

    @Override
    public Object visit(Touches filter, Object extraData) {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public Object visit(Within filter, Object extraData) {
        return addGeomFilter(filter, "IsWithin", extraData);
    }

    @Override
    public Object visit(After after, Object extraData) {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public Object visit(AnyInteracts anyInteracts, Object extraData) {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public Object visit(Before before, Object extraData) {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public Object visit(Begins begins, Object extraData) {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public Object visit(BegunBy begunBy, Object extraData) {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public Object visit(During during, Object extraData) {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public Object visit(EndedBy endedBy, Object extraData) {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public Object visit(Ends ends, Object extraData) {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public Object visit(Meets meets, Object extraData) {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public Object visit(MetBy metBy, Object extraData) {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public Object visit(OverlappedBy overlappedBy, Object extraData) {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public Object visit(TContains contains, Object extraData) {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public Object visit(TEquals equals, Object extraData) {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public Object visit(TOverlaps contains, Object extraData) {
        //TODO
        throw new NotImplementedException();
    }
}
