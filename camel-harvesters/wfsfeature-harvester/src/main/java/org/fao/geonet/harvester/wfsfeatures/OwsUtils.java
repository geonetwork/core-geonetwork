package org.fao.geonet.harvester.wfsfeatures;

import org.apache.http.client.utils.URIBuilder;
import org.opengis.feature.type.AttributeDescriptor;

import java.util.Date;

/**
 * Created by fgravin on 11/5/15.
 */

public class OwsUtils {

    public String getDescribeFeatureTypeUrl(final String wfsUrl, final String featureType) throws Exception {
        URIBuilder builder = new URIBuilder(wfsUrl);
        builder.addParameter("request", "DescribeFeatureType");
        builder.addParameter("service", "WFS");
        builder.addParameter("version", "1.0.0");
        builder.addParameter("TYPENAME", featureType);

        String url = builder.build().toURL().toString();
        return url;
    }

    public static String getGetCapabilitiesUrl(final String wfsUrl) throws Exception {
        URIBuilder builder = new URIBuilder(wfsUrl);
        builder.addParameter("request", "getCapabilities");
        builder.addParameter("service", "WFS");
        builder.addParameter("version", "1.0.0");

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
                Date.class)) {
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
