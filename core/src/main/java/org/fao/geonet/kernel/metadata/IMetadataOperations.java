/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.repository.UserGroupRepository;

import com.google.common.base.Optional;

import jeeves.server.context.ServiceContext;

/**
 * trunk-core
 * 
 * @author delawen
 * 
 * 
 */
public interface IMetadataOperations {
    
    /**
     *FIXME
     * To remove when Spring autowiring works right
     * @param context
     */
    public void init(ServiceContext context);

    /**
     * Adds a permission to a group. Metadata is not reindexed.
     *
     * @param context
     * @param mdId
     * @param grpId
     * @throws Exception
     */
    public void setOperation(ServiceContext context, String mdId, String grpId,
            ReservedOperation op) throws Exception;

    /**
     * Adds a permission to a group. Metadata is not reindexed.
     *
     * @param context
     * @param mdId
     * @param grpId
     * @param opId
     * @throws Exception
     */
    public void setOperation(ServiceContext context, String mdId, String grpId,
            String opId) throws Exception;

    /**
     * Set metadata privileges.
     *
     * Administrator can set operation for any groups.
     *
     * For reserved group (ie. Internet, Intranet & Guest), user MUST be
     * reviewer of one group. For other group, if
     * "Only set privileges to user's groups" is set in catalog configuration
     * user MUST be a member of the group.
     *
     * @param context
     * @param mdId
     *            The metadata identifier
     * @param grpId
     *            The group identifier
     * @param opId
     *            The operation identifier
     *
     * @return true if the operation was set.
     * @throws Exception
     */
    public boolean setOperation(ServiceContext context, int mdId, int grpId,
            int opId) throws Exception;

    /**
     * Check that the operation has not been added and if not that it can be
     * added.
     * <ul>
     * <li>If the operation can be added then an non-empty optional is return.
     * </li>
     * <li>If it has already been added the return empty optional</li>
     * <li>If it is not permitted to be added throw exception.</li>
     * </ul>
     *
     * @param context
     * @param mdId
     * @param grpId
     * @param opId
     * @return
     */
    public Optional<OperationAllowed> getOperationAllowedToAdd(
            final ServiceContext context, final int mdId, final int grpId,
            final int opId);

    public void checkOperationPermission(ServiceContext context, int grpId,
            UserGroupRepository userGroupRepo);

    /**
     *
     * @param context
     * @param mdId
     * @param grpId
     * @param opId
     * @throws Exception
     */
    public void unsetOperation(ServiceContext context, String mdId,
            String grpId, ReservedOperation opId) throws Exception;

    /**
     *
     * @param context
     * @param mdId
     * @param grpId
     * @param opId
     * @throws Exception
     */
    public void unsetOperation(ServiceContext context, String mdId,
            String grpId, String opId) throws Exception;

    /**
     *
     * @param context
     * @param mdId
     *            metadata id
     * @param groupId
     *            group id
     * @param operId
     *            operation id
     */
    public void unsetOperation(ServiceContext context, int mdId, int groupId,
            int operId) throws Exception;

    /**
     * Unset operation without checking if user privileges allows the operation.
     * This may be useful when a user is an editor and internal operations needs
     * to update privilages for reserved group. eg.
     * {@link org.fao.geonet.kernel.metadata.DefaultStatusActions}
     *
     * @param context
     * @param mdId
     * @param groupId
     * @param operId
     * @throws Exception
     */
    public void forceUnsetOperation(ServiceContext context, int mdId,
            int groupId, int operId) throws Exception;

    /**
     * Sets VIEW and NOTIFY privileges for a metadata to a group.
     *
     * @param context
     *            service context
     * @param id
     *            metadata id
     * @param groupId
     *            group id
     * @param fullRightsForGroup
     *            TODO
     * @throws Exception
     *             hmmm
     */
    public void copyDefaultPrivForGroup(ServiceContext context, String id,
            String groupId, boolean fullRightsForGroup) throws Exception;
}
