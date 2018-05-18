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

package org.fao.geonet.kernel.schema;

import org.jdom.Element;

import java.util.List;
import java.util.Map;

/**
 * Created by francois on 31/01/15.
 */
public interface ISOPlugin {
    /**
     * Return the name (with namespace prefix) for the basic default type.
     */
    String getBasicTypeCharacterStringName();

    /**
     * Return an element to be use as default when creating new elements.
     */
    Element createBasicTypeCharacterString();


    /**
     * Add operatesOn and coupledResource element to a service metadata record.
     * @return the updated record
     */
    Element addOperatesOn(Element serviceRecord, Map<String, String> layers, String serviceType, String baseUrl);

    class Extent {
        public double xmin;
        public double xmax;
        public double ymin;
        public double ymax;

        public Extent(Double xmin, Double xmax, Double ymin, Double ymax) {
            this.xmin = xmin;
            this.xmax = xmax;
            this.ymin = ymin;
            this.ymax = ymax;
        }
    }

    List<Extent> getExtents(Element record);
}
