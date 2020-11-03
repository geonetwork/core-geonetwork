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

import net.opengis.wfs.FeatureTypeType;
import org.geotools.data.wfs.internal.v1_x.StrictWFS_1_x_Strategy;

import javax.xml.namespace.QName;

/**
 * Implements some peculiar behavior of QGIS with WFS 1.1.0.
 *
 */
public class QgisStrategy extends StrictWFS_1_x_Strategy {

    @Override
    public String getPrefixedTypeName(QName typeName) {
        // no prefix if prefix is empty or wfs
        return ("".equals(typeName.getPrefix()) || "wfs"
            .equals(typeName.getPrefix())) ? typeName.getLocalPart() : typeName
            .getPrefix() + ":" + typeName.getLocalPart();
    }

    @Override
    public FeatureTypeType translateTypeInfo(FeatureTypeType typeInfo) {
        QName newName =
            new QName(
                "http://www.qgis.org/gml",
                typeInfo.getName().getLocalPart(),
                "ms");
        typeInfo.setName(newName);
        return typeInfo;
    }
}
