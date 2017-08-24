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

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataType;
import org.jdom.Element;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 * Base interface for the search (Lucene or Solr).
 */
public interface ISearchManager {
    void init(ServiceConfig handlerConfig) throws Exception;

    void end() throws Exception;

    MetaSearcher newSearcher(String stylesheetName) throws Exception;

    /**
     * Indexes a metadata record.
     *
     * @param forceRefreshReaders if true then block all searches until they can obtain a up-to-date
     *                            reader
     */
    void index(Path schemaDir, Element metadata, String id, List<Element> moreFields,
               MetadataType metadataType, String root, boolean forceRefreshReaders)
        throws Exception;

    /**
     * Force the index to wait until all changes are processed and the next reader obtained will get
     * the latest data.
     */
    void forceIndexChanges() throws IOException;


    /**
     * Rebuilds the Lucene index. If xlink or from selection parameters are defined, reindex a
     * subset of record. Otherwise reindex all records.
     *
     * @param xlinks        Search all docs with XLinks, clear the XLinks cache and index all
     *                      records found.
     * @param bucket Reindex all records from selection bucket.
     */
    boolean rebuildIndex(ServiceContext context,
                         boolean xlinks,
                         boolean reset,
                         String bucket) throws Exception;

    Map<String, String> getDocsChangeDate() throws Exception;

    ISODate getDocChangeDate(String mdId) throws Exception;

    Set<Integer> getDocsWithXLinks() throws Exception;

    /**
     * deletes a document.
     */
    void delete(String txt) throws Exception;

    /**
     * deletes a list of documents.
     */
    void delete(List<String> txts) throws Exception;

    void rescheduleOptimizer(Calendar beginAt, int interval) throws Exception;

    void disableOptimizer() throws Exception;
}
