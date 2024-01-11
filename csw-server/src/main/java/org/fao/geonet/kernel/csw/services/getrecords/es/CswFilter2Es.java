/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.csw.services.getrecords.es;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.csw.services.getrecords.IFieldMapper;
import org.fao.geonet.utils.Log;
import org.geotools.filter.visitor.AbstractFilterVisitor;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;
import org.opengis.filter.*;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.*;
import org.opengis.filter.temporal.*;
import org.opengis.geometry.BoundingBox;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Manages the translation from CSW &lt;Filter&gt; into an ES query.
 */
public class CswFilter2Es extends AbstractFilterVisitor {
    private static final String BINARY_OPERATOR_AND = "AND";
    private static final String BINARY_OPERATOR_OR = "OR";

    private static final String SPECIAL_RE = "([" + Pattern.quote("+-&|!(){}[]^\\\"~*?:/") + "])";
    private static final String SPECIAL_LIKE_RE = "(?<!\\\\)([" + Pattern.quote("+-&|!(){}[]^\"~:/") + "])";
    private static final String TEMPLATE_NOT = " {\"bool\": {\n" +
        "            \"must_not\": [\n" +
        "             %s\n" +
        "            ]\n" +
        "          }}";
    private static final String TEMPLATE_AND = " {\"bool\": {\n" +
        "            \"must\": [\n" +
        "             %s\n" +
        "            ]\n" +
        "          }}";
    private static final String TEMPLATE_AND_WITH_FILTER = " \"bool\": {\n" +
        "            \"must\": [\n" +
        "             %s\n" +
        "            ]\n" +
        "          ,\"filter\":{\"query_string\":{\"query\":\"%s\"}}}"; //, "minimum_should_match" : 1
    private static final String TEMPLATE_OR = " {\"bool\": {\n" +
        "            \"should\": [\n" +
        "             %s\n" +
        "            ]\n" +
        "          }}";
    private static final String TEMPLATE_OR_WITH_FILTER = " \"bool\": {\n" +
        "            \"should\": [\n" +
        "             %s\n" +
        "            ]\n" +
        "          ,\"filter\":{\"query_string\":{\"query\":\"%s\"}}, \"minimum_should_match\" : 1}";
    private static final String TEMPLATE_MATCH = "{\"query_string\": {\n" +
        "        \"fields\": [\"%s\"],\n" +
        "        \"query\": \"%s\"\n" +
        "    }}";
    private static final String TEMPLATE_PROPERTY_IS_NOT = " {\"bool\": {\n" +
        "            \"must_not\": " + TEMPLATE_MATCH +
        "          }}";
    private static final String TEMPLATE_RANGE = " {\n" +
        "        \"range\" : {\n" +
        "            \"%s\" : {\n" +
        "                \"%s\" : %s\n" +
        "            }\n" +
        "        }\n" +
        "    }";
    private static final String TEMPLATE_BETWEEN = " {\n" +
        "        \"range\" : {\n" +
        "            \"%s\" : {\n" +
        "                \"gte\" : %s,\n" +
        "                \"lte\" : %s\n" +
        "            }\n" +
        "        }\n" +
        "    }";
    private static final String TEMPLATE_IS_LIKE = "{\"query_string\": {\n" +
        "        \"fields\": [\"%s\"],\n" +
        "        \"query\": \"%s\"\n" +
        "    }}";
    private static final String TEMPLATE_SPATIAL = "{ \"geo_shape\": {\"geom\": {\n" +
        "                        \t\"shape\": {\n" +
        "                            \t\"type\": \"%s\",\n" +
        "                            \t\"coordinates\" : %s\n" +
        "                        \t},\n" +
        "                        \t\"relation\": \"%s\"\n" +
        "                    \t}}}";
    private final StringBuilder outQueryString = new StringBuilder();
    private final Expression2CswVisitor expressionVisitor;
    // Stack to build the Elasticsearch Query
    Deque<String> stack = new ArrayDeque<>();
    private boolean useFilter = true;

