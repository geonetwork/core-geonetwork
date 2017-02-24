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
package org.fao.geonet.arcgis;


/**
 * Factory class to retrieve db model depending on the ArcSDE version.
 *
 * @author Jose García
 */
public class ArcSDEVersionFactory {

    public String getTableName(String arcSdeVersion) {
        if (arcSdeVersion.equals("8")) {
            return "GDB_USERMETADATA";
        } else {
            return "GDB_ITEMS";
        }
    }

    public String getMetadataColumnName(String arcSdeVersion) {
        if (arcSdeVersion.equals("8")) {
            return "XML";
        } else {
            return "documentation";
        }
    }
}
