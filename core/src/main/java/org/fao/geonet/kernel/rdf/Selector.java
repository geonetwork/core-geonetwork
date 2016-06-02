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

package org.fao.geonet.kernel.rdf;

import java.util.Arrays;
import java.util.Collection;

import org.jdom.Namespace;

/**
 * Represents the columnName and Paths for reading a row and attributes from a thesaurus.
 *
 * @author jeichar
 */
public class Selector {
    /**
     * The columnName that will be created for the Path by the QueryBuilder
     */
    public final String id;
    /**
     * The path for performing the selection
     */
    private final String path;
    /**
     * The namespaces needed by this path
     */
    private final Namespace[] namespaces;

    public Selector(String id, String path, Namespace... namespaces) {
        this.id = id;
        this.path = path;
        this.namespaces = namespaces;
    }

    public Selector(Selector path2) {
        this.id = path2.id;
        this.path = path2.path;
        this.namespaces = path2.namespaces;
    }

    public Collection<Namespace> getNamespaces() {
        return Arrays.asList(namespaces);
    }

    public String getVariable() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public Selector where(final Where whereClause) {
        return new PathDecorator(this) {
            @Override
            public String getPath() {
                return super.getPath() + " WHERE " + whereClause.getClause();
            }
        };
    }
}