    public CswFilter2Es(IFieldMapper fieldMapper) {
        expressionVisitor = new Expression2CswVisitor(stack, fieldMapper);
    }

    public static String translate(Filter filter, IFieldMapper fieldMapper) {
        CswFilter2Es translator = new CswFilter2Es(fieldMapper);

        if (filter != null) {
            filter.accept(translator, translator);
        }

        return translator.getFilter();
    }

    protected static String escapeLiteral(String text) {
        return text.replaceAll(SPECIAL_RE, "\\\\$1");
    }

    protected static String quoteString(String text) {
        return String.format("\"%s\"", text);
    }

    protected static String escapeLikeLiteral(String text) {
        return text.replaceAll(SPECIAL_LIKE_RE, "\\\\$1");
    }

    protected static String convertLikePattern(PropertyIsLike filter) {
        String result = filter.getLiteral();
        if (!filter.getWildCard().equals("*")) {
            final String wildcardRe =
                StringUtils.isNotEmpty(filter.getEscape())
                    ? Pattern.quote(filter.getEscape() + filter.getWildCard())
                    : filter.getWildCard();
            result = result.replaceAll(wildcardRe, "*");
        }
        if (!filter.getSingleChar().equals("?")) {
            final String singleCharRe =
                StringUtils.isNotEmpty(filter.getEscape())
                    ? Pattern.quote(filter.getEscape() + filter.getSingleChar())
                    : filter.getSingleChar();
            result = result.replaceAll(singleCharRe, "?");
        }

        result = StringEscapeUtils.escapeJson(escapeLikeLiteral(result));
        return result;
    }

    public String getFilter() {
        String condition = stack.isEmpty() ? "" : stack.pop();
        // Check for single condition (no binary operators to wrap the query
        if (!condition.startsWith(" \"bool\":")) {
            condition = String.format(TEMPLATE_AND_WITH_FILTER, condition, "%s");
        }

        if (StringUtils.isEmpty(condition)) {
            // No filter
            condition = "{\"bool\":{\"must\":[{\"query_string\":{\"query\":\"*\"}}],\"filter\":{\"query_string\":{\"query\":\"%s\"}}}";
        } else {
            // Add wrapper
            condition = "{" + condition + "}";
        }

        outQueryString.append(condition);

        return outQueryString.toString();
    }

    @Override
    public Object visit(And filter, Object extraData) {
        return visitBinaryLogic(filter, BINARY_OPERATOR_AND, extraData);
    }

    private Object visitBinaryLogic(BinaryLogicOperator filter, String operator, Object extraData) {
        String filterCondition;

        if (operator.equals(BINARY_OPERATOR_AND)) {
            filterCondition = (useFilter ? TEMPLATE_AND_WITH_FILTER : TEMPLATE_AND);
        } else if (operator.equals(BINARY_OPERATOR_OR)) {
            filterCondition = (useFilter ? TEMPLATE_OR_WITH_FILTER : TEMPLATE_OR);
        } else {
            throw new NotImplementedException();
        }

        if (useFilter) {
            useFilter = false;
        }

        // Visit children adding the conditions to the stack
        for (Filter sub : filter.getChildren()) {
            sub.accept(this, extraData);
        }

        // Process the stack entries for the processed children elements to calculate the filter condition
        final int n = filter.getChildren().size();

        final List<String> conditionList = new ArrayList<>(n);
        for (int i = n; i > 0; i--) {
            conditionList.add(stack.pop());
        }

        int count = StringUtils.countMatches(filterCondition, "%s");

        if (count == 1) {
            filterCondition = String.format(filterCondition, String.join(",", conditionList));

        } else {
            filterCondition = String.format(filterCondition, String.join(",", conditionList), "%s");
        }

        stack.push(filterCondition);

        return this;
    }

    @Override
    public Object visit(Id filter, Object extraData) {
        //TODO
        throw new NotImplementedException();
    }

    @Override
    public Object visit(Not filter, Object extraData) {
        String filterNot = TEMPLATE_NOT;

        filter.getFilter().accept(this, extraData);

        filterNot = String.format(filterNot, stack.pop());
        stack.push(filterNot);

        return this;
    }

