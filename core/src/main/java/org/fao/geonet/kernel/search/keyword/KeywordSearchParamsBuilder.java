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

import org.fao.geonet.Util;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.rdf.QueryBuilder;
import org.fao.geonet.kernel.rdf.Selector;
import org.fao.geonet.kernel.rdf.Selectors;
import org.fao.geonet.kernel.rdf.Where;
import org.fao.geonet.kernel.rdf.Wheres;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Represents the parameters for doing a broad keyword search.
 *
 * @author jeichar
 */
public class KeywordSearchParamsBuilder {
    private IsoLanguagesMapper isoLangMapper;
    private Set<String> thesauriNames = new LinkedHashSet<String>();
    private Set<String> langs = new LinkedHashSet<String>();
    private String thesauriDomainName;
    private int offset = -1;
    private int maxResults = -1;
    private LinkedList<SearchClause> searchClauses = new LinkedList<SearchClause>();
    private LinkedList<Selector> selectClauses = new LinkedList<Selector>();
    private boolean lenient;
    private boolean requireBoundedBy = false;
    private Comparator<KeywordBean> comparator;

    public KeywordSearchParamsBuilder(IsoLanguagesMapper mapper) {
        this.isoLangMapper = mapper;
    }

    /**
     * Parses an Element object and creates search params from the params.  Each param is a child of
     * the root element and the name of the parameter is the name of the Child element and the value
     * is the text.
     *
     * The params tags that are accepted are as follows are defined in the class {@link XmlParams}
     *
     * @param params the root element containing children with each param.
     * @return A params object ready for searching with
     */
    public static KeywordSearchParamsBuilder createFromElement(IsoLanguagesMapper mapper, Element params) throws BadInputEx {
        KeywordSearchParamsBuilder parsedParams = new KeywordSearchParamsBuilder(mapper).lenient(true);

        String keyword = Util.getParam(params, XmlParams.pKeyword, null);
        if (keyword != null) {
            KeywordSearchType searchType = KeywordSearchType.parseString(Util.getParam(params, XmlParams.pTypeSearch, KeywordSearchType.MATCH.name()));
            parsedParams.keyword(keyword, searchType, true);
        }

        String uri = Util.getParam(params, XmlParams.pUri, null);
        if (uri != null) {
            parsedParams.uri(uri);
        }

        String maxResults = Util.getParam(params, XmlParams.maxResults, null);
        if (maxResults != null) {
            parsedParams.maxResults(Integer.parseInt(maxResults));
        }

        String offset = Util.getParam(params, XmlParams.offset, null);
        if (offset != null) {
            parsedParams.offset(Integer.parseInt(offset));
        }

        String thesauriDomainName = Util.getParam(params, XmlParams.pType, null);
        if (thesauriDomainName != null) {
            parsedParams.thesauriDomainName(thesauriDomainName);
        }

        @SuppressWarnings("unchecked")
        List<Element> thesauri = params.getChildren(XmlParams.pThesauri);
        for (Element thesaurusName : thesauri) {
            final String text = thesaurusName.getTextTrim();
            if (!text.isEmpty()) {
                parsedParams.addThesaurus(text);
            }
        }

        @SuppressWarnings("unchecked")
        List<Element> langs = params.getChildren(XmlParams.pLang);
        for (Element lang : langs) {
            parsedParams.addLang(lang.getTextTrim());
        }

        return parsedParams;
    }

    static void addXmlParam(Element params, String paramName, String value) {
        params.addContent(new Element(paramName).setText(value));
    }

    /**
     * if set to true then the params will not throw exceptions when incorrectly configured.
     *
     * This parameter is for backwards compatibility.
     */
    public KeywordSearchParamsBuilder lenient(boolean lenient) {
        this.lenient = lenient;
        checkState(true);
        return this;
    }

    /**
     * Add a language to load during the search.  The definition and value of the Keyword both have
     * translations normally
     *
     * @param lang the three letter language code of the language to load
     * @return this object
     */
    public KeywordSearchParamsBuilder addLang(String lang) {
        this.langs.add(lang);
        checkState(true);
        return this;
    }

    /**
     * The name/key of a thesaurus to add for searching.  If no thesauri are added then all thesauri
     * are searched.
     *
     * This parameter mutually exclusive with thesauriType.  If both are defined (and lenient is
     * false) an exception will be thrown.
     *
     * @param thesaurusName the name/key of the thesaurus to search
     * @return this object
     */
    public KeywordSearchParamsBuilder addThesaurus(String thesaurusName) {
        this.thesauriNames.add(thesaurusName);
        checkState(true);
        return this;
    }

    /**
     * A read-only copy of the thesaurus names
     *
     * @return A read-only copy of the thesaurus names
     */
    public Set<String> getThesauriNames() {
        return Collections.unmodifiableSet(thesauriNames);
    }

    /**
     * Set the thesauriDomainName Domain/category to search.  If the thesauriDomainName is defined
     * then all thesauri in that domain will be searched.
     *
     * This parameter mutually exclusive with thesauriType.  If both are defined (and lenient is
     * false) an exception will be thrown.
     *
     * @return this
     */
    public KeywordSearchParamsBuilder thesauriDomainName(String thesauriDomainName) {
        this.thesauriDomainName = thesauriDomainName;
        checkState(true);
        return this;
    }

