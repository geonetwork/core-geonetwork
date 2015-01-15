//==============================================================================
//===	Copyright (C) 2001-2010 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.search;

import com.google.common.base.Splitter;

import org.fao.geonet.kernel.search.LuceneConfig.LuceneConfigNumericField;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.utils.Log;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.automaton.LevenshteinAutomata;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Class to create a Lucene query from a {@link LuceneQueryInput} representing a
 * search request.
 * 
 * <ul>
 *  <li>all search criteria could have more than one value (AND operator is used)</li>
 *  <li>Non tokenized field could use the {@link #OR_SEPARATOR} to create an OR query (eg. for keyword, use Global or biodiversity)</li>
 *  <li>all search criteria are take into account (except {@link UserQueryInput#RESERVED_FIELDS} and {@link UserQueryInput#SECURITY_FIELDS})</li>
 *  <li>extra search fields are considered as text (and are analyzed according to Lucene index configuration)</li>
 *  <li></li>
 * </ul>
 * 
 * @author heikki doeleman
 * @author francois prunayre
 */
public class LuceneQueryBuilder {

    private static final String OR_SEPARATOR = " or ";
    private static final String FIELD_OR_SEPARATOR = "_OR_";
    private static final String FACET_QUERY_AND_SEPARATOR = "&";
    private static final String STRING_TOKENIZER_DELIMITER = " \n\r\t";
    private Set<String> _tokenizedFieldSet;
    private PerFieldAnalyzerWrapper _analyzer;
    private Map<String, LuceneConfig.LuceneConfigNumericField> _numericFieldSet;
    private FacetsConfig _taxonomyConfiguration;
    private String _language;

    // Lat long bounding box constants
    private static final Double minBoundingLatitudeValue = -90.0;
    private static final Double maxBoundingLatitudeValue = 90.0;
    private static final Double minBoundingLongitudeValue = -180.0;
    private static final Double maxBoundingLongitudeValue = 180.0;

    /**
     * Only one spatial search criteria could be added.
     */
    private boolean spatialCriteriaAdded;
    /**
     * Only one temporal search criteria could be added.
     */
    private boolean temporalCriteriaAdded;
    /**
     * Template = "n" is added if not set in search criteria.
     */
    private boolean templateCriteriaAdded;


    /**
     * TODO javadoc.
     *
     * @param tokenizedFieldSet names of tokenized fields
     * @param numericFieldSet names of numeric fields
     * @param analyzer Lucene analyzer
     * @param langCode language of search terms
     */
    public LuceneQueryBuilder(Set<String> tokenizedFieldSet,
                              Map<String, LuceneConfig.LuceneConfigNumericField> numericFieldSet,
                              FacetsConfig taxonomyConfiguration,
                              PerFieldAnalyzerWrapper analyzer, String langCode) {
        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
        
        _tokenizedFieldSet = tokenizedFieldSet;
        _numericFieldSet = numericFieldSet;
        _taxonomyConfiguration = taxonomyConfiguration;
        _analyzer = analyzer;
        _language = langCode;
    }

    /**
     * Build a Lucene query for the {@link LuceneQueryInput}.
     * 
     * @param luceneQueryInput the requested search parameters
     * @return Lucene query
     */
    public Query build(LuceneQueryInput luceneQueryInput) {
        if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "LuceneQueryBuilder: luceneQueryInput is: \n" + luceneQueryInput.toString());
        Query result = buildBaseQuery(luceneQueryInput);

        if (luceneQueryInput.getFacetQueries().size() > 0) {
            result = addFacetQueries(result, luceneQueryInput.getFacetQueries());
        }

        return result;
    }

    /**
     * Build a Lucene query for the {@link LuceneQueryInput}.
     *
     * A AND clause is used for each search criteria and a OR clause if the content of a criteria is "this or that".
     *
     * A Boolean OR between parameters is used if the parameter has the form A_OR_B with content "this", this will
     * produce a query for documents having "this" in field A, B or both.
     *
     * Some search criteria does not support multi-occurences like spatial, temporal criteria or range fields.
     * 
     * @param luceneQueryInput user and system input
     * @return Lucene query
     */

    private Query buildBaseQuery(LuceneQueryInput luceneQueryInput) {
        // Remember which range fields have been processed
        Set<String> processedRangeFields = new HashSet<String>();

        // top query to hold all sub-queries for each search parameter
        BooleanQuery query = new BooleanQuery();

        // Filter according to user session
        addPrivilegeQuery(luceneQueryInput, query);

        // similarity is passed to textfield-query-creating methods
        String similarity = luceneQueryInput.getSimilarity();

        Map<String, Set<String>> searchCriteria = luceneQueryInput.getSearchCriteria();

        //
        // search criteria fields may contain zero or more _OR_ in their name, in which case the search will be a
        // disjunction of searches for fieldnames separated by that.
        //
        // here such _OR_ fields are parsed, an OR searchCriteria map is created, and they're removed from vanilla
        // searchCriteria map.
        //
        Map<String, Set<String>> searchCriteriaOR = new HashMap<String, Set<String>>();

        for (Iterator<Entry<String, Set<String>>> i = searchCriteria.entrySet().iterator(); i.hasNext();) {
            Entry<String, Set<String>> entry = i.next();
            String fieldName = entry.getKey();
            Set<String> fieldValues = entry.getValue();
            if (fieldName.contains(FIELD_OR_SEPARATOR)) {
                i.remove();
                if (fieldName.contains(LuceneIndexField.NORTH)
                        || fieldName.contains(LuceneIndexField.SOUTH)
                        || fieldName.contains(LuceneIndexField.EAST)
                        || fieldName.contains(LuceneIndexField.WEST)
                        || fieldName.contains(SearchParameter.RELATION)

                        || fieldName.contains("without")
                        || fieldName.contains("phrase")

                        || fieldName.contains(SearchParameter.EXTTO)
                        || fieldName.contains(SearchParameter.EXTFROM)

                        || fieldName.contains(SearchParameter.FEATURED)
                        || fieldName.contains(SearchParameter.TEMPLATE)

                        || UserQueryInput.RANGE_QUERY_FIELDS.contains(fieldName)

                        ) {
                    // not supported in field disjunction
                    continue;
                }

                @SuppressWarnings("resource")
                Scanner scanner = new Scanner(fieldName).useDelimiter(FIELD_OR_SEPARATOR);
                while (scanner.hasNext()) {
                    String field = scanner.next();

                    if(field.equals("or")) {
                        // handle as 'any', add ' or ' for space-separated values

                        field = "any";
                        Set<String> values = searchCriteriaOR.get(field);
                        if(values == null) values = new HashSet<String>();
                        values.addAll(fieldValues);
                        searchCriteriaOR.put(field, values);
                    }
                    else {
                            Set<String> values = searchCriteriaOR.get(field);
                            if(values == null) values = new HashSet<String>();
                            values.addAll(fieldValues);
                            searchCriteriaOR.put(field, values);
                    }
                }
            }
        }
        query = buildORQuery(searchCriteriaOR, query, similarity);
        query = buildANDQuery(searchCriteria, query, similarity, processedRangeFields);
        if(StringUtils.isNotEmpty(_language)) {
            if(Log.isDebugEnabled(Geonet.LUCENE))
                Log.debug(Geonet.LUCENE, "adding locale query for language " + _language);
            return addLocaleTerm(query, _language, luceneQueryInput.isRequestedLanguageOnly());
        }
        else {
            if(Log.isDebugEnabled(Geonet.LUCENE))
                Log.debug(Geonet.LUCENE, "no language set, not adding locale query");
            return query;
        }
    }

    /**
     * Add drilldown queries to a base query
     *
     * There may be many drilldown queries specified in the search parameters
     * Add each one to the base query
     * 
     * @param baseQuery base query for requested search criteria
     * @param facetQueries drilldown queries requested
     * 
     * @return Lucene query
     */

    private Query addFacetQueries(Query baseQuery,
            Set<String> facetQueries) {
        DrillDownQuery result = new DrillDownQuery(_taxonomyConfiguration, baseQuery);

        for (String facetQuery: facetQueries) {
            addFacetQuery(facetQuery, result);
        }

        return result;
    }

    private void addFacetQuery(String facetQuery, DrillDownQuery result) {
        for (String drillDownParam: facetQuery.split(FACET_QUERY_AND_SEPARATOR)) {
            DrillDownPath drillDownPath = new DrillDownPath(drillDownParam);
            result.add(drillDownPath.getDimension(), drillDownPath.getPath());
        }
    }

    /**
     * Builds a query where OR operator is used for each search criteria.
     *
     * @param searchCriteria
     * @param query
     * @param similarity
     * @return
     */
    private BooleanQuery buildORQuery(Map<String, Set<String>> searchCriteria, BooleanQuery query, String similarity) {

        spatialCriteriaAdded = false;
        temporalCriteriaAdded = false;
        templateCriteriaAdded = false;

        if(searchCriteria.size() == 0) {
            return query;
        }

        // Avoid search by field who control privileges
        Set<String> fields = new HashSet<String>();
        for(String requestedField : searchCriteria.keySet()) {
            if(!(UserQueryInput.SECURITY_FIELDS.contains(requestedField) || SearchParameter.EDITABLE.equals(requestedField))) {
                fields.add(requestedField);
            }
        }

        BooleanClause.Occur occur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
        BooleanClause.Occur tokenOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);
        BooleanQuery booleanQuery = new BooleanQuery();
        for(String fieldName : fields) {

            Set<String> fieldValues = searchCriteria.get(fieldName) ;
            for (String fieldValue : fieldValues) {
                if (fieldValue.contains(OR_SEPARATOR)) {
                    // TODO : change OR separator
                    // Add all separated values to the boolean query
                    addSeparatedTextField(fieldValue, OR_SEPARATOR, fieldName, booleanQuery);
                } else {
                    if (LuceneIndexField.ANY.equals(fieldName) || "all".equals(fieldName)) {
                        BooleanClause anyClause = null;
                        if (!onlyWildcard(fieldValue)) {
                            anyClause = tokenizeSearchParam(fieldValue, similarity, tokenOccur, occur);
                        }
                        if (anyClause != null && StringUtils.isNotEmpty(anyClause.toString())) {
                            booleanQuery.add(anyClause);
                        }
                    } else {
                        if (!_tokenizedFieldSet.contains(fieldName)) {
                            // TODO : use similarity when needed
                            TermQuery termQuery = new TermQuery(new Term(fieldName, fieldValue.trim()));
                            BooleanClause clause = new BooleanClause(termQuery, tokenOccur);
                            booleanQuery.add(clause);
                        } else {
                            // tokenize searchParam
                            StringTokenizer st = new StringTokenizer(fieldValue.trim(), STRING_TOKENIZER_DELIMITER);

                            while (st.hasMoreTokens()) {
                                String token = st.nextToken();
                                Query subQuery = textFieldToken(token, fieldName, similarity);
                                if (subQuery != null) {
                                    BooleanClause subClause = new BooleanClause(subQuery, tokenOccur);
                                    booleanQuery.add(subClause);
                                }
                            }
                        }
                    }
                }
            }
        }
        BooleanClause booleanClause = new BooleanClause(booleanQuery, occur);
        query.add(booleanClause);

        // Search only for metadata (no template or sub-templates) if not set by search criteria before
        if (!templateCriteriaAdded) {
            occur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
            Query q = new TermQuery(new Term(LuceneIndexField.IS_TEMPLATE, "n"));
            query.add(q, occur);
            templateCriteriaAdded = true;
        }
        return query;
    }

    /**
     * TODO Javadoc.
     *
     * @param fieldValue
     * @param similarity
     * @param singleTokenOccur
     * @param moreTokensOccur
     * @return
     */
    private BooleanClause tokenizeSearchParam(String fieldValue, String similarity, Occur singleTokenOccur, Occur moreTokensOccur) {
        // tokenize searchParam
        BooleanClause anyClause = null;
        StringTokenizer st = new StringTokenizer(fieldValue, STRING_TOKENIZER_DELIMITER);
        if (st.countTokens() == 1) {
            String token = st.nextToken();
            Query subQuery = textFieldToken(token, LuceneIndexField.ANY, similarity);
            if (subQuery != null) {
                anyClause = new BooleanClause(subQuery, singleTokenOccur);
            }
        }
        else {
            BooleanQuery orBooleanQuery = new BooleanQuery();
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                Query subQuery = textFieldToken(token, LuceneIndexField.ANY, similarity);
                if (subQuery != null) {
                    BooleanClause subClause = new BooleanClause(subQuery, moreTokensOccur);
                    orBooleanQuery.add(subClause);
                }
            }
            anyClause = new BooleanClause(orBooleanQuery, singleTokenOccur);
        }
        return anyClause;
    }

    /**
     * Builds a query where AND operator is used for each search criteria.
     *
     * @param searchCriteria
     * @param query
     * @param similarity
     * @param processedRangeFields
     * @return
     */
    private BooleanQuery buildANDQuery(Map<String, Set<String>> searchCriteria, BooleanQuery query, String similarity,
                                       Set<String> processedRangeFields) {

        for (Entry<String, Set<String>> searchCriterium : searchCriteria.entrySet()) {
            String fieldName = searchCriterium.getKey();
            Set<String> fieldValues = searchCriterium.getValue();
            addANDCriteria(fieldName, fieldValues, similarity, query, searchCriteria, processedRangeFields);
        }

        // Search only for metadata (no template or sub-templates) if not set by search criteria before
        if (!templateCriteriaAdded) {
            BooleanClause.Occur occur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
            Query q = new TermQuery(new Term(LuceneIndexField.IS_TEMPLATE, "n"));
            query.add(q, occur);
            templateCriteriaAdded = true;
        }
        return query;
    }

    private void addANDCriteria(String fieldName, Set<String> fieldValues, String similarity, BooleanQuery query,
                                Map<String, Set<String>> searchCriteria, Set<String> processedRangeFields) {
        BooleanQuery bq = new BooleanQuery();
        BooleanClause.Occur qOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);

        // Avoid search by field who control privileges
        if (UserQueryInput.SECURITY_FIELDS.contains(fieldName) || SearchParameter.EDITABLE.equals(fieldName)) {
            return;
        }

        // whether a set of values is defined for this criteria
        boolean criteriaIsASet = fieldValues.size() > 1;

        // For each field value add a clause to the main query if not a set or no multi value like spatial search or
        // add to the boolean query (will be added to the main query after looping over all values).
        for (String fieldValue : fieldValues) {
            if (LuceneIndexField.ANY.equals(fieldName)) {
                addAnyTextQuery(fieldValue, similarity, (criteriaIsASet ? bq : query));
            }
            else if (LuceneIndexField.UUID.equals(fieldName) || SearchParameter.UUID.equals(fieldName)) {
                addUUIDQuery(fieldValue, similarity, criteriaIsASet, bq, query);
            }
            else if (LuceneIndexField.NORTH.equals(fieldName)
                    || LuceneIndexField.SOUTH.equals(fieldName)
                    || LuceneIndexField.EAST.equals(fieldName)
                    || LuceneIndexField.WEST.equals(fieldName)
                    || SearchParameter.RELATION.equals(fieldName)) {
                addBBoxQuery(searchCriteria, query);
            }
            // template
            else if (LuceneIndexField.IS_TEMPLATE.equals(fieldName) || SearchParameter.TEMPLATE.equals(fieldName)) {
                templateCriteria(fieldValue, query);
            }
            // all -- mapped to same Lucene field as 'any'
            else if ("all".equals(fieldName)) {
                addRequiredTextField(fieldValue, LuceneIndexField.ANY, similarity, (criteriaIsASet ? bq : query));
            }
            // or
            else if ("or".equals(fieldName)) {
                addNotRequiredTextField(fieldValue, LuceneIndexField.ANY, similarity, (criteriaIsASet ? bq : query));
            }
            // without
            else if ("without".equals(fieldName)) {
                addProhibitedTextField(fieldValue, LuceneIndexField.ANY, (criteriaIsASet ? bq : query));
            }
            // phrase
            else if ("phrase".equals(fieldName)) {
                phraseCriteria(fieldValue, query, qOccur);
            }
            // temporal
            else if (SearchParameter.EXTFROM.equals(fieldName) || SearchParameter.EXTTO.equals(fieldName)) {
                if (!temporalCriteriaAdded) {
                    temporalCriteriaAdded = temporalCriteria(searchCriteria, query);
                }
            }
            // range
            else if (UserQueryInput.RANGE_QUERY_FIELDS.contains(fieldName)) {
                rangeCriteria(searchCriteria, fieldName, query,processedRangeFields);
            }
            // featured
            else if (SearchParameter.FEATURED.equals(fieldName)) {
                featuredCriteria(fieldValue, bq);
            }
            else {
                if(criteriaIsASet) {
                    // Add to the boolean query which will be added to the main query
                    addNotRequiredTextField(fieldValue, fieldName, similarity, bq);
                }
                else if (fieldValue.contains(OR_SEPARATOR)) {
                    // TODO : change OR separator
                    // Add all separated values to the boolean query
                    addSeparatedTextField(fieldValue, OR_SEPARATOR, fieldName, bq);
                }
                else {
                    // Add the field to main query
                    addRequiredTextField(fieldValue, fieldName, similarity, query);
                }
            }
        }

        // Add the boolean query created for the set of values for the current criteria
        if (bq.clauses().size() > 0) {
            query.add(bq, qOccur);
        }
    }

    /**
     * Adds featured searchterm to query.
     *
     * @param fieldValue
     * @param bq
     */
    private void featuredCriteria(String fieldValue, BooleanQuery bq) {
        if ("true".equals(fieldValue)) {
            BooleanClause.Occur featuredOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);

            TermQuery featuredQuery = new TermQuery(new Term(LuceneIndexField._OP6, "1"));
            BooleanClause featuredClause = new BooleanClause(featuredQuery, featuredOccur);
            bq.add(featuredClause);

            // featured needs to be visible to all.
            TermQuery viewQuery = new TermQuery(new Term(LuceneIndexField._OP0, "1"));
            BooleanClause viewClause = new BooleanClause(viewQuery, featuredOccur);
            bq.add(viewClause);
        }
    }
    /**
     * Handles range query fields. Boolean query could not be made in the same range query field.
     */
    private void rangeCriteria(Map<String, Set<String>> searchCriteria, String fieldName, BooleanQuery query, Set<String> processedRangeFields) {
        String rangeQueryField = UserQueryInput.getRangeField(fieldName);

        if (!processedRangeFields.contains(rangeQueryField)) {
            Set<String> from;
            Set<String> to;
            if (rangeQueryField.equals(fieldName)) {
                from = searchCriteria.get(rangeQueryField);
                to = from;
            }
            else {
                from = searchCriteria.get(UserQueryInput.getFrom(rangeQueryField));
                to = searchCriteria.get(UserQueryInput.getTo(rangeQueryField));
            }
            // create range query
            addRangeQuery(query,
                    (from != null ? (String) from.toArray()[0]
                            : null),
                    (to != null ? (String) to.toArray()[0] : null),
                    rangeQueryField);

            // Remove upper or lower range field
            processedRangeFields.add(rangeQueryField);
        }
    }

    /**
     * Adds phrase searchterm to query.
     *
     * @param fieldValue
     * @param query
     * @param qOccur
     */
    private void phraseCriteria(String fieldValue, BooleanQuery query, BooleanClause.Occur qOccur) {
        PhraseQuery phraseQ = null;
        // a set of phrase is not supported
        if (StringUtils.isNotBlank(fieldValue)) {
            phraseQ = new PhraseQuery();
            qOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
            // tokenize phrase
            StringTokenizer st = new StringTokenizer(fieldValue, STRING_TOKENIZER_DELIMITER);
            while (st.hasMoreTokens()) {
                String phraseElement = st.nextToken();
                phraseElement = phraseElement.trim().toLowerCase();
                (phraseQ).add(new Term(LuceneIndexField.ANY, phraseElement));
            }
        }
        if (phraseQ != null) {
            query.add(phraseQ, qOccur);
        }
    }

    /**
     * Adds template searchterm to query.
     *
     * @param fieldValue
     * @param query
     */
    private void templateCriteria(String fieldValue, BooleanQuery query) {
        if(! templateCriteriaAdded) {
            BooleanClause.Occur templateOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);

            Query templateQ;
            if (fieldValue != null) {
                if (fieldValue.contains(OR_SEPARATOR)) {
                    templateQ = new BooleanQuery();
                    addSeparatedTextField(fieldValue, OR_SEPARATOR, LuceneIndexField.IS_TEMPLATE, (BooleanQuery) templateQ);
                } else if (fieldValue.equals("y") || fieldValue.equals("s")) {
                    templateQ = new TermQuery(new Term(LuceneIndexField.IS_TEMPLATE, fieldValue));
                } else {
                    templateQ = new TermQuery(new Term(LuceneIndexField.IS_TEMPLATE, "n"));
                }
            } else {
                templateQ = new TermQuery(new Term(LuceneIndexField.IS_TEMPLATE, "n"));
            }
            query.add(templateQ, templateOccur);

            templateCriteriaAdded = true;
        }
    }

    /**
     * Adds temporal searchterms to query.
     *
     * @param searchCriteria
     * @param query
     * @return
     */
    private boolean temporalCriteria(Map<String, Set<String>> searchCriteria, BooleanQuery query) {
        //
        // Temporal extent : finds records where temporal extent overlaps the search extent
        //
        Set<String> from = searchCriteria.get(SearchParameter.EXTFROM);
        Set<String> to = searchCriteria.get(SearchParameter.EXTTO);

        String extTo = to != null ? (String) to.toArray()[0] : null;
        String extFrom = from != null ? (String) from.toArray()[0] : null;

        if (StringUtils.isNotBlank(extTo) || StringUtils.isNotBlank(extFrom)) {
            BooleanQuery temporalExtentQuery = new BooleanQuery();
            BooleanClause.Occur temporalExtentOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
            BooleanClause.Occur temporalRangeQueryOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);

            TermRangeQuery temporalRangeQuery;

            // temporal extent start is within search extent
            temporalRangeQuery = TermRangeQuery.newStringRange(LuceneIndexField.TEMPORALEXTENT_BEGIN, extFrom, extTo, true, true);
            BooleanClause temporalRangeQueryClause = new BooleanClause(temporalRangeQuery, temporalRangeQueryOccur);

            temporalExtentQuery.add(temporalRangeQueryClause);

            // or temporal extent end is within search extent
            temporalRangeQuery = TermRangeQuery.newStringRange(LuceneIndexField.TEMPORALEXTENT_END, extFrom, extTo, true, true);
            temporalRangeQueryClause = new BooleanClause(temporalRangeQuery, temporalRangeQueryOccur);

            temporalExtentQuery.add(temporalRangeQueryClause);

            // or temporal extent contains search extent
            if (StringUtils.isNotBlank(extTo) && StringUtils.isNotBlank(extFrom)) {
                BooleanQuery tempQuery = new BooleanQuery();

                temporalRangeQuery = TermRangeQuery.newStringRange(LuceneIndexField.TEMPORALEXTENT_END, extTo, null, true, true);
                temporalRangeQueryClause = new BooleanClause(temporalRangeQuery, temporalExtentOccur);

                tempQuery.add(temporalRangeQueryClause);

                temporalRangeQuery = TermRangeQuery.newStringRange(LuceneIndexField.TEMPORALEXTENT_BEGIN, null, extFrom, true, true);
                temporalRangeQueryClause = new BooleanClause(temporalRangeQuery, temporalExtentOccur);
                tempQuery.add(temporalRangeQueryClause);

                temporalExtentQuery.add(tempQuery, temporalRangeQueryOccur);
            }

            if (temporalExtentQuery.clauses().size() > 0) {
                temporalRangeQueryClause = new BooleanClause(temporalExtentQuery, temporalExtentOccur);
                query.add(temporalRangeQueryClause);
            }
        }
        return true;
    }

    /**
     * Creates a query for a string. If the string contains a wildcard (* or ?),
     * similarity is ignored.
     * 
     * @param string
     *            string
     * @param luceneIndexField
     *            index field
     * @param similarity
     *            fuzziness
     * @return query
     */
    private Query textFieldToken(String string, String luceneIndexField,
            String similarity) {
        if (string == null) {
            throw new IllegalArgumentException("Cannot create Lucene query for null string");
        }
        Query query = null;

        String analyzedString;
        // wildcards - preserve them by analyzing the parts of the search string
        // around them separately
        // (this is because Lucene's StandardTokenizer would remove wildcards,
        // but that's not what we want)
        if (string.indexOf('*') >= 0 || string.indexOf('?') >= 0) {
            WildCardStringAnalyzer wildCardStringAnalyzer = new WildCardStringAnalyzer();
            analyzedString = wildCardStringAnalyzer.analyze(string, luceneIndexField, _analyzer, _tokenizedFieldSet);
        }
        // no wildcards
        else {
            analyzedString = LuceneSearcher.analyzeQueryText(luceneIndexField, string, _analyzer, _tokenizedFieldSet);
        }

        query = constructQueryFromAnalyzedString(string, luceneIndexField, similarity, query, analyzedString, _tokenizedFieldSet);
        return query;
    }

    static Query constructQueryFromAnalyzedString(String string, String luceneIndexField, String similarity, Query query,
            String analyzedString, Set<String> tokenizedFieldSet) {
        if (StringUtils.isNotBlank(analyzedString)) {
            // no wildcards
            if (string.indexOf('*') < 0 && string.indexOf('?') < 0) {
                if (tokenizedFieldSet.contains(luceneIndexField) && analyzedString.contains(" ")) {
                    // if analyzer creates spaces (by converting ignored
                    // characters like -) then make boolean query
                    String[] terms = analyzedString.split(" ");
                    BooleanQuery booleanQuery = new BooleanQuery();
                    query = booleanQuery;
                    for (String term : terms) {
                        booleanQuery.add(createFuzzyOrTermQuery(luceneIndexField, similarity, term), Occur.MUST);
                    }
                } else {
                    query = createFuzzyOrTermQuery(luceneIndexField, similarity, analyzedString);
                }
            }
            // wildcards
            else {
                query = new WildcardQuery(new Term(luceneIndexField, analyzedString));
            }
        }
        return query;
    }

    private static Query createFuzzyOrTermQuery(String luceneIndexField, String similarity, String analyzedString) {
        Query query = null;
        if (similarity != null) {
            Float minimumSimilarity = Float.parseFloat(similarity);
            
            if (minimumSimilarity < 1) {
                int maxEdits = Math.min((int) ((1D-minimumSimilarity) * 10),  LevenshteinAutomata.MAXIMUM_SUPPORTED_DISTANCE);
                query  = new FuzzyQuery(new Term(luceneIndexField, analyzedString), maxEdits);
            } else if (minimumSimilarity > 1){
                throw new IllegalArgumentException("similarity cannot be > 1.  The provided value was "+similarity);
            }
        }
        
        if (query == null) {
            query = new TermQuery(new Term(luceneIndexField, analyzedString));
        }
        return query;
    }

    /**
     * Add clause to a query for all tokens in the search param. The query must select
     * only results where none of the tokens in the search param is present.
     * 
     * Apply this to tokenized field.
     * 
     * @param searchParam search param
     * @param luceneIndexField index field
     * @param query query being built
     */
    private void addProhibitedTextField(String searchParam, String luceneIndexField, BooleanQuery query) {
        BooleanClause booleanClause = null;
        BooleanClause.Occur occur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
        BooleanClause.Occur dontOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, true);
		if (StringUtils.isNotBlank(searchParam)) {
			BooleanQuery booleanQuery = new BooleanQuery();
			MatchAllDocsQuery matchAllDocsQuery = new MatchAllDocsQuery();
			BooleanClause matchAllDocsClause = new BooleanClause(matchAllDocsQuery, occur);
			booleanQuery.add(matchAllDocsClause);
			// tokenize searchParam
			StringTokenizer st = new StringTokenizer(searchParam.trim(), STRING_TOKENIZER_DELIMITER);
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				// ignore fuzziness in without-queries
				Query subQuery = textFieldToken(token, luceneIndexField, null);
				if (subQuery != null) {
					BooleanClause subClause = new BooleanClause(subQuery, dontOccur);
					booleanQuery.add(subClause);
				}
			}
			booleanClause = new BooleanClause(booleanQuery, occur);
		}
        if (booleanClause != null) {
            query.add(booleanClause);
        }
    }

    /**
     * Add clause to a query for all tokens in the search param. 'Not required' does
     * not mean that this is not a required search parameter; rather it means
     * that if this parameter is present, the query must select results where at
     * least one of the tokens in the search param is present.
     * 
     * @param searchParam search param
     * @param luceneIndexField index field
     * @param similarity fuzziness
     * @param query query being built
     */
	private void addNotRequiredTextField(String searchParam, String luceneIndexField, String similarity, BooleanQuery query) {
		BooleanClause booleanClause = null;
		BooleanClause.Occur occur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
		BooleanClause.Occur tokenOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);
		if (StringUtils.isNotBlank(searchParam)) {
			if (!_tokenizedFieldSet.contains(luceneIndexField)) {
				// TODO : use similarity when needed
				TermQuery termQuery = new TermQuery(new Term(luceneIndexField, searchParam.trim()));
				BooleanClause clause = new BooleanClause(termQuery, occur);
				query.add(clause);
			}
            else {
				// tokenize searchParam
				StringTokenizer st = new StringTokenizer(searchParam.trim(), STRING_TOKENIZER_DELIMITER);
                BooleanQuery booleanQuery = new BooleanQuery();
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					Query subQuery = textFieldToken(token, luceneIndexField, similarity);
					if (subQuery != null) {
						BooleanClause subClause = new BooleanClause(subQuery, tokenOccur);
						booleanQuery.add(subClause);
					}
				}
				booleanClause = new BooleanClause(booleanQuery, occur);
			}
		}
		if (booleanClause != null) {
			query.add(booleanClause);
		}
	}

    /**
     * Add a clause to a query for all tokens in the search param. 'Required' does not
     * mean that this is a required search parameter; rather it means that if
     * this parameter is present, the query must select only results where each
     * of the tokens in the search param is present.
     * 
     * @param searchParam search parameter
     * @param luceneIndexField index field
     * @param similarity fuzziness
     * @param query query being built
     */
	private void addRequiredTextField(String searchParam, String luceneIndexField, String similarity, BooleanQuery query) {
		BooleanClause booleanClause = null;
		BooleanClause.Occur occur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
		if (StringUtils.isNotBlank(searchParam)) {
			if (!_tokenizedFieldSet.contains(luceneIndexField)) {
				// TODO : use similarity when needed
				BooleanClause clause = new BooleanClause(textFieldToken(searchParam, luceneIndexField, similarity), occur);
				query.add(clause);
			}
            else {
				// tokenize searchParam only if tokenized when indexing
				StringTokenizer st = new StringTokenizer(searchParam, STRING_TOKENIZER_DELIMITER);
				if (st.countTokens() == 1) {
					String token = st.nextToken();
					Query subQuery = textFieldToken(token, luceneIndexField, similarity);
					if (subQuery != null) {
						booleanClause = new BooleanClause(subQuery, occur);
					}
				}
                else {
					BooleanQuery booleanQuery = new BooleanQuery();
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						Query subQuery = textFieldToken(token, luceneIndexField, similarity);
						if (subQuery != null) {
							BooleanClause subClause = new BooleanClause(subQuery, occur);
							booleanQuery.add(subClause);
						}
					}
					booleanClause = new BooleanClause(booleanQuery, occur);
				}
			}
		}
		if (booleanClause != null) {
			query.add(booleanClause);
		}
	}

    /**
     * Add clause to a query for all tokens between the separator.
     * 
     * @param text text
     * @param separator separator
     * @param fieldName Lucene field name
     * @param query query being built
     */
    private void addSeparatedTextField(String text, String separator, String fieldName, BooleanQuery query) {

        if (StringUtils.isNotBlank(text)) {
            BooleanClause.Occur occur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);

            for (String token : Splitter.on(separator).trimResults().split(text)) {
                // TODO : here we should use similarity if set
                Query subQuery = textFieldToken(token, fieldName, null);
                // The subquery may be null if the analyzed string is null
                if (subQuery != null) {
                    BooleanClause clause = new BooleanClause(subQuery, occur);
                    query.add(clause);
                }
            }
        }
    }


	/**
	 * Add any field clause to a query.
	 * 
	 * @param any
	 * @param similarity
	 * @param query
	 */
	private void addAnyTextQuery(String any, String similarity, BooleanQuery query) {
		BooleanClause anyClause = null;
		BooleanClause.Occur occur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
		if (StringUtils.isNotBlank(any) && !onlyWildcard(any)) {
            anyClause = tokenizeSearchParam(any, similarity, occur, occur);
		}
		if (anyClause != null) {
			query.add(anyClause);
		}
	}

    private void addUUIDQuery(String fieldValue, String similarity, boolean criteriaIsASet, BooleanQuery bq, BooleanQuery query)  {
        // the uuid param is an 'or' separated list. Remove the 'or's and handle like an 'or' query if more
        // than one uuid parameter is set, then a 'and' query is made
        if (fieldValue.contains(OR_SEPARATOR)) {
            // Add all separated values to the boolean query
            BooleanQuery uuidBooleanQuery = new BooleanQuery();
            addSeparatedTextField(fieldValue, OR_SEPARATOR, LuceneIndexField.UUID, uuidBooleanQuery);
            BooleanClause booleanClause = new BooleanClause(uuidBooleanQuery, BooleanClause.Occur.MUST);
            if (criteriaIsASet) {
                bq.add(booleanClause);
            }
            else {
                query.add(booleanClause);
            }
        }
        else {
            addNotRequiredTextField(fieldValue, LuceneIndexField.UUID, similarity, (criteriaIsASet ? bq : query));
        }
    }

    private void addBBoxQuery(Map<String, Set<String>> searchCriteria, BooleanQuery query) {
        // No multiple BBOX support
        if (!spatialCriteriaAdded) {
            Set<String> r = searchCriteria.get(SearchParameter.RELATION);
            Set<String> e = searchCriteria.get(SearchParameter.EASTBL);
            Set<String> w = searchCriteria.get(SearchParameter.WESTBL);
            Set<String> n = searchCriteria.get(SearchParameter.NORTHBL);
            Set<String> s = searchCriteria.get(SearchParameter.SOUTHBL);

            if (e != null && w != null && n != null && s != null) {
                addBoundingBoxQuery(query, (String) r.toArray()[0],
                        (String) e.toArray()[0],
                        (String) w.toArray()[0],
                        (String) n.toArray()[0],
                        (String) s.toArray()[0]);
            }
            spatialCriteriaAdded = true;
        }
    }

    /**
     * Add search privilege criteria to a query.
     * 
     * @param luceneQueryInput user and system input
     * @param query query being built
     */
    private void addPrivilegeQuery(LuceneQueryInput luceneQueryInput, BooleanQuery query) {
        // Set user groups privileges
        Set<String> groups = luceneQueryInput.getGroups();
        String editable$ = luceneQueryInput.getEditable();
        boolean editable = BooleanUtils.toBoolean(editable$);
        BooleanQuery groupsQuery = new BooleanQuery();
        boolean groupsQueryEmpty = true;
        BooleanClause.Occur groupOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);
        if (!CollectionUtils.isEmpty(groups)) {
            for (String group : groups) {
                if (StringUtils.isNotBlank(group)) {
                    if (!editable) {
                        // add to view
                        TermQuery viewQuery = new TermQuery(new Term(LuceneIndexField._OP0, group.trim()));
                        BooleanClause viewClause = new BooleanClause(viewQuery, groupOccur);
                        groupsQueryEmpty = false;
                        groupsQuery.add(viewClause);
                    }
                    // add to edit
                    TermQuery editQuery = new TermQuery(new Term(LuceneIndexField._OP2, group.trim()));
                    BooleanClause editClause = new BooleanClause(editQuery, groupOccur);
                    groupsQueryEmpty = false;
                    groupsQuery.add(editClause);
                }
            }
        }

        //
        // owner: this goes in groups query. This way if you are logged in you
        // can retrieve the results you are allowed
        // to see by your groups, plus any that you own not assigned to any
        // group.
        //
        String owner = luceneQueryInput.getOwner();
        if (owner != null) {
            TermQuery ownerQuery = new TermQuery(new Term(LuceneIndexField.OWNER, owner));
            BooleanClause.Occur ownerOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);
            BooleanClause ownerClause = new BooleanClause(ownerQuery, ownerOccur);
            groupsQueryEmpty = false;
            groupsQuery.add(ownerClause);
        }

        //
        // "dummy" -- to go in groups query, to retrieve everything for
        // Administrator users.
        //
        boolean admin = luceneQueryInput.getAdmin();
        if (admin) {
            TermQuery adminQuery = new TermQuery(new Term(LuceneIndexField.DUMMY, "0"));
            BooleanClause adminClause = new BooleanClause(adminQuery, groupOccur);
            groupsQueryEmpty = false;
            groupsQuery.add(adminClause);
        }

        // Add the privilege part of the query
        if (!groupsQueryEmpty) {
            BooleanClause.Occur groupsOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
            BooleanClause groupsClause = new BooleanClause(groupsQuery, groupsOccur);
            query.add(groupsClause);
        }
    }

    /**
     * Add a range query according to field type. If field type is numeric, then
     * a numeric range query is used. If not a default range query is uses.
     * 
     * Range query include lower and upper bounds by default.
     * 
     * @param query query being built
     * @param from begin of range
     * @param to end of range
     * @param luceneIndexField Lucene field
     */
    private void addRangeQuery(BooleanQuery query, String from, String to, String luceneIndexField) {
        if (from == null && to == null) {
            return;
        }
        LuceneConfig.LuceneConfigNumericField type = _numericFieldSet.get(luceneIndexField);
        if (type == null) {
            addTextRangeQuery(query, from, to, luceneIndexField);
        }
        else {
            addNumericRangeQuery(query, from, to, true, true, luceneIndexField, true);
        }
    }

    /**
     * Add a numeric range query according to field numeric type.
     * 
     * @param query query being built
     * @param min minimum in range
     * @param max maximum in range
     * @param minInclusive whether minimum is inclusive
     * @param maxExclusive whether maximum is inclusive
     * @param luceneIndexField Lucene field
     * @param required whether this is a required query clause
     *            TODO
     */
    private void addNumericRangeQuery(BooleanQuery query, String min, String max, boolean minInclusive,
                                      boolean maxExclusive, String luceneIndexField, boolean required) {
        if (min != null && max != null) {
            String type = _numericFieldSet.get(luceneIndexField).getType();

            NumericRangeQuery<? extends Number> rangeQuery = buildNumericRangeQueryForType(luceneIndexField, min, max, minInclusive, maxExclusive, type);

            BooleanClause.Occur denoOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(required, false);
            BooleanClause rangeClause = new BooleanClause(rangeQuery, denoOccur);

            query.add(rangeClause);
        }
    }

    public static NumericRangeQuery<? extends Number> buildNumericRangeQueryForType(String fieldName, String min, String max,
                                                                  boolean minInclusive, boolean maxInclusive, String type) {
        NumericRangeQuery<? extends Number> rangeQuery;
        if ("double".equals(type)) {
            rangeQuery = NumericRangeQuery.newDoubleRange(fieldName,
                    (min == null ? Double.MIN_VALUE : Double.parseDouble(min)),
                    (max == null ? Double.MAX_VALUE : Double.parseDouble(max)),
                    true, true);

        }
        else if ("float".equals(type)) {
            rangeQuery = NumericRangeQuery.newFloatRange(fieldName,
                    (min == null ? Float.MIN_VALUE : Float.parseFloat(min)),
                    (max == null ? Float.MAX_VALUE : Float.parseFloat(max)), true,
                    true);
        }
        else if ("long".equals(type)) {
            rangeQuery = NumericRangeQuery.newLongRange(fieldName,
                    (min == null ? Long.MIN_VALUE : Long.parseLong(min)),
                    (max == null ? Long.MAX_VALUE : Long.parseLong(max)), true,
                    true);
        }
        else {
            rangeQuery = NumericRangeQuery.newIntRange(fieldName,
                    (min == null ? Integer.MIN_VALUE : Integer.parseInt(min)),
                    (max == null ? Integer.MAX_VALUE : Integer.parseInt(max)),
                    true, true);
        }
        return rangeQuery;
    }

    /**
     * Add a date range query for a text field type.
     * 
     * @param query query being built
     * @param dateTo end of range
     * @param dateFrom start of range
     * @param luceneIndexField Lucene field
     */
    private void addTextRangeQuery(BooleanQuery query, String dateFrom, String dateTo, String luceneIndexField) {
        if (StringUtils.isNotBlank(dateTo) || StringUtils.isNotBlank(dateFrom)) {
            TermRangeQuery rangeQuery;
            if (dateTo != null) {
                // while the 'from' parameter can be short (like yyyy-mm-dd)
                // the 'until' parameter must be long to match
                if (dateTo.length() == 10) {
                    dateTo = dateTo + "T23:59:59";
                }
            }
            rangeQuery = TermRangeQuery.newStringRange(luceneIndexField, dateFrom, dateTo, true, true);
            BooleanClause.Occur dateOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
            BooleanClause dateRangeClause = new BooleanClause(rangeQuery, dateOccur);
            query.add(dateRangeClause);
        }
    }

    /**
     * TODO Javadoc.
     *
     * @param query
     * @param westBL
     * @param minWestBL
     * @param maxWestBL
     * @param westBLIndexField
     * @param eastBL
     * @param minEastBL
     * @param maxEastBL
     * @param eastBLIndexField
     * @param southBL
     * @param minSouthBL
     * @param maxSouthBL
     * @param southBLIndexField
     * @param northBL
     * @param minNorthBL
     * @param maxNorthBL
     * @param northBLIndexField
     * @param inclusive
     */
    private void addLatLongQuery(BooleanQuery query, String westBL, String minWestBL, String maxWestBL, String westBLIndexField,
                                 String eastBL, String minEastBL, String maxEastBL, String eastBLIndexField,
                                 String southBL, String minSouthBL, String maxSouthBL, String southBLIndexField,
                                 String northBL, String minNorthBL, String maxNorthBL, String northBLIndexField,
                                 boolean inclusive, boolean required) {
        if (eastBL != null) {
            addNumericRangeQuery(query, minEastBL, maxEastBL, inclusive, inclusive, eastBLIndexField, required);
        }
        if (westBL != null) {
            addNumericRangeQuery(query, minWestBL, maxWestBL, inclusive, inclusive, westBLIndexField, required);
        }
        if (southBL != null) {
            addNumericRangeQuery(query, minSouthBL, maxSouthBL, inclusive, inclusive, southBLIndexField, required);
        }
        if (northBL != null) {
            addNumericRangeQuery(query, minNorthBL, maxNorthBL, inclusive, inclusive, northBLIndexField, required);
        }
    }

    /**
     * Handle geographical search using Lucene.
     * 
     * East, North, South and West bounds are indexed as numeric in Lucene.
     * 
     * Lucene bounding box searches are probably faster than using spatial index
     * using geometry criteria. It does not support complex geometries and all
     * type of relation.
     * 
     * If metadata contains multiple bounding boxes invalid results may appear.
     * 
     * If relation is null or is not a known relation type (See
     * {@link org.fao.geonet.constants.Geonet.SearchResult.Relation}), overlap
     * is used.
     * 
     * @param query query being built
     * @param relation spatial relation
     * @param eastBL east
     * @param westBL west
     * @param northBL north
     * @param southBL south
     */
    private void addBoundingBoxQuery(BooleanQuery query, String relation, String eastBL, String westBL, String northBL, String southBL) {

        // Default inclusive value for RangeQuery (includeLower and
        // includeUpper)
        boolean inclusive = true;

        if (relation == null || relation.equals(Geonet.SearchResult.Relation.OVERLAPS)) {
            //
            // overlaps (default value) : uses the equivalence
            // -(a + b + c + d) = -a * -b * -c * -d
            //
            addLatLongQuery(query, westBL, westBL, String.valueOf(maxBoundingLongitudeValue), LuceneIndexField.EAST,
                    eastBL, String.valueOf(minBoundingLongitudeValue), eastBL, LuceneIndexField.WEST,
                    southBL, southBL, String.valueOf(maxBoundingLatitudeValue), LuceneIndexField.NORTH,
                    northBL, String.valueOf(minBoundingLatitudeValue), northBL, LuceneIndexField.SOUTH, inclusive, true);
        }
        //
        // equal: coordinates of the target rectangle within 1 degree from
        // corresponding ones of metadata rectangle
        //
        else if (relation.equals(Geonet.SearchResult.Relation.EQUAL)) {
            addLatLongQuery(query, westBL, westBL, westBL, LuceneIndexField.WEST,
                    eastBL, eastBL, eastBL, LuceneIndexField.EAST,
                    southBL, southBL, southBL, LuceneIndexField.SOUTH,
                    northBL, northBL, northBL, LuceneIndexField.NORTH,  inclusive, true);
        }
        //
        // encloses: metadata rectangle encloses target rectangle
        //
        else if (relation.equals(Geonet.SearchResult.Relation.ENCLOSES)) {
            addLatLongQuery(query, westBL, String.valueOf(minBoundingLongitudeValue), westBL, LuceneIndexField.WEST,
                    eastBL, eastBL, String.valueOf(maxBoundingLongitudeValue), LuceneIndexField.EAST,
                    southBL, String.valueOf(minBoundingLatitudeValue), southBL, LuceneIndexField.SOUTH,
                    northBL, northBL, String.valueOf(maxBoundingLatitudeValue), LuceneIndexField.NORTH, inclusive, true);
        }
        //
        // fullyEnclosedWithin: metadata rectangle fully enclosed within target
        // rectangle
        //
        else if (relation.equals(Geonet.SearchResult.Relation.ENCLOSEDWITHIN)) {
            addLatLongQuery(query, westBL, westBL, eastBL, LuceneIndexField.WEST,
                    eastBL, westBL, eastBL, LuceneIndexField.EAST,
                    southBL, southBL, northBL, LuceneIndexField.SOUTH,
                    northBL, southBL, northBL, LuceneIndexField.NORTH, inclusive, true);
        }
        //
        // fullyOutsideOf: one or more of the 4 forbidden halfplanes contains
        // the metadata
        // rectangle, that is, not true that all the 4 forbidden halfplanes do
        // not contain the metadata rectangle
        else if (relation.equals(Geonet.SearchResult.Relation.OUTSIDEOF)) {
            addLatLongQuery(query, westBL, String.valueOf(minBoundingLongitudeValue), westBL, LuceneIndexField.EAST,
                    eastBL, eastBL, String.valueOf(maxBoundingLongitudeValue), LuceneIndexField.WEST,
                    southBL, String.valueOf(minBoundingLatitudeValue), southBL, LuceneIndexField.NORTH,
                    northBL, northBL, String.valueOf(maxBoundingLatitudeValue), LuceneIndexField.SOUTH, inclusive, false);
        }
    }

    /**
     * Whether a string equals the wildcard *.
     * @param s
     * @return
     */
    private boolean onlyWildcard(String s) {
        return "*".equals(StringUtils.trim(s));
    }

    /**
     * TODO javadoc.
     *
     *
     * @param query
     * @param langCode
     * @param requestedLanguageOnly
     * @return
     */
    static Query addLocaleTerm( Query query, String langCode, SettingInfo.SearchRequestLanguage requestedLanguageOnly ) {
        if (langCode == null || requestedLanguageOnly == null) {
            return query;
        }

        BooleanQuery booleanQuery;
        if (query instanceof BooleanQuery) {
            booleanQuery = (BooleanQuery) query;
        }
        else {
            booleanQuery = new BooleanQuery();
            booleanQuery.add(query, BooleanClause.Occur.MUST);
        }

        requestedLanguageOnly.addQuery(booleanQuery, langCode);
        return booleanQuery;
    }
    
    private static class DrillDownPath {
        private final String dimension;
        private final String[] path;

        private static final String DRILLDOWN_PATH_SEPARATOR = "/";

        public DrillDownPath(String drillDownPath) {
            dimension = getDimension(drillDownPath);
            path = getPath(drillDownPath);
        }

        private String getDimension(String drillDownPath) {
            String[] drilldownQueryComponents = drillDownPath.split(DRILLDOWN_PATH_SEPARATOR);
            return drilldownQueryComponents[0];
        }

        private String[] getPath(String drillDownPath) {
            String[] components = drillDownPath.split(DRILLDOWN_PATH_SEPARATOR);
            String[] result = new String[components.length - 1];

            for (int i=1; i<components.length; i++) {
                try {
                    result[i-1] = URLDecoder.decode(components[i], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }

            return result;
        }

        public String getDimension() {
            return dimension;
        }

        public String[] getPath() {
            return path;
        }

    }
}
