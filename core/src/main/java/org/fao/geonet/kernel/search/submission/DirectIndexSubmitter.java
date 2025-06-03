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
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.search.EsSearchManager;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * An index submitter that directly and synchronously transmits new documents to the index
 */
public class DirectIndexSubmitter implements IIndexSubmitter {
    public static final DirectIndexSubmitter INSTANCE = new DirectIndexSubmitter();

    private DirectIndexSubmitter() {}

    @Override
    public void submitToIndex(String id, String jsonDocument, EsSearchManager searchManager) throws IOException {
        EsRestClient restClient = searchManager.getClient();
        Map<String, String> documents = Collections.singletonMap(id, jsonDocument);

        BulkRequest bulkRequest = restClient.buildIndexBulkRequest(searchManager.getDefaultIndex(), documents);
        final BulkResponse bulkItemResponses = restClient.getClient().bulk(bulkRequest);

        searchManager.handleIndexResponse(bulkItemResponses, documents);
    }
}
