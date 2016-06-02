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

package org.fao.geonet.kernel.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.facet.taxonomy.CategoryPath;

import java.util.Collection;

/**
 * The information that will be put in the index for searching for a particular language.
 *
 * @author Jesse on 10/8/2014.
 * @see org.fao.geonet.kernel.search.SearchManager#buildIndexDocument(String, org.jdom.Element,
 * String, java.util.List)
 */
public class IndexInformation {
    final String language;
    final Document document;
    final Collection<CategoryPath> taxonomy;

    public IndexInformation(String language, Document document, Collection<CategoryPath> taxonomy) {
        this.language = language;
        this.document = document;
        this.taxonomy = taxonomy;
    }
}