    @Override
    public Object visit(Or filter, Object extraData) {
        return visitBinaryLogic(filter, BINARY_OPERATOR_OR, extraData);
    }

    @Override
    public Object visit(PropertyIsBetween filter, Object extraData) {
        String filterBetween = TEMPLATE_BETWEEN;

        if (!(filter.getExpression() instanceof PropertyName)) {
            throw new IllegalArgumentException("Invalid expression property provided");
        }

        if (!(filter.getLowerBoundary() instanceof Literal)) {
            throw new IllegalArgumentException("Invalid expression lower boundary literal provided");
        }

        if (!(filter.getUpperBoundary() instanceof Literal)) {
            throw new IllegalArgumentException("Invalid expression upper boundary literal provided");
        }

        filter.getExpression().accept(expressionVisitor, extraData);
        filter.getLowerBoundary().accept(expressionVisitor, extraData);
        filter.getUpperBoundary().accept(expressionVisitor, extraData);

        String dataPropertyUpperValue = stack.pop();
        if (!NumberUtils.isNumber(dataPropertyUpperValue)) {
            dataPropertyUpperValue = StringEscapeUtils.escapeJson(CswFilter2Es.quoteString(dataPropertyUpperValue));
        }

        String dataPropertyLowerValue = stack.pop();
        if (!NumberUtils.isNumber(dataPropertyLowerValue)) {
            dataPropertyLowerValue = StringEscapeUtils.escapeJson(CswFilter2Es.quoteString(dataPropertyLowerValue));
        }

        String dataPropertyName = stack.pop();

        filterBetween = String.format(filterBetween, dataPropertyName, dataPropertyLowerValue, dataPropertyUpperValue);
        stack.push(filterBetween);

        return this;
    }

    @Override
    public Object visit(PropertyIsEqualTo filter, Object extraData) {
        checkFilterExpressionsInBinaryComparisonOperator(filter);

        filter.getExpression1().accept(expressionVisitor, extraData);
        filter.getExpression2().accept(expressionVisitor, extraData);

        String dataPropertyValue = stack.pop();
        String dataPropertyName = stack.pop();

        final String filterEqualTo = String.format(TEMPLATE_MATCH, dataPropertyName, StringEscapeUtils.escapeJson(escapeLiteral(dataPropertyValue)));
        stack.push(filterEqualTo);

        return this;
    }

    @Override
    public Object visit(PropertyIsNotEqualTo filter, Object extraData) {
        String filterPropertyIsNot = TEMPLATE_PROPERTY_IS_NOT;

        checkFilterExpressionsInBinaryComparisonOperator(filter);

        filter.getExpression1().accept(expressionVisitor, extraData);
        filter.getExpression2().accept(expressionVisitor, extraData);

        String dataPropertyValue = stack.pop();
        String dataPropertyName = stack.pop();

        filterPropertyIsNot = String.format(filterPropertyIsNot, dataPropertyName,
            StringEscapeUtils.escapeJson(escapeLiteral(dataPropertyValue)));
        stack.push(filterPropertyIsNot);

        return this;
    }

    public Object visitRange(BinaryComparisonOperator filter, String operator, Object extraData) {
        String filterRange = TEMPLATE_RANGE;

        checkFilterExpressionsInBinaryComparisonOperator(filter);

        filter.getExpression1().accept(expressionVisitor, extraData);
        filter.getExpression2().accept(expressionVisitor, extraData);

        String dataPropertyValue = stack.pop();
        String dataPropertyName = stack.pop();

        if (!NumberUtils.isNumber(dataPropertyValue)) {
            dataPropertyValue = StringEscapeUtils.escapeJson(CswFilter2Es.quoteString(dataPropertyValue));
        }

        filterRange = String.format(filterRange, dataPropertyName, operator, dataPropertyValue);
        stack.push(filterRange);

        return this;
    }

