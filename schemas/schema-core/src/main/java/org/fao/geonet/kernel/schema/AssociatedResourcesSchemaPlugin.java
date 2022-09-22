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

import java.util.Set;

/**
 * Created by francois on 8/20/14.
 */
public interface AssociatedResourcesSchemaPlugin {

    /**
     * Analyse a metadata record and extract associated resources.
     */
    public abstract Set<AssociatedResource> getAssociatedResourcesUUIDs(Element metadata);

    /**
     * Analyze a metadata record and extract associated parents
     */
    public abstract Set<String> getAssociatedParentUUIDs(Element metadata);

    public abstract Set<String> getAssociatedDatasetUUIDs(Element metadata);

    public abstract Set<String> getAssociatedFeatureCatalogueUUIDs(Element metadata);

    public abstract Set<String> getAssociatedSourceUUIDs(Element metadata);

    public abstract Set<AssociatedResource> getAssociatedParents(Element metadata);

    public abstract Set<AssociatedResource> getAssociatedDatasets(Element metadata);

    public abstract Set<AssociatedResource> getAssociatedFeatureCatalogues(Element metadata);

    public abstract Set<AssociatedResource> getAssociatedSources(Element metadata);
}
