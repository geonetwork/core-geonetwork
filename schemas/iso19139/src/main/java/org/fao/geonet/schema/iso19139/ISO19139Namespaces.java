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

package org.fao.geonet.schema.iso19139;

import org.jdom.Namespace;

/**
 * Namespaces for iso19139 metadata standard.
 * <p/>
 * Created by francois on 3/26/14.
 */
public class ISO19139Namespaces {
    public static final Namespace GCO =
        Namespace.getNamespace("gco", "http://www.isotc211.org/2005/gco");
    public static final Namespace SRV =
        Namespace.getNamespace("srv", "http://www.isotc211.org/2005/srv");
    public static final Namespace GMD =
        Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
    public static final Namespace GML =
        Namespace.getNamespace("gml", "http://www.opengis.net/gml/3.2");
    public static final Namespace GML320 =
        Namespace.getNamespace("gml", "http://www.opengis.net/gml");
    public static final Namespace XSI =
        Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    public static final Namespace GTS =
        Namespace.getNamespace("gts", "http://www.isotc211.org/2005/gts");
    public static final Namespace GMX =
        Namespace.getNamespace("gmx", "http://www.isotc211.org/2005/gmx");
    public static final Namespace XLINK =
        Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
}
