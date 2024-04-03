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

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataStatus;

import jeeves.server.context.ServiceContext;

import java.util.List;

/**
 * Interface to handle all actions related to status of a record
 *
 * @author delawen
 *
 */
public interface IMetadataStatus {

    /**
     * Returns if the user has at least one metadata status associated
     *
     * @param userId
     * @return
     * @throws Exception
     */
    boolean isUserMetadataStatus(int userId) throws Exception;

    /**
     * If groupOwner match regular expression defined in setting metadata/workflow/draftWhenInGroup, then set status to draft to enable
     * workflow.
     */
    void activateWorkflowIfConfigured(ServiceContext context, String newId, String groupOwner) throws Exception;

    /**
     * Safely change the status if the current is compatible.
     */
    void changeCurrentStatus(Integer userId, Integer metadataId, Integer newStatus) throws Exception;

    /**
     * Set status of metadata id and do not reindex metadata id afterwards.
     *
     * @return the saved status entity object
     */
    @Deprecated
    MetadataStatus setStatusExt(ServiceContext context, int id, int status, ISODate changeDate, String changeMessage) throws Exception;

    /**
     * Set status of metadata id and reindex metadata id afterwards based on updateIndex flag
     *
     * @param status metadata status to set
     * @param updateIndex index update flag
     *
     * @return the saved status entity object
     */
    MetadataStatus setStatusExt(MetadataStatus status, boolean updateIndex) throws Exception;

    /**
     * Set status of metadata id and reindex metadata id afterwards.
     *
     * @return the saved status entity object
     */
    MetadataStatus setStatus(ServiceContext context, int id, int status, ISODate changeDate, String changeMessage) throws Exception;

    /**
     * Given a metadata id, return the name of the status of the metadata
     *
     * @param metadataId
     * @return
     * @throws Exception
     */
    String getCurrentStatus(int metadataId) throws Exception;

    /**
     * Given a metadata id, return the last status of the metadata
     *
     * @param metadataId
     * @return
     * @throws Exception
     */
    MetadataStatus getStatus(int metadataId) throws Exception;

    /**
     * Given a metadata id, return the status of the metadata
     *
     * @param metadataId
     * @return
     * @throws Exception
     */
    List<MetadataStatus> getAllStatus(int metadataId) throws Exception;
}
