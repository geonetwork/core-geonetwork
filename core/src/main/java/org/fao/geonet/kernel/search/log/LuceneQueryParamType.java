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

import com.google.common.base.Optional;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * An enumeration of the different types of lucene queries stored in the database.
 *
 * @author Jesse
 */
public enum LuceneQueryParamType {
    TERM {
        @Override
        protected Optional<List<SearchRequestParam>> createTypeFrom(Query query) {
            if (query instanceof TermQuery) {
                final TermQuery termQuery = (TermQuery) query;
                String term = termQuery.getTerm().field();
                if (!(isExcludedField(term))) {
                    SearchRequestParam param = new SearchRequestParam()
                        .setTermField(term)
                        .setTermText(termQuery.getTerm().text())
                        .setQueryType(this);

                    List<SearchRequestParam> list = new ArrayList<>();
                    list.add(param);
                    return Optional.of(list);
                } else {
                    return Optional.absent();
                }
            } else {
                return Optional.absent();
            }
        }
    },
    ConstantScoreQuery {
        @Override
        protected Optional<List<SearchRequestParam>> createTypeFrom(Query query) {
            if (query instanceof ConstantScoreQuery) {
                final ConstantScoreQuery termQuery = (ConstantScoreQuery) query;
                final BooleanQuery booleanQuery = (BooleanQuery) termQuery.getQuery();
                List<SearchRequestParam> list = new ArrayList<>();
                if (booleanQuery != null) {
                    for (BooleanClause clause : booleanQuery.clauses()) {
                        Query q = clause.getQuery();
                        if (q instanceof TermQuery) {
                            TermQuery tQuery = (TermQuery) q;
                            String term = tQuery.getTerm().field();
                            if (!(isExcludedField(term))) {
                                SearchRequestParam param = new SearchRequestParam()
                                    .setTermField(tQuery.getTerm().field())
                                    .setTermText(tQuery.getTerm().text())
                                    .setQueryType(this);

                                list.add(param);
                            }
                        }
                    }
                } else {
                    // CSW search usually
                    final Filter filter = termQuery.getFilter();
                    if (filter instanceof MultiTermQueryWrapperFilter) {
                        MultiTermQueryWrapperFilter f = (MultiTermQueryWrapperFilter) filter;

                        SearchRequestParam param = new SearchRequestParam()
                            .setTermField(f.getField())
                            // TODO: extract value ((WildcardQuery) f.query).getTerm().text()
                            .setTermText(f.toString())
                            .setQueryType(this);

                        list.add(param);
                    }

                }
                return Optional.of(list);
            } else {
                return Optional.absent();
            }
        }
    },
    DRILLDOWN {
        @Override
        protected Optional<List<SearchRequestParam>> createTypeFrom(Query query) {
            if (query instanceof org.apache.lucene.facet.DrillDownQuery) {
                if (Log.isDebugEnabled(Geonet.SEARCH_LOGGER)) {
                    Log.debug(Geonet.SEARCH_LOGGER,
                        "DrillDownQuery can not be logged. You should DrillDownQuery#rewrite and send this to the logger.");
                }
                return Optional.absent();
            } else {
                return Optional.absent();
            }
        }
    },
    FUZZY {
        @Override
        protected Optional<List<SearchRequestParam>> createTypeFrom(Query query) {
            if (query instanceof FuzzyQuery) {
                final FuzzyQuery fuzzyQuery = (FuzzyQuery) query;
                String term = fuzzyQuery.getTerm().field();
                if (!(isExcludedField(term))) {
                    SearchRequestParam param = new SearchRequestParam()
                        .setTermField(fuzzyQuery.getTerm().field())
                        .setTermText(fuzzyQuery.getTerm().text())
                        .setQueryType(this)
                        .setSimilarity(((double) fuzzyQuery.getMaxEdits() - 1) / 10);

                    List<SearchRequestParam> list = new ArrayList<>();
                    list.add(param);
                    return Optional.of(list);
                } else {
                    return Optional.absent();
                }
            } else {
                return Optional.absent();
            }
        }
    }, PREFIX {
        @Override
        protected Optional<List<SearchRequestParam>> createTypeFrom(Query query) {
            if (query instanceof PrefixQuery) {
                PrefixQuery prefixQuery = (PrefixQuery) query;
                String field = prefixQuery.getPrefix().field();
                if (!(isExcludedField(field))) {
                    String text = prefixQuery.getPrefix().text();
                    SearchRequestParam param = new SearchRequestParam()
                        .setTermField(field)
                        .setTermText(text)
                        .setQueryType(this);

                    List<SearchRequestParam> list = new ArrayList<>();
                    list.add(param);
                    return Optional.of(list);
                } else {
                    return Optional.absent();
                }
            } else {
                return Optional.absent();
            }
        }
    }, MATCH_ALL_DOCS {
        @Override
        protected Optional<List<SearchRequestParam>> createTypeFrom(Query query) {
            if (query instanceof MatchAllDocsQuery) {
                // extract all terms for this query (text and field)
                Set<Term> terms = new HashSet<Term>();
                query.extractTerms(terms);
                // one should consider that all terms refer to the same field, or not ?
                String fields = concatTermsField(terms.toArray(new Term[terms.size()]), null);
                String texts = concatTermsText(new Term[terms.size()], null);
                SearchRequestParam param = new SearchRequestParam()
                    .setTermField(fields)
                    .setTermText(texts)
                    .setQueryType(this);

                List<SearchRequestParam> list = new ArrayList<>();
                list.add(param);
                return Optional.of(list);
            } else {
                return Optional.absent();
            }
        }
    }, WILDCARD {
        @Override
        protected Optional<List<SearchRequestParam>> createTypeFrom(Query query) {
            if (query instanceof WildcardQuery) {
                WildcardQuery wildcardQuery = (WildcardQuery) query;
                String field = wildcardQuery.getTerm().field();

                if (!(isExcludedField(field))) {
                    String text = wildcardQuery.getTerm().text();
                    SearchRequestParam param = new SearchRequestParam()
                        .setTermField(field)
                        .setTermText(text)
                        .setQueryType(this);

                    List<SearchRequestParam> list = new ArrayList<>();
                    list.add(param);
                    return Optional.of(list);
                } else {
                    return Optional.absent();
                }
            } else {
                return Optional.absent();
            }
        }
    }, PHRASE {
        @Override
        protected Optional<List<SearchRequestParam>> createTypeFrom(Query query) {
            if (query instanceof PhraseQuery) {
                // extract all terms for this query (text and field)
                Term[] terms = ((PhraseQuery) query).getTerms();
                String fields = concatTermsField(terms, null);
                String texts = concatTermsText(terms, null);
                SearchRequestParam param = new SearchRequestParam()
                    .setTermField(fields)
                    .setTermText(texts)
                    .setQueryType(this);

                List<SearchRequestParam> list = new ArrayList<>();
                list.add(param);
                return Optional.of(list);
            } else {
                return Optional.absent();
            }
        }

    }, RANGE {
        @Override
        protected Optional<List<SearchRequestParam>> createTypeFrom(Query query) {
            if (query instanceof TermRangeQuery) {
                TermRangeQuery rangeQuery = (TermRangeQuery) query;
                SearchRequestParam param = new SearchRequestParam();
                if (rangeQuery.getLowerTerm() != null) {
                    param.setLowerText(rangeQuery.getLowerTerm().utf8ToString());
                }
                if (rangeQuery.getUpperTerm() != null) {
                    param.setUpperText(rangeQuery.getUpperTerm().utf8ToString());
                }
                param.setTermField(rangeQuery.getField());
                param.setQueryType(this);

                List<SearchRequestParam> list = new ArrayList<>();
                list.add(param);
                return Optional.of(list);
            } else {
                return Optional.absent();
            }
        }

    }, NUMERIC_RANGE {
        @Override
        protected Optional<List<SearchRequestParam>> createTypeFrom(Query query) {
            if (query instanceof NumericRangeQuery) {
                NumericRangeQuery<?> numericRangeQuery = (NumericRangeQuery<?>) query;

                SearchRequestParam param = new SearchRequestParam()
                    .setLowerText(numericRangeQuery.getMin().toString())
                    .setUpperText(numericRangeQuery.getMax().toString())
                    .setTermField(numericRangeQuery.getField());

                List<SearchRequestParam> list = new ArrayList<>();
                list.add(param);
                return Optional.of(list);
            } else {
                return Optional.absent();
            }
        }

    };

