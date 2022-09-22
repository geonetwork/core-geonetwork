/*
 * Copyright (C) 2001-2022 Food and Agriculture Organization of the
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

import java.util.ArrayList;
import java.util.List;

/**
 * Class to model a JSON paths to filter the elements to process with JsonStreamUtils methods.
 *
 * Examples:
 *
 *  1) ES search response, to filter the response hits to process:
 *
 *  hits
 *    hits
 *
 *  2) ES multiple search response, to filter several children elements to process (response hits and aggregation hits)
 *
 *  responses
 *    hits
 *      hits
 *    aggregations
 *      *   (special value used to process all children of an element)
 *        buckets
 *          docs
 *            hits
 *
 */
public class JsonPathItem {
    String name;
    List<JsonPathItem> subitems = new ArrayList<>();

    JsonPathItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<JsonPathItem> getSubitems() {
        return subitems;
    }

    public void setSubitems(List<JsonPathItem> subitems) {
        this.subitems = subitems;
    }

    JsonPathItem getSubitem(String name) {
        for(JsonPathItem s : this.subitems) {
            if (s.getName().equals(name)) {
                return s;
            }
        }

        return null;
    }

    JsonPathItem addSubitem(String name) {
        this.subitems.add(create(name));
        return this;
    }

    static JsonPathItem create(String name) {
        return new JsonPathItem(name);
    }
}
