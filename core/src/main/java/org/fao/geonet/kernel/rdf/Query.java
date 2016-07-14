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

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

import org.fao.geonet.kernel.Thesaurus;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;
import org.openrdf.sesame.query.QueryResultsTable;

/**
 * Represents and RDF query
 *
 * @author jeichar
 */
public class Query<Q> {
    private String query;
    private ResultInterpreter<Q> interpreter;
    /**
     * Create a new Query
     */
    public Query(String query, ResultInterpreter<Q> interpreter) {
        this.query = query;
        this.interpreter = interpreter;
    }

    /**
     * Execute the query and obtain the raw RDF API result table
     *
     * @param thesaurus the thesaurus to execute the query on
     * @return the results of the query
     */
    public QueryResultsTable rawExecute(Thesaurus thesaurus) throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        return thesaurus.performRequest(this.query);
    }

    /**
     * Execute the query and return a list that reads from the list on a by demand basis.  The list
     * is lazy in the sense that the elements of the list are not read until requested but it is not
     * lazy in the sense that the elements are not cached after reading.  For example if list.get(0)
     * is called twice the row will be read from the result table twice.  In general this should not
     * be a big performance problem
     *
     * @param thesaurus the thesaurus to use for the execution.
     * @return a "lazy" list of objects read as a result of the execution.
     */
    public List<Q> execute(final Thesaurus thesaurus) throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        final QueryResultsTable table = rawExecute(thesaurus);
        return new QueryResults(table, thesaurus);
    }

    @Override
    public String toString() {
        return "Query [query=" + query + ", interpreter=" + interpreter.getClass().getSimpleName() + "]";
    }

    /**
     * The results of a query.
     *
     * Note: only serializable so that the results can be cached in the JCS cache.  It should not
     * ever be truly serialized.
     */
    public final class QueryResults extends AbstractList<Q> implements Serializable {
        private static final long serialVersionUID = 3968403559675441162L;
        private final QueryResultsTable table;
        private transient final Thesaurus thesaurus;

        public QueryResults(QueryResultsTable table, Thesaurus thesaurus) {
            this.table = table;
            this.thesaurus = thesaurus;
        }

        @Override
        public Q get(int index) {
            if (index > table.getRowCount() - 1) {
                throw new IndexOutOfBoundsException(index + " is greater than " + (table.getRowCount() - 1));
            }
            return interpreter.createFromRow(thesaurus, table, index);
        }

        @Override
        public int size() {
            return table.getRowCount();
        }
    }


}
