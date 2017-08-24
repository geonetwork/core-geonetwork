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

package org.fao.geonet.kernel.search.log;

import org.fao.geonet.domain.Constants;

import javax.persistence.*;

/**
 * Entity representing the search parameters of a request. Related to {@link SearchRequest}.
 *
 * @author Jesse
 */
public class SearchRequestParam {
    static final String ID_SEQ_NAME = "search_request_params_id_seq";
    private int _id;
    private LuceneQueryParamType _queryType;
    private String _termField;
    private String _termText;
    private double _similarity;
    private String _lowerText;
    private String _upperText;
    private char _inclusive = Constants.YN_FALSE;
    private SearchRequest _request;

    /**
     * Get the id of the request parameters this entity represents. This is a generated value and as
     * such new instances should not have this set as it will simply be ignored and could result in
     * reduced performance.
     *
     * @return the id of the request parameters this entity represents.
     */
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the request parameters this entity represents. This is a generated value and as
     * such new instances should not have this set as it will simply be ignored and could result in
     * reduced performance.
     *
     * @param id the id of the request parameters this entity represents.
     */
    public void setId(int id) {
        this._id = id;
    }

    /**
     * Get the request associated with this entity.
     *
     * @return the request associated with this entity.
     */
    public SearchRequest getRequest() {
        return _request;
    }

    /**
     * Set the request associated with this entity.
     *
     * @param request the request associated with this entity.
     */
    public void setRequest(SearchRequest request) {
        this._request = request;
    }

    /**
     * Get the type of query parameter.
     *
     * @return the type of query parameter.
     */
    public LuceneQueryParamType getQueryType() {
        return _queryType;
    }

    /**
     * Set the type of query parameter
     *
     * @param queryType the type of query parameter
     * @return this entity
     */
    public SearchRequestParam setQueryType(LuceneQueryParamType queryType) {
        this._queryType = queryType;
        return this;
    }

    /**
     * Return the name of the term used in the search parameter.
     *
     * @return the name of the term used in the search parameter.
     */
    public String getTermField() {
        return _termField;
    }

    /**
     * Set the name of the term used in the search parameter.
     *
     * @param termField the name of the term used in the search parameter.
     * @return this entity
     */
    public SearchRequestParam setTermField(String termField) {
        this._termField = termField;
        return this;
    }

    /**
     * Get the value searched for in the current search parameter.
     *
     * @return the value searched for in the current search parameter.
     */
    public String getTermText() {
        return _termText;
    }

    /**
     * Set the value searched for in the current search parameter.
     *
     * @param termText the value searched for in the current search parameter.
     * @return this entity
     */
    public SearchRequestParam setTermText(String termText) {
        this._termText = termText;
        return this;
    }

    /**
     * Set the similarity level.
     *
     * @return the similarity level.
     */
    public double getSimilarity() {
        return _similarity;
    }

    /**
     * Set the similarity level.
     *
     * @param similarity the similarity level.
     * @return this entity
     */
    public SearchRequestParam setSimilarity(double similarity) {
        this._similarity = similarity;
        return this;
    }

    /**
     * Get the lower level if the search parameter is a range query.
     *
     * @return the lower level if the search parameter is a range query.
     */
    public String getLowerText() {
        return _lowerText;
    }

    /**
     * Set the lower level if the search parameter is a range query.
     *
     * @param lowerText the lower level if the search parameter is a range query.
     * @return this entity
     */
    public SearchRequestParam setLowerText(String lowerText) {
        this._lowerText = lowerText;
        return this;
    }

    /**
     * Get the upper level if the search parameter is a range query.
     *
     * @return the upper level if the search parameter is a range query.
     */
    public String getUpperText() {
        return _upperText;
    }

    /**
     * Set the upper level if the search parameter is a range query.
     *
     * @param upperText the upper level if the search parameter is a range query.
     * @return this entity
     */
    public SearchRequestParam setUpperText(String upperText) {
        this._upperText = upperText;
        return this;
    }

    /**
     * For backwards compatibility we need the inclusive column to be either 'n' or 'y'. This is a
     * workaround to allow this until future versions of JPA that allow different ways of
     * controlling how types are mapped to the database.
     */
    protected char getInclusive_JPAWorkaround() {
        return _inclusive;
    }

    /**
     * Set the inclusive column value Constants.YN_ENABLED or Constants.YN_DISABLED
     *
     * @param inclusive the inclusive column value Constants.YN_ENABLED or Constants.YN_DISABLED
     * @return this entity
     */
    protected SearchRequestParam setInclusive_JPAWorkaround(char inclusive) {
        this._inclusive = inclusive;
        return this;
    }

    /**
     * Return true if the query is a range query and is inclusive.
     *
     * @return true if the query is a range query and is inclusive.
     */
    @Transient
    public boolean isInclusive() {
        return Constants.toBoolean_fromYNChar(getInclusive_JPAWorkaround());
    }

    /**
     * Set true if the query is a range query and is inclusive.
     *
     * @param inclusive true if the query is a range query and is inclusive.
     * @return this entity
     */
    public SearchRequestParam setInclusive(boolean inclusive) {
        return setInclusive_JPAWorkaround(Constants.toYN_EnabledChar(inclusive));
    }
}