    @Override
    public Object visit(PropertyIsGreaterThan filter, Object extraData) {
        return visitRange(filter, "gt", extraData);
    }

    @Override
    public Object visit(PropertyIsGreaterThanOrEqualTo filter, Object extraData) {
        return visitRange(filter, "gte", extraData);
    }

    @Override
    public Object visit(PropertyIsLessThan filter, Object extraData) {
        return visitRange(filter, "lt", extraData);
    }

    @Override
    public Object visit(PropertyIsLessThanOrEqualTo filter, Object extraData) {
        return visitRange(filter, "lte", extraData);
    }

    @Override
    public Object visit(PropertyIsLike filter, Object extraData) {
        String filterIsLike = TEMPLATE_IS_LIKE;

        String expression = convertLikePattern(filter);

        filter.getExpression().accept(expressionVisitor, extraData);

        filterIsLike = String.format(filterIsLike, stack.pop(), expression);

        stack.push(filterIsLike);

        return this;
    }

    @Override
    public Object visit(PropertyIsNull filter, Object extraData) {
        outQueryString.append("(-");
        filter.getExpression().accept(expressionVisitor, extraData);
        outQueryString.append(":[* TO *])");
        return this;
    }

    @Override
    public Object visit(PropertyIsNil filter, Object extraData) {
        outQueryString.append("(-");
        filter.getExpression().accept(expressionVisitor, extraData);
        outQueryString.append(":[* TO *])");
        return this;
    }

    /**
     * Fills out the templateSpatial.
     *
     * @param shapeType For example "bbox" or "polygon".
     * @param coords    The coordinates in the form needed by shapeType.
     * @param relation  Spatial operation, like "intersects".
     * @return
     */
    private String fillTemplateSpatial(String shapeType, String coords, String relation) {
        return String.format(TEMPLATE_SPATIAL, shapeType, coords, relation);
    }

    @Override
    public Object visit(BBOX filter, Object extraData) {

        final BoundingBox bbox = filter.getBounds();

        final double x0 = bbox.getMinX();
        final double x1 = bbox.getMaxX();
        final double y0 = bbox.getMinY();
        final double y1 = bbox.getMaxY();

        // Specify Locale.US to make Java use dot as decimal separators
        final String coordsValue = String.format(Locale.US,
            "[[%f, %f], [%f, %f]]", x0, y1, x1, y0);

        final String filterSpatial = fillTemplateSpatial("envelope", coordsValue, "intersects");
        stack.push(filterSpatial);
        return this;
    }

    private Object addGeomFilter(BinarySpatialOperator filter, String geoOperator, Object extraData) {
        if (!(filter.getExpression2() == null || filter.getExpression1() == null)) {
            filter.getExpression1().accept(expressionVisitor, extraData);
        }

        final Expression geoExpression = filter.getExpression2() == null ? filter.getExpression1()
            : filter.getExpression2();
        geoExpression.accept(expressionVisitor, extraData);

        String geom = stack.pop();
        // Extract field name
        stack.pop();

        final String filterSpatial;

        WKTReader reader = new WKTReader();
        try {
            Geometry geometryJts = reader.read(geom);

            if (geometryJts instanceof Polygon) {
                Polygon polygonGeom = (Polygon) geometryJts;

                String coordinatesText = buildCoordinatesString(polygonGeom.getCoordinates());

                filterSpatial = fillTemplateSpatial("polygon", String.format("[[%s]]", coordinatesText), geoOperator);

            } else if (geometryJts instanceof Point) {
                Point pointGeom = (Point) geometryJts;

                // Use Locale.US to make java use the dot "." as decimal separator.
                String coordsValue = String.format(Locale.US, "[%f, %f]", pointGeom.getX(), pointGeom.getY());
                filterSpatial = fillTemplateSpatial("point", coordsValue, geoOperator);

            } else if (geometryJts instanceof LineString) {
                LineString lineStringGeom = (LineString) geometryJts;

                String coordinatesText = buildCoordinatesString(lineStringGeom.getCoordinates());

                filterSpatial = fillTemplateSpatial("linestring", String.format("[%s]", coordinatesText), geoOperator);
            } else {
                filterSpatial = null;
            }

            stack.push(filterSpatial);
        } catch (Exception ex) {
            Log.error(Geonet.CSW, "Error parsing geospatial object", ex);
            throw new IllegalArgumentException("Invalid expression for spatial filter", ex);
        }

        return this;
    }