    public static final Pattern excludedFieldPattern =
        Pattern.compile("_op.*|_isTemplate|_locale|_owner|_groupOwner|_dummy|type");

    public static boolean isExcludedField(String field) {
        return excludedFieldPattern.matcher(field).matches();
    }


    public static Optional<List<SearchRequestParam>> createRequestParam(Query query) {
        for (LuceneQueryParamType luceneQueryParamType : values()) {
            Optional<List<SearchRequestParam>> param = luceneQueryParamType.createTypeFrom(query);

            if (param.isPresent()) {
                return param;
            }
        }

        return Optional.absent();
    }

    /**
     * Concatenates the given terms' fields into a single String, with the given separator.
     *
     * @param terms     the set of terms to concatenate
     * @param separator the separator to use to separate text elements  (use ',' if sep is null)
     * @return a string containing all this terms' fields concatenated
     */
    private static String concatTermsField(Term[] terms, String separator) {
        if (terms == null || separator == null) return null;

        StringBuilder sb = new StringBuilder();
        for (Term t : terms) {
            sb.append(t.field()).append(separator);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * Concatenates the given terms' text into a single String, with the given separator.
     *
     * @param terms     the set of terms to concatenate
     * @param separator the separator to use to separate text elements (use ',' if sep is null)
     * @return a string containing all this terms' texts concatenated
     */
    private static String concatTermsText(Term[] terms, String separator) {
        if (terms == null || separator == null) return null;

        StringBuilder sb = new StringBuilder();
        for (Term t : terms) {
            sb.append(t.text()).append(separator);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    protected abstract Optional<List<SearchRequestParam>> createTypeFrom(Query query);
}
