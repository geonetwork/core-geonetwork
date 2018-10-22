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

package org.fao.geonet.api.records.formatters;

import java.util.Arrays;
import java.util.List;

/**
 * Enumerates the support output types.
 *
 * @author Jesse on 10/26/2014.
 */
public enum FormatType {
    txt("text/plain"),
    html("text/html"),
    xml("application/xml"),
    json("application/json"),
    jsonld("application/vnd.schemaorg.ld+json"),
    pdf("application/pdf"),
    testpdf("application/test-pdf");
    public final String contentType;

    private FormatType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Find the best format matching the list of Accept header.
     * If not found, return null
     */
    public static FormatType find(String acceptHeader) {
        if (acceptHeader != null) {
            List<String> accept = Arrays.asList(acceptHeader.toLowerCase().split(","));
            for (String h : accept) {
                for (FormatType c : values()) {
                    if (h.startsWith(c.contentType)) {
                        return c;
                    }
                }
            }
        }
        return null;
    }
}
