//=============================================================================
//===	Copyright (C) 2001-2011 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.datamanager;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.search.ISearchManager;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;

import jeeves.server.context.ServiceContext;

/**
 * Interface to handle all indexing operations
 *
 * @author delawen
 *
 */
public interface IMetadataIndexer {

    /**
     * This is a hopefully soon to be deprecated when no deps on context
     * 
     * @param context
     * @throws Exception
     */
    public void init(ServiceContext context, Boolean force) throws Exception;

    /**
     * Force the index to wait until all changes are processed and the next reader obtained will get the latest data.
     */
    void forceIndexChanges() throws IOException;

    /**
     * Remove the records that matches the specification
     *
     * @param specification
     * @return
     * @throws Exception
     */
    int batchDeleteMetadataAndUpdateIndex(Specification<? extends AbstractMetadata> specification) throws Exception;

    /**
     * Search for all records having XLinks (ie. indexed with _hasxlinks flag), clear the cache and reindex all records found.
     */
    void rebuildIndexXLinkedMetadata(ServiceContext context) throws Exception;

    /**
     * Reindex all records in current selection.
     */
    void rebuildIndexForSelection(ServiceContext context, String bucket, boolean clearXlink) throws Exception;

    /**
     * Index multiple metadata in a separate thread. Wait until the current transaction commits before starting threads (to make sure that
     * all metadata are committed).
     *
     * @param context context object
     * @param metadataIds the metadata ids to index
     */
    void batchIndexInThreadPool(ServiceContext context, List<?> metadataIds);

    /**
     * Is the platform currently indexing?
     *
     * @return
     */
    boolean isIndexing();

    /**
     * Index the list of records passed as parameter in order.
     *
     * @param metadataIds
     * @throws Exception
     */
    void indexMetadata(List<String> metadataIds) throws Exception;

    /**
     * Index one record defined by metadataId
     *
     * @param metadataId
     * @param forceRefreshReaders
     * @throws Exception
     */
    void indexMetadata(String metadataId, boolean forceRefreshReaders) throws Exception;
    void indexMetadataPrivileges(String uuid, int id) throws Exception;

    /**
     * Start record versioning
     *
     * @param context
     * @param id
     * @param md
     * @throws Exception
     */
    void versionMetadata(ServiceContext context, String id, Element md) throws Exception;

    /**
     * Helper function to avoid loop circular dependencies
     *
     * @param metadataUtils
     */
    void setMetadataUtils(IMetadataUtils metadataUtils);

    /**
     * Helper function to avoid loop circular dependencies
     *
     * @param baseMetadataManager
     */
    void setMetadataManager(IMetadataManager baseMetadataManager);

}
