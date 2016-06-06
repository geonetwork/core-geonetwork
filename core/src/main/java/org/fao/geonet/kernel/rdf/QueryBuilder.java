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

package org.fao.geonet.kernel.rdf;

import static java.text.MessageFormat.format;
import static org.fao.geonet.kernel.rdf.Selectors.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.jdom.Namespace;
import org.openrdf.model.Value;

public class QueryBuilder<Q> {

    private Map<String /* Id */, Select> selectPaths = new LinkedHashMap<String, Select>();
    private int limit = -1;
    private int offset = -1;
    private boolean distinct = false;
    @SuppressWarnings("rawtypes")
    private ResultInterpreter interpreter = new IdentityResultInterpreter();
    private Where whereClause = null;
    protected QueryBuilder() {
    }

    /**
     * Creates a simple query builder that returns simple query objects
     */
    public static QueryBuilder<Query<LinkedHashMap<String, Value>>> builder() {
        return new QueryBuilder<Query<LinkedHashMap<String, Value>>>();
    }

    /**
     * Create a query builder that is configured to read keywords with translations in the provided
     * languages
     *
     * @param mapper    the mapper to use to convert the language code when needed
     * @param languages the languages to read from the thesaurus file
     * @return A QueryBuilder configured to read keywords
     */
    public static QueryBuilder<KeywordBean> keywordQueryBuilder(IsoLanguagesMapper mapper, List<String> languages) {
        return keywordQueryBuilder(mapper, languages, false);
    }

    /**
     * Create a query builder that is configured to read keywords with translations in the provided
     * languages
     *
     * @param mapper    the mapper to use to convert the language code when needed
     * @param languages the languages to read from the thesaurus file
     * @return A QueryBuilder configured to read keywords
     */
    public static QueryBuilder<KeywordBean> keywordQueryBuilder(IsoLanguagesMapper mapper, Collection<String> languages, boolean requireBoundedBy) {
        QueryBuilder<KeywordBean> builder = builder()
            .distinct(true)
            .selectId()
            .select(UPPER_CORNER, requireBoundedBy)
            .select(LOWER_CORNER, requireBoundedBy)
            .interpreter(new KeywordResultInterpreter(languages));

        for (String lang : languages) {
            builder.select(Selectors.prefLabel(lang, mapper), false);
            builder.select(Selectors.note(lang, mapper), false);
        }
        return builder;
    }

    /**
     * Create a query builder that is configured to read keywords with translations in the provided
     * languages
     *
     * @param mapper    the mapper to use to convert the language code when needed
     * @param languages the languages to read from the thesaurus file
     * @return A QueryBuilder configured to read keywords
     */
    public static QueryBuilder<KeywordBean> keywordQueryBuilder(IsoLanguagesMapper mapper, String... languages) {
        return keywordQueryBuilder(mapper, Arrays.asList(languages));
    }

    /**
     * Create a query builder that is configured to read distinct languages in the thesaurus
     *
     * @param mapper the mapper to use to convert the language code when needed
     * @return A QueryBuilder configured to read keywords
     */
    public static QueryBuilder<String> languagesQueryBuilder(final IsoLanguagesMapper mapper) {
        ResultInterpreter<String> newInterpreter = new IdentityResultInterpreter().onlyColumn("language").map(new com.google.common.base.Function<Value, String>() {
            @Override
            public String apply(Value input) {
                if (input == null) {
                    throw new AssertionError("Input of ResultInterpreter must not be null");
                }
                return mapper.iso639_1_to_iso639_2(input.toString(), "");
            }
        });

        Selector languages = Selectors.languages(PREF_LABEL);
        QueryBuilder<String> builder = builder()
            .distinct(true)
            .select(languages, true)
            .interpreter(newInterpreter);

        return builder;
    }

    /**
     * Build the query object.
     *
     * @return the query object that can be used to perform the query
     */
    @SuppressWarnings("unchecked")
    public Query<Q> build() {
        String query = createQueryString();
        return new Query<Q>(query, interpreter);
    }

