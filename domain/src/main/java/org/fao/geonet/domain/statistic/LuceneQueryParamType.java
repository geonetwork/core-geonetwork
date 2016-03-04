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

package org.fao.geonet.domain.statistic;

import com.google.common.base.Optional;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.util.HashSet;
import java.util.Set;

/**
 * An enumeration of the different types of lucene queries stored in the database.
 *
 * @author Jesse
 */
public enum LuceneQueryParamType {
    TERM {
        @Override
        protected Optional<SearchRequestParam> createTypeFrom(Query query) {
            if (query instanceof TermQuery) {
                final TermQuery termQuery = (TermQuery) query;
                SearchRequestParam param = new SearchRequestParam()
                        .setTermField(termQuery.getTerm().field())
                        .setTermText(termQuery.getTerm().text())
                        .setQueryType(this);

                return Optional.of(param);
            } else {
                return Optional.absent();
            }
        }
    },
    FUZZY {
        @Override
        protected Optional<SearchRequestParam> createTypeFrom(Query query) {
            if (query instanceof FuzzyQuery) {
                final FuzzyQuery fuzzyQuery = (FuzzyQuery) query;
                SearchRequestParam param = new SearchRequestParam()
                        .setTermField(fuzzyQuery.getTerm().field())
                        .setTermText(fuzzyQuery.getTerm().text())
                        .setQueryType(this)
                        .setSimilarity(((double) fuzzyQuery.getMaxEdits() - 1) / 10);

                return Optional.of(param);
            } else {
                return Optional.absent();
            }
        }
    }, PREFIX {
        @Override
        protected Optional<SearchRequestParam> createTypeFrom(Query query) {
            if (query instanceof PrefixQuery) {
                PrefixQuery prefixQuery = (PrefixQuery) query;
                String field = prefixQuery.getPrefix().field();
                String text = prefixQuery.getPrefix().text();
                SearchRequestParam param = new SearchRequestParam()
                        .setTermField(field)
                        .setTermText(text)
                        .setQueryType(this);

                return Optional.of(param);
            } else {
                return Optional.absent();
            }
        }
    }, MATCH_ALL_DOCS {
        @Override
        protected Optional<SearchRequestParam> createTypeFrom(Query query) {
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

                return Optional.of(param);
            } else {
                return Optional.absent();
            }
        }
    }, WILDCARD {
        @Override
        protected Optional<SearchRequestParam> createTypeFrom(Query query) {
            if (query instanceof WildcardQuery) {
                WildcardQuery wildcardQuery = (WildcardQuery) query;
                String field = wildcardQuery.getTerm().field();
                String text = wildcardQuery.getTerm().text();
                SearchRequestParam param = new SearchRequestParam()
                        .setTermField(field)
                        .setTermText(text)
                        .setQueryType(this);

                return Optional.of(param);
            } else {
                return Optional.absent();
            }
        }
    }, PHRASE {
        @Override
        protected Optional<SearchRequestParam> createTypeFrom(Query query) {
            if (query instanceof PhraseQuery) {
                // extract all terms for this query (text and field)
                Term[] terms = ((PhraseQuery) query).getTerms();
                String fields = concatTermsField(terms, null);
                String texts = concatTermsText(terms, null);
                SearchRequestParam param = new SearchRequestParam()
                        .setTermField(fields)
                        .setTermText(texts)
                        .setQueryType(this);

                return Optional.of(param);
            } else {
                return Optional.absent();
            }
        }

    }, RANGE {
        @Override
        protected Optional<SearchRequestParam> createTypeFrom(Query query) {
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

                return Optional.of(param);
            } else {
                return Optional.absent();
            }
        }

    }, NUMERIC_RANGE {
        @Override
        protected Optional<SearchRequestParam> createTypeFrom(Query query) {
            if (query instanceof NumericRangeQuery) {
                NumericRangeQuery<?> numericRangeQuery = (NumericRangeQuery<?>) query;

                SearchRequestParam param = new SearchRequestParam()
                        .setLowerText(numericRangeQuery.getMin().toString())
                        .setUpperText(numericRangeQuery.getMax().toString())
                        .setTermField(numericRangeQuery.getField());

                return Optional.of(param);
            } else {
                return Optional.absent();
            }
        }

    };

    protected abstract Optional<SearchRequestParam> createTypeFrom(Query query);


    public static Optional<SearchRequestParam> createRequestParam(Query query) {
        for (LuceneQueryParamType luceneQueryParamType : values()) {
            Optional<SearchRequestParam> param = luceneQueryParamType.createTypeFrom(query);

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
}
