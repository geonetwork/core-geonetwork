/*
 * Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.csw.services.getrecords;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.fao.geonet.csw.common.Csw;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

public class SortByParser {

    public List<SortBuilder<FieldSortBuilder>> parseSortBy(Element request, IFieldMapper fieldMapper) {
        Element query = request.getChild("Query", Csw.NAMESPACE_CSW);
        if (query == null) {
            return null;
        }

        Element sortBy = query.getChild("SortBy", Csw.NAMESPACE_OGC);
        if (sortBy == null) {
            return null;
        }

        List<SortBuilder<FieldSortBuilder>> sortFields = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Element> list = sortBy.getChildren();
        for (Element el : list) {
            String field = el.getChildText("PropertyName", Csw.NAMESPACE_OGC);
            String order = el.getChildText("SortOrder", Csw.NAMESPACE_OGC);

            boolean isDescOrder = "DESC".equals(order);

            if (field == null) {
                continue;
            }
            String indexField = fieldMapper.mapSort(field);
            if (StringUtils.isEmpty(indexField) && field.toLowerCase().equals("relevance")) {
                indexField = "_score";
            }
            if (!StringUtils.isEmpty(indexField)) {
                sortFields.add(
                        new FieldSortBuilder(indexField)
                                .order(isDescOrder ? SortOrder.DESC : SortOrder.ASC));
            }
        }
        return sortFields;
    }
}
