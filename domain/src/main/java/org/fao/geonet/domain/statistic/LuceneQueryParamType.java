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
                SearchRequestParam param = new SearchRequestParam()
                        .setLowerText(rangeQuery.getLowerTerm().utf8ToString())
                        .setUpperText(rangeQuery.getUpperTerm().utf8ToString())
                        .setTermField(rangeQuery.getField())
                        .setQueryType(this);

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
