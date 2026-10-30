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

package org.fao.geonet;

import com.google.common.collect.ImmutableSet;

import java.io.Serializable;

/**
 * Encapsulates information about the current node such as the node id.
 * <p>
 * User: Jesse Date: 11/27/13 Time: 2:35 PM
 */
public class NodeInfo implements Serializable {
    public static final String DEFAULT_NODE = "srv";

    /**
     * The webapp based folder can not be used as a portal identifier.
     */
    public static ImmutableSet<String> EXCLUDED_NODE_IDS;

    static {
        EXCLUDED_NODE_IDS = ImmutableSet.<String>builder()
            .add("catalog")
            .add("conversion")
            .add("doc")
            .add("htmlCache")
            .add("images")
            .add("loc")
            .add("resources")
            .add("xml")
            .add("xsl")
            .add("xslt")
            .add("accessDenied.jsp")
            .add("service-not-allowed")
            .build();
    }

    private String id = "srv";
    private boolean defaultNode = true;
    /**
     * Read-only mode applies to the whole webapp (see DB heartbeat), not to a portal. NodeInfo is
     * request scoped, so the flag is static to be shared by all requests and background threads.
     */
    private static volatile boolean readOnly = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDefaultNode() {
        return defaultNode;
    }

    public void setDefaultNode(boolean defaultNode) {
        this.defaultNode = defaultNode;
    }

    public static boolean isReadOnly() {
        return readOnly;
    }

    public static void setReadOnly(boolean readOnly) {
        NodeInfo.readOnly = readOnly;
    }
}
