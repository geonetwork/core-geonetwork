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

import java.util.Collection;
import java.util.List;

import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.repository.UserGroupRepository;

import com.google.common.base.Optional;

import jeeves.server.context.ServiceContext;

/**
 * Interface for hanlding record privileges
 * 
 * @author delawen
 *
 */
public interface IMetadataOperations {

    /**
     * Removes all operations stored for a metadata.
     */
    @Deprecated
    void deleteMetadataOper(ServiceContext context, String metadataId, boolean skipAllReservedGroup) throws Exception;

    /**
     * Removes all operations stored for a metadata.
     */
    void deleteMetadataOper(String metadataId, boolean skipAllReservedGroup) throws Exception;

    /**
     * Removes all operations stored for a metadata except for the operations of the groups in the exclude list.
     * Used for preventing deletion of operations for reserved and restricted groups.
     *
     * @param metadataId        Metadata identifier
     * @param groupIdsToExclude List of group ids to exclude from deletion
     */
    void deleteMetadataOper(String metadataId, List<Integer> groupIdsToExclude);

    /**
     * Adds a permission to a group. Metadata is not reindexed.
     */
    void setOperation(ServiceContext context, String mdId, String grpId, String opId) throws Exception;

    /**
     * Adds a permission to a group. Metadata is not reindexed.
     */
    void setOperation(ServiceContext context, String mdId, String grpId, ReservedOperation op) throws Exception;

    /**
     * Sets VIEW and NOTIFY privileges for a metadata to a group.
     *
     * @param context service context
     * @param id metadata id
     * @param groupId group id
     * @param fullRightsForGroup
     * @throws Exception hmmm
     */
    void copyDefaultPrivForGroup(ServiceContext context, String id, String groupId, boolean fullRightsForGroup) throws Exception;

    /**
     * Unset operation without checking if user privileges allows the operation. This may be useful when a user is an editor and internal
     * operations needs to update privilages for reserved group. eg. {@link org.fao.geonet.kernel.metadata.DefaultStatusActions}
     */
    void forceUnsetOperation(ServiceContext context, int mdId, int groupId, int operId) throws Exception;

    /**
     * Removes a privilege if the user calling this function have enough privileges.
     * 
     * This is the "safe" version of {@link #forceUnsetOperation(ServiceContext, int, int, int)}
     * 
     * @param context
     * @param mdId
     * @param groupId
     * @param operId
     * @throws Exception
     */
    void unsetOperation(ServiceContext context, int mdId, int groupId, int operId) throws Exception;

    /**
     * Set metadata privileges.
     *
     * Administrator can set operation for any groups.
     *
     * For reserved group (ie. Internet, Intranet & Guest), user MUST be reviewer of one group. For other group, if "Only set privileges to
     * user's groups" is set in catalog configuration user MUST be a member of the group.
     *
     * @param mdId The metadata identifier
     * @param grpId The group identifier
     * @param opId The operation identifier
     * @return true if the operation was set.
     */
    boolean setOperation(ServiceContext context, int mdId, int grpId, int opId) throws Exception;

    /**
     * Set metadata privileges even if the user logged in does not have privileges
     *
     * @param mdId The metadata identifier
     * @param grpId The group identifier
     * @param opId The operation identifier
     * @return true if the operation was set.
     */
    boolean forceSetOperation(ServiceContext context, int mdId, int grpId, int opId) throws Exception;

    /**
     * Check that the operation has not been added and if not that it can be added.
     * <ul>
     * <li>If the operation can be added then an non-empty optional is return.</li>
     * <li>If it has already been added the return empty optional</li>
     * <li>If it is not permitted to be added throw exception.</li>
     * </ul>
     */
    Optional<OperationAllowed> getOperationAllowedToAdd(ServiceContext context, int mdId, int grpId, int opId);

    /**
     * Check if the user calling have privileges over a group
     * 
     * @param context
     * @param grpId
     * @param userGroupRepo
     */
    void checkOperationPermission(ServiceContext context, int grpId, UserGroupRepository userGroupRepo);

    /**
     * 
     * Removes a privilege if the user calling this function have enough privileges.
     * 
     * This is the "safe" version of {@link #forceUnsetOperation(ServiceContext, int, int, int)}
     * 
     * @param context
     * @param mdId
     * @param grpId
     * @param opId
     * @throws Exception
     */
    void unsetOperation(ServiceContext context, String mdId, String grpId, ReservedOperation opId) throws Exception;

    /**
     * 
     * Removes a privilege if the user calling this function have enough privileges.
     * 
     * This is the "safe" version of {@link #forceUnsetOperation(ServiceContext, int, int, int)}
     * 
     * @param context
     * @param mdId
     * @param grpId
     * @param opId
     * @throws Exception
     */
    void unsetOperation(ServiceContext context, String mdId, String grpId, String opId) throws Exception;

    /**
     * Checks if a user owns metadata
     * 
     * @param userId
     * @return
     * @throws Exception
     */
    boolean isUserMetadataOwner(int userId) throws Exception;

    /**
     * Checks if a user exists
     * 
     * @param context
     * @param id
     * @return
     * @throws Exception
     */
    boolean existsUser(ServiceContext context, int id) throws Exception;

    /**
     * Return all operations related to one record
     * 
     * @param id
     * @return
     */
    public Collection<OperationAllowed> getAllOperations(int id);
}
