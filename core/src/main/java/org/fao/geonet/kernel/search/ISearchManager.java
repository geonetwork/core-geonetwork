/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.google.common.collect.Multimap;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.search.submission.IDeletionSubmittor;
import org.fao.geonet.kernel.search.submission.IIndexSubmittor;
import org.jdom.Element;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Base interface for the search (Lucene or Solr).
 */
public interface ISearchManager {
    void init(boolean dropIndexFirst, Optional<List<String>> indices) throws Exception;

    void end() throws Exception;

    /**
     * Indexes a metadata record.
     *
     * @param indexSubmittor The submittor to use
     */
    void index(Path schemaDir, Element metadata, String id, Multimap<String, Object> moreFields,
               MetadataType metadataType, IIndexSubmittor indexSubmittor, IndexingMode indexingMode)
        throws Exception;


    /**
     * Rebuilds the Lucene index. If xlink or from selection parameters are defined, reindex a
     * subset of record. Otherwise reindex all records.
     *
     * @param bucket Reindex all records from selection bucket.
     */
    boolean rebuildIndex(ServiceContext context,
                         boolean reset,
                         String bucket) throws Exception;

    Map<String, String> getDocsChangeDate() throws Exception;

    ISODate getDocChangeDate(String mdId) throws Exception;

    /**
     * deletes a document by a query.
     */
    void deleteByQuery(String query, IDeletionSubmittor submittor) throws Exception;

    /**
     * deletes a document by its uuid.
     */
    void deleteByUuid(String uuid, IDeletionSubmittor submittor) throws Exception;

    boolean isIndexWritable(String indexName) throws IOException, ElasticsearchException;
}
