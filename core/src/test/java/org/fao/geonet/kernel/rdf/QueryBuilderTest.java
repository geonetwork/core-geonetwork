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

import static org.junit.Assert.*;
import static org.fao.geonet.kernel.rdf.QueryBuilder.builder;

import java.io.IOException;

import org.fao.geonet.kernel.AbstractThesaurusBasedTest;
import org.jdom.Namespace;
import org.junit.Test;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;
import org.openrdf.sesame.query.QueryResultsTable;

public class QueryBuilderTest extends AbstractThesaurusBasedTest {

    public QueryBuilderTest() {
        super(true);
    }

    @Test
    public void selectIds() throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        QueryResultsTable results = builder().selectId().build().rawExecute(thesaurus);

        assertEquals(1, results.getColumnCount());
        assertEquals(keywords, results.getRowCount());
    }

    @Test
    public void limit() throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        QueryResultsTable results = builder().selectId().limit(2).build().rawExecute(thesaurus);

        assertEquals(1, results.getColumnCount());
        assertEquals(2, results.getRowCount());
    }

    @Test
    public void offset() throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        QueryResultsTable noOffset = builder().selectId().build().rawExecute(thesaurus);
        QueryResultsTable offset = builder().selectId().offset(2).build().rawExecute(thesaurus);

        assertEquals(noOffset.getValue(2, 0), offset.getValue(0, 0));
    }

    @Test
    public void distinct() throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        QueryResultsTable noDistinct = builder().select(Selectors.languages(Selectors.PREF_LABEL), true).build().rawExecute(thesaurus);
        QueryResultsTable distinct = builder().select(Selectors.languages(Selectors.PREF_LABEL), true).distinct(true).build().rawExecute(thesaurus);

        assertTrue(distinct.getRowCount() < noDistinct.getRowCount());
    }


    @Test
    public void where() throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        QueryResultsTable noWhere = builder().selectId().build().rawExecute(thesaurus);
        QueryResultsTable where = builder().selectId().where(Selectors.ID.id + " LIKE \"*#1*\"").build().rawExecute(thesaurus);

        assertTrue(where.getRowCount() < noWhere.getRowCount());
    }

    @Test
    public void optional() throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        Selector path = new Selector("noSuch", "{id} gml:XX {noSuch}", Namespace.getNamespace("gml", "http://www.opengis.net/gml#"));
        QueryResultsTable requireResults = builder().selectId().select(path, true).build().rawExecute(thesaurus);
        QueryResultsTable optionResults = builder().selectId().select(path, false).build().rawExecute(thesaurus);

        assertEquals(0, requireResults.getRowCount());
        assertEquals(keywords, optionResults.getRowCount());
        assertNotNull(optionResults.getValue(0, 0));
        assertNull(optionResults.getValue(0, 1));
    }

}
