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

package org.fao.geonet.kernel.search.keyword;

import java.util.Set;

import org.fao.geonet.kernel.rdf.Selectors;
import org.fao.geonet.kernel.rdf.Where;
import org.fao.geonet.kernel.rdf.Wheres;
import org.jdom.Element;

public class KeywordLabelSearchClause implements SearchClause {
    KeywordSearchType searchType;
    String keyword;
    boolean ignoreCase;

    public KeywordLabelSearchClause(KeywordSearchType searchType, String keyword, boolean ignoreCase) {
        this.searchType = searchType;
        this.keyword = keyword;
        this.ignoreCase = ignoreCase;
    }

    public void addXmlParams(Element params) {
        KeywordSearchParamsBuilder.addXmlParam(params, XmlParams.pKeyword, keyword);
        KeywordSearchParamsBuilder.addXmlParam(params, XmlParams.pTypeSearch, "" + searchType.ordinal());
    }

    @Override
    public Where toWhere(Set<String> langs) {
        Where where = Wheres.NONE;
        for (String lang : langs) {
            where = where.or(searchType.toWhere(lang + Selectors.LABEL_POSTFIX, this));
        }
        return where;
    }
}
