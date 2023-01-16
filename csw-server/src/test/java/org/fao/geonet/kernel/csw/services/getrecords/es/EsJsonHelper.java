/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

/**
 * Tools to build up ElasticSearch JSON structures via Jackson.
 *
 * @author bhoefling
 *
 */
public class EsJsonHelper {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Returns a structure like
     *
     * <pre>
     *  { "match":
     *    {
     *      "property": "matchString"
     *    }
     * </pre>
     *
     * @param property
     * @param matchString
     * @return
     */
    public static ObjectNode match(String property, String matchString) {
        final ObjectNode matchObject = MAPPER.createObjectNode();
        matchObject.put(property, matchString);

        final ObjectNode outer = MAPPER.createObjectNode();
        outer.set("match", matchObject);
        return outer;
    }

    private static ArrayNode bound(double x, double y) {
        final ArrayNode bound = MAPPER.createArrayNode();
        bound.add(x);
        bound.add(y);
        return bound;
    }

    /**
     * An Envelope.
     *
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @return
     */
    public static ObjectNode envelope(double x0, double y0, double x1, double y1) {
        final ArrayNode bbox = MAPPER.createArrayNode();
        bbox.add(bound(x0, y0));
        bbox.add(bound(x1, y1));

        return shape("envelope", bbox);
    }

    /**
     * Creates a "geo_shape" node.
     *
     * @param property The name of the index' property where the geometry search
     *                 should take place.
     * @param geometry A geometry.
     * @param relation For example "intersects".
     * @return
     */
    public static ObjectNode geoShape(String property, ObjectNode geometry, String relation) {
        final ObjectNode geoShape = MAPPER.createObjectNode();
        geoShape.set("shape", geometry);
        geoShape.put("relation", relation);

        final ObjectNode propertyNode = MAPPER.createObjectNode();
        propertyNode.set(property, geoShape);

        final ObjectNode outer = MAPPER.createObjectNode();
        outer.set("geo_shape", propertyNode);
        return outer;

    }

    /**
     * Crate a "shape" object.<br>
     *
     * It is the responsibility of the caller that type and coordinates fit
     * together.
     *
     * @param shapeType   For example "envelope" or "point".
     * @param coordinates
     * @return
     */
    public static ObjectNode shape(String shapeType, ArrayNode coordinates) {
        final ObjectNode shape = MAPPER.createObjectNode();

        shape.put("type", shapeType);
        shape.set("coordinates", coordinates);

        return shape;
    }

    /**
     * Creates a "bool" node with only a "must" clause.
     *
     * @param must
     * @return
     */
    public static ObjectNode bool(JsonNode must) {
        final ObjectNode boolNode = MAPPER.createObjectNode();
        boolNode.set("must", must);

        final ObjectNode outerNode = MAPPER.createObjectNode();
        outerNode.set("bool", boolNode);
        return outerNode;
    }

    /**
     * Creates a "bool" node with "must", "filter", "should", "must_not" clauses.
     * Only non-null parameters will trigger the corresponding clause:
     * <pre>
     *     {
     *         "bool": {
     *             "must": "...",
     *             "filter": "...",
     *             "should": "...",
     *             "must_no": "..."
     *         }
     *     }
     * </pre>
     */
    public static ObjectNode bool(JsonNode must, JsonNode filter, JsonNode should, JsonNode mustNot) {
        final ObjectNode boolNode = MAPPER.createObjectNode();
        if (must != null) {
            boolNode.set("must", must);
        }

        if (filter != null) {
            boolNode.set("filter", filter);
        }

        if (should != null) {
            boolNode.set("should", should);
        }

        if (mustNot != null) {
            boolNode.set("must_not", mustNot);
        }

        final ObjectNode outerNode = MAPPER.createObjectNode();
        outerNode.set("bool", boolNode);
        return outerNode;
    }

    /**
     *
     * @return a new BoolBdr
     */
    public static BoolBdr boolbdr() {
        return new BoolBdr();
    }

    /**
     * Factory Method for "bool" structures.
     *
     * @author bhoefling
     *
     */
    public static class BoolBdr {
        JsonNode must = null;
        JsonNode filter = null;
        JsonNode should = null;
        JsonNode mustNot = null;

        public ObjectNode bld() {
            return bool(this.must, this.filter, this.should, this.mustNot);
        }

        public BoolBdr must(JsonNode must) {
            this.must = must;
            return this;
        }

        /** Set the filter.
         *
         * @param filter the filter.
         * @return this instance of BoolBdr.
         */
        public BoolBdr filter(JsonNode filter) {
            this.filter = filter;
            return this;
        }

        public BoolBdr should(JsonNode should) {
            this.should = should;
            return this;
        }

        public BoolBdr mustNot(JsonNode mustNot) {
            this.mustNot = mustNot;
            return this;
        }

    }

    /** Creates an {@link com.fasterxml.jackson.databind.node.ArrayNode} with the list of itemp passed.
     *
     * @param items elements of the new array.
     * @return an ArrayNode with the items passed.
     */
    public static ArrayNode array(ObjectNode... items) {
        final ArrayNode arrayNode = MAPPER.createArrayNode();

        for (final ObjectNode node : items) {
            arrayNode.add(node);
        }
        return arrayNode;
    }

    public static ArrayNode array(List<ObjectNode> items) {
        final ArrayNode boolNode = MAPPER.createArrayNode();
        return boolNode.addAll(items);
    }
}
