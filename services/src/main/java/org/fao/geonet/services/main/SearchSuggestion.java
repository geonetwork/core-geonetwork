//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.main;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ComparisonChain;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.apache.commons.lang.NotImplementedException;
import org.fao.geonet.kernel.search.SearcherType;
import org.fao.geonet.utils.Log;
import org.fao.geonet.Util;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.jdom.Attribute;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.*;

/**
 * Return a list of suggestion for a field. The values could be filtered and only values contained
 * in field values are returned.
 *
 * Two modes are defined. First one is browse the index for a field and return a list of suggestion.
 * For this origin should be set to INDEX_TERM_VALUES. The second mode, using origin
 * RECORDS_FIELD_VALUES, perform a search and extract the field for all docs and return the matching
 * values.
 *
 * If no origin is set, a RECORDS_FIELD_VALUES mode is done and if no suggestion found, a
 * INDEX_TERM_VALUES mode is done.
 *
 *
 * The response body is converted to JSON using search-suggestion.xsl
 *
 *
 * OpenSearch suggestion specification: http://www.opensearch.org/Specifications/OpenSearch/Extensions/Suggestions/1.0
 */
public class SearchSuggestion implements Service {
    static final String RECORDS_FIELD_VALUES = "RECORDS_FIELD_VALUES";

    static final String INDEX_TERM_VALUES = "INDEX_TERM_VALUES";

    static final String PARAM_ORIGIN = "origin";
    static final String PARAM_FIELD = "field";
    static final String PARAM_Q = "q";
    static final String PARAM_MAX_NUMBER_OF_TERMS = "maxNumberOfTerms";
    static final String PARAM_THRESHOLD = "threshold";
    static final String PARAM_SORT_BY = "sortBy";
    static final String ELEM_ITEMS = "items";
    static final String ELEM_ITEM = "item";
    static final String ATT_TERM = "term";
    static final String ATT_FREQ = "freq";
    static final String CONFIG_PARAM_MAX_NUMBER_OF_TERMS = "max_number_of_terms";
    static final String CONFIG_PARAM_DEFAULT_SEARCH_FIELD = "default_search_field";
    static final String CONFIG_PARAM_THRESHOLD = PARAM_THRESHOLD;
    private static final String SUMMARY_FACET_CONFIG_KEY = "suggestions";

    /**
     * Max number of term's values to look in the index. For large catalogue this value should be
     * increased in order to get better results. If this value is too high, then looking for terms
     * could take more times. The use of good analyzer should allow to reduce the number of useless
     * values like (a, the, ...).
     */
    private Integer _maxNumberOfTerms;

    /**
     * Minimum frequency for a term value to be proposed in suggestion.
     */
    private Integer _threshold;

    /**
     * Default field to search in. any is full-text search field.
     */
    private String _defaultSearchField = "any";

    private ServiceConfig _config;
    private int _defaultSize  = 100;

    /**
     * Set default parameters
     */
    public void init(Path appPath, ServiceConfig config) throws Exception {
        _threshold = Integer.valueOf(config.getValue(PARAM_THRESHOLD));
        String maxNumberOfTerms = config.getValue(CONFIG_PARAM_MAX_NUMBER_OF_TERMS);
        _maxNumberOfTerms = Integer.valueOf(maxNumberOfTerms);
        _defaultSearchField = config.getValue(CONFIG_PARAM_DEFAULT_SEARCH_FIELD);
        _config = config;
    }

    /**
     * Browse the index and return suggestion list.
     */
    public Element exec(Element params, ServiceContext context)
        throws Exception {
        throw new NotImplementedException("Not implemented in ES");
    }
}
