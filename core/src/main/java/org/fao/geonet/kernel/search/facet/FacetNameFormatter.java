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

import org.jdom.Element;

public class FacetNameFormatter implements Formatter {

    private Dimension dimension;

    public FacetNameFormatter(Dimension dimension) {
        this.dimension = dimension;
    }

    @Override
    public Element buildDimensionTag(int count) {
        return new Element(dimension.getLabel());
    }

    @Override
    public Element buildCategoryTag(CategorySummary result) {
        Element categoryTag = new Element(dimension.getName());

        categoryTag.setAttribute("count", Integer.toString(result.count));
        categoryTag.setAttribute("name", result.value);

        if (!result.value.equals(result.label) && !result.label.trim().isEmpty()) {
            categoryTag.setAttribute("label", result.label);
        }

        return categoryTag;
    }

}
