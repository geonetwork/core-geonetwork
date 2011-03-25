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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import jeeves.utils.Log;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.fao.geonet.constants.Geonet;

import com.google.common.base.Splitter;

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
    private Set<String> _tokenizedFieldSet;
    private PerFieldAnalyzerWrapper _analyzer;
    private Map<String, LuceneConfig.LuceneConfigNumericField> _numericFieldSet;

    // Lat long bounding box constants
    static final Double minBoundingLatitudeValue = -90.0;
    static final Double maxBoundingLatitudeValue = 90.0;
    static final Double minBoundingLongitudeValue = -180.0;
    static final Double maxBoundingLongitudeValue = 180.0;

    public LuceneQueryBuilder(Set<String> tokenizedFieldSet,
            Map<String, LuceneConfig.LuceneConfigNumericField> numericFieldSet,
            PerFieldAnalyzerWrapper analyzer) {
        _tokenizedFieldSet = tokenizedFieldSet;
        _numericFieldSet = numericFieldSet;
        _analyzer = analyzer;
    }

    /**
     * Build a Lucene query for the {@link LuceneQueryInput}.
     * A AND clause is used for each search criteria and
     * a OR clause if the content of a criteria is "this or that".
     * 
     * Some search criteria does not support multi-occurences
     * like spatial, temporal criteria or range fields.
     * 
     * @param luceneQueryInput
     * @return
     */
    public Query build(LuceneQueryInput luceneQueryInput) {
        Log.debug(Geonet.SEARCH_ENGINE,
                "LuceneQueryBuilder: luceneQueryInput is: \n"
                        + luceneQueryInput.toString());

        // Remember which range fields have been processed
        HashSet<String> processedRangeFields = new HashSet<String>();

        // top query to hold all sub-queries for each search parameter
        BooleanQuery query = new BooleanQuery();

        // Template = "n" is added if not set in search criteria
        boolean templateCriteriaAdded = false;

        // Only one spatial search criteria could be added.
        boolean spatialCriteriaAdded = false;

        // Only one temporal search criteria could be added
        boolean temporalCriteriaAdded = false;

        // Filter according to user session
        addPrivilegeQuery(luceneQueryInput, query);

        // similarity is passed to textfield-query-creating methods
        String similarity = luceneQueryInput.getSimilarity();

        Map<String, HashSet<String>> searchCriteria = luceneQueryInput
                .getSearchCriteria();

        //
        // AND operator is used for each search criteria
        //
        for (Iterator iter = searchCriteria.entrySet().iterator(); iter
                .hasNext();) {
            Query q = null;
            BooleanQuery bq = new BooleanQuery();
            BooleanClause.Occur qOccur = LuceneUtils
                    .convertRequiredAndProhibitedToOccur(true, false);

            Entry entry = (Entry) iter.next();
            String fieldName = (String) entry.getKey();
            HashSet<String> fieldValues = (HashSet<String>) entry.getValue();

            // Avoid search by field who control privileges
            if (UserQueryInput.SECURITY_FIELDS.contains(fieldName)
                    || SearchParameter.EDITABLE.equals(fieldName)) {
                continue;
            }

            // If a set of values is defined for this criteria
            boolean criteriaIsASet = fieldValues.size() > 1;

            for (String fieldValue : fieldValues) {

                // For each field value add a clause to the main query if not set or no multi value
                // like spatial search or add to the boolean query (added to the main query
                // after looping over all values).
                if (LuceneIndexField.ANY.equals(fieldName)) {
                    addAnyTextQuery(fieldValue, similarity,
                            (criteriaIsASet ? bq : query));
                } else if (LuceneIndexField.UUID.equals(fieldName)
                        || SearchParameter.UUID.equals(fieldName)) {
                    // the uuid param is an 'or' separated list. Remove the
                    // 'or's and handle like an 'or' query
                    // if more than one uuid parameter is set, then a 'and'
                    // query is made
                    fieldValue = fieldValue.replaceAll("\\sor\\s", " ");
                    addNotRequiredTextField(fieldValue, LuceneIndexField.UUID,
                            similarity, (criteriaIsASet ? bq : query));
                } else if (LuceneIndexField.NORTH.equals(fieldName)
                        || LuceneIndexField.SOUTH.equals(fieldName)
                        || LuceneIndexField.EAST.equals(fieldName)
                        || LuceneIndexField.WEST.equals(fieldName)
                        || SearchParameter.RELATION.equals(fieldName)) {
                    // No multiple BBOX support
                    if (!spatialCriteriaAdded) {
                        HashSet<String> r = searchCriteria
                                .get(SearchParameter.RELATION);
                        HashSet<String> e = searchCriteria
                                .get(SearchParameter.EASTBL);
                        HashSet<String> w = searchCriteria
                                .get(SearchParameter.WESTBL);
                        HashSet<String> n = searchCriteria
                                .get(SearchParameter.NORTHBL);
                        HashSet<String> s = searchCriteria
                                .get(SearchParameter.SOUTHBL);

                        if (e != null && w != null && n != null && s != null) {
                            addBoundingBoxQuery(query, (String) r.toArray()[0],
                                    (String) e.toArray()[0],
                                    (String) w.toArray()[0],
                                    (String) n.toArray()[0],
                                    (String) s.toArray()[0]);
                        }
                        spatialCriteriaAdded = true;
                    }
                } else if (LuceneIndexField.IS_TEMPLATE.equals(fieldName)
                        || SearchParameter.TEMPLATE.equals(fieldName)) {
                    //
                    // template
                    //
                    qOccur = LuceneUtils.convertRequiredAndProhibitedToOccur(
                            true, false);

                    if (fieldValue != null && (fieldValue.equals("y") || fieldValue.equals("s"))) {
                        q = new TermQuery(new Term(
                                LuceneIndexField.IS_TEMPLATE, fieldValue));
                    } else {
                        q = new TermQuery(new Term(
                                LuceneIndexField.IS_TEMPLATE, "n"));
                    }
                    query.add(q, qOccur);

                    templateCriteriaAdded = true;
                } else if ("all".equals(fieldName)) {
                    //
                    // all -- mapped to same Lucene field as 'any'
                    //
                    addRequiredTextField(fieldValue, LuceneIndexField.ANY,
                            similarity, (criteriaIsASet ? bq : query));
                } else if ("or".equals(fieldName)) {
                    //
                    // or
                    //
                    addNotRequiredTextField(fieldValue, LuceneIndexField.ANY,
                            similarity, (criteriaIsASet ? bq : query));
                } else if ("without".equals(fieldName)) {
                    //
                    // without
                    //
                    addProhibitedTextField(fieldValue, LuceneIndexField.ANY,
                            (criteriaIsASet ? bq : query));
                } else if ("phrase".equals(fieldName)) {
                    //
                    // phrase
                    //
                    // a set of phrase is not supported
                    if (fieldValue != null) {
                        if (fieldValue.length() > 0) {
                            q = new PhraseQuery();
                            qOccur = LuceneUtils
                                    .convertRequiredAndProhibitedToOccur(true,
                                            false);
                            // tokenize phrase
                            StringTokenizer st = new StringTokenizer(fieldValue);
                            while (st.hasMoreTokens()) {
                                String phraseElement = st.nextToken();
                                phraseElement = phraseElement.trim()
                                        .toLowerCase();
                                ((PhraseQuery) q).add(new Term(
                                        LuceneIndexField.ANY, phraseElement));
                            }
                        }
                    }
                    query.add(q, qOccur);
                } else if (SearchParameter.EXTFROM.equals(fieldName)
                        || SearchParameter.EXTTO.equals(fieldName)) {

                    if (!temporalCriteriaAdded) {
                        //
                        // Temporal extent : finds records where temporal extent
                        // overlaps the
                        // search extent
                        //
                        HashSet<String> from = searchCriteria
                                .get(SearchParameter.EXTFROM);
                        HashSet<String> to = searchCriteria
                                .get(SearchParameter.EXTTO);

                        String extTo = to != null ? (String) to.toArray()[0]
                                : null;
                        String extFrom = from != null ? (String) from.toArray()[0]
                                : null;

                        if ((extTo != null && extTo.length() > 0)
                                || (extFrom != null && extFrom.length() > 0)) {
                            BooleanQuery temporalExtentQuery = new BooleanQuery();
                            BooleanClause.Occur temporalExtentOccur = LuceneUtils
                                    .convertRequiredAndProhibitedToOccur(true,
                                            false);
                            BooleanClause.Occur temporalRangeQueryOccur = LuceneUtils
                                    .convertRequiredAndProhibitedToOccur(false,
                                            false);

                            TermRangeQuery temporalRangeQuery;

                            // temporal extent start is within search extent
                            temporalRangeQuery = new TermRangeQuery(
                                    LuceneIndexField.TEMPORALEXTENT_BEGIN,
                                    extFrom, extTo, true, true);
                            BooleanClause temporalRangeQueryClause = new BooleanClause(
                                    temporalRangeQuery, temporalRangeQueryOccur);

                            temporalExtentQuery.add(temporalRangeQueryClause);

                            // or temporal extent end is within search extent
                            temporalRangeQuery = new TermRangeQuery(
                                    LuceneIndexField.TEMPORALEXTENT_END,
                                    extFrom, extTo, true, true);
                            temporalRangeQueryClause = new BooleanClause(
                                    temporalRangeQuery, temporalRangeQueryOccur);

                            temporalExtentQuery.add(temporalRangeQueryClause);

                            // or temporal extent contains search extent
                            if ((extTo != null && extTo.length() > 0)
                                    && (extFrom != null && extFrom.length() > 0)) {
                                BooleanQuery tempQuery = new BooleanQuery();

                                temporalRangeQuery = new TermRangeQuery(
                                        LuceneIndexField.TEMPORALEXTENT_END,
                                        extTo, null, true, true);
                                temporalRangeQueryClause = new BooleanClause(
                                        temporalRangeQuery, temporalExtentOccur);

                                tempQuery.add(temporalRangeQueryClause);

                                temporalRangeQuery = new TermRangeQuery(
                                        LuceneIndexField.TEMPORALEXTENT_BEGIN,
                                        null, extFrom, true, true);
                                temporalRangeQueryClause = new BooleanClause(
                                        temporalRangeQuery, temporalExtentOccur);
                                tempQuery.add(temporalRangeQueryClause);

                                temporalExtentQuery.add(tempQuery,
                                        temporalRangeQueryOccur);
                            }

                            if (temporalExtentQuery.clauses().size() > 0) {
                                temporalRangeQueryClause = new BooleanClause(
                                        temporalExtentQuery,
                                        temporalExtentOccur);
                                query.add(temporalRangeQueryClause);
                            }
                        }
                        temporalCriteriaAdded = true;
                    }
                } else if (UserQueryInput.RANGE_QUERY_FIELDS
                        .contains(fieldName)) {
                    // range query fields
                    // Boolean query could not be made in the same range query
                    // field
                    String rangeQueryField = UserQueryInput
                            .getRangeField(fieldName);

                    if (!processedRangeFields.contains(rangeQueryField)) {
                        HashSet<String> from = null;
                        HashSet<String> to = null;
                        if (rangeQueryField == fieldName) {
                            from = searchCriteria.get(rangeQueryField);
                            to = from;
                        } else {
                            from = searchCriteria
                                    .get(luceneQueryInput.getFrom(rangeQueryField));
                            to = searchCriteria
                                    .get(luceneQueryInput.getTo(rangeQueryField));
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
                } else if (SearchParameter.FEATURED.equals(fieldName)) {
                    //
                    // featured
                    //
                    if ("true".equals(fieldValue)) {
                        BooleanClause.Occur featuredOccur = LuceneUtils
                                .convertRequiredAndProhibitedToOccur(true,
                                        false);

                        TermQuery featuredQuery = new TermQuery(new Term(
                                LuceneIndexField._OP6, "1"));
                        BooleanClause featuredClause = new BooleanClause(
                                featuredQuery, featuredOccur);
                        bq.add(featuredClause);

                        // featured needs to be visible to all.
                        TermQuery viewQuery = new TermQuery(new Term(
                                LuceneIndexField._OP0, "1"));
                        BooleanClause viewClause = new BooleanClause(viewQuery,
                                featuredOccur);
                        bq.add(viewClause);
                    }
                } else {
                    if (criteriaIsASet) {
                        // Add to the boolean query which will be added to the main query
                        addNotRequiredTextField(fieldValue, fieldName,
                                similarity, bq);
                    } else if (fieldValue.contains(OR_SEPARATOR)) {
                        // TODO : change OR separator
                        // Add all separated values to the boolean query
                        addSeparatedTextField(fieldValue, OR_SEPARATOR, fieldName, bq);
                    } else {
                        // Add the field to main query
                        addRequiredTextField(fieldValue, fieldName, similarity,
                                query);
                    }
                }
            }

            // Add the boolean query created for the set of values for the
            // current criteria
            if (bq.clauses().size() > 0) {
                query.add(bq, qOccur);
            }
        }

        // Search only for metadata (no template or sub-templates) if not set by
        // search criteria before
        if (!templateCriteriaAdded) {
            BooleanClause.Occur qOccur = LuceneUtils
                    .convertRequiredAndProhibitedToOccur(true, false);
            Query q = new TermQuery(new Term(LuceneIndexField.IS_TEMPLATE, "n"));
            query.add(q, qOccur);
        }

        return query;
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
            throw new IllegalArgumentException(
                    "Cannot create Lucene query for null string");
        }
        Query query = null;

        String analyzedString = "";
        // wildcards - preserve them by analyzing the parts of the search string
        // around them separately
        // (this is because Lucene's StandardTokenizer would remove wildcards,
        // but that's not what we want)
        if (string.indexOf('*') >= 0 || string.indexOf('?') >= 0) {
            String starsPreserved = "";
            String[] starSeparatedList = string.split("\\*");
            for (String starSeparatedPart : starSeparatedList) {
                String qPreserved = "";
                // ? present
                if (starSeparatedPart.indexOf('?') >= 0) {
                    String[] qSeparatedList = starSeparatedPart.split("\\?");
                    for (String qSeparatedPart : qSeparatedList) {
                        String analyzedPart = LuceneSearcher.analyzeQueryText(
                                luceneIndexField, qSeparatedPart, _analyzer,
                                _tokenizedFieldSet);
                        qPreserved += '?' + analyzedPart;
                    }
                    // remove leading ?
                    qPreserved = qPreserved.substring(1);
                    starsPreserved += '*' + qPreserved;
                }
                // no ? present
                else {
                    starsPreserved += '*' + LuceneSearcher.analyzeQueryText(
                            luceneIndexField, starSeparatedPart, _analyzer,
                            _tokenizedFieldSet);
                }
            }
            // remove leading *
            starsPreserved = starsPreserved.substring(1);

            // restore ending wildcard
            if (string.endsWith("*")) {
                starsPreserved += "*";
            } else if (string.endsWith("?")) {
                starsPreserved += "?";
            }

            analyzedString = starsPreserved;
        }
        // no wildcards
        else {
            analyzedString = LuceneSearcher.analyzeQueryText(luceneIndexField,
                    string, _analyzer, _tokenizedFieldSet);
        }

        if (StringUtils.isNotBlank(analyzedString)) {
            // no wildcards
            if (string.indexOf('*') < 0 && string.indexOf('?') < 0) {
                // similarity is not set or is 1
                if (similarity == null || similarity.equals("1")) {
                    query = new TermQuery(new Term(luceneIndexField,
                            analyzedString));
                }
                // similarity is not null and not 1
                else {
                    Float minimumSimilarity = Float.parseFloat(similarity);
                    query = new FuzzyQuery(new Term(luceneIndexField,
                            analyzedString), minimumSimilarity);
                }
            }
            // wildcards
            else {
                query = new WildcardQuery(new Term(luceneIndexField,
                        analyzedString));
            }
        }
        return query;
    }

    /**
     * Add clause to a query for all tokens in the search param. The query must select
     * only results where none of the tokens in the search param is present.
     * 
     * Apply this to tokenized field.
     * 
     * @param searchParam
     *            search param
     * @param luceneIndexField
     *            index field
     * @return boolean clause
     */
    private void addProhibitedTextField(String searchParam,
            String luceneIndexField, BooleanQuery query) {
        BooleanClause booleanClause = null;
        BooleanClause.Occur occur = LuceneUtils
                .convertRequiredAndProhibitedToOccur(true, false);
        BooleanClause.Occur dontOccur = LuceneUtils
                .convertRequiredAndProhibitedToOccur(false, true);
        if (searchParam != null) {
            searchParam = searchParam.trim();
            if (searchParam.length() > 0) {
                BooleanQuery booleanQuery = new BooleanQuery();
                MatchAllDocsQuery matchAllDocsQuery = new MatchAllDocsQuery();
                BooleanClause matchAllDocsClause = new BooleanClause(
                        matchAllDocsQuery, occur);
                booleanQuery.add(matchAllDocsClause);
                // tokenize searchParam
                StringTokenizer st = new StringTokenizer(searchParam);
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    // ignore fuzziness in without-queries
                    Query subQuery = textFieldToken(token, luceneIndexField,
                            null);
                    if (subQuery != null) {
                        BooleanClause subClause = new BooleanClause(subQuery,
                                dontOccur);
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
     * Add clause to a query for all tokens in the search param. 'Not required' does
     * not mean that this is not a required search parameter; rather it means
     * that if this parameter is present, the query must select results where at
     * least one of the tokens in the search param is present.
     * 
     * @param searchParam
     *            search param
     * @param luceneIndexField
     *            index field
     * @param similarity
     *            fuzziness
     * @return boolean clause
     */
    private void addNotRequiredTextField(String searchParam,
            String luceneIndexField, String similarity, BooleanQuery query) {
        BooleanClause booleanClause = null;
        BooleanClause.Occur occur = LuceneUtils
                .convertRequiredAndProhibitedToOccur(true, false);
        BooleanClause.Occur tokenOccur = LuceneUtils
                .convertRequiredAndProhibitedToOccur(false, false);
        if (searchParam != null) {
            if (searchParam.length() > 0) {
                if (!_tokenizedFieldSet.contains(luceneIndexField)) {
                    // TODO : use similarity when needed
                    TermQuery termQuery = new TermQuery(new Term(
                            luceneIndexField, searchParam));
                    BooleanClause clause = new BooleanClause(termQuery, occur);
                    query.add(clause);
                } else {
                    // tokenize searchParam
                    StringTokenizer st = new StringTokenizer(searchParam);
                    BooleanQuery booleanQuery = new BooleanQuery();
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        Query subQuery = textFieldToken(token,
                                luceneIndexField, similarity);
                        if (subQuery != null) {
                            BooleanClause subClause = new BooleanClause(
                                    subQuery, tokenOccur);
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
     * Add a clause to a query for all tokens in the search param. 'Required' does not
     * mean that this is a required search parameter; rather it means that if
     * this parameter is present, the query must select only results where each
     * of the tokens in the search param is present.
     * 
     * @param searchParam
     *            search parameter
     * @param luceneIndexField
     *            index field
     * @param similarity
     *            fuzziness
     * @return boolean clause
     */
    private void addRequiredTextField(String searchParam,
            String luceneIndexField, String similarity, BooleanQuery query) {
        BooleanClause booleanClause = null;
        BooleanClause.Occur occur = LuceneUtils
                .convertRequiredAndProhibitedToOccur(true, false);
        if (searchParam != null) {
            if (searchParam.length() > 0) {
                if (!_tokenizedFieldSet.contains(luceneIndexField)) {
                    // TODO : use similarity when needed
                    TermQuery termQuery = new TermQuery(new Term(
                            luceneIndexField, searchParam));
                    BooleanClause clause = new BooleanClause(termQuery, occur);
                    query.add(clause);
                } else {
                    // tokenize searchParam only if tokenized when indexing
                    StringTokenizer st = new StringTokenizer(searchParam);
                    if (st.countTokens() == 1) {
                        String token = st.nextToken();
                        Query subQuery = textFieldToken(token,
                                luceneIndexField, similarity);
                        if (subQuery != null) {
                            booleanClause = new BooleanClause(subQuery, occur);
                        }
                    } else {
                        BooleanQuery booleanQuery = new BooleanQuery();
                        while (st.hasMoreTokens()) {
                            String token = st.nextToken();
                            Query subQuery = textFieldToken(token,
                                    luceneIndexField, similarity);
                            if (subQuery != null) {
                                BooleanClause subClause = new BooleanClause(
                                        subQuery, occur);
                                booleanQuery.add(subClause);
                            }
                        }
                        booleanClause = new BooleanClause(booleanQuery, occur);
                    }
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
     * @param text
     * @param separator
     * @param fieldName
     * @param query
     */
    private void addSeparatedTextField(String text, String separator,
            String fieldName, BooleanQuery query) {

        if (StringUtils.isNotBlank(text)) {
            BooleanClause.Occur occur = LuceneUtils
                    .convertRequiredAndProhibitedToOccur(false, false);

            for (String token : Splitter.on(separator).trimResults().split(text)) {
                // TODO : here we should use similarity if set
                TermQuery termQuery = new TermQuery(new Term(fieldName, token));
                BooleanClause clause = new BooleanClause(termQuery, occur);
                query.add(clause);
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
	private void addAnyTextQuery(String any, String similarity,
			BooleanQuery query) {
		BooleanClause anyClause = null;
		BooleanClause.Occur occur = LuceneUtils
				.convertRequiredAndProhibitedToOccur(true, false);
		if (StringUtils.isNotBlank(any) && !onlyWildcard(any)) {
			// tokenize searchParam
			StringTokenizer st = new StringTokenizer(any);
			if (st.countTokens() == 1) {
				String token = st.nextToken();
				Query subQuery = textFieldToken(token, LuceneIndexField.ANY,
						similarity);
				if (subQuery != null) {
					anyClause = new BooleanClause(subQuery, occur);
				}
			} else {
				BooleanQuery booleanQuery = new BooleanQuery();
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					Query subQuery = textFieldToken(token,
							LuceneIndexField.ANY, similarity);
					if (subQuery != null) {
						BooleanClause subClause = new BooleanClause(subQuery,
								occur);
						if (subClause != null) {
							booleanQuery.add(subClause);
						}
					}
				}
				anyClause = new BooleanClause(booleanQuery, occur);
			}
		}
		if (anyClause != null) {
			query.add(anyClause);
		}
	}

    /**
     * Add search privilege criteria to a query.
     * 
     * @param luceneQueryInput
     * @param query
     */
    private void addPrivilegeQuery(LuceneQueryInput luceneQueryInput,
            BooleanQuery query) {
        // Set user groups privileges
        Set<String> groups = luceneQueryInput.getGroups();
        String editable$ = luceneQueryInput.getEditable();
        boolean editable = BooleanUtils.toBoolean(editable$);
        BooleanQuery groupsQuery = new BooleanQuery();
        boolean groupsQueryEmpty = true;
        BooleanClause.Occur groupOccur = LuceneUtils
                .convertRequiredAndProhibitedToOccur(false, false);
        if (!CollectionUtils.isEmpty(groups)) {
            for (String group : groups) {
                if (StringUtils.isNotBlank(group)) {
                    if (!editable) {
                        // add to view
                        TermQuery viewQuery = new TermQuery(new Term(
                                LuceneIndexField._OP0, group.trim()));
                        BooleanClause viewClause = new BooleanClause(viewQuery,
                                groupOccur);
                        groupsQueryEmpty = false;
                        groupsQuery.add(viewClause);
                    }
                    // add to edit
                    TermQuery editQuery = new TermQuery(new Term(
                            LuceneIndexField._OP2, group.trim()));
                    BooleanClause editClause = new BooleanClause(editQuery,
                            groupOccur);
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
            TermQuery ownerQuery = new TermQuery(new Term(
                    LuceneIndexField.OWNER, owner));
            BooleanClause.Occur ownerOccur = LuceneUtils
                    .convertRequiredAndProhibitedToOccur(false, false);
            BooleanClause ownerClause = new BooleanClause(ownerQuery,
                    ownerOccur);
            groupsQueryEmpty = false;
            groupsQuery.add(ownerClause);
        }

        //
        // "dummy" -- to go in groups query, to retrieve everything for
        // Administrator users.
        //
        boolean admin = luceneQueryInput.getAdmin();
        if (admin) {
            TermQuery adminQuery = new TermQuery(new Term(
                    LuceneIndexField.DUMMY, "0"));
            BooleanClause adminClause = new BooleanClause(adminQuery,
                    groupOccur);
            groupsQueryEmpty = false;
            groupsQuery.add(adminClause);
        }

        // Add the privilege part of the query
        if (!groupsQueryEmpty) {
            BooleanClause.Occur groupsOccur = LuceneUtils
                    .convertRequiredAndProhibitedToOccur(true, false);
            BooleanClause groupsClause = new BooleanClause(groupsQuery,
                    groupsOccur);
            query.add(groupsClause);
        }
    }

    /**
     * Add a range query according to field type. If field type is numeric, then
     * a numeric range query is used. If not a default range query is uses.
     * 
     * Range query include lower and upper bounds by default.
     * 
     * @param query
     * @param from
     * @param to
     * @param luceneIndexField
     */
    private void addRangeQuery(BooleanQuery query, String from, String to,
            String luceneIndexField) {
        if (from == null && to == null)
            return;

        LuceneConfig.LuceneConfigNumericField type = _numericFieldSet
                .get(luceneIndexField);
        if (type == null) {
            addTextRangeQuery(query, from, to, luceneIndexField);
        } else {
            addNumericRangeQuery(query, from, to, true, true, luceneIndexField,
                    true);
        }
    }

    /**
     * Add a numeric range query according to field numeric type.
     * 
     * @param query
     * @param min
     * @param max
     * @param minInclusive
     * @param maxExclusive
     * @param luceneIndexField
     * @param required
     *            TODO
     */
    private void addNumericRangeQuery(BooleanQuery query, String min,
            String max, boolean minInclusive, boolean maxExclusive,
            String luceneIndexField, boolean required) {
        if (min != null && max != null) {
            String type = _numericFieldSet.get(luceneIndexField).getType();

            NumericRangeQuery rangeQuery = buildNumericRangeQueryForType(
                    luceneIndexField, min, max, minInclusive, maxExclusive,
                    type);

            BooleanClause.Occur denoOccur = LuceneUtils
                    .convertRequiredAndProhibitedToOccur(required, false);
            BooleanClause rangeClause = new BooleanClause(rangeQuery, denoOccur);

            query.add(rangeClause);
        }
    }

    public static NumericRangeQuery buildNumericRangeQueryForType(
            String fieldName, String min, String max, boolean minInclusive,
            boolean maxInclusive, String type) {
        NumericRangeQuery rangeQuery;
        if ("double".equals(type)) {
            rangeQuery = NumericRangeQuery.newDoubleRange(fieldName,
                    (min == null ? Double.MIN_VALUE : Double.valueOf(min)),
                    (max == null ? Double.MAX_VALUE : Double.valueOf(max)),
                    true, true);

        } else if ("float".equals(type)) {
            rangeQuery = NumericRangeQuery.newFloatRange(fieldName,
                    (min == null ? Float.MIN_VALUE : Float.valueOf(min)),
                    (max == null ? Float.MAX_VALUE : Float.valueOf(max)), true,
                    true);
        } else if ("long".equals(type)) {
            rangeQuery = NumericRangeQuery.newLongRange(fieldName,
                    (min == null ? Long.MIN_VALUE : Long.valueOf(min)),
                    (max == null ? Long.MAX_VALUE : Long.valueOf(max)), true,
                    true);
        } else {
            rangeQuery = NumericRangeQuery.newIntRange(fieldName,
                    (min == null ? Integer.MIN_VALUE : Integer.valueOf(min)),
                    (max == null ? Integer.MAX_VALUE : Integer.valueOf(max)),
                    true, true);
        }
        return rangeQuery;
    }

    /**
     * Add a date range query for a text field type.
     * 
     * @param query
     * @param dateTo
     * @param dateFrom
     * @param luceneIndexField
     */
    private void addTextRangeQuery(BooleanQuery query, 
            String dateFrom, String dateTo, String luceneIndexField) {
        if ((dateTo != null && dateTo.length() > 0)
                || (dateFrom != null && dateFrom.length() > 0)) {
            TermRangeQuery rangeQuery;
            if (dateTo != null) {
                // while the 'from' parameter can be short (like yyyy-mm-dd)
                // the 'until' parameter must be long to match
                if (dateTo.length() == 10) {
                    dateTo = dateTo + "T23:59:59";
                }
            }
            rangeQuery = new TermRangeQuery(luceneIndexField, dateFrom, dateTo,
                    true, true);
            BooleanClause.Occur dateOccur = LuceneUtils
                    .convertRequiredAndProhibitedToOccur(true, false);
            BooleanClause dateRangeClause = new BooleanClause(rangeQuery,
                    dateOccur);
            query.add(dateRangeClause);
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
     * @param query
     * @param relation
     * @param eastBL
     * @param westBL
     * @param northBL
     * @param southBL
     */
    private void addBoundingBoxQuery(BooleanQuery query, String relation,
            String eastBL, String westBL, String northBL, String southBL) {

        // Default inclusive value for RangeQuery (includeLower and
        // includeUpper)
        boolean inclusive = true;

        if (relation == null
                || relation.equals(Geonet.SearchResult.Relation.OVERLAPS)) {

            //
            // overlaps (default value) : uses the equivalence
            // -(a + b + c + d) = -a * -b * -c * -d
            //
            // eastBL
            if (westBL != null) {
                addNumericRangeQuery(query, westBL,
                        String.valueOf(maxBoundingLongitudeValue), inclusive,
                        inclusive, LuceneIndexField.EAST, true);
            }
            // westBL
            if (eastBL != null) {
                addNumericRangeQuery(query,
                        String.valueOf(minBoundingLongitudeValue), eastBL,
                        inclusive, inclusive, LuceneIndexField.WEST, true);
            }
            // northBL
            if (southBL != null) {
                addNumericRangeQuery(query, southBL,
                        String.valueOf(maxBoundingLatitudeValue), inclusive,
                        inclusive, LuceneIndexField.NORTH, true);
            }
            // southBL
            if (northBL != null) {
                addNumericRangeQuery(query,
                        String.valueOf(minBoundingLatitudeValue), northBL,
                        inclusive, inclusive, LuceneIndexField.SOUTH, true);
            }
        }
        //
        // equal: coordinates of the target rectangle within 1 degree from
        // corresponding ones of metadata rectangle
        //
        else if (relation.equals(Geonet.SearchResult.Relation.EQUAL)) {
            // eastBL
            if (eastBL != null) {
                addNumericRangeQuery(query, eastBL, eastBL, inclusive,
                        inclusive, LuceneIndexField.EAST, true);
            }
            // westBL
            if (westBL != null) {
                addNumericRangeQuery(query, westBL, westBL, inclusive,
                        inclusive, LuceneIndexField.WEST, true);
            }
            // northBL
            if (northBL != null) {
                addNumericRangeQuery(query, northBL, northBL, inclusive,
                        inclusive, LuceneIndexField.NORTH, true);
            }
            // southBL
            if (southBL != null) {
                addNumericRangeQuery(query, southBL, southBL, inclusive,
                        inclusive, LuceneIndexField.SOUTH, true);
            }
        }
        //
        // encloses: metadata rectangle encloses target rectangle
        //
        else if (relation.equals(Geonet.SearchResult.Relation.ENCLOSES)) {
            // eastBL
            if (eastBL != null) {
                addNumericRangeQuery(query, eastBL,
                        String.valueOf(maxBoundingLongitudeValue), inclusive,
                        inclusive, LuceneIndexField.EAST, true);
            }
            // westBL
            if (westBL != null) {
                addNumericRangeQuery(query,
                        String.valueOf(minBoundingLongitudeValue), westBL,
                        inclusive, inclusive, LuceneIndexField.WEST, true);
            }
            // northBL
            if (northBL != null) {
                addNumericRangeQuery(query, northBL,
                        String.valueOf(maxBoundingLatitudeValue), inclusive,
                        inclusive, LuceneIndexField.NORTH, true);
            }
            // southBL
            if (southBL != null) {
                addNumericRangeQuery(query,
                        String.valueOf(minBoundingLatitudeValue), southBL,
                        inclusive, inclusive, LuceneIndexField.SOUTH, true);
            }
        }
        //
        // fullyEnclosedWithin: metadata rectangle fully enclosed within target
        // rectangle
        //
        else if (relation.equals(Geonet.SearchResult.Relation.ENCLOSEDWITHIN)) {
            // eastBL
            if (eastBL != null) {
                addNumericRangeQuery(query, westBL, eastBL, inclusive,
                        inclusive, LuceneIndexField.EAST, true);
            }
            // westBL
            if (westBL != null) {
                addNumericRangeQuery(query, westBL, eastBL, inclusive,
                        inclusive, LuceneIndexField.WEST, true);
            }
            // northBL
            if (northBL != null) {
                addNumericRangeQuery(query, southBL, northBL, inclusive,
                        inclusive, LuceneIndexField.NORTH, true);
            }
            // southBL
            if (southBL != null) {
                addNumericRangeQuery(query, southBL, northBL, inclusive,
                        inclusive, LuceneIndexField.SOUTH, true);
            }
        }
        //
        // fullyOutsideOf: one or more of the 4 forbidden halfplanes contains
        // the metadata
        // rectangle, that is, not true that all the 4 forbidden halfplanes do
        // not contain
        // the metadata rectangle
        //
        else if (relation.equals(Geonet.SearchResult.Relation.OUTSIDEOF)) {
            // eastBL
            if (westBL != null) {
                addNumericRangeQuery(query,
                        String.valueOf(minBoundingLongitudeValue), westBL,
                        inclusive, inclusive, LuceneIndexField.EAST, false);
            }
            // westBL
            if (eastBL != null) {
                addNumericRangeQuery(query, eastBL,
                        String.valueOf(maxBoundingLongitudeValue), inclusive,
                        inclusive, LuceneIndexField.WEST, false);
            }
            // northBL
            if (southBL != null) {
                addNumericRangeQuery(query,
                        String.valueOf(minBoundingLatitudeValue), southBL,
                        inclusive, inclusive, LuceneIndexField.NORTH, false);
            }
            // southBL
            if (northBL != null) {
                addNumericRangeQuery(query, northBL,
                        String.valueOf(maxBoundingLatitudeValue), inclusive,
                        inclusive, LuceneIndexField.SOUTH, false);
            }
        }
    }

    private boolean onlyWildcard(String s) {
        return "*".equals(StringUtils.trim(s));
    }
}
