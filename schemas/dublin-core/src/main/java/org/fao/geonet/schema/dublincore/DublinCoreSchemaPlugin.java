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

package org.fao.geonet.schema.dublincore;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.fao.geonet.kernel.schema.AssociatedResource;
import org.fao.geonet.kernel.schema.AssociatedResourcesSchemaPlugin;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by francois on 6/15/14.
 */
public class DublinCoreSchemaPlugin
    extends org.fao.geonet.kernel.schema.SchemaPlugin
    implements AssociatedResourcesSchemaPlugin {
    public static final String IDENTIFIER = "dublin-core";

    private static ImmutableSet<Namespace> allNamespaces;
    private static Map<String, Namespace> allTypenames;

    static {
        allNamespaces = ImmutableSet.<Namespace>builder()
            .add(DublinCoreNamespaces.DC)
            .add(DublinCoreNamespaces.DCT)
            .build();

        allTypenames = ImmutableMap.<String, Namespace>builder()
            .put("csw:Record", Namespace.getNamespace("csw", "http://www.opengis.net/cat/csw/2.0.2"))
            .put("dcat", Namespace.getNamespace("dcat", "http://www.w3.org/ns/dcat#"))
            .build();
    }

    public DublinCoreSchemaPlugin() {
        super(IDENTIFIER, allNamespaces);
    }


    /**
     * Always return null. Not implemented for dublin core records.
     */
    public Set<AssociatedResource> getAssociatedResourcesUUIDs(Element metadata) {
        return new HashSet<>();
    }

    @Override
    public Set<String> getAssociatedParentUUIDs(Element metadata) {
        return getAssociatedParents(metadata)
            .stream()
            .map(AssociatedResource::getUuid)
            .collect(Collectors.toSet());
    }

    public Set<String> getAssociatedDatasetUUIDs(Element metadata) {
        return new HashSet<>();
    }

    ;

    public Set<String> getAssociatedFeatureCatalogueUUIDs(Element metadata) {
        return new HashSet<>();
    }

    ;

    public Set<String> getAssociatedSourceUUIDs(Element metadata) {
        return new HashSet<>();
    }

    @Override
    public Set<AssociatedResource> getAssociatedParents(Element metadata) {
        ElementFilter elementFilter = new ElementFilter("isPartOf", DublinCoreNamespaces.DCT);
        return Xml.filterElementValues(
            metadata,
            elementFilter,
            null, null,
            null).stream()
            .map(uuid -> new AssociatedResource(uuid, "", "isPartOf"))
            .collect(Collectors.toSet());
    }

    @Override
    public Set<AssociatedResource> getAssociatedDatasets(Element metadata) {
        return new HashSet<>();
    }

    @Override
    public Set<AssociatedResource> getAssociatedFeatureCatalogues(Element metadata) {
        return new HashSet<>();
    }

    @Override
    public Set<AssociatedResource> getAssociatedSources(Element metadata) {
        return new HashSet<>();
    }

    @Override
    public Map<String, Namespace> getCswTypeNames() {
        return allTypenames;
    }
}
