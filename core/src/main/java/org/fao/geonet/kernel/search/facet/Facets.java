//===    Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===    United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===    and United Nations Environment Programme (UNEP)
//===
//===    This program is free software; you can redistribute it and/or modify
//===    it under the terms of the GNU General Public License as published by
//===    the Free Software Foundation; either version 2 of the License, or (at
//===    your option) any later version.
//===
//===    This program is distributed in the hope that it will be useful, but
//===    WITHOUT ANY WARRANTY; without even the implied warranty of
//===    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===    General Public License for more details.
//===
//===    You should have received a copy of the GNU General Public License
//===    along with this program; if not, write to the Free Software
//===    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===    Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===    Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.search.facet;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.facet.FacetsConfig;

public class Facets {

    private static final String FACET_CONFIGURATION_HEADER = " * Facet configuration:\n";

    private List<Dimension> dimensions;

    public Facets(List<Dimension> dimensions) {
        this.dimensions = dimensions;
    }

    public List<Dimension> getDimensions() {
        return new ArrayList<Dimension>(dimensions);
    }

    public List<Dimension> getDimensionsUsing(String indexKey) {
        List<Dimension> result = new ArrayList<Dimension>();

        for (Dimension dimension: dimensions) {
            String dimensionIndexKey = dimension.getIndexKey();

            if (dimensionIndexKey != null && dimensionIndexKey.equals(indexKey)) {
                result.add(dimension);
            }
        }

        return result;
    }

    public FacetsConfig getAsLuceneFacetsConfig () {
        FacetsConfig result = new FacetsConfig();

        for (Dimension dimension : dimensions) {
            result.setIndexFieldName(dimension.getName(), dimension.getFacetFieldName());
            result.setMultiValued(dimension.getName(), true);
            result.setHierarchical(dimension.getName(), true);
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(FACET_CONFIGURATION_HEADER);

        for (Dimension dimension: dimensions) {
            sb.append(dimension);
        }

        return sb.toString();
    }


}
