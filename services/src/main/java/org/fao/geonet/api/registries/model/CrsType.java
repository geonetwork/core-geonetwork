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

package org.fao.geonet.api.registries.model;

/**
 * CRS type as categorized in GeoTools
 */
public enum CrsType {
    CoordinateReferenceSystem(org.geotools.api.referencing.crs.CoordinateReferenceSystem.class),
    VerticalCRS(org.geotools.api.referencing.crs.VerticalCRS.class),
    GeographicCRS(org.geotools.api.referencing.crs.GeographicCRS.class),
    ProjectedCRS(org.geotools.api.referencing.crs.ProjectedCRS.class),
    ;

    private Class clazz;

    CrsType() {
    }

    CrsType(Class clazz) {
        this.clazz = clazz;
    }

    public static CrsType find(String type) {
        for (CrsType t : values()) {
            if (type.equalsIgnoreCase(t.toString())) {
                return t;
            }
        }
        return null;
    }

    public Class getClazz() {
        return clazz;
    }
}