    @Override
    public Object visit(Beyond filter, Object extraData) {
        throw new NotImplementedException();
    }

    @Override
    public Object visit(Contains filter, Object extraData) {
        return addGeomFilter(filter, "contains", extraData);
    }

    @Override
    public Object visit(Crosses filter, Object extraData) {
        //best match...
        return addGeomFilter(filter, "intersects", extraData);
    }

    @Override
    public Object visit(Disjoint filter, Object extraData) {
        return addGeomFilter(filter, "disjoint", extraData);
    }

    @Override
    public Object visit(DWithin filter, Object extraData) {
        throw new NotImplementedException();
    }

    @Override
    public Object visit(Equals filter, Object extraData) {
        throw new NotImplementedException();
    }

    @Override
    public Object visit(Intersects filter, Object extraData) {
        return addGeomFilter(filter, "intersects", extraData);
    }

    @Override
    public Object visit(Overlaps filter, Object extraData) {
        //best match
        return addGeomFilter(filter, "intersects", extraData);
    }

    @Override
    public Object visit(Touches filter, Object extraData) {
        throw new NotImplementedException();
    }

    @Override
    public Object visit(Within filter, Object extraData) {
        return addGeomFilter(filter, "within", extraData);
    }

    @Override
    public Object visit(After after, Object extraData) {
        throw new NotImplementedException();
    }

    @Override
    public Object visit(AnyInteracts anyInteracts, Object extraData) {
        throw new NotImplementedException();
    }

    @Override
    public Object visit(Before before, Object extraData) {
        throw new NotImplementedException();
    }

    @Override
    public Object visit(Begins begins, Object extraData) {
        throw new NotImplementedException();
    }

    @Override
    public Object visit(BegunBy begunBy, Object extraData) {
        throw new NotImplementedException();
    }

    @Override
    public Object visit(During during, Object extraData) {
        throw new NotImplementedException();
    }

    @Override
    public Object visit(EndedBy endedBy, Object extraData) {
        throw new NotImplementedException();
    }

    @Override
    public Object visit(Ends ends, Object extraData) {
        throw new NotImplementedException();
    }

    @Override
    public Object visit(Meets meets, Object extraData) {
        throw new NotImplementedException();
    }

    @Override
    public Object visit(MetBy metBy, Object extraData) {
        throw new NotImplementedException();
    }

    @Override
    public Object visit(OverlappedBy overlappedBy, Object extraData) {
        throw new NotImplementedException();
    }

    @Override
    public Object visit(TContains contains, Object extraData) {
        throw new NotImplementedException();
    }

    @Override
    public Object visit(TEquals equals, Object extraData) {
        throw new NotImplementedException();
    }

    @Override
    public Object visit(TOverlaps contains, Object extraData) {
        throw new NotImplementedException();
    }


    private String buildCoordinatesString(Coordinate[] coordinates) {
        List<String> coordinatesList = new ArrayList<>();

        for (Coordinate c : coordinates) {
            // Use Locale.US to make Java use dot "." as decimal separator
            String coordsValue = String.format(Locale.US, "[%f, %f] ",
                c.getX(), c.getY());

            coordinatesList.add(coordsValue);
        }

        return String.join(" , ", coordinatesList);
    }

    private void checkFilterExpressionsInBinaryComparisonOperator(BinaryComparisonOperator filter) {
        if (!(filter.getExpression1() instanceof PropertyName)) {
            throw new IllegalArgumentException("Invalid expression property provided");
        }

        if (!(filter.getExpression2() instanceof Literal)) {
            throw new IllegalArgumentException("Invalid expression literal provided");
        }
    }
}
