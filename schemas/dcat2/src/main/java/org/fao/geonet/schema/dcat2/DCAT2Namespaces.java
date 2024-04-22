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

package org.fao.geonet.schema.dcat2;

import org.jdom.Namespace;

public class DCAT2Namespaces {
    public static final Namespace DC =
            Namespace.getNamespace("dc", "http://purl.org/dc/elements/1.1/");
    public static final Namespace DCT =
            Namespace.getNamespace("dct", "http://purl.org/dc/terms/");
    public static final Namespace DCAT =
            Namespace.getNamespace("dcat", "http://www.w3.org/ns/dcat#");
    public static final Namespace VCARD =
            Namespace.getNamespace("vcard", "http://www.w3.org/2006/vcard/ns#");
    public static final Namespace FOAF =
            Namespace.getNamespace("foaf", "http://xmlns.com/foaf/0.1/");
    public static final Namespace PROV =
            Namespace.getNamespace("prov", "http://www.w3.org/ns/prov#");
    public static final Namespace LOCN =
            Namespace.getNamespace("locn", "http://www.w3.org/ns/locn#");
    public static final Namespace OWL =
            Namespace.getNamespace("owl", "http://www.w3.org/2002/07/owl#");
    public static final Namespace ADMS =
            Namespace.getNamespace("adms", "http://www.w3.org/ns/adms#");
    public static final Namespace SKOS =
            Namespace.getNamespace("skos", "http://www.w3.org/2004/02/skos/core#");
    public static final Namespace SPDX =
            Namespace.getNamespace("spdx", "http://spdx.org/rdf/terms#");
    public static final Namespace RDF =
            Namespace.getNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    public static final Namespace RDFS =
            Namespace.getNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
}
