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

public class DimensionFormatter implements Formatter {

    private Dimension dimension;

    public DimensionFormatter(Dimension dimension) {
        this.dimension = dimension;
    }

    @Override
    public Element buildDimensionTag(int count) {
        Element dimensionTag = new Element("dimension");
        dimensionTag.setAttribute("name", dimension.getName());
        dimensionTag.setAttribute("label", dimension.getLabel());
        return dimensionTag;
    }

    @Override
    public Element buildCategoryTag(CategorySummary result) {
        Element dimensionTag = new Element("category");
        dimensionTag.setAttribute("value", result.value);
        dimensionTag.setAttribute("label", result.label);
        dimensionTag.setAttribute("count", Integer.toString(result.count));
        return dimensionTag;
    }

}
