/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
package org.fao.geonet.schema.iso19115_3_2018;

import org.jdom.Namespace;

/**
 * Created by francois on 3/26/14.
 */
public class ISO19115_3_2018Namespaces {
    public static final Namespace GCO =
            Namespace.getNamespace("gco",
                    "http://standards.iso.org/iso/19115/-3/gco/1.0");
    public static final Namespace MDB =
            Namespace.getNamespace("mdb",
                    "http://standards.iso.org/iso/19115/-3/mdb/2.0");
    public static final Namespace MRI =
            Namespace.getNamespace("mri",
                    "http://standards.iso.org/iso/19115/-3/mri/1.0");
    public static final Namespace CIT =
            Namespace.getNamespace("cit",
                    "http://standards.iso.org/iso/19115/-3/cit/2.0");
    public static final Namespace SRV =
            Namespace.getNamespace("srv",
                    "http://standards.iso.org/iso/19115/-3/srv/2.0");
    public static final Namespace GEX =
        Namespace.getNamespace("gex",
            "http://standards.iso.org/iso/19115/-3/gex/1.0");
    public static final Namespace MRC =
            Namespace.getNamespace("mrc",
                    "http://standards.iso.org/iso/19115/-3/mrc/2.0");
    public static final Namespace MRL =
            Namespace.getNamespace("mrl",
                    "http://standards.iso.org/iso/19115/-3/mrl/2.0");
    public static final Namespace LAN =
            Namespace.getNamespace("lan",
                    "http://standards.iso.org/iso/19115/-3/lan/1.0");
    public static final Namespace GCX =
            Namespace.getNamespace("gcx",
                    "http://standards.iso.org/iso/19115/-3/gcx/1.0");
    public static final Namespace XLINK =
        Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
}
