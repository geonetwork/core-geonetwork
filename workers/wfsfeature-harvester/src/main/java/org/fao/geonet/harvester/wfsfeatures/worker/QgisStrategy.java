/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2013, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.fao.geonet.harvester.wfsfeatures.worker;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.geotools.data.wfs.v1_1_0.DefaultWFSStrategy;
import org.geotools.xml.Binding;
import org.geotools.xs.XS;
import org.geotools.xs.bindings.XSDoubleBinding;
import org.geotools.xs.bindings.XSIntegerBinding;
import org.geotools.xs.bindings.XSStringBinding;

/**
 * Implements some peculiar behavior of QGIS with WFS 1.1.0.
 *
 */
public class QgisStrategy extends DefaultWFSStrategy {

    @Override
    public String getPrefixedTypeName(QName typeName) {
        // no prefix if prefix is empty or wfs
        return ("".equals(typeName.getPrefix()) || "wfs"
            .equals(typeName.getPrefix())) ? typeName.getLocalPart() : typeName
            .getPrefix() + ":" + typeName.getLocalPart();
    }


    @Override
    public Map<String, String> getNamespaceURIMappings() {
        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("http://www.opengis.net/wfs", "http://www.qgis.org/gml");
        return mappings;
    }

    @Override
    public boolean canIgnoreMissingElementDeclaration() {
        return true;
    }
}
