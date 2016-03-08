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

import com.google.common.collect.ImmutableSet;
import org.jdom.Namespace;

import java.util.Set;

/**
 * Created by francois on 6/16/14.
 */
public abstract class SchemaPlugin implements CSWPlugin {
    public static final String LOGGER_NAME = "geonetwork.schema-plugin";

    public final String identifier;

    public String getIdentifier() {
        return identifier;
    }


    protected SchemaPlugin(String identifier) {
        this.identifier = identifier;
    }


    private static ImmutableSet<Namespace> allNamespaces;
    public Set<Namespace> getNamespaces() {
        return this.allNamespaces;
    }
    public void setAllNamespaces(ImmutableSet<Namespace> allNamespaces) {
        this.allNamespaces = allNamespaces;
    }
}
