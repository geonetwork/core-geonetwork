/*
 * Copyright (C) 2001-2015 Food and Agriculture Organization of the
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

package org.fao.geonet.harvester.wfsfeatures.worker;

import org.apache.http.client.utils.URIBuilder;
import org.opengis.feature.type.AttributeDescriptor;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by fgravin on 11/5/15.
 */

public class OwsUtils {

    public String getDescribeFeatureTypeUrl(final String wfsUrl, final String featureType, final String version) throws Exception {
        URIBuilder builder = new URIBuilder(wfsUrl);
        builder.addParameter("request", "DescribeFeatureType");
        builder.addParameter("service", "WFS");
        builder.addParameter("version", version);
        builder.addParameter("TYPENAME", featureType);

        String url = builder.build().toURL().toString();
        return url;
    }

    public static String getGetCapabilitiesUrl(final String wfsUrl, final String version) throws Exception {
        URIBuilder builder = new URIBuilder(wfsUrl);
        builder.addParameter("request", "GetCapabilities");
        builder.addParameter("service", "WFS");
        builder.addParameter("version", version);

        String url = builder.build().toURL().toString();
        return url;
    }

    public static String getTypeFromFeatureType(final AttributeDescriptor descriptor) {
        String type;
        if (descriptor.getType().getBinding().isAssignableFrom(String.class)) {
            type = "string";
        } else if (descriptor.getType().getBinding().isAssignableFrom(
                Double.class)) {
            type = "double";
        } else if (descriptor.getType().getBinding().isAssignableFrom(
                Integer.class)) {
            type = "integer";
        } else if (descriptor.getType().getBinding().isAssignableFrom(
                Float.class)) {
            type = "double";
        } else if (descriptor.getType().getBinding().isAssignableFrom(
                Date.class) ||
                   descriptor.getType().getBinding().isAssignableFrom(
               Timestamp.class)) {
            type = "date";
        } else if (descriptor.getType().getBinding().isAssignableFrom(
                Long.class)) {
            type = "integer";
        } else if (descriptor.getType().getBinding().isAssignableFrom(
                Short.class)) {
            type = "integer";
        } else if (descriptor.getType().getBinding().getSuperclass().isAssignableFrom(
                        com.vividsolutions.jts.geom.Geometry.class)
                || descriptor.getType().getBinding().getSuperclass().isAssignableFrom(
                        com.vividsolutions.jts.geom.GeometryCollection.class)
                || descriptor.getType().getBinding().isAssignableFrom(
                        com.vividsolutions.jts.geom.Geometry.class)
                || descriptor.getType().getBinding().isAssignableFrom(
                        com.vividsolutions.jts.geom.GeometryCollection.class)) {
            type = "geometry";
        } else {
            type = "string";
        }
        return type;
    }
}