    /**
     * Set the offset from the first record found to return.  IE the number of records to skip
     * before returning a value.  This is for paging primarily.
     *
     * @param offset a value > 0 means to skip records.  <= 0 means start at 0
     * @return this
     */
    public KeywordSearchParamsBuilder offset(int offset) {
        this.offset = offset;
        checkState(true);
        return this;
    }

    /**
     * The maximum number of results to return from in a search.  A value < 1  means return all
     * results
     *
     * @param maxResults maximum number of results to return from in a search.  A value < 1  means
     *                   return all results
     * @return this
     */
    public KeywordSearchParamsBuilder maxResults(int maxResults) {
        this.maxResults = maxResults;
        checkState(true);
        return this;
    }

    /**
     * Define a search parameter.  If defined the keyword must exist in one of the languages that
     * are searched for.
     *
     * @param keyword    the keyword to search for
     * @param searchType type of search (IE startsWith, contains, matches, etc...)
     * @param ignoreCase whether or not to be a case sensitive search
     * @return this
     */
    public KeywordSearchParamsBuilder keyword(String keyword, KeywordSearchType searchType, boolean ignoreCase) {
        this.searchClauses.add(new KeywordLabelSearchClause(searchType, keyword, ignoreCase));
        checkState(true);
        return this;
    }

    /**
     * Create an immutable params object from this builder
     *
     * @return an immutable params object from this builder
     */
    public KeywordSearchParams build() {
        checkState(false);
        return new KeywordSearchParams(createQuery(), thesauriNames, thesauriDomainName, maxResults, this.comparator);
    }

    private QueryBuilder<KeywordBean> createQuery() {
        Where where = Wheres.NONE;

        if (!searchClauses.isEmpty()) {
            for (SearchClause nextClause : searchClauses) {
                where = where.or(nextClause.toWhere(langs));
            }
        }

        QueryBuilder<KeywordBean> builder = QueryBuilder.keywordQueryBuilder(isoLangMapper, new ArrayList<String>(langs), requireBoundedBy)
            .offset(offset)
            .where(where);

        if (!selectClauses.isEmpty()) {
            for (Selector s : selectClauses) {
                builder.select(s, false);
            }
        }

        return builder;
    }

    /**
     * @param errorsOnly if true then it is assumed that the params are not finished and therefore
     *                   only throw exception if there is a state that is illegal currently.  (For
     *                   example don't throw exception if no languages are defined because they
     *                   might be defined later).
     */
    private void checkState(boolean errorsOnly) {
        if (!lenient) {
            // errors that can be ignored if lenient
            if (!thesauriNames.isEmpty() && thesauriDomainName != null) {
                throw new IllegalStateException("thesauriNames and thesauriType cannot both be defined");
            }
        }

        // errors that can not be ignored when ready to execute
        if (!errorsOnly) {
            if (langs.isEmpty()) {
                throw new IllegalStateException("At least one language must be defined");
            }

            if (offset > 0 && thesauriNames.size() != 1) {
                throw new IllegalStateException("Offset can only be used with one thesaurus.  if offest is defined as > 0 then there must be one and only one thesaurus name also defined");
            }
        }
    }

    /**
     * Convert the query to Xml params that are compatible with the old KeywordSearch API
     *
     * @return parameters as Xml
     */
    public Element toXmlParams() {
        Element params = new Element("params");

        addXmlParam(params, XmlParams.offset, "" + offset);
        addXmlParam(params, XmlParams.maxResults, "" + maxResults);
        if (thesauriNames.isEmpty()) {
            if (thesauriDomainName != null) {
                addXmlParam(params, XmlParams.pType, thesauriDomainName);
            }
        } else {
            for (String name : thesauriNames) {
                addXmlParam(params, XmlParams.pThesauri, name);
            }
        }

        for (String lang : langs) {
            addXmlParam(params, XmlParams.pLang, lang);
        }

        for (SearchClause search : searchClauses) {
            search.addXmlParams(params);
        }

        return params;
    }

    /**
     * Return a read only collection of languages
     *
     * @return a read only collection of languages
     */
    public Set<String> getLangs() {
        return Collections.unmodifiableSet(langs);
    }

    /**
     * Add a where clause that accepts the keywordURI
     *
     * @param keywordURI the uri identifying the keyword to find
     * @return this
     */
    public KeywordSearchParamsBuilder uri(String keywordURI) {
        this.searchClauses.add(new URISearchClause(keywordURI));
        return this;
    }

    public KeywordSearchParamsBuilder uri(String keywordURI, KeywordSearchType searchType, boolean ignoreCase) {
        this.searchClauses.add(new URISearchClause(searchType, keywordURI, ignoreCase));
        return this;
    }

    public void relationship(String relatedId, KeywordRelation relation, KeywordSearchType searchType, boolean ignoreCase) {
        this.selectClauses.add(Selectors.BROADER);
        this.searchClauses.add(new RelationShipClause(relation, relatedId, searchType, ignoreCase));

    }

    public KeywordSearchParamsBuilder requireBoundedBy(boolean require) {
        this.requireBoundedBy = require;
        return this;

    }

    public KeywordSearchParamsBuilder setComparator(Comparator<KeywordBean> comparator) {
        this.comparator = comparator;
        return this;
    }
}
