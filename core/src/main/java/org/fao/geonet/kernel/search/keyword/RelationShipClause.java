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

import org.fao.geonet.kernel.rdf.Where;
import org.jdom.Element;

public class RelationShipClause implements SearchClause {

    private KeywordRelation relation;
    private String id;
    private KeywordSearchType searchType;
    private boolean ignoreCase;

    public RelationShipClause(KeywordRelation relation, String relatedId, KeywordSearchType searchType, boolean ignoreCase) {
        this.relation = relation;
        this.id = relatedId;
        this.searchType = searchType;
        this.ignoreCase = ignoreCase;
    }

    @Override
    public Where toWhere(Set<String> langs) {
        return searchType.toWhere(relation.name, id, ignoreCase);
    }

    @Override
    public void addXmlParams(Element params) {
        throw new UnsupportedOperationException();
    }

}
