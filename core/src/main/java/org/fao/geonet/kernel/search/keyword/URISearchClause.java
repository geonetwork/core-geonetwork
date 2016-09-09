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
import org.fao.geonet.kernel.rdf.Wheres;
import org.jdom.Element;

/**
 * A search clause for selecting based on a provided keyowrd URI
 *
 * @author jeichar
 */
public class URISearchClause implements SearchClause {

    KeywordSearchType searchType;
    boolean ignoreCase;
    private String uri;

    public URISearchClause(String uri) {
        this.uri = uri;
    }

    public URISearchClause(KeywordSearchType searchType, String keywordURI, boolean ignoreCase) {
        this.searchType = searchType;
        this.uri = keywordURI;
        this.ignoreCase = ignoreCase;
    }

    @Override
    public Where toWhere(Set<String> langs) {
        if (this.searchType != null) {
            return searchType.toWhere("id", this.uri, this.ignoreCase);
        } else {
            return Wheres.ID(this.uri);
        }
    }

    @Override
    public void addXmlParams(Element params) {
        params.addContent(new Element(XmlParams.pUri).setText(this.uri));
        KeywordSearchParamsBuilder.addXmlParam(params, XmlParams.pUri, this.uri);
        if (this.searchType != null) {
            KeywordSearchParamsBuilder.addXmlParam(params,
                XmlParams.pTypeSearch, "" + searchType.ordinal());
        }
    }

}
