
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

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.apache.lucene.search.Filter;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.jdom.Element;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base interface for the search (Lucene or Solr).
 */
public interface ISearchManager {
    void init(ServiceConfig handlerConfig) throws Exception;

    void end() throws Exception;

    MetaSearcher newSearcher(SearcherType type, String stylesheetName) throws Exception;

    /**
     * Indexes a metadata record.
     *
     * @param forceRefreshReaders if true then block all searches until they can obtain a up-to-date reader
     */
    void index(Path schemaDir, Element metadata, String id, List<Element> moreFields,
               MetadataType metadataType, String root, boolean forceRefreshReaders)
            throws Exception;

    IndexAndTaxonomy getNewIndexReader(String preferredLang) throws IOException, InterruptedException;

    IndexAndTaxonomy getIndexReader(String preferredLang, long versionToken) throws IOException;

    boolean optimizeIndex();

    /**
     * Force the index to wait until all changes are processed and the next reader obtained will get the latest data.
     *
     * @throws IOException
     */
    void forceIndexChanges() throws IOException;

    SettingInfo getSettingInfo();

    void releaseIndexReader(IndexAndTaxonomy reader) throws InterruptedException, IOException;

    ISpatial getSpatial();

    /**
     * Rebuilds the Lucene index. If xlink or from selection parameters
     * are defined, reindex a subset of record. Otherwise reindex all records.
     *
     * @param xlinks        Search all docs with XLinks, clear the XLinks cache and index all records found.
     * @param fromSelection Reindex all records from selection.
     */
    boolean rebuildIndex(ServiceContext context,
                         boolean xlinks,
                         boolean reset,
                         boolean fromSelection) throws Exception;

    Map<String, String> getDocsChangeDate() throws Exception;

    ISODate getDocChangeDate(String mdId) throws Exception;

    Set<Integer> getDocsWithXLinks() throws Exception;

    /**
     * deletes a document.
     */
    void delete(String fld, String txt) throws Exception;

    /**
     * deletes a list of documents.
     *
     * @param fld
     * @param txts
     * @throws Exception
     */
    void delete(String fld, List<String> txts) throws Exception;

    void deleteGroup(String fld, String txt) throws Exception;

    void rescheduleOptimizer(Calendar optimizerBeginAt, int optimizerInterval) throws Exception;

    void disableOptimizer() throws Exception;

    interface ISpatial {
        Filter filter(org.apache.lucene.search.Query query, int numHits, Element filterExpr, String filterVersion)
                throws Exception;
    }
}