    /**
     * Process the configuration and create a SERQL query string
     *
     * @return the created query string
     */
    protected String createQueryString() {
        StringBuilder variables = new StringBuilder();
        StringBuilder paths = new StringBuilder();
        StringBuilder namespaces = new StringBuilder();
        Set<String> addedNamespaces = new HashSet<String>();

        for (Select select : selectPaths.values()) {
            addTo(variables, select.getVariable());
            addTo(paths, select.getPath());
            for (Namespace namespace : select.getNamespaces()) {
                if (!addedNamespaces.contains(namespace.getPrefix())) {
                    addedNamespaces.add(namespace.getPrefix());
                    addTo(namespaces, namespace.getPrefix() + "=<" + namespace.getURI() + ">");
                }
            }
        }

        StringBuilder statement = new StringBuilder("SELECT ");
        if (distinct) statement.append("DISTINCT ");
        statement.append(variables);
        statement.append(" FROM ");
        statement.append(paths);
        statement.append(' ');

        if (whereClause != null) {
            String clause = whereClause.getClause();
            if (!clause.trim().isEmpty()) {
                statement.append("WHERE ");
                statement.append(whereClause.getClause());
                statement.append(' ');
            }
        }

        if (limit > -1) {
            statement.append("LIMIT ");
            statement.append(limit);
            statement.append(' ');
        }
        if (offset > 0) {
            statement.append("OFFSET ");
            statement.append(offset);
            statement.append(' ');
        }

        if (namespaces.length() > 0) {
            statement.append("USING NAMESPACE ");
            statement.append(namespaces);
        }
        return statement.toString();
    }

    private void addTo(StringBuilder builder, String value) {
        if (value.trim().length() > 0) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(value);
        }
    }

    /**
     * add a new path for selection.
     *
     * @param selector the selector to add to the list of selectors
     * @param require  indicate if it is optional or required. (this is ignored if selector does not
     *                 have an associated path)
     * @return a query builder to use for the next configuration option.
     */
    public QueryBuilder<Q> select(Selector selector, boolean require) {
        selectPaths.put(selector.id, new Select(selector, require));
        return this;
    }

    /**
     * Select the id element
     *
     * @return a query builder to use for the next configuration option.
     */
    public QueryBuilder<Q> selectId() {
        return select(Selectors.ID, true);
    }

    /**
     * Set a limit on the query. If value is < -1 then all records will be returned
     *
     * @param limit the maximum number of records that will be returned
     * @return a query builder to use for the next configuration option.
     */
    public QueryBuilder<Q> limit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Set a limit on the query. If value is < -1 then all records will be returned
     *
     * @param limit the maximum number of records that will be returned
     * @return a query builder to use for the next configuration option.
     */
    public QueryBuilder<Q> distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    /**
     * Set a offset on the query. If value is <= 0 then offset is ignored
     *
     * @param offset the number of records to skip. It is useful for paging
     * @return a query builder to use for the next configuration option.
     */
    public QueryBuilder<Q> offset(int offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Set the ResultsInterpreter responsible for converting the raw results into a more "friendly"
     * type of object for java programming
     *
     * @return a query builder to use for the next configuration option.
     */
    @SuppressWarnings("unchecked")
    public <N> QueryBuilder<N> interpreter(ResultInterpreter<N> newInterpreter) {
        this.interpreter = newInterpreter;
        return (QueryBuilder<N>) this;
    }

    /**
     * Replace the current where clause with the new where clause
     *
     * @param clause the where new clause to use in query
     * @return a query builder to use for the next configuration option.
     */
    public QueryBuilder<Q> where(String clause) {
        return where(new WhereClause(clause));
    }

    /**
     * Replace the current where clause with the new where clause
     *
     * @param clause the where new clause to use in query
     * @return a query builder to use for the next configuration option.
     */
    public QueryBuilder<Q> where(Where where) {
        this.whereClause = where;
        return this;
    }

    @Override
    public String toString() {
        return "QueryBuilder [" + createQueryString() + "]";
    }

    private static class Select extends PathDecorator {

        public final boolean require;

        public Select(Selector path, boolean require) {
            super(path);
            this.require = require;
        }

        @Override
        public String getPath() {
            if (!super.getPath().trim().isEmpty()) {
                if (require) {
                    return super.getPath();
                } else {
                    return format("[{0}]", super.getPath());
                }
            }
            return "";
        }
    }


}
