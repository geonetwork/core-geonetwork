/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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
 
package org.fao.geonet.kernel.search.submission;

import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.search.EsSearchManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class DirectDeletionSubmitter implements IDeletionSubmitter {
    public static final DirectDeletionSubmitter INSTANCE = new DirectDeletionSubmitter();

    private DirectDeletionSubmitter() {}

    @Override
    public void submitUUIDToIndex(String uuid, EsSearchManager searchManager) throws IOException {
        EsRestClient restClient = searchManager.getClient();
        List<String> documents = Collections.singletonList(uuid);

        BulkRequest bulkRequest = restClient.buildDeleteBulkRequest(searchManager.getDefaultIndex(), documents);
        final BulkResponse bulkItemResponses = restClient.getClient().bulk(bulkRequest);

        searchManager.handleDeletionResponse(bulkItemResponses, documents);
    }

    @Override
    public void submitQueryToIndex(String query, EsSearchManager searchManager) throws IOException {
        EsRestClient restClient = searchManager.getClient();

        DeleteByQueryRequest deleteByQueryRequest = restClient.buildDeleteByQuery(searchManager.getDefaultIndex(), query);
        final DeleteByQueryResponse deleteByQueryResponse = restClient.getClient().deleteByQuery(deleteByQueryRequest);

        searchManager.handleDeletionResponse(deleteByQueryResponse, query);
    }
}
