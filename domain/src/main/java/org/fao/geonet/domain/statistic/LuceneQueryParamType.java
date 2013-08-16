package org.fao.geonet.domain.statistic;

/**
 * An enumeration of the different types of lucene queries stored in the database.
 *
 * @author Jesse
 */
public enum LuceneQueryParamType {
    BOOLEAN, TERM, FUZZY, PREFIX, MATCH_ALL_DOCS, WILDCARD, PHRASE, RANGE, NUMERIC_RANGE
}
