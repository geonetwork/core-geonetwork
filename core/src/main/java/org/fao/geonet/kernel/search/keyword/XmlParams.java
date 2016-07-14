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

public final class XmlParams {
    /**
     * If defined this param declares the keyword text to search for
     *
     * Optional
     */
    public static final String pKeyword = "pKeyword";
    /**
     * The type of search to execute.  Values accepted are: 0,1,2 or one of the Enumeration
     * KeywordSearchType
     *
     * Optional {@link KeywordSearchType#MATCH} is the default
     */
    public static final String pTypeSearch = "pTypeSearch";
    /**
     * the maximum number of results to return.  Must be a positive integer
     *
     * Optional
     */
    public static final String maxResults = "maxResults";
    /**
     * the number of results to skip before returning values (for paging) Must be an integer > 0 and
     * exactly one thesaurus must be defined.
     *
     * Optional
     */
    public static final String offset = "offset";
    /**
     * type of thesaurus to search in if pThesauri is not defined
     *
     * Optional
     */
    public static final String pType = "pType";
    /**
     * A thesaurus to search.  Several pThesauri Elements can be defined in request
     *
     * Optional default is to search all thesauri
     */
    public static final String pThesauri = "pThesauri";
    /**
     * A language to load when a keyword is found.  Several pLang Elements can be defined in
     * request
     *
     * Optional default is to only load the language of the current gui language
     */
    public static final String pLang = "pLang";
    /**
     * If present then a search clause for the uri is included
     */
    public static final String pUri = "pUri";
    /**
     * Sort order
     */
    public static final String sort = "sort";
}
