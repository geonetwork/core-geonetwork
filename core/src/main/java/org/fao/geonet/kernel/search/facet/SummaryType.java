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

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.BadParameterEx;

public class SummaryType {

    private static final String TEMPLATE = "  * %s {format=%s}%n";

    private String name;

    private List<ItemConfig> items;

    private Format format;

    public SummaryType(String name, List<ItemConfig> items) {
        this.name = name;
        this.items = items;
        format = Format.FACET_NAME;
    }

    public String getName() {
        return name;
    }

    public List<ItemConfig> getItems() {
        return items;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public Format getFormat() {
        return format;
    }

   public ItemConfig get(String name) {
        for (ItemConfig item: items) {
            if (item.getDimension().getName().equals(name)) {
                return item;
            }
        }

        throw new BadParameterEx(
            Geonet.SearchResult.SUMMARY_ITEMS,
            name + " Legal values are: " + getDimensionNames()
        );
    }

    private List<String> getDimensionNames() {
        List<String> result = new ArrayList<String>();

        for (ItemConfig item: items) {
            result.add(item.getDimension().getName());
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(TEMPLATE, name, format));

        for (ItemConfig item: items) {
            sb.append(item);
        }

        return sb.toString();
    }

}
