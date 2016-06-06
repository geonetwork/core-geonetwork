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

package org.fao.geonet.kernel.search;

import static org.junit.Assert.assertEquals;

import jeeves.server.context.ServiceContext;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.repository.statistic.SearchRequestRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Verify that SearchLogger has appropriate transaction as needed.
 *
 * Created by Jesse on 3/11/14.
 */
public class SearchLoggerTaskTest extends AbstractCoreIntegrationTest {

    @Autowired
    private SearchRequestRepository _searchRequestRepository;

    @Test
    public void testRun() throws Exception {
        final long numSearches = _searchRequestRepository.count();
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                final ServiceContext context = createServiceContext();
                final SearchLoggerTask task = context.getBean(SearchLoggerTask.class);
                Query query = new TermQuery(new Term("any", "search"));
                task.configure(context, false, "any", query, 3, Sort.RELEVANCE, null, "value");
                task.run();
            }
        });
        assertEquals(numSearches + 1, _searchRequestRepository.count());
    }
}
