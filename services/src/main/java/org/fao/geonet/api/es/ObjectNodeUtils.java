/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.fao.geonet.api.es;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ObjectNodeUtils {
    public final static String SOURCE_NODE = "_source";

    private ObjectNodeUtils() {
        // Don't allow to instantiate it
    }

    public static String getSourceString(ObjectNode node, String name) {
        if (!node.has(SOURCE_NODE)) {
            return null;
        }
        final JsonNode sub = node.get(SOURCE_NODE).get(name);
        return sub != null ? sub.asText() : null;
    }

    public static Integer getSourceInteger(ObjectNode node, String name) {
        if (!node.has(SOURCE_NODE)) {
            return null;
        }
        final JsonNode sub = node.get(SOURCE_NODE).get(name);
        return sub != null ? sub.asInt() : null;
    }

    public static ObjectNode getSourceNode(ObjectNode doc) {
        if (!doc.has(ObjectNodeUtils.SOURCE_NODE)) {
            return null;
        }

        return (ObjectNode) doc.get(SOURCE_NODE);
    }
}
