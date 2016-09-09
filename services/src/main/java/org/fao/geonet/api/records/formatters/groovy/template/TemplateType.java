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

package org.fao.geonet.api.records.formatters.groovy.template;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents the template types supported byt the template parser.
 *
 * @author Jesse on 11/30/2014.
 */
public enum TemplateType {
    XML, TEXT;

    public static TemplateType fromContentType(String type) {
        if (type.contains("xml") || type.contains("html") || type.contains("xsd") || type.contains("xsl")) {
            return XML;
        } else {
            return TEXT;
        }
    }

    public static TemplateType fromPath(Path path) throws IOException {
        String type = Files.probeContentType(path);
        if (type == null) {
            String file = path.getFileName().toString();
            if (file.endsWith(".xsd") ||
                file.endsWith(".html") ||
                file.endsWith(".xml")) {
                return XML;
            } else {
                return TEXT;
            }
        } else {
            return fromContentType(Files.probeContentType(path));
        }
    }
}
