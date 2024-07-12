/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.kernel.csw.CatalogConfiguration;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static co.elastic.clients.elasticsearch._types.SortOptions.*;

public class SortByParser {

    @Autowired
    IFieldMapper fieldMapper;

    @Autowired
    private CatalogConfiguration _catalogConfig;

    public List<SortOptions> parseSortBy(Element request) {
        Element query = request.getChild("Query", Csw.NAMESPACE_CSW);
        if (query == null) {
            return getDefaultSort();
        }

        Element sortBy = query.getChild("SortBy", Csw.NAMESPACE_OGC);
        if (sortBy == null) {
            return getDefaultSort();
        }

        List<SortOptions> sortFields = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Element> list = sortBy.getChildren();
        for (Element el : list) {
            String esSortFieldName = getEsSortFieldName(el);
            if (!StringUtils.isEmpty(esSortFieldName)) {
                SortOrder esSortOrder = getEsSortOrder(el);

                SortOptions sortFieldOptions =
                    new Builder()
                        .field(new FieldSort.Builder()
                            .field(esSortFieldName)
                            .order(esSortOrder).build())
                        .build();

                sortFields.add(sortFieldOptions);
            }
        }

        if (sortFields.size() == 0) {
            sortFields = getDefaultSort();
        }
        return sortFields;
    }

    private List<SortOptions> getDefaultSort() {
        List<SortOptions> sortFields = new ArrayList<>();

        SortOptions defaultSortField = SortOptions.of(
            b -> b.field(fb -> fb.field(_catalogConfig.getDefaultSortField())
                .order(_catalogConfig.getDefaultSortOrder().equals("DESC")?SortOrder.Desc:SortOrder.Asc))
        );

        sortFields.add(defaultSortField);
        return sortFields;
    }

    private String getEsSortFieldName(Element el) {
        String cswField = el.getChildText("PropertyName", Csw.NAMESPACE_OGC);
        if (cswField == null) {
            return null;
        }
        String matchingEsFieldOrEmpty = fieldMapper.mapSort(cswField);
        if (StringUtils.isEmpty(matchingEsFieldOrEmpty) && cswField.toLowerCase().equals("relevance")) {
            return "_score";
        }
        return matchingEsFieldOrEmpty;
    }

    private SortOrder getEsSortOrder(Element el) {
        String order = el.getChildText("SortOrder", Csw.NAMESPACE_OGC);
        boolean isDescOrder = "DESC".equals(order);
        return isDescOrder ? SortOrder.Desc : SortOrder.Asc;
    }
}
